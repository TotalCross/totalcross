LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

TYPE ?= release

TC_SRCDIR     := ..
TC_INCLUDEDIR := $(TC_SRCDIR)/src
LB_SRCDIR     := $(TC_SRCDIR)/../../../../Litebase/LitebaseSDK/src
LB_INCLUDEDIR := $(TC_SRCDIR)/../../../Litebase/LitebaseSDK/src

_TEST_SUITE ?= DISABLE

ifeq ($(_TEST_SUITE),ENABLE)
TEST_SUITE_FILES = $(TC_SRCDIR)/tests/tc_tests.c $(TC_SRCDIR)/tests/tc_testsuite.c
endif

AXTLS_FILES =                                 \
	$(TC_SRCDIR)/axtls/aes.c                   \
	$(TC_SRCDIR)/axtls/asn1.c                  \
	$(TC_SRCDIR)/axtls/bigint.c                \
	$(TC_SRCDIR)/axtls/crypto_misc.c           \
	$(TC_SRCDIR)/axtls/hmac.c                  \
	$(TC_SRCDIR)/axtls/os_port.c               \
	$(TC_SRCDIR)/axtls/loader.c                \
	$(TC_SRCDIR)/axtls/md5.c                   \
	$(TC_SRCDIR)/axtls/md2.c                   \
	$(TC_SRCDIR)/axtls/openssl.c               \
	$(TC_SRCDIR)/axtls/p12.c                   \
	$(TC_SRCDIR)/axtls/rsa.c                   \
	$(TC_SRCDIR)/axtls/rc4.c                   \
	$(TC_SRCDIR)/axtls/sha1.c                  \
	$(TC_SRCDIR)/axtls/sha256.c                \
	$(TC_SRCDIR)/axtls/tls1.c                  \
	$(TC_SRCDIR)/axtls/tls1_svr.c              \
	$(TC_SRCDIR)/axtls/tls1_clnt.c

VM_FILES =                                    \
	$(TC_SRCDIR)/tcvm/tcthread.c               \
	$(TC_SRCDIR)/tcvm/tcclass.c                \
	$(TC_SRCDIR)/tcvm/objectmemorymanager.c    \
	$(TC_SRCDIR)/tcvm/tcmethod.c               \
	$(TC_SRCDIR)/tcvm/tcfield.c                \
	$(TC_SRCDIR)/tcvm/context.c                \
	$(TC_SRCDIR)/tcvm/tcexception.c            \
	$(TC_SRCDIR)/tcvm/tcvm.c

INIT_FILES =                                  \
	$(TC_SRCDIR)/init/demo.c                   \
	$(TC_SRCDIR)/init/globals.c                \
	$(TC_SRCDIR)/init/nativeProcAddressesTC.c  \
	$(TC_SRCDIR)/init/startup.c                \
	$(TC_SRCDIR)/init/settings.c

UTIL_FILES =                                  \
	$(TC_SRCDIR)/util/jchar.c                  \
	$(TC_SRCDIR)/util/datastructures.c         \
	$(TC_SRCDIR)/util/debug.c                  \
	$(TC_SRCDIR)/util/tcz.c                    \
	$(TC_SRCDIR)/util/utils.c                  \
	$(TC_SRCDIR)/util/dlmalloc.c               \
	$(TC_SRCDIR)/util/mem.c                    \
	$(TC_SRCDIR)/util/errormsg.c               \
	$(TC_SRCDIR)/util/nativelib.c              \
	$(TC_SRCDIR)/util/guid.c                   \
	$(TC_SRCDIR)/util/xtypes.c

MINIZIP_FILES =                               \
	$(TC_SRCDIR)/minizip/ioapi.c               \
	$(TC_SRCDIR)/minizip/unzip.c               \
	$(TC_SRCDIR)/minizip/zip.c

ZLIB_FILES =                                  \
	$(TC_SRCDIR)/zlib/adler32.c                \
	$(TC_SRCDIR)/zlib/compress.c               \
	$(TC_SRCDIR)/zlib/crc32.c                  \
	$(TC_SRCDIR)/zlib/deflate.c                \
	$(TC_SRCDIR)/zlib/infback.c                \
	$(TC_SRCDIR)/zlib/inffast.c                \
	$(TC_SRCDIR)/zlib/inflate.c                \
	$(TC_SRCDIR)/zlib/inftrees.c               \
	$(TC_SRCDIR)/zlib/trees.c                  \
	$(TC_SRCDIR)/zlib/uncompr.c                \
	$(TC_SRCDIR)/zlib/zutil.c

