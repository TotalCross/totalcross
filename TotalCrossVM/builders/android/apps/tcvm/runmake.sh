# Re-create all symbolic links
rm -f ${PWD}/../android-ndk-r4b/apps
rm -f ${PWD}/TotalCross/TotalCrossVM/builders/android/apps/tcvm/TotalCrossVM
rm -f ${PWD}/../android-ndk-r4b/out
rm -f ${PWD}/../android-ndk-r4b/TotalCrossVM
ln -s ${PWD}/TotalCross/TotalCrossVM/builders/android/apps ${PWD}/../android-ndk-r4b
ln -s ${PWD}/TotalCross/TotalCrossVM ${PWD}/TotalCross/TotalCrossVM/builders/android/apps/tcvm
ln -s ${PWD}/TotalCross/TotalCrossVM/builders/android/out ${PWD}/../android-ndk-r4b
ln -s ${PWD}/TotalCross/TotalCrossVM ${PWD}/../android-ndk-r4b

# move to the ndk root folder
cd ${PWD}/../android-ndk-r4b
make APP=tcvm -j $NUMBER_OF_PROCESSORS
