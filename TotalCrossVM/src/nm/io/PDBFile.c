// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "PDBFile.h"

#if defined WINCE || defined WIN32
 #include "win/PDBFile_c.h"
#else
 #include "posix/PDBFile_c.h"
#endif


//////////////////////////////////////////////////////////////////////////
// INTERNAL USE ONLY                                                    //
//////////////////////////////////////////////////////////////////////////

const uint32 dataType = 'D' << 24 | 'A' << 16 | 'T' << 8 | 'A';

static Err releaseRecord(TCObject obj)
{
   TCObject dbPObj = PDBFile_openRef(obj);
   DmOpenRef dbP;
   int32 hvRecordPos = PDBFile_hvRecordPos(obj);
   int32 hvRecordChanged = PDBFile_hvRecordChanged(obj);
   Err err = errNone;

   if (dbPObj == null)
      err = -1;
   else
   {
      if (hvRecordPos != -1) // release the current record
      {
         dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
         if (!dbP)
            err = -1;
         else
         if (DmReleaseRecord(dbP, (UInt16) hvRecordPos, (bool) hvRecordChanged) == errNone)
            PDBFile_hvRecordPos(obj) = -1;
      }
   }

   return err;
}

static void invalidate(TCObject obj)
{
   if (PDBFile_openRef(obj) != null)
   {
      setObjectLock(PDBFile_openRef(obj), UNLOCKED);
      PDBFile_openRef(obj) = null;
      PDBFile_mode(obj) = INVALID;
      PDBFile_dbId(obj) = null;
   }
   if (PDBFile_dbId(obj) != null)
   {
      setObjectLock(PDBFile_dbId(obj), UNLOCKED);
      PDBFile_dbId(obj) = null;
   }
   PDBFile_dontFinalize(obj) = true;
}

static bool destroy(TCObject obj)
{
   bool ok = true;
   TCObject dbPObj = PDBFile_openRef(obj);
   DmOpenRef dbP;

   if (dbPObj == null || releaseRecord(obj) != errNone)
      return false;

   dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
   if (!dbP)
      return false;

   ok = DmCloseDatabase(dbP) == errNone;
   invalidate(obj);
   return ok;
}

static uint32 chars2int(TCHARP fourChars)
{
   uint32 value = 0;
   int32 i = 24;

   while (tcslen(fourChars) > 0 && *fourChars != '.' && i >= 0)
   {
      value |= *fourChars ++ << i;
      i -= 8;
   }

   return value;
}

