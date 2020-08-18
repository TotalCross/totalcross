#include "xtypes.h"
#import <Foundation/Foundation.h>

bool
getElapsed(int32* value)
{
  bool ret = false;
  CFNumberRef cfNumber = (CFNumberRef)CFPreferencesCopyAppValue(
    CFSTR("ttl"), CFSTR("com.totalcross.iphone.TotalCross"));
  if (cfNumber != null) {
    ret = CFNumberGetValue(cfNumber, kCFNumberSInt32Type, value);
    [(id)cfNumber release];
  }
  return false;
}

bool
setElapsed(int32 value)
{
  NSNumber* n = [NSNumber numberWithInt:value];
  CFPreferencesSetAppValue(CFSTR("ttl"),
                           (__bridge CFNumberRef)n,
                           CFSTR("com.totalcross.iphone.TotalCross"));
  [n release];
  return CFPreferencesAppSynchronize(CFSTR("com.totalcross.iphone.TotalCross"));
}
