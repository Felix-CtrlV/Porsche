package Utils;

import Database.DatabaseConnectionManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.InetAddress;
import java.net.UnknownHostException;
// Imports for future HTTP-based email service integration
// import java.io.IOException;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.net.URI;

public class SecurityEmailService {
    private static SecurityEmailService instance;
    // Email configuration (for future use with actual email service)
    // private static final String EMAIL_USERNAME = "your-email@gmail.com"; // Configure this
    
    private SecurityEmailService() {}
    
    public static SecurityEmailService getInstance() {
        if (instance == null) {
            instance = new SecurityEmailService();
        }
        return instance;
    }
    
    /**
     * Sends brute force alert emails to the affected user and admin (ASYNC)
     */
    public void sendBruteForceAlert(int userId, String userType) {
        // Run email sending asynchronously to avoid blocking UI
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                System.out.println("SECURITY ALERT TRIGGERED for userId=" + userId + ", userType=" + userType);
                
                UserInfo userInfo = getUserInfo(userId, userType);
                if (userInfo == null) {
                    System.err.println("FAILED: Could not get user info for userId=" + userId + ", userType=" + userType);
                    return;
                }
                
                System.out.println("User info retrieved: " + userInfo.username + " (" + userInfo.email + ")");
                
                String unlockToken = SecurityManager.getInstance().getUnlockToken(userId, userType);
                String unlockUrl = getUnlockUrl(unlockToken);
                
                // Send email to the user
                String userSubject = "Security Alert: Account Temporarily Locked";
                String userEmailContent = createUserEmailTemplate(userInfo, unlockUrl);
                System.out.println("Sending user email to: " + userInfo.email);
                sendEmail(userInfo.email, userSubject, userEmailContent);
                
                // Send notification to admin
                String adminEmail = getAdminEmail();
                if (adminEmail != null) {
                    String adminSubject = "Security Alert: Brute Force Attack Detected";
                    String adminEmailContent = createAdminEmailTemplate(userInfo);
                    System.out.println("Sending admin email to: " + adminEmail);
                    sendEmail(adminEmail, adminSubject, adminEmailContent);
                } else {
                    System.err.println("WARNING: No admin email found!");
                }
                
            } catch (Exception e) {
                System.err.println("ERROR in sendBruteForceAlert: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private String createUserEmailTemplate(UserInfo userInfo, String unlockUrl) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Security Alert</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: white; margin: 0; font-size: 24px; }
                    .content { padding: 30px; }
                    .alert-box { background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .alert-icon { font-size: 48px; text-align: center; margin-bottom: 15px; }
                    .unlock-button { display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; text-align: center; }
                    .unlock-button:hover { opacity: 0.9; }
                    .info-table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                    .info-table td { padding: 10px; border-bottom: 1px solid #e5e7eb; }
                    .info-table td:first-child { font-weight: bold; color: #374151; width: 30%%; }
                    .footer { background-color: #f9fafb; padding: 20px; text-align: center; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔒 Security Alert</h1>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <div class="alert-icon">🚨</div>
                            <h2 style="color: #dc2626; text-align: center; margin: 0 0 15px 0;">Account Temporarily Locked</h2>
                            <p style="text-align: center; color: #374151; margin: 0;">
                                Your account has been temporarily locked due to multiple failed login attempts.
                            </p>
                        </div>
                        
                        <h3>What happened?</h3>
                        <p>We detected 5 consecutive failed password attempts on your account. As a security measure, we've temporarily locked your account to protect it from unauthorized access.</p>
                        
                        <table class="info-table">
                            <tr>
                                <td>Account:</td>
                                <td>%s (%s)</td>
                            </tr>
                            <tr>
                                <td>Time:</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>Status:</td>
                                <td style="color: #dc2626; font-weight: bold;">🔒 Locked</td>
                            </tr>
                        </table>
                        
                        <h3>Unlock Your Account</h3>
                        <p>If this was you, click the button below to unlock your account immediately:</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="unlock-button">🔓 Unlock My Account</a>
                        </div>
                        
                        <p style="font-size: 14px; color: #6b7280; margin-top: 20px;">
                            <strong>Note:</strong> This unlock link will expire in 24 hours. If you didn't attempt to log in, please contact your administrator immediately.
                        </p>
                        
                        <div style="background-color: #f0f9ff; border: 1px solid #bae6fd; border-radius: 8px; padding: 15px; margin: 20px 0;">
                            <h4 style="margin: 0 0 10px 0; color: #0369a1;">💡 Security Tips:</h4>
                            <ul style="margin: 0; padding-left: 20px; color: #374151;">
                                <li>Always use a strong, unique password</li>
                                <li>Never share your login credentials</li>
                                <li>Report suspicious activity immediately</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>This is an automated security notification from Porsche Management System.</p>
                        <p>If you have any questions, please contact your system administrator.</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            userInfo.username, userInfo.userType.toUpperCase(), 
            timestamp, unlockUrl);
    }
    
    private String createAdminEmailTemplate(UserInfo userInfo) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Security Alert - Admin Notification</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; }
                    .header { background: linear-gradient(135deg, #dc2626 0%%, #991b1b 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: white; margin: 0; font-size: 24px; }
                    .content { padding: 30px; }
                    .alert-box { background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .alert-icon { font-size: 48px; text-align: center; margin-bottom: 15px; }
                    .info-table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                    .info-table td { padding: 10px; border-bottom: 1px solid #e5e7eb; }
                    .info-table td:first-child { font-weight: bold; color: #374151; width: 30%%; }
                    .footer { background-color: #f9fafb; padding: 20px; text-align: center; color: #6b7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 Security Alert</h1>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <div class="alert-icon">⚠️</div>
                            <h2 style="color: #dc2626; text-align: center; margin: 0 0 15px 0;">Brute Force Attack Detected</h2>
                            <p style="text-align: center; color: #374151; margin: 0;">
                                A user account has been automatically locked due to multiple failed login attempts.
                            </p>
                        </div>
                        
                        <h3>Incident Details</h3>
                        <table class="info-table">
                            <tr>
                                <td>Affected User:</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>User Type:</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>Email:</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>Time Detected:</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>Failed Attempts:</td>
                                <td style="color: #dc2626; font-weight: bold;">5 (Maximum reached)</td>
                            </tr>
                            <tr>
                                <td>Action Taken:</td>
                                <td style="color: #dc2626; font-weight: bold;">Account Locked</td>
                            </tr>
                        </table>
                        
                        <h3>Automatic Response</h3>
                        <ul>
                            <li>✅ User account has been automatically locked</li>
                            <li>✅ Security event has been logged</li>
                            <li>✅ User has been notified via email with unlock option</li>
                            <li>✅ Admin notification sent (this email)</li>
                        </ul>
                        
                        <div style="background-color: #fef3c7; border: 1px solid #fbbf24; border-radius: 8px; padding: 15px; margin: 20px 0;">
                            <h4 style="margin: 0 0 10px 0; color: #92400e;">📋 Recommended Actions:</h4>
                            <ul style="margin: 0; padding-left: 20px; color: #374151;">
                                <li>Monitor for additional suspicious activity</li>
                                <li>Contact the user to verify the login attempts</li>
                                <li>Review security logs for patterns</li>
                            </ul>
                        </div>
                        
                        <p style="font-size: 14px; color: #6b7280;">
                            <strong>Note:</strong> The user can unlock their account using the secure link sent to their email. 
                            The unlock link will expire in 24 hours for security purposes.
                        </p>
                    </div>
                    <div class="footer">
                        <p>This is an automated security notification from Porsche Management System.</p>
                        <p>Security alerts are sent to all system administrators.</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            userInfo.username, userInfo.userType.toUpperCase(), 
            userInfo.email, timestamp);
    }
    
    private void sendEmail(String toEmail, String subject, String htmlContent) {
        // Use the same working email system as OTP
        String[] smtpConfigs = {
            // Gmail with app password (same as WorkingEmailService)
            "smtp.gmail.com:587:porscheemailingsys@gmail.com:kcsvkdbzgzqtplod",
            // Gmail SSL fallback
            "smtp.gmail.com:465:porscheemailingsys@gmail.com:kcsvkdbzgzqtplod"
        };
        
        boolean emailSent = false;
        for (String config : smtpConfigs) {
            String[] parts = config.split(":");
            if (trySendSecurityEmail(toEmail, subject, htmlContent, parts[0], parts[1], parts[2], parts[3])) {
                emailSent = true;
                break;
            }
        }
        
        if (!emailSent) {
            // Fallback: Show security alert popup
            showSecurityAlertPopup(toEmail, subject);
        }
    }
    
    private boolean trySendSecurityEmail(String toEmail, String subject, String htmlContent, String host, String port, String username, String password) {
        try {
            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.ssl.checkserveridentity", "false");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            
            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, new jakarta.mail.Authenticator() {
                @Override
                protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new jakarta.mail.PasswordAuthentication(username, password);
                }
            });
            
            jakarta.mail.Message message = new jakarta.mail.internet.MimeMessage(session);
            message.setFrom(new jakarta.mail.internet.InternetAddress(username, "Porsche Security System"));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO, jakarta.mail.internet.InternetAddress.parse(toEmail));
            message.setSubject(subject);
            
            // Set HTML content
            jakarta.mail.internet.MimeBodyPart htmlPart = new jakarta.mail.internet.MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            
            jakarta.mail.internet.MimeMultipart multipart = new jakarta.mail.internet.MimeMultipart();
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);
            
            jakarta.mail.Transport.send(message);
            System.out.println("✅ SECURITY EMAIL SENT SUCCESSFULLY TO: " + toEmail + " via " + host + ":" + port);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send security email via " + host + ":" + port + " - " + e.getMessage());
            return false;
        }
    }
    
    private void showSecurityAlertPopup(String toEmail, String subject) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Security Alert");
            alert.setHeaderText("Account Security Notice");
            alert.setContentText("Security alert email could not be sent to: " + toEmail + 
                                "\n\nSubject: " + subject + 
                                "\n\nThe account has been locked for security. Please contact your administrator.");
            alert.showAndWait();
        });
    }
    
