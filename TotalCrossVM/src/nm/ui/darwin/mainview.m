/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#define Object NSObject*
#include "mainview.h"
#include "gfx_ex.h"

#import <QuartzCore/CALayer.h>
#define LKLayer CALayer

bool allowMainThread();
static NSLock *deviceCtxLock;
int keyboardH;
UIWindow* window;
void Sleep(int ms);

@implementation SSize

- (id)set:(CGSize)size
{
   ssize = size;
   return self;
}

- (CGSize)get
{
   return ssize;
}

@end

//--------------------------------------------------------------------------------------------------------

@implementation MainView

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
   [UIView setAnimationsEnabled:NO];
   [self destroySIP];
   return YES;
}

- (void)viewDidLoad
{
   [super viewDidLoad];
   DEVICE_CTX->_childview = child_view = [[ChildView alloc] init: self];
   [self initEvents];
   self.view = child_view;

   kbd = [[UITextView alloc] init];
   kbd.font = [ UIFont fontWithName: @"Arial" size: 18.0 ];
   kbd.autocapitalizationType = UITextAutocapitalizationTypeWords;
   kbd.returnKeyType = UIReturnKeyDone;
   kbd.keyboardAppearance = UIKeyboardAppearanceAlert;
   [kbd setDelegate: self];
}

- (void)initEvents
{
   _lock = [[NSLock alloc] init];

   [[NSNotificationCenter defaultCenter] addObserver:self
      selector:@selector (keyboardDidShow:)
      name: UIKeyboardDidShowNotification object:nil];
 
   [[NSNotificationCenter defaultCenter] addObserver:self 
      selector:@selector (keyboardDidHide:)
      name: UIKeyboardDidHideNotification object:nil];
}

- (void)destroySIP
{
   [ kbd removeFromSuperview ];
}

