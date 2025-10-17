# Order Table Debugging Guide

## Changes Made

### 1. Simplified Cell Factories
- **Removed Text nodes** that were causing rendering issues
- **Using setText()** directly for all string columns
- **Added explicit styling**: `-fx-text-fill: black; -fx-font-size: 12px;`
- **Cleared graphics**: `setGraphic(null)` to avoid conflicts

### 2. Added Debug Logging
When you run the application, check the console for:
```
DEBUG: Total orders loaded from DB: X
DEBUG: Current month/year filter: M/YYYY
DEBUG: Added order - Customer: [name], Date: [date]
DEBUG: Filtered orders count: X
```

### 3. Added Table Refresh
- Calling `orderTable.refresh()` after setting items
- Forces JavaFX to redraw the table

## How to Debug

### Step 1: Check Console Output
Run the application and look for debug messages:

**If you see:**
```
DEBUG: Total orders loaded from DB: 0
```
→ **Problem**: No data in database or wrong manager ID
→ **Solution**: Check database has orders for this manager

**If you see:**
```
DEBUG: Total orders loaded from DB: 10
DEBUG: Filtered orders count: 0
```
→ **Problem**: Date filter is too restrictive
→ **Solution**: Change month/year or check order dates in DB

**If you see:**
```
DEBUG: Filtered orders count: 5
```
→ **Problem**: Data is loaded but not visible
→ **Solution**: CSS or styling issue (see below)

### Step 2: Test Without Filter
Temporarily comment out the filter to show ALL orders:

```java
// In loadOrder() method, change:
if (currentMonth > 0 && currentYear > 0) {
```
to:
```java
if (false) {  // Temporarily disable filter
```

This will show all orders regardless of date.

### Step 3: Check Column Bindings
Verify PropertyValueFactory names match model getters:

| Column | PropertyValueFactory | Model Getter |
|--------|---------------------|--------------|
| orderDateCol | "order_date" | getOrder_date() |
| customerNameCol | "cus_name" | getCus_name() |
| staffNameCol | "staff_name" | getStaff_name() |
| qtyCol | "totalQty" | getTotalQty() |
| priceCol | "total_amount" | getTotal_amount() |
| statusCol | "is_installmenat" | getIs_installmenat() |

### Step 4: Check FXML IDs
Verify FXML fx:id matches controller @FXML fields:
- `fx:id="orderTable"` → `@FXML private TableView<managerOrderView> orderTable;`
- `fx:id="orderDateCol"` → `@FXML private TableColumn<managerOrderView, Date> orderDateCol;`

### Step 5: CSS Override Check
Check if external CSS is hiding text. Add this temporarily:

```java
orderTable.setStyle("-fx-text-fill: black !important; -fx-font-size: 14px !important;");
```

## Common Issues & Solutions

### Issue 1: Empty Table (No Rows)
**Symptoms**: Table is completely empty, no rows visible
**Causes**:
- No data in database
- Wrong manager ID
- Date filter too restrictive

**Solution**: Check debug output for row count

### Issue 2: Rows Visible But No Text
**Symptoms**: Can see row lines/hover effects but no text
**Causes**:
- Text color matches background
- Font size is 0
- CSS override

**Solution**: Check cell factory styling

### Issue 3: Some Columns Show, Others Don't
**Symptoms**: Date shows but names don't
**Causes**:
- PropertyValueFactory name mismatch
- Getter method missing/wrong name

**Solution**: Verify getter names in managerOrderView.java

### Issue 4: Text Cut Off/Clipped
**Symptoms**: Text partially visible
**Causes**:
- Column too narrow
- Row height fixed

**Solution**: Already fixed with `setFixedCellSize(-1)`

## Quick Test Code

Add this to `initialize()` method to test with dummy data:

```java
// TEST: Add dummy data
managerOrderView testOrder = new managerOrderView(
    1, new Date(), "Test Customer", "Test Staff", 
    5, 1000.0, "Yes", 500.0, 500.0, new Date(), 
    "Car1", "5", "200"
);
orderTable.getItems().add(testOrder);
orderTable.refresh();
System.out.println("TEST: Added dummy order");
```

If dummy data shows → Database/filter issue
If dummy data doesn't show → Rendering/styling issue

## Next Steps

1. **Run the application**
2. **Check console for DEBUG messages**
3. **Report what you see**:
   - How many orders loaded?
   - How many filtered?
   - Can you see rows (even empty)?
   - Can you see ANY text in ANY column?

This will help identify the exact problem!
