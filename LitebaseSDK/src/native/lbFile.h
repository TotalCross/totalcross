/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

/**
 * This module declares functions that handles files.
 */

#ifndef LITEBASE_LBFILE_H
#define LITEBASE_LBFILE_H

#include "Litebase.h"

extern inline Err lbfileCreate(NATIVE_FILE* fref, TCHARP path, int32 mode, int32* slot);
extern inline Err lbfileClose(NATIVE_FILE* fref);
extern inline Err lbfileCreateDir(TCHARP path, int32 slot);
extern inline Err lbfileDelete(NATIVE_FILE* fref, TCHARP path, int32 slot, bool isOpen);
extern inline bool lbfileExists(TCHARP path, int32 slot);
extern inline Err lbfileGetSize(NATIVE_FILE fref, TCHARP szPath, int32* size);
extern inline Err lbfileReadBytes(NATIVE_FILE fref, CharP bytes, int32 offset, int32 length, int32* bytesRead);
extern inline Err lbfileRename(NATIVE_FILE fref, int32 slot, TCHARP currPath, TCHARP newPath, bool isOpen);
extern inline Err lbfileSetPos(NATIVE_FILE fref, int32 position);
extern inline Err lbfileWriteBytes(NATIVE_FILE fref, CharP bytes, int32 offset, int32 length, int32* bytesWritten);
extern inline Err lbfileSetSize(NATIVE_FILE* fref, int32 newSize);
extern inline Err lbfileFlush(NATIVE_FILE fref);

#endif
