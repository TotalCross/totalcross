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

ImplementList(TCHARP) // TCHARPs
ImplementList(CharP) // CharPs
ImplementList(VoidP) // VoidPs

///////////////////////////////////////////////////////////////////////////
//                                  Hashtable                            //
///////////////////////////////////////////////////////////////////////////

/* Creates a new hashtable. Count must be multiple of 2, and
 * it will be rounded to if not.
 */
TC_API Hashtable htNew(int32 count, Heap heap)
{
   Hashtable iht;
   if (count < 8) count = 8;
   else
      count = (int32)(count>>1) << 1; // always multiple of 2

   iht.size = 0;
   iht.heap = heap;
   iht.items = heap ? (HtEntry**)heapAlloc(heap, count*TSIZE) : (HtEntry**)xmalloc(count*TSIZE);
   // already done in xmalloc - xmemzero(iht.items,count*TSIZE);
   iht.hash  = count-1;
   iht.threshold = count;// * 75 / 100;
   return iht;
}

/* Gets the stored item with the given key. If the key is not found,
 * returns null.
 */
static HtEntry* htGet(Hashtable *iht, HTKey key)
{
   if (iht && iht->items && iht->size > 0) // guich@tc113_14: check size
   {
      int32 index = key & iht->hash;
      HtEntry *e;
      for (e = iht->items[index]; e != null ; e = e->next)
         if (e->key == key)
            return e;
   }
   return null;
}

TC_API int32 htGet32(Hashtable *iht, HTKey key)
{
   HtEntry* h = htGet(iht, key);
   return h ? h->i32 : 0;
}

TC_API int32 htGet32Inv(Hashtable *iht, HTKey key)
{
   HtEntry* h = htGet(iht, key);
   return h ? h->i32 : (int32) 0xFFFFFFFF;
}

TC_API VoidP htGetPtr(Hashtable *iht, HTKey key)
{
   HtEntry* h = htGet(iht, key);
   return h ? h->ptr : null;
}

/* Once the number of elements gets above the load factor, rehashes the hashtable.
 Does NOT work when the hashtable has a heap.
 */
static bool htRehash(Hashtable *table)
{
   if (table->heap == null)
   {
      int32 oldCapacity = table->hash+1, i, index;
      HtEntry **oldTable = table->items, *e, *old;
      int32 newCapacity = oldCapacity << 1; // in C: for faster hashes, we must always double the hashtable. (((oldCapacity << 1) + oldCapacity) >> 1) + 1;
      HtEntry **newTable = (HtEntry **)xmalloc(TSIZE*newCapacity);
      //xmemzero(newTable,4*newCapacity);

      if (!newTable)
         return false;
      table->threshold = newCapacity * 75 / 100;
      table->items = newTable;
      table->hash = newCapacity-1;

      for (i = oldCapacity ; i-- > 0 ;)
         for (old = oldTable[i] ; old != null ; )
         {
            e = old;
            old = old->next;

            index = e->key & table->hash;
            e->next = newTable[index];
            newTable[index] = e;
         }
      xfree(oldTable);
   }
   return true;
}

/* Puts the given pair of key/value in the Hashtable.
 * If the key already exists, the value will be replaced.
 */
static bool htPut(Hashtable *iht, HTKey key, int32 i32, VoidP ptr, bool isI32, bool ignoreIfNotNew)
{
   HtEntry *e;
   int32 index;

   index = key & iht->hash;
   if (iht->size > 0) // Only search in non-empty hash tables.
   {
      // Makes sure the key is not already in the hashtable.
      for (e = iht->items[index] ; e; e = e->next)
         if (e->key == key)
         {
            if (!ignoreIfNotNew)
            {
               if (isI32)
                  e->i32 = i32;
               else
                  e->ptr = ptr;
            }
            return true;
         }
   }
   if (iht->size >= iht->threshold)
   {
      // Rehash the table if the threshold is exceeded
      if (!htRehash(iht))
         return false;
      index = key & iht->hash;
   }

   // Creates the new entry.
   e = iht->heap ? (HtEntry*)heapAlloc(iht->heap, sizeof(HtEntry)) : (HtEntry*)xmalloc(sizeof(HtEntry));
   if (!e)
      return false;
   e->key = key;
   if (isI32)
      e->i32 = i32;
   else
      e->ptr = ptr;
   e->next = iht->items[index];
   iht->items[index] = e;
   iht->size++;
   return true;
}

TC_API bool htPut32(Hashtable *iht, HTKey key, int32 value)
{
   return htPut(iht, key, value, null, true,false);
}

TC_API bool htPutPtr(Hashtable *iht, HTKey key, void* value)
{
   return htPut(iht, key, 0, value, false, false);
}

TC_API bool htInc(Hashtable *iht, HTKey key, int32 incValue)
{
   return htPut32(iht, key, incValue + htGet32(iht, key));
}

TC_API bool htPut32IfNew(Hashtable *iht, HTKey key, int32 value)
{
   return htPut(iht, key, value, null, true, true);
}

TC_API bool htPutPtrIfNew(Hashtable *iht, HTKey key, void* value)
{
   return htPut(iht, key, 0, value, false, true);
}

/* Removes the given key from the hashtable. */
TC_API void htRemove(Hashtable *iht, HTKey key)
{
   HtEntry **tab = iht->items;
   HtEntry *e,*prev;
   int index = key & iht->hash;
   if (iht->size > 0) // guich@tc113_14: check size
   for (e = tab[index], prev = null ; e != null ; prev = e, e = e->next)
      if (e->key == key)
      {
         if (prev != null)
            prev->next = e->next;
         else
            tab[index] = e->next;
         if (iht->heap == null) xfree(e);
         iht->size--;
         break;
      }
}

