-- QUICK FIX: Update getAllOrders to handle NULL parameters
-- This allows the procedure to return all orders when month/year are NULL

DROP PROCEDURE IF EXISTS `getAllOrders`;

DELIMITER $$

CREATE DEFINER=`avnadmin`@`%` PROCEDURE `getAllOrders`(
    IN p_manager_id INT,
    IN p_month INT,
    IN p_year INT
)
BEGIN
    SELECT 
        o.order_id,                                                 -- Column 1
        o.order_date,                                               -- Column 2
        ci.customer_name AS cus_name,                              -- Column 3
        ui.user_name AS staff_name,                                -- Column 4
        SUM(od.qty) AS totalQty,                                   -- Column 5
        SUM(od.total_price) AS total_amount,                       -- Column 6
        o.is_installment,                                          -- Column 7
        o.paid_amount AS payed_amount,                             -- Column 8
        (SUM(od.total_price) - o.paid_amount) AS remain_amount,   -- Column 9
        next_due.due_date,                                         -- Column 10
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
    -- Optimized: Move subquery to LEFT JOIN
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
        -- IMPORTANT: Handle NULL parameters to get all orders
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

SELECT 'getAllOrders procedure updated successfully! Restart your application.' AS Status;
