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
#if defined (THEOS)
#import <IOKit/IOKitLib.h>
#endif

void getSerialNum(char *id, int maxlen)
{
#if defined (THEOS)
   CFTypeRef serialNumberAsCFString;

   io_service_t platformExpert = IOServiceGetMatchingService(kIOMasterPortDefault, IOServiceMatching("IOPlatformExpertDevice"));
   if (platformExpert)
   {
      serialNumberAsCFString = IORegistryEntryCreateCFProperty(platformExpert, CFSTR(kIOPlatformSerialNumberKey), kCFAllocatorDefault, 0);
      IOObjectRelease(platformExpert);

      const char *ser = [ (NSString*)serialNumberAsCFString UTF8String ];
      strncpy(id, ser, maxlen);
   }
   else
      strcpy(id, "unknown");
#endif
}

int getRomVersion()
{
   float ver = [[[UIDevice currentDevice] systemVersion] floatValue];
   return (int) (ver * 100);
}
