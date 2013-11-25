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



package tc.test.totalcross;

import totalcross.unit.*;

public class APITests extends TestSuite
{
   /** Need to be used within TotalCross */
   public APITests()
   {
      super("TotalCross API Test Suite");
      addTestCase( tc.test.totalcross.sql.sqlite.SQLiteTests.class );

      if (false) 
      {
      addTestCase( tc.test.totalcross.util.VectorTest.class );
      addTestCase( tc.test.totalcross.util.IntVectorTest.class );
      addTestCase( tc.test.totalcross.util.HashtableTest.class );
      addTestCase( tc.test.totalcross.util.IntHashtableTest.class );
      addTestCase( tc.test.totalcross.util.DateTest.class );
      addTestCase( tc.test.totalcross.util.RandomTest.class );
      addTestCase( tc.test.totalcross.ui.gfx.ColorTest.class );
      addTestCase( tc.test.totalcross.ui.gfx.CoordTest.class );
      addTestCase( tc.test.totalcross.ui.font.FontAndFontMetricsTest.class );
      //addTestCase( tc.test.totalcross.ui.image.ImageTest.class ); - very outdated
      addTestCase( tc.test.totalcross.ui.gfx.RectTest.class );
      //addTestCase( tc.test.totalcross.ui.gfx.GraphicsTest.class ); - idem
      addTestCase( tc.test.totalcross.sys.CharacterConverterTest.class );
      addTestCase( tc.test.totalcross.sys.UTF8CharacterConverterTest.class );
      addTestCase( tc.test.totalcross.sys.TimeTest.class );
      addTestCase( tc.test.totalcross.sys.ConvertTest.class );
      addTestCase( tc.test.totalcross.sys.VmTest.class );
      addTestCase( tc.test.totalcross.io.PDBFileTest.class );
      }
   }
}
