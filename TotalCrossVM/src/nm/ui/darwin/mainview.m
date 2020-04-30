// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define Object NSObject*
#include "mainview.h"
#include "gfx_ex.h"
#import <QuartzCore/CALayer.h>
#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#import <AVFoundation/AVFoundation.h>

#define LKLayer CALayer

bool allowMainThread();
int keyboardH;
UIWindow* window;
void Sleep(int ms);
static bool callingCamera;
UIWindow* barwindow;
static bool callingBarcode;

static char barcode[2048];
static NSMutableString *currBarcode;
extern int32 iosScale;
extern bool isIpad;

@implementation MainViewController
static bool wasNumeric;
- (BOOL)disablesAutomaticKeyboardDismissal {
    return NO;
}

- (UIViewController *) documentInteractionControllerViewControllerForPreview: (UIDocumentInteractionController *) controller {
    return self;
}

bool initGLES(ScreenSurface screen)
{
   deviceCtx = screen->extension = (TScreenSurfaceEx*)malloc(sizeof(TScreenSurfaceEx));
   memset(screen->extension, 0, sizeof(TScreenSurfaceEx));
   isIpad = UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad;
   // initialize the screen bitmap with the full width and height
   CGRect rect = [[UIScreen mainScreen] bounds];
   window = [[UIWindow alloc] initWithFrame: rect];
   window.rootViewController = [(DEVICE_CTX->_mainview = [MainViewController alloc]) init];
   window.autoresizesSubviews = YES; // IOS 8 - make didLayoutSubviews be called
   [window makeKeyAndVisible];
   [DEVICE_CTX->_childview setScreenValues: screen];
   [DEVICE_CTX->_childview createGLcontext];
   return true;
}
- (BOOL)shouldAutorotate // ios 6
{
// [UIView setAnimationsEnabled:NO];
 //  [self destroySIP]; - commented out because its the reason why the keyboard wont close on app launch
   [[UIApplication sharedApplication] setStatusBarHidden:NO withAnimation:UIStatusBarAnimationNone];
   return YES;
}

- (NSUInteger)supportedInterfaceOrientations // ios 6
{
   return UIInterfaceOrientationMaskAll;
}

- (void) setFirstOrientation
{
   lastOrientationSentToVM = [child_view getOrientation];
}

bool iosLowMemory;
- (void)didReceiveMemoryWarning
{
   [super didReceiveMemoryWarning];
   iosLowMemory = true;
}

