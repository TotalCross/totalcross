// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TC_API void tsT_update(NMParams p);

TESTCASE(tsV_arrayCopy_oioii) // totalcross/sys/Vm native public static boolean arrayCopy(Object srcArray, int srcStart, Object dstArray, int dstStart, int length);
{
   TNMParams p;
   TCObject objArray[2];
   int32 i32[3];
   int8 *pBytes, *pBytes2;
   TCObject *pStrings, *pStrings2;

   tzero(p);
   p.currentContext = currentContext;
   // length is less than 0
   p.obj = objArray;
   objArray[0] = createByteArray(currentContext, 5);
   setObjectLock(objArray[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[0]);
   objArray[1] = createByteArray(currentContext, 5);
   setObjectLock(objArray[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[1]);
   p.i32 = i32;
   i32[0] = 0;
   i32[1] = 0;
   i32[2] = -1;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // the length is too big for dstArray
   objArray[0] = createByteArray(currentContext, 15);
   setObjectLock(objArray[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[0]);
   objArray[1] = createByteArray(currentContext, 5);
   setObjectLock(objArray[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[1]);
   i32[0] = 0;
   i32[1] = 0;
   i32[2] = 100;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // srcStart is greater than the srcArray's length
   i32[0] = 15;
   i32[1] = 0;
   i32[2] = 2;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // dstStart is greater than the dstArray's length
   i32[0] = 0;
   i32[1] = 6;
   i32[2] = 2;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // srcArray is null
   p.obj = objArray;
   objArray[0] = NULL;
   objArray[1] = createByteArray(currentContext, 5);
   setObjectLock(objArray[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[1]);
   i32[0] = 0;
   i32[1] = 0;
   i32[2] = 3;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // dstArray is null
   p.obj = objArray;
   objArray[0] = createByteArray(currentContext, 15);
   setObjectLock(objArray[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[0]);
   objArray[1] = NULL;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // the 2 objects are not compatible
   objArray[0] = createStringArray(currentContext, 5);
   setObjectLock(objArray[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[0]);
   objArray[1] = createByteArray(currentContext, 5);
   setObjectLock(objArray[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[1]);
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // success
   objArray[0] = createByteArray(currentContext, 5);
   setObjectLock(objArray[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[0]);
   pBytes = ARRAYOBJ_START(objArray[0]);
   pBytes[0] = '1';
   pBytes[1] = '2';
   pBytes[2] = '3';
   pBytes[3] = '4';
   pBytes[4] = 0;
   //alert("Array1 %s", pBytes);

   objArray[1] = createByteArray(currentContext, 5);
   setObjectLock(objArray[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[1]);
   pBytes2 = ARRAYOBJ_START(objArray[1]);
   pBytes2[0] = 'a';
   pBytes2[1] = 'b';
   pBytes2[2] = 'c';
   pBytes2[3] = 'd';
   pBytes2[4] = 0;
   //alert("Array2 %s", pBytes2);

   i32[0] = 1;
   i32[1] = 1;
   i32[2] = 2;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI);
   //alert("ArrayCopy %s", pBytes2);

   // Tests with array of string
   objArray[0] = createStringArray(currentContext, 15);
   setObjectLock(objArray[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[0]);
   pStrings = (TCObjectArray)ARRAYOBJ_START(objArray[0]);
   pStrings[0] = createStringObjectFromCharP(currentContext, "1", -1);
   setObjectLock(pStrings[0], UNLOCKED);
   pStrings[1] = createStringObjectFromCharP(currentContext, "2", -1);
   setObjectLock(pStrings[1], UNLOCKED);
   pStrings[2] = createStringObjectFromCharP(currentContext, "3", -1);
   setObjectLock(pStrings[2], UNLOCKED);
   pStrings[3] = createStringObjectFromCharP(currentContext, "4", -1);
   setObjectLock(pStrings[3], UNLOCKED);
   pStrings[4] = 0;

   objArray[1] = createStringArray(currentContext, 5);
   setObjectLock(objArray[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objArray[1]);
   pStrings2 = (TCObjectArray)ARRAYOBJ_START(objArray[1]);
   pStrings2[0] = createStringObjectFromCharP(currentContext, "a", -1);
   setObjectLock(pStrings2[0], UNLOCKED);
   pStrings2[1] = createStringObjectFromCharP(currentContext, "b", -1);
   setObjectLock(pStrings2[1], UNLOCKED);
   pStrings2[2] = createStringObjectFromCharP(currentContext, "c", -1);
   setObjectLock(pStrings2[2], UNLOCKED);
   pStrings2[3] = createStringObjectFromCharP(currentContext, "d", -1);
   setObjectLock(pStrings2[3], UNLOCKED);
   pStrings2[4] = 0;

   i32[0] = 1;
   i32[1] = 1;
   i32[2] = 2;
   tsV_arrayCopy_oioii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   {
      CharP result[] = { "a", "2", "3", "d" };
      int i;
	   for (i = 0; i < 4; i++)
   	{
      	char *sz = JCharP2CharP(String_charsStart(pStrings2[i]), String_charsLen(pStrings2[i]));
	      ASSERT1_EQUALS(True, xstrcmp(sz, result[i]) == 0);
	      xfree(sz);
	   }
   }

   ASSERT1_EQUALS(True, p.retI);
   finish:
      currentContext->thrownException = null;
}
TESTCASE(tsV_getTimeStamp) // totalcross/sys/Vm native public static int getTimeStamp();
{
   TNMParams p;

   p.currentContext = currentContext;
   tsV_getTimeStamp(&p);
   ASSERT1_EQUALS(True, p.retI > 0);

   finish: ;
}
TESTCASE(tsV_setTime_t) // totalcross/sys/Vm native public static void setTime(totalcross.sys.Time t);
{
#if defined (WIN32) && !defined (WP8)
   TNMParams p;
   TCObject currentTime;
   TCObject testTime;
   TCObject checkTime;

   currentTime = createObject(currentContext, "totalcross.sys.Time");
   setObjectLock(currentTime, UNLOCKED);
   ASSERT1_EQUALS(NotNull, currentTime);
   checkTime = createObject(currentContext, "totalcross.sys.Time");
   setObjectLock(checkTime, UNLOCKED);
   ASSERT1_EQUALS(NotNull, checkTime);
   testTime = createObject(currentContext, "totalcross.sys.Time");
   setObjectLock(testTime, UNLOCKED);
   ASSERT1_EQUALS(NotNull, testTime);

   p.currentContext = currentContext;
   p.obj = &currentTime;
   tsT_update(&p); // Stores current time.

   Time_year(testTime)     = 2000;
   Time_month(testTime)    = 1;
   Time_day(testTime)      = 1;
   Time_hour(testTime)     = 6;
   Time_minute(testTime)   = 15;
   Time_second(testTime)   = 15;
   Time_millis(testTime)   = 0;

   // Sets the device time and checks if the operation was successfull.
   p.obj = &testTime;
   tsV_setTime_t(&p);
   p.obj = &checkTime;
   tsT_update(&p);
   ASSERT2_EQUALS(I32, Time_year(testTime)   , Time_year(checkTime));
   ASSERT2_EQUALS(I32, Time_month(testTime)  , Time_month(checkTime));
   ASSERT2_EQUALS(I32, Time_day(testTime)    , Time_day(checkTime));
   ASSERT2_EQUALS(I32, Time_hour(testTime)   , Time_hour(checkTime));
   ASSERT2_EQUALS(I32, Time_minute(testTime) , Time_minute(checkTime));
   ASSERT_BETWEEN(I32, Time_second(testTime) , Time_second(checkTime)   , Time_second(testTime)+5);

   finish:
      p.obj = &currentTime;
      tsV_setTime_t(&p);

#else
   finish: ;
#endif
}

TESTCASE(tsV_exitAndReboot) // totalcross/sys/Vm native public static void exitAndReboot();
{
   TNMParams p;
   TEST_CANNOT_RUN;

   p.currentContext = currentContext;
   tsV_exitAndReboot(&p);
   finish: ;
}
TESTCASE(tsV_exec_ssib) // totalcross/sys/Vm native public static int exec(String command, String args, int launchCode, boolean wait);
{

#if 1
   TEST_CANNOT_RUN;
   // Vm.exec testcase cannot be created because there are no ways to sublaunch a process in most devices

#elif defined(WIN32)
   TNMParams p;
   TCObject obj[2];
   int32 i32buf[2];

   p.currentContext = currentContext;
   p.obj = obj;
   p.i32 = i32buf;

#if defined(WINCE)
   p.obj[0] = createStringObjectFromCharP("/totalcross/args1.exe", -1);
   p.obj[1] = createStringObjectFromCharP("wait = true", -1);
#else
   p.obj[0] = createStringObjectFromCharP("C:/Arquivos de programas/Internet Explorer/iexplore.exe", -1);
   p.obj[1] = createStringObjectFromCharP("-nohome www.google.com", -1);
#endif
   p.i32[0] = 0;
   p.i32[1] = (int32)true;
   tsV_exec_ssib(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

#if defined(WINCE)
   p.obj[0] = createStringObjectFromCharP("iexplore.exe", -1);
   p.obj[1] = createStringObjectFromCharP("www.superwaba.com", -1);
#else
   p.obj[0] = createStringObjectFromCharP("C:/Arquivos de programas/Internet Explorer/iexplore.exe", -1);
   p.obj[1] = createStringObjectFromCharP("-nohome www.superwaba.com", -1);
#endif
   p.i32[0] = 0;
   p.i32[1] = (int32)false;
   tsV_exec_ssib(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

#if defined(WINCE)
   p.obj[0] = createStringObjectFromCharP("\\totalcross\\args1", -1);
   p.obj[1] = createStringObjectFromCharP("param1 param2", -1);
   tsV_exec_ssib(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   p.obj[0] = createStringObjectFromCharP("args1", -1);
   p.obj[1] = createStringObjectFromCharP("param1 param2", -1);
   tsV_exec_ssib(&p);
   ASSERT1_EQUALS(True, (bool)p.retI);

   p.obj[0] = createStringObjectFromCharP("args2.exe", -1);
   p.obj[1] = createStringObjectFromCharP("param1 param2", -1);
   tsV_exec_ssib(&p);
   ASSERT1_EQUALS(True, (bool)p.retI);

   p.obj[0] = createStringObjectFromCharP("/superwaba/args2", -1);
   p.obj[1] = createStringObjectFromCharP("", -1);
   tsV_exec_ssib(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   p.obj[0] = createStringObjectFromCharP("/superwaba/args2", -1);
   p.obj[1] = null;
   tsV_exec_ssib(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);
#endif
#endif

   finish: ;
}
TESTCASE(tsV_setAutoOff_b) // totalcross/sys/Vm native public static void setAutoOff(boolean enable);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tsV_sleep_i) // totalcross/sys/Vm native public static void sleep(int millis);
{
   TNMParams p;
   int32 i32buf[1];
   int32 start,end;

   tzero(p);
   p.currentContext = currentContext;
   p.i32 = i32buf;

   // Get the time stamp
   tsV_getTimeStamp(&p);
   start = p.retI;
   // sleep for 120 ms
   p.i32[0] = 120;
   tsV_sleep_i(&p);
   // Get the new time stamp
   tsV_getTimeStamp(&p);
   end = p.retI;
   ASSERT_ABOVE(I32, end-start+20, 120);
   // sleep for more 500 ms
   start = end;
   p.i32[0] = 500;
   tsV_sleep_i(&p);
   // Get the new time stamp
   tsV_getTimeStamp(&p);
   end = p.retI;
   ASSERT_ABOVE(I32, end-start+1, 500);

   finish: ;
}
TESTCASE(tsV_getFreeMemory) // totalcross/sys/Vm native public static int getFreeMemory();
{
   TNMParams p;

   tzero(p);
   p.currentContext = currentContext;
   tsV_getFreeMemory(&p);
   ASSERT_BETWEEN(I32, 1, p.retI, 2147483647);

   finish: ;
}
TESTCASE(tsV_interceptSpecialKeys_I) // totalcross/sys/Vm native public static void interceptSpecialKeys(int []keys);
{
//   TNMParams p;
//   int32 i32buf[1];

//   p.currentContext = currentContext;
//   p.i32 = i32buf;

//   p.i32[0] = SK_HARD1;
//   tsV_interceptSpecialKeys_i(&p);

   TEST_SKIP;//CANNOT_RUN;
   finish: ;
}
TESTCASE(tsV_debug_s) // totalcross/sys/Vm native public static void debug(String s);
{
   TNMParams p;
   TCObject obj;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &obj;
   obj = createStringObjectFromCharP(currentContext, "TESTE",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   tsV_debug_s(&p);
   ASSERT1_EQUALS(True, p.retI);
   finish: ;
}
TESTCASE(tsV_clipboardPaste) // totalcross/sys/Vm native public static String clipboardPaste();
{
   TNMParams p1, p2;
   TCObject copied, pasted;

   tzero(p1);
   tzero(p2);

   p1.currentContext = currentContext;
   p2.currentContext = currentContext;
   p1.obj = &copied;
   copied = createStringObjectFromCharP(currentContext, "Testing tsV_clipboardCopy_s and tsV_clipboardPaste", -1);
   setObjectLock(copied, UNLOCKED);
   ASSERT1_EQUALS(NotNull, copied);
   // copy
   tsV_clipboardCopy_s(&p1);
   
#ifndef WP8   //XXX ver se d� para fazer isso de outro modo
   // paste
   tsV_clipboardPaste(&p2);
   ASSERT1_EQUALS(NotNull, p2.retO);
   pasted = p2.retO;
   ASSERT2_EQUALS(I32, String_charsLen(pasted), String_charsLen(copied));
   ASSERT3_EQUALS(Block, String_charsStart(pasted), String_charsStart(copied), String_charsLen(pasted));
#else
   TEST_CANNOT_RUN;
#endif
   finish: ;
}
TESTCASE(tsV_attachLibrary_s) // totalcross/sys/Vm native public static boolean attachLibrary(String name);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tsV_privateAttachNativeLibrary_s) // totalcross/sys/Vm native private static boolean privateAttachNativeLibrary(String name);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tsV_isKeyDown_i) // totalcross/sys/Vm native public static boolean isKeyDown(int key);
{
   TNMParams p;
   int32 i=0;
   tzero(p);
   p.currentContext = currentContext;
   p.i32 = &i;
   tsV_isKeyDown_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);
   finish: ;
}
TESTCASE(tsV_getFile_s) // totalcross/sys/Vm native public static byte[] getFile(String name);
{
   TNMParams p;
   TCObject obj[3];
   int32 i32buf[3];
   int32 ret;
   CharP pRet;
   char startBytes[23]  = {(char)0xFF, (char)0xD8, (char)0xFF, (char)0xE0, (char)0x00, (char)0x10, (char)0x4A,
                           (char)0x46, (char)0x49, (char)0x46, (char)0x00, (char)0x01, (char)0x01, (char)0x01,
                           (char)0x00, (char)0x48, (char)0x00, (char)0x48, (char)0x00, (char)0x00, (char)0xFF,
                           (char)0xE1, 0x1D};

   char endBytes[37]    = {(char)0x43, (char)0xBB, (char)0x9B, (char)0xC5, (char)0x8A, (char)0xD7, (char)0xA8,
                           (char)0xCB, (char)0x74, (char)0x02, (char)0x83, (char)0x8A, (char)0xE5, (char)0xCA,
                           (char)0x74, (char)0xE9, (char)0x4B, (char)0xB7, (char)0x96, (char)0x3B, (char)0xA4,
                           (char)0x7C, (char)0x91, (char)0xD0, (char)0x76, (char)0xAF, (char)0x5E, (char)0x56,
                           (char)0x03, (char)0x1B, (char)0xB0, (char)0x2A, (char)0x58, (char)0xBB, (char)0x4F,
                           (char)0xFF, (char)0xD9};

   p.currentContext = currentContext;
   p.obj = obj;
   p.i32 = i32buf;

   p.obj[0] = createStringObjectFromCharP(currentContext, "barbara.jpg", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   tsV_getFile_s(&p);
   ASSERT1_EQUALS(NotNull, p.retO);

   pRet = ARRAYOBJ_START(p.retO);
   ASSERT1_EQUALS(NotNull, pRet);

   ret = xmemcmp(pRet, startBytes, 23);
   ASSERT2_EQUALS(I32, 0, ret);

   ret = xmemcmp(pRet+18176, endBytes, 37);
   ASSERT2_EQUALS(I32, 0, ret);

   finish: ;
}
TESTCASE(tsV_getRemainingBattery) // totalcross/sys/Vm native public static int getRemainingBattery();
{
   TNMParams p;
   tzero(p);
   p.currentContext = currentContext;

   tsV_getRemainingBattery(&p);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
#if defined WIN32 && !defined WINCE && !defined WP8
   ASSERT2_EQUALS(I32, p.retI, 100);
#else
   ASSERT_BETWEEN(I32, 0, p.retI,100);
#endif
   finish: ;
}
TESTCASE(tsV_tweak_ib) // totalcross/sys/Vm native public static void tweak(int param, boolean set);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tsV_getStackTrace_t) // totalcross/sys/Vm native public static String getStackTrace(Throwable t);
{
   TEST_SKIP;
   finish: ;
}
