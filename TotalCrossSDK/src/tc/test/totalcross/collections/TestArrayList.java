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

public class TestArrayList extends TestCase
{
   public void testRun()
   {
      Test18 test1 = new Test18();
      ArrayList test2 = new Test18();
      AbstractList test3 = new Test18();
      List test4 = new Test18();
      
      test1.trimToSize();
      test2.trimToSize();
      ((ArrayList)test3).trimToSize();
      ((ArrayList)test4).trimToSize();
      
      test1.ensureCapacity(0);
      test2.ensureCapacity(1);
      ((ArrayList)test3).ensureCapacity(2);
      ((ArrayList)test4).ensureCapacity(3);
   }
   
}

class Test18 extends ArrayList {}
