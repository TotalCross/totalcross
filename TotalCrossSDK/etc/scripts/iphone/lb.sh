#!/bin/bash

export LITEBASE=~/workspace/LitebaseSDK

function display_help
{
  echo "`basename $0` arguments:"
  echo "  -1                 build iphone 1.x"
  echo "  -2                 build iphone 2.x"
  echo "  -force             force cvs update -C"
  echo "  -clean             clean before building"
  echo "  -demo              build a demo version"
  echo "  -help              this help message"
  exit
}

type="release"
noras=""

while [ $1 ];
do
  case "$1" in
    -1)
      do_iphone1=1
      shift
      ;;
    -2)
      do_iphone2=1
      shift
      ;;
    -f|-force)
      do_force=1
      shift
      ;;
    -c|-clean)
      do_clean=1
      shift
      ;;
    -d|-demo)
      type="demo"
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

# update the sources
cd $LITEBASE

if [ $do_force ];
then
  cd ..
  cvs co -d /pcvsroot co -r TC LitebaseSDK
  cd $LITEBASE
else
  cvs update
fi

# generate configure if required
if [ ! -f $LITEBASE/builders/gcc/iphone/configure ];
then
  cd $LITEBASE/builders/gcc/iphone && chmod a+x autogen.sh && ./autogen.sh
fi

# build iphone 1.x
if [ $do_iphone1 ];
then
   mkdir -p $LITEBASE/builders/gcc/iphone/$type
   cd $LITEBASE/builders/gcc/iphone/$type
   ../../configure --host=arm-apple-darwin --with-sdk-prefix=../../../../../TotalCrossSDK --enable-$type --build=i386-linux
   if [ $do_clean ]; then
      make clean
   fi
   make -s
fi

# build iphone 2.x
if [ $do_iphone2 ];
then
   mkdir -p $LITEBASE/builders/gcc/iphone2/$type
   cd $LITEBASE/builders/gcc/iphone2/$type
   ../../configure --host=arm-apple-darwin9 --with-sdk-prefix=../../../../../TotalCrossSDK --enable-$type --build=i386-linux
   if [ $do_clean ]; then
      make clean
   fi
   make codesign
fi

