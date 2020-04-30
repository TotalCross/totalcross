// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "barcode.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{  
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}
