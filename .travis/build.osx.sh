#!/bin/bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
source $TRAVIS_BUILD_DIR/.travis/common.sh

# Break when error occurs
set -e



cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/xcode

pod install

# Create export options
cat >ExportOptions.plist <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
 <key>compileBitcode</key>
 <true/>
 <key>method</key>
 <string>development</string>
 <key>provisioningProfiles</key>
 <dict>
  <key>com.totalcross.sdk</key>
  <string>TotalCross_WildCard_Development</string>
 </dict>
 <key>signingCertificate</key>
 <string>iPhone Developer</string>
 <key>signingStyle</key>
 <string>manual</string>
 <key>stripSwiftSymbols</key>
 <true/>
 <key>teamID</key>
 <string>R8FWE2RXQV</string>
 <key>thinning</key>
 <string>&lt;none&gt;</string>
</dict>
</plist>
EOF

# Create keychain
echo $P12_APPLE_CONTENT | base64 --decode > apple-keysign.p12
security create-keychain -p $P12_APPLE_PASS build.keychain
security default-keychain -s build.keychain
security unlock-keychain -p $P12_APPLE_PASS build.keychain
security import apple-keysign.p12 -k build.keychain -P $P12_APPLE_PASS -T /usr/bin/codesign
security find-identity -v
security set-key-partition-list -S 'apple-tool:,apple:' -s -k $P12_APPLE_PASS build.keychain
rm apple-keysign.p12

# Copy Mobile provision
mkdir -p ~/Library/MobileDevice/Provisioning\ Profiles
echo $MOBILE_PROVISION_CONTENT | base64 --decode | bunzip2 >TotalCross_WildCard_Development.mobileprovision
uuid=`grep UUID -A1 -a TotalCross_WildCard_Development.mobileprovision | grep -io "[-A-F0-9]\{36\}"`
cp TotalCross_WildCard_Development.mobileprovision ~/Library/MobileDevice/Provisioning\ Profiles/$uuid.mobileprovision
ls -l ~/Library/MobileDevice/Provisioning\ Profiles

xcodebuild -workspace TotalCross.xcworkspace -scheme TotalCross archive -archivePath build/TotalCross.xcarchive CODE_SIGN_IDENTITY="Apple Development: Italo Medeiros Bruno" PROVISIONING_PROFILE="TotalCross_WildCard_Development" teamID=$TC_TEAM_ID -configuration Debug -quiet

xcodebuild -exportArchive -archivePath build/TotalCross.xcarchive -exportPath build/TotalCross.ipa -exportOptionsPlist ExportOptions.plist


# Copy files to collect
mkdir -p /tmp/output/osx
cp build/TotalCross.ipa /tmp/output/osx/
cp build/TotalCross.xcarchive /tmp/output/osx/
