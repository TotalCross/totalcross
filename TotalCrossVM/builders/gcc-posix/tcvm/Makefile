LDFLAGS = -lpthread -shared 

ifndef BLDDIR
BLDDIR = /build
endif

ifndef SKIADIR
SKIADIR = /skia
endif

ifndef LIBS
LIBS = -L. -lskia -lstdc++ -lpthread -lEGL -lfontconfig -lSDL2main -lSDL2
endif

SKIA_INC=$(addprefix -I,$(wildcard $(SKIADIR)/*))
INCDIR = $(BLDDIR)/inc

OBJDIR = $(BLDDIR)/bin
CFLAGS = -DHAVE_MREMAP=0 -Dlinux -fPIC -shared -c -Os -Wall -DNDEBUG -Wunused-function -Wno-import -fno-strict-aliasing -DHEADLESS -DPOSIX -D_REENTRANT -DTOTALCROSS -DFORCE_LIBC_ALLOC -DTC_EXPORTS -DSQLITE_HAS_CODEC -I$(SRCDIR) -I$(SRCDIR)/tcvm -I$(SRCDIR)/util -I$(SRCDIR)/zlib -I$(SRCDIR)/nm -I$(SRCDIR)/axtls -I$(SRCDIR)/event -I$(SRCDIR)/scanner -I$(SRCDIR)/nm/ui -I$(SRCDIR)/nm/ui/linux -I$(SRCDIR)/nm/io -I$(SRCDIR)/nm/lang -I$(SRCDIR)/litebase/parser  -I$(SRCDIR)/litebase -I$(SRCDIR)/nm/ui/android  
CXXFLAGS = -Os -Wall -fPIC -shared -DNDEBUG -DHEADLESS -DPOSIX -lpthread -std=c++11 $(SKIA_INC) -I$(SRCDIR) -I$(SRCDIR)/tcvm -I$(SRCDIR)/util -I$(SRCDIR)/zlib -I$(SRCDIR)/nm -I$(SRCDIR)/axtls -I$(SRCDIR)/event -I$(SRCDIR)/scanner -I$(SRCDIR)/nm/ui -I$(SRCDIR)/nm/ui/linux -I$(SRCDIR)/nm/io -I$(SRCDIR)/nm/lang -I$(SRCDIR)/litebase/parser  -I$(SRCDIR)/litebase -I$(SRCDIR)/nm/ui/android
CXX_SOURCES = $(SRCDIR)/init/tcsdl.cpp \
$(SRCDIR)/nm/ui/android/skia.cpp 


SRC = $(wildcard $(SRCDIR)/*.c)

UTIL_FILES =                               \
	$(SRCDIR)/util/jchar.c                  \
	$(SRCDIR)/util/datastructures.c         \
	$(SRCDIR)/util/debug.c                  \
	$(SRCDIR)/util/tcz.c                    \
	$(SRCDIR)/util/utils.c                  \
	$(SRCDIR)/util/mem.c                    \
	$(SRCDIR)/util/nativelib.c              \
	$(SRCDIR)/util/errormsg.c               \
	$(SRCDIR)/util/dlmalloc.c               \
	$(SRCDIR)/util/xtypes.c

MINIZIP_FILES =                               \
	$(SRCDIR)/minizip/ioapi.c               \
	$(SRCDIR)/minizip/unzip.c               \
	$(SRCDIR)/minizip/zip.c

ZLIB_FILES =                                  \
	$(SRCDIR)/zlib/adler32.c                \
	$(SRCDIR)/zlib/compress.c               \
	$(SRCDIR)/zlib/crc32.c                  \
	$(SRCDIR)/zlib/deflate.c                \
	$(SRCDIR)/zlib/infback.c                \
	$(SRCDIR)/zlib/inffast.c                \
	$(SRCDIR)/zlib/inflate.c                \
	$(SRCDIR)/zlib/inftrees.c               \
	$(SRCDIR)/zlib/trees.c                  \
	$(SRCDIR)/zlib/uncompr.c                \
	$(SRCDIR)/zlib/zutil.c

RAS_FILES =                                   \
	$(SRCDIR)/ras/ras_Utils.c

NM_CRYPTO_FILES =                             \
	$(SRCDIR)/nm/crypto/AESCipher.c         \
	$(SRCDIR)/nm/crypto/MD5Digest.c         \
	$(SRCDIR)/nm/crypto/PKCS1Signature.c    \
	$(SRCDIR)/nm/crypto/RSACipher.c         \
	$(SRCDIR)/nm/crypto/SHA1Digest.c        \
	$(SRCDIR)/nm/crypto/SHA256Digest.c

NM_IO_FILES =                                   \
	$(SRCDIR)/nm/io/PDBFile.c                 \
	$(SRCDIR)/nm/io/File.c                    \
	$(SRCDIR)/nm/io/device_PortConnector.c    \
	$(SRCDIR)/nm/io/device/RadioDevice.c      \
	$(SRCDIR)/nm/io/device/scanner/zxing.c    \
	$(SRCDIR)/nm/io/device/gps/GPS.c    \
	$(SRCDIR)/nm/io/device/bluetooth/DiscoveryAgent.c    \
	$(SRCDIR)/nm/io/device/bluetooth/SerialPortClient.c    \
	$(SRCDIR)/nm/io/device/bluetooth/SerialPortServer.c


NM_LANG_FILES =                               \
	$(SRCDIR)/nm/lang/Class.c               \
	$(SRCDIR)/nm/lang/Object.c              \
	$(SRCDIR)/nm/lang/String.c              \
	$(SRCDIR)/nm/lang/StringBuffer.c        \
	$(SRCDIR)/nm/lang/Thread.c              \
	$(SRCDIR)/nm/lang/Throwable.c           \
	$(SRCDIR)/nm/lang/Reflection.c			\
	${SRCDIR}/nm/lang/Runtime.c				\
	${SRCDIR}/nm/lang/cpproc.c 				\
	${SRCDIR}/nm/nio/channels/FileChannelImpl.c \
	${SRCDIR}/nm/lang/Process.c

NM_NET_FILES =                         \
	$(SRCDIR)/nm/net/ssl_SSL.c       \
	$(SRCDIR)/nm/net/ServerSocket.c  \
	$(SRCDIR)/nm/net/Socket.c        \
	$(SRCDIR)/nm/net/ConnectionManager.c

NM_PHONE_FILES =                       \
	$(SRCDIR)/nm/phone/SMS.c         \
	$(SRCDIR)/nm/phone/CellInfo.c    \
	$(SRCDIR)/nm/phone/Dial.c    \
	$(SRCDIR)/nm/telephony/SmsManager.c

NM_MAP_FILES =                       \
	$(SRCDIR)/nm/map/GoogleMaps.c

AXTLS_FILES =                          \
	$(SRCDIR)/axtls/aes.c            \
	$(SRCDIR)/axtls/asn1.c           \
	$(SRCDIR)/axtls/bigint.c         \
	$(SRCDIR)/axtls/crypto_misc.c    \
	$(SRCDIR)/axtls/hmac.c           \
	$(SRCDIR)/axtls/os_port.c        \
	$(SRCDIR)/axtls/loader.c         \
	$(SRCDIR)/axtls/md5.c            \
	$(SRCDIR)/axtls/md2.c            \
	$(SRCDIR)/axtls/openssl.c        \
	$(SRCDIR)/axtls/p12.c            \
	$(SRCDIR)/axtls/rsa.c            \
	$(SRCDIR)/axtls/rc4.c            \
	$(SRCDIR)/axtls/sha1.c           \
	$(SRCDIR)/axtls/sha256.c         \
	$(SRCDIR)/axtls/sha384.c         \
	$(SRCDIR)/axtls/sha512.c         \
	$(SRCDIR)/axtls/x509.c           \
	$(SRCDIR)/axtls/tls1.c           \
	$(SRCDIR)/axtls/tls1_svr.c       \
	$(SRCDIR)/axtls/tls1_clnt.c      \
	$(SRCDIR)/openbsd/libutil/pkcs5_pbkdf2.c

NM_PIM_FILES =                             \
	$(SRCDIR)/nm/pim/POutlook.c     \
	$(SRCDIR)/nm/firebase/firebase.c     \
	$(SRCDIR)/nm/notification/notification.c     \
	$(SRCDIR)/nm/cielo/printer/PrinterManager.c     \
	$(SRCDIR)/scanner/ios/ios_barcode.c     

NM_SYS_FILES =                             \
	$(SRCDIR)/nm/sys/Vm.c                   \
	$(SRCDIR)/nm/sys/Time.c                 \
    $(SRCDIR)/nm/sys/Registry.c             \
    $(SRCDIR)/nm/sys/CharacterConverter.c   \
	$(SRCDIR)/nm/sys/Convert.c

NM_UI_FILES =                              \
	$(SRCDIR)/nm/ui/gfx_Graphics.c          \
	$(SRCDIR)/nm/ui/event_Event.c           \
	$(SRCDIR)/nm/ui/Control.c               \
	$(SRCDIR)/nm/ui/font_Font.c             \
	$(SRCDIR)/nm/ui/font_FontMetrics.c      \
	$(SRCDIR)/nm/ui/image_Image.c           \
	$(SRCDIR)/nm/ui/MainWindow.c            \
	$(SRCDIR)/nm/ui/media_Sound.c           \
	$(SRCDIR)/nm/ui/media_MediaClip.c       \
	$(SRCDIR)/nm/ui/media_Camera.c       \
	$(SRCDIR)/nm/ui/YoutubePlayer.c       \
	$(SRCDIR)/nm/ui/Window.c

NM_UTIL_FILES =                            \
	$(SRCDIR)/nm/util/concurrent_Lock.c  \
	$(SRCDIR)/nm/util/zip_ZLib.c         \
	$(SRCDIR)/nm/util/BigInteger.c       \
	$(SRCDIR)/nm/util/Locale.c

NM_UTIL_ZIP_FILES =                             \
   $(SRCDIR)/nm/util/zip/CompressedStream.c  \
   $(SRCDIR)/nm/util/zip/ZipFile.c           \
   $(SRCDIR)/nm/util/zip/ZipEntry.c

NM_XML_FILES =                            \
	$(SRCDIR)/nm/xml/xml_XmlTokenizer.c

PALMDB_FILES =                            \
	$(SRCDIR)/palmdb/palmdb.c

PNG_FILES =                                \
	$(SRCDIR)/png/pngrutil.c                \
	$(SRCDIR)/png/pngerror.c                \
	$(SRCDIR)/png/pngget.c                  \
	$(SRCDIR)/png/PngLoader.c               \
	$(SRCDIR)/png/pngmem.c                  \
	$(SRCDIR)/png/pngpread.c                \
	$(SRCDIR)/png/pngread.c                 \
	$(SRCDIR)/png/pngrio.c                  \
	$(SRCDIR)/png/pngrtran.c                \
	$(SRCDIR)/png/png.c                     \
	$(SRCDIR)/png/pngtrans.c \
	$(SRCDIR)/png/pngset.c

JPEG_FILES =                               \
    ${SRCDIR}/jpeg/jcapimin.c \
    ${SRCDIR}/jpeg/jcapistd.c \
    ${SRCDIR}/jpeg/jccoefct.c \
    ${SRCDIR}/jpeg/jccolor.c \
    ${SRCDIR}/jpeg/jcdctmgr.c \
    ${SRCDIR}/jpeg/jchuff.c \
    ${SRCDIR}/jpeg/jcinit.c \
    ${SRCDIR}/jpeg/jcmainct.c \
    ${SRCDIR}/jpeg/jcmarker.c \
    ${SRCDIR}/jpeg/jcmaster.c \
    ${SRCDIR}/jpeg/jcomapi.c \
    ${SRCDIR}/jpeg/jcparam.c \
    ${SRCDIR}/jpeg/jcphuff.c \
    ${SRCDIR}/jpeg/jcprepct.c \
    ${SRCDIR}/jpeg/jcsample.c \
    ${SRCDIR}/jpeg/jdapimin.c \
    ${SRCDIR}/jpeg/jdapistd.c \
    ${SRCDIR}/jpeg/jdatadst.c \
    ${SRCDIR}/jpeg/jdatasrc.c \
    ${SRCDIR}/jpeg/jdcoefct.c \
    ${SRCDIR}/jpeg/jdcolor.c \
    ${SRCDIR}/jpeg/jddctmgr.c \
    ${SRCDIR}/jpeg/jdhuff.c \
    ${SRCDIR}/jpeg/jdinput.c \
    ${SRCDIR}/jpeg/jdmainct.c \
    ${SRCDIR}/jpeg/jdmarker.c \
    ${SRCDIR}/jpeg/jdmaster.c \
    ${SRCDIR}/jpeg/jdphuff.c \
    ${SRCDIR}/jpeg/jdpostct.c \
    ${SRCDIR}/jpeg/jdsample.c \
    ${SRCDIR}/jpeg/jerror.c \
    ${SRCDIR}/jpeg/jfdctfst.c \
    ${SRCDIR}/jpeg/jmemmgr.c \
    ${SRCDIR}/jpeg/jmemnobs.c \
    ${SRCDIR}/jpeg/JpegLoader.c \
    ${SRCDIR}/jpeg/jquant1.c \
    ${SRCDIR}/jpeg/jquant2.c \
    ${SRCDIR}/jpeg/jutils.c \
    ${SRCDIR}/jpeg/jcarith.c \
    ${SRCDIR}/jpeg/jdarith.c \
    ${SRCDIR}/jpeg/jdmerge.c \
    ${SRCDIR}/jpeg/jidctflt.c \
    ${SRCDIR}/jpeg/jidctfst.c \
    ${SRCDIR}/jpeg/jidctint.c \
    ${SRCDIR}/jpeg/jfdctflt.c \
    ${SRCDIR}/jpeg/jfdctint.c \
    ${SRCDIR}/jpeg/jaricom.c \
    ${SRCDIR}/jpeg/rdbmp.c


EVENT_FILES =                              \
	$(SRCDIR)/event/specialkeys.c            \
	$(SRCDIR)/event/Event.c

SQLITE3_FILES =    \
    $(SRCDIR)/litebase/Index.c \
    $(SRCDIR)/litebase/Key.c \
    $(SRCDIR)/litebase/lbFile.c \
    $(SRCDIR)/litebase/Litebase.c \
    $(SRCDIR)/litebase/LitebaseGlobals.c \
    $(SRCDIR)/litebase/MarkBits.c \
    $(SRCDIR)/litebase/MemoryFile.c \
    $(SRCDIR)/litebase/NativeMethods.c \
    $(SRCDIR)/litebase/nativeProcAddressesLB.c \
    $(SRCDIR)/litebase/Node.c \
    $(SRCDIR)/litebase/NormalFile.c \
    $(SRCDIR)/litebase/PlainDB.c \
    $(SRCDIR)/litebase/PreparedStatement.c \
    $(SRCDIR)/litebase/ResultSet.c \
    $(SRCDIR)/litebase/SQLValue.c \
    $(SRCDIR)/litebase/Table.c \
    $(SRCDIR)/litebase/TCVMLib.c \
    $(SRCDIR)/litebase/UtilsLB.c \
    $(SRCDIR)/litebase/parser/LitebaseLex.c \
    $(SRCDIR)/litebase/parser/LitebaseMessage.c \
    $(SRCDIR)/litebase/parser/LitebaseParser.c \
    $(SRCDIR)/litebase/parser/SQLBooleanClause.c \
    $(SRCDIR)/litebase/parser/SQLBooleanClauseTree.c \
    $(SRCDIR)/litebase/parser/SQLColumnListClause.c \
    $(SRCDIR)/litebase/parser/SQLDeleteStatement.c \
    $(SRCDIR)/litebase/parser/SQLInsertStatement.c \
    $(SRCDIR)/litebase/parser/SQLSelectStatement.c \
    $(SRCDIR)/litebase/parser/SQLUpdateStatement.c

VM_FILES = \
	$(SRCDIR)/tcvm/tcclass.c \
	$(SRCDIR)/tcvm/tcmethod.c \
	$(SRCDIR)/tcvm/tcfield.c \
	$(SRCDIR)/tcvm/tcthread.c \
	$(SRCDIR)/tcvm/tcexception.c \
	$(SRCDIR)/tcvm/objectmemorymanager.c \
	$(SRCDIR)/tcvm/context.c \
	$(SRCDIR)/tcvm/tcvm.c

INIT_FILES =                                \
	${EXTRA_INIT_FILES}                     \
	$(SRCDIR)/init/demo.c                   \
	$(SRCDIR)/init/startup.c                \
	$(SRCDIR)/init/globals.c                \
	$(SRCDIR)/init/settings.c               \
	${SRCDIR}/init/nativeProcAddressesTC.c  \
	${SRCDIR}/sqlite/sqlite3.c              \
	${SRCDIR}/nm/db/NativeDB.c

QRCODE_FILES = 								\
	${SRCDIR}/nm/qrcode/qrcode_generation.c \
    ${SRCDIR}/nm/qrcode/qrcode.c

GPIO_FILES = 								\
	${SRCDIR}/nm/io/device/gpiod/Gpiod.c      \
	${SRCDIR}/nm/io/device/gpiod/posix/gpiodLib.c

SRC += $(UTIL_FILES) $(MINIZIP_FILES) $(ZLIB_FILES) $(RAS_FILES) $(NM_CRYPTO_FILES) $(NM_IO_FILES) $(NM_LANG_FILES) $(NM_NET_FILES) $(NM_PHONE_FILES) $(NM_MAP_FILES) $(AXTLS_FILES) $(NM_PIM_FILES) $(NM_SYS_FILES) $(NM_UI_FILES) $(NM_UTIL_FILES) $(NM_UTIL_ZIP_FILES) $(NM_XML_FILES) $(PALMDB_FILES)  $(JPEG_FILES) $(PNG_FILES) $(EVENT_FILES) $(SQLITE3_FILES) $(VM_FILES) $(INIT_FILES) $(QRCODE_FILES) $(GPIO_FILES)

OBJ = $(patsubst $(SRCDIR)/%.c, $(OBJDIR)/%.o, $(SRC))
CXX_OBJECTS = $(patsubst $(SRCDIR)/%.cpp, $(OBJDIR)/%.o, $(CXX_SOURCES))
EXE = $(OBJDIR)/libtcvm.so

libutil_la_SOURCES = ${UTIL_FILES}

all: clean $(EXE) 

	echo $(CXX_SOURCES)
    	
$(EXE): $(CXX_OBJECTS) $(OBJ) 
	$(CC) $(LDFLAGS) $(LIBS) $(CXXFLAGS) $(OBJDIR)/**/*.o $(OBJDIR)/**/**/*.o $(OBJDIR)/**/**/**/*.o $(OBJDIR)/**/**/**/**/*.o -o $@ $(LIBS)

# Compile qrcode using c99
$(OBJDIR)/nm/qrcode/qrcode.o : $(SRCDIR)/nm/qrcode/qrcode.c
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) -std=c99 $< -o $@

$(OBJDIR)/%.o : $(SRCDIR)/%.c
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) $< -o $@

$(OBJDIR)/%.o: $(SRCDIR)/%.cpp
	@mkdir -p $(@D)
	$(CXX) $(CPPFLAGS) $(CXXFLAGS) -c "$<" -o "$@"



clean:
	-rm -rf $(OBJDIR)/*.o $(EXE)
