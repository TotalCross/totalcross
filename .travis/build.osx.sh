#!/bin/bash

export JAVA_HOME=/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
source $TRAVIS_BUILD_DIR/.travis/common.sh

# Break when error occurs
set -e

cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/xcode ; bash -C ./build.sh
