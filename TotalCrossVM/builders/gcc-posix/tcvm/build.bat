SET /a THREADCOUNT=%NUMBER_OF_PROCESSORS%/2 + 1

SET WORKDIR=%~DP0
SET WORKDIR=^"%WORKDIR:~0,-1%^"

SET SKIADIR=%~DP0\..\..\..\deps\skia
call :ABSOLUTE_PATH    SKIADIR   %SKIADIR%

SET SRCDIR=%~DP0\..\..\..\src
call :ABSOLUTE_PATH    SRCDIR   %SRCDIR%

docker run ^
-v %WORKDIR%:/build ^
-v %SKIADIR%:/skia ^
-v %SRCDIR%:/src ^
-e SRCDIR=/../../../src ^
-e LIBS="-L. -lskia -lstdc++ -lpthread -lfontconfig -lGL -lSDL2main -lSDL2" ^
-t totalcross/amd64-cross-compile:bionic ^
bash -c "make -j%THREADCOUNT% -f /build/Makefile"

exit /b

:ABSOLUTE_PATH
SET %1=^"%~f2^"
exit /b