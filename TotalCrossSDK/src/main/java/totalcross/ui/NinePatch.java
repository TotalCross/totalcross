// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Hashtable;
import totalcross.util.concurrent.Lock;

/** NinePatch is a class that creates a button of any size by dividing a 
 * sample button into 9 parts: the 4 corners, the 4 sides, and the middle.
 * 
 * Corner are drawn unscaled, sides are resized in a single direction, and the middle
 * is resized, colorized and then dithered.
 * 
 * This class is thread-safe.
 * 
 * @since TotalCross 1.3
 */
public class NinePatch {
  public static final int BUTTON = 0;
  public static final int EDIT = 1;
  public static final int COMBOBOX = 2;
  public static final int LISTBOX = 3;
  public static final int MULTIEDIT = 4;
  public static final int PROGRESSBARV = 5;
  public static final int PROGRESSBARH = 4;
  public static final int SCROLLPOSH = 6;
  public static final int SCROLLPOSV = 7;
  public static final int TAB = 8;
  public static final int GRID = 9;
  public static final int TAB2 = 10;
  public static final int MULTIBUTTON = 11;

  static NinePatch instance;

  /** Defines if we will use the Image.applyColor (1) or Image.applyColor2 (2) algorithms
   * when getting the pressed instance. */
  public static int pressColorAlgorithm = 1;

  public static NinePatch getInstance() {
    return instance != null ? instance : (instance = new NinePatch());
  }

  private NinePatch() {
    switch (Settings.uiStyle) {
    case Settings.Holo:
      parts = new Parts[] { load(Resources.button, 7, 4), load(Resources.edit, 16, 2), load(Resources.combobox, 9, 5),
          load(Resources.listbox, 10, 5), load(Resources.multiedit, 9, 4), load(Resources.progressbarv, 9, 4),
          load(Resources.scrollposh, 3, 2), load(Resources.scrollposv, 3, 2), load(Resources.tab, 10, 4),
          load(Resources.grid, 5, 3), load(Resources.tab2, 18, 3), load(Resources.multibutton, 5, 2), };
      break;
    case Settings.Material:
      parts = new Parts[] { load(Resources.button, 7, 3), load(Resources.combobox, 9, 5), load(Resources.combobox, 9, 5),
          load(Resources.listbox, 10, 5), null, load(Resources.progressbarv, 9, 4), load(Resources.scrollposh, 3, 2),
          load(Resources.scrollposv, 3, 2), load(Resources.tab, 10, 4), load(Resources.grid, 5, 3),
          load(Resources.tab2, 10, 2), load(Resources.multibutton, 5, 2), };
      break;
    default:
      parts = new Parts[] { load(Resources.button, 7, 1), load(Resources.edit, 5, 3), load(Resources.combobox, 9, 5),
          load(Resources.listbox, 5, 3), load(Resources.multiedit, 9, 4), load(Resources.progressbarv, 9, 4),
          load(Resources.scrollposh, 3, 2), load(Resources.scrollposv, 3, 2), load(Resources.tab, 10, 4),
          load(Resources.grid, 5, 3), load(Resources.tab2, 10, 2), load(Resources.multibutton, 5, 2), };
      break;
    }
  }

  public class Parts {
    Image imgLT, imgT, imgRT, imgL, imgC, imgR, imgLB, imgB, imgRB; // left top right bottom
    int scalableAreaStartWidth, scalableAreaEndWidth, scalableAreaStartHeight, scalableAreaEndHeight;
  }

  private Lock imageLock = new Lock();

  private Parts[] parts;

  private Hashtable htBtn = new Hashtable(100);
  private Hashtable htPressBtn = new Hashtable(100);
  private StringBuffer sbBtn = new StringBuffer(25);

  private void copyPixels(int[] buf, Image dst, Image src, int dstX, int dstY, int srcX, int srcY, int srcW, int srcH) {
    int dstW = dst.getWidth();
    int dstH = dst.getHeight();
    if (srcW > dstW) {
      srcW = dstW;
    }
    if (srcH > dstH) {
      srcH = dstH;
    }

    int y2 = srcY + srcH;
    Graphics gd = dst.getGraphics();
    Graphics gs = src.getGraphics();

    for (; srcY < y2; srcY++, dstY++) {
      gs.getRGB(buf, 0, srcX, srcY, srcW, 1);
      gd.setRGB(buf, 0, dstX, dstY, srcW, 1);
    }
  }

