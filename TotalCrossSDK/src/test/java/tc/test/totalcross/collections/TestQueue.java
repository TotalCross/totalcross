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

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import totalcross.unit.TestCase;

public class TestQueue extends TestCase
{
  @Override
  public void testRun()
  {
    Test25 test1 = new Test25();
    Queue test2 = new Test25();
    Collection test3 = new Test25();

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertFalse(test1.isEmpty());
    assertFalse(test2.isEmpty());
    assertFalse(test3.isEmpty());

    assertFalse(test1.contains(null));
    assertFalse(test2.contains(null));
    assertFalse(test3.contains(null));

    assertEquals(null, test1.iterator());
    assertEquals(null, test2.iterator());
    assertEquals(null, test3.iterator());

    assertEquals(null, test1.toArray());
    assertEquals(null, test2.toArray());
    assertEquals(null, test3.toArray());

    assertEquals(null, test1.toArray(null));
    assertEquals(null, test2.toArray(null));
    assertEquals(null, test3.toArray(null));

    assertFalse(test1.remove(null));
    assertFalse(test2.remove(null));
    assertFalse(test3.remove(null));

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

    assertFalse(test1.add(null));
    assertFalse(test2.add(null));
    assertFalse(test3.add(null));

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
  } 
}

class Test25 implements Queue
{
  @Override
  public int size()
  {
    return 0;
  }

  @Override
  public boolean isEmpty()
  {
    return false;
  }

  @Override
  public boolean contains(Object o)
  {
    return false;
  }

  @Override
  public Iterator iterator()
  {
    return null;
  }

  @Override
  public Object[] toArray()
  {
    return null;
  }

  @Override
  public Object[] toArray(Object[] a)
  {
    return null;
  }

  @Override
  public boolean remove(Object o)
  {
    return false;
  }

  @Override
  public boolean containsAll(Collection c)
  {
    return false;
  }

  @Override
  public boolean addAll(Collection c)
  {
    return false;
  }

  @Override
  public boolean removeAll(Collection c)
  {
    return false;
  }

  @Override
  public boolean retainAll(Collection c)
  {
    return false;
  }

  @Override
  public void clear()
  {
  }

  @Override
  public boolean add(Object e)
  {
    return false;
  }

  @Override
  public boolean offer(Object e)
  {
    return false;
  }

  @Override
  public Object remove()
  {
    return null;
  }

  @Override
  public Object poll()
  {
    return null;
  }

  @Override
  public Object element()
  {
    return null;
  }

  @Override
  public Object peek()
  {
    return null;
  }
}
