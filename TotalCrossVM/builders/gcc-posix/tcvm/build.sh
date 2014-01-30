#!/bin/bash

function display_help
{
  echo "`basename $0` arguments:"
  echo "  -demo              build a demo version"
  echo "  -help              this help message"
  echo "  -noras             build a noras version"
  exit
}

type="release"
noras=""
out_folder="ras"

while [ $1 ];
do
  case "$1" in
    -d|-demo)
      type="demo"
      out_folder="demo"
      shift
      ;;
    -n|-noras)
      noras="--enable-release "
      type="noras"
      out_folder="noras"
      shift
      ;;
    -h|-help)
      display_help # a function ;-)
      # no shifting needed here, we'll quit!
      exit
      ;;
    *)
      echo "Error: Unknown option: $1" >&2
      exit 1
      ;;
  esac
done

export PKGNAME="TotalCrossVM"
cd $(dirname $0)
export BASEDIR=$(dirname $0)
export WORKSPACE=$(cd -- "../../../../.." && pwd -P 2>/dev/null | pwd -P)
export SDK=$WORKSPACE/TotalCross/TotalCrossSDK
export SPEC_OPTS="--enable-$type $noras"
export REPONAME="TotalCross"

# generate configure if required
if [ ! -f $BASEDIR/configure ];
then
  cd $BASEDIR && chmod a+x autogen.sh && ./autogen.sh
fi

# build
mkdir -p $BASEDIR/linux/$type
cd $BASEDIR/linux/$type
../../configure --enable-$type --with-sdk-prefix=$SDK $SPEC_OPTS
make clean
make -s -j $NUMBER_OF_PROCESSORS
cp -L .libs/libtcvm.so .
