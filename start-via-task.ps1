Get-Process java,node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep 1
$action = New-ScheduledTaskAction -Execute "D:\software\huaw-demo\start-all.bat"
$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date).AddSeconds(2)
$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable
Register-ScheduledTask -TaskName "GaokaoStart" -Action $action -Trigger $trigger -Settings $settings -Force | Out-Null
Start-ScheduledTask -TaskName "GaokaoStart"
"Task started"