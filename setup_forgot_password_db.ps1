# PowerShell Script to Setup Forgot Password Database Procedures
# This script will execute the SQL procedures in your MySQL database

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Forgot Password Database Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Database connection details
$dbHost = "localhost"
$dbPort = "3306"
$dbName = "car_show_room"
$dbUser = Read-Host "Enter MySQL username (default: root)"
if ([string]::IsNullOrWhiteSpace($dbUser)) {
    $dbUser = "root"
}

$dbPassword = Read-Host "Enter MySQL password" -AsSecureString
$dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword))

Write-Host ""
Write-Host "Connecting to database: $dbName..." -ForegroundColor Yellow

# SQL commands
$sql = @"
USE $dbName;

-- 1. Procedure to get user by email
DELIMITER //

DROP PROCEDURE IF EXISTS getUserByEmail//

CREATE PROCEDURE getUserByEmail(
    IN p_email VARCHAR(255)
)
BEGIN
    SELECT 
        id,
        username,
        email,
        phone,
        address,
        dob,
        role
    FROM users
    WHERE email = p_email
    AND is_active = 'active'
    LIMIT 1;
END//

-- 2. Procedure to reset password
DROP PROCEDURE IF EXISTS resetPassword//

CREATE PROCEDURE resetPassword(
    IN p_username VARCHAR(255),
    IN p_new_password VARCHAR(255)
)
BEGIN
    UPDATE users
    SET password = p_new_password
    WHERE username = p_username;
    
    SELECT ROW_COUNT() as affected_rows;
END//

DELIMITER ;
"@

# Save SQL to temp file
$tempSqlFile = "$PSScriptRoot\temp_forgot_password.sql"
$sql | Out-File -FilePath $tempSqlFile -Encoding UTF8

try {
    # Execute SQL using mysql command
    Write-Host "Executing SQL procedures..." -ForegroundColor Yellow
    
    $mysqlCmd = "mysql -h $dbHost -P $dbPort -u $dbUser -p$dbPasswordPlain $dbName"
    Get-Content $tempSqlFile | & mysql -h $dbHost -P $dbPort -u $dbUser "-p$dbPasswordPlain" $dbName
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✓ SUCCESS! Procedures created successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "The following procedures were created:" -ForegroundColor Cyan
        Write-Host "  1. getUserByEmail(email)" -ForegroundColor White
        Write-Host "  2. resetPassword(username, password)" -ForegroundColor White
        Write-Host ""
        Write-Host "You can now use the Forgot Password feature!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "✗ ERROR: Failed to create procedures" -ForegroundColor Red
        Write-Host "Please run the SQL manually from forgot_password_procedures.sql" -ForegroundColor Yellow
    }
} catch {
    Write-Host ""
    Write-Host "✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please ensure:" -ForegroundColor Yellow
    Write-Host "  1. MySQL is installed and in your PATH" -ForegroundColor White
    Write-Host "  2. Database credentials are correct" -ForegroundColor White
    Write-Host "  3. Database 'car_show_room' exists" -ForegroundColor White
    Write-Host ""
    Write-Host "Alternative: Run the SQL manually from forgot_password_procedures.sql" -ForegroundColor Yellow
} finally {
    # Clean up temp file
    if (Test-Path $tempSqlFile) {
        Remove-Item $tempSqlFile -Force
    }
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
