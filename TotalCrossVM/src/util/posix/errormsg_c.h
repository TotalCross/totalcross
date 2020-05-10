// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <string.h>

CharP privateGetErrorMessage(int32 errorCode, CharP buffer, uint32 size)
{
   if (strerror_r(errorCode, buffer, size))
      return null;
   return buffer;
}
