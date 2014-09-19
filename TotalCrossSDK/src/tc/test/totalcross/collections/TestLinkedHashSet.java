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

public class TestLinkedHashSet extends TestCase
{
   public void testRun()
   {
      Test23 test1 = new Test23();
      LinkedHashSet test2 = new Test23(10);
      HashSet test3 = new Test23(test2);
   }
}

class Test23 extends LinkedHashSet
{
   public Test23()
   {
      super();
   }
  
   public Test23(int initialCapacity)
   {
      super(initialCapacity);
   }

   public Test23(Collection c)
   {
      super(c);
   }
}
