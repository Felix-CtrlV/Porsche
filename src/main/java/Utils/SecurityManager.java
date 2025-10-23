package Utils;

import Database.DatabaseConnectionManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityManager {
    private static SecurityManager instance;
    private static final int MAX_ATTEMPTS = 5;
    private static final int UNLOCK_TOKEN_VALIDITY_HOURS = 24;
    
    private SecurityManager() {}
    
    public static SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }
    
    /**
     * Records a failed password attempt for a user
     * @param userId The user ID
     * @param userType "admin" or "manager"
     * @return true if account should be locked, false otherwise
     */
    public boolean recordFailedAttempt(int userId, String userType) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // First, get current attempt count
            String selectSql = "SELECT attempt_count, is_locked FROM security_attempts WHERE user_id = ? AND user_type = ?";
            int currentAttempts = 0;
            boolean isAlreadyLocked = false;
            
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, userId);
                selectStmt.setString(2, userType);
                ResultSet rs = selectStmt.executeQuery();
                
                if (rs.next()) {
                    currentAttempts = rs.getInt("attempt_count");
                    isAlreadyLocked = rs.getBoolean("is_locked");
                }
            }
            
            if (isAlreadyLocked) {
                return true; // Already locked
            }
            
            currentAttempts++;
            boolean shouldLock = currentAttempts >= MAX_ATTEMPTS;
            
            // Update or insert attempt record
            String upsertSql = """
                INSERT INTO security_attempts (user_id, user_type, attempt_count, is_locked, locked_at, unlock_token, unlock_token_expires) 
                VALUES (?, ?, ?, ?, ?, ?, ?) 
                ON DUPLICATE KEY UPDATE 
                    attempt_count = ?, 
                    is_locked = ?, 
                    locked_at = IF(? = TRUE, NOW(), locked_at),
                    unlock_token = IF(? = TRUE, ?, unlock_token),
                    unlock_token_expires = IF(? = TRUE, DATE_ADD(NOW(), INTERVAL ? HOUR), unlock_token_expires),
                    last_attempt = NOW()
                """;
            
            String unlockToken = shouldLock ? generateUnlockToken() : null;
            
            try (PreparedStatement upsertStmt = conn.prepareStatement(upsertSql)) {
                // Insert values
                upsertStmt.setInt(1, userId);
                upsertStmt.setString(2, userType);
                upsertStmt.setInt(3, currentAttempts);
                upsertStmt.setBoolean(4, shouldLock);
                upsertStmt.setTimestamp(5, shouldLock ? Timestamp.valueOf(LocalDateTime.now()) : null);
                upsertStmt.setString(6, unlockToken);
                upsertStmt.setTimestamp(7, shouldLock ? Timestamp.valueOf(LocalDateTime.now().plusHours(UNLOCK_TOKEN_VALIDITY_HOURS)) : null);
                
                // Update values
                upsertStmt.setInt(8, currentAttempts);
                upsertStmt.setBoolean(9, shouldLock);
                upsertStmt.setBoolean(10, shouldLock);
                upsertStmt.setBoolean(11, shouldLock);
                upsertStmt.setString(12, unlockToken);
                upsertStmt.setBoolean(13, shouldLock);
                upsertStmt.setInt(14, UNLOCK_TOKEN_VALIDITY_HOURS);
                
                upsertStmt.executeUpdate();
            }
            
            // Log the security event
            logSecurityEvent(userId, userType, shouldLock ? "account_locked" : "failed_attempt", null);
            
            if (shouldLock) {
                logSecurityEvent(userId, userType, "brute_force_detected", null);
            }
            
            return shouldLock;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Checks if a user account is currently locked
     */
    public boolean isAccountLocked(int userId, String userType) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT is_locked FROM security_attempts WHERE user_id = ? AND user_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getBoolean("is_locked");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Resets failed attempts for a user (called on successful login)
     */
    public void resetAttempts(int userId, String userType) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = """
                UPDATE security_attempts 
                SET attempt_count = 0, is_locked = FALSE, locked_at = NULL, 
                    unlock_token = NULL, unlock_token_expires = NULL 
                WHERE user_id = ? AND user_type = ?
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Unlocks an account using the unlock token
     */
    public boolean unlockAccount(String unlockToken) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = """
                UPDATE security_attempts 
                SET is_locked = FALSE, attempt_count = 0, locked_at = NULL, 
                    unlock_token = NULL, unlock_token_expires = NULL 
                WHERE unlock_token = ? AND unlock_token_expires > NOW()
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, unlockToken);
                int updated = stmt.executeUpdate();
                
                if (updated > 0) {
                    // Log unlock event
                    getUserByUnlockToken(unlockToken, (userId, userType) -> {
                        logSecurityEvent(userId, userType, "account_unlocked", null);
                    });
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Gets the unlock token for a locked user
     */
    public String getUnlockToken(int userId, String userType) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT unlock_token FROM security_attempts WHERE user_id = ? AND user_type = ? AND is_locked = TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getString("unlock_token");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Gets remaining attempts before lockout
     */
    public int getRemainingAttempts(int userId, String userType) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT attempt_count FROM security_attempts WHERE user_id = ? AND user_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int attempts = rs.getInt("attempt_count");
                    return Math.max(0, MAX_ATTEMPTS - attempts);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return MAX_ATTEMPTS;
    }
    
    private String generateUnlockToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    private void logSecurityEvent(int userId, String userType, String eventType, String additionalData) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = """
                INSERT INTO security_logs (user_id, user_type, event_type, additional_data) 
                VALUES (?, ?, ?, ?)
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType);
                stmt.setString(3, eventType);
                stmt.setString(4, additionalData);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void getUserByUnlockToken(String unlockToken, UserCallback callback) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT user_id, user_type FROM security_attempts WHERE unlock_token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, unlockToken);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    callback.onUser(rs.getInt("user_id"), rs.getString("user_type"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FunctionalInterface
    private interface UserCallback {
        void onUser(int userId, String userType);
    }
}
