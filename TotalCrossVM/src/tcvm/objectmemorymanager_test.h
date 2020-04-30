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

static bool saveRestoreOMM(bool save)
{
   if (save)
   {
      CANTRAVERSE = false;
      // save
      freeList2 = freeList;             freeList = null;
      usedList2 = usedList;             usedList = null;
      lockList2 = lockList;             lockList = null;
      markedAsUsed2 = markedAsUsed;     markedAsUsed = 0;
      gcCount2 = *tcSettings.gcCount;   *tcSettings.gcCount = 0;
      ommHeap2 = ommHeap;               ommHeap = null;
      chunksHeap2 = chunksHeap;         chunksHeap = null;
      objCreated2 = objCreated;         objCreated = 0;
      skippedGC2 = skippedGC;           skippedGC = 0;
      objLocked2 = objLocked;           objLocked = 0;
      objStack2 = objStack;             objStack = null;

      return initObjectMemoryManager();
   }
   else
   {
      destroyObjectMemoryManager(); // destroy currently allocated objects

      freeList = freeList2;
      usedList = usedList2;
      lockList = lockList2;
      markedAsUsed = markedAsUsed2;
      *tcSettings.gcCount = gcCount2;
      ommHeap = ommHeap2;
      chunksHeap = chunksHeap2;
      objCreated = objCreated2;
      skippedGC = skippedGC2;
      objLocked = objLocked2;
      objStack = objStack2;

      CANTRAVERSE = true;
      return true;
   }
}

TESTCASE(StringObject) // #DEPENDS(DblList)
{
   JChar buf[9];
   JCharP s = CharP2JCharPBuf("Michelle",8, buf, true);
   TCObject o = createStringObjectFromJCharP(currentContext, s,-1);
   TCObject charArray = o[0].asObj;

   setObjectLock(o, UNLOCKED);

   ASSERT2_EQUALS(Sz, OBJ_CLASS(o)->name, "java.lang.String");
   ASSERT2_EQUALS(Sz, OBJ_CLASS(charArray)->name, CHAR_ARRAY);
   ASSERT2_EQUALS(I32, ARRAYOBJ_LEN(charArray), 8);
   ASSERT3_EQUALS(Block, ARRAYOBJ_START(charArray), s, 16);

   ASSERT2_EQUALS(I32, String_charsLen(o), 8);
   ASSERT3_EQUALS(Block, String_charsStart(o), s, 16);

   finish: ;
}

static TCObject _newObj()
{
   Chunk c = xmalloc(4+sizeof(TObjectProperties));
   return (TCObject)(c+sizeof(TObjectProperties));
}

TESTCASE(Stack) // #3
{
   Stack s;
   int32 v=0;

   s = newStack(3, sizeof(int), null);

   ASSERT1_EQUALS(NotNull, s);
   IF_HEAP_ERROR(s->heap)
   {
      TEST_FAIL(tc, "Error on Stack");
      stackDestroy(s);
   }
   v = 1; stackPush(s, &v);
   ASSERT1_EQUALS(True, stackPop(s, &v));
   ASSERT2_EQUALS(I32, v, 1);
   ASSERT1_EQUALS(False, stackPop(s, &v));

   v = 1; stackPush(s, &v);
   stackPop(s, &v);
   ASSERT2_EQUALS(I32, v, 1);
   ASSERT1_EQUALS(False, stackPop(s, &v));

   v = 1; stackPush(s, &v);
   v = 2; stackPush(s, &v);
   v = 3; stackPush(s, &v);
   v = 4; stackPush(s, &v);
   v = 5; stackPush(s, &v);
   v = 6; stackPush(s, &v);
   v = 7; stackPush(s, &v);
   v = 8; stackPush(s, &v);
   v = 9; stackPush(s, &v);
   v = 10; stackPush(s, &v);
   v = 11; stackPush(s, &v);
   v = 12; stackPush(s, &v);
   v = 13; stackPush(s, &v);

   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 13);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 12);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 11);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 10);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 9);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 8);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 7);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 6);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 5);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 4);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 3);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 2);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 1);
   ASSERT1_EQUALS(False, stackPop(s, &v));

   v = listGetCount(s->head);
   ASSERT2_EQUALS(I32, v, 5);

   v = 1; stackPush(s, &v);
   v = 2; stackPush(s, &v);
   v = 3; stackPush(s, &v);
   v = 4; stackPush(s, &v);
   v = 5; stackPush(s, &v);
   v = 6; stackPush(s, &v);
   v = 7; stackPush(s, &v);
   v = 8; stackPush(s, &v);
   v = 9; stackPush(s, &v);
   v = 10; stackPush(s, &v);
   v = 11; stackPush(s, &v);
   v = 12; stackPush(s, &v);
   v = 13; stackPush(s, &v);

   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 13);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 12);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 11);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 10);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 9);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 8);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 7);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 6);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 5);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 4);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 3);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 2);
   ASSERT1_EQUALS(True, stackPop(s, &v)); ASSERT2_EQUALS(I32, v, 1);
   ASSERT1_EQUALS(False, stackPop(s, &v));

   v = listGetCount(s->head);
   ASSERT2_EQUALS(I32, v, 5);
