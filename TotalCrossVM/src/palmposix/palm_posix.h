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



#ifndef __PALM_POSIX_H__
#define __PALM_POSIX_H__

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

typedef void (*alert_func)(char *s, ...);
typedef UInt32 (*get_appId_func)();

extern get_appId_func appId_cb;
extern alert_func alert_cb;

int  initPalmPosix(get_appId_func appId, alert_func alert);
void destroyPalmPosix();
void closeAllFiles();

#endif //  __PALM_POSIX_H__
