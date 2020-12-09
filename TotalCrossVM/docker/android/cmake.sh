#!/bin/bash
BASEDIR=$(dirname $0)
WORKDIR=$(pwd)

rm -Rf bin
mkdir build

# execute docker run
docker run -v ${WORKDIR}/../android/build:/build \
                -v ${WORKDIR}/../../:/sources \
                -it fabernovel/android:api-28-gcloud-ndk-v1.2.0 /bin/bash
