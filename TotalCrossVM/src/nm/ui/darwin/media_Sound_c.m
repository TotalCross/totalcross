// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <Foundation/Foundation.h>
#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>
#import <Speech/Speech.h>
#import <UIKit/UIKit.h>
#include "xtypes.h"

static SystemSoundID sound;
static char lastSound[MAX_PATHNAME];

void iphone_soundPlay(CharP filename)
{
   if (!strEq(filename,lastSound))
   {
      if (sound) AudioServicesDisposeSystemSoundID(sound);
      xstrcpy(lastSound, filename);
      NSString* string = [NSString stringWithFormat:@"%s", filename];
      NSURL *wav = [NSURL fileURLWithPath:string];
      AudioServicesCreateSystemSoundID((__bridge CFURLRef)wav, &sound);
   }
   AudioServicesPlaySystemSound(sound);
}

void iphone_soundBeep()
{
   AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);
}

// UIKit and recognition state must be accessed only on the main dispatch queue.
// The VM calling thread waits on a semaphore, never on the UIKit main thread.
@interface TCSpeechInput : NSObject
{
   dispatch_semaphore_t _completion;
   NSString *_title;
   NSString *_text;
   NSInteger _silenceMillis;
   SFSpeechRecognizer *_recognizer;
   SFSpeechAudioBufferRecognitionRequest *_request;
   SFSpeechRecognitionTask *_task;
   AVAudioEngine *_engine;
   UIAlertController *_alert;
   NSTimer *_silenceTimer;
   NSTimer *_finishTimer;
   NSTimer *_maxTimer;
   BOOL _sessionActive;
   BOOL _tapInstalled;
   BOOL _ending;
   BOOL _completed;
}
- (id)initWithTitle:(NSString *)title timeout:(NSInteger)timeout;
- (dispatch_semaphore_t)completion;
- (NSString *)text;
- (void)begin;
- (void)cancel;
@end

static TCSpeechInput *activeSpeechInput; // Main queue only.

@implementation TCSpeechInput

- (id)initWithTitle:(NSString *)title timeout:(NSInteger)timeout
{
   if ((self = [super init]))
   {
      _completion = dispatch_semaphore_create(0);
      _title = [title copy];
      _silenceMillis = timeout;
   }
   return self;
}

- (dispatch_semaphore_t)completion { return _completion; }
- (NSString *)text { return _text; }

- (void)invalidate:(NSTimer **)timer
{
   if (*timer)
   {
      [*timer invalidate];
      [*timer release];
      *timer = nil;
   }
}

- (void)stopAudio
{
   if (_engine)
   {
      if (_tapInstalled)
      {
         [[_engine inputNode] removeTapOnBus:0];
         _tapInstalled = NO;
      }
      if ([_engine isRunning])
         [_engine stop];
   }
   if (_request && !_ending)
   {
      _ending = YES;
      [_request endAudio];
   }
}

- (void)finish:(BOOL)keepText
{
   if (_completed) return;
   _completed = YES;
   [self invalidate:&_silenceTimer];
   [self invalidate:&_finishTimer];
   [self invalidate:&_maxTimer];
   [self stopAudio];
   [_task cancel];
   if (!keepText)
   {
      [_text release];
      _text = nil;
   }
   if (_alert)
   {
      [_alert dismissViewControllerAnimated:YES completion:nil];
      [_alert release];
      _alert = nil;
   }
   if (_sessionActive)
   {
      [[AVAudioSession sharedInstance] setActive:NO
         withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
         error:nil];
      _sessionActive = NO;
   }
   if (activeSpeechInput == self)
      activeSpeechInput = nil;
   dispatch_semaphore_signal(_completion);
}

- (void)cancel { [self finish:NO]; }
- (void)finishedWaiting:(NSTimer *)timer { [self finish:(_text.length > 0)]; }

- (void)stopListening
{
   if (_completed || _ending) return;
   [self invalidate:&_silenceTimer];
   [self stopAudio];
   // Allow Speech to deliver its final result after endAudio.
   _finishTimer = [[NSTimer scheduledTimerWithTimeInterval:7.0
      target:self selector:@selector(finishedWaiting:)
      userInfo:nil repeats:NO] retain];
}

- (void)silenceElapsed:(NSTimer *)timer { [self stopListening]; }
- (void)maximumElapsed:(NSTimer *)timer { [self stopListening]; }

- (void)resetSilenceTimer
{
   if (_ending) return;
   [self invalidate:&_silenceTimer];
   _silenceTimer = [[NSTimer scheduledTimerWithTimeInterval:
      (NSTimeInterval)_silenceMillis / 1000.0
      target:self selector:@selector(silenceElapsed:)
      userInfo:nil repeats:NO] retain];
}

- (void)handleResult:(SFSpeechRecognitionResult *)result error:(NSError *)error
{
   if (_completed) return;
   if (result)
   {
      NSString *candidate = result.bestTranscription.formattedString;
      if (candidate.length && ![candidate isEqualToString:_text])
      {
         [_text release];
         _text = [candidate copy];
         [self resetSilenceTimer];
      }
      if (result.isFinal)
      {
         [self finish:(_text.length > 0)];
         return;
      }
   }
   if (error)
      [self finish:(_text.length > 0)];
}

