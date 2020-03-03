#!/bin/bash
export DOCKER_CLI_EXPERIMENTAL=enabled
export DOCKER_BUILDKIT=1
docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
sudo docker buildx create --use --name armbuilder
sudo docker buildx inspect --bootstrap
sudo docker buildx build --platform linux/arm/v7 --load -t totalcross/cross-compile .
