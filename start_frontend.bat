@echo off
cd /d "%~dp0frontend"
set PATH=D:\software\node\node-v20.18.0-win-x64;%%PATH%%
echo Starting frontend on http://localhost:3000 ...
node.exe ".\node_modules\.pnpm\vite@5.4.21\node_modules\vite\bin\vite.js" --port 3000
pause
