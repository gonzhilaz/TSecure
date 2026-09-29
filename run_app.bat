@echo off
REM TelkomSecure - One-Click Emulator and APK Launcher
setlocal
set SCRIPT_DIR=%~dp0
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%run_app.ps1" %*
endlocal
