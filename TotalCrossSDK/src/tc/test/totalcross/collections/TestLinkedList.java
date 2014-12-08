/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.collections;

import java.util.*;
import totalcross.unit.TestCase;

public class TestLinkedList extends TestCase
{
   public void testRun()
   {
      Test24 test1 = new Test24();
      LinkedList test2 = new Test24(test1);
      AbstractSequentialList test3 = new Test24(test2);
      
      try
      {
         test1.getFirst();
         fail("1");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         test2.getFirst();
         fail("2");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         ((LinkedList)test3).getFirst();
         fail("3");
      } 
      catch (NoSuchElementException exception) {}
      
      try
      {
         test1.getLast();
         fail("4");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         test2.getLast();
         fail("5");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         ((LinkedList)test3).getLast();
         fail("6");
      } 
      catch (NoSuchElementException exception) {}
      
      try
      {
         test1.removeFirst();
         fail("7");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         test2.removeFirst();
         fail("8");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         ((LinkedList)test3).removeFirst();
         fail("9");
      } 
      catch (NoSuchElementException exception) {}
      
      try
      {
         test1.removeLast();
         fail("10");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         test2.removeLast();
         fail("11");
      } 
      catch (NoSuchElementException exception) {}
      try
      {
         ((LinkedList)test3).removeLast();
         fail("12");
      } 
      catch (NoSuchElementException exception) {}
      
      test1.addFirst(null);
      test2.addFirst(null);
      ((LinkedList)test3).addFirst(null);
      
      test1.addLast(null);
      test2.addLast(null);
      ((LinkedList)test3).addLast(null);
      
      assertTrue(test1.contains(null));
      assertTrue(test2.contains(null));
      assertTrue(test3.contains(null));
      
      assertEquals(2, test1.size());
      assertEquals(2, test2.size());
      assertEquals(2, test3.size());
      
      assertTrue(test1.add(null));
      assertTrue(test2.add(null));
      assertTrue(test3.add(null));
      
      assertTrue(test1.remove(null));
      assertTrue(test2.remove(null));
      assertTrue(test3.remove(null));
      
      assertTrue(test1.addAll(test1));
      assertTrue(test2.addAll(test2));
      assertTrue(test3.addAll(test3));
      
      assertTrue(test1.addAll(0, test1));
      assertTrue(test2.addAll(1, test2));
      assertTrue(test3.addAll(2, test3));
      
      test1.clear();
      test2.clear();
      test3.clear();
      
      try
      {
         test1.get(0);
         fail("13");
      }
      catch (IndexOutOfBoundsException exception) {}
      try
      {
         test2.get(0);
         fail("14");
      }
      catch (IndexOutOfBoundsException exception) {}
      try
      {
         test3.get(0);
         fail("15");
      }
      catch (IndexOutOfBoundsException exception) {}
      
      try
      {
         test1.set(0, null);
         fail("16");
      }
      catch (IndexOutOfBoundsException exception) {}
      try
      {
         test2.set(0, null);
         fail("17");
      }
      catch (IndexOutOfBoundsException exception) {}
      try
      {
         test3.set(0, null);
         fail("18");
      }
      catch (IndexOutOfBoundsException exception) {}

      test1.add(0, null);
      test2.add(0, null);
      test3.add(0, null);
         
      assertEquals(null, test1.remove(0));
      assertEquals(null, test2.remove(0));
      assertEquals(null, test3.remove(0));
      
      assertEquals(-1, test1.indexOf(null));
      assertEquals(-1, test2.indexOf(null));
      assertEquals(-1, test3.indexOf(null));
   
      assertEquals(-1, test1.lastIndexOf(null));
      assertEquals(-1, test2.lastIndexOf(null));
      assertEquals(-1, test3.lastIndexOf(null));
      
      assertTrue(test1.listIterator(0) instanceof ListIterator);
      assertTrue(test2.listIterator(0) instanceof ListIterator);
      assertTrue(test3.listIterator(0) instanceof ListIterator);
      
      assertEquals(0, test1.toArray().length);
      assertEquals(0, test2.toArray().length);
      assertEquals(0, test3.toArray().length);
      
      Object[] array = new Object[0];
      assertEquals(0, test1.toArray(array).length);
      assertEquals(0, test2.toArray(array).length);
      assertEquals(0, test3.toArray(array).length);
      
      assertTrue(test1.offer(null));
      assertTrue(test2.offer(null));
      assertTrue(((LinkedList)test3).offer(null));
      
      assertEquals(null, test1.element());
      assertEquals(null, test2.element());
      assertEquals(null, ((LinkedList)test3).element());
      
      assertEquals(null, test1.peek());
      assertEquals(null, test2.peek());
      assertEquals(null, ((LinkedList)test3).peek());
      
      assertEquals(null, test1.poll());
      assertEquals(null, test2.poll());
      assertEquals(null, ((LinkedList)test3).poll());
      
      try
      {
         test1.remove();
         fail("19");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         test2.remove();
         fail("20");
      }
      catch (NoSuchElementException exception) {} 
      try
      {
         ((LinkedList)test3).remove();
         fail("21");
      }
      catch (NoSuchElementException exception) {} 
     
      Iterator iterator1 = test1.descendingIterator();
      Iterator iterator2 = test2.descendingIterator();
      Iterator iterator3 = ((LinkedList)test3).descendingIterator();
      
      assertFalse(iterator1.hasNext());
      assertFalse(iterator2.hasNext());
      assertFalse(iterator3.hasNext());
      
      try
      {
         iterator1.next();
         fail("22");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         iterator2.next();
         fail("23");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         iterator3.next();
         fail("24");
      }
      catch (NoSuchElementException exception) {}
      
      try
      {
         iterator1.remove();
         fail("25");
      }
      catch (IllegalStateException exception) {}
      try
      {
         iterator2.remove();
         fail("26");
      }
      catch (IllegalStateException exception) {}
      try
      {
         iterator3.remove();
         fail("27");
      }
      catch (IllegalStateException exception) {}
      
      assertTrue(test1.offerFirst(null));
      assertTrue(test2.offerFirst(null));
      assertTrue(((LinkedList)test3).offerFirst(null));
      
      assertTrue(test1.offerLast(null));
      assertTrue(test2.offerLast(null));
      assertTrue(((LinkedList)test3).offerLast(null));
      
      assertEquals(null, test1.peek());
      assertEquals(null, test2.peek());
      assertEquals(null, ((LinkedList)test3).peek());
      
      assertEquals(null, test1.peekLast());
      assertEquals(null, test2.peekLast());
      assertEquals(null, ((LinkedList)test3).peekLast());
      
      assertEquals(null, test1.pollFirst());
      assertEquals(null, test2.pollFirst());
      assertEquals(null, ((LinkedList)test3).pollFirst());
      
      assertEquals(null, test1.pollLast());
      assertEquals(null, test2.pollLast());
      assertEquals(null, ((LinkedList)test3).pollLast());
      
      try
      {
         test1.pop();
         fail("28");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         test2.pop();
         fail("29");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         ((LinkedList)test3).pop();
         fail("30");
      }
      catch (NoSuchElementException exception) {}
      
      test1.push(null);
      test2.push(null);
      ((LinkedList)test3).push(null);
      
      assertTrue(test1.removeFirstOccurrence(null));
      assertTrue(test2.removeFirstOccurrence(null));
      assertTrue(((LinkedList)test3).removeFirstOccurrence(null));
   
      assertFalse(test1.removeLastOccurrence(null));
      assertFalse(test2.removeLastOccurrence(null));
      assertFalse(((LinkedList)test3).removeLastOccurrence(null));
   
      assertEquals(test1, test1.clone());
      assertEquals(test2, test2.clone());
      assertEquals(test3, ((LinkedList)test3).clone());
   }
}

class Test24 extends LinkedList implements Cloneable
{
   public Test24()
   {
      super();
   }
   
   public Test24(Collection c)
   {
      super(c);
   }
   
   public Object clone()
   {
      return super.clone();     
   }
}
