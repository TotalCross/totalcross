/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include <directfb.h>

bool privateIsEventAvailable()
{
	return (DEVICE_CTX->events->HasEvent(DEVICE_CTX->events) == DFB_OK);
}

void privatePumpEvent(Context currentContext)
{
   if (!privateIsEventAvailable())
      return;

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
}

bool privateInitEvent()
{
   DFBResult err;

   deviceCtx = (TScreenSurfaceEx*)xmalloc(sizeof(TScreenSurfaceEx));

   err = DirectFBInit(0, NULL);
   if (err != DFB_OK) return false;
   err = DirectFBCreate(&DEVICE_CTX->dfb);
   if (err != DFB_OK || !DEVICE_CTX->dfb) return false;

   DFBResult res = DEVICE_CTX->dfb->CreateInputEventBuffer(DEVICE_CTX->dfb, DICAPS_ALL, DFB_TRUE, &DEVICE_CTX->events);
   return (res == DFB_OK && DEVICE_CTX->events);
}

void privateDestroyEvent()
{
   if (DEVICE_CTX->dfb)
   {
      DEVICE_CTX->dfb->Release(DEVICE_CTX->dfb);
      DEVICE_CTX->dfb = NULL;
   }
   //xfree(deviceCtx); todo@ crashes since another access is done yet
}
