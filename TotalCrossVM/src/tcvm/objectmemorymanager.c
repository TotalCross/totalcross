/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#include "tcvm.h"

/**************************************************************************************

Handling allocation and disposal of objects

   Definitions
   ~~~~~~~~~~~

There's an array of available blocks of data, ordered by size. Each block has two
lists: free objects and used objects.

+-------+
|   8   |.freeList
| bytes |.usedList
+-------+
|  12   |.freeList
| bytes |.usedList
+-------+
|  16   |.freeList
| bytes |.usedList
+-------+
    .
    .
    .

   Memory allocation
   ~~~~~~~~~~~~~~~~~

When an Object is allocated, it is removed from the free list and put in the used list
at the given array index (based on object's size).

* Initial state:

+-------+
|  16   |.freeList --> obj1 --> obj2 --> obj3 --> 0
| bytes |.usedList --> 0
+-------+

* Allocation of an object with 16 bytes:

+-------+
|  16   |.freeList --> obj2 --> obj3 --> 0
| bytes |.usedList --> obj1 --> 0
+-------+

* Allocation of another object with 16 bytes:

+-------+
|  16   |.freeList --> obj3 --> 0
| bytes |.usedList --> obj1 --> obj2 --> 0
+-------+


   A single-pass Garbage Collector
   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

First, the list of used objects is appended to the list of free objects, emptying the
former.

+-------+
|  16   |.freeList --> obj3 --> obj1 --> obj2 --> 0
| bytes |.usedList --> 0
+-------+

Secondly, all the reachable objects (all objects in the object registers and all the
static class members) are removed from the free list and placed back in the used one.

(Suppose obj1 is the only reachable)

+-------+
|  16   |.freeList --> obj3 --> obj2 --> 0
| bytes |.usedList --> obj1 --> 0
+-------+

Once this pass finishes, only unreachable (and, so, freed) objects remain in the free
list.

Note: although not required, we implemented a chunk compactor, which joins adjacent
objects, thus reducing memory fragmentation. This introduced a "clean free objects"
stage into the GC, slowing down the GC in 25%.


  Marked Objects
  ~~~~~~~~~~~~~~

During the GC, we need a way to mark reached objects to avoid recursion. For simplicity,
This space is grabbed from the lower bit in the class member of TObjectProperties. Since
this is a memory dynamically allocated, we ensure that it starts in an address that never
uses the lower bit (x&1 == 0), so we can safely use them to our purposes. The value that
means "marked" is inverted after each gc to prevent unmarking all the objects after they
are freed.


  Implementation
  ~~~~~~~~~~~~~~

To easy the implementation, the array of available blocks of data is splitted into two
arrays, one containing the used objects, and another containing the free objects, per
size.

The objects are stored as a doubly-linked list. A new element is always inserted right
after the head. The last element in the list points to NULL. The previous element
of the head points to the last element in the list. E.G. ( = pointer to prev, -
pointer to next):

    += H ----> obj3 ----> obj2 ----> 0
    |  ^====<===+ ^===<===+ |
    +==>===========>========+

****************************************************************************************/

// debugging conditionals

#ifdef TRACE_LOCKED_BYTEARRAYS
#define _TRACE_LOCKED_BYTEARRAYS 1
#else
#define _TRACE_LOCKED_BYTEARRAYS 0
#endif

#ifdef TRACE_OBJCREATION
#define _TRACE_OBJCREATION 1
#else
#define _TRACE_OBJCREATION 0
#endif

#ifdef TRACE_OBJDESTRUCTION
#define _TRACE_OBJDESTRUCTION 1
#else
#define _TRACE_OBJDESTRUCTION 0
#endif

#ifdef DEBUG_OMM_LIST
#define _DEBUG_OMM_LIST 1
#else
#define _DEBUG_OMM_LIST 0
#endif

#if defined(ENABLE_TRACE) || defined (DEBUG_OMM_LIST)
#define COMPUTETIME 1
#else
#define COMPUTETIME 0
#endif
//

void soundTone(int32 frequency, int32 duration);

#define MIN_SPACE_LEFT 16
#define DEFAULT_CHUNK_SIZE (65536-64)
#define OBJARRAY_MAX_INDEX 128 // 4,8,12,16....4*OBJARRAY_MAX_INDEX

static int32 size2idx(int32 size) // size must exclude sizeof(TObjectProperties) !
{
   int32 index;
   size = ((size+3)>>2)<<2;
   index = size>>2;
   return (index >= OBJARRAY_MAX_INDEX) ? OBJARRAY_MAX_INDEX : index;
}

typedef struct
{
   TCObjectArray start;
   int32 n;
} *ObjectsToVisit, TObjectsToVisit;

#define OBJ_SIZE(o) OBJ_PROPERTIES(o)->size

#define CHUNK2OBJECT(c) (TCObject)(((uint8*)c)+sizeof(TObjectProperties))
#define OBJECT2CHUNK(o) (Chunk)(((uint8*)o)-sizeof(TObjectProperties))

#define OBJ_MARK(o)         OBJ_PROPERTIES(o)->mark
#define OBJ_ISLOCKED(o)     (OBJ_PROPERTIES(o)->lock  == 1)
#define OBJ_SETLOCKED(o)    OBJ_PROPERTIES(o)->lock = 1
#define OBJ_SETUNLOCKED(o)  OBJ_PROPERTIES(o)->lock = 0

#ifdef DEBUG_OMM_LIST
static int32 countObjectsInList(TCObject o, bool dump, int32 mark, int32 *size);
#endif

#if defined(ENABLE_TEST_SUITE)
#define CANTRAVERSE canTraverse
#else
#define CANTRAVERSE true
#endif

