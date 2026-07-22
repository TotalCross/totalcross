// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
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
@import UniformTypeIdentifiers;
#include "barcode_session_state.h"

#define LKLayer CALayer

bool allowMainThread();
int keyboardH;
UIWindow* window;
void Sleep(int ms);
void iphone_privateSetSurfaceWillChange(bool willChange);
static bool callingCamera;
UIWindow* barwindow;
static bool callingBarcode;
static uint64_t barcodeDiagnosticSessionId;
static uint64_t barcodeSessionGeneration;
static bool callingDocumentPicker;
static char documentChars[4096];
static NSLock *barcodeSessionLock;

static char barcode[2048];
static NSMutableString *currBarcode;
extern int32 iosScale;
extern bool isIpad;

typedef enum
{
   TCBarcodeFinishSuccess,
   TCBarcodeFinishCancelled,
   TCBarcodeFinishSetupError,
   TCBarcodeFinishPermissionDenied,
   TCBarcodeFinishConfigurationError,
   TCBarcodeFinishBusy
} TCBarcodeFinishReason;

@interface TCBarcodeSession : NSObject
{
@public
   uint64_t generation;
   TCBarcodeSessionState state;
   TCBarcodeFinishReason finishReason;
   NSString *result;
   NSString *mode;
   NSDate *startedAt;
   dispatch_semaphore_t completionSignal;
   dispatch_queue_t captureQueue;
   AVCaptureSession *captureSession;
   AVCaptureDeviceInput *captureInput;
   AVCaptureMetadataOutput *captureOutput;
   BOOL cleanupCompleted;
}
- (id)initWithGeneration:(uint64_t)sessionGeneration;
@end

@implementation TCBarcodeSession
- (id)initWithGeneration:(uint64_t)sessionGeneration
{
   self = [super init];
   if (self)
   {
      generation = sessionGeneration;
      state = TCBarcodeSessionIdle;
      finishReason = TCBarcodeFinishCancelled;
      result = [@"" retain];
      mode = [@"" retain];
      startedAt = [[NSDate date] retain];
      completionSignal = dispatch_semaphore_create(0);
      captureQueue = dispatch_queue_create("com.totalcross.barcode.capture", DISPATCH_QUEUE_SERIAL);
      cleanupCompleted = NO;
   }
   return self;
}

- (void)dealloc
{
   [result release];
   [mode release];
   [startedAt release];
   [captureSession release];
   [captureInput release];
   [captureOutput release];
   [super dealloc];
}
@end

static TCBarcodeSession *activeBarcodeSession;

static void initializeBarcodeSessionLock(void)
{
   static dispatch_once_t onceToken;
   dispatch_once(&onceToken, ^{
      barcodeSessionLock = [[NSLock alloc] init];
   });
}

@interface MainViewController ()
- (void)layoutBarcodeOverlay;
@end

@implementation MainViewController
static bool wasNumeric;
- (void)logBarcodeDiagnostic:(uint64_t)sessionId event:(NSString *)event
{
   NSLog(@"TCBarcode[%llu] %@ main=%d calling=%d window=%d button=%d session=%d input=%d output=%d preview=%d viewWindow=%d running=%d previewAttached=%d",
      (unsigned long long)sessionId,
      event,
      [NSThread isMainThread],
      callingBarcode,
      barwindow != nil,
      barCodeButton != nil,
      _session != nil,
      _input != nil,
      _output != nil,
      _prevLayer != nil,
      self.view.window != nil,
      _session.isRunning,
      _prevLayer.superlayer != nil);
}

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
   CGSize res = [child_view getResolution];
   int width = (int)res.width;
   int height = (int)res.height;
   bool sizeChanged = width > 0 && height > 0 && (width != lastScreenWidthSentToVM || height != lastScreenHeightSentToVM);
   bool orientationChanged = orientation != lastOrientationSentToVM;
   if (sizeChanged)
   {
      iphone_privateSetSurfaceWillChange(true);
      res = [child_view resizeGLDrawable];
      width = (int)res.width;
      height = (int)res.height;
      //[self destroySIP];
      lastOrientationSentToVM = orientation;
      lastScreenWidthSentToVM = width;
      lastScreenHeightSentToVM = height;
      [ self addEvent: [[NSDictionary alloc] initWithObjectsAndKeys: @"screenChange", @"type",
                        [NSNumber numberWithInt: width], @"width",
                        [NSNumber numberWithInt: height], @"height", nil] ];
   }
   else
   if (orientationChanged)
      lastOrientationSentToVM = orientation;
   [self layoutBarcodeOverlay];
}

