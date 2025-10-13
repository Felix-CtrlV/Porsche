# Manager Dashboard - Complete Flow Flowchart

## Overview
This document provides a comprehensive flowchart of the Manager Dashboard system, covering all navigation paths, user interactions, and data flows.

---

## 1. Login & Authentication Flow

```
START (Login Screen)
    │
    ├─→ User enters Username & Password
    │
    ├─→ Click Login Button
    │
    ├─→ Validate Input
    │   ├─→ Empty? → Show Error → Return to Input
    │   └─→ Valid → Call DB login()
    │
    ├─→ Database Authentication
    │   ├─→ Failed → Show Error → Return to Login
    │   └─→ Success → Get User Data
    │
    ├─→ Check User Role
    │   ├─→ Admin → Admin Dashboard
    │   ├─→ Manager → Manager Dashboard ✓
    │   └─→ Staff → Staff Dashboard
    │
    ├─→ Create Session (Store User Info)
    │
    └─→ Load Manager Dashboard (managerDashboard.fxml)
```

**Key Components:**
- `loginController.java` - Handles login logic
- `Session.java` - Manages user session data
- Database Procedure: `login(username, password, timestamp)`

---

## 2. Manager Dashboard Main Screen

```
Manager Dashboard Initialize
    │
    ├─→ Load User Profile Data
    │   ├─→ Username
    │   ├─→ Email
    │   ├─→ Address
    │   └─→ Date of Birth
    │
    ├─→ Setup Navigation Buttons
    │   ├─→ Overview Button (Default Active)
    │   ├─→ Inventory Button
    │   ├─→ Orders Button
    │   ├─→ Staffs Button
    │   └─→ Settings Icon
    │
    ├─→ Load Default View (Overview)
    │
    └─→ Setup Window Close Handler (Auto Logout)
```

**Navigation Options:**

| Button | Action | Loads FXML | Controller |
|--------|--------|-----------|------------|
| Overview | `clickOverview()` | `managerOverview.fxml` | `managerOverviewController` |
| Inventory | `clickInventory()` | `managerInventory.fxml` | `managerInventoryController` |
| Orders | `clickOrders()` | `managerOrderManagement.fxml` | `managerOrderManagementController` |
| Staffs | `clickStaffs()` | `managerStaffview.fxml` | `managerStaffViewController` |
| Settings | `clickSetting()` | Opens Side Panel | Same controller |
| Logout | `clickLogout()` | Returns to Login | `loginController` |

---

## 3. Overview View Detailed Flow

```
Overview Screen Initialize
    │
    ├─→ Get Manager ID from Session
    │
    ├─→ Setup Date Controls
    │   ├─→ Current Month/Year
    │   ├─→ Year ChoiceBox (2000 - Current Year)
    │   ├─→ Month ChoiceBox (Dynamic based on year)
    │   └─→ Previous/Next Buttons
    │
    ├─→ Initialize Sales Performance Section
    │   ├─→ Bar Chart (Cars & Parts Sold)
    │   ├─→ Area Chart (Revenue)
    │   └─→ ComboBox (Daily/Weekly/Monthly)
    │
    ├─→ Initialize Best Sellers Section
    │   ├─→ Default: Best Cars
    │   ├─→ Tabs: Cars | Parts | Staff
    │   └─→ Load Dynamic Cards
    │
    ├─→ Initialize Attendance & Target Carousel
    │   ├─→ Slide 1: Attendance Table & Pie Chart
    │   └─→ Slide 2: Target Progress Circles
    │
    └─→ Load Initial Data
```

### 3.1 Sales Performance Charts Flow

```
User Action: Select Time Period (Daily/Weekly/Monthly)
    │
    ├─→ Call Database: getSalesChartData(month, year, period)
    │
    ├─→ Retrieve Data:
    │   ├─→ Date/Week/Month Labels
    │   ├─→ Cars Sold Quantity
    │   ├─→ Parts Sold Quantity
    │   └─→ Revenue Amount
    │
    ├─→ Update Bar Chart
    │   ├─→ Clear Previous Data
    │   ├─→ Add Car Series
    │   └─→ Add Part Series
    │
    └─→ Update Area Chart
        ├─→ Clear Previous Data
        └─→ Add Revenue Series
```

### 3.2 Best Sellers Section Flow

