#define Object NSObject*
#import "childview.h"
#import "mainview.h"

@implementation ChildView

static ScreenSurface gscreen;
int getTimeStamp();
char* createPixelsBuffer(int width, int height);
int realAppH;

- (id)init:(UIViewController*) ctrl
{                                    
   controller = ctrl;
   self = [ super init ];
   if (self != nil )
   {
      // initialize the screen bitmap with the full width and height
      CGRect rect = [[UIScreen mainScreen] bounds];
      int w = rect.size.width, h = rect.size.height;
      int s = w > h ? w : h;
      screenBuffer = (char*)createPixelsBuffer(s, s);
      colorSpace = CGColorSpaceCreateDeviceRGB();
      provider = CGDataProviderCreateWithData(NULL, screenBuffer, 4*w*h, NULL);
      [self setOpaque:YES];
      [self setClearsContextBeforeDrawing:NO];
      [self setClipsToBounds:NO];
      //[self setContentMode:UIViewContentModeRedraw];
      UIPinchGestureRecognizer *pinchGesture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
      [self addGestureRecognizer:pinchGesture];
      [pinchGesture release];
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

void Sleep(int ms);
BOOL invalidated;

extern BOOL callingScreenChange;

- (void)drawRect:(CGRect)frame
{
   // when rotated, the UIViewController still thinks that we want to draw it horizontally, so we invert the size.
   int orientation = [[UIDevice currentDevice] orientation];
   if (orientation == UIDeviceOrientationUnknown || orientation == UIDeviceOrientationFaceDown || orientation == UIDeviceOrientationFaceUp)
      orientation = lastOrientation;
   lastOrientation = orientation;
   bool landscape = orientation == UIDeviceOrientationLandscapeLeft || orientation == UIDeviceOrientationLandscapeRight;
   int w = self.frame.size.width;
   int h = self.frame.size.height;
   if (landscape && w < h)
   {
      int temp = w; w = h; h = temp;
   }
   if (w != clientW)
   {
      realAppH = h;
      if (cgImage != null) CGImageRelease(cgImage);
      cgImage = CGImageCreate(w, h, 8, 32, w*4, colorSpace, kCGImageAlphaNoneSkipLast|kCGBitmapByteOrder32Little, provider, NULL, false, kCGRenderingIntentDefault);
      if (clientW != 0)
      {
         callingScreenChange = true;
         [self updateScreen: gscreen];
         [ (MainView*)controller addEvent: [[NSDictionary alloc] initWithObjectsAndKeys: 
           @"screenChange", @"type", [NSNumber numberWithInt:w], @"width", [NSNumber numberWithInt:h], @"height", nil] ];         
         while (callingScreenChange)
            Sleep(10); // let these 2 events be processed - use Sleep, not sleep. 10, not 1.
      }
   }
   clientW = w;
   // CGContext: 6.5s; CGLayer: 3.5s
   CGSize s = CGSizeMake(w,h);
   CGContextRef context = UIGraphicsGetCurrentContext();
   CGLayerRef layer = CGLayerCreateWithContext(context, s, NULL);
   
   CGContextRef layerContext = CGLayerGetContext(layer);
   CGContextTranslateCTM(layerContext, 0, h-shiftY);
   CGContextScaleCTM(layerContext, 1, -1);
   CGContextDrawImage(layerContext, (CGRect){ CGPointZero, s }, cgImage);
   CGContextDrawLayerAtPoint(context, CGPointZero, layer);
   CGLayerRelease(layer);
   invalidated = TRUE;
}

void getDirtyFromContext(void* context, int* dirtyX1, int* dirtyY1, int* dirtyX2, int* dirtyY2);

- (void)invalidateScreen:(void*)vscreen withContext:(void*)context
{
   ScreenSurface screen = (ScreenSurface)vscreen;
   int dirtyX1,dirtyY1,dirtyX2,dirtyY2;
   getDirtyFromContext(context, &dirtyX1,&dirtyY1,&dirtyX2,&dirtyY2);
   
   shiftY = screen->shiftY;
   
   CGRect r = CGRectMake(dirtyX1,dirtyY1,dirtyX2-dirtyX1,dirtyY2-dirtyY1);
   NSInvocation *redrawInv = [NSInvocation invocationWithMethodSignature:
                              [self methodSignatureForSelector:@selector(setNeedsDisplayInRect:)]];
   [redrawInv setTarget:self];
   [redrawInv setSelector:@selector(setNeedsDisplayInRect:)];
   [redrawInv setArgument:&r atIndex:2];
   [redrawInv retainArguments];
   [redrawInv performSelectorOnMainThread:@selector(invoke) withObject:nil waitUntilDone:YES];
}    

- (void)processEvent:(NSSet *)touches withEvent:(UIEvent *)event
{
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
      if (touch != nil && (touch.phase == UITouchPhaseBegan || touch.phase == UITouchPhaseMoved || touch.phase == UITouchPhaseEnded))
      {
         int ts = getTimeStamp();
         if (touch.phase == UITouchPhaseMoved && (ts-lastEventTS) < 20) // ignore events if sent too fast
            return;
         lastEventTS = ts;
         CGPoint point = [touch locationInView: self];
         [ (MainView*)controller addEvent:
          [[NSDictionary alloc] initWithObjectsAndKeys:
           touch.phase == UITouchPhaseBegan ? @"mouseDown" : touch.phase == UITouchPhaseMoved ? @"mouseMoved" : @"mouseUp", @"type",
           [NSNumber numberWithInt:(int)point.x], @"x",
           [NSNumber numberWithInt:(int)point.y], @"y", nil]
          ];
      }
   }
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
	if ( [gestureRecognizer isKindOfClass:[UIPinchGestureRecognizer class]] )
      [ (MainView*)controller addEvent:
       [[NSDictionary alloc] initWithObjectsAndKeys:
        @"multitouchScale", @"type",
        [NSNumber numberWithInt:(int)1], @"key",
        [NSNumber numberWithInt:(int)0], @"x",
        [NSNumber numberWithInt:(int)0], @"y",
        nil]
       ];
	return YES;
}

- (BOOL)gestureRecognizerShouldEnd:(UIGestureRecognizer *)gestureRecognizer
{
	if ( [gestureRecognizer isKindOfClass:[UIPinchGestureRecognizer class]] )
      [ (MainView*)controller addEvent:
       [[NSDictionary alloc] initWithObjectsAndKeys:
        @"multitouchScale", @"type",
        [NSNumber numberWithInt:(int)2], @"key",
        [NSNumber numberWithInt:(int)0], @"x",
        [NSNumber numberWithInt:(int)0], @"y",
        nil]
       ];
	return YES;
}

-(void)handlePinch:(UIPinchGestureRecognizer*)sender
{
   double dscale = sender.scale;
   int *iscale = (int*)&dscale;
   [ (MainView*)controller addEvent:
     [[NSDictionary alloc] initWithObjectsAndKeys:
      @"multitouchScale", @"type",
      [NSNumber numberWithInt:(int)0], @"key",
      [NSNumber numberWithInt:(int)iscale[0]], @"x",
      [NSNumber numberWithInt:(int)iscale[1]], @"y",
      nil]
   ];
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
   [self processEvent: touches withEvent:event];
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
   [self processEvent: touches withEvent:event];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
   [self processEvent: touches withEvent:event];
}

@end
