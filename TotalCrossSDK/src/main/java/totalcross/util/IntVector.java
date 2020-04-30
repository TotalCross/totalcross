// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;

/**
 * An int vector is an array of int's. The vector grows
 * dynamically as ints are added, but it never shrinks.
 * The IntVector can also be used as an IntStack or a bit vector.
 * <p>
 * Here is an example showing a vector being used:
 *
 * <pre>
 * ...
 * IntVector vec = new IntVector();
 * vec.addElement(int1);
 * vec.addElement(int22);
 * ...
 * vec.insertElementAt(int3, 3);
 * vec.removeElementAt(2);
 * if (vec.size() > 5)
 * ...
 * </pre>
 * <br>For efficiency, get and set is made directly through the <items> public array.
 * <b>Please use the add and remove methods to manipulate the IntVector.</b>
 */
public class IntVector {
  /** This member is public for fast access. Always use the correct methods
   * for add and remove, otherwise you'll be in trouble. */
  public int items[];
  protected int count;

  /** Constructs an empty vector. */
  public IntVector() {
    this(20);
  }

  /**
   * Constructs an empty vector with a given initial size. The size is
   * the initial size of the vector's internal int array. The vector
   * will grow as needed when ints are added. SIZE CANNOT BE 0!
   */
  public IntVector(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("The argument 'size' must be non-negative");
    }
    items = new int[size];
  }

  /** Constructs a vector by directly assigning the given int array. Changes on the
   * original array will reflect the items of this array and vice-versa.
   * @since SuperWaba 5.11
   */
  public IntVector(int[] items) // guich@511_11
  {
    count = items.length; // flsobral@tc100b4: fail-fast policy - throws NPE if items is null.
    this.items = items;
  }

  /** Useful method to use when this IntVector will act like a bit vector, through
   * the methods <code>isBitSet</code> and <code>setBit</code>. Just call it
   * with the maximum bit index what will be used (starting from 0); then
   * you can safely use the two methods. This must be done because those methods
   * does not check the bounds of the array.
   */
  public void ensureBit(int index) // guich@400_5
  {
    int newCount = (index >> 5) + 1; // convert from bits to int
    if (newCount >= items.length) {
      if (count == 0) {
        items = new int[newCount];
      } else {
        int newItems[] = new int[newCount];
        Vm.arrayCopy(items, 0, newItems, 0, count);
        items = newItems;
      }
    }
    count = newCount;
  }

  /** Used to let this int vector act like a bit vector.
   * @return true if the bit specified is set. you must guarantee that the index exists in the vector.
   */
  public boolean isBitSet(int index) // guich@400_5
  {
    return (items[index >> 5] & ((int) 1 << (index & 31))) != 0; // guich@321_7
  }

  /** Used to let this int vector act like a bit vector.
   * you must guarantee that the index exists in the vector.
   */
  public void setBit(int index, boolean on) // guich@400_5
  {
    if (on) {
      items[index >> 5] |= ((int) 1 << (index & 31)); // set
    } else {
      items[index >> 5] &= ~((int) 1 << (index & 31)); // reset
    }
  }

  // methods to let the vector act like a stack

  /** Pushes an int. */
  public void push(int obj) {
    if (count < items.length) {
      items[count++] = obj;
    } else {
      insertElementAt(obj, count);
    }
  }

  /** Pops the given number of elements from this vector.
   * @since SuperWaba 5.0
   */
  public void pop(int howMany) // guich@500_4
  {
    this.count -= howMany;
    if (this.count < 0) {
      this.count = 0;
    }
  }

  /** Returns the last int, removing it.
   * @throws totalcross.util.ElementNotFoundException When the stack is empty.
   */
  public int pop() throws ElementNotFoundException {
    if (count > 0) {
      return items[--count];
    }
    throw new ElementNotFoundException("Empty stack");
  }

  /** Returns the last int, without removing it.
   * @throws totalcross.util.ElementNotFoundException When the stack is empty.
   */
  public int peek() throws ElementNotFoundException {
    if (count > 0) {
      return items[count - 1];
    }
    throw new ElementNotFoundException("Empty stack");
  }

  /** Returns if this IntVector is empty.
   * @since TotalCross 1.0.
   */
  public boolean isEmpty() {
    return count == 0;
  }

  /** Returns the number of ints in the vector. */
  public int size() {
    return count;
  }

  /**
   * Finds the index of the given int. The list is searched using a O(n) linear
   * search through all the ints in the vector.
   * @return -1 if the object is not found.
   */
  public int indexOf(int elem) {
    return indexOf(elem, 0);
  }

  /**
   * Finds the index of the given int. The list is searched using a O(n) linear
   * search starting in startIndex up through all the ints in the vector.
   * @return -1 if the object is not found.
   */
  public int indexOf(int elem, int index) {
    int n = count;
    for (int i = index; i < n; i++) {
      if (items[i] == elem) {
        return i;
      }
    }
    return -1;
  }

  /** Deletes the int reference at the given index. */
  public void removeElementAt(int index) {
    if (0 <= index && index < count) // guich@566_33
    {
      if (index != count - 1) {
        Vm.arrayCopy(items, index + 1, items, index, count - index - 1);
      }
      items[count - 1] = 0;
      count--;
    }
  }

  /** Inserts the object at the given index. If index is less than 0 or above the number of elements, it is inserted at the end. */
  public void insertElementAt(int obj, int index) {
    if (index < 0 || index > count) {
      index = count; // guich@200b3: check if index is valid
    }
    if (count == items.length) {
      // On device, grows 20% + 1. On Java, grows 100% + 1.
      int newSize = (Settings.onJavaSE ? items.length * 2 : items.length * 12 / 10) + 1; // flsobral@tc110_5: new size is >= current size + 1. - guich@tc112_6: +1 in both cases
      int newItems[] = new int[newSize];
      Vm.arrayCopy(items, 0, newItems, 0, count);
      items = newItems;
    }
    if (index != count) {
      Vm.arrayCopy(items, index, items, index + 1, count - index);
    }
    items[index] = obj;
    count++;
  }

  /** Adds an int to the end of the vector. */
  public void addElement(int obj) {
    if (count < items.length) {
      items[count++] = obj;
    } else {
      insertElementAt(obj, count);
    }
  }

  /**
   * Appends an array of integers at the end of this vector.
   * 
   * @param elements
   *           array of integers to be added to this vector.
   * @since TotalCross 1.2
   */
  public void addElements(int[] elements) // flsobral@tc120_34: new method to add array of integers.
  {
    int newSize = count + elements.length;
    if (items.length < newSize) {
      int newItems[] = new int[newSize];
      Vm.arrayCopy(items, 0, newItems, 0, count);
      items = newItems;
    }
    Vm.arrayCopy(elements, 0, items, count, elements.length);
    count += elements.length;
  }

  /** Deletes the given integer from this vector. */
  public void removeElement(int obj) {
    removeElementAt(indexOf(obj, 0));
  }

  /** Sets all elements in this vector to 0 and its size to 0. */
  public void removeAllElements() {
    Convert.fill(items, 0, count, 0);
    count = 0;
  }

  /** Does a quick sort in the elements of this IntVector */
  public void qsort() {
    qsort(0, count - 1);
  }

  private void qsort(int first, int last) {
    int[] items = this.items;
    int low = first;
    int high = last;
    if (first >= last) {
      return;
    }
    int mid = items[(first + last) >> 1];
    while (true) {
      while (high >= low && items[low] < mid) {
        low++;
      }
      while (high >= low && items[high] > mid) {
        high--;
      }
      if (low <= high) {
        int temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }
    if (first < high) {
      qsort(first, high);
    }
    if (low < last) {
      qsort(low, last);
    }
  }

  /** Returns a new array with the added elements
   * @since SuperWaba 5.54
   */
  public int[] toIntArray() // guich@554_34
  {
    int[] a = new int[count];
    Vm.arrayCopy(items, 0, a, 0, count);
    return a;
  }

  /**
   * Sets the size of this vector. If the new size is greater than the current size, new items are added to the end of
   * the vector. If the new size is less than the current size, all components at index newSize and greater are
   * discarded.
   * 
   * @param newSize
   *           the new size of this vector
   * @throws ArrayIndexOutOfBoundsException
   *            if the new size is negative
   * @since TotalCross 1.0
   */
  public void setSize(int newSize) throws ArrayIndexOutOfBoundsException //flsobral@tc112_11: removed boolean argument "copyOldData". The original content is always kept now.
  {
    if (newSize < 0) {
      throw new ArrayIndexOutOfBoundsException("Argument 'newSize' must be positive.");
    }

    if (newSize > items.length) // grow buffer to reach newSize
    {
      int newItems[] = new int[((int) (newSize * 1.2) + 1)];
      Vm.arrayCopy(items, 0, newItems, 0, count);
      items = newItems;
    }
    count = newSize;
  }

  /** Copies the items of this vector into the given array, which must have at least the current size of this vector.
   * If the out vector is greater than the current size, the remaining positions will remain unchanged.
   * @since TotalCross 1.0 beta 4
   */
  public void copyInto(int[] out) {
    Vm.arrayCopy(items, 0, out, 0, count);
  }

  /**
   * Returns true if this vector contains the specified element.
   * 
   * @param v
   *           element whose presence in this vector is to be tested
   * @return true if this vector contains the specified element
   * @since TotalCross 1.15
   */
  public boolean contains(int v) {
    return indexOf(v) >= 0;
  }

  /**
   * Reverses the order of the elements in this vector.<br>
   * In a vector with n elements, the element of index 0 is moved to the index n-1, the element of index 1 is moved to
   * the index n-2, and so on.
   * 
   * @since TotalCross 1.15
   */
  public void reverse() // guich@tc115_70
  {
    for (int i = 0, j = count - 1; i < j; i++, j--) {
      int temp = items[i];
      items[i] = items[j];
      items[j] = temp;
    }
  }
}