@echo off
chcp 65001 >nul
title 高考志愿填报系统启动器

echo ============================================
echo   高考志愿填报系统
echo ============================================
echo.

echo [1/2] 启动后端服务(端口8080)...
start "GaokaoBackend" /min cmd /c "D:\software\huaw-demo\start-backend-only.bat"
echo 等待后端启动...
timeout /t 25 /nobreak >nul

echo [2/2] 启动前端服务(端口3000)...
start "GaokaoFrontend" /min cmd /c "D:\software\huaw-demo\start-frontend-only.bat"
echo 等待前端启动...
timeout /t 15 /nobreak >nul

echo.
echo ============================================
echo   启动完成!
echo   前端: http://localhost:3000
echo   后端: http://localhost:8080
echo   账号: 13900000000  密码: 123456
echo ============================================
echo.
echo 正在打开浏览器...
timeout /t 2 /nobreak >nul
start "" http://localhost:3000

echo.
echo 服务运行中，关闭此窗口不会停止服务。
echo.
pause
