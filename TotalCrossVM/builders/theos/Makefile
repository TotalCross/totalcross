export TARGET=iphone:latest:5.1
GO_EASY_ON_ME = 1

type ?= demo

ARCHS = armv7

libtcvm_FRAMEWORKS = UIKit CoreGraphics AudioToolbox IOKit CFNetwork CoreLocation Foundation OpenGLES QuartzCore

libtcvm_CFLAGS = -std=gnu99 -fms-extensions -O3 -fomit-frame-pointer -DTHEOS -Dlinux -pthread -DPOSIX -Ddarwin -Ddarwin9 -D_REENTRANT -DTOTALCROSS -DFORCE_LIBC_ALLOC -DTC_EXPORTS -Wall -Wunused-function -Wno-import -fno-strict-aliasing -I${sourcedir}/init -I${sourcedir}/util -I${sourcedir}/tcvm -I${sourcedir}/nm/io -I${sourcedir}/nm/ui -I${sourcedir}/nm/ui/darwin -I${sourcedir}/axtls -I${sourcedir}/event

ifeq ($(type), demo)
	libtcvm_CFLAGS += -DENABLE_DEMO
endif
ifeq ($(type), noras)
	libtcvm_CFLAGS += -DDISABLE_RAS -DNORASID=$(norasid)
	THEOS_OBJ_DIR_NAME = $(type)_$(norasid)
else
	THEOS_OBJ_DIR_NAME = $(type)
endif

EXTRA_UTIL_FILES = $(sourcedir)/util/darwin/debug_c.m

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

MINIZIP_FILES =                               \
	$(sourcedir)/minizip/ioapi.c               \
	$(sourcedir)/minizip/unzip.c               \
	$(sourcedir)/minizip/zip.c

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

RAS_FILES =                                   \
	$(sourcedir)/ras/ras_Utils.c

NM_CRYPTO_FILES =                             \
	$(sourcedir)/nm/crypto/AESCipher.c         \
	$(sourcedir)/nm/crypto/MD5Digest.c         \
	$(sourcedir)/nm/crypto/PKCS1Signature.c    \
	$(sourcedir)/nm/crypto/RSACipher.c         \
	$(sourcedir)/nm/crypto/SHA1Digest.c        \
	$(sourcedir)/nm/crypto/SHA256Digest.c

NM_IO_FILES =                                   \
	$(sourcedir)/nm/io/PDBFile.c                 \
	$(sourcedir)/nm/io/File.c                    \
	$(sourcedir)/nm/io/device_PortConnector.c    \
	$(sourcedir)/nm/io/device/RadioDevice.c      \
	$(sourcedir)/nm/io/device/scanner/zxing.c    \
	$(sourcedir)/nm/io/device/gps/GPS.c

NM_LANG_FILES =                               \
	$(sourcedir)/nm/lang/Class.c               \
	$(sourcedir)/nm/lang/Object.c              \
	$(sourcedir)/nm/lang/String.c              \
	$(sourcedir)/nm/lang/StringBuffer.c        \
	$(sourcedir)/nm/lang/Thread.c              \
	$(sourcedir)/nm/lang/Throwable.c

EXTRA_NM_NET_FILES = \
   $(sourcedir)/nm/net/darwin/Socket_c.m

NM_NET_FILES =                         \
   ${EXTRA_NM_NET_FILES}               \
	$(sourcedir)/nm/net/ssl_SSL.c       \
	$(sourcedir)/nm/net/ServerSocket.c  \
	$(sourcedir)/nm/net/Socket.c        \
	$(sourcedir)/nm/net/ConnectionManager.c

NM_PHONE_FILES =                       \
	$(sourcedir)/nm/phone/SMS.c         \
	$(sourcedir)/nm/phone/CellInfo.c    \
	$(sourcedir)/nm/phone/Dial.c

NM_MAP_FILES =                       \
	$(sourcedir)/nm/map/GoogleMaps.c

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

NM_PIM_FILES =                             \
	$(sourcedir)/nm/pim/POutlook.c

EXTRA_NM_SYS_FILES = \
	$(sourcedir)/nm/sys/darwin/Registry_c.m \
	$(sourcedir)/nm/sys/darwin/Vm_c.m

NM_SYS_FILES =                                  \
    ${EXTRA_NM_SYS_FILES}                       \
	$(sourcedir)/nm/sys/Vm.c                     \
	$(sourcedir)/nm/sys/Time.c                   \
    $(sourcedir)/nm/sys/Registry.c              \
    $(sourcedir)/nm/sys/CharacterConverter.c    \
	$(sourcedir)/nm/sys/Convert.c

EXTRA_NM_UI_FILES =                             \
	$(sourcedir)/nm/ui/darwin/mainview.m         \
	$(sourcedir)/nm/ui/darwin/childview.m        \
	$(sourcedir)/nm/ui/darwin/sipargs.m          \
	$(sourcedir)/nm/ui/darwin/media_Sound_c.m    \
	$(sourcedir)/nm/ui/darwin/Window_c.m