RAS_FILES =                                   \
	$(TC_SRCDIR)/ras/ras_Utils.c

MAP_FILES =                                   \
	$(TC_SRCDIR)/nm/map/GoogleMaps.c

NM_CRYPTO_FILES =                             \
	$(TC_SRCDIR)/nm/crypto/AESCipher.c         \
	$(TC_SRCDIR)/nm/crypto/MD5Digest.c         \
	$(TC_SRCDIR)/nm/crypto/PKCS1Signature.c    \
	$(TC_SRCDIR)/nm/crypto/RSACipher.c         \
	$(TC_SRCDIR)/nm/crypto/SHA1Digest.c        \
	$(TC_SRCDIR)/nm/crypto/SHA256Digest.c

NM_IO_FILES =                                 \
	$(TC_SRCDIR)/nm/io/PDBFile.c               \
	$(TC_SRCDIR)/nm/io/File.c                  \
	$(TC_SRCDIR)/nm/io/device_PortConnector.c  \
	$(TC_SRCDIR)/nm/io/device/RadioDevice.c    \
	$(TC_SRCDIR)/nm/io/device/scanner/zxing.c  \
	$(TC_SRCDIR)/nm/io/device/gps/GPS.c

NM_IO_DEVICE_BLUETOOTH_FILES =                \
	$(TC_SRCDIR)/nm/io/device/bluetooth/DiscoveryAgent.c     \
	$(TC_SRCDIR)/nm/io/device/bluetooth/SerialPortClient.c     \
	$(TC_SRCDIR)/nm/io/device/bluetooth/SerialPortServer.c 

NM_LANG_FILES =                               \
	$(TC_SRCDIR)/nm/lang/Reflection.c          \
	$(TC_SRCDIR)/nm/lang/Class.c               \
	$(TC_SRCDIR)/nm/lang/Object.c              \
	$(TC_SRCDIR)/nm/lang/String.c              \
	$(TC_SRCDIR)/nm/lang/StringBuffer.c        \
	$(TC_SRCDIR)/nm/lang/Thread.c              \
	$(TC_SRCDIR)/nm/lang/Throwable.c

NM_NET_FILES =                                \
	$(TC_SRCDIR)/nm/net/ssl_SSL.c              \
	$(TC_SRCDIR)/nm/net/ServerSocket.c         \
	$(TC_SRCDIR)/nm/net/Socket.c               \
	$(TC_SRCDIR)/nm/net/ConnectionManager.c

NM_PIM_FILES =                                \
	$(TC_SRCDIR)/nm/pim/POutlook.c

NM_PHONE_FILES =                              \
	$(TC_SRCDIR)/nm/phone/Dial.c               \
	$(TC_SRCDIR)/nm/phone/SMS.c                \
	$(TC_SRCDIR)/nm/phone/CellInfo.c

NM_SYS_FILES =                                \
	$(TC_SRCDIR)/nm/sys/CharacterConverter.c   \
	$(TC_SRCDIR)/nm/sys/Registry.c             \
	$(TC_SRCDIR)/nm/sys/Vm.c                   \
	$(TC_SRCDIR)/nm/sys/Time.c                 \
	$(TC_SRCDIR)/nm/sys/Convert.c

NM_UI_FILES =                                 \
	$(TC_SRCDIR)/nm/ui/gfx_Graphics.c          \
	$(TC_SRCDIR)/nm/ui/event_Event.c           \
	$(TC_SRCDIR)/nm/ui/Control.c               \
	$(TC_SRCDIR)/nm/ui/font_Font.c             \
	$(TC_SRCDIR)/nm/ui/font_FontMetrics.c      \
	$(TC_SRCDIR)/nm/ui/image_Image.c           \
	$(TC_SRCDIR)/nm/ui/MainWindow.c            \
	$(TC_SRCDIR)/nm/ui/media_Sound.c           \
	$(TC_SRCDIR)/nm/ui/media_MediaClip.c       \
	$(TC_SRCDIR)/nm/ui/media_Camera.c          \
	$(TC_SRCDIR)/nm/ui/Window.c

NM_UTIL_FILES =                               \
	$(TC_SRCDIR)/nm/util/concurrent_Lock.c     \
	$(TC_SRCDIR)/nm/util/zip_ZLib.c            \
	$(TC_SRCDIR)/nm/util/BigInteger.c          \

