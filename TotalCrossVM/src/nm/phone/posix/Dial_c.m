// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only


#define Object NSObject*
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <QuartzCore/QuartzCore.h>
#define Class __Class
#include "../../nm/ui/darwin/mainview.h"
#include "GraphicsPrimitives.h"
typedef id Context;
#include "event.h"
#undef Class

void iphone_dialNumber(char* number)
{
    NSString* string = [NSString stringWithFormat:@"%s", number];
    [DEVICE_CTX->_mainview dialNumber:string];
}
