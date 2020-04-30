// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCAPI_H
#define TCAPI_H

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

#endif