#!/bin/bash
BASEDIR=$(dirname $0)
WORKDIR=$(cd $BASEDIR; pwd)
#############
# Compile if it doesnt exist
#############
FILE="$WORKDIR"/../sdl/build/libSDL2.a
if [ ! -f $FILE ]
then
    "$WORKDIR"/../sdl/build.sh
else
    echo "SDL was already built!"
fi

############
# Compile skia for linux x86_64 if it doesnt exist
############
FILE="$WORKDIR"/libskia.a
if [ ! -f $FILE ]
then
    echo "TODO compile libskia.a using git submodule"
# TODO Compile using submodule
else
    echo "libskia.a was already built!"
fi

############
# Copile TCVM
############

export SRCDIR="$WORKDIR"/../../../src
export BLDDIR="${WORKDIR}"
export SKIADIR="${WORKDIR}"/../../../deps/skia
export SDLDIR="${WORKDIR}"/../sdl
export SDL_INC="${WORKDIR}"/../../../deps/SDL/include
export LIBS="${WORKDIR}/libskia.a -lstdc++ -lpthread -lfontconfig -lGL ${SDLDIR}/build/libSDL2.a"
bash -c "make  -j$(($(nproc) + 2)) -f ${WORKDIR}/Makefile"