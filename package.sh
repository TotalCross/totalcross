#!/bin/bash

secs_to_human() {
    echo
    echo
    echo "Time Elapsed : $(( ${1} / 3600 ))h $(( (${1} / 60) % 60 ))m $(( ${1} % 60 ))s"
}

start=$(date +%s)

OUTDIR=${PWD}/build/TotalCross

rm -rf build/
git clean -f -d -X

./package-sdk.sh & ./package-ios.sh & ./package-android.sh
wait

secs_to_human "$(($(date +%s) - ${start}))"