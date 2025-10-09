# Apply OTP Password Change Fix
$filePath = "d:\7.Java_Workspace\Porsche\src\main\java\Controllers\adminDashboardController.java"

Write-Host "Reading file..." -ForegroundColor Cyan
$content = Get-Content $filePath -Raw

Write-Host "Applying changes..." -ForegroundColor Cyan

# Change 1: Update FXML path
$content = $content -replace '/View/Authentication\.fxml', '/View/ChangePassword.fxml'

# Change 2: Remove authentication controller lines
$content = $content -replace '                authenticationController controller = loader\.getController\(\);\r?\n', ''
$content = $content -replace '                controller\.setStep\("factor"\);\r?\n', ''

Write-Host "Saving file..." -ForegroundColor Cyan
$content | Set-Content $filePath -NoNewline

Write-Host "Done! Changes applied successfully" -ForegroundColor Green
