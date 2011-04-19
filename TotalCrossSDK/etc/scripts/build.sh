#!/bin/bash

function display_help
{
  echo "`basename $0` arguments:"
  echo "  -tcvm              build TCVM"
  echo "  -litebase          build Litebase"
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

package="tcvm"
type="release"
noras=""
do_force=""

while [ $1 ];
do
  case "$1" in
    -tcvm)
      package="tcvm"
      shift
      ;;
    -litebase)
      package="litebase"
      shift
      ;;
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

export WORKSPACE=~/workspace
mkdir -p $WORKSPACE

if [ $package == "tcvm" ];
then
  export PKGNAME="TotalCrossVM"
  export BASEDIR=$WORKSPACE/$PKGNAME/builders/gcc-posix/tcvm
  export SDK=../../../../../../TotalCrossSDK
  export SPEC_OPTS=--enable-$type $noras
  export CO_OPTS="co $PKGNAME"
fi

if [ $package == "litebase" ];
then
  export PKGNAME="LitebaseSDK"
  export BASEDIR=$WORKSPACE/$PKGNAME/builders/gcc
  export SDK=../../../../../TotalCrossSDK
  export CO_OPTS="co -r TC $PKGNAME"
fi

# update the sources
cd $WORKSPACE

if [ ! -d $PKGNAME ] || [ $do_force ];
then
  #cvs -d :ext:frank@tcs/pcvsroot $CO_OPTS
  cvs -d /pcvsroot $CO_OPTS
  cd $PKGNAME
  mv compilation.date src/init
else
  cd $PKGNAME
  cvs update
fi

# generate configure if required
if [ ! -f $BASEDIR/configure ];
then
  cd $BASEDIR && chmod a+x autogen.sh && ./autogen.sh
fi

# build iphone 1.x
if [ $do_iphone1 ];
then
   mkdir -p $BASEDIR/iphone/$type
   cd $BASEDIR/iphone/$type
   echo $PATH
   ../../configure --host=arm-apple-darwin --with-sdk-prefix=$SDK $SPEC_OPTS --build=i386-linux
   if [ $do_clean ]; then
      make clean
   fi
   make -s
fi

# build iphone 2.x
if [ $do_iphone2 ];
then
   mkdir -p $BASEDIR/iphone2/$type
   cd $BASEDIR/iphone2/$type
   echo $PATH
   ../../configure --host=arm-apple-darwin9 --with-sdk-prefix=$SDK $SPEC_OPTS --build=i386-linux
   if [ $do_clean ]; then
      make clean
   fi
   make codesign
fi

# build linux
if [ $do_linux ];
then
   mkdir -p $BASEDIR/linux/$type
   cd $BASEDIR/linux/$type
   echo $PATH
   ../../configure --with-sdk-prefix=$SDK $SPEC_OPTS
   if [ $do_clean ]; then
      make clean
   fi
   make -s
fi
