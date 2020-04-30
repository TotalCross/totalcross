// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define Object NSObject*
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#include "../../nm/ui/darwin/mainview.h"
#define Class __Class
#include "GraphicsPrimitives.h"
#define Context id
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

void screenChange(Context currentContext, int32 newWidth, int32 newHeight, int32 hRes, int32 vRes, bool nothingChanged); // rotate the screen
void setEditText(Context currentContext, TCObject control, NSString *str);

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

      if([type isEqualToString:@"multitouchScale"])
      {
         postEvent(currentContext, MULTITOUCHEVENT_SCALE, [[event objectForKey:@"key"] intValue],  [[event objectForKey:@"x"] intValue], [[event objectForKey:@"y"] intValue], -1);
      }
      else
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
         #define SK_ENTER -1009
         if (pressedKey == '\n')
            postEvent(currentContext, KEYEVENT_SPECIALKEY_PRESS, SK_ENTER, 0,0,-1);
         else
         if (pressedKey == '\b')
            postEvent(currentContext, KEYEVENT_SPECIALKEY_PRESS, SK_BACKSPACE, 0,0,-1);
         else
            postEvent(currentContext, KEYEVENT_KEY_PRESS, pressedKey, 0, 0, -1);         
      }
      if([type isEqualToString:@"updateEdit"])
      {
         setEditText(currentContext, (TCObject)[[ event objectForKey:@"control"] longValue ], [ event objectForKey:@"value"]);
      }
      [event release];
   }
   if (enumerator)
   {
      [events release];
      [enumerator release];
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
   printf("notifyStopVM\n");
   keepRunning = false;
}
