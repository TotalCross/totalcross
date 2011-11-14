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



#include <FeatureMgr.h>
#include <VfsMgr.h>
#include <DateTime.h>
#include <TimeMgr.h>

#define IS_DEBUG_CONSOLE(path) (xstrstr(path,"DebugConsole") != null)

//---------------------------------------------------
// INTERNAL USE ONLY
//---------------------------------------------------

static void splitPath(const CharP path, CharP parent, CharP fileName)
{
   int32 size, len;
   CharP first, last, slash;

   len = xstrlen(path);
   slash = path + len - 1;

   while (slash >= path && *slash != '/')
      slash --;

   if (parent != null) // get the parent
   {
      first = path;
      last = slash;
      size = last - first + 1;

      if (parent != null)
      {
         xmemmove(parent, first, size);
         parent[size] = 0;
      }
   }

   if (fileName != null) // get the filename
   {
      first = slash + 1;
      last = path + len - 1;
      size = last - first + 1;

      if (fileName != null)
      {
         xmemmove(fileName, first, size);
         fileName[size] = 0;
      }
   }
}

//---------------------------------------------------
// NATIVE METHODS
//---------------------------------------------------

__attribute__((unused)) static bool fileIsCardInserted(int32 slot)
{
   VolumeInfoType volInfo;
   UInt16 vol;
   int32 lastVol = 0;
   UInt32 it = expIteratorStart;

   if (slot == lastVolume)
   {
      while (it != expIteratorStop)
      {
         if (ExpSlotEnumerate(&vol, &it) == errNone)
            lastVol = (int32) vol;
         else
            break;
      }
      if (lastVol && (ExpCardPresent(lastVol) == errNone))
         return true;
      return false;
   }
   return (VFSVolumeInfo(slot, &volInfo) != errNone);
}

__attribute__((unused)) static Err fileCreate(NATIVE_FILE* fref, TCHARP path, int32 mode, int32* slot)
{
   Err err;
   bool erase = false;
   bool create = false;
   NATIVE_FILE ref;
   UInt16 slot16;
   UInt16 mode16;
   UInt32 attr;

   switch (mode)
   {
      case READ_WRITE:     mode = vfsModeReadWrite;                                 break;
      case CREATE_EMPTY:   mode = vfsModeReadWrite;   create = true; erase = true;  break;
      case CREATE:         mode = vfsModeReadWrite;   create = true;                break;
      case READ_ONLY:      mode = vfsModeRead;                                      break;
   }

   fref->handle = 0;
   slot16 = (UInt16) *slot;
   mode16 = (UInt16) mode;

   if (erase) // mode == CREATE_EMPTY
   {
      err = VFSFileDelete(slot16, path);
      if (err != errNone && err != vfsErrFileNotFound)
         return err;
   }

   if (create) // mode == CREATE or mode == CREATE_EMPTY
   {
      err = VFSFileCreate(slot16, path);
      if (err != errNone && err != vfsErrFileAlreadyExists)
         return err;
   }

   err = VFSFileOpen(slot16, path, mode16, &ref.handle);
   if (err != errNone)
      return err;

   VFSFileGetAttributes(ref.handle, &attr);
   if (attr & vfsFileAttrDirectory) // file is dir, so close it
   {
      VFSFileClose(ref.handle);
      ref.handle = 0;
   }

   fref->handle = ref.handle;
   return errNone;
}

__attribute__((unused)) static Err fileClose(NATIVE_FILE* fref)
{
   NATIVE_FILE ref;

   ref.handle = fref->handle;
   fref->handle = 0;
   return VFSFileClose(ref.handle);
}

__attribute__((unused)) static bool fileExists(TCHARP path, int32 slot)
{
   Err err;
   FileRef fref;

   if ((err = VFSFileOpen((UInt16) slot, path, vfsModeRead, &fref)) == errNone)
      VFSFileClose(fref);

   return (err == errNone || err == vfsErrFileStillOpen || err == vfsErrFilePermissionDenied);
}

__attribute__((unused)) static Err fileCreateDir(TCHARP path, int32 slot)
{
   TCHARP c;
   Err err;

   if (VFSDirCreate((UInt16) slot, path) != errNone)
   {
      if (path[0] == '/')
         c = path + 1;
      else
         c = path;
      while (*c != 0)
      {
         if (*c == '/')
         {
            *c = 0;
            if (!fileExists(path, slot))
            {
               if ((err = VFSDirCreate((UInt16) slot, path)) != errNone)
                  goto error;
            }
            *c = '/';
         }
         c++;
      }
      if ((err = VFSDirCreate((UInt16) slot, path)) != errNone)
         goto error;
   }
   return NO_ERROR;

error:
   return err;
}

static inline Err fileDelete(NATIVE_FILE* fref, TCHARP path, int32 slot, bool isOpen)
{
   if (isOpen)
      fileClose(fref);
   return VFSFileDelete((UInt16) slot, path);
}

