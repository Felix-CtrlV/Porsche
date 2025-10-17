-- OPTIMIZED VERSION OF getAllOrders
-- Key improvements:
-- 1. Added proper parameter usage with filtering
-- 2. Moved subquery to LEFT JOIN for better performance
-- 3. Reduced GROUP_CONCAT operations
-- 4. Added index recommendations

-- RECOMMENDED INDEXES (run these first):
-- CREATE INDEX idx_orders_date ON orders(order_date DESC);
-- CREATE INDEX idx_order_details_order ON order_details(order_id);
-- CREATE INDEX idx_installment_list_order ON installment_list(order_id, is_finished, due_date);
-- CREATE INDEX idx_user_workinfo_manager ON user_workinfo(manager);
-- CREATE INDEX idx_user_info_role ON user_info(user_role);

DROP PROCEDURE IF EXISTS `getAllOrders`;

DELIMITER $$

CREATE DEFINER=`avnadmin`@`%` PROCEDURE `getAllOrders`(
    IN p_manager_id INT,
    IN p_month INT,        -- Add month filter parameter
    IN p_year INT          -- Add year filter parameter
)
BEGIN
    SELECT 
        o.order_id,
        o.order_date,
        ci.customer_name AS cus_name,
        ui.user_name AS staff_name,
        ui.user_id AS staff_id,
        SUM(od.qty) AS totalQty,
        SUM(od.total_price) AS total_amount,
        o.is_installment,
        o.paid_amount AS payed_amount,
        (SUM(od.total_price) - o.paid_amount) AS remain_amount,
        next_due.due_date,  -- Use pre-calculated due date from JOIN
        GROUP_CONCAT(
            DISTINCT
            CASE 
                WHEN od.car_id IS NOT NULL THEN c.model_name
                WHEN od.part_id IS NOT NULL THEN cp.part_name
            END SEPARATOR ','
        ) AS carsandparts_name,
        GROUP_CONCAT(DISTINCT od.qty SEPARATOR ',') AS carsandparts_qty,
        GROUP_CONCAT(DISTINCT od.total_price SEPARATOR ',') AS carsandparts_perprice
    FROM orders o
    INNER JOIN customer_info ci ON o.customer_id = ci.customer_id
    INNER JOIN user_info ui ON o.user_id = ui.user_id
    INNER JOIN order_details od ON o.order_id = od.order_id
    LEFT JOIN cars c ON od.car_id = c.car_id
    LEFT JOIN car_parts cp ON od.part_id = cp.part_id
    -- Move subquery to LEFT JOIN for better performance
    LEFT JOIN (
        SELECT 
            order_id,
            MIN(due_date) AS due_date
        FROM installment_list
        WHERE is_finished = 0
        GROUP BY order_id
    ) next_due ON o.order_id = next_due.order_id
    WHERE 
        -- Filter by manager/admin role
        (p_manager_id IN (SELECT user_id FROM user_info WHERE user_role = 'admin'))
        OR
        (ui.user_id IN (
            SELECT user_id 
            FROM user_workinfo 
            WHERE manager = p_manager_id
        ))
        -- Add date filtering at database level
        AND (p_month IS NULL OR MONTH(o.order_date) = p_month)
        AND (p_year IS NULL OR YEAR(o.order_date) = p_year)
    GROUP BY 
        o.order_id, 
        o.order_date, 
        ci.customer_name, 
        ui.user_name,
        ui.user_id, 
        o.is_installment, 
        o.paid_amount,
        next_due.due_date  -- Add to GROUP BY
    ORDER BY 
        o.order_date DESC, 
        o.order_id DESC;
END$$

DELIMITER ;

-- Alternative version for fetching ALL orders (for initial load and search)
DROP PROCEDURE IF EXISTS `getAllOrdersUnfiltered`;

DELIMITER $$

CREATE DEFINER=`avnadmin`@`%` PROCEDURE `getAllOrdersUnfiltered`(
    IN p_manager_id INT
)
BEGIN
    SELECT 
        o.order_id,
        o.order_date,
        ci.customer_name AS cus_name,
        ui.user_name AS staff_name,
        ui.user_id AS staff_id,
        SUM(od.qty) AS totalQty,
        SUM(od.total_price) AS total_amount,
        o.is_installment,
        o.paid_amount AS payed_amount,
        (SUM(od.total_price) - o.paid_amount) AS remain_amount,
        next_due.due_date,
        GROUP_CONCAT(
            DISTINCT
            CASE 
                WHEN od.car_id IS NOT NULL THEN c.model_name
                WHEN od.part_id IS NOT NULL THEN cp.part_name
            END SEPARATOR ','
        ) AS carsandparts_name,
        GROUP_CONCAT(DISTINCT od.qty SEPARATOR ',') AS carsandparts_qty,
        GROUP_CONCAT(DISTINCT od.total_price SEPARATOR ',') AS carsandparts_perprice
    FROM orders o
    INNER JOIN customer_info ci ON o.customer_id = ci.customer_id
    INNER JOIN user_info ui ON o.user_id = ui.user_id
    INNER JOIN order_details od ON o.order_id = od.order_id
    LEFT JOIN cars c ON od.car_id = c.car_id
    LEFT JOIN car_parts cp ON od.part_id = cp.part_id
    LEFT JOIN (
        SELECT 
            order_id,
            MIN(due_date) AS due_date
        FROM installment_list
        WHERE is_finished = 0
        GROUP BY order_id
    ) next_due ON o.order_id = next_due.order_id
    WHERE 
        (p_manager_id IN (SELECT user_id FROM user_info WHERE user_role = 'admin'))
        OR
        (ui.user_id IN (
            SELECT user_id 
            FROM user_workinfo 
            WHERE manager = p_manager_id
        ))
    GROUP BY 
        o.order_id, 
        o.order_date, 
        ci.customer_name, 
        ui.user_name,
        ui.user_id, 
        o.is_installment, 
        o.paid_amount,
        next_due.due_date
    ORDER BY 
        o.order_date DESC, 
        o.order_id DESC;
END$$

DELIMITER ;
