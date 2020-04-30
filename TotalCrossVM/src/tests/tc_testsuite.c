// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "../tcvm/tcvm.h"
#ifdef LB_EXPORTS
#include "Litebase.h"
#endif

#ifdef ENABLE_TEST_SUITE

#if HAVE_STDARG_H
#include <stdarg.h>
#endif

#if defined(WIN32)
//#define DISPLAY_RESULT(text)	MessageBox(NULL,text,"Tests finished",MB_OK);
#define DISPLAY_RESULT(text) OutputDebugString(text);
#define OUTPUT(text)          TCAPI_FUNC(debug)(text);
#else //others
#define DISPLAY_RESULT(text)  TCAPI_FUNC(debug)("\nTests finished\n%s\n", text); //getchar();
#define OUTPUT(text)          TCAPI_FUNC(debug)(text);
#endif

#if 0
static char* dump16(char16 *s, int len, int bufIdx)
{
   static char buf1[256];
   static char buf2[256];

   int i,n;
   char *c = (bufIdx==1) ? buf1 : buf2;

   n = (len == -1) ? 255 : len;
   for (i=0; i < n; i++)
   {
      if (len == -1 && !*s)
         break;
      *c++ = (char)*s++;
   }
   *c = 0;
   return (bufIdx==1) ? buf1 : buf2;
}
#endif
static void _long2hex(int64 b, int32 places, CharP outBuf) // placed here to remove dependency of TotalCross for Litebase testcases
{
   CharP b2h = "0123456789ABCDEF";
   outBuf += places;
   *outBuf-- = 0;
   for (;places-- > 0; b>>=4)
      *outBuf-- = b2h[b & 0xF];
}

/** Implementation of the functions that will be used by the test cases */

