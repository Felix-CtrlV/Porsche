# Stored Procedure Fix Guide

## Problem

The stored procedure `getSalesChartData` had a syntax error when being created in MySQL. The issue is that MySQL requires a **DELIMITER** change when creating procedures with multiple statements.

## Why DELIMITER is Needed

MySQL uses semicolons (`;`) to end statements. When creating a stored procedure with multiple statements inside, MySQL gets confused about which semicolons end the procedure definition vs. which ones are inside the procedure.

**Solution:** Change the delimiter temporarily to `$$` or `//` so MySQL knows the procedure isn't finished until it sees `$$`.

## Files Created

### 1. `database/getSalesChartData_SIMPLE.sql` ✅ RECOMMENDED
- Clean version without DEFINER clause
- Proper DELIMITER syntax
- Formatted for readability
- Works with standard MySQL installations

### 2. `database/getSalesChartData_FIXED.sql`
- Includes DEFINER clause for Aiven Cloud
- Same functionality as SIMPLE version
- Use if you need specific user permissions

## How to Deploy

### Option 1: Using PowerShell Script (Easiest)
```powershell
.\apply_chart_fix.ps1
```

### Option 2: MySQL Command Line
```bash
mysql -u your_username -p your_database < database/getSalesChartData_SIMPLE.sql
```

### Option 3: MySQL Workbench
1. Open MySQL Workbench
2. Connect to your database
3. Open `database/getSalesChartData_SIMPLE.sql`
4. Execute the entire script (Ctrl+Shift+Enter)

### Option 4: Copy-Paste in MySQL CLI
```bash
mysql -u your_username -p
USE your_database;
source D:/Porsche/database/getSalesChartData_SIMPLE.sql
```

## What the Procedure Returns

**4 columns:**
1. `period_label` (VARCHAR) - "Jan", "Feb 15", "Week 1", etc.
2. `car_qty` (INT) - Number of cars sold
3. `part_qty` (INT) - Number of parts sold
4. `revenue` (DECIMAL) - Total revenue

## Testing the Procedure

After deployment, test it:

```sql
-- Test Daily view
CALL getSalesChartData(1, 10, 2024, 'Daily');

-- Test Weekly view
CALL getSalesChartData(1, 10, 2024, 'Weekly');

-- Test Monthly view
CALL getSalesChartData(1, 10, 2024, 'Monthly');
```

Replace `1` with your actual manager/admin user ID.

## Common Errors and Solutions

### Error: "DELIMITER command not found"
**Cause:** Using a MySQL client that doesn't support DELIMITER
**Solution:** Use `mysql` command-line client or MySQL Workbench

### Error: "Access denied for user"
**Cause:** Insufficient permissions
**Solution:** 
- Remove the DEFINER clause (use SIMPLE version)
- Or grant CREATE ROUTINE privilege: `GRANT CREATE ROUTINE ON database.* TO 'user'@'host';`

### Error: "Table 'order_details' doesn't exist"
**Cause:** Missing table in database
**Solution:** Verify your database schema has:
- `orders` table
- `order_details` table with `inventory_type` column
- `user_info` table
- `user_workinfo` table

### Error: "Unknown column 'inventory_type'"
**Cause:** `order_details` table doesn't have `inventory_type` column
**Solution:** Add the column:
```sql
ALTER TABLE order_details ADD COLUMN inventory_type VARCHAR(10);
UPDATE order_details SET inventory_type = 'car' WHERE car_id IS NOT NULL;
UPDATE order_details SET inventory_type = 'part' WHERE part_id IS NOT NULL;
```

## Verification

After deployment, verify the procedure exists:

```sql
SHOW PROCEDURE STATUS WHERE Name = 'getSalesChartData';
```

Check the procedure definition:

```sql
SHOW CREATE PROCEDURE getSalesChartData;
```

## Java Controller Compatibility

The Java controller has been updated to match this procedure format:

```java
String periodLabel = rs.getString("period_label");
int carQty = rs.getInt("car_qty");
int partQty = rs.getInt("part_qty");
double revenue = rs.getDouble("revenue");
```

No further Java changes needed - it's already compatible!

## Rollback

If you need to rollback:

```sql
DROP PROCEDURE IF EXISTS getSalesChartData;
-- Then restore your backup or previous version
```
