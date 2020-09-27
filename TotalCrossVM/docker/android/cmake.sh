#!/bin/bash
BASEDIR=$(dirname $0)
WORKDIR=$(pwd)

mkdir -p ${WORKDIR}/.gradle

# execute docker run
docker run 
    -v ${WORKDIR}/../../:/vm                                \
    -v ${WORKDIR}/.gradle:/root/.gradlew                    \
    -it fabernovel/android:api-28-gcloud-ndk-v1.2.0         \
    bash -c "cd /vm/android && ./gradlew assembleDebug"
