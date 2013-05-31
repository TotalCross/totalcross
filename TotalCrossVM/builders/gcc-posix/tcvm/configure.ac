#
# Process this file with autoconf to produce a configure script.
#
# Copyright 2004 by Superwaba, Inc.
#
# This file is part of the TotalCross package.
#

AC_INIT
AC_CONFIG_SRCDIR([../../../src/tcvm/tcvm.c])
AC_COPYRIGHT([
/*********************************************************************************
 *  TotalCross Virtual Machine                                                   *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  Please see the software license located at /license.txt for more details.    *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, write to                                                             *
 *                                                                               *
 *     SuperWaba Ltda.                                                           *
 *     Rua Padre Leonel Franca, 480, sala 27A                                    *
 *     Instituto Genesis - PUC-RIO                                               *
 *     Rio de Janeiro / RJ - Brazil                                              *
 *     Cep: 22451-000                                                            *
 *     E-mail: licensing@superwaba.com.br                                        *
 *********************************************************************************/
])

SDK_ROOT=../../../
AC_SUBST(SDK_ROOT)

PACKAGE=TotalCross
AC_SUBST(PACKAGE)

#------------------------------------------------------------------------------
# Version information
#------------------------------------------------------------------------------

dnl version numbers
MAJOR_VERSION=${MAJOR_VERSION:-2}
AC_SUBST(MAJOR_VERSION)
MINOR_VERSION=${MINOR_VERSION:-0}
AC_SUBST(MINOR_VERSION)
MICRO_VERSION=${MICRO_VERSION:-0}
AC_SUBST(MICRO_VERSION)

VERSION=$MAJOR_VERSION.$MINOR_VERSION.$MICRO_VERSION
AC_MSG_NOTICE([Configuring TotalCross/VM $VERSION])

AC_CANONICAL_TARGET()

AM_INIT_AUTOMAKE($PACKAGE, $VERSION)
AC_DEFINE_UNQUOTED(PACKAGE, "$PACKAGE")
AC_DEFINE_UNQUOTED(VERSION, "$VERSION")

# Checks for programs.
#AC_CONFIG_HEADERS([config.h])
AC_PROG_CC
AC_PROG_OBJC
AM_PROG_CC_C_O
AM_PROG_LIBTOOL
_AM_DEPENDENCIES([OBJC])

#------------------------------------------------------------------------------
# build targets specifics
#------------------------------------------------------------------------------
AC_MSG_CHECKING(Checking the build target)
case "$target" in
    *-apple-darwin*)
        dnl work around since we don't have Objective-C support in autoconf
        CFLAGS="-Dlinux -Ddarwin ${CFLAGS}"
        DARWIN_ROOTDIR=`dirname \`which $target-gcc\``/..
        if test "x$target" = "xarm-apple-darwin9"; then
           CFLAGS="-Ddarwin9 ${CFLAGS}"
           PLATFORM=darwin9
           DARWIN_ROOTDIR="${DARWIN_ROOTDIR}/.."
           AC_MSG_RESULT(iphoneOS_2x)
        else
           PLATFORM=darwin
           AC_MSG_RESULT(iphoneOS_1x)
        fi
        ;;
    *-linux*)
    	PLATFORM=linux
        AC_MSG_RESULT(linux)
        ;;
    *)
        AC_MSG_ERROR([Unsupported target: Fix the configure.in if it should be])
        ;;
esac
AC_SUBST(PLATFORM)
AC_SUBST(DARWIN_ROOTDIR)

#------------------------------------------------------------------------------
# Enable documentation build.
#------------------------------------------------------------------------------
AC_ARG_ENABLE(doxygen,
	AS_HELP_STRING([--enable-doxygen],[enable documentation generation with doxygen]))
enable_doc=no
if test "x$enable_doxygen" = "xyes"; then
	AC_PATH_PROG(DOXYGEN, doxygen, , $PATH)
	if test "x$DOXYGEN" = "x"; then
		AC_MSG_ERROR([could not find doxygen])
	else
		enable_doc=yes
	fi
fi
AM_CONDITIONAL(DOC, test x$enable_doc = "xyes")

