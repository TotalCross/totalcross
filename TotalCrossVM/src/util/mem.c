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

#if defined(darwin)
#define EXTRA4ALIGN 0
#else
//#if defined(WIN32) || defined(ANDROID) || defined(darwin) // windows always give us aligned blocks
#define EXTRA4ALIGN 4
#endif

#define XMALLOC_MARK_START 8  // note: start must be multiple of 4, otherwise a "datatype misnaligned" will be thrown in wince
#define XMALLOC_MARK_END 2
#define XMALLOC_MARKSSIZE (XMALLOC_MARK_START + XMALLOC_MARK_END)
#define XMALLOC_EXTRASIZE XMALLOC_MARKSSIZE

#if defined(FORCE_LIBC_ALLOC) || defined(ENABLE_WIN32_POINTER_VERIFICATION)
int32 getUsedMemory();
static void updateStats()
{
   totalAllocated = getUsedMemory();
   if (totalAllocated > maxAllocated)
      maxAllocated = totalAllocated;
}
#endif

//// Memory failure test ////
int32 allocCount2ReturnNull;
TC_API void setCountToReturnNull(int32 n)
{
   allocCount2ReturnNull = n;
}
TC_API int32 getCountToReturnNull()
{
   return allocCount2ReturnNull;
}

//// Primitive allocation ////
static VoidP realMalloc(uint32 size)
{
   VoidP p = malloc(size);
   if (p == null)
   {
      Sleep(500);
      p = malloc(size);
      if (p)
         debug("*** Sleep worked to alloc %d", size);
   }
   return p;
}
static void realFree(VoidP p) 
{
   free(p);
}
////

#ifdef ENABLE_WIN32_POINTER_VERIFICATION // special memory allocation to find bugs in windows 32
#define malloc DbgMalloc
#define free DbgFree
int32 alocd;

VOID* DbgMalloc(SIZE_T size)
{
   SIZE_T rounded_size;
   BYTE* start;
	DWORD old_protect;
   BOOL res;
   int32 pageSize;
   SYSTEM_INFO sys_info;

   if (allocCount2ReturnNull > 0 && --allocCount2ReturnNull == 0) // used on test suites to return null after a given number of allocations
      return null;

#ifdef INITIAL_MEM
   if (size > maxAvail)
      return null;
   maxAvail -= size;
#endif
   alocd += size;
   //debug("malloc %d / %d",size,alocd);

	GetSystemInfo(&sys_info);
	pageSize = sys_info.dwPageSize;
	rounded_size = (size + 4 + pageSize - 1) & (-pageSize); // round up to the page boundary

	start = (BYTE*)VirtualAlloc(NULL, rounded_size + pageSize, MEM_COMMIT, PAGE_READWRITE);
   if (start == null)
      return null;
	res = VirtualProtect(start + rounded_size, pageSize, PAGE_NOACCESS, &old_protect);
   start += (rounded_size - size - 4);
   *((int32*)start) = size;
	return start + 4;
}

VOID DbgFree(VOID* memblock)
{
   uint32* p = memblock;
   int32 n = p[-1];
   alocd -= n;
#ifdef INITIAL_MEM
   maxAvail += n;
#endif
   //debug("free %d / %d",n,alocd);
	VirtualFree(p-1, 0, MEM_RELEASE);
}

uint32 dbgGetPtrSize(VOID* memblock)
{
   uint32* p = memblock;
   return p[-1];
}
#endif

///////////////////////////////////////////////////////////////////////////
//                              Memory Heap                              //
///////////////////////////////////////////////////////////////////////////

static bool checkMallocLeaks();
static bool checkMemHeapLeaks();

bool initMem()
{
   #if defined(WIN32) && !defined(WINCE) && defined(_DEBUG)
   //leakCheckingEnabled = true;
   #endif
   return true;
}

static bool checkMemHeapLeaks()
{
   if (createdHeaps != null) // no threads are alive at this point, so there's no need to lock
   {
      VoidPs* current = createdHeaps;
      debug("The programmer forgot to destroy some heaps created at:");
      do
      {
         Heap m = (Heap)current->value;
         debug(" %s (%d): %X #%d", m->ex.creationFile, m->ex.creationLine, m, m->count);
         current = current->next;
      } while (createdHeaps != current);
      return true;
   }
   return false;
}

