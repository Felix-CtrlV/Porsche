# Stored Procedure Parameter Mismatch Fix

## Critical Issue Found! 🚨

The `insertFullCar` stored procedure has **9 parameters**, but the Java controller is calling it with **11 parameters**.

---

## The Problem

### Java Controller (managerInventoryController.java - Line 2263)
```java
String sql = "{CALL insertFullCar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";  // 11 parameters
CallableStatement cs = con.prepareCall(sql);
cs.setString(1, modelName);
cs.setString(2, trimName);
cs.setString(3, extColor);
cs.setString(4, intColor);
cs.setString(5, fuelType);
cs.setInt(6, year);
cs.setInt(7, qty);
cs.setDouble(8, price);
cs.setString(9, photoPath);
cs.setString(10, combinedSpeed);    // ❌ MISSING IN PROCEDURE
cs.setString(11, description);      // ❌ MISSING IN PROCEDURE
```

### Current Stored Procedure (WRONG - Only 9 Parameters)
```sql
CREATE PROCEDURE "insertFullCar"(
    IN in_model_name VARCHAR(200),      -- 1
    IN in_trim_name VARCHAR(200),       -- 2
    IN in_car_color VARCHAR(20),        -- 3
    IN in_interior_color VARCHAR(20),   -- 4
    IN in_fuel_type VARCHAR(40),        -- 5
    IN in_production_year INT,          -- 6
    IN in_car_qty INT,                  -- 7
    IN in_price DOUBLE(15,3),           -- 8
    IN in_photo_url LONGTEXT            -- 9
    -- ❌ MISSING: in_car_speed (parameter 10)
    -- ❌ MISSING: in_car_description (parameter 11)
)
```

---

## The Fix

### Updated Stored Procedure (CORRECT - 11 Parameters)
```sql
CREATE PROCEDURE "insertFullCar"(
    IN in_model_name VARCHAR(200),      -- 1
    IN in_trim_name VARCHAR(200),       -- 2
    IN in_car_color VARCHAR(20),        -- 3
    IN in_interior_color VARCHAR(20),   -- 4
    IN in_fuel_type VARCHAR(40),        -- 5
    IN in_production_year INT,          -- 6
    IN in_car_qty INT,                  -- 7
    IN in_price DOUBLE(15,3),           -- 8
    IN in_photo_url LONGTEXT,           -- 9
    IN in_car_speed LONGTEXT,           -- 10 ✅ ADDED
    IN in_car_description LONGTEXT      -- 11 ✅ ADDED
)
BEGIN
    -- ... existing code ...
    
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
        TRUE,
        in_car_speed,      -- ✅ ADDED: e.g., "250 + 4.5"
        in_car_description -- ✅ ADDED
    );
    
    COMMIT;
    SELECT LAST_INSERT_ID() AS new_car_id;
END
```

---

## How to Apply the Fix

### Step 1: Run the Fixed SQL Script

**File:** `d:\Porsche\database\insertFullCar_FIXED.sql`

Execute this in your MySQL database:

```bash
# Option 1: Using MySQL command line
mysql -u your_username -p your_database < d:\Porsche\database\insertFullCar_FIXED.sql

# Option 2: Using MySQL Workbench
# - Open the file insertFullCar_FIXED.sql
# - Click Execute (⚡ icon)
```

### Step 2: Verify the Procedure

Run this query to check the parameters:

```sql
SHOW CREATE PROCEDURE insertFullCar;
```

You should see **11 parameters** including `in_car_speed` and `in_car_description`.

---

## Why This Caused the "Can't Add Car" Issue

When the Java code tried to call the procedure with 11 parameters but the procedure only expected 9:

1. **Parameter Mismatch Error:** MySQL throws an error like:
   ```
   Incorrect number of arguments for PROCEDURE insertFullCar; expected 9, got 11
   ```

2. **Transaction Fails:** The car is not inserted into the database

3. **No Error Shown:** The error might be caught silently or shown in the console

4. **User Sees:** Nothing happens when clicking "Confirm" button

---

## Parameter Mapping

