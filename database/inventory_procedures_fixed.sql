-- ============================================
-- FIXED INVENTORY PROCEDURES
-- ============================================
-- These procedures are corrected to match the actual database schema
-- The cars table structure: car_id, model_name, trim_name, car_color, interior_color, 
--                          fuel_type, production_year, car_qty, price, car_photo, car_status

USE car_show_room;

-- ============================================
-- 1. FIXED: getAllCars
-- ============================================
-- Java expects: car_id, car_name, car_color, interior_color, fuel_type, 
--               production_year, car_qty, price, car_photo, car_status

DROP PROCEDURE IF EXISTS getAllCars;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE "getAllCars"()
BEGIN
    SELECT 
        c.car_id,                                      -- Column 1
        CONCAT(c.model_name, ' ', c.trim_name) AS car_name,  -- Column 2 (combined name)
        c.car_color,                                   -- Column 3
        c.interior_color,                              -- Column 4
        c.fuel_type,                                   -- Column 5
        c.production_year,                             -- Column 6
        c.car_qty,                                     -- Column 7
        c.price,                                       -- Column 8
        c.car_photo,                                   -- Column 9
        c.car_status                                   -- Column 10
    FROM cars c
    ORDER BY c.car_id;
END$$
DELIMITER ;

-- ============================================
-- 2. FIXED: getAllParts
-- ============================================
-- Java expects: part_id, part_name, car_name, description, part_qty, price, part_photo, part_status

DROP PROCEDURE IF EXISTS getAllParts;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE "getAllParts"()
BEGIN 
    SELECT 
        p.part_id,                                     -- Column 1
        p.part_name,                                   -- Column 2
        CASE 
            WHEN p.for_car IS NOT NULL THEN CONCAT(c.model_name, ' ', c.trim_name)
            ELSE 'Universal'
        END AS car_name,                               -- Column 3
        p.description,                                 -- Column 4
        p.part_qty,                                    -- Column 5
        p.price,                                       -- Column 6
        p.part_photo,                                  -- Column 7
        p.part_status                                  -- Column 8
    FROM car_parts p
    LEFT JOIN cars c ON p.for_car = c.car_id
    ORDER BY p.part_status DESC, p.part_id;
END$$
DELIMITER ;

-- ============================================
-- 3. FIXED: insertFullCar
-- ============================================
-- Simplified to work with actual cars table structure (no car_models table)

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
    IN in_photo_url LONGTEXT
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
        car_status
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
        TRUE  -- Default to available
    );
    
    COMMIT;
    
    SELECT LAST_INSERT_ID() AS new_car_id;
END$$
DELIMITER ;

-- ============================================
-- 4. FIXED: insertFullPart
-- ============================================
-- Simplified to work with actual car_parts table structure

DROP PROCEDURE IF EXISTS insertFullPart;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE "insertFullPart"(
    IN in_part_name VARCHAR(200),
    IN in_description LONGTEXT,
    IN in_for_car_model VARCHAR(200),  -- Can be NULL or empty for universal parts
    IN in_part_qty INT,
    IN in_price DOUBLE(15,3),
    IN in_photo_url LONGTEXT
)
BEGIN
    DECLARE v_car_id INT DEFAULT NULL;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN 
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Find the car_id based on model name if provided
    IF in_for_car_model IS NOT NULL AND in_for_car_model != '' AND in_for_car_model != 'Universal' THEN
        -- Try to find car by matching "model_name trim_name"
        SELECT c.car_id INTO v_car_id
        FROM cars c
        WHERE CONCAT(c.model_name, ' ', c.trim_name) = in_for_car_model
        LIMIT 1;
        
        -- If not found, try just model_name
        IF v_car_id IS NULL THEN
            SELECT c.car_id INTO v_car_id
            FROM cars c
            WHERE c.model_name = in_for_car_model
            LIMIT 1;
        END IF;
    END IF;
    
    -- Insert the part
    INSERT INTO car_parts(
        part_name,
        description,
        for_car,
        part_qty,
        price,
        part_photo,
        part_status
    ) VALUES (
        in_part_name,
        in_description,
        v_car_id,  -- NULL for universal parts
        in_part_qty,
        in_price,
        in_photo_url,
        TRUE  -- Default to available
    );
    
    COMMIT;
    
    SELECT LAST_INSERT_ID() AS new_part_id;
END$$
DELIMITER ;

-- ============================================
-- 5. FIXED: updateFullCar
-- ============================================
-- Simplified to work with actual cars table structure (no car_models table)

