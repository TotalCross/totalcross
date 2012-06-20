#!/bin/bash

cd $(dirname $0)
#resolve symlinks to get clean absolute path
export sourcedir=$(cd -- "../../." && pwd -P 2>/dev/null | pwd -P)
ln -sf /opt/theos

make
