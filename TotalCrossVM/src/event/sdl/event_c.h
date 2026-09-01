// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#if __APPLE__
#include "SDL.h"
#else
#include "SDL2/SDL.h"
#endif
#include "../../init/tcsdl.h"
#include "event_sdl.h"

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

static void dispatchPortableSpecialKey(PortableSpecialKeys key, int32 modifiers)
{
   if (key == SK_SCREEN_CHANGE)
   {
      int32 width;
      int32 height;
      TCSDL_GetWindowSize(&screen, &width, &height);
      if (width <= 0 || height <= 0 || width == height)
         return;
      if (TCSDL_SetWindowSize(height, width))
      {
         int32 minimum = screen.minScreenW;
         screen.minScreenW = screen.minScreenH;
         screen.minScreenH = minimum;
      }
   }
   else
      postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, key, 0, 0, modifiers);
}

#if defined(WIN32) && !defined(WINCE)
static bool windowsMessageHookInstalled;

PortableSpecialKeys vmWin32KeyToPortable(int32 key);

static void SDLCALL sdlWindowsMessageHook(void *userdata, void *hWnd,
   unsigned int message, Uint64 wParam, Sint64 lParam)
{
   int32 key;
   Int32Array keys;
   int32 len;

   UNUSED(userdata);
   UNUSED(lParam);
   if (message != WM_HOTKEY || hWnd != (void*)mainHWnd
      || interceptedSpecialKeys == null)
      return;

   key = (int32)wParam;
   keys = interceptedSpecialKeys;
   len = ARRAYLEN(keys);
   while (len-- > 0)
   {
      if (*keys++ == key)
      {
         dispatchPortableSpecialKey(vmWin32KeyToPortable(key), -1);
         return;
      }
   }
}

void sdlInstallWindowsMessageHook()
{
   SDL_SetWindowsMessageHook(sdlWindowsMessageHook, null);
   windowsMessageHookInstalled = true;
}

void sdlRemoveWindowsMessageHook()
{
   if (windowsMessageHookInstalled)
   {
      SDL_SetWindowsMessageHook(null, null);
      windowsMessageHookInstalled = false;
   }
}
#endif

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
#if !__APPLE__
      if (isDragging)
      {
         isDragging = false;
         postEvent(mainContext, PENEVENT_PEN_UP, 0, 10000, 10000, -1);
      }
#endif
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
   // Pump native events while checking availability without consuming them.
   return SDL_PollEvent(NULL);
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
#if !__APPLE__
         else
         {
            isDragging = true;
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0, x, y, -1);
         }
#endif
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
#if !__APPLE__
         if (!scaleGestureActive)
         {
            isDragging = false;
            postEvent(mainContext, PENEVENT_PEN_UP, 0, x, y, -1);
         }
#endif
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
   switch (event.type)
   {
      case SDL_MOUSEBUTTONDOWN:
         if (event.button.button == SDL_BUTTON_LEFT)
         {
            isDragging = true;
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0,
               event.button.x, event.button.y, -1);
         }
         break;
      case SDL_MOUSEBUTTONUP:
         if (event.button.button == SDL_BUTTON_LEFT)
         {
            isDragging = false;
            postEvent(mainContext, PENEVENT_PEN_UP, 0,
               event.button.x, event.button.y, -1);
         }
         break;
      case SDL_MOUSEMOTION:
         if (event.motion.state & SDL_BUTTON_LMASK)
            postEvent(mainContext, PENEVENT_PEN_DRAG, 0,
               event.motion.x, event.motion.y, -1);
         else
            postEvent(mainContext, MOUSEEVENT_MOUSE_MOVE, 0,
               event.motion.x, event.motion.y, -1);
         break;
   }
}

static bool isControlShortcut(int32 key, int32 modifiers)
{
   if ((modifiers & KMOD_CTRL) == 0)
      return false;
   switch (key)
   {
      case SDLK_a:
      case SDLK_c:
      case SDLK_p:
      case SDLK_v:
      case SDLK_x:
      case SDLK_SPACE:
         return true;
      default:
         return false;
   }
}

