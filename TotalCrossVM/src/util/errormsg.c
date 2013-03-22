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