- (void)showSIP:(SipArguments*)args
{
   int options = [ args values].options;
   if (options == SIP_HIDE)
      [ self destroySIP ];
   else
   {
      [ child_view addSubview: kbd ];
      [ kbd becomeFirstResponder ];
   }
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text
{
   // Any new character added is passed in as the "text" parameter
   if ([text isEqualToString:@"\n"]) // Be sure to test for equality using the "isEqualToString" message
   {
      [textView resignFirstResponder];
      [kbd removeFromSuperview];
      return FALSE; // Return FALSE so that the final '\n' character doesn't get added
   }
   if ([text length] == 0)
      [self addEvent:[[NSDictionary alloc] initWithObjectsAndKeys: @"keyPress", @"type", [NSNumber numberWithInt:(int)'\b'], @"key", nil]];
   else 
   if (lastRange.location <= 0 || !NSEqualRanges(range, lastRange)) //flsobral@tc126: avoid adding the same character twice.
   {
      lastRange.location = range.location;
      lastRange.length = range.length;     
      unsigned short* chars = (unsigned short*)[text cStringUsingEncoding: NSUnicodeStringEncoding];
      if (chars != null)
         [self addEvent: [[NSDictionary alloc] initWithObjectsAndKeys: @"keyPress", @"type", [NSNumber numberWithInt: chars[0]], @"key", nil]];
   }
   // For any other character return TRUE so that the text gets added to the view
   return TRUE;
}

- (bool)isEventAvailable;
{
   [_lock lock];
   unsigned int num = [_events count];
   [_lock unlock];
   return num > 0;
}

- (NSArray*)getEvents
{
   [_lock lock];
   NSArray* events = _events;
   _events = nil;
   [_lock unlock];

   return events;
}

- (void)addEvent:(NSDictionary*)event
{
   [_lock lock];
   if(_events == nil)
      _events = [[NSMutableArray alloc] init];
   [_events addObject: event];
   [_lock unlock];
}

-(void) keyboardDidShow: (NSNotification *)notif
{
   if (keyboardH != 0) 
      return;

   // Get the size of the keyboard.
   NSDictionary* info = [notif userInfo];
   NSValue* aValue = [info objectForKey:UIKeyboardBoundsUserInfoKey];
   CGSize keyboardSize = [aValue CGRectValue].size;
   keyboardH = keyboardSize.height;
}

-(void) keyboardDidHide: (NSNotification *)notif
{
   keyboardH = 0;
   [ self addEvent:
      [[NSDictionary alloc] initWithObjectsAndKeys:
       @"sipClosed", @"type",
       [NSNumber numberWithInt:0], @"x",
       [NSNumber numberWithInt:0], @"y",
       nil
      ]
   ];
}

-(void) dialNumber:(NSString*) number
{
   dispatch_sync(dispatch_get_main_queue(), ^
   {
      NSString *s = @"telprompt://";
      s = [s stringByAppendingString:number];
      NSURL *url = [NSURL URLWithString:s];
      [[UIApplication sharedApplication] openURL:url];
   });
}

static bool callingCamera;

-(BOOL) cameraClick:(NSString*) fileName width:(int)w height:(int)h
{
   callingCamera = true;
   imageFileName = fileName;
   imageW = w;
   imageH = h;
   dispatch_sync(dispatch_get_main_queue(), ^
   {
      UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
      if([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
         [imagePicker setSourceType:UIImagePickerControllerSourceTypeCamera];
      else
         [imagePicker setSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
      [imagePicker setDelegate:self];
      [self presentModalViewController:imagePicker animated:YES];
   });
   while (callingCamera)
      Sleep(100);
   return imageFileName != null;
}   

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
   [[picker parentViewController] dismissModalViewControllerAnimated:YES];
   imageFileName = null;
   callingCamera = false;
}
-(void) imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
   UIImage* finalImage = [info objectForKey:UIImagePickerControllerOriginalImage];
   if (finalImage == NULL)
      imageFileName = NULL;
   else 
   {
      int w = finalImage.size.width;
      int h = finalImage.size.height;
      if (imageW != 0 && imageH != 0 && (w >= imageW || h >= imageH))
      {
         int ww=imageW,hh;
         if (w < h)
            hh = (int)(imageW * w / h);
         else
            hh = (int)(imageW * h / w);
         CGRect imageRect = CGRectMake(0, 0, ww,hh);
         UIGraphicsBeginImageContext(imageRect.size);
         [finalImage drawInRect:imageRect];
         UIImage *thumbnail = UIGraphicsGetImageFromCurrentImageContext();
         UIGraphicsEndImageContext();
         finalImage = thumbnail;
      }
      NSData* data = UIImageJPEGRepresentation(finalImage, 0.8);
      [data writeToFile:imageFileName atomically:NO];
   }
   [self dismissModalViewControllerAnimated:YES];
   callingCamera = false;
}
//--------------------------------------------------------------------------------------------------------

@end

void orientationChanged() {} // called by the UI
void privateFullscreen(bool on) {}
void privateScreenChange(int32 w, int32 h) {}

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
   deviceCtxLock = (NSLock*)[[NSRecursiveLock alloc] init];
   deviceCtx = screen->extension = (TScreenSurfaceEx*)malloc(sizeof(TScreenSurfaceEx));
   memset(screen->extension, 0, sizeof(TScreenSurfaceEx));
   // initialize the screen bitmap with the full width and height
   CGRect rect = [[UIScreen mainScreen] bounds];
   DEVICE_CTX->_window = window = [[UIWindow alloc] initWithFrame: rect];
   window.rootViewController = [(DEVICE_CTX->_mainview = [MainView alloc]) init];
   [window makeKeyAndVisible];
   
   [ DEVICE_CTX->_childview updateScreen: screen ];
   screen->pixels = (void*)1;
   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   [deviceCtxLock lock];
   screen->extension = deviceCtx;
   [deviceCtxLock unlock];
   return true;
}

void graphicsUpdateScreen(ScreenSurface screen, int32 transitionEffect)
{
   [deviceCtxLock lock];
   ChildView* vw = (ChildView*)DEVICE_CTX->_childview;
   if (allowMainThread())
      [vw invalidateScreen: screen];
   [deviceCtxLock unlock];       
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   [deviceCtxLock lock];
   if (isScreenChange)
     screen->extension = NULL;
   else
   {
      if (screen->extension)
        free(screen->extension);
     deviceCtx = screen->extension = NULL;
   }
   [deviceCtxLock unlock];
}

bool graphicsLock(ScreenSurface screen, bool on)
{
   if (on)
      [deviceCtxLock lock];
   else
      [deviceCtxLock unlock];
   return true;
}
