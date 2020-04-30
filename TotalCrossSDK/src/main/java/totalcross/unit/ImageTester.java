// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.unit;

import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.zip.ZLib;

/** This class is a helper to test if two images are equal, ie,
 * their width and height are the satest and their pixel values
 * are the satest.
 * There's first a learning stage where the images are generated
 * and the compressed strings are created.
 * Then the test should run again to do the effective comparision
 * at a pda.
 * <p>Here's a sample: <pre>
 * public class GraphicsTest extends TestCase
 * {
 *    private Graphics g;
 *    private boolean learning = false; // change this to true to generate the strings
 *
 *    // generated string during learning stage
 *    private String drawLine = "58C3AD94510EC2300CC5DEFD0FEA73C0186C0335892791DF7956DB240F6485CCB571027CCAC80C6E2E32822F1599C0DD4406F02D223DF8F190163C34A4034F0B69C08B84D4E0D5414AF04BD17C4ACDA5BA16D5997EFFA778FF4C5C9613B4E0B21A5DDA2EFCC527CFD7DE17F77EDCED87ECAF9C17397FF33C73633FE4BEC9FD957920F345E695CC3F99A7329F0DB683AA1EE9F8D157";
 *
 *    private ImageTester testDrawLine()
 *    {
 *       int w = 40, h = 40;
 *       ImageTester it = new ImageTester(w,h);
 *       Graphics g = it.g;
 *       g.backColor = Color.WHITE;
 *       g.fillRect(0,0,w,h);
 *       g.drawLine(0,0,w,h);
 *       g.drawLine(0,0,w,0);
 *       g.drawLine(0,0,0,h);
 *       g.drawLine(w-1,0,w-1,h);
 *       g.drawLine(0,h-1,w,h-1);
 *       g.drawLine(w,0,0,h);
 *       g.drawLine(5,10,5,10); // a single pixel
 *       g.drawLine(5,15,6,16); // two pixels
 *       return it;
 *    }
 *
 *    public void testRun()
 *    {
 *       g = MainWindow.getMainWindow().getGraphics();
 *
 *       test(testDrawLine(), drawLine, "drawLine");
 *    }
 *
 *    private void test(ImageTester it, String compressedResults, String title)
 *    {
 *       if (learning)
 *       {
 *          g.drawImage(it,0,0);
 *          Vm.debug("private String "+title+" = \""+it.toString()+"\";");
 *       }
 *       else
 *          assertEquals(it, compressedResults);
 *    }
 * }
 * </pre>
 *
 */

public class ImageTester extends Image // guich@565_7
{
  public Graphics g;
  public String title;
  public String name;

  private byte[] bytes;
  private static int[] rgbs;
  private static StringBuffer sbuf = new StringBuffer(10000);
  private static ByteArrayStream normal = new ByteArrayStream(65000);
  private static byte[] normalBytes = normal.getBuffer();
  private static ByteArrayStream compressed = new ByteArrayStream(65000);
  private static byte[] compressedBytes = compressed.getBuffer();

  public ImageTester(int width, int height) throws ImageException {
    super(width, height);
    create();
  }

  public ImageTester(String name) throws ImageException, IOException {
    super(name);
    this.name = name;
    create();
  }

  private void create() {
    bytes = new byte[getNrBytes()];
    g = getGraphics();
  }

  private int getNrBytes() {
    int bpp = Settings.screenBPP;
    int div = 32 / bpp;
    return ((width + div - 1) / div) * div;
  }

  /** This testthod is the reason for the creation of this class.
   * It compares two images byte per byte. */
  @Override
  public boolean equals(Object originalObj) {
    if (originalObj instanceof ImageTester) {
      ImageTester original = (ImageTester) originalObj;
      if (this.width != original.getWidth() || this.height != original.getHeight()) {
        return false;
      }

      byte[] testb = bytes;
      byte[] originalb = original.bytes;

      if (testb.length != originalb.length) {
        return false;
      }

      int n = testb.length;
      int h = height;

      for (int j = 0; j < h; j++) {
        super.getPixelRow(testb, j);
        original.getPixelRow(originalb, j);
        for (int i = 0; i < n; i++) {
          if (testb[i] != originalb[i]) {
            return false;
          }
        }
      }
      return true;
    }
    if (originalObj instanceof String) // created with the toString below?
    {
      normal.reset();
      hex2bytes((String) originalObj, compressed);
      int whoriginal = 0;
      try {
        whoriginal = ZLib.inflate(compressed, normal);
      } catch (IOException e) {
        e.printStackTrace(); // not the best, better let the user handle it!
        return false;
      }
      int whtest = bytes.length * height;
      if (whoriginal != whtest) {
        return false;
      }

      byte[] testbytes = bytes;
      byte[] originalbytes = normalBytes;
      int[] palme = rgbs;

      int n = testbytes.length;
      int ofs = 0;
      int h = height;
      int test = 0, orig = 0, i = 0, j;
      int w = width;

      int skip = n - width;

      for (j = 0; j < h; j++) {
        super.getPixelRow(testbytes, j);
        for (i = 0; i < w; i++) {
          orig = palme[originalbytes[ofs++] & 0xFF];
          test = palme[testbytes[i] & 0xFF];
          if (test != orig) {
            new CompareBox(title, rgbs, orig, test, i, j, n, originalbytes, this).popup();
            TestCase.output(title + " failed");
            MainWindow.getMainWindow().repaintNow();
            return false;
          }
        }
        ofs += skip;
      }
      return true;
    }
    return super.equals(originalObj);
  }

  public static void hex2bytes(String str, ByteArrayStream bas) {
    byte[] in = str.getBytes();
    int size = in.length >> 1;
    byte[] out = bas.getBuffer();
    int ofs = 0;
    for (int i = 0; i < size; i++) {
      out[i] = (byte) (Convert.digitOf((char) in[ofs++], 16) << 4 | Convert.digitOf((char) in[ofs++], 16));
    }
    bas.reset();
    bas.skipBytes(size);
    bas.mark();
  }

  /** Converts this image to String, as a string of hexadecimal numbers. */
  @Override
  public String toString() {
    StringBuffer sb = sbuf;
    sb.setLength(0);
    byte[] b = bytes;
    int n = b.length;
    compressed.reset();
    normal.reset();
    int h = height;
    for (int j = 0; j < h; j++) {
      super.getPixelRow(b, j);
      normal.writeBytes(b, 0, n);
    }
    normal.mark();
    int len = 0;
    try {
      len = ZLib.deflate(normal, compressed, 9);
    } catch (IOException e) {
      e.printStackTrace(); // not the best, better let the user handle it!
      return sb.toString();
    }
    byte[] out = compressedBytes;
    for (int i = 0; i < len; i++) {
      sb.append(Convert.unsigned2hex(out[i], 2));
    }
    return sb.toString();
  }
}
