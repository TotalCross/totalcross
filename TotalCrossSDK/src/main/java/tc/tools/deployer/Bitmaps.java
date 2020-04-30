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
package tc.tools.deployer;

import java.io.OutputStream;

import tc.tools.deployer.Bitmaps.Bmp;
import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.ElementNotFoundException;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;

public class Bitmaps {
  static int palette256[] = // web safe palette
      { 0xFFFFFF, 0xFFCCFF, 0xFF99FF, 0xFF66FF, 0xFF33FF, 0xFF00FF, 0xFFFFCC, 0xFFCCCC, // 0-7
          0xFF99CC, 0xFF66CC, 0xFF33CC, 0xFF00CC, 0xFFFF99, 0xFFCC99, 0xFF9999, 0xFF6699, // 8-16
          0xFF3399, 0xFF0099, 0xCCFFFF, 0xCCCCFF, 0xCC99FF, 0xCC66FF, 0xCC33FF, 0xCC00FF, // 16-23
          0xCCFFCC, 0xCCCCCC, 0xCC99CC, 0xCC66CC, 0xCC33CC, 0xCC00CC, 0xCCFF99, 0xCCCC99, // 24-31
          0xCC9999, 0xCC6699, 0xCC3399, 0xCC0099, 0x99FFFF, 0x99CCFF, 0x9999FF, 0x9966FF, // 32-39
          0x9933FF, 0x9900FF, 0x99FFCC, 0x99CCCC, 0x9999CC, 0x9966CC, 0x9933CC, 0x9900CC, // 40-47
          0x99FF99, 0x99CC99, 0x999999, 0x996699, 0x993399, 0x990099, 0x66FFFF, 0x66CCFF, // 48-55
          0x6699FF, 0x6666FF, 0x6633FF, 0x6600FF, 0x66FFCC, 0x66CCCC, 0x6699CC, 0x6666CC, // 56-63
          0x6633CC, 0x6600CC, 0x66FF99, 0x66CC99, 0x669999, 0x666699, 0x663399, 0x660099, // 64-71
          0x33FFFF, 0x33CCFF, 0x3399FF, 0x3366FF, 0x3333FF, 0x3300FF, 0x33FFCC, 0x33CCCC, // 72-79
          0x3399CC, 0x3366CC, 0x3333CC, 0x3300CC, 0x33FF99, 0x33CC99, 0x339999, 0x336699, // 80-87
          0x333399, 0x330099, 0x00FFFF, 0x00CCFF, 0x0099FF, 0x0066FF, 0x0033FF, 0x0000FF, // 88-95
          0x00FFCC, 0x00CCCC, 0x0099CC, 0x0066CC, 0x0033CC, 0x0000CC, 0x00FF99, 0x00CC99, // 96-103
          0x009999, 0x006699, 0x003399, 0x000099, 0xFFFF66, 0xFFCC66, 0xFF9966, 0xFF6666, // 104-111
          0xFF3366, 0xFF0066, 0xFFFF33, 0xFFCC33, 0xFF9933, 0xFF6633, 0xFF3333, 0xFF0033, // 112-119
          0xFFFF00, 0xFFCC00, 0xFF9900, 0xFF6600, 0xFF3300, 0xFF0000, 0xCCFF66, 0xCCCC66, // 120-127
          0xCC9966, 0xCC6666, 0xCC3366, 0xCC0066, 0xCCFF33, 0xCCCC33, 0xCC9933, 0xCC6633, // 128-135
          0xCC3333, 0xCC0033, 0xCCFF00, 0xCCCC00, 0xCC9900, 0xCC6600, 0xCC3300, 0xCC0000, // 136-143
          0x99FF66, 0x99CC66, 0x999966, 0x996666, 0x993366, 0x990066, 0x99FF33, 0x99CC33, // 144-151
          0x999933, 0x996633, 0x993333, 0x990033, 0x99FF00, 0x99CC00, 0x999900, 0x996600, // 152-159
          0x993300, 0x990000, 0x66FF66, 0x66CC66, 0x669966, 0x666666, 0x663366, 0x660066, // 160-167
          0x66FF33, 0x66CC33, 0x669933, 0x666633, 0x663333, 0x660033, 0x66FF00, 0x66CC00, // 168-175
          0x669900, 0x666600, 0x663300, 0x660000, 0x33FF66, 0x33CC66, 0x339966, 0x336666, // 176-183
          0x333366, 0x330066, 0x33FF33, 0x33CC33, 0x339933, 0x336633, 0x333333, 0x330033, // 184-191
          0x33FF00, 0x33CC00, 0x339900, 0x336600, 0x333300, 0x330000, 0x00FF66, 0x00CC66, // 192-199
          0x009966, 0x006666, 0x003366, 0x000066, 0x00FF33, 0x00CC33, 0x009933, 0x006633, // 200-207
          0x003333, 0x000033, 0x00FF00, 0x00CC00, 0x009900, 0x006600, 0x003300, 0x111111, // 208-215
          0x222222, 0x444444, 0x555555, 0x777777, 0x888888, 0xAAAAAA, 0xBBBBBB, 0xDDDDDD, // 216-223
          0xEEEEEE, 0xC0C0C0, 0x800000, 0x800080, 0x008000, 0x008080, 0x000000, 0x000000, // 224-231
          0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, // 232-239
          0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, // 240-247
          0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x060003 // 248-255
      };

