/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


#if HAVE_CONFIG_H
#include "config.h"
#endif

#define AUDIO_COMPILE_ISSUE

#import <UIKit/UIKit.h>
#import <Foundation/NSThread.h>
#include <stdio.h>
#include <dlfcn.h>
#import <UIKit/UIAlert.h>

/* undef rpl_realloc */
#undef realloc

typedef id Context;

typedef int  (*StartVMProc)             (char* args, Context context);
typedef void (*NotifyStopVMProc)        ();
typedef int  (*StartProgramProc)        (Context context);

typedef void *dlHandle;

static char *cmdLine;

@interface LauncherMain : NSObject <UIApplicationDelegate>
{
   int startupRC;
   StartProgramProc fStartProgram;
   dlHandle tcvm;
   Context context;
}
- (void)   mainLoop: (id)param;
- (float)  systemVolume;
- (void) fatalError: (NSString*)msg;
@end

@implementation LauncherMain

-(id) init
{
	[super init];
    fStartProgram = NULL;
	return self;
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
   [ alertView release ];
   [[UIApplication sharedApplication] terminate];
}

- (void) fatalError: (NSString*)msg
{
   UIAlertView *myAlert = [ [ UIAlertView alloc ]
      initWithTitle:@"Fatal Error"
      message: msg
      delegate:self
      cancelButtonTitle: nil
      otherButtonTitles: @"Quit", nil];
   [ myAlert show ];
}

- (void) mainLoop: (id)param
{
   [[NSAutoreleasePool alloc] init];

   if (startupRC == 0)
   {
      fStartProgram = (StartProgramProc)dlsym(tcvm, "startProgram");
      if (!fStartProgram)
      {
         [ self fatalError: @"Cannot find the 'startProgram' entry point" ];
         return;
      }
      fStartProgram(context);
   }

   dlclose(tcvm); // free the library
   free(cmdLine);
   [[UIApplication sharedApplication] terminate];
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
   // setup for device orientation change events
   [[ UIDevice currentDevice ] beginGeneratingDeviceOrientationNotifications ];

   tcvm = dlopen("libtcvm.dylib", RTLD_LAZY);                    // load in current folder - otherwise, we'll not be able to debug
   if (!tcvm)
      tcvm = dlopen("../libtcvm.dylib", RTLD_LAZY);              // load in parent folder
   if (!tcvm)
      tcvm = dlopen("/Applications/TotalCross.app/libtcvm.dylib", RTLD_LAZY); // load in most common absolute path
   if (!tcvm)
   {
      [ self fatalError: @"Cannot find the 'libtcvm' shared lib" ];
      return;
   }

   StartVMProc fStartVM = (StartVMProc)dlsym(tcvm, "startVM");
   if (!fStartVM)
   {
      [ self fatalError: @"Cannot find the 'startVM' entry point" ];
      return;
   }

   startupRC = fStartVM(cmdLine, &context);

   //printf("volume = %f\n", [self systemVolume]);
   [NSThread detachNewThreadSelector:@selector(mainLoop:) toTarget:self withObject:nil];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
   if ([NSThread isMainThread])
   {
      NotifyStopVMProc fNotifyStopVM = (NotifyStopVMProc)dlsym(tcvm, "notifyStopVM");
      if (!fNotifyStopVM)
      {
         [ self fatalError: @"Cannot find the 'notifyStopVM' entry point" ];
         return;
      }
      fNotifyStopVM();

      [ NSThread sleepForTimeInterval: 1800.0 ]; // wait a maximum of 30 minutes, once notified the VM thread should end before 
   }
}

// Getting volume
- (float)systemVolume
{
    float         volume = 0, volumeL, volumeR;
#ifndef AUDIO_COMPILE_ISSUE
    OSStatus      err;
    AudioDeviceID device;
    UInt32        size;
    UInt32        channels[2];

    size = sizeof device;
    err = AudioHardwareGetProperty(kAudioHardwarePropertyDefaultOutputDevice, &size, &device);

    size = sizeof channels;
    if (noErr == err) err = AudioDeviceGetProperty(device, 0, false, kAudioDevicePropertyPreferredChannelsForStereo, &size, &channels);

    size = sizeof volume;
    if (noErr == err) err = AudioDeviceGetProperty(device, channels[0], false, kAudioDevicePropertyVolumeScalar, &size, &volumeL);
    if (noErr == err) err = AudioDeviceGetProperty(device, channels[1], false, kAudioDevicePropertyVolumeScalar, &size, &volumeR);

    if (noErr == err)
    {
        // Select greatest volume
        volume = (volumeL < volumeR) ? volumeR : volumeL;
    }
    else
    {
        volume = 1.0;	// Fallback
    }
#endif
    return volume;
}

//- (void)setView: (TCView*)view
//{
//   _currentView = view;
//}

@end

#define LAUNCHED_FROM_SPRINGBOARD "--launchedFromSB"

int main(int argc, char *argv[])
{
   cmdLine = strdup(argv && argv[0] ? argv[0] : "");
   if (argc > 1) // if there's a commandline passed by the system or one passed by the user
   {
      cmdLine = (char*)realloc(cmdLine, strlen(cmdLine) + sizeof(" /cmd "));
      strcat(cmdLine, " /cmd ");
      char **p = argv + 1;
      int n = argc;
      while (n-- > 1)
      {
         // remove the "launched by the SpringBoard" option
         if (strcmp(*p, LAUNCHED_FROM_SPRINGBOARD))
         {
            cmdLine = (char*)realloc(cmdLine, strlen(cmdLine) + strlen(*p) + 2);
            strcat(cmdLine, " ");
            strcat(cmdLine, *p);
         }
         p++;
      }
   }

   [[NSAutoreleasePool alloc] init];
   return UIApplicationMain(argc, argv, nil, @"LauncherMain");
}
