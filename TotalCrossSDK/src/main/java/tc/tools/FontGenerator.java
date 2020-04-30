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
/*
         String fontName = "SansSerif";
         String tit = "__ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678890`~!@#$%^&*()_+=-{}\\][:;\"'<,>./?�������";
         Label l;
         ScrollContainer sc = new ScrollContainer();
         add(sc,LEFT,TOP,FILL,FILL);
         for (int i = 10; i < 80; i++)
         {
            // how only the fonts that match this name.
            Font ff = Font.getFont(fontName, false, i);
            if (ff.name.equals(fontName))
            {
               l = new Label(i+" "+tit);
               l.setFont(ff);
               sc.add(l,LEFT,AFTER);
            }
            ff = Font.getFont(fontName, true, i);
            if (ff.name.equals(fontName))
            {
               l = new Label(i+" "+tit);
               l.setFont(ff);
               sc.add(l,LEFT,AFTER);
            }
         }
         sc.resize();

 */

package tc.tools;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import totalcross.io.ByteArrayStream;
import totalcross.io.DataStreamLE;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.util.IntVector;
import totalcross.util.Vector;
import totalcross.util.zip.TCZ;
import totalcross.util.zip.ZLib;

/** Converts a Windows true type font to a pdb file that can be used by TotalCross programs
 * (and also by other programs)
 * Must be compiled with JDK 1.2.2 or above.
 */

public class FontGenerator {
  public static byte detailed = 0; // 1 show messages, 2 show msgs + chars

  class Range {
    int s, e;
    String name;

    Range(int ss, int ee, String nn) {
      if (ss > ee) {
        throw new IllegalArgumentException("End must be after start");
      }
      s = ss;
      e = ee;
      name = nn;
    }

    @Override
    public String toString() {
      return "Unicode range " + name + ": " + s + " - " + e;
    }
  }

  String fontName;
  PalmFont pf;
  int antialiased;
  java.awt.Component comp;
  static IntVector sizes = new IntVector(30);
  boolean skipBigChars;

