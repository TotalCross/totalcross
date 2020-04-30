// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

// Platform-specific code
#if defined(WP8)
 #include "wp8/specialkeys_c.h"
#elif defined(WINCE) || defined(WIN32)
 #include "win/specialkeys_c.h"
#elif defined(darwin)
 #include "darwin/specialkeys_c.h"
#elif defined(ANDROID)
 #include "android/specialkeys_c.h"
#elif defined(linux)
 #include "linux/specialkeys_c.h"
#endif
//

int32 keyPortable2Device(PortableSpecialKeys key)
{
   return privateKeyPortable2Device(key);
}

PortableSpecialKeys keyDevice2Portable(int32 key)
{
   return privateKeyDevice2Portable(key);
}

PortableModifiers keyGetPortableModifiers(int32 mods)
{
   return privateKeyGetPortableModifiers(mods);
}