```
User Action: Click Tab (Car/Part/Staff)
    │
    ├─→ Set Active Button Style
    │
    ├─→ Call Database Based on Selection:
    │   ├─→ Cars: getBestSellingCars(managerId, month, year)
    │   ├─→ Parts: getBestSellingParts(managerId, month, year)
    │   └─→ Staff: getBestStaff(managerId, month, year)
    │
    ├─→ Retrieve Data:
    │   ├─→ For Cars/Parts:
    │   │   ├─→ Rank
    │   │   ├─→ Target Quantity
    │   │   ├─→ Sold Quantity
    │   │   └─→ Inventory Name
    │   └─→ For Staff:
    │       ├─→ Rank
    │       ├─→ Staff ID & Name
    │       ├─→ Work Hours
    │       ├─→ Total Sales
    │       └─→ Photo
    │
    ├─→ Load FXML Cards:
    │   ├─→ Cars/Parts: managerCarPartProgressBar.fxml
    │   └─→ Staff: bestSeller.fxml
    │
    └─→ Display in ScrollPane
```

### 3.3 Attendance & Target Carousel Flow

```
Carousel Slide 1: Attendance View
    │
    ├─→ Load Attendance Table
    │   ├─→ Call: getAttendanceView()
    │   ├─→ Columns: Worker Name | Sign-In Time | Status
    │   └─→ Display Today's Attendance
    │
    ├─→ Load Attendance Pie Chart
    │   ├─→ Call: getAttendancePercentage()
    │   ├─→ Data: Attended Workers vs Total Workers
    │   └─→ Display Percentage
    │
    └─→ Arrow Right Button → Transition to Slide 2

Carousel Slide 2: Target View
    │
    ├─→ Call Database: targetViewChart(managerId, month, year)
    │
    ├─→ Retrieve Target Data:
    │   ├─→ Car Target & Achieved
    │   └─→ Part Target & Achieved
    │
    ├─→ Update Car Circle Progress
    │   ├─→ Calculate Progress Percentage
    │   ├─→ Set Circle Stroke Offset
    │   ├─→ Display Achievement Status
    │   └─→ Show +/- Difference
    │
    ├─→ Update Part Circle Progress
    │   ├─→ Calculate Progress Percentage
    │   ├─→ Set Circle Stroke Offset
    │   ├─→ Display Achievement Status
    │   └─→ Show +/- Difference
    │
    └─→ Arrow Left Button → Transition to Slide 1
```

### 3.4 Date Selection Flow

```
User Changes Date (Month/Year)
    │
    ├─→ Update Current Month/Year Variables
    │
    ├─→ Enable/Disable Navigation Buttons
    │   ├─→ Disable Next if Current Month/Year
    │   └─→ Enable Previous (unless at start date)
    │
    ├─→ Update Month ChoiceBox Options
    │   └─→ Limit to available months for selected year
    │
    ├─→ Refresh All Data:
    │   ├─→ Sales Charts
    │   ├─→ Best Sellers
    │   ├─→ Target Progress
    │   └─→ Attendance Data
    │
    └─→ Update UI Display
```

---

## 4. Inventory View Detailed Flow

```
Inventory Screen Initialize
    │
    ├─→ Load Cars Data from Database
    │
    ├─→ Load Parts Data from Database
    │
    ├─→ Setup Table Columns
    │   ├─→ ID
    │   ├─→ Name
    │   ├─→ Quantity
    │   ├─→ Price
    │   ├─→ Status
    │   └─→ Actions (Edit/Delete)
    │
    ├─→ Setup Filters
    │   ├─→ Series CheckBoxes (718, 911, Cayenne, etc.)
    │   └─→ Models CheckBoxes (Dynamic)
    │
    ├─→ Setup ComboBox (Cars/Parts)
    │
    ├─→ Setup Search Bar
    │
    └─→ Display Cars Table (Default)
```

### 4.1 View Toggle Flow

```
User Action: Select Cars/Parts from ComboBox
    │
    ├─→ Cars Selected?
    │   ├─→ Show Series Filter Panel
    │   ├─→ Show Models Filter Panel
    │   ├─→ Apply Selected Filters
    │   └─→ Display Filtered Cars Table
    │
    └─→ Parts Selected?
        ├─→ Hide Series Filter Panel
        ├─→ Hide Models Filter Panel
        └─→ Display All Parts Table
```

