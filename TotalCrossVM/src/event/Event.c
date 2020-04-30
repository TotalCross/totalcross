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

void updateScreen(Context currentContext);
void vmSetAutoOff(bool enable); // vm_c.h

// Platform-specific code
#if defined(WINCE) || defined(WIN32)
 #include "win/event_c.h"
#elif defined(darwin)
 #include "darwin/event_c.h"
#elif defined(ANDROID)                                                             
 #include "android/event_c.h"
#else
 #include "linux/event_c.h"
#endif
//

static Method onMinimize;
static Method onRestore;
static bool isMinimized;

static void checkTimer(Context currentContext)
{
   if (nextTimerTick != 0 && !isMinimized)
   {
      int32 now = getTimeStamp();

      if (now >= nextTimerTick && _onTimerTick && _onTimerTick->code)
      {
         nextTimerTick = 0;
         executeMethod(currentContext, _onTimerTick, mainClass, true);
      }
   }
}

extern bool wokeUp();

static bool pumpEvent(Context currentContext)
{          
   bool ok = true;   
   if (currentContext != mainContext) // only pump events on the mainContext
   {
      ok = false;
      goto sleep;
   }
   if (callGConMainThread)
   {
      callGConMainThread = false;
      gc(currentContext);
   }
   if (privateIsEventAvailable())
      privatePumpEvent(currentContext);
   checkTimer(currentContext);
sleep:
#ifndef darwin   
   Sleep(1); // avoid 100% cpu - important on Android!
#endif   
   return ok;
}

bool isEventAvailable()
{  
   Sleep(1); // avoid 100% cpu - important on Android!
   return privateIsEventAvailable();
}

void pumpEvents(Context currentContext)
{
   if (keepRunning)
      do
      {
         if (!pumpEvent(currentContext))
            break;
      } while (isEventAvailable() && keepRunning);

   if (!keepRunning && !appExitThrown)
   {
      appExitThrown = true;
      throwException(currentContext, AppExitException,null);
   }
}

void graphicsSetupIOS();

void mainEventLoop(Context currentContext)
{
   // now that the Main class was load, it's safe to get these methods
   _postEvent = getMethod(OBJ_CLASS(mainClass), true, "_postEvent", 6, J_INT, J_INT, J_INT, J_INT, J_INT, J_INT);
   _onTimerTick = getMethod(OBJ_CLASS(mainClass), true, "_onTimerTick", 1, J_BOOLEAN);
   onMinimize = getMethod(OBJ_CLASS(mainClass), true, "_onMinimize", 0);
   onRestore = getMethod(OBJ_CLASS(mainClass), true, "_onRestore", 0);

#ifdef darwin
    graphicsSetupIOS(); // start the opengl context in the same thread of the events
#endif
   if (_onTimerTick == null || _postEvent == null || onMinimize == null || onRestore == null) // unlikely to occur...
      throwException(currentContext, RuntimeException, "Can't find event methods.");
   else
      while (keepRunning)
      {    
#ifdef __gl2_h_
         if (markedImages > 0) // if another thread asked for a gc, then trigger another one now to dispose the images
         {
            lastGC = markedImages = 0;
            gc(currentContext);
         }
#endif         
         pumpEvent(currentContext);
      }
}

void postEvent(Context currentContext, TotalCrossUiEvent type, int32 key, int32 x, int32 y, int32 mods)
{
   if (mainClass != null && _postEvent != null)
   {
      executeMethod(currentContext, _postEvent, mainClass, (int32)type, key, x, y, keyGetPortableModifiers(mods), getTimeStamp()); // events are always posted to the main execution line
   }
}

void postOnMinimizeOrRestore(bool minimized)
{                                  
   isMinimized = minimized;
   if (mainClass != null)
      executeMethod(lifeContext, (isMinimized ? onMinimize : onRestore), mainClass); // events are always posted to the main execution line
}

bool initEvent()
{
   return privateInitEvent();
}

void destroyEvent()
{
   if (oldAutoOffValue != 0) // if user changed the state, restore the old value of the auto-off timer
      vmSetAutoOff(true);
   privateDestroyEvent();
   freeArray(interceptedSpecialKeys);
}
