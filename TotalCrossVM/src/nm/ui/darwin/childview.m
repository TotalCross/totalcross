#define Object NSObject*
#import "childview.h"
#import "mainview.h"

@implementation ChildView

static ScreenSurface gscreen;
int getTimeStamp();
int realAppH;
extern int appW,appH;
void Sleep(int ms);
void checkGlError(const char* op, int line);


+ (Class)layerClass 
{
   return [CAEAGLLayer class];
}

bool setupGL(int width, int height);

- (id)init:(UIViewController*) ctrl
{                                    
   self = [ super init ];
   controller = ctrl;
   taskbarHeight = MIN([UIApplication sharedApplication].statusBarFrame.size.width,[UIApplication sharedApplication].statusBarFrame.size.height);
   self.opaque = YES;
   self.contentMode = UIViewContentModeScaleToFill;
   self.contentScaleFactor = [UIScreen mainScreen].scale; // support for high resolution
   UIPinchGestureRecognizer *pinchGesture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
   [self addGestureRecognizer:pinchGesture];
   [pinchGesture release];
   return self;
}

extern int32 deviceFontHeight,iosScale;

- (void)setScreenValues: (void*)scr
{
   ScreenSurface screen = gscreen == null ? gscreen = scr : gscreen;
   iosScale = [UIScreen mainScreen].scale;
   screen->screenW = self.bounds.size.width *iosScale;
   screen->screenH = self.bounds.size.height*iosScale;
   screen->pitch = screen->screenW*4;
   screen->bpp = 32;
   screen->pixels = (uint8*)1;
   deviceFontHeight = [UIFont labelFontSize] * iosScale;
}

- (void)doRotate
{
   [self createGLcontext]; // recreate buffers at the new screen layout
}

void graphicsSetupIOS()
{
   [EAGLContext setCurrentContext:DEVICE_CTX->_childview->glcontext];
}

extern int lastOrientationIsPortrait;
void recreateTextures();

- (CGRect)getBounds
{
   CGRect r = [[UIScreen mainScreen] bounds];
   if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7)
   {
      switch ([[UIDevice currentDevice] orientation])
      {
         case UIDeviceOrientationPortraitUpsideDown:
            r.origin.y = 0;
            r.size.height -= taskbarHeight;
            break;
         case UIDeviceOrientationLandscapeLeft:
            r.origin.x = 0;
            r.size.width  -= taskbarHeight;
            break;
         case UIDeviceOrientationLandscapeRight:
            r.origin.x = taskbarHeight;
            r.size.width  -= taskbarHeight;
            break;
         default: // UIDeviceOrientationPortrait and others
            r.origin.y = taskbarHeight;
            r.size.height -= taskbarHeight;
            break;
      }
   }
   return r;
}

- (void)createGLcontext
{
   self.frame = [self getBounds];
   CAEAGLLayer *eaglLayer = (CAEAGLLayer *)self.layer;
   eaglLayer.opaque = TRUE;
   if (glcontext != null)
   {
      glDeleteFramebuffers(1, &defaultFramebuffer);
      glDeleteRenderbuffers(1, &colorRenderbuffer);
      [glcontext release];
   }
   glcontext = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
   [EAGLContext setCurrentContext:glcontext];
   glGenFramebuffers(1, &defaultFramebuffer); GL_CHECK_ERROR
   glGenRenderbuffers(1, &colorRenderbuffer); GL_CHECK_ERROR
   // Create default framebuffer object. The backing will be allocated for the current layer in -resizeFromLayer
   glBindFramebuffer(GL_FRAMEBUFFER, defaultFramebuffer); GL_CHECK_ERROR
   glBindRenderbuffer(GL_RENDERBUFFER, colorRenderbuffer); GL_CHECK_ERROR
   glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRenderbuffer); GL_CHECK_ERROR

   [glcontext renderbufferStorage:GL_RENDERBUFFER fromDrawable:eaglLayer];
   int stat = glCheckFramebufferStatus(GL_FRAMEBUFFER);
   if (stat != GL_FRAMEBUFFER_COMPLETE)
      NSLog(@"Failed to make complete framebuffer object %x", stat);
   setupGL(self.bounds.size.width*2,self.bounds.size.height*2);
   realAppH = appH;
   recreateTextures();
}

- (void)updateScreen
{
   [glcontext presentRenderbuffer:GL_RENDERBUFFER];
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
         [ (MainViewController*)controller addEvent:
          [[NSDictionary alloc] initWithObjectsAndKeys:
           touch.phase == UITouchPhaseBegan ? @"mouseDown" : touch.phase == UITouchPhaseMoved ? @"mouseMoved" : @"mouseUp", @"type",
           [NSNumber numberWithInt:(int)point.x * iosScale], @"x",
           [NSNumber numberWithInt:(int)point.y * iosScale], @"y", nil]
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

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
	if ( [gestureRecognizer isKindOfClass:[UIPinchGestureRecognizer class]] )
   {
      [ (MainViewController*)controller addEvent:
       [[NSDictionary alloc] initWithObjectsAndKeys:
        @"multitouchScale", @"type",
        [NSNumber numberWithInt:(int)1], @"key",
        [NSNumber numberWithInt:(int)0], @"x",
        [NSNumber numberWithInt:(int)0], @"y",
        nil]
       ];
   }
	return YES;
}

- (BOOL)gestureRecognizerShouldEnd:(UIGestureRecognizer *)gestureRecognizer
{
	if ( [gestureRecognizer isKindOfClass:[UIPinchGestureRecognizer class]] )
      [ (MainViewController*)controller addEvent:
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
   // note: unlike android, that sends the step since the last value, ios sends the actual scale value, as the docs says:
   // The scale value is an absolute value that varies over time. It is not the delta value from the last time that the
   // scale was reported. Apply the scale value to the state of the view when the gesture is first recognized—
   // do not concatenate the value each time the handler is called.
   // -- so we use the velocity to achieve the same results as in android. TODO use sender.scale to compute the steps
   if (sender.velocity == 0 || sender.velocity > 10 || sender.velocity < -10) return;
   double dscale = 1+sender.velocity/100;
   //NSLog(@"scale: %f, real scale: %f, vel: %f",dscale,sender.scale,sender.velocity);
   int *iscale = (int*)&dscale;
   [ (MainViewController*)controller addEvent:
    [[NSDictionary alloc] initWithObjectsAndKeys:
     @"multitouchScale", @"type",
     [NSNumber numberWithInt:(int)0], @"key",
     [NSNumber numberWithInt:(int)iscale[1]], @"x",
     [NSNumber numberWithInt:(int)iscale[0]], @"y",
     nil]
    ];
}

@end
