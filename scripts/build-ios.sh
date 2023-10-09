#!/bin/bash

mkdir ~/build
cd ~/build

BASEDIR=${PWD}

rm -rf ios*

git clone --shallow-submodules --depth 1 --single-branch --branch master git@gitlab.com:totalcross/vm.git ios

pushd ios/TotalCrossVM/xcode
   cmake ../ -GXcode
   pod install
   xcodebuild -workspace TotalCross.xcworkspace -scheme TotalCross BUILD_DIR=${PWD} archive -archivePath build/TotalCross.xcarchive teamID="W5Y7X2KNUL" -configuration Release
   xcodebuild -exportArchive -archivePath build/TotalCross.xcarchive -exportPath build/TotalCross.ipa -exportOptionsPlist ExportOptions.plist
popd

pushd ios/TotalCrossVM/builders/xcode/build
   zip -r -q $BASEDIR/ios.zip .
popd
