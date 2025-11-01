# Notification System Migration Guide

## Overview
All Alert boxes throughout the application are being replaced with:
1. **Toast Notifications** - For informational messages (success, error, warning, info)
2. **Confirmation Dialogs** - For user confirmations

## New Notification Manager

### Location
`Utils/NotificationManager.java`

### Usage

#### Toast Notifications
```java
// Success
NotificationManager.getInstance().showSuccess("Title", "Message");

// Error
NotificationManager.getInstance().showError("Title", "Message");

// Warning
NotificationManager.getInstance().showWarning("Title", "Message");

// Info
NotificationManager.getInstance().showInfo("Title", "Message");

// Custom type
NotificationManager.getInstance().showToast("Title", "Message", "type");
```

#### Confirmation Dialogs
```java
NotificationManager.getInstance().showConfirmation(
    "Confirm Action",
    "Are you sure?",
    () -> {
        // Code to execute on confirm
    }
);
```

## Migration Checklist

### Controllers to Update

#### 1. managerInventoryController
**File:** `d:\7.Java_Workspace\Porsche\src\main\java\Controllers\managerInventoryController.java`

**Replace:**
- Line 427: `showAlert("No Data", "There is no data to export.", Alert.AlertType.WARNING);`
  - With: `NotificationManager.getInstance().showWarning("No Data", "There is no data to export.");`

- Line 467-468: Export success alert
  - With: `NotificationManager.getInstance().showSuccess("Export Successful", "Data exported successfully to:\n" + file.getAbsolutePath());`

- Line 471-472: Export failed alert
  - With: `NotificationManager.getInstance().showError("Export Failed", "Failed to export data: " + e.getMessage());`

- Lines 1039-1042: Error adding item
  - With: `NotificationManager.getInstance().showError("Error", "Failed to add: " + e.getMessage());`

- Lines 1046-1061: Success alerts for add/edit/update
  - With: `NotificationManager.getInstance().showSuccess("Finished", "Successfully Added The Car");`

- Lines 1109-1117: Delete success/error
  - With: `NotificationManager.getInstance().showSuccess("Success", "Car has been marked as unavailable!");`

- Lines 1147-1155: Restore success/error
  - With: `NotificationManager.getInstance().showSuccess("Success", "Car has been restored successfully!");`

**Confirmation Dialogs:**
- Replace `alertController` dialogs with `NotificationManager.getInstance().showConfirmation()`

#### 2. AddAbsentDialogController
**File:** `d:\7.Java_Workspace\Porsche\src\main\java\Controllers\AddAbsentDialogController.java`

**Replace all `showAlert()` calls:**
- Line 265: Invalid input → `NotificationManager.getInstance().showError("Invalid Input", "Please fill all fields and select a date range.");`
- Line 272: Invalid date range → `NotificationManager.getInstance().showError("Invalid Date Range", "Start date cannot be after end date.");`
- Line 294: Staff not found → `NotificationManager.getInstance().showError("Error", "Staff member not found.");`
- Line 332: Success → `NotificationManager.getInstance().showSuccess("Success", recordsAdded + " absent record(s) added successfully.");`
- Line 335: No records added → `NotificationManager.getInstance().showWarning("No Records Added", "Records already exist for the selected date range.");`
- Line 341: Database error → `NotificationManager.getInstance().showError("Database Error", "Failed to add absent record: " + e.getMessage());`

#### 3. managerAttendanceManagementController
**File:** `d:\7.Java_Workspace\Porsche\src\main\java\Controllers\managerAttendanceManagementController.java`

**Replace:**
- Line 717: Access denied → `NotificationManager.getInstance().showWarning("Access Denied", "Only managers can add absent records.");`
- Line 756: Error opening dialog → `NotificationManager.getInstance().showError("Error", "Failed to open add absent dialog: " + e.getMessage());`

#### 4. staffToolsController
**File:** `d:\7.Java_Workspace\Porsche\src\main\java\Controllers\staffToolsController.java`

**Replace:**
- Line 255-259: Empty cart → `NotificationManager.getInstance().showInfo("Empty Cart", "Add some items to your cart first!");`
- Line 321-331: Clear cart confirmation → Use `NotificationManager.getInstance().showConfirmation()`
- Line 336-340: Empty cart on checkout → `NotificationManager.getInstance().showWarning("Empty Cart", "Add items before checking out.");`
- Line 351-355: Navigation error → `NotificationManager.getInstance().showError("Error", "Could not load checkout page.");`

#### 5. staffShoppingCartController
**File:** `d:\7.Java_Workspace\Porsche\src\main\java\Controllers\staffShopingCartController.java`

**Replace:**
- Line 537-541: Warning alert → `NotificationManager.getInstance().showWarning(title, message);`

## Implementation Steps

1. **Add import statement** to each controller:
   ```java
   import Utils.NotificationManager;
   ```

2. **Remove old methods** (if they exist):
   ```java
   private void showAlert(String title, String message, Alert.AlertType type) { ... }
   ```

3. **Replace all Alert instantiations** with NotificationManager calls

4. **For confirmation dialogs**, replace the old alert controller pattern with:
   ```java
   NotificationManager.getInstance().showConfirmation(
       "Title",
       "Message",
       () -> {
           // Action on confirm
       }
   );
   ```

## Toast Notification Types

| Type | Icon | Color | Usage |
|------|------|-------|-------|
| success | ✓ | Green (#10b981) | Successful operations |
| error | ✕ | Red (#ef4444) | Errors and failures |
| warning | ! | Amber (#f59e0b) | Warnings and cautions |
| info | ℹ | Blue (#3b82f6) | Information messages |

## Notes

- Toast notifications automatically hide after 3 seconds
- Confirmation dialogs remain until user clicks OK or Cancel
- All notifications are non-blocking and don't interrupt user workflow
- NotificationManager is a singleton - use `getInstance()` to access
- Notifications are thread-safe and use `Platform.runLater()` internally
