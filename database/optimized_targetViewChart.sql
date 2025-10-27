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

-- ============================================
-- BEST SELLING ITEMS PROCEDURES WITH IMAGE SUPPORT
-- ============================================

-- Get Best Selling Cars with Image URLs
DROP PROCEDURE IF EXISTS getBestSellingCars;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE getBestSellingCars(
    IN in_manager_id INT,
    IN in_month INT,
    IN in_year INT
)
BEGIN
    SELECT
        ROW_NUMBER() OVER (ORDER BY COALESCE(SUM(od.qty), 0) DESC) as car_rank,
        10 as target_qty, -- Default target of 10
        COALESCE(SUM(od.qty), 0) as sold_qty,
        CONCAT(c.model_name, ' ', c.trim_name) as inventory_name,
        c.car_id,
        c.car_color,
        c.fuel_type,
        c.price,
        ROUND((COALESCE(SUM(od.qty), 0) / 10) * 100, 2) as achievement_percentage,
        c.car_photo as image_url
    FROM cars c
    LEFT JOIN order_details od ON c.car_id = od.car_id
    LEFT JOIN orders o ON od.order_id = o.order_id
    LEFT JOIN user_info ui ON o.user_id = ui.user_id
    WHERE ui.user_id = in_manager_id
        AND MONTH(o.order_date) = in_month
        AND YEAR(o.order_date) = in_year
        AND o.order_status IN ('confirm', 'completed')
    GROUP BY c.car_id, c.model_name, c.trim_name, c.car_color, c.fuel_type, c.price, c.car_photo
    ORDER BY sold_qty DESC
    LIMIT 10;
END$$
DELIMITER ;

-- Get Best Selling Parts with Image URLs
DROP PROCEDURE IF EXISTS getBestSellingParts;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE getBestSellingParts(
    IN in_manager_id INT,
    IN in_month INT,
    IN in_year INT
)
BEGIN
    SELECT
        ROW_NUMBER() OVER (ORDER BY COALESCE(SUM(od.qty), 0) DESC) as part_rank,
        5 as target_qty, -- Default target of 5
        COALESCE(SUM(od.qty), 0) as sold_qty,
        cp.part_name as inventory_name,
        cp.part_id,
        ROUND((COALESCE(SUM(od.qty), 0) / 5) * 100, 2) as achievement_percentage,
        cp.part_photo as image_url
    FROM car_parts cp
    LEFT JOIN order_details od ON cp.part_id = od.part_id
    LEFT JOIN orders o ON od.order_id = o.order_id
    LEFT JOIN user_info ui ON o.user_id = ui.user_id
    WHERE ui.user_id = in_manager_id
        AND MONTH(o.order_date) = in_month
        AND YEAR(o.order_date) = in_year
        AND o.order_status IN ('confirm', 'completed')
    GROUP BY cp.part_id, cp.part_name, cp.part_photo
    ORDER BY sold_qty DESC
    LIMIT 10;
END$$
DELIMITER ;

-- Get Best Staff Performance
DROP PROCEDURE IF EXISTS getBestStaff;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE getBestStaff(
    IN in_manager_id INT,
    IN in_month INT,
    IN in_year INT
)
BEGIN
    SELECT
        ROW_NUMBER() OVER (ORDER BY COALESCE(SUM(od.qty * od.total_price), 0) DESC) as rank,
        ui.user_id as staffId,
        ui.user_name as staffName,
        ui.user_photo as staffPhoto,
        COALESCE(SUM(TIMESTAMPDIFF(HOUR, s.start_time, s.end_time)), 0) as work_hours,
        COALESCE(LAG(SUM(TIMESTAMPDIFF(HOUR, s.start_time, s.end_time))) OVER (ORDER BY ui.user_id), 0) as prev_work_hours,
        COALESCE(SUM(od.qty * od.total_price), 0) as total_sale,
        COALESCE(LAG(SUM(od.qty * od.total_price)) OVER (ORDER BY ui.user_id), 0) as prev_total_sale
    FROM user_info ui
    LEFT JOIN orders o ON ui.user_id = o.user_id
    LEFT JOIN order_details od ON o.order_id = od.order_id
    LEFT JOIN staff_attendance s ON ui.user_id = s.user_id
    WHERE ui.manager_id = in_manager_id
        AND ui.user_role = 'staff'
        AND MONTH(o.order_date) = in_month
        AND YEAR(o.order_date) = in_year
        AND o.order_status IN ('confirm', 'completed')
        AND MONTH(s.date) = in_month
        AND YEAR(s.date) = in_year
    GROUP BY ui.user_id, ui.user_name, ui.user_photo
    ORDER BY total_sale DESC
    LIMIT 10;
END$$
DELIMITER ;