NM_UTIL_ZIP_FILES =                             \
   $(TC_SRCDIR)/nm/util/zip/CompressedStream.c  \
   $(TC_SRCDIR)/nm/util/zip/ZipFile.c           \
   $(TC_SRCDIR)/nm/util/zip/ZipEntry.c

PNG_FILES =                                   \
	$(TC_SRCDIR)/png/pngrutil.c                \
	$(TC_SRCDIR)/png/pngerror.c                \
	$(TC_SRCDIR)/png/pngget.c                  \
	$(TC_SRCDIR)/png/PngLoader.c               \
	$(TC_SRCDIR)/png/pngmem.c                  \
	$(TC_SRCDIR)/png/pngpread.c                \
	$(TC_SRCDIR)/png/pngread.c                 \
	$(TC_SRCDIR)/png/pngrio.c                  \
	$(TC_SRCDIR)/png/pngrtran.c                \
	$(TC_SRCDIR)/png/png.c                     \
	$(TC_SRCDIR)/png/pngset.c                  \
	$(TC_SRCDIR)/png/pngtrans.c

JPEG_FILES =                                  \
	$(TC_SRCDIR)/jpeg/jcapimin.c               \
	$(TC_SRCDIR)/jpeg/jcapistd.c               \
	$(TC_SRCDIR)/jpeg/jccoefct.c               \
	$(TC_SRCDIR)/jpeg/jccolor.c                \
	$(TC_SRCDIR)/jpeg/jcdctmgr.c               \
	$(TC_SRCDIR)/jpeg/jchuff.c                 \
	$(TC_SRCDIR)/jpeg/jcinit.c                 \
	$(TC_SRCDIR)/jpeg/jcmainct.c               \
	$(TC_SRCDIR)/jpeg/jcmarker.c               \
	$(TC_SRCDIR)/jpeg/jcmaster.c               \
	$(TC_SRCDIR)/jpeg/jcomapi.c                \
	$(TC_SRCDIR)/jpeg/jcparam.c                \
	$(TC_SRCDIR)/jpeg/jcphuff.c                \
	$(TC_SRCDIR)/jpeg/jcprepct.c               \
	$(TC_SRCDIR)/jpeg/jcsample.c               \
	$(TC_SRCDIR)/jpeg/jdapimin.c               \
	$(TC_SRCDIR)/jpeg/jdapistd.c               \
	$(TC_SRCDIR)/jpeg/jdatadst.c               \
	$(TC_SRCDIR)/jpeg/jdatasrc.c               \
	$(TC_SRCDIR)/jpeg/jdcoefct.c               \
	$(TC_SRCDIR)/jpeg/jdcolor.c                \
	$(TC_SRCDIR)/jpeg/jddctmgr.c               \
	$(TC_SRCDIR)/jpeg/jdhuff.c                 \
	$(TC_SRCDIR)/jpeg/jdinput.c                \
	$(TC_SRCDIR)/jpeg/jdmainct.c               \
	$(TC_SRCDIR)/jpeg/jdmarker.c               \
	$(TC_SRCDIR)/jpeg/jdmaster.c               \
	$(TC_SRCDIR)/jpeg/jdphuff.c                \
	$(TC_SRCDIR)/jpeg/jdpostct.c               \
	$(TC_SRCDIR)/jpeg/jdsample.c               \
	$(TC_SRCDIR)/jpeg/jerror.c                 \
	$(TC_SRCDIR)/jpeg/jfdctfst.c               \
	$(TC_SRCDIR)/jpeg/jidctfst.c               \
	$(TC_SRCDIR)/jpeg/jidctred.c               \
	$(TC_SRCDIR)/jpeg/jmemmgr.c                \
	$(TC_SRCDIR)/jpeg/jmemnobs.c               \
	$(TC_SRCDIR)/jpeg/JpegLoader.c             \
	$(TC_SRCDIR)/jpeg/jquant1.c                \
	$(TC_SRCDIR)/jpeg/jquant2.c                \
	$(TC_SRCDIR)/jpeg/jutils.c                 \
	$(TC_SRCDIR)/jpeg/rdbmp.c

EVENT_FILES =                                 \
	$(TC_SRCDIR)/event/Event.c                 \
	$(TC_SRCDIR)/event/specialkeys.c

XML_FILES =                                   \
	$(TC_SRCDIR)/nm/xml/xml_XmlTokenizer.c

PALMDB_FILES =                                \
	$(TC_SRCDIR)/palmdb/palmdb.c
	
SQLITE_FILES =                               \
	$(TC_SRCDIR)/sqlite/sqlite3.c             \
	$(TC_SRCDIR)/nm/db/NativeDB.c               

