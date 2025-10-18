# Schema Verification - getAllOrders Procedure

## Your Database Schema (from tables.sql)

### Key Tables:

**cars** table:
```sql
CREATE TABLE cars(
    car_id int auto_increment primary key,
    model_name varchar(200),        -- ✅ Model name stored directly
    trim_name varchar(200),          -- ✅ Trim name stored directly
    car_color varchar(20),
    interior_color varchar(20),
    fuel_type varchar(40),
    car_status boolean default true,
    production_year int,
    car_qty int,
    price double(15,3),
    car_photo LONGTEXT
);
```

**car_parts** table:
```sql
CREATE TABLE car_parts(
    part_id int auto_increment primary key,
    part_name varchar(200),
    for_car int,                     -- ✅ Foreign key to cars.car_id
    description longtext,
    part_qty int,
    price double(15,3),
    part_status boolean default true,
    part_photo LONGTEXT,
    FOREIGN KEY (for_car) REFERENCES cars(car_id)
);
```

**order_details** table:
```sql
CREATE TABLE order_details(
    detail_id int auto_increment primary key,  -- ✅ Used for ordering
    order_id int,
    is_customize boolean DEFAULT false,
    car_id int null,                           -- ✅ NULL if part order
    part_id int null,                          -- ✅ NULL if car order
    qty int,                                   -- ✅ Quantity
    total_price double(15,3),                  -- ✅ Total price per line
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (car_id) REFERENCES cars(car_id),
    FOREIGN KEY (part_id) REFERENCES car_parts(part_id)
);
```

## Procedure Verification

### ✅ Fixed Procedure Matches Your Schema

**Line 29:** Uses `c.model_name` and `c.trim_name` directly from `cars` table
```sql
WHEN od.car_id IS NOT NULL THEN CONCAT(c.model_name, ' ', c.trim_name)
```

**Line 30:** Uses `cp.part_name` from `car_parts` table
```sql
WHEN od.part_id IS NOT NULL THEN cp.part_name
```

**Line 32:** Orders by `od.detail_id` (primary key in order_details)
```sql
ORDER BY od.detail_id
```

**Line 35-36:** No DISTINCT - shows all items
```sql
GROUP_CONCAT(od.qty ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_qty,
GROUP_CONCAT(od.total_price ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_perprice
```

**Line 41-42:** Correct table joins
```sql
LEFT JOIN cars c ON od.car_id = c.car_id
LEFT JOIN car_parts cp ON od.part_id = cp.part_id
```

## What Was Fixed

### ❌ Original Issue:
```sql
-- Wrong: Assumed car_models table exists
LEFT JOIN car_models cm ON c.model_id = cm.model_id
CONCAT(cm.model_name, ' ', cm.trim_name)  -- cm doesn't exist!
```

### ✅ Fixed Version:
```sql
-- Correct: Uses cars table directly
LEFT JOIN cars c ON od.car_id = c.car_id
CONCAT(c.model_name, ' ', c.trim_name)  -- c.model_name and c.trim_name exist!
```

## Expected Output Format

For an order with multiple items, the procedure returns:

| Column | Example Value | Type |
|--------|---------------|------|
| order_id | 1 | INT |
| order_date | 2024-10-15 | DATE |
| cus_name | John Doe | VARCHAR |
| staff_name | Jane Smith | VARCHAR |
| totalQty | 5 | INT |
| total_amount | 215855.00 | DOUBLE |
| is_installment | 1 (true) | BOOLEAN |
| payed_amount | 50000.00 | DOUBLE |
| remain_amount | 165855.00 | DOUBLE |
| due_date | 2024-11-15 | DATE |
| **carsandparts_name** | **"911 Carrera S,Taycan 4S,Wheel Caps,Floor Mats,LED Projectors"** | VARCHAR |
| **carsandparts_qty** | **"1,1,4,2,2"** | VARCHAR |
| **carsandparts_perprice** | **"120000.00,95000.00,120.00,285.50,450.75"** | VARCHAR |

### Key Points:
- ✅ All 5 items included (not just 1)
- ✅ Quantities: 1,1,4,2,2 (5 values - no DISTINCT)
- ✅ Prices: 5 values matching 5 items
- ✅ Names: 5 values matching 5 items
- ✅ All arrays same length → All items display in table

## Deployment

The procedure is now ready to deploy:

```powershell
.\apply_getAllOrders_fix.ps1
```

Or manually:
```bash
mysql -u your_username -p your_database < database/getAllOrders_FIXED.sql
```

## Testing Query

After deployment, test with:

```sql
-- Test the procedure
CALL getAllOrders(1, NULL, NULL);

-- Check a specific order's items
SELECT 
    od.detail_id,
    CASE 
        WHEN od.car_id IS NOT NULL THEN CONCAT(c.model_name, ' ', c.trim_name)
        WHEN od.part_id IS NOT NULL THEN cp.part_name
    END AS item_name,
    od.qty,
    od.total_price
FROM order_details od
LEFT JOIN cars c ON od.car_id = c.car_id
LEFT JOIN car_parts cp ON od.part_id = cp.part_id
WHERE od.order_id = 1
ORDER BY od.detail_id;
```

## Summary

✅ **Procedure now matches your actual database schema**
✅ **No car_models table required**
✅ **Uses cars.model_name and cars.trim_name directly**
✅ **All items will display in installment table**
✅ **Ready to deploy!**
