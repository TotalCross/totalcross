# Re-create all symbolic links
rm -f ${PWD}/../android-ndk-r4b/apps
rm -f ${PWD}/TotalCross/TotalCrossVM/builders/android/apps/litebase/LitebaseSDK
rm -f ${PWD}/TotalCross/TotalCrossVM/builders/android/apps/litebase/TotalCrossVM
rm -f ${PWD}/../android-ndk-r4b/out
rm -f ${PWD}/../android-ndk-r4b/LitebaseSDK
rm -f ${PWD}/../android-ndk-r4b/TotalCrossVM
ln -s ${PWD}/TotalCross/TotalCrossVM/builders/android/apps ${PWD}/../android-ndk-r4b
ln -s ${PWD}/Litebase/LitebaseSDK ${PWD}/TotalCross/TotalCrossVM/builders/android/apps/litebase
ln -s ${PWD}/TotalCross/TotalCrossVM ${PWD}/TotalCross/TotalCrossVM/builders/android/apps/litebase
ln -s ${PWD}/TotalCross/TotalCrossVM/builders/android/out ${PWD}/../android-ndk-r4b
ln -s ${PWD}/Litebase/LitebaseSDK ${PWD}/../android-ndk-r4b
ln -s ${PWD}/TotalCross/TotalCrossVM ${PWD}/../android-ndk-r4b

# move to the ndk root folder
cd ${PWD}/../android-ndk-r4b
make APP=litebase -j $NUMBER_OF_PROCESSORS
