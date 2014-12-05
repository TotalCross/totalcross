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

public class TestEnumMap extends TestCase
{
   public void testRun()
   {
      Test34 test1 = new Test34(TestEnum.class);
      AbstractMap test2 = new Test34(test1);
      EnumMap test3 = new Test34(test2);
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      assertFalse(test1.containsValue(1));
      assertFalse(test2.containsValue(2));
      assertFalse(test3.containsValue(3));
      
      assertFalse(test1.containsKey(TestEnum.One));
      assertFalse(test2.containsKey(TestEnum.Two));
      assertFalse(test3.containsKey(TestEnum.Three));
      
      
   }
}

class Test34 extends EnumMap
{
   public Test34(Class keyType)
   {
      super(keyType);
   }
   public Test34(EnumMap map)
   {
      super(map);
   }
   public Test34(Map map)
   {
      super(map);
   }
}

enum TestEnum
{
   One,
   Two,
   Three;
}
