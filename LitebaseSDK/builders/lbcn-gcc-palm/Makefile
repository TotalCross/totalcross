# path locations

TC_ROOT = ../../..
EXTLIBS = $(TC_ROOT)/../extlibs
PALM_SDK ?= /PalmDev/sdk-5r4
OUTDIR  = /OutDir/conduit

# tools used to build this vm

CC = m68k-palmos-gcc
PEAL_HOME = $(EXTLIBS)/peal
BUILDPRC = build-prc
PILRC = pilrc

PALMOS_INCS = \
	-I$(PALM_SDK)/include \
	-I$(PALM_SDK)/include/Dynamic \
	-I$(PALM_SDK)/include/Core \
	-I$(PALM_SDK)/include/Core/UI \
	-I$(PALM_SDK)/include/Core/System \
	-I$(PALM_SDK)/include/Core/Hardware \
	-I$(PALM_SDK)/include/Libraries \
	-I$(PALM_SDK)/include/Extensions/ExpansionMgr \
	-I$(PALM_SDK)/include/Libraries/PalmOSGlue

TCVM_CODE_RESID_DEC = 4096

M68K_CFLAGS = \
	-DPALMOS

OBJECT_FILES = \
	../../src/native/conduit/LitebaseConduit.o
OBJECTS = $(addprefix $(OUTDIR)/, $(notdir $(OBJECT_FILES)))

#########################################################################

all: Init LitebaseConduit.prc

Init:
	@-mkdir -p $(OUTDIR)

.c.o:
	$(CC) -Os -c $(M68K_CFLAGS) -I. -I$(PEAL_HOME)/m68k $(PALMOS_INCS) -o $(OUTDIR)/$(@F) $(basename $@).c

LitebaseConduit.prc: $(OBJECT_FILES)
	$(CC) -o $(OUTDIR)/LitebaseConduit.bin.o $(OBJECTS) $(PALMOS_GLUE_LIB)
	$(BUILDPRC) --hidden --output LitebaseConduit.prc --name LitebaseConduit --creator LBcn $(OUTDIR)/LitebaseConduit.bin.o

clean:
	rm -rf $(OUTDIR) LitebaseConduit.prc
