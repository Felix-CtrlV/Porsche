# PowerShell script to apply the getSalesChartData procedure fix
# This script updates the stored procedure to include car_qty and part_qty columns

Write-Host "=== Applying getSalesChartData Procedure Fix ===" -ForegroundColor Cyan
Write-Host ""

# Database connection parameters
$DB_HOST = Read-Host "Enter MySQL host (default: localhost)"
if ([string]::IsNullOrWhiteSpace($DB_HOST)) { $DB_HOST = "localhost" }

$DB_PORT = Read-Host "Enter MySQL port (default: 3306)"
if ([string]::IsNullOrWhiteSpace($DB_PORT)) { $DB_PORT = "3306" }

$DB_NAME = Read-Host "Enter database name"
$DB_USER = Read-Host "Enter MySQL username"
$DB_PASS = Read-Host "Enter MySQL password" -AsSecureString
$DB_PASS_TEXT = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASS))

Write-Host ""
Write-Host "Connecting to database..." -ForegroundColor Yellow

# Check if mysql is available
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlPath) {
    Write-Host "ERROR: mysql command not found. Please install MySQL client or add it to PATH." -ForegroundColor Red
    exit 1
}

# Execute the SQL file
$sqlFile = Join-Path $PSScriptRoot "database\getSalesChartData_WORKING.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found at: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "Executing SQL file: $sqlFile" -ForegroundColor Yellow

try {
    # Drop existing procedure first
    $dropSQL = "DROP PROCEDURE IF EXISTS getSalesChartData;"
    $dropSQL | mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASS_TEXT" $DB_NAME 2>&1
    
    # Create new procedure
    Get-Content $sqlFile | mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASS_TEXT" $DB_NAME 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✓ Procedure updated successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Changes applied:" -ForegroundColor Cyan
        Write-Host "  - Added car_qty column (car quantity sold)" -ForegroundColor White
        Write-Host "  - Added part_qty column (part quantity sold)" -ForegroundColor White
        Write-Host "  - Joined order_details table to get inventory quantities" -ForegroundColor White
        Write-Host "  - Returns 4 columns: period_label, car_qty, part_qty, revenue" -ForegroundColor White
        Write-Host ""
        Write-Host "Java controller has been updated to match the new format." -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "✗ Error updating procedure. Check the output above." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "✗ Error: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Fix Complete ===" -ForegroundColor Cyan