SCANNER_FILES =                                \
	$(TC_SRCDIR)/scanner/android/Android_barcode.c
	
LITEBASE_FILES = \
	$(LB_SRCDIR)/lbFile.c	\
	$(LB_SRCDIR)/PlainDB.c	\
	$(LB_SRCDIR)/TCVMLib.c	\
	$(LB_SRCDIR)/Litebase.c	\
	$(LB_SRCDIR)/ResultSet.c	\
	$(LB_SRCDIR)/NativeMethods.c	\
	$(LB_SRCDIR)/Table.c \
	$(LB_SRCDIR)/LitebaseGlobals.c \
	$(LB_SRCDIR)/Key.c \
	$(LB_SRCDIR)/Node.c \
	$(LB_SRCDIR)/Index.c \
	$(LB_SRCDIR)/SQLValue.c \
	$(LB_SRCDIR)/MarkBits.c \
	$(LB_SRCDIR)/MemoryFile.c \
	$(LB_SRCDIR)/NormalFile.c \
	$(LB_SRCDIR)/PreparedStatement.c \
	$(LB_SRCDIR)/UtilsLB.c

PARSER_FILES = \
	$(LB_SRCDIR)/parser/LitebaseLex.c \
	$(LB_SRCDIR)/parser/LitebaseMessage.c \
	$(LB_SRCDIR)/parser/LitebaseParser.c \
	$(LB_SRCDIR)/parser/SQLBooleanClause.c \
	$(LB_SRCDIR)/parser/SQLBooleanClauseTree.c \
	$(LB_SRCDIR)/parser/SQLColumnListClause.c \
	$(LB_SRCDIR)/parser/SQLDeleteStatement.c \
	$(LB_SRCDIR)/parser/SQLInsertStatement.c \
	$(LB_SRCDIR)/parser/SQLSelectStatement.c \
	$(LB_SRCDIR)/parser/SQLUpdateStatement.c
	

SOURCE_FILES =                                \
	$(LITEBASE_FILES)                          \
	$(PARSER_FILES)                            \
	$(SQLITE_FILES)                            \
	$(NM_UI_FILES)                             \
	$(EVENT_FILES)                             \
	$(XML_FILES)                               \
	$(UTIL_FILES)                              \
	$(MINIZIP_FILES)                           \
	$(ZLIB_FILES)                              \
	$(INIT_FILES)                              \
	$(NM_IO_FILES)                             \
	$(NM_IO_DEVICE_BLUETOOTH_FILES)            \
	$(NM_LANG_FILES)                           \
	$(NM_NET_FILES)                            \
	$(NM_PIM_FILES)                            \
	$(NM_SYS_FILES)                            \
	$(NM_UTIL_FILES)                           \
	$(NM_UTIL_ZIP_FILES)                       \
	$(NM_PHONE_FILES)                          \
	$(PNG_FILES)                               \
	$(JPEG_FILES)                              \
	$(VM_FILES)                                \
	$(NM_CRYPTO_FILES)                         \
	$(RAS_FILES)                               \
	$(MAP_FILES)                               \
	$(AXTLS_FILES)                             \
	$(PALMDB_FILES)                            \
	$(SCANNER_FILES)                            \
	$(TEST_SUITE_FILES)


LOCAL_ARM_MODE   := arm
LOCAL_MODULE     := tcvm
LOCAL_SRC_FILES  := $(SOURCE_FILES)
LOCAL_C_INCLUDES := $(TC_INCLUDEDIR)/tcvm $(TC_INCLUDEDIR)/axtls $(TC_INCLUDEDIR)/util $(TC_INCLUDEDIR)/zlib $(TC_INCLUDEDIR)/nm/io $(TC_INCLUDEDIR)/scanner $(TC_INCLUDEDIR)/sqlite $(TC_INCLUDEDIR)/nm $(LB_INCLUDEDIR)/parser $(LB_INCLUDEDIR)
LOCAL_LDLIBS     := -llog -ldl -lGLESv2 -lEGL -landroid
LOCAL_CFLAGS     := -DTOTALCROSS -DTC_EXPORTS -D$(_TEST_SUITE)_TEST_SUITE $(EXTRA_DEFINES)
LOCAL_LDFLAGS    := -Wl,-Map,$(NDK_APP_DST_DIR)/$(LOCAL_MODULE).map
NDK_APP_DST_DIR  := $(NDK_OUT)/libs/$(TARGET_ARCH_ABI)

include $(BUILD_SHARED_LIBRARY)
