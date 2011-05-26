@echo off

echo This script installs the TotalCross VM on a WINDOWS MOBILE device.
rem test on the ProgramFiles variable
if not "%ProgramFiles%"=="" goto ok
set ProgramFiles=c:\Progra~1
:ok
"%ProgramFiles%\Microsoft ActiveSync\CeAppMgr.exe" .\CeInstall_WMOBILE.ini
if "%errorlevel%"=="1" goto end

"C:\Windows\WindowsMobile\ceappmgr.exe" .\CeInstall_WMOBILE.ini
if "%errorlevel%"=="1" goto end

CeAppMgr.exe .\CeInstall_WMOBILE.ini
if "%errorlevel%"=="0" goto end

echo:
echo ERROR: Cannot locate CeAppMgr.exe. Please put the
echo "Microsoft ActiveSync" directory on your path variable.
echo It is usually located under "Program Files".
echo:
pause
:end

