// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "palmdb.h"

#if defined WIN32 || defined WINCE
 #if defined _RAPI_
   #include "rapi/palmdb_c.h"
 #else
   #include "win/palmdb_c.h"
 #endif
#else
 #include <sys/param.h>
#ifdef ANDROID
 #include <unistd.h>
#else
 #include <sys/unistd.h>
#endif
 #define MAX_PATH MAXPATHLEN
 //PATH_MAX
 #include "posix/palmdb_c.h"
#endif

#ifdef darwin
bool getDataPath(CharP blah) {
    return false;
}
#endif


#if defined(WIN32) || defined(linux) || defined(ANDROID)

/* INTERNAL USE FUNCTIONS */
static void destroyRecPtrs(PDBFile db);
static void swapDatabaseHeader(DatabaseHeader *src, DatabaseHeader *dst);
static RecordList* retrieveRecList(PDBFile db, uint16 index);
static void shiftFile(PDBFile db, int32 start, int32 end, int32 len);
static void shiftOffsets(PDBFile db, int32 start, int32 end, int32 delta);
static int32 getRecordOffset(PDBFile db, int32 index);
static bool destroyRecPtr(PDBFile db, uint16 index);
static MemHandle resizeBuffer(PDBFile db, uint32 size);
static bool storeRecPtr(PDBFile db, CharP mh, uint16 index, uint16 size);
static MemHandle getRecord(PDBFile db, uint16 index, CharP buf, int32 ofs, uint32* length, bool queryOnly);
static int32 getRecordSize(PDBFile db, uint16 index);

Err myDmGetLastErr()
{
   return PDBGetLastErr();
}

Err myDmRecordInfo(DmOpenRef dbP, uint16 index, uint16* attrs)
{
   int32 read;
   return !PDBReadAt(((PDBFile) dbP)->fh, attrs, 1, GET_OFFSET(index)+4, &read) ? PDBGetLastErr() : errNone;
}

Err myDmSetRecordInfo(DmOpenRef dbP, uint16 index, uint16 attrs)
{
   PDBFile db = (PDBFile) dbP;
   int32 written;

   ++db->dbh.modificationNumber;
   return !PDBWriteAt(db->fh, &attrs, 1, GET_OFFSET(index)+4, &written) ? PDBGetLastErr() : errNone;
}

Err myDmReleaseRecord(DmOpenRef dbP, int32 index, bool dirty)
{
   PDBFile db = (PDBFile) dbP;
   uint16 attr=0;
   bool isNew;

   int32 size,originalSize;
   RecordList *rl;
   VoidP buf;
   bool changed;

   bool appending;
   int32 pos1; // stores the position for this index in the record headers table
   int32 pos2; // stores the position where the record really starts.
   uint16 count;
   int32 growingSize;
   int32 delta;

   int32 bytesRW;

   RecordHeader tmp;

   if (!DB_IS_OPEN(db))
      return PALM_ERROR;
   isNew = db->isNewRecord;
   if (!isNew && myDmRecordInfo(dbP, (uint16)index, &attr) == PALM_ERROR)
      return PALM_ERROR;
   db->recordCachedPos = -1; // required! otherwise, AllTests will fail

   if (isNew || (attr & dmRecAttrBusy))
   {
      rl = null;

      if (index == db->recordIndex) // are we releasing the current record that is being written?
      {
         buf = db->recordBuf + sizeof(uint32);
         size = db->recordSize;
         originalSize = db->originalRecordSize;
         changed = dirty;
      }
      else
      {
         rl = retrieveRecList(db, (uint16)index);
         if (rl == null)
            return PALM_ERROR;
         size = rl->size;
         originalSize = getRecordSize(db, (uint16)index);
         buf = rl->recPtr;
         changed = rl->changed;
      }

      if (buf == null)
         return PALM_ERROR;

      // if we changed the records size, we must resize the whole file
      if (size != originalSize || isNew)
      {
         appending = 0;
         count = db->dbh.numRecords;
         growingSize = isNew ? (size + ((db->fileSize==82)?6:8)) : (size - originalSize); // guich@550
         delta = isNew ? size : growingSize;

         if (isNew)
            count--;
         if (index >= count)
         {
            index = count;
            appending = 1;
         }

         // 1. change the file size
         if (growingSize > 0)
         {
            if (!PDBGrowFileSize(db->fh, db->fileSize, growingSize))
               return PALM_ERROR;
            db->fileSize += growingSize;
         }
         pos1 = GET_OFFSET(index);

         // 2. if not appending, insert space (or remove space) for the new record, shifting the file to the right
         if (!appending)
         {
            // get the offset to the current record index
            PDBReadAt(db->fh, &pos2, 4, pos1, &bytesRW);
            pos2 = SWAP32_FORCED(pos2);
            // shift the file
            if (growingSize > 0)
            {
               if (isNew)
                  shiftFile(db, pos2, db->fileSize-growingSize, growingSize);
               else
                  shiftFile(db, pos2+originalSize, db->fileSize-growingSize, growingSize);
            }
            else
               shiftFile(db, pos2+originalSize, db->fileSize, growingSize);
         }
         else
            pos2 = db->fileSize - size;

         // 3. now we insert space for the new index at the offset table
         if (isNew)
            shiftFile(db, pos1, pos2, 8);

         // 4. write the record header for the new index
         {
            tmp.offset = SWAP32_FORCED(pos2);                // swapRecordHeader
            tmp.attr = dmRecAttrDirty;
            tmp.uniqueId[0] = 0;
            tmp.uniqueId[1] = 0;
            tmp.uniqueId[2] = 0;
            PDBWriteAt(db->fh, &tmp, 8, pos1, &bytesRW);
         }

         // 5. now shift the offset of all records in the offsets table after this index
         if (isNew)
            shiftOffsets(db, 0,appending?index:(index+1),8); // since we inserted a new index, shift the record offsets 8 bytes down // guich@330_2: without the "appending?:", creating an empty catalog, adding one record and closing it, makes the first record have an offset of 96 instead of 88.
         if (!appending)
            shiftOffsets(db, index+1,isNew?(count+1):count,growingSize); // move the others down

         // 6. change the file size
         if (growingSize < 0)
         {
            if (!PDBGrowFileSize(db->fh, db->fileSize, growingSize))
               return PALM_ERROR;
            db->fileSize += growingSize;
         }

         if (db->dbh.appInfoOffset > 0)
            db->dbh.appInfoOffset += delta;
         if (db->dbh.sortInfoOffset > 0)
            db->dbh.sortInfoOffset += delta;
      }
      // write the record to disk
      if (changed && index >= 0)
         PDBWriteAt(db->fh, buf, size, getRecordOffset(db, index), &bytesRW);
      if (rl == null) // if not updating a record...
         db->recordIndex = -1;

      if (!isNew)
      {
         // unlock record
         attr &= ~dmRecAttrBusy; // clear busy attribute
         if (dirty)
            attr |= dmRecAttrDirty;
         PDBWriteAt(db->fh, &attr, 1, GET_OFFSET(index)+4, &bytesRW);
         //myDmSetRecordAttributes(db, (uint16)index, attr);
         destroyRecPtr(db, (uint16)index);
      }
      else
      {
         db->isNewRecord = 0;
//         db->dbh.modificationNumber++;
         if (dirty)
            db->dbh.modificationNumber++;
      }

      db->lockedRecords--;

      return PALM_SUCCESS;
   }
   return PALM_ERROR;
}

