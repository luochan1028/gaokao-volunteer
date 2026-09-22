@echo off
cd /d "%~dp0"
if not exist logs mkdir logs
"C:\Program Files\nodejs\node.exe" "%~dp0src\index.js" --once >> "%~dp0logs\task.log" 2>&1
