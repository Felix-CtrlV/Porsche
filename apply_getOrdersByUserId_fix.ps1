# PowerShell script to apply getOrdersByUserId fix to MySQL database

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Apply getOrdersByUserId Fix" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Database configuration
$mysqlPath = "mysql"  # Assumes MySQL is in PATH, otherwise use full path like "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$sqlFile = "database\getOrdersByUserId_FIXED.sql"

# Prompt for database credentials
Write-Host "Enter your MySQL database details:" -ForegroundColor Yellow
$dbHost = Read-Host "Host (default: localhost)"
if ([string]::IsNullOrWhiteSpace($dbHost)) { $dbHost = "localhost" }

$dbName = Read-Host "Database name"
$dbUser = Read-Host "Username"
$dbPassword = Read-Host "Password" -AsSecureString
$dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword))

Write-Host ""
Write-Host "Applying fix to database: $dbName@$dbHost" -ForegroundColor Green
Write-Host ""

# Execute the SQL file
try {
    $arguments = @(
        "-h", $dbHost,
        "-u", $dbUser,
        "-p$dbPasswordPlain",
        $dbName,
        "-e", "source $sqlFile"
    )
    
    & $mysqlPath $arguments 2>&1 | Out-String | Write-Host
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  ✓ Fix Applied Successfully!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "The getOrdersByUserId procedure has been updated." -ForegroundColor Green
        Write-Host "Your Manager Staff View will now show all items in the installment table." -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "  ✗ Error Applying Fix" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Please check the error message above." -ForegroundColor Red
    }
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  ✗ Error" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure MySQL is installed and accessible from the command line." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
