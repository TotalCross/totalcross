#!/bin/bash

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build/TotalCross

echo "STARTING ANDROID"

# ANDROID
mkdir -p $OUTDIR/dist/vm/android
mkdir -p $OUTDIR/etc/launchers/android
mkdir -p $OUTDIR/etc/tools/android
pushd $BASEDIR/TotalCrossVM/builders/droid
   ./build.sh
popd
pushd $BASEDIR/TotalCrossSDK
   cp dist/vm/android/TotalCross.apk $OUTDIR/dist/vm/android
   pushd etc
      cp launchers/android/resources.ap_ $OUTDIR/etc/launchers/android
      pushd tools/android
         cp AndroidManifest_singleApk.xml $OUTDIR/etc/tools/android
         cp resources_singleApk.arsc $OUTDIR/etc/tools/android
         cp AndroidManifest_includeSms.xml $OUTDIR/etc/tools/android
         cp resources_includeSms.arsc $OUTDIR/etc/tools/android
      popd
   popd
popd
