-- FIXED VERSION OF getOrdersByUserId
-- Key fixes:
-- 1. Removed DISTINCT from qty and price GROUP_CONCAT to show all items
-- 2. Added ORDER BY od.detail_id for consistent ordering
-- 3. Removed unnecessary transaction wrapper (SELECT doesn't need it)

DROP PROCEDURE IF EXISTS `getOrdersByUserId`;

DELIMITER $$

CREATE PROCEDURE `getOrdersByUserId`(
    IN in_user_id INT,
    IN in_month INT,
    IN in_year INT
)
BEGIN 
    SELECT
        ROW_NUMBER() OVER (ORDER BY o.order_date DESC) AS no,
        o.order_id,
        c.customer_name AS cus_name,
        o.order_date,
        SUM(od.total_price) AS total_amount,
        o.is_installment AS is_installment,
        GROUP_CONCAT(
            CASE 
                WHEN od.car_id IS NOT NULL THEN car.model_name
                WHEN od.part_id IS NOT NULL THEN part.part_name
            END 
            ORDER BY od.detail_id
            SEPARATOR ','
        ) AS carsandparts_name,
        GROUP_CONCAT(od.qty ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_qty,
        GROUP_CONCAT(od.total_price ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_perprice,
        o.paid_amount AS payed_amount,
        (SUM(od.total_price) - o.paid_amount) AS remain_amount,
        (
            SELECT MIN(il.due_date) 
            FROM installment_list il 
            WHERE il.order_id = o.order_id
              AND il.is_finished = 0
        ) AS due_date
    FROM 
        orders o
    JOIN 
        customer_info c ON o.customer_id = c.customer_id
    JOIN 
        user_info ui ON o.user_id = ui.user_id
    LEFT JOIN 
        order_details od ON o.order_id = od.order_id
    LEFT JOIN 
        cars car ON od.car_id = car.car_id
    LEFT JOIN 
        car_parts part ON od.part_id = part.part_id
    WHERE 
        ui.user_id = in_user_id
        AND MONTH(o.order_date) = in_month
        AND YEAR(o.order_date) = in_year
    GROUP BY 
        o.order_id, c.customer_name, o.order_date, o.paid_amount 
    ORDER BY 
        o.order_date DESC;
END$$

DELIMITER ;