void destroyMem()
{
   bool b1 = checkMemHeapLeaks();
   bool b2 = checkMallocLeaks();
#if defined(DEBUG) || defined(debug)
   if (showMemoryMessagesAtExit && (b1 || b2 || warnOnExit)) // guich@tc114_44
      alert("Memory %s found. Check the\ndebug console for more information.", warnOnExit ? "problems" : "leaks");
#endif        
}

static int32 hpcount;

TC_API Heap privateHeapCreate(bool add2list, const char *file, int32 line)
{
   Heap p = newX(Heap);
   if (p)
   {
      int32 len = xstrlen(file);
      VoidPs* ch;
      xstrcpy(p->ex.creationFile, max32(0,len-(sizeof(p->ex.creationFile)-1)) + (char*)file); // if src is bigger than the buffer, copy the end of the string (the filename is more important than the path)
      p->ex.creationLine = line;
      p->count = ++hpcount;
      if (add2list)
      {
         LOCKVAR(createdHeaps);
         ch = VoidPsAdd(createdHeaps, p, null); // cannot use a heap in VoidPsAdd
         if (ch == null)
            xfree(p);
         else
            createdHeaps = ch;
         UNLOCKVAR(createdHeaps);
      }
   }
   return p;
}

static MemBlock createMemBlock(Heap m, uint32 size)
{
   MemBlock mb = newX(MemBlock);
   //debug("createMemBlock %d",size);
   if (!mb)
      HEAP_ERROR(m, HEAP_MEMORY_ERROR);
   mb->block = newPtrArrayOf(UInt8, size+EXTRA4ALIGN, 0); // to be able to align if necessary
   if (!mb->block)
   {
      xfree(mb);
      debug("heap error of heap created at %s (%d). setjmp at %s (%d)",m->ex.creationFile,m->ex.creationLine,m->ex.setjmpFile, m->ex.setjmpLine);
      HEAP_ERROR(m, HEAP_MEMORY_ERROR);
   }
   mb->current = mb->block;
   mb->availSize = ARRAYLEN(mb->block) - EXTRA4ALIGN; // guich@tc114_16: decrease EXTRA4ALIGN from the avail size
   mb->next = m->current;
   m->current = mb;
   m->blocksAlloc++;
   while ((int32)mb->current & 1) // make sure that the address is aligned
      mb->current++;
   return mb;
}

static uint32 getIdealBlockSize(Heap m) // 100 x mean size of allocated blocks
{
   uint32 mean = m->totalAlloc / m->numAlloc;
   if (m->greedyAlloc)
      return 3*mean;
   else
      return min32(16300, max32(100*mean, MEMBLOCK_SIZE));
}

TC_API void* heapAlloc(Heap m, uint32 size)
{
   void* ret;
   MemBlock allocFrom = m->current;
   if (allocCount2ReturnNull > 0 && --allocCount2ReturnNull == 0) // used on test suites to return null after a given number of allocations
   {
      HEAP_ERROR(m, HEAP_MEMORY_ERROR);
   }
   size = ((size+3)>>2)<<2; // align at 4-bytes
   m->totalAlloc += size;
   m->numAlloc++;
   if (m->current == null || allocFrom->availSize < size) // need to alloc one more chunk?
   {
      uint32 blockSize = 0;
      if (size >= MEMBLOCK_SIZE) // must be kept separately, bc the user may free this block
         allocFrom = createMemBlock(m, blockSize = size);
      else
      if (m->current == null || m->totalAvail < size) // first block or total available not enough to hold the block?
         allocFrom = createMemBlock(m, blockSize = max32(size,getIdealBlockSize(m))); // must always have size for the desired block
      else
      {
         // search in allocated blocks for one that can hold the requested size
         for (; allocFrom != null; allocFrom = allocFrom->next)
            if (allocFrom->availSize >= size)
               break;
         if (!allocFrom)
            allocFrom = createMemBlock(m, blockSize = max32(size,getIdealBlockSize(m)));
      }
      if (blockSize > 0)
         m->totalAvail += blockSize;
   }
   ret = allocFrom->current;
   allocFrom->current += size;
   allocFrom->availSize -= size;
   m->totalAvail -= size;
   return ret;
}

