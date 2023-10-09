#!/bin/bash
BASEDIR=$(dirname $0)
WORKDIR=$(pwd)

mkdir -p ${WORKDIR}/.gradle

# execute docker run
docker run                                                  \
    -v ${WORKDIR}/../../../:/totalcross                     \
    -v ${WORKDIR}/.gradle:/root/.gradle                     \
    -it fabernovel/android:api-29-ndk-v1.2.0                \
    bash -c "cd /totalcross/TotalCrossVM/builders/droid  && \
            ./gradlew --no-daemon clean assembleAssets assembleRelease"
