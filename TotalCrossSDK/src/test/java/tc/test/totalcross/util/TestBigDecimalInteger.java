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


package tc.test.totalcross.util;

import totalcross.unit.TestSuite;

public class TestBigDecimalInteger extends TestSuite
{

  public TestBigDecimalInteger()
  {
    super("BigInteger and BigDecimal Test Suite");
    addTestCase(TestBigInteger.class); // juliana@210_2: now Litebase supports tables with ascii strings.
    addTestCase(TestBigDecimal.class);
  }

}