- (void)viewSafeAreaInsetsDidChange
{
   [super viewSafeAreaInsetsDidChange];
   UIEdgeInsets insets = self.view.safeAreaInsets;
   BOOL changed = !hasLastSafeAreaInsetsSentToVM || !UIEdgeInsetsEqualToEdgeInsets(insets, lastSafeAreaInsetsSentToVM);
   lastSafeAreaInsetsSentToVM = insets;
   hasLastSafeAreaInsetsSentToVM = YES;
   if (changed)
      [self addEvent: [[NSDictionary alloc] initWithObjectsAndKeys: @"screenChanged", @"type", nil]];
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

-(TCBarcodeSession *)beginBarcodeSessionWithMode:(NSString *)mode
{
   initializeBarcodeSessionLock();
   [barcodeSessionLock lock];
   if (activeBarcodeSession != nil)
   {
      [barcodeSessionLock unlock];
      return nil;
   }

   TCBarcodeSession *session = [[TCBarcodeSession alloc] initWithGeneration:++barcodeSessionGeneration];
   [session->mode release];
   session->mode = [(mode == nil ? @"" : mode) copy];
   if (tcBarcodeSessionCanTransition(session->state, TCBarcodeSessionRequestingPermission))
      session->state = TCBarcodeSessionRequestingPermission;
   activeBarcodeSession = session;
   [barcodeSessionLock unlock];
   return session;
}

-(BOOL)isCurrentBarcodeSession:(TCBarcodeSession *)session
{
   [barcodeSessionLock lock];
   BOOL current = activeBarcodeSession == session
      && tcBarcodeSessionMatchesGeneration(session->generation, activeBarcodeSession->generation)
      && tcBarcodeSessionCanFinish(session->state);
   [barcodeSessionLock unlock];
   return current;
}

-(BOOL)transitionBarcodeSession:(TCBarcodeSession *)session toState:(TCBarcodeSessionState)state
{
   [barcodeSessionLock lock];
   BOOL transitioned = activeBarcodeSession == session
      && tcBarcodeSessionMatchesGeneration(session->generation, activeBarcodeSession->generation)
      && tcBarcodeSessionCanTransition(session->state, state);
   if (transitioned)
      session->state = state;
   [barcodeSessionLock unlock];
   return transitioned;
}

-(BOOL)isBarcodeSessionFinished:(TCBarcodeSession *)session
{
   [barcodeSessionLock lock];
   BOOL finished = session->cleanupCompleted;
   [barcodeSessionLock unlock];
   return finished;
}

-(uint64_t)activeBarcodeSessionGeneration
{
   [barcodeSessionLock lock];
   uint64_t generation = activeBarcodeSession != nil && tcBarcodeSessionCanFinish(activeBarcodeSession->state)
      ? activeBarcodeSession->generation
      : 0;
   [barcodeSessionLock unlock];
   return generation;
}

-(BOOL)finishBarcodeSessionForGeneration:(uint64_t)generation reason:(TCBarcodeFinishReason)reason value:(NSString *)value
{
   [barcodeSessionLock lock];
   TCBarcodeSession *session = activeBarcodeSession;
   BOOL accepted = session != nil
      && tcBarcodeSessionMatchesGeneration(generation, session->generation)
      && tcBarcodeSessionCanFinish(session->state);
   if (!accepted)
   {
      [barcodeSessionLock unlock];
      return NO;
   }

   session->state = TCBarcodeSessionFinishing;
   session->finishReason = reason;
   [session->result release];
   session->result = [(value == nil ? @"" : value) copy];
   [barcodeSessionLock unlock];

   [self logBarcodeDiagnostic:generation event:[NSString stringWithFormat:@"finish reason=%d", reason]];
   if (_output != nil)
      [_output setMetadataObjectsDelegate:nil queue:dispatch_get_main_queue()];
   if (session->captureOutput != nil)
      [session->captureOutput setMetadataObjectsDelegate:nil queue:dispatch_get_main_queue()];
   AVCaptureSession *captureSession = [session->captureSession retain];
   if (captureSession != nil)
   {
      dispatch_async(session->captureQueue, ^{
         [captureSession stopRunning];
         [captureSession release];
      });
   }
   [_prevLayer removeFromSuperlayer];
   if (barcodeOverlayGeneration == generation)
   {
      [_highlightView removeFromSuperview];
      [_highlightView release];
      _highlightView = nil;
      [_barcodeOverlay removeFromSuperview];
      [_barcodeOverlay release];
      _barcodeOverlay = nil;
      barCodeButton = nil;
      barcodeOverlayGeneration = 0;
   }

   [barcodeSessionLock lock];
   if (activeBarcodeSession == session && session->state == TCBarcodeSessionFinishing)
   {
      session->cleanupCompleted = YES;
      session->state = TCBarcodeSessionFinished;
      callingBarcode = false;
      dispatch_semaphore_signal(session->completionSignal);
   }
   [barcodeSessionLock unlock];
   return YES;
}

-(void)copyBarcodeSessionResult:(TCBarcodeSession *)session
{
   [barcodeSessionLock lock];
   NSString *result = [session->result retain];
   [barcodeSessionLock unlock];

   const char *asciiResult = [result cStringUsingEncoding:NSASCIIStringEncoding];
   if (asciiResult != NULL)
   {
      strncpy(barcode, asciiResult, sizeof(barcode) - 1);
      barcode[sizeof(barcode) - 1] = 0;
   }
   else
      barcode[0] = 0;
   [result release];
}

-(void)releaseBarcodeSession:(TCBarcodeSession *)session
{
   [barcodeSessionLock lock];
   if (activeBarcodeSession == session && session->state == TCBarcodeSessionFinished)
      activeBarcodeSession = nil;
   [barcodeSessionLock unlock];
   [session release];
}

-(NSArray *)barcodeMetadataTypesForMode:(NSString *)mode
{
   NSArray *linear = @[AVMetadataObjectTypeUPCECode, AVMetadataObjectTypeCode39Code, AVMetadataObjectTypeCode39Mod43Code,
      AVMetadataObjectTypeEAN13Code, AVMetadataObjectTypeEAN8Code, AVMetadataObjectTypeCode93Code, AVMetadataObjectTypeCode128Code];
   NSArray *twoDimensional = @[AVMetadataObjectTypePDF417Code, AVMetadataObjectTypeQRCode, AVMetadataObjectTypeAztecCode,
      AVMetadataObjectTypeDataMatrixCode];
   if (mode.length == 0)
      return [linear arrayByAddingObjectsFromArray:twoDimensional];
   if ([mode isEqualToString:@"1D"])
      return linear;
   if ([mode isEqualToString:@"2D"])
      return twoDimensional;
   return nil;
}

-(void)configureBarcodeSession:(TCBarcodeSession *)session
{
   if (![self isCurrentBarcodeSession:session])
      return;
   [self transitionBarcodeSession:session toState:TCBarcodeSessionConfiguring];
   [session retain];
   dispatch_async(session->captureQueue, ^{
      NSArray *requestedTypes = [self barcodeMetadataTypesForMode:session->mode];
      if (requestedTypes == nil)
      {
         dispatch_async(dispatch_get_main_queue(), ^{
            [self finishBarcodeSessionForGeneration:session->generation reason:TCBarcodeFinishConfigurationError value:nil];
            [session release];
         });
         return;
      }

      AVCaptureSession *captureSession = [[AVCaptureSession alloc] init];
      AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
      NSError *error = nil;
      AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:device error:&error];
      AVCaptureMetadataOutput *output = [[AVCaptureMetadataOutput alloc] init];
      if (input == nil || ![captureSession canAddInput:input] || ![captureSession canAddOutput:output])
      {
         [captureSession release];
         [output release];
         dispatch_async(dispatch_get_main_queue(), ^{
            NSLog(@"TCBarcode[%llu] configurationFailed input=%d errorPresent=%d", (unsigned long long)session->generation, input != nil, error != nil);
            [self finishBarcodeSessionForGeneration:session->generation reason:TCBarcodeFinishConfigurationError value:nil];
            [session release];
         });
         return;
      }

      [captureSession addInput:input];
      [captureSession addOutput:output];
      NSMutableArray *supportedTypes = [[NSMutableArray alloc] init];
      for (NSString *type in requestedTypes)
         if ([[output availableMetadataObjectTypes] containsObject:type])
            [supportedTypes addObject:type];
      if (supportedTypes.count == 0)
      {
         [captureSession release];
         [output release];
         [supportedTypes release];
         dispatch_async(dispatch_get_main_queue(), ^{
            [self finishBarcodeSessionForGeneration:session->generation reason:TCBarcodeFinishConfigurationError value:nil];
            [session release];
         });
         return;
      }
      output.metadataObjectTypes = supportedTypes;
      [supportedTypes release];
      [output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];

      [barcodeSessionLock lock];
      BOOL current = activeBarcodeSession == session && tcBarcodeSessionCanFinish(session->state);
      if (current)
      {
         session->captureSession = captureSession;
         session->captureInput = [input retain];
         session->captureOutput = output;
      }
      [barcodeSessionLock unlock];
      if (!current)
      {
         [captureSession stopRunning];
         [captureSession release];
         [output release];
         [session release];
         return;
      }

      [captureSession startRunning];
      dispatch_async(dispatch_get_main_queue(), ^{
         if ([self isCurrentBarcodeSession:session])
         {
            _session = session->captureSession;
            _input = session->captureInput;
            _output = session->captureOutput;
            _prevLayer = [AVCaptureVideoPreviewLayer layerWithSession:_session];
            _prevLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
            [self mountBarcodeOverlay:_prevLayer generation:session->generation];
            [self transitionBarcodeSession:session toState:TCBarcodeSessionRunning];
            [self logBarcodeDiagnostic:session->generation event:@"captureRunning"];
         }
         else
            [captureSession stopRunning];
         [session release];
      });
   });
}

