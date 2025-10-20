-- Fixed insertFullCar procedure with car_speed and car_description parameters

DROP PROCEDURE IF EXISTS insertFullCar;

DELIMITER $$

CREATE DEFINER="avnadmin"@"%" PROCEDURE "insertFullCar"(
    IN in_model_name VARCHAR(200),
    IN in_trim_name VARCHAR(200),
    IN in_car_color VARCHAR(20),
    IN in_interior_color VARCHAR(20),
    IN in_fuel_type VARCHAR(40),
    IN in_production_year INT,
    IN in_car_qty INT,
    IN in_price DOUBLE(15,3),
    IN in_photo_url LONGTEXT,
    IN in_car_speed LONGTEXT,        -- ✅ ADDED: Parameter 10
    IN in_car_description LONGTEXT   -- ✅ ADDED: Parameter 11
)
BEGIN
    DECLARE v_fuel_storage VARCHAR(40);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN 
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Process fuel type to standardized format
    CASE 
        WHEN LOWER(in_fuel_type) = 'petrol' OR LOWER(in_fuel_type) = 'gasoline' THEN 
            SET v_fuel_storage = 'PET';
        WHEN LOWER(in_fuel_type) = 'diesel' THEN 
            SET v_fuel_storage = 'DSL';
        WHEN LOWER(in_fuel_type) = 'electric' THEN 
            SET v_fuel_storage = 'ELEC';
        WHEN LOWER(in_fuel_type) LIKE '%hybrid%' THEN 
            SET v_fuel_storage = 'HYB';
        ELSE 
            SET v_fuel_storage = UPPER(in_fuel_type);
    END CASE;
    
    -- Insert directly into cars table
    INSERT INTO cars(
        model_name,
        trim_name,
        car_color,
        interior_color,
        fuel_type,
        production_year,
        car_qty,
        price,
        car_photo,
        car_status,
        car_speed,        -- ✅ ADDED
        car_description   -- ✅ ADDED
    ) VALUES (
        in_model_name,
        in_trim_name,
        in_car_color,
        in_interior_color,
        v_fuel_storage,
        in_production_year,
        in_car_qty,
        in_price,
        in_photo_url,
        TRUE,              -- Default to available
        in_car_speed,      -- ✅ ADDED: e.g., "250 + 4.5"
        in_car_description -- ✅ ADDED: Car description text
    );
    
    COMMIT;
    
    SELECT LAST_INSERT_ID() AS new_car_id;
END$$

DELIMITER ;
