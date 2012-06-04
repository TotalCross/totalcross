#!/bin/bash

type=""
norasid=""
password=""
keychain="$HOME/Library/Keychains/$(whoami).keychain"
extra_defines=""

function display_help
{
  echo "`basename $0` arguments:"
  echo "  -release <type>    specifies the build release type, either demo, ras or noras (which in this case must also specify the id)."
  echo "  -help              this help message"
  echo "  -keychain <path>   path to the keychain, the default keychain will be used if not provided"
  echo "  -password <pw>     password to unlock the keychain"
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
    -p|-password)
      shift
      password="$1"
      shift
      ;;
    -k|-keychain)
      shift
      keychain="$1"
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

if [ $type == "demo" ]; then
   extra_defines="ENABLE_DEMO"
fi

cd $(dirname $0)
basedir=$(cd -- "../../../.." && pwd -P 2>/dev/null | pwd -P)

cd $basedir/Litebase/LitebaseSDK/builders/xcode
/usr/bin/xcodebuild -alltargets -project Litebase.xcodeproj -configuration Release clean build GCC_PREPROCESSOR_DEFINITIONS="POSIX linux darwin LB_EXPORTS FORCE_LIBC_ALLOC $extra_defines"

cd $basedir/TotalCross/TotalCrossVM/builders/xcode
/usr/bin/xcodebuild -alltargets -project tcvm.xcodeproj -configuration Release clean build GCC_PREPROCESSOR_DEFINITIONS="POSIX linux darwin TOTALCROSS TC_EXPORTS FORCE_LIBC_ALLOC $extra_defines"

/usr/bin/security list-keychains -s $keychain
/usr/bin/security default-keychain -d user -s $keychain
/usr/bin/security unlock-keychain -p $password $keychain
/usr/bin/xcodebuild -alltargets -project TotalCross.xcodeproj -configuration Release build
/usr/bin/xcrun -sdk iphoneos PackageApplication -v $basedir/TotalCross/TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross.app -o $basedir/TotalCross/TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross.ipa
