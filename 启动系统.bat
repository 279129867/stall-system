@echo off
chcp 65001 >nul
title 摆摊集市系统 - 一键启动

echo ========================================
echo   摆摊集市系统 - 一键启动脚本
echo ========================================
echo.

echo [1/5] 启动 MySQL...
net start MySQL80 2>nul
echo     MySQL 已启动
echo.

echo [2/5] 启动 Redis...
start "Redis" /min cmd /k "cd /d C:\redis && redis-server.exe redis.windows.conf"
timeout /t 2 >nul
echo     Redis 已启动
echo.

echo [3/5] 启动 ChromaDB...
start "ChromaDB" /min cmd /k "chroma run --path C:\chroma_data"
timeout /t 2 >nul
echo     ChromaDB 已启动
echo.

echo [4/5] 启动后端服务 (8080端口)...
start "后端" /min cmd /k "cd /d C:\Users\Administrator\IdeaProjects\stall-system\target && java -jar stall-system-1.0.0.jar"
echo     等待后端启动...
timeout /t 8 >nul
echo.

echo [5/5] 启动前端并打开浏览器...
start "前端" /min cmd /k "cd /d D:\code\stall-frontend && npx vite preview --port 5173"
timeout /t 3 >nul
start http://localhost:5173
echo.

echo ========================================
echo   全部启动完成！
echo   后端: http://localhost:8080
echo   前端: http://localhost:5173
echo.
echo   演示完直接关闭弹出的窗口即可
echo ========================================
pause
