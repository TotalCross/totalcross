#!/bin/bash
BASEDIR=$(dirname $0)
WORKDIR=$(pwd)

# execute docker run
docker run -v ${WORKDIR}/../../:/vm \
         -it fabernovel/android:api-28-gcloud-ndk-v1.2.0 \
         bash -c "cd /vm/builders/droid && ./gradlew assembleRelease"
