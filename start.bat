@echo off
set JAVA_HOME=D:\software\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
echo Starting backend...
start "Backend" /B java -jar "%~dp0backend\target\ai-outfit-system-1.0.0.jar"
echo Backend started, waiting...
timeout /t 8 /nobreak >nul
echo Starting frontend...
cd /d "%~dp0frontend"
set PATH=D:\software\node\node-v20.18.0-win-x64;%PATH%
start "Frontend" /B node.exe ".\node_modules\.pnpm\vite@5.4.21\node_modules\vite\bin\vite.js" --port 3000
echo.
echo ========================================
echo  AI Outfit System Started!
echo  Backend:  http://localhost:8080
echo  Frontend: http://localhost:3000
echo ========================================
echo.
pause
