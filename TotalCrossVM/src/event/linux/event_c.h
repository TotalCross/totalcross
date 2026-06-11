// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef HEADLESS
#include <directfb.h>
#else
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

static void postScaleEvent(double scale) {
   union
   {
      double d;
      uint64 l;
   } bits;

   bits.d = scale;
   postEvent(mainContext, MULTITOUCHEVENT_SCALE, 0, (int32)(bits.l >> 32), (int32)bits.l, -1);
}

static int32 findScaleFinger(SDL_FingerID id) {
   int32 i;
   for (i = 0; i < MAX_SCALE_FINGERS; i++) {
      if (scaleFingers[i].active && scaleFingers[i].id == id) {
         return i;
      }
   }
   return -1;
}

static double scaleSqrt(double value) {
   int32 i;
   double result;

   if (value <= 0) {
      return 0;
   }

   result = value >= 1 ? value : 1;
   for (i = 0; i < 12; i++) {
      result = (result + value / result) / 2;
   }
   return result;
}

static double getScaleDistance() {
   double dx = (double)scaleFingers[0].x - (double)scaleFingers[1].x;
   double dy = (double)scaleFingers[0].y - (double)scaleFingers[1].y;
   return scaleSqrt(dx * dx + dy * dy);
}

static void beginScaleGesture() {
   if (!scaleGestureActive && scaleFingerCount >= MAX_SCALE_FINGERS) {
      scaleGestureActive = true;
      lastScaleDistance = getScaleDistance();
#if !__APPLE__
      if (isDragging) {
         isDragging = false;
         postEvent(mainContext, PENEVENT_PEN_UP, 0, 10000, 10000, -1);
      }
#endif
      postEvent(mainContext, MULTITOUCHEVENT_SCALE, 1, 0, 0, -1);
   }
}

static void endScaleGesture() {
   if (scaleGestureActive) {
      scaleGestureActive = false;
      lastScaleDistance = 0;
      postEvent(mainContext, MULTITOUCHEVENT_SCALE, 2, 0, 0, -1);
   }
}
#endif

bool privateIsEventAvailable()
{
#ifndef HEADLESS
	return (DEVICE_CTX->events->HasEvent(DEVICE_CTX->events) == DFB_OK);
#else
   return SDL_PollEvent(NULL);
#endif
}

void handleFingerTouchEvent(SDL_Event event) {
   int width = 0, height = 0;
   TCSDL_GetWindowSize(&screen, &width, &height);
   int32 x = event.tfinger.x * width, y = event.tfinger.y * height;
   switch (event.type) {
      case SDL_FINGERDOWN: {
         int32 i = findScaleFinger(event.tfinger.fingerId);
         if (i < 0 && scaleFingerCount < MAX_SCALE_FINGERS) {
            for (i = 0; i < MAX_SCALE_FINGERS; i++) {
               if (!scaleFingers[i].active) {
                  scaleFingers[i].id = event.tfinger.fingerId;
                  scaleFingers[i].x = event.tfinger.x;
                  scaleFingers[i].y = event.tfinger.y;
                  scaleFingers[i].active = true;
                  scaleFingerCount++;
                  break;
               }
            }
         }
         if (scaleFingerCount >= MAX_SCALE_FINGERS) {
            beginScaleGesture();
         }
#if !__APPLE__
         else {
            isDragging = true;
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0, x, y, -1);
         }
#endif
         break;
      }
      case SDL_FINGERUP:
      {
         int32 i = findScaleFinger(event.tfinger.fingerId);
         if (i >= 0) {
            scaleFingers[i].active = false;
            scaleFingerCount--;
            if (scaleGestureActive) {
               endScaleGesture();
               break;
            }
         }
#if !__APPLE__
         if (!scaleGestureActive) {
            isDragging = false;
            postEvent(mainContext, PENEVENT_PEN_UP, 0, x, y, -1);
         }
#endif
         break;
      }
      case SDL_FINGERMOTION: {
         int32 i = findScaleFinger(event.tfinger.fingerId);
         if (i >= 0) {
            scaleFingers[i].x = event.tfinger.x;
            scaleFingers[i].y = event.tfinger.y;
         }
         if (scaleGestureActive) {
            if (i >= 0) {
               double distance = getScaleDistance();
               if (lastScaleDistance > 0 && distance > 0) {
                  postScaleEvent(distance / lastScaleDistance);
               }
               lastScaleDistance = distance;
            }
         } else {
            postEvent(mainContext, MOUSEEVENT_MOUSE_MOVE, 0, x, y, -1);
         }
         break;
      }
   }
}

