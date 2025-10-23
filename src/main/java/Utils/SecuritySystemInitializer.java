package Utils;

import Utils.LocalUnlockServer;
import Database.DatabaseConnectionManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SecuritySystemInitializer {
    private static SecuritySystemInitializer instance;
    private LocalUnlockServer localUnlockServer;
    
    private SecuritySystemInitializer() {}
    
    public static SecuritySystemInitializer getInstance() {
        if (instance == null) {
            instance = new SecuritySystemInitializer();
        }
        return instance;
    }
    
    /**
     * Initialize the security system components
     */
    public void initialize() {
        try {
            // 1. Initialize database schema
            initializeDatabase();
            
            // 2. Start unlock server
            startUnlockServer();
            
            System.out.println("Security system initialized successfully!");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize security system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize database schema for security system
     */
    private void initializeDatabase() throws SQLException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            createSecurityTablesManually(conn);
        }
    }
    
    /**
     * Create security tables manually with proper error handling
     */
    private void createSecurityTablesManually(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create security_attempts table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS security_attempts (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    user_type ENUM('admin', 'manager') NOT NULL,
                    attempt_count INT DEFAULT 0,
                    last_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    is_locked BOOLEAN DEFAULT FALSE,
                    unlock_token VARCHAR(255),
                    unlock_token_expires TIMESTAMP NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_user_attempt (user_id, user_type)
                )
                """);
            
            // Create security_logs table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS security_logs (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    user_type ENUM('admin', 'manager') NOT NULL,
                    event_type ENUM('failed_attempt', 'account_locked', 'account_unlocked', 'brute_force_detected') NOT NULL,
                    ip_address VARCHAR(45),
                    user_agent TEXT,
                    additional_data JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
            
            // Create indexes with error handling for duplicates
            createIndexSafely(stmt, "idx_security_attempts_user", "security_attempts", "user_id, user_type");
            createIndexSafely(stmt, "idx_security_attempts_locked", "security_attempts", "is_locked");
            createIndexSafely(stmt, "idx_security_logs_user", "security_logs", "user_id, user_type");
            createIndexSafely(stmt, "idx_security_logs_event", "security_logs", "event_type");
            createIndexSafely(stmt, "idx_security_logs_created", "security_logs", "created_at");
            
            System.out.println("✅ Security tables and indexes created successfully!");
        }
    }
    
    /**
     * Create index safely, ignoring duplicate key errors
     */
    private void createIndexSafely(Statement stmt, String indexName, String tableName, String columns) {
        try {
            String sql = String.format("CREATE INDEX IF NOT EXISTS %s ON %s(%s)", indexName, tableName, columns);
            stmt.execute(sql);
            System.out.println("✅ Index created: " + indexName);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate key name")) {
                System.out.println("ℹ️  Index already exists: " + indexName);
            } else {
                System.err.println("⚠️  Failed to create index " + indexName + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Start the local unlock server
     */
    private void startUnlockServer() {
        localUnlockServer = LocalUnlockServer.getInstance();
        localUnlockServer.start();
    }
    
    /**
     * Shutdown the security system
     */
    public void shutdown() {
        if (localUnlockServer != null) {
            localUnlockServer.stop();
        }
        
        // Shutdown unlock monitoring service
        UnlockMonitorService.getInstance().shutdown();
        
        System.out.println("Security system shutdown completed.");
    }
    
    /**
     * Check if an account is locked (utility method)
     */
    public boolean isAccountLocked(int userId, String userType) {
        return SecurityManager.getInstance().isAccountLocked(userId, userType);
    }
    
    /**
     * Unlock an account using token (utility method)
     */
    public boolean unlockAccount(String token) {
        return SecurityManager.getInstance().unlockAccount(token);
    }
}
