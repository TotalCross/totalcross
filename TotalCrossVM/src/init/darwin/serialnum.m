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



#import <Foundation/Foundation.h>
#import <UIKit/UIDevice.h>
#import "UIDevice+IdentifierAddition.h"
#import <AdSupport/ASIdentifierManager.h>

void getSerialNum(char *id, int maxlen)
{
    float ver = [[[UIDevice currentDevice] systemVersion] floatValue];
    NSString* nsserial = ver >= 6 ? [[[ASIdentifierManager sharedManager] advertisingIdentifier] UUIDString] : // new for iOS 6 or above
                                    [[UIDevice currentDevice] uniqueGlobalDeviceIdentifier];
    if (nsserial != nil)
    {
       const char* serial = [nsserial cStringUsingEncoding:NSASCIIStringEncoding];
       strncpy(id, serial, maxlen);
    }
}

int getRomVersion()
{
   float ver = [[[UIDevice currentDevice] systemVersion] floatValue];
   return (int) (ver * 100);
}
