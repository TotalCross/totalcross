#!/bin/bash
export DOCKER_CLI_EXPERIMENTAL=enabled
export DOCKER_BUILDKIT=1
docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
sudo docker build -t totalcross/linux-amd64-build .