static void handleKeyboardEvent(SDL_Event event)
{
   int key;
   if (event.type == SDL_KEYDOWN)
   {
      key = keyDevice2Portable(event.key.keysym.sym);
      if (showKeyCodes)
         printf("Event keysym: %d\n", event.key.keysym.sym);
      if (key != event.key.keysym.sym)
         dispatchPortableSpecialKey(key, event.key.keysym.mod);
      else if (isControlShortcut(event.key.keysym.sym, event.key.keysym.mod))
         postEvent(mainContext, KEYEVENT_KEY_PRESS, event.key.keysym.sym,
            0, 0, event.key.keysym.mod);
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

static void postTextCodePoint(uint32 codePoint, int32 modifier)
{
   if (codePoint <= 0xFFFF)
      postEvent(mainContext, KEYEVENT_KEY_PRESS, (int32)codePoint, 0, 0, modifier);
   else
   {
      codePoint -= 0x10000;
      postEvent(mainContext, KEYEVENT_KEY_PRESS,
         (int32)(0xD800 + (codePoint >> 10)), 0, 0, modifier);
      postEvent(mainContext, KEYEVENT_KEY_PRESS,
         (int32)(0xDC00 + (codePoint & 0x3FF)), 0, 0, modifier);
   }
}

static void handleTextInputEvent(SDL_Event event)
{
   const uint8 *text = (const uint8*)event.text.text;
   int32 modifier = SDL_GetModState();
   while (*text != '\0')
   {
      uint32 codePoint;
      int32 length;
      uint8 first = *text++;

      if (first < 0x80)
      {
         codePoint = first;
         length = 0;
      }
      else if (first >= 0xC2 && first <= 0xDF)
      {
         codePoint = first & 0x1F;
         length = 1;
      }
      else if (first >= 0xE0 && first <= 0xEF)
      {
         codePoint = first & 0x0F;
         length = 2;
      }
      else if (first >= 0xF0 && first <= 0xF4)
      {
         codePoint = first & 0x07;
         length = 3;
      }
      else
      {
         postTextCodePoint(0xFFFD, modifier);
         continue;
      }

      const uint8 *continuation = text;
      bool valid = true;
      for (int32 i = 0; i < length; i++)
      {
         if ((continuation[i] & 0xC0) != 0x80)
         {
            valid = false;
            break;
         }
         codePoint = (codePoint << 6) | (continuation[i] & 0x3F);
      }
      if (!valid || (length == 2 && codePoint < 0x800)
         || (length == 3 && codePoint < 0x10000)
         || codePoint > 0x10FFFF
         || (codePoint >= 0xD800 && codePoint <= 0xDFFF))
      {
         postTextCodePoint(0xFFFD, modifier);
         continue;
      }
      text += length;
      postTextCodePoint(codePoint, modifier);
   }
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
         switch (event.window.event)
         {
            case SDL_WINDOWEVENT_SIZE_CHANGED:
            case SDL_WINDOWEVENT_DISPLAY_CHANGED:
            case SDL_WINDOWEVENT_MOVED:
            {
               TScreenConfiguration configuration;
               if (TCSDL_QueryWindowMetrics(&screen, &configuration))
               {
                  ScreenChangeFlags changes = screenApplyConfiguration(
                     &screen, &configuration);
                  screenConsumePendingChanges(&screen);
                  screenChangeCommitted(mainContext, changes);
               }
               break;
            }
            case SDL_WINDOWEVENT_MINIMIZED:
               postOnMinimizeOrRestore(true);
               break;
            case SDL_WINDOWEVENT_RESTORED:
               postOnMinimizeOrRestore(false);
               break;
            case SDL_WINDOWEVENT_EXPOSED:
               markWholeScreenDirty(mainContext);
               break;
            case SDL_WINDOWEVENT_CLOSE:
               keepRunning = false;
               break;
         }
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
      case SDL_TEXTEDITING:
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

void sdlEventWindowCreated(void)
{
   SDL_StartTextInput();
}

void sdlEventWindowDestroying(void)
{
   SDL_StopTextInput();
}

void privateDestroyEvent()
{
   SDL_FlushEvents(SDL_FIRSTEVENT, SDL_LASTEVENT);
}
