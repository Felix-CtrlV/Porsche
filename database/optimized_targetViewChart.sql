-- OPTIMIZED VERSION OF targetViewChart
-- Key improvements:
-- 1. Removed unnecessary transaction for read-only operation
-- 2. Simplified logic and removed redundant variables
-- 3. Better error handling
-- 4. Added index recommendation

-- RECOMMENDED INDEX:
-- CREATE INDEX idx_user_target_lookup ON user_target(user_id, effective_date DESC);

DROP PROCEDURE IF EXISTS `targetViewChart`;

DELIMITER $$

CREATE DEFINER=`avnadmin`@`%` PROCEDURE `targetViewChart`(
    IN in_uid INT,
    IN in_month INT,
    IN in_year INT
)
BEGIN
    DECLARE target_car INT DEFAULT 0;
    DECLARE target_part INT DEFAULT 0;
    DECLARE achieve_car INT DEFAULT 0;
    DECLARE achieve_part INT DEFAULT 0;
    DECLARE user_name VARCHAR(200) DEFAULT 'Unknown';
    DECLARE user_role ENUM('admin','manager','staff') DEFAULT 'staff';
    DECLARE target_text TEXT;
    DECLARE achieve_text TEXT;

    -- Get target and achieve data with user info (no transaction needed for read-only)
    SELECT 
        ui.user_name,
        ui.user_role,
        ut.target,
        ut.achieve
    INTO user_name, user_role, target_text, achieve_text
    FROM user_target ut
    INNER JOIN user_info ui ON ut.user_id = ui.user_id
    WHERE ut.user_id = in_uid
      AND MONTH(ut.effective_date) = in_month
      AND YEAR(ut.effective_date) = in_year
    ORDER BY ut.target_id DESC
    LIMIT 1;

    -- Parse target and achieve values if data was found
    IF target_text IS NOT NULL THEN
        -- Parse target (format: "cars-10,parts-5")
        SET target_car = IFNULL(
            CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(target_text, 'cars-', -1), ',', 1) AS UNSIGNED), 
            0
        );
        SET target_part = IFNULL(
            CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(target_text, 'parts-', -1), ',', 1) AS UNSIGNED), 
            0
        );
        
        -- Parse achieve (format: "cars-8,parts-3")
        SET achieve_car = IFNULL(
            CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(achieve_text, 'cars-', -1), ',', 1) AS UNSIGNED), 
            0
        );
        SET achieve_part = IFNULL(
            CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(achieve_text, 'parts-', -1), ',', 1) AS UNSIGNED), 
            0
        );
    END IF;

    -- Return results
    SELECT 
        in_uid as user_id,
        user_name,
        user_role,
        target_car, 
        target_part, 
        achieve_car, 
        achieve_part,
        CASE 
            WHEN target_car > 0 THEN ROUND((achieve_car / target_car) * 100, 2)
            ELSE 0 
        END as car_achievement_percentage,
        CASE 
            WHEN target_part > 0 THEN ROUND((achieve_part / target_part) * 100, 2)
            ELSE 0 
        END as part_achievement_percentage,
        CASE 
            WHEN target_car > 0 AND achieve_car >= target_car AND target_part > 0 AND achieve_part >= target_part THEN 'Target Achieved'
            WHEN target_car > 0 AND achieve_car >= target_car THEN 'Car Target Achieved'
            WHEN target_part > 0 AND achieve_part >= target_part THEN 'Part Target Achieved'
            ELSE 'In Progress'
        END as achievement_status;
END$$

DELIMITER ;

-- RECOMMENDATION: Consider normalizing the target/achieve data structure
-- Instead of storing "cars-10,parts-5" as text, create a proper table:
/*
CREATE TABLE user_target_details (
    target_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    target_id INT NOT NULL,
    item_type ENUM('car', 'part') NOT NULL,
    target_value INT NOT NULL,
    achieve_value INT NOT NULL DEFAULT 0,
    FOREIGN KEY (target_id) REFERENCES user_target(target_id),
    INDEX idx_target_lookup (target_id, item_type)
);
*/
