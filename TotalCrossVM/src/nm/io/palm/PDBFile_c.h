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

// $Id: PDBFile_c.h,v 1.24 2011-01-04 13:31:07 guich Exp $

static TCHARPs* PDBFileListByTypeCreator(UInt32 creator, UInt32 type, int32* count, Heap h)
{
   Err err;
   VoidP dbId = null;
   DmSearchStateType state;
   char name[32];
   int32 nameLen;
   UInt32 creatorRead, typeRead;
   TCHARPs* list = null;
   CharP s;

   err = DmGetNextDatabaseByTypeCreator(true, &state, type, creator, false, (VoidP) &dbId);
   while (err == errNone && dbId != null)
   {
      if (DmDatabaseInfo(dbId, name, null, null, null, null, null, null, null, null, &typeRead, &creatorRead) == errNone)
      {
         nameLen = xstrlen(name);
         s = (CharP) heapAlloc(h, nameLen + 11);
         xstrcpy(s, name);
         s[nameLen] = '.';
         int2CRID((int32) creatorRead, s + nameLen + 1);
         s[nameLen+5] = '.';
         int2CRID((int32) typeRead, s + nameLen + 6);
         s[nameLen+11] = 0;

         list = TCHARPsAdd(list, s, h); // add entry to list
         (*count) ++;
      }
      err = DmGetNextDatabaseByTypeCreator(false, &state, type, creator, false, (VoidP) &dbId);
   }
   return list;
}
