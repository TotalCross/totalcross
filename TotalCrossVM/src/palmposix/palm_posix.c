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

// $Id: palm_posix.c,v 1.15 2011-01-04 13:31:15 guich Exp $

#if HAVE_CONFIG_H
#include "config.h"
#endif

#include "palm_posix.h"
#include "stdlib.h"

get_appId_func appId_cb;
alert_func alert_cb;
extern UInt16 currentOwnerID;
SysAppInfoPtr SysGetAppInfo(SysAppInfoPtr *uiAppPP, SysAppInfoPtr *actionCodeAppPP) SYS_TRAP(sysTrapSysGetAppInfo);

int initPalmPosix(get_appId_func appId, alert_func alert)
{
   SysAppInfoPtr anAppInfoP;
   appId_cb = appId;
   alert_cb = alert;
   currentOwnerID = SysGetAppInfo(&anAppInfoP, &anAppInfoP)->memOwnerID;

   MemHeapCompact(0); // compact the heap since we will create only nonmovable chunks
   return 1;
}

void destroyPalmPosix()
{
   closeAllFiles();
}