Err myDmCloseDatabase(DmOpenRef dbP)
{
   PDBFile db = (PDBFile) dbP;
   DatabaseHeader dbh;
   int32 bytesRW;
   Err err = errNone;

   if (db->recordIndex != -1)    // flsobral@120_30: make sure all records are released.
      myDmReleaseRecord(db, db->recordIndex, 1);

   if (db->dbh.attributes & dmHdrAttrRecyclable)
   {
      if (!PDBCloseFile(db->fh))
         return PDBGetLastErr();
      if (!PDBRemove(db->fullPath))
         return PDBGetLastErr();
      goto finish;
   }

   // anything changed?
   xmemmove(&dbh, &db->dbh, sizeof(DatabaseHeader));
   if (db->originalModificationNumber != db->dbh.modificationNumber)
      dbh.modificationDate = PDBGetNow();   // update modification date
   if (db->lockedRecords <= 0)
      dbh.attributes &= ~dmHdrAttrOpen; // flsobral@120_30: reset the open flag only if all records are unlocked - guich@tc123_17: save in the correct structure (local dbh variable, not db->dbh!)
   swapDatabaseHeader(&dbh, &dbh);        // write back the modified header
   if (!PDBWriteAt(db->fh, &dbh, 78, 0, &bytesRW))
      err = PDBGetLastErr();
   if (!PDBCloseFile(db->fh))
      if (!err)
         err = PDBGetLastErr();
   if (err)
      return err;

finish:
   destroyRecPtrs(db);
   xfree(db->recordBuf);
   xfree(db->queryRecordBuf);
   db->fh = null;
   return errNone;
}

bool PDBMatchExact(TCHARP filePath, VoidP userVars)
{
   FileMatches_Vars* vars = (FileMatches_Vars*) userVars;
   DatabaseHeader dbh;
   int32 bytesRW;

   if (!PDBCreateFile(filePath, false, true, &vars->fileRef)) // createIt - false, readOnly - true
      return false;
   if (!PDBRead(vars->fileRef, &dbh, 78, &bytesRW))
      goto error;
   swapDatabaseHeader(&dbh, &dbh);

   if (xstrcmp(vars->name, dbh.name))
      goto error;
   tcscpy(vars->fullPath, filePath);

   return true;

error:
   PDBCloseFile(vars->fileRef);
   vars->fileRef = INVALID_HANDLE_VALUE;
   return false;
}

VoidP myDmFindDatabase(Context currentContext, TCHARP fileName, PDBFile dbId)
{
   TCHAR filePath[MAX_PATH];
   TCHAR fullPath[MAX_PATH];
   bool pdbFound;
   char dataPathBuf[MAX_PATHNAME];

   FileMatches_Vars userVars;

   tcscpy(filePath, fileName);
   tcscat(filePath, TEXT(".pdb"));

   // The file name sent might be the full path, so we'll try that first.
   pdbFound = PDBCreateFile(filePath, false, true, &userVars.fileRef); // createIt - false, readOnly - true

   // Not found? Let's check the data path.
   if (!pdbFound)
   {
      userVars.fileRef = INVALID_HANDLE_VALUE;
      TCHARP2CharPBuf(fileName, userVars.name);

      if (getDataPath(dataPathBuf) && *dataPathBuf != 0)
      {
         CharP2TCHARPBuf(dataPathBuf, fullPath);
         PDBListDatabasesIn(fullPath, true, (HandlePDBSearchProcType) PDBMatchExact, &userVars);
         pdbFound = userVars.fileRef != INVALID_HANDLE_VALUE;
      }

      // Nothing yet? Let's check the app path.
      if (!pdbFound)
      {
         CharP2TCHARPBuf(getAppPath(), fullPath);
         PDBListDatabasesIn(fullPath, true, (HandlePDBSearchProcType) PDBMatchExact, &userVars);
         pdbFound = userVars.fileRef != INVALID_HANDLE_VALUE;
      }

      // ok, now search on vm's path - guich@tc110_94
      if (!pdbFound)
      {
         CharP2TCHARPBuf(getVMPath(), fullPath);
         PDBListDatabasesIn(fullPath, true, (HandlePDBSearchProcType) PDBMatchExact, &userVars);
         pdbFound = userVars.fileRef != INVALID_HANDLE_VALUE;
      }
   }
   else
      tcscpy(userVars.fullPath, filePath); // Found on first try!

   if (pdbFound)
   {
      PDBCloseFile(userVars.fileRef);
      userVars.fileRef = INVALID_HANDLE_VALUE;
      tcscpy(dbId->fullPath, userVars.fullPath);
      return dbId;
   }
   return null;
}

