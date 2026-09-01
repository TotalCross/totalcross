// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

// Platform-specific code
#if TC_WINDOWING_SDL
 #include "sdl/specialkeys_c.h"
#elif TC_WINDOWING_NATIVE
 #if TC_OS_WINDOWS || TC_OS_WINCE
  #include "win/specialkeys_c.h"
 #elif TC_OS_ANDROID
  #include "android/specialkeys_c.h"
 #elif TC_OS_IOS
  #include "darwin/specialkeys_c.h"
 #elif TC_OS_LINUX
  #include "linux/specialkeys_c.h"
 #else
  #error Unsupported native special-key backend
 #endif
#else
 #error No special-key backend selected
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