finish:
   stackDestroy(s);
}

TESTCASE(DblList) // #4
{
   TCObject o1,o2,o3,o4,o5,head;
   ObjectProperties p1,p2,p3,p4,p5,ph;
   o1 = _newObj();
   o2 = _newObj();
   o3 = _newObj();
   o4 = _newObj();
   o5 = _newObj();
   head = _newObj();
   p1 = OBJ_PROPERTIES(o1);
   p2 = OBJ_PROPERTIES(o2);
   p3 = OBJ_PROPERTIES(o3);
   p4 = OBJ_PROPERTIES(o4);
   p5 = OBJ_PROPERTIES(o5);
   ph = OBJ_PROPERTIES(head);

   insertNodeInDblList(head, o1);
   ASSERT2_EQUALS(Ptr,ph->next,o1);
   ASSERT2_EQUALS(Ptr,p1->prev,head);
   insertNodeInDblList(head, o2);
   ASSERT2_EQUALS(Ptr,ph->next,o2);
   ASSERT2_EQUALS(Ptr,p1->prev,o2);
   ASSERT2_EQUALS(Ptr,p2->next,o1);
   ASSERT2_EQUALS(Ptr,p2->prev,head);
   insertNodeInDblList(head, o3);
   ASSERT2_EQUALS(Ptr,ph->next,o3);
   ASSERT2_EQUALS(Ptr,p2->prev,o3);
   ASSERT2_EQUALS(Ptr,p3->next,o2);
   ASSERT2_EQUALS(Ptr,p3->prev,head);
   insertNodeInDblList(head, o4);
   ASSERT2_EQUALS(Ptr,ph->next,o4);
   ASSERT2_EQUALS(Ptr,p3->prev,o4);
   ASSERT2_EQUALS(Ptr,p4->next,o3);
   ASSERT2_EQUALS(Ptr,p4->prev,head);
   insertNodeInDblList(head, o5);
   ASSERT2_EQUALS(Ptr,ph->next,o5);
   ASSERT2_EQUALS(Ptr,p4->prev,o5);
   ASSERT2_EQUALS(Ptr,p5->next,o4);
   ASSERT2_EQUALS(Ptr,p5->prev,head);
   ASSERT2_EQUALS(Ptr,ph->prev,o1); // last

   removeNodeFromDblList(head,o1);
   ASSERT2_EQUALS(Ptr,p2->next,null);
   ASSERT2_EQUALS(Ptr,ph->prev,o2); // last
   removeNodeFromDblList(head,o2);
   ASSERT2_EQUALS(Ptr,p3->next,null);
   ASSERT2_EQUALS(Ptr,ph->prev,o3); // last
   removeNodeFromDblList(head,o4);
   ASSERT2_EQUALS(Ptr,p5->next,o3);
   ASSERT2_EQUALS(Ptr,p5->prev,head);
   ASSERT2_EQUALS(Ptr,p3->prev,o5);
   ASSERT2_EQUALS(Ptr,ph->prev,o3); // last
   removeNodeFromDblList(head,o5);
   ASSERT2_EQUALS(Ptr,ph->next,o3);
   ASSERT2_EQUALS(Ptr,p3->next,null);
   ASSERT2_EQUALS(Ptr,p3->prev,head);
   ASSERT2_EQUALS(Ptr,ph->prev,o3); // last
   removeNodeFromDblList(head,o3);
   ASSERT2_EQUALS(Ptr,ph->next,null);
   ASSERT2_EQUALS(Ptr,ph->prev,null); // last

finish:
   xfree(p1);
   xfree(p2);
   xfree(p3);
   xfree(p4);
   xfree(p5);
   xfree(ph);
}

