###############################################################################
# Makefile for the Superwaba VM.
# @configure_input@
# $Id: Makefile.am,v 1.77 2010-09-28 18:32:47 guich Exp $
###############################################################################

sourcedir	= ${srcdir}/../../../src

AM_CFLAGS = $(CFLAGS) -DTHEOS -DPOSIX -D_REENTRANT -DTOTALCROSS -DFORCE_LIBC_ALLOC -DTC_EXPORTS -Wall -Wunused-function -Wno-import -fno-strict-aliasing -I${sourcedir}/util
if LINUX
AM_CFLAGS += -I${sourcedir}/init -I${sourcedir}/tcvm -I${sourcedir}/nm/io -I${sourcedir}/nm/ui
endif

if LINUX
AM_CFLAGS += -D_XOPEN_SOURCE=500 -D_BSD_SOURCE @DIRECTFB_CFLAGS@ 
# uncomment for a valgrind build
# AM_CFLAGS += -I/opt/include/valgrind
endif

# enable through "--enable-demo" during "configure"
if DEMO_VERSION
AM_CFLAGS += -DENABLE_DEMO
endif

# enable through "--enable-noras" during "configure"
if NORAS_VERSION
AM_CFLAGS += -DDISABLE_RAS
endif

# enable through "--enable-testsuite" during "configure"
if TESTSUITE
AM_CFLAGS += -DENABLE_TEST_SUITE
endif

SQLITE_CFLAGS_EXTRA = -I$(sourcedir)/sqlite

AM_CFLAGS += $(SQLITE_CFLAGS_EXTRA)

if LINUX
ALL_LIBS = \
	libnmui.la \
	libnmsys.la \
	libutil.la \
	libminizip.la \
	libzlib.la \
	libras.la \
	libaxtls.la \
	libnmphone.la \
	libnmmap.la \
	libnmcrypto.la \
	libnmiofiles.la \
	libnmlang.la \
	libnmnet.la \
	libnmpim.la \
	libnmutil.la \
	libnmutilzip.la \
	libnmxml.la \
	libpalmdb.la \
	libpng.la \
	libjpeg.la \
	libevent.la \
	libvm.la \
	libsqlite3.la \
	libinit.la
else
ALL_LIBS = 
endif

noinst_LTLIBRARIES = ${ALL_LIBS}

if LINUX
inst_LTLIBRARIES = libtcvm.la
else
inst_LTLIBRARIES =
endif
instdir = /home/jeffque/lib/totalcross

if LINUX
libtcvm_la_LDFLAGS = @DIRECTFB_LIBS@ -lm
endif

if DARWIN
EXTRA_UTIL_FILES = $(sourcedir)/util/darwin/debug_c.m
endif

libutil_la_CFLAGS = ${AM_CFLAGS} -I${sourcedir}/nm/ui/darwin

UTIL_FILES =                               \
   ${EXTRA_UTIL_FILES}                        \
	$(sourcedir)/util/jchar.c                  \
	$(sourcedir)/util/datastructures.c         \
	$(sourcedir)/util/debug.c                  \
	$(sourcedir)/util/tcz.c                    \
	$(sourcedir)/util/utils.c                  \
	$(sourcedir)/util/mem.c                    \
	$(sourcedir)/util/nativelib.c              \
	$(sourcedir)/util/errormsg.c               \
	$(sourcedir)/util/dlmalloc.c               \
	$(sourcedir)/util/xtypes.c

libutil_la_SOURCES = ${UTIL_FILES}

MINIZIP_FILES =                               \
	$(sourcedir)/minizip/ioapi.c               \
	$(sourcedir)/minizip/unzip.c               \
	$(sourcedir)/minizip/zip.c

libminizip_la_SOURCES = ${MINIZIP_FILES}

ZLIB_FILES =                                  \
	$(sourcedir)/zlib/adler32.c                \
	$(sourcedir)/zlib/compress.c               \
	$(sourcedir)/zlib/crc32.c                  \
	$(sourcedir)/zlib/deflate.c                \
	$(sourcedir)/zlib/infback.c                \
	$(sourcedir)/zlib/inffast.c                \
	$(sourcedir)/zlib/inflate.c                \
	$(sourcedir)/zlib/inftrees.c               \
	$(sourcedir)/zlib/trees.c                  \
	$(sourcedir)/zlib/uncompr.c                \
	$(sourcedir)/zlib/zutil.c

libzlib_la_SOURCES = ${ZLIB_FILES}

RAS_FILES =                                   \
	$(sourcedir)/ras/ras_Utils.c

libras_la_SOURCES = ${RAS_FILES}

