# DONT FORGET TO MOUNT THE OUTPUT FOLDER: mount -tf "o:/" /OutDir

# path locations - the p: is needed so UltraEdit can properly find the errors

include ../common.mk

TYPE ?= release

ifeq ($(TYPE), noras)
	include options_noras.mk
endif
ifeq ($(TYPE), release)
	include options_nodemo.mk
endif
ifeq ($(TYPE), demo)
	include options_demo.mk
endif

TCVM_CODE_RESID_DEC = 4096
TCVM_CODE_RESID_HEX = 1000

_TEST_SUITE ?= DISABLE

ifeq ($(_TEST_SUITE),ENABLE)
TEST_SUITE_OBJS = $(TC_SRCDIR)/tests/tc_tests.o $(TC_SRCDIR)/tests/tc_testsuite.o
endif

VM_FILES =                                    \
	$(TC_SRCDIR)/tcvm/tcthread.o               \
	$(TC_SRCDIR)/tcvm/tcclass.o                \
	$(TC_SRCDIR)/tcvm/objectmemorymanager.o    \
	$(TC_SRCDIR)/tcvm/tcmethod.o               \
	$(TC_SRCDIR)/tcvm/tcfield.o                \
	$(TC_SRCDIR)/tcvm/context.o                \
	$(TC_SRCDIR)/tcvm/tcexception.o            \
	$(TC_SRCDIR)/tcvm/tcvm.o

INIT_FILES =                                  \
	$(TC_SRCDIR)/init/demo.o                   \
	$(TC_SRCDIR)/init/globals.o                \
	$(TC_SRCDIR)/init/startup.o                \
	$(TC_SRCDIR)/init/settings.o

UTIL_FILES =                                  \
	$(TC_SRCDIR)/util/jchar.o                  \
	$(TC_SRCDIR)/util/datastructures.o         \
	$(TC_SRCDIR)/util/debug.o                  \
	$(TC_SRCDIR)/util/tcz.o                    \
	$(TC_SRCDIR)/util/utils.o                  \
	$(TC_SRCDIR)/util/mem.o                    \
	$(TC_SRCDIR)/util/errormsg.o               \
	$(TC_SRCDIR)/util/nativelib.o              \
	$(TC_SRCDIR)/util/dlmalloc.o               \
	$(TC_SRCDIR)/util/xtypes.o

MINIZIP_FILES =                               \
	$(TC_SRCDIR)/minizip/ioapi.o               \
	$(TC_SRCDIR)/minizip/unzip.o               \
	$(TC_SRCDIR)/minizip/zip.o

ZLIB_FILES =                                  \
	$(TC_SRCDIR)/zlib/adler32.o                \
	$(TC_SRCDIR)/zlib/compress.o               \
	$(TC_SRCDIR)/zlib/crc32.o                  \
	$(TC_SRCDIR)/zlib/deflate.o                \
	$(TC_SRCDIR)/zlib/infback.o                \
	$(TC_SRCDIR)/zlib/inffast.o                \
	$(TC_SRCDIR)/zlib/inflate.o                \
	$(TC_SRCDIR)/zlib/inftrees.o               \
	$(TC_SRCDIR)/zlib/trees.o                  \
	$(TC_SRCDIR)/zlib/uncompr.o                \
	$(TC_SRCDIR)/zlib/zutil.o

RAS_FILES =                                   \
	$(TC_SRCDIR)/ras/ras_Utils.o

NM_CRYPTO_FILES =                             \
	$(TC_SRCDIR)/nm/crypto/AESCipher.o         \
	$(TC_SRCDIR)/nm/crypto/MD5Digest.o         \
	$(TC_SRCDIR)/nm/crypto/PKCS1Signature.o    \
	$(TC_SRCDIR)/nm/crypto/RSACipher.o         \
	$(TC_SRCDIR)/nm/crypto/SHA1Digest.o        \
	$(TC_SRCDIR)/nm/crypto/SHA256Digest.o

NM_IO_FILES =                                   \
	$(TC_SRCDIR)/nm/io/PDBFile.o                 \
	$(TC_SRCDIR)/nm/io/File.o                    \
	$(TC_SRCDIR)/nm/io/device_PortConnector.o    \
	$(TC_SRCDIR)/nm/io/device/RadioDevice.o      \
	$(TC_SRCDIR)/nm/io/device/gps/GPS.o