  public FontGenerator(String fontName, String[] extraArgs) throws Exception {
    int i;
    String sizesArg = null;
    comp = new java.awt.Frame();
    comp.addNotify();
    List<Range> newRanges = new ArrayList<>();
    newRanges.add(new Range(32, 255, "u0")); // default range
    String jdkversion = System.getProperty("java.version");
    if (jdkversion.startsWith("1.1.") || jdkversion.startsWith("1.2.")) {
      throw new Exception("This program requires JDK version greater or equal than 1.3!");
    }
    String outName = fontName; // guich@401_11
    boolean noBold = false;
    boolean isMono = false;
    for (i = 1; i < extraArgs.length; i++) {
      if (extraArgs[i] != null) {
        String arg = extraArgs[i];
        String argLow = arg.toLowerCase();
        if (argLow.indexOf("monospace") >= 0) {
          isMono = true;
        } else if (argLow.equals("/skipbigchars")) {
          skipBigChars = true;
        } else if (argLow.equals("/nobold")) {
          noBold = true;
        } else if (argLow.startsWith("/sizes")) {
          sizesArg = argLow.substring(argLow.indexOf(':') + 1);
        } else if (argLow.equals("/aa")) {
          antialiased = AA_8BPP; // always force 8bpp
        } else if (argLow.equals("/aa8")) {
          antialiased = AA_8BPP;
        } else if (argLow.startsWith("/rename:")) {
          outName = arg.substring(8);
          if (outName.indexOf('.') >= 0) {
            throw new Exception("Invalid rename parameter: " + outName);
          }
        } else if (argLow.startsWith("/detailed:")) {
          detailed = (byte) (arg.charAt(10) - '0');
          if (detailed != 1 && detailed != 2) {
            println("Invalid detailed value. Resetting to 0");
            detailed = 0;
          }
        } else if (argLow.startsWith("/u")) {
          newRanges = new ArrayList<>();
          try {
            while (++i < extraArgs.length) {
              final String[] tokens = extraArgs[i].split("[^0-9]");
              newRanges.add(new Range(Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1]), null));
            }
          } catch (RuntimeException e) {
            throw new Exception("Invalid range '" + extraArgs[i] + "' is not in the format 'start-end'", e);
          }
        } else {
          throw new Exception("Invalid argument: " + extraArgs[i]);
        }
      }
    }

    totalcross.sys.Settings.showDesktopMessages = false;
    // parse parameters
    if (fontName.indexOf('_') != -1) {
      fontName = fontName.replace('_', ' ');
    }
    this.fontName = fontName;
    String realName;
    if (!(realName = new java.awt.Font(fontName, java.awt.Font.PLAIN, 14).getFontName()).toLowerCase()
        .startsWith(fontName.toLowerCase())) {
      println("Font " + fontName + " not found. Was replaced by " + realName
          + ". Run the program without arguments to see the list of possible fonts.");
      System.exit(1);
    }
    // create fonts
    println("FontGenerator - Copyright (c) SuperWaba 2002-2015. Processing...");
    if (sizesArg != null) {
      String[] ss = totalcross.sys.Convert.tokenizeString(sizesArg, ',');
      for (i = 0; i < ss.length; i++) {
        sizes.addElement(totalcross.sys.Convert.toInt(ss[i]));
      }
      sizes.qsort();
    } else if (antialiased == AA_NO) {
      for (i = 7; i <= 60; i++) {
        sizes.addElement(i);
      }
    } else {
      sizes.addElements(new int[] { 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 40, 60, 80 });
    }
    Vector v = new Vector(30);

    for (i = 0; i < sizes.size(); i++) {
      final int s = sizes.items[i];
      final Font f = getFont(fontName, noBold ? java.awt.Font.PLAIN : java.awt.Font.BOLD, s);

      newRanges = processRanges(f, newRanges);
      convertFont(v, f, outName + "$p" + s, newRanges, isMono);
      if (!noBold) {
        convertFont(v, f, outName + "$b" + s, newRanges, isMono);
      }
    }

    // write the file
    try (File f = new File(outName)) {
      if (f.exists()) {
        try {
          f.delete();
        } catch (FileNotFoundException e) {
        }
      }
    }
    new TCZ(v, outName, (short) 0);
    System.out.println("\nFile " + outName + ".tcz created.");
  }

  private Font getFont(String name, int bold, int s) {
    int destSize = s;
    while (s > 0) {
      Font f = new java.awt.Font(name, bold, s);
      java.awt.FontMetrics fm = comp.getFontMetrics(f);
      if (fm.getHeight() <= destSize) {
        return f;
      }
      s--;
    }
    return null;
  }

  private void convertFont(Vector v, java.awt.Font f, String fileName, List<Range> newRanges, boolean isMono) {
    java.awt.FontMetrics fm = comp.getFontMetrics(f);
    final int width = fm.charWidth('@') * 4;
    final int height = fm.getHeight() * 4;

    java.awt.Image img = comp.createImage(width, height);
    java.awt.Graphics2D g = (java.awt.Graphics2D) img.getGraphics();
    try {
      // must disable graphic drawing antialias to be able to turn on text antialiasing. This is a bug with the JVM. 
      g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
      g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, antialiased != AA_NO
          ? java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON : java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    } catch (Throwable t) {
      println("Antialiased font not supported!");
      System.exit(2);
    }
    g.setFont(f);

    // Note: Sun's JDK does not return a optimal width value for the
    // characters, notabily chars W,V,T,A. Because of this, we need to
    // find the width ourselves.
    // Note2: due to this, monospaced fonts may not be converted properly
    int widths[] = new int[65536];
    byte gaps[] = new byte[65536];
    short sum = 0;
    int totalBits = 0;
    int maxW = 0;
    int maxH = 0;
    int i;
    int wW = 0;

    // first, we have to compute the sizes for all ranges
    if (detailed == 0) {
      System.out.println("Computing bounds for " + f);
    }
    int x = width >> 2;
    int n = newRanges.size();
    char back = 8;
    String backs = "" + back + back + back + back + back + back;
    for (int ri = 0; ri < n; ri++) {
      Range rr = newRanges.get(ri);
      if (detailed == 0) {
        System.out.print(backs + ((ri + 1) * 100 / n) + "% ");
      }
      int ini = rr.s;
      int end = rr.e;
      for (i = ini; i <= end; i++) {
        int ch = i;
        final String characterAsString = new String(new int[] { ch }, 0, 1);
        g.setColor(java.awt.Color.white);
        g.fillRect(0, 0, width, height);
        g.setColor(java.awt.Color.black);
        if (ch > 255 && !f.canDisplay(ch)) // guich@tc115_69
        {
          System.out.println("Warning! The true type font cannot display the character number " + i
              + ". Character will be replaced by space.");
          ch = ' ';
        }
        g.drawString(characterAsString, x, height >> 2);

        totalcross.ui.gfx.Rect r = computeBounds(getPixels(img, width, height), width);

        if (r == null) // blank char?
        {
          if (detailed == 1 && i == ch) {
            println("char " + ch + " is blank");
          }
          widths[i] = fm.charWidth(ch);
        } else {
          int w = r.width;

          // guich@tc126_44: skip chars above normal
          if (skipBigChars) {
            if (wW == 0 && ch == 'W') {
              wW = w;
            } else if (wW > 0 && w > wW) {
              println("Skipped char " + ch + " " + characterAsString + ": " + w);
              continue;
            }
          }

          int h = r.height;
          int gap = x - r.x;
          maxW = Math.max(maxW, w);
          maxH = Math.max(maxH, h);
          gaps[i] = (byte) gap;//<=0 ? 1 : 0; // most chars Java puts 1 pixel away; some chars Java puts one pixel near; for those chars, we make them one pixel right
          widths[i] = (w + fm.charWidth(ch)) / 2;
          if (detailed == 1) {
            println("Width of " + characterAsString + " = " + widths[i] + " - gap: " + gap);
          }
        }
      }
    }
    if (isMono) // guich@tc113_31
    {
      for (i = 0; i < widths.length; i++) {
        widths[i] = maxW;
      }
      println("Setting all widths to " + maxW);
    }

    maxH = fm.getHeight();
    int nullCount = 0;
    if (detailed == 0) {
      System.out.println(backs + "Saving glyphs");
    }
    // now, for each range, compute the totalbits and the chars
    for (int ri = 0; ri < n; ri++) {
      Range rr = newRanges.get(ri);
      int ini = rr.s;
      int end = rr.e;
      totalBits = 0;
      for (i = ini; i <= end; i++) {
        totalBits += widths[i];
      }

      // Create the PalmFont and set its parameters
      println((ri + 1) + " of " + n + ". Original height: " + fm.getHeight() + ", ascent: " + fm.getMaxAscent()
          + ", descent: " + fm.getMaxDescent() + ", leading: " + fm.getLeading());
      pf = new PalmFont(fileName + rr.name);
      pf.antialiased = antialiased;
      pf.firstChar = ini;
      pf.lastChar = end;
      pf.spaceWidth = isMono ? maxW : fm.charWidth(' '); // guich@tc115_87
      pf.maxWidth = maxW;
      pf.maxHeight = fm.getHeight();
      pf.descent = fm.getMaxDescent();
      pf.ascent = (maxH - pf.descent);
      pf.rowWords = ((totalBits + 15) / 16);
      pf.rowWords = (((int) (pf.rowWords + 1) / 2) * 2); // guich@400_67
      pf.owTLoc = (pf.rowWords * pf.maxHeight + (pf.lastChar - pf.firstChar) + 8);
      if (detailed >= 1) {
        pf.debugParams();
        println("totalBits: " + totalBits + ", rowWords: " + pf.rowWords);
      }

      pf.initTables();

      // fill in PalmFont tables
      sum = 0;
      for (i = ini; i <= end; i++) {
        pf.bitIndexTable[i - ini] = sum;
        sum += widths[i];
      }
      pf.bitIndexTable[i - ini] = sum;
      i++;

      // draw the chars in the image and decode that image
      sum = 0;
      for (i = ini; i <= end; i++) {
        int ww = widths[i];

        g.setColor(java.awt.Color.white);
        g.fillRect(0, 0, width, height);
        g.setColor(java.awt.Color.black);
        g.drawString(totalcross.sys.Convert.toString((char) i), gaps[i], pf.ascent);
        int[] pixels = getPixels(img, ww, maxH);
        if (pixels != null) {
          computeBits(pixels, sum, ww);
        } else {
          nullCount++;
        }
        sum += ww;
      }
      // save the image
      v.addElement(pf.save());
    }
    if (nullCount > 0) {
      System.out.println("A total of " + nullCount + " characters had no glyphs");
    }
  }

  private totalcross.ui.gfx.Rect computeBounds(int pixels[], int w) {
    int white = -1;
    int x = 0, y = 0;
    int xmin = 10000, ymin = 10000, xmax = -1, ymax = -1;
    for (int i = 0; i < pixels.length; i++) {
      if (pixels[i] != white) {
        xmin = Math.min(xmin, x);
        xmax = Math.max(xmax, x);
        ymin = Math.min(ymin, y);
        ymax = Math.max(ymax, y);
      }

      if (++x == w) {
        x = 0;
        y++;
      }
    }
    if (xmin == 10000) {
      return null;
    }

    return new totalcross.ui.gfx.Rect(xmin, ymin, xmax - xmin + 1, ymax - ymin + 1);
  }

  int bits[] = { 128, 64, 32, 16, 8, 4, 2, 1 };

  private void setBit(int x, int y) {
    pf.bitmapTable[(x >> 3) + y * pf.rowWidthInBytes] |= bits[x % 8]; // set
    if (detailed == 2) {
      System.out.print((char) ('@'));
    }
  }

  private void setNibble4(int pixel, int x, int y) {
    int nibble = 0xF0 - (pixel & 0xF0); // 4 bits of transparency
    if (detailed == 2) {
      System.out.print((char) ('a' + nibble)/*'@'*/);
    }
    if ((x & 1) != 0) {
      nibble >>= 4;
    }
    pf.bitmapTable[(x >> 1) + (y * pf.rowWidthInBytes)] |= nibble; // set
  }

  private void setNibble8(int pixel, int x, int y) {
    int nibble = 0xFF - (pixel & 0xFF); // FFF5F5F5
    if (detailed == 2) {
      System.out.print((char) ('a' + nibble)/*'@'*/);
    }
    pf.bitmapTable[x + (y * pf.rowWidthInBytes)] = (byte) nibble; // set
  }

  private void computeBits(int pixels[], int xx, int w) {
    int white = -1;
    int x = 0, y = 0;
    for (int i = 0; i < pixels.length; i++) {
      if (pixels[i] != white) {
        switch (antialiased) {
        case AA_NO:
          setBit(xx + x, y);
          break;
        case AA_4BPP:
          setNibble4(pixels[i], xx + x, y);
          break;
        case AA_8BPP:
          setNibble8(pixels[i], xx + x, y);
          //AndroidUtils.debug("@"+(xx+x)+","+y+": "+Integer.toHexString(pixels[i]).toUpperCase()+" -> "+Integer.toHexString(pf.bitmapTable[xx+x + (y * pf.rowWidthInBytes)] & 0xFF).toUpperCase());
          break;
        }
      } else if (detailed == 2) {
        System.out.print(' ');
      }

      if (++x == w) {
        x = 0;
        y++;
        if (detailed == 2) {
          println("");
        }
      }
    }
  }

  /*
   chi: 32-127 160-383 402-402 506-511 12288-12291 12296-12309 19968-40869 65281-65374 65504-65510
   Jap: 32-127 160-383 402-402 506-511 12288-12291 12296-12309 12353-12435 12449-12534 65281-65374 65504-65510
   Kor: 32-127 160-383 402-402 506-511 12288-12291 12296-12309 44032-55203 65281-65374 65504-65510
   Internal ranges are stored as 0-255, 256-511, 512-767, ...
   So, 160-383 is splitted into 160-255, 256-383
   */
  private List<Range> processRanges(Font f, List<Range> ranges) {
    // Generate full integer list, checking each character
    final Set<Integer> set = new HashSet<>();
    for (Range range : ranges) {
      final int first = range.s;
      final int last = range.e;
      for (int k = first; k <= last; k++) {
        if (f.canDisplay(k)) {
          set.add(Integer.valueOf(k));
        }
      }
    }

    // Converts the integer set into an ordered list
    List<Integer> list = new ArrayList<>(set);
    Collections.sort(list);

    // split list into ranges of size 256
    int start = 0;
    final List<Range> newRanges = new ArrayList<>();

    int section = list.get(start) / 256;
    for (int k = 1; k < list.size(); k++) {
      if ((list.get(k) / 256) > section) {
        final Range r = new Range(list.get(start), list.get(k - 1), "u" + (section * 256));
        newRanges.add(r);
        println(r.toString());
        start = k;
        section = list.get(start) / 256;
      }
    }
    section = list.get(start) / 256;
    final Range r = new Range(list.get(start), list.get(list.size() - 1), "u" + (section * 256));
    newRanges.add(r);
    println(r.toString());

    return newRanges;
  }

  public static int[] getPixels(java.awt.Image img, int width, int height) {
    java.awt.image.PixelGrabber pg = new java.awt.image.PixelGrabber(img, 0, 0, width, height, true);
    try {
      pg.grabPixels();
    } catch (InterruptedException ie) {
      System.out.println("interrupted!");
    }
    return (int[]) pg.getPixels();
  }

  static public void println(String s) {
    System.out.println(s);
  }

  public static void main(String args[]) {
    try {
      java.awt.Font fonts[] = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
      if (args.length < 1) {
        println("Format: java FontGenerator <font name> /rename:newName /detailed:1_or_2 /aa");
        println("  /NoBold /sizes:<comma-separeted list of sizes> /u <list of ranges>");
        println("");
        println("Parameters are case insensitive, meaning:");
        println(". /monospace To create a monospaced font.");
        println(". /rename:newName to rename the output font name.");
        println(". /detailed:1_or_2 to show detailed information.");
        println(". /aa to create a 4-bpp antialiased font.");
        println(". /aa8 to create a 8-bpp antialiased font.");
        println("  /sizes:<comma-separeted list of sizes> to create a font with the given sizes");
        println(". /NoBold to don't create the bold font.");
        println(
            ". /skipBigChars: useful when creating monospaced fonts; the glyphs that have a width above the W char are skipped.");
        println(". /u to create unicode chars in the range. By default, we create chars in the");
        println("range 32-255. Using this option, you can pass ranges in the form");
        println("\"start0-end0 start1-end1 start2-end2 ...\", which creates a file containing the");
        println("characters ranging from \"startN <= c <= endN\". For example:");
        println(
            "\"/u 32-255 256-383 20284-40869 402-402 \". The ranges may be unordered and overlapping ranges (like 32-160 128-255) are correctly handled.");
        println("The /u option must be the LAST option used.");
        println("");
        println("When creating unicode fonts of a wide range, using options /nobold");
        println("will create a file 1/4 of the original size.");
        println("");
        println("Copyright (c) SuperWaba 2002-2012");
        println("Must use JDK 1.3 or higher!");
        println("\nPress enter to list the available fonts, or q+enter to stop.");
        try {
          if (Character.toLowerCase((char) System.in.read()) != 'q') {
            for (int i = 0; i < fonts.length; i++) {
              println(" " + fonts[i].getName());
            }
          }
        } catch (java.io.IOException ie) {
        }
      } else {
        new FontGenerator(args[0], args);
        System.exit(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static final int AA_NO = 0;
  static final int AA_4BPP = 1;
  static final int AA_8BPP = 2;

  static class PalmFont {
    public int antialiased; // true if its antialiased
    public int firstChar; // ASCII code of first character
    public int lastChar; // ASCII code of last character
    public int spaceWidth; // width of the space char
    public int maxWidth; // width of font rectangle
    public int maxHeight; // height of font rectangle
    public int owTLoc; // offset to offset/width table
    public int ascent; // ascent
    public int descent; // descent
    public int rowWords; // row width of bit image / 2

    public int rowWidthInBytes;
    public int bitmapTableSize;
    public byte[] bitmapTable;
    public int[] bitIndexTable;

    public String fileName;

    public TCZ.Entry save() {
      try {
        ByteArrayStream from = new ByteArrayStream(4096);
        ByteArrayStream to = new ByteArrayStream(2048);
        DataStreamLE ds = new DataStreamLE(from);
        ds.writeShort(antialiased); // note that writeShort already writes an unsigned short (its the same method)
        ds.writeShort(firstChar);
        ds.writeShort(lastChar);
        ds.writeShort(spaceWidth);
        ds.writeShort(maxWidth);
        ds.writeShort(maxHeight);
        ds.writeShort(owTLoc);
        ds.writeShort(ascent);
        ds.writeShort(descent);
        ds.writeShort(rowWords);
        ds.writeBytes(bitmapTable);
        for (int i = 0; i < bitIndexTable.length; i++) {
          ds.writeShort(bitIndexTable[i]);
        }
        // compress the font
        int f = from.getPos(), s;
        from.mark();
        s = ZLib.deflate(from, to, 9);
        // write the name uncompressed before it
        println("Font " + fileName + " stored compressed (" + f + " -> " + s + ")");
        return new TCZ.Entry(to.toByteArray(), fileName.toLowerCase(), f);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    public PalmFont(String fileName) {
      this.fileName = fileName;
    }

    public void debugParams() {
      println("antialiased: " + antialiased);
      println("firstChar  : " + firstChar);
      println("lastChar   : " + lastChar);
      println("spaceWidth : " + spaceWidth);
      println("maxWidth   : " + maxWidth);
      println("maxHeight  : " + maxHeight);
      println("owTLoc     : " + owTLoc);
      println("ascent     : " + ascent);
      println("descent    : " + descent);
      println("rowWords   : " + rowWords);
    }

    public void initTables() {
      rowWidthInBytes = 2 * rowWords * (antialiased == AA_NO ? 1 : antialiased == AA_4BPP ? 4 : 8); // 4 bits of transparency or 1 bit (B/W)
      bitmapTableSize = (int) rowWidthInBytes * (int) maxHeight;

      bitmapTable = new byte[bitmapTableSize];
      bitIndexTable = new int[lastChar - firstChar + 1 + 1];
    }

    public int charWidth(char ch) {
      int index = ch - firstChar;
      return index < 0 || index > lastChar ? spaceWidth : bitIndexTable[index + 1] - bitIndexTable[index];
    }
  }
}