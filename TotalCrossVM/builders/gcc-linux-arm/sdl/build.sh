#!/bin/bash -x
BASEDIR=$(dirname $0)
WORKDIR=$(cd $BASEDIR; pwd)
mkdir build
git submodule update --init

# reset multi-arch
sudo docker run --rm --privileged multiarch/qemu-user-static --reset -p yes

# execute docker run
sudo docker run \
-v $WORKDIR/../../../deps/SDL:/src \
-v $WORKDIR/build:/build \
-e CFLAGS='-O3 -fPIC' \
-t totalcross/cross-compile \
bash -c "cmake /src -DSDL_SHARED=0 -DSDL_AUDIO=0 -DVIDEO_VIVANTE=ON -DVIDEO_WAYLAND=ON -DWAYLAND_SHARED=ON; make -j$(($(nproc) + 2))"