-(void)beginBarcodeCaptureForSession:(TCBarcodeSession *)session
{
   if (![self isCurrentBarcodeSession:session])
      return;
   [self transitionBarcodeSession:session toState:TCBarcodeSessionPresenting];
   [self mountBarcodeOverlay:nil generation:session->generation];
   AVAuthorizationStatus status = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
   [self logBarcodeDiagnostic:session->generation event:[NSString stringWithFormat:@"authorization=%ld", (long)status]];
   if (status == AVAuthorizationStatusAuthorized)
      [self configureBarcodeSession:session];
   else if (status == AVAuthorizationStatusNotDetermined)
      [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
         dispatch_async(dispatch_get_main_queue(), ^{
            if (![self isCurrentBarcodeSession:session])
               return;
            if (granted)
               [self configureBarcodeSession:session];
            else
               [self finishBarcodeSessionForGeneration:session->generation reason:TCBarcodeFinishPermissionDenied value:nil];
         });
      }];
   else
      [self finishBarcodeSessionForGeneration:session->generation reason:TCBarcodeFinishPermissionDenied value:nil];
}

-(void) readBarcode:(NSString*) mode diagnosticSessionId:(uint64_t)sessionId
{
   TCBarcodeSession *session = [self beginBarcodeSessionWithMode:mode];
   if (session == nil)
   {
      barcode[0] = 0;
      NSLog(@"TCBarcode[%llu] busy", (unsigned long long)sessionId);
      return;
   }

   callingBarcode = true;
   barcode[0] = 0;
   AVAuthorizationStatus authorizationStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
   [self logBarcodeDiagnostic:sessionId event:[NSString stringWithFormat:@"start modePresent=%d authorization=%ld beforeMainDispatch", mode != nil, (long)authorizationStatus]];
    dispatch_sync(dispatch_get_main_queue(), ^
    {
           [self logBarcodeDiagnostic:sessionId event:@"enteredMainDispatch"];
           [self beginBarcodeCaptureForSession:session];
    });
   [self logBarcodeDiagnostic:sessionId event:@"enteredCallerWait"];
   int waitCount = 0;
   while (![self isBarcodeSessionFinished:session])
   {
      Sleep(100);
      if (++waitCount % 100 == 0)
         [self logBarcodeDiagnostic:sessionId event:@"callerWaitHeartbeat"];
   }
   [self copyBarcodeSessionResult:session];
   [self releaseBarcodeSession:session];
   [self logBarcodeDiagnostic:sessionId event:@"callerWaitCompleted"];
}

