/* PriorityQueue.java -- Unbounded priority queue
   Copyright (C) 2004, 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package totalcross.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;
import totalcross.sys.Vm;

/**
 * @author Tom Tromey (tromey@redhat.com)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @since 1.5
 */
public class PriorityQueue4D<E> extends AbstractQueue4D<E> {
  private static final int DEFAULT_CAPACITY = 11;

  /** Number of elements actually used in the storage array.  */
  int used;

  /**
   * This is the storage for the underlying binomial heap.
   * The idea is, each node is less than or equal to its children.
   * A node at index N (0-based) has two direct children, at
   * nodes 2N+1 and 2N+2.
   */
  E[] storage;

  /**
   * The comparator we're using, or null for natural ordering.
   */
  Comparator<? super E> comparator;

  public PriorityQueue4D() {
    this(DEFAULT_CAPACITY, null);
  }

  public PriorityQueue4D(Collection<? extends E> c) {
    this(Math.max(1, (int) (1.1 * c.size())), null);

    // Special case where we can find the comparator to use.
    if (c instanceof SortedSet) {
      SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
      this.comparator = (Comparator<? super E>) ss.comparator();
      // We can insert the elements directly, since they are sorted.
      int i = 0;
      for (E val : ss) {
        if (val == null) {
          throw new NullPointerException();
        }
        storage[i++] = val;
      }
    } else if (c instanceof PriorityQueue) {
      PriorityQueue4D<? extends E> pq = (PriorityQueue4D<? extends E>) c;
      this.comparator = (Comparator<? super E>) pq.comparator();
      // We can just copy the contents.
      Vm.arrayCopy(pq.storage, 0, storage, 0, pq.storage.length);
    }

    addAll(c);
  }

  public PriorityQueue4D(int cap) {
    this(cap, null);
  }

  public PriorityQueue4D(int cap, Comparator<? super E> comp) {
    this.used = 0;
    this.storage = (E[]) new Object[cap];
    this.comparator = comp;
  }

  public PriorityQueue4D(PriorityQueue4D<? extends E> c) {
    this(Math.max(1, (int) (1.1 * c.size())), (Comparator<? super E>) c.comparator());
    // We can just copy the contents.
    Vm.arrayCopy(c.storage, 0, storage, 0, Math.min(c.storage.length, storage.length));
  }

  public PriorityQueue4D(SortedSet<? extends E> c) {
    this(Math.max(1, (int) (1.1 * c.size())), (Comparator<? super E>) c.comparator());
    // We can insert the elements directly, since they are sorted.
    int i = 0;
    for (E val : c) {
      if (val == null) {
        throw new NullPointerException();
      }
      storage[i++] = val;
    }
  }

  @Override
  public void clear() {
    Arrays.fill(storage, null);
    used = 0;
  }

  public Comparator<? super E> comparator() {
    return comparator;
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      int index = -1;
      int count = 0;

      @Override
      public boolean hasNext() {
        return count < used;
      }

      @Override
      public E next() {
        while (storage[++index] == null) {
          ;
        }

        ++count;
        return storage[index];
      }

      @Override
      public void remove() {
        PriorityQueue4D.this.remove(index);
      }
    };
  }

  @Override
  public boolean offer(E o) {
    if (o == null) {
      throw new NullPointerException();
    }

    int slot = findSlot(-1);

    storage[slot] = o;
    ++used;
    bubbleUp(slot);

    return true;
  }

  @Override
  public E peek() {
    return used == 0 ? null : storage[0];
  }

  @Override
  public E poll() {
    if (used == 0) {
      return null;
    }
    E result = storage[0];
    remove(0);
    return result;
  }

  @Override
  public boolean remove(Object o) {
    if (o != null) {
      for (int i = 0; i < storage.length; ++i) {
        if (o.equals(storage[i])) {
          remove(i);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int size() {
    return used;
  }

  // It is more efficient to implement this locally -- less searching
  // for free slots.
  @Override
  public boolean addAll(Collection<? extends E> c) {
    if (c == this) {
      throw new IllegalArgumentException();
    }

    int newSlot = -1;
    int save = used;
    for (E val : c) {
      if (val == null) {
        throw new NullPointerException();
      }
      newSlot = findSlot(newSlot);
      storage[newSlot] = val;
      ++used;
      bubbleUp(newSlot);
    }

    return save != used;
  }

  int findSlot(int start) {
    int slot;
    if (used == storage.length) {
      resize();
      slot = used;
    } else {
      for (slot = start + 1; slot < storage.length; ++slot) {
        if (storage[slot] == null) {
          break;
        }
      }
      // We'll always find a slot.
    }
    return slot;
  }

  void remove(int index) {
    // Remove the element at INDEX.  We do this by finding the least
    // child and moving it into place, then iterating until we reach
    // the bottom of the tree.
    while (storage[index] != null) {
      int child = 2 * index + 1;

      // See if we went off the end.
      if (child >= storage.length) {
        storage[index] = null;
        break;
      }

      // Find which child we want to promote.  If one is not null,
      // we pick it.  If both are null, it doesn't matter, we're
      // about to leave.  If neither is null, pick the lesser.
      if (child + 1 >= storage.length || storage[child + 1] == null) {
        // Nothing.
      } else if (storage[child] == null
          || (Collections4D.compare(storage[child], storage[child + 1], comparator) > 0)) {
        ++child;
      }
      storage[index] = storage[child];
      index = child;
    }
    --used;
  }

  void bubbleUp(int index) {
    // The element at INDEX was inserted into a blank spot.  Now move
    // it up the tree to its natural resting place.
    while (index > 0) {
      // This works regardless of whether we're at 2N+1 or 2N+2.
      int parent = (index - 1) / 2;
      if (Collections4D.compare(storage[parent], storage[index], comparator) <= 0) {
        // Parent is the same or smaller than this element, so the
        // invariant is preserved.  Note that if the new element
        // is smaller than the parent, then it is necessarily
        // smaller than the parent's other child.
        break;
      }

      E temp = storage[index];
      storage[index] = storage[parent];
      storage[parent] = temp;

      index = parent;
    }
  }

  void resize() {
    E[] new_data = (E[]) new Object[2 * storage.length];
    Vm.arrayCopy(storage, 0, new_data, 0, storage.length);
    storage = new_data;
  }
}