NM_LANG_FILES =                               \
	$(TC_SRCDIR)/nm/lang/Class.o               \
	$(TC_SRCDIR)/nm/lang/Object.o              \
	$(TC_SRCDIR)/nm/lang/String.o              \
	$(TC_SRCDIR)/nm/lang/StringBuffer.o        \
	$(TC_SRCDIR)/nm/lang/Thread.o              \
	$(TC_SRCDIR)/nm/lang/Throwable.o

NM_MAP_FILES =                                \
	$(TC_SRCDIR)/nm/map/GoogleMaps.o

NM_PHONE_FILES =                              \
	$(TC_SRCDIR)/nm/phone/Dial.o               \
	$(TC_SRCDIR)/nm/phone/SMS.o                \
	$(TC_SRCDIR)/nm/phone/CellInfo.o

NM_NET_FILES =                                \
	$(TC_SRCDIR)/nm/net/ssl_SSL.o              \
	$(TC_SRCDIR)/nm/net/ServerSocket.o         \
	$(TC_SRCDIR)/nm/net/Socket.o               \
	$(TC_SRCDIR)/nm/net/ConnectionManager.o

NM_PIM_FILES =                                \
	$(TC_SRCDIR)/nm/pim/POutlook.o

NM_SYS_FILES =                                \
	$(TC_SRCDIR)/nm/sys/CharacterConverter.o   \
	$(TC_SRCDIR)/nm/sys/Registry.o             \
	$(TC_SRCDIR)/nm/sys/Vm.o                   \
	$(TC_SRCDIR)/nm/sys/Time.o                 \
	$(TC_SRCDIR)/nm/sys/Convert.o

NM_UI_FILES =                                 \
	$(TC_SRCDIR)/nm/ui/event_Event.o           \
	$(TC_SRCDIR)/nm/ui/Control.o               \
	$(TC_SRCDIR)/nm/ui/font_Font.o             \
	$(TC_SRCDIR)/nm/ui/font_FontMetrics.o      \
	$(TC_SRCDIR)/nm/ui/gfx_Graphics.o          \
	$(TC_SRCDIR)/nm/ui/image_Image.o           \
	$(TC_SRCDIR)/nm/ui/MainWindow.o            \
	$(TC_SRCDIR)/nm/ui/media_Sound.o           \
	$(TC_SRCDIR)/nm/ui/media_MediaClip.o       \
	$(TC_SRCDIR)/nm/ui/media_Camera.o          \
	$(TC_SRCDIR)/nm/ui/Window.o

NM_UTIL_FILES =                               \
	$(TC_SRCDIR)/nm/util/zip_ZLib.o            \
	$(TC_SRCDIR)/nm/util/BigInteger.o          \
	$(TC_SRCDIR)/nm/util/concurrent_Lock.o

NM_UTIL_ZIP_FILES =                             \
	$(TC_SRCDIR)/nm/util/zip/CompressedStream.o  \
	$(TC_SRCDIR)/nm/util/zip/ZipFile.o           \
	$(TC_SRCDIR)/nm/util/zip/ZipEntry.o

PNG_FILES =                                   \
	$(TC_SRCDIR)/png/pngrutil.o                \
	$(TC_SRCDIR)/png/pngerror.o                \
	$(TC_SRCDIR)/png/pngget.o                  \
	$(TC_SRCDIR)/png/PngLoader.o               \
	$(TC_SRCDIR)/png/pngmem.o                  \
	$(TC_SRCDIR)/png/pngpread.o                \
	$(TC_SRCDIR)/png/pngread.o                 \
	$(TC_SRCDIR)/png/pngrio.o                  \
	$(TC_SRCDIR)/png/pngrtran.o                \
	$(TC_SRCDIR)/png/png.o                     \
	$(TC_SRCDIR)/png/pngset.o                  \
	$(TC_SRCDIR)/png/pngtrans.o

