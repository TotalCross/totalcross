#!/bin/bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
source $TRAVIS_BUILD_DIR/.travis/common.sh

# Break when error occurs
set -e

cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/xcode ; bash -C ./build.sh

cat >ExportOptions.plist <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
 <key>compileBitcode</key>
 <true/>
 <key>method</key>
 <string>enterprise</string>
 <key>provisioningProfiles</key>
 <dict>
  <key>com.totalcross.sdk</key>
  <string>TotalCross Wildcard</string>
 </dict>
 <key>signingCertificate</key>
 <string>iPhone Distribution</string>
 <key>signingStyle</key>
 <string>manual</string>
 <key>stripSwiftSymbols</key>
 <true/>
 <key>teamID</key>
 <string>$TC_TEAM_ID</string>
 <key>thinning</key>
 <string>&lt;none&gt;</string>
</dict>
</plist>
EOF

cd $TRAVIS_BUILD_DIR
xcodebuild -exportArchive -archivePath TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross.xcarchive -exportPath TotalCrossVM/builders/xcode/build/Release-iphoneos -exportOptionsPlist TotalCrossVM/builders/xcode/ExportOptions.plist
mkdir -p TotalCrossSDK/dist/vm/ios && mv TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross*.ipa TotalCrossSDK/dist/vm/ios/TotalCross.ipa


