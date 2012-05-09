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

@synthesize window = _window;
@synthesize viewController = _viewController;

-(id) init
{
	[super init];
    fStartProgram = NULL;
    fOrientationChanged = NULL;
	return self;
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    // setup for device orientation change events
    [[ UIDevice currentDevice ] beginGeneratingDeviceOrientationNotifications ];
    
    NSString* appNameKey = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleName"];
    const char* name = [[appNameKey stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] cStringUsingEncoding:NSASCIIStringEncoding];
    [tcvm startVM:&context appName:(char*)name];
    [Litebase libOpen:context];

    [NSThread detachNewThreadSelector:@selector(mainLoop:) toTarget:self withObject:nil];
}
/*
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    // Override point for customization after application launch.
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        self.viewController = [[ViewController alloc] initWithNibName:@"ViewController_iPhone" bundle:nil];
    } else {
        self.viewController = [[ViewController alloc] initWithNibName:@"ViewController_iPad" bundle:nil];
    }
    self.window.rootViewController = self.viewController;
    [self.window makeKeyAndVisible];
    return YES;
}
*/
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    
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

- (void) mainLoop: (id)param
{
    [[NSAutoreleasePool alloc] init];
    
    [tcvm startProgram:context];
    free(cmdLine);
    exit(0);
}

@end
