# No Order Placeholder - Orders Table

## Feature Added

When there are no orders for the selected staff member and date range, the system now displays a placeholder row in the **orders table** (not just the installment table) with a clear "No order data available" message.

## Changes Made

### ✅ 1. Added Placeholder Row to Orders Table (Lines 500-507)

When no orders exist, create a placeholder `managerOrderView` object:

```java
// Add "No order data" placeholder row to orders table
managerOrderView placeholderOrder = new managerOrderView();
placeholderOrder.setNo(0);
placeholderOrder.setCus_name("No order data available");
placeholderOrder.setOrder_date(null);
placeholderOrder.setTotal_amount(0.0);
placeholderOrder.setIs_installmenat("---");
ordersTable.getItems().add(placeholderOrder);
```

### ✅ 2. Updated Date Column to Show "---" for Null (Lines 304-305)

Changed the Date column cell factory to display "---" instead of blank when date is null:

```java
if (empty || item == null) {
    setText("---");  // ✅ Shows "---" instead of blank
} else {
    setText(item.toString());
}
```

### ✅ 3. Updated Due Date Label to Show "N/A" for Null (Line 628)

```java
dueDateLabel.setText(orders.getDue_date() != null ? String.valueOf(orders.getDue_date()) : "N/A");
```

## Visual Result

### Before This Fix:

**When no orders:**
```
Orders Table:
┌────────────────────────────────────────────────┐
│ [Empty - looks broken or loading]             │
│                                                │
│                                                │
└────────────────────────────────────────────────┘

Installment Table:
| No order data available | --- | --- |
```
❌ Orders table looks empty/broken

### After This Fix:

**When no orders:**
```
Orders Table:
┌────────────────────────────────────────────────┐
│ No | Customer Name           | Date | Amount   │
├────┼────────────────────────┼──────┼──────────┤
│ 0  | No order data available | ---  | $0.00    │
└────────────────────────────────────────────────┘

Installment Table:
| No order data available | --- | --- |
```
✅ Clear message in both tables!

## How It Works

### Scenario 1: Orders Available ✅

**User Actions:**
1. Select a staff member
2. View current month (has orders)

**System Response:**
```
Orders Table:
┌────────────────────────────────────────────────┐
│ No | Customer Name    | Date       | Amount    │
├────┼─────────────────┼────────────┼───────────┤
│ 1  | John Smith       | 2024-10-15 | $120,000  │
│ 2  | Jane Doe         | 2024-10-18 | $95,000   │
│ 3  | Bob Johnson      | 2024-10-20 | $450      │
└────────────────────────────────────────────────┘

Installment Table (for selected order):
| 911 Carrera S | 1 | 120000.00 |
| Taycan 4S     | 1 | 95000.00  |
```

### Scenario 2: No Orders Available ✅

**User Actions:**
1. Select a staff member
2. Change to a month with no orders

**System Response:**
```
Orders Table:
┌────────────────────────────────────────────────┐
│ No | Customer Name           | Date | Amount   │
├────┼────────────────────────┼──────┼──────────┤
│ 0  | No order data available | ---  | $0.00    │
└────────────────────────────────────────────────┘

Installment Table:
| No order data available | --- | --- |

Order Details:
Total Price: $0.00
Paid Amount: $0.00
Remain Amount: $0.00
Due Date: N/A
```

## Placeholder Row Details

### Column Values:

| Column | Value | Display |
|--------|-------|---------|
| **No** | 0 | `0` |
| **Customer Name** | "No order data available" | `No order data available` |
| **Date** | null | `---` |
| **Total Amount** | 0.0 | `$0.00` |
| **Installment** | "---" | `---` |

### Why These Values?

1. **No = 0**: Indicates placeholder (real orders start from 1)
2. **Customer Name = "No order data available"**: Clear message
3. **Date = null → "---"**: Shows placeholder, not an error
4. **Total Amount = 0.0 → "$0.00"**: Consistent formatting
5. **Installment = "---"**: Indicates no data

## User Experience Flow

### Flow 1: Switching Between Months

```
Month with orders → Month without orders → Month with orders
      ↓                      ↓                      ↓
  Shows data          Shows placeholder       Shows data
```

**Example:**
1. **October 2024** (5 orders)
   - Orders Table: 5 rows with real data
   - First order auto-selected
   - Details displayed

2. **November 2024** (0 orders)
   - Orders Table: 1 placeholder row
   - "No order data available" message
   - No selection
   - Installment table also shows placeholder

3. **December 2024** (3 orders)
   - Orders Table: 3 rows with real data
   - First order auto-selected
   - Details displayed

