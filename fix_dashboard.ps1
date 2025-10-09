# Fix adminDashboardController compilation errors
$filePath = "d:\7.Java_Workspace\Porsche\src\main\java\Controllers\adminDashboardController.java"

Write-Host "Reading file..." -ForegroundColor Cyan
$content = Get-Content $filePath -Raw

Write-Host "Fixing optionProfile section..." -ForegroundColor Cyan

# Fix optionProfile - change back to Authentication.fxml and restore controller lines
$content = $content -replace '(optionProfile\.setOnMouseClicked\(e -> \{\s+try \{\s+FXMLLoader loader = new FXMLLoader\(getClass\(\)\.getResource\(")\/View\/ChangePassword\.fxml("\)\);)', '$1/View/Authentication.fxml$2'

# Add the missing controller declaration
$content = $content -replace '(optionProfile\.setOnMouseClicked\(e -> \{\s+try \{\s+FXMLLoader loader = new FXMLLoader\(getClass\(\)\.getResource\("\/View\/Authentication\.fxml"\)\);\s+Parent root = loader\.load\(\);)\s+controller\.init', '$1`r`n                authenticationController controller = loader.getController();`r`n                controller.init'

Write-Host "Saving file..." -ForegroundColor Cyan
$content | Set-Content $filePath -NoNewline

Write-Host "Done! Fixed adminDashboardController" -ForegroundColor Green
