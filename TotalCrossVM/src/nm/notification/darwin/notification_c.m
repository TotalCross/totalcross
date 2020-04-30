// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifdef __OBJC__
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>

@import UserNotifications;

#endif

#include "tcvm.h"

Err NmNotify(TCObject title, TCObject text)
{
	NSString *nsTitle = [[[NSString alloc]
							initWithCharacters:String_charsStart(title) 
							length:String_charsLen(title)
						] autorelease];
	NSString *nsText = [[[NSString alloc]
							initWithCharacters:String_charsStart(text) 
							length:String_charsLen(text)
						] autorelease];
    
    if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_7_1) {
        // iOS 7.1 or earlier. Disable the deprecation warnings.
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
#pragma clang diagnostic pop
    } else if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
        // iOS 8 or later
	    UILocalNotification* localNotification = [[UILocalNotification alloc] init];
	    localNotification.fireDate = [NSDate dateWithTimeIntervalSinceNow:1];
	    localNotification.alertTitle = nsTitle;
	    localNotification.alertBody = nsText;
	    localNotification.timeZone = [NSTimeZone defaultTimeZone];
	    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    } else {
        // iOS 10 or later
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
	    UNMutableNotificationContent *content = [UNMutableNotificationContent new];
		content.title = nsTitle;
		content.body = nsText;
		content.sound = [UNNotificationSound defaultSound];
        [content setValue:@"YES" forKey:@"shouldAlwaysAlertWhileAppIsForeground"];
		
        UNTimeIntervalNotificationTrigger *trigger =
			[UNTimeIntervalNotificationTrigger 
				triggerWithTimeInterval:1
				repeats:NO
			];
		
        NSString *identifier = @"UYLLocalNotification";
        UNNotificationRequest *request =
			[UNNotificationRequest 
				requestWithIdentifier:identifier
				content:content 
				trigger:trigger
			];

		UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
		[center addNotificationRequest:request withCompletionHandler:^(NSError * _Nullable error) {
            [nsTitle release];
            [nsTitle release];
            if (error != nil) {
		        NSLog(@"Something went wrong: %@",error);
		    }
		}];
#endif
    }
    
    return NO_ERROR;
}
