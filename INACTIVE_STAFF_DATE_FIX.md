# Inactive Staff Date Box Fix

## Issue
For **inactive staff**, the date boxes (month and year) were showing **today's date** instead of their **end_date** (termination date).

---

## The Problem

### Before (Lines 504-509)
```java
highlightSelectedCard(staff.getId());
selectedStaffId = staff.getId();

currentDateSelect();  // ❌ Always sets to TODAY
insertMonthYearChoiceBox(staff);
updateChoiceBoxes();
```

**What `currentDateSelect()` does:**
```java
private void currentDateSelect() {
    currentMonth = today.getMonthValue();  // ❌ Always today
    currentYear = today.getYear();         // ❌ Always today
}
```

**Result:** Even for inactive staff who were terminated months ago, the date boxes showed the current month/year.

---

## The Fix

### After (Lines 507-516) ✅
```java
highlightSelectedCard(staff.getId());
selectedStaffId = staff.getId();

// For inactive staff, set date to their end_date instead of today
// (isInactive already declared above at line 488)
if (isInactive && staff.getEnd_date() != null) {
    // Set to end_date for inactive staff
    currentMonth = staff.getEnd_date().getMonthValue();  // ✅ Use end_date
    currentYear = staff.getEnd_date().getYear();         // ✅ Use end_date
} else {
    // Set to today for active staff
    currentDateSelect();  // ✅ Only for active staff
}

insertMonthYearChoiceBox(staff);
updateChoiceBoxes();
```

---

## How It Works Now

### For Active Staff:
1. User clicks on an active staff card
2. `showStaffDetails()` is called
3. `isInactive = false`
4. Calls `currentDateSelect()` → Sets to **today's date**
5. Date boxes show current month/year
6. User can view current performance data

### For Inactive Staff:
1. User clicks on an inactive staff card
2. `showStaffDetails()` is called
3. `isInactive = true` and `end_date` exists
4. Sets `currentMonth` and `currentYear` to **end_date** values
5. Date boxes show the month/year when they were terminated
6. User can view their final performance data

---

## Example Scenarios

### Scenario 1: Active Staff
```
Staff: John Doe
Status: Active
Start Date: 2023-01-15
End Date: NULL

→ Date boxes show: December 2024 (today)
→ User can see current performance
```

### Scenario 2: Inactive Staff (Terminated)
```
Staff: Jane Smith
Status: Inactive
Start Date: 2022-06-01
End Date: 2024-08-31 (terminated in August)
Reason: "Resigned"

→ Date boxes show: August 2024 (end_date)
→ User can see final month's performance
→ Termination reason is displayed
```

### Scenario 3: Inactive Staff (No End Date)
```
Staff: Bob Johnson
Status: Inactive
Start Date: 2021-03-10
End Date: NULL (data error)

→ Date boxes show: December 2024 (fallback to today)
→ System handles gracefully
```

---

## Database Schema

The `end_date` is retrieved from the database in the `createCards` stored procedure:

```sql
SELECT 
    u.user_id,
    u.user_name,
    u.user_email,
    u.user_address,
    u.dob,
    u.user_status,
    s.start_date,
    s.end_date,      -- ✅ This is the termination date
    s.reason         -- ✅ Termination reason
FROM users u
JOIN staff s ON u.user_id = s.user_id
WHERE s.manager_id = ? 
  AND u.user_status = (? = 'active' ? 1 : 0)
```

---

## User Model

The `user` class has these fields:

```java
private LocalDate start_date;  // When staff joined
private LocalDate end_date;    // When staff was terminated (NULL if active)
private String is_active;      // "Active" or "Inactive"
private String reason;         // Termination reason (NULL if active)

public LocalDate getEnd_date() {
    return end_date;
}

public String getIs_active() {
    return is_active;
}
```

---

## UI Behavior

### Date Box Range

The `insertMonthYearChoiceBox()` method already handles the range correctly:

```java
private void insertMonthYearChoiceBox(user staff) {
    yearBox.getItems().clear();
    LocalDate end = (staff.getEnd_date() != null) ? staff.getEnd_date() : today;
    
    // Populate years from start_date to end_date (or today)
    for (int y = staff.getStart_date().getYear(); y <= end.getYear(); y++) {
        yearBox.getItems().add(y);
    }
    
    yearBox.setValue(currentYear);
    updateMonthBoxForYear(currentYear, staff);
}
```

**For inactive staff:**
- Year range: `start_date.year` to `end_date.year`
- Month range: Limited to months they worked
- Default selection: **end_date** (their last month)

**For active staff:**
- Year range: `start_date.year` to `today.year`
- Month range: Up to current month
- Default selection: **today** (current month)

---

## Benefits

✅ **Accurate Data Display:** Shows performance data from the staff's final working month
✅ **Better UX:** Users immediately see the relevant time period for inactive staff
✅ **Logical Default:** End date makes more sense than today's date for terminated staff
✅ **Consistent Behavior:** Active staff still default to today as expected
✅ **Graceful Fallback:** If end_date is NULL, falls back to today's date

---

## Testing

### Test Case 1: View Active Staff
1. Click on an active staff card
2. **Expected:** Date boxes show current month/year
3. **Verify:** Can navigate to any month from start_date to today

### Test Case 2: View Inactive Staff
1. Click on an inactive staff card
2. **Expected:** Date boxes show their end_date month/year
3. **Verify:** Termination reason is displayed
4. **Verify:** Can navigate months from start_date to end_date

### Test Case 3: Switch Between Staff
1. Click active staff → See current date
2. Click inactive staff → See end_date
3. Click back to active staff → See current date again
4. **Expected:** Date boxes update correctly each time

### Test Case 4: Inactive Staff Without End Date
1. Click inactive staff with NULL end_date
2. **Expected:** Date boxes show current month/year (fallback)
3. **Verify:** No errors or crashes

---

## Code Location

**File:** `d:\Porsche\src\main\java\Controllers\managerStaffViewController.java`

**Method:** `showStaffDetails(user staff)` (Lines 461-523)

**Lines Changed:** 507-516

---

## Related Features

This fix works together with:

1. **Termination Reason Display** (Lines 487-502)
   - Shows reason box only for inactive staff
   - Displays termination reason text

2. **Date Range Limiting** (Lines 1197-1238)
   - Restricts available months/years based on employment period
   - Prevents selecting dates outside employment range

3. **Navigation Buttons** (Lines 1150-1160)
   - Disables next/previous buttons at boundaries
   - Prevents navigating beyond start_date or end_date

---

## Summary

**Before:** Inactive staff always showed today's date ❌
**After:** Inactive staff show their end_date (termination date) ✅

This provides a more logical and useful default view for reviewing terminated staff performance data.