static TCObject allocAndFillObj(Context currentContext, uint32 size, uint8 fillWith)
{
   TCObject o = createByteArray(currentContext, size);
   setObjectLock(o, UNLOCKED);
   if (o)
      xmemset(ARRAYOBJ_START(o), fillWith, size);
   return o;
}
// This test will implement the explanation in the top of the file objectmemorytest.c
// MUST COME RIGHT AFTER ArrayFileCreation TEST CASE !!!
TESTCASE(GarbageCollector) // #5
{
   int32 nfree,nused;
   TCObjectArray held = currentContext->regOStart; // objects that will be held (in use) after the GC
   TCObjectArray held0 = held;
   TCObject rel1,rel2,rel3,rel4,rel5,rel13,rel14;         // objects that will be released (ready to be reused later)
   TCObject rel6,rel7,rel8,rel9,rel10,rel11,rel12;        // objects that will be released (ready to be reused later)
   TCObject free1,free2;                                  // objects that will be freed (returned to the system)

   if (!saveRestoreOMM(true))
   {
      alert("Not enough memory to\nrun GarbageCollector\ntest case.\nAborting tests!");
      TEST_ABORT;
   }

   // 0. make sure all the lists are empty
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 1, nfree); // only the first chunk exist.
   ASSERT2_EQUALS(I32, 0, nused);
   // 1. alloc some objects, pushing some objects to the object's stack
   rel1    = allocAndFillObj(currentContext,DEFAULT_CHUNK_SIZE-4-8-sizeof(TObjectProperties),1); // big object 1 - compute the size to let allocate held[0] below
   free1   = allocAndFillObj(currentContext,DEFAULT_CHUNK_SIZE-4,  2); // big object 2 - will have exactly DEFAULT_CHUNK_SIZE bytes. * WILL BE COLLECTED
   *held++ = allocAndFillObj(currentContext,4,3); // this will be allocated in the remaining block of huge object 1
   *held++ = allocAndFillObj(currentContext,4,4);
   rel2    = allocAndFillObj(currentContext,4,5);
   *held++ = allocAndFillObj(currentContext,14,6);
   *held++ = allocAndFillObj(currentContext,14,7);
   *held++ = allocAndFillObj(currentContext,14,8);
   *held++ = allocAndFillObj(currentContext,24,9);
   rel3    = allocAndFillObj(currentContext,24,10);
   rel4    = allocAndFillObj(currentContext,24,11);
   rel5    = allocAndFillObj(currentContext,24,12);
   free2   = allocAndFillObj(currentContext,DEFAULT_CHUNK_SIZE+10,13); // huge object 1 - * WILL BE COLLECTED
   // 1b. check if the objects were created correctly
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(    rel1), ARRAYOBJ_LEN(rel1)    , 1);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(   free1), ARRAYOBJ_LEN(free1)   , 2);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(held0[0]), ARRAYOBJ_LEN(held0[0]), 3);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(held0[1]), ARRAYOBJ_LEN(held0[1]), 4);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(    rel2), ARRAYOBJ_LEN(rel2)    , 5);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(held0[2]), ARRAYOBJ_LEN(held0[2]), 6);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(held0[3]), ARRAYOBJ_LEN(held0[3]), 7);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(held0[4]), ARRAYOBJ_LEN(held0[4]), 8);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(held0[5]), ARRAYOBJ_LEN(held0[5]), 9);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(    rel3), ARRAYOBJ_LEN(rel3)    , 10);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(    rel4), ARRAYOBJ_LEN(rel4)    , 11);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(    rel5), ARRAYOBJ_LEN(rel5)    , 12);
   ASSERT3_EQUALS(Filled, ARRAYOBJ_START(   free2), ARRAYOBJ_LEN(free2)   , 13);
   // 1b. mark the end of pushed objects - the method should do this too.
   currentContext->regO/*Protected */= held;
   // 1c. check if the objects were created in the right places
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 1, nfree); // first chunk still exists
   ASSERT2_EQUALS(I32, 13, nused);
   // 2. run the GC
   gc(currentContext);
   // 2b. check if only the held objects are alive
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 8, nfree); // rel1-rel5 + first chunk - two objects were freed
   ASSERT2_EQUALS(I32, 6, nused); // held objects
   // 3. allocate 12 objects
   held = currentContext->regO/*Protected*/ - 2; // simulate the release of a two held objects. note that these are in the longlive list, so they won't be freed in the next gc
   *held++ = allocAndFillObj(currentContext,34,13);
   *held++ = allocAndFillObj(currentContext,34,14);
   rel6    = allocAndFillObj(currentContext,34,15);
   rel7    = allocAndFillObj(currentContext,34,16);
   *held++ = allocAndFillObj(currentContext,4,17); // take space of rel2
   *held++ = allocAndFillObj(currentContext,4,18);
   rel8    = allocAndFillObj(currentContext,4,19);
   rel9    = allocAndFillObj(currentContext,4,20);
   *held++ = allocAndFillObj(currentContext,16,21);
   *held++ = allocAndFillObj(currentContext,16,22);
   *held++ = allocAndFillObj(currentContext,16,23);
   rel10   = allocAndFillObj(currentContext,16,24);
   // 3b. mark the end of pushed objects
   currentContext->regO/*Protected*/ = held;
   // 3c. check if the objects were created in the right places
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 7, nfree);
   ASSERT2_EQUALS(I32, 6+12, nused); // all objects allocated since the last gc
   // 4. run the GC
   gc(currentContext);
   // 4b. check if only the held objects are alive
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 5-1+5+2+1+2, nfree);
   ASSERT2_EQUALS(I32, 7+6-2, nused);
   // 5. allocate another bunch of objects
   held    = currentContext->regO/*Protected*/ - 1; // simulate the release of 1 held object.
   *held++ = allocAndFillObj(currentContext,34,25); // take space of rel7
   rel11   = allocAndFillObj(currentContext,4,26);  // take place of rel9
   rel12   = allocAndFillObj(currentContext,16,27);
   // 5b. mark the end of pushed objects
   currentContext->regO/*Protected */= held;
   // 5c. check if the objects were created in the right places
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 12-1, nfree);
   ASSERT2_EQUALS(I32, 11+3, nused);
   // 5d. run the GC
   gc(currentContext);
   // 5e. check if only the held objects are alive
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 12+2, nfree);
   ASSERT2_EQUALS(I32, 11, nused);
   // 6. allocate another bunch of objects
   held    = currentContext->regO/*Protected */- 3; // simulate the release of 3 held objects.
   *held++ = allocAndFillObj(currentContext,34,28); // take place of rel6
   rel13   = allocAndFillObj(currentContext,4,29);  // take place of rel8
   rel14   = allocAndFillObj(currentContext,16,30);
   // 6b. mark the end of pushed objects
   currentContext->regO/*Protected */= held;
   // 6c. check if the objects were created in the right places
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 14-3, nfree);
   ASSERT2_EQUALS(I32, 11+3, nused);
   // 6d. run the GC
   gc(currentContext);
   // 6e. check if only the held objects are alive
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 14+2, nfree);
   ASSERT2_EQUALS(I32, 14-3-2, nused);
   // 7a. run the GC again, just to make sure nothing else will be collected
   gc(currentContext);
   // 7b. check if only the held objects are alive
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 16, nfree);
   ASSERT2_EQUALS(I32, 9, nused);
   // 8. release everyone
   currentContext->regO/*Protected*/ = currentContext->regOStart;
   // 8a. run the GC
   gc(currentContext);
   // 8b. check if only the held objects are alive
   nfree = countObjectsIn(freeList,false,false,markedAsUsed);
   nused = countObjectsIn(usedList,false,false,markedAsUsed);
   ASSERT2_EQUALS(I32, 25, nfree);
   ASSERT2_EQUALS(I32, 0, nused);

   finish:
   saveRestoreOMM(false);
}