#ifdef DEBUG_OMM_LIST
static void dumpList(TCObject o, bool showSize)
{
   TCObject o0 = o;
   o = OBJ_PROPERTIES(o)->next;
   debug("G %X<-*%X->%X",OBJ_PROPERTIES(o0)->prev,o0,o);
   if (o)
      for (; o != null; o = OBJ_PROPERTIES(o)->next)
      {
         debug("G %X<-%X->%X",OBJ_PROPERTIES(o)->prev,o,OBJ_PROPERTIES(o)->next);
         if (showSize) debug("G size: %d",OBJ_SIZE(o));
      }
   debug("G");
}
#endif

static void removeNodeFromDblList(TCObject headObj, TCObject nodeObj)
{
   ObjectProperties node = OBJ_PROPERTIES(nodeObj);
   ObjectProperties head = OBJ_PROPERTIES(headObj);
   ObjectProperties nodePrev = OBJ_PROPERTIES(node->prev);
   ObjectProperties nodeNext = OBJ_PROPERTIES(node->next);

#ifdef DEBUG_OMM_LIST
   int32 nIni,nEnd;
   nIni = countObjectsInList(headObj,false,-1,null);
#endif

   nodePrev->next = node->next;
   if (node->next) // not the last node?
      nodeNext->prev = node->prev; // make next one point to our previous node
   else
      head->prev = node->prev == headObj ? null : node->prev; // if this is the last node being removed, we must set to null; otherwise, store the pre-last as last
   node->prev = node->next = null;

#ifdef DEBUG_OMM_LIST
   nEnd = countObjectsInList(headObj,false,-1,null);
   if (nEnd > 0 && nEnd != (nIni-1))
      debug("M @@@@@@@ Error while removing node %X! Size should be %d, but is %d (idx = %d)", nodeObj, nIni-1, nEnd, size2idx(OBJ_SIZE(nodeObj)));
#endif
}

static void insertNodeInDblList(TCObject headObj, TCObject nodeObj) // insert at the head
{
   ObjectProperties node = OBJ_PROPERTIES(nodeObj);
   ObjectProperties head = OBJ_PROPERTIES(headObj);

#ifdef DEBUG_OMM_LIST
   int32 nIni,nEnd;
   nIni = countObjectsInList(headObj,false,-1,null);
#endif

   node->next = head->next;
   head->next = nodeObj;
   node->prev = headObj;
   if (!head->prev)
      head->prev = nodeObj; // make the prev in head point to the last: since all nodes are inserted at the start, the last item is the first inserted
   if (node->next) // not the first element? tell the element where the head was pointing to, that they now have to point to us
   {
      ObjectProperties propsOld = OBJ_PROPERTIES(node->next);
      propsOld->prev = nodeObj;
   }

#ifdef DEBUG_OMM_LIST
   nEnd = countObjectsInList(headObj, false,-1,null);
   if (nEnd != (nIni+1))
      debug("M @@@@@@@ Error while inserting node %X! Size should be %d, but is %d", nodeObj, nIni+1, nEnd);
#endif
}

static void moveDblList(TCObject srcList, TCObject dstList) // append the src list to the dst list
{
   ObjectProperties src = OBJ_PROPERTIES(srcList);
   ObjectProperties dst = OBJ_PROPERTIES(dstList);
   TCObject frstSrcObj = src->next;                       // first object of src list
   TCObject lastSrcObj = src->prev;                       // last object of src list
   TCObject lastDstObj = dst->prev ? dst->prev : dstList;  // last object of dst list
   ObjectProperties frstSrc = OBJ_PROPERTIES(frstSrcObj);
   ObjectProperties lastDst = OBJ_PROPERTIES(lastDstObj);

#ifdef DEBUG_OMM_LIST
   int32 b4src,b4dst,afsrc,afdst;
   b4src = countObjectsInList(srcList,false,-1,null);
   b4dst = countObjectsInList(dstList,false,-1,null);
#endif

   frstSrc->prev = lastDstObj;
   lastDst->next = frstSrcObj;
   dst->prev     = lastSrcObj;
   src->next = src->prev = null;

#ifdef DEBUG_OMM_LIST
   afsrc = countObjectsInList(srcList,false,-1,null);
   afdst = countObjectsInList(dstList,false,-1,null);
   if ((afsrc + afdst) != (b4src + b4dst))
   {
      debug("M @@@@@@@ moveDblList failed! %d != %d",(afsrc+afdst) , (b4src+b4dst));
      countObjectsInList(dstList,true,-1,null);
   }
#endif
}

static bool createChunk(uint32 size)
{
   Chunk chunk;
   TCObject o;
   IF_HEAP_ERROR(chunksHeap)
      return false;

   chunk = heapAlloc(chunksHeap, size+sizeof(TObjectProperties));
   o = CHUNK2OBJECT(chunk);
   xmemzero(OBJ_PROPERTIES(o),sizeof(TObjectProperties));
   OBJ_SIZE(o) = size;
   insertNodeInDblList(freeList[OBJARRAY_MAX_INDEX], o);
   if (tcSettings.chunksCreated) 
      (*tcSettings.chunksCreated)++; // caution: ++ has precedence over *

#if defined(TRACE_OBJCREATION) || defined(DEBUG_OMM_LIST) || defined(ENABLE_TRACE)
   debug("M +++ %2d Created chunk %X size %d, first obj: %X",tcSettings.chunksCreated ? *tcSettings.chunksCreated : 1,chunk,size, o);
#endif
   return true;
}

