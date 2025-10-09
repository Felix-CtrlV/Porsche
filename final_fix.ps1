# Final fix for adminDashboardController
$file = "d:\7.Java_Workspace\Porsche\src\main\java\Controllers\adminDashboardController.java"

Write-Host "Applying final fix..." -ForegroundColor Cyan

# Read file as array of lines
$lines = Get-Content $file

# Find and replace the specific line
for ($i = 0; $i -lt $lines.Count; $i++) {
    # Change optionChange to use ChangePassword.fxml
    if ($lines[$i] -match 'optionChange.*Authentication\.fxml') {
        $lines[$i] = $lines[$i] -replace 'Authentication\.fxml', 'ChangePassword.fxml'
        Write-Host "Fixed optionChange at line $($i+1)" -ForegroundColor Green
        
        # Remove the next 2 lines (controller lines)
        if ($lines[$i+2] -match 'authenticationController controller') {
            $lines = $lines[0..$i] + $lines[($i+1)] + $lines[($i+4)..($lines.Count-1)]
            Write-Host "Removed controller lines" -ForegroundColor Green
        }
    }
}

# Save file
$lines | Set-Content $file
Write-Host "Done!" -ForegroundColor Green
