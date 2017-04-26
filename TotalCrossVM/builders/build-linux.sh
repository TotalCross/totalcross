#!/bin/bash

set -xv

PKG_LIST=(tcvm litebase)
PLATFORM_LIST=(
	"linux:Linux 32-bits x86 platform"
	"linux64:Linux 64-bits x86_64 platform"
	"iphone2:iPhone firmware 2.x"
)

packages=(tcvm)
type="release"
platform=linux
type=release
noras=""
norasid=""
do_force=""
do_clean=false
#out_folder=$type

WORKSPACE_BASE=../../..
TC_SDK=$WORKSPACE_BASE/TotalCross/TotalCrossSDK

function list_platform
{
	local it prefix

	while [ $# -gt 0 ]; do
		case "$1" in
			--prefix=*)
				prefix="${1#*=}"
				shift
				;;
			--prefix)
				shift
				prefix="$1"
				shift
				;;
			*)
				shift
				;;
		esac
	done

	for it in "${PLATFORM_LIST[@]}"; do
		echo "$prefix*${it%%:*} : ${it#*:}"
	done
}

function display_help
{
	#local PKG_LIST=`grep '#PKG=' $0 | grep -o '[^=]*$'`
# --force             force cvs update -C
# --tcvm              build TCVM
# --litebase          build Litebase
cat <<EOM
`basename $0` arguments:
  -h, --help              this help message
      --package=PKG_LIST  build the comma separated list of packages;
                            possible options: ${PKG_LIST[@]}
      --platform=PLATFORM     build for one of the possible platform architectures:
$(list_platform --prefix='                            ')

      --clean             clean before building
      --demo              build a demo version
      --noras id          build a noras version
      --tc-sdk PATHTOSDK  overrides path to TotalCrossSDK with given argument
EOM
}

eval "function is_package
{
	local ret_val=1
	case \"\$1\" in
$( for pkg in ${PKG_LIST[@]}; do
	echo " $pkg) ret_val=0 ;;"
done )
	esac
	return \$ret_val
}"

eval "function is_platform
{
	local ret_val=1
	case \"\$1\" in
$( for tgt in "${PLATFORM_LIST[@]}"; do
	echo " ${tgt%%:*}) ret_val=0 ;;"
done )
	esac
	return \$ret_val
}"

function select_packages
{
	local it candidates

	for it in ${1//,/ }; do
		if is_package $it; then
			candidates=${candidates:+${candidates},}$it
		else
			echo "Unkown package: $it, ignoring it" >&2
		fi
	done

	packages=(${candidates//,/ })
}

function select_platform
{
	if is_platform $1; then
		platform=$1
	else
		echo "Unknown platform: $1, aborting" >&2
		exit 1
	fi
}

function call_configure
{
	local configure_args
	case "$platform" in
		iphone2)
			configure_args="--enable-$type --with-sdk-prefix=$SDK $SPEC_OPTS --host=arm-apple-darwin9 --build=i386-linux"
			;;
		linux64)
			echo "Platform \"$platform\" not yet implemented" >&2
			;;
		linux|*)
			configure_args="--enable-$type --with-sdk-prefix=$SDK $SPEC_OPTS"
			;;
	esac

	../../configure $configure_args
}

function build_tcvm
{
	local WORKSPACE=$WORKSPACE_BASE/TotalCross/TotalCrossVM
	local BASEDIR=${WORKSPACE}/builders/gcc-posix/tcvm
	local OUTDIR=$BASEDIR/$platform/$type${outdir_extra:+.$outdir_extra}
	local SPEC_OPTS="--enable-$type $noras"

	#cd $BASEDIR
	if [ ! -d $OUTDIR  ]; then
		mkdir -p $OUTDIR
	fi

	if [ ! -f $BASEDIR/configure ]; then
		(
			cd $BASEDIR
			./autogen.sh
		)
	fi

	(
		cd $OUTDIR
		call_configure

		if ${do_clean:-false}; then
			make clean
		fi

		make -j $NUMBER_OF_PROCESSORS
		make install
	)
}

#XXX Ainda não foi testada!
function build_litebase
{
	local WORKSPACE=$WORKSPACE_BASE/Litebase/LitebaseSDK
	local BASEDIR=${WORKSPACE}/builders/gcc
	local SPEC_OPTS="--enable-$type $noras"
	local OUTDIR=$BASEDIR/$platform/$type${outdir_extra:+.$outdir_extra}

	echo "This function, build_litebase(), had not yet been tested" >&2

	if [ ! -d $OUTDIR  ]; then
		mkdir -p $OUTDIR
	fi

	if [ ! -f $BASEDIR/configure ]; then
		(
			cd $BASEDIR
			./autogen.sh
		)
	fi

	(
		cd $OUTDIR
		call_configure

		if ${do_clean:-false}; then
			make clean
		fi

		make -j $NUMBER_OF_PROCESSORS
		make install
	)
}

while [ $# -gt 0 ]; do
	case "$1" in
		--package=*)
			select_packages ${1#*=}
			shift
			;;
		--package)
			shift
			select_packages ${1}
			shift
			;;
		-h|--help)
			shift
			display_help $@
			exit
			;;
		--platform=*)
			select_platform ${1#*=}
			shift
			;;
		--platform)
			shift
			select_platform ${1}
			shift
			;;
		--clean)
			do_clean=true
			shift
			;;
#TODO tratamentos para DEMO, NORAS, RELEASE...
#XXX por hora, tratando como se fosse apenas RELEASE
		--sdk=*)
			TC_SDK="${1#*=}"
			shift
			;;
		--sdk)
			shift
			TC_SDK="$1"
			shift
			;;
		
		*)
			echo "Error: Unknown option: $1" >&2
			display_help >&2
			exit 1
			;;
	esac
done

declare -p packages

for pkg in  ${packages[@]}; do
	echo pkg $pkg
	build_${pkg}
done

# build do TCVM
# após entrar no diretório TotalCrossVM/builders/gcc-posix/tcvm:
#    ./autogen.sh
#    ####XXX o autogen.sh foi alterado para esse propósito específico
#    mkdir -p linux/$type; cd linux/$type
#    ../../configure --enable-$type --with-sdk-prefix=$SDK $SPEC_OPTS
#    make
#    make install
# type testado foi o demo;
# $SDK foi /home/jeffque/tc/TotalCross/TotalCrossSDK/;
# SPEC_OPTS foi vazio