bool initObjectMemoryManager()
{
   int32 i,skip = sizeof(TObjectProperties), size = skip+4, n = OBJARRAY_MAX_INDEX+1;
   uint8 *f, *u, *l;
   ommHeap = heapCreate();
   chunksHeap = heapCreate();
   if (chunksHeap == null) return false;
   IF_HEAP_ERROR(ommHeap)
   {
      heapDestroy(ommHeap);
      return false;
   }

   f = heapAlloc(ommHeap, size * n); // here we alloc the whole block of objects
   u = heapAlloc(ommHeap, size * n);
   l = heapAlloc(ommHeap, size * 1); // the locked list is a single array
   f += skip;
   u += skip;
   l += skip;
   freeList = newPtrArrayOf(TCObject,n,ommHeap);
   usedList = newPtrArrayOf(TCObject,n,ommHeap);
   lockList = newPtrArrayOf(TCObject,1,ommHeap);
   lockList[0] = (TCObject)l;
   for (i =0; i < n; i++) // and now we just assign the starting pointer of each block
   {
      freeList[i] = (TCObject)f; f += size;
      usedList[i] = (TCObject)u; u += size;
   }
   markedAsUsed = 1;
   objStack = newStack(2048, sizeof(TObjectsToVisit), null); // must be > 1k!
   return objStack != null && createChunk(DEFAULT_CHUNK_SIZE); // create the first chunk
}

void destroyObjectMemoryManager()
{
   if (IS_VMTWEAK_ON(VMTWEAK_DUMP_MEMORY_STATS))
      debug("M Times gc was called: %d. Total gc time: %d. Chunks created: %d. Max allocated: %d",tcSettings.gcCount ? *tcSettings.gcCount : 0, tcSettings.gcTime ? *tcSettings.gcTime : 0, tcSettings.chunksCreated ? *tcSettings.chunksCreated : 1, maxAllocated);
   stackDestroy(objStack);
   heapDestroy(chunksHeap);
   heapDestroy(ommHeap);
}

static TCObject allocObjWith(uint32 size)
{
   TCObject o=null;
   uint32 idx = size2idx(size);
   if (idx == OBJARRAY_MAX_INDEX || (o=OBJ_PROPERTIES(freeList[idx])->next) == null) // if this is the max index, or there's no free objects in the current index, try on the highest index
   {
      for (o = OBJ_PROPERTIES(freeList[OBJARRAY_MAX_INDEX])->next; o != null; o = OBJ_PROPERTIES(o)->next)
         if (OBJ_SIZE(o) >= size)
            return o;
      o = null;
      // check if there's someone at a higher index. This fragments memory, but avoids the allocation of one more chunk
      while (++idx < OBJARRAY_MAX_INDEX)
         if ((o = OBJ_PROPERTIES(freeList[idx])->next) != null)
            return o;
   }
   return o;
}

extern bool iosLowMemory;

TCObject allocObject(Context currentContext, uint32 size)
{
   TCObject o = null;
   bool firstPass = true;
   ObjectProperties op;

#ifdef darwin
   if (iosLowMemory && size > 1024*1024)
   {
      iosLowMemory = false;
      throwException(currentContext, OutOfMemoryError, "iOS low memory warning");
      return null;
   }
#endif
   if (currentContext == gcContext) // a finalize method is creating an object? return null
   {
      throwException(currentContext, OutOfMemoryError, "Objects can't be allocated during finalize.");
      return null;
   }
   else
      while (runningFinalizer) // otherwise, another thread is trying to create an object; pass the timeslice back to the gc.
         Sleep(1);

   if (size < 4)
      size = 4;
   size = ((size+3)>>2)<<2; // make power of 4

   LOCKVAR(omm);
   o = allocObjWith(size);
   if (!o) // no more memory to create this object? Run the GC to free up memory
   {
      if (size < 1024*1024)
      {
      tryAgain:
         #ifndef ENABLE_TEST_SUITE // test suite requires that no gc is run in this case - just create the chunk directly
         UNLOCKVAR(omm);
         gc(currentContext);
         LOCKVAR(omm);
         o = allocObjWith(size);
         #endif
      }
      if (!o)
      {
         // still no memory? allocate a new chunk and place it at the OBJARRAY_MAX_INDEX
         if (!createChunk(size > DEFAULT_CHUNK_SIZE ? size : DEFAULT_CHUNK_SIZE))
         {
            // "force" gc again, ignoring the timeout
            if (firstPass)
            {
               firstPass = false;
               goto tryAgain;
            }
            if (COMPUTETIME) alert("out of memory!");
            throwException(currentContext, OutOfMemoryError, null);
            goto end; // no more memory at all, quit.
         }
         o = allocObjWith(size);
      }
   }
   // found a free object?
   if (o)
   {
      uint32 oSize, oldIdx;
      int32 objectBytesRemaining; // may be < 0 !
      op = OBJ_PROPERTIES(o);
      oSize = OBJ_SIZE(o);
      oldIdx = size2idx(oSize);

      objCreated++;
      //debug("G alloc obj size %d : %X (idx %d -> %d)",size, o, oldIdx,newIdx);
      // set as not marked
      OBJ_MARK(o) = !markedAsUsed;
      if (_TRACE_OBJCREATION) debug("G %X setting mark to %d at allocObject", o, OBJ_MARK(o));
      // remove the Object from the free list
      removeNodeFromDblList(freeList[oldIdx], o);

      if (oSize > size)
      {
         objectBytesRemaining = (int32)oSize - (int32)size - sizeof(TObjectProperties);
         if (objectBytesRemaining >= MIN_SPACE_LEFT) // is there enough space to create another minimum object?
         {
            // make the rest of this object a free object
            Chunk startOfNextChunk = ((uint8*)o) + size;
            TCObject oremain = CHUNK2OBJECT(startOfNextChunk);
            uint32 ridx = size2idx(objectBytesRemaining);
            xmemzero(OBJ_PROPERTIES(oremain),sizeof(TObjectProperties));
            OBJ_SIZE(oremain) = objectBytesRemaining;
            insertNodeInDblList(freeList[ridx], oremain);
            if (_TRACE_OBJCREATION) debug("G Allocd: %5d. Remaining object: %X with size %d at index %d. lock: %d. context: %X",size,oremain,objectBytesRemaining,ridx,OBJ_ISLOCKED(oremain),currentContext);
            // and put it in the used list
         }
         else size = oSize; // not enough memory remains in the object, so keep the old size
      }
      OBJ_SIZE(o) = size;
      // objects are always locked
      OBJ_SETLOCKED(o);
      insertNodeInDblList(lockList[0], o);
      objLocked++;

      if (_TRACE_OBJCREATION) debug("G Object %X locked",o);
      // erase the object.
      xmemzero(o, size);
   }
end:
   UNLOCKVAR(omm);
   return o;
}

