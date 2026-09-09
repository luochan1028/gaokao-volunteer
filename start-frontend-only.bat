@echo off
chcp 65001 >nul
title 前端服务 - 高考志愿系统
cd /d "D:\software\huaw-demo\gaokao-volunteer\frontend"
echo 前端服务启动中...
echo 访问地址: http://localhost:3000
echo 关闭此窗口将停止前端服务
call npm run dev
pause