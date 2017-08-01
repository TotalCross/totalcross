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



package tc.test.converter.util;

import tc.tools.converter.Storage;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.unit.ImageTester;
import totalcross.unit.TestCase;

public class LzmaTest extends TestCase
{
  private static final boolean REGEN = false;
  public static final String TestTypes_compressed = "950100000031FFFC80766BA6F48D987E646717D11F300A158685F8ED25E5924E5B493D6B79FDEE287104030A39545B0854C85F046D566D525CB40F54FD455E9A7912682869609192F2D6FA4D44ECFEB2BE8C825F2D6EAF1E8C54470254EF68E7526C535F5F6CCC1328010F837E6EC71224833F23B46D314904D53B608E8880CF4AEA7D70FF1FE4E0EEED38F2DB25FC15EF91F7B195BED476180D66785430A8AC1224691E87D5B9684528103CD95A2DAEB3C26BCC57BDD5E5738565AF23DFDF374D9D24BE7301CBC3AC460ECF436DF1189B0704F4D00946985C8C014ABD374ED0BBB206718AC3879562C52CC2D665951EC194F5E058B679B5E36A2EC1FCEC3C22608EADD2489B531B877FE86B8D064DF67E363FEC824200";
  public static final String TestTypes_uncompressed = "6400040822000B000000040008000200000000000000030000000500030002000200030016000B0064000000D859000078563412410000007856341278563412ADFA5C6D454A934054E3A59B444A93C00D00560065007200610020004E0061007200640065006C006C0069000754657374457874095465737454797065730162017301690163016C0164016F02616202617302616902616302616402616C02616F09737562737472696E6702736902736402736C02736F09676574446F75626C65057072696E74040A000F1006000600022100031002210007100221000610022100051002210010100221001310022100111002210012100222001410010C0200011402100118031001100410012E07100A000100010108100D00010109100F0001010A10100001010B100E0001010C10130001010D10110001010E10140001000610011C05100900000001000000000000000000000014100900A0000200010000000300000000000000000100001700000012000100AB0200009900000049000000000000000000000000000001151000000A00";
  @Override
  public void testRun()
  {
    // get the bytes from the string
    ByteArrayStream compressed = new ByteArrayStream(512);
    ByteArrayStream uncompressed = new ByteArrayStream(512);
    ImageTester.hex2bytes(TestTypes_compressed, compressed);
    ImageTester.hex2bytes(TestTypes_uncompressed, uncompressed);
    ByteArrayStream saved = new ByteArrayStream(512);
    ByteArrayStream loaded = new ByteArrayStream(512);
    // decompress it
    if (REGEN){
      try {uncompressed.skipBytes(uncompressed.available()); Storage.compressAndWrite(uncompressed, new DataStream(saved)); dumpBas(saved); System.exit(0);} catch (Exception e) {e.printStackTrace();}
    }
    try
    {
      Storage.readAndDecompress(new DataStream(compressed), loaded, -1);
    }
    catch (Exception e)
    {
      fail(e.getMessage());
    }
    byte[] compr = compressed.toByteArray();
    uncompressed.skipBytes(uncompressed.available()); // hex2bytes calls "mark"
    byte[] uncompr = uncompressed.toByteArray();

    assertEquals(loaded.available(), 405);
    loaded.skipBytes(405); // must skip bc compressAndWrite calls "mark()"
    if (false){
      dumpBas(loaded);
    }
    assertEquals(uncompr, loaded.toByteArray());

    // compress it
    try
    {
      Storage.compressAndWrite(loaded, new DataStream(saved));
    }
    catch (Exception e)
    {
      fail(e.getMessage());
    }

    // now compare the buffers
    assertEquals(compr, saved.toByteArray());
  }
  private void dumpBas(ByteArrayStream bas)
  {
    int n = bas.getPos();
    if (n == 0){
      n = bas.available();
    }
    byte[] buf = bas.getBuffer();
    for (int i =0; i < n; i++) {
      System.out.print(totalcross.sys.Convert.unsigned2hex(buf[i],2));
    }
    System.out.println("\n=========");
  }

}
