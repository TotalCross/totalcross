cd $WORKSPACE/TotalCrossVM/builders/gcc-posix/tcvm
aclocal
chmod a+x autogen.sh build.sh
./autogen.sh
./build.sh -demo

cd $WORKSPACE/LitebaseSDK/builders/gcc
aclocal
chmod a+x autogen.sh build.sh
./autogen.sh
./build.sh -demo