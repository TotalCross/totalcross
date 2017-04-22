/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.test.converter;

import totalcross.unit.*;

public class ConverterTests extends TestSuite
{
   /** Need to be used within TotalCross */
   public ConverterTests()
   {
      super("Converter Test Suite");
      addTestCase( tc.test.converter.TCCodeTest.class );
      addTestCase( tc.test.converter.util.LzmaTest.class );
      addTestCase( tc.test.converter.InfoTest.class );
      addTestCase( tc.test.converter.Bytecode2TCCodeTest.class );
   }
}
