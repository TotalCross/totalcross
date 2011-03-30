/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: barcode.h,v 1.6 2011-01-04 13:31:22 guich Exp $

#ifndef BARCODE_H
#define BARCODE_H

#include "tcvm.h"

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
#ifdef __cplusplus
}
#endif


#endif
