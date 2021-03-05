#!/bin/bash

OUTDIR=${PWD}/build/TotalCross
IOS_USER=user180640
IOS_SERVER=NY741.macincloud.com

rm -rf build/
git clean -f -d -X

# BUILD SDK
pushd TotalCrossSDK
   mkdir -p $OUTDIR/dist/vm
   cp README.txt $OUTDIR
   cp license.txt $OUTDIR

   # PREPARE ETC
   rsync --recursive \
         --exclude=obfuscator \
         --exclude=scripts \
         --exclude=tools/ant/*[0-9].jar \
         --exclude=tools/jdeb \
         --exclude=tools/makecab/*.inf \
      etc \
      $OUTDIR

   ./gradlew clean dist -x test
   rsync \
         --exclude=*-intermediate.jar  \
         --exclude=*-proguard.jar      \
      build/libs/totalcross-sdk*.jar \
      $OUTDIR/dist/
   cp build/libs/totalcross-sdk-?.?.?.jar $OUTDIR/dist/totalcross-sdk.jar
   cp build/libs/*.tcz $OUTDIR/dist/vm
   cp etc/fonts/TCFont.tcz $OUTDIR/dist/vm

   # DOCS
   mkdir -p $OUTDIR/docs/html 
   cp -r \
      build/docs/javadoc/**  \
      $OUTDIR/docs/html

   # SOURCES
   rsync --recursive -m \
         --exclude=test \
         --exclude=jdkcompat* \
         --exclude=ras \
         --exclude=tc/tools \
         --exclude=**/package.html \
         --exclude=**/subbuild.xml \
         --exclude=**/*4D.java \
      src \
      $OUTDIR
popd

# iOS
scp package-ios.sh $IOS_USER@$IOS_SERVER:~/build/
ssh $IOS_USER@$IOS_SERVER "~/build/package-ios.sh"
scp $IOS_USER@$IOS_SERVER:~/build/ios.zip build/ios.zip
unzip -u -j -q build/ios.zip **/TotalCross.ipa -d $OUTDIR/dist/vm/ios
unzip -u -q build/ios.zip TotalCross.xcarchive/* -d $OUTDIR/etc/tools/iOSCodesign

# ANDROID
mkdir -p $OUTDIR/dist/vm/android
pushd TotalCrossVM/builders/droid
   ./build.sh
   pushd app/build/outputs/apk
      cp standard/release/app-standard-release.apk $OUTDIR/dist/vm/android/TotalCross.apk
      pushd singleApk/release
         unzip -u -q app-singleApk-release.apk AndroidManifest.xml resources.arsc
         cp AndroidManifest.xml $OUTDIR/etc/tools/android/AndroidManifest_singleapk.xml
         cp resources.arsc $OUTDIR/etc/tools/android/resources_singleapk.arsc
      popd
      pushd includeSms/release
         unzip -u -q app-includeSms-release.apk AndroidManifest.xml resources.arsc
         cp AndroidManifest.xml $OUTDIR/etc/tools/android/AndroidManifest_includeSms.xml
         cp resources.arsc $OUTDIR/etc/tools/android/resources_includeSms.arsc
      popd
   popd
popd