- (void)layoutBarcodeOverlay
{
    if (_barcodeOverlay == nil)
        return;
    _barcodeOverlay.frame = self.view.bounds;
    _prevLayer.frame = _barcodeOverlay.bounds;
    UIEdgeInsets safeArea = _barcodeOverlay.safeAreaInsets;
    barCodeButton.frame = CGRectMake(safeArea.left + 16, safeArea.top + 16, 96, 44);
}

- (void)mountBarcodeOverlay:(AVCaptureVideoPreviewLayer *)layer generation:(uint64_t)generation
{
    if (_barcodeOverlay != nil)
    {
        if (barcodeOverlayGeneration == generation && layer != nil && layer.superlayer != _barcodeOverlay.layer)
            [_barcodeOverlay.layer insertSublayer:layer atIndex:0];
        return;
    }
    if (self.view.window == nil)
    {
        NSLog(@"TCBarcode[%llu] overlayPresentationFailed viewWindow=0", (unsigned long long)generation);
        [self finishBarcodeSessionForGeneration:generation reason:TCBarcodeFinishSetupError value:nil];
        return;
    }

    _barcodeOverlay = [[UIView alloc] initWithFrame:self.view.bounds];
    _barcodeOverlay.backgroundColor = [UIColor blackColor];
    _barcodeOverlay.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    barcodeOverlayGeneration = generation;
    if (layer != nil)
        [_barcodeOverlay.layer addSublayer:layer];

    barCodeButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [barCodeButton setTitle:@"Cancel" forState:UIControlStateNormal];
    [barCodeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    barCodeButton.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.55];
    barCodeButton.accessibilityLabel = @"Cancel barcode scan";
    [barCodeButton addTarget:self action:@selector(closeBarcode:) forControlEvents:UIControlEventTouchUpInside];
    [_barcodeOverlay addSubview:barCodeButton];

    _highlightView = [[UIView alloc] init];
    _highlightView.autoresizingMask = UIViewAutoresizingFlexibleTopMargin|UIViewAutoresizingFlexibleLeftMargin|UIViewAutoresizingFlexibleRightMargin|UIViewAutoresizingFlexibleBottomMargin;
    _highlightView.layer.borderColor = [UIColor greenColor].CGColor;
    _highlightView.layer.borderWidth = 3;
    [_barcodeOverlay addSubview:_highlightView];
    [self.view addSubview:_barcodeOverlay];
    [self layoutBarcodeOverlay];
    NSLog(@"TCBarcode[%llu] overlayPresented button=%d previewAttached=%d", (unsigned long long)generation, barCodeButton != nil, layer.superlayer == _barcodeOverlay.layer);
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection
{
    uint64_t generation = [self activeBarcodeSessionGeneration];
    if (generation == 0)
        return;
    NSLog(@"TCBarcode metadataCallback count=%lu session=%d preview=%d", (unsigned long)metadataObjects.count, _session != nil, _prevLayer != nil);
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
                NSLog(@"TCBarcode metadataRecognized valuePresent=%d", detectionString != nil);
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
                        [self finishBarcodeSessionForGeneration:generation reason:TCBarcodeFinishSuccess value:detectionString];
                    }
                }
                break;
            }
        }
    }
    
}

