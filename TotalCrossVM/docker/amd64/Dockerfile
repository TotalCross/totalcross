FROM ubuntu:bionic AS build

MAINTAINER Allan César "acmlira@gmail.com"

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        apt-transport-https \
        ca-certificates \
        cmake \
        curl \
        git \
        gnupg \
        libsdl2-dev \
        libfontconfig1-dev \
        ninja-build \
        rsync \
        wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
ENV BUILD_FOLDER /build

WORKDIR ${BUILD_FOLDER}

# utilize o diretorio TotalCrossVM como diretorio de contexto de build para o Docker
COPY . /sources

RUN cmake /sources -G Ninja && ninja

CMD ["/bin/bash", "-c", "make", "-j$(($(nproc) + 2))", "-f", "${BUILD_FOLDER}/Makefile"]

FROM scratch AS export
COPY --from=build /build /
