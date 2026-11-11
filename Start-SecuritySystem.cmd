@echo off
setlocal
if exist "%~dp0.env.local-demo" if exist "%~dp0..\.tools\start-security-system.ps1" goto local

pushd "%~dp0"
if not exist .env (
    echo Copy .env.example to .env and configure it first. See README.md.
    goto error
)
where docker >nul 2>&1
if errorlevel 1 (
    echo Docker was not found. Start Docker Desktop and try again.
    goto error
)
docker compose up -d --build --wait
if errorlevel 1 goto error
popd
if /i not "%~1"=="-NoBrowser" start "" "http://localhost:3002/login"
exit /b 0

:error
popd
pause
exit /b 1

:local
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0..\.tools\start-security-system.ps1" %*
if errorlevel 1 (
    pause
    exit /b 1
)
exit /b 0
