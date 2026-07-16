@echo off
set JAVA_HOME=D:\software\jdk-17
set PATH=%%JAVA_HOME%%\bin;%%PATH%%
cd /d "%~dp0backend"
echo Starting backend on http://localhost:8080 ...
java -jar "%~dp0backend\target\ai-outfit-system-1.0.0.jar"
pause
