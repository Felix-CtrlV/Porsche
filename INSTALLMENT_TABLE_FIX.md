# Installment Table Fix - Manager Order Management

## Problem

In the **Manager Order Management** screen, when viewing order details, the **installment table** only shows **one item** even though the order contains multiple cars and parts.

### Example:
**Order contains:**
- 911 Carrera S (qty: 1, price: $120,000)
- Taycan 4S (qty: 1, price: $95,000)
- Wheel Center Caps (qty: 4, price: $120)
- Floor Mats (qty: 2, price: $285)
- LED Door Projectors (qty: 2, price: $450)

**Installment table shows:**
- Only 1 item instead of all 5 items ❌

## Root Cause

The stored procedure `getAllOrders` uses `DISTINCT` in the `GROUP_CONCAT` statements:

```sql
-- WRONG (lines 44-45 in optimized_getAllOrders.sql):
GROUP_CONCAT(DISTINCT od.qty SEPARATOR ',') AS carsandparts_qty,
GROUP_CONCAT(DISTINCT od.total_price SEPARATOR ',') AS carsandparts_perprice
```

### Why This Causes the Problem:

When you have multiple items with the same quantity (e.g., qty=1), `DISTINCT` removes the duplicates:

| Item | Quantity | Price |
|------|----------|-------|
| Car 1 | 1 | 120000 |
| Car 2 | 1 | 95000 |
| Part 1 | 4 | 120 |
| Part 2 | 2 | 285 |
| Part 3 | 2 | 450 |

**With DISTINCT:**
- Quantities: `"1,4,2"` (only 3 values - duplicates removed!)
- Prices: `"120000,95000,120,285,450"` (5 values - all unique)
- Names: `"911 Carrera S,Taycan 4S,Caps,Mats,Projectors"` (5 values)

**Result:** Arrays have different lengths → Only first 3 items display!

## Solution

Remove `DISTINCT` and add `ORDER BY` to maintain proper item order:

```sql
-- FIXED:
GROUP_CONCAT(
    CASE 
        WHEN od.car_id IS NOT NULL THEN CONCAT(cm.model_name, ' ', cm.trim_name)
        WHEN od.part_id IS NOT NULL THEN cp.part_name
    END 
    ORDER BY od.detail_id
    SEPARATOR ','
) AS carsandparts_name,
GROUP_CONCAT(od.qty ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_qty,
GROUP_CONCAT(od.total_price ORDER BY od.detail_id SEPARATOR ',') AS carsandparts_perprice
```

### Key Changes:

1. ✅ **Removed `DISTINCT`** from qty and price concatenation
2. ✅ **Added `ORDER BY od.detail_id`** to maintain consistent order across all three fields
3. ✅ **Improved car name display** by joining with `car_models` table to show full name (model + trim)
4. ✅ **Removed `DISTINCT` from names** to ensure all items are included

## How It Works

### Controller Code (managerOrderManagementController.java):

```java
private void orderDetails(managerOrderView orders) {
    String[] names = orders.getCarsandparts_name();   // Split by comma
    String[] qty = orders.getCarsandparts_qty();       // Split by comma
    String[] price = orders.getCarsandparts_perprice(); // Split by comma
    
    // Find minimum length to avoid ArrayIndexOutOfBoundsException
    int minLength = Math.min(Math.min(names.length, qty.length), price.length);
    
    // Populate table with all items
    for (int i = 0; i < minLength; i++) {
        String rowData = String.format("%s|%s|%s", names[i], qty[i], price[i]);
        installmentTable.getItems().add(rowData);
    }
}
```

### Model Code (managerOrderView.java):

```java
public String[] getCarsandparts_name() {
    return carsandparts_name.split(",");  // Split comma-separated string
}

public String[] getCarsandparts_qty() {
    return carsandparts_qty.split(",");
}

public String[] getCarsandparts_perprice() {
    return carsandparts_perprice.split(",");
}
```

## Deployment

