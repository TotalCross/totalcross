/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#ifndef TCVM_H
#define TCVM_H

//#define ENABLE_TRACE

#ifdef darwin
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#define __gl2_h_
#endif

#if defined(ANDROID)
#include <jni.h>
#define __gl2_h_
#endif

#if HAVE_CONFIG_H
#include "config.h"
#endif

/** The following ifdef block is the standard way of creating macros which make exporting
 from a DLL simpler. All files within this DLL are compiled with the TESTE_EXPORTS
 symbol defined on the command line. this symbol should not be defined on any project
 that uses this DLL. This way any other project whose source files include this file see
 TESTE_API functions as being imported from a DLL, wheras this DLL sees symbols
 defined with this macro as being exported.
 */
#if defined(WIN32) || defined(WINCE)
 #ifdef TC_EXPORTS
 #define TC_API __declspec(dllexport)
 #else
 #define TC_API __declspec(dllimport)
 #endif
#else
 #define TC_API extern
#endif

#if defined(TC_EXPORTS) || defined(DONT_PREFIX_WITH_TC_FOR_LIBRARIES)
#define TCAPI_FUNC(x) x
#else
#define TCAPI_FUNC(x) TC_##x
#endif

#define NATIVE_METHOD(x) TC_API void x(NMParams p)

#if defined(WINCE) || defined(WIN32)
 #define INCL_WINSOCK_API_PROTOTYPES 0
 #define INCL_WINSOCK_API_TYPEDEFS 1
 #include "winsock2.h"
 #include <windows.h>
 #include <winnt.h>
 #include <Mmsystem.h>
 #if defined (WIN32) && !defined (WINCE) && _MSC_VER < 1500 && !defined __cplusplus
  #include <stdint.h>
 #endif
 #if _WIN32_WCE >= 300
  #include <notify.h>
 #endif
 typedef HWAVEOUT MediaClipHandle;
 typedef WAVEHDR  MediaClipHeader;
#endif

#include <stdarg.h>
#include <stdio.h>
#include <math.h>

#if defined(DISABLE_RAS)
#include "../init/noras_ids/noras.inc"
#endif

#ifdef darwin
#define inline
#endif

#if !(defined(FORCE_LIBC_ALLOC) || defined(ENABLE_WIN32_POINTER_VERIFICATION))
#include "dlmalloc.h"                                                         
#endif
#include "xtypes.h"
#include "../event/specialkeys.h"
#include "mem.h"
#include "datastructures.h"
#include "jchar.h"
#include "tcclass.h"
#include "tcexception.h"
#include "tcmethod.h"
#include "tcfield.h"
#include "tcthread.h"
#include "tcz.h"
#include "opcodes.h"
#include "utils.h"
#include "debug.h"
#include "objectmemorymanager.h"
#include "../tests/tc_testsuite.h"
#include "../nm/instancefields.h"
#include "../nm/ui/GraphicsPrimitives.h"
#include "../nm/ui/PalmFont.h"
#include "context.h"
#include "nativelib.h"
#include "../event/event.h"
#include "../init/settings.h"
#include "../init/startup.h"
#include "errormsg.h"
#include "../init/globals.h"
#include "../init/demo.h"
#ifdef PALMOS
#include "palm_posix.h"
#endif

#ifdef darwin
#undef inline
#endif

/// Executes a Java method. The method must be retrieved with getMethod.
TC_API TValue executeMethod(Context currentContext, Method method, ...);
typedef TValue (*executeMethodFunc)(Context currentContext, Method method, ...);
/// check if the given range is inside the bounds of the array (start < x <= end)
TC_API bool checkArrayRange(Context currentContext, Object obj, int32 start, int32 count);
typedef bool (*checkArrayRangeFunc)(Context currentContext, Object obj, int32 start, int32 count);

#endif