  private Image getImageArea(int[] buf, Image orig, int x, int y, int w, int h) throws ImageException {
    Image img = new Image(w, h);
    copyPixels(buf, img, orig, 0, 0, x, y, w, h);
    return img;
  }
  /**
   * Returns a Parts that should be used to set the npParts of a Control.
   * @param original the original image with the guides.
   * */
  public Parts load(Image original) {
	int w = original.getWidth();
	int h = original.getHeight();
	int[] buf = new int[w > h ? w : h];
	int[] scalableAreas = getScalableArea(original);
	Parts p = null;
	try {
		p = load(getImageArea(buf, original, 1, 1, w - 2, h - 2), scalableAreas[0], scalableAreas[1], scalableAreas[2], scalableAreas[3]);
	} catch (ImageException e) {
		e.printStackTrace();
	}
	return p;
  }
  
  private int[] getScalableArea(Image image) {
	  int w = image.getWidth();
      int h = image.getHeight();
      byte[] colors = new byte[w*4];
	  int scalableArea[] = new int[4];
      //LT
	  image.getPixelRow(colors, 0);
	  
	  int[] c2 = new int[w];
	  for (int i = 0, j = 0 ; i < w ; i++, j=i*4) {
		  c2[i] = (colors[j+3] << 24) | (colors[j] << 16) | (colors[j+1] << 8) | colors[j+2];
	  }
	  
	  int startW;
	  for(startW = 0 ; startW < w ; startW++) {
		  if (colors[(startW*4)+3] == -1) {
			  startW--; // Discouting the guide pixel
			  break;
		  }
	  }
	  
	  int endW;
	  for(endW = w - 1; endW >= 0 ; endW--) {
		  if (colors[(endW*4)+3] == -1) {
			  endW++; // Discouting the guide pixel
			  break;
		  }
	  }
	  
	  int startH;
	  for (startH = 0 ; startH < h ; startH++) {
		  image.getPixelRow(colors, startH);
		  if (colors[3] == -1) {
			  startH--; // Discouting the guide pixel
			  break;
		  }
	  }
	  
	  int endH;
	  for (endH = h - 1; endH >= 0 ; endH--) {
		  image.getPixelRow(colors, endH);
		  if (colors[3] == -1) {
			  endH++; // Discouting the guide pixel
			  break;
		  }
	  }
	  
	  
	  if (startW < endW && startH < endH) {
		  scalableArea[0] = startW;
		  scalableArea[1] = endW;
		  scalableArea[2] = startH;
		  scalableArea[3] = endH;
	  } else {
		  System.out.println("Failed to find guide points for this ninepatch. Printing the stack trace");
		  Vm.printStackTrace();
		  scalableArea[0] = 1;
		  scalableArea[1] = w - 2;
		  scalableArea[2] = 1;
		  scalableArea[3] = h - 2;
	  }
	  
      return scalableArea;
  }
  
