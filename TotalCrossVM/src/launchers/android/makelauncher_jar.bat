cd bin\classes
jar cvf Launcher.jar totalcross\app\stub\*.class totalcross\*.class
move Launcher.jar P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android
copy /y P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android\*.* c:\TotalCross3\etc\launchers\android
rem to generate the resources.ap_, export an apk from the stub using "Android tools" -> "Export unsigned application package", delete the classes.dex, and move to P:\gitrepo\TotalCross\TotalCrossSDK\etc\launchers\android\resources.ap_
cd ..\..
pause