Err myDmDatabaseInfo(VoidP dbId, TCHARP name, uint16* attributes, uint16* version,
      uint32* creationDate, uint32* modificationDate, uint32* backupDate,
      uint32* modificationNumber, VoidP appInfoIDP, VoidP sortInfoIDP,
      uint32* type, uint32* creator)
{
   PDBFile db = (PDBFile) dbId;
   PDBFileRef fileRef;
   int32 bytesRW;
   Err err = errNone;

   if (!db->fh)
   {
      if (!PDBCreateFile(db->fullPath, false, true, &fileRef)) // createIt - false, readOnly - true
         return PDBGetLastErr();
      if (!PDBRead(fileRef, &db->dbh, 78, &bytesRW))
         err = PDBGetLastErr();
      if (!PDBCloseFile(fileRef))
         if (!err)
            err = PDBGetLastErr();
      if (err)
         return err;
      swapDatabaseHeader(&db->dbh, &db->dbh);
   }

   if (name)
      CharP2TCHARPBuf(db->dbh.name, name);
   if (attributes)
      *attributes = db->dbh.attributes;
   if (version)
      *version = db->dbh.version;
   if (creationDate)
      *creationDate = db->dbh.creationDate;
   if (modificationDate)
      *modificationDate = db->dbh.modificationDate;
   if (backupDate)
      *backupDate = db->dbh.lastBackupDate;
   if (modificationNumber)
      *modificationNumber = db->dbh.modificationNumber;
   if (type)
      *type = db->dbh.type;
   if (creator)
      *creator = db->dbh.creator;

   return errNone;
}

Err myDmDeleteDatabase(VoidP dbId)
{
#ifdef _RAPI_
   TCHAR fullPath[MAX_PATHNAME];
   lstrcpy(fullPath, ((PDBFile) dbId)->fullPath);
   myDmCloseDatabase(dbId);
   return !PDBRemove(fullPath) ? PDBGetLastErr() : errNone;
#else
   return !PDBRemove(((PDBFile) dbId)->fullPath) ? PDBGetLastErr() : errNone;
#endif
}

Err myDmCreateDatabase(TCHARP fileName, uint32 creator, uint32 type, bool isResDB)
{
   TCHAR fullPath[MAX_PATH] = TEXT("");
   PDBFileRef fileRef;
   TCHARP strP;
   TCHARP name;
   char dataPathBuf[MAX_PATHNAME];
   DatabaseHeader dbh;
   int16 pad = 0;
   int32 bytesRW;
   Err err;

   name = fileName;
   for (strP = fileName ; *strP != 0 ; strP++)
   {
      if (*strP == '/' || *strP == '\\')
         name = strP+1;
   }
   if (name == fileName)
   {
      if (getDataPath(dataPathBuf) && *dataPathBuf)
         CharP2TCHARPBuf(dataPathBuf, fullPath);
      else
         CharP2TCHARPBuf(getAppPath(), fullPath);
      tcscat(fullPath, TEXT("/"));
   }
   tcscat(fullPath, fileName);
   tcscat(fullPath, TEXT(".pdb"));

   if (!PDBCreateFile(fullPath, true, false, &fileRef)) // createIt - true, readOnly - false
      return PDBGetLastErr();

   xmemzero(&dbh, sizeof(DatabaseHeader));
   // set the fields for the newly created database
   TCHARP2CharPBuf(name, dbh.name);
   dbh.version = 1;
   dbh.modificationDate = dbh.creationDate = PDBGetNow();
   dbh.lastBackupDate   = 0;
   dbh.type             = type;
   dbh.creator          = creator;

   // write header info into the newly created file
   swapDatabaseHeader(&dbh, &dbh);
   if (!PDBWrite(fileRef, &dbh, 78, &bytesRW))
      goto error;
   if (!PDBWrite(fileRef, &pad, 2, &bytesRW)) // this goes after the table of record headers
      goto error;

   if (!PDBCloseFile(fileRef))
      goto error;
   return errNone;

error:
   err = PDBGetLastErr();
   PDBCloseFile(fileRef);
   PDBRemove(fullPath);
   return err;
}

