/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: ConverterTests.java,v 1.8 2011-01-04 13:19:23 guich Exp $

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