void heapFree(Heap m, void* ptr)
{
   MemBlock head, prev;
   for (head = m->current, prev = null; head != null; prev = head, head = head->next)
      if (head->block == ptr)
      {
         int32 size = ARRAYLEN(head->block);
         if (size > MEMBLOCK_SIZE)
         {
            // remove from the linked list
            if (head == m->current)
               m->current = head->next;
            else
            if (prev) // guich@tc330
               prev->next = head->next;
            freeArray(head->block);
            xfree(head);
            m->blocksAlloc--;
            if (prev == null) // prevents program crash
               break;
         }
         break;
      }
}

void heapFreeAsking(Heap m, AskIfFreeFunc ask)
{
   MemBlock current = m->current, prev = null, next;
   while (current != null)
      if (ask(current->block, ARRAYLEN(current->block) - EXTRA4ALIGN))
      {
         // remove from the linked list
         if (current == m->current)
            next = m->current = current->next;
         else
            next = prev->next = current->next;
         freeArray(current->block);
         xfree(current);
         current = next;
      }
      else
      {
         prev = current;
         current = current->next;
      }
}

TC_API void heapDestroyPrivate(Heap m, bool added2list)
{
   if (!m)
      return;
   // run the finalizer
   if (m->finalizerFunc)
      m->finalizerFunc(m, m->finalizerBag);
   // free all allocated blocks
   //debug("Freeing heap created at %s (%d). setjmp at %s (%d): %X",m->ex.creationFile,m->ex.creationLine,m->ex.setjmpFile, m->ex.setjmpLine, m);
   while (m->current != null)
   {
      volatile MemBlock mb = m->current->next;
      //xmemzero(m->current->block, ARRAYLEN(m->current->block)); // erase the block to make sure that no pointers inside of it are reused
      freeArray(m->current->block);
      xfree(m->current);
      m->current = mb;
   }
   m->finalizerFunc = null;
   if (added2list)
   {
      LOCKVAR(createdHeaps);
      createdHeaps = VoidPsRemove(createdHeaps, m, null);
      UNLOCKVAR(createdHeaps);
   }
   xfree(m);
}

TC_API void privateHeapError(Heap m, int32 errorCode, const char *file, int32 line)
{
   jmp_buf errorJump;
   int32 len = xstrlen(file);
   xstrcpy(m->ex.errorFile, max32(0,len-(sizeof(m->ex.errorFile)-1)) + (char*)file); // if src is bigger than the buffer, copy the end of the string (the filename is more important than the path)
   m->ex.errorLine = line;
   m->ex.errorCode = errorCode;
   xmemmove(errorJump, m->ex.errorJump, sizeof(jmp_buf)); // create a local copy since the original will be destroyed
   //heapDestroy(m); - must be done by the user, otherwise, the error* informations will be destroyed
   longjmp(errorJump, errorCode);
}

void heapSetFinalizer(Heap m, HeapFinalizerFunc fin, void* bag)
{
   m->finalizerFunc = fin;
   m->finalizerBag = bag;
}

TC_API int32 privateHeapSetJump(Heap m, const char *file, int32 line)
{
   int32 len = xstrlen(file);
   xstrcpy(m->ex.setjmpFile, max32(0,len-(sizeof(m->ex.setjmpFile)-1)) + (char*)file); // if src is bigger than the buffer, copy the end of the string (the filename is more important than the path)
   m->ex.setjmpLine = line;
   return 0;
}


///////////////////////////////////////////////////////////////////////////
//                      Atomic Memory Allocation                         //
///////////////////////////////////////////////////////////////////////////

/********************************************************************
 * Hashtable support - important: we can't use the one defined in
 * datastructures.h because we must use a different method for allocating
 * memory. Using the other one would result in a infinite recursive call.
 ********************************************************************/
struct htmElem
{
   uint32 addr;
   void* memH;
   int32 line;
   char src[64];
   int32 count;
   struct htmElem *next;
};

#ifdef ENABLE_WIN32_POINTER_VERIFICATION
#define MEMHASHSIZE 0xFFFF
#else
#define MEMHASHSIZE 0xFF
#endif
#define htmHash(key) (unsigned)((key>>2) & MEMHASHSIZE)
struct htmElem *mHashTable[MEMHASHSIZE+1];

/* return the htElem found, else NULL if key doesn't exist */
static struct htmElem *htmGet(uint32 addr)
{
   struct htmElem *np;
   for (np = mHashTable[htmHash(addr)]; np; np = np->next)
      if (np->addr == addr)
         return np;
   return NULL;
}
static int32 alcount;

