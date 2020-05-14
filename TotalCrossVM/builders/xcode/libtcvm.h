// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <Foundation/Foundation.h>

@interface tcvm : NSObject

+ (NSInteger) startVM:(Context*)context appName:(char *)appName;

+ (NSInteger) startProgram:(Context) context;

+ (void) privateOnMessageReceived: (NSString *)messageId messageType:(NSString *)messageType keys:(NSArray *) keys values:(NSArray *)values collapsedKey:(NSString *) collapsedKey ttl:(int)ttl;

+ (void) privateOnTokenRefresh;

@end
