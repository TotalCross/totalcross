#define Object NSObject*
#import "childview.h"
#import "mainview.h"

@implementation ChildView

ScreenSurface gscreen;
int getTimeStamp();
extern CGContextRef bitmapContextW,bitmapContextH;
extern int statusbarHeight;

- (id)initIt:(UIViewController*) ctrl
{                                    
   controller = ctrl;
   self = [ super init ];
   if (self != nil )
   {
      [self setOpaque:YES];
      [self setClearsContextBeforeDrawing:NO];
      [self setContentMode:UIViewContentModeRedraw];
      [self setUserInteractionEnabled:YES];
      [self setClipsToBounds:NO];
   }  
   return self; 
}

- (void)updateScreen: (void*)scr
{
   ScreenSurface screen = gscreen = scr;
   screen->screenW = self.frame.size.width;
   screen->screenH = self.frame.size.height;
   screen->pitch = screen->screenW*4;
   screen->bpp = 32;
}

static int lastOrientation;
- (void)drawRect:(CGRect)frame
{
   int w = self.frame.size.width;
   int h = self.frame.size.height;
   // when rotated, the UIViewController still thinks that we want to draw it horizontally, so we invert the size.
   int orientation = [[UIDevice currentDevice] orientation];
   if (orientation == UIDeviceOrientationUnknown || orientation == UIDeviceOrientationFaceDown || orientation == UIDeviceOrientationFaceUp)
      orientation = lastOrientation;
   lastOrientation = orientation;
   bool landscape = orientation == UIDeviceOrientationLandscapeLeft || orientation == UIDeviceOrientationLandscapeRight;
   if (landscape && w < h)
   {
      int temp = w; w = h; h = temp;
   }
   if (w != clientW)
   {
      bool first = clientW == 0;
      clientW = w;
      if (!first)
      {
         [self updateScreen: gscreen];
         [self screenChange: w height:h ];
         return;       
      }
   }
/*
   if (shiftY != 0 && self.layer.frame.origin.y != (-shiftY+statusbarHeight))
      [self setFrame: CGRectMake(0, -shiftY+statusbarHeight, w,h)];
   else
   if (shiftY == 0 && self.frame.origin.y < 0)
      [self setFrame: CGRectMake(0, statusbarHeight, w,h)];
*/
   CGImageRef cgImage = CGBitmapContextCreateImage(h > w ? bitmapContextW : bitmapContextH);
   CGContextRef context = UIGraphicsGetCurrentContext();
   CGContextSaveGState(context);
   CGContextClipToRect(context, frame);
   CGContextTranslateCTM(context, 0, h);
   CGContextScaleCTM(context, 1, -1);
   CGContextDrawImage(context, CGRectMake(0, 0, w,h), cgImage);
   CGImageRelease(cgImage);
   CGContextRestoreGState(context);
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

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
      if (touch != nil && touch.phase == UITouchPhaseBegan)
      {
         lastEventTS = getTimeStamp();
         CGPoint point = [touch locationInView: self];
         [ (MainView*)controller addEvent:
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
        [ (MainView*)controller addEvent:
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
         [ (MainView*)controller addEvent:
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
   [ (MainView*)controller addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"screenChange", @"type",
       [NSNumber numberWithInt:w], @"width",
       [NSNumber numberWithInt:h], @"height",
       nil
      ]
   ];
}

@end