NM_UI_FILES =                                   \
   ${EXTRA_NM_UI_FILES}                         \
	$(sourcedir)/nm/ui/gfx_Graphics.c            \
	$(sourcedir)/nm/ui/event_Event.c             \
	$(sourcedir)/nm/ui/Control.c                 \
	$(sourcedir)/nm/ui/font_Font.c               \
	$(sourcedir)/nm/ui/font_FontMetrics.c        \
	$(sourcedir)/nm/ui/image_Image.c             \
	$(sourcedir)/nm/ui/MainWindow.c              \
	$(sourcedir)/nm/ui/media_Sound.c             \
	$(sourcedir)/nm/ui/media_MediaClip.c         \
	$(sourcedir)/nm/ui/media_Camera.c            \
	$(sourcedir)/nm/ui/Window.c

NM_UTIL_FILES =                            \
	$(sourcedir)/nm/util/concurrent_Lock.c  \
	$(sourcedir)/nm/util/zip_ZLib.c         \
	$(sourcedir)/nm/util/BigInteger.c

NM_UTIL_ZIP_FILES =                             \
   $(sourcedir)/nm/util/zip/CompressedStream.c  \
   $(sourcedir)/nm/util/zip/ZipFile.c           \
   $(sourcedir)/nm/util/zip/ZipEntry.c

NM_XML_FILES =                            \
	$(sourcedir)/nm/xml/xml_XmlTokenizer.c

PALMDB_FILES =                            \
	$(sourcedir)/palmdb/palmdb.c

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

JPEG_FILES =                                 \
   $(sourcedir)/jpeg/jcapimin.c               \
   $(sourcedir)/jpeg/jcapistd.c               \
   $(sourcedir)/jpeg/jccoefct.c               \
   $(sourcedir)/jpeg/jccolor.c                \
   $(sourcedir)/jpeg/jcdctmgr.c               \
   $(sourcedir)/jpeg/jchuff.c                 \
   $(sourcedir)/jpeg/jcinit.c                 \
   $(sourcedir)/jpeg/jcmainct.c               \
   $(sourcedir)/jpeg/jcmarker.c               \
   $(sourcedir)/jpeg/jcmaster.c               \
   $(sourcedir)/jpeg/jcomapi.c                \
   $(sourcedir)/jpeg/jcparam.c                \
   $(sourcedir)/jpeg/jcphuff.c                \
   $(sourcedir)/jpeg/jcprepct.c               \
   $(sourcedir)/jpeg/jcsample.c               \
	$(sourcedir)/jpeg/jidctred.c               \
	$(sourcedir)/jpeg/jdapimin.c               \
	$(sourcedir)/jpeg/jdapistd.c               \
	$(sourcedir)/jpeg/jdatasrc.c               \
   $(sourcedir)/jpeg/jdatadst.c               \
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
	$(sourcedir)/jpeg/jfdctfst.c               \
	$(sourcedir)/jpeg/jidctfst.c               \
	$(sourcedir)/jpeg/jmemmgr.c                \
	$(sourcedir)/jpeg/jmemnobs.c               \
	$(sourcedir)/jpeg/JpegLoader.c             \
	$(sourcedir)/jpeg/jquant1.c                \
	$(sourcedir)/jpeg/jquant2.c                \
	$(sourcedir)/jpeg/jutils.c

EXTRA_EVENT_FILES = $(sourcedir)/event/darwin/event.m

EVENT_FILES =                              \
	${EXTRA_EVENT_FILES}                       \
	$(sourcedir)/event/specialkeys.c            \
	$(sourcedir)/event/Event.c

VM_FILES =                                     \
	$(sourcedir)/tcvm/tcclass.c                \
	$(sourcedir)/tcvm/tcmethod.c               \
	$(sourcedir)/tcvm/tcfield.c                \
	$(sourcedir)/tcvm/tcthread.c               \
	$(sourcedir)/tcvm/tcexception.c            \
	$(sourcedir)/tcvm/objectmemorymanager.c    \
	$(sourcedir)/tcvm/context.c                \
	$(sourcedir)/tcvm/tcvm.c

EXTRA_INIT_FILES = \
	$(sourcedir)/init/darwin/demo_c.m                          \
	$(sourcedir)/init/darwin/startup_c.m                       \
	$(sourcedir)/init/darwin/serialnum.m                       \
	$(sourcedir)/init/darwin/NSString+MD5Addition.m            \
	$(sourcedir)/init/darwin/UIDevice+IdentifierAddition.m

INIT_FILES =                                  \
	${EXTRA_INIT_FILES}                        \
	$(sourcedir)/init/demo.c                   \
	$(sourcedir)/init/startup.c                \
	$(sourcedir)/init/globals.c                \
	$(sourcedir)/init/settings.c

include theos/makefiles/common.mk

LIBRARY_NAME = libtcvm
libtcvm_FILES = ${UTIL_FILES} ${MINIZIP_FILES} ${ZLIB_FILES} ${RAS_FILES} ${NM_CRYPTO_FILES} ${NM_IO_FILES} ${NM_LANG_FILES} ${NM_NET_FILES} ${NM_PHONE_FILES} ${NM_MAP_FILES} ${AXTLS_FILES} ${NM_PIM_FILES} ${NM_SYS_FILES} ${NM_UI_FILES} ${NM_UTIL_FILES} ${NM_UTIL_ZIP_FILES} ${NM_XML_FILES} ${PALMDB_FILES} ${PNG_FILES} ${JPEG_FILES} ${EVENT_FILES} ${VM_FILES} ${INIT_FILES}

include $(THEOS_MAKE_PATH)/library.mk
