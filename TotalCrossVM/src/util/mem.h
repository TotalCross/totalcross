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

#ifndef MEMORY_H
#define MEMORY_H

#ifdef __cplusplus
extern "C" {
#endif

#include <setjmp.h>

#include "../tcvm/tcapi.h"

////////////////////////////////////////////////////////////////////////////////
// Atomic memory allocation
// The pointer is handled as a unique pointer, which must be freed separatedly.
// Samples. Suppose that Symbol is declared as: "typedef TSymbol* Symbol;"
//
// To create a new symbol, use:
// Symbol s = newX(Symbol);
// Note that, since Symbol is a pointer, you must use s-> (and not s.)
//
// To free the symbol, use:
// xfree(s);

#if !defined(FORCE_LIBC_ALLOC) && !defined(ENABLE_WIN32_POINTER_VERIFICATION)
#define malloc dlmalloc
#define free dlfree
#define realloc dlrealloc
#define calloc dlcalloc
#endif

#define xmalloc(size) TCAPI_FUNC(privateXmalloc)(size,__FILE__,__LINE__)
#define xfree(p) do {if (p) TCAPI_FUNC(privateXfree)(p,__FILE__,__LINE__); p = null;} while (0)
#define xrealloc(ptr, size) TCAPI_FUNC(privateXrealloc)(ptr, size,__FILE__,__LINE__)
#define xcalloc(NumOfElements, SizeOfElements) TCAPI_FUNC(privateXcalloc)(NumOfElements,SizeOfElements,__FILE__,__LINE__)

TC_API uint8* privateXmalloc(uint32 size, const char *file, int line); // allocate and zero the memory region
typedef uint8* (*privateXmallocFunc)(uint32 size, const char *file, int line);
TC_API void privateXfree(void *ptr, const char *file, int line); // never use privatexfree, use xfree instead
typedef void (*privateXfreeFunc)(void *ptr, const char *file, int line);
TC_API uint8* privateXrealloc(uint8* ptr, uint32 size, const char *file, int line); // allocate and zero the memory region
typedef uint8* (*privateXreallocFunc)(uint8* ptr, uint32 size, const char *file, int line);
TC_API uint8* privateXcalloc(uint32 NumOfElements, uint32 SizeOfElements, const char *file, int line); // allocate and zero the memory region
typedef uint8* (*privateXcallocFunc)(uint32 NumOfElements, uint32 SizeOfElements, const char *file, int line);
#define newX(x) (x)xmalloc(sizeof(T##x))
#define newXH(x,p) (x)heapAlloc(p, sizeof(T##x))

TC_API void setCountToReturnNull(int32 n); // defines a number that, when reached, will cause xmalloc to return null.
typedef void (*setCountToReturnNullFunc)(int32 n);
TC_API int32 getCountToReturnNull(); // returns the current count. check this after you run the test; a value greater than 0 means that you reached the maximum number of xmalloc called by your routine and the memory test should end.
typedef int32 (*getCountToReturnNullFunc)();

uint8* verifyMemMarks(void *p, char*msg, uint32* _size, bool replaceMarks, const char *file, int line); // only if not defined "darwin"

////////////////////////////////////////////////////////////////////////////////
// Heap memory allocation
// Creates a block of memory from where other small pointers
// will be "allocated". This pointer cannot be deallocated, only the
// whole heap.
//

#ifdef ENABLE_WIN32_POINTER_VERIFICATION // if debugging in windows, make sure that each pointer allocated is placed on its own page
#define MEMBLOCK_SIZE 2
#else
#define MEMBLOCK_SIZE 1024
#endif

typedef struct THeap THeap;
typedef THeap* Heap;

typedef struct TMemBlock TMemBlock;
typedef TMemBlock* MemBlock;

typedef void (*HeapFinalizerFunc)(Heap heap, void* bag);

struct TMemBlock
{
   uint32 availSize;
   uint8* block;
   uint8* current; // current pointer inside the block
   struct TMemBlock *next;
};

typedef char CExceptionFileCharBuf[64];

/**
 Structure used to save all informations about a C Exception (simulated with setjmp/longjmp.
 You can save the Heap's exception and overwrite it with a new one, and then restore the original.
 E.G.:
 CException old;
 old = heap->ex;
 IF_HEAP_ERROR(heap)
 {
    heap->ex = old;
    ...

 Note that each CException takes about 400 bytes of memory.
*/
typedef struct
{
   CExceptionFileCharBuf creationFile, errorFile, setjmpFile; // can't be a char pointer because an external library may release the pointer before we show the leak error
   uint32 creationLine, errorLine, setjmpLine;
   int32 errorCode;
   jmp_buf errorJump;
} CException;

struct THeap
{
   MemBlock current;
   CException ex;
   HeapFinalizerFunc finalizerFunc;
   void* finalizerBag;
   uint32 totalAvail,totalAlloc,numAlloc,blocksAlloc,count; // memory statistics
   bool greedyAlloc; // set to true allocate less memory for each block. will increase the number of blocks. In UIGadgets for example, greedy = unused 11982 (495 blocks), not greedy = unused 576428 (72 blocks)
};

/// create a memory heap, with an optional finalizer
TC_API Heap privateHeapCreate(bool add2list, const char *file, int32 line);
typedef Heap(*privateHeapCreateFunc)(bool add2list, const char *file, int32 line);
/// destroy the heap and all pointers inside it
TC_API void heapDestroyPrivate(Heap m, bool added2list);
typedef void(*heapDestroyPrivateFunc)(Heap m, bool added2list);
/// alloc a pointer inside the heap
TC_API void* heapAlloc(Heap m, uint32 size);
typedef void* (*heapAllocFunc)(Heap m, uint32 size);
/// dispatch an error to the heap, releasing the allocated memory and doing a long jump to the error handler
TC_API void privateHeapError(Heap m, int32 errorCode, const char *file, int32 line);
typedef void (*privateHeapErrorFunc)(Heap m, int32 errorCode, const char *file, int32 line);
/// the bag is passed as parameter
void heapSetFinalizer(Heap m, HeapFinalizerFunc fin, void* bag);
/// "frees" a pointer: returns the block to the OS, if and only if the pointer is above the MEMBLOCK_SIZE. In all other cases, just ignore
void heapFree(Heap m, void* ptr);
/// function to be passed to heapFreeAsking
typedef bool (*AskIfFreeFunc)(uint8* block, uint32 size);
/// walks on all blocks of a heap and ask if its to delete each block
void heapFreeAsking(Heap m, AskIfFreeFunc ask);
/// does a setjmp, storing the file and line informations
TC_API int32 privateHeapSetJump(Heap m, const char *file, int32 line);
typedef int32 (*privateHeapSetJumpFunc)(Heap m, const char *file, int32 line);

/// important: m must be set to null before calling the destroy, otherwise strange errors will occur in windows.
#define heapDestroy(m) do {Heap mm = m; m = null; TCAPI_FUNC(heapDestroyPrivate)(mm,true);} while (0)
#define heapDestroyB(m,b) do {Heap mm = m; m = null; TCAPI_FUNC(heapDestroyPrivate)(mm,b);} while (0)

#define heapCreate() TCAPI_FUNC(privateHeapCreate)(true,__FILE__,__LINE__)
#define heapCreateB(b) TCAPI_FUNC(privateHeapCreate)(b,__FILE__,__LINE__)
#define HEAP_ERROR(heap, errorCode) TCAPI_FUNC(privateHeapError)(heap, errorCode, __FILE__,__LINE__)
#define IF_HEAP_ERROR(heap) if (!heap || TCAPI_FUNC(privateHeapSetJump)(heap, __FILE__,__LINE__) || setjmp(heap->ex.errorJump) != 0)

#define HEAP_MEMORY_ERROR 1
#define HEAP_ZIP_ERROR 2
#define HEAP_NO_ERROR 0
#define HEAP_OTHER_ERROR 3

bool initMem();
void destroyMem();

#ifdef __cplusplus
}
#endif

#endif
