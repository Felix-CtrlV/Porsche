# PowerShell script to fix getAllOrders procedure
# This fixes the installment table to show all items instead of just one

Write-Host "=== Fixing getAllOrders Procedure ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Issue: Installment table only shows one item even when order has multiple cars/parts" -ForegroundColor Yellow
Write-Host "Cause: DISTINCT keyword in GROUP_CONCAT removes duplicate quantities" -ForegroundColor Yellow
Write-Host "Fix: Remove DISTINCT and add ORDER BY to maintain item order" -ForegroundColor Green
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
$sqlFile = Join-Path $PSScriptRoot "database\getAllOrders_FIXED.sql"

if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found at: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "Executing SQL file: $sqlFile" -ForegroundColor Yellow

try {
    Get-Content $sqlFile | mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASS_TEXT" $DB_NAME 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✓ Procedure updated successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Changes applied:" -ForegroundColor Cyan
        Write-Host "  - Removed DISTINCT from qty GROUP_CONCAT" -ForegroundColor White
        Write-Host "  - Removed DISTINCT from price GROUP_CONCAT" -ForegroundColor White
        Write-Host "  - Added ORDER BY detail_id to maintain item order" -ForegroundColor White
        Write-Host "  - Improved car name display (model + trim)" -ForegroundColor White
        Write-Host ""
        Write-Host "Result: Installment table will now show ALL items in the order!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Example:" -ForegroundColor Cyan
        Write-Host "  Before: Only 1 item shown (due to DISTINCT collapsing duplicates)" -ForegroundColor Red
        Write-Host "  After:  All 5 items shown (2 cars + 3 parts)" -ForegroundColor Green
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
Write-Host "Please restart your Java application to see the changes." -ForegroundColor Yellow
