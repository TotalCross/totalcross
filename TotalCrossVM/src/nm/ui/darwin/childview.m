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
   controller = ctrl;
   self = [ super init ];
   [self setOpaque:YES];
   self.contentMode = UIViewContentModeCenter;
   self.contentScaleFactor = [UIScreen mainScreen].scale; // support for high resolution
   return self;
}

extern int32 deviceFontHeight,iosScale;

- (void)setScreenValues: (void*)scr
{
   ScreenSurface screen = gscreen == null ? gscreen = scr : gscreen;
   iosScale = [UIScreen mainScreen].scale;//([[UIScreen mainScreen] respondsToSelector:@selector(displayLinkWithTarget:selector:)] && ( == 2.0)) ?2:1;
   screen->screenW = self.bounds.size.width *iosScale;
   screen->screenH = self.bounds.size.height*iosScale;
   screen->pitch = screen->screenW*4;
   screen->bpp = 32;
   screen->pixels = (uint8*)1;
   if (iosScale == 2) deviceFontHeight = 38;
}

- (void)doRotate
{
   [self createGLcontext]; // recreate buffers at the new screen layout
}

void graphicsSetupIOS()
{
   [EAGLContext setCurrentContext:DEVICE_CTX->_childview->glcontext];
}

- (void)createGLcontext
{
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
   setupGL(gscreen->screenW,gscreen->screenH);
   realAppH = appH;
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

@end
