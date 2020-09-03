#!/bin/bash
BASEDIR=$(dirname $0)
WORKDIR=$(cd $BASEDIR; pwd)

sudo rm -Rf bin
mkdir build

# execute docker run
sudo docker run -v ${WORKDIR}/../arm64/build:/build \
                -v ${WORKDIR}/../../:/sources \
                -t totalcross/linux-arm64-build \
                bash -c "cmake /sources -DPNG_ARM_NEON_OPT=0 -G Ninja && ninja"

# PNG_ARM_NEON_OPT must be disabled when building for arm64, 
# because NEON instructions are not supported by qemu yet