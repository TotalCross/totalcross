cd bin\classes
jar cvf Launcher.jar totalcross\app\stub\*.class totalcross\*.class
move Launcher.jar P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android
rem copy resources.ap_ P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android                                                   
cd ..\..
pause