static TCObject privateCreateObject(Context currentContext, CharP className, bool callDefaultConstructor)
{
   TCClass c;
   uint32 objectSize;
   TCObject o=null;

   c = loadClass(currentContext, className, true);
   if (!c)
      goto end;

   objectSize = c->objSize;
   o = allocObject(currentContext, objectSize);
   if (!o)
      goto end;

   OBJ_CLASS(o) = c;

   if (_TRACE_OBJCREATION) debug("G %X obj created %s of size %d at %d. lock: %d. mark: %d. context: %X", o, className, objectSize, size2idx(objectSize), OBJ_ISLOCKED(o), markedAsUsed, currentContext);

   if (callDefaultConstructor)
   {
      // call the default constructor. first, get a reference to it
      Method defaultConstructor = getMethod(c, false, CONSTRUCTOR_NAME, 0);
      if (defaultConstructor) // if there's no default constructor, no problem.
         executeMethod(currentContext, defaultConstructor, o);
   }
end:
   return o;
}

TC_API TCObject createObjectWithoutCallingDefaultConstructor(Context currentContext, CharP className)
{
   return privateCreateObject(currentContext, className, false);
}

TC_API TCObject createObject(Context currentContext, CharP className)
{
   return privateCreateObject(currentContext, className, true);
}

TCObject createArrayObject(Context currentContext, CharP type, int32 len)
{
   TCClass c;
   uint32 arraySize, objectSize;
   TCObject o=null;

   if (len < 0)
      return null;

   c = loadClass(currentContext, type, true);
   if (!c)
      goto end;
   arraySize = len << c->flags.bits2shift;
   objectSize = 4 + arraySize; // there's a single instance field in the Array class: length
   o = allocObject(currentContext, objectSize);
   if (!o)
      goto end;

   if (_TRACE_OBJCREATION) debug("G %X array obj created %s len %d, size = %d at %d. lock: %d", o, c->name,len, objectSize, size2idx(objectSize), OBJ_ISLOCKED(o));

   ARRAYOBJ_LEN(o) = len;
   OBJ_CLASS(o) = c;
end:
   return o;
}

TCObject createByteArrayObject(Context currentContext, int32 len, const char *file, int32 line)
{
   TCObject o = createArrayObject(currentContext, BYTE_ARRAY, len);
#if _TRACE_LOCKED_BYTEARRAYS
   debug("byteArray %X created at %s (%d)",o,file,line);
#endif
   return o;
}

TCObject createArrayObjectMulti(Context currentContext, CharP type, int32 count, uint8* dims, int32* regI)
{
   uint32 len;
   TCObject o;
   TCObject *oa;

   // note that all dimensions are type java.lang.Array, except the last one.
   len = dims == null ? regI[0] : *dims < 65 ? regI[*dims] : (*dims-65);
   o = createArrayObject(currentContext, type, len);
   if (o != null && count > 1)
   {
      for (oa = (TCObject*)ARRAYOBJ_START(o); len-- > 0; oa++)
         if ((*oa = createArrayObjectMulti(currentContext, type+1, count-1, dims == null ? null : dims+1, dims == null ? regI+1 : regI)) == null)
            return null;
         else
            setObjectLock(*oa, UNLOCKED);
   }
   return o;
}

TCObject createStringObjectWithLen(Context currentContext, int32 len)
{
   TCObject str;
   str = createObjectWithoutCallingDefaultConstructor(currentContext, "java.lang.String"); // do not call default constructor, we'll set our own char array (2 lines below)
   if (str)
   {
      String_chars(str) = createCharArray(currentContext, len);
      if (String_chars(str) != null)
         setObjectLock(String_chars(str), UNLOCKED);
   }
   return (str && String_chars(str)) ? str : null;
}

TCObject createStringObjectFromJCharP(Context currentContext, JCharP srcChars, int32 len)
{
   TCObject str;
   if (len < 0) len = JCharPLen(srcChars);
   str = createStringObjectWithLen(currentContext, len);
   if (str)
      xmemmove(ARRAYOBJ_START(String_chars(str)), srcChars, len<<1);
   return str;
}

TCObject createStringObjectFromTCHARP(Context currentContext, TCHARP srcChars, int32 len)
{
#if !defined (WINCE)
   JCharP dst;
#endif
   TCObject str;
   if (len < 0) len = tcslen(srcChars);
   if ((str = createStringObjectWithLen(currentContext, len)) != null)
   {
#if defined (WINCE)
      xmemmove(ARRAYOBJ_START(String_chars(str)), srcChars, len<<1);
#else
      dst = (JCharP)ARRAYOBJ_START(String_chars(str));
      while (len-- > 0)
         *dst++ = (JChar)(*srcChars++ & 0xFF);
#endif
   }
   return str;
}

TCObject createStringObjectFromCharP(Context currentContext, CharP srcChars, int32 len)
{
   JCharP dst;
   TCObject str;
   if (len < 0) len = xstrlen(srcChars);
   str = createStringObjectWithLen(currentContext, len);
   if (str == null)
      return null;
   dst = (JCharP)ARRAYOBJ_START(String_chars(str));
   while (len-- > 0)
      *dst++ = (JChar)(*srcChars++ & 0xFF);
   return str;
}

