# path locations

TC_ROOT = $(LAUNCHER_BASE_DIR)/../../..
EXTLIBS = $(TC_ROOT)/../../../extlibs
PALM_SDK ?= $(EXTLIBS)/sdk-5r4
OUTDIR  = ../output/launcher

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
	-I$(PALM_SDK)/include/Libraries/PalmOSGlue

TCVM_CODE_RESID_DEC = 4096

M68K_CFLAGS = \
	-DTCVM_CODE_RESID=$(TCVM_CODE_RESID_DEC) \
	-DPALMOS \
	-DUSE_PEAL

OBJECT_FILES = \
	$(PEAL_HOME)/m68k/peal.o \
	$(TC_ROOT)/src/launchers/palm5/Launcher.o
OBJECTS = $(addprefix $(OUTDIR)/, $(notdir $(OBJECT_FILES)))

RESOURCE_FILES = tAIN03e8.bin taic03e8.bin tver03e8.bin tAIB03e8.bin tAIB03e9.bin Talt03ec.bin Talt03ed.bin
RESOURCES = $(addprefix $(OUTDIR)/, $(RESOURCE_FILES))

# required by Peal.c:
PALMOS_GLUE_LIB = -L$(PALM_SDK)/lib/m68k-palmos-coff -lPalmOSGlue

#########################################################################

all: Init Launcher.prc

Init:
	@-mkdir -p $(OUTDIR)

$(RESOURCES) :	resources.rcp resources.h
	$(PILRC) resources.rcp $(OUTDIR)

.c.o:
	$(CC) -Os -c $(M68K_CFLAGS) -I. -I$(PEAL_HOME)/m68k $(PALMOS_INCS) -o $(OUTDIR)/$(@F) $(basename $@).c

Launcher.prc: $(OBJECT_FILES) $(RESOURCES)
	$(CC) -o $(OUTDIR)/Launcher.bin.o $(OBJECTS) $(PALMOS_GLUE_LIB)
	$(BUILDPRC) --output Launcher.prc --name Launcher --creator TCAp $(OUTDIR)/Launcher.bin.o $(OUTDIR)/*.bin

clean:
	rm -rf $(OUTDIR) Launcher.prc
