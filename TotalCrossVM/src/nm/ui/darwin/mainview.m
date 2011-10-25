/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#define Object NSObject*
#include "mainview.h"
#include "gfx_ex.h"

#ifdef darwin9
#import <UIKit/UIHardware.h> //flsobral@tc125: UIHardware is not part of the public API, and may be changed by Apple without any warning!
#import <QuartzCore/CALayer.h>
#define LKLayer CALayer
#else
#import <LayerKit/LKLayer.h>
#endif

void privateScreenChange(int32 w, int32 h);
bool isFullScreen();
bool allowMainThread();

static bool unlockOrientationChanges = false;
static NSLock *deviceCtxLock;
static int statusbar_height;

void lockDeviceCtx(const char *info)
{
   DEBUG1("lock DeviceCtx: '%s'\n", info);
   if (!deviceCtxLock)
   {
      deviceCtxLock = [[NSRecursiveLock alloc] init];
      [deviceCtxLock retain];
   }
   [ deviceCtxLock lock ];
   DEBUG0("DeviceCtx locked\n");
}

void unlockDeviceCtx()
{
   [ deviceCtxLock unlock ];
   DEBUG0("DeviceCtx unlocked\n");
}

static NSThread *screenLockThread;

void checkThread(const char *mesg)
{
/*
   if (screenLockThread != [NSThread currentThread])
   {
      DEBUG1("================== WRONG THREAD ======== %s =======\n", mesg);
      exit(0);
   }
*/
}

void _debug(const char *format, ...)
{
   char buffer[1024];
   va_list va;
   va_start(va, format);
   vsprintf(buffer, format, va);
   va_end(va);

   bool dont_close = false;
   FILE *lout = fopen( [NSThread isMainThread] ? "/tmp/MAIN.out" : "/tmp/TC.out", "a+");
   if (!lout)
   {
      lout = stdout;
      dont_close = true;
   }
   fprintf(lout, [NSThread isMainThread] ? "MAIN [%08x]: " : "tc   [%08x]: ", [NSThread currentThread]);
   fprintf(lout, buffer);
   if (buffer[strlen(buffer)-1] != '\n')
	  fprintf(lout, "\n");

   if (!dont_close)
      fclose(lout);
}

@implementation SSize

- (id)set:(CGSize)size
{
   ssize = size;
   return self;
}

- (CGSize)get
{
   return ssize;
}

@end

//--------------------------------------------------------------------------------------------------------

@implementation MainView

- (double)durationForTransition:(int)type
{
#ifndef darwin9
   if (kbd_view)
      return [ super durationForTransition: type ];
#endif
   return 1.0f;
}

