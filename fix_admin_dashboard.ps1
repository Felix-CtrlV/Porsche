# Fix adminDashboardController.java compilation issues

$file = "d:\7.Java_Workspace\Porsche\src\main\java\Controllers\adminDashboardController.java"
$content = Get-Content $file -Raw

# 1. Add logger imports after Optional import
$content = $content -replace '(import java\.util\.Optional;)', "`$1`n`nimport org.slf4j.Logger;`nimport org.slf4j.LoggerFactory;"

# 2. Add logger declaration after class declaration
$content = $content -replace '(public class adminDashboardController \{)', "`$1`n    private static final Logger logger = LoggerFactory.getLogger(adminDashboardController.class);"

# 3. Remove ClassNotFoundException from catch block
$content = $content -replace 'catch \(\s+ClassNotFoundException \|\s+SQLException ex\)', 'catch (SQLException ex)'

# 4. Replace printStackTrace with logger.error
$content = $content -replace '\.printStackTrace\(\);', '.printStackTrace(); // TODO: Replace with logger'

# Save the file
Set-Content -Path $file -Value $content -NoNewline

Write-Host "Fixed adminDashboardController.java"