### 4.2 Add Item Flow

```
User Action: Click Add Button
    │
    ├─→ Show Add Pane (Overlay)
    │
    ├─→ Display Tabs: Add Car | Add Part
    │
    ├─→ User Selects Tab
    │
    ├─→ Add Car Tab:
    │   ├─→ Fields: Model, Trim, Year, Ext Color, Int Color
    │   ├─→ Fuel Type Radio Buttons
    │   ├─→ Electric CheckBox
    │   ├─→ Quantity, Price
    │   ├─→ Image Upload (Drag & Drop or Click)
    │   └─→ Confirm Button
    │
    ├─→ Add Part Tab:
    │   ├─→ Fields: Name, Quantity, Price, Description
    │   ├─→ For Car ComboBox
    │   ├─→ Image Upload (Drag & Drop or Click)
    │   └─→ Confirm Button
    │
    ├─→ User Fills Form & Clicks Confirm
    │
    ├─→ Validate Data
    │   ├─→ Invalid → Show Error
    │   └─→ Valid → Proceed
    │
    ├─→ Insert to Database
    │
    ├─→ Show Success Alert
    │
    ├─→ Clear Form
    │
    ├─→ Close Add Pane
    │
    └─→ Refresh Table
```

### 4.3 Edit Item Flow

```
User Action: Click Edit Icon in Action Column
    │
    ├─→ Load Item Data
    │
    ├─→ Show Edit Pane (Overlay)
    │
    ├─→ Populate Fields with Current Data
    │   ├─→ For Cars: All car fields
    │   └─→ For Parts: All part fields
    │
    ├─→ User Modifies Fields
    │
    ├─→ User Clicks Apply
    │
    ├─→ Check for Changes
    │   ├─→ No Changes → Show Warning → Close
    │   └─→ Changes Detected → Proceed
    │
    ├─→ Validate Modified Data
    │   ├─→ Invalid → Show Error
    │   └─→ Valid → Proceed
    │
    ├─→ Update Database
    │
    ├─→ Show Success Alert
    │
    ├─→ Close Edit Pane
    │
    └─→ Refresh Table
```

### 4.4 Delete Item Flow

```
User Action: Click Delete Icon in Action Column
    │
    ├─→ Show Confirmation Alert
    │   └─→ "Do you really want to delete this item?"
    │
    ├─→ User Confirms?
    │   ├─→ No → Cancel Operation
    │   └─→ Yes → Proceed
    │
    ├─→ Delete from Database
    │
    ├─→ Show Success Alert
    │
    └─→ Refresh Table
```

### 4.5 Export CSV Flow

```
User Action: Click Export CSV Button
    │
    ├─→ Open File Chooser Dialog
    │
    ├─→ User Selects Save Location & Filename
    │
    ├─→ Get Current Table Items
    │
    ├─→ Check if Data Exists
    │   ├─→ No Data → Show Warning
    │   └─→ Has Data → Proceed
    │
    ├─→ Determine Item Type (Cars or Parts)
    │
    ├─→ Write CSV Header
    │   ├─→ Cars: ID, Name, Series, Models, Colors, Fuel, Year, Qty, Price, Status
    │   └─→ Parts: ID, Name, For Car, Description, Qty, Price, Status
    │
    ├─→ Write Data Rows
    │   └─→ Escape special characters (commas, quotes)
    │
    ├─→ Save File
    │
    └─→ Show Success Alert with File Path
```

### 4.6 Search Flow

```
User Action: Enter Text in Search Bar
    │
    ├─→ User Clicks Search or Presses Enter
    │
    ├─→ Filter Table Items by:
    │   ├─→ ID (contains search text)
    │   ├─→ Name (contains search text)
    │   └─→ Other relevant fields
    │
    ├─→ Display Filtered Results
    │
    └─→ Clear Search → Show All Items
```

### 4.7 Filter by Series/Models Flow

```
User Action: Select/Deselect Series CheckBox
    │
    ├─→ Update Models CheckBoxes
    │   └─→ Show models for selected series
    │
    ├─→ Apply Filter to Cars Table
    │   └─→ Show only cars matching selected series/models
    │
    └─→ Update Table Display
```

---

## 5. Order Management View Detailed Flow