  public Parts load(Image original, int scalableAreaStartWidth, int scalableAreaEndWidth, int scalableAreaStartHeight, int scalableAreaEndHeight) {
	  try {
	      int w = original.getWidth();
	      int h = original.getHeight();
	      int[] buf = new int[w > h ? w : h];
	      Parts p = new Parts();
	      p.scalableAreaStartWidth = scalableAreaStartWidth;
	      p.scalableAreaEndWidth = scalableAreaEndWidth;
	      p.scalableAreaStartHeight = scalableAreaStartHeight;
	      p.scalableAreaEndHeight = scalableAreaEndHeight;
	      p.imgLT = getImageArea(buf, original, 0, 0, scalableAreaStartWidth, scalableAreaStartHeight);
	      p.imgRT = getImageArea(buf, original, scalableAreaEndWidth, 0, w - scalableAreaEndWidth, scalableAreaStartHeight);
	      p.imgLB = getImageArea(buf, original, 0, scalableAreaEndHeight, scalableAreaStartWidth, h - scalableAreaEndHeight);
	      p.imgRB = getImageArea(buf, original, scalableAreaEndWidth, scalableAreaEndHeight, w - scalableAreaEndWidth, h - scalableAreaEndHeight);
	      p.imgT = getImageArea(buf, original, scalableAreaStartWidth, 0, scalableAreaEndWidth - scalableAreaStartWidth, scalableAreaStartHeight);
	      p.imgB = getImageArea(buf, original, scalableAreaStartWidth, scalableAreaEndHeight, scalableAreaEndWidth - scalableAreaStartWidth, h - scalableAreaEndHeight);
	      p.imgL = getImageArea(buf, original, 0, scalableAreaStartHeight, scalableAreaStartWidth, scalableAreaEndHeight - scalableAreaStartHeight);
	      p.imgR = getImageArea(buf, original, scalableAreaEndWidth, scalableAreaStartHeight, w - scalableAreaEndWidth, scalableAreaEndHeight - scalableAreaStartHeight);
	      p.imgC = getImageArea(buf, original, scalableAreaStartWidth, scalableAreaStartHeight, scalableAreaEndWidth - scalableAreaStartWidth, scalableAreaEndHeight - scalableAreaStartHeight);
	      return p;
	    } catch (Exception e) {
	      throw new RuntimeException(e + " " + e.getMessage());
	    }
  }
  
/**
 * 
 * @param original teste0
 * @param side teste
 * @param corner teste2
 * */
  public Parts load(Image original, int scalableAreaWidth, int scalableAreaHeight) {
    try {
      int w = original.getWidth();
      int h = original.getHeight();
      int[] buf = new int[w > h ? w : h];
      Parts p = new Parts();
      p.scalableAreaStartWidth = scalableAreaWidth;
      p.scalableAreaEndWidth = original.getWidth() - scalableAreaWidth;
      p.scalableAreaStartHeight = scalableAreaHeight;
      p.scalableAreaEndHeight = original.getHeight() - scalableAreaHeight;
      p.imgLT = getImageArea(buf, original, 0, 0, scalableAreaWidth, scalableAreaHeight);
      p.imgRT = getImageArea(buf, original, w - scalableAreaWidth, 0, scalableAreaWidth, scalableAreaHeight);
      p.imgLB = getImageArea(buf, original, 0, h - scalableAreaHeight, scalableAreaWidth, scalableAreaHeight);
      p.imgRB = getImageArea(buf, original, w - scalableAreaWidth, h - scalableAreaHeight, scalableAreaWidth, scalableAreaHeight);
      p.imgT = getImageArea(buf, original, scalableAreaWidth, 0, w - scalableAreaWidth * 2, scalableAreaHeight);
      p.imgB = getImageArea(buf, original, scalableAreaWidth, h - scalableAreaHeight, w - scalableAreaWidth * 2, scalableAreaHeight);
      p.imgL = getImageArea(buf, original, 0, scalableAreaHeight, scalableAreaWidth, h - scalableAreaHeight * 2);
      p.imgR = getImageArea(buf, original, w - scalableAreaWidth, scalableAreaHeight, scalableAreaWidth, h - scalableAreaHeight * 2);
      p.imgC = getImageArea(buf, original, scalableAreaWidth, scalableAreaHeight, w - scalableAreaWidth * 2, h - scalableAreaHeight * 2);
      return p;
    } catch (Exception e) {
      throw new RuntimeException(e + " " + e.getMessage());
    }
  }

