-- Stored Procedures for Forgot Password System
-- Run these SQL commands in your MySQL database

-- 1. Procedure to get user by email
DELIMITER $$

DROP PROCEDURE IF EXISTS getUserByEmail$$

CREATE PROCEDURE getUserByEmail(
    IN p_email VARCHAR(255)
)
BEGIN
    SELECT 
        id,
        username,
        email,
        phone,
        address,
        dob,
        role
    FROM users
    WHERE email = p_email
    AND is_active = 'active'
    LIMIT 1;
END$$

DELIMITER ;

-- 2. Procedure to reset password
DELIMITER $$

DROP PROCEDURE IF EXISTS resetPassword$$

CREATE PROCEDURE resetPassword(
    IN p_username VARCHAR(255),
    IN p_new_password VARCHAR(255)
)
BEGIN
    UPDATE users
    SET password = p_new_password
    WHERE username = p_username;
    
    -- Return success indicator
    SELECT ROW_COUNT() as affected_rows;
END$$

DELIMITER ;

-- Test the procedures (optional)
-- CALL getUserByEmail('test@example.com');
-- CALL resetPassword('testuser', 'newpassword123');
