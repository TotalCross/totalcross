//
//  Litebase.m
//  Litebase
//
//  Created by Guilherme Hazan on 4/30/12.
//  Copyright (c) 2012 SuperWaba Ltda. All rights reserved.
//

#import "tcvm.h"
#import "NativeMethods.h"
#import "liblitebase.h"
#import "nativeProcAddressesLB.h"

@implementation Litebase

+ (void) libOpen:(Context)context
{
    TOpenParams params;
    params.alert = alert;
    params.commandLine = commandLine;
    params.currentContext = context;
    params.getProcAddress = getProcAddress;
    LibOpen(&params);
    fillNativeProcAddressesLB();
}

@end
