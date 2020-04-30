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

//typedef id Context;

#import "tcvm.h"
#import "startup.h"
#import "libtcvm.h"
#import "firebase.h"
@implementation tcvm

+ (NSInteger) startVM:(Context*)context appName:(char *)appName
{
    int32 ret = startVM(appName, context);
    if (ret == 0) tczLoad(*context, "LitebaseLib.tcz");
    return ret;
}

+ (NSInteger) startProgram:(Context) context
{
    return startProgram(context);
}

+ (void) privateOnMessageReceived: (NSString *)messageId messageType:(NSString *)messageType keys:(NSArray *) keys values:(NSArray *)values collapsedKey:(NSString *) collapsedKey ttl:(int)ttl {
    [FirebaseTCCallback privateOnMessageReceived:messageId messageType:messageType keys:keys values:values collapsedKey:collapsedKey ttl:ttl];
}

+ (void) privateOnTokenRefresh {
    [FirebaseTCCallback privateOnTokenRefresh];
}
@end
