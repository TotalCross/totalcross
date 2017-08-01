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

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Iterator;

import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestAbstractSet extends TestCase
{
  @Override
  public void testRun()
  {
    Test16 test1 = new Test16();
    AbstractSet test2 = new Test16();
    AbstractCollection test3 = new Test16();

    assertEquals(null, test1.iterator());
    assertEquals(null, test2.iterator());
    assertEquals(null, test3.iterator());

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertTrue(test1.equals(test1));
    assertTrue(test2.equals(test2));
    assertTrue(test3.equals(test3));
    if (Settings.onJavaSE){
      assertFalse(test1.equals(test2));
    }else {
      assertTrue(test1.equals(test2));
    }
    if (Settings.onJavaSE){
      assertFalse(test2.equals(test3));
    }else {
      assertTrue(test2.equals(test3));
    }
    if (Settings.onJavaSE){
      assertFalse(test3.equals(test1));
    }else {
      assertTrue(test3.equals(test1));
    }

    try
    {
      assertEquals(0, test1.hashCode());
      assertFalse(Settings.onJavaSE);
    }
    catch (NullPointerException exception) 
    {
      assertTrue(Settings.onJavaSE);
    }
    try
    {
      assertEquals(0, test2.hashCode());
      assertFalse(Settings.onJavaSE);
    }
    catch (NullPointerException exception) 
    {
      assertTrue(Settings.onJavaSE);
    }
    try
    {
      assertEquals(0, test3.hashCode());
      assertFalse(Settings.onJavaSE);
    }
    catch (NullPointerException exception) 
    {
      assertTrue(Settings.onJavaSE);
    }

    try
    {
      assertFalse(test1.removeAll(test1));
      assertFalse(Settings.onJavaSE);
    }
    catch (NullPointerException exception) 
    {
      assertTrue(Settings.onJavaSE);
    }
    try
    {
      assertFalse(test2.removeAll(test2));
      assertFalse(Settings.onJavaSE);
    }
    catch (NullPointerException exception) 
    {
      assertTrue(Settings.onJavaSE);
    }
    try
    {
      assertFalse(test3.removeAll(test3));
      assertFalse(Settings.onJavaSE);
    }
    catch (NullPointerException exception) 
    {
      assertTrue(Settings.onJavaSE);
    }
  }
}

class Test16 extends AbstractSet
{
  @Override
  public Iterator iterator()
  {
    return null;
  }

  @Override
  public int size()
  {
    return 0;
  }  
}