DROP PROCEDURE IF EXISTS updateFullCar;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE "updateFullCar"(
    IN in_car_id INT,
    IN in_car_name VARCHAR(400),      -- Full name like "911 Carrera T"
    IN in_car_color VARCHAR(20),
    IN in_interior_color VARCHAR(20),
    IN in_fuel_type VARCHAR(40),
    IN in_production_year INT,
    IN in_car_qty INT,
    IN in_price DOUBLE(15,3),
    IN in_photo_url LONGTEXT
)
BEGIN
    DECLARE v_fuel_storage VARCHAR(40);
    DECLARE v_model_name VARCHAR(200);
    DECLARE v_trim_name VARCHAR(200);
    DECLARE v_space_count INT;
    DECLARE v_last_space_pos INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN 
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Parse car name to extract model_name and trim_name
    -- Count spaces in the car name
    SET v_space_count = LENGTH(in_car_name) - LENGTH(REPLACE(in_car_name, ' ', ''));
    
    IF v_space_count >= 2 THEN
        -- Has 2 or more spaces: split at last space
        -- Example: "911 Carrera T" -> model_name="911 Carrera", trim_name="T"
        SET v_last_space_pos = LENGTH(in_car_name) - LENGTH(SUBSTRING_INDEX(in_car_name, ' ', -1));
        SET v_model_name = TRIM(SUBSTRING(in_car_name, 1, v_last_space_pos - 1));
        SET v_trim_name = TRIM(SUBSTRING(in_car_name, v_last_space_pos + 1));
    ELSEIF v_space_count = 1 THEN
        -- Has 1 space: split at that space
        -- Example: "911 Turbo" -> model_name="911", trim_name="Turbo"
        SET v_model_name = TRIM(SUBSTRING_INDEX(in_car_name, ' ', 1));
        SET v_trim_name = TRIM(SUBSTRING_INDEX(in_car_name, ' ', -1));
    ELSE
        -- No spaces: entire name is model, trim is empty
        -- Example: "Cayenne" -> model_name="Cayenne", trim_name=""
        SET v_model_name = TRIM(in_car_name);
        SET v_trim_name = '';
    END IF;
    
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
    
    -- Update the car directly in cars table
    UPDATE cars SET
        model_name = v_model_name,
        trim_name = v_trim_name,
        car_color = in_car_color,
        interior_color = in_interior_color,
        fuel_type = v_fuel_storage,
        production_year = in_production_year,
        car_qty = in_car_qty,
        price = in_price,
        car_photo = CASE 
            WHEN in_photo_url IS NOT NULL AND in_photo_url != '' THEN in_photo_url
            ELSE car_photo  -- Keep existing photo if no new one provided
        END
    WHERE car_id = in_car_id;
    
    COMMIT;
    
    SELECT ROW_COUNT() AS rows_affected;
END$$
DELIMITER ;

-- ============================================
-- 6. FIXED: updateFullPart
-- ============================================
-- Simplified to work with actual car_parts table structure

DROP PROCEDURE IF EXISTS updateFullPart;

DELIMITER $$
CREATE DEFINER="avnadmin"@"%" PROCEDURE "updateFullPart"(
    IN in_part_id INT,
    IN in_part_name VARCHAR(200),
    IN in_description LONGTEXT,
    IN in_for_car_model VARCHAR(200),
    IN in_part_qty INT,
    IN in_price DOUBLE(15,3),
    IN in_photo_url LONGTEXT
)
BEGIN
    DECLARE v_car_id INT DEFAULT NULL;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN 
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
    -- Find the car_id based on model name if provided
    IF in_for_car_model IS NOT NULL AND in_for_car_model != '' AND in_for_car_model != 'Universal' THEN
        -- Try to find car by matching "model_name trim_name"
        SELECT c.car_id INTO v_car_id
        FROM cars c
        WHERE CONCAT(c.model_name, ' ', c.trim_name) = in_for_car_model
        LIMIT 1;
        
        -- If not found, try just model_name
        IF v_car_id IS NULL THEN
            SELECT c.car_id INTO v_car_id
            FROM cars c
            WHERE c.model_name = in_for_car_model
            LIMIT 1;
        END IF;
    END IF;
    
    -- Update the part
    UPDATE car_parts SET
        part_name = in_part_name,
        description = in_description,
        for_car = v_car_id,  -- NULL for universal parts
        part_qty = in_part_qty,
        price = in_price,
        part_photo = CASE 
            WHEN in_photo_url IS NOT NULL AND in_photo_url != '' THEN in_photo_url
            ELSE part_photo  -- Keep existing photo if no new one provided
        END
    WHERE part_id = in_part_id;
    
    COMMIT;
    
    SELECT ROW_COUNT() AS rows_affected;
END$$
DELIMITER ;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify procedures were created
SHOW PROCEDURE STATUS WHERE Db = 'car_show_room' 
AND Name IN ('getAllCars', 'getAllParts', 'insertFullCar', 'insertFullPart', 'updateFullCar', 'updateFullPart');

-- ============================================
-- KEY FIXES MADE:
-- ============================================
-- 1. getAllCars: Returns columns in correct order expected by Java
-- 2. getAllParts: Returns columns in correct order expected by Java
-- 3. insertFullCar: Removed references to non-existent car_models table
-- 4. insertFullPart: Removed references to non-existent photos table
-- 5. updateFullCar: Simplified to update cars table directly
-- 6. updateFullPart: Simplified to update car_parts table directly
-- 7. All procedures now work with actual table structure: cars and car_parts
-- 8. Photo handling simplified - stores photo URL directly in car_photo/part_photo columns
-- ============================================
