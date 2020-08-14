FROM arm32v7/ubuntu:xenial
# 

MAINTAINER Italo Yeltsin "br.yeltsin@gmail.com"

# TotalCross
RUN apt-get update
RUN apt-get install -y cmake ninja-build libfontconfig1-dev

# SDL2 
RUN apt-get install -y build-essential \
    libtool libasound2-dev libpulse-dev libaudio-dev libx11-dev libxext-dev \
    libxrandr-dev libxcursor-dev libxi-dev libxinerama-dev libxxf86vm-dev \
    libxss-dev libgl1-mesa-dev libesd0-dev libdbus-1-dev libudev-dev \
    libgles2-mesa-dev libegl1-mesa-dev libibus-1.0-dev \
    fcitx-libs-dev libsamplerate0-dev libsndio-dev
RUN apt-get install -y libwayland-dev libxkbcommon-dev wayland-protocols
RUN apt-get install -y libgles1-mesa-dev || apt-get -f install
RUN apt-get install -y git
RUN git clone https://github.com/SDL-mirror/SDL.git --branch=release-2.0.10 --single-branch --depth=1 \
    && cd SDL && mkdir build; cd build \
    && CFLAGS="-O3 -fPIC" cmake ../ -DSDL_SHARED=0 -DSDL_AUDIO=0 -DVIDEO_VIVANTE=ON -DVIDEO_WAYLAND=ON \ 
    -DWAYLAND_SHARED=ON; make -j$(($(nproc) + 2)) install 

# clean up
RUN rm -r /SDL  

ENV BUILD_FOLDER /build

WORKDIR ${BUILD_FOLDER}
CMD ["/bin/bash", "-c", "make", "-j$(($(nproc) + 2))", "-f", "${BUILD_FOLDER}/Makefile"]
# CMD ["make", "clean"]