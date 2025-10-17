# Porsche Database Schema Documentation

This document contains the complete database schema and sample data for the Porsche Car Sales Management System.

---

## Table of Contents
1. [Car Models](#car-models)
2. [Car Parts](#car-parts)
3. [Cars](#cars)
4. [Customer Info](#customer-info)
5. [Customize](#customize)
6. [Installment List](#installment-list)
7. [Order Details](#order-details)
8. [Orders](#orders)
9. [Photos](#photos)
10. [User Attendance](#user-attendance)
11. [User Info](#user-info)
12. [User Target](#user-target)
13. [User Work Info](#user-work-info)

---

## Car Models

**Table:** `car_models`

Stores information about different Porsche car models and their trims.

| Column Name  | Type    | Description                           |
|-------------|---------|---------------------------------------|
| model_id    | INT     | Primary key, unique model identifier  |
| model_name  | VARCHAR | Name of the car model (e.g., 911)     |
| trim_name   | VARCHAR | Trim level (e.g., Turbo S, GTS)       |
| model_photo | INT     | Foreign key to photos table           |

**Sample Data:**
- 911 Carrera S
- 911 Turbo S Cabriolet
- 718 Cayman GT4 RS
- Cayenne SE-Hybrid
- Panamera 4 E-Hybrid Executive
- Taycan 4S, Turbo, Turbo Cross Turismo
- Macan Turbo

**Total Records:** 17 models

---

## Car Parts

**Table:** `car_parts`

Stores aftermarket parts and accessories available for purchase.

| Column Name  | Type    | Description                              |
|-------------|---------|------------------------------------------|
| part_id     | INT     | Primary key, unique part identifier      |
| part_name   | VARCHAR | Name of the part/accessory               |
| for_car     | VARCHAR | Specific car model (if applicable)       |
| description | TEXT    | Detailed description of the part         |
| part_qty    | INT     | Available quantity in stock              |
| price       | DECIMAL | Price of the part                        |
| part_status | INT     | Status (1=active, 0=inactive)            |
| part_photo  | INT     | Foreign key to photos table              |

**Sample Parts:**
1. Porsche Logo Wheel Center Caps - $120
2. All-Season Floor Mats Set - $285.50
3. LED Door Projectors - $450.75
4. SportDesign Fuel Cap - $320
5. Porsche Wall Charger for EV/PHEV - $1,199.99
6. Illuminated Door Sill Guards - $875.50
7. Porsche Design Child Seat - $425
8. Porsche Cleaning and Care Kit - $89.99
9. Porsche Classic Car Cover - $425.75

**Total Records:** 10 parts

---

## Cars

**Table:** `cars`

Stores individual car inventory with specifications.

| Column Name      | Type    | Description                              |
|-----------------|---------|------------------------------------------|
| car_id          | INT     | Primary key, unique car identifier       |
| model_id        | INT     | Foreign key to car_models                |
| car_color       | VARCHAR | Exterior color                           |
| interior_color  | VARCHAR | Interior color/material                  |
| fuel_type       | VARCHAR | PET/DSL/HYB/ELEC                        |
| car_status      | INT     | Status (1=available, 0=sold)             |
| production_year | INT     | Year of manufacture                      |
| car_qty         | INT     | Available quantity                       |
| price           | DECIMAL | Price of the car                         |
| car_photo       | INT     | Foreign key to photos table              |

**Fuel Types:**
- **PET**: Petrol/Gasoline
- **DSL**: Diesel
- **HYB**: Hybrid (Petrol+Electric)
- **ELEC**: Electric

**Price Range:** $62,000 - $1,850,000

**Total Records:** 17 cars

---

## Customer Info

**Table:** `customer_info`

Stores customer information for order processing.

| Column Name       | Type     | Description                          |
|------------------|----------|--------------------------------------|
| customer_id      | INT      | Primary key, unique customer ID      |
| customer_name    | VARCHAR  | Full name of customer                |
| customer_nrc     | VARCHAR  | National Registration Card number    |
| customer_phone   | VARCHAR  | Contact phone number                 |
| customer_address | TEXT     | Full address                         |
| created_at       | DATETIME | Customer registration timestamp      |

**Total Records:** 40 customers

**Regions Covered:**
- Yangon, Mandalay, Bago, Shan State, Mon State, Naypyidaw, Ayeyarwady, Rakhine, Magway, Kayin State

---

## Customize

**Table:** `customize`

Stores customization options for cars (currently empty - feature for future use).

| Column Name         | Type    | Description                          |
|--------------------|---------|--------------------------------------|
| customize_id       | INT     | Primary key                          |
| detail_id          | INT     | Foreign key to order_details         |
| customization_type | VARCHAR | Type of customization                |
| customization_value| VARCHAR | Value/specification                  |
| customization_price| DECIMAL | Additional price for customization   |

**Total Records:** 0 (feature not yet implemented)

---

## Installment List

**Table:** `installment_list`

Tracks installment payment schedules for orders.

| Column Name        | Type    | Description                              |
|-------------------|---------|------------------------------------------|
| installment_id    | INT     | Primary key                              |
| order_id          | INT     | Foreign key to orders                    |
| installment_number| INT     | Installment sequence number              |
| due_date          | DATE    | Payment due date                         |
| installment_amount| DECIMAL | Amount due for this installment          |
| is_finished       | INT     | Payment status (0=pending, 1=completed)  |

**Total Records:** 68 installment entries

**Note:** All current installments show `is_finished = 0` (pending)

---

## Order Details

**Table:** `order_details`

Stores line items for each order (cars and parts).

| Column Name  | Type    | Description                              |
|-------------|---------|------------------------------------------|
| detail_id   | INT     | Primary key                              |
| order_id    | INT     | Foreign key to orders                    |
| is_customize| INT     | Whether item is customized (0/1)         |
| car_id      | INT     | Foreign key to cars (if car purchase)    |
| part_id     | INT     | Foreign key to car_parts (if part)       |
| qty         | INT     | Quantity ordered                         |
| total_price | DECIMAL | Total price for this line item           |

**Total Records:** 236 order detail entries

**Business Rules:**
- Each order detail has either a `car_id` OR `part_id`, not both
- `qty` is typically 1 for cars, can be multiple for parts
- `is_customize` currently always 0 (customization feature pending)

---

## Orders

**Table:** `orders`

Main order/transaction table.

| Column Name    | Type    | Description                              |
|---------------|---------|------------------------------------------|
| order_id      | INT     | Primary key                              |
| user_id       | INT     | Foreign key to user_info (staff)         |
| customer_id   | INT     | Foreign key to customer_info             |
| order_date    | DATE    | Date order was placed                    |
| order_status  | VARCHAR | confirm/pending                          |
| is_installment| INT     | Payment type (0=full, 1=installment)     |
| paid_amount   | DECIMAL | Amount already paid                      |

**Total Records:** 112 orders

**Order Status:**
- **confirm**: Order completed/confirmed
- **pending**: Order awaiting payment/processing

**Date Range:** 2020-10-15 to 2025-11-11

---

## Photos

**Table:** `photos`

Stores photo URLs and metadata.

| Column Name | Type    | Description                              |
|------------|---------|------------------------------------------|
| photo_id   | INT     | Primary key                              |
| photo_url  | VARCHAR | File path or URL to photo                |
| description| TEXT    | Description of photo                     |

**Total Records:** 12 photos

**Categories:**
- User profile photos (admin, manager, staff)
- Car model photos
- Part photos

---

## User Attendance

**Table:** `user_attendance`

Tracks employee check-in/check-out times.

| Column Name    | Type     | Description                          |
|---------------|----------|--------------------------------------|
| attendance_id | INT      | Primary key                          |
| user_id       | INT      | Foreign key to user_info             |
| check_in      | DATETIME | Check-in timestamp                   |
| check_out     | DATETIME | Check-out timestamp                  |
| reason        | VARCHAR  | Attendance status (on time/late)     |

**Total Records:** 13 attendance entries

**Attendance Reasons:**
- `on time`: Arrived on time
- `late`: Arrived late
- (empty): Standard attendance

---

## User Info

**Table:** `user_info`

Stores employee/user account information.

| Column Name  | Type     | Description                              |
|-------------|----------|------------------------------------------|
| user_id     | INT      | Primary key                              |
| user_name   | VARCHAR  | Full name                                |
| user_nrc    | VARCHAR  | National Registration Card number        |
| user_email  | VARCHAR  | Email address                            |
| user_address| TEXT     | Full address                             |
| user_phone  | VARCHAR  | Contact phone number                     |
| user_role   | VARCHAR  | admin/manager/staff                      |
| dob         | DATE     | Date of birth                            |
| start_date  | DATE     | Employment start date                    |
| user_status | INT      | Active status (1=active, 0=inactive)     |
| end_date    | DATE     | Employment end date (if applicable)      |
| password    | VARCHAR  | Hashed password (SHA-256)                |
| user_photo  | INT      | Foreign key to photos                    |
| reason      | VARCHAR  | Fired Reason                             |


**Total Records:** 11 users (7 active, 4 inactive)

**User Roles:**
- **admin** (1): Full system access
- **manager** (2): Management functions (2 active)
- **staff** (8): Sales and operations (5 active, 3 inactive)

**Password Hash:** All users currently use the same hashed password for testing

---

## User Target

**Table:** `user_target`

Stores monthly sales targets and achievements for users.

| Column Name    | Type    | Description                                      |
|---------------|---------|--------------------------------------------------|
| target_id     | INT     | Primary key                                      |
| user_id       | INT     | Foreign key to user_info                         |
| effective_date| DATE    | Date target becomes effective (YYYY-MM-01)       |
| target        | VARCHAR | Target in format "cars-X,parts-Y"                |
| achieve       | VARCHAR | Achievement in format "cars-X,parts-Y" (nullable)|

**Total Records:** 9 target entries

**Sample Data:**
- Manager (user_id=2): Target "cars-5,parts-10", Achieved "cars-5,parts-11"
- Staff (user_id=3-10): Monthly targets of 1 car, 1 part each
- Targets set for October 2020
- All entries include achievement data

**Format Examples:**
- Target: `"cars-10,parts-50"` (10 cars, 50 parts)
- Achieve: `"cars-1,parts-2"` (1 car sold, 2 parts sold)

---

## User Work Info

**Table:** `user_workinfo`

Stores employment and compensation details.

| Column Name    | Type    | Description                          |
|---------------|---------|--------------------------------------|
| workinfo_id   | INT     | Primary key                          |
| user_id       | INT     | Foreign key to user_info             |
| manager       | INT     | Foreign key to user_info (manager)   |
| salary_amount | DECIMAL | Monthly salary                       |
| bonus         | DECIMAL | Bonus amount                         |

**Total Records:** 12 work info entries

**Salary Structure:**
- Admin (user_id=1): 5,000 MMK
- Managers (user_id=2, 15): 2,500 MMK each
- Staff (user_id=3-11): 1,000 MMK each

**Reporting Structure:**
- Admin (user_id=1) has no manager
- Manager (user_id=2) reports to Admin (manager=1)
- Manager (user_id=15) has no manager
- All staff (user_id=3-11) report to Manager (manager=2)

**Note:** All bonuses currently set to 0

---

## Database Relationships

### Primary Relationships:

1. **cars** → **order_details** (one-to-many)
   - One car can appear in multiple orders

2. **car_parts** → **order_details** (one-to-many)
   - One part can appear in multiple orders

3. **customer_info** → **orders** (one-to-many)
   - One customer can place multiple orders

4. **user_info** → **orders** (one-to-many)
   - One staff member can process multiple orders

5. **orders** → **order_details** (one-to-many)
   - One order can contain multiple items (cars and parts)

6. **orders** → **installment_list** (one-to-many)
   - One order can have multiple installment payments

7. **order_details** → **customize** (one-to-many)
   - One order detail can have multiple customizations

8. **user_info** → **user_workinfo** (one-to-one)
   - Each user has one work info record

9. **user_info** → **user_attendance** (one-to-many)
   - One user can have multiple attendance records

10. **user_info** → **user_target** (one-to-many)
    - One user can have multiple monthly targets

---

## Business Logic Notes

### Order Processing:
- Orders can be paid in full (`is_installment = 0`) or via installments (`is_installment = 1`)
- Installment orders typically have 3 payment installments
- `paid_amount` tracks the initial down payment

### Inventory Management:
- `car_qty` and `part_qty` track available stock
- `car_status` and `part_status` indicate if items are active/available

### User Management:
- Three role levels: admin, manager, staff
- Manager ID is stored in `user_workinfo` to establish hierarchy
- `user_status` indicates active employees
- `end_date` is set when employment ends

### Attendance Tracking:
- Records check-in and check-out times
- Tracks lateness with `reason` field
- Used for payroll and performance monitoring

---

## Data Statistics

- **Total Customers:** 10
- **Total Users:** 11 (1 admin, 2 managers, 8 staff) - 7 active, 4 inactive
- **Total Cars in Inventory:** 19 unique configurations (18 active, 1 discontinued)
- **Total Parts:** 12 types (10 active, 2 inactive)
- **Total Orders:** 10
- **Total Order Details:** 10 line items
- **Date Range:** October 2020 - November 2020
- **Total Attendance Records:** 10
- **Total User Targets:** 9
- **Pending Installments:** 15 unpaid installments

---

## Common Queries

### Get all orders with customer and staff details:
```sql
SELECT o.order_id, c.customer_name, u.user_name as staff_name, 
       o.order_date, o.order_status, o.paid_amount
FROM orders o
JOIN customer_info c ON o.customer_id = c.customer_id
JOIN user_info u ON o.user_id = u.user_id
ORDER BY o.order_date DESC;
```

### Get order details with car/part information:
```sql
SELECT od.detail_id, o.order_id, 
       COALESCE(CONCAT(c.model_name, ' ', c.trim_name), cp.part_name) as item_name,
       od.qty, od.total_price
FROM order_details od
JOIN orders o ON od.order_id = o.order_id
LEFT JOIN cars c ON od.car_id = c.car_id
LEFT JOIN car_parts cp ON od.part_id = cp.part_id;
```

### Get pending installments:
```sql
SELECT il.installment_id, o.order_id, c.customer_name,
       il.due_date, il.installment_amount
FROM installment_list il
JOIN orders o ON il.order_id = o.order_id
JOIN customer_info c ON o.customer_id = c.customer_id
WHERE il.is_finished = 0
ORDER BY il.due_date;
```

### Get staff performance (total sales):
```sql
SELECT u.user_name, COUNT(o.order_id) as total_orders,
       SUM(od.total_price) as total_sales
FROM user_info u
JOIN orders o ON u.user_id = o.user_id
JOIN order_details od ON o.order_id = od.order_id
WHERE u.user_role = 'staff'
GROUP BY u.user_id, u.user_name
ORDER BY total_sales DESC;
```

---

*Last Updated: October 17, 2025*
