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

import java.util.Iterator;
import totalcross.unit.TestCase;

public class TestIterator extends TestCase
{
   public void testRun()
   {
      Test4 test1 = new Test4();
      Iterator test2 = new Test4();
      
      assertTrue(test1.hasNext());
      assertTrue(test2.hasNext());
      
      assertTrue(test1.next() instanceof Object);
      assertTrue(test2.next() instanceof Object);
   }
}

class Test4 implements Iterator
{
   public boolean hasNext()
   {
      return this instanceof Iterator;
   }

   public Object next()
   {
      return new Object();
   }
   
   public void remove()
   {
   }
}
