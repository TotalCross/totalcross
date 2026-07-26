// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import tc.tools.JarClassPathLoader;
import tc.tools.deployer.DeploySettings;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;
import totalcross.preview.AppletPreviewSurface;
import totalcross.preview.AwtWindowBackend;
import totalcross.preview.PreviewRuntime;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.PenEvent;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.zip.TCZ;

@SuppressWarnings({"deprecation", "removal"})
abstract class LauncherFontTypes extends LauncherStreamTypes {
  static final int AA_NO = 0;
  static final int AA_4BPP = 1;
  static final int AA_8BPP = 2;
  protected totalcross.util.Hashtable htLoadedFonts = new totalcross.util.Hashtable(31);
  static Hashtable htBaseFonts = new Hashtable(5);
  protected Map<String, String> loadedFontsMap = new HashMap<>();
  public static class CharBits // pgr@402_50 - describe the bitmap for a given character
  {
    public int rowWIB; // width in bytes
    public byte[] charBitmapTable;
    public int offset; // offset relative to the bitmap table
    public int width;
    public int index;
    public totalcross.ui.image.Image img;
  }

  protected static Hashtable loadedTCZs = new Hashtable(31);

  protected int toInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      return 0;
    }
  }

  protected InputStream openInputStream(String path) {
    return null;
  }

  class FontInfo {
    totalcross.io.ByteArrayStream chunks[];
    int[] sizes;
  }

  public class UserFont {
    // 25/120 14/70 4/25 2/15
    public UserFont ubase;
    public totalcross.ui.image.Image[] nativeFonts; // stores the system font in some platforms
    public int antialiased; // AA_ flags
    public int firstChar; // ASCII code of first character
    public int lastChar; // ASCII code of last character
    public int spaceWidth; // width of the space char
    public int maxWidth; // width of font rectangle - unused
    public int maxHeight; // height of font rectangle
    public int owTLoc; // offset to offset/width table - unused
    public int ascent; // ascent
    public int descent; // descent
    public int rowWords; // row width of bit image / 2 - used only to compute rowWidthInBytes

    protected int rowWidthInBytes;
    protected byte[] bitmapTable;
    protected int[] bitIndexTable;
    protected String fontName;
    protected int numberWidth;
    protected int minusW;

    protected UserFont(String fontName, String sufix, int size, totalcross.ui.font.Font base) throws Exception {
      UserFont ubase = (UserFont) base.hv_UserFont;
      this.ubase = ubase;
      this.maxHeight = size;
      this.rowWidthInBytes = ubase.rowWidthInBytes * maxHeight / ubase.maxHeight;
      this.bitIndexTable = new int[ubase.bitIndexTable.length];
      for (int i = 0; i < bitIndexTable.length; i++) {
        this.bitIndexTable[i] = ubase.bitIndexTable[i] * maxHeight / ubase.maxHeight;
      }
      this.nativeFonts = new totalcross.ui.image.Image[bitIndexTable.length];
      this.fontName = base.name;
      this.firstChar = ubase.firstChar;
      this.lastChar = ubase.lastChar;
      this.antialiased = ubase.antialiased;
      this.descent = ubase.descent * maxHeight / ubase.maxHeight;
      this.ascent = size - this.descent;
      this.numberWidth = ubase.numberWidth * maxHeight / ubase.maxHeight;
      this.spaceWidth = ubase.spaceWidth * maxHeight / ubase.maxHeight;
      this.minusW = ubase.minusW;
    }

    protected UserFont(String fontName, String sufix) throws Exception {
      this.fontName = fontName;
      String fileName = fontName + ".tcz";
      TCZ z = (TCZ) loadedTCZs.get(fileName.toLowerCase());
      if (z == null) {
        InputStream is = openInputStream(fileName);
        if (is == null) {
          is = openInputStream("vm/" + fileName); // for the release sdk, there's no etc/fonts. the tcfont.tcz is located at dist/vm/tcfont.tcz
          if (is == null) {
            is = openInputStream("etc/fonts/" + fileName); // if looking for the default font when debugging, use etc/fonts
            if (is == null) {
              loadedFontsMap.put(fontName.toLowerCase(), null);
              throw new Exception("file " + fileName + " " + sufix + " not found"); // loaded = false
            }
          }
        }
        z = new TCZ(new IS2S(is));
        FontInfo fi = new FontInfo();
        int n = z.numberOfChunks;
        fi.chunks = new totalcross.io.ByteArrayStream[n];
        totalcross.util.IntVector sizes = new totalcross.util.IntVector(n / 2);
        for (int i = 0; i < n; i++) {
          int s = z.getNextChunkSize();
          fi.chunks[i] = new totalcross.io.ByteArrayStream(s);
          z.readNextChunk(fi.chunks[i]);
          // compute size - $p20u0
          String name = z.names[i];
          String ss = name.substring(name.lastIndexOf('$') + 2, name.lastIndexOf('u'));
          int size = toInt(ss);
          if (!sizes.contains(size)) {
            sizes.addElement(size);
          }
        }
        sizes.qsort();
        fi.sizes = sizes.toIntArray();
        z.bag = fi;
        loadedTCZs.put(fileName.toLowerCase(), z);
        loadedFontsMap.put(fontName.toLowerCase(), fileName);
      }
      fontName += sufix;
      int index = z.findNamePosition(fontName.toLowerCase());
      if (index == -1) {
        throw new Exception("name " + fontName + " not found"); // loaded = false
      }

      totalcross.io.ByteArrayStream bas = ((FontInfo) z.bag).chunks[index];
      bas.reset();
      totalcross.io.DataStreamLE ds = new totalcross.io.DataStreamLE(bas);
      antialiased = ds.readUnsignedShort();
      firstChar = ds.readUnsignedShort();
      lastChar = ds.readUnsignedShort();
      spaceWidth = ds.readUnsignedShort();
      maxWidth = ds.readUnsignedShort();
      maxHeight = ds.readUnsignedShort();
      owTLoc = ds.readUnsignedShort();
      ascent = ds.readUnsignedShort();
      descent = ds.readUnsignedShort();
      rowWords = ds.readUnsignedShort();

      rowWidthInBytes = 2 * rowWords * (antialiased == AA_NO ? 1 : antialiased == AA_4BPP ? 4 : 8);
      int bitmapTableSize = (int) rowWidthInBytes * (int) maxHeight;

      bitmapTable = new byte[bitmapTableSize];
      ds.readBytes(bitmapTable);
      bitIndexTable = new int[lastChar - firstChar + 1 + 1];
      for (int i = 0; i < bitIndexTable.length; i++) {
        bitIndexTable[i] = ds.readUnsignedShort();
      }
      //
      minusW = antialiased == AA_8BPP && fontName.equals("TCFont") ? 1 : 0;
      if (firstChar <= '0' && '0' <= lastChar) {
        index = (int) '0' - (int) firstChar;
        numberWidth = bitIndexTable[index + 1] - bitIndexTable[index] - minusW;
      }
      if (antialiased == AA_8BPP) {
        nativeFonts = new totalcross.ui.image.Image[bitIndexTable.length];
      }
    }

    protected totalcross.ui.image.Image getBaseCharImage(int index) throws totalcross.ui.image.ImageException // called only in ubase instances
    {
      if (bitmapTable == null && ubase != null) {
        return ubase.getBaseCharImage(index);
      }
      int offset = bitIndexTable[index];
      int width = bitIndexTable[index + 1] - offset - minusW;
      totalcross.ui.image.Image img = new totalcross.ui.image.Image(width, maxHeight);
      int[] pixels = img.getPixels();
      for (int y = 0, idx = 0; y < maxHeight; y++) {
        for (int x = 0; x < width; x++, idx++) {
          pixels[idx] = bitmapTable[y * rowWidthInBytes + x + offset] << 24;
        }
      }
      return img;
    }

    // Get the source x coordinate and width of the character
    public void setCharBits(char ch, CharBits bits) {
      if (firstChar <= ch && ch <= lastChar) {
        int index = (int) ch - (int) firstChar;
        bits.index = index;
        bits.rowWIB = rowWidthInBytes;
        bits.charBitmapTable = bitmapTable;
        bits.offset = bitIndexTable[index];
        bits.width = bitIndexTable[index + 1] - bits.offset - minusW;
        if (bits.width == 0) {
          bits.width += minusW;
        }
        if (ubase != null && ubase.nativeFonts != null) {
          try {
            if (ubase.nativeFonts[index] == null) {
              ubase.nativeFonts[index] = ubase.getBaseCharImage(index);
            }
            if (nativeFonts[index] == null) {
              nativeFonts[index] = ubase.nativeFonts[index].getHwScaledInstance(bits.width, maxHeight);
            }
            bits.img = nativeFonts[index];
            bits.rowWIB = bits.width;
          } catch (Exception e) {
            if (Settings.showDesktopMessages) {
              e.printStackTrace();
            }
            bits.width = spaceWidth;
            bits.offset = -1;
          }
        }
      } else {
        bits.width = spaceWidth;
        bits.offset = -1;
      }
    }
  }

  public int getCharWidth(totalcross.ui.font.Font f, char ch) // guich@tc122_16: moved to outside UserFont, because each char may be in a different UserFont
  {
    UserFont font = (UserFont) f.hv_UserFont;
    if (ch < font.firstChar || ch > font.lastChar) {
      f.hv_UserFont = font = Launcher.instance.getFont(f, ch);
    }
    if (ch == 160) {
      return font.numberWidth;
    }
    if (ch < ' ') {
      return (ch == '\t') ? font.spaceWidth * totalcross.ui.font.Font.TAB_SIZE : 0; // guich@tc100: handle tabs
    }
    int index = (int) ch - (int) font.firstChar;
    return (font.firstChar <= ch && ch <= font.lastChar)
        ? font.bitIndexTable[index + 1] - font.bitIndexTable[index] - font.minusW : font.spaceWidth;
  }


}
