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

/*import tc.tools.converter.*;
import tc.tools.converter.info.*;
import tc.tools.converter.tclass.*;
import tc.test.converter.util.*;
import totalcross.io.*;
import totalcross.util.zip.*;
 */
import totalcross.unit.TestCase;

public class InfoTest extends TestCase {
  @Override
  public void testRun() {
    /*      GlobalConstantPool.init();
      // get the bytes from the string
      ByteArrayStream uncompressed = new ByteArrayStream(512);
      ImageTester.hex2bytes(LzmaTest.TestTypes_uncompressed, uncompressed);
    
      // load the tclass
      TCClass tc = null;
      try
      {
         tc = InfoStorage.loadTClass(new DataStream(uncompressed));
      }
      catch (ZLibException le)
      {
         fail(le.getMessage());
      }
      catch (StorageException e)
      {
         fail(e.getMessage());
      }
    
      // save the tclass
      ByteArrayStream saved = new ByteArrayStream(512);
      try
      {
         InfoStorage.storeClass(tc, saved);
      }
      catch (StorageException e)
      {
         fail(e.getMessage());
      }
    
      byte[] u = uncompressed.toByteArray();
      byte[] s = saved.toByteArray();
      assertEquals(u, s);
     */ }
}