static inline Err fileGetFreeSpace(CharP szPath, int32* freeSpace, int32 slot)
{
   Err err;
   UInt32 size;
   UInt32 total;

   UNUSED(szPath);

   err = VFSVolumeSize(slot, &size, &total);
   *freeSpace = total - size;

   return err;
}

static inline Err fileGetSize(NATIVE_FILE fref, TCHARP szPath, int32* size)
{
   UNUSED(szPath)
   return VFSFileSize(fref.handle, (UInt32*) size);
}

__attribute__((unused)) static bool fileIsDir(TCHARP path, int32 slot)
{
   UInt32 attr = 0;
   NATIVE_FILE fref;

   if (VFSFileOpen((UInt16) slot, path, vfsModeRead, &fref.handle) == errNone)
   {
      VFSFileGetAttributes(fref.handle, &attr);
      VFSFileClose(fref.handle);
   }

   return ((attr & vfsFileAttrDirectory) != 0);
}

__attribute__((unused)) static Err fileIsEmpty(NATIVE_FILE* fref, TCHARP path, int32 slot, int32* isEmpty)
{
   UInt32 attr = 0;
   Err err = errNone;             

   if (fref != INVALID_HANDLE_VALUE)
      return fileGetSize(*fref, path, isEmpty);

   if ((err = VFSFileOpen((UInt16) slot, path, vfsModeRead, &fref->handle)) == errNone)  // the handle that comes here is invalid
   {
      if ((err = VFSFileGetAttributes(fref->handle, &attr)) != errNone)
      {
         if ((attr & vfsFileAttrDirectory) != 0) // a folder?
         {                        
            char fileName[MAX_PATHNAME];
            UInt32 it;
            FileInfoType info;
            info.nameP = fileName;
            info.nameBufLen = sizeof(fileName);
            it = vfsIteratorStart;

            if ((err = VFSDirEntryEnumerate(fref->handle, &it, &info)) == expErrEnumerationEmpty) // at least one file
            {
               *isEmpty = true;
               err = errNone;
            }
         }
         else
            err = fileGetSize(*fref, path, isEmpty);
      }
      VFSFileClose(fref->handle);
   }
   return err;
}

__attribute__((unused)) static Err fileChmod(NATIVE_FILE* fref, TCHARP path, int32 slot, int32* mod)
{
   *mod = -1;
   return errNone;
}

static inline Err fileReadBytes(NATIVE_FILE fref, CharP bytes, int32 offset, int32 length, int32* bytesRead)
{
   Err err = VFSFileRead(fref.handle, (UInt32) length, bytes + offset, (UInt32*) bytesRead);

   if (err != vfsErrFileEOF) // flsobral@tc110_1: if EOF, ignore error and return 0.
      return err;
   return errNone; //flsobral@tc114_96: don't return 0 on EOF, just the number of bytes read.
}

__attribute__((unused)) static Err fileRename(NATIVE_FILE fref, int32 slot, TCHARP currPath, TCHARP newPath, bool isOpen)
{
   Err err;
   char parent[MAX_PATHNAME];
   char newParent[MAX_PATHNAME];
   char newFileName[MAX_PATHNAME];
   int32 len;

   splitPath(currPath, parent, null);
   splitPath(newPath, newParent, newFileName);

   len = xstrlen(parent);
   if (len != xstrlen(newParent))
   {
      err = vfsErrBadName;
      goto finish;
   }

   if (!strCaseEqn(parent, newParent, len))
   {
      err = vfsErrBadName;
      goto finish;
   }

   if (isOpen)
      fileClose(&fref);
   err = VFSFileRename((UInt16) slot, currPath, newFileName);

   finish:
      return err;
}

static inline Err fileSetPos(NATIVE_FILE fref, int32 pos)
{
   Err err;
   return (((err = VFSFileSeek(fref.handle, vfsOriginBeginning, pos)) == vfsErrFileEOF) ? errNone : err);
}

__attribute__((unused)) static Err fileWriteBytes(NATIVE_FILE fref, CharP buf, int32 off, int32 len, int32* bytesWritten)
{
   Err err;
   UInt32 size, pos;

   if ((err = VFSFileWrite(fref.handle, (UInt32) len, buf + off, (UInt32*) bytesWritten)) != errNone) // first, simply try to write
   {  // error? now we do something more complex: expand the file and write to it
      if (VFSFileSize(fref.handle, &size) == errNone && VFSFileTell(fref.handle, &pos) == errNone && pos+len <= size) // check if expanding the file may solve the first error
      {
         if ((err = VFSFileResize(fref.handle, pos+len)) == errNone) // expand file
            err = VFSFileWrite(fref.handle, (UInt32) len, buf + off, (UInt32*) bytesWritten); // write again
      }
   }

   return err;
}

