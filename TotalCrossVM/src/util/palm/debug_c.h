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



#include "resources.h"
#include "VFSMgr.h"

// Debug
#define titleSystemOut "DebugConsole"
#define IS_TITLE(x) (strEq((CharP)x,titleSystemOut,xstrlen(titleSystemOut)))

#define vfsIncludePrivateVolumes	0x80000000	/**< Use this flag to show hidden volumes if the device supports hidden volumes */
#define DEBUG_MAX_SIZE 32000 // guich@580_36: increased from 10000

typedef enum
{
   DEBUG_CLOSED,
   DEBUG_OPEN,
   DEBUG_ERASED,
   DEBUG_UNDEFINED,
} DebugState;

uint16 volNVFS;
FileRef debugFH;
UInt32 debugSize;
uint16 debugPos;
bool recurseDebug;
DebugState debugState;

Err syncDB(DmOpenRef dbRef)
{
   struct
   {
      DmOpenRef dbRef;
   } __attribute((packed)) sync_database_args;

   sync_database_args.dbRef = (DmOpenRef)SWAP32_FORCED(dbRef);
   return (UInt32)(*gCall68KFuncP)(gEmulStateP, PceNativeTrapNo(sysTrapDmSyncDatabase), &sync_database_args, sizeof(sync_database_args));
}

static bool getNVFSVolume(uint16 *vol)
{
   Err err;
   bool ret = false;
   VolumeInfoType volInfo;
	UInt32 vfsMgrVersion;
	if (FtrGet(sysFileCVFSMgr, vfsFtrIDVersion, &vfsMgrVersion) == errNone)
	{
      UInt32 volIterator = vfsIteratorStart | vfsIncludePrivateVolumes;
      while (volIterator != vfsIteratorStop)
      {
         if ((err = VFSVolumeEnumerate(vol, &volIterator)) == errNone)
         {
            err = VFSVolumeInfo(*vol, &volInfo);
            if (err)
               break;
            if (volInfo.attributes & vfsVolumeAttrHidden)
            {
               ret = true;
               break;
            }
         }
      }
   }
   return ret;
}

static bool privateInitDebug()
{
   if (!getNVFSVolume(&volNVFS))
   {
      alert("This is not a NVFS device! Aborting...");
      return false;
   }
   return true;
}

static void debugOpen()
{
   if (VFSFileOpen(volNVFS, "/DebugConsole", vfsModeReadWrite|vfsModeCreate, &debugFH) == errNone)
   {
      VFSFileSize(debugFH, &debugSize); // get size
      VFSFileSeek(debugFH, vfsOriginEnd, 0);     // and seek to end
      debugState = DEBUG_OPEN;
   }
}

void closeDebug()
{
   if (debugState == DEBUG_OPEN)
   {
      VFSFileClose(debugFH);
      debugState = DEBUG_CLOSED;
   }
}

void privateDestroyDebug()
{
   closeDebug();
}

static bool privateDebug(CharP msg)   // guich@310_22
{
   int32 slen = msg?xstrlen(msg):0;
   bool result = false;

   /* protect against recurse calls */
   if (recurseDebug) return true;
   recurseDebug = true;

   if (debugState == DEBUG_CLOSED) // start the db
      debugOpen();
   if (debugState == DEBUG_OPEN)
   {
      UInt32 written;
      if (strEq(msg,ERASE_DEBUG_STR))
      {
         closeDebug();
         VFSFileDelete(volNVFS, "/DebugConsole");
      }
      else
      {
         /*if (debugSize > DEBUG_MAX_SIZE)
         {
            debugSize = 0;
            VFSFileResize(debugFH, 0); // truncate
         }*/
         result = VFSFileWrite(debugFH, slen, msg, &written) == errNone;
         VFSFileSize(debugFH, &debugSize); // get size
         VFSFileResize(debugFH, ++debugSize); // guich@584_1: force the file flush
         VFSFileWrite(debugFH, 1, "\n", &written);
      }
   }
   recurseDebug = false;
   return result;
}

// Alert
static void privateAlert(CharP str)
{
   struct {CharP str;} __attribute((packed)) args;
   args.str = (CharP)SWAP32_FORCED(str);
   (*(gCall68KFuncP))(gEmulStateP, (uint32)pealAlert68K, &args, sizeof(args));
   if (supportsDIA) PINSetInputTriggerState(pinInputTriggerEnabled); // reenables the trigger for dynamic input areas
}
