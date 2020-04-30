// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TCZLIB_H
#define TCZLIB_H

#include "../zlib/zlib.h"

#define TCZ_VERSION 200
#define ATTR_HAS_MAINCLASS  1 // if false, it is a library-only module
#define ATTR_HAS_MAINWINDOW 2
#define ATTR_LIBRARY 4
//#define ATTR_NEW_FONT_SET 8
#define ATTR_RESIZABLE_WINDOW 16
#define ATTR_WINDOWFONT_DEFAULT 32
#define ATTR_WINDOWSIZE_320X480 64
#define ATTR_WINDOWSIZE_480X640 128
#define ATTR_WINDOWSIZE_600X800 256

#define TCZ_BUFFER_SIZE 4096
typedef struct TTCZFile TTCZFile;
typedef TTCZFile* TCZFile;

typedef struct TTCZFileHeader TTCZFileHeader;
typedef TTCZFileHeader* TCZFileHeader;

struct TTCZFileHeader // common members to all instances
{
   CharPArray names;
   Int32Array offsets;
   Int32Array uncompressedSizes;
   int16 version;
   int16 attr; // see ATTR_xxx above
#ifdef ANDROID
   int32 apkIdx; // index in java's array
#else      
   FILE* fin;
#endif   
   int32 instanceCount;
   int32 realFilePos; // the current seek position
   ConstantPool cp; // this is the Global constant pool that came in this tcz file
   Heap hheap;
};

/** A TCZ (TotalCross ZLib) file is a kind of zip file. It supports multiple opens and reads
    in the same physical file.
*/
struct TTCZFile
{
   TCZFileHeader header; // common properties
   uint8 buf[TCZ_BUFFER_SIZE];
   int32 expectedFilePos; // the expected seek position (may change if several instances are processing the same file)
   Heap tempHeap; // can be assigned by the user to branch to an error handler if something wrong happens
   int32 uncompressedSize;
   z_stream zs;
};

/// Reads a number of bytes from the given tcz. Returns the number of bytes read, if the end of file has been reached
int32 tczRead(TCZFile f, void* outBuf, int32 count);
/// Reads a LE 32-bit value from the given tcz.
int32 tczRead32(TCZFile f);
/// Reads a LE 16-bit value from the given tcz.
int16 tczRead16(TCZFile f);
/// Reads a BE 32-bit value from the given tcz.
int32 tczRead32BE(TCZFile f);
/// Reads a BE 16-bit value from the given tcz.
int16 tczRead16BE(TCZFile f);
/// Reads a 8-bit value from the given tcz.
int8  tczRead8(TCZFile f);
/// Closes a file open by tczFindName and tczOpen
void tczClose(TCZFile tcz);
/// Locates the name and also positions the stream at the place to start reading it
TCZFile tczFindName(TCZFile tcz, CharP name);
/// Opens a tcz file from the given FILE. Use only if there's no constant pools in the file, otherwise, use tczLoad.
/// fileName may be null for font files.
#ifdef ANDROID
TCZFile tczOpen(CharP fileName, bool isFont);
#else
TCZFile tczOpen(FILE* fin, CharP fileName);
#endif
/// Loads a TotalCross library with the given tcz name. If there's a constant pool in the file,
/// it is loaded too. Also binds the tcz to the list of open tczs. There's no need to close the returned tcz instance.
/// VERY IMPORTANT: the tczName parameter MUST BE a temporary buffer, NEVER a constant string, because it may be changed
/// inside the function.
TCZFile tczLoad(Context currentContext, CharP tczName);
/// Locates the given filename in all loaded tcz files, "strict" mode prevents mainClass package lookup
TCZFile tczGetFile(CharP filename, bool strict);

void destroyTCZ();

#endif
