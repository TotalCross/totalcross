// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#if defined(WINCE) || defined(WIN32)
 #include "win/errormsg_c.h"
#else
 #include "posix/errormsg_c.h"
#endif

TC_API CharP getErrorMessage(int32 errorCode, CharP buffer, uint32 size)
{
   return privateGetErrorMessage(errorCode, buffer, size);
}
