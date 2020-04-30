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

#include <stdio.h>
#include <dlfcn.h>
#import "AppDelegate.h"

#import "ViewController.h"
#import "libtcvm.h"
#import "liblitebase.h"

@import Firebase;

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
@import UserNotifications;
#endif

// Implement UNUserNotificationCenterDelegate to receive display notification via APNS for devices
// running iOS 10 and above.
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
@interface AppDelegate () <UNUserNotificationCenterDelegate>

@end

#endif

// Copied from Apple's header in case it is missing in some cases (e.g. pre-Xcode 8 builds).
#ifndef NSFoundationVersionNumber_iOS_9_x_Max
#define NSFoundationVersionNumber_iOS_9_x_Max 1299
#endif

@implementation AppDelegate

NSString *const kGCMMessageIDKey = @"gcm.message_id";

#ifdef DEBUG
#define TCZNAME "TotalCross"
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
    [[UIApplication sharedApplication] registerForRemoteNotifications];
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    [self initApp];
    
    NSString *filePath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"GoogleService-Info.plist"];
    FIROptions *options = [[FIROptions alloc] initWithContentsOfFile:filePath];
    
    if (options != nil) {
      [FIRApp configureWithOptions:options];
        
        // [START register_for_notifications]
        
        // Register for remote notifications. This shows a permission dialog on first run, to
        // show the dialog at a more appropriate time move this registration accordingly.
        if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_7_1) {
            // iOS 7.1 or earlier. Disable the deprecation warnings.
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
            UIRemoteNotificationType allNotificationTypes =
            (UIRemoteNotificationTypeSound |
             UIRemoteNotificationTypeAlert |
             UIRemoteNotificationTypeBadge);
            [application registerForRemoteNotificationTypes:allNotificationTypes];
#pragma clang diagnostic pop
        } else if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
            // iOS 8 or later
            UIUserNotificationType allNotificationTypes =
            (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
            UIUserNotificationSettings *settings =
            [UIUserNotificationSettings settingsForTypes:allNotificationTypes categories:nil];
            [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    } else {
            // iOS 10 or later
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
            // For iOS 10 display notification (sent via APNS)
            [UNUserNotificationCenter currentNotificationCenter].delegate = self;
            UNAuthorizationOptions authOptions =
            UNAuthorizationOptionAlert
            | UNAuthorizationOptionSound
            | UNAuthorizationOptionBadge;
            [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:authOptions completionHandler:^(BOOL granted, NSError * _Nullable error) {
                
            }];
        application.applicationIconBadgeNumber = 0;
        [FIRMessaging messaging].delegate = self;
        [FIRMessaging messaging].shouldEstablishDirectChannel = YES;
        
#endif
    }
        // [END register_for_notifications]
    }
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

// [START receive_message]
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    // If you are receiving a notification message while your app is in the background,
    // this callback will not be fired till the user taps on the notification launching the application.
    // TODO: Handle data of notification
    
    // With swizzling disabled you must let Messaging know about the message, for Analytics
    [[FIRMessaging messaging] appDidReceiveMessage:userInfo];
    // Print message ID.
    if (userInfo[kGCMMessageIDKey]) {
        NSLog(@"Message ID: %@", userInfo[kGCMMessageIDKey]);
    }
    // Print full message.
    NSLog(@"%@", userInfo);
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    // If you are receiving a notification message while your app is in the background,
    // this callback will not be fired till the user taps on the notification launching the application.
    // TODO: Handle data of notification
    
    // With swizzling disabled you must let Messaging know about the message, for Analytics
    [[FIRMessaging messaging] appDidReceiveMessage:userInfo];
    
    // Print message ID.
    if (userInfo[kGCMMessageIDKey]) {
        NSLog(@"Message ID: %@", userInfo[kGCMMessageIDKey]);
    }
    
    // Print full message.
    NSLog(@"%@", userInfo);
    
    completionHandler(UIBackgroundFetchResultNewData);
}
// [END receive_message]

// [START ios_10_message_handling]
// Receive displayed notifications for iOS 10 devices.
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
// Handle incoming notification messages while app is in the foreground.
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    NSDictionary *userInfo = notification.request.content.userInfo;
    
    // With swizzling disabled you must let Messaging know about the message, for Analytics
    [[FIRMessaging messaging] appDidReceiveMessage:userInfo];
    NSArray *allKeys = [userInfo allKeys];
    NSMutableArray *keyArray = [[NSMutableArray alloc] init];
    NSMutableArray *valueArray = [[NSMutableArray alloc] init];
    
    for (NSString *key in allKeys) {
        NSObject * obj = userInfo[key];
        if ([obj isKindOfClass:[NSString class]]) {
            [keyArray addObject:key];
            [valueArray addObject:userInfo[key]];
        }
    }
    // Print message ID.
    [tcvm privateOnMessageReceived: userInfo[kGCMMessageIDKey] messageType:NULL keys:keyArray values:valueArray collapsedKey:NULL ttl:0];
    
    // Change this to your preferred presentation option
    completionHandler(UNNotificationPresentationOptionNone);
}

// Handle notification messages after display notification is tapped by the user.
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)())completionHandler {
    NSDictionary *userInfo = response.notification.request.content.userInfo;
    if (userInfo[kGCMMessageIDKey]) {
        NSLog(@"Message ID: %@", userInfo[kGCMMessageIDKey]);
    }
    
    // Print full message.
    NSLog(@"%@", userInfo);
    
    completionHandler();
}
#endif
// [END ios_10_message_handling]

// [START refresh_token]
- (void)messaging:(nonnull FIRMessaging *)messaging didRefreshRegistrationToken:(nonnull NSString *)fcmToken {
    // Note that this callback will be fired everytime a new token is generated, including the first
    // time. So if you need to retrieve the token as soon as it is available this is where that
    // should be done.
    [tcvm privateOnTokenRefresh];
    // TODO: If necessary send token to application server.
}
// [END refresh_token]

// [START ios_10_data_message]
// Receive data messages on iOS 10+ directly from FCM (bypassing APNs) when the app is in the foreground.
// To enable direct data messages, you can set [Messaging messaging].shouldEstablishDirectChannel to YES.
- (void)messaging:(nonnull FIRMessaging *)messaging
didReceiveMessage:(nonnull FIRMessagingRemoteMessage *)remoteMessage {
    
    NSArray *allKeys = [remoteMessage.appData allKeys];
    NSMutableArray *keyArray = [[NSMutableArray alloc] init];
    NSMutableArray *valueArray = [[NSMutableArray alloc] init];
    
    for (NSString *key in allKeys) {
        NSObject * obj = remoteMessage.appData[key];
        if ([obj isKindOfClass:[NSString class]]) {
            [keyArray addObject:key];
            [valueArray addObject:remoteMessage.appData[key]];
        }
    }
    [tcvm privateOnMessageReceived: NULL messageType:NULL keys:keyArray values:valueArray collapsedKey:NULL ttl:0];
}
// [END ios_10_data_message]

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    NSLog(@"Unable to register for remote notifications: %@", error);
}

// This function is added here only for debugging purposes, and can be removed if swizzling is enabled.
// If swizzling is disabled then this function must be implemented so that the APNs device token can be paired to
// the FCM registration token.
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    NSLog(@"APNs device token retrieved: %@", deviceToken);
    
    // With swizzling disabled you must set the APNs device token here.
    [FIRMessaging messaging].APNSToken = deviceToken;
}


@end
