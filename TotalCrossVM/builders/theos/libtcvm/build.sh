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

cd $(dirname $0)
#resolve symlinks to get clean absolute path
export sourcedir=$(cd -- "../../../src" && pwd -P 2>/dev/null | pwd -P)
export type
ln -sf /opt/theos

# build iphone
if [ $do_clean ]; then
  make clean
fi
make
