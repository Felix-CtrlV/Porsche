-- ============================================
-- FORGOT PASSWORD SYSTEM - DATABASE SETUP
-- ============================================
-- Copy and paste this ENTIRE file into MySQL Workbench or your MySQL client
-- Make sure you're connected to the 'car_show_room' database

USE car_show_room;

-- ============================================
-- 1. DROP EXISTING PROCEDURES (if any)
-- ============================================

DROP PROCEDURE IF EXISTS getUserByEmail;
DROP PROCEDURE IF EXISTS resetPassword;

-- ============================================
-- 2. CREATE getUserByEmail PROCEDURE
-- ============================================

DELIMITER $$

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

-- ============================================
-- 3. CREATE resetPassword PROCEDURE
-- ============================================

DELIMITER $$

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

-- ============================================
-- 4. VERIFY PROCEDURES WERE CREATED
-- ============================================

SHOW PROCEDURE STATUS WHERE Db = 'car_show_room' AND Name IN ('getUserByEmail', 'resetPassword');

-- ============================================
-- 5. TEST THE PROCEDURES (Optional)
-- ============================================

-- Test getUserByEmail with an existing email from your database
-- CALL getUserByEmail('your-test-email@example.com');

-- Test resetPassword (uncomment to test)
-- CALL resetPassword('testuser', 'newpassword123');

-- ============================================
-- SETUP COMPLETE!
-- ============================================
-- You should see 2 procedures listed above:
-- 1. getUserByEmail
-- 2. resetPassword
--
-- If you see them, the setup is complete!
-- Now you can use the Forgot Password feature in your application.
-- ============================================
