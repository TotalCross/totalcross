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

char* createPixelsBuffer(int width, int height);

- (id)initWithFrame:(CGRect)rect orientation:(int)orient
{                                                       
   orientation = orient;
   width = rect.size.width;
   height = rect.size.height;
   
   int romVersion = getRomVersion();
   DEBUG4("CHILDVIEW: %dx%d,%dx%d\n", (int)rect.origin.x, (int)rect.origin.y, (int)rect.size.width, (int)rect.size.height);

   self = [ super initWithFrame: rect ];
   if (self != nil )
   {
      pitch = width * (romVersion >= 320 ? 4 : 2);
      int size = pitch * height;

      if (romVersion >= 320)
      {
         screenBuffer = createPixelsBuffer(width,height);
         CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
         bitmapContext = CGBitmapContextCreate(
               screenBuffer,
               width,
               height,
               8, // bitsPerComponent
               pitch, // bytesPerRow
               colorSpace,
               kCGImageAlphaNoneSkipLast | kCGBitmapByteOrder32Little);
         CFRelease(colorSpace);
      }
      else
      {
         char *_buffer = malloc(height*pitch); // single screen memory buffer
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

extern int globalShiftY;
- (void)drawRect:(CGRect)frame
{
   if (getRomVersion() >= 320)
   {
      int shiftY = globalShiftY;
      if (shiftY != 0) CGContextTranslateCTM(bitmapContext, 0, -shiftY);
      cgImage = CGBitmapContextCreateImage(bitmapContext);
      [ screenLayer setContents: (id)cgImage ];
//      if (shiftY != 0) CGContextTranslateCTM(bitmapContext, 0, shiftY);         
      CGImageRelease(cgImage); //flsobral@tc126: using CGImageRelease instead of CFRelease. Not sure if this makes any difference, just thought it would be better to use the method designed specifically for this object.
   }
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
   DEBUG0("touch BEGIN");
   
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
      if (touch != nil && touch.phase == UITouchPhaseBegan)
      {
         CGPoint point = [touch locationInView: self];
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
