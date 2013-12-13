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



#include "FiveWay.h"
#include "P:/extlibs/PalmOS5RE/Incs/System/EventPrv.h"

void screenChange(Context currentContext, int32 newWidth, int32 newHeight, int32 hRes, int32 vRes, bool nothingChanged);

bool privateIsEventAvailable()
{
   return SysEventAvail();
}

static bool keysMatch(int32 tcK, int32 sysK, EventType* eventP) // verifies if the given user key matches the system key
{
   // map the TotalCross keys into the device-specific keys
   // Note that more than one device key may be mapped to a single tc key
   int32 k = keyPortable2Device(sysK);
   if (k == tcK)
      return true;
   // check the five-way navigation
   switch (tcK)
   {
      case SK_PAGE_UP        : return sysK == vchrNavChange && NavKeyPressed(eventP, Up);
      case SK_PAGE_DOWN      : return sysK == vchrNavChange && NavKeyPressed(eventP, Down);
      case SK_LEFT           : return sysK == vchrNavChange && NavKeyPressed(eventP, Left);
      case SK_RIGHT          : return sysK == vchrNavChange && NavKeyPressed(eventP, Right);
      case SK_ACTION         : return sysK == vchrNavChange && NavKeyPressed(eventP, Select);
      case SK_MENU           :
      case SK_KEYBOARD_ABC   :
      case SK_KEYBOARD_123   :
      case SK_KEYBOARD       : return true; // always intercepted
   }
   return false;
}

#define Treo600DevID 'H101'

static bool checkGETreo650() // guich@570_53: is greater or equal to Treo 650?
{
   UInt32 companyId; // guich@554_38: added the version variable
   UInt32 deviceID;
   if (FtrGet(sysFtrCreator, sysFtrNumOEMCompanyID, &companyId) == 0 && FtrGet(sysFtrCreator, sysFtrNumOEMDeviceID, &deviceID))
      return companyId == 'hspr' && deviceID != Treo600DevID;
   return false;
}

static bool checkSupportsDIA()
{
   UInt32 version;
   return FtrGet(pinCreator, pinFtrAPIVersion, &version) == errNone && version != 0;
}

void privatePumpEvent(Context currentContext)
{
   EventType event;
   Int16 xx,yy;
   Err err;
   Boolean pdown;

   //if (!SysEventAvail()) return; - guich@tc100b4: Palm OS requires SysEventGet to be called always, otherwise incoming phone calls will not work

   SysEventGet(&event, 0);
   if (handleEvent(&event)) // let the attached native libs handle this event
      return;

   switch ((int32)event.eType)
   {
      case winDisplayChangedEvent:
         if (supportsDIA)
         {
            RectangleType r;
            UInt16 cs = WinSetCoordinateSystem(kCoordinatesNative);
            WinGetBounds (WinGetDisplayWindow(), &r);
            WinSetCoordinateSystem(cs);
            screenChange(currentContext, r.extent.x, r.extent.y, 0, 0, false);
         }
         break;
      case appStopEvent:
         if (!*tcSettings.dontCloseApplicationPtr) // allow the user intercept the exit event
            keepRunning = false;
         break;
      // guich@560_21: the zire 22 and Treo 650 may reset if the attention manager appears (when the center button is kept pressed)
      case keyHoldEvent:
         if (event.data.keyHold.chr == vchrHardRockerCenter && event.data.keyHold.keyCode == vchrRockerCenter && event.data.keyDown.modifiers == 0x808)
            return;
         break;
      case keyUpEvent:
         if (event.data.keyDown.chr == vchrHardRockerCenter)
            postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, ((getTimeStamp()-actionStart) >= 800) ? SK_MENU : SK_ACTION, 0,0,-1);
         actionStart = 0;
         break;
      case keyDownEvent:
      {
         int32 key = event.data.keyDown.chr,pkey;
         if (showKeyCodes) // debug keys?
         {
            alert("Key code: %d\nModifier: %X",(int)keyDevice2Portable(key), (int)keyGetPortableModifiers(event.data.keyDown.modifiers));
            return;
         }
         if (key == vchrHardRockerCenter)
         {
            actionStart = getTimeStamp();
            return;
         }
         if (key == 8 && isGETreo650) // guich@570_53
            return;
         // fdie@570_40: also intercept all attention mgr related events (including alarm UI notifications)
         if (key >= vchrAttnStateChanged && key <= vchrAttnReopen)
            return;
         if (key == menuChr || key == keyboardNumericChr || key == keyboardAlphaChr) // always intercept the menu
         {
            postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, keyDevice2Portable(key), 0,0, event.data.keyDown.modifiers);
            return;
         }
         if (interceptedSpecialKeys != null)
         {
            Int32Array keys = interceptedSpecialKeys;
            int32 len = ARRAYLEN(keys);
            for (; len-- > 0; keys++)
               if (keysMatch(*keys, key, &event))
               {
                  int32 kk = keyDevice2Portable(*keys);
                  postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, kk, 0,0, event.data.keyDown.modifiers);
                  return;
               }
         }
         pkey = keyDevice2Portable(key);
         if (pkey != key)
            postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, pkey, 0,0, event.data.keyDown.modifiers);
         else
         if (key <= 255) // and unicode?
            postEvent(mainContext, KEYEVENT_KEY_PRESS, key, 0,0, event.data.keyDown.modifiers);
         break;
      }
		case penUpEvent:
         EvtGetPenNative(WinGetDisplayWindow(), &xx, &yy, &pdown);
         lastPenX = lastPenY = -1;
         if (yy < screen.screenH) // ignore events posted in the grafitti area
            postEvent(mainContext, PENEVENT_PEN_UP, 0, xx, yy, -1);
         break;
		case penDownEvent:
         EvtGetPenNative(WinGetDisplayWindow(), &xx, &yy, &pdown);
         lastPenX = xx; lastPenY = yy;
         if (yy < screen.screenH) // ignore events posted in the grafitti area
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0, xx, yy, -1);
         break;
		case penMoveEvent:
         EvtGetPenNative(WinGetDisplayWindow(), &xx, &yy, &pdown);
         if ((lastPenX != xx || lastPenY != yy) && yy < screen.screenH) // ignore events posted in the grafitti area
            postEvent(mainContext, PENEVENT_PEN_DRAG, 0, lastPenX = xx, lastPenY = yy, -1);
         break;
   }
   if (SysHandleEvent(&event) || MenuHandleEvent((void *)0, &event, &err) || FrmDispatchEvent(&event)) {/* do nothing */}
}

bool privateInitEvent()
{
   isGETreo650 = checkGETreo650();
   supportsDIA = checkSupportsDIA();
   return true;
}

void privateDestroyEvent()
{
}