  String prefix;

  public Bitmaps(String prefix) throws Exception // guich@330_48: added a prefix
  {
    this.prefix = prefix;
    int oldW = totalcross.sys.Settings.screenWidth;
    int oldH = totalcross.sys.Settings.screenHeight;
    totalcross.sys.Settings.screenWidth = 1024;
    totalcross.sys.Settings.screenHeight = 1024; // let Image work correctly
    if (oldW > 0) // restore only if screen had valid dimensions
    {
      totalcross.sys.Settings.screenWidth = oldW;
      totalcross.sys.Settings.screenHeight = oldH;
    }
  }

  public class Bmp {
    byte[] pixels;
    int[] palette;
    private byte[] wholeImage;
    private boolean shouldInvertY;

    public Bmp(int w, int h) throws ImageException, IOException {
      shouldInvertY = false;
      boolean invertY = h < 0;
      h = Math.abs(h);
      int k = Math.min(w, h);
      Image sized = IconStore.getSquareIcon(k);
      if (w > h) // center the icon horizontally
      {
        Image img = new Image(w, h);
        Graphics gg = img.getGraphics();
        gg.backColor = sized.getGraphics().getPixel(0, 0);
        gg.fillRect(0, 0, w, h);
        gg.drawImage(sized, (w - k) / 2, 0);
        sized = img;
      }
      // the sized image is 24bpp. create the palette, reading all pixels in the image;
      palette = new int[palette256.length];
      totalcross.sys.Vm.arrayCopy(palette256, 0, palette, 0, palette256.length);
      //palette[0] = 0xFFFFFF; palette[255] = 0;
      int realW = ((w + 3) >> 2) << 2;
      pixels = new byte[realW * h];
      Graphics g = sized.getGraphics();

      // map the resized image into the web safe palette

      // ok, here's a very dirty dithering algorithm.
      // I really don't care for performance nor memory!
      // taken from Wikipedia
      int oldpixel, newpixel;
      int[][] pix = new int[w][h];
      // get all r,g,b components for all pixels
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          pix[x][y] = g.getPixel(x, y);
        }
      }
      // now dither them
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          oldpixel = pix[x][y];
          newpixel = findNearest(palette256, oldpixel);
          newpixel = palette256[newpixel];
          pix[x][y] = newpixel;
          // compute the quantization errors
          int or = (oldpixel >> 16) & 0xFF;
          int og = (oldpixel >> 8) & 0xFF;
          int ob = oldpixel & 0xFF;
          int nr = (newpixel >> 16) & 0xFF;
          int ng = (newpixel >> 8) & 0xFF;
          int nb = newpixel & 0xFF;