    // Example method for SendGrid integration (commented out)
    /*
    private void sendEmailViaSendGrid(String toEmail, String subject, String htmlContent) throws IOException, InterruptedException {
        String apiKey = "YOUR_SENDGRID_API_KEY";
        String requestBody = String.format("""
            {
                "personalizations": [{
                    "to": [{"email": "%s"}]
                }],
                "from": {"email": "%s"},
                "subject": "%s",
                "content": [{
                    "type": "text/html",
                    "value": "%s"
                }]
            }
            """, toEmail, EMAIL_USERNAME, subject, htmlContent.replace("\"", "\\\""));
            
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 202) {
            throw new IOException("Failed to send email: " + response.body());
        }
    }
    */
    
    private String getUnlockUrl(String unlockToken) {
        try {
            // Use the local-only unlock server
            LocalUnlockServer localServer = LocalUnlockServer.getInstance();
            return localServer.getUnlockURL(unlockToken);
        } catch (Exception e) {
            e.printStackTrace();
            return "http://localhost:7777/unlock?token=" + unlockToken;
        }
    }
    
    private String getServerHost() {
        try {
            // Try to get the actual network IP address
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();
            
            // If it's a local address, try to get a better one
            if (hostAddress.startsWith("127.") || hostAddress.startsWith("0.")) {
                // You might want to configure this manually or use a service discovery mechanism
                return "your-server-ip"; // Configure this with your actual server IP
            }
            
            return hostAddress;
        } catch (UnknownHostException e) {
            return "localhost"; // Fallback
        }
    }
    
