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



#if HAVE_CONFIG_H
#include "config.h"
#endif

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#ifndef PALMOS5
#include <Extensions/ExpansionMgr/VFSMgr.h>
#include <Core/System/FileStream.h>
#endif
#endif

//#include <palm_libc_c.h>
#include "limits.h"
#include <assert.h>
#include <stdlib.h>
#include <logs.h>
#include <unistd.h>
#include <stdio.h>
#ifdef STDINC
#include <fcntl.h>
#endif

#ifdef __arm__
#define PALM_CARD(x)
#else
#define PALM_CARD(x) x,
#endif

typedef struct FILEDESC
{
   FileHand fh;
   FileRef fr;
}
FILEDESC;

#define MAX_DESC  255

static FILEDESC *descriptors[MAX_DESC];

static UInt16 getVN()
{
   UInt32 it = vfsIteratorStart;
   UInt16 vn, lastValid = 0;
   Err err;
   while (it != vfsIteratorStop)
   {
      err = VFSVolumeEnumerate(&vn, &it);
      if (err == errNone)
         lastValid = vn;
      else
         break;
   }
   return lastValid;
}

int open(const char *pathname, int flags, ...)
{
   FileHand fh = NULL;
   FileRef fr = 0;
   Err error;
   FILEDESC *newFILEDESC;
   int i;

   if (pathname == NULL || StrLen(pathname) == 0) return -1;

   if (pathname[0] == '/')
   {
      UInt16 openMode = 0xFFFF;
      UInt16 volRefNum = getVN();

      if (flags == O_RDONLY)
      {
         openMode = vfsModeRead;
      }
      else
      {
         if (flags & O_RDWR)
         {
            openMode = vfsModeReadWrite;
         }
         else if (flags & O_WRONLY)
         {
            openMode = vfsModeWrite;
         }
         if (flags & O_APPEND)
         {
            openMode = vfsModeWrite;
         }
      }

      if (openMode == 0xFFFF)
         fatal(__FILE__, __LINE__, "'0x%x' unsupported flags", flags);

      error = VFSFileOpen (volRefNum, pathname, openMode, &fr);

      if (error != errNone || fr == 0)
      {
         fprintf(stderr, "VFSFileOpen(%s) vol=%d err=%d", pathname, (int)volRefNum, (int)error);
         return 0;
      }
   }
   else
   {
      UInt32 openMode = UINT_MAX;

      if (flags == O_RDONLY)
      {
         openMode=fileModeReadOnly;
      }
      else
      {
         if (flags & O_RDWR)
         {
            openMode=fileModeUpdate;
         }
         else if (flags & O_WRONLY)
         {
            openMode=fileModeReadWrite;
         }
         if (flags & O_APPEND)
         {
            openMode=fileModeAppend;
         }
      }

      if (openMode == UINT_MAX)
         fatal(__FILE__, __LINE__, "'0x%x' unsupported flags", flags);

      fh = FileOpen (PALM_CARD(0) pathname, 0, 0, openMode, &error);

      if (error != errNone || fh == NULL)
      {
         fprintf(stderr, "FileOpen(%s) err=%d", pathname, (int)error);
         return 0;
      }
      else
      {
         if (openMode != fileModeAppend)
         {
            FileRewind (fh);
         }
      }
   }

   newFILEDESC = (FILEDESC *)malloc(sizeof(FILEDESC));
   if (newFILEDESC == NULL)
   {
      FileClose(fh);
      fatal(__FILE__, __LINE__, "open() out of memory");
   }

   newFILEDESC->fh = fh;
   newFILEDESC->fr = fr;

   for (i = 0; i < MAX_DESC; i++)
      if (descriptors[i] == NULL) break;

   if (i == MAX_DESC) return -1;

   descriptors[i] = newFILEDESC;

   return i;
}

ssize_t read(int fd, void *buf, size_t nbytes)
{
   Err error = 0;
   Int32 pos, fileSize;

   if (descriptors[fd] != NULL && descriptors[fd]->fh != NULL)
   {
      int num;
      pos = FileTell (descriptors[fd]->fh, &fileSize, &error);
      if ((fileSize-pos) < (Int32)nbytes)
         nbytes = fileSize-pos;

      num = FileRead(descriptors[fd]->fh, buf, nbytes, 1, &error);
      if (error == errNone || error == fileErrEOF) return nbytes;
   }
   else if (descriptors[fd] != NULL && descriptors[fd]->fr != 0)
   {
      int num;
      UInt32 nread;
      num = VFSFileRead(descriptors[fd]->fr, nbytes, buf, &nread);
//Err VFSFileRead(FileRef fileRef, UInt32 numBytes, void *bufP, UInt32 *numBytesReadP)
      if (error == errNone || error == fileErrEOF) return nbytes;
      //fatal(__FILE__, __LINE__, "card not supported");
   }
   else
      fatal(__FILE__, __LINE__, "read() bad handle");

   fprintf(stderr, "read error %x", error);

   return -1;
}

ssize_t write (int fd, const void *buf, size_t n)
{
   Err error = 0;

   if (descriptors[fd] != NULL && descriptors[fd]->fh != NULL)
   {
      int num;
      num = FileWrite(descriptors[fd]->fh, buf, n, 1, &error);
      if (error == errNone) return n;
   }
   else if (descriptors[fd] != NULL && descriptors[fd]->fr != 0)
   {
      fatal(__FILE__, __LINE__, "card not supported");
   }
   else
      fatal(__FILE__, __LINE__, "write() bad handle");

   fprintf(stderr, "write error %x", error);

   return -1;
}

int close(int fd)
{
   if (fd <= 0) return -1;

   fprintf(stderr, "close %x", fd);
   if (descriptors[fd] != NULL && descriptors[fd]->fh != NULL)
   {
      FileClose(descriptors[fd]->fh);
   }
   else if (descriptors[fd] != NULL && descriptors[fd]->fr != 0)
   {
      VFSFileClose(descriptors[fd]->fr);
   }
   else
      fatal(__FILE__, __LINE__, "close() bad handle");

   descriptors[fd] = NULL;
   return 0;
}
