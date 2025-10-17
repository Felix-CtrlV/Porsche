# Order Table Styling Guide

## What We Implemented

### **Normal State (Default)**
- All table rows display with **normal font weight** (not bold)
- Black text on white background
- Clean, readable appearance

### **Selected State (When Clicked)**
- Selected row becomes **bold**
- Light blue background (`#e3f2fd`)
- Blue border (`#2196f3`)
- Clearly distinguishes the active/selected row

### **Hover State**
- Light gray background (`#f8f9fa`) when hovering over unselected rows
- Normal font weight maintained during hover
- Selected rows maintain their bold styling even when hovered

## How It Works

### **Row Factory Logic**
```java
orderTable.setRowFactory(tv -> {
    TableRow<managerOrderView> row = new TableRow<>();
    
    // Listen for selection changes
    row.selectedProperty().addListener((obs, oldSelected, newSelected) -> updateRowStyle(row));
    
    // Handle mouse hover
    row.setOnMouseEntered(e -> {
        if (!row.isEmpty() && !row.isSelected()) {
            // Only change hover style for non-selected rows
        }
    });
    
    return row;
});
```

### **Dynamic Styling Method**
```java
private void updateRowStyle(TableRow<managerOrderView> row) {
    if (row.isSelected()) {
        // Bold text with blue highlight
        row.setStyle("...-fx-font-weight: bold...");
    } else {
        // Normal text
        row.setStyle("...-fx-font-weight: normal...");
    }
}
```

## Visual Behavior

### **When You Click a Row:**
1. ✅ **Previous selection** loses bold styling and returns to normal
2. ✅ **New selection** becomes bold with blue background
3. ✅ **Order details** display for the selected row
4. ✅ **Clear visual feedback** shows which row is active

### **When You Hover:**
1. ✅ **Unselected rows** get light gray background
2. ✅ **Selected row** maintains bold styling and blue background
3. ✅ **Smooth visual transitions**

## Benefits

- **Better UX**: Clear indication of which order is currently selected
- **Professional Look**: Clean, modern table styling
- **Accessibility**: High contrast and clear visual hierarchy
- **Consistency**: Follows standard UI patterns

## Testing

To verify it's working:
1. **Run the application**
2. **Click different rows** in the order table
3. **Observe**: Only the clicked row should be bold
4. **Hover over rows**: Should see light gray background on unselected rows
5. **Selection should persist** until you click another row

The table now provides clear visual feedback for user interactions while maintaining a clean, professional appearance!
