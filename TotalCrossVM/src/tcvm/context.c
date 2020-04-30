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

#include "tcvm.h"

Context initContexts()
{
   gcContext = newContext(null, null, false);
   if (gcContext == null)
      return null;                           
   lifeContext = newContext(null,null,false);
   mainContext = newContext(null,null,true);
   return mainContext;
}

void destroyContexts()
{                                    
   int32 i;
   Context c;
   for (i = 0; i < MAX_CONTEXTS; i++)
      if ((c=contexts[i]) != null)
      {
         contexts[i] = null;
         deleteContext(c, true);
      }
}


Context newContext(ThreadHandle thread, TCObject threadObj, bool bigContextSizes)
{
   volatile Heap heap;
   Context c;
   int32 i;
   int32 regIsize, regOsize, reg64size, stackSize;
   
   bool freeSlot = false;
   for (i = 0; i < MAX_CONTEXTS && !freeSlot; i++)
      if (contexts[i] == null)
         freeSlot = true;
   if (!freeSlot) 
      return null;

   heap = heapCreate();
   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      return null;
   }
   regIsize  = bigContextSizes ? STARTING_REGI_SIZE : (STARTING_REGI_SIZE/10);
   regOsize  = bigContextSizes ? STARTING_REGO_SIZE : (STARTING_REGO_SIZE/10);
   reg64size = bigContextSizes ? STARTING_REG64_SIZE : (STARTING_REG64_SIZE/10);
   stackSize = bigContextSizes ? STARTING_STACK_SIZE : (STARTING_STACK_SIZE/10);
   c = newXH(Context, heap);
   LOCKVAR(omm);
   for (i = 0; i < MAX_CONTEXTS; i++)
      if (contexts[i] == null)
      {
         contexts[i] = c;
         break;
      }
   UNLOCKVAR(omm);
   if (i == MAX_CONTEXTS)
   {                     
      debug("*** NO MORE CONTEXT SLOTS!");
      heapDestroy(heap);
      return null;
   }
   
   c->heap = heap;
   c->threadObj = threadObj;
   c->regI         = c->regIStart      = newPtrArrayOf(Int32, regIsize, c->heap);
   c->regIEnd      = c->regIStart      + regIsize;
   c->regO         = c->regOStart      = newPtrArrayOf(TCObject, regOsize, c->heap);
   c->regOEnd      = c->regOStart      + regOsize;
   c->reg64        = c->reg64Start     = newArrayOf(Value64, reg64size, c->heap);
   c->reg64End     = c->reg64Start     + reg64size;
   c->callStack    = c->callStackStart = newPtrArrayOf(VoidP, stackSize, c->heap);
   c->callStackEnd = c->callStackStart + stackSize;
   c->thread = thread;
   c->nmp.currentContext = c;
   SETUP_MUTEX;
   INIT_MUTEX(c->usageLock);
      
   if (mainContext != null && c != null) // for the first context, it will be created later
      c->OutOfMemoryErrorObj = createObject(c, "java.lang.OutOfMemoryError"); // create the exception and prevent the exception from being collected. Note that there's no need to lock the msg and trace strings inside of it.
   return c;
}

void deleteContext(Context c, bool destroyThread)
{
   int32 i;
   if (c->thread && destroyThread) // destroy the thread
      threadDestroy(c->thread, false);
   LOCKVAR(omm);
   for (i = 0; i < MAX_CONTEXTS; i++)
      if (contexts[i] == c) 
      {
         contexts[i] = null;
         break;
      }
   if (c->OutOfMemoryErrorObj != null) setObjectLock(c->OutOfMemoryErrorObj, UNLOCKED);
   UNLOCKVAR(omm);
   xfree(c->litebasePtr); // free litebase pointer
   DESTROY_MUTEX(c->usageLock);
   heapDestroy(c->heap);
}

static bool contextIncrease(Heap h, uint8** start, uint8** end, uint8** current, uint8** r, uint32 ssize, uint32 inc)
{
   uint8* a;
   int32 oldLen,newLen;
   IF_HEAP_ERROR(h)  // do NOT destroy the heap
      return false;
   oldLen = ARRAYLEN(*start);
   newLen = oldLen + inc;
   a = (uint8*)newArray(ssize, newLen, h); // create the new array
   xmemmove(a, *start, oldLen * ssize); // move old contents
   // setup new pointers
   *current = ((int32)(*current - *start)) + a; // must be the first update!
   if (r) *r = ((int32)(*r - *start)) + a;
   heapFreeArray(h, *start); // free old pointer
   *start = a;
   *end   = a + newLen * ssize;
   return true;
}

bool contextIncreaseRegI(Context c, int32** r)
{
   return contextIncrease(c->heap, (uint8**)&c->regIStart, (uint8**)&c->regIEnd, (uint8**)&c->regI, (uint8**)r, sizeof(*c->regI), STARTING_REGI_SIZE / 4);
}
bool contextIncreaseRegO(Context c, TCObject** r)
{
   return contextIncrease(c->heap, (uint8**)&c->regOStart, (uint8**)&c->regOEnd, (uint8**)&c->regO, (uint8**)r, sizeof(*c->regO), STARTING_REGO_SIZE / 4);
}
bool contextIncreaseReg64(Context c, Value64* r)
{
   return contextIncrease(c->heap, (uint8**)&c->reg64Start, (uint8**)&c->reg64End, (uint8**)&c->reg64, (uint8**)r, sizeof(*c->reg64), STARTING_REG64_SIZE / 4);
}
bool contextIncreaseCallStack(Context c)
{
   return contextIncrease(c->heap, (uint8**)&c->callStackStart, (uint8**)&c->callStackEnd, (uint8**)&c->callStack, null, sizeof(*c->callStack), STARTING_STACK_SIZE / 4);
}

Context getMainContext()
{
   return mainContext;
}