/* return struct containing value or NULL if unable to store */
static struct htmElem *htmPut(uint32 addr, int line, const char* src)
{
   struct htmElem *np;
   int32 hashval;
   //debug(int2str("htmPut",addr));
   if ((np = htmGet(addr)) == NULL) // duplicated keys not allowed
   {
      void* memH = realMalloc(sizeof(struct htmElem));
      if (memH)
      {
         int len;
         np = (struct htmElem *)memH;
         hashval = htmHash(addr);
         /* point to last w/ this hash */
         np->next = mHashTable[hashval];
         np->addr = addr;
         np->line = line;
         np->memH = memH;
         np->count = ++alcount;
         len = xstrlen(src);
         xstrcpy(np->src, max32(0,len-(sizeof(np->src)-1)) + (char*)src); // if src is bigger than the buffer, copy the end of the string (the filename is more important than the path)
         /* attach to hashtab array */
         mHashTable[hashval] = np;
      }
   }
   //else (*vmGlobals->int2str)("**** duplicated key in hashtable ****",key);
   return np;
}
static bool htmRemove(uint32 addr)
{
   struct htmElem *np;
   int32 h = htmHash(addr);
   struct htmElem *start;
   struct htmElem *prev = 0;
   start = mHashTable[h];
   for (np = start; np; prev=np, np = np->next)
      if (np->addr == addr)
      {
         if (prev)
            prev->next = np->next;
         else
            mHashTable[h] = np->next;
         //debug(int2str("htRem ok",addr));
         realFree(np->memH);
         return true;
      }
   //debug(int2str("htRem NOK",addr));
   return false;
}
static bool htmDispose()
{
   int32 i;
   bool error = false;
   // dispose the hashtable
   for (i = 0; i <= MEMHASHSIZE; i++) // no threads are running at this moment.
   {
      struct htmElem *np, *next;
      for (np = mHashTable[i]; np != null; np = next)
      {
         next = np->next;
         debug("%s (%d): %X #%d", np->src, np->line, np->addr, np->count);
         realFree(np->memH);
         error = true;
      }
      mHashTable[i] = NULL;
   }
   return error;
}
/***********************************************************************/

#if defined WIN32
extern size_t  _msize(void *);
#endif

#if !defined(USE_MEMCHECKER) && !defined(darwin) && !defined(ANDROID)
#ifndef __clang__
size_t dlmalloc_usable_size(void*); 
#endif
static uint32 getPtrSize(void *p)
{
#if defined(WIN32)
#if defined(ENABLE_WIN32_POINTER_VERIFICATION) || defined(FORCE_LIBC_ALLOC)
      return dbgGetPtrSize(p);
   #else
      return dlmalloc_usable_size(p);
   #endif
#else
   return malloc_usable_size(p);
#endif
}
#endif

///// guich@550: added sanity checks for xmalloc/xfree/xrealloc
#define XPTR_SIZE(ptr) *((uint32*)(((uint8*)ptr)-XMALLOC_MARK_START))

#ifndef darwin
static void *addMemMarks(uint32 size, uint8 *ptr)
{
   // guich@550: detecting begin/end overwrites
   size -= XMALLOC_EXTRASIZE;
   *((uint32*)ptr) = (uint32)size;
   ptr += 4;
   *ptr++ = 0x01; // these two may be
   *ptr++ = 0x01; // used for other purposes
   *ptr++ = 0x01;
   *ptr++ = 0x01;
   // p will go here
   *(ptr+size)   = 0x01;
   *(ptr+size+1) = 0x01;
   return ptr;
}
uint8* verifyMemMarks(void *p, char*msg, uint32* _size, bool replaceMarks, const char *file, int line)
{
   if (_size) *_size = 0;
   if (p)
   {
      uint8 *ptr = (uint8 *)p;
      if (*(ptr-1) != 0x01 || *(ptr-2) != 0x01 || *(ptr-3) != 0x01 || *(ptr-4) != 0x01)
      {
         struct htmElem *e = htmGet((uint32)p);
         debug("%s - not a memory chunk or chunk's start was overwritten: %lx - %d. %s at %s, line %d",msg, (long)p, (int)*(ptr-1), e ? "allocated" : "freed", e ? e->src : file, e ? e->line : line); // this message will be printed inconditionally
         return null;
      }
      else
      {
         uint32 size = XPTR_SIZE(ptr);
         if (_size) *_size = size;
         if (replaceMarks)
            *(ptr-1) = *(ptr-2) = *(ptr-3) = *(ptr-4) = 2; // set them to 2 so we can find a block that was freed twice
         if (*(ptr+size) != 0x01 || *(ptr+size+1) != 0x01)
         {
            struct htmElem *e = htmGet((uint32)p);
            debug("%s - chunk's end was overwritten: %lx (%ld) - %d. %s at %s, line %d",msg,(long)p,(long)size,(int)*(ptr+size), e ? "allocated" : "freed", e ? e->src : file, e ? e->line : line); // this message will be printed inconditionally
            return null;
         }
         else
         if (replaceMarks)
            *(ptr+size) = *(ptr+size+1) = 2;
      }
      return ptr-XMALLOC_MARK_START;
   }
   return p;
}
#endif

