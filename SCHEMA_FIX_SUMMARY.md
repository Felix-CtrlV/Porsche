# Database Schema Fix - getSalesChartData Procedure

## Problem

**Error:** `Unknown column 'od.inventory_type' in 'field list'`

The stored procedure was trying to use a column `inventory_type` that doesn't exist in your `order_details` table.

## Root Cause

Your `order_details` table structure:
```sql
- detail_id (INT)
- order_id (INT)
- is_customize (INT)
- car_id (INT) -- NULL if it's a part order
- part_id (INT) -- NULL if it's a car order
- qty (INT) -- NOT "quantity"
- total_price (DECIMAL)
```

The procedure was incorrectly assuming:
- Column name: `inventory_type` ❌ (doesn't exist)
- Column name: `quantity` ❌ (actual name is `qty`)

## Solution

Instead of checking `inventory_type`, we now check which ID is present:
- If `car_id IS NOT NULL` → it's a car order
- If `part_id IS NOT NULL` → it's a part order

### Fixed SQL Logic:
```sql
-- Car quantity: sum qty where car_id is not null
COALESCE(SUM(CASE WHEN od.car_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS car_qty

-- Part quantity: sum qty where part_id is not null
COALESCE(SUM(CASE WHEN od.part_id IS NOT NULL THEN od.qty ELSE 0 END), 0) AS part_qty
```

## Files Created

### ✅ `database/getSalesChartData_WORKING.sql` - USE THIS ONE
- Matches your actual database schema
- Uses `car_id IS NOT NULL` / `part_id IS NOT NULL` logic
- Uses correct column name `qty` (not `quantity`)
- Proper DELIMITER syntax
- No DEFINER clause (works everywhere)

## How to Deploy

### Option 1: PowerShell Script (Recommended)
```powershell
.\apply_chart_fix.ps1
```

### Option 2: MySQL Command Line
```bash
mysql -u your_username -p your_database < database/getSalesChartData_WORKING.sql
```

### Option 3: MySQL Workbench
1. Open `database/getSalesChartData_WORKING.sql`
2. Execute the entire script

## Testing

After deployment, test with:

```sql
-- Test with your actual user ID
CALL getSalesChartData(1, 10, 2024, 'Daily');
CALL getSalesChartData(1, 10, 2024, 'Weekly');
CALL getSalesChartData(1, 10, 2024, 'Monthly');
```

Expected output: 4 columns
- `period_label` (VARCHAR) - "Oct 01", "Week 1", "Jan", etc.
- `car_qty` (INT) - Number of cars sold
- `part_qty` (INT) - Number of parts sold
- `revenue` (DECIMAL) - Total revenue

## What Changed

| Before (Wrong) | After (Correct) |
|----------------|-----------------|
| `od.inventory_type = 'car'` | `od.car_id IS NOT NULL` |
| `od.inventory_type = 'part'` | `od.part_id IS NOT NULL` |
| `od.quantity` | `od.qty` |

## Java Controller

No changes needed! The Java code already expects the correct format:

```java
String periodLabel = rs.getString("period_label");
int carQty = rs.getInt("car_qty");
int partQty = rs.getInt("part_qty");
double revenue = rs.getDouble("revenue");
```

## Verification

After deployment, verify:

```sql
-- Check procedure exists
SHOW PROCEDURE STATUS WHERE Name = 'getSalesChartData';

-- View procedure definition
SHOW CREATE PROCEDURE getSalesChartData;

-- Test with sample data
CALL getSalesChartData(2, MONTH(CURDATE()), YEAR(CURDATE()), 'Monthly');
```

## Business Logic

The procedure correctly handles:
- ✅ Role-based filtering (admin sees all, manager sees subordinates)
- ✅ Date range filtering (current month limited to today)
- ✅ Order status filtering (confirm/pending only)
- ✅ Separate car and part quantities
- ✅ Revenue calculation from paid_amount
- ✅ Daily, Weekly, and Monthly aggregation

## Troubleshooting

### Still getting "Unknown column" error?
- Make sure you're using `getSalesChartData_WORKING.sql`
- Check that the procedure was actually updated: `SHOW CREATE PROCEDURE getSalesChartData;`

### No data returned?
- Verify you have orders in the date range
- Check that orders have `order_status` = 'confirm' or 'pending'
- Verify `order_details` are linked to orders

### Wrong quantities?
- Check that `order_details.car_id` is set for car orders
- Check that `order_details.part_id` is set for part orders
- Verify `order_details.qty` has correct values