__attribute__((unused)) static Err fileSetAttributes(NATIVE_FILE fref, TCHARP path, int32 tcAttr)
{
   UInt32 attr = 0;
   UNUSED(path)

   if (tcAttr != ATTR_NORMAL)
   {
      if (tcAttr & ATTR_ARCHIVE)
         attr |= vfsFileAttrArchive;
      if (tcAttr & ATTR_HIDDEN)
         attr |= vfsFileAttrHidden;
      if (tcAttr & ATTR_READ_ONLY)
         attr |= vfsFileAttrReadOnly;
      if (tcAttr & ATTR_SYSTEM)
         attr |= vfsFileAttrSystem;
   }
   return VFSFileSetAttributes(fref.handle, attr);
}

__attribute__((unused)) static Int32 fileGetAttributes(NATIVE_FILE fref, TCHARP path, int32* attributes)
{
   Err err;
   UInt32 attr;
   UNUSED(path)

   *attributes = 0;
   if ((err = VFSFileGetAttributes(fref.handle, &attr)) == errNone)
   {
      if (attr & vfsFileAttrArchive)  *attributes |= ATTR_ARCHIVE;
      if (attr & vfsFileAttrHidden)   *attributes |= ATTR_HIDDEN;
      if (attr & vfsFileAttrReadOnly) *attributes |= ATTR_READ_ONLY;
      if (attr & vfsFileAttrSystem)   *attributes |= ATTR_SYSTEM;
   }

   return err;
}

__attribute__((unused)) static Err fileSetTime(NATIVE_FILE fref, TCHARP path, int32 which, Object time)
{
   Err err;
   DateTimeType sysTime;
   UInt32 date;
   UNUSED(path)

   sysTime.year   = Time_year(time); // create private time from tc time
   sysTime.month  = Time_month(time);
   sysTime.day    = Time_day(time);
   sysTime.hour   = Time_hour(time);
   sysTime.minute = Time_minute(time);
   sysTime.second = Time_second(time);
   date = TimDateTimeToSeconds(&sysTime);

   err = errNone;
   if (which & TIME_CREATED)
      err = VFSFileSetDate(fref.handle, vfsFileDateCreated, date);
   if (which & TIME_ACCESSED && err == errNone)
      err = VFSFileSetDate(fref.handle, vfsFileDateAccessed, date);
   if (which & TIME_MODIFIED && err == errNone)
      err = VFSFileSetDate(fref.handle, vfsFileDateModified, date);

   return err;
}

__attribute__((unused)) static Err fileGetTime(Context currentContext, NATIVE_FILE fref, TCHARP path, int32 which, Object* time)
{
   Err err = errNone;
   UInt32 date = 0;
   DateTimeType sysTime;
   UNUSED(path)

   switch (which)
   {
      case TIME_CREATED : err = VFSFileGetDate(fref.handle, vfsFileDateCreated,  &date); break;
      case TIME_ACCESSED: err = VFSFileGetDate(fref.handle, vfsFileDateAccessed, &date); break;
      case TIME_MODIFIED: err = VFSFileGetDate(fref.handle, vfsFileDateModified, &date); break;
   }

   if (err == errNone)
   {
      TimSecondsToDateTime(date, &sysTime); // create tc time from private time

      *time = createObject(currentContext, "totalcross.sys.Time");
      if (*time == null)
         return errNone;

      Time_year(*time)   = sysTime.year;
      Time_month(*time)  = sysTime.month;
      Time_day(*time)    = sysTime.day;
      Time_hour(*time)   = sysTime.hour;
      Time_minute(*time) = sysTime.minute;
      Time_second(*time) = sysTime.second;
      Time_millis(*time) = 0; // not supplied by system time
   }

   return err;
}

static inline Err fileSetSize(NATIVE_FILE* fref, int32 newSize)
{
   return VFSFileResize(fref->handle, (UInt32) newSize);
}

__attribute__((unused)) static Err fileGetCardSerialNumber(int32 slot, CharP serialNumber)
{
   VolumeInfoType volInfo;
   ExpCardInfoType cardInfo;
   CharP serialP;
   Err err = errNone;

   UInt16 vol;
   int32 lastVol = 0;
   UInt32 it = expIteratorStart;

   *serialNumber = 0;

   if (slot == lastVolume)
   {
      while (it != expIteratorStop)
      {
         if (ExpSlotEnumerate(&vol, &it) == errNone)
            lastVol = (int32) vol;
         else
            break;
      }
   }

   if (lastVol && (ExpCardPresent(lastVol) == errNone))
   {
      if ((err = VFSVolumeInfo((UInt16) lastVol, &volInfo)) == errNone)
      {
         if (volInfo.mediaType == 'sdig')
         {
            if ((err = ExpCardInfo((UInt16) lastVol, &cardInfo)) == errNone)
            {
               serialP = xstrchr(cardInfo.deviceUniqueIDStr, '_') + 1;
               xstrncpy(serialNumber, serialP, 8);
            }
         }
      }
   }

   return err;
}

static inline Err fileFlush(NATIVE_FILE fref)
{
   UNUSED(fref)
   return 0;
}