DmOpenRef myDmOpenDatabase(VoidP dbId, uint16 mode)
{
   PDBFile db = (PDBFile) dbId;
   PDBFileRef fileRef;

   uint16 att;
   uint16 count;
   int32 offset;
   bool mustSave;
   int32 i;
   bool isFileOpen = false;
   int32 bytesRW;

   if (!(isFileOpen = PDBCreateFile(db->fullPath, false, false, &fileRef))) // createIt - true, readOnly - false
      goto error;

   if (!PDBRead(fileRef, &db->dbh, 78, &bytesRW))
      goto error;
   swapDatabaseHeader(&db->dbh, &db->dbh);
   if (!PDBGetFileSize(fileRef, (int32*) &db->fileSize))
      goto error;
   db->fh = fileRef;
   if (!PDBGetFileSize(fileRef, (int32*) &db->fileSize))
      goto error;
   db->mode = mode;
   db->recordIndex = -1;
   db->recordCachedPos = -1;

   // was the db properly closed?
   if (db->dbh.attributes & dmHdrAttrOpen)
   {
      // read all record headers and check if they were left busy
      count = db->dbh.numRecords;
      offset = 78;

      while (count > 16)
      {
         mustSave = false;
         if (!PDBReadAt(db->fh, db->rhs, 8*16, offset, &bytesRW))
            goto error;
         for (i = 0; i < 16; i++)
            if (db->rhs[i].attr & dmRecAttrBusy) // no need to swapRecordHeader here
            {
               db->rhs[i].attr &= ~dmRecAttrBusy;
               mustSave = true;
            }
         if (mustSave)
            if (!PDBWriteAt(db->fh, db->rhs, 8*16, offset, &bytesRW))
               goto error;
         count -= 16;
         offset += 8*16;
      }
      if (count > 0)
      {
         mustSave = false;
         if (!PDBReadAt(db->fh, db->rhs, 8*count, offset, &bytesRW))
            goto error;
         for (i = 0; i < count; i++)
            if (db->rhs[i].attr & dmRecAttrBusy)
            {
               db->rhs[i].attr &= ~dmRecAttrBusy;
               mustSave = true;
            }
         if (mustSave)
            if(!PDBWriteAt(db->fh, db->rhs, 8*count, offset, &bytesRW))
               goto error;
      }
   }
   else
   {
      // set the opened attribute
      db->dbh.attributes |= dmHdrAttrOpen;
      att = SWAP16_FORCED(db->dbh.attributes);
      if (!PDBWriteAt(db->fh, &att, 2, 32, &bytesRW))
         goto error;
   }

   db->originalModificationNumber = db->dbh.modificationNumber;
   return dbId;

error:
   if (isFileOpen)
      PDBCloseFile(fileRef);
   return null;
}

Err myDmSetDatabaseInfo(VoidP dbId, TCHARP name, uint16* attributes, uint16* version,
      uint32* creationDate, uint32* modificationDate, uint32* backupDate,
      uint32* modificationNumber, VoidP appInfoIDP, VoidP sortInfoIDP,
      uint32* type, uint32* creator)
{
   PDBFile db = (PDBFile) dbId;
   TCHAR newPath[MAX_PATHNAME];
   TCHARP strP;
   PDBFileRef fileRef;
   int32 newPathLen;
   int32 bytesRW;
   bool wasOpen = (db->fh != null);
   Err err;

   if (!wasOpen)
   {
      if (!PDBCreateFile(db->fullPath, false, false, &fileRef)) // createIt - false, readOnly - true
         return PDBGetLastErr();
      if (!PDBRead(fileRef, &db->dbh, 78, &bytesRW))
      {
         err = PDBGetLastErr();
         PDBCloseFile(fileRef);
         return err;
      }
      swapDatabaseHeader(&db->dbh, &db->dbh);
   }
   else
      fileRef = db->fh;

   if (name)
      TCHARP2CharPBuf(name, db->dbh.name);
   if (attributes)
      db->dbh.attributes = *attributes;
   if (version)
      db->dbh.version = *version;
   if (creationDate)
      db->dbh.creationDate = *creationDate;
   if (modificationDate)
      db->dbh.modificationDate = *modificationDate;
   if (backupDate)
      db->dbh.lastBackupDate = *backupDate;
   if (modificationNumber)
      db->dbh.modificationNumber = *modificationNumber;
   else
      db->dbh.modificationNumber++;
   if (type)
      db->dbh.type = *type;
   if (creator)
      db->dbh.creator = *creator;

   if (!wasOpen || name)
   {
      swapDatabaseHeader(&db->dbh, &db->dbh);
      if (!PDBWriteAt(fileRef, &db->dbh, 78, 0, &bytesRW))
      {
         err = PDBGetLastErr();
         PDBCloseFile(fileRef);
         return err;
      }
      if (!PDBCloseFile(fileRef))
          return PDBGetLastErr();
      db->fh = null;
   }

   if (name)
   {
      tcscpy(newPath, db->fullPath);
      newPathLen = (int32) tcslen(newPath);
      for (strP = newPath + newPathLen ; true ; strP--)
      {
         if (*strP == '/' || *strP == '\\')
         {
            *(strP+1) = 0;
            break;
         }
         if (strP == newPath)
         {
            *strP = 0;
            break;
         }
      }
      tcscat(newPath, name);
      tcscat(newPath, TEXT(".pdb"));
      if (!PDBRename(db->fullPath, newPath))
         return PDBGetLastErr();

      if (wasOpen)
      {
         swapDatabaseHeader(&db->dbh, &db->dbh);
         tcscpy(db->fullPath, newPath);
         if (!PDBCreateFile(db->fullPath, false, false, &fileRef)) // createIt - false, readOnly - true
            return PDBGetLastErr();
         db->fh = fileRef;
      }
   }

   return errNone;
}

