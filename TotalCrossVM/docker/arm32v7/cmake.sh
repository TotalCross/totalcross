#!/bin/bash
BASEDIR=$(dirname $0)
WORKDIR=$(cd $BASEDIR; pwd)

sudo rm -Rf bin
mkdir build

# execute docker run
sudo docker run -v ${WORKDIR}/../arm32v7/build:/build \
                -v ${WORKDIR}/../../:/sources \
                -t totalcross/linux-arm32v7-build bash -c "cmake /sources -G Ninja && ninja"