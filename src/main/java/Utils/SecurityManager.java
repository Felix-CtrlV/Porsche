package Utils;

import Database.DatabaseConnectionManager;
import javafx.concurrent.Task;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class SecurityManager {
    private static SecurityManager instance;
    private static final int MAX_ATTEMPTS = 5;
    private static final int UNLOCK_TOKEN_VALIDITY_HOURS = 24;
    
    // Cache for security attempts to reduce database calls
    private final ConcurrentHashMap<String, SecurityAttemptCache> attemptCache = new ConcurrentHashMap<>();
    
    // Cache entry class
    private static class SecurityAttemptCache {
        int attemptCount;
        boolean isLocked;
        long lastUpdate;
        
        SecurityAttemptCache(int attemptCount, boolean isLocked) {
            this.attemptCount = attemptCount;
            this.isLocked = isLocked;
            this.lastUpdate = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - lastUpdate > 30000; // 30 seconds cache
        }
    }
    
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
        String cacheKey = userId + "_" + userType;
        
        // Get current state from cache or database
        SecurityAttemptCache cache = attemptCache.get(cacheKey);
        int currentAttempts = 0;
        boolean isAlreadyLocked = false;
        
        if (cache != null && !cache.isExpired()) {
            currentAttempts = cache.attemptCount;
            isAlreadyLocked = cache.isLocked;
        } else {
            // Quick single query to get current state
            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
                String selectSql = "SELECT attempt_count, is_locked FROM security_attempts WHERE user_id = ? AND user_type = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, userId);
                    selectStmt.setString(2, userType);
                    ResultSet rs = selectStmt.executeQuery();
                    
                    if (rs.next()) {
                        currentAttempts = rs.getInt("attempt_count");
                        isAlreadyLocked = rs.getBoolean("is_locked");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        if (isAlreadyLocked) {
            return true; // Already locked
        }
        
        currentAttempts++;
        boolean shouldLock = currentAttempts >= MAX_ATTEMPTS;
        
        // Create final variables for lambda
        final int finalAttempts = currentAttempts;
        final boolean finalShouldLock = shouldLock;
        
        // Update cache immediately for instant UI response
        attemptCache.put(cacheKey, new SecurityAttemptCache(finalAttempts, finalShouldLock));
        
        // Perform database update asynchronously to avoid blocking UI
        CompletableFuture.runAsync(() -> {
            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
                // Simplified upsert query
                String upsertSql = """
                    INSERT INTO security_attempts (user_id, user_type, attempt_count, is_locked, locked_at, unlock_token, unlock_token_expires, last_attempt) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, NOW()) 
                    ON DUPLICATE KEY UPDATE 
                        attempt_count = VALUES(attempt_count), 
                        is_locked = VALUES(is_locked), 
                        locked_at = VALUES(locked_at),
                        unlock_token = VALUES(unlock_token),
                        unlock_token_expires = VALUES(unlock_token_expires),
                        last_attempt = NOW()
                    """;
                
                String unlockToken = finalShouldLock ? generateUnlockToken() : null;
                Timestamp lockTime = finalShouldLock ? Timestamp.valueOf(LocalDateTime.now()) : null;
                Timestamp expireTime = finalShouldLock ? Timestamp.valueOf(LocalDateTime.now().plusHours(UNLOCK_TOKEN_VALIDITY_HOURS)) : null;
                
                try (PreparedStatement upsertStmt = conn.prepareStatement(upsertSql)) {
                    upsertStmt.setInt(1, userId);
                    upsertStmt.setString(2, userType);
                    upsertStmt.setInt(3, finalAttempts);
                    upsertStmt.setBoolean(4, finalShouldLock);
                    upsertStmt.setTimestamp(5, lockTime);
                    upsertStmt.setString(6, unlockToken);
                    upsertStmt.setTimestamp(7, expireTime);
                    
                    upsertStmt.executeUpdate();
                }
                
                // Log security events asynchronously
                logSecurityEventAsync(userId, userType, finalShouldLock ? "account_locked" : "failed_attempt", null);
                if (finalShouldLock) {
                    logSecurityEventAsync(userId, userType, "brute_force_detected", null);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                // If database update fails, remove from cache to force refresh
                attemptCache.remove(cacheKey);
            }
        });
        
        return shouldLock;
    }
    
    /**
     * Checks if a user account is currently locked
     */
    public boolean isAccountLocked(int userId, String userType) {
        String cacheKey = userId + "_" + userType;
        
        // Check cache first
        SecurityAttemptCache cache = attemptCache.get(cacheKey);
        if (cache != null && !cache.isExpired()) {
            return cache.isLocked;
        }
        
        // If not in cache or expired, check database
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT attempt_count, is_locked FROM security_attempts WHERE user_id = ? AND user_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    boolean isLocked = rs.getBoolean("is_locked");
                    int attemptCount = rs.getInt("attempt_count");
                    
                    // Update cache
                    attemptCache.put(cacheKey, new SecurityAttemptCache(attemptCount, isLocked));
                    
                    return isLocked;
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
        String cacheKey = userId + "_" + userType;
        
        // Update cache immediately for instant UI response
        attemptCache.put(cacheKey, new SecurityAttemptCache(0, false));
        
        // Update database asynchronously
        CompletableFuture.runAsync(() -> {
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
                // If database update fails, remove from cache
                attemptCache.remove(cacheKey);
            }
        });
    }
    
    /**
     * Unlocks an account using the unlock token (OPTIMIZED)
     */
    public boolean unlockAccount(String unlockToken) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Combined query to get user info and unlock in one operation
            String sql = """
                UPDATE security_attempts 
                SET is_locked = FALSE, attempt_count = 0, locked_at = NULL, 
                    unlock_token = NULL, unlock_token_expires = NULL 
                WHERE unlock_token = ? AND unlock_token_expires > NOW()
                """;
            
            // Get user info first for cache clearing and logging
            String userInfoSql = "SELECT user_id, user_type FROM security_attempts WHERE unlock_token = ? AND unlock_token_expires > NOW()";
            int userId = -1;
            String userType = null;
            
            try (PreparedStatement userStmt = conn.prepareStatement(userInfoSql)) {
                userStmt.setString(1, unlockToken);
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    userType = rs.getString("user_type");
                }
            }
            
            if (userId == -1) {
                return false; // Invalid or expired token
            }
            
            // Perform the unlock
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, unlockToken);
                int updated = stmt.executeUpdate();
                
                if (updated > 0) {
                    // Clear cache immediately for instant UI response
                    String cacheKey = userId + "_" + userType;
                    attemptCache.put(cacheKey, new SecurityAttemptCache(0, false));
                    
                    // Log unlock event asynchronously
                    final int finalUserId = userId;
                    final String finalUserType = userType;
                    CompletableFuture.runAsync(() -> {
                        logSecurityEvent(finalUserId, finalUserType, "account_unlocked", null);
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
        String cacheKey = userId + "_" + userType;
        
        // Check cache first
        SecurityAttemptCache cache = attemptCache.get(cacheKey);
        if (cache != null && !cache.isExpired()) {
            return Math.max(0, MAX_ATTEMPTS - cache.attemptCount);
        }
        
        // If not in cache, check database
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT attempt_count, is_locked FROM security_attempts WHERE user_id = ? AND user_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, userType);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int attempts = rs.getInt("attempt_count");
                    boolean isLocked = rs.getBoolean("is_locked");
                    
                    // Update cache
                    attemptCache.put(cacheKey, new SecurityAttemptCache(attempts, isLocked));
                    
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
    
    /**
     * Async version of logSecurityEvent to avoid blocking UI
     */
    private void logSecurityEventAsync(int userId, String userType, String eventType, String additionalData) {
        CompletableFuture.runAsync(() -> {
            logSecurityEvent(userId, userType, eventType, additionalData);
        });
    }
    
    /**
     * Clears the cache for a specific user (useful for testing or manual cache invalidation)
     */
    public void clearCache(int userId, String userType) {
        String cacheKey = userId + "_" + userType;
        attemptCache.remove(cacheKey);
    }
    
    /**
     * Clears all cache entries
     */
    public void clearAllCache() {
        attemptCache.clear();
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
