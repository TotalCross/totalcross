/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#define Object NSObject*
#include "mainview.h"
#include "gfx_ex.h"

#import <QuartzCore/CALayer.h>
#define LKLayer CALayer

bool allowMainThread();
static NSLock *deviceCtxLock;
int keyboardH,realAppH,statusbarHeight;
char* createPixelsBuffer(int width, int height);
CGContextRef bitmapContextW,bitmapContextH;
UIWindow* window;

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

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
   [UIView setAnimationsEnabled:NO];
   [UIView setAnimationDelay:0.1];
   [UIView setAnimationDuration:0.1];
   return YES;
}

- (void)viewDidLoad
{
   [super viewDidLoad];
   realAppH = self.view.bounds.size.height;
   DEVICE_CTX->_childview = child_view = [[ChildView alloc] initIt: self];
   [self initEvents];
   self.view = child_view;
}

bool duringRotation;
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
   duringRotation = true;
   [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:0];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
   duringRotation = false;
   [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
}

- (void)initEvents
{
   _lock = [[NSLock alloc] init];

   [[NSNotificationCenter defaultCenter] addObserver:self
      selector:@selector (keyboardDidShow:)
      name: UIKeyboardDidShowNotification object:nil];
 
   [[NSNotificationCenter defaultCenter] addObserver:self 
      selector:@selector (keyboardDidHide:)
      name: UIKeyboardDidHideNotification object:nil];
}

- (void)destroySIP
{
   [ _lock lock ];

   if (kbd_view != null)
   {
      [ kbd_view removeFromSuperview ];
      kbd_view = nil;
   }
   [ _lock unlock ];   
}

- (void)showSIP:(SipArguments*)args
{
   int options = [ args values].options;

   if (options == SIP_HIDE)
      [ self destroySIP ];
   else
   {
      [ _lock lock ];
      if (kbd_view != nil)
      {
         kbd_view.hidden = YES;
         [ kbd_view removeFromSuperview ];
      }

      kbd_view = [ [ KeyboardView alloc ] init:self ];
      [ _lock unlock ];
      if (kbd_view != null)
         [ child_view addSubview: kbd_view ];
   }
}

- (bool)isEventAvailable;
{
   [_lock lock];
   unsigned int num = [_events count];
   [_lock unlock];
   return num > 0;
}

- (NSArray*)getEvents
{
   [_lock lock];
   NSArray* events = _events;
   _events = nil;
   [_lock unlock];

   return events;
}

- (void)addEvent:(NSDictionary*)event
{
   [_lock lock];
   if(_events == nil)
      _events = [[NSMutableArray alloc] init];
   [_events addObject: event];
   [_lock unlock];
}

-(void) keyboardDidShow: (NSNotification *)notif
{
   if (keyboardH != 0) 
      return;

   // Get the size of the keyboard.
   NSDictionary* info = [notif userInfo];
   NSValue* aValue = [info objectForKey:UIKeyboardBoundsUserInfoKey];
   CGSize keyboardSize = [aValue CGRectValue].size;
   keyboardH = keyboardSize.height;
}

-(void) keyboardDidHide: (NSNotification *)notif
{
   keyboardH = 0;
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"sipClosed", @"type",
       [NSNumber numberWithInt:0], @"x",
       [NSNumber numberWithInt:0], @"y",
       nil
      ]
   ];
}

//--------------------------------------------------------------------------------------------------------

@end

void orientationChanged() {} // called by the UI
void privateFullscreen(bool on) {}
void privateScreenChange(int32 w, int32 h) {}

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
   deviceCtxLock = (NSLock*)[[NSRecursiveLock alloc] init];
   deviceCtx = screen->extension = (TScreenSurfaceEx*)malloc(sizeof(TScreenSurfaceEx));
   memset(screen->extension, 0, sizeof(TScreenSurfaceEx));
   // initialize the screen bitmap with the full width and height
   CGRect rect = [[UIScreen mainScreen] bounds];
   int w = rect.size.width, h = rect.size.height;
   statusbarHeight = [[UIApplication sharedApplication] statusBarFrame].size.height;
   
   //[[UIScreen mainScreen] respondsToSelector:@selector(scale)];
   float scale = 1;//[UIScreen mainScreen].scale;
   w = (int)(w * scale);
   h = (int)(h * scale);
   int s = w > h ? w : h;
   char* screenBuffer = (char*)createPixelsBuffer(s, s);
   CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
   bitmapContextW = CGBitmapContextCreate(screenBuffer,w,h-statusbarHeight,8,w*4,colorSpace,kCGImageAlphaNoneSkipLast | kCGBitmapByteOrder32Little);
   bitmapContextH = CGBitmapContextCreate(screenBuffer,h,w-statusbarHeight,8,h*4,colorSpace,kCGImageAlphaNoneSkipLast | kCGBitmapByteOrder32Little);
   CFRelease(colorSpace);

   ////////////////////
   DEVICE_CTX->_window = window = [[UIWindow alloc] initWithFrame: rect];
   window.rootViewController = [(DEVICE_CTX->_mainview = [MainView alloc]) init];
   [window makeKeyAndVisible];
   
   [ DEVICE_CTX->_childview updateScreen: screen ];
   screen->pixels = (void*)1;
   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   [deviceCtxLock lock];
   screen->extension = deviceCtx;
   [deviceCtxLock unlock];
   return true;
}

void graphicsUpdateScreen(ScreenSurface screen, int32 transitionEffect)
{
   [deviceCtxLock lock];
   ChildView* vw = (ChildView*)DEVICE_CTX->_childview;
   if (allowMainThread())
      [vw invalidateScreen: screen];
   [deviceCtxLock unlock];       
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   [deviceCtxLock lock];
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
   [deviceCtxLock unlock];
}

bool graphicsLock(ScreenSurface screen, bool on)
{
   if (on)
      [deviceCtxLock lock];
   else
      [deviceCtxLock unlock];
   return true;
}
