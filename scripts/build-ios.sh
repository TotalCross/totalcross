#!/bin/bash

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build/TotalCross

echo "STARTING iOS"

mkdir ~/build
cd ~/build

rm -rf ios*

git clone --shallow-submodules --depth 1 --single-branch --branch master git@gitlab.com:totalcross/vm.git ios

pushd ios/TotalCrossVM/xcode
   cmake ../ -GXcode
   pod install
   # Make sure legacy build system is enabled
   /usr/libexec/PlistBuddy -c "Add :DisableBuildSystemDeprecationDiagnostic bool" TotalCross.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings
   /usr/libexec/PlistBuddy -c "Set :DisableBuildSystemDeprecationDiagnostic true" TotalCross.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings
   read -p "Fix the mbedTLS dependencies and press any key to continue... " -n1 -s
   xcodebuild -workspace TotalCross.xcworkspace -scheme TotalCross BUILD_DIR=${PWD} archive -archivePath build/TotalCross.xcarchive teamID="W5Y7X2KNUL" -configuration Release
   xcodebuild -exportArchive -archivePath build/TotalCross.xcarchive -exportPath build/TotalCross.ipa -exportOptionsPlist ExportOptions.plist
popd

pushd ios/TotalCrossVM/builders/xcode/build
   cp TotalCross.ipa/TotalCross.ipa $OUTDIR/dist/vm/ios/
   cp -R TotalCross.xcarchive $OUTDIR/etc/tools/iOSCodesign/
popd
