/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.util;

import totalcross.sys.*;

/**
 * A vector is an array of object references. The vector grows
 * dynamically as objects are added, but it never shrinks.
 * The Vector class can also be used as a Stack class.
 * <p>
 * Here is an example showing a vector being used:
 *
 * <pre>
 * ...
 * Vector vec = new Vector();
 * vec.addElement(obj1);
 * vec.addElement(obj2);
 * ...
 * vec.insertElementAt(obj3, 3);
 * vec.removeElementAt(2);
 * if (vec.size() > 5)
 * ...
 * </pre>
 * This Vector class does not support Generics; use the ArrayList class instead.
 */
public class Vector
{
   /** This member is public for fast access. Always use the correct methods
     * for add and remove, otherwise you'll be in trouble. */
   public Object items[];
   protected int count;

   /** Constructs an empty vector. */
   public Vector()
   {
      this(8);
   }

   /**
   * Constructs an empty vector with a given initial size, which is
   * the initial size of the vector's internal object array. The vector
   * will grow as needed when objects are added.
   */
   public Vector(int size)
   {
      if (size < 0) // flsobral@tc100b4_21: throw exception if size has invalid value. - guich@tc100_19: allow size=0
         throw new IllegalArgumentException("The argument 'size' must be non-negative");
      items = new Object[size];
   }

   /**
    * Constructs a vector starting with the given elements. The vector can grow after this. Note that the given array
    * must not have null elements.
    * 
    * @param startingWith
    */
   public Vector(Object[] startingWith) // guich@200b4_31
   {
      count = startingWith.length; // flsobral@tc100b4_21: fail-fast policy - throws NPE if startingWith is null.
      items = startingWith;
   }

   /** Converts the vector to an array of objects.
   * If there are no elements in this vector, returns null.
   * Note that if the elements are Strings, you can cast the result to a String[] array.
   */
   public Object []toObjectArray()
   {
      if (count == 0) return null; // guich@200b2
      Object objs[];
      if (items[0] instanceof String) // guich@220_32
         objs = new String[count];
      else
         objs = new Object[count];
      if (count > 0)  // guich
         Vm.arrayCopy(items, 0, objs, 0, count);
      return objs;
   }

   /** Pushes an object. */ // guich@102
   public void push(Object obj)
   {
      if (count < items.length)
         items[count++] = obj;
      else
         insertElementAt(obj, count);
   }

   /** Returns the last object, removing it.
    * @throws totalcross.util.ElementNotFoundException When the stack is empty.
    */ // guich@102
   public Object pop() throws ElementNotFoundException
   {
      if (count > 0)
      {
         Object o = items[--count];
         items[count] = null; // let gc do their work
         return o;
      }
      throw new ElementNotFoundException("Empty stack");
   }

   /** Returns the last object, without removing it.
    * @throws totalcross.util.ElementNotFoundException When the stack is empty.
    */ // guich@102
   public Object peek() throws ElementNotFoundException
   {
      if (count > 0)
         return items[count-1];
      throw new ElementNotFoundException("Empty stack");
   }

   /** Returns the n-last object, without removing it.
    * Note that <code>peek(0)</code> is the same of <code>peek()</code>.
    * @param n How many elements to get from the top; must be a positive number.
    * @throws totalcross.util.ElementNotFoundException When the stack is empty.
    */ // guich@102
   public Object peek(int n) throws ElementNotFoundException
   {
      if (n < 0)
         throw new IllegalArgumentException("Argument 'n' must be positive.");
      if (count > 0)
         return items[count-1-n];
      throw new ElementNotFoundException("Empty stack");
   }

   /** Pops n last elements from the stack.
    */
   public void pop(int n)
   {
      if (count >= n)
         count -= n;
      else
         throw new IllegalArgumentException();
   }

   /** Returns if this Vector is empty.
    * @since TotalCross 1.0.
    */
   public boolean isEmpty()
   {
      return count == 0;
   }

   /** Returns the number of objects in the vector. */
   public int size()
   {
      return count;
   }

   /**
    * Sets the size of this vector. If the new size is greater than the current size, new null items are added to the
    * end of the vector. If the new size is less than the current size, all components at index newSize and greater are
    * discarded.
    * 
    * @param newSize
    *           the new size of this vector
    * @throws ArrayIndexOutOfBoundsException
    *            if the new size is negative
    * @since TotalCross 1.0 beta 5
    */
   public void setSize(int newSize) throws ArrayIndexOutOfBoundsException // flsobral@tc112: Throw AIOOBE instead of IllegalArgument to match the Java implementation.
   {
      if (newSize < 0)
         throw new ArrayIndexOutOfBoundsException("Argument 'newSize' must be positive.");

      if (newSize < count) // truncating
         Convert.fill(items, newSize, items.length, null);
      else // growing
      {
//         while (newSize > items.length) 
            grow(newSize); // grow buffer to reach newSize
      }
      count = newSize;
   }

   /**
    * Finds the index of the given object. The list is searched using a O(n) linear
    * search through all the objects in the vector.
    */
   public int indexOf(Object elem)
   {
	   return indexOf(elem, 0);
   }

   /**
    * Finds the index of the given object. The list is searched using a O(n) linear
    * search starting in startIndex up through all the objects in the vector.
    */
   public int indexOf(Object obj, int startIndex)
   {
      if (obj != null)
      {
         Object []its = items; // guich@560_13: cache to speedup performance
         int n = count;
         for (int i = startIndex; i < n; i++)
            if (its[i] != null && its[i].equals(obj)) //flsobral@tc114_93: changed Vector.indexOf(Object, int) to perform the comparison using the Vector's elements equals method, instead of the opposite.
               return i;
      }
      return -1;
   }

