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

public class TestBitSet extends TestCase
{
   public void testRun()
   {
      Test19 test1 = new Test19();
      BitSet test2 = new Test19(64);
   }
}

class Test19 extends BitSet
{
   public Test19()
   {
      super();
   }
   
   public Test19(int nbits)
   {
      super(nbits);
   }
}
