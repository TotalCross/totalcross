#!/bin/bash

type=""
norasid=""

function display_help
{
  echo "`basename $0` arguments:"
  echo "  -clean             clean before building"
  echo "  -demo              build a demo version"
  echo "  -help              this help message"
  echo "  -noras id          build a noras version"
  exit
}

while [ $1 ];
do
  case "$1" in
    -c|-clean)
      do_clean=1
      shift
      ;;
    -r|-release)
      shift
      type="$1"
      if [ $type == "noras" ]; then
        shift
        export norasid="$1"
      fi
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

if [ $type != "demo" ] && [ $type != "ras" ] && [ $type != "noras" ]; then
	echo "Error: Must specify a build type, either demo, ras or noras."
	exit 2
fi
if [ $type == "noras" ] && [ $norasid == "" ]; then
	echo "Error: Must specify a norasid for build type noras."
	exit 3
fi

cd $(dirname $0)
#resolve symlinks to get clean absolute path
export sourcedir=$(cd -- "../../src" && pwd -P 2>/dev/null | pwd -P)
export type
ln -sf /opt/theos

# build iphone
if [ $do_clean ]; then
  make clean
fi
make
