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

// $Id: stdlib.c,v 1.18 2011-01-04 13:31:15 guich Exp $

#define TRACK_MEMORY_LEAKS

#include "stdlib.h"
#include "palm_posix.h"

extern MemPtr MemGluePtrNew(UInt32 size);
UInt16 currentOwnerID;

char *getenv(const char *name)
{
   name = name; // remove warning
   return 0;
}

void *malloc(UInt32 size)
{
   UInt8 * ptr = MemChunkNew(0, size, currentOwnerID | memNewChunkFlagAllowLarge | memNewChunkFlagNonMovable);
   if (ptr)
      MemSet(ptr, size, 0);
   //if (alert_cb) alert_cb("alloc(%d) in %s line %d: %lX",(int)size, file, line, (long)memH);}
   return ptr;
}

void free(void *p)
{
   if (p)
      MemChunkFree(p);
}

// resizes the ptr.
void *realloc(void *p, UInt32 newSize)
{
   p = p;
   newSize = newSize;
   if (alert_cb) alert_cb("realloc not supported!");
   return NULL;
}

UInt32 freeMemInfo()
{
   UInt32 maxP,freeBytesP; // guich@340_56
   MemHeapFreeBytes (0, &freeBytesP, &maxP);
   return freeBytesP;
}

void exit (int status)
{
   EventType newEvent;
   MemSet(&newEvent, sizeof(newEvent), 0);
   newEvent.eType = keyDownEvent;
   newEvent.data.keyDown.chr = launchChr;
   newEvent.data.keyDown.modifiers = commandKeyMask;
   SysHandleEvent(&newEvent);
   status = status;
}