  public Image getNormalInstance(Parts p, int width, int height, int color, boolean rotate) throws ImageException {
    int[] buf = new int[width > height ? width : height];
    Image ret = new Image(width, height);
    Image c;
//    int side = p.side, s;
//    int corner = p.corner;
    int s;
    int scalableAreaStartWidth = p.scalableAreaStartWidth;
    int scalableAreaStartHeight = p.scalableAreaStartHeight;
    // sides
    s = width - (p.imgLT.getWidth() + p.imgRT.getWidth());
    if (s > 0) {
      c = p.imgT.getScaledInstance(s, p.imgT.getHeight());
      copyPixels(buf, ret, c, scalableAreaStartWidth, 0, 0, 0, s, c.getHeight());
      c = p.imgB.getScaledInstance(s, p.imgB.getHeight());
      copyPixels(buf, ret, c, scalableAreaStartWidth, height - c.getHeight(), 0, 0, s, c.getHeight());
    }
    s = height - (p.imgLT.getHeight() + p.imgLB.getHeight());
    if (s > 0) {
      c = p.imgL.getScaledInstance(p.imgL.getWidth(), s);
      copyPixels(buf, ret, c, 0, scalableAreaStartHeight, 0, 0, c.getWidth(), s);
      c = p.imgR.getScaledInstance(p.imgR.getWidth(), s);
      copyPixels(buf, ret, c, width - c.getWidth(), scalableAreaStartHeight, 0, 0, c.getWidth(), s);
    }
    // corners
    try {
      copyPixels(buf, ret, p.imgLT, 0, 0, 0, 0, p.imgLT.getWidth(), p.imgLT.getHeight());
      copyPixels(buf, ret, p.imgRT, width - p.imgRT.getWidth(), 0, 0, 0, p.imgRT.getWidth(), p.imgRT.getHeight());
      copyPixels(buf, ret, p.imgLB, 0, height - p.imgLB.getHeight(), 0, 0, p.imgLB.getWidth(), p.imgLB.getHeight());
      copyPixels(buf, ret, p.imgRB, width - p.imgRB.getWidth(), height - p.imgRB.getHeight(), 0, 0, p.imgRB.getWidth(), p.imgRB.getHeight());
    } catch (ArrayIndexOutOfBoundsException aioobe) {
    } // ignore error that comes sometimes in JavaSE
    // center
    int cW = width - (p.imgLT.getWidth() + p.imgRT.getWidth());
    int cH = height - (p.imgLT.getHeight() + p.imgLB.getHeight());
    if (cW > 0 &&  cH > 0) {
      c = p.imgC.getScaledInstance(cW, cH); // smoothscale generates a worst result because it enhances the edges
      copyPixels(buf, ret, c, scalableAreaStartWidth, scalableAreaStartHeight, 0, 0, cW, cH);
    }
    if (Settings.screenBPP == 16) {
      ret.getGraphics().dither(0, 0, ret.getWidth(), ret.getHeight());
    }
    if (color != -1) {
      ret.applyColor2(color);
    }
    if (rotate) {
      ret = ret.getRotatedScaledInstance(100, 180, -1);
    }
    return ret;
  }

  // rotate is only used by TabbedContainer
  public Image getNormalInstance(int type, int width, int height, int color, boolean rotate) throws ImageException {
    Image ret = null;
    synchronized (imageLock) {
      int hash = 0;
      sbBtn.setLength(0);
      hash = Convert.hashCode(sbBtn.append(type).append('|').append(width).append('|').append(height).append('|')
          .append(color).append('|').append(rotate));
      ret = (Image) htBtn.get(hash);
      if (ret == null) {
        ret = getNormalInstance(parts[type], width, height, color, rotate);
        htBtn.put(hash, ret);
      }
    }
    return ret;
  }

  public Image getPressedInstance(Image img, int backColor, int pressColor) throws ImageException {
    Image pressed = null;
    sbBtn.setLength(0);
    int hash = Convert.hashCode(sbBtn.append(img).append('|').append(backColor).append('|').append(pressColor));
    pressed = (Image) htPressBtn.get(hash);
    if (pressed == null) {
      synchronized (imageLock) {
        if (pressColor != -1) {
          pressed = img.getFrameInstance(0); // get a copy of the image
          if (pressColorAlgorithm == 1) {
            pressed.applyColor(pressColor); // colorize it
          } else {
            pressed.applyColor2(pressColor); // colorize it
          }
        } else {
          pressed = img.getTouchedUpInstance(Color.getBrightness(backColor) > (256 - 32) ? (byte) -64 : (byte) 32,
              (byte) 0);
        }
        htPressBtn.put(hash, pressed);
      }
    }
    return pressed;
  }

  public void flush() {
    htBtn.clear();
    htPressBtn.clear();
  }

  /** Used internally to prevent Out of Memory errors. */
  public static void tryDrawImage(Graphics g, Image npback, int x, int y) {
    try {
      if (npback != null) {
        g.drawImage(npback, x, y);
      }
    } catch (OutOfMemoryError oome) {
      getInstance().flush(); // release memory and try again
      g.drawImage(npback, x, y);
    }
  }
}
