Get-Process java,node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep 2
$jar = "D:\software\huaw-demo\gaokao-volunteer\backend\target\gaokao-volunteer-1.0.0.jar"
$wd = "D:\software\huaw-demo\gaokao-volunteer\backend"
$s1 = New-Object System.Diagnostics.ProcessStartInfo("C:\Program Files\Java\jdk-17\bin\java.exe", "-jar `"$jar`"")
$s1.WorkingDirectory = $wd; $s1.UseShellExecute = $true; $s1.WindowStyle = "Minimized"
[System.Diagnostics.Process]::Start($s1) | Out-Null
$s2 = New-Object System.Diagnostics.ProcessStartInfo("npm.cmd", "run dev")
$s2.WorkingDirectory = "D:\software\huaw-demo\gaokao-volunteer\frontend"; $s2.UseShellExecute = $true; $s2.WindowStyle = "Minimized"
[System.Diagnostics.Process]::Start($s2) | Out-Null
for ($i=0; $i -lt 25; $i++) { Start-Sleep 2; try { Invoke-RestMethod "http://localhost:8080/api/public/colleges?page=0&size=1" -TimeoutSec 2 | Out-Null; break } catch {} }
for ($i=0; $i -lt 15; $i++) { Start-Sleep 2; try { Invoke-WebRequest "http://localhost:3000" -TimeoutSec 2 -UseBasicParsing | Out-Null; break } catch {} }
Start-Process "http://localhost:3000"
"OK browser opened"
Start-Sleep 80