TC_API void setObjectLock(TCObject o, LockState lock)
{
   int32 size,idx;
   if (o == null) return;
   LOCKVAR(omm);
   size = OBJ_SIZE(o);
   idx = size2idx(size);
   if (lock == LOCKED)
   {
      if (OBJ_ISLOCKED(o))
         alert("FATAL ERROR: OBJECT %X (%s) IS BEING LOCKED BUT IT WAS NOT PREVIOUSLY UNLOCKED!", o, OBJ_CLASS(o)->name);
      OBJ_SETLOCKED(o);
      // remove from the used list
      removeNodeFromDblList(usedList[idx], o);
      insertNodeInDblList(lockList[0], o);
      objLocked++;
   }
   else
   {
      if (!OBJ_ISLOCKED(o))
         alert("FATAL ERROR: OBJECT %X (%s) IS BEING UNLOCKED BUT IT WAS NOT PREVIOUSLY LOCKED!", o, OBJ_CLASS(o)->name);
      OBJ_SETUNLOCKED(o);
      // add it back to the used list
      removeNodeFromDblList(lockList[0], o);
      insertNodeInDblList(usedList[idx], o);
      objLocked--;
   }
   OBJ_MARK(o) = !markedAsUsed;
   //if (_TRACE_OBJCREATION) debug("G %s object %X class %s. mark: %d",lock == LOCKED ? "locking" : "unlocking", o, OBJ_CLASS(o)->name, OBJ_MARK(o));
   UNLOCKVAR(omm);
}

static void markSingleObject(TCObject o)
{
   TCClass c;
   TObjectsToVisit objs;

   if (OBJ_MARK(o) == markedAsUsed) // don't remove! this test is important
      return;
   // mark as used to avoid infinite recursion
   OBJ_MARK(o) = markedAsUsed;
   //if (_TRACE_OBJCREATION) debug("G %X setting mark to %d at markSingleObject", o, OBJ_MARK(o));
   if (!OBJ_ISLOCKED(o)) // locked objects can't be revived, since they are not in the used/free lists
   {
      // "revive" the object
      int32 size,idx;
      size = OBJ_SIZE(o);
      idx = size2idx(size);
      // remove from the free list
      removeNodeFromDblList(freeList[idx], o);
      // and put it in the used list
      insertNodeInDblList(usedList[idx], o);
      if (_TRACE_OBJCREATION) debug("G Object revived: %X (%s). mark: %d",o, OBJ_CLASS(o)->name, OBJ_MARK(o));
   }
   c = OBJ_CLASS(o);
   // if this object is an array, and the elements are objects (or arrays), then push them to be marked later
   if (c->flags.isObjectArray) // array of objects or array of arrays?
   {
      objs.start = (TCObjectArray)ARRAYOBJ_START(o);
      if (objs.start != null && (objs.n = ARRAYOBJ_LEN(o)) > 0)
         stackPush(objStack, &objs);
   }
   else // else, mark the instance fields of this object (note that arrays have no object instance fields)
   if (c->objInstanceFields != null)
   {
      objs.start = (TCObjectArray)FIELD_OBJ_OFFSET(o,c);
      objs.n = (int32)((TCObjectArray)FIELD_V64_OFFSET(o,c) - objs.start); // the object fields ends where the 64-bit ones start.
      stackPush(objStack, &objs);
   }
}

static void markObjects(TCObject o)
{
   TObjectsToVisit objs;

   markSingleObject(o);

   // Here we will mark recursively all objects inside this one.
   // First we go through all fields and array values (if applicable),
   // marking them, and pushing them to the recurse buffer.
   // Then we pop each and do the recursion.
   while (stackPop(objStack, &objs))
   {
      do
      {
         o = *objs.start++;
      } while (--objs.n > 0 && o == null); // skip null objects
      if (objs.n > 0) // if there still more objects to visit, push the structure back.
         stackPush(objStack, &objs);
      if (o != null)
         markSingleObject(o);
   }
}

static void markClass(int32 i32, VoidP ptr)
{
   TCClass c = (TCClass)ptr;
   int32 i;
   TCObject* f = c->objStaticValues;
   UNUSED(i32)

   // mark all static fields
   for (i = ARRAYLENV(f); i-- > 0; f++)
      if (*f && OBJ_MARK(*f) != markedAsUsed) // we must also mark the objects inside a locked object
         markObjects(*f);
}

static int32 countObjectsInList(TCObject o, bool dump, int32 mark, int32* size)
{
   int32 n = 0;
   if (size) *size = 0;
   for (o=OBJ_PROPERTIES(o)->next; o != null; o = OBJ_PROPERTIES(o)->next)
   {
      ObjectProperties op = OBJ_PROPERTIES(o);
      if (size)
         *size += op->size;
      if (_TRACE_OBJCREATION && dump) debug("G %X",o);
      if (_TRACE_OBJCREATION && o == OBJ_PROPERTIES(o)->next)
         alert("Infinite recursion detected. Probable cause:\nsetObjectLock called for object %x (size %d)\nwhile it was already in the desired state.",o,op->size);
#ifndef ENABLE_TEST_SUITE
      if (mark >= 0)
         if ((int32)op->mark != mark)
         {
            debug("G %X: mark is %d but should be %d (%s)",o,(int)op->mark, (int)mark, OBJ_CLASS(o) ? OBJ_CLASS(o)->name : "null class");
            alert("G %X: mark is %d but should be %d (%s)",o,(int)op->mark, (int)mark, OBJ_CLASS(o) ? OBJ_CLASS(o)->name : "null class");
         }
#endif
      n++;
   }
   return n;
}
static int32 countObjectsIn(TCObjectArray oa, bool dumpCount, bool dumpObj, int32 mark)
{
   int32 n = 0,i,j;
   int32 partial=0,total=0;
   for (i = 0; i <= OBJARRAY_MAX_INDEX; i++, oa++)
   {
      j = countObjectsInList(*oa,dumpObj,mark, &partial);
      n += j;
      if (dumpCount && j > 0) debug("G %5d free of size %4d (%6d)",j,i<<2,partial);
      total += partial;
   }
   if (dumpCount && total > 0) debug("G                   Total: %6d", total);
   return n;
}

