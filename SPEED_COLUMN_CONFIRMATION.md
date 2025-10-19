# Speed Column Implementation - Confirmation

## ✅ CONFIRMED: Both Speed Fields Use SAME Database Column

### Database Schema
```sql
-- Single column stores both values combined
car_speed VARCHAR(50)  -- Stores: "250 + 4.5"
```

### Implementation Flow

#### 1. **Reading from Database (setEditCar)**
```
Database Column: car_speed
Value: "250 + 4.5"
        ↓
editPath.getSpeed() returns "250 + 4.5"
        ↓
Split by "+"
        ↓
editCarSpeed  = "250"    (Top Speed)
editCarSpeed2 = "4.5"    (0-100 km/h)
```

**Code (Lines 1968-1984):**
```java
// Split combined speed value (e.g., "250 + 4.5" -> "250" and "4.5")
if (editCarSpeed != null && editCarSpeed2 != null) {
    String speedValue = editPath.getSpeed();  // ← Gets from car_speed column
    if (speedValue != null && !speedValue.isEmpty()) {
        String[] speedParts = speedValue.split("\\+");
        editCarSpeed.setText(speedParts[0].trim());   // "250"
        if (speedParts.length > 1) {
            editCarSpeed2.setText(speedParts[1].trim()); // "4.5"
        } else {
            editCarSpeed2.setText("");
        }
    }
}
```

---

#### 2. **Writing to Database (updateCar)**
```
editCarSpeed  = "250"    (Top Speed)
editCarSpeed2 = "4.5"    (0-100 km/h)
        ↓
Combine with " + "
        ↓
combinedSpeed = "250 + 4.5"
        ↓
Stored in: car_speed column
```

**Code (Lines 2406-2411):**
```java
// Combine both speed values (speed + second value) - same as insertCar
String speed1 = editCarSpeed.getText().trim();   // "250"
String speed2 = editCarSpeed2.getText().trim();  // "4.5"
String combinedSpeed = speed1 + (speed2.isEmpty() ? "" : " + " + speed2);
// Result: "250 + 4.5"
```

**Stored Procedure Call (Line 2459):**
```java
cs.setString(10, combinedSpeed);  // in_car_speed → saves to car_speed column
```

---

## Database Column Mapping

| UI Field | Variable | Database Column | Example Value |
|----------|----------|-----------------|---------------|
| Top Speed | `editCarSpeed` | `car_speed` | "250" (split from "250 + 4.5") |
| 0-100 km/h | `editCarSpeed2` | `car_speed` | "4.5" (split from "250 + 4.5") |
| **Combined** | `combinedSpeed` | **`car_speed`** | **"250 + 4.5"** |

---

## Key Points

✅ **Single Column:** Only ONE database column (`car_speed`) is used
✅ **Split on Load:** The combined value is split into two UI fields when editing
✅ **Combine on Save:** The two UI fields are combined back into one value when saving
✅ **Same Format:** Uses the same "X + Y" format as the add car section
✅ **Consistent:** Both add and edit sections work identically

---

## Example Scenarios

### Scenario 1: Car with Both Values
```
Database: car_speed = "320 + 3.8"
    ↓ Load (setEditCar)
UI: editCarSpeed = "320", editCarSpeed2 = "3.8"
    ↓ User edits to "330" and "3.5"
    ↓ Save (updateCar)
Database: car_speed = "330 + 3.5"
```

### Scenario 2: Car with Only Top Speed
```
Database: car_speed = "280"
    ↓ Load (setEditCar)
UI: editCarSpeed = "280", editCarSpeed2 = ""
    ↓ User adds "4.2" to second field
    ↓ Save (updateCar)
Database: car_speed = "280 + 4.2"
```

### Scenario 3: Car with No Speed Data
```
Database: car_speed = NULL or ""
    ↓ Load (setEditCar)
UI: editCarSpeed = "", editCarSpeed2 = ""
    ↓ User enters "300" and "4.0"
    ↓ Save (updateCar)
Database: car_speed = "300 + 4.0"
```

---

## Stored Procedure

### updateFullCar Procedure
```sql
CALL updateFullCar(
    in_car_id,           -- 1
    in_car_name,         -- 2
    in_car_color,        -- 3
    in_interior_color,   -- 4
    in_fuel_type,        -- 5
    in_production_year,  -- 6
    in_car_qty,          -- 7
    in_price,            -- 8
    in_photo_url,        -- 9
    in_car_speed,        -- 10 ← Combined speed: "250 + 4.5"
    in_car_description   -- 11
)
```

**Parameter 10 (`in_car_speed`):**
- Receives: `combinedSpeed` variable
- Value: `"250 + 4.5"` (combined from both UI fields)
- Saves to: `car_speed` column in `cars` table

---

## Model Class (inventory.java)

```java
private String speed;  // Stores combined value from car_speed column

public String getSpeed() {
    return speed;  // Returns: "250 + 4.5"
}

public void setSpeed(String speed) {
    this.speed = speed;  // Sets: "250 + 4.5"
}
```

---

## Summary

🎯 **The implementation is correct:**

1. ✅ Both `editCarSpeed` and `editCarSpeed2` read from the **same** `car_speed` column
2. ✅ The value is **split** when loading (e.g., "250 + 4.5" → "250" and "4.5")
3. ✅ The values are **combined** when saving (e.g., "250" + "4.5" → "250 + 4.5")
4. ✅ Only **ONE** database column is used (`car_speed`)
5. ✅ The format is consistent with the add car section

**No separate columns exist for top speed and acceleration. Everything is stored in the single `car_speed` column.**

---

## Files Involved

1. **Database:** `cars` table → `car_speed` column (VARCHAR)
2. **Model:** `inventory.java` → `speed` field + getters/setters
3. **Controller:** `managerInventoryController.java`
   - `setEditCar()` - splits the value
   - `updateCar()` - combines the value
4. **FXML:** `managerInventory.fxml`
   - `editCarSpeed` - TextField for top speed
   - `editCarSpeed2` - TextField for 0-100 km/h
5. **Stored Procedure:** `updateFullCar` - receives combined value

---

## Verification

To verify this is working correctly, you can:

1. **Check Database:**
   ```sql
   SELECT car_id, car_name, car_speed FROM cars;
   ```
   You should see values like: `"250 + 4.5"`, `"320 + 3.8"`, etc.

2. **Test Edit:**
   - Open a car for editing
   - Check if speed fields populate correctly
   - Modify values
   - Save and check database

3. **Check Terminal Output:**
   - No errors related to speed fields
   - Application running smoothly ✅ (as shown in terminal)
