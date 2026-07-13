@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven was not found on PATH.
  echo Install Maven or add it to PATH before running this script.
  exit /b 1
)
mvn -q javafx:run
