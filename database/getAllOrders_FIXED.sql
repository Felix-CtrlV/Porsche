-- FIXED VERSION OF getAllOrders
-- Key fix: Removed DISTINCT from qty and price GROUP_CONCAT to show all items
-- This ensures the installment table shows all cars/parts, not just unique quantities
-- Updated to match actual database schema (no car_models table)

DROP PROCEDURE IF EXISTS `getAllOrders`;

DELIMITER $$

CREATE PROCEDURE `getAllOrders`(
    IN p_manager_id INT,
    IN p_month INT,
    IN p_year INT
)
BEGIN
    SELECT 
        o.order_id,
        o.order_date,
        ci.customer_name AS cus_name,
        ui.user_name AS staff_name,
        SUM(od.qty) AS totalQty,
        SUM(od.total_price) AS total_amount,
        o.is_installment,
        o.paid_amount AS payed_amount,
        (SUM(od.total_price) - o.paid_amount) AS remain_amount,
        next_due.due_date,
        GROUP_CONCAT(
            CASE 
                WHEN od.car_id IS NOT NULL THEN CONCAT(c.model_name, ' ', c.trim_name)
                WHEN od.part_id IS NOT NULL THEN cp.part_name
            END 
            ORDER BY od.detail_id
            SEPARATOR ','
        ) AS carsandparts_name,
        GROUP_CONCAT(od.qty ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_qty,
        GROUP_CONCAT(od.total_price ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_perprice
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
        AND (p_month IS NULL OR MONTH(o.order_date) = p_month)
        AND (p_year IS NULL OR YEAR(o.order_date) = p_year)
    GROUP BY 
        o.order_id, 
        o.order_date, 
        ci.customer_name, 
        ui.user_name,
        o.is_installment, 
        o.paid_amount,
        next_due.due_date
    ORDER BY 
        o.order_date DESC, 
        o.order_id DESC;
END$$

DELIMITER ;
