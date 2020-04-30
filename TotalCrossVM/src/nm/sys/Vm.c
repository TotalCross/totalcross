// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "NativeMethods.h"

#if defined(WINCE) || defined(WIN32)
 #include "win/Vm_c.h"
#elif defined(ANDROID)
 #include "android/Vm_c.h"
#else
 #include "posix/Vm_c.h"
#endif

void vmVibrate(int32 ms);
#ifdef darwin
int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait);
#endif

CompatibilityResult areArraysCompatible(Context currentContext, TCObject array, CharP ident)
{
   // this function searches for the first non-null element in array and checks if it is compatible with ident
   int32 n = ARRAYOBJ_LEN(array);
   TCObjectArray oa = (TCObjectArray)ARRAYOBJ_START(array);
   for (; n-- > 0; oa++)
      if (*oa != null)
         return areClassesCompatible(currentContext, OBJ_CLASS(*oa), ident);
   return COMPATIBLE; // if all elements are null, then it is safe to move null to any kind other of array,
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_arrayCopy_oioii(NMParams p) // totalcross/sys/Vm native public static boolean arrayCopy(Object srcArray, int srcStart, Object dstArray, int dstStart, int length);
{
   TCObject srcArray = p->obj[0];
   TCObject dstArray = p->obj[1];
   int32 srcStart = p->i32[0];
   int32 dstStart = p->i32[1];
   int32 length = p->i32[2];
   uint8 *src, *dst;
   bool result = false;

   if (!srcArray)
      throwNullArgumentException(p->currentContext, "srcArray");
   else
   if (!dstArray)
      throwNullArgumentException(p->currentContext, "dstArray");
   else
   {
      bool isObjectArray;
      isObjectArray = OBJ_PROPERTIES(srcArray)->class_->flags.isObjectArray;
      if (isObjectArray && p->currentContext != gcContext) // prevent another thread from calling the gc while running this method, which can change objects in memory and result in a crash
         LOCKVAR(omm);
      if (dstStart < 0)
         throwIllegalArgumentExceptionI(p->currentContext, "dstStart",dstStart);
      else
      if (length < 0)
         throwIllegalArgumentExceptionI(p->currentContext, "length",length);
      else
      if (length == 0)
         result = true;
      else
      if (!checkArrayRange(p->currentContext, srcArray, srcStart, length) || !checkArrayRange(p->currentContext, dstArray, dstStart, length)) // Check the array's range
         result = false;                                              
      else
      if (OBJ_CLASS(srcArray) != OBJ_CLASS(dstArray) && // quick test
         areClassesCompatible(p->currentContext, OBJ_CLASS(srcArray), OBJ_CLASS(dstArray)->name) != COMPATIBLE && // Check if srcArray and dstArray are compatible
         (OBJ_CLASS(srcArray)->name[1] == '&' || OBJ_CLASS(dstArray)->name[1] == '&' || // make sure the arrays are not primitive arrays
            areArraysCompatible(p->currentContext, srcArray, OBJ_CLASS(dstArray)->name+1) != COMPATIBLE)) // check if the first elements are compatible
         throwException(p->currentContext, ArrayStoreException, "srcArray and dstArray are not compatible");
      else
      {
         TCClass c = OBJ_CLASS(srcArray);
         src = ARRAYOBJ_START(srcArray);
         dst = ARRAYOBJ_START(dstArray);
         if (isObjectArray) // copy pointers directly to prevent a partial assigned value from being considered a valid object
         {
            TCObjectArray psrc = (TCObjectArray)src;
            TCObjectArray pdst = (TCObjectArray)dst;
            if (src == dst && srcStart < dstStart) // copy arrays overlap?
               for (psrc += srcStart + length - 1, pdst += dstStart + length - 1;  --length >= 0; ) // must go backwards to allow copy into overlapping array
                  *pdst-- = *psrc--;
            else
               for (psrc += srcStart, pdst += dstStart;  --length >= 0; )
                  *pdst++ = *psrc++;
         }
         else
         {
            uint32 s = TC_ARRAYSIZE(c, 1);
            xmemmove(dst + dstStart * s, src + srcStart * s, length * s);
         }
         result = true;
      }
      if (isObjectArray && p->currentContext != gcContext)
         UNLOCKVAR(omm);
   }
   p->retI = result;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_getTimeStamp(NMParams p) // totalcross/sys/Vm native public static int getTimeStamp();
{
   p->retI = getTimeStamp();
}
//////////////////////////////////////////////////////////////////////////
void updateDaylightSavings(Context currentContext);

TC_API void tsV_setTime_t(NMParams p) // totalcross/sys/Vm native public static void setTime(totalcross.sys.Time t);
{
   TCObject time = p->obj[0];

   if (!time)
      throwNullArgumentException(p->currentContext, "t");
   else
   {
      vmSetTime(time);
      updateDaylightSavings(p->currentContext); // guich@tc112_2
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_exitAndReboot(NMParams p) // totalcross/sys/Vm native public static void exitAndReboot();
{
   UNUSED(p);
   printf("tsV_exitAndReboot\n");
   keepRunning = false; // stop event loop
   rebootOnExit = true;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_exec_ssib(NMParams p) // totalcross/sys/Vm native public static int exec(String command, String args, int launchCode, boolean wait);
{
   TCObject command = p->obj[0];
   TCObject args = p->obj[1];
   int32 launchCode = p->i32[0];
   bool wait = (bool) p->i32[1];
   TCHARP szCommand;
   TCHARP szArgs = null;

   if (!command)
      throwNullArgumentException(p->currentContext, "command");
   else                  
#ifdef ANDROID
   p->retI = vmExec(command, args, launchCode, wait);
#else        
   if ((szCommand = JCharP2TCHARP(String_charsStart(command), String_charsLen(command))) != null)
   {
      if (!args || (szArgs = JCharP2TCHARP(String_charsStart(args), String_charsLen(args))) != null)
         p->retI = vmExec(szCommand, szArgs, launchCode, wait);
      else
         p->retI = -1;

      xfree(szCommand);
      xfree(szArgs);
   } 
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_setAutoOff_b(NMParams p) // totalcross/sys/Vm native public static void setAutoOff(boolean enable);
{
   vmSetAutoOff(p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_sleep_i(NMParams p) // totalcross/sys/Vm native public static void sleep(int millis);
{
   int32 millis = p->i32[0];

   if (millis < 0)
      throwIllegalArgumentExceptionI(p->currentContext, "millis",millis);
   else
   {
      Sleep(max32(millis,1)); // don't sleep 0, or threads may starve to death
#if defined WIN32 && !defined WP8
      //if (millis == 1) - guich@310: also for other sleep values
      {
         MSG msg;
         PeekMessage(&msg, NULL, 0, 0, PM_NOREMOVE );
      }
#endif
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_getFreeMemory(NMParams p) // totalcross/sys/Vm native public static int getFreeMemory();
{
   p->retI = getFreeMemory(false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_interceptSpecialKeys_I(NMParams p) // totalcross/sys/Vm native public static void interceptSpecialKeys(int []keys);
{
   TCObject keysArray = p->obj[0];
   int32 *keys = null;
   int32 keysLen = 0;

   if (keysArray)
   {
      keys = (int32*) ARRAYOBJ_START(keysArray);
      keysLen = ARRAYOBJ_LEN(keysArray);
   }

   vmInterceptSpecialKeys(keys, keysLen);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_debug_s(NMParams p) // totalcross/sys/Vm native public static void debug(String s);
{
   TCObject strObj = p->obj[0];
   CharP s;

   if (!strObj)             
      p->retI = debugStr("null");
   else
   if ((s = String2CharP(strObj)) != null) // just ignore if no more memory
   {
      p->retI = debugStr(s);
      xfree(s);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_alert_s(NMParams p) // totalcross/sys/Vm native public static void alert(String s);
{
   TCObject strObj = p->obj[0];
   CharP s;

   if (!strObj)
      throwNullArgumentException(p->currentContext, "s");
   else
   if ((s = String2CharP(strObj)) != null) // just ignore if no more memory
   {
      alert(s);
      xfree(s);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_clipboardCopy_s(NMParams p) // totalcross/sys/Vm native public static void clipboardCopy(String s);
{
   TCObject string = p->obj[0];
   if (!string)
      throwNullArgumentException(p->currentContext, "s");
   else
   {   
#if defined(WIN32)
      CharP s;
      int32 sLen;

      if ((s = JCharP2CharP(String_charsStart(string), (sLen = String_charsLen(string)))) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else
      {
         vmClipboardCopy(s, sLen);
         xfree(s);
      }
#else // use unicode on other platforms
      vmClipboardCopy(String_charsStart(string), String_charsLen(string));
#endif
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_clipboardPaste(NMParams p) // totalcross/sys/Vm native public static String clipboardPaste();
{
   p->retO = vmClipboardPaste(p->currentContext);
   if (p->retO)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_attachLibrary_s(NMParams p) // totalcross/sys/Vm native public static boolean attachLibrary(String name);
{
   TCObject nameObj;
   char name[MAX_PATHNAME];

   nameObj = p->obj[0];
   if (nameObj == null)
      throwNullArgumentException(p->currentContext, "name");
   else
   {
      String2CharPBuf(nameObj, name);
      xstrcat(name, ".tcz");
      p->retI = tczLoad(p->currentContext, name) != null;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_privateAttachNativeLibrary_s(NMParams p) // totalcross/sys/Vm native private static boolean privateAttachNativeLibrary(String name);
{
   TCObject nameObj;
   char name[MAX_PATHNAME],*c; // usualy a short name

   p->retI = false;
   nameObj = p->obj[0];
   if (nameObj == null)
      throwNullArgumentException(p->currentContext, "name");
   else
   {
      String2CharPBuf(nameObj, name);
      // strip any .CRTR from the file name, TotalCross doesn't use it
      c = xstrrchr(name, '.');
      if (c)
         *c = 0;
      p->retI = attachNativeLib(p->currentContext, name); // nativelib.c/h
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_isKeyDown_i(NMParams p) // totalcross/sys/Vm native public static boolean isKeyDown(int key);
{
   p->retI = vmIsKeyDown(p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_gc(NMParams p) // totalcross/sys/Vm native public static void gc();
{
   if (!runningGC)
   {
      lastGC = 0; // force gc execution
      gc(p->currentContext);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_getFile_s(NMParams p) // totalcross/sys/Vm native public static byte[] getFile(String name);
{
   TCObject name = p->obj[0];
   char szName[256];
   int32 nameLen;
   TCZFile file;

   if (!name)
      throwNullArgumentException(p->currentContext, "name");
   else
   if ((nameLen = String_charsLen(name)) > 0)
   {
      String2CharPBuf(name, (CharP)szName);
      if ((file = tczGetFile((CharP)szName, false)) != null &&
         (p->retO = createByteArray(p->currentContext, file->uncompressedSize)) != null)
      {
         tczRead(file, ARRAYOBJ_START(p->retO), file->uncompressedSize);
         setObjectLock(p->retO, UNLOCKED);
      }
      tczClose(file);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_getRemainingBattery(NMParams p) // totalcross/sys/Vm native public static int getRemainingBattery();
{
   p->retI = vmGetRemainingBattery();
}
//////////////////////////////////////////////////////////////////////////
int tweakSSL;

TC_API void tsV_tweak_ib(NMParams p) // totalcross/sys/Vm native public static void tweak(int param, boolean set);
{
   int32 param = p->i32[0];
   int32 bit = param - 1;
   int32 on = p->i32[1];
   
   if (bit == -999) // temporary for Tekann only
      tweakSSL = on;
   else   
   if (on)
      vmTweaks |=  (1<<bit);
   else
      vmTweaks &= ~(1<<bit);
   if (param == VMTWEAK_TRACE_METHODS)
      traceOn = on;
   else
   if (param == VMTWEAK_DISABLE_GC && !on) // guich@tc114_18
      gc(p->currentContext);
   else
   if (param == VMTWEAK_MEM_PROFILER) // guich@tc111_4
   {
      if (profilerMaxMem == 0)
         profilerMaxMem = totalAllocated;
      if (on || profilerMaxMem != totalAllocated)
         debug("P Now allocated: %d", totalAllocated);
      if (on)
         profilerMaxMem = 0;
      else
         debug("P Max allocated: %d", profilerMaxMem);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_getStackTrace_t(NMParams p) // totalcross/sys/Vm native public static String getStackTrace(Throwable t);
{
   TCObject t = p->obj[0];
   if (!t)
      throwNullArgumentException(p->currentContext, "t");
   else
      p->retO = *Throwable_trace(t);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_showKeyCodes_b(NMParams p) // totalcross/sys/Vm native public static void showKeyCodes(boolean on);
{
   vmShowKeyCodes(showKeyCodes = p->i32[0] == 1);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_turnScreenOn_b(NMParams p) // totalcross/sys/Vm native public static boolean turnScreenOn(boolean on);
{
   p->retI = vmTurnScreenOn(p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_vibrate_i(NMParams p) // totalcross/sys/Vm native public static void vibrate(int millis);
{
#if defined(WIN32) || defined(ANDROID) || defined(darwin)
   vmVibrate(p->i32[0]);
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_identityHashCode_o(NMParams p) // totalcross/sys/Vm native public static int identityHashCode(Object object);
{
   jlO_nativeHashCode(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsV_preallocateArray_oi(NMParams p) // totalcross/sys/Vm native public static void preallocateArray(Object sample, int length);
{
   TCObject t = p->obj[0];
   int32 len = p->i32[0];
   if (!t)
      throwNullArgumentException(p->currentContext, "sample");
   else
      preallocateArray(p->currentContext, t,len);
}

#ifdef ENABLE_TEST_SUITE
#include "Vm_test.h"
#endif
