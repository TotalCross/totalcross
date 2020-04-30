// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <UIKit/UIKit.h>

#define Class __Class
#include "tcvm.h"
#undef Class

void privateGetInt(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   CFStringRef cfValue = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8);
   CFNumberRef cfNumber = (CFNumberRef)CFPreferencesCopyAppValue(cfValue, kCFPreferencesCurrentApplication);
   if (cfNumber != null)
   {
     Boolean ok = CFNumberGetValue(cfNumber, kCFNumberSInt32Type, &p->retI);
     if (ok)
        return;
   }
   throwException(p->currentContext, ExceptionClass, "Cannot read value");
}

void privateGetString(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   CFStringRef cfValue = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8);
   CFStringRef cfData = (CFStringRef)CFPreferencesCopyAppValue(cfValue, kCFPreferencesCurrentApplication);

   if (cfData != null)
   {
      int tries = 7;
      int size = 256;
      char *buf = (char*)xmalloc(size);
      Boolean ok;
      do
         ok = CFStringGetCString (cfData, buf, size, kCFStringEncodingUTF8);
      while (tries-- > 0 && !ok);
      if (ok)
      {
         p->retO = createStringObjectFromCharP(p->currentContext, buf, xstrlen(buf));
         setObjectLock(p->retO, UNLOCKED);
      }
      xfree(buf);
      if (ok)
         return;
   }
   throwException(p->currentContext, ExceptionClass, "Cannot read value");
}

void privateGetBlob(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   CFStringRef cfValue = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8);
   CFDataRef cfData = (CFDataRef)CFPreferencesCopyAppValue(cfValue, kCFPreferencesCurrentApplication);
   if (cfData != null)
   {
      if ((p->retO = createByteArray(p->currentContext, (int32)CFDataGetLength(cfData))) != null)
      {
         CFDataGetBytes(cfData, CFRangeMake(0,CFDataGetLength(cfData)), ARRAYOBJ_START(p->retO));
         setObjectLock(p->retO, UNLOCKED);
         return;
      }
   }
   throwException(p->currentContext, ExceptionClass, "Cannot read value");
}

void privateSetInt(Context c, int32 hk, TCHARP key, TCHARP value, int32 data)
{
   CFStringRef cfValue = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8);
   CFNumberRef aCFNumber = CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, &data);
   CFPreferencesSetAppValue(cfValue, aCFNumber, kCFPreferencesCurrentApplication);
   // Write out the preference data.
   if (!CFPreferencesAppSynchronize(kCFPreferencesCurrentApplication))
      throwException(c, ExceptionClass, "Cannot write value.");
}

void privateSetString(Context c, int32 hk, TCHARP key, TCHARP value, CharP data)
{
   CFStringRef cfValue = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8);
   CFStringRef cfData = CFStringCreateWithCString(kCFAllocatorDefault, data, kCFStringEncodingUTF8);
   CFPreferencesSetAppValue(cfValue, cfData, kCFPreferencesCurrentApplication);
   // Write out the preference data.
   if (!CFPreferencesAppSynchronize(kCFPreferencesCurrentApplication))
      throwException(c, ExceptionClass, "Cannot write value.");
}

void privateSetBlob(Context c, int32 hk, TCHARP key, TCHARP value, uint8* data, int32 len)
{
   CFStringRef cfValue = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8);
   CFDataRef cfData = CFDataCreate (kCFAllocatorDefault, data, len);
   CFPreferencesSetAppValue(cfValue, cfData, kCFPreferencesCurrentApplication);
   // Write out the preference data.
   if (!CFPreferencesAppSynchronize(kCFPreferencesCurrentApplication))
      throwException(c, ExceptionClass, "Cannot write value.");
}

bool privateDelete(int32 hk, TCHARP key, TCHARP value)
{
   CFStringRef cfValue = CFStringCreateWithCString(kCFAllocatorDefault, value, kCFStringEncodingUTF8);
   CFPreferencesSetAppValue(cfValue, NULL, kCFPreferencesCurrentApplication);
   // Write out the preference data.
   return CFPreferencesAppSynchronize(kCFPreferencesCurrentApplication);
}

TCObject privateList(Context c, int32 hk, TCHARP key)
{
   return null;
}
