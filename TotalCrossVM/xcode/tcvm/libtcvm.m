// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

//typedef id Context;

#import "tcvm.h"
#import "startup.h"
#import "libtcvm.h"
#import "nm/firebase/firebase.h"

extern const char totalcrossBuildIdentityMarker[];
extern const char totalcrossSourceTreeMarker[];

@implementation tcvm

+ (NSInteger) startVM:(Context*)context appName:(char *)appName
{
    // Keep the identity marker object when libtcvm.a is linked into the app.
    if (totalcrossBuildIdentityMarker[0] != 'T' || totalcrossSourceTreeMarker[0] != 'T') {
        return -1;
    }
    int32 ret = startVM(appName, context);
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
