/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#define Object NSObject*
#include "mainview.h"
#include "gfx_ex.h"
#import <QuartzCore/CALayer.h>
#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#define LKLayer CALayer

bool allowMainThread();
int keyboardH;
UIWindow* window;
void Sleep(int ms);
extern int32 iosScale;
extern bool isIpad;

@implementation MainViewController

bool initGLES(ScreenSurface screen)
{
   deviceCtx = screen->extension = (TScreenSurfaceEx*)malloc(sizeof(TScreenSurfaceEx));
   memset(screen->extension, 0, sizeof(TScreenSurfaceEx));
   isIpad = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad;
   // initialize the screen bitmap with the full width and height
   CGRect rect = [[UIScreen mainScreen] bounds];
   window = [[UIWindow alloc] initWithFrame: rect];
   window.rootViewController = [(DEVICE_CTX->_mainview = [MainViewController alloc]) init];
   window.autoresizesSubviews = NO;
   [window makeKeyAndVisible];
   [DEVICE_CTX->_childview setScreenValues: screen];
   [DEVICE_CTX->_childview createGLcontext];
   return true;
}
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
   [UIView setAnimationsEnabled:NO];
   [self destroySIP];
   return YES;
}

- (void) setFirstOrientation
{
   lastOrientationSentToVM = [child_view getOrientation];
}

- (void)viewDidLayoutSubviews
{
   int orientation = [child_view getOrientation];
   if (orientation != lastOrientationSentToVM)
   {
      [self destroySIP];
      lastOrientationSentToVM = orientation;
      child_view.frame = [child_view getBounds]; // SET THE CHILD_VIEW FRAME
      CGSize res = [child_view getResolution];
      [ self addEvent: [[NSDictionary alloc] initWithObjectsAndKeys: @"screenChange", @"type",
                        [NSNumber numberWithInt: res.width], @"width",
                        [NSNumber numberWithInt: res.height], @"height", nil] ];
   }
}

- (void)loadView
{
   self.view = DEVICE_CTX->_childview = child_view = [[ChildView alloc] init: self];
   [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector (keyboardDidShow:) name: UIKeyboardDidShowNotification object:nil];
   [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector (keyboardDidHide:) name: UIKeyboardDidHideNotification object:nil];

   kbd = [[UITextView alloc] init];
   kbd.font = [ UIFont fontWithName: @"Arial" size: 18.0 ];
   kbd.autocapitalizationType = UITextAutocapitalizationTypeNone;
   kbd.returnKeyType = UIReturnKeyDone;
   kbd.keyboardAppearance = UIKeyboardAppearanceAlert;
   kbd.autocorrectionType = UITextAutocorrectionTypeNo;
   kbd.delegate = self;
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
      [ self setFirstOrientation ];
      [ child_view addSubview: kbd ];
      [ kbd becomeFirstResponder ];
   }
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text
{
   if ([text isEqualToString:@" "])
   {
      [self addEvent: [[NSDictionary alloc] initWithObjectsAndKeys: @"keyPress", @"type", [NSNumber numberWithInt: ' '], @"key", nil]];
      return NO;
   }
   // Any new character added is passed in as the "text" parameter
   if ([text isEqualToString:@"\n"]) // Be sure to test for equality using the "isEqualToString" message
   {
      [textView resignFirstResponder];
      [kbd removeFromSuperview];
      return FALSE; // Return FALSE so that the final '\n' character doesn't get added
   }
   if ([text length] == 0)
   {
      lastRange.length = -1; // guich: fixed bug when pressing backspace and the next key was being ignored
      [self addEvent:[[NSDictionary alloc] initWithObjectsAndKeys: @"keyPress", @"type", [NSNumber numberWithInt:(int)'\b'], @"key", nil]];
   }
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
   unsigned int num = [_events count];
   Sleep(5); // prevent 100% cpu as shown in XCode. 5ms=0%cpu, 2ms=3%cpu
   return num > 0;
}

- (NSArray*)getEvents
{
   NSArray* events = _events;
   _events = nil;
   return events;
}

- (bool)hasEvents
{
   return _events != nil && _events.count > 0;
}
- (void)addEvent:(NSDictionary*)event
{
   if(_events == nil)
      _events = [[NSMutableArray alloc] init];
   [_events addObject: event];
}