    private UserInfo getUserInfo(int userId, String userType) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Use user_info table which contains both admin and manager data
            String sql = "SELECT user_name, user_email, user_role FROM user_info WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String userName = rs.getString("user_name");
                    String userEmail = rs.getString("user_email");
                    String userRole = rs.getString("user_role");
                    
                    // Debug logging
                    System.out.println("🔍 DEBUG - getUserInfo for userId=" + userId + ", userType=" + userType);
                    System.out.println("   Found user: " + userName + ", email: " + userEmail + ", role: " + userRole);
                    
                    // Check if email is null or empty
                    if (userEmail == null || userEmail.trim().isEmpty()) {
                        System.err.println("❌ WARNING: User " + userName + " (ID: " + userId + ") has no email address!");
                        return null;
                    }
                    
                    return new UserInfo(
                        userId,
                        userName,
                        userEmail,
                        userType
                    );
                } else {
                    System.err.println("❌ WARNING: No user found with ID: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error in getUserInfo: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    private String getAdminEmail() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Get the first admin user's email from user_info table
            String sql = "SELECT user_email FROM user_info WHERE user_role = 'Admin' AND user_status = TRUE ORDER BY user_id LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("user_email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static class UserInfo {
        final int userId;
        final String username;
        final String email;
        final String userType;
        
        UserInfo(int userId, String username, String email, String userType) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.userType = userType;
        }
    }
}