### Option 1: PowerShell Script (Recommended)
```powershell
.\apply_getAllOrders_fix.ps1
```

### Option 2: MySQL Command Line
```bash
mysql -u your_username -p your_database < database/getAllOrders_FIXED.sql
```

### Option 3: MySQL Workbench
1. Open `database/getAllOrders_FIXED.sql`
2. Execute the script

## Testing

After deployment:

1. **Restart your Java application**
2. Navigate to **Manager Order Management**
3. Select an order that has multiple items
4. Check the **installment table** on the right side

### Expected Results:

✅ All cars and parts in the order should be displayed
✅ Each row shows: Item Name | Quantity | Price
✅ Number of rows matches total items in order

### Test Cases:

**Test 1: Order with multiple cars**
- Order has 2 cars (both qty=1)
- Should show 2 rows in installment table

**Test 2: Order with multiple parts**
- Order has 3 parts (qty=1,2,4)
- Should show 3 rows in installment table

**Test 3: Order with cars and parts**
- Order has 2 cars + 3 parts
- Should show 5 rows in installment table

**Test 4: Order with duplicate quantities**
- Order has 5 items (all qty=1)
- Should show 5 rows, not 1 row

## Before vs After

### Before (With DISTINCT):
```
Installment Table:
┌────────────────────┬─────┬──────────┐
│ Item Name          │ Qty │ Price    │
├────────────────────┼─────┼──────────┤
│ 911 Carrera S      │ 1   │ 120000   │
└────────────────────┴─────┴──────────┘
(Only 1 item shown - others hidden due to DISTINCT)
```

### After (Without DISTINCT):
```
Installment Table:
┌────────────────────┬─────┬──────────┐
│ Item Name          │ Qty │ Price    │
├────────────────────┼─────┼──────────┤
│ 911 Carrera S      │ 1   │ 120000   │
│ Taycan 4S          │ 1   │ 95000    │
│ Wheel Center Caps  │ 4   │ 120      │
│ Floor Mats Set     │ 2   │ 285      │
│ LED Door Projectors│ 2   │ 450      │
└────────────────────┴─────┴──────────┘
(All 5 items displayed correctly!)
```

## Technical Details

### Database Schema:
- `orders` table: Contains order header info
- `order_details` table: Contains line items (one row per car/part)
- `detail_id`: Primary key in order_details (used for ordering)

### GROUP_CONCAT Behavior:
- **Without DISTINCT**: Returns all values, including duplicates
- **With DISTINCT**: Returns only unique values
- **ORDER BY**: Ensures consistent order across multiple GROUP_CONCAT calls

### Why ORDER BY detail_id:
- Ensures names, quantities, and prices are in the same order
- Prevents mismatched data (e.g., wrong price for wrong item)
- Uses primary key for reliable ordering

## Troubleshooting

### Still showing only one item?

1. **Check procedure was updated:**
   ```sql
   SHOW CREATE PROCEDURE getAllOrders;
   ```
   Look for `DISTINCT` - it should NOT be there for qty and price

2. **Check database connection:**
   - Make sure you updated the correct database
   - Verify connection string in Java application

3. **Restart application:**
   - Java caches database connections
   - Full restart required to pick up procedure changes

### Items in wrong order?

- Make sure `ORDER BY od.detail_id` is present in all three GROUP_CONCAT statements
- Check that detail_id exists in order_details table

### Missing car names?

- Verify `car_models` table has data
- Check JOIN between `cars` and `car_models` tables

## Files Modified

1. `database/getAllOrders_FIXED.sql` - Fixed stored procedure
2. `apply_getAllOrders_fix.ps1` - Deployment script

## Summary

The installment table issue was caused by the `DISTINCT` keyword in the stored procedure, which removed duplicate quantities and caused array length mismatches. By removing `DISTINCT` and adding `ORDER BY`, all items now display correctly in the installment table.

**Impact:** ✅ All order items now visible in Manager Order Management screen!
