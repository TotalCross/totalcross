This note explains how to setup a PalmOS/ARM development environment to build the Totalcross PalmOS/ARM version
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

For a complete build environment, you have to setup the following list of tools:

-PEAL, the Palm ELF ARM Loader which allows to compile an elf relocatable binary and load a
big executable split in Palm records less than 64K and fullfil standard elf loading on the device
just like any other platform loader does.
see http://www.sealiesoftware.com/peal for more information

-an "Unofficial PalmOS 5 SDK for native applications/libraries and advanced PNOlets" from
the "mobile-stream" company. This library generates ARM stubs to call natively PalmOS syscalls
and thus avoid the drawback of switching back and forth to m68k emulated mode.
see http://www.mobile-stream.com/devzone.html

-PilRC is the PILot Resource Compiler.
It takes an ascii input file containing descriptions of Palm Pilot resources and outputs
binary resources that can be included into a '.prc' file.
see http://pilrc.sourceforge.net/

-prc-tools the Palm OS programming with GCC tools for the m68k bootstrap code compilation.
see http://prc-tools.sourceforge.net/

-A PalmOS SDK version >= sdk-5r4. Insofar no official SDK supporting Linux has been release since sdk-5r3, you can use for instance
the official sdk-5r4 (the one provide with PODS works fine). But you have to fix 2 issues:
.replace in "include/Core/System/SystemPublic.h" the include "IMCUtils.h" by "ImcUtils.h" insofar Linux, like all good OSes, is case sensitive.
.convert linefeeds from dos to unix in all include files insofar gcc2.95 cannot handle lines with macros written on several lines with the '\'
character when this character is followed by a dos linefeed. Execute in a Linux shell for instance: "find . -name "*.h" -exec dos2unix -f {} \;"

You need to build the two above mentioned components PEAL and PalmOS5RE. Just go to their respective build
folders "extlibs/peal/postlink", "extlibs/PalmOS5RE/Tools" and "extlibs/PalmOS5RE/Libs" and
just run "make". Both tools are located with relativ paths so it's important to
retrieve "extlibs" and "TotalCrossVM" at a same level (folder).

The Peal tool requires a host gcc compiler to build the peal-postlink tool
that will be executed on your system to process the arm elf binary to convert it to palm resource records.
The PalmOS5RE was delivered to use the prc-tools assembler (arm-palmos-as). We switched to gnuarm
(www.gnuarm.org) which is the our recommanded arm toolchain.


You may install the setup provided for your system or follow the build instructions on the site (see below):

NOTE: Since the gnuarm.org site is updated with newer version that have not been tested, here is the list of the packages I currently use with success:
-http://ftp.gnu.org/gnu/binutils/binutils-2.17.tar.bz2
-http://ftp.gnu.org/gnu/gcc/gcc-4.2.0/gcc-4.2.0.tar.bz2
-ftp://sources.redhat.com/pub/newlib/newlib-1.15.0.tar.gz

***************
ARM toolchain Build process

GCC's t-arm-elf file was updated to enable support for the following options:

    * marm/mthumb (arm or thumb code generation)
    * mlittle-endian/mbig-endian (little or big endian architectures)
    * mhard-float/msoft-float (hardware or software fpu instructions)
    * mno-thumb-interwork/mthumb-interwork (arm-thumb modes interworking)
    * mcpu=arm7 (arm7 targets without hardware multiply) 

The disabled options were mapcs-32/mapcs-26 (for old ARM machines with 26-bit program counters) and fno-leading-underscore/fleading-underscore (non-ELF library function format).

The following steps and configuration switches were used in the compilation process:

   1. cd [binutils-build]
   2. [binutils-source]/configure --target=arm-elf --prefix=[toolchain-prefix] --enable-interwork --enable-multilib --with-float=soft
   3. make all install
   4. export PATH="$PATH:[toolchain-prefix]/bin"
   5. cd [gcc-build]
   6. [gcc-source]/configure --target=arm-elf --prefix=[toolchain-prefix] --enable-interwork --enable-multilib --with-float=soft --enable-languages="c,c++" --with-newlib --with-headers=[newlib-source]/newlib/libc/include
   7. make all-gcc install-gcc
   8. cd [newlib-build]
   9. [newlib-source]/configure --target=arm-elf --prefix=[toolchain-prefix] --enable-interwork --enable-multilib --with-float=soft
  10. make all install
  11. cd [gcc-build]
  12. make all install


That's it!
Frank Diebolt <frank@nospam.superwaba_nospam_com.br>



********************************************************************************************************

LICENSE

Peal was written by Greg Parker <gparker-peal@sealiesoftware.com>,
using ELF headers by David O'Brien and John Polstra.

***

Copyright (c) 2004-2005 Greg Parker.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY GREG PARKER ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

***

Copyright (c) 2001 David E. O'Brien
Copyright (c) 1996-1998 John D. Polstra.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

***
