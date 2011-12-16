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



#include <PalmOSARM.h>
#include <System/Emul68K.h>

/*typedef struct
{
   UInt32 pilotMainBranch;
   UInt32 dispatchTableOffset;
   UInt32 shlIndex;
   UInt32 version;
   UInt32 entries;
   UInt32 more[4];
} ModuleHeaderType;
*/

VoidP privateLoadLibrary(CharP libName)
{
   struct
   {
      CharP name;
      DmResType resType;
      DmResID resID;
   } __attribute((packed)) args;
   args.name = (char*)SWAP32_FORCED(libName);
   args.resType = SWAP32_FORCED('ARMC');
   args.resID = SWAP16_FORCED(0x1000);
   return (void*)(*gCall68KFuncP)(gEmulStateP, (int32)pealLoadLibrary68K, &args, sizeof(args));
}

void privateUnloadLibrary(VoidP libPtr)
{
   struct
   {
      void *module;
   } __attribute((packed)) args;
   args.module = (void*)SWAP32_FORCED(libPtr);
   (*gCall68KFuncP)(gEmulStateP, (int32)pealUnloadLibrary68K, &args, sizeof(args));
}

VoidP privateGetProcAddress(const VoidP module, const CharP funcName)
{
   void* ret;
   struct
   {
      void *module;
      char *func;
   } __attribute((packed)) args;

   args.module = (void*)SWAP32_FORCED(module);
   args.func = (char *)SWAP32_FORCED(funcName);
   ret = (void*)(*gCall68KFuncP)(gEmulStateP, (int32)pealGetProcAddress68K, &args, sizeof(args));
   return ret;
}

Err LinkModule(UInt32 type, UInt32 creator, void **dispatchTableP, UInt32 *numEntriesP)
{
   UInt32 refNum;

   Err err = SysFindModule(type, creator, 0, 0, &refNum);
   if (err) err = SysLoadModule(type, creator, 0, 0, &refNum);
   if (err) return err;

   DmOpenRef dbP = DmOpenDatabaseByTypeCreator(type, creator, dmModeReadOnly);
   if (dbP == NULL)
   {
      err = DmGetLastErr();
      return err;
   }

   MemHandle resH = DmGet1Resource(sysResTModuleCode, 0);
   if (resH != NULL)
   {
      void *resP = MemHandleLock(resH);
      if (resP)
      {
         ModuleHeaderType *header = (ModuleHeaderType *)resP;
         *dispatchTableP = (UInt8 *)resP + header->dispatchTableOffset;
         if (numEntriesP) *numEntriesP = header->entries;
         MemHandleUnlock(resH);
      }
      DmReleaseResource(resH);
   }
   return err;
}
