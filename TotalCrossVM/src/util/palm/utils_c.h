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

// $Id: utils_c.h,v 1.14 2011-01-04 13:31:04 guich Exp $

#include <DateTime.h>
#include <TimeMgr.h>

TC_API int32 getLastVolume()
{
   Err err;
   UInt16 vol;
   int32 lastVol = 0;
   UInt32 it = vfsIteratorStart | 0x80000000; // include hidden volumes

   while (it != vfsIteratorStop)
   {
      err = VFSVolumeEnumerate(&vol, &it);

      if (err == errNone)
         lastVol = (int32) vol;
      else
         break;
   }

   return lastVol;
}

int32 millisToTicks(int32 millis)
{
   millis = millis % (int32) ((1L << 30) / 1000L);
   return ((millis * SysTicksPerSecond()) / 1000L);
}

static int32 privateGetFreeMemory(bool maxblock)
{
   UInt32 maxP,freeBytesP; // guich@340_56
   MemHeapFreeBytes (0, &freeBytesP, &maxP);
   return maxblock ? maxP : freeBytesP;
}

static void privateSleep(uint32 millis)
{
   SysTaskDelay(millis > 0 ? millisToTicks(millis) : 0); // a sleep of 0 is important to give the next thread this timeslice
}

static int32 privateGetTimeStamp()
{
   return TimGetTicks() * (1000 / SysTicksPerSecond());
}

static Err privateListFiles(TCHARP path, int32 slot, TCHARPs** list, int32* count, Heap h, int32 options)
{
   char fileName[MAX_PATHNAME];
   CharP s;
   Err err = errNone;
   bool recursive = options & LF_RECURSIVE;
   UInt32 appid,type;

   if (slot == 0) // main memory?
   {
      MemHandle mh;
      int32 extra = 0, i;
      char buf5[5];
      *count = DmNumDatabases();

      if (options & LF_FILE_TYPE)
         extra += 5;
      if (options & LF_FILE_APPID)
         extra += 5;
      for (i = 0; i < *count; i++)
      {                            
         mh = DmGetDatabase(i); // guich@tc115_80: use this instead of GetNextDatabaseByTypeCreator, which is buggy
         DmDatabaseInfo(mh, fileName,0,0,0,0,0,0,0,0,&type,&appid);

         s = (CharP) heapAlloc(h, xstrlen(fileName) + 1 + extra);
         xstrcpy(s, fileName);
         if (options & LF_FILE_TYPE)
         {
            xstrcat(s,".");
            int2CRID(type,buf5);
            xstrcat(s,buf5);
         }
         if (options & LF_FILE_APPID)
         {
            xstrcat(s,".");
            int2CRID(appid,buf5);
            xstrcat(s,buf5);
         }

         *list = TCHARPsAdd(*list, s, h); // add to list
      }
      err = errNone;
   }
   else // nvfs volumes
   {
      FileRef fref;
      UInt16 slot16 = (UInt16) slot;
      UInt32 it;
      int32 len,pathlen=0;
      FileInfoType info;
      info.nameP = fileName;
      info.nameBufLen = sizeof(fileName);
      if (recursive) pathlen = xstrlen(path) + 1; // extra for the /

      if ((err = VFSFileOpen(slot16, path, vfsModeRead, &fref)) != errNone)
         return err;

      it = vfsIteratorStart;
      while (it != vfsIteratorStop) // fill array with entries
      {
         fileName[0] = 0; // clear string
         if ((err = VFSDirEntryEnumerate(fref, &it, &info)) == errNone)
         {
            if (info.attributes & vfsFileAttrDirectory) // is directory; append a slash to its path
            {
               len = xstrlen(fileName);
               fileName[len] = '/';
               fileName[len + 1] = 0;
            }
            else
            if ((info.attributes & vfsFileAttrVolumeLabel) && fileName[0] == 'V' && strEqn(&fileName[1], "OLUME.NAM", 9)) // is a volume; replace it with the real volume label
            {
               err = VFSVolumeGetLabel(slot16, &fileName[1], sizeof(fileName) - 3);
               fileName[0] = '[';
               len = xstrlen(fileName);
               fileName[len] = ']';
               fileName[len + 1] = 0;
            }
            s = (CharP) heapAlloc(h, xstrlen(fileName) + 1 + pathlen);
            if (recursive) // must include the path in the filename
            {
               xstrcpy(s, path);
               if (path[pathlen-2] != '/')
                  xstrcat(s, TEXT("/"));
            }
            xstrcat(s, fileName);
            *list = TCHARPsAdd(*list, s, h); // add to list
            (*count) ++;

            if (recursive && (info.attributes & vfsFileAttrDirectory))
            {
               err = privateListFiles(s, slot, list, count, h, recursive);
               if (err != errNone)
                  break; // allow the file to be closed
            }
         }
      }
      if (err == expErrEnumerationEmpty)
         err = errNone;
      VFSFileClose(fref);
   }

   return err;
}

static void privateGetDateTime(int32* year, int32* month, int32* day, int32* hour, int32* minute, int32* second, int32* millis)
{
   DateTimeType sysTime;
   TimSecondsToDateTime(TimGetSeconds(), &sysTime);

   *year    = sysTime.year;
   *month   = sysTime.month;
   *day     = sysTime.day;
   *hour    = sysTime.hour;
   *minute  = sysTime.minute;
   *second  = sysTime.second;
   *millis  = 0; // not supplied by system time
}