JPEG_FILES =                                  \
	$(TC_SRCDIR)/jpeg/jcapimin.o               \
	$(TC_SRCDIR)/jpeg/jcapistd.o               \
	$(TC_SRCDIR)/jpeg/jccoefct.o               \
	$(TC_SRCDIR)/jpeg/jccolor.o                \
	$(TC_SRCDIR)/jpeg/jcdctmgr.o               \
	$(TC_SRCDIR)/jpeg/jchuff.o                 \
	$(TC_SRCDIR)/jpeg/jcinit.o                 \
	$(TC_SRCDIR)/jpeg/jcmainct.o               \
	$(TC_SRCDIR)/jpeg/jcmarker.o               \
	$(TC_SRCDIR)/jpeg/jcmaster.o               \
	$(TC_SRCDIR)/jpeg/jcomapi.o                \
	$(TC_SRCDIR)/jpeg/jcparam.o                \
	$(TC_SRCDIR)/jpeg/jcphuff.o                \
	$(TC_SRCDIR)/jpeg/jcprepct.o               \
	$(TC_SRCDIR)/jpeg/jcsample.o               \
	$(TC_SRCDIR)/jpeg/jdapimin.o               \
	$(TC_SRCDIR)/jpeg/jdapistd.o               \
	$(TC_SRCDIR)/jpeg/jdatadst.o               \
	$(TC_SRCDIR)/jpeg/jdatasrc.o               \
	$(TC_SRCDIR)/jpeg/jdcoefct.o               \
	$(TC_SRCDIR)/jpeg/jdcolor.o                \
	$(TC_SRCDIR)/jpeg/jddctmgr.o               \
	$(TC_SRCDIR)/jpeg/jdhuff.o                 \
	$(TC_SRCDIR)/jpeg/jdinput.o                \
	$(TC_SRCDIR)/jpeg/jdmainct.o               \
	$(TC_SRCDIR)/jpeg/jdmarker.o               \
	$(TC_SRCDIR)/jpeg/jdmaster.o               \
	$(TC_SRCDIR)/jpeg/jdphuff.o                \
	$(TC_SRCDIR)/jpeg/jdpostct.o               \
	$(TC_SRCDIR)/jpeg/jdsample.o               \
	$(TC_SRCDIR)/jpeg/jerror.o                 \
	$(TC_SRCDIR)/jpeg/jfdctfst.o               \
	$(TC_SRCDIR)/jpeg/jidctfst.o               \
	$(TC_SRCDIR)/jpeg/jidctred.o               \
	$(TC_SRCDIR)/jpeg/jmemmgr.o                \
	$(TC_SRCDIR)/jpeg/jmemnobs.o               \
	$(TC_SRCDIR)/jpeg/JpegLoader.o             \
	$(TC_SRCDIR)/jpeg/jquant1.o                \
	$(TC_SRCDIR)/jpeg/jquant2.o                \
	$(TC_SRCDIR)/jpeg/jutils.o                 \
	$(TC_SRCDIR)/jpeg/rdbmp.o

EVENT_FILES =                                 \
	$(TC_SRCDIR)/event/Event.o                 \
	$(TC_SRCDIR)/event/specialkeys.o

XML_FILES =                                   \
	$(TC_SRCDIR)/nm/xml/xml_XmlTokenizer.o

OBJECT_FILES =                                \
	$(XML_FILES)                               \
	$(EVENT_FILES)                             \
	$(VM_FILES)                                \
	$(INIT_FILES)                              \
	$(UTIL_FILES)                              \
	$(MINIZIP_FILES)                           \
	$(ZLIB_FILES)                              \
	$(RAS_FILES)                               \
	$(NM_CRYPTO_FILES)                         \
	$(NM_IO_FILES)                             \
	$(NM_MAP_FILES)                            \
	$(NM_LANG_FILES)                           \
	$(NM_NET_FILES)                            \
	$(NM_PHONE_FILES)                          \
	$(NM_PIM_FILES)                            \
	$(NM_SYS_FILES)                            \
	$(NM_UI_FILES)                             \
	$(NM_UTIL_FILES)                           \
	$(NM_UTIL_ZIP_FILES)                       \
	$(PNG_FILES)                               \
	$(JPEG_FILES)                              \
	$(PEAL_HOME)/arm/pealstub.o                \
	$(TEST_SUITE_OBJS)

DEFINES = \
	$(EXTRA_DEFINES) \
	$(COMMON_DEFINES) \
	-D$(_TEST_SUITE)_TEST_SUITE \
	-DPALMOS_ALL_SDKS=0 \
	-DTOTALCROSS \
	-DTC_EXPORTS

