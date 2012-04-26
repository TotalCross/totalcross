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

@implementation tcvm

+ (NSInteger) startVM:(Context*)context appName:(char *)appName
{
    return startVM(appName, context);
}

+ (NSInteger) startProgram:(Context) context
{
    return startProgram(context);
}


@end

int teste()
{
    return 0;
}
