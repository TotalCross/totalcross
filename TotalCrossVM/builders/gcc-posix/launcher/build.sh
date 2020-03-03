#!/bin/bash -x
BASEDIR=$(dirname $0)
WORKDIR=$(cd $BASEDIR; pwd)
export SRCDIR="$WORKDIR"/../../../src/launchers/linux
export BLDDIR="${WORKDIR}"

make -f "$WORKDIR"/Makefile
