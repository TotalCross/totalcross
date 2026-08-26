// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <directfb.h>

bool privateIsEventAvailable()
{
   return DEVICE_CTX->events->HasEvent(DEVICE_CTX->events) == DFB_OK;
}

void privatePumpEvent(Context currentContext)
{
   DFBInputEvent evt;
   int x, y;
   int key;
   UNUSED(currentContext)

   if (DEVICE_CTX->events->GetEvent(DEVICE_CTX->events, DFB_EVENT(&evt)) == DFB_OK)
   {
      if (handleEvent(&evt))
         return;

      switch (evt.type)
      {
         case DIET_KEYPRESS:
            key = keyDevice2Portable(evt.key_symbol);
            if (showKeyCodes)
            {
               alert("Key code: %d\nModifier: %X", (int)key,
                  (int)keyGetPortableModifiers(evt.modifiers));
               return;
            }
            if (key == SK_SCREEN_CHANGE)
            {
               if (*tcSettings.screenWidthPtr != *tcSettings.screenHeightPtr)
                  screenChange(mainContext, *tcSettings.screenHeightPtr,
                     *tcSettings.screenWidthPtr, 0, 0, false);
            }
            else if (key != evt.key_symbol)
               postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, key, 0, 0, evt.modifiers);
            else if (key < 255)
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
            postEvent(mainContext,
               isDragging ? PENEVENT_PEN_DRAG : MOUSEEVENT_MOUSE_MOVE,
               0, x, y, -1);
            break;

         default:
            break;
      }
   }
}

bool privateInitEvent()
{
   DFBResult err;
   deviceCtx = (TScreenSurfaceEx*)xmalloc(sizeof(TScreenSurfaceEx));

   err = DirectFBInit(0, NULL);
   if (err != DFB_OK)
      return false;
   err = DirectFBCreate(&DEVICE_CTX->dfb);
   if (err != DFB_OK || !DEVICE_CTX->dfb)
      return false;

   return DEVICE_CTX->dfb->CreateInputEventBuffer(
      DEVICE_CTX->dfb, DICAPS_ALL, DFB_TRUE, &DEVICE_CTX->events) == DFB_OK
      && DEVICE_CTX->events;
}

void privateDestroyEvent()
{
   if (DEVICE_CTX->dfb)
   {
      DEVICE_CTX->dfb->Release(DEVICE_CTX->dfb);
      DEVICE_CTX->dfb = NULL;
   }
   // xfree(deviceCtx) is intentionally deferred; another access occurs later.
}
