﻿Get-Process java,node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep 2
Remove-Item "D:\software\huaw-demo\gaokao-volunteer\backend\logs" -Recurse -Force -ErrorAction SilentlyContinue

$jar = "D:\software\huaw-demo\gaokao-volunteer\backend\target\gaokao-volunteer-1.0.0.jar"
$wd = "D:\software\huaw-demo\gaokao-volunteer\backend"
$psi1 = New-Object System.Diagnostics.ProcessStartInfo
$psi1.FileName = "C:\Program Files\Java\jdk-17\bin\java.exe"
$psi1.Arguments = "-jar `"$jar`""
$psi1.WorkingDirectory = $wd
$psi1.UseShellExecute = $true
$psi1.WindowStyle = "Minimized"
[System.Diagnostics.Process]::Start($psi1) | Out-Null

$frontendDir = "D:\software\huaw-demo\gaokao-volunteer\frontend"
$psi2 = New-Object System.Diagnostics.ProcessStartInfo
$psi2.FileName = "npm.cmd"
$psi2.Arguments = "run dev"
$psi2.WorkingDirectory = $frontendDir
$psi2.UseShellExecute = $true
$psi2.WindowStyle = "Minimized"
[System.Diagnostics.Process]::Start($psi2) | Out-Null

$bk=$false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep 2
    try { Invoke-RestMethod -Uri "http://localhost:8080/api/public/colleges?page=0&size=1" -Method Get -TimeoutSec 2 | Out-Null; $bk=$true; break } catch {}
}
$fk=$false
for ($i = 0; $i -lt 20; $i++) {
    Start-Sleep 2
    try { Invoke-WebRequest -Uri "http://localhost:3000" -Method Get -TimeoutSec 2 -UseBasicParsing | Out-Null; $fk=$true; break } catch {}
}

"Backend=$bk Frontend=$fk"
if ($bk -and $fk) {
    Start-Process "http://localhost:3000"
    "浏览器已打开"
}
Start-Sleep 100