static bool splitName(TCHARP fullName, TCHARP* name, TCHARP* creator, TCHARP* type)
{
   TCHARP strP;
   int32 nameLen;
   int32 pathLen;

   if ((*type = tcsrchr(fullName, '.')) == null)
      return false;
   if (tcslen(*type) != 5)
	   return false;
   **type = 0;


   if ((*creator = tcsrchr(fullName, '.')) == null)
      return false;
   if (tcslen(*creator) != 5)
	   return false;
   **creator = 0;

   pathLen = tcslen(fullName);
   *name = fullName;
   for (strP = *creator ; strP != fullName && *strP != '/' && *strP != '\\' ; strP--);
   if (strP == fullName)
   {
      if (pathLen >= DB_NAME_LENGTH || pathLen == 0)
         return false;
   }
   else
   {
      nameLen = (int)(*creator - strP);
      if (nameLen >= DB_NAME_LENGTH || nameLen == 0)
         return false;
   }

   (*creator)++;
   (*type)++;
   return true;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_create_sssi(NMParams p) // totalcross/io/PDBFile native private void create(String name, int mode);
{
   TCObject pdbFile = p->obj[0];
   TCObject name = p->obj[1];
   TCObject creator = p->obj[2];
   TCObject type = p->obj[3];
   int32 mode = p->i32[0];

   TCObject dbIdObj;
   TCObject dbPObj;

   DmOpenRef dbP;
   VoidP dbId;

   UInt16 version;
   UInt32 modificationNumber;
   UInt32 creatorId, typeId, creatorId2, typeId2;
   Err err;

   CharP fnfeMsg;
   TCHAR szName[32];
   TCHAR szCreator[5];
   TCHAR szType[5];

   UInt16 attr = 0; //dmHdrAttrBackup;

   dbPObj = createByteArray(p->currentContext, sizeof(TPDBFile));
   if (dbPObj == null)
      return;
   dbIdObj = dbPObj;

   dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
   dbId = dbP;
   PDBFile_dbId(pdbFile) = dbIdObj;
   PDBFile_openRef(pdbFile) = dbPObj;

   JCharP2TCHARPBuf(String_charsStart(name), String_charsLen(name), szName);
   JCharP2TCHARPBuf(String_charsStart(creator), String_charsLen(creator), szCreator);
   JCharP2TCHARPBuf(String_charsStart(type), String_charsLen(type), szType);
   creatorId = chars2int(szCreator);
   typeId = chars2int(szType);

   dbId = DmFindDatabase(p->currentContext, szName, dbId);  // ATENCAO: TEM QUE VER SE O DMFINDDATABASE FAZ UM LOCK DO OBJETO NOVAMENTE! ISSO CORROMPERIA O GC

   if (dbId) // check if the creator id and type are those expected
   {
      err = DmDatabaseInfo(dbId, null, &attr, null, null, null, null, null, null, null, &typeId2, &creatorId2);
      if (err != errNone)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else if (typeId2 != typeId && creatorId2 != creatorId)
         throwException(p->currentContext, IOException, "Database already exists with different creator and type.");
      else if (typeId2 != typeId)
         throwException(p->currentContext, IOException, "Database already exists with different type.");
      else if (creatorId2 != creatorId)
         throwException(p->currentContext, IOException, "Database already exists with different creator.");
   }
   if (p->currentContext->thrownException) // So far, so good.
      goto error;
   else
   {
      switch (mode)
      {
         case CREATE_EMPTY:
            if (dbId && ((err = DmDeleteDatabase(dbId)) != errNone))
            {
               throwExceptionWithCode(p->currentContext, IOException, err);
               goto error;
            }
            dbId = 0;
            attr = 0; // flsobral@tc120_29: keep original attribute value if mode is CREATE, set to 0 on CREATE_EMTPY.
         case CREATE:
            if (!dbId && (err = DmCreateDatabase(szName, creatorId, typeId, false) != errNone))
            {
               throwExceptionWithCode(p->currentContext, IOException, err);
               goto error;
            }
            dbId = dbP;
            dbId = DmFindDatabase(p->currentContext, szName, dbId); // update dbRef
            if (!dbId)
            {
              throwExceptionWithCode(p->currentContext, IOException, DmGetLastErr());
              goto error;
            }
            version = 1;
            modificationNumber = 0;
            if ((err = DmSetDatabaseInfo(dbId, null, &attr, &version, null, null, null, &modificationNumber, null, null, null, null)) != errNone)
            {
               throwExceptionWithCode(p->currentContext, IOException, err);
               goto error;
            }
         case READ_WRITE: // open database
            if (!dbId)
            {
               fnfeMsg = (char*)xmalloc(128);
               if (fnfeMsg != null)
               {
                  xstrcpy(fnfeMsg, "Could not find the pdb file: ");
                  TCHARP2CharPBuf(szName, fnfeMsg+xstrlen(fnfeMsg));
                  xstrcat(fnfeMsg, ".");
                  TCHARP2CharPBuf(szCreator, fnfeMsg+xstrlen(fnfeMsg));
                  xstrcat(fnfeMsg, ".");
                  TCHARP2CharPBuf(szType, fnfeMsg+xstrlen(fnfeMsg));
               }
               throwException(p->currentContext, FileNotFoundException, fnfeMsg);
               xfree(fnfeMsg);
               goto error;
            }
            if (!(dbP = DmOpenDatabase(dbId, dmModeReadWrite)))
            {
               throwExceptionWithCode(p->currentContext, IOException, DmGetLastErr());
               goto error;
            }
      }
   }
   return;
error:
   invalidate(pdbFile);
}

//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_rename_s(NMParams p) // totalcross/io/PDBFile native public void rename(String newName) throws totalcross.io.IOException;
{
   TCObject pdbFile = p->obj[0];
   TCObject newName = p->obj[1];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   TCObject dbIdObj = PDBFile_dbId(pdbFile);

   VoidP dbId;

   Err err;

   TCHAR szNewPath[MAX_PATHNAME];
   TCHARP szNewName;
   TCHARP szCreator;
   TCHARP szType;
   UInt32 creator, type;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbId = (VoidP) ARRAYOBJ_START(dbIdObj);

      if (newName == null)
         throwNullArgumentException(p->currentContext, "newName");
      else
      {
         char tbuf[256];
         JCharP2TCHARPBuf(String_charsStart(newName), String_charsLen(newName), szNewPath);
         if (!splitName(szNewPath, &szNewName, &szCreator, &szType)) // split full name into NAME, CREATOR and TYPE
            throwIllegalArgumentIOException(p->currentContext, "newName", TCHARP2CharPBuf(szNewPath, tbuf));
         else
         {
            creator = chars2int(szCreator);
            type = chars2int(szType);

            if ((err = DmSetDatabaseInfo(dbId, szNewName, null, null, null, null, null, null, null, null, &type, &creator)) != NO_ERROR) // rename
               throwExceptionWithCode(p->currentContext, IOException, err);
            SysNotifyBroadcast(null);
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_addRecord_ii(NMParams p) // totalcross/io/PDBFile native public void addRecord(int size, int pos) throws totalcross.io.IOException;
{
   TCObject pdbFile = p->obj[0];
   int32 size = p->i32[0];
   int32 pos =  p->i32[1];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   MemHandle handle;

   IntBuf intBuf;
   Err err;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
      if (size < 0 || size > 65535)
         throwIllegalArgumentIOException(p->currentContext, "size", int2str(size, intBuf));
      else
      if (pos != 65535 && (pos < 0 || pos > DmNumRecords(dbP)))
         throwIllegalArgumentIOException(p->currentContext, "pos", int2str(pos, intBuf));
      else
      if ((err = releaseRecord(pdbFile)) != errNone)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else if (!(handle = DmNewRecord(dbP, (UInt16*) &pos, (UInt32) size)))
         throwExceptionWithCode(p->currentContext, IOException, DmGetLastErr());
      else
      {
         PDBFile_hvRecordPos(pdbFile) = pos;
         PDBFile_hvRecordOffset(pdbFile) = 0;
         PDBFile_hvRecordLength(pdbFile) = size;
         PDBFile_hvRecordHandle(pdbFile) = handle;
         PDBFile_hvRecordChanged(pdbFile) = false;
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_addRecord_i(NMParams p) // totalcross/io/PDBFile native public int addRecord(int size) throws totalcross.io.IOException;
{
   int32 i32[2];
   int32* i32P;

   i32[0] = p->i32[0];
   i32[1] = dmMaxRecordIndex;

   i32P = p->i32;
   p->i32 = i32;
   tiPDBF_addRecord_ii(p);
   p->i32 = i32P;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_resizeRecord_i(NMParams p) // totalcross/io/PDBFile native public void resizeRecord(int size) throws totalcross.io.IOException;
{
   TCObject pdbFile = p->obj[0];
   int32 size = p->i32[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   int32 hvRecordPos;
   MemHandle handle;

   IntBuf intBuf;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
      if (size < 0 || size > 65535)
         throwIllegalArgumentIOException(p->currentContext, "size", int2str(size, intBuf));
      else
      if ((hvRecordPos = PDBFile_hvRecordPos(pdbFile)) == -1)
         throwException(p->currentContext, IOException, "No record selected for this operation.");
      else if (!(handle = DmResizeRecord(dbP, (UInt16) hvRecordPos, (UInt32) size)))
         throwExceptionWithCode(p->currentContext, IOException, DmGetLastErr());
      else
      {
         PDBFile_hvRecordLength(pdbFile) = size;
         PDBFile_hvRecordHandle(pdbFile) = handle;
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_nativeClose(NMParams p) // totalcross/io/PDBFile native private void nativeClose();
{
   TCObject pdbFile = p->obj[0];
   TCObject dbPObj = PDBFile_openRef(pdbFile);

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   if (!destroy(pdbFile))
      throwException(p->currentContext, IOException, "Could not close the database");
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_delete(NMParams p) // totalcross/io/PDBFile native public void delete() throws totalcross.io.IOException;
{
   TCObject pdbFile = p->obj[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   TCObject dbIdObj = PDBFile_dbId(pdbFile);

   DmOpenRef dbP;
   VoidP dbId;

   Err err;

   if (dbPObj == null || dbIdObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
      dbId = (VoidP) ARRAYOBJ_START(dbIdObj);
      if (!dbP || !dbId)
         throwException(p->currentContext, IOException, "The pdb file is closed.");
      else
      {
         if ((err = DmCloseDatabase(dbP)) != errNone)
            throwExceptionWithCode(p->currentContext, IOException, err);
         else if ((err = DmDeleteDatabase(dbId)) != errNone)
         {
            dbP = DmOpenDatabase(dbId, dmModeReadWrite); // try to re-open database
            throwExceptionWithCode(p->currentContext, IOException, err);
         }
         else invalidate(pdbFile);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_listPDBs_ii(NMParams p) // totalcross/io/PDBFile native public static String []listPDBs(int creatorId, int type);
{
   uint32 creatorId = (uint32) p->i32[0];
   uint32 type = (uint32) p->i32[1];

   TCHARPs* list;
   TCHARPs* next;
   int32 count = 0, i;
   TCObject* array;
   volatile Heap h;

   h = heapCreate();
   IF_HEAP_ERROR(h)
   {
      heapDestroy(h);
      throwException(p->currentContext, OutOfMemoryError, null);
      return;
   }

   list = PDBFileListByTypeCreator(creatorId, type, &count, h);
   if (list != null)
   {
      p->retO = createArrayObject(p->currentContext, "[java.lang.String", count);
      if (p->retO != null)
      {
         array = (TCObject*) ARRAYOBJ_START(p->retO);
         for (i = 0; i < count; i ++)
            if (list->value != null)
            {
               *array = createStringObjectFromTCHAR(p->currentContext, list->value, -1);
               setObjectLock(*array, UNLOCKED);
               array++;
               next = list->next;
               list = next;
            }
         setObjectLock(p->retO, UNLOCKED);
      }
   }
   heapDestroy(h);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_deleteRecord(NMParams p) // totalcross/io/PDBFile native public void deleteRecord() throws totalcross.io.IOException;
{
   TCObject pdbFile = p->obj[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   int32 hvRecordPos;

   Err err;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
      if ((hvRecordPos = PDBFile_hvRecordPos(pdbFile)) == -1)
         throwException(p->currentContext, IOException, "No record selected for this operation.");
      else if ((err = DmRemoveRecord(dbP, (uint16) hvRecordPos) != errNone))
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         PDBFile_hvRecordPos(pdbFile) = -1;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_getRecordCount(NMParams p) // totalcross/io/PDBFile native public int getRecordCount();
{
   TCObject pdbFile = p->obj[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);
      p->retI = DmNumRecords(dbP);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_setRecordPos_i(NMParams p) // totalcross/io/PDBFile native public boolean setRecordPos(int pos) throws totalcross.io.IOException;
{
   TCObject pdbFile = p->obj[0];
   int32 pos = p->i32[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   MemHandle handle;
   int32 size = 0;

   IntBuf intBuf;

   Err err;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);

      if (pos < -1 || pos >= DmNumRecords(dbP))
         throwIllegalArgumentIOException(p->currentContext, "pos", int2str(pos, intBuf));
      else
      if ((err = releaseRecord(pdbFile)) != errNone)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
      {
         if (pos != -1)
         {
            if ((handle = DmGetRecord(dbP, (UInt16) pos)) == null)
            {
               throwExceptionWithCode(p->currentContext, IOException, DmGetLastErr());
               return;
            }
            else
               size = MemHandleSize(handle);
            PDBFile_hvRecordLength(pdbFile) = size;
            PDBFile_hvRecordHandle(pdbFile) = handle;
         }
         PDBFile_hvRecordPos(pdbFile) = pos;
         PDBFile_hvRecordOffset(pdbFile) = 0;
         PDBFile_hvRecordChanged(pdbFile) = false;
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_readWriteBytes_Biib(NMParams p)
{
   TCObject obj = p->obj[0];
   TCObject buf = p->obj[1];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   bool isRead = p->i32[2];

   int32 hvRecordOffset;
   MemHandle handle;
   CharP recPtr;
   CharP bufP;

   Err err;

   if (count == 0) // guich@tc110_65: asking to read 0 is ok.
   {
      p->retI = 0;
      return;
   }

   hvRecordOffset = PDBFile_hvRecordOffset(obj);
   handle = (MemHandle) PDBFile_hvRecordHandle(obj);

   bufP = (CharP)ARRAYOBJ_START(buf);
   recPtr = MemHandleLock(handle);
   if (isRead)
      xmemmove(bufP + start, recPtr + hvRecordOffset, count); // copy to buffer
   else
   {
      DmWrite(recPtr, (uint32) hvRecordOffset, bufP + start, (uint32) count); // Palm OS requires use of DmWrite is we don't use the semaphore trick
      PDBFile_hvRecordChanged(obj) = true;
   }
   err = MemHandleUnlock(handle);

   PDBFile_hvRecordOffset(obj) += count;
   p->retI = count == 0 && isRead ? -1 : count; // guich@tc110_65: return -1 if eof
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_inspectRecord_Bii(NMParams p) // totalcross/io/PDBFile native public int inspectRecord(byte []buf, int recPosition) throws totalcross.io.IOException;
{
   TCObject pdbFile = p->obj[0];
   TCObject byteBuf = p->obj[1];
   int32 recordPos = p->i32[0];
   int32 offsetInRec = p->i32[1];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   MemHandle handle;
   CharP recPtr;
   int32 size;
   CharP buf;
   int32 bufLen;

   IntBuf intBuf;
   Err err = errNone;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);

      if (byteBuf == null)
         throwNullArgumentException(p->currentContext, "byteBuf");
      else
      if (offsetInRec < 0 || offsetInRec > 65535)
         throwIllegalArgumentIOException(p->currentContext, "offsetInRec", int2str(offsetInRec, intBuf));
      else
      if (recordPos < 0 || recordPos >= DmNumRecords(dbP))
         throwIllegalArgumentIOException(p->currentContext, "recordPos", int2str(recordPos, intBuf));
      else
      {
         buf = (CharP)ARRAYOBJ_START(byteBuf);
         bufLen = ARRAYOBJ_LEN(byteBuf);

         if (!(handle = DmQueryRecord(dbP, (UInt16) recordPos)))
            throwExceptionWithCode(p->currentContext, IOException, DmGetLastErr());
         else
         {
            size = MemHandleSize(handle);
            if (offsetInRec > size)
               p->retI = 0;
            else
            {
               if (size > bufLen)
                  size = bufLen;

               recPtr = MemHandleLock(handle);
               xmemmove(buf, recPtr + offsetInRec, size); // copy to buffer
               err = MemHandleUnlock(handle);

               p->retI = (err == errNone ? size : -1);
            }
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_getRecordAttributes_i(NMParams p) // totalcross/io/PDBFile native public byte getRecordAttributes(int recordPos);
{
   TCObject pdbFile = p->obj[0];
   int32 recordPos = p->i32[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   UInt16 attr;
   Err err;

   IntBuf intBuf;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);

      if (recordPos < 0 || recordPos >= DmNumRecords(dbP))
         throwIllegalArgumentIOException(p->currentContext, "recordPos", int2str(recordPos, intBuf));
      else
      if ((err = DmRecordInfo(dbP, (uint16)recordPos, &attr, 0, null)) != errNone)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         p->retI = (int8) attr;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_setRecordAttributes_ib(NMParams p) // totalcross/io/PDBFile native public void setRecordAttributes(int recordPos, byte attr);
{
   TCObject pdbFile = p->obj[0];
   int32 recordPos = p->i32[0];
   int32 attr = p->i32[1];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   UInt16 attr16 = (UInt16) attr;
   Err err = errNone;

   IntBuf intBuf;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);

      if (recordPos < 0 || recordPos >= DmNumRecords(dbP))
         throwIllegalArgumentIOException(p->currentContext, "recordPos", int2str(recordPos, intBuf));
      else
      {
         if (attr == -1)
            err = DmReleaseRecord(dbP, recordPos, PDBFile_hvRecordChanged(pdbFile));
         else
            err = DmSetRecordInfo(dbP, (uint16) recordPos, &attr16, null);

         if (err != errNone)
            throwExceptionWithCode(p->currentContext, IOException, err);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_getAttributes(NMParams p) // totalcross/io/PDBFile native public int getAttributes();
{
   TCObject pdbFile = p->obj[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   TCObject dbIdObj = PDBFile_dbId(pdbFile);
   VoidP dbId;

   UInt16 attr;
   Err err;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbId = (VoidP) ARRAYOBJ_START(dbIdObj);

      if ((err = DmDatabaseInfo(dbId, null, &attr, null, null, null, null, null, null, null, null, null)) != errNone )
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         p->retI = (int32) attr;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_setAttributes_i(NMParams p) // totalcross/io/PDBFile native public void setAttributes(int i);
{
   TCObject pdbFile = p->obj[0];
   UInt16 attr = (UInt16) p->i32[0];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   TCObject dbIdObj = PDBFile_dbId(pdbFile);
   VoidP dbId;

   Err err;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbId = (VoidP) ARRAYOBJ_START(dbIdObj);

      if ((err = DmSetDatabaseInfo(dbId, null, &attr, null, null, null, null, null, null, null, null, null)) != errNone)
         throwExceptionWithCode(p->currentContext, IOException, err);
      SysNotifyBroadcast(null);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiPDBF_searchBytes_Bii(NMParams p) // totalcross/io/PDBFile native public int searchBytes(byte []toSearch, int length, int offsetInRec);
{
   TCObject pdbFile = p->obj[0];
   TCObject toSearch = p->obj[1];
   int32 length = p->i32[0];
   int32 offsetInRec = p->i32[1];

   TCObject dbPObj = PDBFile_openRef(pdbFile);
   DmOpenRef dbP;
   MemHandle handle;

   CharP recPtr;
   CharP toSearchBuf;
   CharP toSearchFirst;
   CharP toSearchLast;
   int32 i;
   int32 n;
   int32 lenM1;
   int32 lenM2;
   int32 found;
   int32 toSearchLen;
   uint32 minRecSize;

   IntBuf intBuf;

   if (dbPObj == null)
      throwException(p->currentContext, IOException, "The pdb file is closed.");
   else
   {
      dbP = (DmOpenRef) ARRAYOBJ_START(dbPObj);

      if (toSearch == null)
         throwNullArgumentException(p->currentContext, "toSearch");
      else
      if (offsetInRec < 0)
         throwIllegalArgumentIOException(p->currentContext, "offsetInRec", int2str(offsetInRec, intBuf));
      else
      if (length <= 0 || length > (toSearchLen = ARRAYOBJ_LEN(toSearch)))
         throwException(p->currentContext, ArrayIndexOutOfBoundsException, null);
      else
      {
         toSearchBuf = (CharP)ARRAYOBJ_START(toSearch);
         toSearchFirst = toSearchBuf + 1;
         toSearchLast = toSearchBuf + length - 1;
         lenM1 = length - 1;
         lenM2 = length - 2;
         found = -1;
         minRecSize = length + offsetInRec;

         n = DmNumRecords(dbP);

         for (i = max32(0, PDBFile_hvRecordPos(pdbFile)); i < n && found == -1; i ++)
         {
            handle = DmQueryRecord(dbP, (UInt16) i);
            if (handle != null && MemHandleSize(handle) >= minRecSize)
            {
               recPtr = ((CharP) MemHandleLock(handle)) + offsetInRec;
               if (*recPtr == *toSearchBuf && *(recPtr + lenM1) == *toSearchLast)
               {
                  if (length <= 2) // block has one or two bytes, so we have already compared the whole block
                     found = i;
                  else // otherwise, we still have more bytes to compare
                  if (xmemcmp(recPtr + 1, toSearchFirst, lenM2) == 0)
                     found = i;
               }
               MemHandleUnlock(handle);
            }
         }
         p->retI = found;
      }
   }
}

#ifdef ENABLE_TEST_SUITE
#include "PDBFile_test.h"
#endif
