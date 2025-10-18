# Installment Table Setup Fix - Manager Staff View

## Changes Made

Applied the same installment table setup from `managerOrderManagementController` to `managerStaffViewController` for consistent behavior and better text wrapping.

## Problem

The installment table column setup was being done **inside the `orderDetails()` method**, which meant:
- ❌ Cell factories were recreated every time an order was clicked
- ❌ Inefficient and unnecessary overhead
- ❌ No text wrapping for long item names
- ❌ Inconsistent with managerOrderManagementController

## Solution

Moved the installment table setup to the **`initialize()` method** where it belongs, and added text wrapping for the name column.

## Changes in Detail

### ✅ 1. Moved to initialize() Method (Lines 352-389)

**Before:** Cell value factories were in `orderDetails()` method
**After:** Cell value factories are in `initialize()` method (set up once)

```java
// Set up installment table columns
installmentNameCol.setCellValueFactory(cellData -> {
    String[] parts = cellData.getValue().split("\\|");
    return new SimpleStringProperty(parts.length > 0 ? parts[0] : "");
});

// Add text wrapping to name column
installmentNameCol.setCellFactory(column -> {
    TableCell<String, String> cell = new TableCell<String, String>() {
        private final Text text = new Text();
        
        {
            text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
            setGraphic(text);
        }
        
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                text.setText(null);
            } else {
                text.setText(item);
            }
        }
    };
    return cell;
});

installmentQtyCol.setCellValueFactory(cellData -> {
    String[] parts = cellData.getValue().split("\\|");
    return new SimpleStringProperty(parts.length > 1 ? parts[1] : "");
});

installmentPriceCol.setCellValueFactory(cellData -> {
    String[] parts = cellData.getValue().split("\\|");
    return new SimpleStringProperty(parts.length > 2 ? parts[2] : "");
});
```

### ✅ 2. Added Text Wrapping (Lines 359-379)

**New Feature:** Long item names now wrap to multiple lines instead of being cut off.

**Implementation:**
```java
installmentNameCol.setCellFactory(column -> {
    TableCell<String, String> cell = new TableCell<String, String>() {
        private final Text text = new Text();
        
        {
            // Bind text width to column width minus padding
            text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
            setGraphic(text);
        }
        
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                text.setText(null);
            } else {
                text.setText(item);
            }
        }
    };
    return cell;
});
```

**Benefits:**
- ✅ Long names like "Porsche 911 Carrera S Cabriolet" wrap to multiple lines
- ✅ No text cutoff with "..."
- ✅ Better readability
- ✅ Professional appearance

### ✅ 3. Cleaned up orderDetails() Method (Lines 614-621)

**Before:**
```java
// Clear and populate the installment table
installmentTable.getItems().clear();

for (int i = 0; i < names.length; i++) {
    String rowData = String.format("%s|%s|%s", names[i].trim(), qty[i].trim(), price[i].trim());
    installmentTable.getItems().add(rowData);
}

// Set up table columns to display the data properly ❌ DUPLICATE!
installmentNameCol.setCellValueFactory(cellData -> {
    String[] parts = cellData.getValue().split("\\|");
    return new SimpleStringProperty(parts.length > 0 ? parts[0] : "");
});
// ... more duplicate setup
```

**After:**
```java
// Clear and populate the installment table
installmentTable.getItems().clear();

for (int i = 0; i < names.length; i++) {
    String rowData = String.format("%s|%s|%s", names[i].trim(), qty[i].trim(), price[i].trim());
    installmentTable.getItems().add(rowData);
}
// ✅ Clean! No duplicate setup
```

## Benefits

### 1. **Performance Improvement**
- Cell factories created **once** at initialization
- Not recreated every time an order is clicked
- Faster order detail loading

### 2. **Better Text Display**
- Long item names wrap to multiple lines
- No text cutoff
- Easier to read full item names

### 3. **Code Quality**
- Follows JavaFX best practices
- Initialization code in `initialize()` method
- Display code in `orderDetails()` method
- Clear separation of concerns

### 4. **Consistency**
- Same pattern as `managerOrderManagementController`
- Easier to maintain
- Predictable behavior

## Visual Comparison

### Before (No Text Wrapping):
```
| Name                          | Quantity | Price    |
|-------------------------------|----------|----------|
| Porsche 911 Carrera S Cab...  | 1        | 120000.00|
| Taycan 4S Cross Turismo E...  | 1        | 95000.00 |
```
❌ Names cut off with "..."

### After (With Text Wrapping):
```
| Name                          | Quantity | Price    |
|-------------------------------|----------|----------|
| Porsche 911 Carrera S         | 1        | 120000.00|
| Cabriolet                     |          |          |
|-------------------------------|----------|----------|
| Taycan 4S Cross Turismo       | 1        | 95000.00 |
| E-Hybrid                      |          |          |
```
✅ Full names visible with wrapping

## Code Flow

### Initialization (Once):
1. Application starts
2. `initialize()` method runs
3. Installment table columns configured
4. Text wrapping set up
5. Ready to display data

### Runtime (Every order click):
1. User clicks on an order
2. `orderDetails()` method runs
3. Clears old data from table
4. Adds new data to table
5. Table automatically formats using pre-configured cell factories
6. Text wraps as needed

## Testing

### Test 1: Short Names
1. Select an order with short item names (e.g., "Wheel Caps")
2. ✅ Names display normally in single line
3. ✅ No unnecessary wrapping

### Test 2: Long Names
1. Select an order with long item names (e.g., "Porsche 911 Carrera S Cabriolet")
2. ✅ Names wrap to multiple lines
3. ✅ Full name visible
4. ✅ No "..." cutoff

### Test 3: Multiple Orders
1. Click on order 1 → Details display
2. Click on order 2 → Details update
3. Click on order 3 → Details update
4. ✅ Fast switching between orders
5. ✅ No lag or delay

### Test 4: Mixed Items
1. Select an order with cars and parts
2. ✅ Car names wrap if long
3. ✅ Part names display correctly
4. ✅ All items visible

## Comparison with managerOrderManagementController

Both controllers now have **identical** installment table setup:

| Feature | managerOrderManagementController | managerStaffViewController |
|---------|----------------------------------|----------------------------|
| Cell factories in initialize() | ✅ | ✅ |
| Text wrapping for names | ✅ | ✅ |
| Qty column setup | ✅ | ✅ |
| Price column setup | ✅ | ✅ |
| Clean orderDetails() method | ✅ | ✅ |

## Files Modified

- **`src/main/java/Controllers/managerStaffViewController.java`**
  - Lines 352-389: Added installment table setup in initialize()
  - Lines 614-621: Cleaned up orderDetails() method

## Summary

✅ **Moved installment table setup to initialize() method**
✅ **Added text wrapping for name column**
✅ **Removed duplicate cell factory setup from orderDetails()**
✅ **Improved performance (setup once vs every click)**
✅ **Better text display for long item names**
✅ **Consistent with managerOrderManagementController**

The installment table in Manager Staff View now has the same professional setup as Manager Order Management, with better text wrapping and improved performance!
