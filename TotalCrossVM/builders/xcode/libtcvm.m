//
//  tcvm.m
//  tcvm
//
//  Created by Guilherme Hazan on 3/13/12.
//  Copyright (c) 2012 SuperWaba Ltda. All rights reserved.
//

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
