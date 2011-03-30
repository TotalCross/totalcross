#define Object NSObject*
#import "childview.h"
#import "mainview.h"
#import <UIKit/UIHardware.h>

unsigned short* screenBuffer = nil;

@implementation ChildView

- (void)updateScreen: (void*)scr
{
   ScreenSurface screen = scr;
   screen->screenW = width;
   screen->screenH = height;
   screen->pitch = pitch;
   screen->bpp = getRomVersion() >= 320 ? 32 : 16;

   DEBUG4("update screen INFOS: %dx%dx%d, pitch=%d\n", width, height, 16, pitch); 
}

- (id)initWithFrame:(CGRect)rect orientation:(int)orient
{
   orientation = orient;
   width = rect.size.width;
   height = rect.size.height;
   
   int romVersion = getRomVersion();
   DEBUG4("CHILDVIEW: %dx%d,%dx%d\n", 
   			(int)rect.origin.x, (int)rect.origin.y, (int)rect.size.width, (int)rect.size.height);

   self = [ super initWithFrame: rect ];
   if (self != nil )
   {
      pitch = width * (romVersion >= 320 ? 4 : 2);
      int size = pitch * height;

      static const char *_buffer; // single screen memory buffer
      if (!_buffer)
         _buffer = malloc(height*pitch); // allocate the maximum size
      //memset(_buffer, 0, height*pitch);

#ifdef darwin9
      if (romVersion >= 320)
      {
         screenBuffer = _buffer;
         CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
         bitmapContext = CGBitmapContextCreate(
               screenBuffer,
               width,
               height,
               8, // bitsPerComponent
               pitch, // bytesPerRow
               colorSpace,
               kCGImageAlphaNoneSkipFirst | kCGBitmapByteOrder32Little);
         CFRelease(colorSpace);
      }
      else
      {
         screenSurface = CoreSurfaceBufferCreate(
               (CFDictionaryRef)[NSDictionary dictionaryWithObjectsAndKeys:
                  [NSNumber numberWithInt:width],     kCoreSurfaceBufferWidth,
                  [NSNumber numberWithInt:height],    kCoreSurfaceBufferHeight,
                  [NSNumber numberWithInt:'L565'],    kCoreSurfaceBufferPixelFormat,
                  [NSNumber numberWithInt:size],      kCoreSurfaceBufferAllocSize,
                  [NSNumber numberWithBool:YES],      kCoreSurfaceBufferGlobal,
                  [NSNumber numberWithInt:pitch],     kCoreSurfaceBufferPitch,
                  @"PurpleGFXMem",                    kCoreSurfaceBufferMemoryRegion,
                  //[NSNumber numberWithInt:_buffer],   kCoreSurfaceBufferClientAddress,
                  nil]);
              
         // Create layer for surface
         CoreSurfaceBufferLock(screenSurface, 3);
      }

      screenLayer = [[CALayer layer] retain];
      [screenLayer setMagnificationFilter:0];
      [screenLayer setEdgeAntialiasingMask:0];
      [screenLayer setFrame: CGRectMake(0, 0, width+1, height+1)];
      if (romVersion < 320)
         [screenLayer setContents:screenSurface];
      [screenLayer setOpaque:YES];
      [[self layer] addSublayer:screenLayer];

      if (romVersion < 320)
         CoreSurfaceBufferUnlock(screenSurface);
#else   
      CFMutableDictionaryRef dict;
      char *pixelFormat = "565L";
      
      /* Create a screen surface */
      dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 0,
				       &kCFTypeDictionaryKeyCallBacks, &kCFTypeDictionaryValueCallBacks);
      CFDictionarySetValue(dict, kCoreSurfaceBufferGlobal, kCFBooleanTrue);
      CFDictionarySetValue(dict, kCoreSurfaceBufferPitch,
			   CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &pitch));
      CFDictionarySetValue(dict, kCoreSurfaceBufferWidth,
			   CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &width));
      CFDictionarySetValue(dict, kCoreSurfaceBufferHeight,
			   CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &height));
      CFDictionarySetValue(dict, kCoreSurfaceBufferPixelFormat,
			   CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, pixelFormat));
      CFDictionarySetValue(dict, kCoreSurfaceBufferAllocSize,
			   CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &size));

      CFDictionarySetValue(dict, kCoreSurfaceBufferClientAddress,
			   CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &_buffer));

      screenSurface = CoreSurfaceBufferCreate(dict);
      CoreSurfaceBufferLock(screenSurface, 3);
	  
      screenLayer = [ [ LKLayer layer ] retain ];
      [ screenLayer setFrame: CGRectMake(0, 0, width, height) ];
      [ screenLayer setContents: screenSurface ];
      [ screenLayer setOpaque: YES ];
	
      [ [ self _layer ] addSublayer: screenLayer ];

      CoreSurfaceBufferUnlock(screenSurface);
      [ dict release ];