          int er = or - nr;
          int eg = og - ng;
          int eb = ob - nb;
          if (x < w - 1) {
            pix[x + 1][y] = computePixel(pix[x + 1][y], 7, er, eg, eb);
          }
          if (x > 0 && y < h - 1) {
            pix[x - 1][y + 1] = computePixel(pix[x - 1][y + 1], 3, er, eg, eb);
          }
          if (y < h - 1) {
            pix[x][y + 1] = computePixel(pix[x][y + 1], 5, er, eg, eb);
          }
          if (x < w - 1 && y < h - 1) {
            pix[x + 1][y + 1] = computePixel(pix[x + 1][y + 1], 1, er, eg, eb);
          }
        }
      }
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          pixels[(invertY ? (h - y - 1) : y) * realW + x] = (byte) findExact(palette256, pix[x][y]);
        }
      }
    }

    private int computePixel(int p, int dif, int er, int eg, int eb) {
      int r = (p >> 16) & 0xFF;
      int g = (p >> 8) & 0xFF;
      int b = p & 0xFF;
      r += dif / 16 * er;
      g += dif / 16 * eg;
      b += dif / 16 * eb;
      if (r < 0) {
        r = 0;
      } else if (r > 255) {
        r = 255;
      }
      if (g < 0) {
        g = 0;
      } else if (g > 255) {
        g = 255;
      }
      if (b < 0) {
        b = 0;
      } else if (b > 255) {
        b = 255;
      }
      return (r << 16) | (g << 8) | b;
    }

    public Bmp(byte[] bytes) {
      shouldInvertY = true;
      wholeImage = new byte[bytes.length - 14];
      System.arraycopy(bytes, 14, wholeImage, 0, wholeImage.length);
      wholeImage[22 - 14] *= 2; // guich@340_60: icons have the height doubled
      // header (54 bytes)
      // 0-1   magic chars 'BM'
      // 2-5   uint32 filesize (not reliable)
      // 6-7   uint16 0
      // 8-9   uint16 0
      // 10-13 uint32 bitmapOffset
      // 14-17 uint32 info size    --- icon starts here
      // 18-21 int32  width
      // 22-25 int32  height
      // 26-27 uint16 nplanes
      // 28-29 uint16 bits per pixel
      // 30-33 uint32 compression flag
      // 34-37 uint32 image size in bytes
      // 38-41 int32  biXPixelsPerMeter
      // 42-45 int32  biYPixelsPerMeter
      // 46-49 uint32 colors used
      // 50-53 uint32 important color count
      // 54-   uchar  bitmap bytes depending to type
      // Each scan line of image data is padded to the next four byte boundary

      /* guich@340 notes:
            An icon differs from a bmp in another way: the icon has a mask at the
            end of the file. Also, the height is doubled (for a 32x32 icon, the
            stored height is 32*2 = 64)
      
            Here is how to compute the icon's size.
      
            icon     - 32x32x8
            40       - header
            4*256    - palette: 256 colors
            32*32/1  - pixels / 1 pixel per byte
            32*32/8  - mask
            -------
            2216 bytes
      
            icon     - 32x32x4
            40       - header
            4*16     - palette: 16 colors
            32*32/2  - pixels / 2 pixels per byte
            32*32/8  - mask
            -------
            744 bytes
      
            icon     - 32x32x1
            40       - header
            4*2      - palette: 2 colors
            32*32/8  - pixels / 8 pixels per byte
            32*32/8  - mask
            -------
            304 bytes
      
            icon     - 16x16x4
            40       - header
            4*16     - palette: 16 colors
            20*16/2  - pixels / 2 pixels per byte
            16*16/8  - mask
            -------
            296 bytes
      
       */
      int width;
      int height;

      short bitsPerPixel;

      int compression;
      int imageSize;
      int numberColors;

      if (bytes[0] != 'B' || bytes[1] != 'M') {
        return;
      }
      int offset = 0;
      /*headerSize  = */readInt(offset);
      offset += 4;
      width = readInt(offset);
      offset += 4;
      height = readInt(offset);
      offset += 4;
      /*planes      = */readShort(offset);
      offset += 2;
      bitsPerPixel = readShort(offset);
      offset += 2;
      if (bitsPerPixel > 8) {
        throw new IllegalArgumentException("Bitmaps " + width + "x" + (height / 2) + " cannot be 24bpp!");
      }
      compression = readInt(offset);
      offset += 4;
      if (compression != 0) {
        new Exception("The bitmap used to create icons can't be compressed (RLE)!");
      }
      imageSize = readInt(offset);
      offset += 4;
      if (imageSize == 0) // fdiebolt@341_12: if image size is not defined, compute a default size
      {
        int div = 32 / bitsPerPixel;
        int rowW = ((width + div - 1) / div) * div;
        imageSize = (rowW / (8 / bitsPerPixel)) * height;
      }
      /*xPixels     = */readInt(offset);
      offset += 4;
      /*yPixels     = */readInt(offset);
      offset += 4;
      numberColors = readInt(offset);
      offset += 4;
      /*colorsImport= */readInt(offset);
      offset += 4;
      //System.out.println(width+"x"+height+"x"+bitsPerPixel);
      if (numberColors <= 0) {
        numberColors = 1 << bitsPerPixel;
      }
      palette = loadPalette(wholeImage, offset, numberColors);
      offset += 4 * numberColors;
      pixels = new byte[imageSize];
      System.arraycopy(wholeImage, offset, pixels, 0, imageSize);
      if (palette[0] == 0xFFFFFF && width != 22 && width != 15 && width != 30 && width != 44 && width != 20) {
        System.out.println("This image won't appear correct in WinCE. Palette index 0 must be black (it is white): "
            + width + "x" + (height >> 1) + "x" + bitsPerPixel + ". Please read the instructions.");
      }

      //Utils.println(""+width+'x'+height+'x'+bitsPerPixel+" ("+totalcross.sys.Convert.unsigned2hex(imageSize,4)+")");
    }

    private int readInt(int offset) {
      return (((wholeImage[offset + 3] & 0xFF) << 24) | ((wholeImage[offset + 2] & 0xFF) << 16)
          | ((wholeImage[offset + 1] & 0xFF) << 8) | (wholeImage[offset + 0] & 0xFF));
    }

    private short readShort(int offset) {
      return (short) (((wholeImage[offset + 1] & 0xFF) << 8) | (wholeImage[offset] & 0xFF));
    }
  }

  /** Returns the nearest color in the given palette */
  private int[] lastPal;
  private int lastColor = -1, lastnc = -1;

  public int findNearest(int[] palette, int color) {
    if (palette == lastPal && color == lastColor) {
      return lastnc;
    }
    int nc = 0;
    int sdist = 255 * 255 * 255 + 1;
    int r = (color >> 16) & 0xFF;
    int g = (color >> 8) & 0xFF;
    int b = color & 0xFF;
    for (int i = 0; i < palette.length; i++) {
      int c = palette[i];
      int cr = (c >> 16) & 0xFF;
      int cg = (c >> 8) & 0xFF;
      int cb = c & 0xFF;
      int dist = (r - cr) * (r - cr) + (g - cg) * (g - cg) + (b - cb) * (b - cb);
      if (dist < sdist) {
        nc = i;
        sdist = dist;
      }
    }
    lastPal = palette;
    lastColor = color;
    lastnc = nc;
    return nc;
  }

  /** Returns the exact index for the matching color in the given palette, or -1 if not found */
  public int findExact(int[] palette, int color) {
    for (int i = 0; i < palette.length; i++) {
      if (palette[i] == color) {
        return i;
      }
    }
    return -1;
  }

  /** Copy pixel bits from src to dest, converting the palette
   *  If maskOffset != -1, the mask is created and stored in that location (the most bright pixel is used as background - usually, white)
   */
  private void copyBits(byte[] src, int[] paletteFrom, int[] paletteTo, int srcWidthInBytes, byte[] dest,
      int destOffset, int destWidthInBytes, int rows, int bpp, int maskOffset, boolean inverseMask,
      boolean shouldInvertY) // TODO use the maskOffset in the other platforms too
  {
    IntHashtable ht = new IntHashtable(511);
    int color, newColor, idx, white = 0, x, y;
    int maskWidthInBytes = ((srcWidthInBytes + 31) / 32) << 2;
    int conv[] = null;

    if (maskOffset != -1) {
      conv = inverseMask ? new int[] { 1, 2, 4, 8, 16, 32, 64, 128 } : new int[] { 128, 64, 32, 16, 8, 4, 2, 1 }; // epoc uses the inverse mask
      white = paletteFrom[findNearest(paletteFrom, 0xFFFFFF)];
    }
    // invert mono image?
    boolean doInvert = bpp == 1 && paletteFrom[0] != paletteTo[0];
    boolean invertY = shouldInvertY && rows < 0;
    rows = Math.abs(rows);

    for (y = 0; y < rows; y++) {
      int soff = (invertY ? y : rows - y - 1) * srcWidthInBytes;
      int doff = destOffset + y * destWidthInBytes;
      int moff = maskOffset + y * maskWidthInBytes;

      for (x = 0; x < destWidthInBytes; x++, doff++, soff++) {
        idx = src[soff] & 0xFF;
        if (doInvert || bpp != 8) {
          dest[doff] = doInvert ? (byte) (src[soff] ^ 0xFF) : src[soff];
        } else {
          color = paletteFrom[idx];
          try {
            newColor = ht.get(color);
          } catch (ElementNotFoundException e) {
            newColor = findExact(paletteTo, color);
            if (newColor == -1) {
              newColor = findNearest(paletteTo, color);
            }
            ht.put(color, newColor);
          }
          dest[doff] = (byte) newColor; // is just the index
        }
        if (maskOffset != -1) // also create and store the mask
        {
          color = paletteFrom[idx & 0xFF];
          int bit = x & 7;
          if (color == white) {
            dest[moff] |= conv[bit];
          } else {
            dest[moff] &= ~conv[bit];
          }
          if (bit == 7) {
            moff++;
          }
        }
      }
    }
  }

  public void saveWinCEIcons(byte[] bytes, int bitmap16x16x8_Offset, int bitmap32x32x8_Offset, int bitmap48x48x8_Offset)
      throws ImageException, IOException {
    Bmp bmp16x16x8 = IconStore.getBmp(16, 16, 8);
    Bmp bmp32x32x8 = IconStore.getBmp(32, 32, 8);
    Bmp bmp48x48x8 = IconStore.getBmp(48, 48, 8);
    if (bmp16x16x8 != null && bitmap16x16x8_Offset != -1) {
      copyBits(bmp16x16x8.pixels, bmp16x16x8.palette, loadPalette(bytes, bitmap16x16x8_Offset + 40, 256), 16, bytes,
          bitmap16x16x8_Offset + 40 + 1024, 16, -16, 8, bitmap16x16x8_Offset + 16 * 16 + 40 + 1024, false,
          bmp16x16x8.shouldInvertY);
    }
    if (bmp32x32x8 != null && bitmap32x32x8_Offset != -1) {
      copyBits(bmp32x32x8.pixels, bmp32x32x8.palette, loadPalette(bytes, bitmap32x32x8_Offset + 40, 256), 32, bytes,
          bitmap32x32x8_Offset + 40 + 1024, 32, -32, 8, bitmap32x32x8_Offset + 32 * 32 + 40 + 1024, false,
          bmp32x32x8.shouldInvertY);
    }
    if (bmp48x48x8 != null && bitmap48x48x8_Offset != -1) {
      copyBits(bmp48x48x8.pixels, bmp48x48x8.palette, loadPalette(bytes, bitmap48x48x8_Offset + 40, 256), 48, bytes,
          bitmap48x48x8_Offset + 40 + 1024, 48, -48, 8, bitmap48x48x8_Offset + 48 * 48 + 40 + 1024, false,
          bmp48x48x8.shouldInvertY);
    }
  }

  public void saveWin32Icon(byte[] bytes, int iconOffset) throws ImageException, IOException {
    Bmp bmp32x32x8 = IconStore.getBmp(32, 32, 8);
    if (bmp32x32x8 != null && iconOffset != -1) {
      copyBits(bmp32x32x8.pixels, bmp32x32x8.palette, loadPalette(bytes, iconOffset + 40, 256), 32, bytes,
          iconOffset + 40 + 1024, 32, -32, 8, iconOffset + 32 * 32 + 40 + 1024, false, bmp32x32x8.shouldInvertY);
    }
  }

  public void saveAndroidIcon(OutputStream zos, int res) throws Exception // icon.png
  {
    Image img = IconStore.getSquareIcon(res);
    ByteArrayStream s = new ByteArrayStream(res * res);
    img.createPng(s);
    zos.write(s.getBuffer(), 0, s.getPos());
  }

  private int[] loadPalette(byte[] bytes, int offset, int numberColors) {
    int[] palette = new int[numberColors];
    for (int i = 0; i < numberColors; i++, offset += 4) {
      palette[i] = (((bytes[offset + 3] & 0xFF) << 24) | ((bytes[offset + 2] & 0xFF) << 16)
          | ((bytes[offset + 1] & 0xFF) << 8) | (bytes[offset + 0] & 0xFF));
    }
    return palette;
  }

  static final Image4iOS[] IOS_ICONS = { new Image4iOS("Icon.png", 57), new Image4iOS("Icon@2x.png", 114),
      new Image4iOS("Icon-iPad.png", 72), new Image4iOS("Icon-iPad@2x.png", 144), new Image4iOS("Icon-Small.png", 29),
      new Image4iOS("Icon-Small@2x.png", 58), new Image4iOS("Icon-Small-iPad.png", 50),
      new Image4iOS("Icon-Small-iPad@2x.png", 100), new Image4iOS("Icon-Small-iPad.png", 50),
      new Image4iOS("Icon-Small-iPad@2x.png", 100), new Image4iOS("Default-568h@2x.png", 640, 1136),
      // ios 7
      new Image4iOS("Icon76.png", 76), new Image4iOS("Icon152.png", 152), new Image4iOS("Icon120.png", 120), };

  static class Image4iOS {
    final String name;
    final int width;
    final int height;

    private Image4iOS(String name, int size) {
      this.name = name;
      this.width = size;
      this.height = size;
    }

    private Image4iOS(String name, int width, int height) {
      this.name = name;
      this.width = width;
      this.height = height;
    }

    public byte[] getImage() {
      byte[] b = null;
      try {
        Image img = width == height ? IconStore.getSquareIcon(width) : IconStore.getSplashImage(width, height);
        ByteArrayStream bas = new ByteArrayStream(width * height);
        img.createPng(bas);
        b = bas.toByteArray();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return b;
    }
  }
}

