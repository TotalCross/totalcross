/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
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

/* not used!
int iphone_cameraClick(int w, int h, char* fileName)
{
    NSString* string = [NSString stringWithFormat:@"%s", fileName];
    int ret = [DEVICE_CTX->_mainview cameraClick:string width:w height:h];
    return ret;
}
*/