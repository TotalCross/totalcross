#import <Foundation/Foundation.h>
#include "xtypes.h"

bool getElapsed(int32 *value)
{
   CFNumberRef cfNumber = (CFNumberRef)CFPreferencesCopyAppValue(CFSTR("ttl"), CFSTR("com.totalcross.iphone.TotalCross"));
   if (cfNumber != null)
      return CFNumberGetValue(cfNumber, kCFNumberSInt32Type, value);
   return false;
}

bool setElapsed(int32 value)
{
    NSNumber* n = [NSNumber numberWithInt:value];
   CFPreferencesSetAppValue(CFSTR("ttl"), (__bridge CFNumberRef)n, CFSTR("com.totalcross.iphone.TotalCross"));
    [n release];
   return CFPreferencesAppSynchronize(CFSTR("com.totalcross.iphone.TotalCross"));
}
