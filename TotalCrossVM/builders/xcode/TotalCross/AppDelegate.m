//
//  AppDelegate.m
//  TotalCross
//
//  Created by Guilherme Hazan on 3/7/12.
//  Copyright (c) 2012 SuperWaba Ltda. All rights reserved.
//

#include <stdio.h>
#include <dlfcn.h>
#import "AppDelegate.h"

#import "ViewController.h"
#import "libtcvm.h"
#import "liblitebase.h"

@implementation AppDelegate

#ifdef DEBUG
#define TCZNAME "AllTests"
#endif

-(void) initApp
{
   // list all files in the pkg folder. if there are new files or files with a newer creation date, copy it to the appPath
   NSString *pkgDirectory = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"pkg"];
   if ([[NSFileManager defaultManager] fileExistsAtPath:pkgDirectory]) // First check if the pkg directory exists
   {
      NSString *documentsDirectory = [NSHomeDirectory() stringByAppendingPathComponent:  @"Documents"];
      NSError *error = nil;
      NSArray *fileList = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:pkgDirectory error:&error];
      for (NSString *s in fileList)
      {
         NSString *targetFilePath = [documentsDirectory stringByAppendingPathComponent:s];
         NSString *sourceFilePath = [pkgDirectory stringByAppendingPathComponent:s];
         bool targetExists = [[NSFileManager defaultManager] fileExistsAtPath:targetFilePath];
         NSDate *sourceDate = [[[NSFileManager defaultManager] attributesOfItemAtPath:sourceFilePath error:&error] objectForKey:NSFileCreationDate];
         NSDate *targetDate = !targetExists ? nil : [[[NSFileManager defaultManager] attributesOfItemAtPath:targetFilePath error:&error] objectForKey:NSFileCreationDate];
         if (!targetExists || [targetDate compare:sourceDate] == NSOrderedAscending) // pkg date is more recent than target?
         {
            if (targetExists) //File exist, delete it
               [[NSFileManager defaultManager] removeItemAtPath:targetFilePath error:&error];
            [[NSFileManager defaultManager] copyItemAtPath:sourceFilePath toPath:targetFilePath error:&error];
         }
      }
   }
   
   // setup for device orientation change events
   [[ UIDevice currentDevice ] beginGeneratingDeviceOrientationNotifications ];
    
   const char* name =
#ifdef TCZNAME
       TCZNAME;
#else
       [[[[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleName"] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] cStringUsingEncoding:NSASCIIStringEncoding];
#endif
   NSInteger ret = [tcvm startVM:&context appName:(char*)name];
   if (ret != 0)
      exit((int)ret);
   else
   {
      [Litebase fillNativeProcAddressesLB];
      [NSThread detachNewThreadSelector:@selector(mainLoop:) toTarget:self withObject:nil];
   }
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    [self initApp];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    [self initApp];
    return YES;
}

void postOnMinimizeOrRestore(bool isMinimized);

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    postOnMinimizeOrRestore(true);
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    postOnMinimizeOrRestore(false);
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

- (void) mainLoop: (id)param
{
    [[NSAutoreleasePool alloc] init];
    [tcvm startProgram:context];
    exit(0);
}

@end
