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

#ifndef DATASTRUCTURES_H
#define DATASTRUCTURES_H

#include "tcclass.h"
#include "tcapi.h"
#include "xtypes.h"

// Here we concentrate all data structures used, like hashtable, array, list, etc.
// Must turn on "32bit ints" for the target platform

/////////////////////////////////////////////////////////////////////////
// Hashtable

#ifdef __cplusplus
extern "C" {
#endif

typedef size_t HTKey; // 64-bit hardware
   
typedef struct HTEntryType
{
   HTKey key;
   union
   {
      int32 i32;
      VoidP ptr;
   };
   struct HTEntryType *next;
} HtEntry;

typedef struct
{
   HtEntry **items;
   int32 size;
   int32 hash;
   int32 threshold;
   Heap heap;
} Hashtable;

typedef void (*VisitElementFunc)(int32 i32, VoidP ptr); // i32 is an union with ptr, so be sure to access what you really stored!
typedef void (*VisitElementKeyFunc)(HTKey key, int32 i32, VoidP ptr); // i32 is an union with ptr, so be sure to access what you really stored!
typedef bool (*VisitElementContextFunc)(Context context, VoidP ptr); // i32 is an union with ptr, so be sure to access what you really stored!

TC_API  Hashtable   htNew     (int32 count, Heap heap); // heap is optional; if passed, the rehash function will not be called if the hashtable gets too big (in this case, remember to initialize the hashtable with a value big enough to hold all items)
typedef Hashtable (*htNewFunc)(int32 count, Heap heap);
TC_API  int32   htGet32           (Hashtable *iht, HTKey key);  // returns 0 if the key was not found
typedef int32 (*htGet32Func)      (Hashtable *iht, HTKey key);
TC_API  int32   htGet32Inv        (Hashtable *iht, HTKey key);  // returns -1 if the key was not found
typedef int32 (*htGet32InvFunc)   (Hashtable *iht, HTKey key);
TC_API  VoidP   htGetPtr          (Hashtable *iht, HTKey key);
typedef VoidP (*htGetPtrFunc)     (Hashtable *iht, HTKey key);
TC_API  bool    htPut32           (Hashtable *iht, HTKey key, int32 value);  // replaces the key if it exists
typedef bool  (*htPut32Func)      (Hashtable *iht, HTKey key, int32 value);
TC_API  bool    htPut32IfNew      (Hashtable *iht, HTKey key, int32 value);  // keeps the old key if it exists
typedef bool  (*htPut32IfNewFunc) (Hashtable *iht, HTKey key, int32 value);
TC_API  bool    htPutPtr          (Hashtable *iht, HTKey key, VoidP value);  // replaces the key if it exists
typedef bool  (*htPutPtrFunc)     (Hashtable *iht, HTKey key, VoidP value);
TC_API  bool    htPutPtrIfNew     (Hashtable *iht, HTKey key, VoidP value);  // keeps the old key if it exists
typedef bool  (*htPutPtrIfNewFunc)(Hashtable *iht, HTKey key, VoidP value);
TC_API  void    htRemove          (Hashtable *iht, HTKey key);
typedef void  (*htRemoveFunc)     (Hashtable *iht, HTKey key);               // removes the key from the table. If there's a heap, the pointer is NOT freed
TC_API  void    htFree     (Hashtable *iht, VisitElementFunc freeElement); // if there's a heap, the structures are not freed; you must free them by destroying the heap yourself
typedef void  (*htFreeFunc)(Hashtable *iht, VisitElementFunc freeElement);
TC_API  bool    htFreeContext     (Context context, Hashtable *iht, VisitElementContextFunc freeElement); // if there's a heap, the structures are not freed; you must free them by destroying the heap yourself
typedef bool  (*htFreeContextFunc)(Context context, Hashtable *iht, VisitElementContextFunc freeElement);
TC_API  bool    htInc             (Hashtable *iht, HTKey key, int32 incValue);  // holds a count of something
typedef bool  (*htIncFunc)        (Hashtable *iht, HTKey key, int32 incValue);
void htTraverse(Hashtable *iht, VisitElementFunc visitElement);
void htTraverseWithKey(Hashtable *iht, VisitElementKeyFunc visitElement);
///////////////////////////////////////////////////////////////////////////
// Linked list
// Defines a template for a circular list that will be used for many types.
// The items are added in order.
//
// To declare a list of String, use in the header file:
// DeclareList(String)
//
// The String list will be named Strings (an s is added to the end of the
// type)
//
// At some c file, you must add the implementation:
// ImplementList(String)
//
// To add an item, use:
// Strings ss;
// ss = StringsAdd(ss, newString);
//
// To destroy the list, use:
// StringsFree(ss);
//
// To iterate in a list, use:
// Strings* head = list;
// do
// {
//    String s = list->value;
//    list = list->next;
// } while (head != list);
//
// To remove a node from the list:
// ss = StringsRemove(ss, "something");
//
// putting next/prev first lets you call listGetCount/list2array for any type of list.
// You can optionally pass a created Heap to the list in order to use it as the memory
// allocator. Note that the Remove method does not work with lists stored in heaps.

// List declaration
#define DeclareList(type)                              \
typedef struct type##s                                 \
{                                                      \
   struct type##s *next;                               \
   struct type##s *prev;                               \
   type value;                                         \
} type##s;                                             \
type##s* type##sAdd(type##s *l, type value, Heap h);   \
type##s* type##sRemove(type##s *l, type value, Heap h);\
bool type##sContains(type##s *l, type value);          \
void type##sDestroy(type##s *l, Heap h);

// List implementation
#define ImplementList(type)                         \
type##s* type##sAdd(type##s *l, type value, Heap h) \
{                                                   \
   if (!l)                                          \
   {                                                \
      l = h ? (type##s*)heapAlloc(h,sizeof(type##s))  \
            : (type##s*)xmalloc(sizeof(type##s));   \
      if (l)                                        \
      {                                             \
         l->value = value;                          \
         l->next = l->prev = l;                     \
      }                                             \
   }                                                \
   else                                             \
   {                                                \
      type##s *e;                                   \
      e = h ? (type##s*)heapAlloc(h,sizeof(type##s))  \
            : (type##s*)xmalloc(sizeof(type##s));   \
      if (e)                                        \
      {                                             \
         e->value = value;                          \
         e->prev = l->prev;                         \
         e->next = l;                               \
         l->prev->next = e;                         \
         l->prev = e;                               \
      }                                             \
      else return null;                             \
   }                                                \
   return l;                                        \
}                                                   \
                                                    \
void type##sDestroy(type##s *l, Heap h)             \
{                                                   \
   if (h)                                           \
      heapDestroy(h);                               \
   else                                             \
   {                                                \
      type##s *le = l;                              \
      if (le != null)                               \
         do                                         \
         {                                          \
            type##s *next = le->next;               \
            le->next = le->prev = null;             \
            xfree(le);                              \
            le = next;                              \
         } while (le != l);                         \
   }                                                \
}                                                   \
                                                    \
type##s* type##sRemove(type##s *l, type value, Heap h)\
{                                                   \
   type##s* head = l;                               \
   if (head && h == null)                           \
      do                                            \
      {                                             \
         if (l->value == value)                     \
         {                                          \
            if (l->prev) l->prev->next = l->next;   \
            if (l->next) l->next->prev = l->prev;   \
            if (l == head)                          \
               head = l->prev != head ? l->prev : null; \
            l->next = l->prev = null;               \
            xfree(l);                               \
            break;                                  \
         }                                          \
         l = l->next;                               \
      } while (head != l);                          \
   return head;                                     \
}                                                   \
bool type##sContains(type##s *l, type value)        \
{                                                   \
   type##s* head = l;                               \
   if (head)                                        \
      do                                            \
      {                                             \
         if (l == null)                             \
            return false;                           \
      	if (l->value == value)                     \
      		return true;                            \
         l = l->next;                               \
      } while (head != l);                          \
   return false;                                    \
}

// Returns the number of elements added to this list.
int32 listGetCount(VoidP list);
// Converts a list into an array.
VoidP list2array(VoidP list, int32 sizeofElem, int32 extra); // extra adds empty elements at the array's start

DeclareList(TCHARP); // TCHARPs
DeclareList(CharP); // CharPs
DeclareList(VoidP); // VoidPs

/////////////////////////////////////////////////////////////////////////
// Array
// Samples. Suppose that Symbol is declared as: "typedef TSymbol* Symbol;"
// and "typedef Symbol* SymbolArray"
//
// To create a new symbol, use:
// Symbol s = newX(Symbol);
// Note that, since Symbol is a pointer, you must use s-> (and not s.)
//
// To create an array of Symbol, use:
// SymbolArray symArray = newArrayOf(Symbol, numberOfElements);
//
// To create an array of pointers to Symbol, use:
// SymbolArray* symArrayPtr = newPtrArrayOf(Symbol, numberOfElements);
//
// To access each element, use:
// Symbol s1 = symArray[i];
//
// To get the length, use:
// uint32 len = ARRAYLEN(symArray); // = numberOfElements
//
// To free it:
// freeArray(symArray);

#define newArray(sizeofElem,len,mp) privateNewArray(sizeofElem,len,mp,__FILE__,__LINE__)
void* privateNewArray(int32 sizeofElem, int32 len, Heap mp, const char *file, int line);

#define freeArray(p) do {if (p) {uint8* b = ((uint8*)p)-TSIZE; xfree(b); p = null;}} while (0)
#define heapFreeArray(heap,p) do {if (p) {uint8* b = ((uint8*)p)-TSIZE; heapFree(heap, b); p = null;}} while (0)

#define newArrayOf(type, len, heap) (type)newArray(sizeof(T##type), len, heap)
#define newPtrArrayOf(type, len, heap) (type##Array)newArray(sizeof(type), len, heap) // used for primitive types, TCObject and (J)CharP arrays

#define SET_ARRAYLEN(x) (((size_t*)x)[-1])
#define ARRAYLEN(x) ((int32)(((size_t*)x)[-1]))
#define ARRAYLENV(x) ((x)?ARRAYLEN(x):0) // checks if x is null, returning 0 if yes

///////////////////////////////////////////////////////////////////////////
// Stack
// The stack is implemented as a linked list of blocks to hold the pushed
// values.
// The stackPush uses a heap to store the blocks, so you must call it
// using IF_HEAP_ERROR
// When popping, false is returned when the stack end is reached.

typedef struct
{
   Heap heap;
   VoidPs *current, *head;
   UInt8Array pos, blockEnd, blockStart;
   int32 initialBlockSize, elemSize;
} *Stack, TStack;

TC_API Stack newStack(int32 elemCount, int32 elemSize, Heap defHeap);
typedef Stack (*newStackFunc)(int32 elemCount, int32 elemSize, Heap defHeap);
TC_API bool stackPop(Stack s, VoidP out);
typedef bool (*stackPopFunc)(Stack s, VoidP out);
TC_API void stackPush(Stack s, VoidP in);
typedef void (*stackPushFunc)(Stack s, VoidP in);

#define stackPeek(s) *s->pos
#define stackDestroy(s) heapDestroy(s->heap)

#ifdef __cplusplus
}
#endif

#endif // DATASTRUCTURES_H