class IconStore extends Hashtable {
  private Image largestSquareIcon;
  private Image largestSplashImage;
  private static IconStore instance;

  private IconStore() throws IOException {
    super(30);
    if (DeploySettings.classPath != null && DeploySettings.classPath.length > 0) {
      for (int i = DeploySettings.classPath.length - 1; i >= 0; i--) {
        if (DeploySettings.classPath[i].length() > 0) {
          addFrom(DeploySettings.classPath[i]);
        }
      }
    }
    addFrom(DeploySettings.mainClassDir);
    addFrom(DeploySettings.baseDir);
    addFrom(".");
    addFrom(DeploySettings.currentDir);
    if (this.size() == 0) {
      addFrom(DeploySettings.etcDir + "images/");
    }
  }

  public static IconStore getInstance() throws IOException {
    if (instance == null) {
      instance = new IconStore();
    }
    return instance;
  }

  public static byte[] getImageData(String dimensions) throws IOException {
    return (byte[]) getInstance().get(dimensions);
  }

  public static Bmp getBmp(int width, int height, int bpp) throws IOException, ImageException {
    byte[] b = (byte[]) getInstance().get(width + "x" + Math.abs(height) + "x" + bpp);
    return b != null ? DeploySettings.bitmaps.new Bmp(b) : DeploySettings.bitmaps.new Bmp(width, height);
  }