- (void)startRecording
{
   if (_completed) return;
   _recognizer = [[SFSpeechRecognizer alloc] initWithLocale:[NSLocale currentLocale]];
   if (!_recognizer || !_recognizer.isAvailable)
   {
      [self cancel];
      return;
   }
   AVAudioSession *session = [AVAudioSession sharedInstance];
   NSError *error = nil;
   if (![session setCategory:AVAudioSessionCategoryRecord
      mode:AVAudioSessionModeMeasurement
      options:AVAudioSessionCategoryOptionDuckOthers error:&error] ||
       ![session setActive:YES error:&error])
   {
      [self cancel];
      return;
   }
   _sessionActive = YES;
   _request = [[SFSpeechAudioBufferRecognitionRequest alloc] init];
   _request.shouldReportPartialResults = YES;
   _engine = [[AVAudioEngine alloc] init];
   AVAudioInputNode *input = [_engine inputNode];
   AVAudioFormat *format = [input outputFormatForBus:0];
   if (!input || !format || format.channelCount == 0)
   {
      [self cancel];
      return;
   }
   SFSpeechAudioBufferRecognitionRequest *request = _request;
   [input installTapOnBus:0 bufferSize:1024 format:format
      block:^(AVAudioPCMBuffer *buffer, AVAudioTime *when) {
         [request appendAudioPCMBuffer:buffer];
      }];
   _tapInstalled = YES;
   [_engine prepare];
   _task = [[_recognizer recognitionTaskWithRequest:_request
      resultHandler:^(SFSpeechRecognitionResult *result, NSError *recognitionError) {
         dispatch_async(dispatch_get_main_queue(), ^{
            [self handleResult:result error:recognitionError];
         });
      }] retain];
   if (!_task || ![_engine startAndReturnError:&error])
   {
      [self cancel];
      return;
   }

   extern UIWindow *window; // Existing TotalCross application window.
   UIViewController *presenter = window.rootViewController;
   if (!presenter || presenter.presentedViewController)
   {
      [self cancel];
      return;
   }

   _alert = [[UIAlertController alertControllerWithTitle:_title
      message:NSLocalizedString(@"Speak now. Tap Done to finish.", nil)
      preferredStyle:UIAlertControllerStyleAlert] retain];
   [_alert addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Cancel", nil)
      style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
         [self cancel];
      }]];
   [_alert addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Done", nil)
      style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
         [self stopListening];
      }]];
   [presenter presentViewController:_alert animated:YES completion:nil];
   _maxTimer = [[NSTimer scheduledTimerWithTimeInterval:90.0
      target:self selector:@selector(maximumElapsed:)
      userInfo:nil repeats:NO] retain];
}

- (void)requestMicrophone
{
   if (_completed) return;
   [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted) {
      dispatch_async(dispatch_get_main_queue(), ^{
         if (granted) [self startRecording];
         else [self cancel];
      });
   }];
}

- (void)begin
{
   [SFSpeechRecognizer requestAuthorization:^(SFSpeechRecognizerAuthorizationStatus status) {
      dispatch_async(dispatch_get_main_queue(), ^{
         if (_completed) return;
         if (status == SFSpeechRecognizerAuthorizationStatusAuthorized)
            [self requestMicrophone];
         else
            [self cancel];
      });
   }];
}

- (void)dealloc
{
   [_title release];
   [_text release];
   [_alert release];
   [_engine release];
   [_request release];
   [_task release];
   [_recognizer release];
#if !OS_OBJECT_USE_OBJC
   dispatch_release(_completion);
#endif
   [super dealloc];
}
@end

// The return buffer is malloc-owned UTF-16. NULL means cancel/error/denial.
// Serialize recording sessions, and do not block the UIKit event loop.
void iphone_soundToText(JCharP parameters, int32 parameterLength,
                        JCharP *outText, int32 *outLength)
{
   *outText = NULL;
   *outLength = 0;
   if ([NSThread isMainThread]) return;
   @autoreleasepool
   {
      NSString *raw = parameterLength > 0
         ? [NSString stringWithCharacters:(const unichar *)parameters
                                    length:(NSUInteger)parameterLength] : @"";
      NSString *title = NSLocalizedString(@"Voice input", nil);
      NSInteger timeout = 2000;
      for (NSString *part in [raw componentsSeparatedByString:@"|"])
      {
         if ([part hasPrefix:@"title:"])
            title = [part substringFromIndex:6];
         else if ([part hasPrefix:@"timeout:"])
         {
            NSInteger requested = [[part substringFromIndex:8] integerValue];
            if (requested > 0)
               timeout = MAX(500, MIN(requested, 30000));
         }
      }
      TCSpeechInput *operation = [[TCSpeechInput alloc] initWithTitle:title timeout:timeout];
      dispatch_async(dispatch_get_main_queue(), ^{
         if (activeSpeechInput)
            [operation cancel];
         else
         {
            activeSpeechInput = operation;
            [operation begin];
         }
      });
      if (dispatch_semaphore_wait([operation completion],
          dispatch_time(DISPATCH_TIME_NOW, 120 * NSEC_PER_SEC)) != 0)
      {
         dispatch_sync(dispatch_get_main_queue(), ^{
            [operation cancel];
         });
      }
      NSString *transcript = [operation text];
      if (transcript.length > 0 && transcript.length <= INT32_MAX / sizeof(JChar))
      {
         JCharP buffer = (JCharP)malloc(transcript.length * sizeof(JChar));
         if (buffer)
         {
            [transcript getCharacters:(unichar *)buffer];
            *outText = buffer;
            *outLength = (int32)transcript.length;
         }
      }
      [operation release];
   }
}
