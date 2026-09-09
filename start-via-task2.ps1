Get-Process java,node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep 1
Unregister-ScheduledTask -TaskName "GaokaoStart" -Confirm:$false -ErrorAction SilentlyContinue
$action = New-ScheduledTaskAction -Execute "cmd.exe" -Argument "/c D:\software\huaw-demo\start-all.bat" -WorkingDirectory "D:\software\huaw-demo"
$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date).AddSeconds(3)
$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable -ExecutionTimeLimit (New-TimeSpan -Minutes 30)
$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType Interactive -RunLevel Limited
Register-ScheduledTask -TaskName "GaokaoStart" -Action $action -Trigger $trigger -Settings $settings -Principal $principal -Force | Out-Null
Start-ScheduledTask -TaskName "GaokaoStart"
"Task started at $(Get-Date)"