bool joinAdjacentObjects(uint8* block, uint32 size)
{
   uint8* block00 = block;
   uint8* block0 = block;
   uint8* blockEnd = block + size;
   ObjectProperties op=null,op0;
   uint32 newSize;
   int32 usedCount=0,usedSize=0;
   if (size < DEFAULT_CHUNK_SIZE)
      return false;
   while (block < blockEnd)
   {
      op = (ObjectProperties)block;
      if (/*op->next == null || */op->class_ != null) // not empty object? skip  - guich@tc113_19: commented first part. since the blocks are added FIFO, retaining that block is not good. the idea here was to keep the first memory block created, but since the block was created for the constant pool strings, and these are locked, this comparison is uneeded.
      {
         usedCount++;
         if (COMPUTETIME) usedSize += op->size+sizeof(TObjectProperties);
         if (op->lock) // if its locked, this chunk is probably used for constant pools, so we just ignore the whole chunk to improve performance.
            return false;
         block += sizeof(TObjectProperties) + op->size;
      }
      else
      {
         // this object is free. check if the next object is also free and merge the blocks
         block0 = block;
         op0 = op;
         newSize = op0->size;
         while (true)
         {
            block += sizeof(TObjectProperties) + op->size; // move to next chunk
            if (block >= blockEnd)
               break;
            op = (ObjectProperties)block;
            if (op->class_ != null) // if next is not free, stop
            {
               usedCount++;
               if (COMPUTETIME) usedSize += op->size+sizeof(TObjectProperties);
               break;
            }
            // next is free, remove it from the list of free ones and inc the first object's size
            removeNodeFromDblList(freeList[size2idx(op->size)], CHUNK2OBJECT(block));
            newSize += sizeof(TObjectProperties) + op->size;
         }
         if (newSize != op0->size) // chunks were merged?
         {
            int32 oldIdx = size2idx(op0->size);
            int32 newIdx = size2idx(newSize);
            TCObject o = CHUNK2OBJECT(block0);
            if (_DEBUG_OMM_LIST) debug("G %X merged: %4d -> %4d (%3d->%3d)", o, op0->size, newSize,oldIdx,newIdx);
            op0->size = newSize;
            if (oldIdx != newIdx)
            {
               removeNodeFromDblList(freeList[oldIdx], o);
               insertNodeInDblList(freeList[newIdx], o);
            }
         }
         // already positioned in next chunk
      }
   }
   if (usedCount == 0)
   {
      // remove, from the list of free chunks, the chunk that will be deleted
      if (COMPUTETIME) debug("G --- Removed chunk %X size %d",block00, size-sizeof(TObjectProperties));
      removeNodeFromDblList(freeList[size2idx(((ObjectProperties)block00)->size)], CHUNK2OBJECT(block00));
      if (tcSettings.chunksCreated) (*tcSettings.chunksCreated)--;
      return true;
   }
   if (COMPUTETIME) debug("G Chunk %X size %d has %d used objects with %d bytes",block0, size-sizeof(TObjectProperties), usedCount, usedSize > 0 ? (usedSize-sizeof(TObjectProperties)) : 0);
   return false;
}

static void markContexts()
{                         
   int32 i;         
   Context c;
   Context copy[MAX_CONTEXTS];
   xmemmove(copy,contexts,MAX_CONTEXTS*sizeof(Context));
   
   for (i = 0; i < MAX_CONTEXTS; i++)
      if ((c=copy[i]) != null)
      {
         TCObjectArray oa = c->regOStart;
         if (c->threadObj)
            markObjects(c->threadObj);
         if (c->nmp.retO)
            markObjects(c->nmp.retO);
         for (oa = c->regOStart; oa < c->regO; oa++)
            if (*oa && OBJ_MARK(*oa) != markedAsUsed) // we must also mark the objects inside a locked object
               markObjects(*oa);
         if (c->thrownException != null)
            markObjects(c->thrownException);
      }
}

void finalizeObject(TCObject o, TCClass c)
{
   while (c != null) 
   {
      if (c->finalizeMethod == null) 
         c = c->superClass;
      else 
      {
         if (c->dontFinalizeFieldIndex == 0 || FIELD_I32(o,(c->dontFinalizeFieldIndex-1)) == false) 
         {
            if (_TRACE_OBJDESTRUCTION) debug("G object being finalized: %X (%s)", o, OBJ_CLASS(o)->name);
            executeMethod(gcContext, c->finalizeMethod, o);
            if (_TRACE_OBJDESTRUCTION) debug("G object finalized: %X (%s)", o, OBJ_CLASS(o)->name);
         }
         c = null; // stop
      }
   }
}

