FROM arm64v8/ubuntu:bionic AS build
# 

# TotalCross
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        cmake \
        ninja-build \
        build-essential \
        libfontconfig1-dev \
# SDL2 
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
# Lastly available on Xenial, deprecated for Bionic
        libesd0-dev \
        libdbus-1-dev \
        libudev-dev \
        libgles2-mesa-dev \
        libegl1-mesa-dev \
        libibus-1.0-dev \
        fcitx-libs-dev \
        libsamplerate0-dev \
        libsndio-dev \
# Wayland
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

ENV BUILD_FOLDER /build

WORKDIR ${BUILD_FOLDER}

# utilize o diretorio TotalCrossVM como diretorio de contexto de build para o Docker
COPY . /sources

RUN cmake /sources -DPNG_ARM_NEON_OPT=0 -G Ninja && ninja

CMD ["/bin/bash", "-c", "make", "-j$(($(nproc) + 2))", "-f", "${BUILD_FOLDER}/Makefile"]
# CMD ["make", "clean"]

FROM scratch AS export
COPY --from=build /build /