/* Just create memory for the record; the record will be written and
the file will be shifted in myDmReleaseRecord. */
MemHandle myDmNewRecord(DmOpenRef dbP, uint16 *atP, uint32 size)
{
   PDBFile db = (PDBFile) dbP;
   uint16 numRecords;
   uint16 modNumber;
   int32 bytesRW;
   Err err = -1;

   if (!DB_IS_OPEN(db) || size == 0 || size > 65520L)
      return null; // closed or invalid size?
   if (db->isNewRecord)
      return null;

   // 0. create the buffer that will store the data - used only in myDmWrite
   if (resizeBuffer(db, size) == null)
      return null;

   // 1. recompute the internal parameters
   if (*atP == 65535) // guich@360_94
      *atP = db->dbh.numRecords;
   db->recordIndex = *atP;
   db->isNewRecord = 1;  // when releasing the record, shift the file
   db->recordChanged = 0;
   db->dbh.numRecords++;
   db->originalRecordSize = (uint16)size;
   db->lockedRecords++;
   db->dbh.modificationNumber++;

   // update the number of records
   numRecords = SWAP16_FORCED(db->dbh.numRecords);
   err = PDBWriteAt(db->fh, &numRecords, 2, 76, &bytesRW);

   modNumber = SWAP16_FORCED(db->dbh.modificationNumber);
   err = PDBWriteAt(db->fh, &modNumber, 2, 50, &bytesRW);

   return (MemHandle) db->recordBuf;
}

MemHandle myDmQueryRecord(DmOpenRef dbP, uint16 index)
{
   PDBFile db = (PDBFile) dbP;
   int32 currentIndex;

   if (db->recordChanged || db->isNewRecord)
   {
      currentIndex = db->recordIndex;
      myDmReleaseRecord(dbP, db->recordIndex, true);

      db->recordIndex = index;
      db->recordSize = db->originalRecordSize;
      db->lockedRecords++;
   }
   return getRecord(db, index, null, 0, null, true); // queryOnly -> true.
}

uint16 myDmNumRecords(DmOpenRef dbP)
{
   return ((PDBFile) dbP)->dbh.numRecords;
}

/** If the record is open, the MemHandle to it will be returned. If the record is closed,
it will be opened, but a non-zero (but invalid) value will be returned */
MemHandle myDmResizeRecord(DmOpenRef dbP, uint16 index, uint32 size)
{
   PDBFile db = (PDBFile) dbP;
   uint8* recPtrTemp;
   uint32 temp;
   MemHandle handleToBeReleased;
   RecordList *rl;

   if (!DB_IS_OPEN(db) || size == 0 || size > 65520L)
      return null; // closed, invalid index or invalid size?
   if (db->recordIndex != index) // resizing a record got with getRecord? (updating a record?)
   {
      rl = retrieveRecList(db,index);
      temp = size + sizeof(uint32);
      handleToBeReleased = null;
      if (rl == null) // guich@552_25: if the record was not loaded, load and then release it instead of just returning an error
      {
         handleToBeReleased = myDmGetRecord(dbP, index);
         if (!handleToBeReleased)
            return null;
         rl = retrieveRecList(db,index);
         if (rl == null) // if failed for the second time, we have no options beside just return
            return null;
      }
      if (rl->size != size)
      {
         recPtrTemp = (uint8*)xrealloc(rl->recPtr, temp);
         if (recPtrTemp == null)
            return null;
         rl->recPtr = recPtrTemp;
         (*(uint32*) rl->recPtr) = size;
         rl->changed = 1;
         rl->size = (uint16) size;
      }
      //db->dbh.modificationNumber++;
      if (handleToBeReleased) // guich@552_25
      {
         myDmReleaseRecord(dbP, index, 1);
         return (MemHandle)1; // just to tell that the record was resized with success
      }
      return (MemHandle)rl->recPtr;
   }
   else
      return resizeBuffer(db, size);
}

MemHandle myDmGetRecord(DmOpenRef dbP, uint16 index)
{
   PDBFile db = (PDBFile) dbP;
   MemHandle handle = getRecord(db, index, null, 0, null, false);

   if (handle)
   {
      db->recordIndex = index;
      db->recordSize = db->originalRecordSize;
   }
   else
   {
      db->recordIndex = -1;
      db->recordSize = 0;
   }
   return handle;
}

