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

// $Id: PDBFile_c.h,v 1.6 2011-01-04 13:31:19 guich Exp $

static inline TCHARPs* PDBFileListByTypeCreator(uint32 creator, uint32 type, int32* count, Heap h)
{
   return listDatabasesByTypeCreator(type, creator, count, h);
}