#	-DTRACE_OBJCREATION \
#	-DDEBUG_OMM_LIST \
#	-DENABLE_DEMO \
#  -DENABLE_TRACE \

PILRC_DEFINES = -D $(_ARM_DEBUG)_ARM_DEBUG -D $(_TEST_SUITE)_TEST_SUITE

CC_OPTIONS = -c $(BUILD_ASM) $(INTERWORK) -s \
			-fPIC -msoft-float -fshort-enums -fno-reorder-blocks -fno-crossjumping -fno-strict-aliasing \
			-Os -march=armv4t -mstructure-size-boundary=8 -ffixed-r8 -ffixed-r9 -mpic-register=r10 -msingle-pic-base \
			-W -Wall -Wpointer-arith -Wno-unknown-pragmas -Wno-multichar -Wno-format-y2k $(DEBUG) $(PALMOS_INCS) \
			-I$(EXTLIBS)/PalmOS5RE/Incs -I. -I$(TC_SRCDIR)/palmposix -I$(TC_SRCDIR)/axtls \
			-I$(TC_SRCDIR)/tcvm -I$(TC_SRCDIR)/util -I$(TC_SRCDIR)/zlib -I$(PEAL_HOME)/arm $(DEFINES)

LDFLAGS = -L$(OUTDIR)/axtls -L$(OUTDIR)/palmposix -L$(EXTLIBS)/PalmOS5RE/Libs -laxtls -lpalmposix -lBoot -lUI -lNET -lpalmOneCamera -lHsExt -lm -lgcc -lDAL -Wl,--split-by-file=64000 --stack=16384

OBJECTS = $(addprefix $(OUTDIR)/tcvm/, $(notdir $(OBJECT_FILES)))

RESOURCE_FILES = tver03e8.bin Talt03ec.bin Talt03ed.bin
RESOURCES = $(addprefix $(OUTDIR)/tcvm/, $(RESOURCE_FILES))

ARMFILE = $(OUTDIR)/tcvm/ARMC$(TCVM_CODE_RESID_HEX).bin.elf

ARMFLAGS = $(INTERWORK) -Wl,-M -Wl,-Map=$(OUTDIR)/tcvm/tcvm.map -Wl,--script arm-elf-palmos.ld -march=armv4t \
	-Wl,--emit-relocs -nostartfiles -I. -I$(TC_SRCDIR)/palmposix -I$(TC_SRCDIR)/tcvm -I$(TC_SRCDIR)/util -nostdlib -fPIC -Wl,--no-warn-mismatch

#########################################################################

all: Init palmposix axtls TCVM.prc

Init:
	@-mkdir -p $(OUTDIR)/tcvm
	@echo CC OPTIONS: $(CC_OPTIONS)
	@echo

$(RESOURCES) :	resources.rcp resources.h
	@echo == Compiling resources...
	@$(PILRC) $(PILRC_DEFINES) resources.rcp $(OUTDIR)/tcvm

.c.o:
	@if test $(basename $@).c -nt $(OUTDIR)/tcvm/$(@F); then \
		echo -- Compiling $(basename $@).c; \
		$(CC) $(CC_OPTIONS) -o $(OUTDIR)/tcvm/$(@F) $(basename $@).c; \
	else :; fi

$(ARMFILE): $(OBJECT_FILES)
	@echo
	@echo == Building ELF file
	@$(CC) $(ARMFLAGS) $(OBJECTS) -o $@ $(LDFLAGS)

$(OUTDIR)/tcvm/TCVM.ro: $(ARMFILE)
	@echo == Post linking...
	@$(POSTLINK) -s $(TCVM_CODE_RESID_DEC) -o $@ -t ARMC $^

TCVM.prc: $(OUTDIR)/tcvm/TCVM.ro $(RESOURCES)
	@echo == Building TCVM.prc
	@$(BUILDPRC) --hidden --no-check-resources --output TCVM.prc --name TCVM --creator TCvm $^
	@echo == Done.

palmposix:
	$(MAKE) -f palmposix.mk

axtls:
	$(MAKE) -f axtls.mk

clean:
	rm -rf $(OUTDIR)/tcvm TCVM.prc
	$(MAKE) -f palmposix.mk clean
	$(MAKE) -f axtls.mk clean