bool assertEqualsI8(struct TestSuite *tc, int8 v1, int8 v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsI8(%d == %d) failed!\n",file,line,(int)v1,(int)v2);
}
bool assertEqualsI16(struct TestSuite *tc, int16 v1, int16 v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsI16(%d == %d) failed!\n",file,line,(int)v1,(int)v2);
}
bool assertEqualsI32(struct TestSuite *tc, int32 v1, int32 v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsI32(%ld == %ld) failed!\n",file,line,(long)v1,(long)v2);
}
bool assertEqualsI64(struct TestSuite *tc, int64 v1, int64 v2, const char *file, int line)
{
   if (!(v1 == v2))
   {
      LongBuf buf1,buf2;
      fail(tc, "%s (%d): assertEqualsI64(%s == %s) failed!\n",file,line,TCAPI_FUNC(long2str)(v1, buf1),TCAPI_FUNC(long2str)(v2, buf2));
      return false;
   }
   return true;
}
bool assertEqualsU8(struct TestSuite *tc, uint8 v1, uint8 v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsU8(%u == %u) failed!\n",file,line,(unsigned int)v1,(unsigned int)v2);
}
bool assertEqualsU16(struct TestSuite *tc, uint16 v1, uint16 v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsU16(%u == %u) failed!\n",file,line,(unsigned int)v1,(unsigned int)v2);
}
bool assertEqualsU32(struct TestSuite *tc, uint32 v1, uint32 v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsU32(%lu == %lu) failed!\n",file,line,(unsigned long)v1,(unsigned long)v2);
}
bool assertEqualsDbl(struct TestSuite *tc, double v1, double v2, const char *file, int line)
{
   double diff = v1-v2;
   if (diff < 0) diff = -diff;
   if (!(diff <= tc->doubleError))
   {
      char xbuf1[20],xbuf2[20];
      DoubleBuf buf1,buf2;
      _long2hex(*((int64*)&v1), 16, xbuf1); // printing hex values helps find endian issues
      _long2hex(*((int64*)&v2), 16, xbuf2);
      return fail(tc, "%s (%d): assertEqualsDbl(%s == %s) (0x%s == 0x%s) failed!\n",file,line,TCAPI_FUNC(double2str)(v1,-1, buf1),TCAPI_FUNC(double2str)(v2,-1, buf2),xbuf1,xbuf2);
   }
   return true;
}
bool assertEqualsSz(struct TestSuite *tc, char* v1, char* v2, const char *file, int line)
{
   char *b1 = v1;
   char *b2 = v2;
   int32 i=0;

   if (!v1 || !v2)
      return fail(tc, "%s (%d): assertEqualsSz(%x == %x) failed because %s!\n", file, line, (int)v1,(int)v2,(!v1&&!v2)?"both are null":"one is null");
   for (; *b1 && *b2; i++,b1++,b2++)
      if (*b1 != *b2)
         return fail(tc, "%s (%d): assertEqualsSz(%s == %s) failed!\n",file,line,v1,v2);
   if (*b1 || *b2)
      return fail(tc, "%s (%d): assertEqualsSz(%s == %s) failed because the strings have different lengths!\n",file,line,v1,v2);
   return true;
}
bool assertEqualsObj(struct TestSuite *tc, TCObject v1, TCObject v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsObj(%lu == %lu) failed!\n",file,line,(long)v1,(long)v2);
}
bool assertEqualsPtr(struct TestSuite *tc, VoidP v1, VoidP v2, const char *file, int line)
{
   return (v1 == v2) ? true : fail(tc, "%s (%d): assertEqualsPtr(%lu == %lu) failed!\n",file,line,(long)v1,(long)v2);
}
bool assertEqualsBlock(struct TestSuite *tc, void* v1, void* v2, int32 n, const char *file, int line)
{
   uint8 *b1 = (uint8*)v1;
   uint8 *b2 = (uint8*)v2;
   int32 i=0;

   if (!v1 || !v2)
      return fail(tc, "%s (%d): assertEqualsBlock(%x == %x) failed because %s!\n", file, line, (int)v1,(int)v2,(!v1&&!v2)?"both are null":"one is null");
   for (; i < n; i++,b1++,b2++)
      if (*b1 != *b2)
         return fail(tc, "%s (%d): assertEqualsBlock(%x == %x) failed at position %d (%d != %d)!\n",file,line,(int)v1,(int)v2,(int)i,(int)*b1,(int)*b2);
   return true;
}
bool assertEqualsFilled(struct TestSuite *tc, void* v1, uint32 size, uint8 filledWith, const char *file, int line)
{
   uint8 *b1 = (uint8*)v1;
   uint32 i=0;

   if (!v1)
      return fail(tc, "%s (%d): assertEqualsFill(%x,%d,%d) failed because block is null!\n", file, line, (int)v1,(int)size,(int)filledWith);
   for (; i < size; i++,b1++)
      if (*b1 != filledWith)
         return fail(tc, "%s (%d): assertEqualsFill(%x,%d,%d) failed at position %d (%d != %d)!\n",file,line,(int)v1,(int)size,(int)filledWith,(int)i,(int)*b1,(int)filledWith);
   return true;
}
bool assertEqualsNull(struct TestSuite *tc, void* v1, const char *file, int line)
{
   return !v1 ? true : fail(tc, "%s (%d): assertEqualsNull(%d) failed!\n",file,line,(int)v1);
}
bool assertEqualsNotNull(struct TestSuite *tc, void* v1, const char *file, int line)
{
   return v1 ? true : fail(tc, "%s (%d): assertEqualsNotNull(%d) failed!\n",file,line,(int)v1);
}
bool assertEqualsTrue(struct TestSuite *tc, int32 v1, const char *file, int line)
{
   return v1 ? true : fail(tc, "%s (%d): assertEqualsTrue(%d) failed!\n",file,line,v1);
}
bool assertEqualsFalse(struct TestSuite *tc, int32 v1, const char *file, int line)
{
   return !v1 ? true : fail(tc, "%s (%d): assertEqualsFalse(%d) failed!\n",file,line,v1);
}