/* Frees the hashtable. An optional function can be passed as parameter
 * so that a custom cleanup can be made on that item.
 */
TC_API void htFree(Hashtable *iht, VisitElementFunc freeElement)
{
   if (iht)
   {
      HtEntry **tab = iht->items;
      HtEntry *e,*next;
      int32 n = iht->hash;
      if (tab == null)
         return;
      while (n-- >= 0)
         for (e = *tab++; e != null ;)
         {
            next = e->next;
            if (freeElement)
               freeElement(e->i32, e->ptr);
            if (iht->heap == null) xfree(e);
            e = next;
         }
      if (iht->heap == null) xfree(iht->items);
      iht->size = 0;
   }
}

/* Frees the hashtable. An optional function can be passed as parameter
 * so that a custom cleanup can be made on that item.
 */
TC_API bool htFreeContext(Context context, Hashtable *iht, VisitElementContextFunc freeElement)
{
   HtEntry **tab = iht->items;
   HtEntry *e,*next;
   int32 n = iht->hash;
   bool ret = true;
   if (tab == null)
      return true;
   while (n-- >= 0)
      for (e = *tab++; e != null ;)
      {
         next = e->next;
         if (freeElement && !freeElement(context, e->ptr))
            ret = false;
         if (iht->heap == null) xfree(e);
         e = next;
      }
   if (iht->heap == null) xfree(iht->items);
   iht->size = 0;
   return ret;
}

void htTraverse(Hashtable *iht, VisitElementFunc visitElement)
{
   HtEntry **tab = iht->items;
   HtEntry *e;
   int32 n = iht->hash;
   if (tab == null || visitElement == null)
      return;
   while (n-- >= 0)
      for (e = *tab++; e != null ;e = e->next)
         visitElement(e->i32, e->ptr);
}

void htTraverseWithKey(Hashtable *iht, VisitElementKeyFunc visitElement)
{
   HtEntry **tab = iht->items;
   HtEntry *e;
   int32 n = iht->hash;
   if (tab == null || visitElement == null)
      return;
   while (n-- >= 0)
      for (e = *tab++; e != null ;e = e->next)
         visitElement(e->key, e->i32, e->ptr);
}

///////////////////////////////////////////////////////////////////////////
//                                Stack                                  //
///////////////////////////////////////////////////////////////////////////

TC_API Stack newStack(int32 elemCount, int32 elemSize, Heap defHeap)
{
   Stack s;
   Heap heap = defHeap ? defHeap : heapCreate();
   s = newXH(Stack, heap);
   s->heap = heap;
   s->initialBlockSize = elemCount * elemSize;
   s->elemSize = elemSize;
   return s;
}

TC_API void stackPush(Stack s, VoidP p)
{
   if (s->pos == s->blockEnd)
   {
      if (s->current == null || s->current->next == s->head) // reached the end? create a new block
      {
         UInt8Array newBlock = newPtrArrayOf(UInt8, s->initialBlockSize, s->heap);
         s->head = VoidPsAdd(s->head, newBlock, s->heap);
         s->current = s->head->prev;
      }
      else s->current = s->current->next; // reuse a previously allocated block

      s->blockStart = s->pos = s->current->value;
      s->blockEnd = s->pos + s->initialBlockSize;
   }                                 
   if (s->elemSize == 8) // most common
      *((int64*)s->pos) = *((int64*)p);
   else
      xmemmove(s->pos, p, s->elemSize);
   s->pos += s->elemSize;
}

TC_API bool stackPop(Stack s, VoidP out)
{
   if (s->pos == s->blockStart) // underflow? go to previous block
   {
      if (s->current == s->head)
         return false;
      s->current = s->current->prev;
      s->blockStart = s->current->value;
      s->pos = s->blockEnd = s->blockStart + s->initialBlockSize;
   }
   s->pos -= s->elemSize;
   if (out)
   {
      if (s->elemSize == 8) // most common
         *((int64*)out) = *((int64*)s->pos);
      else
         xmemmove(out, s->pos, s->elemSize);
   }
   return true;
}

///////////////////////////////////////////////////////////////////////////
//                                Array                                  //
///////////////////////////////////////////////////////////////////////////

void* privateNewArray(int32 sizeofElem, int32 len, Heap mp, const char *file, int line)
{
   if (len)
   {
      int32 size = (len*sizeofElem)+TSIZE;
      uint8* p = mp ? heapAlloc(mp, size) : xmalloc(size);
      if (p)
      {
         p += TSIZE;
         SET_ARRAYLEN(p) = len;
         return p;
      }
      else
      debug("newArray(%d,%d,%X) called from %s (%d)",sizeofElem, len, mp, file, line);
   }
   return null;
}

///////////////////////////////////////////////////////////////////////////
//                                  List                                 //
///////////////////////////////////////////////////////////////////////////

/* Given a linked list of VoidPs, returns the number of items stored. */
int32 listGetCount(VoidP l)
{
   int32 count = 0;
   if (l)
   {
      VoidPs* list = (VoidPs*)l;
      VoidPs* head = list;
      do
      {
         count++;
         list = list->next;
      } while (head != list);
   }
   return count;
}

VoidP list2array(VoidP li, int32 sizeofElem, int32 extra)
{
   UInt8Array l = NULL,ll;
   if (li)
   {
      VoidPs* list = (VoidPs*)li;
      int32 n = listGetCount(list);
      l = newArray(sizeofElem, n+extra, null);
      ll = l + extra*sizeofElem;
      for (; n-- > 0; list = list->next, ll += sizeofElem)
         xmemmove(ll, &list->value, sizeofElem);
   }
   return l;
}
