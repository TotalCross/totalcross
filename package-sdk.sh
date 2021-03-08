#!/bin/bash

OUTDIR=${PWD}/build/TotalCross

echo "STARTING SDK"

mkdir -p $OUTDIR/dist/vm/wince
pushd TotalCrossSDK
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

   # CAB bat
   cp etc/tools/makecab/*CEinstall* $OUTDIR/dist/vm/wince
popd