#------------------------------------------------------------------------------
# Enable testsuite.
#------------------------------------------------------------------------------
AC_ARG_ENABLE(testsuite,
	AS_HELP_STRING([--enable-testsuite],[enable testsuite running]))
AM_CONDITIONAL(TESTSUITE, test "x$enable_testsuite" = "xyes")

#------------------------------------------------------------------------------
# Enable demo version.
#------------------------------------------------------------------------------
AC_ARG_ENABLE(demo,
	AS_HELP_STRING([--enable-demo],[enable demo version]))
AM_CONDITIONAL(DEMO_VERSION, test "x$enable_demo" = "xyes")

#------------------------------------------------------------------------------
# Enable noras version.
#------------------------------------------------------------------------------
AC_ARG_ENABLE(noras,
   AS_HELP_STRING([--enable-noras],[enable noras version]))
AM_CONDITIONAL(NORAS_VERSION, test "x$enable_noras" = "xyes")

#------------------------------------------------------------------------------
# Check for pthread.
#------------------------------------------------------------------------------
AC_MSG_CHECKING(for pthread lib)
AC_CHECK_LIB(pthread, pthread_create, [have_pthread="yes"], [have_pthread="no"])
if test "$have_pthread" = "yes"; then
	AC_MSG_RESULT(yes)
    AC_DEFINE([HAVE_PTHREAD],[1],[pthread available on the computer])
       CFLAGS="$CFLAGS -pthread"
       LDFLAGS="$LDFLAGS -lpthread"
else
	AC_MSG_ERROR([could not find the pthread library])
fi

# standard native libs
NATIVE_LIBS=

AC_SUBST(NATIVE_LIBS)

DISTRIB_LIBS=
for l in $NATIVE_LIBS
do
  DISTRIB_LIBS="\${lib_path}/vm/linux/$l $DISTRIB_LIBS"
done
AC_SUBST(DISTRIB_LIBS)

#------------------------------------------------------------------------------
# Release or Debug build.
#------------------------------------------------------------------------------
AC_MSG_CHECKING([whether to enable release build])
AC_ARG_ENABLE(release,
	[AS_HELP_STRING([--enable-release],[compile with release options])],
	USE_RELEASE_FLAGS="${enableval}",
	USE_RELEASE_FLAGS="yes"
)
if test "x$GCC" = "xyes"; then
	CFLAGS=`echo ${CFLAGS} | sed 's/-g//' | sed 's/-O[[^ ]]*//'`;
	if test "x$USE_RELEASE_FLAGS" = "xyes"; then
		CFLAGS="-O3 -fno-strict-aliasing -fomit-frame-pointer ${CFLAGS}"
	else
		CFLAGS="-O0 -g -fno-strict-aliasing -DDEBUG -DENABLE_TRACE ${CFLAGS}"
		# to use the native iPhone gdb, these settings are required: see http://www.246tnt.com/iPhone/
        if test "x$PLATFORM" == "xdarwin9"; then
            CFLAGS="-ggdb -march=armv6 -mcpu=arm1176jzf-s ${CFLAGS}"
		fi
	fi
fi
AC_MSG_RESULT($USE_RELEASE_FLAGS)

#------------------------------------------------------------------------------
# Check for DirectFB on Linux
#------------------------------------------------------------------------------
if test "x$PLATFORM" == "xlinux"; then
   if pkg-config --atleast-version=1.2 direct; then
      PKG_CHECK_MODULES(DIRECTFB, directfb >= 1.2)
   else
      if ! test -d ${HOME}/usr/include/directfb; then
         AC_MSG_ERROR([directfb 1.2 not available, deploy it int ${HOME}/usr])
      fi
      DIRECTFB_CFLAGS="-D_REENTRANT -I${HOME}/usr/include/directfb"
      DIRECTFB_LIBS="-L${HOME}/usr/lib/ -ldirectfb -lfusion -ldirect -lpthread"
      AC_SUBST(DIRECTFB_CFLAGS)
      AC_SUBST(DIRECTFB_LIBS)
   fi
fi