void visitImages(VisitElementFunc onImage, int32 param) // visits all images
{
   TCObjectArray usedL;
   TCObject o;
   int32 i;                    
   if (destroyingApplication) return;

   LOCKVAR(omm);
   for (i = 0, usedL = usedList; i <= OBJARRAY_MAX_INDEX; i++, usedL++)
      if (*usedL)
         for (o=OBJ_PROPERTIES(*usedL)->next; o != null; o = OBJ_PROPERTIES(o)->next)
            if (OBJ_CLASS(o) == imageClass) // if user defined a dontFinalize field and set it to true, don't call finalize
               onImage(param,o);
   if (lockList)
   for (o=OBJ_PROPERTIES(*lockList)->next; o != null; o = OBJ_PROPERTIES(o)->next)
      if (OBJ_CLASS(o) == imageClass) // if user defined a dontFinalize field and set it to true, don't call finalize
         onImage(param,o);
   UNLOCKVAR(omm);
}

void runFinalizers() // calls finalize of all objects in use
{
   TCObjectArray usedL;
   TCObject o;
   int32 i;
   TCClass c;
   gcContext->litebasePtr = mainContext->litebasePtr;  // let litebase destroy the ptr if he wants so
   for (i = 0, usedL = usedList; i <= OBJARRAY_MAX_INDEX; i++, usedL++)
      if (*usedL)
         for (o=OBJ_PROPERTIES(*usedL)->next; o != null; o = OBJ_PROPERTIES(o)->next)
            if ((c = OBJ_CLASS(o)) != null && c->finalizeMethod != null && (c->dontFinalizeFieldIndex == 0 || FIELD_I32(o,(c->dontFinalizeFieldIndex-1)) == false)) // if user defined a dontFinalize field and set it to true, don't call finalize
               finalizeObject(o, OBJ_CLASS(o));
   for (o=OBJ_PROPERTIES(*lockList)->next; o != null; o = OBJ_PROPERTIES(o)->next)
      if ((c = OBJ_CLASS(o)) != null && c->finalizeMethod != null && (c->dontFinalizeFieldIndex == 0 || FIELD_I32(o,(c->dontFinalizeFieldIndex-1)) == false)) // if user defined a dontFinalize field and set it to true, don't call finalize
         finalizeObject(o, OBJ_CLASS(o));
   mainContext->litebasePtr = gcContext->litebasePtr; // update the ptr
}

#if defined(ANDROID) || defined(WINCE)
 #define CRITICAL_SIZE 2*1024*1024
 #define USE_MAX_BLOCK true
#elif defined(WIN32)
 #define CRITICAL_SIZE 512*1024
 #define USE_MAX_BLOCK false
#else
 #define CRITICAL_SIZE 2*1024*1024
 #define USE_MAX_BLOCK false
#endif

void preallocateArray(Context currentContext, TCObject sample, int32 length)
{
   int32 size = OBJ_SIZE(sample);
   int32 totSize = size * length;
   int32 totChunks = (totSize+DEFAULT_CHUNK_SIZE-1) / DEFAULT_CHUNK_SIZE;
   if (totSize >= DEFAULT_CHUNK_SIZE)
      while (totChunks-- > 0)
         createChunk(DEFAULT_CHUNK_SIZE);
}
#if defined(WIN32) && !defined(WINCE)
#define MINTIME 50
#else
#define MINTIME 500
#endif

static void dumpCount(int32 key, int32 i32, VoidP ptr)
{
   TCClass cc = (TCClass)key;
   debug("%30s: %d",cc->name, i32);
}

