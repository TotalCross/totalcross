include ../common.mk

OBJECT_FILES =                         \
	$(TC_SRCDIR)/axtls/aes.o            \
	$(TC_SRCDIR)/axtls/asn1.o           \
	$(TC_SRCDIR)/axtls/bigint.o         \
	$(TC_SRCDIR)/axtls/crypto_misc.o    \
	$(TC_SRCDIR)/axtls/hmac.o           \
	$(TC_SRCDIR)/axtls/os_port.o        \
	$(TC_SRCDIR)/axtls/loader.o         \
	$(TC_SRCDIR)/axtls/md5.o            \
	$(TC_SRCDIR)/axtls/md2.o            \
	$(TC_SRCDIR)/axtls/openssl.o        \
	$(TC_SRCDIR)/axtls/p12.o            \
	$(TC_SRCDIR)/axtls/rsa.o            \
	$(TC_SRCDIR)/axtls/rc4.o            \
	$(TC_SRCDIR)/axtls/sha1.o           \
	$(TC_SRCDIR)/axtls/sha256.o         \
	$(TC_SRCDIR)/axtls/tls1.o           \
	$(TC_SRCDIR)/axtls/tls1_svr.o       \
	$(TC_SRCDIR)/axtls/tls1_clnt.o

CFLAGS = $(BUILD_ASM) $(INTERWORK) -s -DPALMOS \
			-fPIC -msoft-float -fshort-enums -fno-reorder-blocks -fno-crossjumping -fno-strict-aliasing \
			-Os -march=armv4t -mstructure-size-boundary=8 -ffixed-r8 -ffixed-r9 -mpic-register=r10 -msingle-pic-base \
			-W -Wall -Wpointer-arith -Wno-unknown-pragmas -Wno-multichar -Wno-format-y2k $(DEBUG) \
			$(PALMOS_INCS) \
			-I$(EXTLIBS)/PalmOS5RE/Incs -I. -I$(TC_SRCDIR)/tcvm -I$(TC_SRCDIR)/axtls -I$(TC_SRCDIR)/util -I$(TC_SRCDIR)/palmposix \
			-I$(TC_SRCDIR)/zlib ${DEFINES}

OBJECTS = $(addprefix $(OUTDIR)/axtls/, $(notdir $(OBJECT_FILES)))

all:	Init $(OUTDIR)/axtls/libaxtls.a

Init:
	@-mkdir -p $(OUTDIR)/axtls
	@echo CC OPTIONS: $(CFLAGS)
	@echo

$(OUTDIR)/axtls/libaxtls.a:	$(OBJECT_FILES)
	@echo
	@echo == Building ELF file
	$(AR) rvu $@ $(OBJECTS)

.c.o:
	@if test $(basename $@).c -nt $(OUTDIR)/axtls/$(@F); then \
		echo -- Compiling $(basename $@).c; \
		$(CC) -c $(CFLAGS) -o $(OUTDIR)/axtls/$(@F) $(basename $@).c; \
	else :; fi
#		echo $(CC) -c $(CFLAGS) -o $(OUTDIR)/axtls/$(@F) $(basename $@).c; \
#		echo -- $(basename $@).c is up to date; \

clean:
	rm -rf $(OUTDIR)/axtls
