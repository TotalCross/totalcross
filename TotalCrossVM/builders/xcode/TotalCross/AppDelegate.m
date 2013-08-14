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

//#define APPNAME "TCTestWin"

-(void) initApp
{
   // This is the first launch ever
   NSString* currentVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];
   NSString* storedVersion  = [[NSUserDefaults standardUserDefaults] stringForKey:@"LastVersion"];
   
   if (!storedVersion || ![currentVersion isEqualToString:storedVersion])
   {
      NSString *documentsDirectory = [NSHomeDirectory() stringByAppendingPathComponent:  @"Documents"];
      NSString *pkgDirectory = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"pkg"];
      NSError *error = nil;

      // First check if the pkg directory exists
      if ([[NSFileManager defaultManager] fileExistsAtPath:pkgDirectory])
      {
         NSArray *fileList = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:pkgDirectory error:&error];
         for (NSString *s in fileList)
         {
            NSString *targetFilePath = [documentsDirectory stringByAppendingPathComponent:s];
            NSString *sourceFilePath = [pkgDirectory stringByAppendingPathComponent:s];
            if ([[NSFileManager defaultManager] fileExistsAtPath:targetFilePath]) //File exist, delete it
               [NSFileManager removeItemAtPath: targetFilePath error:NULL];
            [[NSFileManager defaultManager] copyItemAtPath:sourceFilePath toPath:targetFilePath error:&error];
         }
      }
       
      [[NSUserDefaults standardUserDefaults] setObject:currentVersion forKey:@"LastVersion"];
      [[NSUserDefaults standardUserDefaults] synchronize];
   }
   
    // setup for device orientation change events
    [[ UIDevice currentDevice ] beginGeneratingDeviceOrientationNotifications ];
    
    NSString* appNameKey = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleName"];

    const char* name = 
#ifdef APPNAME
       APPNAME;
#else
       [appNameKey stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] cStringUsingEncoding:NSASCIIStringEncoding];
#endif
    [tcvm startVM:&context appName:(char*)name];
    [Litebase fillNativeProcAddressesLB];
    
    [NSThread detachNewThreadSelector:@selector(mainLoop:) toTarget:self withObject:nil];
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    [self initApp];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
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
