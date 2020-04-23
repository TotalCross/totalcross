#!/bin/bash

export TCVM=~/workspace/TotalCrossVM

function display_help
{
  echo "`basename $0` arguments:"
  echo "  -1                 build iphone for firmware 1.x"
  echo "  -2                 build iphone for firmware 2.x"
  echo "  -linux             build linux"
  echo "  -force             force cvs update -C"
  echo "  -clean             clean before building"
  echo "  -demo              build a demo version"
  echo "  -help              this help message"
  echo "  -noras             build a noras version"
  exit
}

type="release"
noras=""

while [ $1 ];
do
  case "$1" in
    -1|-iphone1)
      do_iphone1=1
      shift
      ;;
    -2|-iphone2)
      do_iphone2=1
      shift
      ;;
    -lx|-linux)
      do_linux=1
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
    -n|-noras)
      noras="--enable-noras"
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
cd $TCVM

if [ $do_force ];
then
  #cvs update does not bring folders that doesn't exist
  cd ..
  cvs -d /pcvsroot co TotalCrossVM
  cd $TCVM
  mv compilation.date src/init
else
  cvs update
fi

# generate configure if required
if [ ! -f $TCVM/builders/gcc-posix/tcvm/configure ];
then
  cd $TCVM/builders/gcc-posix/tcvm && chmod a+x autogen.sh && ./autogen.sh
fi

# build iphone 1.x
if [ $do_iphone1 ];
then
   mkdir -p $TCVM/builders/gcc-posix/tcvm/iphone/$type
   cd $TCVM/builders/gcc-posix/tcvm/iphone/$type
   ../../configure --host=arm-apple-darwin --with-sdk-prefix=../../../../../../TotalCrossSDK --enable-$type $noras --build=i386-linux
   if [ $do_clean ]; then
      make clean
   fi
   make -s
fi

# build iphone 2.x
if [ $do_iphone2 ];
then
   mkdir -p $TCVM/builders/gcc-posix/tcvm/iphone2/$type
   cd $TCVM/builders/gcc-posix/tcvm/iphone2/$type
   echo $PATH
   ../../configure --host=arm-apple-darwin9 --with-sdk-prefix=../../../../../../TotalCrossSDK --enable-$type $noras --build=i386-linux
   if [ $do_clean ]; then
      make clean
   fi
   make codesign
fi

# build linux
if [ $do_linux ];
then
   mkdir -p $TCVM/builders/gcc-posix/tcvm/linux/$type
   cd $TCVM/builders/gcc-posix/tcvm/linux/$type
   echo $PATH
   ../../configure --with-sdk-prefix=../../../../../../TotalCrossSDK --enable-$type $noras
   if [ $do_clean ]; then
      make clean
   fi
   make -s
fi
