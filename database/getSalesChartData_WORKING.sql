-- Drop existing procedure first
DROP PROCEDURE IF EXISTS getSalesChartData;

DELIMITER $$

CREATE PROCEDURE getSalesChartData(
    IN p_id INT,
    IN p_month INT,
    IN p_year INT,
    IN p_period VARCHAR(10)
)
BEGIN
    DECLARE v_last_day INT;
    DECLARE v_weeks INT;
    DECLARE v_months_end INT;
    DECLARE v_role VARCHAR(20);

    -- Get user role
    SELECT user_role INTO v_role 
    FROM user_info 
    WHERE user_id = p_id;

    -- Calculate last day of month
    SET v_last_day = DAY(LAST_DAY(CONCAT(p_year, '-', LPAD(p_month, 2, '0'), '-01')));

    -- Limit to current date if current month/year
    IF p_month = MONTH(CURDATE()) AND p_year = YEAR(CURDATE()) THEN
        SET v_last_day = DAY(CURDATE());
    END IF;

    -- Calculate number of weeks
    SET v_weeks = CEIL(v_last_day / 7);

    -- Calculate months end
    SET v_months_end = IF(p_year = YEAR(CURDATE()), MONTH(CURDATE()), 12);

    -- DAILY VIEW
    IF p_period = 'Daily' THEN
        WITH RECURSIVE days AS (
            SELECT 1 AS day_num
            UNION ALL
            SELECT day_num + 1 FROM days WHERE day_num < v_last_day
        )
        SELECT 
            DATE_FORMAT(STR_TO_DATE(CONCAT(p_year,'-',LPAD(p_month,2,'0'),'-',LPAD(d.day_num,2,'0')), '%Y-%m-%d'), '%b %d') AS period_label,
            COALESCE(SUM(CASE WHEN od.car_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS car_qty,
            COALESCE(SUM(CASE WHEN od.part_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS part_qty,
            COALESCE(SUM(o.paid_amount), 0) AS revenue
        FROM days d
        LEFT JOIN orders o ON 
            DAY(o.order_date) = d.day_num
            AND MONTH(o.order_date) = p_month 
            AND YEAR(o.order_date) = p_year
            AND o.order_status IN ('confirm','pending')
            AND (v_role = 'admin' OR (v_role = 'manager' AND o.user_id IN (
                SELECT ui.user_id FROM user_info ui
                INNER JOIN user_workinfo uw ON ui.user_id = uw.user_id
                WHERE uw.manager = p_id
            )))
        LEFT JOIN order_details od ON o.order_id = od.order_id
        GROUP BY d.day_num
        ORDER BY d.day_num;

    -- WEEKLY VIEW
    ELSEIF p_period = 'Weekly' THEN
        WITH RECURSIVE weeks AS (
            SELECT 1 AS week_num
            UNION ALL
            SELECT week_num + 1 FROM weeks WHERE week_num < v_weeks
        )
        SELECT 
            CONCAT('Week ', w.week_num) AS period_label,
            COALESCE(SUM(CASE WHEN od.car_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS car_qty,
            COALESCE(SUM(CASE WHEN od.part_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS part_qty,
            COALESCE(SUM(o.paid_amount), 0) AS revenue
        FROM weeks w
        LEFT JOIN orders o ON 
            (((DAY(o.order_date) - 1) DIV 7) + 1) = w.week_num
            AND MONTH(o.order_date) = p_month
            AND YEAR(o.order_date) = p_year
            AND o.order_status IN ('confirm','pending')
            AND (v_role = 'admin' OR (v_role = 'manager' AND o.user_id IN (
                SELECT ui.user_id FROM user_info ui
                INNER JOIN user_workinfo uw ON ui.user_id = uw.user_id
                WHERE uw.manager = p_id
            )))
        LEFT JOIN order_details od ON o.order_id = od.order_id
        GROUP BY w.week_num
        ORDER BY w.week_num;

    -- MONTHLY VIEW
    ELSE
        WITH RECURSIVE months AS (
            SELECT 1 AS month_num
            UNION ALL
            SELECT month_num + 1 FROM months WHERE month_num < v_months_end
        )
        SELECT 
            DATE_FORMAT(STR_TO_DATE(CONCAT(p_year,'-',LPAD(m.month_num,2,'0'),'-01'), '%Y-%m-%d'), '%b') AS period_label,
            COALESCE(SUM(CASE WHEN od.car_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS car_qty,
            COALESCE(SUM(CASE WHEN od.part_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS part_qty,
            COALESCE(SUM(o.paid_amount), 0) AS revenue
        FROM months m
        LEFT JOIN orders o ON 
            MONTH(o.order_date) = m.month_num
            AND YEAR(o.order_date) = p_year
            AND o.order_status IN ('confirm','pending')
            AND (v_role = 'admin' OR (v_role = 'manager' AND o.user_id IN (
                SELECT ui.user_id FROM user_info ui
                INNER JOIN user_workinfo uw ON ui.user_id = uw.user_id
                WHERE uw.manager = p_id
            )))
        LEFT JOIN order_details od ON o.order_id = od.order_id
        GROUP BY m.month_num
        ORDER BY m.month_num;
    END IF;
END$$

DELIMITER ;