| # | Java Variable | Procedure Parameter | Database Column | Example Value |
|---|---------------|---------------------|-----------------|---------------|
| 1 | `modelName` | `in_model_name` | `model_name` | "911" |
| 2 | `trimName` | `in_trim_name` | `trim_name` | "Carrera S" |
| 3 | `extColor` | `in_car_color` | `car_color` | "Guards Red" |
| 4 | `intColor` | `in_interior_color` | `interior_color` | "Black" |
| 5 | `fuelType` | `in_fuel_type` | `fuel_type` | "Petrol" → "PET" |
| 6 | `year` | `in_production_year` | `production_year` | 2024 |
| 7 | `qty` | `in_car_qty` | `car_qty` | 5 |
| 8 | `price` | `in_price` | `price` | 150000.00 |
| 9 | `photoPath` | `in_photo_url` | `car_photo` | "Images/911.jpg" |
| 10 | `combinedSpeed` | `in_car_speed` | `car_speed` | "308 + 3.5" |
| 11 | `description` | `in_car_description` | `car_description` | "High performance sports car" |

---

## Database Schema Verification

Make sure your `cars` table has these columns:

```sql
-- Check if columns exist
DESCRIBE cars;

-- Should include:
-- car_speed LONGTEXT (or VARCHAR)
-- car_description LONGTEXT (or TEXT)
```

If the columns don't exist, add them:

```sql
ALTER TABLE cars 
ADD COLUMN car_speed LONGTEXT AFTER car_photo,
ADD COLUMN car_description LONGTEXT AFTER car_speed;
```

---

## Testing After Fix

### Test 1: Add New Car
1. Fill in all fields in the Add Car form
2. Click "Confirm"
3. **Expected:** Success message, car appears in table
4. **Check Database:**
   ```sql
   SELECT car_id, model_name, trim_name, car_speed, car_description 
   FROM cars 
   ORDER BY car_id DESC 
   LIMIT 1;
   ```

### Test 2: Verify Speed and Description
1. Add a car with:
   - Top Speed: `320`
   - 0-100 km/h: `3.8`
   - Description: `Test car description`
2. Check database:
   ```sql
   SELECT car_speed, car_description FROM cars WHERE model_name = 'test';
   ```
3. **Expected:**
   - `car_speed`: `"320 + 3.8"`
   - `car_description`: `"Test car description"`

---

## Error Messages to Look For

Before the fix, you might see these errors in the console/terminal:

```
❌ Incorrect number of arguments for PROCEDURE insertFullCar; expected 9, got 11
❌ SQLException: Wrong number of parameters
❌ Failed to add: [SQL error message]
```

After the fix, you should see:

```
✅ Successfully Added The Car
✅ No SQL errors in console
```

---

## Summary

### What Was Wrong:
- ✅ Java controller validation fixed (added model and year)
- ❌ **Stored procedure missing 2 parameters** (car_speed, car_description)

### What Needs to Be Done:
1. **Run the fixed SQL script:** `insertFullCar_FIXED.sql`
2. **Verify the procedure** has 11 parameters
3. **Test adding a car** with all fields filled
4. **Check database** to confirm speed and description are saved

### Files:
- **Fixed SQL:** `d:\Porsche\database\insertFullCar_FIXED.sql`
- **Java Controller:** Already correct (calls with 11 parameters)
- **Database Table:** Verify `car_speed` and `car_description` columns exist

---

## Quick Fix Command

```sql
-- Copy and paste this entire block into MySQL:

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
    IN in_car_speed LONGTEXT,
    IN in_car_description LONGTEXT
)
BEGIN
    DECLARE v_fuel_storage VARCHAR(40);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN 
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    
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
    
    INSERT INTO cars(
        model_name, trim_name, car_color, interior_color, fuel_type,
        production_year, car_qty, price, car_photo, car_status,
        car_speed, car_description
    ) VALUES (
        in_model_name, in_trim_name, in_car_color, in_interior_color, v_fuel_storage,
        in_production_year, in_car_qty, in_price, in_photo_url, TRUE,
        in_car_speed, in_car_description
    );
    
    COMMIT;
    SELECT LAST_INSERT_ID() AS new_car_id;
END$$

DELIMITER ;
```

After running this, try adding a car again! 🚗✨