void handleMouseEvent(SDL_Event event) {
   int32 timestamp = getTimeStamp();
   if(event.button.button == SDL_BUTTON_LEFT) {
      switch (event.type) {
         case SDL_MOUSEBUTTONDOWN:
            isDragging = true;
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0, event.button.x, event.button.y, timestamp);
            break;
         case SDL_MOUSEBUTTONUP:
            isDragging = false;
            postEvent(mainContext, PENEVENT_PEN_UP, 0, event.button.x, event.button.y, timestamp);
            break;
         case SDL_MOUSEMOTION:
            if(event.motion.state == SDL_PRESSED) { // start dragging
               postEvent(mainContext, PENEVENT_PEN_DRAG, 0, event.motion.x, event.motion.y, timestamp);
            }
            else {
               postEvent(mainContext, MOUSEEVENT_MOUSE_MOVE, 0, event.motion.x, event.motion.y, timestamp);
            }
      }
   }
}

void handleKeyboardEvent(SDL_Event event) {
   int key, modifier;
   if(event.type == SDL_KEYDOWN) {
      key = keyDevice2Portable(event.key.keysym.sym);
      modifier = (int)keyGetPortableModifiers(event.key.keysym.mod);
      if (showKeyCodes) // debug keys?
      {
            // printf("Key code: %d\nModifier: %X\n",(int)key, modifier);
            printf("Event keysym: %d\n", event.key.keysym.sym);
      }
      if (key == SK_SCREEN_CHANGE)
      {
         // if (*tcSettings.screenWidthPtr != *tcSettings.screenHeightPtr)
         //    screenChange(mainContext, *tcSettings.screenHeightPtr,*tcSettings.screenWidthPtr,0,0,false);
      }
      else if (key != event.key.keysym.sym) {
         postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, key, 0, 0, modifier);
      }
      // else if (key < 255)
      //    postEvent(mainContext, KEYEVENT_KEY_PRESS, event.key.keysym.unicode, 0, 0, modifier);
   }
}

void handleWheelEvent(SDL_Event event) {
   int x, y;
   SDL_GetMouseState(&x, &y);
   if(event.wheel.y > 0) // scroll up
   {
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_UP, x, max32(y,0),-1);       
   }
   else if(event.wheel.y < 0) // scroll down
   {
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_DOWN, x, max32(y,0),-1);  
   }
   if(event.wheel.x > 0) // scroll right
   {
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_RIGHT, x, max32(y,0),-1);  
   }
   else if(event.wheel.x < 0) // scroll left
   {
      postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, WHEEL_LEFT, x, max32(y,0),-1);  
   }
}

void handleTextInputEvent(SDL_Event event) {
   char text[32];
   text[0] = '\0';
   int i = 0;
   strcpy(text, event.text.text);
   for (; text[i] != '\0'; i++) {
      int modifier = (int)keyGetPortableModifiers(SDL_GetModState());
      postEvent(mainContext, KEYEVENT_KEY_PRESS, text[i], 0, 0, modifier);
   }
}