static uint8* memError(char* func, int32 origSize, const char* file, int line)
{
#ifdef WIN32
   debug("%s(%d) RETURNING NULL TO %s, line %d. Free memory: %d, avail per process: %d",func, (int)origSize,file,line,(int)getFreeMemory(false),(int)getFreeMemory(true));
#else
   debug("%s(%d) RETURNING NULL TO %s, line %d. Free memory: %d",func, (int)origSize,file,line,(int)getFreeMemory(false));
#endif
   warnOnExit = true;
   return null;
}

bool checkMallocLeaks()
{
   if (leakCheckingEnabled) // no threads are running at this point
   {
#ifdef ENABLE_WIN32_POINTER_VERIFICATION
      if (allocCount != freeCount)
         debug("\nMemory leaked\n Number of xmallocs: %d\n Number of xfrees: %d\nList of places where the leaks were xmalloc'd:",(int)allocCount,(int)freeCount); // guich@570_94
#else
      if (totalAllocated != 0)
         debug("\nTotal memory leaked: %d bytes\n Number of xmallocs: %d\n Number of xfrees: %d\nList of places where the leaks were xmalloc'd:",(int)totalAllocated,(int)allocCount,(int)freeCount); // guich@570_94
#endif
      return htmDispose() || totalAllocated != 0 || allocCount != freeCount;
   }
   return false;
}

TC_API uint8 *privateXmalloc(uint32 size,const char *file, int line)
{
#if defined(FORCE_LIBC_ALLOC) || defined(ENABLE_WIN32_POINTER_VERIFICATION)
	uint8 *ptr;

   if (allocCount2ReturnNull > 0 && --allocCount2ReturnNull == 0) // used on test suites to return null after a given number of allocations
      return null;

   LOCKVAR(alloc);
   ptr = malloc(size);
   
   allocCount++;
	if (ptr != null)
   {
      xmemzero(ptr, size);
      if (leakCheckingEnabled)
         htmPut((uint32)ptr, line, file);
   }
   else
      memError("XMALLOC",size, file, line);
   updateStats();
   UNLOCKVAR(alloc);
   return ptr;
#else
   void *p=null;
   uint32 origSize = size;
   size += XMALLOC_EXTRASIZE;
   //size = ((size >> 2) << 2) + 4; dlmalloc already aligns
   if (allocCount2ReturnNull > 0 && --allocCount2ReturnNull == 0) // used on test suites to return null after a given number of allocations
      return null;
   LOCKVAR(alloc);
#ifdef INITIAL_MEM
   if (size <= maxAvail)
#endif
      p = realMalloc(size);

   if (!p)
   {
      UNLOCKVAR(alloc);
      return memError("XMALLOC",origSize, file, line);
   }
#ifdef INITIAL_MEM
   maxAvail -= size;
#ifdef TRACE_OBJCREATION
   //debug("xmalloc(%d) at %s (%d). avail after: %d",size, file, line, maxAvail);
#endif
#endif
   xmemzero(p, size); // guich@340_21: make sure we erase the allocated memory
//   debug("%7d at %s (%d)",size, file, line);

// fdie: If you want to use a memory checking tool such as Valgrind,
// you have to define USE_MEMCHECKER to limit your memory use to the requested
// memory size or the checking tool may detect many false memory corruptions.
#if !defined(USE_MEMCHECKER) && !defined(darwin) && !defined(ANDROID)
   size = getPtrSize(p);
#endif

   p = addMemMarks(size, p);

   allocCount++;
   totalAllocated += size;
   if (totalAllocated > maxAllocated)
      maxAllocated = totalAllocated;
   if (totalAllocated > profilerMaxMem) // guich@tc111_4
      profilerMaxMem = totalAllocated;
   if (leakCheckingEnabled)
      htmPut((uint32)p, line, file);
#ifdef ENABLE_TRACE
   //debug("alloc(%d) in %s line %d. Free: %d, used: %d (%lX)",(int)size, file, line, (int)getFreeMemory(false),totalAllocated,(long)p);
#endif
   UNLOCKVAR(alloc);
   return p;
#endif
}