Err myDmRemoveRecord(DmOpenRef dbP, uint16 index)
{
   PDBFile db = (PDBFile) dbP;
   uint16 numRecords;
   uint8 removingFromEnd = 0;
   int32 pos1; // stores the position for this index in the record headers table
   int32 pos2; // stores the position where the record really starts.
   int32 shrinkingSize;
   uint16 size;
   uint16 modNumber;
   int32 bytesRW;
   db->recordCachedPos = -1;

   if (!DB_IS_OPEN(db) )
      return PALM_ERROR;
   if (index >= db->dbh.numRecords )
      return PALM_ERROR;
   if (db->recordIndex == -1)
      return PALM_ERROR; // closed or invalid size?

   if (db->isNewRecord)
   {
      db->recordIndex = -1;
      db->isNewRecord = 0;  // when releasing the record, shift the file
//      db->recordChanged = 0;
//      db->dbh.numRecords--;
      db->originalRecordSize = 0;
      db->lockedRecords--;
      db->sizeofRecordBuf = 0;
      if (db->recordBuf)
         xfree(db->recordBuf);

      // update the number of records
//    numRecords = SWAP16_FORCED(db->dbh.numRecords);
//    _myWriteAt(db->fh, &numRecords, 2, 76);

      goto finish; //return PALM_SUCCESS;
   }


   size = (uint16)getRecordSize(db, index);

   if (index == (db->dbh.numRecords-1))
      removingFromEnd = 1;

   pos1 = GET_OFFSET(index);
   shrinkingSize = - ((int32)size + 8);

   // 2. if not removingFromEnd, remove the space of the current record, shifting the file to the right
   if (!removingFromEnd)
   {
      PDBReadAt(db->fh, &pos2, 4, pos1, &bytesRW);
      pos2 = SWAP32_FORCED(pos2);
   }
   else
      pos2 = db->fileSize-size;

   // 3. records before this one only shift by the removed entry of the records table
   shiftOffsets(db, 0, index, -8);

   // 4. records after this one shifts the total bytes removed
   shiftOffsets(db, index+1, db->dbh.numRecords, shrinkingSize);

   // 5. now we remove the space of the index at the offset table
   shiftFile(db, pos1+8, pos2, -8);

   // 6. and now we remove the space of the record
   if (!removingFromEnd)
      shiftFile(db, pos2+size, db->fileSize, shrinkingSize); // shrinkingSize is always < 0

   // 7. change the file size
   if (!PDBGrowFileSize(db->fh, db->fileSize, shrinkingSize))
      return PALM_ERROR;
   db->fileSize += shrinkingSize;

   // 8. recompute the internal parameters
//   db->dbh.numRecords--;
//   db->dbh.modificationNumber++;
//   db->recordChanged = 0;
   if (db->dbh.appInfoOffset > 0)
      db->dbh.appInfoOffset += size;
   if (db->dbh.sortInfoOffset > 0)
      db->dbh.sortInfoOffset += size;

finish:
   // update the number of records

   db->dbh.numRecords--;
   db->recordChanged = 0;
   db->dbh.modificationNumber += 2;

   numRecords = SWAP16_FORCED(db->dbh.numRecords);
   PDBWriteAt(db->fh, &numRecords, 2, 76, &bytesRW);

   modNumber = SWAP16_FORCED(db->dbh.modificationNumber);
   PDBWriteAt(db->fh, &modNumber, 2, 50, &bytesRW);
   return PALM_SUCCESS;
}

Err myDmWrite (VoidP recordP, uint32 offset, VoidP srcP, uint32 bytes)
{
   xmemmove(((CharP) recordP) + offset, (CharP) srcP, bytes);
   return errNone;
}

#if defined _RAPI_
int32 listDatabases(TCHARP searchPath, HandlePDBSearchProcType proc, void *userVars, uint8 recursive)
{
   PDBListDatabasesIn(searchPath, true, proc, userVars);
   return 0;
}
#else
bool PDBMatchByTypeCreator(TCHARP fileName, VoidP userVars)
{
   FileMatches_Vars* vars = (FileMatches_Vars*) userVars;
   DatabaseHeader dbh;
   TCHARP entry;
   int32 bytesRW;
   int32 nameLen;
   char aux[10];

   if (!PDBCreateFile(fileName, false, true, &vars->fileRef)) // createIt - false, readOnly - true
      goto finish;
   if (!PDBRead(vars->fileRef, &dbh, 78, &bytesRW))
      goto finish;
   swapDatabaseHeader(&dbh, &dbh);

   if (dbh.creator == 0 || dbh.type == 0)
      goto finish;
   if (vars->type != 0 && vars->type != dbh.type)
      goto finish;
   if (vars->creator != 0 && vars->creator != dbh.creator)
      goto finish;

   nameLen = xstrlen(dbh.name);
   entry = (TCHARP) heapAlloc(vars->h, (nameLen+11)*sizeof(TCHAR));
   CharP2TCHARPBuf(dbh.name, entry);
   aux[0] = '.';
   int2CRID(dbh.creator, aux+1);
   CharP2TCHARPBuf(aux, entry + nameLen);
   int2CRID(dbh.type, aux+1);
   CharP2TCHARPBuf(aux, entry + nameLen + 5);

   vars->resultList = TCHARPsAdd(vars->resultList, entry, vars->h); // add entry to list
   (vars->resultListLen)++;

finish:
   PDBCloseFile(vars->fileRef);
   return false;
}

TCHARPs* listDatabasesByTypeCreator(uint32 type, uint32 creator, int32* count, Heap h)
{
   FileMatches_Vars userVars;
   TCHAR searchPath[MAX_PATHNAME];
   char dataPath[MAX_PATHNAME];
   CharP appPath;
   CharP vmPath;

   // Initialize variables for list
   userVars.creator = creator;
   userVars.type = type;
   userVars.h = h;
   userVars.resultList = null;
   userVars.resultListLen = 0;

   // flsobral@tc111_20c: Search in dataPath, vmPath and appPath.
   if (getDataPath(dataPath)) // search in Settings.dataPath
   {
      CharP2TCHARPBuf(dataPath, searchPath);
      PDBListDatabasesIn(searchPath, false, (HandlePDBSearchProcType) PDBMatchByTypeCreator, &userVars);
   }
   appPath = getAppPath();
   vmPath = getVMPath();
   if (!strEq(appPath, vmPath)) // appPath and vmPath are not the same.
   {
      CharP2TCHARPBuf(appPath, searchPath); // search in Settings.appPath
      PDBListDatabasesIn(searchPath, false, (HandlePDBSearchProcType) PDBMatchByTypeCreator, &userVars);
   }
   CharP2TCHARPBuf(vmPath, searchPath); // search in Settings.vmPath
   PDBListDatabasesIn(searchPath, false, (HandlePDBSearchProcType) PDBMatchByTypeCreator, &userVars);

   (*count) = userVars.resultListLen;

   return userVars.resultList;
}
#endif