-(void) keyboardDidShow: (NSNotification *)notif
{
   if (keyboardH != 0) 
      return;

   // Get the size of the keyboard.
   NSDictionary* info = [notif userInfo];
   NSValue* aValue = [info objectForKey:UIKeyboardBoundsUserInfoKey];
   CGSize keyboardSize = [aValue CGRectValue].size;
   keyboardH = keyboardSize.height * iosScale;
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
      NSURL *url = [NSURL URLWithString:number];
      [[UIApplication sharedApplication] openURL:url];
   });
   [self updateLayout];
}

-(void) updateLayout
{
   dispatch_sync(dispatch_get_main_queue(), ^
   {
      lastOrientationSentToVM = -1;
      [self viewDidLayoutSubviews];
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
      imagePicker = [[UIImagePickerController alloc] init];
      if([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
         [imagePicker setSourceType:UIImagePickerControllerSourceTypeCamera];
      else
         [imagePicker setSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
      [imagePicker setDelegate:self];
      [self presentModalViewController:imagePicker animated:YES];
   });
   while (callingCamera)
      Sleep(100);
   UIDeviceOrientation o = [child_view getOrientation];
   if (o != UIDeviceOrientationLandscapeLeft && o != UIDeviceOrientationLandscapeRight) // when the camera comes back from landscape and we call updateLayout, the screen gets painted as if it was in portrait. this hack makes the screen a bit better, but still buggy.
      [self updateLayout];
   return imageFileName != null;
}

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
   [self->imagePicker dismissModalViewControllerAnimated:YES];
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

- (BOOL) mapsShowAddress:(NSString*) address showSatellitePhotos:(bool)showSat;
{
   NSString *stringURL;
   char c = [address characterAtIndex:0];
   NSString* type = showSat ? @"&t=h" : @"&t=m";
   if ([address length] == 0) // not working yet
   {
//      CLLocationCoordinates2D cl = [self getCurrentLocation];
      stringURL = [NSString stringWithFormat:@"http://maps.google.com/maps?q=Current%20Location&z=14%@%",type];
   }
   else
   if (c == '@')
      stringURL = [NSString stringWithFormat:@"http://maps.google.com/maps?q=%@%&z=14%@%", [address substringFromIndex:1],type];
   else
      stringURL = [NSString stringWithFormat:@"http://maps.google.com/maps?q=%@%&z=14%@%", [address stringByReplacingOccurrencesOfString:@" " withString:@"%20"],type];
   dispatch_sync(dispatch_get_main_queue(), ^
   {
      NSURL *url = [NSURL URLWithString:stringURL];
      [[UIApplication sharedApplication] openURL:url];
   });
   [self updateLayout];
   return TRUE;
}

- (void)locationManager:(CLLocationManager *)manager
    didUpdateToLocation:(CLLocation *)newLocation
           fromLocation:(CLLocation *)oldLocation
{
   // test the age of the location measurement to determine if the measurement is cached
   // in most cases you will not want to rely on cached measurements
   NSTimeInterval locationAge = -[newLocation.timestamp timeIntervalSinceNow];
   if (locationCount > 0 && locationAge > 5.0) return;
   // test that the horizontal accuracy does not indicate an invalid measurement
   if (newLocation.horizontalAccuracy < 0) return;
      
   locationCount++;
   locationFlags = 1 | 2 | 4 | 8 | 32; // ios dont have satellite count 
   locationLat = newLocation.coordinate.latitude;
   locationLon = newLocation.coordinate.longitude;
   locationDir = newLocation.course;
   locationPDOP = newLocation.horizontalAccuracy;
   locationVeloc = newLocation.speed;
   NSDateComponents *components = [[NSCalendar currentCalendar] components:NSDayCalendarUnit | NSMonthCalendarUnit | NSYearCalendarUnit fromDate:newLocation.timestamp];
   locationDate = [components day] + [components month] * 100 + [components year] * 10000;
   locationTime = [components second] + [components minute] * 100 + [components hour] * 10000;
}

- (int) gpsStart
{
   if (locationManager == NULL)
      dispatch_sync(dispatch_get_main_queue(), ^
      {
         locationManager = [[CLLocationManager alloc] init];
         locationManager.delegate = self;
         locationManager.desiredAccuracy = kCLLocationAccuracyBest;
         [locationManager startUpdatingLocation];
         locationCount = 0;
      });
   return 0;
}
- (void) gpsStop
{
   dispatch_sync(dispatch_get_main_queue(), ^
   {
      if (locationManager != NULL)
      {
         [locationManager stopUpdatingLocation];
         [locationManager release];
         locationManager = NULL;
      }
   });
}

- (int) gpsUpdateLocation
{
   return 0;
}

//--------------------------------------------------------------------------------------------------------

@end

void privateFullscreen(bool on) {}

void graphicsUpdateScreenIOS()
{                
   [DEVICE_CTX->_childview updateScreen];
}
void graphicsIOSdoRotate()
{
   [DEVICE_CTX->_childview doRotate];
}

void fillIOSSettings(int* daylightSavingsPtr, int* daylightSavingsMinutesPtr, int* timeZonePtr, int* timeZoneMinutesPtr, char* timeZoneStrPtr, int sizeofTimeZoneStr)
{
   NSTimeZone* tz = [NSTimeZone systemTimeZone];
   *daylightSavingsPtr = [tz isDaylightSavingTime];
   *daylightSavingsMinutesPtr = (int)([tz daylightSavingTimeOffset] / 60);
   *timeZoneMinutesPtr = (int)(tz.secondsFromGMT / 60) - *daylightSavingsMinutesPtr; // as of 22/10/2013, android returns -180/60 and ios returns -120/60
   *timeZonePtr = *timeZoneMinutesPtr / 60;
   NSString *name = tz.name;
   if (name != nil)
   {
      const char* s = [name cStringUsingEncoding:NSASCIIStringEncoding];
      strncpy(timeZoneStrPtr, s, sizeofTimeZoneStr-1);
   }
}

//////////////// interface to mainview methods ///////////////////

bool iphone_mapsShowAddress(char* addr, bool showSatellitePhotos)
{
   NSString* string = [NSString stringWithFormat:@"%s", addr];
   return [DEVICE_CTX->_mainview mapsShowAddress:string showSatellitePhotos:showSatellitePhotos];
}

void iphone_dialNumber(char* number)
{
   NSString* string = [NSString stringWithFormat:@"telprompt://%s", number];
   [DEVICE_CTX->_mainview dialNumber:string];
}

int iphone_cameraClick(int w, int h, char* fileName)
{
   NSString* string = [NSString stringWithFormat:@"%s", fileName];
   return [DEVICE_CTX->_mainview cameraClick:string width:w height:h];
}

int iphone_gpsStart()
{
   return ![CLLocationManager locationServicesEnabled] ? 2 : [DEVICE_CTX->_mainview gpsStart];
}
void iphone_gpsStop()
{
   [DEVICE_CTX->_mainview gpsStop];
}
int iphone_gpsUpdateLocation(BOOL *flags, int *date, int *time, int* sat, double *veloc, double* pdop, double* dir, double* lat, double* lon)
{   
   MainViewController* mw = DEVICE_CTX->_mainview;
   *flags = 0;
   if (mw->locationManager == NULL)
      return 1;
   if (mw->locationFlags == 0)
      return 0;
   *flags = mw->locationFlags;
   *lat = mw->locationLat;
   *lon = mw->locationLon;
   *date = mw->locationDate;
   *time = mw->locationTime;
   *sat = mw->locationSat;
   *veloc = mw->locationVeloc;
   *pdop = mw->locationPDOP;
   *dir = mw->locationDir;
   mw->locationFlags = 0;
   return 0;
}

//////////////////// clipboard //////////////////////

void vmClipboardCopy(JCharP string, int32 sLen) // from Vm.c
{
   UIPasteboard *pb = [UIPasteboard generalPasteboard];
   [pb setString:[[[NSString alloc] initWithCharacters:string length:sLen] autorelease]];
}
unsigned short* ios_ClipboardPaste()
{
   UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
   NSString *text = pasteboard.string;
   return !text ? NULL : (unsigned short*)[text cStringUsingEncoding: NSUnicodeStringEncoding];
}
