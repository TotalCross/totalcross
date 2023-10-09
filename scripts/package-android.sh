#!/bin/bash

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build/TotalCross

echo "STARTING ANDROID"

# ANDROID
mkdir -p $OUTDIR/dist/vm/android
mkdir -p $OUTDIR/etc/launchers/android
mkdir -p $OUTDIR/etc/tools/android
pushd $BASEDIR/TotalCrossVM/android
   ./gradlew --no-daemon clean assembleAssets bundleRelease
   pushd app/build/outputs/bundle/standardRelease
      cp app-standard-release.aab $OUTDIR/dist/vm/android/TotalCross.aab
   popd
popd
