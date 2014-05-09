###############################################################################
# Makefile for the Superwaba VM.
# @configure_input@
# $Id: Makefile.am,v 1.11.4.1.2.1.4.30 2011-01-26 19:44:55 juliana Exp $
###############################################################################

sourcedir	= ${srcdir}/../../src/native
tcvmdir     = ${srcdir}/../../../../TotalCross/TotalCrossVM/src

AM_CFLAGS = -DTHEOS -DPOSIX -D_REENTRANT -DLITEBASE -Wall -Wunused-function -Wno-import -DLB_EXPORTS -fno-strict-aliasing -I${sourcedir} -I${sourcedir}/parser -I${tcvmdir}/util -I${tcvmdir}/tcvm -I${tcvmdir}/nm/io -I${tcvmdir}/nm/lang

# uncomment for a debug build
# AM_CFLAGS += -I/opt/include/valgrind -g -O0 -DENABLE_TRACE

#if LINUX
AM_CFLAGS += -I/usr/include/_directfb 
#endif

# enable through "--enable-demo" during "configure"
if DEMO_VERSION
AM_CFLAGS += -DENABLE_DEMO
endif

if TESTSUITE
# enable through "--enable-testsuite" during "configure"
AM_CFLAGS += -DENABLE_TEST_SUITE
endif

inst_LTLIBRARIES = libLitebase.la
instdir = /opt/totalcross

Litebase_sources = \
	$(sourcedir)/lbFile.c \
	$(sourcedir)/PlainDB.c \
	$(sourcedir)/TCVMLib.c \
	$(sourcedir)/Litebase.c \
	$(sourcedir)/ResultSet.c \
	$(sourcedir)/Table.c \
	$(sourcedir)/NativeMethods.c \
	$(sourcedir)/UtilsLB.c \
	$(sourcedir)/Key.c \
	$(sourcedir)/Node.c \
	$(sourcedir)/Index.c \
	$(sourcedir)/SQLValue.c \
	$(sourcedir)/MarkBits.c \
	$(sourcedir)/MemoryFile.c \
	$(sourcedir)/NormalFile.c \
	$(sourcedir)/PreparedStatement.c \
	$(sourcedir)/LitebaseGlobals.c

Parser_sources = \
	$(sourcedir)/parser/LitebaseLex.c \
	$(sourcedir)/parser/LitebaseMessage.c \
	$(sourcedir)/parser/LitebaseParser.c \
	$(sourcedir)/parser/SQLBooleanClause.c \
	$(sourcedir)/parser/SQLBooleanClauseTree.c \
	$(sourcedir)/parser/SQLColumnListClause.c \
	$(sourcedir)/parser/SQLDeleteStatement.c \
	$(sourcedir)/parser/SQLInsertStatement.c \
	$(sourcedir)/parser/SQLSelectStatement.c \
	$(sourcedir)/parser/SQLUpdateStatement.c

Tests_sources = \
	$(tcvmdir)/tests/tc_testsuite.c

libLitebase_la_SOURCES = ${Litebase_sources} ${Parser_sources} ${Tests_sources}

libLitebase_la_LIBADD = ${AM_LDFLAGS} -lm

if LINUX
OBJCEXT=c
endif

if DARWIN
AM_OBJCFLAGS = ${AM_CFLAGS} -I${sourcedir}/util -I${sourcedir}/event -I${sourcedir}/nm/ui -I${sourcedir}/init  -I${sourcedir}/tcvm
OBJCEXT=m
endif

if DARWIN9
codesign: all
	arm-apple-darwin9-strip -S `find .libs/ -type f -name "libLitebase*dylib" -print`
	CODESIGN_ALLOCATE=${DARWIN_ROOTDIR}/pre/bin/arm-apple-darwin9-codesign_allocate \
	    ${TOTALCROSS_SDK_ROOT}/etc/tools/ldid/ldid.bin -S `find .libs/ -type f -name "libLitebase*dylib" -print`
endif
