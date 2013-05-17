export TARGET=iphone:latest:5.1
GO_EASY_ON_ME = 1

type ?= demo

ARCHS = armv7

libLitebase_FRAMEWORKS = 

libLitebase_CFLAGS = -std=gnu99 -DTHEOS -DPOSIX -D_REENTRANT -DLITEBASE -Wall -Wunused-function -Wno-import -DLB_EXPORTS -fno-strict-aliasing -I${sourcedir} -I${sourcedir}/parser -I${tcvmdir}/util -I${tcvmdir}/tcvm -I${tcvmdir}/nm/io -I${tcvmdir}/nm/lang

ifeq ($(type), demo)
	libLitebase_CFLAGS += -DENABLE_DEMO
endif
THEOS_OBJ_DIR_NAME = $(type)

LITEBASE_SOURCES =                  \
	$(sourcedir)/lbFile.c            \
	$(sourcedir)/PlainDB.c           \
	$(sourcedir)/TCVMLib.c           \
	$(sourcedir)/Litebase.c          \
	$(sourcedir)/ResultSet.c         \
	$(sourcedir)/Table.c             \
	$(sourcedir)/NativeMethods.c     \
	$(sourcedir)/UtilsLB.c           \
	$(sourcedir)/Key.c               \
	$(sourcedir)/Node.c              \
	$(sourcedir)/Index.c             \
	$(sourcedir)/SQLValue.c          \
	$(sourcedir)/MarkBits.c          \
	$(sourcedir)/MemoryFile.c        \
	$(sourcedir)/NormalFile.c        \
	$(sourcedir)/PreparedStatement.c \
	$(sourcedir)/LitebaseGlobals.c

PARSER_SOURCES =                                \
	$(sourcedir)/parser/LitebaseLex.c            \
	$(sourcedir)/parser/LitebaseMessage.c        \
	$(sourcedir)/parser/LitebaseParser.c         \
	$(sourcedir)/parser/SQLBooleanClause.c       \
	$(sourcedir)/parser/SQLBooleanClauseTree.c   \
	$(sourcedir)/parser/SQLColumnListClause.c    \
	$(sourcedir)/parser/SQLDeleteStatement.c     \
	$(sourcedir)/parser/SQLInsertStatement.c     \
	$(sourcedir)/parser/SQLSelectStatement.c     \
	$(sourcedir)/parser/SQLUpdateStatement.c

TESTS_SOURCES =                        \
	$(tcvmdir)/tests/tc_testsuite.c
	
include theos/makefiles/common.mk

LIBRARY_NAME = libLitebase
libLitebase_FILES = ${LITEBASE_SOURCES} ${PARSER_SOURCES} ${TESTS_SOURCES}

include $(THEOS_MAKE_PATH)/library.mk