TC_API void privateXfree(void *p, const char *file, int line)
{
#if defined(FORCE_LIBC_ALLOC) || defined(ENABLE_WIN32_POINTER_VERIFICATION)
   if (leakCheckingEnabled && !htmRemove((uint32)p))
      debug("free: %lX NOT REMOVED. xfree called from %s (%d)",(long)p, file, line);
   LOCKVAR(alloc);
   freeCount++;
   free(p);
   updateStats();
#else
   uint32 size=0;
   LOCKVAR(alloc);

   if (p)
   {
      void* pp = p;
      p = verifyMemMarks(p,"xfree", &size, true, file, line);
      if (p)
      {
         size += XMALLOC_EXTRASIZE;
#ifdef INITIAL_MEM
         maxAvail += size;
#ifdef TRACE_OBJCREATION
         //debug("xfree %d at %s (%d). avail after: %d",size + XMALLOC_EXTRASIZE, file, line, maxAvail);
#endif
#endif
         if (leakCheckingEnabled)
         {
            if (!htmRemove((uint32)pp))
               debug("free(%d): %lX NOT REMOVED. xfree called from %s (%d)",(int)size, (long)pp, file, line);
            else
            {
               totalAllocated -= size; // guich@585_3: only shrink memory if remove is successfull
               freeCount++;
            }
         }
         else totalAllocated -= size; // guich@tc100b5: remember to decrease it
         realFree(p);
#ifdef ENABLE_TRACE
         //debug("free: %d, used: %d (%lX)",(int)getFreeMemory(false), totalAllocated, (long)pp);
#endif
      }
   }
#endif
   UNLOCKVAR(alloc);
}

TC_API uint8 *privateXrealloc(uint8* ptr, uint32 size,const char *file, int line)
{                           
   uint32 origSize = size;
#ifndef darwin
   uint32 oldSize=0;
#endif
   uint8* p=null;
   if (ptr == null) // first allocation?
      return privateXmalloc(size, file, line);
   LOCKVAR(alloc);
   if (leakCheckingEnabled)
      htmRemove((uint32)ptr);
#if defined(FORCE_LIBC_ALLOC)
   if (size == 0) // xrealloc(0) fails on android
   {
      free(ptr);
      p = malloc(0);
   }
   else p = realloc(ptr, size);
   if (p && leakCheckingEnabled)
      htmPut((uint32)p, line, file);
   if (!p)
      memError("XREALLOC",origSize, file, line);
   updateStats();
   UNLOCKVAR(alloc);
   return p;
#elif defined(ENABLE_WIN32_POINTER_VERIFICATION)
   UNUSED(origSize);
   p = malloc(size);
   if (p != null)
   {
      oldSize = getPtrSize(ptr);
      xmemzero(p,size);
      xmemmove(p,ptr,min32(size,oldSize));
      free(ptr);
      if (leakCheckingEnabled)
         htmPut((uint32)p, line, file);
   }
   UNLOCKVAR(alloc);
   return p;
#else
   size += XMALLOC_EXTRASIZE;
   //size = ((size >> 2) << 2) + 4; dlmalloc already aligns
#ifdef INITIAL_MEM
   if (size <= maxAvail)
#endif                  
   {
      p = verifyMemMarks(ptr, "xrealloc", &oldSize, false, file, line);
      if (p)
      {   
         void* pp = dlrealloc(p, size);
         if (!pp)
         {
            pp = dlmalloc(size);
            if (pp)
            {                                              
               xmemmove(pp, p, size);
               xfree(ptr);
            }
         }
         p = pp;
      }
   }
   if (!p)
   {
      UNLOCKVAR(alloc);
      return memError("XREALLOC",origSize, file, line);
   }
#ifdef INITIAL_MEM
   maxAvail -= (int32)size - (int32)oldSize;
#endif

// fdie: If you want to use a memory checking tool such as Valgrind,
// you have to define USE_MEMCHECKER to limit your memory use to the requested
// memory size or the checking tool may detect many false memory corruptions.
#if !defined(USE_MEMCHECKER) && !defined(darwin) && !defined(ANDROID)
   size = getPtrSize(p);
#endif
   if (origSize > oldSize) // if size increased, erase the new memory area
      xmemzero(p+oldSize + XMALLOC_EXTRASIZE, origSize - oldSize); 

   p = addMemMarks(size, p);

   totalAllocated -= oldSize + XMALLOC_EXTRASIZE;
   totalAllocated += size;
   if (totalAllocated > maxAllocated)
      maxAllocated = totalAllocated;
   if (totalAllocated > profilerMaxMem) // guich@tc111_4
      profilerMaxMem = totalAllocated;
   if (leakCheckingEnabled)
      htmPut((uint32)p, line, file);
#ifdef ENABLE_TRACE
   //debug("realloc(%d->%d) in %s line %d. Free: %d, used: %d (%lX)",(int)oldSize, (int)size, file, line, (int)getFreeMemory(false),totalAllocated,(long)p);
#endif
   UNLOCKVAR(alloc);
   return p;
#endif
}

