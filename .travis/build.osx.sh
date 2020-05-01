#!/bin/bash

# Break when error occurs
set -e

cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders/xcode ; bash -C ./build.sh
