#define Object NSObject*
#import "childview.h"
#import "mainview.h"
#include <math.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>

// This function is intentionally confined to the diagnostic branch.
// The persisted file is needed because iOS unified logs can redact native
// NSLog values and Appium screenshots cannot diagnose framebuffer state.
void tcIosGraphicsProbe(const char *stage, const char *format, ...)
{
   static volatile int sequence = 0;
   char detail[640];
   va_list args;
   va_start(args, format);
   vsnprintf(detail, sizeof(detail), format, args);
   va_end(args);
   const int seq = __sync_add_and_fetch(&sequence, 1);

   @autoreleasepool
   {
      NSString *line = [NSString stringWithFormat:
          @"TC_IOS_GFX seq=%d time=%.3f mainThread=%d stage=%s %s\n",
          seq, CFAbsoluteTimeGetCurrent(), [NSThread isMainThread], stage, detail];
      const char *record = [line UTF8String];
      if (record != NULL)
      {
         fputs(record, stderr);
         fflush(stderr);
         NSString *dir = [NSHomeDirectory() stringByAppendingPathComponent:@"Documents"];
         [[NSFileManager defaultManager] createDirectoryAtPath:dir
             withIntermediateDirectories:YES attributes:nil error:NULL];
         NSString *file = [dir stringByAppendingPathComponent:@"tc-ios-native-graphics.log"];
         FILE *out = fopen([file fileSystemRepresentation], "a");
         if (out != NULL)
         {
            fputs(record, out);
            fclose(out);
         }
      }
   }
}

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
   firstCall = true;
   controller = ctrl;
   self.opaque = YES;
   self.contentMode = UIViewContentModeScaleToFill;
   self.contentScaleFactor = [UIScreen mainScreen].scale; // support for high resolution
   tcIosGraphicsProbe("child-init", "bounds=%.1fx%.1f scale=%.2f layer=%p",
       self.bounds.size.width, self.bounds.size.height,
       self.contentScaleFactor, self.layer);
   UIPinchGestureRecognizer *pinchGesture = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
   [self addGestureRecognizer:pinchGesture];
   [pinchGesture release];
   return self;
}

extern double screenContentScale;

- (void)setScreenValues: (void*)scr
{
   ScreenSurface screen = gscreen == null ? gscreen = scr : gscreen;
   screenContentScale = [UIScreen mainScreen].scale;
   screen->screenW = (int32)lround(self.bounds.size.width * screenContentScale);
   screen->screenH = (int32)lround(self.bounds.size.height * screenContentScale);
   screen->contentScale = screenContentScale;
   screen->fontScale = 1;
   screen->pitch = screen->screenW*4;
   screen->bpp = 32;
   screen->pixels = (uint8*)1;
   screen->deviceFontHeight = (int32)[UIFont labelFontSize];
   screen->surfaceReady = true;
   tcIosGraphicsProbe("set-screen", "bounds=%.1fx%.1f scale=%.2f screen=%dx%d ready=%d",
       self.bounds.size.width, self.bounds.size.height, screenContentScale,
       (int)screen->screenW, (int)screen->screenH, screen->surfaceReady);
   // if ((deviceFontHeight&1) == 1) deviceFontHeight++; // even size fonts are better
}

void graphicsSetupIOS()
{
   static int calls = 0;
   EAGLContext *context = DEVICE_CTX->_childview->glcontext;
   BOOL selected = [EAGLContext setCurrentContext:context];
   const int call = ++calls;
   if (call <= 16 || call % 100 == 0 || !selected)
      tcIosGraphicsProbe("graphics-setup", "call=%d selected=%d context=%p current=%p",
          call, selected, context, [EAGLContext currentContext]);
}

- (UIDeviceOrientation)getOrientation
{
   int o = [[UIDevice currentDevice] orientation];
   if (o == UIDeviceOrientationFaceDown || o == UIDeviceOrientationFaceUp || o == UIDeviceOrientationUnknown)
      o = lastKnownOrientation;
   else
      lastKnownOrientation = o;
   //debug("orientation: %s",o == UIDeviceOrientationPortrait ? "Portrait" : o == UIDeviceOrientationLandscapeLeft ? "Landscape Left" : o == UIDeviceOrientationLandscapeRight ? "Landscape Right" : o == UIDeviceOrientationPortraitUpsideDown ? "Portrait UpsideDown" : "Unknown");
   return o;
}

