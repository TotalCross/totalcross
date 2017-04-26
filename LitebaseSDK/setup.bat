call %TOTALCROSS_HOME%\etc\tools\setenv\SetEnv.exe -a LITEBASE_HOME "%CD%"
if errorlevel 1 call %TOTALCROSS_HOME%\etc\tools\setenv\SetEnv.exe -ua LITEBASE_HOME "%CD%"