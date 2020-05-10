// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



static TCHARPs* PDBFileListByTypeCreator(uint32 creator, uint32 type, int32* count, Heap h)
{
   return listDatabasesByTypeCreator(type, creator, count, h);
}