NM_CRYPTO_FILES =                             \
	$(sourcedir)/nm/crypto/AESCipher.c         \
	$(sourcedir)/nm/crypto/MD5Digest.c         \
	$(sourcedir)/nm/crypto/PKCS1Signature.c    \
	$(sourcedir)/nm/crypto/RSACipher.c         \
	$(sourcedir)/nm/crypto/SHA1Digest.c        \
	$(sourcedir)/nm/crypto/SHA256Digest.c

libnmcrypto_la_SOURCES = ${NM_CRYPTO_FILES}

NM_IO_FILES =                                   \
	$(sourcedir)/nm/io/PDBFile.c                 \
	$(sourcedir)/nm/io/File.c                    \
	$(sourcedir)/nm/io/device_PortConnector.c    \
	$(sourcedir)/nm/io/device/RadioDevice.c      \
	$(sourcedir)/nm/io/device/scanner/zxing.c    \
	$(sourcedir)/nm/io/device/gps/GPS.c

libnmiofiles_la_SOURCES = ${NM_IO_FILES}

NM_LANG_FILES =                               \
	$(sourcedir)/nm/lang/Class.c               \
	$(sourcedir)/nm/lang/Object.c              \
	$(sourcedir)/nm/lang/String.c              \
	$(sourcedir)/nm/lang/StringBuffer.c        \
	$(sourcedir)/nm/lang/Thread.c              \
	$(sourcedir)/nm/lang/Throwable.c

libnmlang_la_SOURCES = ${NM_LANG_FILES}

if DARWIN
EXTRA_NM_NET_FILES = \
   $(sourcedir)/nm/net/darwin/Socket_c.m
endif

NM_NET_FILES =                         \
   ${EXTRA_NM_NET_FILES}               \
	$(sourcedir)/nm/net/ssl_SSL.c       \
	$(sourcedir)/nm/net/ServerSocket.c  \
	$(sourcedir)/nm/net/Socket.c        \
	$(sourcedir)/nm/net/ConnectionManager.c

libnmnet_la_SOURCES = ${NM_NET_FILES}

NM_PHONE_FILES =                       \
	$(sourcedir)/nm/phone/SMS.c         \
	$(sourcedir)/nm/phone/CellInfo.c    \
	$(sourcedir)/nm/phone/Dial.c

libnmphone_la_SOURCES = ${NM_PHONE_FILES}

NM_MAP_FILES =                       \
	$(sourcedir)/nm/map/GoogleMaps.c

libnmmap_la_SOURCES = ${NM_MAP_FILES}

AXTLS_FILES =                          \
	$(sourcedir)/axtls/aes.c            \
	$(sourcedir)/axtls/asn1.c           \
	$(sourcedir)/axtls/bigint.c         \
	$(sourcedir)/axtls/crypto_misc.c    \
	$(sourcedir)/axtls/hmac.c           \
	$(sourcedir)/axtls/os_port.c        \
	$(sourcedir)/axtls/loader.c         \
	$(sourcedir)/axtls/md5.c            \
	$(sourcedir)/axtls/md2.c            \
	$(sourcedir)/axtls/openssl.c        \
	$(sourcedir)/axtls/p12.c            \
	$(sourcedir)/axtls/rsa.c            \
	$(sourcedir)/axtls/rc4.c            \
	$(sourcedir)/axtls/sha1.c           \
	$(sourcedir)/axtls/sha256.c         \
	$(sourcedir)/axtls/tls1.c           \
	$(sourcedir)/axtls/tls1_svr.c       \
	$(sourcedir)/axtls/tls1_clnt.c

libaxtls_la_SOURCES = ${AXTLS_FILES}

libnmnet_la_CFLAGS = ${AM_CFLAGS} -I${sourcedir}/axtls -I${sourcedir}/init -I${sourcedir}/util -I${sourcedir}/tcvm -I${sourcedir}/nm/io -I${sourcedir}/nm/ui

NM_PIM_FILES =                             \
	$(sourcedir)/nm/pim/POutlook.c

libnmpim_la_SOURCES = ${NM_PIM_FILES}

if DARWIN
EXTRA_NM_SYS_FILES = \
	$(sourcedir)/nm/sys/darwin/Registry_c.m \
	$(sourcedir)/nm/sys/darwin/Vm_c.m
endif

NM_SYS_FILES =                             \
    ${EXTRA_NM_SYS_FILES}                      \
	$(sourcedir)/nm/sys/Vm.c                   \
	$(sourcedir)/nm/sys/Time.c                 \
    $(sourcedir)/nm/sys/Registry.c             \
    $(sourcedir)/nm/sys/CharacterConverter.c   \
	$(sourcedir)/nm/sys/Convert.c

libnmsys_la_SOURCES = ${NM_SYS_FILES}

