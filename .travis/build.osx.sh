#!/bin/bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
source $TRAVIS_BUILD_DIR/.travis/common.sh

# Break when error occurs
set -e

cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/xcode ; bash -C ./build.sh

xcodebuild -exportArchive -archivePath TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross.xcarchive -exportPath TotalCrossVM/builders/xcode/build/Release-iphoneos -exportOptionsPlist TotalCrossVM/builders/xcode/ExportOptions.plist
mkdir -p TotalCrossSDK/dist/vm/ios && mv TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross*.ipa TotalCrossSDK/dist/vm/ios/TotalCross.ipa


