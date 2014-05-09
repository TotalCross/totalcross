# DONT FORGET TO MOUNT THE OUTPUT FOLDER: mount -tf "o:/" /OutDir

# path locations - the p: is needed so UltraEdit can properly find the errors
include ../../../../TotalCross/TotalCrossVM/builders/gcc-palm/common.mk
include options.mk

LBDIR ?= ../../src/native

PROJECT_PATH = ../../../../TotalCross/TotalCrossVM
OUTDIR ?= $(PROJECT_PATH)/builders/gcc-palm/output
TC_SRCDIR = $(PROJECT_PATH)/src

# tools used to build this vm

TCVM_CODE_RESID_DEC = 4096
TCVM_CODE_RESID_HEX = 1000

Litebase_sources = \
	$(LBDIR)/PlainDB.o	\
	$(LBDIR)/TCVMLib.o	\
	$(LBDIR)/Litebase.o	\
	$(LBDIR)/LitebaseGlobals.o	\
	$(LBDIR)/MemoryFile.o	\
	$(LBDIR)/NativeMethods.o	\
	$(LBDIR)/ResultSet.o	\
	$(LBDIR)/Table.o \
	$(LBDIR)/Key.o \
	$(LBDIR)/Node.o \
	$(LBDIR)/Index.o \
	$(LBDIR)/lbFile.o \
	$(LBDIR)/MarkBits.o \
	$(LBDIR)/SQLValue.o \
	$(LBDIR)/NormalFile.o \
	$(LBDIR)/PreparedStatement.o \
	$(LBDIR)/UtilsLB.o

Parser_sources = \
	$(LBDIR)/parser/LitebaseLex.o \
	$(LBDIR)/parser/LitebaseMessage.o \
	$(LBDIR)/parser/LitebaseParser.o \
	$(LBDIR)/parser/SQLBooleanClause.o \
	$(LBDIR)/parser/SQLBooleanClauseTree.o \
	$(LBDIR)/parser/SQLColumnListClause.o \
	$(LBDIR)/parser/SQLDeleteStatement.o \
	$(LBDIR)/parser/SQLInsertStatement.o \
	$(LBDIR)/parser/SQLSelectStatement.o \
	$(LBDIR)/parser/SQLUpdateStatement.o

Tests_sources = \
	$(TC_SRCDIR)/tests/tc_testsuite.o

OBJECT_FILES =         \
	$(Litebase_sources) \
	$(Parser_sources) \
	$(Tests_sources) \

DEFINES = \
	$(EXTRA_DEFINES)    \
	$(COMMON_DEFINES)   \
	-DPALMOS_ALL_SDKS=0 \
	-DPALMOS \
	-DLB_EXPORTS

OTHER_INCS = \
	-I$(EXTLIBS)/PalmOS5RE/Incs \
	-I.                         \
	-I$(TC_SRCDIR)/tcvm         \
	-I$(TC_SRCDIR)/palmposix    \
	-I${LBDIR}                  \
	-I${LBDIR}/parser           \
	-I${TC_SRCDIR}/nm/lang			\
	-I${TC_SRCDIR}/nm/io        \
	-I${TC_SRCDIR}/zlib					\
	-I$(TC_SRCDIR)/util

#	-DENABLE_TEST_SUITE
#	-DVIDEO_BITSPERPIXEL=$(_BITSPERPIXEL) \
#	-DERROR_CHECK_LEVEL=ERROR_CHECK_FULL \
#	-DDEBUG_PEAL \
#	-DENABLE_TRACE \
#	-DDEBUG_TRACES \

CFLAGS = $(BUILD_ASM) $(INTERWORK) -s \
			-fPIC -msoft-float -fshort-enums -fno-reorder-blocks -fno-crossjumping -fno-strict-aliasing \
			-Os -march=armv4t -mstructure-size-boundary=8 -ffixed-r9 -ffixed-r10 -mpic-register=r8 -msingle-pic-base \
			-W -Wall -Wpointer-arith -Wno-unknown-pragmas -Wno-multichar -Wno-format-y2k $(DEBUG) \
			 $(PALMOS_INCS) ${OTHER_INCS} ${DEFINES}

LDFLAGS = -L$(OUTDIR)/palmposix -L$(EXTLIBS)/PalmOS5RE/Libs -lpalmposix -lBoot -lUI -lNET -lHsExt -lm -lgcc -lDAL -Wl,--split-by-file=64000 --stack=16384

OBJECTS = $(addprefix $(OUTDIR)/Litebase/, $(notdir $(OBJECT_FILES)))

RESOURCE_FILES = tver03e8.bin
RESOURCES = $(addprefix $(OUTDIR)/Litebase/, $(RESOURCE_FILES))

ARMFILE = $(OUTDIR)/Litebase/ARMC$(TCVM_CODE_RESID_HEX).bin.elf

ARMFLAGS = $(INTERWORK) -Wl,-M -Wl,-Map=$(OUTDIR)/Litebase/litebase.map -Wl,--script arm-elf-palmos.ld -march=armv4t \
	-Wl,--emit-relocs -nostartfiles ${OTHER_INCS} -nostdlib -fPIC -Wl,--no-warn-mismatch

CC_OPTIONS = -c $(CFLAGS) -I$(PEAL_HOME)/m68k

#########################################################################

all: Init Litebase.prc

Init:
	@-mkdir -p $(OUTDIR)/Litebase
	@echo CC OPTIONS: $(CC_OPTIONS)
	@echo

 .c.o:
	@if test $(basename $@).c -nt $(OUTDIR)/Litebase/$(@F); then \
		echo -- Compiling $(basename $@).c; \
		$(CC) $(CC_OPTIONS) -o $(OUTDIR)/Litebase/$(@F) $(basename $@).c; \
	else :; fi

$(RESOURCES) :	resources.rcp resources.h
	@echo == Compiling resources...
	@$(PILRC) resources.rcp $(OUTDIR)/Litebase

$(ARMFILE): $(OBJECT_FILES)
	@echo
	@echo == Building ELF file
	@$(CC) $(ARMFLAGS) $(OBJECTS) -o $@ $(LDFLAGS)

$(OUTDIR)/Litebase/Litebase.ro: $(ARMFILE)
	@echo == Post linking...
	@$(POSTLINK) -s $(TCVM_CODE_RESID_DEC) -o $@ -t ARMC $^

Litebase.prc: $(OUTDIR)/Litebase/Litebase.ro $(RESOURCES)
	@echo == Building Litebase.prc
	@$(BUILDPRC) --hidden --no-check-resources --output Litebase.prc --name Litebase --creator LBTC $^
	@echo == Done.

clean:
	rm -rf $(OUTDIR)/Litebase Litebase.prc
