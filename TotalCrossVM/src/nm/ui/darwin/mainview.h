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



#ifndef MAINVIEW_H
#define MAINVIEW_H

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <CoreFoundation/CoreFoundation.h>
#import <UIKit/UITextView.h>
#import <QuartzCore/CALayer.h>

#include "GraphicsPrimitives.h"
#import "kbdview.h"
#import "sipargs.h"

#if 0
 #define DEBUG0(fmt)         debug(fmt)
 #define DEBUG1(fmt,a)       debug(fmt,a)
 #define DEBUG2(fmt,a,b)     debug(fmt,a,b)
 #define DEBUG3(fmt,a,b,c)   debug(fmt,a,b,c)
 #define DEBUG4(fmt,a,b,c,d) debug(fmt,a,b,c,d)
#else
 #define DEBUG0(fmt)
 #define DEBUG1(fmt,a)
 #define DEBUG2(fmt,a,b)
 #define DEBUG3(fmt,a,b,c)
 #define DEBUG4(fmt,a,b,c,d)
#endif

/*
 * fdie@ add iPhone full screen support.
 * The UIView can't be created during the app launching insofar its creation have to be deferred until
 * we started the video mode where the we choose between fullscreen and normal mode.
 * But it also requires to call the video switching code in the app main thread. The app mainloop() is
 * executed in a event dispatching thread that is not able to successfully call into the UIKit framework :-(
 */

@interface SSize : NSObject
{
   CGSize ssize;
}

- (id)set:(CGSize)size;
- (CGSize)get;

@end

@interface MainView : UIView
{
   NSMutableArray* _events;
   NSLock* _lock;
   KeyboardView *kbd_view;
   int current_orientation;
   bool full_screen;
   // child
   CGContextRef bitmapContext;
   CGImageRef cgImage;
   int width, height, pitch;
   int lastEventTS;
   int shiftY;
}

- (double)durationForTransition:(int)type;

- (id)initWithFrame:(CGRect)rect;
- (void)setFullscreen:(bool)mode;
- (bool)isFullscreen;
- (void)geometryChanged;
- (bool)isKbdShown;
- (int)orientation;

- (void)lock:(const char *)info;
- (void)unlock;

- (void)addEvent:(NSDictionary*)event;
- (bool)isEventAvailable;
- (NSArray*)getEvents;

- (void)showSIP:(SipArguments*)args;
- (void)destroySIP;

- (void)screenChange: (bool)force;
- (void)scheduleScreenChange: (CGSize)size;
- (void)doScreenChange: (SSize*)size;

- (void)didRotate:(NSNotification *)notification;
- (void) keyboardDidShow: (NSNotification *)notif;
- (void) keyboardDidHide: (NSNotification *)notif;

// child

- (void)drawRect:(CGRect)frame;
- (void)invalidateScreen:(void*)vscreen;

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)screenChange:(int)w height:(int)h;

@end

typedef struct
{
   __unsafe_unretained UIWindow  *_window;
   __unsafe_unretained MainView  *_mainview;
//   __unsafe_unretained ChildView *_childview;
} TScreenSurfaceEx, *ScreenSurfaceEx;

#endif
