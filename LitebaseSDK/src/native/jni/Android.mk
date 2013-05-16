LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

TYPE ?= release

ifeq ($(TYPE), noras)
	include $(LOCAL_PATH)/options_noras.mk
endif
ifeq ($(TYPE), release)
	include $(LOCAL_PATH)/options_nodemo.mk
endif
ifeq ($(TYPE), demo)
	include $(LOCAL_PATH)/options_demo.mk
endif

LB_SRCDIR := ..
LB_INCLUDEDIR := $(LB_SRCDIR)/native
TC_SRCDIR := $(LB_SRCDIR)/../../../../TotalCross/TotalCrossVM/src
TC_INCLUDEDIR := $(LB_SRCDIR)/../../../TotalCross/TotalCrossVM/src

LITEBASE_FILES = \
	$(LB_SRCDIR)/lbFile.c	\
	$(LB_SRCDIR)/PlainDB.c	\
	$(LB_SRCDIR)/TCVMLib.c	\
	$(LB_SRCDIR)/Litebase.c	\
	$(LB_SRCDIR)/ResultSet.c	\
	$(LB_SRCDIR)/NativeMethods.c	\
	$(LB_SRCDIR)/Table.c \
	$(LB_SRCDIR)/LitebaseGlobals.c \
	$(LB_SRCDIR)/Key.c \
	$(LB_SRCDIR)/Node.c \
	$(LB_SRCDIR)/Index.c \
	$(LB_SRCDIR)/SQLValue.c \
	$(LB_SRCDIR)/MarkBits.c \
	$(LB_SRCDIR)/MemoryFile.c \
	$(LB_SRCDIR)/NormalFile.c \
	$(LB_SRCDIR)/PreparedStatement.c \
	$(LB_SRCDIR)/UtilsLB.c

PARSER_FILES = \
	$(LB_SRCDIR)/parser/LitebaseLex.c \
	$(LB_SRCDIR)/parser/LitebaseMessage.c \
	$(LB_SRCDIR)/parser/LitebaseParser.c \
	$(LB_SRCDIR)/parser/SQLBooleanClause.c \
	$(LB_SRCDIR)/parser/SQLBooleanClauseTree.c \
	$(LB_SRCDIR)/parser/SQLColumnListClause.c \
	$(LB_SRCDIR)/parser/SQLDeleteStatement.c \
	$(LB_SRCDIR)/parser/SQLInsertStatement.c \
	$(LB_SRCDIR)/parser/SQLSelectStatement.c \
	$(LB_SRCDIR)/parser/SQLUpdateStatement.c

_TEST_SUITE ?= DISABLE

ifeq ($(_TEST_SUITE),ENABLE)
TEST_SUITE_FILES = $(TC_SRCDIR)/tests/tc_testsuite.c
endif

SOURCE_FILES = \
	$(LITEBASE_FILES) \
	$(PARSER_FILES) \
	$(TEST_SUITE_FILES)


LOCAL_ARM_MODE   := arm
LOCAL_MODULE     := litebase
LOCAL_SRC_FILES  := $(SOURCE_FILES)
LOCAL_C_INCLUDES := $(TC_INCLUDEDIR)/tcvm $(TC_INCLUDEDIR)/util $(TC_INCLUDEDIR)/nm/io $(TC_INCLUDEDIR)/nm/lang $(LB_INCLUDEDIR)/parser $(LB_INCLUDEDIR)
LOCAL_LDLIBS     := -llog -ldl
LOCAL_CFLAGS     := -DTOTALCROSS -DLB_EXPORTS -DFORCE_LIBC_ALLOC $(EXTRA_DEFINES)
LOCAL_LDFLAGS    := -Wl,-Map,$(NDK_APP_DST_DIR)/$(LOCAL_MODULE).map

include $(BUILD_SHARED_LIBRARY)
