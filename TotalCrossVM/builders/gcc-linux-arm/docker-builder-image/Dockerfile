FROM arm32v7/ubuntu:xenial
# 

MAINTAINER Italo Yeltsin "br.yeltsin@gmail.com"


RUN apt-get update && apt-get install -y git && apt-get install -y build-essential && apt-get install -y wget && apt-get install -y python && apt-get install -y libfontconfig1-dev && apt-get install -y mesa-common-dev

RUN apt-get install -y cmake
RUN apt-get install -y build-essential mercurial make cmake autoconf automake \
    libtool libasound2-dev libpulse-dev libaudio-dev libx11-dev libxext-dev \
    libxrandr-dev libxcursor-dev libxi-dev libxinerama-dev libxxf86vm-dev \
    libxss-dev libgl1-mesa-dev libesd0-dev libdbus-1-dev libudev-dev \
    libgles2-mesa-dev libegl1-mesa-dev libibus-1.0-dev \
    fcitx-libs-dev libsamplerate0-dev libsndio-dev
RUN apt-get install -y libwayland-dev libxkbcommon-dev wayland-protocols
RUN apt-get install -y libgles1-mesa-dev || apt-get -f install
RUN apt-get install -y build-essential libgtk-3-dev
RUN apt-get install -y libsdl2-dev
ENV BUILD_FOLDER /build

WORKDIR ${BUILD_FOLDER}
CMD ["/bin/bash", "-c", "make", "-j$(($(nproc) + 2))", "-f", "${BUILD_FOLDER}/Makefile"]
# CMD ["make", "clean"]