### Flow 2: Switching Between Staff Members

```
Staff A (has orders) → Staff B (no orders) → Staff C (has orders)
        ↓                      ↓                      ↓
   Shows data            Shows placeholder       Shows data
```

## Benefits

### 1. **Consistent UI**
- Both orders table AND installment table show messages
- No empty/blank tables
- Professional appearance

### 2. **Clear Communication**
- User knows immediately: no data (not loading, not error)
- "No order data available" is explicit
- "---" indicates placeholder values

### 3. **Better UX**
- No confusion about empty state
- Consistent with installment table behavior
- Matches Manager Order Management screen patterns

### 4. **Prevents Misunderstanding**
Empty table could mean:
- ❌ Still loading...
- ❌ Error occurred
- ❌ Bug/broken feature

Placeholder row clearly means:
- ✅ **No data for this period**

## Testing Scenarios

### Test 1: New Staff Member (No Orders Yet)
1. Select a newly hired staff member
2. ✅ Orders table shows placeholder row
3. ✅ Customer Name: "No order data available"
4. ✅ Date: "---"
5. ✅ Amount: "$0.00"
6. ✅ Installment: "---"

### Test 2: Future Month
1. Select any staff member
2. Navigate to a future month
3. ✅ Orders table shows placeholder row
4. ✅ Installment table shows placeholder

### Test 3: Past Month with No Sales
1. Select a staff member
2. Navigate to a past month with no sales
3. ✅ Orders table shows placeholder row
4. ✅ All columns show appropriate placeholder values

### Test 4: Switching Between Months
1. Start on current month (has orders)
2. ✅ Shows real order data
3. Navigate to empty month
4. ✅ Shows placeholder row
5. Navigate back to month with orders
6. ✅ Shows real order data again

### Test 5: Clicking Placeholder Row
1. Navigate to month with no orders
2. Try clicking the placeholder row
3. ✅ Nothing happens (no selection)
4. ✅ Details remain at default values

## Technical Details

### Placeholder Object Creation

```java
managerOrderView placeholderOrder = new managerOrderView();
placeholderOrder.setNo(0);                              // Row number
placeholderOrder.setCus_name("No order data available"); // Message
placeholderOrder.setOrder_date(null);                   // No date
placeholderOrder.setTotal_amount(0.0);                  // Zero amount
placeholderOrder.setIs_installmenat("---");             // Placeholder
ordersTable.getItems().add(placeholderOrder);           // Add to table
```

### Date Column Handling

```java
DateCol.setCellFactory(column -> new TableCell<managerOrderView, Date>() {
    @Override
    protected void updateItem(Date item, boolean empty) {
        super.updateItem(item, empty);
        setStyle("-fx-text-fill: black; -fx-alignment: CENTER;");
        if (empty || item == null) {
            setText("---");  // ✅ Shows "---" for null dates
        } else {
            setText(item.toString());
        }
    }
});
```

### Amount Column Formatting

The TotalAmount column already handles 0.0 correctly:
```java
setText(String.format("$%.2f", item));  // Shows "$0.00"
```

## Edge Cases Handled

### ✅ Empty List
```java
if (!orders.isEmpty()) {
    // Show real data
} else {
    // Show placeholder
}
```

### ✅ Null Date
```java
if (empty || item == null) {
    setText("---");  // Placeholder
}
```

### ✅ Zero Amount
```java
setText(String.format("$%.2f", 0.0));  // Shows "$0.00"
```

### ✅ No Selection
```java
ordersTable.getSelectionModel().clearSelection();  // Don't select placeholder
```

## Comparison with Manager Order Management

Both screens now have consistent behavior:

| Feature | Manager Order Management | Manager Staff View |
|---------|--------------------------|-------------------|
| Placeholder row when no orders | ✅ | ✅ |
| "No order data available" message | ✅ | ✅ |
| "---" for null dates | ✅ | ✅ |
| "$0.00" for zero amounts | ✅ | ✅ |
| No selection on placeholder | ✅ | ✅ |

## Summary

✅ **Added placeholder row to orders table**
✅ **Shows "No order data available" in Customer Name column**
✅ **Shows "---" for Date and Installment columns**
✅ **Shows "$0.00" for Total Amount**
✅ **Updated Date column to handle null values**
✅ **Updated Due Date label to show "N/A" for null**
✅ **Consistent with installment table behavior**
✅ **Professional and clear user experience**

The Manager Staff View now provides clear visual feedback in BOTH the orders table and installment table when there are no orders, preventing confusion and improving the overall user experience!
