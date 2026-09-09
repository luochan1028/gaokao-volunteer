@echo off
chcp 65001 >nul
title 后端服务 - 高考志愿系统
cd /d "D:\software\huaw-demo\gaokao-volunteer\backend"
echo 后端服务启动中...
echo 访问地址: http://localhost:8080
echo 关闭此窗口将停止后端服务
"C:\Program Files\Java\jdk-17\bin\java.exe" -jar "D:\software\huaw-demo\gaokao-volunteer\backend\target\gaokao-volunteer-1.0.0.jar"
pause