/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: specialkeys.c,v 1.8 2011-01-04 13:31:03 guich Exp $

#include "tcvm.h"

// Platform-specific code
#if defined(PALMOS)
 #include "palm/specialkeys_c.h"
#elif defined(WINCE) || defined(WIN32)
 #include "win/specialkeys_c.h"
#elif defined(darwin)
 #include "darwin/specialkeys_c.h"
#elif defined(linux)
 #include "linux/specialkeys_c.h"
#elif defined(__SYMBIAN32__)
 #include "symbian/specialkeys_c.h"
#elif defined(ANDROID)
 #include "android/specialkeys_c.h"
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
