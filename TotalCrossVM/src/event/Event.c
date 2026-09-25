// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include <stdlib.h>
#include <string.h>

void updateScreen(Context currentContext);
void markWholeScreenDirty(Context currentContext);
void vmSetAutoOff(bool enable); // vm_c.h

// Platform-specific code
#if TC_WINDOWING_SDL
 #include "sdl/event_c.h"
#elif TC_WINDOWING_NATIVE
 #if TC_OS_WINDOWS || TC_OS_WINCE
  #include "win/event_c.h"
 #elif TC_OS_ANDROID
  #include "android/event_c.h"
 #elif TC_OS_IOS
  #include "darwin/event_c.h"
 #elif TC_OS_LINUX
  #include "linux/event_c.h"
 #else
  #error Unsupported native event backend
 #endif
#else
 #error No event backend selected
#endif
//

static Method onMinimize;
static Method onRestore;
static bool isMinimized;
static void checkTimer(Context currentContext);

#if TC_OS_DESKTOP && TC_WINDOWING_SDL
static bool parseEventLoopWaitMode(const char *value)
{
   return value != NULL && strcmp(value, "wait") == 0;
}

static bool eventLoopHasPendingWork(bool gcPending, bool eventPending,
   bool timerDue)
{
   return gcPending || eventPending || timerDue;
}

static bool eventLoopShouldWakeForAsyncWork(bool waitMode, bool waiting)
{
   return waitMode && waiting;
}

static bool eventLoopShouldWait(bool waitMode, bool wakeEventReady)
{
   return waitMode && wakeEventReady;
}
#endif

bool eventLoopTimerDeadlineWasEarlier(bool absoluteMode, int64 previousDeadline,
   int64 nextDeadline)
{
   if (nextDeadline == 0)
      return false;
   if (previousDeadline == 0)
      return true;
   if (absoluteMode)
      return nextDeadline < previousDeadline;
   return (int32)((uint32)(int32)nextDeadline
      - (uint32)(int32)previousDeadline) < 0;
}

#if TC_OS_DESKTOP && TC_WINDOWING_SDL
static int64 eventLoopTimerRemainingNs(bool absoluteMode,
   int64 absoluteDeadlineNs, int32 relativeDeadlineMs, int64 nowNs,
   int32 nowMs, bool minimized)
{
   if (minimized)
      return -1;
   if (absoluteMode)
   {
      int64 remainingNs;
      if (absoluteDeadlineNs == 0)
         return -1;
      remainingNs = absoluteDeadlineNs - nowNs;
      return remainingNs > 0 ? remainingNs : 0;
   }
   if (relativeDeadlineMs == 0)
      return -1;
   {
      int64 remainingMs = (int64)relativeDeadlineMs - (int64)nowMs;
      return remainingMs > 0 ? (int64)remainingMs * 1000000 : 0;
   }
}

static int32 eventLoopTimeoutMilliseconds(int64 remainingNs)
{
   int64 timeoutMs;
   if (remainingNs < 0)
      return -1;
   timeoutMs = remainingNs / 1000000;
   if (remainingNs % 1000000 != 0)
      timeoutMs++;
   return timeoutMs > 2147483647LL ? 2147483647 : (int32)timeoutMs;
}

static volatile bool eventLoopWaiting;

static bool isEventLoopWaitMode(void)
{
   static int32 mode = -1;
   if (mode < 0)
      mode = parseEventLoopWaitMode(getenv("TC_EVENT_LOOP_MODE"));
   return eventLoopShouldWait(mode != 0, privateHasMainLoopWakeEvent());
}

static int64 eventLoopTimeUntilTimerNs(void)
{
   if (isAbsoluteTimerDeadlineMode())
      return eventLoopTimerRemainingNs(true, nextTimerDeadlineNs, 0,
         getNanoTime(), 0, isMinimized);
   return eventLoopTimerRemainingNs(false, 0, nextTimerTick, 0,
      getTimeStamp(), isMinimized);
}

static bool eventLoopTimerIsDue(void)
{
   return eventLoopTimeUntilTimerNs() == 0;
}

static void pumpEventWait(Context currentContext)
{
   SDL_Event event;
   for (;;)
   {
      int64 remainingNs;
      int32 timeoutMs;
      bool received;

      if (callGConMainThread)
      {
         callGConMainThread = false;
         gc(currentContext);
      }
      checkTimer(currentContext);
      if (privateIsEventAvailable())
      {
         privatePumpEvent(currentContext);
         return;
      }

      remainingNs = eventLoopTimeUntilTimerNs();
      timeoutMs = eventLoopTimeoutMilliseconds(remainingNs);
      eventLoopWaiting = true;
      if (eventLoopHasPendingWork(callGConMainThread,
            privateIsEventAvailable(), eventLoopTimerIsDue()))
      {
         eventLoopWaiting = false;
         continue;
      }

      received = privateWaitEvent(&event, timeoutMs);
      eventLoopWaiting = false;
      if (received)
         privateDispatchEvent(currentContext, event);
      return;
   }
}
#endif