if DARWIN
EXTRA_NM_UI_FILES = \
	$(sourcedir)/nm/ui/darwin/mainview.m \
	$(sourcedir)/nm/ui/darwin/childview.m \
	$(sourcedir)/nm/ui/darwin/kbdview.m \
	$(sourcedir)/nm/ui/darwin/sipargs.m \
	$(sourcedir)/nm/ui/darwin/media_Sound_c.m \
	$(sourcedir)/nm/ui/darwin/Window_c.m
endif

NM_UI_FILES =                              \
   ${EXTRA_NM_UI_FILES}                       \
	$(sourcedir)/nm/ui/gfx_Graphics.c          \
	$(sourcedir)/nm/ui/event_Event.c           \
	$(sourcedir)/nm/ui/Control.c               \
	$(sourcedir)/nm/ui/font_Font.c             \
	$(sourcedir)/nm/ui/font_FontMetrics.c      \
	$(sourcedir)/nm/ui/image_Image.c           \
	$(sourcedir)/nm/ui/MainWindow.c            \
	$(sourcedir)/nm/ui/media_Sound.c           \
	$(sourcedir)/nm/ui/media_MediaClip.c       \
	$(sourcedir)/nm/ui/Window.c

libnmui_la_SOURCES = ${NM_UI_FILES}

NM_UTIL_FILES =                            \
	$(sourcedir)/nm/util/concurrent_Lock.c  \
	$(sourcedir)/nm/util/zip_ZLib.c         \
	$(sourcedir)/nm/util/BigInteger.c

libnmutil_la_SOURCES = ${NM_UTIL_FILES}

NM_UTIL_ZIP_FILES =                             \
   $(sourcedir)/nm/util/zip/CompressedStream.c  \
   $(sourcedir)/nm/util/zip/ZipFile.c           \
   $(sourcedir)/nm/util/zip/ZipEntry.c

libnmutilzip_la_SOURCES = ${NM_UTIL_ZIP_FILES}

NM_XML_FILES =                            \
	$(sourcedir)/nm/xml/xml_XmlTokenizer.c

libnmxml_la_SOURCES = ${NM_XML_FILES}

PALMDB_FILES =                            \
	$(sourcedir)/palmdb/palmdb.c

libpalmdb_la_SOURCES = ${PALMDB_FILES}

PNG_FILES =                                \
	$(sourcedir)/png/pngrutil.c                \
	$(sourcedir)/png/pngerror.c                \
	$(sourcedir)/png/pngget.c                  \
	$(sourcedir)/png/PngLoader.c               \
	$(sourcedir)/png/pngmem.c                  \
	$(sourcedir)/png/pngpread.c                \
	$(sourcedir)/png/pngread.c                 \
	$(sourcedir)/png/pngrio.c                  \
	$(sourcedir)/png/pngrtran.c                \
	$(sourcedir)/png/png.c                     \
	$(sourcedir)/png/pngset.c                  \
	$(sourcedir)/png/pngtrans.c

libpng_la_CFLAGS = -DPNG_SETJMP_NOT_SUPPORTED ${AM_CFLAGS}

libpng_la_SOURCES = ${PNG_FILES}

JPEG_FILES =                               \
	$(sourcedir)/jpeg/jidctred.c               \
	$(sourcedir)/jpeg/jdapimin.c               \
	$(sourcedir)/jpeg/jdapistd.c               \
	$(sourcedir)/jpeg/jdatasrc.c               \
	$(sourcedir)/jpeg/jdcoefct.c               \
	$(sourcedir)/jpeg/jdcolor.c                \
	$(sourcedir)/jpeg/jddctmgr.c               \
	$(sourcedir)/jpeg/jdhuff.c                 \
	$(sourcedir)/jpeg/jdinput.c                \
	$(sourcedir)/jpeg/jdmainct.c               \
	$(sourcedir)/jpeg/jdmarker.c               \
	$(sourcedir)/jpeg/jdmaster.c               \
	$(sourcedir)/jpeg/jdphuff.c                \
	$(sourcedir)/jpeg/jdpostct.c               \
	$(sourcedir)/jpeg/jdsample.c               \
	$(sourcedir)/jpeg/jerror.c                 \
	$(sourcedir)/jpeg/jidctfst.c               \
	$(sourcedir)/jpeg/jcomapi.c                \
	$(sourcedir)/jpeg/jmemmgr.c                \
	$(sourcedir)/jpeg/jmemnobs.c               \
	$(sourcedir)/jpeg/JpegLoader.c             \
	$(sourcedir)/jpeg/jquant1.c                \
	$(sourcedir)/jpeg/jquant2.c                \
	$(sourcedir)/jpeg/jutils.c

libjpeg_la_SOURCES = ${JPEG_FILES}

if DARWIN
EXTRA_EVENT_FILES = $(sourcedir)/event/darwin/event.m
endif