  public static Image getSplashImage(int width, int height) throws ImageException, IOException {
    Image img;
    IconStore store = getInstance();
    byte[] b = (byte[]) store.get(width + "x" + height);
    if (b != null) {
      img = new Image(b);
    } else if (store.largestSplashImage == null) {
      img = whiteImage(width, height);
    } else {
      img = store.largestSplashImage.getSmoothScaledInstance(width, height);
    }
    if (img.getHeight() != height || img.getWidth() != width) {
      throw new ImageException("splash" + img.getHeight() + "x" + img.getWidth() + " must be " + width + "x" + height);
    }
    return img;
  }

  public static Image getSquareIcon(int size) throws ImageException, IOException {
    Image img;
    IconStore store = getInstance();
    byte[] b = (byte[]) store.get(size + "x" + size);
    if (b != null) {
      img = new Image(b);
    } else if (store.largestSquareIcon == null) {
      img = whiteImage(size, size);
    } else {
      img = store.largestSquareIcon.getSmoothScaledInstance(size, size);
    }
    if (img.getHeight() != size || img.getWidth() != size) {
      throw new ImageException("icon" + img.getHeight() + "x" + img.getWidth() + " must be " + size + "x" + size);
    }
    return img;
  }

  public static Image getIcon(int width, int height) throws ImageException, IOException {
    Image img;
    IconStore store = getInstance();
    byte[] b = (byte[]) store.get(width + "x" + height);
    if (b != null) {
      img = new Image(b);
    } else if (store.largestSquareIcon == null) {
      img = whiteImage(width, height);
    } else {
      img = store.largestSquareIcon.getSmoothScaledInstance(width, height);
    }
    if (img.getHeight() != height || img.getWidth() != width) {
      throw new ImageException("icon" + img.getHeight() + "x" + img.getWidth() + " must be " + height + "x" + width);
    }
    return img;
  }

