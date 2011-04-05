@echo off
cd bin
jar cvf Launcher.jar totalcross\app\stub\*.class totalcross\*.class
move Launcher.jar P:\TotalCrossSDK\etc\launchers\android
copy resources.ap_ P:\TotalCrossSDK\etc\launchers\android                                                   
cd ..
rem pause