- (id)initWithFrame:(CGRect)rect
{
   child_view = nil;
   child_added = true;
   current_orientation = kOrientationVertical; // initial orientation

   _events = nil;
   _lock = [[NSLock alloc] init];
   [_lock retain];

   DEBUG4("initWithFrame: %dx%d,%dx%d\n",
         (int)rect.origin.x, (int)rect.origin.y, (int)rect.size.width, (int)rect.size.height);   
   
   self = [ super initWithFrame: rect ];
   [ self geometryChanged ];
   
#ifdef darwin9 //flsobral@tc126: register didRotate to receive orientation change notifications.
   [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
   
   [[NSNotificationCenter defaultCenter] addObserver:self
      selector:@selector(didRotate:)
      name:UIDeviceOrientationDidChangeNotification object:nil];
#endif

   return self;
}

- (void)geometryChanged
{
   [ self lock: "mainview:geometryChanged" ];
   DEBUG0("MainView geometryChanged\n");

   /* start orientation */
   DEBUG1("init rotate: %d\n", current_orientation);

   old_view = child_view;

   CGRect rect = [ self frame ];
   DEBUG4("NEWCHILD: %dx%d,%dx%d\n",
   			(int)rect.origin.x, (int)rect.origin.y, (int)rect.size.width, (int)rect.size.height);

   float max_dim = rect.size.width > rect.size.height ? rect.size.width  : rect.size.height;
   float min_dim = rect.size.width > rect.size.height ? rect.size.height : rect.size.width;
   DEBUG2("max_dim=%f min_dim=%f\n", max_dim, min_dim);

#ifndef darwin9
   if (old_view != nil)
      [ old_view setEnabled: FALSE ];
#endif

   if (current_orientation == kOrientationHorizontalLeft || current_orientation == kOrientationHorizontalRight)
   {
      float diff = max_dim - min_dim;
      rect = CGRectMake(-diff/2, diff/2, max_dim, min_dim);
      child_view = [ [ ChildView alloc ] initWithFrame: rect orientation:current_orientation ];

      struct CGAffineTransform transEnd =
      		(current_orientation == kOrientationHorizontalLeft) ?
       		   CGAffineTransformMake(0,  1, -1, 0, 0, 0) :
       		   CGAffineTransformMake(0, -1,  1, 0, 0, 0);

	  [ child_view setTransform:transEnd];
   }
   else
   {
      rect = CGRectMake(0, 0, min_dim, max_dim);
      child_view = [ [ ChildView alloc ] initWithFrame: rect orientation:current_orientation ];

      struct CGAffineTransform transEnd = (current_orientation == kOrientationVerticalUpsideDown) ?
      			CGAffineTransformMake(-1,  0,  0, -1, 0, 0) :
      			CGAffineTransformMake(1,  0,  0, 1, 0, 0);

	  [ child_view setTransform:transEnd];

   }

   if (DEVICE_CTX->_childview != null) //flsobral@tc126: fixed a huge memory leak on screen rotation, caused by the chieldview not being released.
      [ DEVICE_CTX->_childview release ];

   DEVICE_CTX->_childview = child_view;
   [ DEVICE_CTX->_childview retain ];

#if DELAYED_SHOWING
   child_added = false;
#else

   [ self addSubview: child_view ];
   //[ self sendSubviewToBack: child_view ];
   DEBUG0("child_view done\n");

   if (old_view != nil)
   {
      [ self transition: 6 fromView: old_view toView: child_view ];
      DEBUG0("transition to new view\n");
   }
   else
   {
      [ self bringSubviewToFront: child_view ];
      DEBUG0("put new view to front\n");
   }

   if (old_view != nil)
   {
      [ old_view removeFromSuperview ];
      [ old_view release ];
   }
#endif

   [ self unlock ];
}

#if DELAYED_SHOWING
void _switchView()
{
   DEBUG1("switchView: %x\n", DEVICE_CTX->_mainview);
   [DEVICE_CTX->_mainview scheduleSwitchView ];
}

- (void)scheduleSwitchView
{
   DEBUG0("scheduleSwitchView\n");
   if (!child_added || old_view != nil && allowMainThread())
      [DEVICE_CTX->_mainview performSelectorOnMainThread:@selector(switchView) withObject:nil waitUntilDone: YES];
   DEBUG0("scheduleSwitchView done\n");
}

- (void)switchView
{
   DEBUG0("switchView\n");
   if (!child_added)
   {
      [ self addSubview: child_view ];
      child_added = true;

      if (old_view != nil)
      {
         [ self transition: 6 fromView: old_view toView: child_view ];
         //[ self _startTransition: 8 fromView: old_view toView: child_view ];
         DEBUG0("transition to new view\n");
      }
      else
      {
         [ self bringSubviewToFront: child_view ];
         DEBUG0("put new view to front\n");
      }
   }

   if (old_view != nil)
   {
      [ old_view removeFromSuperview ];
      [ old_view release ];
      old_view = nil;
   }
   DEBUG0("switchView done\n");
}
#endif

- (bool)isKbdShown
{
   return (kbd_view != nil);
}

- (int)orientation
{
   return current_orientation;
}

- (void)destroySIP
{
   DEBUG1("********************************************************** destroySIP: kbd_view=%x\n", kbd_view);
   [ self lock: "mainview:destroySIP" ];
#ifndef darwin9   
   if (child_view != nil && kbd_view != null)
      [ self transition: 6 fromView: kbd_view toView: child_view ];
   else
#endif      
      [ self bringSubviewToFront: child_view ];

   if (kbd_view != null)
   {
      DEBUG0("really release kbd_view\n");
      [ kbd_view removeFromSuperview ];
      [ kbd_view release ];
      kbd_view = nil;
   }
   [ self unlock ];
}

- (void)showSIP:(SipArguments*)args
{
   int options = [ args values].options;

   DEBUG1("********************************************************** showSIP option=%d\n", options);

   if (options == SIP_HIDE)
     [ self destroySIP ];
   else
   {
      [ self lock: "mainview:showSIP" ];
	  if (kbd_view != nil)
	  {
#ifdef darwin9
	     kbd_view.hidden = YES;
#endif
	     [ kbd_view removeFromSuperview ];
	     [ kbd_view release ];
	  }

      CGRect rect = [ self frame ];
      kbd_view = [ [ KeyboardView alloc ] initWithFrame: CGRectMake(0, 0, rect.size.width, rect.size.height) params: args ];
      if (kbd_view != null)
      {
	     [ kbd_view retain ];
//	     [ kbd_view setOpaque: NO ];
//	     kbd_view.alpha = 0.75;
#ifndef darwin9
	     [ kbd_view setClearsContext: TRUE ];
#endif
	     [ self addSubview: kbd_view ];

#ifndef darwin9
	     if (child_view != null)
	        [ self transition: 6 fromView: child_view toView: kbd_view ];
	     else
#endif	        
	        [ self bringSubviewToFront: kbd_view ];
	  }
      [ self unlock ];
   }
   DEBUG0("showSIP DONE\n");
}

- (void)dealloc
{
   [_events release];
   [_lock release];
   [ super dealloc ];
}

static bool verbose_lock;

- (void)lock:(const char *)info;
{
   verbose_lock = (info != NULL);
   if (verbose_lock)
      DEBUG1("claim MainView lock for '%s'\n", info);
   [_lock lock];
   if (verbose_lock)
      DEBUG0("got MainView lock\n");
}

- (void)unlock
{
   [_lock unlock];
   if (verbose_lock)
      DEBUG0("MainView lock released\n");
}

- (bool)isEventAvailable;
{
   [self lock: NULL ];
   unsigned int num = [_events count];
   [self unlock];
   return num > 0;
}

- (NSArray*)getEvents
{
   [self lock: NULL ];
   NSArray* events = _events;
   _events = nil;
   [self unlock];

   [events autorelease];

   return events;
}

- (void)addEvent:(NSDictionary*)event
{
   [self lock: "addEvent"];

   if(_events == nil)
      _events = [[NSMutableArray alloc] init];

   [_events addObject: event];

   [self unlock];
}

- (void)setFullscreen:(bool)mode
{
   full_screen = mode;
   [ self screenChange: true ];
}

- (bool)isFullscreen
{
   return full_screen;
}

- (void)screenChange: (bool)force
{
   if ( [ self isKbdShown ]) return;
   DEBUG1("main screenChange: force=%d\n", force);
   int orientation = [ UIHardware deviceOrientation: YES ];

   if (child_view != nil && !force)
   {
      if (orientation == kOrientationUnknown || orientation == kOrientationFlatUp || orientation == kOrientationFlatDown)
	  	 return; // keep previous

      if (orientation == current_orientation)
         return; // don't change
   }

   int width, height;
   CGRect rect = [ self frame ];
#ifdef darwin9
   if (orientation == kOrientationHorizontalLeft || orientation == kOrientationHorizontalRight)
   {
      height = rect.size.width - getStatusBarHeight();
      if (current_orientation == kOrientationHorizontalLeft || current_orientation == kOrientationHorizontalRight)
         width = rect.size.height;
      else
         width = rect.size.height + getStatusBarHeight();
   }
   else
   {
      width = rect.size.width;
      height = rect.size.height;
      
      if (current_orientation == kOrientationHorizontalLeft || current_orientation == kOrientationHorizontalRight)
         height -= getStatusBarHeight();
   }
#else
   if (orientation == kOrientationHorizontalLeft || orientation == kOrientationHorizontalRight)
   {
      width = 480;
      height = full_screen ? 320 : 300;
   }
   else
   {
      width = 320;
      height = full_screen ? 480 : 460;
   }
#endif
   current_orientation = orientation;

   lockDeviceCtx("screenChange");
   if (DEVICE_CTX && DEVICE_CTX->_childview)
   {
      [ DEVICE_CTX->_childview screenChange: width height:height ];
   }
   unlockDeviceCtx();

}

- (void)scheduleScreenChange: (CGSize)size
{
   if (allowMainThread())
   {
      // must be an object, cannot be a struct
      SSize *s = [[[ SSize alloc ] set: size ] autorelease ];
      [ self performSelectorOnMainThread:@selector(doScreenChange:) withObject:s waitUntilDone: YES ];
   }
}

- (void)doScreenChange: (SSize*)size
{
   privateScreenChange([size get].width, [size get].height);
}

#ifdef darwin9 //flsobral@tc126: new method to handle screen rotation
- (void)didRotate:(NSNotification *)notification
{
   [self screenChange: NO];
}
#endif

//--------------------------------------------------------------------------------------------------------

@end

void orientationChanged() // called by the UI
{
   if (unlockOrientationChanges && DEVICE_CTX && DEVICE_CTX->_mainview)
   {
      DEBUG0("orientationChanged() call screenChange\n");
      [DEVICE_CTX->_mainview screenChange: false];
   }
}

void privateFullscreen(bool on)
{
   if (DEVICE_CTX && DEVICE_CTX->_mainview)
      [ DEVICE_CTX->_mainview setFullscreen: on ];
}

int getStatusBarHeight()
{
   MainView *main_view = DEVICE_CTX->_mainview;
   bool fullscreen = (main_view != nil) ? [ main_view isFullscreen ] : false;
   
   if (fullscreen)
      return 0;
   return [ UIHardware statusBarHeight ];
}

void privateScreenChange(int32 w, int32 h)
{
   DEBUG0("privateScreenChange\n");
   if (![NSThread isMainThread])
   {
      [DEVICE_CTX->_mainview scheduleScreenChange: CGSizeMake(w, h)];
      return;
   }

   int romVersion = getRomVersion();
   float bar_orientation = 0.0f;

   lockDeviceCtx("privateScreenChange");

   float bar_size = (float) getStatusBarHeight();
   int current_orientation = [DEVICE_CTX->_mainview orientation];
   DEBUG2("orientation: %d bar_size=%f\n", current_orientation, bar_size);

   MainView *main_view = DEVICE_CTX->_mainview;
   bool fullscreen = (main_view != nil) ? [ main_view isFullscreen ] : false;
#ifdef darwin9 //flsobral@tc126: from now on, the status bar will always be shown when the application is not full screen.   
   if (fullscreen)
#else
   if (fullscreen || current_orientation == kOrientationVerticalUpsideDown)
#endif
   {
      bar_size = 0.0f; //hide the status bar
   }
   else if (current_orientation == kOrientationHorizontalLeft)
      bar_orientation = 90;
   else if (current_orientation == kOrientationHorizontalRight)
      bar_orientation = -90;
   
#ifdef darwin9
   [[UIApplication sharedApplication] setStatusBarHidden: (bar_size > 0) ? false:true ];
   [[UIApplication sharedApplication] setStatusBarOrientation: current_orientation animated: true];
#else
   [UIHardware _setStatusBarHeight: bar_size]; //flsobral@tc125_31: Apparently this function is not available on version 3.2, and not required on previous versions either. So I moved it to be used only on iPhone 1.0
   [UIApp setStatusBarMode: (bar_size > 0) ? 0:2 orientation:bar_orientation duration:0.0f fenceID:0];
#endif

#ifdef darwin9
   CGRect rect = [[UIScreen mainScreen] applicationFrame];
#else
   CGRect rect = [ UIHardware fullScreenApplicationContentRect ];
#endif   
   DEBUG4("SCREEN: %dx%d,%dx%d\n",
   			(int)rect.origin.x, (int)rect.origin.y, (int)rect.size.width, (int)rect.size.height);

   if (!fullscreen)
   {
      if (current_orientation == kOrientationHorizontalLeft)
      {
         rect.origin.x -= getStatusBarHeight();
         rect.origin.y = 0;
      }
      else if (current_orientation == kOrientationHorizontalRight)
         rect.origin.y = 0;
   }

   DEBUG4("WINDOW: %dx%d,%dx%d\n",
   			(int)rect.origin.x, (int)rect.origin.y, (int)rect.size.width, (int)rect.size.height);

   UIWindow *window = DEVICE_CTX->_window;
   if (window == nil)
   {
#ifdef darwin9
      DEVICE_CTX->_window = window = [ [ UIWindow alloc ] initWithFrame: rect ];
#else
      DEVICE_CTX->_window = window = [ [ UIWindow alloc ] initWithContentRect: rect ];
#endif
      [ DEVICE_CTX->_window retain ];
   }
   else
      [ window setFrame: rect ];

   CGRect viewRect = CGRectMake(0, 0, rect.size.width, rect.size.height);
   DEBUG4("MAINVIEW: %dx%d,%dx%d\n",
   			(int)0, (int)0, (int)rect.size.width, (int)rect.size.height);

   if (main_view == nil)
   {
      DEVICE_CTX->_mainview = main_view = [ [ MainView alloc ] initWithFrame: viewRect];
      [ DEVICE_CTX->_mainview retain ];
      DEBUG0("new MainView\n");
#ifdef darwin9
      [ window addSubview: main_view ];
      [ window makeKeyAndVisible ];
#else
      [ window setContentView: main_view ];
      [ window orderFront: UIApp ];
      [ window makeKey: UIApp ];
      [ window _setHidden: NO ];
#endif
   }
   else
   {
      [ main_view geometryChanged ];
   }

   unlockDeviceCtx();
}

bool graphicsStartup(ScreenSurface screen)
{
   lockDeviceCtx("graphicsStartup");

   deviceCtx = screen->extension = (TScreenSurfaceEx*)malloc(sizeof(TScreenSurfaceEx));
   memset(screen->extension, 0, sizeof(TScreenSurfaceEx));

   privateScreenChange(0, 0);
   DEBUG0("graphicsStartup done\n");

   [ DEVICE_CTX->_childview updateScreen: screen ];

   screen->pixels = 1;

   unlockDeviceCtx();

   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   lockDeviceCtx("graphicsCreateScreenSurface");
   screen->extension = deviceCtx;
   unlockDeviceCtx();
   return true;
}

void graphicsUpdateScreen(ScreenSurface screen, int32 transitionEffect)
{
   lockDeviceCtx("graphicsUpdateScreen");
   ChildView* vw = (ChildView*)SCREEN_EX(screen)->_childview;
   DEBUG1("graphicsUpdateScreen begin %x\n", vw);
   //MainView* mw = (MainView*)SCREEN_EX(screen)->_mainview;
   if (allowMainThread())
      [vw performSelectorOnMainThread:@selector(setNeedsDisplay) withObject:nil waitUntilDone: NO];
   DEBUG0("graphicsUpdateScreen done\n");
   unlockOrientationChanges = true;
   unlockDeviceCtx();
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   lockDeviceCtx("graphicsDestroy");
   if (isScreenChange)
   {
	  screen->extension = NULL;
   }
   else
   {
      if (screen->extension)
	     free(screen->extension);
	  deviceCtx = screen->extension = NULL;
   }
   unlockDeviceCtx();
}

bool graphicsLock(ScreenSurface screen, bool on)
{
   DEBUG1("graphicsLock begin screen=%x\n", screen);
   int romVersion = getRomVersion();
   if (on)
   {
      lockDeviceCtx("graphicsLock");
      if (romVersion >= 320)
         screen->pixels = [SCREEN_EX(screen)->_childview getPixels];
      else
      {
         CoreSurfaceBufferRef surf = [SCREEN_EX(screen)->_childview getSurface];
         CoreSurfaceBufferLock(surf, 3);
         screen->pixels = CoreSurfaceBufferGetBaseAddress(surf);
         DEBUG3("===============================lock screen:%x screenSurface=%x pixels=%x\n", screen, surf, screen->pixels);
      }
   }
   else
   {
      if (romVersion < 320)
      {
         CoreSurfaceBufferRef surf = [SCREEN_EX(screen)->_childview getSurface];
         DEBUG0("===============================unlock pixels\n");
         CoreSurfaceBufferUnlock(surf);
      }
      unlockDeviceCtx();
   }
   return true;
}
