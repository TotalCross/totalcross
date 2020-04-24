SET /a THREADCOUNT=%NUMBER_OF_PROCESSORS%/2 + 1

SET WORKDIR=%~DP0
SET WORKDIR=^"%WORKDIR:~0,-1%^"

SET SRCDIR=%~DP0\..\..\..\deps\SDL
call :ABSOLUTE_PATH    SRCDIR   %SRCDIR%

SET BUILDDIR=%~DP0\build
call :ABSOLUTE_PATH    BUILDDIR   %BUILDDIR%

docker run ^
-v %SRCDIR%:/src ^
-v %BUILDDIR%:/build ^
-e CFLAGS="-O3 -fPIC" ^
-t totalcross/cross-compile ^
bash -c "cmake /src -DSDL_SHARED=0 -DSDL_AUDIO=0 -DVIDEO_VIVANTE=ON -DVIDEO_WAYLAND=ON -DWAYLAND_SHARED=ON; make -j%THREADCOUNT%"

exit /b

:ABSOLUTE_PATH
SET %1=^"%~f2^"
exit /b