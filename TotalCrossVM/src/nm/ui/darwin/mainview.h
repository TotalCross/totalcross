/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: mainview.h,v 1.18 2011-03-11 19:13:03 fabio Exp $

#ifndef MAINVIEW_H
#define MAINVIEW_H

#import <UIKit/UIKit.h>
#import <GraphicsServices/GraphicsServices.h>
#import <Foundation/Foundation.h>
#import <CoreSurface/CoreSurface.h>
#import <UIKit/UITransitionView.h>

#include "GraphicsPrimitives.h"
#import "kbdview.h"
#import "childview.h"
#import "sipargs.h"

#if 0
 #define DEBUG0(fmt)         _debug(fmt)
 #define DEBUG1(fmt,a)       _debug(fmt,a)
 #define DEBUG2(fmt,a,b)     _debug(fmt,a,b)
 #define DEBUG3(fmt,a,b,c)   _debug(fmt,a,b,c)
 #define DEBUG4(fmt,a,b,c,d) _debug(fmt,a,b,c,d)
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

@interface MainView : UITransitionView
{
   NSMutableArray* _events;
   //NSRecursiveLock* _lock;
   NSLock* _lock;
   KeyboardView *kbd_view;
   bool child_added;
   ChildView *child_view;
   ChildView *old_view;
   int current_orientation;
   bool full_screen;
}

- (double)durationForTransition:(int)type;

- (id)initWithFrame:(CGRect)rect;
- (void)setFullscreen:(bool)mode;
- (bool)isFullscreen;
- (void)geometryChanged;
- (bool)isKbdShown;
- (int)orientation;
- (void)dealloc;

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

#ifdef darwin9
- (void)didRotate:(NSNotification *)notification;
#endif

#if DELAYED_SHOWING
- (void)scheduleSwitchView;
- (void)switchView;
#endif

@end

typedef struct
{
   UIWindow  *_window;
   MainView  *_mainview;
   ChildView *_childview;
} TScreenSurfaceEx, *ScreenSurfaceEx;

#endif