   /** Deletes the object reference at the given index. */
   public void removeElementAt(int index)
   {
      if (0 <= index && index < count) // guich@566_33
      {
         count--;
         if (index != count)
            Vm.arrayCopy(items, index + 1, items, index, count - index);
         items[count] = null;
      }
   }

   /** Inserts the object at the given index. If index is less than 0 or above the number of elements, it is inserted at the end. */
   public void insertElementAt(Object obj, int index)
   {
      if (index < 0 || index > count) index = count; // guich@200b3: check if index is valid
      if (count == items.length)
         grow(items.length + 1);
      if (index != count)
         Vm.arrayCopy(items, index, items, index + 1, count - index);
      items[index] = obj;
      count++;
   }

   /** Adds an object to the end of the vector. */
   public void addElement(Object obj)
   {
      if (count < items.length)
         items[count++] = obj;
      else
         insertElementAt(obj, count);
   }

   /**
    * Adds an array of objects at the end of the vector.
    * 
    * @param objects
    *           array with the objects to be added to the vector.
    * @since TotalCross 1.13
    */
   public void addElements(Object[] objects) // flsobral@tc113_39: new method to add array of objects.
   {
      int newSize = count + objects.length;
      if (items.length < newSize)
         grow(newSize);
      Vm.arrayCopy(objects, 0, items, count, objects.length); //flsobral@tc120_33: inserting elements one position higher than it should.
      count += objects.length;
   }

   /**
    * Adds an array of objects at the end of the vector 
    * (null objects are skipped).
    * 
    * @param objects
    *           array with the objects to be added to the vector.
    * @since TotalCross 1.24
    */
   public void addElementsNotNull(Object[] objects) // guich@tc124_8
   {
      int newSize = count + objects.length;
      if (items.length < newSize)
         grow(newSize);
      for (int i = 0; i < objects.length; i++)
         if (objects[i] != null)
            items[count++] = objects[i];
   }

   /** Deletes the object. */
   public boolean removeElement(Object obj)
   {
      int i = indexOf(obj,0);
      if (i >= 0)
      {
         removeElementAt(i);
         return true;
      }
      return false;
   }

   /** Clears all elements in this vector and sets the count to 0. Note that this method sets all items in this vector to <code>null</code>,
    * so, if you had directly assigned an array to this vector, all items inside it will be nulled. */
   public void removeAllElements()
   {
      Convert.fill(items, 0, count, null);
      count = 0;
   }

   /** Sorts the elements of this Vector. If they are Strings,
     the sort will be much faster because a cast to String is done;
     if they are not strings, the toString() method is used to return
     the string that will be used for comparison. */
   public void qsort() // flsobral@tc100b4_22: changed return type to void.
   {
      if (count > 0)
         Convert.qsort(items, 0, count-1);
   }

   /** Dumps the contents of this vector and returns a string of it.
    * If the number of elements is big, it can take a lot of memory!
    */
   public String dump()
   {
      StringBuffer sb = new StringBuffer(1024);
      sb.append(super.toString()).append('\n');
      sb.append("Number of elements: ").append(count).append('\n');
      for (int i =0; i < count; i++)
         sb.append('[').append(i).append("] = ").append(items[i]).append('\n');
      return sb.toString();
   }

   /** Copies the items of this vector into the given array, which must have at least the current size of this vector.
    * If the out vector is greater than the current size, the remaining positions will remain unchanged.
    * @since TotalCross 1.0 beta 4
    */
   public void copyInto(Object[] out)
   {
      Vm.arrayCopy(items, 0, out, 0, count);
   }

   /**
    * Grows the internal buffer.
    * 
    * @param ensureSize
    *           minimum size for the new internal buffer
    */
   private void grow(int ensureSize)
   {
      if (ensureSize > 0 && items.length < ensureSize)
      {
         // On device, grows 20% + 1. On Java, grows 100% + 1.
         int newSize = (Settings.onJavaSE ? ensureSize*2 : ensureSize*12/10) + 1; // flsobral@tc110_5: new size is >= current size + 1.- guich@tc112_6: +1 in both cases
         Object newItems[] = new Object[newSize];
         Vm.arrayCopy(items, 0, newItems, 0, count);
         items = newItems;
      }
   }
   
   /** Returns the items of this vector separated by comma
    * @since TotalCross 1.13 
    */
   public String toString()
   {
      return super.toString()+" size: "+count+", items: "+toString(",");
   }
   
   
   /** Returns the items of this vector separated by the given string.
    * @since TotalCross 1.13 
    */
   public String toString(String separator)
   {
      StringBuffer sb = new StringBuffer(10*count);
      for (int i = 0, n = count; i < n; i++)
         if (items[i] != null)
            sb.append(items[i].toString()).append(separator);
      if (sb.length() > 0) // cut the last separator
         sb.setLength(sb.length()-separator.length());
      return sb.toString();
   }
   
   /**
    * Returns true if this vector contains the specified element. 
    * 
    * @param o
    *           element whose presence in this vector is to be tested
    * @return true if this vector contains the specified element
    * @since TotalCross 1.15
    */
   public boolean contains(Object o)
   {
      return indexOf(o) >= 0;
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
      for (int i = 0, j = count - 1; i < j; i++, j--)
      {
         Object temp = items[i];
         items[i] = items[j];
         items[j] = temp;
      }
   }
   
   public Object elementAt(int i)
   {
      return items[i];
   }
}