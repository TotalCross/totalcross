rem - mkdir output\release\TotalCrossSDK\dist\vm\win32

@echo OFF

setlocal EnableExtensions EnableDelayedExpansion
set "SEARCHTEXT=.exe"
set "REPLACETEXT=.com"

set KEY_NAME="HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Microsoft\VisualStudio\9.0\Setup\VS"
set VALUE_NAME=EnvironmentPath

FOR /F "usebackq tokens=2*" %%A IN (`REG QUERY %KEY_NAME% /v %VALUE_NAME% 2^>nul`) DO (
    set ValueName=%%A
    set ValueType=%%B
    set ValueValue=%%B
    set "PATH=!ValueValue:%SEARCHTEXT%=%REPLACETEXT%!"
)

@echo ON

"%PATH%" TCVM.sln /rebuild "Release|Win32"

"%PATH%" TCVM.sln /rebuild "Release|Pocket PC 2003 (ARMV4)

