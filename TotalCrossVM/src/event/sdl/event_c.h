// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#if __APPLE__
#include "SDL.h"
#else
#include "SDL2/SDL.h"
#endif
#include "../../init/tcsdl.h"

#define MAX_SCALE_FINGERS 2

typedef struct
{
   SDL_FingerID id;
   float x;
   float y;
   bool active;
} ScaleFinger;

static ScaleFinger scaleFingers[MAX_SCALE_FINGERS];
static int32 scaleFingerCount;
static bool scaleGestureActive;
static double lastScaleDistance;

static void postScaleEvent(double scale)
{
   union
   {
      double d;
      uint64 l;
   } bits;

   bits.d = scale;
   postEvent(mainContext, MULTITOUCHEVENT_SCALE, 0,
      (int32)(bits.l >> 32), (int32)bits.l, -1);
}

static int32 findScaleFinger(SDL_FingerID id)
{
   int32 i;
   for (i = 0; i < MAX_SCALE_FINGERS; i++)
      if (scaleFingers[i].active && scaleFingers[i].id == id)
         return i;
   return -1;
}

static double scaleSqrt(double value)
{
   int32 i;
   double result;

   if (value <= 0)
      return 0;
   result = value >= 1 ? value : 1;
   for (i = 0; i < 12; i++)
      result = (result + value / result) / 2;
   return result;
}

static double getScaleDistance()
{
   double dx = (double)scaleFingers[0].x - (double)scaleFingers[1].x;
   double dy = (double)scaleFingers[0].y - (double)scaleFingers[1].y;
   return scaleSqrt(dx * dx + dy * dy);
}

static void beginScaleGesture()
{
   if (!scaleGestureActive && scaleFingerCount >= MAX_SCALE_FINGERS)
   {
      scaleGestureActive = true;
      lastScaleDistance = getScaleDistance();
      if (isDragging)
      {
         isDragging = false;
         postEvent(mainContext, PENEVENT_PEN_UP, 0, 10000, 10000, -1);
      }
      postEvent(mainContext, MULTITOUCHEVENT_SCALE, 1, 0, 0, -1);
   }
}

static void endScaleGesture()
{
   if (scaleGestureActive)
   {
      scaleGestureActive = false;
      lastScaleDistance = 0;
      postEvent(mainContext, MULTITOUCHEVENT_SCALE, 2, 0, 0, -1);
   }
}

bool privateIsEventAvailable()
{
   SDL_Event event;
   return SDL_PeepEvents(&event, 1, SDL_PEEKEVENT,
      SDL_FIRSTEVENT, SDL_LASTEVENT) > 0;
}

static void handleFingerTouchEvent(SDL_Event event)
{
   int width = 0, height = 0;
   TCSDL_GetWindowSize(&screen, &width, &height);
   int32 x = event.tfinger.x * width;
   int32 y = event.tfinger.y * height;

   switch (event.type)
   {
      case SDL_FINGERDOWN:
      {
         int32 i = findScaleFinger(event.tfinger.fingerId);
         if (i < 0 && scaleFingerCount < MAX_SCALE_FINGERS)
         {
            for (i = 0; i < MAX_SCALE_FINGERS; i++)
            {
               if (!scaleFingers[i].active)
               {
                  scaleFingers[i].id = event.tfinger.fingerId;
                  scaleFingers[i].x = event.tfinger.x;
                  scaleFingers[i].y = event.tfinger.y;
                  scaleFingers[i].active = true;
                  scaleFingerCount++;
                  break;
               }
            }
         }
         if (scaleFingerCount >= MAX_SCALE_FINGERS)
            beginScaleGesture();
         else
         {
            isDragging = true;
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0, x, y, -1);
         }
         break;
      }
      case SDL_FINGERUP:
      {
         int32 i = findScaleFinger(event.tfinger.fingerId);
         if (i >= 0)
         {
            scaleFingers[i].active = false;
            scaleFingerCount--;
            if (scaleGestureActive)
            {
               endScaleGesture();
               break;
            }
         }
         if (!scaleGestureActive)
         {
            isDragging = false;
            postEvent(mainContext, PENEVENT_PEN_UP, 0, x, y, -1);
         }
         break;
      }
      case SDL_FINGERMOTION:
      {
         int32 i = findScaleFinger(event.tfinger.fingerId);
         if (i >= 0)
         {
            scaleFingers[i].x = event.tfinger.x;
            scaleFingers[i].y = event.tfinger.y;
         }
         if (scaleGestureActive && i >= 0)
         {
            double distance = getScaleDistance();
            if (lastScaleDistance > 0 && distance > 0)
               postScaleEvent(distance / lastScaleDistance);
            lastScaleDistance = distance;
         }
         else
            postEvent(mainContext, MOUSEEVENT_MOUSE_MOVE, 0, x, y, -1);
         break;
      }
   }
}

