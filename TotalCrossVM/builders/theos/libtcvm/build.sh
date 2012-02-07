#!/bin/bash

function display_help
{
  echo "`basename $0` arguments:"
  echo "  -clean             clean before building"
  echo "  -demo              build a demo version"
  echo "  -help              this help message"
  echo "  -noras id          build a noras version"
  exit
}

type="release"
noras=""
norasid=""

while [ $1 ];
do
  case "$1" in
    -c|-clean)
      do_clean=1
      shift
      ;;
    -d|-demo)
      type="demo"
      shift
      ;;
    -n|-noras)
	  shift
	  norasid="$1"
      noras="--enable-release "
	  type="noras"
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

export BASEDIR=$(dirname $0)
cd ${BASEDIR}
export sourcedir=$(cd -- "../../../src" && pwd -P 2>/dev/null | pwd -P) #resolve symlinks and get clean absolute path
ln -sf /opt/theos
export OUTPUTDIR=$type
export SPEC_OPTS="--enable-$type $noras"

# build iphone
mkdir -p $type
if [ $do_clean ]; then
  make clean
fi
make
