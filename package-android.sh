#!/bin/bash

OUTDIR=${PWD}/build/TotalCross

echo "STARTING ANDROID"

# ANDROID
mkdir -p $OUTDIR/dist/vm/android
mkdir -p $OUTDIR/etc/launchers/android
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
   pushd app/build/intermediates/processed_res/standardRelease/out
      cp resources-standardRelease.ap_ $OUTDIR/etc/launchers/android/resources.ap_
   popd
popd
