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

public class TestDeque extends TestCase
{
   public void testRun()
   {
      Test26 test1 = new Test26();
      Deque test2 = new Test26();
      Queue test3 = new Test26();
      
      assertFalse(test1.isEmpty());
      assertFalse(test2.isEmpty());
      assertFalse(test3.isEmpty());
      
      assertEquals(null, test1.toArray());
      assertEquals(null, test2.toArray());
      assertEquals(null, test3.toArray());
      
      assertEquals(null, test1.toArray(null));
      assertEquals(null, test2.toArray(null));
      assertEquals(null, test3.toArray(null));
      
      assertFalse(test1.containsAll(null));
      assertFalse(test2.containsAll(null));
      assertFalse(test3.containsAll(null));
      
      assertFalse(test1.addAll(null));
      assertFalse(test2.addAll(null));
      assertFalse(test3.addAll(null));
      
      assertFalse(test1.removeAll(null));
      assertFalse(test2.removeAll(null));
      assertFalse(test3.removeAll(null));
      
      assertFalse(test1.retainAll(null));
      assertFalse(test2.retainAll(null));
      assertFalse(test3.retainAll(null));
      
      test1.clear();
      test2.clear();
      test3.clear();
      
      test1.addFirst(null);
      test2.addFirst(null);
      ((Deque)test3).addFirst(null);
      
      test1.addLast(null);
      test2.addLast(null);
      ((Deque)test3).addLast(null);
      
      assertTrue(test1.offerFirst(null));
      assertTrue(test2.offerFirst(null));
      assertTrue(((Deque)test3).offerFirst(null));
      
      assertFalse(test1.offerLast(null));
      assertFalse(test2.offerLast(null));
      assertFalse(((Deque)test3).offerLast(null));
      
      assertEquals(null, test1.removeFirst());
      assertEquals(null, test2.removeFirst());
      assertEquals(null, ((Deque)test3).removeFirst());
      
      assertEquals(null, test1.removeLast());
      assertEquals(null, test2.removeLast());
      assertEquals(null, ((Deque)test3).removeLast());
      
      assertEquals(null, test1.pollFirst());
      assertEquals(null, test2.pollFirst());
      assertEquals(null, ((Deque)test3).pollFirst());
      
      assertEquals(null, test1.pollLast());
      assertEquals(null, test2.pollLast());
      assertEquals(null, ((Deque)test3).pollLast());
      
      assertEquals(null, test1.getFirst());
      assertEquals(null, test2.getFirst());
      assertEquals(null, ((Deque)test3).getFirst());
      
      assertEquals(null, test1.getLast());
      assertEquals(null, test2.getLast());
      assertEquals(null, ((Deque)test3).getLast());
      
      assertEquals(null, test1.peekFirst());
      assertEquals(null, test2.peekFirst());
      assertEquals(null, ((Deque)test3).peekFirst());
      
      assertEquals(null, test1.peekLast());
      assertEquals(null, test2.peekLast());
      assertEquals(null, ((Deque)test3).peekLast());
      
      assertFalse(test1.removeFirstOccurrence(null));
      assertFalse(test2.removeFirstOccurrence(null));
      assertFalse(((Deque)test3).removeFirstOccurrence(null));
      
      assertFalse(test1.removeLastOccurrence(null));
      assertFalse(test2.removeLastOccurrence(null));
      assertFalse(((Deque)test3).removeLastOccurrence(null));
      
      assertTrue(test1.add(null));
      assertTrue(test2.add(null));
      assertTrue(test3.add(null));
      
      assertFalse(test1.offer(null));
      assertFalse(test2.offer(null));
      assertFalse(((Queue)test3).offer(null));
      
      assertEquals(null, test1.remove());
      assertEquals(null, test2.remove());
      assertEquals(null, ((Queue)test3).remove());
      
      assertEquals(null, test1.poll());
      assertEquals(null, test2.poll());
      assertEquals(null, ((Queue)test3).poll());
      
      assertEquals(null, test1.element());
      assertEquals(null, test2.element());
      assertEquals(null, ((Queue)test3).element());
      
      assertEquals(null, test1.peek());
      assertEquals(null, test2.peek());
      assertEquals(null, ((Queue)test3).peek());
      
      test1.push(null);
      test2.push(null);
      ((Deque)test3).push(null);
      
      assertEquals(null, test1.pop());
      assertEquals(null, test2.pop());
      assertEquals(null, ((Deque)test3).pop());
      
      assertFalse(test1.remove(null));
      assertFalse(test2.remove(null));
      assertFalse(test3.remove(null));
      
      assertFalse(test1.contains(null));
      assertFalse(test2.contains(null));
      assertFalse(test3.contains(null));
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      assertEquals(null, test1.iterator());
      assertEquals(null, test2.iterator());
      assertEquals(null, test3.iterator());
      
      assertEquals(null, test1.descendingIterator());
      assertEquals(null, test2.descendingIterator());
      assertEquals(null, ((Deque)test3).descendingIterator());
   }
}

class Test26 implements Deque
{
   public boolean isEmpty()
   {
      return false;
   }

   public Object[] toArray()
   {
      return null;
   }

   public Object[] toArray(Object[] a)
   {
      return null;
   }

   public boolean containsAll(Collection c)
   {
      return false;
   }

   public boolean addAll(Collection c)
   {
      return false;
   }

   public boolean removeAll(Collection c)
   {
      return false;
   }

   public boolean retainAll(Collection c)
   {
      return false;
   }

   public void clear()
   {
   }

   public void addFirst(Object e)
   {
   }

   public void addLast(Object e)
   {
   }

   public boolean offerFirst(Object e)
   {
      return true;
   }

   public boolean offerLast(Object e)
   {
      return false;
   }

   public Object removeFirst()
   {
      return null;
   }

   public Object removeLast()
   {
      return null;
   }

   public Object pollFirst()
   {
      return null;
   }

   public Object pollLast()
   {
      return null;
   }

   public Object getFirst()
   {
      return null;
   }

   public Object getLast()
   {
      return null;
   }

   public Object peekFirst()
   {
      return null;
   }

   public Object peekLast()
   {
      return null;
   }

   public boolean removeFirstOccurrence(Object o)
   {
      return false;
   }

   public boolean removeLastOccurrence(Object o)
   {
      return false;
   }

   public boolean add(Object e)
   {
      return true;
   }

   public boolean offer(Object e)
   {
      return false;
   }

   public Object remove()
   {
      return null;
   }

   public Object poll()
   {
      return null;
   }

   public Object element()
   {
      return null;
   }

   public Object peek()
   {
      return null;
   }

   public void push(Object e)
   {
   }

   public Object pop()
   {
      return null;
   }

   public boolean remove(Object o)
   {
      return false;
   }

   public boolean contains(Object o)
   {
      return false;
   }

   public int size()
   {
      return 0;
   }

   public Iterator iterator()
   {
      return null;
   }

   public Iterator descendingIterator()
   {
      return null;
   }
}