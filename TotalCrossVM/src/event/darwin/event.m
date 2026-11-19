// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#define Object NSObject*
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#include "../../nm/ui/darwin/mainview.h"
#define Class __Class
#include "GraphicsPrimitives.h"
#include "WindowSafeArea.h"
#define Context id
#include "event.h"
#undef Class

extern int32 keepRunning;
extern double screenContentScale;
static bool mainThreadSuspended;

bool allowMainThread()
{
   return !mainThreadSuspended;
}

int32 iphone_privateIsEventAvailable()
{
   return [DEVICE_CTX->_mainview isEventAvailable] ? 1 : 0;
}

void screenChange(Context currentContext, int32 newWidth, int32 newHeight, int32 hRes, int32 vRes, int32 nothingChanged); // rotate the screen
void setEditText(Context currentContext, TCObject control, NSString *str);

// ChildView queues single-touch pen events in physical pixels; Java window
// hit testing expects logical points. Convert only pen events here, leaving
// multitouch gestures and the physical safe-area insets unchanged.
static int32 iosPenPhysicalToLogical(int32 value)
{
   double scale = screenContentScale > 0 ? screenContentScale : 1.0;
   double logical = (double)value / scale;
   return logical >= 0 ? (int32)(logical + 0.5) : (int32)(logical - 0.5);
}

static void postIosPenEvent(Context currentContext, TotalCrossUiEvent type, NSDictionary *event)
{
   int32 physicalX = [[event objectForKey:@"x"] intValue];
   int32 physicalY = [[event objectForKey:@"y"] intValue];
   postEvent(currentContext, type, 0,
       iosPenPhysicalToLogical(physicalX),
       iosPenPhysicalToLogical(physicalY), -1);
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

      if([type isEqualToString:@"multitouchScale"])
      {
         postEvent(currentContext, MULTITOUCHEVENT_SCALE, [[event objectForKey:@"key"] intValue],  [[event objectForKey:@"x"] intValue], [[event objectForKey:@"y"] intValue], -1);
      }
      else
      if([type isEqualToString:@"mouseDown"])
      {
         postIosPenEvent(currentContext, PENEVENT_PEN_DOWN, event);
      }
      else
      if([type isEqualToString:@"mouseUp"])
      {
         postIosPenEvent(currentContext, PENEVENT_PEN_UP, event);
      }
      else
      if(([type isEqualToString:@"mouseDragged"]) || ([type isEqualToString:@"mouseMoved"]))
      {
         postIosPenEvent(currentContext, PENEVENT_PEN_DRAG, event);
      }
      else
      if([type isEqualToString:@"sipClosed"])
      {                                   
         #define CONTROLEVENT_SIP_CLOSED 306
         postEvent(currentContext, CONTROLEVENT_SIP_CLOSED, 0,0,0,0);
      }
      else
      if([type isEqualToString:@"safeAreaChanged"])
      {
         windowUpdateSafeAreaInsetsPhysical(
            currentContext,
            [[event objectForKey:@"top"] intValue],
            [[event objectForKey:@"left"] intValue],
            [[event objectForKey:@"bottom"] intValue],
            [[event objectForKey:@"right"] intValue]);
      }
      else
      if([type isEqualToString:@"screenChange"])
      {
         int width = [[event objectForKey:@"width"] intValue];
         int height = [[event objectForKey:@"height"] intValue];
         screenChange(currentContext, width, height,0,0,false);
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

int32 iphone_privateInitEvent()
{
   return 1;
}

void iphone_privateDestroyEvent()
{
}

void notifyStopVM() // fdie@ the launcher mainthread notifies the vm thread to stop execution.
{
   mainThreadSuspended = true;
   printf("notifyStopVM\n");
   keepRunning = 0;
}
