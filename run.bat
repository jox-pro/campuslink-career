@echo off
setlocal
cd /d "%~dp0"

if exist "%~dp0mvnw.cmd" (
  call "%~dp0mvnw.cmd" -q javafx:run
) else (
  where mvn >nul 2>nul
  if errorlevel 1 (
    echo Maven was not found on PATH.
    echo Install Maven or use the Maven wrapper scripts.
    exit /b 1
  )
  mvn -q javafx:run
)
