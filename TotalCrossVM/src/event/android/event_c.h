// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "jni/totalcross_Launcher4A.h"
#include "specialkeys.h"
#include "GraphicsPrimitives.h"
#include "startup.h"
#include "WindowSafeArea.h"

bool isEssentialKey(int32 portableKey);

static bool keysMatch(int32 tcK, int32 sysK) // verifies if the given user key matches the system key
{
   // map the TotalCross keys into the device-specific keys
   // Note that more than one device key may be mapped to a single tc key
   int32 k = keyPortable2Device(sysK);
   return k == tcK;
}

static int32 androidPhysicalToLogical(int32 value)
{
   double scale = screen.contentScale > 0
      ? screen.contentScale
      : 1;

   double logical = value / scale;

   return logical >= 0
      ? (int32)(logical + 0.5)
      : (int32)(logical - 0.5);
}

extern int32 *shiftYfield, glShiftY;
static bool programStarted;
static bool hasPendingSafeAreaInsets;
static int32 pendingSafeAreaTop;
static int32 pendingSafeAreaLeft;
static int32 pendingSafeAreaBottom;
static int32 pendingSafeAreaRight;

/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeSafeAreaInsetsChanged
 * Signature: (IIII)V
 */
void JNICALL Java_totalcross_Launcher4A_nativeSafeAreaInsetsChanged(
   JNIEnv *env,
   jobject thisObject,
   jint top,
   jint left,
   jint bottom,
   jint right)
{
   UNUSED(env);
   UNUSED(thisObject);

   if (!programStarted)
   {
      pendingSafeAreaTop = top;
      pendingSafeAreaLeft = left;
      pendingSafeAreaBottom = bottom;
      pendingSafeAreaRight = right;
      hasPendingSafeAreaInsets = true;
      return;
   }

   windowUpdateSafeAreaInsetsPhysical(mainContext, top, left, bottom, right);
}

/*
 * The argument 'x' is actually the keyCode when the pressed key cannot be translated to an unicode char.
 *
 * Class:     totalcross_Launcher4A
 * Method:    nativeOnEvent
 * Signature: (IIIIII)V
 */
void JNICALL Java_totalcross_Launcher4A_nativeOnEvent(JNIEnv *env, jobject thisObject, jint type, jint key, jint x, jint y, jint modifiers, jint timestamp)
{
   UNUSED(env);
   UNUSED(thisObject);
   UNUSED(timestamp);

   switch (type)
   {
      case totalcross_Launcher4A_SIP_CLOSED:
         postEvent(mainContext, CONTROLEVENT_SIP_CLOSED, 0,0,0,0);
         break;
      case totalcross_Launcher4A_STOPVM_EVENT:
         printf("Java_totalcross_Launcher4A_nativeOnEvent\n");
         keepRunning = false;
         break;
      case totalcross_Launcher4A_KEY_PRESS:
      {
         int32 key2 = keyDevice2Portable(x);
         if (key2 == x) // no change?
            postEvent(mainContext, key == 0 ? KEYEVENT_SPECIALKEY_PRESS : KEYEVENT_KEY_PRESS, key == 0 ? key2 : key, 0,0, modifiers == 18 ? 0 : modifiers); // check if user is pressing the ALT key and pass 0, otherwise characters that are accessed using the alt key won't appear on screen
         else
         {
            bool post = isEssentialKey(key2);
            if (!post && interceptedSpecialKeys != null) // guich@tc122_12: must check if post even if there's no special keys being intercepted
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
         postEvent(mainContext, PENEVENT_PEN_DOWN, 0, androidPhysicalToLogical(x), androidPhysicalToLogical(y), modifiers);
         break;
      case totalcross_Launcher4A_PEN_UP:
         postEvent(mainContext, PENEVENT_PEN_UP, 0, androidPhysicalToLogical(x), androidPhysicalToLogical(y), modifiers);
         break;
      case totalcross_Launcher4A_PEN_DRAG:
         postEvent(mainContext, PENEVENT_PEN_DRAG, 0, androidPhysicalToLogical(x), androidPhysicalToLogical(y), modifiers);
         break;
      case totalcross_Launcher4A_MULTITOUCHEVENT_SCALE:
         postEvent(mainContext, MULTITOUCHEVENT_SCALE, 0, x, y, modifiers);
         break;
      case totalcross_Launcher4A_APP_PAUSED:
         postOnMinimizeOrRestore(true);
         glShiftY = 0;
         break;
      case totalcross_Launcher4A_APP_RESUMED:
         if (shiftYfield)
            *shiftYfield = 0;
         repaintActiveWindows(mainContext);
         postOnMinimizeOrRestore(false);
         break;
      case totalcross_Launcher4A_SCREEN_CHANGED:
      {
         ScreenChangeFlags changes = screenConsumePendingChanges(&screen);

         // Surface values are committed by nativeSurfaceChanged before this
         // notification reaches the event layer. Event arguments are not part
         // of the surface lifecycle protocol anymore.
         if (!screen.surfaceReady)
            break;

         if (!programStarted)
         {
            programStarted = true;
            if (hasPendingSafeAreaInsets)
            {
               windowUpdateSafeAreaInsetsPhysical(
                  mainContext,
                  pendingSafeAreaTop,
                  pendingSafeAreaLeft,
                  pendingSafeAreaBottom,
                  pendingSafeAreaRight);
               hasPendingSafeAreaInsets = false;
            }
            callExecuteProgram(); // blocks until the program has finished
         }
         else if (changes != SCREEN_CHANGE_NONE)
         {
            screenChangeCommitted(mainContext, changes);
         }
         break;
      }
      case totalcross_Launcher4A_BARCODE_READ:
      {
         static Method scannerPostEvent;
         static Context cont;
         if (cont == null)
            cont = newContext(null,null,false);
         if (scannerPostEvent == null)
            scannerPostEvent = getMethod(loadClass(mainContext,"totalcross.io.device.scanner.Scanner",false),false,"_onEvent",1,J_INT);
         executeMethod(cont, scannerPostEvent, 1);
         break;
      }
      case totalcross_Launcher4A_TOKEN_RECEIVED:
         postEvent(mainContext, PUSHNOTIFICATIONEVENT_TOKEN_RECEIVED, 0, x, y, modifiers);
         break;
      case totalcross_Launcher4A_MESSAGE_RECEIVED:
         postEvent(mainContext, PUSHNOTIFICATIONEVENT_MESSAGE_RECEIVED, 0, x, y, modifiers);
         break;
   }
}

bool privateIsEventAvailable()
{
   JNIEnv *env = androidJVM ? getJNIEnv() : null;
   return env != null && (*env)->CallStaticBooleanMethod(env, applicationClass, jeventIsAvailable);
}

void privatePumpEvent(Context currentContext)
{
   UNUSED(currentContext);
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
