cd bin\classes
jar cvf Launcher.jar totalcross\app\stub\*.class totalcross\*.class
move Launcher.jar P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android
rem to generate the resources.ap_, export an apk from the stub, delete the classes.dex and META-INF, and move to P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android\resources.ap_
cd ..\..
pause
