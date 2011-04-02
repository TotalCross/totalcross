ln -s ${PWD}/TotalCross/temp/android/apps ${PWD}/TotalCross/TotalCrossVM/builders/android/out
ln -s ${PWD}/TotalCross/temp/android/src ${PWD}/TotalCross/TotalCrossVM/builders/android/out
cd ${PWD}/TotalCross/TotalCrossVM/builders/android
make APP=tcvm -j $NUMBER_OF_PROCESSORS