- (CGSize)getResolution
{
   UIScreen *screen = [UIScreen mainScreen];
   CGSize viewSize = self.bounds.size;
   CGFloat scale = self.contentScaleFactor > 0 ? self.contentScaleFactor : screen.scale;
   screenContentScale = (double) scale;
   CGSize resolution = CGSizeMake(lround(viewSize.width * scale), lround(viewSize.height * scale));
   return resolution;
}

- (CGSize)resizeGLDrawable
{
   CGSize fallback = [self getResolution];
   if (glcontext == nil)
   {
      tcIosGraphicsProbe("drawable-skip", "no-context bounds=%.1fx%.1f fallback=%.0fx%.0f",
          self.bounds.size.width, self.bounds.size.height,
          fallback.width, fallback.height);
      return fallback;
   }

   BOOL selected = [EAGLContext setCurrentContext:glcontext];
   glBindFramebuffer(GL_FRAMEBUFFER, defaultFramebuffer); GL_CHECK_ERROR
   glBindRenderbuffer(GL_RENDERBUFFER, colorRenderbuffer); GL_CHECK_ERROR
   BOOL allocated = [glcontext renderbufferStorage:GL_RENDERBUFFER fromDrawable:(CAEAGLLayer *)self.layer];

   GLint width = 0;
   GLint height = 0;
   glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH, &width); GL_CHECK_ERROR
   glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT, &height); GL_CHECK_ERROR

   GLenum stat = glCheckFramebufferStatus(GL_FRAMEBUFFER);
   tcIosGraphicsProbe("drawable", "selected=%d storage=%d bounds=%.1fx%.1f fallback=%.0fx%.0f layerBounds=%.1fx%.1f layerScale=%.2f renderbuffer=%dx%d fbStatus=0x%x fb=%u rb=%u",
       selected, allocated,
       self.bounds.size.width, self.bounds.size.height,
       fallback.width, fallback.height,
       self.layer.bounds.size.width, self.layer.bounds.size.height,
       self.layer.contentsScale, (int)width, (int)height,
       (unsigned int)stat, defaultFramebuffer, colorRenderbuffer);
   if (stat != GL_FRAMEBUFFER_COMPLETE)
      NSLog(@"Failed to make complete framebuffer object %x", stat);

   if (width <= 0 || height <= 0)
   {
      width = (GLint)fallback.width;
      height = (GLint)fallback.height;
   }
   glViewport(0, 0, width, height); GL_CHECK_ERROR
   return CGSizeMake(width, height);
}

- (void)createGLcontext
{
   CAEAGLLayer *eaglLayer = (CAEAGLLayer *)self.layer;
   eaglLayer.opaque = TRUE;
   eaglLayer.drawableProperties = [NSDictionary dictionaryWithObjectsAndKeys:
      [NSNumber numberWithBool:YES], kEAGLDrawablePropertyRetainedBacking,
      kEAGLColorFormatRGBA8, kEAGLDrawablePropertyColorFormat,
      nil];
   if (glcontext != null)
   {
      glDeleteFramebuffers(1, &defaultFramebuffer);
      glDeleteRenderbuffers(1, &colorRenderbuffer);
      [glcontext release];
   }
   glcontext = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
   BOOL selected = [EAGLContext setCurrentContext:glcontext];
   tcIosGraphicsProbe("create-context", "context=%p selected=%d bounds=%.1fx%.1f layerBounds=%.1fx%.1f scale=%.2f",
       glcontext, selected, self.bounds.size.width, self.bounds.size.height,
       eaglLayer.bounds.size.width, eaglLayer.bounds.size.height,
       eaglLayer.contentsScale);
   glGenFramebuffers(1, &defaultFramebuffer); GL_CHECK_ERROR
   glGenRenderbuffers(1, &colorRenderbuffer); GL_CHECK_ERROR
   // Create default framebuffer object. The backing will be allocated for the current layer in -resizeFromLayer
   glBindFramebuffer(GL_FRAMEBUFFER, defaultFramebuffer); GL_CHECK_ERROR
   glBindRenderbuffer(GL_RENDERBUFFER, colorRenderbuffer); GL_CHECK_ERROR
   glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRenderbuffer); GL_CHECK_ERROR
   tcIosGraphicsProbe("create-attachments", "fb=%u rb=%u context=%p",
       defaultFramebuffer, colorRenderbuffer, glcontext);

   [self resizeGLDrawable];
   realAppH = appH;
   //invalidateTextures(INVTEX_INVALIDATE);
}

