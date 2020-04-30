// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef BARCODE_H
#define BARCODE_H

#ifdef __cplusplus
extern "C" {
#endif
#include "../tcvm/tcvm.h"
#ifdef __cplusplus
}
#endif


#if defined(WIN32) || defined(WINCE)
 #ifdef SCAN_EXPORTS
 #define SCAN_API __declspec(dllexport)
 #else
 #define SCAN_API __declspec(dllimport)
 #endif
#else
 #define SCAN_API extern
#endif

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WINCE
   SCAN_API int32 LibOpen(OpenParams params);
   SCAN_API void LibClose();
   SCAN_API bool HandleEvent(VoidP eventP);

   SCAN_API void tidsS_scannerActivate(NMParams p);
   SCAN_API void tidsS_setBarcodeParam_ib(NMParams p);
   SCAN_API void tidsS_setParam_iii(NMParams p);
   SCAN_API void tidsS_setBarcodeLength_iiii(NMParams p);
   SCAN_API void tidsS_commitBarcodeParams(NMParams p);
   SCAN_API void tidsS_getData(NMParams p);
   SCAN_API void tidsS_getScanPortDriverVersion(NMParams p);
   SCAN_API void tidsS_getScanManagerVersion(NMParams p);
   SCAN_API void tidsS_deactivate(NMParams p);
#elif defined (ANDROID) || defined (darwin)
   TC_API void tidsS_scannerActivate(NMParams p);
   TC_API void tidsS_setBarcodeParam_ib(NMParams p);
   TC_API void tidsS_setParam_iii(NMParams p);
   TC_API void tidsS_setBarcodeLength_iiii(NMParams p);
   TC_API void tidsS_commitBarcodeParams(NMParams p);
   TC_API void tidsS_getData(NMParams p);
   TC_API void tidsS_getScanPortDriverVersion(NMParams p);
   TC_API void tidsS_getScanManagerVersion(NMParams p);
   TC_API void tidsS_deactivate(NMParams p);
#if defined (ANDROID)
   bool callBoolMethodWithoutParams(CharP name);
#endif
#endif   
#ifdef __cplusplus
}
#endif


#endif