static void checkTimer(Context currentContext)
{
   if (isAbsoluteTimerDeadlineMode())
   {
      int64 nowNs = getNanoTime();
      if (nextTimerDeadlineNs != 0 && nowNs >= nextTimerDeadlineNs
         && !isMinimized && _onTimerTick && _onTimerTick->code)
      {
         lastFiredTimerDeadlineNs = nextTimerDeadlineNs;
         nextTimerDeadlineNs = 0;
         executeMethod(currentContext, _onTimerTick, mainClass, true);
      }
   }
   else if (nextTimerTick != 0 && !isMinimized)
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
#if TC_OS_DESKTOP && TC_WINDOWING_SDL
   if (isEventLoopWaitMode())
   {
      pumpEventWait(currentContext);
      return ok;
   }
#endif
   if (callGConMainThread)
   {
      callGConMainThread = false;
      gc(currentContext);
   }
   if (privateIsEventAvailable())
      privatePumpEvent(currentContext);
   checkTimer(currentContext);
sleep:
#if !TC_OS_IOS
   Sleep(1); // avoid 100% cpu - important on Android!
#endif   
   return ok;
}

void wakeMainEventLoop(void)
{
#if TC_OS_DESKTOP && TC_WINDOWING_SDL
   if (eventLoopShouldWakeForAsyncWork(isEventLoopWaitMode(),
         eventLoopWaiting))
      privateWakeMainEventLoop();
#endif
}

int32 isEventAvailable()
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

#if TC_OS_IOS
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

void postOnMinimizeOrRestore(int32 minimized)
{
   isMinimized = minimized != 0;
   if (mainClass != null)
      executeMethod(lifeContext, (isMinimized ? onMinimize : onRestore), mainClass); // events are always posted to the main execution line
}

int32 initEvent()
{
   return privateInitEvent() != 0;
}

void destroyEvent()
{
   if (oldAutoOffValue != 0) // if user changed the state, restore the old value of the auto-off timer
      vmSetAutoOff(true);
   privateDestroyEvent();
   freeArray(interceptedSpecialKeys);
}

#ifdef ENABLE_TEST_SUITE
int32 eventLoopTestResult(int32 testCase)
{
#if TC_OS_DESKTOP && TC_WINDOWING_SDL
   switch (testCase)
   {
      case 0:
      {
         SDL_Event event;
         Uint32 previousWakeEvent = sdlMainLoopWakeEvent;
         Uint32 wakeEvent = previousWakeEvent == (Uint32)-1
            ? 0x8000 : previousWakeEvent;
         bool consumed;
         sdlMainLoopWakeEvent = wakeEvent;
         SDL_zero(event);
         event.type = wakeEvent;
         consumed = privateDispatchEvent(mainContext, event);
         sdlMainLoopWakeEvent = previousWakeEvent;
         return consumed && !privateIsWakeEventType(0x8001, wakeEvent);
      }
      case 1:
         return eventLoopTimerDeadlineWasEarlier(true, 200, 100)
            && !eventLoopTimerDeadlineWasEarlier(true, 100, 200)
            && eventLoopTimerDeadlineWasEarlier(false, 200, 100)
            && !eventLoopTimerDeadlineWasEarlier(false, 100, 200)
            && eventLoopTimerDeadlineWasEarlier(true, 0, 100)
            && !eventLoopTimerDeadlineWasEarlier(true, 100, 0);
      case 2:
         return eventLoopTimerRemainingNs(true, 0, 0, 100, 0, false) == -1
            && eventLoopTimeoutMilliseconds(-1) == -1;
      case 3:
         return eventLoopTimeoutMilliseconds(1) == 1
            && eventLoopTimeoutMilliseconds(1000000) == 1
            && eventLoopTimeoutMilliseconds(1000001) == 2
            && eventLoopTimeoutMilliseconds(-1) == -1;
      case 4:
         return eventLoopHasPendingWork(true, false, false)
            && eventLoopHasPendingWork(false, true, false)
            && eventLoopHasPendingWork(false, false, true)
            && !eventLoopHasPendingWork(false, false, false);
      case 5:
         return eventLoopShouldWakeForAsyncWork(true, true)
            && !eventLoopShouldWakeForAsyncWork(true, false)
            && !eventLoopShouldWakeForAsyncWork(false, true);
      case 6:
         return !parseEventLoopWaitMode(NULL)
            && !parseEventLoopWaitMode("poll")
            && parseEventLoopWaitMode("wait")
            && !eventLoopShouldWait(parseEventLoopWaitMode(NULL), true)
            && !eventLoopShouldWait(parseEventLoopWaitMode("wait"), false)
            && eventLoopShouldWait(parseEventLoopWaitMode("wait"), true);
      default:
         return false;
   }
#else
   UNUSED(testCase)
   return false;
#endif
}
#endif
