@echo off
echo Compiling Java Server...
javac Server.java
if %errorlevel% neq 0 (
    echo Compilation Failed!
    pause
    exit /b %errorlevel%
)

echo Starting Server on http://localhost:8080...
echo Keep this window open to keep the server running.
java Server
pause
