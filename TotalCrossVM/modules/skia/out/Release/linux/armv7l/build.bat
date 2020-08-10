SET /a THREADCOUNT=%NUMBER_OF_PROCESSORS%/2 + 1

SET WORKDIR=%~DP0
SET WORKDIR=^"%WORKDIR:~0,-1%^"

SET SKIADIR=%~DP0\..\..\..\deps\skia
call :ABSOLUTE_PATH    SKIADIR   %SKIADIR%

SET SRCDIR=%~DP0\..\..\..\src
call :ABSOLUTE_PATH    SRCDIR   %SRCDIR%

SET SDLDIR=%~DP0\..\sdl
call :ABSOLUTE_PATH    SDLDIR   %SDLDIR%

SET SDL2DIR=%~DP0\..\..\..\deps\SDL\include
call :ABSOLUTE_PATH    SDL2DIR   %SDL2DIR%

docker run ^
-v %WORKDIR%:/build ^
-v %SKIADIR%:/skia ^
-v %SDLDIR%:/sdl ^
-v %SDL2DIR%:/SDL2 ^
-v %SRCDIR%:/src ^
-t totalcross/cross-compile ^
bash -c "make -j%THREADCOUNT% -f /build/Makefile"

exit /b

:ABSOLUTE_PATH
SET %1=^"%~f2^"
exit /b