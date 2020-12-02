FROM arm32v7/ubuntu:bionic AS build
#

MAINTAINER Italo Yeltsin "br.yeltsin@gmail.com"

# TotalCross
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        cmake \
        ninja-build \
        build-essential \
        libfontconfig1-dev \
        libtool \
        libasound2-dev \
        libpulse-dev \
        libaudio-dev \
        libx11-dev \
        libxext-dev \
        libxrandr-dev \
        libxcursor-dev \
        libxi-dev \
        libxinerama-dev \
        libxxf86vm-dev \
        libxss-dev \
        libgl1-mesa-dev \
        libdbus-1-dev \
        libudev-dev \
        libgles2-mesa-dev \
        libegl1-mesa-dev \
        libibus-1.0-dev \
        fcitx-libs-dev \
        libsamplerate0-dev \
        libsndio-dev \
        libwayland-dev \
        libxkbcommon-dev \
        wayland-protocols \
        git \
        ca-certificates; \
    apt-get install -y libglvnd-dev || apt-get -f install; \
    apt-get clean

RUN git clone https://github.com/SDL-mirror/SDL.git \
        --branch=release-2.0.10 \
        --single-branch \
        --depth=1 \
    && cd SDL \
    && mkdir build; cd build \
    && CFLAGS="-O3 -fPIC" \
    cmake ../ -G Ninja \
        -DSDL_SHARED=0 \
        -DSDL_AUDIO=0 \
        -DVIDEO_VIVANTE=ON \
        -DVIDEO_WAYLAND=ON \
        -DWAYLAND_SHARED=ON; \
    ninja install

# clean up
RUN rm -r /SDL