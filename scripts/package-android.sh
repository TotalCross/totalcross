#!/bin/bash
# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda
# Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build/TotalCross

echo "STARTING ANDROID"

# ANDROID
mkdir -p $OUTDIR/dist/vm/android
mkdir -p $OUTDIR/etc/launchers/android
mkdir -p $OUTDIR/etc/tools/android
pushd $BASEDIR/TotalCrossVM/android
   # GitHub-hosted jobs start with a fresh build tree, so deleting outputs
   # first only prevents reuse of incremental build and task-cache state.
   # Preserve the historical clean build for local packaging.
   if [ "${CI:-}" = "true" ]; then
      ./gradlew --no-daemon assembleAssets bundleRelease
   else
      ./gradlew --no-daemon clean assembleAssets bundleRelease
   fi
   pushd app/build/outputs/bundle/standardRelease
      cp app-standard-release.aab $OUTDIR/dist/vm/android/TotalCross.aab
   popd
popd
