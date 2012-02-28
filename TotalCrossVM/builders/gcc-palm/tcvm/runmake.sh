while [ $1 ]
do
	case "$1" in
		-demo)
		export TYPE=demo
		shift
		;;
		-release)
		export TYPE=release
		shift
		;;
		-noras)
		shift
		export NORASID="$1"
		export TYPE=noras
		shift
		;;
	esac
done

PALM_BASE_DIR=$(cygpath $1)
export PALM_BASE_DIR
cd $PALM_BASE_DIR
make_580