void privatePumpEvent(Context currentContext)
{
#ifndef HEADLESS
   DFBInputEvent evt;
   int x, y;
   int key;

   if (DEVICE_CTX->events->GetEvent(DEVICE_CTX->events, DFB_EVENT(&evt)) == DFB_OK)
   {
      if (handleEvent(&evt)) // let the attached native libs handle this event
         return;

      switch (evt.type)
      {
//      case DIET_KEYRELEASE:
//         if (evt.key_symbol == DIKS_ESCAPE)
//            keepRunning = false;
//         break;
      case DIET_KEYPRESS:
      //case DIET_KEYRELEASE:
         key = keyDevice2Portable(evt.key_symbol);
         if (showKeyCodes) // debug keys?
         {
            alert("Key code: %d\nModifier: %X\n",(int)key, (int)keyGetPortableModifiers(evt.modifiers));
            return;
         }

         if (key == SK_SCREEN_CHANGE)
         {
            if (*tcSettings.screenWidthPtr != *tcSettings.screenHeightPtr)
               screenChange(mainContext, *tcSettings.screenHeightPtr,*tcSettings.screenWidthPtr,0,0,false);
         }
         else
         if (key != evt.key_symbol)
            postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, key, 0, 0, evt.modifiers);
         else
         if (key < 255)
            postEvent(mainContext, KEYEVENT_KEY_PRESS, key, 0, 0, evt.modifiers);
         break;

      case DIET_BUTTONPRESS:
         isDragging = true;
         DEVICE_CTX->layer->GetCursorPosition(DEVICE_CTX->layer, &x, &y);
         postEvent(mainContext, PENEVENT_PEN_DOWN, 0, x, y, -1);
         break;

      case DIET_BUTTONRELEASE:
         isDragging = false;
         DEVICE_CTX->layer->GetCursorPosition(DEVICE_CTX->layer, &x, &y);
         postEvent(mainContext, PENEVENT_PEN_UP, 0, x, y, -1);
         break;

      case DIET_AXISMOTION:
         DEVICE_CTX->layer->GetCursorPosition(DEVICE_CTX->layer, &x, &y);
         postEvent(mainContext, isDragging ? PENEVENT_PEN_DRAG : MOUSEEVENT_MOUSE_MOVE, 0, x, y, -1);
         break;

      default:
         break;
      }
   }
#else
   SDL_Event event;
   if(SDL_PollEvent(&event)) {
      if(event.type == SDL_WINDOWEVENT) {
         if(event.window.event == SDL_WINDOWEVENT_SIZE_CHANGED) {
            int width = event.window.data1;
            int height = event.window.data2;
            // SDL_Surface must be refreshed after a SDL_WINDOWEVENT_SIZE_CHANGED event
            TCSDL_WindowSizeChanged(&screen, width, height);
            // screenChange(mainContext, width, height, 0, 0, false);
         }
         TCSDL_Present();
      }
      if(event.type >= SDL_FINGERDOWN && event.type <= SDL_FINGERMOTION) { // Finger Touch Events
         handleFingerTouchEvent(event);
      }
      else if(event.type >= SDL_MOUSEMOTION && event.type <= SDL_MOUSEWHEEL) { //Mouse events
         handleMouseEvent(event);
      }
      if(event.type >= SDL_KEYDOWN) { // KeyBoard events
        handleKeyboardEvent(event);
      }
      if(event.type == SDL_TEXTINPUT) { // Text Input Events
         handleTextInputEvent(event);
      }
      if(event.type == SDL_MOUSEWHEEL) { // Wheel Events
         handleWheelEvent(event);
      }
      if(event.type == SDL_QUIT) {
         keepRunning = false;
      }      
   }   
#endif
}

bool privateInitEvent()
{
#ifndef HEADLESS
   DFBResult err;

   deviceCtx = (TScreenSurfaceEx*)xmalloc(sizeof(TScreenSurfaceEx));

   err = DirectFBInit(0, NULL);
   if (err != DFB_OK) return false;
   err = DirectFBCreate(&DEVICE_CTX->dfb);
   if (err != DFB_OK || !DEVICE_CTX->dfb) return false;

   DFBResult res = DEVICE_CTX->dfb->CreateInputEventBuffer(DEVICE_CTX->dfb, DICAPS_ALL, DFB_TRUE, &DEVICE_CTX->events);
   return (res == DFB_OK && DEVICE_CTX->events);
#else
    return true;
#endif
}

void privateDestroyEvent()
{
#ifndef HEADLESS
   if (DEVICE_CTX->dfb)
   {
      DEVICE_CTX->dfb->Release(DEVICE_CTX->dfb);
      DEVICE_CTX->dfb = NULL;
   }
   //xfree(deviceCtx); todo@ crashes since another access is done yet
#else
   SDL_FlushEvents(SDL_FIRSTEVENT, SDL_LASTEVENT);
#endif
}
