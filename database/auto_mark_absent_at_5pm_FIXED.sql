-- Automatic Absent Marking System
-- Marks staff as "absent without report" at 5 PM if they haven't checked in

-- Create stored procedure to mark absent staff
DROP PROCEDURE IF EXISTS markAbsentStaff;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE markAbsentStaff()
BEGIN
    DECLARE current_date DATE;
    SET current_date = CURDATE();
    
    -- Insert absent records for all active staff who haven't checked in today
    -- and don't already have an attendance record for today
    -- Use 00:00:00 for both check_in and check_out to mark as absent
    INSERT INTO user_attendance (user_id, check_in, check_out, reason)
    SELECT 
        u.user_id,
        CONCAT(current_date, ' 00:00:00'),  -- 00:00:00 check_in = absent
        CONCAT(current_date, ' 00:00:00'),  -- 00:00:00 check_out = absent
        'Absent - absent without report'
    FROM user_info u
    WHERE u.user_status = TRUE
        AND u.user_role = 'staff'
        AND u.user_id NOT IN (
            -- Exclude staff who already have a record for today
            SELECT DISTINCT user_id 
            FROM user_attendance 
            WHERE DATE(check_in) = current_date
        );
    
    -- Log the number of records added
    SELECT CONCAT('Marked ', ROW_COUNT(), ' staff as absent without report') AS result;
END$$
DELIMITER ;

-- Create event scheduler to run at 5 PM every day
-- First, enable the event scheduler (run this once)
SET GLOBAL event_scheduler = ON;

-- Drop existing event if it exists
DROP EVENT IF EXISTS auto_mark_absent_daily;

-- Create event to run at 5 PM (17:00) every day
CREATE EVENT auto_mark_absent_daily
ON SCHEDULE EVERY 1 DAY
STARTS CONCAT(CURDATE(), ' 17:00:00')
DO
CALL markAbsentStaff();

-- To manually trigger the procedure for testing:
-- CALL markAbsentStaff();

-- To check if the event is active:
-- SHOW EVENTS;

-- To disable the event:
-- ALTER EVENT auto_mark_absent_daily DISABLE;

-- To enable the event:
-- ALTER EVENT auto_mark_absent_daily ENABLE;
