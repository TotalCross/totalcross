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
import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestAbstractSequentialList extends TestCase
{
   public void testRun()
   {
      Test15 test1 = new Test15();
      AbstractSequentialList test2 = new Test15();
      AbstractList test3 = new Test15();
      
      assertEquals(null, test1.listIterator());
      assertEquals(null, test2.listIterator());
      assertEquals(null, test3.listIterator());
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      try
      {
         test1.add(0, null);
         fail("1");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.add(0, null);
         fail("2");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.add(0, null);
         fail("3");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         test1.addAll(0, null);
         fail("4");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.addAll(0, null);
         fail("5");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.addAll(0, null);
         fail("6");
      }
      catch (NullPointerException exception) {}

      try
      {
         test1.get(0);
         fail("7");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      try
      {
         test2.get(0);
         fail("8");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      try
      {
         test3.get(0);
         fail("9");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      
      assertEquals(null, test1.iterator());
      assertEquals(null, test2.iterator());
      assertEquals(null, test3.iterator());
      
      try
      {
         test1.remove(0);
         fail("10");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      try
      {
         test2.remove(0);
         fail("11");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      try
      {
         test3.remove(0);
         fail("12");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      
      try
      {
         test1.set(0, null);
         fail("13");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      try
      {
         test2.set(0, null);
         fail("14");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
      try
      {
         test3.set(0, null);
         fail("15");
      }
      catch (NullPointerException exception) 
      {
         assertTrue(Settings.onJavaSE);
      }
      catch (IndexOutOfBoundsException exception)
      {
         assertFalse(Settings.onJavaSE);
      }
   }
}

class Test15 extends AbstractSequentialList
{
   public ListIterator listIterator(int index)
   {
      return null;
   }

   public int size()
   {
      return 0;
   }
}