static void handleMouseEvent(SDL_Event event)
{
   int32 timestamp = getTimeStamp();
   if (event.button.button == SDL_BUTTON_LEFT)
   {
      switch (event.type)
      {
         case SDL_MOUSEBUTTONDOWN:
            isDragging = true;
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0,
               event.button.x, event.button.y, timestamp);
            break;
         case SDL_MOUSEBUTTONUP:
            isDragging = false;
            postEvent(mainContext, PENEVENT_PEN_UP, 0,
               event.button.x, event.button.y, timestamp);
            break;
         case SDL_MOUSEMOTION:
            if (event.motion.state == SDL_PRESSED)
               postEvent(mainContext, PENEVENT_PEN_DRAG, 0,
                  event.motion.x, event.motion.y, timestamp);
            else
               postEvent(mainContext, MOUSEEVENT_MOUSE_MOVE, 0,
                  event.motion.x, event.motion.y, timestamp);
            break;
      }
   }
}

static void handleKeyboardEvent(SDL_Event event)
{
   int key, modifier;
   if (event.type == SDL_KEYDOWN)
   {
      key = keyDevice2Portable(event.key.keysym.sym);
      modifier = (int)keyGetPortableModifiers(event.key.keysym.mod);
      if (showKeyCodes)
         printf("Event keysym: %d\n", event.key.keysym.sym);
      if (key != event.key.keysym.sym)
         postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, key, 0, 0, modifier);
   }
}

static void handleWheelEvent(SDL_Event event)
{
   int x, y;
   SDL_GetMouseState(&x, &y);
   if (event.wheel.y > 0)
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_UP, x, max32(y, 0), -1);
   else if (event.wheel.y < 0)
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_DOWN, x, max32(y, 0), -1);
   if (event.wheel.x > 0)
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_RIGHT, x, max32(y, 0), -1);
   else if (event.wheel.x < 0)
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_LEFT, x, max32(y, 0), -1);
}

static void handleTextInputEvent(SDL_Event event)
{
   int i;
   int modifier = (int)keyGetPortableModifiers(SDL_GetModState());
   for (i = 0; event.text.text[i] != '\0'; i++)
      postEvent(mainContext, KEYEVENT_KEY_PRESS, event.text.text[i], 0, 0, modifier);
}

void privatePumpEvent(Context currentContext)
{
   SDL_Event event;
   UNUSED(currentContext)

   if (!SDL_PollEvent(&event))
      return;

   switch (event.type)
   {
      case SDL_WINDOWEVENT:
         if (event.window.event == SDL_WINDOWEVENT_SIZE_CHANGED)
            TCSDL_WindowSizeChanged(&screen, event.window.data1, event.window.data2);
         TCSDL_Present();
         break;
      case SDL_FINGERDOWN:
      case SDL_FINGERUP:
      case SDL_FINGERMOTION:
         handleFingerTouchEvent(event);
         break;
      case SDL_MOUSEMOTION:
      case SDL_MOUSEBUTTONDOWN:
      case SDL_MOUSEBUTTONUP:
         handleMouseEvent(event);
         break;
      case SDL_KEYDOWN:
         handleKeyboardEvent(event);
         break;
      case SDL_TEXTINPUT:
         handleTextInputEvent(event);
         break;
      case SDL_MOUSEWHEEL:
         handleWheelEvent(event);
         break;
      case SDL_QUIT:
         keepRunning = false;
         break;
   }
}

bool privateInitEvent()
{
   return true;
}

void privateDestroyEvent()
{
   SDL_FlushEvents(SDL_FIRSTEVENT, SDL_LASTEVENT);
}
