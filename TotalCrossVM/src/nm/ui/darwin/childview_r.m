#define Object NSObject*
#import "childview.h"
#import "mainview.h"

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
   pitch = width * 4;
   
   self = [ super initWithFrame: rect ];
   if (self != nil )
   {
      unsigned short* screenBuffer = (unsigned short*)createPixelsBuffer(width, height);
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
      [self setClearsContextBeforeDrawing:NO];
   }  
   return self; 
}

- (void)invalidateScreen:(void*)vscreen
{
   ScreenSurface screen = (ScreenSurface)vscreen;
   shiftY = screen->shiftY;
   
   CGRect r = CGRectMake(screen->dirtyX1,screen->dirtyY1 + shiftY,screen->dirtyX2-screen->dirtyX1,screen->dirtyY2-screen->dirtyY1);
   NSInvocation *redrawInv = [NSInvocation invocationWithMethodSignature:
   [self methodSignatureForSelector:@selector(setNeedsDisplayInRect:)]];
   [redrawInv setTarget:self];
   [redrawInv setSelector:@selector(setNeedsDisplayInRect:)];
   [redrawInv setArgument:&r atIndex:2];
   [redrawInv retainArguments];
   [redrawInv performSelectorOnMainThread:@selector(invoke) withObject:nil waitUntilDone:YES];
}    

- (void)drawRect:(CGRect)frame
{    
    int w = width;
    int h = height;
   if (shiftY != 0 && self.layer.frame.origin.y != -shiftY)
      [self setFrame: CGRectMake(0, -shiftY, w, h)];
   else
   if (shiftY == 0 && self.frame.origin.y < 0)
      [self setFrame: CGRectMake(0, 0, width, height)];
            
   cgImage = CGBitmapContextCreateImage(bitmapContext);
   CGContextRef context = UIGraphicsGetCurrentContext();
   CGContextSaveGState(context);
   CGContextClipToRect(context, frame);
   switch (orientation)
   {
      case UIDeviceOrientationPortrait:
      case UIDeviceOrientationLandscapeLeft:
      case UIDeviceOrientationLandscapeRight:
         CGContextTranslateCTM(context, 0, height);
         CGContextScaleCTM(context, 1, -1);
         break;
      case UIDeviceOrientationPortraitUpsideDown:
         CGContextTranslateCTM(context, 0,height);
         CGContextRotateCTM(context, -M_PI);
         CGContextScaleCTM(context, -1, 1);
         break;
   }
   CGContextDrawImage(context, CGRectMake(0, 0, width,height), cgImage);
   CGImageRelease(cgImage);
   CGContextRestoreGState(context);
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
      if (touch != nil && touch.phase == UITouchPhaseBegan)
      {
         lastEventTS = getTimeStamp();
         CGPoint point = [touch locationInView: self];
         [ self addEvent:
           [[NSDictionary alloc] initWithObjectsAndKeys:
              @"mouseDown", @"type",
              [NSNumber numberWithInt:(int)point.x], @"x",
              [NSNumber numberWithInt:(int)point.y-shiftY], @"y",
              nil
           ]
        ];
      }
   }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
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
        [ self addEvent:
           [[NSDictionary alloc] initWithObjectsAndKeys:
              @"mouseMoved", @"type",
              [NSNumber numberWithInt:(int)point.x], @"x",
              [NSNumber numberWithInt:(int)point.y-shiftY], @"y",
              nil
           ]
        ];
     }
   }
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
      if (touch != nil && touch.phase == UITouchPhaseEnded)
      {
         CGPoint point = [touch locationInView: self];
         [ self addEvent:
           [[NSDictionary alloc] initWithObjectsAndKeys:
              @"mouseUp", @"type",
              [NSNumber numberWithInt:(int)point.x], @"x",
              [NSNumber numberWithInt:(int)point.y-shiftY], @"y",
              nil
           ]
         ];
      }
   }
}

- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event
{
}

- (void)screenChange:(int)w height:(int)h 
{
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
