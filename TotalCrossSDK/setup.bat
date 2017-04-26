@echo off
SETLOCAL
echo Setting up TOTALCROSS_HOME environment variable...
call %CD%\etc\tools\setenv\SetEnv.exe -a TOTALCROSS3_HOME "%CD%"
if errorlevel 1 call %CD%\etc\tools\setenv\SetEnv.exe -ua TOTALCROSS3_HOME "%CD%"
call %CD%\etc\tools\setenv\SetEnv.exe -a PATH %%"~TOTALCROSS3_HOME~\dist\vm\win32"
if errorlevel 1 call %CD%\etc\tools\setenv\SetEnv.exe -ua PATH %%"~TOTALCROSS3_HOME~\dist\vm\win32"
ENDLOCAL
echo Creating Install Android registry key
echo %CD%
copy PostInstall.* %CD%\dist\vm\win32
cd %CD%\dist\vm\win32
PostInstall.exe
cd ..\..\..
del PostInstall.*
