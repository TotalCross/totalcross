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

// $Id: Launcher.c,v 1.20 2011-03-23 18:17:05 guich Exp $

#if HAVE_CONFIG_H
#include "config.h"
#endif

#include <PalmOS.h>
#include "resources.h"
#include "peal.h"

#define Swap32(n) (((((unsigned long) n) << 24) & 0xFF000000) |   \
                   ((((unsigned long) n) <<  8) & 0x00FF0000) |   \
                   ((((unsigned long) n) >>  8) & 0x0000FF00) |   \
                   ((((unsigned long) n) >> 24) & 0x000000FF))

uint32_t pealLoadLibrary(const char *name, DmResType resType, DmResID resID);
uint32_t pealUnloadLibrary(PealModule *module);
uint32_t pealGetProcAddress(PealModule *module, char *func);
void pealAlert(char *s);

PealModule *pealMod;

UInt32 PilotMain (UInt16 launchCode, MemPtr cmdPBP, UInt16 launchFlags)
{
   UInt32 status = 0;

   // 0. currently, we only process normal launch codes
   if (launchCode == sysAppLaunchCmdNormalLaunch)
   {
      char launchString68k[256];
      UInt32 processorType;
      uint32_t *pealLoadLibrary68K, *pealUnloadLibrary68K, *pealGetProcAddress68K, *pealAlert68K;
      void *executeProgram;

      // 1. check the processor type
      FtrGet(sysFileCSystem, sysFtrNumProcessorID, &processorType);
      if (!sysFtrNumProcessorIsARM(processorType))
         return FrmCustomAlert(OKAlert, "The TotalCross VM requires an ARM cpu type.", "", "") + 1;

      // 2. load the TotalCross library
      pealMod = (PealModule*)pealLoadLibrary(NULL, 'ARMC', TCVM_CODE_RESID);
      if (!pealMod)
         return FrmCustomAlert(OKAlert, "Unable to load TotalCross VM.", "Have you installed it?", "") + 2;

      // 3. fill some variables in the ARM code, so the ARM code can call functions in 68k mode
      pealLoadLibrary68K    = (void *)PealLookupSymbol(pealMod, "pealLoadLibrary68K");
      pealUnloadLibrary68K  = (void *)PealLookupSymbol(pealMod, "pealUnloadLibrary68K");
      pealGetProcAddress68K = (void *)PealLookupSymbol(pealMod, "pealGetProcAddress68K");
      pealAlert68K          = (void *)PealLookupSymbol(pealMod, "pealAlert68K");
      executeProgram        = (void *)PealLookupSymbol(pealMod, "executeProgram");
      if (!pealLoadLibrary68K || !pealUnloadLibrary68K || !pealGetProcAddress68K || !executeProgram || !pealAlert68K)
         return FrmCustomAlert(OKAlert, "Unable to find entry points!","","") + 3;
      else
      {
         UInt16 cardNo;
         LocalID dbID;

         // store the 68k function pointers into the arm pointers
         *pealLoadLibrary68K    = Swap32(&pealLoadLibrary);
         *pealUnloadLibrary68K  = Swap32(&pealUnloadLibrary);
         *pealGetProcAddress68K = Swap32(&pealGetProcAddress);
         *pealAlert68K          = Swap32(&pealAlert);

         // get the creator id of the running app
         if (SysCurAppDatabase(&cardNo, &dbID) == errNone && DmDatabaseInfo(cardNo, dbID, launchString68k,0,0,0,0,0,0,0,0,0,0) == errNone)
         {
            FtrUnregister('PWER',1); // remove any previously registered flag
            SysNotifyRegister(cardNo, dbID, sysNotifyLateWakeupEvent, NULL, sysNotifyNormalPriority, NULL); // guich@tc110_63: register for wakeup event
            if (cmdPBP) // if there's a commandline, append to the current line
               StrCat(StrCat(launchString68k," /cmd "),cmdPBP);
            // 4. get the address to the startup ARM code
            status = PealCall(pealMod, executeProgram, launchString68k);
            SysNotifyUnregister(cardNo, dbID, sysNotifyLateWakeupEvent, sysNotifyNormalPriority);
         }
      }
      pealUnloadLibrary(pealMod);
   }
   else
   if (launchCode == sysAppLaunchCmdNotify && ((SysNotifyParamType*)cmdPBP)->notifyType == sysNotifyLateWakeupEvent) // guich@tc110_63: have to use launchcodes to handle this
      FtrSet('PWER',1,1);
   return status;
}

void pealAlert(char *s)
{
   FrmCustomAlert(OKAlert, s,"","");
}


// public 68k function callable from the ARM code
uint32_t pealLoadLibrary(const char *name, DmResType resType, DmResID resID)
{
   uint32_t ret;
   LocalID dbID;
   DmOpenRef dbRef;

   if (!name) // the default place for code is always the TCVM libr
      name = "TCVM";

   dbID = DmFindDatabase(0,name);
   if (!dbID)
      return 0;
   dbRef = DmOpenDatabase (0, dbID, dmModeReadOnly); // guich@tc126_70: open as readonly instead of readwrite
   if (!dbRef)
      return 0;
   return (uint32_t)PealLoadFromResources(dbRef, resType, resID);
}

// public 68k function callable from the ARM code
uint32_t pealUnloadLibrary(PealModule *module)
{
   PealUnload(module);
   return 1;
}

// public 68k function callable from the ARM code
uint32_t pealGetProcAddress(PealModule *module, char *func)
{
   return (uint32_t)PealLookupSymbol(!module ? pealMod : module, func);
}
