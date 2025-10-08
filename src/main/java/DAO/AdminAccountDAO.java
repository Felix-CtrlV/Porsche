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
            SELECT user_id, user_name, user_phone, user_email, user_address, dob, start_date, end_date, user_status
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
                            rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null
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

    public int[][] getTargetData(int staffId, int month, int year) throws SQLException {
        String sql = "CALL targetviewchart(?,?,?)";
        
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, staffId);
            cs.setInt(2, month);
            cs.setInt(3, year);
            
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    // Returns: target_car, target_part, achieve_car, achieve_part
                    int targetCar = rs.getInt(1);
                    int targetPart = rs.getInt(2);
                    int achieveCar = rs.getInt(3);
                    int achievePart = rs.getInt(4);
                    
                    return new int[][]{{achieveCar, targetCar}, {achievePart, targetPart}};
                }
            }
        }
        return new int[][]{{0, 0}, {0, 0}};
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
