# Table Styling Fix - Manager Staff View

## Changes Made

Applied the same table logic and styling from `managerOrderManagementController` to `managerStaffViewController` for consistent UI/UX across the application.

## Features Added

### 1. ✅ Column Cell Factories with Formatting

**TotalAmount Column** (Lines 285-296):
- Formats currency with `$` symbol and 2 decimal places
- Centers text alignment
- Ensures black text color

```java
TotalAmountCol.setCellFactory(column -> new TableCell<managerOrderView, Double>() {
    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);
        setStyle("-fx-text-fill: black; -fx-alignment: CENTER;");
        if (empty || item == null) {
            setText(null);
        } else {
            setText(String.format("$%.2f", item));
        }
    }
});
```

**Date Column** (Lines 299-310):
- Formats date display
- Centers text alignment
- Ensures black text color

**IsInstallment Column** (Lines 313-324):
- Displays "Yes" or "No" clearly
- Centers text alignment
- Ensures black text color

### 2. ✅ Table-Level Styling (Lines 327-328)

```java
ordersTable.setFixedCellSize(-1);
ordersTable.setStyle("-fx-text-fill: black !important; -fx-font-size: 14px !important; -fx-background-color: white;");
```

**Benefits:**
- Fixed cell size for better performance
- Consistent font size (14px)
- White background
- Black text (visible and readable)

### 3. ✅ Row Factory with Interactive Styling (Lines 330-350)

**Features:**
- Dynamic row styling based on selection state
- Hover effects for better UX
- Smooth visual feedback

```java
ordersTable.setRowFactory(tv -> {
    TableRow<managerOrderView> row = new TableRow<>();
    
    // Update row style based on selection
    row.itemProperty().addListener((obs, oldItem, newItem) -> updateRowStyle(row));
    row.selectedProperty().addListener((obs, oldSelected, newSelected) -> updateRowStyle(row));
    
    row.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);

    row.setOnMouseEntered(e -> {
        if (!row.isEmpty() && !row.isSelected()) {
            row.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: black !important; -fx-font-size: 14px !important; -fx-font-weight: normal; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
        }
    });

    row.setOnMouseExited(e -> {
        updateRowStyle(row);
    });

    return row;
});
```

### 4. ✅ updateRowStyle Method (Lines 1165-1175)

**Purpose:** Centralized row styling logic

**Three States:**

1. **Empty Row:**
   ```java
   row.setStyle("");  // No styling
   ```

2. **Selected Row:**
   ```java
   // Blue highlight with bold text
   row.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: black !important; -fx-font-size: 14px !important; -fx-font-weight: bold; -fx-border-color: #2196f3; -fx-border-width: 0 0 1 0;");
   ```

3. **Normal Row:**
   ```java
   // White background with normal text
   row.setStyle("-fx-background-color: white; -fx-text-fill: black !important; -fx-font-size: 14px !important; -fx-font-weight: normal; -fx-border-color: #e9ecef; -fx-border-width: 0 0 1 0;");
   ```

## Visual Behavior

### Row States:

| State | Background | Text Color | Font Weight | Border |
|-------|------------|------------|-------------|--------|
| **Normal** | White | Black | Normal | Light gray bottom |
| **Hover** | Light gray (#f8f9fa) | Black | Normal | Light gray bottom |
| **Selected** | Light blue (#e3f2fd) | Black | **Bold** | Blue bottom (#2196f3) |

### User Experience:

1. **Hover Effect:**
   - Mouse enters row → Background changes to light gray
   - Mouse exits row → Returns to normal/selected state
   - Selected rows don't change on hover

2. **Selection Effect:**
   - Click row → Blue background, bold text, blue border
   - Clear visual indication of selected item
   - Order details automatically display

3. **Consistency:**
   - Same styling as Manager Order Management screen
   - Professional and modern appearance
   - Easy to read and navigate

## Comparison: Before vs After

### Before:
- ❌ No hover effects
- ❌ Unclear which row is selected
- ❌ Plain text display for currency
- ❌ Inconsistent styling
- ❌ Poor visual feedback

### After:
- ✅ Smooth hover effects
- ✅ Clear selection highlighting (blue background + bold text)
- ✅ Currency formatted with `$` symbol
- ✅ Consistent styling across all columns
- ✅ Professional appearance matching Manager Order Management

## Code Structure

### Initialization Flow (in `initialize()` method):

1. **Set Cell Value Factories** (Lines 278-282)
   - Define how to extract data from model

2. **Set Cell Factories** (Lines 285-324)
   - Define how to display/format the data

3. **Set Table Style** (Lines 327-328)
   - Overall table appearance

4. **Set Row Factory** (Lines 330-350)
   - Row-level styling and interactions

### Runtime Flow:

1. **Data Loads** → Rows created by row factory
2. **User Hovers** → `onMouseEntered` fires → Light gray background
3. **User Clicks** → Selection changes → `updateRowStyle` fires → Blue background + bold
4. **User Moves Away** → `onMouseExited` fires → Returns to selected/normal state

## Benefits

### 1. **Consistent User Experience**
- Same look and feel as Manager Order Management
- Users familiar with one screen can easily use the other

### 2. **Better Readability**
- Currency formatting makes amounts clear
- Bold text on selection draws attention
- Proper alignment improves scannability

### 3. **Professional Appearance**
- Modern UI design
- Smooth transitions
- Clear visual hierarchy

### 4. **Improved Usability**
- Hover effects provide feedback
- Selection is obvious
- Easy to track which order you're viewing

### 5. **Maintainability**
- Centralized styling in `updateRowStyle` method
- Easy to modify colors/styles in one place
- Consistent code structure

## Testing

### Test 1: Visual Appearance
1. Open Manager Staff View
2. Select a staff member
3. ✅ Table should have white background
4. ✅ Text should be black and readable
5. ✅ Currency should show `$` symbol

### Test 2: Hover Effects
1. Move mouse over different rows
2. ✅ Row should highlight with light gray on hover
3. ✅ Row should return to normal when mouse leaves
4. ✅ Selected row should NOT change on hover

### Test 3: Selection
1. Click on a row
2. ✅ Row should highlight with blue background
3. ✅ Text should become bold
4. ✅ Blue border should appear at bottom
5. ✅ Order details should display

### Test 4: Multiple Selections
1. Click row 1 → Blue highlight
2. Click row 2 → Row 1 returns to normal, Row 2 becomes blue
3. ✅ Only one row highlighted at a time

### Test 5: Data Formatting
1. Check TotalAmount column
2. ✅ Should show `$120.50` format (not `120.5`)
3. Check Date column
4. ✅ Should show date in readable format
5. Check IsInstallment column
6. ✅ Should show "Yes" or "No"

## Related Files

- **Controller:** `src/main/java/Controllers/managerStaffViewController.java`
- **Reference:** `src/main/java/Controllers/managerOrderManagementController.java`
- **FXML:** `src/main/resources/View/managerStaffview.fxml`

## Summary

✅ **Applied managerOrderManagementController table logic**
✅ **Added column formatting (currency, date, installment)**
✅ **Added row factory with hover and selection effects**
✅ **Added updateRowStyle method for centralized styling**
✅ **Consistent UI/UX across Manager screens**

The orders table in Manager Staff View now has the same professional appearance and interactive behavior as the Manager Order Management screen!
