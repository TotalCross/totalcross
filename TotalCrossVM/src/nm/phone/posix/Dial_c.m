/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/


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
