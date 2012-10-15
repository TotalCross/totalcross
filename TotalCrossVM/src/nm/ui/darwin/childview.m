#define Object NSObject*
#import "childview.h"
#import "mainview.h"

@implementation ChildView

static ScreenSurface gscreen;
int getTimeStamp();
char* createPixelsBuffer(int width, int height);
int realAppH;
extern int appW,appH;
void Sleep(int ms);
extern BOOL callingScreenChange;

+ (Class)layerClass 
{
   return [CAEAGLLayer class];
}

bool setupGL(int width, int height);

- (id)init:(UIViewController*) ctrl
{                                    
   controller = ctrl;
   self = [ super init ];
   if (self != nil )
   {
      [self setOpaque:YES];
      //  self.contentScaleFactor = [UIScreen mainScreen].scale;
   }  
   return self; 
}

- (void)setScreenValues: (void*)scr
{
   ScreenSurface screen = gscreen = scr;
   int scale = ([[UIScreen mainScreen] respondsToSelector:@selector(displayLinkWithTarget:selector:)] && ([UIScreen mainScreen].scale == 2.0)) ?2:1;
   screen->screenW = self.frame.size.width * scale;
   screen->screenH = self.frame.size.height * scale;
   screen->pitch = screen->screenW*4;
   screen->bpp = 32;
}

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
      //if (cgImage != null) CGImageRelease(cgImage);
      //cgImage = CGImageCreate(w, h, 8, 32, w*4, colorSpace, kCGImageAlphaNoneSkipLast|kCGBitmapByteOrder32Little, provider, NULL, false, kCGRenderingIntentDefault);
      if (clientW != 0)
      {
         callingScreenChange = true;
         [self setScreenValues: gscreen];
         [ (MainView*)controller addEvent: [[NSDictionary alloc] initWithObjectsAndKeys: 
           @"screenChange", @"type", [NSNumber numberWithInt:w], @"width", [NSNumber numberWithInt:h], @"height", nil] ];         
         while (callingScreenChange)
            Sleep(10); // let these 2 events be processed - use Sleep, not sleep. 10, not 1.
      }
   }
   clientW = w;
   // CGContext: 6.5s; CGLayer: 3.5s
/*   CGSize s = CGSizeMake(w,h);
   CGContextRef context = UIGraphicsGetCurrentContext();
   CGLayerRef layer = CGLayerCreateWithContext(context, s, NULL);
   
   CGContextRef layerContext = CGLayerGetContext(layer);
   CGContextTranslateCTM(layerContext, 0, h-shiftY);
   CGContextScaleCTM(layerContext, 1, -1);
   CGContextDrawImage(layerContext, (CGRect){ CGPointZero, s }, cgImage);
   CGContextDrawLayerAtPoint(context, CGPointZero, layer);
   CGLayerRelease(layer);*/
}

- (void)graphicsSetup
{
   bool ok;
   CAEAGLLayer *eaglLayer = (CAEAGLLayer *)self.layer;
   eaglLayer.opaque = TRUE;
   glcontext = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
   ok = [EAGLContext setCurrentContext:glcontext];
   // Create default framebuffer object. The backing will be allocated for the current layer in -resizeFromLayer
   glGenFramebuffers(1, &defaultFramebuffer);
   glGenRenderbuffers(1, &colorRenderbuffer);
   glBindFramebuffer(GL_FRAMEBUFFER, defaultFramebuffer);
   glBindRenderbuffer(GL_RENDERBUFFER, colorRenderbuffer);
   glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRenderbuffer);

   [glcontext renderbufferStorage:GL_RENDERBUFFER fromDrawable:eaglLayer];
   int stat = glCheckFramebufferStatus(GL_FRAMEBUFFER);
   if (stat != GL_FRAMEBUFFER_COMPLETE)
      NSLog(@"Failed to make complete framebuffer object %x", stat);
   setupGL(gscreen->screenW,gscreen->screenH);
   glClearColor(1,1,1,1); glClear(GL_COLOR_BUFFER_BIT);
   realAppH = appH;
}
- (void)invalidateScreen:(void*)vscreen
{
   [glcontext presentRenderbuffer:GL_RENDERBUFFER];
   glClearColor(1,1,1,1); glClear(GL_COLOR_BUFFER_BIT);
   {
   /*ScreenSurface screen = (ScreenSurface)vscreen;
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
   [redrawInv performSelectorOnMainThread:@selector(invoke) withObject:nil waitUntilDone:YES];*/
   }
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
