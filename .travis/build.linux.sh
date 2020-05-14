#!/bin/bash

# Break when error occurs
set -e
source $TRAVIS_BUILD_DIR/.travis/common.sh

# Prepare environment
#sudo apt-get update
#sudo apt-get -y install ninja-build

# Download and unzip the Android SDK tools (if not already there thanks to the cache mechanism)
# Latest version available here: https://developer.android.com/studio/#command-tools
if test ! -e $HOME/android-sdk-dl/sdk-tools.zip ; then
    curl https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip > $HOME/android-sdk-dl/sdk-tools.zip
fi
unzip -qq -n $HOME/android-sdk-dl/sdk-tools.zip -d $HOME/android-sdk

# Install or update Android SDK components (will not do anything if already up to date thanks to the cache mechanism)
echo y | $HOME/android-sdk/tools/bin/sdkmanager 'platform-tools' > /dev/null
echo y | $HOME/android-sdk/tools/bin/sdkmanager 'build-tools;28.0.3' > /dev/null
echo y | $HOME/android-sdk/tools/bin/sdkmanager 'platforms;android-28' > /dev/null
echo y | $HOME/android-sdk/tools/bin/sdkmanager "ndk-bundle" > /dev/null
echo y | $HOME/android-sdk/tools/bin/sdkmanager "ndk;17.2.4988734" > /dev/null


### Build tools

# Build SDK
cd $TRAVIS_BUILD_DIR/TotalCrossSDK ; sh ./gradlew clean dist --console=plain
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders ; ant makeNativeHT
cd $TRAVIS_BUILD_DIR/LitebaseSDK/builders ; ant makeNativeHT
cd $TRAVIS_BUILD_DIR/LitebaseSDK ; ant device

# Build linux-arm target
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/gcc-linux-arm/sdl ; ./build.sh
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/gcc-linux-arm/launcher ; ./build.sh
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/gcc-linux-arm/tcvm ; ./build.sh

# Build linux target
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/gcc-posix/launcher ; ./build.sh
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/gcc-posix/tcvm ; ./build.sh

# Build Android
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/droid/ ; sh ./gradlew clean assembleRelease copyApk --console=plain