uint32 myMemHandleSize(MemHandle h)
{
   return (*(uint32*) h);
}

CharP myMemHandleLock(MemHandle h)
{
   return (h + sizeof(uint32));
}

static int32 getRecordOffset(PDBFile db, int32 index)
{
   int32 offset;
   int32 bytesRW;
   // get the offset to the current record index
   PDBReadAt(db->fh, &offset, 4, GET_OFFSET(index), &bytesRW);
   return SWAP32_FORCED(offset);
}

static RecordList* retrieveRecList(PDBFile db, uint16 index)
{
   int32 pos = index & 0xF;
   RecordList *rl = (RecordList *) db->recPtrList[pos];
   while (rl != null)
      if (rl->index == index)
         return rl;
      else
         rl = rl->next;
   return null;
}

static bool destroyRecPtr(PDBFile db, uint16 index)
{
   int32 pos = index & 0xF;
   RecordList *rl = (RecordList *) db->recPtrList[pos];
   RecordList *last = null;

   while (rl != null)
      if (rl->index == index)
      {
         if (last == null)
            db->recPtrList[pos] = rl->next; // first item?
         else
            last->next = rl->next;
         xfree(rl->recPtr);
         xfree(rl);
         return true;
      }
      else
      {
         last = rl;
         rl = rl->next;
      }
   return false;
}

static void destroyRecPtrs(PDBFile db)
{
   RecordList *rl, *next;
   int32 i;
   for (i = 0; i < 16; i++)
   {
      rl = (RecordList *) db->recPtrList[i];
      while (rl != null)
      {
         next = rl->next;
         xfree(rl->recPtr);
         xfree(rl);
         rl = next;
      }
   }
}

static bool storeRecPtr(PDBFile db, CharP mh, uint16 index, uint16 size)
{
   int32 pos = index & 0xF;
   RecordList *rl;

   if ((rl = (RecordList*) xmalloc(sizeof(RecordList))) == null)
      return false;
   xmemzero(rl, sizeof(RecordList));

   rl->recPtr = (uint8*)mh;
   rl->index = index;
   rl->size = size;
   rl->next = db->recPtrList[pos];
   db->recPtrList[pos] = rl;
   return true;
}

static int32 getRecordSize(PDBFile db, uint16 index)
{
   RecordHeader rh;
   int32 size=0;
   int32 bytesRW;

   // guich@556_5: removed cache code
   PDBReadAt(db->fh, &rh, 8, GET_OFFSET(index), &bytesRW);   // get current record header
   rh.offset = SWAP32_FORCED(rh.offset);           // swapRecordHeader
   if (index < (db->dbh.numRecords-1))
   {
      PDBRead(db->fh, &size, 4, &bytesRW);             // get next offset
      size = SWAP32_FORCED(size);
   }
   else
      size = db->fileSize;

   size -= rh.offset; // sub
   size = (size < 0) ? 0 : size;
   return size;
}

static MemHandle getRecord(PDBFile db, uint16 index, CharP buf, int32 ofs, uint32* length, bool queryOnly)
{
   CharP memHandle = null;
   int32 offset = GET_OFFSET(index); // offset to the offsets table
   uint32 size;
   int32 bytesRW;

   if (!DB_IS_OPEN(db) || index >= db->dbh.numRecords)
      return null;

   if (index != db->recordCachedPos) // guich@550: do a little cache - alltests gets above 15000 cache hits, benchpdbdriver above 50000
   {
      PDBReadAt(db->fh, &db->recordCachedHeader, 8, offset, &bytesRW);       // get current record header
      db->recordCachedHeader.offset = SWAP32_FORCED(db->recordCachedHeader.offset);    // swapRecordHeader

      // get record offset and real size
      if (index < (db->dbh.numRecords-1))
      {
         PDBRead(db->fh, &db->recordCachedSize, 4, &bytesRW); // get next offset
         db->recordCachedSize = SWAP32_FORCED(db->recordCachedSize);
      }
      else
      {
         db->recordCachedSize = db->fileSize;
      }
      db->recordCachedSize -= db->recordCachedHeader.offset; // sub
      if ((int32)db->recordCachedSize < 0) // error?
         return null;

      db->recordCachedPos = index;
   }

   // flsobral@tc120_30: now we check if there are any locked records before checking if the requested record is busy.
   if (!queryOnly                                    // query only? go ahead!
   && (db->lockedRecords > 0)                        // no records locked? go ahead!
   && (db->recordCachedHeader.attr & dmRecAttrBusy)) // is the record we want already locked?
      return null;

   if (length == null)
      size = db->recordCachedSize;
   else
   {
      if (*length == 0)
      {
         *length = db->recordCachedSize;
         return null;
      }
      else
         size = *length = (db->recordCachedSize < *length) ? db->recordCachedSize : *length; // wants to read a piece of the record only
   }

   if (buf)
      memHandle = buf;
   else
   {
      if ((memHandle = (CharP) xmalloc(size + sizeof(uint32))) == null)
         return null;
   }
   PDBReadAt(db->fh, memHandle + sizeof(uint32), size, db->recordCachedHeader.offset+ofs, &bytesRW); // can't change to READ_SIZE_AT bc the macro uses & in the pointer, corrupting memory
   (*(uint32*) memHandle) = size;

   // set the busy bit
   if (!queryOnly)
   {
      if (!storeRecPtr(db, memHandle, index, (uint16) (size + sizeof(uint32))))
         xfree(memHandle);
      else
      {
         db->recordCachedHeader.attr |= dmRecAttrBusy;
         PDBWriteAt(db->fh, &db->recordCachedHeader.attr, 1, offset+4, &bytesRW);
         db->originalRecordSize = (uint16) db->recordCachedSize;
         db->recordChanged = 0;
         db->lockedRecords++;
         resizeBuffer(db, size);
         xmemmove(db->recordBuf, memHandle, size + sizeof(uint32));
         memHandle = (CharP)db->recordBuf;
      }
   }
   else
   {
      if (db->queryRecordBuf)
         xfree(db->queryRecordBuf);
      db->queryRecordBuf = memHandle;
   }
   return (MemHandle) memHandle;
}