- (void)updateScreen
{
   static int ignoreWhiteBackground = 2;
   static int updateCalls = 0;
   const int call = ++updateCalls;
   const BOOL suppressed = (--ignoreWhiteBackground >= 0);
   const BOOL current = glcontext != nil && [EAGLContext currentContext] == glcontext;
   GLint boundFramebuffer = -1;
   GLint boundRenderbuffer = -1;
   GLenum status = 0;
   if (current)
   {
      glGetIntegerv(GL_FRAMEBUFFER_BINDING, &boundFramebuffer);
      glGetIntegerv(GL_RENDERBUFFER_BINDING, &boundRenderbuffer);
      status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
   }
   // Preserve the existing two-frame suppression and GL context selection.
   BOOL presented = NO;
   if (!suppressed)
      presented = [glcontext presentRenderbuffer:GL_RENDERBUFFER];

   if (call <= 64 || call % 100 == 0 || (!suppressed && !presented))
      tcIosGraphicsProbe("present", "call=%d suppressed=%d presented=%d current=%d context=%p fb=%d rb=%d fbStatus=0x%x bounds=%.1fx%.1f",
          call, suppressed, presented, current, glcontext,
          (int)boundFramebuffer, (int)boundRenderbuffer, (unsigned int)status,
          self.bounds.size.width, self.bounds.size.height);
}

- (void)processEvent:(NSSet *)touches withEvent:(UIEvent *)event
{
   static int touchCalls = 0;
   int call = ++touchCalls;
   if ([ touches count ] != 1 && call <= 32)
      tcIosGraphicsProbe("touch-count", "call=%d touches=%d",
          call, (int)[touches count]);
   if ([ touches count ] == 1)
   {
      UITouch *touch = [ touches anyObject ];
      if (touch != nil && (touch.phase == UITouchPhaseBegan || touch.phase == UITouchPhaseMoved || touch.phase == UITouchPhaseEnded))
      {
         if (isMultitouching)
            [self gestureShouldEnd];
         if (touch.phase == UITouchPhaseMoved && [(MainViewController*)controller hasEvents]) // ignore move events if the last one was not yet consumed
            return;
         CGPoint point = [touch locationInView: self];
         int nativeX = (int)lround(point.x * screenContentScale);
         int nativeY = (int)lround(point.y * screenContentScale);
         if (call <= 64 || touch.phase != UITouchPhaseMoved)
            tcIosGraphicsProbe("touch", "call=%d phase=%d local=%.1f,%.1f vm=%d,%d scale=%.2f bounds=%.1fx%.1f",
                call, (int)touch.phase, point.x, point.y, nativeX, nativeY,
                screenContentScale, self.bounds.size.width, self.bounds.size.height);
         [ (MainViewController*)controller addEvent:
          [[NSDictionary alloc] initWithObjectsAndKeys:
           touch.phase == UITouchPhaseBegan ? @"mouseDown" : touch.phase == UITouchPhaseMoved ? @"mouseMoved" : @"mouseUp", @"type",
           [NSNumber numberWithInt:nativeX], @"x",
           [NSNumber numberWithInt:nativeY], @"y", nil]
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
      isMultitouching = true;
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

- (void)gestureShouldEnd
{
   isMultitouching = false;
   [ (MainViewController*)controller addEvent:
    [[NSDictionary alloc] initWithObjectsAndKeys:
     @"multitouchScale", @"type",
     [NSNumber numberWithInt:(int)2], @"key",
     [NSNumber numberWithInt:(int)0], @"x",
     [NSNumber numberWithInt:(int)0], @"y",
     nil]
    ];
}

- (BOOL)gestureRecognizerShouldEnd:(UIGestureRecognizer *)gestureRecognizer
{
	if ( [gestureRecognizer isKindOfClass:[UIPinchGestureRecognizer class]] )
      [self gestureShouldEnd];
	return YES;
}

-(void)handlePinch:(UIPinchGestureRecognizer*)sender
{
   if ([(MainViewController*)controller hasEvents]) // ignore move events if the last one was not yet consumed
      return;
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