```
Order Management Screen Initialize
    │
    ├─→ Setup Order Table Columns
    │   ├─→ Order Date
    │   ├─→ Customer Name
    │   ├─→ Price
    │   ├─→ Staff Name
    │   └─→ Status (Installment Yes/No)
    │
    ├─→ Setup Installment Table Columns
    │   ├─→ Item Name
    │   ├─→ Quantity
    │   └─→ Price
    │
    ├─→ Setup Statistics Panel
    │   ├─→ Confirmed Orders Count
    │   ├─→ Pending Orders Count
    │   ├─→ Total Price
    │   └─→ Price Rate
    │
    ├─→ Setup Revenue Chart
    │
    ├─→ Setup Date Controls
    │
    └─→ Load Initial Data
```

### 5.1 View Orders Flow

```
Display Orders Table
    │
    ├─→ Show All Orders for Current Month/Year
    │
    ├─→ User Clicks Order Row
    │
    ├─→ Load Order Details
    │   ├─→ Customer Name
    │   ├─→ Staff Name
    │   ├─→ Total Amount
    │   ├─→ Paid Amount
    │   ├─→ Remaining Amount
    │   └─→ Due Date
    │
    ├─→ Load Order Items into Installment Table
    │   ├─→ Item Names
    │   ├─→ Quantities
    │   └─→ Prices
    │
    └─→ Display in Details Panel
```

### 5.2 Search Orders Flow

```
User Action: Enter Search Text
    │
    ├─→ User Clicks Search or Presses Enter
    │
    ├─→ Search by:
    │   ├─→ Customer Name
    │   ├─→ Order ID
    │   └─→ Staff Name
    │
    ├─→ Filter Orders Table
    │
    └─→ Display Matching Results
```

### 5.3 Date Filter Flow

```
User Changes Month/Year
    │
    ├─→ Update Current Month/Year
    │
    ├─→ Enable/Disable Navigation Buttons
    │
    ├─→ Reload Orders for Selected Period
    │
    ├─→ Update Statistics
    │   ├─→ Recalculate Confirmed Orders
    │   ├─→ Recalculate Pending Orders
    │   ├─→ Recalculate Total Price
    │   └─→ Recalculate Price Rate
    │
    └─→ Update Revenue Chart
```

---

## 6. Staff View Detailed Flow

```
Staff View Screen Initialize
    │
    ├─→ Get Manager ID from Session
    │
    ├─→ Setup Date Controls (Current Month/Year)
    │
    ├─→ Load Active Staff Cards
    │
    ├─→ Setup Staff List Container
    │
    ├─→ Setup Staff Details Panel
    │
    ├─→ Setup Performance Panel
    │   ├─→ Orders Table
    │   ├─→ Target Progress Circles
    │   ├─→ Attendance Circle
    │   └─→ Order Status Labels
    │
    └─→ Select First Staff (Default)
```

### 6.1 Load Staff Cards Flow

```
Initialize or Toggle Active/Inactive
    │
    ├─→ Clear Staff List Container
    │
    ├─→ Call Database: createCards(managerId, status)
    │   └─→ status = "active" or "inactive"
    │
    ├─→ Retrieve Staff Data:
    │   ├─→ User ID
    │   ├─→ Name
    │   ├─→ Phone
    │   ├─→ Email
    │   ├─→ Address
    │   ├─→ DOB
    │   ├─→ Status
    │   ├─→ Start Date
    │   └─→ End Date
    │
    ├─→ For Each Staff:
    │   ├─→ Load FXML: userCards.fxml
    │   ├─→ Set Card Data (ID, Name, Status)
    │   ├─→ Add Click Event Handler
    │   └─→ Add to Staff List Container
    │
    └─→ Select First Staff or Previously Selected
```

### 6.2 Show Staff Details Flow

```
User Action: Click Staff Card
    │
    ├─→ Highlight Selected Card
    │
    ├─→ Store Selected Staff ID
    │
    ├─→ Display Staff Personal Info
    │   ├─→ Name
    │   ├─→ Phone
    │   ├─→ Email
    │   ├─→ Address
    │   └─→ Date of Birth
    │
    ├─→ Initialize Date Range
    │   ├─→ Year ChoiceBox (Start Year to End Year/Current)
    │   └─→ Month ChoiceBox (Dynamic based on year)
    │
    ├─→ Load Performance Data
    │   ├─→ Orders Table
    │   ├─→ Monthly Order Status
    │   ├─→ Target Progress
    │   └─→ Attendance Percentage
    │
    └─→ Show Target View (Default)
```

