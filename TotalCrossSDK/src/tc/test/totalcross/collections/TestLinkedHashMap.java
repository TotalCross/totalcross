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

public class TestLinkedHashMap extends TestCase
{
   public void testRun()
   {
      Test22 test1 = new Test22();
      LinkedHashMap test2 = new Test22(test1);
      HashMap test3 = new Test22(10);
      
      test1.clear();
      test2.clear();
      test3.clear();
      
      assertFalse(test1.containsValue(null));
      assertFalse(test2.containsValue(null));
      assertFalse(test3.containsValue(null));
      
      assertEquals(null, test1.get(null));
      assertEquals(null, test2.get(null));
      assertEquals(null, test3.get(null));
      
      
   }
}

class Test22 extends LinkedHashMap
{
   public Test22()
   {
      super();
   }
   
   public Test22(Map m)
   {
      super(m);
   }
   
   public Test22(int initialCapacity)
   {
      super(initialCapacity);
   }
}