-(IBAction)closeBarcode:(id)sender
{
   NSLog(@"TCBarcode closeBarcode session=%d running=%d overlay=%d", _session != nil, _session.isRunning, _barcodeOverlay != nil);
   uint64_t generation = [self activeBarcodeSessionGeneration];
   [self finishBarcodeSessionForGeneration:generation reason:(sender == nil ? TCBarcodeFinishSuccess : TCBarcodeFinishCancelled) value:[NSString stringWithCString:barcode encoding:NSASCIIStringEncoding]];
   NSLog(@"TCBarcode closeBarcodeComplete calling=%d overlay=%d", callingBarcode, _barcodeOverlay != nil);
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

-(BOOL) documentPickerStart:(NSString*) fileName width:(int)w height:(int)h type:(int)t
{
    documentChars[0] = 0;
    callingDocumentPicker = true;
   {
	   dispatch_sync(dispatch_get_main_queue(), ^
	   {
           BOOL allowsMultipleSelection = false;
           BOOL pickDirectory = false;
           BOOL isDirectory = false;
           @try{
               self->documentPickerController = [[UIDocumentPickerViewController alloc]
                                                initWithDocumentTypes: isDirectory ? @[@"public.folder"] : @[@"public.item"]/*self.allowedExtensions*/
                                                inMode: isDirectory ? UIDocumentPickerModeOpen : UIDocumentPickerModeImport];
           } @catch (NSException * e) {
//               Log(@"Couldn't launch documents file picker. Probably due to iOS version being below 11.0 and not having the iCloud entitlement. If so, just make sure to enable it for your app in Xcode. Exception was: %@", e);
//               _result = nil;
               return;
           }
           
           self->documentPickerController.allowsMultipleSelection = NO;//allowsMultipleSelection;
           self->documentPickerController.delegate = self;
           self->documentPickerController.presentationController.delegate = self;
           
           [documentPickerController setDelegate:self];
           [self presentModalViewController:documentPickerController animated:NO];
	   });
	   while (callingDocumentPicker)
	      Sleep(100);
	   UIDeviceOrientation o = [child_view getOrientation];
	   if (o != UIDeviceOrientationLandscapeLeft && o != UIDeviceOrientationLandscapeRight) // when the camera comes back from landscape and we call updateLayout, the screen gets painted as if it was in portrait. this hack makes the screen a bit better, but still buggy.
	      [self updateLayout];
   }

   return true;
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls{
    if (urls != NULL && urls.count > 0) {
        NSString *documentUrlString = [urls[0] absoluteString];
        strncpy(documentChars, [documentUrlString cStringUsingEncoding: NSASCIIStringEncoding], MIN([documentUrlString length], sizeof(documentChars)));
    }
    
    [self dismissModalViewControllerAnimated:NO];
    callingDocumentPicker = false;
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentAtURL:(NSURL *)url
{
//    [self documentPickerCompleted:controller documents:@[url]];
    if (url != NULL) {
        NSString *documentUrlString = [url absoluteString];
        strncpy(documentChars, [documentUrlString cStringUsingEncoding: NSASCIIStringEncoding], MIN([documentUrlString length], sizeof(documentChars)));
    }
    
    [self dismissModalViewControllerAnimated:NO];
    callingDocumentPicker = false;
}

- (void)documentPickerWasCancelled:(UIDocumentPickerViewController *)controller
{
    [self dismissModalViewControllerAnimated:NO];
    callingDocumentPicker = false;
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
   NSDateComponents *components = [[NSCalendar currentCalendar] components:NSCalendarUnitDay | NSCalendarUnitMonth | NSCalendarUnitYear | NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond fromDate:newLocation.timestamp];
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
   uint64_t sessionId = ++barcodeDiagnosticSessionId;
   NSLog(@"TCBarcode[%llu] bridgeEnter main=%d modePresent=%d", (unsigned long long)sessionId, [NSThread isMainThread], mode != NULL);
   NSString* cmode = [NSString stringWithFormat:@"%s", mode];
   [DEVICE_CTX->_mainview readBarcode:cmode diagnosticSessionId:sessionId];
   NSLog(@"TCBarcode[%llu] bridgeReturn resultPresent=%d", (unsigned long long)sessionId, barcode[0] != 0);
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

char* iphone_documentPickerStart(int w, int h, int t, char** ret)
{
   NSString* string = [NSString stringWithFormat:@"file.txt"];
    xmemzero(documentChars, sizeof(documentChars));
   [DEVICE_CTX->_mainview documentPickerStart:string width:w height:h type:t];
    
    if (documentChars[0] != 0) {
        int n = xstrlen(documentChars);
        (*ret) = xmalloc(sizeof(char) * (n + 1));
        xstrncpy(*ret, documentChars, n);
    }

    return (*ret);
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
