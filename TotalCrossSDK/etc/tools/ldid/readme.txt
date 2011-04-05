LDID the iPhone tool to sign your binaries and prevent the kernel checks to fail.
(ldid-1.0.476.tgz is also available in extlibs/packages).

Install ldid in Linux, so that you can fake codesign it

cd ~/Projects
wget http://svn.telesphoreo.org/trunk/data/ldid/ldid-1.0.476.tgz
tar -zxf ldid-1.0.476.tgz
cd ldid-1.0.476
g++ -I . -o util/ldid{,.cpp} -x c util/{lookup2,sha1}.c
sudo cp -a util/ldid /usr/bin


If you need to codesign the iPhone binary in Linux add this (as one line) to your build script

export CODESIGN_ALLOCATE=/usr/toolchain2/pre/bin/arm-apple-darwin9-codesign_allocate; ldid -S $(PROJECTNAME)

For more details, read :
-http://www.saurik.com/id/8
-http://iphonesdkdev.blogspot.com/2008/10/how-to-install-llvm-gcc-for-iphone-sdk.html