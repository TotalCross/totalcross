javah -classpath bin totalcross.Launcher4A
javah -classpath bin totalcross.AndroidUtils
del totalcross_AndroidUtils_StartupTask.h
del totalcross_Launcher4A_PhoneListener.h
move *.h P:\gitrepo\TotalCross\TotalCrossVM\src\init\android
