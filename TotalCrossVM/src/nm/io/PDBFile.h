// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef PDBFILE_H
#define PDBFILE_H

#include "../../palmdb/palmdb.h"

#define errNone 0

 #define PALM_ERROR !errNone
 #define DB_NAME_LENGTH 32
 #define DB_FULLNAME_LENGTH DB_NAME_LENGTH + 10

enum
{
   INVALID        = -1,
   READ_WRITE     =  3,
   CREATE         =  4,
   CREATE_EMPTY   =  5
};


#endif
