#!/bin/bash

secs_to_human() {
    echo
    echo
    echo "Time Elapsed : $(( ${1} / 3600 ))h $(( (${1} / 60) % 60 ))m $(( ${1} % 60 ))s"
}

start=$(date +%s)

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build/TotalCross

pushd $BASEDIR
    rm -rf $BASEDIR/build/
    git clean -f -d -X
popd

./package-sdk.sh
./package-android.sh
./package-ios.sh

secs_to_human "$(($(date +%s) - ${start}))"