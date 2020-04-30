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

#include "..\barcode.h"

#undef malloc
#undef free

#define UNEXPORTED_METHOD
#include "bbappapi.h"

extern HMODULE apidll;
extern Context currentContext;

typedef HBBPRINTER (__stdcall *ProcBBPrinterOpen)(DWORD dwReserved);
typedef DWORD      (__stdcall *ProcBBPrinterInit)(HBBPRINTER hPrinter);
typedef DWORD      (__stdcall *ProcBBPrinterSetDensity)(HBBPRINTER hPrinter, UCHAR ucDensity);
typedef DWORD      (__stdcall *ProcBBPrinterGetDensity)(HBBPRINTER hPrinter, UCHAR *pucDensity);
typedef DWORD      (__stdcall *ProcBBPrinterSetLineSpacing)(HBBPRINTER hPrinter, UCHAR ucLineSpacing);
typedef DWORD      (__stdcall *ProcBBPrinterGetLineSpacing)(HBBPRINTER hPrinter, UCHAR *pucLineSpacing);
typedef DWORD      (__stdcall *ProcBBPrinterPrint)(HBBPRINTER hPrinter, LPCTSTR lpcszData, DWORD dwOptions);
typedef DWORD      (__stdcall *ProcBBPrinterPrintBarcode)(HBBPRINTER hPrinter, LPCTSTR lpcszData, UCHAR ucHeight, UCHAR ucWidth, UCHAR ucBarcodeSystem, UCHAR ucAlign);
typedef DWORD      (__stdcall *ProcBBPrinterPrintBitmap)(HBBPRINTER hPrinter, LPCTSTR lpcszFilename, DWORD dwMode);
typedef DWORD      (__stdcall *ProcBBPrinterWaitUntilPrintEnd)(HBBPRINTER hPrinter);
typedef DWORD      (__stdcall *ProcBBPrinterClose)(HBBPRINTER hPrinter);

ProcBBPrinterOpen              _BBPrinterOpen             ;
ProcBBPrinterInit              _BBPrinterInit             ;
ProcBBPrinterClose             _BBPrinterClose            ;
ProcBBPrinterSetDensity        _BBPrinterSetDensity       ;
ProcBBPrinterGetDensity        _BBPrinterGetDensity       ;
ProcBBPrinterSetLineSpacing    _BBPrinterSetLineSpacing   ;
ProcBBPrinterGetLineSpacing    _BBPrinterGetLineSpacing   ;
ProcBBPrinterPrint             _BBPrinterPrint            ;
ProcBBPrinterPrintBarcode      _BBPrinterPrintBarcode     ;
ProcBBPrinterPrintBitmap       _BBPrinterPrintBitmap      ;
ProcBBPrinterWaitUntilPrintEnd _BBPrinterWaitUntilPrintEnd;

static HBBPRINTER handle;

static bool check(Context currentContext, int32 ret);

static throwExceptionNamedFunc TC_throwExceptionNamed;

bool PrinterLibOpen(OpenParams params)
{
   TC_throwExceptionNamed = (throwExceptionNamedFunc)params->getProcAddress(null, "throwExceptionNamed");
   _BBPrinterOpen              = (ProcBBPrinterOpen             ) GetProcAddress(apidll, TEXT("BBPrinterOpen"));
   _BBPrinterInit              = (ProcBBPrinterInit             ) GetProcAddress(apidll, TEXT("BBPrinterInit"));
   _BBPrinterClose             = (ProcBBPrinterClose            ) GetProcAddress(apidll, TEXT("BBPrinterClose"));
   _BBPrinterSetDensity        = (ProcBBPrinterSetDensity       ) GetProcAddress(apidll, TEXT("BBPrinterSetDensity"));
   _BBPrinterGetDensity        = (ProcBBPrinterGetDensity       ) GetProcAddress(apidll, TEXT("BBPrinterGetDensity"));
   _BBPrinterSetLineSpacing    = (ProcBBPrinterSetLineSpacing   ) GetProcAddress(apidll, TEXT("BBPrinterSetLineSpacing"));
   _BBPrinterGetLineSpacing    = (ProcBBPrinterGetLineSpacing   ) GetProcAddress(apidll, TEXT("BBPrinterGetLineSpacing"));
   _BBPrinterPrint             = (ProcBBPrinterPrint            ) GetProcAddress(apidll, TEXT("BBPrinterPrint"));
   _BBPrinterPrintBarcode      = (ProcBBPrinterPrintBarcode     ) GetProcAddress(apidll, TEXT("BBPrinterPrintBarcode"));
   _BBPrinterPrintBitmap       = (ProcBBPrinterPrintBitmap      ) GetProcAddress(apidll, TEXT("BBPrinterPrintBitmap"));
   _BBPrinterWaitUntilPrintEnd = (ProcBBPrinterWaitUntilPrintEnd) GetProcAddress(apidll, TEXT("BBPrinterWaitUntilPrintEnd"));

   return _BBPrinterOpen && _BBPrinterInit && _BBPrinterClose && 
         _BBPrinterSetDensity && _BBPrinterGetDensity && _BBPrinterSetLineSpacing &&
         _BBPrinterGetLineSpacing && _BBPrinterPrint && _BBPrinterPrintBarcode && _BBPrinterPrintBitmap 
         && _BBPrinterWaitUntilPrintEnd;
}

