# Speed Fields Split Fix - Edit Car Section

## Problem
The edit car section had the acceleration field (0-100 km/h) as a **Label** (display-only), but it should be a **TextField** (editable) like in the add car section.

## Solution
Made the edit section match the add car section by:
1. Having TWO separate editable fields
2. Splitting the combined database value when loading
3. Combining them again when saving

---

## Changes Made

### 1. FXML File (`managerInventory.fxml`)

**Changed acceleration from Label to TextField (Line 731):**

```xml
<!-- BEFORE -->
<Label fx:id="editCarAcceleration" style="..." text="-" />

<!-- AFTER -->
<TextField fx:id="editCarSpeed2" prefHeight="40.0" prefWidth="120.0" 
           promptText="seconds" 
           style="-fx-background-radius: 6; -fx-border-radius: 6; 
                  -fx-background-color: #f8fafc; -fx-border-color: #d1d5db; 
                  -fx-padding: 0 12 0 12;" />
```

---

### 2. Controller File (`managerInventoryController.java`)

#### A. Added Field Declaration (Line 306)

```java
@FXML
private TextField editCarSpeed;   // Top speed field
@FXML
private TextField editCarSpeed2;  // 0-100 km/h acceleration field ✨ NEW
@FXML
private TextArea editCarDescription;
```

#### B. Updated `setEditCar()` - Split Combined Value (Lines 1968-1984)

```java
// Populate speed and description fields
// Split combined speed value (e.g., "250 + 4.5" -> "250" and "4.5")
if (editCarSpeed != null && editCarSpeed2 != null) {
    String speedValue = editPath.getSpeed();
    if (speedValue != null && !speedValue.isEmpty()) {
        String[] speedParts = speedValue.split("\\+");
        editCarSpeed.setText(speedParts[0].trim());      // "250"
        if (speedParts.length > 1) {
            editCarSpeed2.setText(speedParts[1].trim()); // "4.5"
        } else {
            editCarSpeed2.setText("");
        }
    } else {
        editCarSpeed.setText("");
        editCarSpeed2.setText("");
    }
}
```

**How it works:**
- Database stores: `"250 + 4.5"`
- Split by `+` character
- First part → `editCarSpeed` (Top Speed)
- Second part → `editCarSpeed2` (0-100 km/h)

#### C. Updated `updateCar()` - Combine Values (Lines 2406-2411)

```java
// Combine both speed values (speed + second value) - same as insertCar
String speed1 = (editCarSpeed != null && editCarSpeed.getText() != null) 
                ? editCarSpeed.getText().trim() : "";
String speed2 = (editCarSpeed2 != null && editCarSpeed2.getText() != null) 
                ? editCarSpeed2.getText().trim() : "";
String combinedSpeed = speed1 + (speed2.isEmpty() ? "" : " + " + speed2);  // e.g., "250 + 4.5"
```

**How it works:**
- Read from `editCarSpeed`: `"250"`
- Read from `editCarSpeed2`: `"4.5"`
- Combine: `"250 + 4.5"`
- Save to database

#### D. Updated Stored Procedure Call (Line 2459)

```java
cs.setString(10, combinedSpeed);  // in_car_speed (combined: "250 + 4.5")
```

---

## Field Mapping

### Add Car Section (Already Working)
| Field | fx:id | Purpose | Example |
|-------|-------|---------|---------|
| Top Speed | `carSpeedText` | Maximum speed | "250" |
| 0-100 km/h | `carSpeed2Text` | Acceleration | "4.5" |
| **Combined** | - | Stored in DB | "250 + 4.5" |

### Edit Car Section (Now Fixed)
| Field | fx:id | Purpose | Example |
|-------|-------|---------|---------|
| Top Speed | `editCarSpeed` | Maximum speed | "250" |
| 0-100 km/h | `editCarSpeed2` | Acceleration | "4.5" |
| **Combined** | - | Stored in DB | "250 + 4.5" |

---

## Data Flow

### Loading (setEditCar)
```
Database: "250 + 4.5"
    ↓
Split by "+"
    ↓
editCarSpeed = "250"
editCarSpeed2 = "4.5"
```

### Saving (updateCar)
```
editCarSpeed = "250"
editCarSpeed2 = "4.5"
    ↓
Combine with " + "
    ↓
Database: "250 + 4.5"
```

---

## UI Layout

```
┌─────────────────────────────────────────┐
│  PERFORMANCE                            │
│  ┌──────────┐    ┌──────────┐          │
│  │TOP SPEED │    │0-100 km/h│          │
│  │  [250]   │    │  [4.5]   │          │
│  └──────────┘    └──────────┘          │
│   (editable)      (editable)           │
└─────────────────────────────────────────┘
```

**Before:** Only top speed was editable, acceleration was display-only
**After:** Both fields are editable ✅

---

## Consistency with Add Car Section

Both sections now work identically:

| Feature | Add Car | Edit Car | Status |
|---------|---------|----------|--------|
| Two separate fields | ✅ | ✅ | Matching |
| Both editable | ✅ | ✅ | Matching |
| Combined on save | ✅ | ✅ | Matching |
| Split on load | N/A | ✅ | Matching |
| Format: "X + Y" | ✅ | ✅ | Matching |

---

## Example Usage

### Scenario 1: Edit existing car with speed data
1. Database has: `"320 + 3.8"`
2. User clicks Edit
3. Top Speed field shows: `"320"`
4. 0-100 km/h field shows: `"3.8"`
5. User changes to: `"330"` and `"3.5"`
6. Click Apply
7. Database saves: `"330 + 3.5"`

### Scenario 2: Edit car without speed data
1. Database has: `null` or `""`
2. User clicks Edit
3. Both fields are empty
4. User enters: `"280"` and `"4.2"`
5. Click Apply
6. Database saves: `"280 + 4.2"`

### Scenario 3: Only top speed entered
1. User enters top speed: `"300"`
2. Leaves 0-100 km/h empty
3. Click Apply
4. Database saves: `"300"` (no " + " suffix)

---

## Benefits

✅ **Consistent UX** - Edit works same as Add
✅ **Both Fields Editable** - Users can update both values
✅ **Smart Splitting** - Handles various formats gracefully
✅ **Null-Safe** - Won't crash on missing data
✅ **Format Preserved** - Maintains "X + Y" format in database

---

## Files Modified

1. **d:\Porsche\src\main\resources\View\managerInventory.fxml**
   - Line 731: Changed Label to TextField with `fx:id="editCarSpeed2"`

2. **d:\Porsche\src\main\java\Controllers\managerInventoryController.java**
   - Line 306: Added `editCarSpeed2` field declaration
   - Lines 1968-1984: Added split logic in `setEditCar()`
   - Lines 2406-2411: Added combine logic in `updateCar()`
   - Line 2459: Updated to use `combinedSpeed`

---

## Testing Checklist

- [ ] Edit car with both speed values (e.g., "250 + 4.5")
- [ ] Verify both fields populate correctly
- [ ] Change both values
- [ ] Save and verify database has combined format
- [ ] Edit car with only top speed (e.g., "300")
- [ ] Verify second field is empty
- [ ] Add acceleration value
- [ ] Save and verify format is "300 + X"
- [ ] Edit car with no speed data
- [ ] Verify both fields are empty
- [ ] Enter values and save
- [ ] Verify database stores correctly
