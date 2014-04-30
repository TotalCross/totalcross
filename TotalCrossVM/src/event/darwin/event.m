/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#define Object NSObject*
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#include "../../nm/ui/darwin/mainview.h"
#define Class __Class
#include "GraphicsPrimitives.h"
typedef id Context;
#include "event.h"
#undef Class

extern bool keepRunning;
static bool mainThreadSuspended;

bool allowMainThread()
{
   return !mainThreadSuspended;
}

bool iphone_privateIsEventAvailable()
{
   return [DEVICE_CTX->_mainview isEventAvailable];
}

void iphone_privatePumpEvent(Context currentContext)
{
   NSArray* events = [DEVICE_CTX->_mainview getEvents];
   if(events == nil) return;

   NSEnumerator* enumerator = [events objectEnumerator];
   id event;

   while(event = [enumerator nextObject])
   {
      id type = [event objectForKey:@"type"];
      if(type == nil) continue;

      if([type isEqualToString:@"mouseDown"])
      {
         postEvent(currentContext, PENEVENT_PEN_DOWN, 0, [[event objectForKey:@"x"] intValue], [[event objectForKey:@"y"] intValue], -1);
      }
      else
      if([type isEqualToString:@"mouseUp"])
      {
         postEvent(currentContext, PENEVENT_PEN_UP, 0, [[event objectForKey:@"x"] intValue], [[event objectForKey:@"y"] intValue], -1);
      }
      else
      if(([type isEqualToString:@"mouseDragged"]) || ([type isEqualToString:@"mouseMoved"]))
      {
         postEvent(currentContext, PENEVENT_PEN_DRAG, 0, [[event objectForKey:@"x"] intValue], [[event objectForKey:@"y"] intValue], -1);
      }
      else
      if([type isEqualToString:@"sipClosed"])
      {                                   
         #define CONTROLEVENT_SIP_CLOSED 306
         postEvent(currentContext, CONTROLEVENT_SIP_CLOSED, 0,0,0,0);
      }
      else
      if([type isEqualToString:@"screenChanged"])
      {
         #define SK_SCREEN_CHANGE -1030
         postEvent(currentContext, KEYEVENT_SPECIALKEY_PRESS, SK_SCREEN_CHANGE, 0,0,-1);
      }
      else
      if([type isEqualToString:@"screenChange"])
      {
         screenChange(currentContext, [[event objectForKey:@"width"] intValue], [[event objectForKey:@"height"] intValue],0,0,false);
      }
      else
      if([type isEqualToString:@"keyPress"]) //flsobral@tc126_59: now we support text edition directly in TotalCross controls!
      {
         int pressedKey = [[event objectForKey:@"key"] intValue];
         #define SK_BACKSPACE -1011
         if (pressedKey == '\b')
            postEvent(currentContext, KEYEVENT_SPECIALKEY_PRESS, SK_BACKSPACE, 0,0,-1);
         else
            postEvent(currentContext, KEYEVENT_KEY_PRESS, pressedKey, 0, 0, -1);         
      }
      if([type isEqualToString:@"updateEdit"])
      {
         setEditText(currentContext, [[ event objectForKey:@"control"] intValue ], [ event objectForKey:@"value"]);
      }
   }
}

bool iphone_privateInitEvent()
{
   return true;
}

void iphone_privateDestroyEvent()
{
}

void notifyStopVM() // fdie@ the launcher mainthread notifies the vm thread to stop execution.
{
   mainThreadSuspended = true;
   keepRunning = false;
}