static bool openPrinter()
{
   bool ok = true;
   if (handle == null)
   {
      handle = _BBPrinterOpen(0);
      if (handle == 0)
         ok = check(currentContext,4);
      else
         ok = check(currentContext,_BBPrinterInit(handle));
   }
   return ok;
}

static void closePrinter()
{
   if (handle != 0)
   {
      _BBPrinterClose(handle);
      handle = 0;
   }
}

void PrinterLibClose()
{
   closePrinter();
}

static TCHAR* getString(TCHAR* buf, TCObject str)
{
   JCharP c;
   int32 len;
   TCHAR* s = buf;
   if (!str)
      return NULL;
   c = String_charsStart(str);
   len = String_charsLen(str);
   for (; --len >= 0; c++)
      *s++ = *c == '/' ? '\\' : *c;
   *s = 0;
   return buf;
}

static bool check(Context currentContext, int32 ret)
{
   char* msg = 0;
   switch (ret)
   {
      case 0: return true;
      case 1: msg = "No paper"; break;
      case 2: msg = "No black mark"; break;
      case 3: msg = "Overheat"; break;
      case 4: msg = "Unkown error"; break;
      case 5: msg = "Invalid handle"; break;
      case 6: msg = "No font"; break;
      case 7: msg = "Error reading file"; break;
      case 8: msg = "No bitmap file"; break;
      case 9: msg = "Invalid bitmap file"; break;
      case 10: msg = "setDensity error"; break;
      case 11: msg = "setDensity out of range"; break;
      case 12: msg = "getDensity error"; break;
      case 13: msg = "Low temperature"; break;
      case 14: msg = "Low voltage"; break;
      case 15: msg = "High voltage"; break;
      case 16: msg = "setLineSpacing error"; break;
      case 17: msg = "getLineSpacing error"; break;
   }
   TC_throwExceptionNamed(currentContext, "pidion.PrinterException", msg);
   return false;
}

#ifdef __cplusplus
extern "C" {
#endif
//////////////////////////////////////////////////////////////////////////
SCAN_API void pP_setDensity_i(NMParams p) // pidion/Printer native public static void setDensity(int value) throws PrinterException;
{
   if (!openPrinter()) return;
   check(p->currentContext,_BBPrinterSetDensity(handle, p->i32[0]));
   _BBPrinterInit(handle);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pP_getDensity(NMParams p) // pidion/Printer native public static int getDensity() throws PrinterException;
{
   UCHAR u;
   if (!openPrinter()) return;
	if (check(p->currentContext,_BBPrinterGetDensity(handle, &u)))
      p->retI = u;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pP_setLineSpacing_i(NMParams p) // pidion/Printer native public static void setLineSpacing(int value) throws PrinterException;
{
   if (!openPrinter()) return;
   check(p->currentContext,_BBPrinterSetLineSpacing(handle, p->i32[0]));
   _BBPrinterInit(handle);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pP_getLineSpacing(NMParams p) // pidion/Printer native public static int getLineSpacing() throws PrinterException;
{
   UCHAR u;
   if (!openPrinter()) return;
	if (check(p->currentContext,_BBPrinterGetLineSpacing(handle, &u)))
      p->retI = u;
}
//////////////////////////////////////////////////////////////////////////
TCHAR buff[1024];

SCAN_API void pP_print_si(NMParams p) // pidion/Printer native public static void print(String text, int options) throws PrinterException;
{
   TCObject text = p->obj[0];
   JCharP start = String_charsStart(text);
   int32 len = String_charsLen(text);
   TCHARP temp = buff;

   if (len > 1023)
   {
      temp = (TCHAR*)malloc(sizeof(TCHAR)*(len+1));
      if (!temp)
         return;
   }

   xmemmove(temp, start, len*2);
   temp[len] = 0;

   if (!openPrinter()) return;
   check(p->currentContext,_BBPrinterPrint(handle, temp, p->i32[0]));

   if (len > 1023)
      free(temp);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pP_printBarcode_siiii(NMParams p) // pidion/Printer native public static void printBarcode(String data, int width, int height, int type, int align) throws PrinterException;
{
   TCObject text = p->obj[0];
   JCharP start = String_charsStart(text);
   int32 len = String_charsLen(text);
   TCHARP temp = buff;

   if (len > 1023)
   {
      temp = (TCHAR*)malloc(sizeof(TCHAR)*(len+1));
      if (!temp)
         return;
   }

   xmemmove(temp, start, len*2);
   temp[len] = 0;

   if (!openPrinter()) return;
   check(p->currentContext,_BBPrinterPrintBarcode(handle, temp, p->i32[1], p->i32[0], p->i32[2], p->i32[3])); // width and height are inverted in the api call

   if (len > 1023)
      free(temp);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pP_printBitmap_si(NMParams p) // pidion/Printer native public static void printBitmap(String filename, int mode) throws PrinterException;
{
   if (!openPrinter()) return;
   check(p->currentContext,_BBPrinterPrintBitmap(handle, getString(buff, p->obj[0]), p->i32[0]));
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void pP_flush(NMParams p) // pidion/Printer native public static void flush() throws PrinterException;
{
   if (!openPrinter()) return;
	check(p->currentContext,_BBPrinterWaitUntilPrintEnd(handle));
}
#ifdef __cplusplus
}
#endif
