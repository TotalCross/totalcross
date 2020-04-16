#!/bin/bash -x
BASEDIR=$(dirname $0)
WORKDIR=$(cd $BASEDIR; pwd)

# execute docker run
sudo docker run \
-v $WORKDIR:/build \
-v $WORKDIR/../../../src:/src \
-t totalcross/amd64-cross-compile:bionic