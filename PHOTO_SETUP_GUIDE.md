# Photo Setup Guide for Cross-Computer Compatibility

## What Changed
The inventory system now automatically copies photos to a shared `Images` folder and stores relative paths instead of absolute paths in the database.

## Setup Instructions

### 1. Create Images Folder Structure
```
d:\Porsche\
├── src/
├── Images/          ← Create this folder
│   ├── cars/        ← Optional: organize by type
│   └── parts/       ← Optional: organize by type
└── database files
```

### 2. For New Computers
When setting up on a new computer:

1. **Copy the entire project folder** including the `Images` directory
2. **Ensure database connection** points to the shared database
3. **Photos will work automatically** because paths are now relative

### 3. How It Works Now

**Before (Problem):**
- Stored: `D:\User1\Desktop\car.jpg` 
- Won't work on User2's computer

**After (Solution):**
- Selected file gets copied to: `d:\Porsche\Images\car.jpg`
- Database stores: `Images/car.jpg`
- Works on any computer with the project

### 4. Alternative Solutions

#### Option A: Network Shared Folder (Recommended for Teams)
```java
// In resolveImagePath method, add network path support:
String networkPath = "\\\\ServerName\\SharedImages\\";
```

#### Option B: Cloud Storage Integration
- Use cloud storage APIs (Google Drive, OneDrive, etc.)
- Store cloud URLs instead of local paths

#### Option C: Database BLOB Storage
- Store images directly in database as BLOB
- No file path issues, but larger database size

## Migration for Existing Data

If you have existing inventory with absolute paths:

1. **Run this SQL to find absolute paths:**
```sql
SELECT id, photo_url FROM cars WHERE photo_url LIKE '%:\%';
SELECT id, photo_url FROM parts WHERE photo_url LIKE '%:\%';
```

2. **Copy existing images to Images folder**
3. **Update database paths:**
```sql
UPDATE cars SET photo_url = 'Images/' + RIGHT(photo_url, CHARINDEX('\', REVERSE(photo_url)) - 1) 
WHERE photo_url LIKE '%:\%';
```

## Testing
1. Add a new car/part with photo on Computer A
2. Copy project folder to Computer B  
3. Open application on Computer B
4. Photo should display correctly

## Troubleshooting
- If images don't show: Check `Images` folder exists and has correct permissions
- If copy fails: Check disk space and write permissions
- For network issues: Use UNC paths like `\\\\server\\share\\Images\\`
