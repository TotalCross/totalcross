/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#ifndef MAINVIEW_H
#define MAINVIEW_H

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UITextView.h>
#import <CoreLocation/CLLocation.h>
#import <CoreLocation/CLLocationManager.h>
#import <CoreLocation/CLLocationManagerDelegate.h>
#import <WebKit/WebKit.h>
#include "GraphicsPrimitives.h"
#import "childview.h"
#import "sipargs.h"

@interface MainViewController : UIViewController<UITextViewDelegate,UIImagePickerControllerDelegate,CLLocationManagerDelegate, UIDocumentInteractionControllerDelegate, AVCaptureMetadataOutputObjectsDelegate>
{
   NSMutableArray* _events;
   ChildView *child_view;
   int lastOrientationSentToVM;
   // keyboard
   UITextView* kbd;
   UIView* kbdDisabled;
   NSRange lastRange;
   // camera
   NSString* imageFileName;
   int imageW,imageH;
   UIImagePickerController *imagePicker;
   // barcode
   AVCaptureSession *_session;
   AVCaptureDevice *_device;
   AVCaptureDeviceInput *_input;
   AVCaptureMetadataOutput *_output;
   AVCaptureVideoPreviewLayer *_prevLayer;
   UIButton *barCodeButton;
   UIView *_highlightView;
   NSTimeInterval timeSpentReadingTheSameBarCode;
   // gps
@public   
   CLLocationManager* locationManager;
   int locationFlags, locationDate, locationTime, locationSat, locationCount;
   double locationVeloc, locationPDOP, locationDir;
   double locationLat, locationLon;
   WKWebView * webView;
}
- (bool)hasEvents;
- (void)addEvent:(NSDictionary*)event;
- (bool)isEventAvailable;
- (NSArray*)getEvents;
- (void)showSIP:(SipArguments*)args;
- (void)destroySIP;
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string;
- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text;
- (void) keyboardDidShow: (NSNotification *)notif;
- (void) keyboardDidHide: (NSNotification *)notif;
- (BOOL) cameraClick:(NSString*) fileName width:(int)w height:(int)h type:(int)t;
- (void) updateLayout;
- (void) dialNumber:(NSString*) number;
- (BOOL) mapsShowAddress:(NSString*) address flags:(int)flags;
- (int) gpsStart;
- (void) gpsStop;
- (int) gpsUpdateLocation;
- (IBAction)closeWebView:(id)sender;
- (IBAction)closeBarcode:(id)sender;
- (UIViewController *) documentInteractionControllerViewControllerForPreview : (UIDocumentInteractionController *) controller;
@end

typedef struct
{
//   __unsafe_unretained UIWindow  *_window;
   __unsafe_unretained MainViewController  *_mainview;
   __unsafe_unretained ChildView *_childview;
} TScreenSurfaceEx, *ScreenSurfaceEx;

#endif