void gc(Context currentContext)
{
   int32 i;
   TCClass c;
   TCObjectArray freeL, usedL;
   TCObject o;
   int32 iniT,endT, elapsed;
   int32 nfree,nused,compIni,freemem;
   LOCKVAR(omm); // guich@tc120: another fix for concurrent threads

   iniT = getTimeStamp();
   elapsed = iniT - lastGC;
#ifdef WINCE // guich@tc113_20
   if (oldAutoOffValue != 0) // guich@450_33: since the autooff timer function don't work on wince, we must keep resetting the idle timer so that the device will never go sleep - guich@554_7: reimplemented this feature
      SystemIdleTimerReset();
#endif
#if !defined(ENABLE_TEST_SUITE) // this scrambles the test
   freemem = getFreeMemory(USE_MAX_BLOCK);
   if (disableGC || (IS_VMTWEAK_ON(VMTWEAK_DISABLE_GC) || elapsed < MINTIME) && freemem > CRITICAL_SIZE) // use an agressive gc if memory is under 2MB - guich@tc114_18: let user control gc runs
   {
      skippedGC++;
      if (COMPUTETIME) debug("G ====  GC SKIPPED DUE %dms < 500ms", elapsed);
      UNLOCKVAR(omm);
      return;
   }
#endif
   //debug("gc %d (%dms / %d bytes)",tcSettings.gcCount ? *tcSettings.gcCount : 0,elapsed,freemem);
   if (destroyingApplication)
   {
      UNLOCKVAR(omm);
      return;
   }
   lastGC = getTimeStamp(); // guich@tc210: moving to here will make only one thread using the gc and the others will just allocate the needed memory. this fixes a crash in LaudoMovel loading jpegs in threads

//   while (runningGC) Sleep(1);
   runningGC = true;

   if (IS_VMTWEAK_ON(VMTWEAK_AUDIBLE_GC))
      soundTone(1000,10);

   if (COMPUTETIME)
   {
      debug("G checking free at start"); nfree = countObjectsIn(freeList,false,false,-1);
      debug("G checking used at start"); nused = countObjectsIn(usedList,false,false,!markedAsUsed);
      debug("G ====  GC INI : %d (skipped: %d) free: %d, used: %d, mark: %d, chunks: %d, objs created: %d (%d ms ago), locked objs: %d, context: %X, free mem: %d (max: %d). context: %X", tcSettings.gcCount ? *tcSettings.gcCount : 0, skippedGC, nfree, nused, markedAsUsed, tcSettings.chunksCreated ? *tcSettings.chunksCreated : 1, objCreated, iniT - lastGC, objLocked, currentContext, getFreeMemory(false), getFreeMemory(true), currentContext);
      iniT = getTimeStamp(); // discount the time used to compute these
   }

   skippedGC = objCreated = 0;
   if (tcSettings.gcCount) (*tcSettings.gcCount)++;
   IF_HEAP_ERROR(objStack->heap)
   {
      goto heaperror;
   }
   IF_HEAP_ERROR(chunksHeap)
   {
heaperror:
      if (COMPUTETIME) alert("out of memory!");
      throwException(currentContext, OutOfMemoryError, "During object.mark stage");
      goto end;
   }

   // 1. move the list of used objects to the free list
   for (freeL=freeList, usedL=usedList, i=0; i <= OBJARRAY_MAX_INDEX; i++, freeL++,usedL++)
      if (OBJ_PROPERTIES(*usedL)->next != null)
         moveDblList(*usedL, *freeL);
   if (!destroyingApplication) // if this is the last gc, just collect all objects
   {
#if _TRACE_LOCKED_BYTEARRAYS
      Hashtable htCount = htNew(100,null);
      int lockCount=0;
#endif
      // 2. go through all the reachable objects and move them back to the used list
      // 2a. static fields of loaded classes
      if (CANTRAVERSE)
         htTraverse(&htLoadedClasses, markClass);
      // 2b. mark the locked objects
      if (_TRACE_OBJCREATION) debug("G marking locked objs start");
      for (o=OBJ_PROPERTIES(*lockList)->next; o != null; o = OBJ_PROPERTIES(o)->next)
      {
#if _TRACE_LOCKED_BYTEARRAYS
         TCClass cc = OBJ_CLASS(o);
         if (strEq(cc->name,BYTE_ARRAY))
            debug("locked ba: %X",o);
         htPut32(&htCount, (int32)cc, 1 + htGet32(&htCount, (int32)cc));
         lockCount++;
#endif
         //if (_TRACE_OBJCREATION) debug("G marking locked obj %X",o);
         if (OBJ_CLASS(o)->flags.isString) // 99% of the locked objects, due to the constant pool
         {
            OBJ_MARK(o) = markedAsUsed;
            if (String_chars(o)) markSingleObject(String_chars(o));
         }
         else 
            markObjects(o);
      }
#if _TRACE_LOCKED_BYTEARRAYS
      htTraverseWithKey(&htCount, dumpCount);
      debug("locked: %d",lockCount);
#endif
      if (_TRACE_OBJCREATION) debug("G marking locked objs end");
      // 2c. used objects in the object registers of all available contexts
      markContexts();
   }
   // now all reachable objects are moved to the still-alive list.
   if (COMPUTETIME) compIni = getTimeStamp();
   // 3. mark the free chunks as empty, so the compact can work correctly, and run the finalize methods if any
   markedAsUsed = !markedAsUsed; // otherwise, objects allocated in this executeMethod will have problems when being collected
   runningFinalizer = true;
   if (COMPUTETIME) debug("G finalizing objects");
   gcContext->litebasePtr = currentContext->litebasePtr;  // let litebase destroy the ptr if he wants so
   // bruno@tc134: split the finalize method call and the free object stages
   for (i = 0, freeL = freeList; i <= OBJARRAY_MAX_INDEX; i++, freeL++)
      if (*freeL)
         for (o=OBJ_PROPERTIES(*freeL)->next; o != null; o = OBJ_PROPERTIES(o)->next)
            finalizeObject(o, OBJ_CLASS(o));
   for (i = 0, freeL = freeList; i <= OBJARRAY_MAX_INDEX; i++, freeL++)
      if (*freeL)
         for (o=OBJ_PROPERTIES(*freeL)->next; o != null; o = OBJ_PROPERTIES(o)->next)
            if ((c = OBJ_CLASS(o)) != null)
            {
               if (_TRACE_OBJCREATION) debug("G object being freed: %X (%s)",o, OBJ_CLASS(o)->name);
               OBJ_CLASS(o) = null; // set the object "free"
            }
   currentContext->litebasePtr = gcContext->litebasePtr; // update the ptr
   if (COMPUTETIME) debug("G finished finalizers");

   runningFinalizer = false;
   markedAsUsed = !markedAsUsed;
   // 4. Join all adjacent free objects and free the chunk if only one object remains
#ifndef ENABLE_TEST_SUITE // this scrambles the test
   if (COMPUTETIME) debug("G joining chunks...");
   heapFreeAsking(chunksHeap, joinAdjacentObjects);
#endif
end:
   endT = getTimeStamp();
   //if (endT != iniT) debug("G GC elapsed: %d",endT-iniT);
   if (tcSettings.gcTime) 
      *tcSettings.gcTime += endT - iniT;

   //debug("gc %d - chunks %d",tcSettings.gcCount ? *tcSettings.gcCount : 0,tcSettings.chunksCreated ? *tcSettings.chunksCreated : 0);
   if (COMPUTETIME)
   {
      debug("G checking free at end"); nfree = countObjectsIn(freeList,true,false,-1);
      nused = countObjectsIn(usedList,false,false,markedAsUsed);
      debug("G checking used at end"); debug("G ====  GC END : free: %d, used: %d, chunks: %d, elapsed: %4d (compact: %3d)", nfree, nused, tcSettings.chunksCreated ? *tcSettings.chunksCreated : 1, endT-iniT, endT - compIni);
   }
   // and now INVERT THE MARK BIT
   markedAsUsed = !markedAsUsed;

   if (IS_VMTWEAK_ON(VMTWEAK_AUDIBLE_GC))
      soundTone(1100,10);

   //debug("G Freed objects (including allocated chunks)"); countObjectsIn(freeList,false);
   runningGC = false;
   UNLOCKVAR(omm);
}

#ifdef ENABLE_TEST_SUITE
#include "objectmemorymanager_test.h"
#endif
