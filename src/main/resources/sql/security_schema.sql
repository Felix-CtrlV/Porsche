-- Security tracking tables for brute force protection

-- Table to track login attempts per user session
CREATE TABLE IF NOT EXISTS security_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_type ENUM('admin', 'manager') NOT NULL,
    attempt_count INT DEFAULT 0,
    last_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_locked BOOLEAN DEFAULT FALSE,
    locked_at TIMESTAMP NULL,
    unlock_token VARCHAR(255) NULL,
    unlock_token_expires TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_type (user_id, user_type)
);

-- Table to log security events
CREATE TABLE IF NOT EXISTS security_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_type ENUM('admin', 'manager') NOT NULL,
    event_type ENUM('failed_attempt', 'account_locked', 'account_unlocked', 'brute_force_detected') NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    additional_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_security_attempts_user ON security_attempts(user_id, user_type);
CREATE INDEX idx_security_attempts_locked ON security_attempts(is_locked);
CREATE INDEX idx_security_logs_user ON security_logs(user_id, user_type);
CREATE INDEX idx_security_logs_event ON security_logs(event_type);
CREATE INDEX idx_security_logs_created ON security_logs(created_at);