bool assertBetweenI8(struct TestSuite *tc, int8    v1, int8    v2,  int8    v3, const char *file, int line)
{
   return (v1 <= v2 && v2 <= v3) ? true : fail(tc, "%s (%d): assertBetweenI8(%d <= %d <= %d) failed!\n",file,line,(int)v1,(int)v2,(int)v3);
}
bool assertBetweenI16(struct TestSuite *tc, int16   v1, int16   v2,  int16   v3, const char *file, int line)
{
   return (v1 <= v2 && v2 <= v3) ? true : fail(tc, "%s (%d): assertBetweenI16(%d <= %d <= %d) failed!\n",file,line,(int)v1,(int)v2,(int)v3);
}
bool assertBetweenI32(struct TestSuite *tc, int32   v1, int32   v2,  int32   v3, const char *file, int line)
{
   return (v1 <= v2 && v2 <= v3) ? true : fail(tc, "%s (%d): assertBetweenI32(%ld <= %ld <= %ld) failed!\n",file,line,(long)v1,(long)v2,(long)v3);
}
bool assertBetweenI64(struct TestSuite *tc, int64   v1, int64   v2,  int64   v3, const char *file, int line)
{
   if (!(v1 <= v2 && v2 <= v3))
   {
      LongBuf buf1,buf2,buf3;
      ;
      ;
      ;
      fail(tc, "%s (%d): assertBetweenI64(%s <= %s <= %s) failed!\n",file,line,TCAPI_FUNC(long2str)(v1, buf1),TCAPI_FUNC(long2str)(v2, buf2),TCAPI_FUNC(long2str)(v3, buf3));
      return false;
   }
   return true;
}
bool assertBetweenU8(struct TestSuite *tc, uint8   v1, uint8   v2,  uint8   v3, const char *file, int line)
{
   return (v1 <= v2 && v2 <= v3) ? true : fail(tc, "%s (%d): assertBetweenU8(%d <= %d <= %d) failed!\n",file,line,(int)v1,(int)v2,(int)v3);
}
bool assertBetweenU16(struct TestSuite *tc, uint16  v1, uint16  v2,  uint16  v3, const char *file, int line)
{
   return (v1 <= v2 && v2 <= v3) ? true : fail(tc, "%s (%d): assertBetweenU16(%d <= %d <= %d) failed!\n",file,line,(uint16)v1,(uint16)v2,(uint16)v3);
}
bool assertBetweenU32(struct TestSuite *tc, uint32  v1, uint32  v2,  uint32  v3, const char *file, int line)
{
   return (v1 <= v2 && v2 <= v3) ? true : fail(tc, "%s (%d): assertBetweenU32(%ld <= %ld <= %ld) failed!\n",file,line,(long)v1,(long)v2,(long)v3);
}
bool assertBetweenDbl(struct TestSuite *tc, double  v1, double  v2,  double  v3, const char *file, int line)
{
   if (!(v1 <= v2 && v2 <= v3))
   {
      DoubleBuf buf1,buf2,buf3;
      fail(tc, "%s (%d): assertBetweenDbl(%s <= %s <= %s) failed!\n",file,line,TCAPI_FUNC(double2str)(v1,-1,buf1),TCAPI_FUNC(double2str)(v2,-1,buf2),TCAPI_FUNC(double2str)(v3,-1,buf3));
      return false;
   }
   return true;
}

bool assertAboveI8(struct TestSuite *tc, int8    v1, int8    v2,  const char *file, int line)
{
   return (v1 > v2) ? true : fail(tc, "%s (%d): assertAboveI8(%d > %d) failed!\n",file,line,(int)v1,(int)v2);
}
bool assertAboveI16(struct TestSuite *tc, int16   v1, int16   v2, const char *file, int line)
{
   return (v1 > v2) ? true : fail(tc, "%s (%d): assertAboveI16(%d > %d) failed!\n",file,line,(int)v1,(int)v2);
}
bool assertAboveI32(struct TestSuite *tc, int32   v1, int32   v2, const char *file, int line)
{
   return (v1 > v2) ? true : fail(tc, "%s (%d): assertAboveI32(%ld > %ld) failed!\n",file,line,(long)v1,(long)v2);
}
bool assertAboveI64(struct TestSuite *tc, int64   v1, int64   v2, const char *file, int line)
{
   if (!(v1 > v2))
   {
      LongBuf buf1,buf2;
      fail(tc, "%s (%d): assertAboveI64(%s > %s) failed!\n",file,line,TCAPI_FUNC(long2str)(v1, buf1),TCAPI_FUNC(long2str)(v2, buf2));
      return false;
   }
   return true;
}
bool assertAboveU8(struct TestSuite *tc, uint8   v1, uint8   v2,  const char *file, int line)
{
   return (v1 > v2) ? true : fail(tc, "%s (%d): assertAboveU8(%d > %d) failed!\n",file,line,(int)v1,(int)v2);
}
bool assertAboveU16(struct TestSuite *tc, uint16  v1, uint16  v2, const char *file, int line)
{
   return (v1 > v2) ? true : fail(tc, "%s (%d): assertAboveU16(%d > %d) failed!\n",file,line,(uint16)v1,(uint16)v2);
}
bool assertAboveU32(struct TestSuite *tc, uint32  v1, uint32  v2, const char *file, int line)
{
   return (v1 > v2) ? true : fail(tc, "%s (%d): assertAboveU32(%ld > %ld) failed!\n",file,line,(long)v1,(long)v2);
}
bool assertAboveDbl(struct TestSuite *tc, double  v1, double  v2, const char *file, int line)
{
   if (!(v1 > v2))
   {
      DoubleBuf buf1,buf2;
      fail(tc, "%s (%d): assertAboveDbl(%s > %s) failed!\n",file,line,TCAPI_FUNC(double2str)(v1,-1,buf1),TCAPI_FUNC(double2str)(v2,-1,buf2));
      return false;
   }
   return true;
}

