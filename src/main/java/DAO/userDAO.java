package DAO;

import Model.user;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class userDAO {

    private static Connection con;

    public userDAO(Connection con){
        this.con = con;
    }

    public static List<user> accountData() throws SQLException{
        List<user> users = new ArrayList<>();
        String sql = "select user_id, user_name, user_phone, user_email, user_address, dob, user_status from user_info order by user_status desc";
        PreparedStatement get = con.prepareStatement(sql);
        ResultSet rs = get.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String phone = rs.getString(3);
            String email = rs.getString(4);
            String address = rs.getString(5);
            String dob = rs.getString(6);
            String status = rs.getInt(7) == 1 ? "active" : "inactive";
            user u = new user(id, name, phone, email, address, LocalDate.parse(dob), status);
            users.add(u);
        }
        return users;
    }

    /**
     * Get user by username or email for forgot password functionality
     * @param usernameOrEmail Username or email to search for
     * @return User data if found, null otherwise
     */
    public static user getUserByUsernameOrEmail(String usernameOrEmail) throws SQLException {
        String sql = "SELECT user_id, user_name, user_email FROM user_info WHERE user_name = ? OR user_email = ?";
        PreparedStatement ps = userDAO.con.prepareStatement(sql);
        ps.setString(1, usernameOrEmail);
        ps.setString(2, usernameOrEmail);
        
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int id = rs.getInt("user_id");
            String name = rs.getString("user_name");
            String email = rs.getString("user_email");
            // Create a minimal user object with required fields
            user u = new user();
            u.setId(id);
            u.setUsername(name);
            u.setEmail(email);
            return u;
        }
        return null;
    }

    /**
     * Update password for a user (with SHA-2 hashing)
     * @param userId User ID to update
     * @param newPassword New password (will be hashed by MySQL using SHA-2)
     * @return true if update successful, false otherwise
     */
    public static boolean updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE user_info SET password = SHA2(?,256) WHERE user_id = ?";
        PreparedStatement ps = userDAO.con.prepareStatement(sql);
        
        // MySQL will hash the password using SHA-2 (256-bit)
        ps.setString(1, newPassword);
        ps.setInt(2, userId);
        
        int rowsAffected = ps.executeUpdate();
        return rowsAffected > 0;
    }

}
