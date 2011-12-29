# cross project PalmOS/arm shared build settings

EXTLIBS = p:/extlibs
PALM_SDK ?= $(EXTLIBS)/sdk-5r4
PEAL_HOME = $(EXTLIBS)/peal
PROJECT_PATH = $(PALM_BASE_DIR)/../../..
OUTDIR ?= $(PROJECT_PATH)/builders/gcc-palm/output
TC_SRCDIR = $(PROJECT_PATH)/src

# tools used to build this vm

CC = arm-elf-gcc
POSTLINK = $(PEAL_HOME)/postlink/peal-postlink
BUILDPRC = build-prc
PILRC = pilrc

ENDIAN = LITTLE_ENDIAN

PALMOS_INCS = \
	-I$(PALM_SDK)/include \
	-I$(PALM_SDK)/include/Dynamic \
	-I$(PALM_SDK)/include/Core \
	-I$(PALM_SDK)/include/Core/UI \
	-I$(PALM_SDK)/include/Core/System \
	-I$(PALM_SDK)/include/Core/System/Unix \
	-I$(PALM_SDK)/include/Core/Hardware \
	-I$(PALM_SDK)/include/Libraries \
	-I$(PALM_SDK)/include/Libraries/Telephony \
	-I$(PALM_SDK)/include/Libraries/Telephony/UI \
	-I$(PALM_SDK)/include/Libraries/Sms \
	-I$(PALM_SDK)/include/Libraries/Pdi \
	-I$(PALM_SDK)/include/Libraries/PalmOSGlue \
	-I$(PALM_SDK)/include/Libraries/Lz77 \
	-I$(PALM_SDK)/include/Libraries/INet \
	-I$(PALM_SDK)/include/Libraries/exglocal \
	-I$(PALM_SDK)/include/Extensions \
	-I$(PALM_SDK)/include/Extensions/ExpansionMgr

COMMON_DEFINES = \
	-DHAVE_STDARG_H=1 \
	-DHAVE_GETOPT_LONG=0 \
	-DHAVE_GETOPT_H=0 \
	-DHAVE_STDLIB_H=1 \
	-DHAVE_STDIO_H=1 \
	-DHAVE_STRING_H=1 \
	-DHAVE_WAITPID=0 \
	-DHAVE_MATH_H=1 \
	-DPALMOS \
	-D$(ENDIAN)