### 6.3 Load Staff Orders Flow

```
Load Orders for Selected Staff
    │
    ├─→ Call Database: getOrdersByUserId(staffId, month, year)
    │
    ├─→ Retrieve Order Data:
    │   ├─→ Order Number
    │   ├─→ Order ID
    │   ├─→ Customer Name
    │   ├─→ Order Date
    │   ├─→ Total Amount
    │   ├─→ Installment Status
    │   ├─→ Items (Names, Quantities, Prices)
    │   ├─→ Paid Amount
    │   ├─→ Remaining Amount
    │   └─→ Due Date
    │
    ├─→ Populate Orders Table
    │
    └─→ Clear Selection
```

### 6.4 View Order Details Flow

```
User Action: Click Order in Orders Table
    │
    ├─→ Hide Target View
    │
    ├─→ Show Installment Pane
    │
    ├─→ Display Order Summary
    │   ├─→ Total Price
    │   ├─→ Paid Amount
    │   ├─→ Remaining Amount
    │   └─→ Due Date
    │
    ├─→ Load Items into Installment Table
    │   ├─→ Parse Items Array
    │   ├─→ Parse Quantities Array
    │   ├─→ Parse Prices Array
    │   └─→ Display in Table
    │
    └─→ Show Installment Pane
```

### 6.5 Load Target Progress Flow

```
Load Target Data for Selected Staff
    │
    ├─→ Call Database: targetviewchart(staffId, month, year)
    │
    ├─→ Retrieve Target Data:
    │   ├─→ Car Target
    │   ├─→ Car Achieved
    │   ├─→ Part Target
    │   └─→ Part Achieved
    │
    ├─→ Calculate Car Progress
    │   ├─→ Progress Percentage = Achieved / Target
    │   ├─→ Difference = Achieved - Target
    │   ├─→ Set Circle Stroke Offset
    │   └─→ Display Status Message
    │
    ├─→ Calculate Part Progress
    │   ├─→ Progress Percentage = Achieved / Target
    │   ├─→ Difference = Achieved - Target
    │   ├─→ Set Circle Stroke Offset
    │   └─→ Display Status Message
    │
    └─→ Update UI
```

### 6.6 Load Attendance Flow

```
Load Attendance for Selected Staff
    │
    ├─→ Call Database: getMonthlyAttendance(staffId, month, year)
    │
    ├─→ Retrieve Attendance Data:
    │   ├─→ Present Days
    │   ├─→ Absent Days
    │   └─→ Attendance Percentage
    │
    ├─→ Calculate Circle Progress
    │   └─→ Progress = Percentage / 100
    │
    ├─→ Update Attendance Circle
    │   ├─→ Set Stroke Dash Offset
    │   └─→ Display Percentage
    │
    └─→ Show/Hide Circles Based on Data
```

### 6.7 Search Staff Flow

```
User Action: Type in Search Bar
    │
    ├─→ Show Suggestions (ContextMenu)
    │   └─→ Filter staff by ID or Name
    │
    ├─→ Display Matching Suggestions
    │   └─→ Format: "ID - Name"
    │
    ├─→ User Selects Suggestion or Presses Enter
    │
    ├─→ Find Matching Staff
    │   ├─→ Match by ID
    │   └─→ Match by Name
    │
    ├─→ Show Staff Details
    │
    └─→ Hide Suggestions
```

### 6.8 Monthly Performance Update Flow

```
User Changes Month/Year
    │
    ├─→ Update Current Month/Year Variables
    │
    ├─→ Validate Date Range
    │   ├─→ Check against Staff Start Date
    │   └─→ Check against Staff End Date or Current Date
    │
    ├─→ Enable/Disable Navigation Buttons
    │   ├─→ Disable Previous if at Start Date
    │   └─→ Disable Next if at Current Date
    │
    ├─→ Update Month ChoiceBox Options
    │
    ├─→ Refresh All Performance Data:
    │   ├─→ Orders Table
    │   ├─→ Monthly Order Status
    │   ├─→ Target Progress
    │   └─→ Attendance Percentage
    │
    └─→ Reset to Target View
```

---

## 7. Settings Panel Flow