EVENT_FILES =                              \
	${EXTRA_EVENT_FILES}                       \
	$(sourcedir)/event/specialkeys.c            \
	$(sourcedir)/event/Event.c

libevent_la_SOURCES = ${EVENT_FILES}

SQLITE3_FILES =    \
        $(sourcedir)/sqlite/sqlite3.c

libsqlite3_la_SOURCES =  \
	$(SQLITE3_FILES)

VM_FILES =                                     \
	$(sourcedir)/tcvm/tcclass.c                \
	$(sourcedir)/tcvm/tcmethod.c               \
	$(sourcedir)/tcvm/tcfield.c                \
	$(sourcedir)/tcvm/tcthread.c               \
	$(sourcedir)/tcvm/tcexception.c            \
	$(sourcedir)/tcvm/objectmemorymanager.c    \
	$(sourcedir)/tcvm/context.c                \
	$(sourcedir)/tcvm/tcvm.c


libvm_la_SOURCES = ${VM_FILES}

if DARWIN
EXTRA_INIT_FILES = \
	$(sourcedir)/init/darwin/demo_c.m \
	$(sourcedir)/init/darwin/serialnum.m
endif

INIT_FILES =                                   \
	${EXTRA_INIT_FILES}                        \
	$(sourcedir)/init/demo.c                   \
	$(sourcedir)/init/startup.c                \
	$(sourcedir)/init/globals.c                \
	$(sourcedir)/init/settings.c

libinit_la_SOURCES = ${INIT_FILES}

#noinst_LTLIBRARIES = libtcvm.la

libtcvm_la_LIBADD = ${ALL_LIBS}

if TESTSUITE
TEST_SUITE_SOURCES = 	\
	$(sourcedir)/tests/tc_tests.c				\
	$(sourcedir)/tests/tc_testsuite.c
endif

libtcvm_la_SOURCES =	\
	$(TEST_SUITE_SOURCES)


inst_PROGRAMS = Launcher

#---------------------- Linux -------------------------
if LINUX
Launcher_CFLAGS = ${AM_CFLAGS} -I${sourcedir}/util
Launcher_LDADD = -ldl
Launcher_SOURCES = $(sourcedir)/launchers/linux/Launcher.c
endif

#---------------------- iPhone -------------------------
if DARWIN

AM_OBJCFLAGS = ${AM_CFLAGS}
if LINUX
AM_OBJCFLAGS += -I${sourcedir}/util -I${sourcedir}/event -I${sourcedir}/nm/ui -I${sourcedir}/nm/ui/darwin -I${sourcedir}/init  -I${sourcedir}/tcvm
endif

Launcher_SOURCES = $(sourcedir)/launchers/darwin/Launcher.m
Launcher_LDADD = ${AM_OBJLDFLAGS} -ldl -lobjc

inst_PROGRAMS += tcpriv
tcpriv_SOURCES = $(sourcedir)/launchers/darwin/tcpriv.m
tcpriv_LDADD = ${AM_OBJLDFLAGS} -lxml2 -lobjc

if DARWIN9
  Launcher_LDFLAGS = ${AM_LDFLAGS} -F${DARWIN_ROOTDIR}/sys/System/Library/PrivateFrameworks \
	  -bind_at_load -framework UIKit -framework CoreGraphics -framework CoreSurface -framework GraphicsServices \
	  -framework CoreFoundation -framework Foundation -w
  tcpriv_LDFLAGS = ${AM_LDFLAGS} -F${DARWIN_ROOTDIR}/sys/System/Library/PrivateFrameworks \
	  -bind_at_load -framework CoreFoundation -framework Foundation -framework AudioToolbox -w
  tcpriv_OBJCFLAGS = -I${DARWIN_ROOTDIR}/sys/usr/include/libxml2 $(AM_OBJCFLAGS)

codesign: all
#	arm-apple-darwin9-strip -S ./Launcher `find .libs/ -type f -name "libtcvm*dylib" -print`
#	CODESIGN_ALLOCATE=${DARWIN_ROOTDIR}/pre/bin/arm-apple-darwin9-codesign_allocate \
#	    ${TOTALCROSS_SDK_ROOT}/etc/tools/ldid/ldid.bin -S ./Launcher `find .libs/ -type f -name "libtcvm*dylib" -print`
else
  Launcher_LDFLAGS = ${AM_LDFLAGS} -framework LayerKit -framework CoreFoundation -framework UIKit -framework CoreGraphics \
      -framework CoreSurface -framework GraphicsServices -framework AudioToolbox
  tcpriv_LDFLAGS = ${AM_LDFLAGS} -framework CoreFoundation -framework Foundation
  tcpriv_OBJCFLAGS = -I${DARWIN_ROOTDIR}/arm-apple-darwin/include/libxml2 $(AM_OBJCFLAGS)
endif

endif
