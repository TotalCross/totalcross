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

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.unit.TestSuite;

public class APITests extends TestSuite {
  static {
    Settings.resizableWindow = true;
  }

  /** Need to be used within TotalCross */
  public APITests() {
    super("TotalCross API Test Suite");
    Vm.debug(Vm.ALTERNATIVE_DEBUG);
    addTestCase(tc.test.totalcross.sql.sqlite.BackupTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.ConnectionTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.ExtensionTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.FetchSizeTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.InsertQueryTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.PrepStmtTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.QueryTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.RSMetaDataTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.SQLiteJDBCLoaderTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.StatementTest.class);
    addTestCase(tc.test.totalcross.sql.sqlite.TransactionTest.class);

    if (false) {
      addTestCase(tc.test.totalcross.lang.reflect.ArrayTest.class);
      addTestCase(tc.test.totalcross.lang.reflect.FieldTest.class);
      addTestCase(tc.test.totalcross.lang.reflect.MethodTest.class);
      addTestCase(tc.test.totalcross.lang.reflect.ConstructorTest.class);
      addTestCase(tc.test.totalcross.lang.reflect.ClassTest.class);
      addTestCase(tc.test.totalcross.util.VectorTest.class);
      addTestCase(tc.test.totalcross.util.IntVectorTest.class);
      addTestCase(tc.test.totalcross.util.HashtableTest.class);
      addTestCase(tc.test.totalcross.util.IntHashtableTest.class);
      addTestCase(tc.test.totalcross.util.DateTest.class);
      addTestCase(tc.test.totalcross.util.RandomTest.class);
      addTestCase(tc.test.totalcross.ui.gfx.ColorTest.class);
      addTestCase(tc.test.totalcross.ui.gfx.CoordTest.class);
      addTestCase(tc.test.totalcross.ui.font.FontAndFontMetricsTest.class);
      //addTestCase( tc.test.totalcross.ui.image.ImageTest.class ); - very outdated
      addTestCase(tc.test.totalcross.ui.gfx.RectTest.class);
      //addTestCase( tc.test.totalcross.ui.gfx.GraphicsTest.class ); - idem
      addTestCase(tc.test.totalcross.sys.CharacterConverterTest.class);
      addTestCase(tc.test.totalcross.sys.UTF8CharacterConverterTest.class);
      addTestCase(tc.test.totalcross.sys.TimeTest.class);
      addTestCase(tc.test.totalcross.sys.ConvertTest.class);
      addTestCase(tc.test.totalcross.sys.VmTest.class);
      addTestCase(tc.test.totalcross.io.PDBFileTest.class);
    }
  }
}
