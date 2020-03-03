#!/bin/bash -x
BASEDIR=$(dirname $0)
WORKDIR=$(cd $BASEDIR; pwd)
mkdir "${WORKDIR}/build"
CURRENT_DIR=$(pwd)
git submodule update --init
cd "${WORKDIR}"/build
export CFLAGS='-O3 -fPIC'
echo $(pwd)
cmake $WORKDIR/../../../deps/SDL -DSDL_AUDIO=0 -DVIDEO_VIVANTE=ON -DVIDEO_WAYLAND=ON -DWAYLAND_SHARED=ON
make -j$(($(nproc) + 2))
cd $CURRENT_DIR
echo $(pwd)
