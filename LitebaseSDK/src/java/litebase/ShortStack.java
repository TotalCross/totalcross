/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package litebase;

import totalcross.sys.*;
import totalcross.util.*;

/**
 * A short stack is an array of shorts's. The stack grows dynamically as short's are added, but it never shrinks.
 */
class ShortStack
{
   /** 
    * The items of the stack 
    */
   private short[] items;
   
   /**
    * The current number of elements of the stack.
    */
   private int count;

   /**
    * Constructs an empty stack with a given initial size. The size is the initial size of the vector's internal short array. The vector will grow 
    * as needed when values are added. 
    *
    * @param size The size of the new stack.
    */
   ShortStack(int size)
   {
      items = new short[size];
   }
   
   /** 
    * Pushes a short. Increases the array if necessary.
    * 
    * @param value The value to be pushed.
    */
   public void push(short value)
   {
      if (count >= items.length)
      {
         // On device, grows 20% + 1. On Java, grows 100% + 1.
         // flsobral@tc110_5: new size is >= current size + 1. - guich@tc112_6: +1 in both cases.
         int newSize = (Settings.onJavaSE? items.length << 1 : items.length * 12 / 10) + 1; 
         short[] newItems = new short[newSize];
         Vm.arrayCopy(items, 0, newItems, 0, count);
         items = newItems;
      }
      items[count++] = value;
   }

   /** 
    * Pops the given number of elements from this stack.
    *
    * @param howMany The number of elements to be popped of the stack.
    */
   public void pop(int howMany) 
   {
      count -= howMany;
      if (count < 0)
         count = 0;
   }

   /** 
    * Returns the last short, removing it.
    *
    * @returns the last inserted short.
    * @throws totalcross.util.ElementNotFoundException When the stack is empty.
    */
   public int pop() throws ElementNotFoundException
   {
      if (count > 0)
         return items[--count];
      throw new ElementNotFoundException("");
   }

   /** 
    * Returns a new array with the added elements.
    * 
    * @return An array of short with all the elements.
    */
   public short[] toShortArray() // guich@554_34
   {
      short[] a = new short[count];
      Vm.arrayCopy(items, 0, a, 0, count);
      return a;
   }
}
