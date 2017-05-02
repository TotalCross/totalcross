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



package tc.test.totalcross.util;

import totalcross.unit.*;
import totalcross.util.*;

public class IntVectorTest extends TestCase
{
   private void IntVector_Int()
   {
      IntVector v = new IntVector(10);
      assertEquals(10, v.items.length);
   }
   private void IntVector_Obj()
   {
      int []s = {1,2};
      IntVector v = new IntVector(s);
      assertTrue(v.items == s);
      v.addElement(3); // force the creation of a new array
      assertFalse(v.items == s);
   }
   private void insert(/*int index, Object obj*/)
   {
      IntVector v = new IntVector(3);
      v.addElement(1);
      v.addElement(3);
      v.insertElementAt(2,1);
      assertEquals(3,v.size());
      assertEquals(v.items[1],2);
   }
   private void del_Int(/*int index*/)
   {
      IntVector v = new IntVector(3);
      v.addElement(1);
      v.addElement(2);
      v.removeElementAt(0);
      v.removeElementAt(1); // invalid index
      assertEquals(1, v.size());
      v.removeElementAt(0);
      assertEquals(0, v.size());
   }
   private void /*int*/ find(/*Object obj, int startIndex*/)
   {
      IntVector v = new IntVector(3);
      v.addElement(1);
      v.addElement(2);
      assertEquals(-1, v.indexOf(1,1));
      assertEquals(-1, v.indexOf(3,0));
      assertEquals(1, v.indexOf(2,1));
   }
   private void /*Object []*/toObjectArray()
   {
      IntVector v = new IntVector(3);
      v.addElement(1);
      v.addElement(2);
      v.addElement(3);
      v.addElement(4);
      v.addElement(5);
      v.addElement(6);
      assertNotEquals(6,v.items.length); // space for 7 items
      int []o = v.toIntArray();
      assertEquals(6, o.length);
   }
   private void push_pop_peek(/*Object obj*/)
   {
      IntVector v = new IntVector(5);
      v.push(1);
      v.push(2);
      v.push(3);
      assertEquals(3,v.size());
      try {assertEquals(3,v.peek());} catch (ElementNotFoundException e) {fail();}
      assertEquals(3,v.size()); // make sure that peek does not remove the element
      try {assertEquals(3,v.pop());} catch (ElementNotFoundException e) {fail();}
      try {assertEquals(2,v.pop());} catch (ElementNotFoundException e) {fail();}
      v.pop(2); // remove the last two
      try {v.pop();  fail(); } catch (ElementNotFoundException e) {}
      try {v.peek(); fail(); } catch (ElementNotFoundException e) {}
   }
   private void clear()
   {
      IntVector v = new IntVector(3);
      v.addElement(1);
      v.addElement(2);
      assertEquals(2,v.size());
      v.removeAllElements();
      assertEquals(0,v.size());
      assertEquals(0,v.items[0]);
   }
   private void /*boolean*/ qsort()
   {
      IntVector v = new IntVector(3);
      v.addElement(2);
      v.addElement(1);
      v.qsort();
      assertEquals(1,v.items[0]);
      assertEquals(2,v.items[1]);
   }

   private void bitTest()
   {
      IntVector v = new IntVector(1);
      v.ensureBit(33);
      assertEquals(2,v.items.length); // two ints are needed to store 33 bits, 0 - 32
      v.setBit(7,true);
      v.setBit(8,true);
      v.setBit(31,true);
      v.setBit(32,true);
      assertTrue(v.isBitSet(7));
      assertTrue(v.isBitSet(8));
      assertTrue(v.isBitSet(31));
      assertTrue(v.isBitSet(32));
      assertFalse(v.isBitSet(0));
      assertFalse(v.isBitSet(6));
      assertFalse(v.isBitSet(9));
      assertFalse(v.isBitSet(30));
      assertFalse(v.isBitSet(33));
   }

   private void dataStream()
   {
/*      IntVector v = new IntVector(3);
      v.addElement(1);
      v.addElement(2);
      v.addElement(3);
      v.addElement(4);
      v.addElement(5);
      v.addElement(6);
      ByteArrayStream bas = new ByteArrayStream(1000);
      DataStream ds = new DataStream(bas);
      v.writeTo(ds);

      bas.reset();
      IntVector v2 = new IntVector(ds);
      assertEquals(6,v2.size());
      assertEquals(1,v.items[0]);
      assertEquals(2,v.items[1]);
      assertEquals(3,v.items[2]);
      assertEquals(4,v.items[3]);
      assertEquals(5,v.items[4]);
      assertEquals(6,v.items[5]);
*/   }

   public void testRun()
   {
      IntVector_Int();
      IntVector_Obj();
      insert();
      del_Int();
      find();
      toObjectArray();
      push_pop_peek();
      clear();
      qsort();
      bitTest();
      dataStream();
   }
}
