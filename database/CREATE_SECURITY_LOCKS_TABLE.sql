-- Create security_locks table for dashboard lockout system
-- This table stores lockout information when users fail verification attempts

CREATE TABLE IF NOT EXISTS security_locks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    lock_token VARCHAR(255) NOT NULL UNIQUE,
    unlock_status BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unlocked_at TIMESTAMP NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    FOREIGN KEY (user_id) REFERENCES user_info(user_id) ON DELETE CASCADE,
    INDEX idx_user_token (user_id, lock_token),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment to table
ALTER TABLE security_locks COMMENT = 'Stores dashboard lockout tokens and unlock status';

-- Sample query to insert a lock record (used by the application)
-- INSERT INTO security_locks (user_id, lock_token) VALUES (?, ?);

-- Sample query to check unlock status (used by polling)
-- SELECT unlock_status FROM security_locks 
-- WHERE user_id = ? AND lock_token = ? AND created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- Sample query to unlock (used by unlock endpoint)
-- UPDATE security_locks SET unlock_status = TRUE, unlocked_at = NOW() 
-- WHERE user_id = ? AND lock_token = ?;