void swapDatabaseHeader(DatabaseHeader *src, DatabaseHeader *dst)
{
   xmemmove(dst->name, src->name, DB_NAME_LENGTH);
   dst->attributes          = SWAP16_FORCED(src->attributes);
   dst->version             = SWAP16_FORCED(src->version);
   dst->creationDate        = SWAP32_FORCED(src->creationDate);
   dst->modificationDate    = SWAP32_FORCED(src->modificationDate);
   dst->lastBackupDate      = SWAP32_FORCED(src->lastBackupDate);
   dst->modificationNumber  = SWAP32_FORCED(src->modificationNumber);
   dst->appInfoOffset       = SWAP32_FORCED(src->appInfoOffset);
   dst->sortInfoOffset      = SWAP32_FORCED(src->sortInfoOffset);
   dst->type                = SWAP32_FORCED(src->type);
   dst->creator             = SWAP32_FORCED(src->creator);
   dst->uniqueIDSeed        = SWAP32_FORCED(src->uniqueIDSeed);
   dst->nextRecordListID    = SWAP32_FORCED(src->nextRecordListID);
   dst->numRecords          = SWAP16_FORCED(src->numRecords);
}

static void shiftOffsets(PDBFile db, int32 start, int32 end, int32 delta)
{
   int32 count = end-start;
   int32 pos1 = GET_OFFSET(start);
   int32 i;
   int32 bytesRW;

   uint32 ofs;

   while (count > 16)
   {
      PDBReadAt(db->fh, db->rhs, 8*16, pos1, &bytesRW);
      for (i = 0; i < 16; i++)
      {
         ofs = SWAP32_FORCED(db->rhs[i].offset) + delta;   // swapRecordHeader
         db->rhs[i].offset = SWAP32_FORCED(ofs);                  // swapRecordHeader
      }
      PDBWriteAt(db->fh, db->rhs, 8*16, pos1, &bytesRW);
      pos1 += 16*8;
      count -= 16;
   }
   if (count > 0)
   {
      PDBReadAt(db->fh, db->rhs, 8*count, pos1, &bytesRW);
      for (i = 0; i < count; i++)
      {
         ofs = SWAP32_FORCED(db->rhs[i].offset) + delta;  // swapRecordHeader
         db->rhs[i].offset = SWAP32_FORCED(ofs);                 // swapRecordHeader
      }
      PDBWriteAt(db->fh, db->rhs, 8*count, pos1, &bytesRW);
   }
}

static void shiftFile(PDBFile db, int32 start, int32 end, int32 len)
{
   char byteBuf[BYTE_BUF_LEN];
   int32 bytesRW;

   if (len > 0) // insert bytes at current position?
   {
      while ((end-BYTE_BUF_LEN) > start)
      {
         end -= BYTE_BUF_LEN;
         PDBReadAt(db->fh, byteBuf, BYTE_BUF_LEN, end, &bytesRW);
         PDBWriteAt(db->fh, byteBuf, BYTE_BUF_LEN, end+len, &bytesRW);
      }
      PDBReadAt(db->fh, byteBuf, end-start, start, &bytesRW);
      PDBWriteAt(db->fh, byteBuf, end-start, start+len, &bytesRW);
   }
   else // delete bytes at current position
   {
      while ((start+BYTE_BUF_LEN) < end)
      {
         PDBReadAt(db->fh, byteBuf, BYTE_BUF_LEN, start, &bytesRW);
         PDBWriteAt(db->fh, byteBuf, BYTE_BUF_LEN, start+len, &bytesRW);
         start += BYTE_BUF_LEN;
      }
      PDBReadAt(db->fh, byteBuf, end-start, start, &bytesRW);
      PDBWriteAt(db->fh, byteBuf, end-start, start+len, &bytesRW);
   }
}

static MemHandle resizeBuffer(PDBFile db, uint32 size)
{
   CharP recordBuf = null;
   uint32 temp;
   uint32 recordBufSize = size + sizeof(uint32);

   if (db->recordBuf == null || db->sizeofRecordBuf < recordBufSize)
   {
      temp = recordBufSize;
      recordBuf = (CharP)xrealloc((uint8*)db->recordBuf, temp);
      if (recordBuf == null)
         return null;
      db->recordBuf = (int8*)recordBuf;
      db->sizeofRecordBuf = temp;
   }
   (*(uint32*) db->recordBuf) = size;
   db->recordSize = size;

   db->dbh.modificationNumber++;
   return (MemHandle)db->recordBuf;
}

bool endsWithPDB(TCHARP fName)
{
   int32 len = 0;
   while (*fName) {fName++; len++;}
   return (len > 3 && (*(fName-3) == (TCHAR)'p' || *(fName-3) == (TCHAR)'P')
                         && (*(fName-2) == (TCHAR)'d' || *(fName-2) == (TCHAR)'D')
                         && (*(fName-1) == (TCHAR)'b' || *(fName-1) == (TCHAR)'B'));
}
#endif
