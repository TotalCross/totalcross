@echo off
cd bin
jar cvf Launcher.jar totalcross\app\stub\*.class totalcross\*.class
move Launcher.jar P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android
copy resources.ap_ P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android                                                   
cd ..
pause