#endif

#ifdef SAMPLE
#define PIXEL 0xFFFF
      CoreSurfaceBufferLock(screenSurface, 3);
      uint16 *baseAddress = CoreSurfaceBufferGetBaseAddress(screenSurface);
      DEBUG2("==================== screenSurface=%x baseAdress=%x\n", screenSurface, baseAddress); 
      int i;
      for (i = 0; i < width-1; i++)
	     baseAddress[width * (height * i / width) + i] = PIXEL;
      for (i = 0; i < width/2; i++)
      {
         baseAddress[i] = PIXEL;
	     baseAddress[(width * height) - 1 - i] = PIXEL;
      }
      for (i = 0; i < height/2; i++)
      {
         baseAddress[width * i] = PIXEL;
         baseAddress[(width * height) - 1 - width * i] = PIXEL;
      }
      CoreSurfaceBufferUnlock(screenSurface);
#endif

   }	
   return self; 
}

- (void)dealloc
{
   CGContextRelease(bitmapContext); //flsobral@tc126: release last reference to bitmapContext
   [ screenLayer release ];
   [ super dealloc ];
}

- (CoreSurfaceBufferRef)getSurface
{
   return screenSurface;
}

- (unsigned short*)getPixels
{
   return screenBuffer;
}

- (CGContextRef)getBitmap
{
   return bitmapContext;
}

- (bool)isPortrait
{
   return ![ self isLandscape ];
}

- (bool)isLandscape
{
   return (orientation == kOrientationHorizontalLeft || orientation == kOrientationHorizontalRight);
}

- (void)drawRect:(CGRect)frame
{
   if (getRomVersion() >= 320)
   {
      cgImage = CGBitmapContextCreateImage(bitmapContext);
      [ screenLayer setContents: (id)cgImage ];
      CGImageRelease(cgImage); //flsobral@tc126: using CGImageRelease instead of CFRelease. Not sure if this makes any difference, just thought it would be better to use the method designed specifically for this object.
   }
}

- (void)fixCoord: (CGPoint*)p
{
#ifndef darwin9
   float tmp;
   switch (orientation)
   {
      case kOrientationVerticalUpsideDown:
         p->x = width - p->x;
         p->y = height - p->y;
         DEBUG2("UPSIDE_DOWN x=%d y=%d\n", (int)p->x, (int)p->y);
         break;
      case kOrientationHorizontalLeft:
         tmp = p->x;
         p->x = p->y;
         p->y = height - tmp;
         DEBUG2("LEFT x=%d y=%d\n", (int)p->x, (int)p->y);
         break;
      case kOrientationHorizontalRight:
         tmp = p->x;
         p->x = width - p->y;
         p->y = tmp;
         DEBUG2("RIGHT x=%d y=%d\n", (int)p->x, (int)p->y);
         break;
   }
#endif   
}

#ifdef darwin9

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
   DEBUG0("touch BEGIN");
   
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
	  if (touch != nil && touch.phase == UITouchPhaseBegan)
      {
         CGPoint point = [touch locationInView: self];
         [ self fixCoord: &point ];
	     DEBUG2("down: x=%d, y=%d\n", (int)point.x, (int)point.y);
	     [ self addEvent:
	        [[NSDictionary alloc] initWithObjectsAndKeys:
	           @"mouseDown", @"type",
	           [NSNumber numberWithInt:(int)point.x], @"x",
	           [NSNumber numberWithInt:(int)point.y], @"y",
	           nil
	        ]
	     ];
	  }
   }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
   DEBUG0("touch MOVE");
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
	  if (touch != nil && touch.phase == UITouchPhaseMoved)
      {
         CGPoint point = [touch locationInView: self];
         [ self fixCoord: &point ];
	     DEBUG2("move: x=%d, y=%d\n", (int)point.x, (int)point.y);
	 
	     [ self addEvent:
	        [[NSDictionary alloc] initWithObjectsAndKeys:
	           @"mouseMoved", @"type",
	           [NSNumber numberWithInt:(int)point.x], @"x",
	           [NSNumber numberWithInt:(int)point.y], @"y",
	           nil
	        ]
	     ];
	  }
   }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
   DEBUG0("touch END");
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
	  if (touch != nil && touch.phase == UITouchPhaseEnded)
      {
         CGPoint point = [touch locationInView: self];
         [ self fixCoord: &point ];
	     DEBUG2("up: x=%d, y=%d\n", (int)point.x, (int)point.y);
	 
//todo@ temp manual rotation
if (orientation == kOrientationHorizontalLeft || orientation == kOrientationHorizontalRight && point.y > 280)
   orientationChanged();
else if (point.y > 430)
   orientationChanged();
	 
	     [ self addEvent:
	        [[NSDictionary alloc] initWithObjectsAndKeys:
	           @"mouseUp", @"type",
	           [NSNumber numberWithInt:(int)point.x], @"x",
	           [NSNumber numberWithInt:(int)point.y], @"y",
	           nil
	        ]
	     ];
	  }
   }
}

- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event
{
   DEBUG0("touch CANCEL");
}

#else //darwin9

- (void)gestureChanged:(GSEvent*)event
{
   DEBUG0("gesture changed");
}

- (void)gestureEnded:(GSEvent*)event
{
   DEBUG0("gesture ended");
}

- (void)gestureStarted:(GSEvent*)event
{
   DEBUG0("gesture started");
}

- (void)keyDown:(GSEvent*)event
{
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"keyDown", @"type",
       nil
      ]
   ];
}

- (void)keyUp:(GSEvent*)event
{
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
        @"keyUp", @"type",
       nil
      ]
   ];
}

- (void)mouseDown:(GSEvent*)event
{
   struct CGPoint point = GSEventGetLocationInWindow(event);
   [ self fixCoord: &point ];
   DEBUG2("down: x=%d, y=%d\n", (int)point.x, (int)point.y);
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"mouseDown", @"type",
       [NSNumber numberWithInt:(int)point.x], @"x",
       [NSNumber numberWithInt:(int)point.y], @"y",
       nil
      ]
   ];
}

- (void)mouseDragged:(GSEvent*)event
{
   struct CGPoint point = GSEventGetLocationInWindow(event);
   [ self fixCoord: &point ];
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"mouseDragged", @"type",
       [NSNumber numberWithInt:(int)point.x], @"x",
       [NSNumber numberWithInt:(int)point.y], @"y",
       nil
      ]
   ];
}

- (void)mouseEntered:(GSEvent*)event
{
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"mouseEntered", @"type",
       nil
      ]
   ];
}

- (void)mouseExited:(GSEvent*)event
{
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"mouseExited", @"type",
       nil
      ]
   ];
}

- (void)mouseMoved:(GSEvent*)event
{
   struct CGPoint point = GSEventGetLocationInWindow(event);
   [ self fixCoord: &point ];
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"mouseMoved", @"type",
       [NSNumber numberWithInt:(int)point.x], @"x",
       [NSNumber numberWithInt:(int)point.y], @"y",
       nil
      ]
   ];
}

- (void)mouseUp:(GSEvent*)event
{
   struct CGPoint point = GSEventGetLocationInWindow(event);
   [ self fixCoord: &point ];
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"mouseUp", @"type",
       [NSNumber numberWithInt:(int)point.x], @"x",
       [NSNumber numberWithInt:(int)point.y], @"y",
       nil
      ]
   ];
}

#endif //darwin9

- (void)screenChange:(int)w height:(int)h 
{
   DEBUG2("screen rotated event: %d x %d\n", w, h);
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"screenChange", @"type",
       [NSNumber numberWithInt:w], @"width",
       [NSNumber numberWithInt:h], @"height",
       nil
      ]
   ];
}

- (void)addEvent:(NSDictionary*)event;
{
   [(MainView*)[self superview] addEvent: event ];
}

void iphone_postEvent(int type)
{
   DEBUG1("iphone_postEvent: %d\n", type);
   [ DEVICE_CTX->_mainview addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"screenChanged", @"type",
       [NSNumber numberWithInt:0], @"x",
       [NSNumber numberWithInt:0], @"y",
       nil
      ]
   ];
}

void iphone_postEditUpdate(id control, NSString *str)
{
   DEBUG2("iphone_postEditUpdate: %x,%x\n", (int)control, str);
   [ DEVICE_CTX->_mainview addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"updateEdit", @"type",
       [NSNumber numberWithInt: (int)control], @"control",
       str, @"value",
       nil
      ]
   ];
}

@end
