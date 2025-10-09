$filePath = "d:\7.Java_Workspace\Porsche\src\main\java\Controllers\adminDashboardController.java"
$content = Get-Content $filePath -Raw
$content = $content -replace '``r``n', "`r`n"
$content | Set-Content $filePath -NoNewline
Write-Host "Fixed newlines"