- (void)viewDidLayoutSubviews
{
   //NSLog(@"*** view will layout subviews");
   int orientation = [child_view getOrientation];
   if (orientation != lastOrientationSentToVM)
   {
      //[self destroySIP];
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
    kbdDisabled = [[UIView alloc] init];
   kbd.font = [ UIFont fontWithName: @"Arial" size: 18.0 ];
   kbd.autocapitalizationType = UITextAutocapitalizationTypeNone;
   kbd.returnKeyType = UIReturnKeyDone;
   kbd.keyboardAppearance = UIKeyboardAppearanceAlert;
   kbd.autocorrectionType = UITextAutocorrectionTypeNo;
   kbd.delegate = self;
    currBarcode = [NSMutableString stringWithString:@""];
}

int isShown;

- (void)destroySIP
{
   isShown = false;
   [ kbd removeFromSuperview ];
}

- (void)showSIP:(SipArguments*)args
{
   int options = [ args values].options;
   if (options == SIP_HIDE)
      [ self destroySIP ];
   else
   {
       if(wasNumeric != [args values].numeric) [ self destroySIP ]; // Destroy to repaint keybaord with different type
       wasNumeric = [args values].numeric;
       if([args values].numeric) kbd.keyboardType = UIKeyboardTypeDecimalPad;
       else kbd.keyboardType = UIKeyboardTypeDefault;
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
      [self addEvent:[[NSDictionary alloc] initWithObjectsAndKeys: @"keyPress", @"type", [NSNumber numberWithInt:(int)'\n'], @"key", nil]];
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
   int num = (int)[_events count];
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
   isShown = true;
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
    isShown = false;
    [kbdDisabled becomeFirstResponder];
}

-(void) readBarcode:(NSString*) mode
{
   callingBarcode = true;
   barcode[0] = 0;
    dispatch_sync(dispatch_get_main_queue(), ^
    {
           _session = [[AVCaptureSession alloc] init];
           _device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
           NSError *error = nil;
           _input = [AVCaptureDeviceInput deviceInputWithDevice:_device error:&error];
           if (_input) {
               [_session addInput:_input];
           } else {
               NSLog(@"Error: %@", error);
           }
           
           _output = [[AVCaptureMetadataOutput alloc] init];
           [_output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
           [_session addOutput:_output];
           
           _output.metadataObjectTypes = [_output availableMetadataObjectTypes];
           
           _prevLayer = [AVCaptureVideoPreviewLayer layerWithSession:_session];
           _prevLayer.frame = self.view.bounds;
           _prevLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
           [self mountBarCodeWindow:_prevLayer];
           [_session startRunning];
              
    });
   while (callingBarcode)
      Sleep(100);
}

- (void)mountBarCodeWindow:(AVCaptureVideoPreviewLayer *) layer{
    [barwindow.layer addSublayer: layer];
    
    [barwindow bringSubviewToFront:barCodeButton];
    
    _highlightView = [[UIView alloc] init];
    _highlightView.autoresizingMask = UIViewAutoresizingFlexibleTopMargin|UIViewAutoresizingFlexibleLeftMargin|UIViewAutoresizingFlexibleRightMargin|UIViewAutoresizingFlexibleBottomMargin;
    _highlightView.layer.borderColor = [UIColor greenColor].CGColor;
    _highlightView.layer.borderWidth = 3;
    [barwindow addSubview:_highlightView];
    barwindow.hidden = NO;
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection
{
    CGRect highlightViewRect = CGRectZero;
    AVMetadataMachineReadableCodeObject *barCodeObject;
    NSString *detectionString = nil;
    NSArray *barCodeTypes = @[AVMetadataObjectTypeUPCECode, AVMetadataObjectTypeCode39Code, AVMetadataObjectTypeCode39Mod43Code,
                              AVMetadataObjectTypeEAN13Code, AVMetadataObjectTypeEAN8Code, AVMetadataObjectTypeCode93Code, AVMetadataObjectTypeCode128Code,
                              AVMetadataObjectTypePDF417Code, AVMetadataObjectTypeQRCode, AVMetadataObjectTypeAztecCode];
    
    for (AVMetadataObject *metadata in metadataObjects) {
        for (NSString *type in barCodeTypes) {
            if ([metadata.type isEqualToString:type])
            {
                barCodeObject = (AVMetadataMachineReadableCodeObject *)[_prevLayer transformedMetadataObjectForMetadataObject:(AVMetadataMachineReadableCodeObject *)metadata];
                detectionString = [(AVMetadataMachineReadableCodeObject *)metadata stringValue];
                highlightViewRect = barCodeObject.bounds;
                _highlightView.frame = highlightViewRect;
                NSString *currBar = [NSString stringWithCString:barcode encoding:NSASCIIStringEncoding];
                if(![detectionString isEqualToString:currBar]) {
                    timeSpentReadingTheSameBarCode = ([[NSDate date] timeIntervalSince1970]*1000);
                    strncpy(barcode, [detectionString cStringUsingEncoding: NSASCIIStringEncoding], MIN([detectionString length], sizeof(barcode)));
                }
                else {
                    NSTimeInterval currTime = ([[NSDate date] timeIntervalSince1970]*1000) - timeSpentReadingTheSameBarCode;
                    if(currTime > 1500) {
                        [self closeBarcode:0];
                    }
                }
                break;
            }
        }
    }
    
}

-(IBAction)closeBarcode:(id)sender
{
   if(_session != null)[_session stopRunning];
   barwindow.hidden = YES;
   callingBarcode = false;
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

-(BOOL) cameraClick:(NSString*) fileName width:(int)w height:(int)h type:(int)t
{
   callingCamera = false;
   imageFileName = fileName;
   imageW = w;
   imageH = h;

   	AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];   
   if (t != 3) {
	    if(authStatus == AVAuthorizationStatusAuthorized)
	    {
	        NSLog(@"%@", @"You have camera access");
	    }
	    else if(authStatus == AVAuthorizationStatusDenied)
	    {
	        NSLog(@"%@", @"Denied camera access");
	
			/*
	        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
	            if(granted){
	                NSLog(@"Granted access to %@", AVMediaTypeVideo);
	            } else {
	                NSLog(@"Not granted access to %@", AVMediaTypeVideo);
	            }
	        }];
	        */
	    }
	    else if(authStatus == AVAuthorizationStatusRestricted)
	    {
	        NSLog(@"%@", @"Restricted, normally won't happen");
	    }
	    else if(authStatus == AVAuthorizationStatusNotDetermined)
	    {
	    	callingCamera = true;
	        NSLog(@"%@", @"Camera access not determined. Ask for permission.");
	
	        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
	            if(granted){
	                NSLog(@"Granted access to %@", AVMediaTypeVideo);
	            } else {
	                NSLog(@"Not granted access to %@", AVMediaTypeVideo);
	            }
	            callingCamera = false;
	        }];
	    }
	    else
	    {
	        NSLog(@"%@", @"Camera access unknown error.");
	    }
	   while (callingCamera)
	      Sleep(100);
	      
	    if(authStatus == AVAuthorizationStatusNotDetermined) {
	      authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
	    }
    }
    
    callingCamera = true;
    if (t == 3 || authStatus == AVAuthorizationStatusAuthorized) {
	   dispatch_sync(dispatch_get_main_queue(), ^
	   {
	      imagePicker = [[UIImagePickerController alloc] init];
	      if(t != 3 && [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
	      {
	         [imagePicker setSourceType:UIImagePickerControllerSourceTypeCamera];
	         imagePicker.allowsEditing = NO;
	      }
	      else
	         [imagePicker setSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
	      [imagePicker setDelegate:self];
	      [self presentModalViewController:imagePicker animated:NO];
	   });
	   while (callingCamera)
	      Sleep(100);
	   UIDeviceOrientation o = [child_view getOrientation];
	   if (o != UIDeviceOrientationLandscapeLeft && o != UIDeviceOrientationLandscapeRight) // when the camera comes back from landscape and we call updateLayout, the screen gets painted as if it was in portrait. this hack makes the screen a bit better, but still buggy.
	      [self updateLayout];
   }
   return imageFileName != null;
}

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
   [self->imagePicker dismissModalViewControllerAnimated:NO];
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
      if (h > w && imageW > imageH) // if user selected a landscape resolution and the photo was taken in portrait, swap the resolution
      {
         int t = imageW; imageW = imageH; imageH = t;
      }
      if (imageW != 0 && imageH != 0 && (w >= imageW || h >= imageH))
      {
         int ww,hh;
         if (w < h)
         {
            hh = imageH;
            ww = (int)(imageH * w / h);
         }
         else
         {
            ww = imageW;
            hh = (int)(imageW * h / w);
         }
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
   [self dismissModalViewControllerAnimated:NO];
   callingCamera = false;
}

#define SHOW_SAT 1
#define USE_WAZE 2
- (BOOL) mapsShowAddress:(NSString*) address flags:(int)flags;
{
   NSString *stringURL;
   char c = [address characterAtIndex:0];
   NSString* type = (flags & SHOW_SAT) != 0 ? @"&t=h" : @"&t=m";
   if ((flags & USE_WAZE) != 0 && [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"waze://"]])
      stringURL = [NSString stringWithFormat:@"waze://?q=%@&navigate=yes", address];
   else
   if ([address length] == 0) // not working yet
   {
//      CLLocationCoordinates2D cl = [self getCurrentLocation];
      stringURL = [NSString stringWithFormat:@"http://maps.google.com/maps?q=Current%%20Location&z=14%@",type];
   }
   else
   if (c == '@')
      stringURL = [NSString stringWithFormat:@"http://maps.google.com/maps?q=%@%%&z=14%@", [address substringFromIndex:1],type];
   else
      stringURL = [NSString stringWithFormat:@"http://maps.google.com/maps?q=%@%%&z=14%@", [address stringByReplacingOccurrencesOfString:@" " withString:@"%20"],type];
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
   locationDate = (int)([components day] + [components month] * 100 + [components year] * 10000);
   locationTime = (int)([components second] + [components minute] * 100 + [components hour] * 10000);
}

- (int) gpsStart
{
   if (locationManager == NULL)
      dispatch_sync(dispatch_get_main_queue(), ^
      {
         locationManager = [[CLLocationManager alloc] init];
         locationManager.delegate = self;
         locationManager.desiredAccuracy = kCLLocationAccuracyBest;
         if ([locationManager respondsToSelector:@selector(requestAlwaysAuthorization)])
            [locationManager requestAlwaysAuthorization];
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


-(IBAction)closeWebView:(id)sender
{
   self.view = child_view;
   webView = nil;
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

char* iphone_readBarcode(char* mode)
{
   NSString* cmode = [NSString stringWithFormat:@"%s", mode];
   [DEVICE_CTX->_mainview readBarcode:cmode];
   return barcode;
}

bool iphone_mapsShowAddress(char* addr, int flags)
{
   NSString* string = [NSString stringWithFormat:@"%s", addr];
   return [DEVICE_CTX->_mainview mapsShowAddress:string flags:flags];
}

void iphone_dialNumber(char* number)
{
   NSString* string = [NSString stringWithFormat:@"telprompt://%s", number];
   [DEVICE_CTX->_mainview dialNumber:string];
}

int iphone_cameraClick(int w, int h, int t, char* fileName)
{
   NSString* string = [NSString stringWithFormat:@"%s", fileName];
   return [DEVICE_CTX->_mainview cameraClick:string width:w height:h type:t];
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
