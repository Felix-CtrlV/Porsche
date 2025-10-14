package DAO;

import Database.DatabaseConnectionManager;
import Model.user;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminAccountDAO {
    private static final Logger logger = LoggerFactory.getLogger(AdminAccountDAO.class);
    private final Connection conn;

    public AdminAccountDAO() throws SQLException {
        this.conn = DatabaseConnectionManager.getInstance().getConnection();
        logger.debug("AdminAccountDAO initialized with connection from pool");
    }

    public List<user> getStaffCards(String status, String role) throws SQLException {
        String sql = """
            SELECT user_id, user_name, user_phone, user_email, user_address, dob, start_date, end_date, user_status, reason
            FROM user_info
            WHERE user_role = ? AND user_status = ?
            ORDER BY user_id
        """;

        List<user> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.toLowerCase());
            ps.setInt(2, status.equalsIgnoreCase("Active") ? 1 : 0);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new user(
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getString("user_phone"),
                            rs.getString("user_email"),
                            rs.getString("user_address"),
                            rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null,
                            rs.getInt("user_status") == 1 ? "Active" : "Inactive",
                            rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                            rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                            rs.getString("reason")
                    ));
                }
            }
        }
        return list;
    }

    public List<Double> getWeeklySales(int staffId, int month, int year) throws SQLException {
        List<Double> weeks = new ArrayList<>(List.of(0.0, 0.0, 0.0, 0.0));
        String sql = """
            SELECT 
              CASE 
                WHEN DAY(order_date) BETWEEN 1 AND 7 THEN 1
                WHEN DAY(order_date) BETWEEN 8 AND 14 THEN 2
                WHEN DAY(order_date) BETWEEN 15 AND 21 THEN 3
                ELSE 4 
              END AS wk, 
              SUM(paid_amount) AS total
            FROM orders
            WHERE (? = 0 OR user_id = ?) 
              AND MONTH(order_date) = ? 
              AND YEAR(order_date) = ?
            GROUP BY wk
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setInt(2, staffId);
            ps.setInt(3, month);
            ps.setInt(4, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    weeks.set(rs.getInt("wk") - 1, rs.getDouble("total"));
                }
            }
        }
        return weeks;
    }

    public double getMonthlyAttendance(int staffId, int month, int year) throws SQLException {
        String sql = "CALL getMonthlyAttendance(?,?,?)";
        
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, staffId);
            cs.setInt(2, month);
            cs.setInt(3, year);
            
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    // Returns: present_day, absent_day, total_day, attendance_percentage
                    return rs.getDouble(4); // attendance_percentage
                }
            }
        }
        return 0.0;
    }

    public int[][] getTargetData(int userId, int month, int year) throws SQLException {
        // Check if user is a manager by checking if they have staff reporting to them
        if (isManager(userId)) {
            return getManagerTargetData(userId, month, year);
        } else {
            return getStaffTargetData(userId, month, year);
        }
    }
    
    private boolean isManager(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_workinfo WHERE manager = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Has staff reporting to them
                }
            }
        }
        return false;
    }
    
    private int[][] getStaffTargetData(int staffId, int month, int year) throws SQLException {
        // Query user_target table with new schema format for individual staff
        String sql = """
            SELECT target, achieve 
            FROM user_target 
            WHERE user_id = ? 
            AND YEAR(effective_date) = ? 
            AND MONTH(effective_date) = ?
            ORDER BY effective_date DESC
            LIMIT 1
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String targetStr = rs.getString("target");
                    String achieveStr = rs.getString("achieve");
                    
                    // Parse target format: "cars-10,parts-50"
                    int[] targetData = parseTargetString(targetStr);
                    int[] achieveData = parseTargetString(achieveStr);
                    
                    // Return: [[achieveCar, targetCar], [achievePart, targetPart]]
                    return new int[][]{{achieveData[0], targetData[0]}, {achieveData[1], targetData[1]}};
                }
            }
        }
        return new int[][]{{0, 0}, {0, 0}};
    }
    
    private int[][] getManagerTargetData(int managerId, int month, int year) throws SQLException {
        // For managers, get their own target and combine all staff achievements
        String managerTargetSql = """
            SELECT target 
            FROM user_target 
            WHERE user_id = ? 
            AND YEAR(effective_date) = ? 
            AND MONTH(effective_date) = ?
            ORDER BY effective_date DESC
            LIMIT 1
        """;
        
        String staffAchievementSql = """
            SELECT ut.achieve
            FROM user_target ut
            INNER JOIN user_workinfo uw ON ut.user_id = uw.user_id
            WHERE uw.manager = ?
            AND YEAR(ut.effective_date) = ?
            AND MONTH(ut.effective_date) = ?
        """;
        
        int[] managerTarget = new int[]{0, 0};
        int totalCarsSold = 0;
        int totalPartsSold = 0;
        
        // Get manager's target
        try (PreparedStatement ps = conn.prepareStatement(managerTargetSql)) {
            ps.setInt(1, managerId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String targetStr = rs.getString("target");
                    managerTarget = parseTargetString(targetStr);
                }
            }
        }
        
        // Get combined achievements of all staff under this manager
        try (PreparedStatement ps = conn.prepareStatement(staffAchievementSql)) {
            ps.setInt(1, managerId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String achieveStr = rs.getString("achieve");
                    int[] staffAchieve = parseTargetString(achieveStr);
                    totalCarsSold += staffAchieve[0];
                    totalPartsSold += staffAchieve[1];
                }
            }
        }
        
        // Return: [[achieveCar, targetCar], [achievePart, targetPart]]
        return new int[][]{{totalCarsSold, managerTarget[0]}, {totalPartsSold, managerTarget[1]}};
    }
    
    private int[] parseTargetString(String targetStr) {
        if (targetStr == null || targetStr.isEmpty()) {
            return new int[]{0, 0};
        }
        
        try {
            // Format: "cars-10,parts-50"
            String[] parts = targetStr.split(",");
            int cars = 0;
            int partCount = 0;
            
            for (String part : parts) {
                String[] keyValue = part.trim().split("-");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    int value = Integer.parseInt(keyValue[1].trim());
                    
                    if (key.equalsIgnoreCase("cars")) {
                        cars = value;
                    } else if (key.equalsIgnoreCase("parts")) {
                        partCount = value;
                    }
                }
            }
            
            return new int[]{cars, partCount};
        } catch (Exception e) {
            logger.error("Failed to parse target string: " + targetStr, e);
            return new int[]{0, 0};
        }
    }

    public List<user> getStaffUnderManager(int managerId, String status) throws SQLException {
        // Use direct SQL query instead of stored procedure to ensure we get the reason column
        String sql = """
            SELECT ui.user_id, ui.user_name, ui.user_phone, ui.user_email, ui.user_address, 
                   ui.dob, ui.start_date, ui.end_date, ui.user_status, ui.reason
            FROM user_info ui
            INNER JOIN user_workinfo uw ON ui.user_id = uw.user_id
            WHERE uw.manager = ? 
            AND ui.user_status = ?
            ORDER BY ui.user_id
        """;
        
        List<user> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            ps.setInt(2, status.equalsIgnoreCase("Active") ? 1 : 0);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new user(
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getString("user_phone"),
                            rs.getString("user_email"),
                            rs.getString("user_address"),
                            rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null,
                            rs.getInt("user_status") == 1 ? "Active" : "Inactive",
                            rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                            rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                            rs.getString("reason")
                    ));
                }
            }
        }
        return list;
    }

    public double getCurrentSalary(int userId) throws SQLException {
        String sql = "SELECT salary_amount FROM user_workinfo WHERE user_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("salary_amount");
                }
            }
        }
        return 0.0;
    }

    public boolean updateSalary(int userId, double newSalary) throws SQLException {
        String sql = "UPDATE user_workinfo SET salary_amount = ? WHERE user_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newSalary);
            ps.setInt(2, userId);
            
            int rowsAffected = ps.executeUpdate();
            logger.info("Updated salary for user ID: {} to {}", userId, newSalary);
            return rowsAffected > 0;
        }
    }

    public boolean applyBonus(int userId, double bonusAmount) throws SQLException {
        // Store bonus in bonus column (will be paid at month-end)
        String sql = "UPDATE user_workinfo SET bonus = bonus + ? WHERE user_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, bonusAmount);
            ps.setInt(2, userId);
            
            int rowsAffected = ps.executeUpdate();
            logger.info("Applied bonus for user ID: {} amount: {} (stored in bonus column)", userId, bonusAmount);
            return rowsAffected > 0;
        }
    }

    public boolean terminateEmployee(int userId, String reason) throws SQLException {
        String updateUserSql = "UPDATE user_info SET user_status = 0, end_date = CURDATE(), reason = ? WHERE user_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(updateUserSql)) {
            ps.setString(1, reason);
            ps.setInt(2, userId);
            
            int rowsAffected = ps.executeUpdate();
            logger.info("Terminated employee ID: {} with reason: {}", userId, reason);
            return rowsAffected > 0;
        }
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                logger.debug("Connection returned to pool");
            }
        } catch (SQLException e) {
            logger.error("Error closing connection", e);
        }
    }
}
