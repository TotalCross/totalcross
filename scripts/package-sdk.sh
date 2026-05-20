#!/bin/bash

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build/TotalCross

echo "PACKAGE SDK"

mkdir -p $OUTDIR/dist/vm

pushd $BASEDIR/TotalCrossSDK
   cp license.txt $OUTDIR

   # PREPARE ETC
   rsync --recursive \
         --exclude=obfuscator \
         --exclude=scripts \
      etc \
      $OUTDIR

   ./gradlew clean dist -x test
   rsync \
         --exclude=*-intermediate.jar  \
         --exclude=*-proguard.jar      \
      build/libs/totalcross-sdk*.jar \
      $OUTDIR/dist/
   sdk_jar="$(find build/libs -maxdepth 1 -name 'totalcross-sdk-[0-9]*.[0-9]*.[0-9]*.jar' -print -quit)"
   if [ -z "$sdk_jar" ]; then
      echo "Could not find versioned totalcross-sdk jar" >&2
      exit 1
   fi

   cp "$sdk_jar" "$OUTDIR/dist/totalcross-sdk.jar"
   mkdir -p $OUTDIR/dist/libs
   rsync --recursive dist/libs/ $OUTDIR/dist/libs/
   cp build/libs/*.tcz $OUTDIR/dist/vm
   cp etc/fonts/*.tcz $OUTDIR/dist/vm

   # DOCS
   mkdir -p $OUTDIR/docs/html 
   cp -R \
      build/docs/javadoc/.  \
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
