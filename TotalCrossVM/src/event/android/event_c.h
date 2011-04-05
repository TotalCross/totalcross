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



#include "../init/android/totalcross_Launcher4A.h"

static bool keysMatch(int32 tcK, int32 sysK) // verifies if the given user key matches the system key
{
   // map the TotalCross keys into the device-specific keys
   // Note that more than one device key may be mapped to a single tc key
   int32 k = keyPortable2Device(sysK);
   return k == tcK;
}

/*
 * The argument 'x' is actually the keyCode when the pressed key cannot be translated to an unicode char.
 *
 * Class:     totalcross_Launcher4A
 * Method:    nativeOnEvent
 * Signature: (IIIIII)V
 */
void JNICALL Java_totalcross_Launcher4A_nativeOnEvent(JNIEnv *env, jobject this, jint type, jint key, jint x, jint y, jint modifiers, jint timestamp)
{                             
   switch (type)
   {
      case totalcross_Launcher4A_STOPVM_EVENT:
         keepRunning = false;
         break;
      case totalcross_Launcher4A_KEY_PRESS:
      {
         int32 key2 = privateKeyDevice2Portable(x);
         if (key2 == x) // no change?
         {                           
            //if (!(('A' <= key && key <= 'Z') || ('a' <= key && key <= 'z') || ('0' <= key && key <= '9')))
            //   debug("pressed key %d (u=%d)", key,x);
            postEvent(mainContext, KEYEVENT_KEY_PRESS, key, 0,0, modifiers == 18 ? 0 : modifiers); // check if user is pressing the ALT key and pass 0, otherwise characters that are accessed using the alt key won't appear on screen
         }
         else
         {                                
            bool post = isEssentialKey(key2);
            if (!post && interceptedSpecialKeys != null) // guich@tc122_12: must check if post even if there´s no special keys being intercepted
            {
               Int32Array keys = interceptedSpecialKeys; // can store special keys (> 0) or totalcross keys (< 0)
               int32 len = ARRAYLEN(keys);
               for (; len-- > 0 && !post; keys++)
                  if (keysMatch(*keys, key2))                                   
                     post = true;         
            }
            if (post)
               postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, key2, 0,0, modifiers);
         }
         break;
      }
      case totalcross_Launcher4A_PEN_DOWN:
         postEvent(mainContext, PENEVENT_PEN_DOWN, 0, x, y, modifiers);
         break;
      case totalcross_Launcher4A_PEN_UP:
         postEvent(mainContext, PENEVENT_PEN_UP, 0, x, y, modifiers);
         break;
      case totalcross_Launcher4A_PEN_DRAG:
         postEvent(mainContext, PENEVENT_PEN_DRAG, 0, x, y, modifiers);
         break;
      case totalcross_Launcher4A_APP_PAUSED:
         postOnMinimizeOrRestore(mainContext, true);
         break;
      case totalcross_Launcher4A_APP_RESUMED:
         repaintActiveWindows(mainContext);
         postOnMinimizeOrRestore(mainContext, false);
         break;
      case totalcross_Launcher4A_SCREEN_CHANGED:
      {
         int32 w = key;
         int32 h = x;
         bool starting = lastW == -2;
         if (w == -999)
         {
            if (starting) // called when app is being installed
               return;
            w = lastW;
            h = lastH;
         }
         lastW = w;
         lastH = h;
         if (starting)
            callExecuteProgram(); // note that this will block until the program has finished
         else
            screenChange(mainContext, w, h, true); // guich@tc126_14: passing true here solves the problem
         break;
      }
   }
}

bool privateIsEventAvailable()
{
   JNIEnv *env = androidJVM ? getJNIEnv() : null;
   return env != null && (*env)->CallStaticBooleanMethod(env, applicationClass, jeventIsAvailable);
}

void privatePumpEvent(Context currentContext)
{
   if (privateIsEventAvailable())
   {
      JNIEnv *env = getJNIEnv();
      (*env)->CallStaticVoidMethod(env, applicationClass, jpumpEvents);
   }
}

bool privateInitEvent()
{
   return true;
}

void privateDestroyEvent()
{
}