  private static Image whiteImage(int w, int h) throws ImageException {
    Image img = new Image(w, h);
    int[] pixels = img.getPixels();
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = 0xFFFFFFFF;
    }
    return img;
  }

  private void addFrom(String path) throws IOException {
    int largestIconSize = 0;
    int largestSplashSize = 0;

    if (path != null && new File(path).isDir()) {
      path = path.trim();
      String[] files = File.listFiles(path, false);
      if (files != null) {
        for (int i = files.length - 1; i >= 0; i--) {
          String name = null;
          String file = files[i].toLowerCase();
          if (file.endsWith("appicon.gif")) {
            System.out.println("*** Warning: appicon.gif is deprecated. Convert it to a 256x256 png with transparency");
            name = "appicon";
          } else if (file.endsWith("appicon.png")) {
            name = "appicon";
          } else if (file.endsWith(".bmp") || file.endsWith(".png")) {
            int idxLastSlash = file.lastIndexOf('/');
            int idxIconName = file.indexOf("icon");
            if (idxLastSlash != -1 && idxIconName != -1) {
              name = file.substring(idxIconName + 4, file.lastIndexOf('.'));
            } else {
              int idxSplashName = file.indexOf("splash");
              if (idxLastSlash != -1 && idxSplashName != -1) {
                name = file.substring(idxIconName + 6, file.lastIndexOf('.'));
              }
            }
          }

          if (name != null) {
            try {
              byte[] b = Utils.loadFile(files[i], false);
              if (b == null) {
                System.out.println("File not found: " + files[i]);
                continue;
              }
              Image img = new Image(b);
              if (img.getWidth() == img.getHeight()) {
                int imgSize = img.getWidth() * img.getHeight();
                if (imgSize > largestIconSize) {
                  largestIconSize = imgSize;
                  largestSquareIcon = img;
                }
              } else {
                int imgSize = img.getWidth() * img.getHeight();
                if (imgSize > largestSplashSize) {
                  largestSplashSize = imgSize;
                  largestSplashImage = img;
                }
              }
              if (name.length() > 1) {
                this.put(name, b);
              }
            } catch (ImageException e) {
              // ignore and keep searching
            } catch (IOException e) {
              // ignore and keep searching
            }
          }
        }
      }
    }
  }
}