```
User Action: Click Settings Icon
    │
    ├─→ Open Settings Panel
    │   ├─→ Slide In Animation (300ms)
    │   ├─→ Blur Main Content
    │   ├─→ Show Overlay
    │   └─→ Disable Main Content
    │
    ├─→ Display Settings Options:
    │   ├─→ View Profile
    │   ├─→ Change Password
    │   └─→ Setup 2FA
    │
    └─→ User Clicks Option or Overlay to Close
```

### 7.1 View Profile Flow

```
User Action: Click Profile Option
    │
    ├─→ Open Authentication Modal
    │   └─→ Step: "password"
    │
    ├─→ User Enters Password
    │
    ├─→ Validate Password
    │   ├─→ Incorrect → Show Error
    │   └─→ Correct → Proceed
    │
    ├─→ Show Profile Pane
    │   ├─→ Display Profile Name
    │   ├─→ Display Email
    │   ├─→ Display Address
    │   └─→ Display DOB
    │
    └─→ Close Button → Hide Profile Pane
```

### 7.2 Change Password Flow

```
User Action: Click Change Password Option
    │
    ├─→ Open Authentication Modal
    │   └─→ Step: "password"
    │
    ├─→ User Enters Current Password
    │
    ├─→ Validate Password
    │   ├─→ Incorrect → Show Error
    │   └─→ Correct → Proceed
    │
    ├─→ Proceed with OTP Flow
    │   └─→ (Implementation in authenticationController)
    │
    └─→ Close Modal
```

### 7.3 Setup 2FA Flow

```
User Action: Click 2FA Setup Option
    │
    ├─→ Open Authentication Modal
    │   └─→ Step: "factor"
    │
    ├─→ Display 2FA Setup Interface
    │
    └─→ User Completes Setup
```

### 7.4 Close Settings Flow

```
User Action: Click Overlay or Close
    │
    ├─→ Close Settings Panel
    │   ├─→ Slide Out Animation (300ms)
    │   ├─→ Remove Blur from Main Content
    │   ├─→ Hide Overlay
    │   └─→ Enable Main Content
    │
    └─→ Return to Main Dashboard
```

---

## 8. Logout Flow

```
User Action: Click Logout Button
    │
    ├─→ Get Current User ID from Session
    │
    ├─→ Call LogoutHelper.performLogout(userId)
    │   └─→ Update database logout timestamp
    │
    ├─→ Close Manager Dashboard Window
    │
    ├─→ Clear Session Data
    │
    ├─→ Open Login Screen
    │
    └─→ END
```

---

## 9. Window Close Handler Flow

```
User Action: Close Window (X button)
    │
    ├─→ Trigger Window Close Event
    │
    ├─→ Get Current User ID from Session
    │
    ├─→ Call LogoutHelper.performLogout(userId)
    │   └─→ Update database logout timestamp
    │
    ├─→ Open Login Screen
    │
    └─→ Close Current Window
```

---

## Key Database Procedures Used

| Procedure Name | Parameters | Purpose |
|----------------|------------|---------|
| `login` | username, password, timestamp | Authenticate user |
| `getSalesChartData` | month, year, period | Get sales data for charts |
| `getBestSellingCars` | managerId, month, year | Get best selling cars |
| `getBestSellingParts` | managerId, month, year | Get best selling parts |
| `getBestStaff` | managerId, month, year | Get best performing staff |
| `targetViewChart` | managerId/staffId, month, year | Get target progress |
| `getAttendanceView` | - | Get today's attendance |
| `getAttendancePercentage` | - | Get attendance statistics |
| `createCards` | managerId, status | Get staff list |
| `getOrdersByUserId` | staffId, month, year | Get staff orders |
| `getMonthlyOrderStatus` | staffId, month, year | Get order statistics |
| `getMonthlyAttendance` | staffId, month, year | Get attendance data |

---

## Key Java Classes

| Class | Purpose |
|-------|---------|
| `loginController` | Handles login authentication |
| `managerDashboardController` | Main dashboard navigation |
| `managerOverviewController` | Overview screen logic |
| `managerInventoryController` | Inventory management |
| `managerOrderManagementController` | Order viewing |
| `managerStaffViewController` | Staff performance tracking |
| `authenticationController` | Password verification |
| `Session` | User session management |
| `LogoutHelper` | Logout operations |

---

## End of Flowchart Documentation
