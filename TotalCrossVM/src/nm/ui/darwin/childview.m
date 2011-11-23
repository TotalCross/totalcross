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
   screen->bpp = 32;
}

extern int statusbar_height;
char* createPixelsBuffer(int width, int height);

- (id)initWithFrame:(CGRect)rect orientation:(int)orient
{                                                       
   orientation = orient;
   width = rect.size.width;
   height = rect.size.height;
   
   DEBUG4("CHILDVIEW: %dx%d,%dx%d\n", (int)rect.origin.x, (int)rect.origin.y, (int)rect.size.width, (int)rect.size.height);

   self = [ super initWithFrame: rect ];
   if (self != nil )
   {
      pitch = width * 4;
      int size = pitch * height;

      screenBuffer = createPixelsBuffer(width+statusbar_height,height+statusbar_height);
      CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
      bitmapContext = CGBitmapContextCreate(
            screenBuffer,
            width,
            height,
            8,     // bitsPerComponent
            pitch, // bytesPerRow
            colorSpace,
            kCGImageAlphaNoneSkipLast | kCGBitmapByteOrder32Little);
      CFRelease(colorSpace);

      [self setOpaque:YES];
   }  
   return self; 
}

- (void)dealloc
{
   CGContextRelease(bitmapContext); //flsobral@tc126: release last reference to bitmapContext
//   [ screenLayer release ];
   [ super dealloc ];
}

/*- (CoreSurfaceBufferRef)getSurface
{
   return screenSurface;
}

- (unsigned short*)getPixels
{
   return screenBuffer;
}*/

- (void)invalidateScreen:(void*)vscreen
{
   ScreenSurface screen = (ScreenSurface)vscreen;
   shiftY = screen->shiftY;
   CGRect r = CGRectMake(screen->dirtyX1,screen->dirtyY1,screen->dirtyX2-screen->dirtyX1,screen->dirtyY2-screen->dirtyY1);
   
   NSInvocation *redrawInv = [NSInvocation invocationWithMethodSignature:
   [self methodSignatureForSelector:@selector(setNeedsDisplayInRect:)]];
   [redrawInv setTarget:self];
   [redrawInv setSelector:@selector(setNeedsDisplayInRect:)];
   [redrawInv setArgument:&r atIndex:2];
   [redrawInv retainArguments];
   [redrawInv performSelectorOnMainThread:@selector(invoke)
   withObject:nil waitUntilDone:YES];
}    

- (void)drawRect:(CGRect)frame
{                            
/*   if (shiftY != 0 && self.layer.frame.origin.y != -shiftY)
      [self setFrame: CGRectMake(0, -shiftY, width+1, height+1)];
   else
   if (shiftY == 0 && self.frame.origin.y < 0)
      [self setFrame: CGRectMake(0, 0, width+1, height+1)];
   */
   //debug("frame: %d %d %d %d",(int)frame.origin.x, (int)frame.origin.y, (int)frame.size.width, (int)frame.size.height);
   cgImage = CGBitmapContextCreateImage(bitmapContext);
   CGContextRef context = UIGraphicsGetCurrentContext();
   CGContextSaveGState(context);
   switch (orientation)
   {                       
      case kOrientationVertical:
         CGContextTranslateCTM(context, 0, height);
         CGContextScaleCTM(context, 1, -1);
         break;
      case kOrientationHorizontalLeft:
         CGContextRotateCTM(context, M_PI / 2);
         CGContextTranslateCTM(context, 0, -width);
         break;
      case kOrientationHorizontalRight: 
         CGContextRotateCTM(context, - M_PI / 2);
         CGContextTranslateCTM(context, -height, 0);
         break;
      case kOrientationVerticalUpsideDown:
         CGContextTranslateCTM(context, width,height);
         CGContextRotateCTM(context, -M_PI);
/*         CGContextTranslateCTM(context, 0, height);
         CGContextScaleCTM(context, 1, -1);
         CGContextTranslateCTM(context, 0, -height);
         CGContextRotateCTM(context, M_PI);
         CGContextTranslateCTM(context, -width, -height);*/
//         CGContextRotateCTM(context, M_PI);
//            CGContextScaleCTM(context, 1, -1);
//            CGContextTranslateCTM(context, width, 0);
         //CGContextTranslateCTM(context, width, height);
         //CGContextRotateCTM(context, -M_PI);
         break;
   }
   //CGContextClipToRect(context, frame);
   CGContextDrawImage(context, CGRectMake(0, 0, width,height/*min32(width, height), max32(width, height)*/), cgImage);
   CGImageRelease(cgImage);
   CGContextRestoreGState(context);
/*
   CGSize size = CGSizeMake(width, height);
   //create the rect zone that we draw from the image
   CGRect imageRect;
   
   if (orientation==kOrientationVertical || orientation==kOrientationVerticalUpsideDown) 
      imageRect = CGRectMake(0, 0, width, height); 
   else 
      imageRect = CGRectMake(0, 0, height, width); 
   
   //UIGraphicsBeginImageContext(size);
   CGContextRef context = UIGraphicsGetCurrentContext();
   //Save current status of graphics context
   CGContextSaveGState(context);
   
   //Do stupid stuff to draw the image correctly
   CGContextTranslateCTM(context, 0, height);
   CGContextScaleCTM(context, 1.0, -1.0);
   
/*   switch (orientation)
   {
      case kOrientationHorizontalLeft:
         CGContextRotateCTM(context, M_PI / 2);
         CGContextTranslateCTM(context, 0, -width);
         break;
      case kOrientationHorizontalRight: 
         CGContextRotateCTM(context, - M_PI / 2);
         CGContextTranslateCTM(context, -height, 0);
         break;
      case kOrientationVerticalUpsideDown:
         CGContextTranslateCTM(context, width, height);
         CGContextRotateCTM(context, M_PI);
         break;
   }*/
  /*
   cgImage = CGBitmapContextCreateImage(bitmapContext);
   //CGContextClipToRect(context, frame);
   CGContextDrawImage(context, imageRect, cgImage);
   CGImageRelease(cgImage);
   
   //After drawing the image, roll back all transformation by restoring the old context
   CGContextRestoreGState(context);
   //get the image from the graphic context
   //UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
   //commit all drawing effects
   UIGraphicsEndImageContext();   */
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
   DEBUG0("touch BEGIN");
   
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
      if (touch != nil && touch.phase == UITouchPhaseBegan)
      {
         lastEventTS = getTimeStamp();
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
        // ignore events if sent too fast
        int ts = getTimeStamp();
        if ((ts-lastEventTS) < 50)
           return;
        lastEventTS = ts;
        
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
