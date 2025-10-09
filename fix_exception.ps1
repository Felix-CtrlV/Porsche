$file = "d:\7.Java_Workspace\Porsche\src\main\java\Controllers\adminDashboardController.java"
$content = Get-Content $file
$newContent = $content | ForEach-Object {
    $_ -replace "ClassNotFoundException \|", ""
}
$newContent | Set-Content $file
Write-Host "Fixed ClassNotFoundException"
