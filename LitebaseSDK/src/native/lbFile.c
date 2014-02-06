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
 * This module defines useful functions for other Litebase modules.
 */

#include "lbFile.h"

#if defined PALMOS
   #include "palm/File_c.h"
#elif defined WIN32
   #include "win/File_c.h"
#else
   #include "posix/File_c.h"
#endif

inline_ Err lbfileCreate(NATIVE_FILE* fref, TCHARP path, int32 mode, int32* slot)
{
   return fileCreate(fref, path, mode, slot);
}
inline_ Err lbfileClose(NATIVE_FILE* fref)
{
   return fileClose(fref);
}
inline_ Err lbfileCreateDir(TCHARP path, int32 slot)
{
   return fileCreateDir(path, slot);
}
inline_ Err lbfileDelete(NATIVE_FILE* fref, TCHARP path, int32 slot, bool isOpen)
{
   return fileDelete(fref, path, slot, isOpen);
}
inline_ bool lbfileExists(TCHARP path, int32 slot)
{                                              
   return fileExists(path, slot);
}
inline_ Err lbfileGetSize(NATIVE_FILE fref, TCHARP szPath, int32* size)
{                                
   return fileGetSize(fref, szPath, size);
}
inline_ Err lbfileReadBytes(NATIVE_FILE fref, CharP bytes, int32 offset, int32 length, int32* bytesRead)
{                                         
   return fileReadBytes(fref, bytes, offset, length, bytesRead);
}
inline_ Err lbfileRename(NATIVE_FILE fref, int32 slot, TCHARP currPath, TCHARP newPath, bool isOpen)
{                                                              
   return fileRename(fref, slot, currPath, newPath, isOpen);
}
inline_ Err lbfileSetPos(NATIVE_FILE fref, int32 position)
{                                                           
   return fileSetPos(fref, position);
}
inline_ Err lbfileWriteBytes(NATIVE_FILE fref, CharP bytes, int32 offset, int32 length, int32* bytesWritten)
{                                    
   return fileWriteBytes(fref, bytes, offset, length, bytesWritten);
}
inline_ Err lbfileSetSize(NATIVE_FILE* fref, int32 newSize)
{                                                                   
   return fileSetSize(fref, newSize);
}
inline_ Err lbfileFlush(NATIVE_FILE fref)
{                                    
   return fileFlush(fref);
}
