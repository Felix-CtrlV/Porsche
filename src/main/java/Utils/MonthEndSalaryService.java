package Utils;

import DAO.AdminAccountDAO;
import Database.DatabaseConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class MonthEndSalaryService {
    private static final Logger logger = LoggerFactory.getLogger(MonthEndSalaryService.class);
    
    /**
     * Process month-end salary payments:
     * 1. Automatically calculate and apply bonuses for employees with >= 80% attendance
     * 2. Get all active employees with their salary + bonus
     * 3. Send email notification to each employee
     * 4. Reset bonus to 0 for next month
     */
    public static void processMonthEndSalary() {
        logger.info("Starting month-end salary processing...");
        
        try {
            // First, automatically calculate and apply bonuses
            applyBonusesAutomatically();
            
            // Then process salaries with the applied bonuses
            processSalariesWithBonuses();
            
        } catch (Exception e) {
            logger.error("Error during month-end salary processing", e);
        }
    }
    
    /**
     * Automatically calculate and apply bonuses for all eligible employees (>= 80% attendance)
     */
    private static void applyBonusesAutomatically() {
        logger.info("Automatically calculating and applying bonuses...");
        
        AdminAccountDAO dao = null;
        try {
            dao = new AdminAccountDAO();
            LocalDate now = LocalDate.now();
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();
            
            // Get all active employees
            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
                String query = """
                    SELECT 
                        ui.user_id,
                        ui.user_name,
                        ui.user_role,
                        uw.salary_amount
                    FROM user_info ui
                    JOIN user_workinfo uw ON ui.user_id = uw.user_id
                    WHERE ui.user_status = 1 AND ui.user_role IN ('staff', 'manager')
                    ORDER BY ui.user_role, ui.user_name
                """;
                
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                
                int bonusAppliedCount = 0;
                int bonusSkippedCount = 0;
                
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String userName = rs.getString("user_name");
                    double salary = rs.getDouble("salary_amount");
                    
                    try {
                        // Check attendance
                        double attendancePercent = dao.getUserAttendancePercentage(userId, currentMonth, currentYear);
                        
                        if (attendancePercent < 80.0) {
                            logger.info("Skipping bonus for user {} ({}): Attendance is {}% (minimum 80% required)", 
                                userName, userId, attendancePercent >= 0 ? attendancePercent : 0.0);
                            bonusSkippedCount++;
                            continue;
                        }
                        
                        // Get target data and calculate bonus
                        int[][] targetData = dao.getTargetData(userId, currentMonth, currentYear);
                        int carAchieved = targetData[0][0];
                        int carTarget = targetData[0][1];
                        int partAchieved = targetData[1][0];
                        int partTarget = targetData[1][1];
                        
                        // Calculate bonus (same logic as in adminAccountController)
                        int carsOverTarget = Math.max(0, carAchieved - carTarget);
                        double carBonusPercent = carsOverTarget * 2.0;
                        double carBonusAmount = (salary * carBonusPercent) / 100.0;
                        
                        int partsOverTarget = Math.max(0, partAchieved - partTarget);
                        double partBonusPercent = partsOverTarget * 1.0;
                        double partBonusAmount = (salary * partBonusPercent) / 100.0;
                        
                        double totalBonus = carBonusAmount + partBonusAmount;
                        
                        // Apply bonus if > 0
                        if (totalBonus > 0) {
                            boolean applied = dao.applyBonus(userId, totalBonus);
                            if (applied) {
                                logger.info("Applied bonus ${} for user {} (ID: {}), Attendance: {}%", 
                                    totalBonus, userName, userId, attendancePercent);
                                bonusAppliedCount++;
                            } else {
                                logger.warn("Failed to apply bonus for user {} (ID: {})", userName, userId);
                                bonusSkippedCount++;
                            }
                        } else {
                            logger.info("No bonus earned for user {} (ID: {}): Targets not exceeded", userName, userId);
                            bonusSkippedCount++;
                        }
                        
                    } catch (Exception e) {
                        logger.error("Error processing bonus for user {} (ID: {})", userName, userId, e);
                        bonusSkippedCount++;
                    }
                }
                
                logger.info("Bonus application completed. Applied: {}, Skipped: {}", bonusAppliedCount, bonusSkippedCount);
                
            } catch (SQLException e) {
                logger.error("Database error during bonus application", e);
            }
            
        } catch (SQLException e) {
            logger.error("Failed to initialize AdminAccountDAO for bonus application", e);
        } finally {
            if (dao != null) {
                try {
                    dao.close();
                } catch (Exception e) {
                    logger.error("Error closing AdminAccountDAO", e);
                }
            }
        }
    }
    
    /**
     * Process salaries with the applied bonuses
     */
    private static void processSalariesWithBonuses() {
        logger.info("Processing salaries with bonuses...");
        
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Get all active employees with their salary and bonus
            String query = """
                SELECT 
                    ui.user_id,
                    ui.user_name,
                    ui.user_email,
                    ui.user_role,
                    uw.salary_amount,
                    COALESCE(uw.bonus, 0) as bonus_amount
                FROM user_info ui
                JOIN user_workinfo uw ON ui.user_id = uw.user_id
                WHERE ui.user_status = 1
                ORDER BY ui.user_role, ui.user_name
            """;
            
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            int successCount = 0;
            int failCount = 0;
            
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("user_name");
                String userEmail = rs.getString("user_email");
                String userRole = rs.getString("user_role");
                double salary = rs.getDouble("salary_amount");
                double bonus = rs.getDouble("bonus_amount");
                double total = salary + bonus;
                
                try {
                    // Send email notification
                    sendSalaryNotification(userName, userEmail, userRole, salary, bonus, total);
                    
                    // Reset bonus to 0 for next month
                    resetBonus(conn, userId);
                    
                    logger.info("Processed salary for user: {} (ID: {}), Total: ${}", userName, userId, total);
                    successCount++;
                    
                } catch (Exception e) {
                    logger.error("Failed to process salary for user: {} (ID: {})", userName, userId, e);
                    failCount++;
                }
            }
            
            logger.info("Month-end salary processing completed. Success: {}, Failed: {}", successCount, failCount);
            
        } catch (SQLException e) {
            logger.error("Database error during month-end salary processing", e);
        }
    }
    
    /**
     * Send salary notification email to employee
     */
    private static void sendSalaryNotification(String userName, String email, String role, 
                                               double salary, double bonus, double total) throws Exception {
        
        LocalDate now = LocalDate.now();
        String monthYear = now.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        
        // Email configuration (using Gmail SMTP)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        
        // Email credentials for Porsche system
        final String senderEmail = "porscheemailingsys@gmail.com";
        final String senderPassword = "kcsvkdbzgzqtplod";
        
        jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(senderEmail, senderPassword);
            }
        });
        
        jakarta.mail.Message message = new jakarta.mail.internet.MimeMessage(mailSession);
        message.setFrom(new InternetAddress(senderEmail, "Porsche HR System"));
        message.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("💰 Salary Payment Notification - " + monthYear);
        
        // HTML email content
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .salary-box { background: white; padding: 20px; margin: 20px 0; 
                                  border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .amount { font-size: 24px; font-weight: bold; color: #10b981; }
                    .total { font-size: 32px; font-weight: bold; color: #667eea; 
                            padding: 20px; background: #f0f4ff; border-radius: 8px; text-align: center; }
                    .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
                    table { width: 100%%; border-collapse: collapse; }
                    td { padding: 10px; border-bottom: 1px solid #e5e7eb; }
                    .label { font-weight: bold; color: #6b7280; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏎️ Porsche Salary Payment</h1>
                        <p>%s</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Your salary for <strong>%s</strong> has been processed successfully.</p>
                        
                        <div class="salary-box">
                            <table>
                                <tr>
                                    <td class="label">Position:</td>
                                    <td><strong>%s</strong></td>
                                </tr>
                                <tr>
                                    <td class="label">Base Salary:</td>
                                    <td class="amount">$ %.2f</td>
                                </tr>
                                <tr>
                                    <td class="label">Performance Bonus:</td>
                                    <td class="amount">$ %.2f</td>
                                </tr>
                            </table>
                        </div>
                        
                        <div class="total">
                            Total Payment: $ %.2f
                        </div>
                        
                        <p style="margin-top: 20px;">
                            %s
                        </p>
                        
                        <p>Thank you for your dedication and hard work!</p>
                        
                        <div class="footer">
                            <p>This is an automated message from Porsche HR System.</p>
                            <p>© 2025 Porsche. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
            monthYear,
            userName,
            monthYear,
            role,
            salary,
            bonus,
            total,
            bonus > 0 ? "🎉 Congratulations on earning a performance bonus this month!" 
                      : "Keep up the great work to earn performance bonuses!"
        );
        
        message.setContent(htmlContent, "text/html; charset=utf-8");
        
        // Send email
        Transport.send(message);
        logger.info("Salary notification email sent to: {} ({})", userName, email);
    }
    
    /**
     * Reset bonus to 0 after payment
     */
    private static void resetBonus(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE user_workinfo SET bonus = 0 WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Check if today is the last day of the month
     */
    public static boolean isLastDayOfMonth() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return today.getMonth() != tomorrow.getMonth();
    }
    
    /**
     * Main method for testing or manual execution
     */
    public static void main(String[] args) {
        System.out.println("Starting month-end salary processing...");
        processMonthEndSalary();
        System.out.println("Processing completed!");
    }
}