# preset some checks that can't be executed in cross-compiling mode
if test "$cross_compiling" != no; then
   	AC_DEFINE(USE_AP_SNPRINTF, 1, [Define to 1 if snprintf() support is enabled.])
   	ac_cv_func_malloc_0_nonnull=yes
   	ac_cv_func_realloc_0_nonnull=yes
fi

# Checks for header files.
AC_HEADER_DIRENT
AC_HEADER_STDC
AC_CHECK_HEADERS([fcntl.h math.h malloc.h memory.h stdarg.h stddef.h stdlib.h string.h sys/param.h sys/time.h sys/timeb.h sys/statfs.h unistd.h])
AC_CHECK_HEADERS(getopt.h,AC_DEFINE(HAVE_GETOPT_H,1,[getopt.h is needed for getopt_long support]),AC_MSG_WARN(getopt.h was not found.))

# Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST
AC_HEADER_STDBOOL
AC_C_INLINE
AC_TYPE_SIZE_T
AC_HEADER_TIME
AC_STRUCT_TM
AC_HEADER_SYS_WAIT
AC_CHECK_TYPES([ptrdiff_t])

# Checks for library functions.
AC_FUNC_ALLOCA
AC_FUNC_CLOSEDIR_VOID
AC_FUNC_MKTIME
AC_FUNC_MMAP
AC_FUNC_REALLOC
AC_FUNC_STAT
AC_CHECK_FUNCS([statfs getopt_long getenv ftruncate getpagesize gettimeofday localtime_r memset munmap pow])

#------------------------------------------------------------------------------
# Specify the TotalCrossSDK folder
#------------------------------------------------------------------------------
AC_MSG_CHECKING(TotalCrossSDK root folder)
AC_ARG_WITH(sdk-prefix,
	AS_HELP_STRING([--with-sdk-prefix=folder],[Prefix where TotalCrossSDK is installed]),
	[ac_with_sdk_prefix=$withval],
	[ac_with_sdk_prefix=no])
case "$ac_with_sdk_prefix" in
	no)
		AC_MSG_RESULT(no)
		TOTALCROSS_SDK_ROOT=
		;;
   *)
		TOTALCROSS_SDK_ROOT="$ac_with_sdk_prefix"
		AC_DEFINE(TOTALCROSS_SDK_ROOT, $TOTALCROSS_SDK_ROOT, TotalCrossSDK root folder)
		AC_SUBST(TOTALCROSS_SDK_ROOT)
		#AC_CONFIG_LINKS(lib:$TOTALCROSS_SDK_ROOT/lib)
		AC_MSG_RESULT($ac_with_sdk_prefix)
		;;
esac

AM_CONDITIONAL(LINUX,          test ${PLATFORM} == linux)
AM_CONDITIONAL(DARWIN,         test ${PLATFORM} == darwin || test ${PLATFORM} == darwin9)
AM_CONDITIONAL(DARWIN9,        test ${PLATFORM} == darwin9)

#------------------------------------------------------------------------------
# Templates processing.
#------------------------------------------------------------------------------
AC_CONFIG_HEADER(config.h)
#AC_CONFIG_FILES([Makefile totalcross.spec docs/Makefile docs/Doxyfile])
AC_CONFIG_FILES([Makefile])

AC_OUTPUT

AC_MSG_NOTICE([])
AC_MSG_NOTICE([================== You can proceed with: ====================])
AC_MSG_NOTICE([- 'make'              to build the TotalCross VM.])
AC_MSG_NOTICE([- 'make dist'         to build a source tarball.])
AC_MSG_NOTICE([- 'make dist-samples' to build a "source" tarball of the samples.])
AC_MSG_NOTICE([- 'make rpm'          to build redhat source/release RPMs.])
AC_MSG_NOTICE([- 'make rpm2deb'      to convert redhat release RPMs to debian packages (requires alien).])
AC_MSG_NOTICE([- 'make deploy-all'   to build everything.])
AC_MSG_NOTICE([])
AC_MSG_NOTICE([*) All targets except the first require the --with-sdk-prefix option])
AC_MSG_NOTICE([=============================================================])
AC_MSG_NOTICE([])