bool fail(struct TestSuite *tc, char *msg, ...)
{
   char buf[512];
   va_list args;
   va_start(args, msg);
   xstrvprintf(buf, msg, args);
   va_end(args);
   OUTPUT(buf);
   tc->failed++;
   return false;
}

void output(struct TestSuite *tc, char *msg, ...)
{
   char buf[512];
   va_list args;
   va_start(args, msg);
   xstrvprintf(buf, msg, args);
   va_end(args);
   OUTPUT(buf);
   tc = tc; // remove warning
}

static struct TestSuite swtc; // making this locally because this way we don't rely on memory allocation

struct TestSuite *createTestSuite()
{
   swtc.assertEqualsI8      = assertEqualsI8;
   swtc.assertEqualsI16     = assertEqualsI16;
   swtc.assertEqualsI32     = assertEqualsI32;
   swtc.assertEqualsI64     = assertEqualsI64;
   swtc.assertEqualsU8      = assertEqualsU8;
   swtc.assertEqualsU16     = assertEqualsU16;
   swtc.assertEqualsU32     = assertEqualsU32;
   swtc.assertEqualsDbl     = assertEqualsDbl;
   swtc.assertEqualsSz      = assertEqualsSz;
   swtc.assertEqualsObj     = assertEqualsObj;
   swtc.assertEqualsPtr     = assertEqualsPtr;
   swtc.assertEqualsBlock   = assertEqualsBlock;
   swtc.assertEqualsFilled  = assertEqualsFilled;
   swtc.assertEqualsNull    = assertEqualsNull;
   swtc.assertEqualsNotNull = assertEqualsNotNull;
   swtc.assertEqualsTrue    = assertEqualsTrue;
   swtc.assertEqualsFalse   = assertEqualsFalse;
   swtc.assertBetweenI8     = assertBetweenI8;
   swtc.assertBetweenI16    = assertBetweenI16;
   swtc.assertBetweenI32    = assertBetweenI32;
   swtc.assertBetweenI64    = assertBetweenI64;
   swtc.assertBetweenU8     = assertBetweenU8;
   swtc.assertBetweenU16    = assertBetweenU16;
   swtc.assertBetweenU32    = assertBetweenU32;
   swtc.assertBetweenDbl    = assertBetweenDbl;
   swtc.assertAboveI8       = assertAboveI8;
   swtc.assertAboveI16      = assertAboveI16;
   swtc.assertAboveI32      = assertAboveI32;
   swtc.assertAboveI64      = assertAboveI64;
   swtc.assertAboveU8       = assertAboveU8;
   swtc.assertAboveU16      = assertAboveU16;
   swtc.assertAboveU32      = assertAboveU32;
   swtc.assertAboveDbl      = assertAboveDbl;
   swtc.fail                = fail;
   swtc.output              = output;
   swtc.doubleError         = 1e-8;
   swtc.failed = swtc.total = 0;
   return &swtc;
}

void showTestResults(int *testResults)
{
   char buf[128];
   TCHAR buf2[128];
   int i;

   if (!swtc.abort)
   {
      xstrprintf(buf,"%ld test total\n%ld tests skipped\n%ld cannot run\n%ld tests completed\n%ld succeeded\n%ld failed\n", (long)swtc.total, (long)swtc.skipped, (long)swtc.cannotRun, (long)(swtc.total + swtc.failed - swtc.skipped - swtc.cannotRun), (long)(swtc.total - swtc.skipped - swtc.cannotRun), (long)swtc.failed);
	  CharP2TCHARPBuf(buf, buf2);
      DISPLAY_RESULT(buf2);

      if (testResults != NULL)
         for (i = 0; i < (long)swtc.total; i++) {
            if (testResults[i]) {
               xstrprintf(buf, "Test %d failed\n", i);
               CharP2TCHARPBuf(buf, buf2);
               DISPLAY_RESULT(buf2);
            }
         }
   }
}

#endif // ENABLE_TEST_SUITE
