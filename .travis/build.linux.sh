#!/bin/bash

# Break when error occurs

openssl aes-256-cbc -d -a -pass pass:$JENKINKS_PASS -in jenkins-sa.json.enc -out ~/jenkins-sa.json -d 

cd ~
curl https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-290.0.1-windows-x86_64.zip -o ~/cloud.zip
unzip -q ./cloud.zip
./google-cloud-sdk/install.sh --quiet

gcloud config set disable_usage_reporting false
gcloud auth activate-service-account --key-file ~/jenkins-sa.json

TIMESTAMP=`date +%s`

gcloud compute instances create build-windows-$TIMESTAMP \
      --create-disk image=build-windows,image-project=totalcross,size=50 \
      --zone us-central1-b

#gcloud compute scp folder_you_want_to_copy/ build-windows-cccccc:~/
gcloud compute ssh -t build-windows-CCCC YOUR_COMMAND
gcloud compute instances delete build-windows-$TIMESTAMP


exit 0

set -e

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
