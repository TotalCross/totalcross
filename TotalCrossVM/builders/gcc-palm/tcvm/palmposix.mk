include ../common.mk

OBJECT_FILES =                         \
	$(TC_SRCDIR)/palmposix/palm_posix.o        \
	$(TC_SRCDIR)/palmposix/assert.o            \
	$(TC_SRCDIR)/palmposix/signal.o            \
	$(TC_SRCDIR)/palmposix/stdio.o             \
	$(TC_SRCDIR)/palmposix/error.o             \
	$(TC_SRCDIR)/palmposix/time.o              \
	$(TC_SRCDIR)/palmposix/unistd.o            \
	$(TC_SRCDIR)/palmposix/string_posix.o      \
	$(TC_SRCDIR)/palmposix/stdlib.o

CFLAGS = $(BUILD_ASM) $(INTERWORK) -s \
			-fPIC -msoft-float -fshort-enums -fno-reorder-blocks -fno-crossjumping -fno-strict-aliasing \
			-Os -march=armv4t -mstructure-size-boundary=8 -ffixed-r8 -ffixed-r9 -mpic-register=r10 -msingle-pic-base \
			-W -Wall -Wpointer-arith -Wno-unknown-pragmas -Wno-multichar -Wno-format-y2k $(DEBUG) \
			$(PALMOS_INCS) ${COMMON_DEFINES} -I$(TC_SRCDIR)/zlib \
			-I$(EXTLIBS)/PalmOS5RE/Incs -I. -I$(TC_SRCDIR)/tcvm -I$(TC_SRCDIR)/palmposix -I$(TC_SRCDIR)/util

OBJECTS = $(addprefix $(OUTDIR)/palmposix/, $(notdir $(OBJECT_FILES)))

all:	Init $(OUTDIR)/palmposix/libpalmposix.a

Init:
	@-mkdir -p $(OUTDIR)/palmposix
	@echo CC OPTIONS: $(CFLAGS)
	@echo

$(OUTDIR)/palmposix/libpalmposix.a:	$(OBJECT_FILES)
	@echo
	@echo == Building ELF file
	$(AR) r $@ $(OBJECTS)

.c.o:
	@if test $(basename $@).c -nt $(OUTDIR)/palmposix/$(@F); then \
		echo -- Compiling $(basename $@).c; \
		$(CC) -c $(CFLAGS) -o $(OUTDIR)/palmposix/$(@F) $(basename $@).c; \
	else :; fi
#		echo -- $(basename $@).c is up to date; \

clean:
	rm -rf $(OUTDIR)/palmposix