TC_API uint8 *privateXcalloc(uint32 NumOfElements,uint32 SizeOfElements,const char *file, int line)
{
#if defined(FORCE_LIBC_ALLOC) || defined(ENABLE_WIN32_POINTER_VERIFICATION)
   uint8 *ptr;
   uint32 size = NumOfElements * SizeOfElements;

   if (allocCount2ReturnNull > 0 && --allocCount2ReturnNull == 0) // used on test suites to return null after a given number of allocations
      return null;

   LOCKVAR(alloc);
   ptr = calloc(NumOfElements, SizeOfElements);
   
   allocCount++;
	if (ptr != null)
   {
      xmemzero(ptr, size);
      if (leakCheckingEnabled)
         htmPut((uint32)ptr, line, file);
   }
   else
      memError("XMALLOC",size, file, line);
   updateStats();
   UNLOCKVAR(alloc);
   return ptr;
#else
   void *p=null;
   uint32 origSize = NumOfElements * SizeOfElements;
   uint32 size = origSize + XMALLOC_EXTRASIZE;
   //size = ((size >> 2) << 2) + 4; dlmalloc already aligns
   if (allocCount2ReturnNull > 0 && --allocCount2ReturnNull == 0) // used on test suites to return null after a given number of allocations
      return null;
   LOCKVAR(alloc);
#ifdef INITIAL_MEM
   if (size <= maxAvail)
#endif
      p = realMalloc(size);

   if (!p)
   {
      UNLOCKVAR(alloc);
      return memError("XMALLOC",origSize, file, line);
   }
#ifdef INITIAL_MEM
   maxAvail -= size;
#ifdef TRACE_OBJCREATION
   //debug("xmalloc(%d) at %s (%d). avail after: %d",size, file, line, maxAvail);
#endif
#endif
   xmemzero(p, size); // guich@340_21: make sure we erase the allocated memory
//   debug("%7d at %s (%d)",size, file, line);

// fdie: If you want to use a memory checking tool such as Valgrind,
// you have to define USE_MEMCHECKER to limit your memory use to the requested
// memory size or the checking tool may detect many false memory corruptions.
#if !defined(USE_MEMCHECKER) && !defined(darwin) && !defined(ANDROID)
   size = getPtrSize(p);
#endif

   p = addMemMarks(size, p);

   allocCount++;
   totalAllocated += size;
   if (totalAllocated > maxAllocated)
      maxAllocated = totalAllocated;
   if (totalAllocated > profilerMaxMem) // guich@tc111_4
      profilerMaxMem = totalAllocated;
   if (leakCheckingEnabled)
      htmPut((uint32)p, line, file);
#ifdef ENABLE_TRACE
   //debug("alloc(%d) in %s line %d. Free: %d, used: %d (%lX)",(int)size, file, line, (int)getFreeMemory(false),totalAllocated,(long)p);
#endif
   UNLOCKVAR(alloc);
   return p;
#endif
}
