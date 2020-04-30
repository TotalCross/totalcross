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

package totalcross.ui.gfx;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.Launcher;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.font.Font;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Hashtable;

/**
 * Graphics draws on a surface.
 * <p>
 * Surfaces are objects that implement the GfxSurface interface. MainWindow and Image are both examples of surfaces.
 * <p>
 * Here is an example that uses Graphics to draw a line:
 *
 * <pre>
 * public class MyProgram extends MainWindow {
 *    public void onPaint(Graphics g) {
 *       g.foreColor = 0x0000FF;
 *       g.drawLine(0, 0, 10, 10);
 *       g.backColor = Color.MAGENTA;
 *       g.fillCircle(50,50,20);
 *       ...
 * </pre>
 * 
 * @see Color
 * @see GfxSurface
 */

public final class Graphics {
  // ORDER MUST NOT BE CHANGED!
  // instance ints

  /** The foreground color, in 0xRRGGBB format. */
  public int foreColor; // black
  /** The background color, in 0xRRGGBB format. */
  public int backColor = Color.BRIGHT;

  /**
   * Set to true to use antialiase in all drawing operations that draw a diagonal line, such as drawPoligon and
   * drawLine.
   * 
   * @deprecated This is not used in OpenGL platforms
   */
  @Deprecated
  public boolean useAA;

  protected int width, height;

  private int transX, transY;

  private int clipX1, clipY1, clipX2, clipY2; // clip1 <= k < clip2

  private int minX, minY, maxX, maxY;

  private int lastXC, lastYC, lastRX, lastRY, lastSize; // used by arcPiePointDrawAndFill

  private int pitch;

  /** Alpha value used on operations, shifted by 24 bits left. E.G.: to use an alpha value of 0x80, set this with 0x80000000, or 0x80<<24.
   * The default value is 0xFF000000 and is used only when drawing on Images. 
   */
  public int alpha;

  /**
   * States that the next calls to drawText will draw it vertically. We decided to do this using a boolean instead of
   * duplicating all the 10 drawText method calls, to keep it simple.
   * 
   * @see #drawVerticalText(String, int, int)
   * @since TotalCross 1.7
   */
  public boolean isVerticalText;

  private int lastClipFactor;

  private boolean isControlSurface;

  private int lastcRX, lastcRY, lastcSize;

  // instance doubles
  private double lastPPD, lastcPPD; // used by arcPiePointDrawAndFill

  // instance objects
  /** The surface where this Graphics will draw on. */
  protected GfxSurface surface;

  private Font font;

  protected int xPoints[], yPoints[]; // used by arcPiePointDrawAndFill

  protected int[] ints; // used by fillPolygon

  private int cyPoints[], cxPoints[];

  // static objects
  /** Defines if the screen has been changed. */
  public static boolean needsUpdate; // IMPORTANT: NOT USED IN DEVICE

  private static int[] pal685;

  /** Contains the pixels of the MainWindow. */
  public static int[] mainWindowPixels; // create the pixels

  /** used in the draw3dRect method */
  static public final byte R3D_EDIT = 1;
  /** used in the draw3dRect method */
  static public final byte R3D_LOWERED = 2;
  /** used in the draw3dRect method */
  static public final byte R3D_RAISED = 3;
  /** used in the draw3dRect method */
  static public final byte R3D_CHECK = 4;
  /**
   * used in the draw3dRect method. Note that the <i>simple</i> parameter acts in anothe way: if is true, a menu shaded
   * is drawn; if is false, a listbox shaded is drawn. Also note that the fore color must have been set.
   */
  static public final byte R3D_SHADED = 5;
  /** used in the draw3dRect method */
  static public final byte R3D_FILL = 6;
  /** used in the drawArrow method */
  static public final byte ARROW_UP = 1;
  /** used in the drawArrow method */
  static public final byte ARROW_DOWN = 2;
  /** used in the drawArrow method */
  static public final byte ARROW_LEFT = 3;
  /** used in the drawArrow method */
  static public final byte ARROW_RIGHT = 4;

  private static int ands8Mask[] = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };

  private int gxPoints[], gyPoints[], axPoints[][], ayPoints[][], anPoints[], aBase[];

  private int[] translateAndClipResults = new int[4];

  private int[] tlx, tly;

  /**
   * Constructs a graphics object which can be used to draw on the given surface.
   * <p>
   * Only totalcross.ui.Control and totalcross.ui.image.Image classes can have a graphics. Trying to create it with
   * another class will result in a runtime exception.
   * <p>
   * If you are trying to create a graphics object for drawing in a subclass of control, use the getGraphics() method
   * in the Control class.
   * 
   * @see totalcross.ui.Control#getGraphics
   */
  public Graphics(GfxSurface surface) {
    this.surface = surface;
    if (surface instanceof Image) {
      alpha = 0xFF000000;
    } else {
      isControlSurface = true;
    }
    create(surface);
  }

  @ReplacedByNativeOnDeploy
  private void create(GfxSurface surface) {
    if (surface instanceof Image) {
      pitch = ((Image) surface).getWidth();
    } else if (surface instanceof Control) {
      if (mainWindowPixels == null || mainWindowPixels.length < Settings.screenWidth * Settings.screenHeight) {
        mainWindowPixels = new int[Settings.screenWidth * Settings.screenHeight]; // create the pixels
      }
      pitch = Settings.screenWidth;
    } else {
      throw new RuntimeException("Only Image and Control can have a Graphics.");
    }
  }

  /** Returns true if the source surface is from a Control (if false, its an Image). */
  public boolean isControlSurface() {
    return isControlSurface;
  }

  /**
   * Fades all window pixels with the given value. The window is not repainted, so, if you fade it to bright, you will
   * have to repaint the window to get the original pixels back. Also, don't forget to call updateWindow after this
   * method. Here's a sample of how to fade the screen to black:
   * 
   * <pre>
   * for (int i = 0; i < 256; i++) {
   *    Graphics.fadeScreen(1);
   *    updateScreen();
   * }
   * </pre>
   * 
   * And now how to fade it back:
   * 
   * <pre>
   * for (int i = 256; --i >= 0;) {
   *    enableUpdateScreen = false;
   *    for (int j = 0, n = zStack.size(); j < n; j++) {
   *       // repaints every window, from the nearest with the MainWindow size to last parent
   *       ((Window) zStack.items[j]).repaintNow();
   *    }
   *    enableUpdateScreen = true;
   *    Graphics.fadeScreen(i);
   *    updateScreen();
   * }
   * </pre>
   * 
   * You may want to add a delay in the fade out, since its much faster than the fade in. <br>
   * <br>
   * CAUTION: the code above does not work on Android.
   * 
   * @since TotalCross 1.2
   * @see Window#fadeOtherWindows
   * @see Window#fadeValue
   */
  @ReplacedByNativeOnDeploy
  public static void fadeScreen(int fadeValue) {
	int[] pixels = Graphics.mainWindowPixels;
    int lastColor = -1;
    int lastFaded = 0;
    for (int j = pixels.length; --j >= 0;) {
      int rgb = pixels[j];
      if (rgb == lastColor) {
        pixels[j] = lastFaded;
      } else {
        lastColor = rgb;
        int a = ((rgb >> 24) & 0xFF);
        int r = ((rgb >> 16) & 0xFF) * fadeValue / 255;
        int g = ((rgb >> 8) & 0xFF) * fadeValue / 255;
        int b = (rgb & 0xFF) * fadeValue / 255;
        lastFaded = pixels[j] = (a << 24) | (r << 16) | (g << 8) | b;
      }
    }
  }

  private int[] getSurfacePixels(GfxSurface surface) {
    return surface instanceof Image ? ((Image) surface).getPixels() : mainWindowPixels;
  }

  /**
   * Refreshes the clipping bounds, translation and font for this Graphics.
   * 
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public void refresh(int sx, int sy, int sw, int sh, int tx, int ty, Font f) {
    int scrW, scrH;
    if (isControlSurface) {
      if (mainWindowPixels.length < Settings.screenHeight * Settings.screenWidth) {
        mainWindowPixels = null; // let the gc collect the old array before allocating the new one if necessary.
        mainWindowPixels = new int[Settings.screenHeight * Settings.screenWidth];
      }
      scrW = pitch = Settings.screenWidth;
      scrH = Settings.screenHeight;
    } else {
      // image
      scrW = surface.getWidth();
      scrH = surface.getHeight();
    }
    clipX1 = minX = Math.max(0, sx);
    clipY1 = minY = Math.max(0, sy);
    clipX2 = maxX = Math.min(sx + sw, scrW);
    clipY2 = maxY = Math.min(sy + sh, scrH);
    transX = tx;
    transY = ty;
    if (f != null) {
      setFont(f);
    }
  }

  /** Expands the clipping limits. Used internally. */
  public void expandClipLimits(int dx1, int dy1, int dx2, int dy2) {
    minX += dx1;
    minY += dy1;
    maxX += dx2;
    maxY += dy2;
  }

  /**
   * Returns the palette used when the screen has 8 bpp. You can view the palette online
   * <a href='http://www.superwaba.org/tc/pal685_values.png' target=_blank>here</a>. There's no need to convert your
   * colors to these ones; this will be done on-the-fly by the vm.
   * 
   * @since TotalCross 1.0
   */
  public static int[] getPalette() {
    if (pal685 == null) {
      pal685 = new int[256];
      int ii = 0, rr, gg, bb;
      int R = 6, G = 8, B = 5;
      for (int k = 0; k <= 15; k++) {
        pal685[ii++] = k * 0x111111;
      }
      for (int r = 1; r <= R; r++) {
        rr = Math.min(r * 256 / R, 255);
        for (int g = 1; g <= G; g++) {
          gg = Math.min(g * 256 / G, 255);
          for (int b = 1; b <= B; b++) {
            bb = Math.min(b * 256 / B, 255);
            pal685[ii++] = (rr << 16) | (gg << 8) | bb;
          }
        }
      }
    }
    return pal685;
  }

  /**
   * Draws an Ellipse, using the current foreground color as the outline color.
   * 
   * @param xc x coordinate of the center of the ellipse
   * @param yc y coordinate of the center of the ellipse
   * @param rx radix x of the ellipse
   * @param ry radix y of the ellipse
   */
  @ReplacedByNativeOnDeploy
  public void drawEllipse(int xc, int yc, int rx, int ry) {
    ellipseDrawAndFill(xc, yc, rx, ry, foreColor | alpha, foreColor | alpha, false, false);
  }

  /**
   * Fills an Ellipse, using the current background color as the fill color.
   * 
   * @param xc x coordinate of the center of the ellipse
   * @param yc y coordinate of the center of the ellipse
   * @param rx radix x of the ellipse
   * @param ry radix y of the ellipse
   */
  @ReplacedByNativeOnDeploy
  public void fillEllipse(int xc, int yc, int rx, int ry) {
    ellipseDrawAndFill(xc, yc, rx, ry, backColor | alpha, backColor | alpha, true, false);
  }

  /**
   * Fills a gradient Ellipse, using a gradient from the foreground color to the background color.
   * 
   * @param xc x coordinate of the center of the ellipse
   * @param yc y coordinate of the center of the ellipse
   * @param rx radix x of the ellipse
   * @param ry radix y of the ellipse
   */
  @ReplacedByNativeOnDeploy
  public void fillEllipseGradient(int xc, int yc, int rx, int ry) {
    ellipseDrawAndFill(xc, yc, rx, ry, foreColor | alpha, backColor | alpha, true, true);
  }

  /**
   * Draws an arc, using the current foreground color as the outline color. If startAngle < 0 and endAngle > 359 a
   * whole circle is drawn. A pie differs from an arc so the pie connects the end points of the arc to the center, thus
   * making a closed figure.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param r radix of the circle that contains the arc
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void drawArc(int xc, int yc, int r, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, r, r, startAngle, endAngle, foreColor | alpha, foreColor | alpha, false, false, false);
  }

  /**
   * Draws a pie slice, using the current foreground color as the outline color. If startAngle < 0 and endAngle > 359 a
   * whole circle is drawn. A pie differs from an arc so the pie connects the end points of the arc to the center, thus
   * making a closed figure.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param r radix of the circle that contains the pie
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void drawPie(int xc, int yc, int r, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, r, r, startAngle, endAngle, foreColor | alpha, foreColor | alpha, false, true, false);
  }

  /**
   * Fills a pie slice, using the current background color as the fill color and the current foreground color as the
   * outline color. If startAngle < 0 and endAngle > 359 a whole circle is filled.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param r radix of the circle that contains the pie
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void fillPie(int xc, int yc, int r, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, r, r, startAngle, endAngle, foreColor | alpha, backColor | alpha, true, true, false);
  }

  /**
   * Fills a pie slice, using a gradient from the foreground color to the background color. To draw the outline, you
   * must call drawPie. If startAngle < 0 and endAngle > 359 a whole circle is filled.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param r radix of the circle that contains the pie
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void fillPieGradient(int xc, int yc, int r, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, r, r, startAngle, endAngle, foreColor | alpha, backColor | alpha, true, true, true);
  }

  /**
   * Draws an elliptical arc, using the current foreground color as the outline color. If startAngle < 0 and endAngle >
   * 359 a whole circle is drawn. A pie differs from an arc so the pie connects the end points of the arc to the
   * center, thus making a closed figure.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param rx x radix of the circle that contains the pie
   * @param ry y radix of the circle that contains the pie
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void drawEllipticalArc(int xc, int yc, int rx, int ry, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, endAngle, foreColor | alpha, foreColor | alpha, false, false,
        false);
  }

  /**
   * Draws an elliptical pie slice, using the current foreground color as the outline color. If startAngle < 0 and
   * endAngle > 359 a whole circle is drawn. A pie differs from an arc so the pie connects the end points of the arc to
   * the center, thus making a closed figure.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param rx x radix of the circle that contains the pie
   * @param ry y radix of the circle that contains the pie
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void drawEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, endAngle, foreColor | alpha, foreColor | alpha, false, true,
        false); // guich@402_57: was true,false
  }

  /**
   * Fills an elliptical pie slice, using the current background color as the fill color and the current foreground
   * color as the outline color. If startAngle < 0 and endAngle > 359 a whole circle is filled.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param rx x radix of the circle that contains the pie
   * @param ry y radix of the circle that contains the pie
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void fillEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, endAngle, foreColor | alpha, backColor | alpha, true, true,
        false); // guich@402_57: was true,false
  }

  /**
   * Fills an elliptical pie slice, using a gradient from the foreground color to the background color. To draw the
   * outline, you must call drawEllipticalPie. If startAngle < 0 and endAngle > 359 a whole circle is filled.
   * 
   * @param xc x coordinate of the center of the circle that contains the arc.
   * @param yc y coordinate of the center of the circle that contains the arc.
   * @param rx x radix of the circle that contains the pie
   * @param ry y radix of the circle that contains the pie
   * @param startAngle starting angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param endAngle ending angle of the arc. It must be between 0 and 360 (degrees). 0º is at 3 o'clock.
   */
  @ReplacedByNativeOnDeploy
  public void fillEllipticalPieGradient(int xc, int yc, int rx, int ry, double startAngle, double endAngle) {
    arcPiePointDrawAndFill(xc, yc, rx, ry, startAngle, endAngle, foreColor | alpha, backColor | alpha, true, true,
        true); // guich@402_57: was true,false
  }

  /**
   * Draws a circle, using the current foreground color as the outline color.
   * 
   * @param xc x coordinate of the center of the circle
   * @param yc y coordinate of the center of the circle
   * @param r radix of the circle
   */
  @ReplacedByNativeOnDeploy
  public void drawCircle(int xc, int yc, int r) {
    ellipseDrawAndFill(xc, yc, r, r, foreColor | alpha, foreColor | alpha, false, false);
  }

  /**
   * Fills a circle, using the current background color as the fill color.
   * 
   * @param xc x coordinate of the center of the circle
   * @param yc y coordinate of the center of the circle
   * @param r radix of the circle
   */
  @ReplacedByNativeOnDeploy
  public void fillCircle(int xc, int yc, int r) {
    ellipseDrawAndFill(xc, yc, r, r, backColor | alpha, backColor | alpha, true, false);
  }

  /**
   * Fills a circle, using a gradient from the foreground color to the background color. To draw the outline, you must
   * call drawCircle.
   * 
   * @param xc x coordinate of the center of the circle
   * @param yc y coordinate of the center of the circle
   * @param r radix of the circle
   */
  @ReplacedByNativeOnDeploy
  public void fillCircleGradient(int xc, int yc, int r) {
    ellipseDrawAndFill(xc, yc, r, r, foreColor | alpha, backColor | alpha, true, true);
  }

  /**
   * Gets the pixel color at given position in the 0x00RRGGBB format. Note that if the image has an alpha channel, the
   * format is 0xAARRGGBB, where AA is the alpha value; to get the RRGGBB, use <code>pixel & 0xFFFFFF</code>.
   * 
   * @param x x coordinate of the pixel
   * @param y y coordinate of the pixel
   * @return the color of the pixel or -1 if the pixel lies outside the clip bounds.
   */
  @ReplacedByNativeOnDeploy
  public int getPixel(int x, int y) {
    x += transX;
    y += transY;
    if (clipX1 <= x && x < clipX2 && clipY1 <= y && y < clipY2) {
      return getSurfacePixels(surface)[y * pitch + x] & 0xFFFFFF;
    }
    return -1;
  }

  /**
   * Sets the pixel at the specified position, using the current foreground color as the pixel color.
   * 
   * @param x x coordinate of the pixel
   * @param y y coordinate of the pixel
   */
  @ReplacedByNativeOnDeploy
  public void setPixel(int x, int y) {
    setPixel(x, y, foreColor | alpha);
  }

  private boolean surelyOutsideClip(int x1, int y1, int x2, int y2) {
    int cx1 = clipX1;
    int cx2 = clipX2;
    int cy1 = clipY1;
    int cy2 = clipY2;
    x1 += transX;
    x2 += transX;
    y1 += transY;
    y2 += transY;
    return (x1 < cx1 && x2 < cx1) || (x1 > cx2 && x2 > cx2) || (y1 < cy1 && y2 < cy1) || (y1 > cy2 && y2 > cy2);
  }

  /**
   * Draws a line at any direction, using the current foreground color as the line color.
   * 
   * @param Ax x coordinate of the start of the line.
   * @param Ay y coordinate of the start of the line.
   * @param Bx x coordinate of the end of the line.
   * @param By y coordinate of the end of the line.
   */
  @ReplacedByNativeOnDeploy
  public void drawLine(int Ax, int Ay, int Bx, int By) {
    drawLine(Ax, Ay, Bx, By, foreColor | alpha);
  }

  /**
   * Draws a dotted line in any direction. Uses the current foreground color as the 1st, 3rd, ... pixels and the
   * background color as the 2nd, 4th, ... pixels.
   * 
   * @param Ax x coordinate of the start of the line.
   * @param Ay y coordinate of the start of the line.
   * @param Bx x coordinate of the end of the line.
   * @param By y coordinate of the end of the line.
   */
  @ReplacedByNativeOnDeploy
  public void drawDots(int Ax, int Ay, int Bx, int By) {
    drawDottedLine(Ax, Ay, Bx, By, foreColor | alpha, backColor | alpha);
  }

  /**
   * Draws a rectangle, using the current foreground color as the outline color.
   * 
   * @param x left coordinate of the rectangle
   * @param y top coordinate of the rectangle
   * @param w width of the rectangle. the rectangle is drawn from x to x+w-1.
   * @param h height of the rectangle. the rectangle is drawn from y to y+h-1.
   */
  @ReplacedByNativeOnDeploy
  public void drawRect(int x, int y, int w, int h) {
    drawHLine(x, y, w, foreColor | alpha);
    drawHLine(x, y + h - 1, w, foreColor | alpha);
    drawVLine(x, y, h, foreColor | alpha);
    drawVLine(x + w - 1, y, h, foreColor | alpha);
  }

  /**
   * Fills a rectangle, using the current background color as the fill color.
   * 
   * @param x left coordinate of the rectangle
   * @param y top coordinate of the rectangle
   * @param w width of the rectangle. the rectangle is filled from x to x+w-1.
   * @param h height of the rectangle. the rectangle is filled from y to y+h-1.
   */
  @ReplacedByNativeOnDeploy
  public void fillRect(int x, int y, int w, int h) {
    if (translateAndClip(x, y, w, h)) {
      x = translateAndClipResults[0];
      y = translateAndClipResults[1];
      w = translateAndClipResults[2];
      h = translateAndClipResults[3];
      if (x < clipX1) {
        w -= clipX1 - x;
        x = clipX1;
      } // line start before clip x1
      if ((x + w) > clipX2) {
        w = clipX2 - x; // line stops after clip x2
      }
      if (y < clipY1) {
        h -= clipY1 - y;
        y = clipY1;
      } // line start before clip y1
      if ((y + h) > clipY2) {
        h = clipY2 - y; // line stops after clip y2
      }
      if (x < 0 || y < 0 || w <= 0 || h <= 0) {
        return;
      }
      int[] pix = getSurfacePixels(surface);
      for (x += y * pitch; h-- > 0; x += pitch) {
        Convert.fill(pix, x, x + w, backColor | alpha);
      }
      if (isControlSurface) {
        needsUpdate = true;
      }
    }
  }

  /**
   * Fills a polygon, using the current background color as the fill color. The polygon can be convex or concave.
   * 
   * @param xPoints the array with the x coordinate points
   * @param yPoints the array with the y coordinate points
   * @param nPoints the total number of point to be drawn
   */
  @ReplacedByNativeOnDeploy
  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    if (nPoints > xPoints.length || nPoints > yPoints.length) {
      throw new ArrayIndexOutOfBoundsException("array index out of range at fillPolygon: " + nPoints);
    } else {
      fillPolygon(xPoints, yPoints, 0, nPoints, backColor | alpha, backColor | alpha, false);
    }
  }

  /**
   * Fills a vertical gradient polygon, mixing from the foreground color to the background color. The polygon can be
   * convex or concave.
   * 
   * @param xPoints the array with the x coordinate points
   * @param yPoints the array with the y coordinate points
   * @param nPoints the total number of point to be drawn
   * @since TotalCross 1.01
   */
  @ReplacedByNativeOnDeploy
  public void fillPolygonGradient(int[] xPoints, int[] yPoints, int nPoints) {
    if (nPoints > xPoints.length || nPoints > yPoints.length) {
      throw new ArrayIndexOutOfBoundsException("array index out of range at fillPolygon: " + nPoints);
    } else {
      fillPolygon(xPoints, yPoints, 0, nPoints, foreColor | alpha, backColor | alpha, true);
    }
  }

  /**
   * Draws a polygon, using the current foreground color as the outline color. The polygon is automatically closed.
   * 
   * @param xPoints the array with the x coordinate points
   * @param yPoints the array with the y coordinate points
   * @param nPoints the total number of point to be drawn
   */
  @ReplacedByNativeOnDeploy
  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    if (nPoints > xPoints.length || nPoints > yPoints.length) {
      throw new ArrayIndexOutOfBoundsException("array index out of range at drawPolygon: " + nPoints);
    } else {
      drawPolygon(xPoints, yPoints, 0, nPoints, foreColor | alpha);
      drawLine(xPoints[0], yPoints[0], xPoints[nPoints - 1], yPoints[nPoints - 1], foreColor | alpha);
    }
  }

  /**
   * Draws a dotted rectangle. Use the current foreground color as the 1st, 3rd, ... pixels and the background color as
   * the 2nd, 4th, ... pixels.
   * 
   * @param x left coordinate of the rectangle
   * @param y top coordinate of the rectangle
   * @param w width of the rectangle. the rectangle is drawn from x to x+w-1.
   * @param h height of the rectangle. the rectangle is drawn from y to y+h-1.
   */
  public void drawDottedRect(int x, int y, int w, int h) {
    int x2 = x + w - 1;
    int y2 = y + h - 1;
    if (w <= 0 || h <= 0) {
      return; // guich@566_43
    }
    drawDots(x, y, x2, y);
    drawDots(x, y, x, y2);
    drawDots(x2, y, x2, y2);
    drawDots(x, y2, x2, y2);
  }

  /**
   * Draws a sequence of connected lines, using the current foreground color as the outline color. The lines are
   * <b>not</b> closed.
   * 
   * @param xPoints the array with the x coordinate points
   * @param yPoints the array with the y coordinate points
   * @param nPoints the total number of point to be drawn
   */
  @ReplacedByNativeOnDeploy
  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    if (nPoints > xPoints.length || nPoints > yPoints.length) {
      throw new ArrayIndexOutOfBoundsException("array index out of range at drawPolyline: " + nPoints);
    } else {
      drawPolygon(xPoints, yPoints, 0, nPoints, foreColor | alpha);
    }
  }

  /**
   * Draws a sequence of points, using the current foreground color.
   * 
   * @param xPoints the array with the x coordinate points
   * @param yPoints the array with the y coordinate points
   * @param nPoints the total number of point to be drawn
   */
  @ReplacedByNativeOnDeploy
  public void setPixels(int[] xPoints, int[] yPoints, int nPoints) {
    if (nPoints > xPoints.length || nPoints > yPoints.length) {
      throw new ArrayIndexOutOfBoundsException("array index out of range at drawPolyline: " + nPoints);
    } else {
      setPixels(xPoints, yPoints, nPoints, foreColor | alpha);
    }
  }


  public void drawText(char chars[], int chrStart, int chrCount, int x, int y, boolean shadow, int shadowColor) {
    drawText(new String(chars, chrStart, chrCount), x, y, 0, shadow, shadowColor);
  }

  public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, boolean shadow, int shadowColor) {
    try {
      drawText(sb.toString().substring(chrStart, chrStart + chrCount), x, y, 0, shadow, shadowColor);
    } catch (StringIndexOutOfBoundsException e) {
      drawText(sb.toString().substring(chrStart), x, y, 0, shadow, shadowColor);
    }
  }

  public void drawText(StringBuffer sb, int chrStart, int chrCount, int x, int y, int justifyWidth, boolean shadow, int shadowColor) {
    try {
      drawText(sb.toString().substring(chrStart, chrStart + chrCount), x, y, justifyWidth, shadow, shadowColor);
    } catch (StringIndexOutOfBoundsException e) {
      drawText(sb.toString().substring(chrStart), x, y, justifyWidth, shadow, shadowColor);
    }
  }

  public void drawText(String text, int x, int y, boolean shadow, int shadowColor) {
    drawText(text, x, y, 0, shadow, shadowColor);
  }

  public void drawText(String text, int x, int y, int justifyWidth, boolean shadow, int shadowColor) {
    if (shadow) {
      int old = foreColor;
      foreColor = shadowColor | alpha;
      drawText(text, x - 1, y - 1, justifyWidth);
      drawText(text, x + 1, y - 1, justifyWidth);
      drawText(text, x + 1, y + 1, justifyWidth);
      drawText(text, x - 1, y + 1, justifyWidth);
      foreColor = old;
    }
    drawText(text, x, y, justifyWidth);
  }

  /**
   * Draws a text with the current font and the current foregrount color. On BlackBerry devices, this method is slower
   * than drawText(String,x,y) because it convert the chars to a string.
   * 
   * @param chars the character array to display
   * @param chrStart the start position in array
   * @param chrCount the number of characters to display
   * @param x the left coordinate of the text's bounding box
   * @param y the top coordinate of the text's bounding box
   */
  public void drawText(char chars[], int chrStart, int chrCount, int x, int y) {
    drawText(new String(chars, chrStart, chrCount), x, y);
  }

  /**
   * Draws a text with the current font and the current foregrount color.
   * 
   * @param text the text to be drawn
   * @param x x coordinate of the text
   * @param y y coordinate of the text
   */
  public void drawText(String text, int x, int y) {
    drawText(text, x, y, 0);
  }

  /**
   * Draws a VERTICAL text with the current font and the current foregrount color. This is a shorthand of:
   * 
   * <pre>
   * g.isVerticalText = true;
   * g.drawText(text, x, y);
   * g.isVerticalText = false;
   * </pre>
   * 
   * @param text the text to be drawn
   * @param x x coordinate of the text
   * @param y y coordinate of the text
   */
  public void drawVerticalText(String text, int x, int y) {
    isVerticalText = true;
    drawText(text, x, y, 0);
    isVerticalText = false;
  }

  static final int AA_NO = 0;
  static final int AA_4BPP = 1;
  static final int AA_8BPP = 2;

  /**
   * Draws a text with the current font and the current foregrount color, justifying it to the given width. The text
   * can be vertical if you set the isVerticalText property. Note that vertical text does not allow justification. You
   * can use the justifyWidth to adjust the kerning; depending on the characters you're using, you can decrease the
   * distance using <code>font.fm.height - font.fm.ascent</code> as the justifyWidth.
   * 
   * @see #isVerticalText
   * @see #drawVerticalText(String, int, int)
   * @param text the text to be drawn
   * @param x x coordinate of the text
   * @param y y coordinate of the text
   * @param justifyWidth The width to justify the text to.
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public void drawText(String text, int x, int y, int justifyWidth) {
    int x0 = x;
    int y0 = y;
    int chrCount;
    int xMax, xMin, yMax, yMin;
    if (text == null || (chrCount = text.length()) == 0) {
      return; // guich@200b3: corrected bug if text is null
    }
    int ands8[] = ands8Mask;
    int width;
    int start, current;
    int startBit, currentBit;
    byte[] bitmapTable; // pgr@402_50
    int rowWIB;
    // speed-up
    totalcross.Launcher.UserFont font = (totalcross.Launcher.UserFont) this.font.hv_UserFont;
    totalcross.Launcher.CharBits bits = new totalcross.Launcher.CharBits();
    int height = font.maxHeight;
    int chrStart = 0;
    int pxRB = foreColor & 0xFF00FF;
    int pxG = foreColor & 0x00FF00;
    int extraPixelsPerChar = 0, extraPixelsRemaining = -1, rem;
    boolean isVert = isVerticalText;
    
    if (justifyWidth > 0 && !isVert) {
      while (chrCount > 0 && text.charAt(chrCount - 1) <= ' ') {
        chrCount--;
      }
      if (chrCount == 0) {
        return;
      }
      rem = justifyWidth - this.font.fm.stringWidth(text.toCharArray(), 0, chrCount);
      if (rem > 0) {
        extraPixelsPerChar = rem / chrCount;
        extraPixelsRemaining = rem % chrCount;
      }
    }

    if (isVert) {
      y += transY;
    }
    int incY = height + justifyWidth;
    x0 += transX;
    y0 += transY;
    xMax = xMin = (x0 < clipX1) ? clipX1 : x0;
    yMax = y0 + (isVert ? chrCount * incY : height);
    if (yMax >= clipY2) {
      yMax = clipY2;
    }
    yMin = (y0 < clipY1) ? clipY1 : y0;
    int[] pixels = getSurfacePixels(surface);

    for (int k = 0; k < chrCount; k++) {
      char ch = text.charAt(chrStart++);
      if (ch <= ' ' || ch == 160) {
        if (ch == 160 || ch == ' ' || ch == '\t') {
          if (isVert) {
            y += ch == '\t' ? incY * Font.TAB_SIZE : incY;
          } else {
            x0 += Launcher.instance.getCharWidth(this.font, ch) + extraPixelsPerChar;
            if (k <= extraPixelsRemaining) {
              x0++;
            }
          }
        }
        continue; // for all other control chars, just skip to next
      }
      if (font.ubase == null || ch < font.firstChar || ch > font.lastChar) {
        this.font.hv_UserFont = font = Launcher.instance.getFont(this.font, ch);
      }
      font.setCharBits(ch, bits);
      if (bits.offset == -1) {
        x0 += bits.width + extraPixelsPerChar;
        if (k <= extraPixelsRemaining) {
          x0++;
        }
        continue;
      }
      rowWIB = bits.rowWIB;
      bitmapTable = bits.charBitmapTable;
      width = bits.width;
      if ((xMax = x0 + width) > clipX2) {
        xMax = clipX2;
      }
      int y1 = y, r = 0;
      start = 0;
      if (!isVert) {
        if (y0 < yMin) {
          start += (yMin - y0) * rowWIB;
        }
        y = yMin;
      } else if (y < yMin) {
        r += yMin - y;
        start += (yMin - y) * rowWIB; // guich@tc100b4_1: skip rows before yMin
        y = yMin;
      }
      int rmax = (y + height > yMax) ? yMax - y : height;

      switch (font.antialiased) {
      case AA_NO: {
        start += bits.offset >> 3;
        startBit = bits.offset & 7;

        // draws the char
        for (; r < rmax; start += rowWIB, r++, y++) {
          // draw each row
          int yy = y * pitch;
          current = start;
          currentBit = startBit;
          for (x = x0; x < xMax; x++) {
            // draw each column
            if (bitmapTable != null && (bitmapTable[current] & ands8[currentBit]) != 0 && x >= xMin) {
              pixels[yy + x] = foreColor | alpha;
            }
            if (++currentBit == 8) { // finished this byte?
              currentBit = 0; // reset counter
              current++; // inc current byte
            }
          }
        }
        break;
      }
      case AA_4BPP: {
        start += bits.offset >> 1;
        boolean isNibbleStartingLow = (bits.offset & 1) == 1;
        int transparency;
        // draw the char
        for (; r < rmax; start += rowWIB, r++, y++) {
          // draw each row
          int yy = y * pitch;
          current = start;
          boolean isLowNibble = isNibbleStartingLow;
          for (x = x0; x < xMax; x++) {
            // draw each column
            transparency = isLowNibble ? (bitmapTable[current++] & 0xF) : ((bitmapTable[current] >> 4) & 0xF);
            isLowNibble = !isLowNibble;
            if (transparency == 0 || x < xMin) {
              continue;
            }
            if (transparency == 0xF) {
              pixels[yy + x] = foreColor | alpha;
            } else {
              int i = pixels[yy + x];
              int j = i & 0xFF00FF;
              i &= 0x00FF00;
              pixels[yy + x] = ((j + (((pxRB - j) * transparency) >> 4)) & 0xFF00FF)
                  | ((i + (((pxG - i) * transparency) >> 4)) & 0x00FF00) | alpha;
            }
          }
        }
        break;
      }
      case AA_8BPP: {
        int transparency;
        int[] imgPixels = font.nativeFonts[bits.index].getPixels();
        // draw the char
        for (; r < rmax; start += rowWIB, r++, y++) {
          // draw each row
          int yy = y * pitch;
          current = start;
          for (x = x0; x < xMax; x++) {
            // draw each column
            transparency = (imgPixels[current++] >>> 24) & 0xFF;
            if (transparency == 0 || x < xMin) {
              continue;
            }
            if (transparency == 0xFF) {
              pixels[yy + x] = foreColor | alpha;
            } else {
              int i = pixels[yy + x];
              int j = i & 0xFF00FF;
              i &= 0x00FF00;
              pixels[yy + x] = ((j + (((pxRB - j) * transparency) >> 8)) & 0xFF00FF)
                  | ((i + (((pxG - i) * transparency) >> 8)) & 0x00FF00) | alpha;
            }
          }
        }
        break;
      }
      }
      if (isVert) {
        y = y1 + incY;
        if (y >= yMax) {
          break;
        }
      } else {
        if (xMax >= clipX2) {
          xMax = clipX2;
          break;
        }
        x0 = xMax; // next character
        x0 += extraPixelsPerChar;
        if (k <= extraPixelsRemaining) {
          x0++;
        }
      }
    }
  }

  
  /**
   * Draws a rectangle with rounded corners, using the current foreground color as the outline color.
   * 
   * @param x left coordinate of the rectangle
   * @param y top coordinate of the rectangle
   * @param width width of the rectangle. the rectangle is drawn from x to x+w-1.
   * @param height height of the rectangle. the rectangle is drawn from y to y+h-1.
   * @param r radix of the circle at the corners. If its greater than width/2 or greater than height/2, it will be
   *           adjusted to the minimum of both values.
   */
  @ReplacedByNativeOnDeploy
  public void drawRoundRect(int x, int y, int width, int height, int r) {
    if (r > (width >> 1) || r > (height >> 1)) {
      r = Math.min(width >> 1, height >> 1); // guich@200b4_6: correct bug that crashed the device.
    }
    int w = width - (r << 1);
    int h = height - (r << 1);
    int x1 = x + r, y1 = y + r, x2 = x + width - r - 1, y2 = y + height - r - 1;
    int dec = 3 - (r << 1), xx, yy;
    int c = foreColor;

    drawHLine(x + r, y, w, c); // top
    drawHLine(x + r, y + height - 1, w, c); // bottom
    drawVLine(x, y + r, h, c); // left
    drawVLine(x + width - 1, y + r, h, c); // right

    for (xx = 0, yy = r; xx <= yy; xx++) {
      setPixel(x2 + xx, y2 + yy, c);
      setPixel(x2 + xx, y1 - yy, c);
      setPixel(x1 - xx, y2 + yy, c);
      setPixel(x1 - xx, y1 - yy, c);

      setPixel(x2 + yy, y2 + xx, c);
      setPixel(x2 + yy, y1 - xx, c);
      setPixel(x1 - yy, y2 + xx, c);
      setPixel(x1 - yy, y1 - xx, c);
      if (dec >= 0) {
        dec += -4 * (yy--) + 4;
      }
      dec += 4 * xx + 6;
    }
  }

  /**
   * Fills a rectangle with rounded corners, using the current background color as the fill color.
   * 
   * @param xx left coordinate of the rectangle
   * @param yy top coordinate of the rectangle
   * @param width width of the rectangle. the rectangle is filled from x to x+w-1.
   * @param height height of the rectangle. the rectangle is filled from y to y+h-1.
   * @param r radix of the circle at the corners. If its greater than width/2 or greater than height/2, it will be
   *           adjusted to the minimum of both values.
   */
  @ReplacedByNativeOnDeploy
  public void fillRoundRect(int xx, int yy, int width, int height, int r) {
    int px1, px2, py1, py2, xm, ym, x, y = 0, i, x2, e2, err;
    if (r > (width / 2) || r > (height / 2)) {
      r = Math.min(width / 2, height / 2); // guich@200b4_6: correct bug that crashed the device.
    }
    int c = backColor | alpha;

    x = -r;
    err = 2 - 2 * r;

    px1 = xx + r;
    py1 = yy + r;
    px2 = xx + width - r - 1;
    py2 = yy + height - r - 1;

    // fill area outside round borders
    height -= 2 * r;
    yy += r;
    while (height-- > 0) {
      drawHLine(xx, yy++, width, c);
    }

    r = 1 - err;
    do {
      i = 255 - 255 * Math.abs(err - 2 * (x + y) - 2) / r;

      drawLine(px1 + x + 1, py1 - y, px2 - x - 1, py1 - y, c);
      drawLine(px1 + x + 1, py2 + y, px2 - x - 1, py2 + y, c);

      if (i < 256 && i > 0) {
        xm = px2;
        ym = py2;
        setPixel(xm - x, ym + y, backColor | (i << 24)); // br
        xm = px1;
        ym = py2;
        setPixel(xm - y, ym - x, backColor | (i << 24)); // bl
        xm = px1;
        ym = py1;
        setPixel(xm + x, ym - y, backColor | (i << 24)); // tl
        xm = px2;
        ym = py1;
        setPixel(xm + y, ym + x, backColor | (i << 24)); // tr
      }
      e2 = err;
      x2 = x;
      if (err + y > 0) {
        i = 255 - 255 * (err - 2 * x - 1) / r;
        if (i < 256 && i > 0) {
          xm = px2;
          ym = py2;
          setPixel(xm - x, ym + y + 1, backColor | (i << 24));
          xm = px1;
          ym = py2;
          setPixel(xm - y - 1, ym - x, backColor | (i << 24));
          xm = px1;
          ym = py1;
          setPixel(xm + x, ym - y - 1, backColor | (i << 24));
          xm = px2;
          ym = py1;
          setPixel(xm + y + 1, ym + x, backColor | (i << 24));
        }
        err += ++x * 2 + 1;
      }
      if (e2 + x2 <= 0) {
        i = 255 - 255 * (2 * y + 3 - e2) / r;
        if (i < 256 && i > 0) {
          xm = px2;
          ym = py2;
          setPixel(xm - x2 - 1, ym + y, backColor | (i << 24));
          xm = px1;
          ym = py2;
          setPixel(xm - y, ym - x2 - 1, backColor | (i << 24));
          xm = px1;
          ym = py1;
          setPixel(xm + x2 + 1, ym - y, backColor | (i << 24));
          xm = px2;
          ym = py1;
          setPixel(xm + y, ym + x2 + 1, backColor | (i << 24));
        }
        err += ++y * 2 + 1;
      }
    } while (x < 0);
  }

  /**
   * Sets the clipping rectangle, translated to the current translated origin. Anything drawn outside of the
   * rectangular area specified will be clipped. Setting a clip overrides any previous clip. This clipping rectangle
   * affects all the drawing operations.
   * 
   * @param x left coordinate of the rectangle
   * @param y top coordinate of the rectangle
   * @param w width of the rectangle. the rectangle is filled from x to x+w-1.
   * @param h height of the rectangle. the rectangle is filled from y to y+h-1.
   */
  public void setClip(int x, int y, int w, int h) {
    int clipX1 = x + transX;
    int clipY1 = y + transY;
    int clipX2 = clipX1 + w;
    int clipY2 = clipY1 + h;

    if (clipX1 < minX) {
      clipX1 = minX;
    }
    if (clipY1 < minY) {
      clipY1 = minY;
    }
    if (clipX1 > maxX) {
      clipX1 = maxX;
    }
    if (clipY1 > maxY) {
      clipY1 = maxY;
    }

    if (clipX2 < minX) {
      clipX2 = minX;
    }
    if (clipY2 < minY) {
      clipY2 = minY;
    }
    if (clipX2 > maxX) {
      clipX2 = maxX;
    }
    if (clipY2 > maxY) {
      clipY2 = maxY;
    }

    int surfW = isControlSurface ? Settings.screenWidth : surface.getWidth();
    int surfH = isControlSurface ? Settings.screenHeight : surface.getHeight();

    if (clipX2 > surfW) {
      clipX2 = surfW;
    }
    if (clipY2 > surfH) {
      clipY2 = surfH;
    }

    this.clipX1 = clipX1;
    this.clipY1 = clipY1;
    this.clipX2 = clipX2;
    this.clipY2 = clipY2;
  }

  /**
   * Sets the clipping rectangle, translated to the current translated origin. Anything drawn outside of the
   * rectangular area specified will be clipped. Setting a clip overrides any previous clip. This clipping rectangle
   * affects all the drawing operations.
   * 
   * @param r the rectangle with the clipping coordinates
   */
  public void setClip(Rect r) {
    setClip(r.x, r.y, r.width, r.height);
  }

  /**
   * Gets the current clipping rectangle.
   * 
   * @param r a Rect object where the clip coordinates will be stored and returned.
   */
  public Rect getClip(Rect r) {
    r.x = clipX1 - transX; // guich@200b4_38: fixed getClip - and guich@241_5: changed + to -
    r.y = clipY1 - transY;
    r.width = clipX2 - clipX1; // guich@300_36: removed +1 here and in line below
    r.height = clipY2 - clipY1;
    return r;
  }

  /** Returns the clip's width */
  public int getClipWidth() {
    return clipX2 - clipX1;
  }

  /** Returns the clip's height */
  public int getClipHeight() {
    return clipY2 - clipY1;
  }

  /**
   * Clips the specified rectangle to the clipping bounds.
   * 
   * @param r The coordinates of the rectangle that are to be adjusted to match the clipping bounds.
   * @return false if the specified rectangle does not intersects the clipping bounds or the new coordinates of the
   *         clipped rectangle.
   */
  public boolean clip(Rect r) {
    if (translateAndClip(r.x, r.y, r.width, r.height)) {
      r.x = translateAndClipResults[0] - transX;
      r.y = translateAndClipResults[1] - transY;
      r.width = translateAndClipResults[2];
      r.height = translateAndClipResults[3];
      return true;
    }
    return false;
  }

  /**
   * Clears the current clipping rectangle. This allows drawing to occur anywhere on the current surface.
   */
  public void clearClip() {
    clipX1 = minX;
    clipY1 = minY;
    clipX2 = maxX;
    clipY2 = maxY;
  }

  /**
   * Translates the origin of the of the current coordinate system by the given dx and dy. The final translation coords
   * cannot be lesser than 0.
   * 
   * @param dx the delta x of the x coordinate translation. the new x origin coordinate will be x + dx
   * @param dy the delta y of the y coordinate translation. the new y origin coordinate will be y + dy
   */
  public void translate(int dx, int dy) {
    transX += dx;
    transY += dy;
  }

  /**
   * Copies a rectangular area from a surface to the given coordinates on the current surface. The copy operation is
   * performed by combining pixels according to the setting of the current drawing operation. The same surface can be
   * used as a source and destination to implement quick scrolling.
   *
   * @param surface the surface to copy from
   * @param x the source x coordinate
   * @param y the source y coordinate
   * @param width the width of the area on the source surface
   * @param height the height of the area on the source surface
   * @param dstX the destination x location on the current surface
   * @param dstY the destination y location on the current surface
   */
  @ReplacedByNativeOnDeploy
  public void copyRect(GfxSurface surface, int x, int y, int width, int height, int dstX, int dstY) {
    int[] srcPixels = (int[]) getSurfacePixels(surface);
    if (srcPixels != null) {
      drawSurface(srcPixels, surface, x, y, width, height, dstX, dstY, true, surface.getX(), surface.getY(),
          surface.getWidth(), surface.getHeight());
    }
  }

  /** Sets the current font for operations that draw text. */
  public void setFont(Font font) {
    if (font != null && font != this.font) {
      this.font = font;
    }
  }

  /** Returns the current translation from the origin for this Graphics object. */
  public Coord getTranslation() {
    return new Coord(transX, transY);
  }

  /**
   * Draws a rectangle with a gradient fill and optional rounded corners
   * 
   * @param startX x coordinate of the top-left pixel
   * @param startY y coordinate of the top-left pixel
   * @param endX x coordinate of the bottom-right pixel
   * @param endY y coordinate of the bottom-right pixel
   * @param topLeftRadius Radius (in pixels) of the top-left corner. A value of zero means that this corner will not be
   *           rounded
   * @param topRightRadius Radius (in pixels) of the top-right corner. A value of zero means that this corner will not
   *           be rounded
   * @param bottomLeftRadius Radius (in pixels) of the bottom-left corner. A value of zero means that this corner will
   *           not be rounded
   * @param bottomRightRadius Radius (in pixels) of the bottom-right corner. A value of zero means that this corner
   *           will not be rounded
   * @param startColor The color value for the top of the rectangle
   * @param endColor The color value for the bottom of the rectangle
   * @param vertical True to be a vertical gradient, false to be a horizontal one
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public void drawRoundGradient(int startX, int startY, int endX, int endY, int topLeftRadius, int topRightRadius,
      int bottomLeftRadius, int bottomRightRadius, int startColor, int endColor, boolean vertical) {
    if (startX > endX) {
      int temp = startX;
      startX = endX;
      endX = temp;
    }
    if (startY > endY) {
      int temp = startY;
      startY = endY;
      endY = temp;
    }
    int numSteps = Math.max(1, vertical ? (endY - startY) : (endX - startX)); // guich@tc110_11: support horizontal gradient - guich@gc114_41: prevent div by 0 if numsteps is 0
    int startRed = (startColor >> 16) & 0xFF;
    int startGreen = (startColor >> 8) & 0xFF;
    int startBlue = startColor & 0xFF;
    int endRed = (endColor >> 16) & 0xFF;
    int endGreen = (endColor >> 8) & 0xFF;
    int endBlue = endColor & 0xFF;
    int redInc = ((endRed - startRed) << 16) / numSteps;
    int greenInc = ((endGreen - startGreen) << 16) / numSteps;
    int blueInc = ((endBlue - startBlue) << 16) / numSteps;
    int red = startRed << 16;
    int green = startGreen << 16;
    int blue = startBlue << 16;
    int leftOffset = 0;
    int rightOffset = 0;
    boolean hasRadius = (topLeftRadius + topRightRadius + bottomLeftRadius + bottomRightRadius) > 0;
    for (int i = 0; i < numSteps; i++) {
      if (hasRadius) {
        leftOffset = rightOffset = 0;

        if (topLeftRadius > 0 && i < topLeftRadius) {
          leftOffset = getOffset(topLeftRadius, topLeftRadius - i - 1) - 1;
        } else if (bottomLeftRadius > 0 && i > numSteps - bottomLeftRadius) {
          leftOffset = getOffset(bottomLeftRadius, bottomLeftRadius - (numSteps - i + 1)) - 1;
        }

        if (topRightRadius > 0 && i < topRightRadius) {
          rightOffset = getOffset(topRightRadius, topRightRadius - i - 1) - 1;
        } else if (bottomRightRadius > 0 && i > numSteps - bottomRightRadius) {
          rightOffset = getOffset(bottomRightRadius, bottomRightRadius - (numSteps - i + 1)) - 1;
        }

        if (leftOffset < 0) {
          leftOffset = 0;
        }
        if (rightOffset < 0) {
          rightOffset = 0;
        }
      }

      int fc = foreColor = (red & 0xFF0000) | ((green >> 8) & 0x00FF00) | ((blue >> 16) & 0xFF);
      if (vertical) {
        drawLine(startX + leftOffset, startY + i, endX - rightOffset, startY + i);
        if (rightOffset != 0) {
          drawFadedPixel(endX - rightOffset + 1, startY + i, fc);
        }
        if (leftOffset != 0) {
          drawFadedPixel(startX + leftOffset - 1, startY + i, fc);
        }
      } else {
        drawLine(startX + i, startY + leftOffset, startX + i, endY - rightOffset);
        drawFadedPixel(startX + i, startY + leftOffset - 1, fc);
        drawFadedPixel(startX + i, endY - rightOffset + 1, fc);
      }

      red += redInc;
      green += greenInc;
      blue += blueInc;
    }
  }

  private void drawFadedPixel(int xx, int yy, int color) {
    foreColor = color | 0x14000000;// Color.interpolate(color,getPixel(xx,yy),20);
    setPixel(xx, yy);
  }

  private static int getOffset(int radius, int y) {
    return radius - (int) Math.sqrt(radius * radius - y * y);
  }

  /**
   * Draws the image in the given position with the given draw operation and back color. Note that the current draw
   * operation is not changed, neither the current back color.
   * <P>
   * Deprecated use: backColor forces the transparentColor of the Image, and is ignored when drawOp is DRAW_PAINT.<BR>
   * <P>
   * Preferred use: when <code>backColor</code> is -1, the transparentColor is the one which has been defined for this
   * Image, using <code>Image.transparentColor</code>. Note that for a GIF Image, the transparent pixel is
   * automatically set from the image's information.
   * <P>
   * The parameter doClip must be used with caution. If false, it will not do any clipping checks (what may improve
   * drawings in 8%). Set it to true only when you're sure that the image will not pass the current surface bounds.
   * Otherwise, be prepared to even <b>hard-reset</b> your device!
   * 
   * @since SuperWaba 3.3
   */
  @ReplacedByNativeOnDeploy
  public void drawImage(totalcross.ui.image.Image image, int x, int y, boolean doClip) {
    int[] srcPixels = (int[]) image.getPixels();
    if (srcPixels != null) {
      drawSurface(srcPixels, image, 0, 0, image.getWidth(), image.getHeight(), x, y, doClip, 0, 0, image.getWidth(),
          image.getHeight());
    }
  }

  /**
   * Copies a part of the given source image to here at the given position with the given draw operation and back
   * color. Note that the current draw operation is not changed, neither the current back color.
   * <P>
   * Deprecated use: backColor forces the transparentColor of the Image, and is ignored when drawOp is DRAW_PAINT.<BR>
   * <P>
   * Preferred use: when <code>backColor</code> is -1, the transparentColor is the one which has been defined for this
   * Image, using <code>Image.transparentColor</code>. Note that for a GIF Image, the transparent pixel is
   * automatically set from the image's information.
   * <P>
   * The parameter doClip must be used with caution. If false, it will not do any clipping checks (what may improve
   * drawings in 8%). Set it to true only when you're sure that the image will not pass the current surface bounds.
   * Otherwise, be prepared to even <b>hard-reset</b> your device!
   * 
   * @since SuperWaba 3.3
   */
  @ReplacedByNativeOnDeploy
  public void copyImageRect(totalcross.ui.image.Image src, int x, int y, int width, int height, boolean doClip) {
    int[] srcPixels = (int[]) src.getPixels();
    if (srcPixels != null) {
      drawSurface(srcPixels, src, x, y, width, height, 0, 0, doClip, 0, 0, src.getWidth(), src.getHeight());
    }
  }

  /**
   * Draws an image at the given absolute x and y coordinates. The x and y coordinates must be absolute values;
   * LEFT/RIGHT/TOP/BOTTOM/CENTER are not allowed.
   * 
   * @see #copyRect
   */
  @ReplacedByNativeOnDeploy
  public void drawImage(totalcross.ui.image.Image src, int x, int y) {
    // guich@tc100b5_5: refactored to use the transparent color
    int[] srcPixels = (int[]) src.getPixels();
    if (srcPixels != null) {
      drawSurface(srcPixels, src, 0, 0, src.getWidth(), src.getHeight(), x, y, true, 0, 0, src.getWidth(),
          src.getHeight());
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  private static Hashtable ht3dColors = new Hashtable(83);

  /**
   * Prior to use draw3dRect, you must compute the colors. This must be done everytime you change the enabled state or
   * the fore/back colors. This can be easily achieved if you extend the onColorsChanged method (but don't forget to
   * call super.onColorsChanged).
   */
  public static void compute3dColors(boolean enabled, int backColor, int foreColor, int fourColors[]) {
    if (backColor < 0 || foreColor < 0) {
      return;
    }
    StringBuffer sbc = new StringBuffer(20);
    String key = sbc.append(enabled).append(backColor).append(',').append(foreColor).toString();
    int four[] = (int[]) ht3dColors.get(key);
    if (four == null) {
      four = new int[4];
      four[0] = backColor; // outside topLeft - BRIGHT
      four[1] = Color.brighter(backColor, Color.LESS_STEP); // inside topLeft - WHITE
      if (four[0] == four[1]) {
        four[1] = Color.darker(four[0], Color.LESS_STEP);
      }
      four[0] = Color.darker(four[0], Color.HALF_STEP); // in 16 colors, make the buttons more 3d
      four[2] = Color.brighter(foreColor, enabled ? Color.LESS_STEP : Color.FULL_STEP); // inside bottomRight - DARK - guich@tc122_48: use full_step if not enabled
      four[3] = foreColor; // outside bottomRight - BLACK
      if (!enabled) {
        four[3] = four[2];
        four[2] = Color.brighter(four[2], Color.LESS_STEP);
      }
      ht3dColors.put(key, four);
    }
    fourColors[0] = four[0];
    fourColors[1] = four[1];
    fourColors[2] = four[2];
    fourColors[3] = four[3];
  }

  /**
   * Draws a shaded rectangle.
   */
  public void drawVistaRect(int x, int y, int width, int height, int topColor, int rightColor, int bottomColor,
      int leftColor) {
    int x1 = x + 1;
    int y1 = y + 1;
    int x2 = x + width - 1;
    int y2 = y + height - 1;
    foreColor = topColor | alpha;
    drawLine(x1, y, x2 - 1, y, foreColor);
    foreColor = rightColor | alpha;
    drawLine(x2, y1, x2, y2 - 1, foreColor);
    foreColor = bottomColor | alpha;
    drawLine(x1, y2, x2 - 1, y2, foreColor);
    foreColor = leftColor | alpha;
    drawLine(x, y1, x, y2 - 1, foreColor);
  }

  /**
   * Fills a shaded rectangle. Used to draw many Vista user interface style controls
   */
  public void fillVistaRect(int x, int y, int width, int height, int back, boolean invert, boolean rotate) {
    int step = UIColors.vistaFadeStep;
    int s = rotate ? width : height;
    int mid = s * 5 / 11;
    int ini1 = back, end1 = Color.darker(ini1, 3 * step);
    int ini2 = Color.darker(end1, step), end2 = Color.darker(end1, step * 7);
    if (rotate) {
      fillShadedRect(x, y, mid, height, !invert, rotate, invert ? ini2 : ini1, invert ? end2 : end1, 100);
      fillShadedRect(x + mid, y, width - mid, height, !invert, rotate, invert ? ini1 : ini2, invert ? end1 : end2, 100);
    } else {
      if (invert) {
        fillShadedRect(x, y, width, mid, true, rotate, end2, ini2, 100);
        fillShadedRect(x, y + mid, width, height - mid, true, rotate, end1, ini1, 100);
      } else {
        fillShadedRect(x, y, width, mid, true, rotate, ini1, end1, 100);
        fillShadedRect(x, y + mid, width, height - mid, true, rotate, ini2, end2, 100);
      }
    }
  }

  /**
   * Fills a shaded rectangle. Used to draw many Android user interface style controls
   * 
   * @factor Ranges from 0 to 100
   */
  public void fillShadedRect(int x, int y, int width, int height, boolean invert, boolean rotate, int c1, int c2,
      int factor) {
    int dim = rotate ? width : height, dim0 = dim;
    int y0 = rotate ? x : y;
    int hh = rotate ? x + dim : y + dim;
    dim <<= 16;
    if (dim0 == 0) {
      return;
    }
    int inc = dim / dim0;
    int lineS = (inc >> 16) + 1;
    int line0 = 0;
    int lastF = -1;
    // now paint the shaded area
    for (int c = 0; line0 < dim; c++, line0 += inc) {
      int i = c >= dim0 ? dim0 - 1 : c;
      int f = (invert ? dim0 - 1 - i : i) * factor / dim0;
      if (f != lastF) {
        backColor = Color.interpolate(c1, c2, lastF = f) | alpha;
      }
      int yy = y0 + (line0 >> 16);
      int k = hh - yy;
      if (k > lineS) {
        k = lineS;
      }
      if (!rotate) {
        fillRect(x, yy, width, k);
      } else {
        fillRect(yy, y, k, height);
      }
    }
  }

  /**
   * Draws a 3d rect, respecting the current user interface style.
   * 
   * @param x the x position
   * @param y the y position
   * @param width the width of the rectangle
   * @param height the height of the rectangle
   * @param type one of the R3D constants
   * @param yMirror no longer used
   * @param simple if true, a simple rectangle will be drawn
   * @param fourColors the four colors used if ui style is WinCE
   * @see #R3D_EDIT
   * @see #R3D_LOWERED
   * @see #R3D_RAISED
   * @see #R3D_CHECK
   * @see #R3D_SHADED
   */
  public void draw3dRect(int x, int y, int width, int height, byte type, boolean yMirror, boolean simple,
      int[] fourColors) {
    if (type == R3D_FILL) {
      backColor = backColor | alpha;
      fillRect(x, y, width, height);
    } else if (type == R3D_SHADED) {
      boolean menu = simple; // is menu?
      drawLine(menu ? 0 : 1, 0, menu ? width - 1 : width - 3, 0, foreColor | alpha);
      drawLine(0, 1, 0, height - 3, foreColor | alpha);
      drawLine(width - 2, 1, width - 2, height - 3, foreColor | alpha);
      drawLine(width - 1, menu ? 1 : 2, width - 1, height - 3, foreColor | alpha);
      drawLine(1, height - 2, width - 2, height - 2, foreColor | alpha);
      drawLine(2, height - 1, width - 3, height - 1, foreColor | alpha);
    } else {
      switch (Settings.uiStyle) {
      case Settings.Flat:
        foreColor = fourColors[2] | alpha;
        switch (type) {
        case R3D_CHECK:
          drawRect(x, y, width, height);
          break;
        case R3D_EDIT:
          drawRect(x, y, x + width, y + height);
          break;
        case R3D_LOWERED:
          backColor = fourColors[1] | alpha; // dont move it from here!
          fillRect(x, y, width, height);
        case R3D_RAISED:
          drawRect(x, y, width, height);
          break;
        }
        break;
      case Settings.Holo:
      case Settings.Android:
      case Settings.Vista:
      case Settings.Material:
        foreColor = fourColors[2] | alpha;
        switch (type) {
        case R3D_CHECK:
          foreColor = fourColors[0] | alpha;
          drawRect(x, y, width, height);
          break;
        case R3D_EDIT:
          drawRect(x, y, x + width, y + height);
          break;
        case R3D_RAISED:
          drawVistaRect(x, y, width, height, fourColors[1], fourColors[1], fourColors[2], fourColors[3]);
          break;
        case R3D_LOWERED:
          drawVistaRect(x, y, width, height, fourColors[2], fourColors[3], fourColors[1], fourColors[1]); // guich@tc122_10; 2113 -> 2311
          break;
        }
        break;
      }
    }
  }

  @Override
  public String toString() {
    Rect r = new Rect();
    getClip(r);
    return super.toString() + ", Clip:" + r + ", translation from origin: " + getTranslation();
  }

  /**
   * Draws an arrow using the current foreground color.
   * 
   * @param x the x position from where the draw will start
   * @param y the y position from where the draw will start
   * @param h is the height (or width, if left/right arrows) in pixels. 1st line has 1 pixel, 2nd line has 3, 3rd line
   *           has 5, etc. Must be >= 2 or no arrow is drawn.
   * @param type one of the ARROW_ constants.
   * @param pressed if true, x and y are shifted by one pixel
   * @param foreColor the foreground color to be used. This Graphic's fore color is changed after this method is
   *           called.
   *           <p>
   *           Example:
   * 
   *           <pre>
   * public void onPaint(Graphics g) {
   *    g.drawArrow(30,30,8, Graphics.ARROW_LEFT, false, Color.BLACK );
   * ...
   *           </pre>
   * 
   * @see #ARROW_DOWN
   * @see #ARROW_LEFT
   * @see #ARROW_RIGHT
   * @see #ARROW_UP
   */
  public void drawArrow(int x, int y, int h, byte type, boolean pressed, int foreColor) {
    foreColor |= alpha;
    this.foreColor = foreColor;
    if (pressed) {
      x++;
      y++;
    }
    int step = 1;
    if (type == ARROW_RIGHT || type == ARROW_LEFT) {
      if (type == ARROW_LEFT) {
        x += h - 1;
        step = -1;
      }
      h--;
      while (h >= 0) {
        drawLine(x, y, x, y + (h << 1), foreColor);
        x += step;
        y++;
        h--;
      }
    } else {
      if (type == ARROW_UP) {
        y += h - 1;
        step = -1;
      }
      h--;
      while (h >= 0) {
        drawLine(x, y, x + (h << 1), y, foreColor);
        y += step;
        x++;
        h--;
      }
    }
  }

  /**
   * Gets the coordinates where the specified point lies. This is useful if you're drawing a pie chart and want to know
   * where to draw the label. The computed values are cached to speedup the process.
   * 
   * @param xc x coordinate of the center of the circle
   * @param yc y coordinate of the center of the circle
   * @param rx x radix of the circle that contains the point. Maximum rx value is 8000; above this, the result is
   *           unpredictable.
   * @param ry y radix of the circle that contains the point. Maximum ry value is 8000; above this, the result is
   *           unpredictable.
   * @param angle angle of the point. It should be between 0 and 360 (degrees). 0º is at 3 o'clock.
   * @param out the Coord with the x,y points
   */
  public void getAnglePoint(int xc, int yc, int rx, int ry, int angle, Coord out) {
    if (angle < 0 || angle >= 360) {
      while (angle < 0) {
        angle += 360;
      }
      while (angle >= 360) {
        angle -= 360;
      }
    }

    int nPoints = 0;
    // this algorithm was created by Guilherme Campos Hazan
    double ppd;
    int index, i;
    int size = 0;
    // step 0: correct angle values
    int[] xPoints = cxPoints;
    int[] yPoints = cyPoints;
    // step 0: if possible, use cached results
    boolean sameR = rx == lastcRX && ry == lastcRY;
    if (!sameR) {
      long t1 = (long) rx * rx, t2 = t1 << 1, t3 = t2 << 1;
      long t4 = (long) ry * ry, t5 = t4 << 1, t6 = t5 << 1;
      long t7 = (long) rx * t5, t8 = t7 << 1, t9 = 0;
      long d1 = t2 - t7 + (t4 >> 1); // error terms
      long d2 = (t1 >> 1) - t8 + t5;
      int x = rx, y = 0; // ellipse points

      if (sameR) {
        size = lastcSize;
      } else {
        // step 1: computes how many points the circle has (computes only 45 degrees and mirrors the rest)
        // intermediate terms to speed up loop
        while (d2 < 0) { // til slope = -1
          t9 += t3;
          if (d1 < 0) { // move straight up
            d1 += t9 + t2;
            d2 += t9;
          } else { // move up and left
            x--;
            t8 -= t6;
            d1 += t9 + t2 - t8;
            d2 += t9 + t5 - t8;
          }
          size++;
        }

        // rest of top right quadrant
        do {
          x--; // always move left here
          t8 -= t6;
          if (d2 < 0) { // move up and left
            t9 += t3;
            d2 += t9 + t5 - t8;
          } else {
            d2 += t5 - t8;
          }
          size++;

        } while (x >= 0);
      }
      // step 2: computes how many points per degree
      ppd = (double) size / 90.0;
      // step 3: create space in the buffer so it can save 1/4 of the circle
      if (nPoints < size) {
        cxPoints = cyPoints = null;
        xPoints = cxPoints = new int[size];
        yPoints = cyPoints = new int[size];
      }
      // step 4: stores all the 1/4 circle in the array. the odd arcs are drawn in reverse order
      // intermediate terms to speed up loop
      t2 = t1 << 1;
      t3 = t2 << 1;
      t8 = t7 << 1;
      t9 = 0;
      d1 = t2 - t7 + (t4 >> 1); // error terms
      d2 = (t1 >> 1) - t8 + t5;
      x = rx;

      i = 0;
      while (d2 < 0) { // til slope = -1
                       // save 4 points using symmetry
        xPoints[i] = x;
        yPoints[i] = y;
        i++;

        y++; // always move up here
        t9 += t3;
        if (d1 < 0) { // move straight up
          d1 += t9 + t2;
          d2 += t9;
        } else { // move up and left
          x--;
          t8 -= t6;
          d1 += t9 + t2 - t8;
          d2 += t9 + t5 - t8;
        }
      }

      // rest of top right quadrant
      do {
        // save 4 points using symmetry
        // guich@340_3: added clipping
        xPoints[i] = x;
        yPoints[i] = y;
        i++;

        x--; // always move left here
        t8 -= t6;
        if (d2 < 0) { // move up and left
          y++;
          t9 += t3;
          d2 += t9 + t5 - t8;
        } else {
          d2 += t5 - t8;
        }
      } while (x >= 0);
      // save last arguments
      lastcPPD = ppd;
      lastcSize = size;
      lastcRX = rx;
      lastcRY = ry;
    } else {
      ppd = lastcPPD;
      size = lastcSize;
    }
    // step 5: computes the start and end indexes that will become part of the arc
    index = (int) (ppd * angle + 0.5);
    int x = 0;
    int y = 0;
    switch (angle / 90) {
    case 0:
      x = +xPoints[index];
      y = -yPoints[index];
      break;
    case 1:
      x = -xPoints[size - (index - size) - 1];
      y = -yPoints[size - (index - size) - 1];
      break;
    case 2:
      x = -xPoints[index - 2 * size];
      y = +yPoints[index - 2 * size];
      break;
    case 3:
      int idx = size - (index - 3 * size) - 1;
      if (idx < 0) {
        idx = 0;
      }
      x = +xPoints[idx];
      y = +yPoints[idx];
      break;
    }

    out.x = xc + x;
    out.y = yc + y;
  }

  /**
   * Gets raw RGB data from a rectangular region of the surface this graphic s drawing to and stores it in the provided
   * array.
   * 
   * @param data Array where the RGB data will be stored.
   * @param offset Offset into the data array to start writing to.
   * @param x Left edge of the region to copy from.
   * @param y Top edge of the region to copy from.
   * @param w Width of the region to copy from.
   * @param h Height of the region to copy from.
   * @return The total number of pixels copied from the surface to the array.
   * @throws ArrayIndexOutOfBoundsException If the data array is not big enough to hold the RGB values.
   */
  @ReplacedByNativeOnDeploy
  public int getRGB(int[] data, int offset, int x, int y, int w, int h) {
    if (!translateAndClip(x, y, w, h)) {
      return 0;
    } else {
      int[] results = translateAndClipResults;
      x = results[0];
      y = results[1];
      w = results[2];
      h = results[3];

      int[] pixels = getSurfacePixels(surface);
      int inc = pitch, pos = y * inc + x, count = w * h;

      for (; h-- > 0; pos += inc, offset += w) {
        System.arraycopy(pixels, pos, data, offset, w);
      }

      return count;
    }
  }

  /**
   * Sets raw RGB data of a rectangular region of the surface this graphic s drawing to copying it from the provided
   * array.
   * <p>
   * Important: the data is in RAW format, so this method only works if the data was retrieved using getRGB.
   * 
   * @param data Array where the RGB data is stored.
   * @param offset Offset into the data array to start reading from.
   * @param x Left edge of the region to copy to.
   * @param y Top edge of the region to copy to.
   * @param w Width of the region to copy to.
   * @param h Height of the region to copy to.
   * @return The total number of pixels copied from the array to the surface.
   * @throws ArrayIndexOutOfBoundsException If the data array has not enough RGB values.
   */
  @ReplacedByNativeOnDeploy
  public int setRGB(int[] data, int offset, int x, int y, int w, int h) {
    if (!translateAndClip(x, y, w, h)) {
      return 0;
    } else {
      int[] results = translateAndClipResults;
      x = results[0];
      y = results[1];
      w = results[2];
      h = results[3];

      int[] pixels = getSurfacePixels(surface);
      int inc = pitch, pos = y * inc + x, count = w * h;

      for (; h-- > 0; pos += inc, offset += w) {
        Vm.arrayCopy(data, offset, pixels, pos, w);
      }

      return count;
    }
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  /////////////////////////////////////////////////////////////////////////////////
  // I N T E R N A L R O U T I N E S //
  /////////////////////////////////////////////////////////////////////////////////

  //////////////////// METHODS TAKED FROM THE TOTALCROSS VIRTUAL MACHINE //////////
  // copy the area x,y,width,height of the bitmap bmp with dimensions bmpW,bmpH to the (current active) screen location
  //////////////////// dstX,dstY
  private void drawSurface(int[] pixels, Object srcSurface, int x, int y, int width, int height, int dstX, int dstY,
      boolean doClip, int bmpX, int bmpY, int bmpW, int bmpH) {
    boolean isScaled = false;
    if (srcSurface instanceof Image) {
      Image img = (Image) srcSurface;
      if (img.hwScaleH != 1 || img.hwScaleW != 1) {
        try {
          img = img.hwScaleW < 1 && img.hwScaleH < 1 ? img.smoothScaledBy(img.hwScaleW, img.hwScaleH)
              : img.scaledBy(img.hwScaleW, img.hwScaleH);
          srcSurface = img;
          pixels = img.getPixels();
          isScaled = true;
        } catch (ImageException ie) {
        }
      }
    }
    try {
      // petrus@450_7: revamp of the drawBitmap clipping algorithm
      int bmpPt, screenPt;
      int i, j;
      dstX += transX;
      dstY += transY;

      if (!doClip) {
        /*
         * | Although doClip is not set, we make sure that the area of the bitmap | that we want to copy IS inside
         * its area | If doClip is set, then the clipping computation below ensures | the sanity of the operation
         * (and is quite as fast). | DoClip is important for game sprites where programmers ensure that the | sprite
         * will never pass screen boundaries.
         */
        if ((x <= -width) || (x >= bmpW) || (y <= -height) || (y >= bmpH)) {
          return;
        }
      } else {
        /* clip the source rectangle to the source surface */
        if (x < 0) {
          width += x;
          dstX -= x;
          x = 0;
        }
        i = bmpW - x;
        if (width > i) {
          width = i;
        }
        if (y < 0) {
          height += y;
          dstY -= y;
          y = 0;
        }
        i = bmpH - y;
        if (height > i) {
          height = i;
        }

        /* clip the destination rectangle against the clip rectangle */
        if (dstX < clipX1) {
          i = clipX1 - dstX;
          dstX = clipX1;
          x += i;
          width -= i;
        }
        if ((dstX + width) > clipX2) {
          width = clipX2 - dstX;
        }
        if (dstY < clipY1) {
          i = clipY1 - dstY;
          dstY = clipY1;
          y += i;
          height -= i;
        }
        if ((dstY + height) > clipY2) {
          height = clipY2 - dstY;
        }

        /* check the validity */
        if ((width <= 0) || (height <= 0)) {
          return;
        }
      }

      int[] dst = getSurfacePixels(surface);
      boolean isSrcScreen = !(srcSurface instanceof Image);
      int scrPitch = pixels == mainWindowPixels ? Settings.screenWidth : bmpW; // if we're copying from a control, use the real width instead of the control's width
      int psrc = (bmpY + y) * scrPitch + bmpX + x;
      int pdst = dstY * pitch + dstX;
      int alphaMask = srcSurface instanceof Image ? ((Image) srcSurface).alphaMask : 255;
      for (j = height; --j >= 0; psrc += scrPitch, pdst += pitch) {
        int srcIdx = psrc; // guich@450_1
        int dstIdx = pdst;
        if (isSrcScreen) {
          for (i = width; --i >= 0;) {
            dst[dstIdx++] = pixels[srcIdx++] | 0xFF000000;
          }
        } else {
          for (i = width; --i >= 0; dstIdx++) {
            bmpPt = pixels[srcIdx++];
            int a = (bmpPt >>> 24) & 0xFF;
            a = alphaMask * a / 255;
            if (a == 0xFF) {
              dst[dstIdx] = bmpPt;
            } else if (a != 0) {
              screenPt = dst[dstIdx];
              int br = (bmpPt >> 16) & 0xFF;
              int bg = (bmpPt >> 8) & 0xFF;
              int bb = (bmpPt) & 0xFF;
              int sr = (screenPt >> 16) & 0xFF;
              int sg = (screenPt >> 8) & 0xFF;
              int sb = (screenPt) & 0xFF;

              int ma = 0xFF - a;
              int r = (a * br + ma * sr);
              r = (r + 1 + (r >> 8)) >> 8; // fast way to divide by 255
              int g = (a * bg + ma * sg);
              g = (g + 1 + (g >> 8)) >> 8;
              int b = (a * bb + ma * sb);
              b = (b + 1 + (b >> 8)) >> 8;
              dst[dstIdx] = (dst[dstIdx] & 0xFF000000) | (r << 16) | (g << 8) | b;
            }
          }
        }
      }
      if (isControlSurface) {
        needsUpdate = true;
      }
    } catch (Exception e) {
      if (!isScaled) {
        Vm.warning("Exception in drawBitmap\n" + "drawBitmap(" + x + ',' + y + ',' + width + ',' + height + " -> "
            + dstX + ',' + dstY + ',' + doClip + ")\n" + "clip: " + clipX1 + "," + clipY1 + "," + clipX2 + "," + clipY2
            + " - trans: " + transX + "," + transY + ", isScaled: " + isScaled);
        e.printStackTrace();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // took from gd 2.0.1 beta (http://www.boutell.com/gd/manual2.0.html) from Thomas Boutell
  private void fillPolygon(int[] xPoints1, int[] yPoints1, int base1, int nPoints1, int c1, int c2, boolean gradient) {
    int x1, y1, x2, y2, n = 0, temp, numSteps = 0, startRed = 0, startGreen = 0, startBlue = 0, endRed = 0,
        endGreen = 0, endBlue = 0, redInc = 0, greenInc = 0, blueInc = 0, red = 0, green = 0, blue = 0, c = 0, j;
    if (xPoints1 == null || yPoints1 == null || nPoints1 <= 0) {
      return;
    }
    if (nPoints1 < 2) {
      return;
    }
    totalcross.util.IntVector ints = new totalcross.util.IntVector(2);
    if (axPoints == null) {
      axPoints = new int[2][];
      ayPoints = new int[2][];
      anPoints = new int[2];
      aBase = new int[2];
    }
    axPoints[0] = xPoints1;
    ayPoints[0] = yPoints1;
    anPoints[0] = nPoints1;
    aBase[0] = base1;

    int miny = yPoints1[0];
    int maxy = yPoints1[0];
    for (int i = 1; i < nPoints1; i++) {
      if (yPoints1[base1 + i] < miny) {
        miny = yPoints1[base1 + i];
      }
      if (yPoints1[base1 + i] > maxy) {
        maxy = yPoints1[base1 + i];
      }
    }
    if (gradient) {
      numSteps = maxy - miny; // guich@tc110_11: support horizontal gradient
      if (numSteps == 0) {
        numSteps = 1; // guich@tc115_86: prevent divide by 0
      }
      startRed = (c1 >> 16) & 0xFF;
      startGreen = (c1 >> 8) & 0xFF;
      startBlue = c1 & 0xFF;
      endRed = (c2 >> 16) & 0xFF;
      endGreen = (c2 >> 8) & 0xFF;
      endBlue = c2 & 0xFF;
      redInc = ((endRed - startRed) << 16) / numSteps;
      greenInc = ((endGreen - startGreen) << 16) / numSteps;
      blueInc = ((endBlue - startBlue) << 16) / numSteps;
      red = startRed << 16;
      green = startGreen << 16;
      blue = startBlue << 16;
    } else {
      c = c1;
    }
    for (int y = miny; y <= maxy; y++) {
      for (int a = 0; a < 2; a++) {
        int[] xPoints = axPoints[a];
        int[] yPoints = ayPoints[a];
        int nPoints = anPoints[a];
        int base = aBase[a];
        j = nPoints - 1;
        for (int i = 0; i < nPoints; j = i, i++) {
          y1 = yPoints[base + j];
          y2 = yPoints[base + i];
          if (y1 == y2) {
            continue;
          }
          if (y1 > y2) { // invert
            temp = y1;
            y1 = y2;
            y2 = temp;
          }
          // compute next x point
          if ((y1 <= y && y < y2) || (y == maxy && y1 < y && y <= y2)) {
            if (yPoints[base + j] < yPoints[base + i]) {
              x1 = xPoints[base + j];
              x2 = xPoints[base + i];
            } else {
              x2 = xPoints[base + j];
              x1 = xPoints[base + i];
            }
            ints.addElement((y - y1) * (x2 - x1) / (y2 - y1) + x1);
            n++;
          }
        }
      }
      if (n >= 2) {
        if (gradient) {
          c = (red & 0xFF0000) | ((green >> 8) & 0x00FF00) | ((blue >> 16) & 0xFF);
          red += redInc;
          green += greenInc;
          blue += blueInc;
        }
        int[] items = ints.items;
        if (n == 2) { // most of the times
          if (items[1] > items[0]) {
            drawHLine(items[0], y, items[1] - items[0] + 1, c);
          } else {
            drawHLine(items[1], y, items[0] - items[1] + 1, c);
          }
        } else {
          // sort the ints
          ints.qsort();
          // draw the lines
          for (int i = 0; i < n; i += 2) {
            drawHLine(items[i], y, items[i + 1] - items[i] + 1, c);
          }
        }
      }
      if (n > 0) {
        ints.removeAllElements();
        n = 0;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // draws a series of pixels
  private void setPixels(int[] xPoints, int[] yPoints, int nPoints, int c) {
    int i;
    if (xPoints == null || yPoints == null || nPoints <= 0) {
      return;
    }
    for (i = 0; i < nPoints; i++) {
      setPixel(xPoints[i], yPoints[i], c);
    }
    if (isControlSurface) {
      needsUpdate = true;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // draws a polygon. if the polygon is not closed, close it
  private void drawPolygon(int[] xPoints1, int[] yPoints1, int base1, int nPoints1, int c) {
    int i;
    if (xPoints1 == null || yPoints1 == null || nPoints1 < 2) {
      return;
    }

    for (i = 1; i < nPoints1; i++) {
      drawLine(xPoints1[base1 + i - 1], yPoints1[base1 + i - 1], xPoints1[base1 + i], yPoints1[base1 + i], c);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // sets the bit value
  private void setPixel(int x, int y, int color) {
    x += transX;
    y += transY;
    if (clipX1 <= x && x < clipX2 && clipY1 <= y && y < clipY2) {
      if (x < 0 || y < 0) {
        return;
      }
      int[] pixels = getSurfacePixels(surface);
      pixels[y * pitch + x] = color;
      if (isControlSurface) {
        needsUpdate = true;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // draws a line from x to x+w-1
  private void drawHLine(int x, int y, int w, int color) {
    if (translateAndClip(x, y, w, 1)) {
      x = translateAndClipResults[0];
      y = translateAndClipResults[1];
      w = translateAndClipResults[2];
      if (x < clipX1) {
        w -= clipX1 - x;
        x = clipX1;
      } // line start before clip x1
      if ((x + w) > clipX2) {
        w = clipX2 - x; // line stops after clip x2
      }
      if (x < 0 || y < 0 || w < 0) {
        return;
      }
      int[] pix = getSurfacePixels(surface);
      x += y * pitch;
      Convert.fill(pix, x, x + w, color);
      if (isControlSurface) {
        needsUpdate = true;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // draws a line from y to y+h-1
  private void drawVLine(int x, int y, int h, int color) {
    if (translateAndClip(x, y, 1, h)) {
      x = translateAndClipResults[0];
      y = translateAndClipResults[1];
      h = translateAndClipResults[3];
      if (y < clipY1) {
        h -= clipY1 - y;
        y = clipY1;
      } // line start before clip y1
      if ((y + h) > clipY2) {
        h = clipY2 - y; // line stops after clip y2
      }
      if (x < 0 || y < 0 || h < 0) {
        return;
      }
      int[] pixels = getSurfacePixels(surface);
      for (x += y * pitch; h-- > 0; x += pitch) {
        pixels[x] = color;
      }
      if (isControlSurface) {
        needsUpdate = true;
      }
    }
  }

  // Bresenham algorithm to draw lines (modified by guich to improve performance in vertical and horizontal lines)
  @ReplacedByNativeOnDeploy
  private void drawLine(int Ax, int Ay, int Bx, int By, int c) {
    if (surelyOutsideClip(Ax, Ay, Bx, By)) {
      return;
    }

    // INITIALIZE THE COMPONENTS OF THE ALGORITHM THAT ARE NOT AFFECTED BY THE SLOPE OR DIRECTION OF THE LINE
    int dX = Math.abs(Bx - Ax); // store the change in X and Y of the line endpoints
    int dY = Math.abs(By - Ay);

    int CurrentX = Ax; // store the starting point (just point A)
    int CurrentY = Ay;

    // DETERMINE "DIRECTIONS" TO INCREMENT X AND Y (REGARDLESS OF DECISION)
    int Xincr, Yincr;

    // guich: if its a pixel, draw only the pixel
    if (dX == 0 && dY == 0) {
      setPixel(Ax, Ay, c);
    } else if (dY == 0) { // guich: if they are vertical or horizontal lines, use specific methods
      drawHLine(Math.min(Ax, Bx), Math.min(Ay, By), dX + 1, c);
    } else if (dX == 0) {
      drawVLine(Math.min(Ax, Bx), Math.min(Ay, By), dY + 1, c);
    } else { // diagonal
      if (Ax > Bx) {
        Xincr = -1;
      } else {
        Xincr = 1;
      } // which direction in X?
      if (Ay > By) {
        Yincr = -1;
      } else {
        Yincr = 1;
      } // which direction in Y?

      // DETERMINE INDEPENDENT VARIABLE (ONE THAT ALWAYS INCREMENTS BY 1 (OR -1) )
      // AND INITIATE APPROPRIATE LINE DRAWING ROUTINE (BASED ON FIRST OCTANT
      // ALWAYS). THE X AND Y'S MAY BE FLIPPED IF Y IS THE INDEPENDENT VARIABLE.
      if (dX >= dY) { // if X is the independent variable
        int dPr = dY << 1; // amount to increment decision if right is chosen (always)
        int dPru = dPr - (dX << 1); // amount to increment decision if up is chosen
        int P = dPr - dX; // decision variable start value

        // process each point in the line one at a time (just use dX)
        for (; dX >= 0; dX--) {
          setPixel(CurrentX, CurrentY, c); // plot the pixel
          CurrentX += Xincr; // increment independent variable
          if (P > 0) { // is the pixel going right AND up?
            CurrentY += Yincr; // increment dependent variable
            P += dPru; // increment decision (for up)
          } else {
            P += dPr; // increment decision (for right)
          }
        }
      } else { // if Y is the independent variable
        int dPr = dX << 1; // amount to increment decision if right is chosen (always)
        int dPru = dPr - (dY << 1); // amount to increment decision if up is chosen
        int P = dPr - dY; // decision variable start value

        // process each point in the line one at a time (just use dY)
        for (; dY >= 0; dY--) {
          setPixel(CurrentX, CurrentY, c); // plot the pixel
          CurrentY += Yincr; // increment independent variable
          if (P > 0) { // is the pixel going up AND right?
            CurrentX += Xincr; // increment dependent variable
            P += dPru; // increment decision (for up)
          } else {
            P += dPr; // increment decision (for right)
          }
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // Bresenham algorithm to draw lines. draws one pixel with color c1 and one pixel with color c2
  private void drawDottedLine(int Ax, int Ay, int Bx, int By, int c1, int c2) {
    boolean on = true;
    // INITIALIZE THE COMPONENTS OF THE ALGORITHM THAT ARE NOT AFFECTED BY THE SLOPE OR DIRECTION OF THE LINE
    int dX = Math.abs(Bx - Ax); // store the change in X and Y of the line endpoints
    int dY = Math.abs(By - Ay);

    int CurrentX = Ax; // store the starting point (just point A)
    int CurrentY = Ay;

    // DETERMINE "DIRECTIONS" TO INCREMENT X AND Y (REGARDLESS OF DECISION)
    int Xincr, Yincr;

    if (Ax > Bx) {
      Xincr = -1;
    } else {
      Xincr = 1;
    } // which direction in X?
    if (Ay > By) {
      Yincr = -1;
    } else {
      Yincr = 1;
    } // which direction in Y?

    // DETERMINE INDEPENDENT VARIABLE (ONE THAT ALWAYS INCREMENTS BY 1 (OR -1) )
    // AND INITIATE APPROPRIATE LINE DRAWING ROUTINE (BASED ON FIRST OCTANT
    // ALWAYS). THE X AND Y'S MAY BE FLIPPED IF Y IS THE INDEPENDENT VARIABLE.
    if (dX >= dY) { // if X is the independent variable
      int dPr = dY << 1; // amount to increment decision if right is chosen (always)
      int dPru = dPr - (dX << 1); // amount to increment decision if up is chosen
      int P = dPr - dX; // decision variable start value

      // process each point in the line one at a time (just use dX)
      for (; dX >= 0; dX--) {
        setPixel(CurrentX, CurrentY, on ? c1 : c2); // plot the pixel
        on = !on;
        CurrentX += Xincr; // increment independent variable
        if (P > 0) { // is the pixel going right AND up?
          CurrentY += Yincr; // increment dependent variable
          P += dPru; // increment decision (for up)
        } else {
          P += dPr; // increment decision (for right)
        }
      }
    } else { // if Y is the independent variable
      int dPr = dX << 1; // amount to increment decision if right is chosen (always)
      int dPru = dPr - (dY << 1); // amount to increment decision if up is chosen
      int P = dPr - dY; // decision variable start value

      // process each point in the line one at a time (just use dY)
      for (; dY >= 0; dY--) {
        setPixel(CurrentX, CurrentY, on ? c1 : c2); // plot the pixel
        on = !on;
        CurrentY += Yincr; // increment independent variable
        if (P > 0) { // is the pixel going up AND right?
          CurrentX += Xincr; // increment dependent variable
          P += dPru; // increment decision (for up)
        } else {
          P += dPr; // increment decision (for right)
        }
      }
    }
  }

  // transltates the given coords and returns the intersection between the clip rect and the coords passed. returns 0
  // if the coords are outside the clip rect
  private boolean translateAndClip(int x, int y, int w, int h) {
    x += transX;
    y += transY;

    if (x < clipX1) {
      if ((x + w) > clipX2) {
        w = clipX2 - clipX1;
      } else {
        w -= clipX1 - x;
      }
      x = clipX1;
    } else if ((x + w) > clipX2) {
      w = clipX2 - x;
    }
    if (y < clipY1) {
      if ((y + h) > clipY2) {
        h = clipY2 - clipY1;
      } else {
        h -= clipY1 - y;
      }
      y = clipY1;
    } else if ((y + h) > clipY2) {
      h = clipY2 - y;
    }

    if (x < 0 || y < 0 || h <= 0 || w <= 0) {
      return false; // guich@566_42: check the resulting w/h - guich@tc112_34: check also x and y
    }

    translateAndClipResults[0] = x;
    translateAndClipResults[1] = y;
    translateAndClipResults[2] = w;
    translateAndClipResults[3] = h;
    return true;
  }

  private int doClipX(int x) {
    return ((x + transX) < clipX1) ? (clipX1 - transX) : ((x + transX) >= clipX2) ? (clipX2 - transX) : x; // guich@401_2: added TRANSX/Y
  }

  private int doClipY(int y) {
    return ((y + transY) < clipY1) ? (clipY1 - transY) : ((y + transY) >= clipY2) ? (clipY2 - transY) : y;
  }

	  
  // draws an ellipse incrementally
  private void ellipseDrawAndFill(int xc, int yc, int rx, int ry, int c1, int c2, boolean fill, boolean gradient) {
    int numSteps = 0, startRed = 0, startGreen = 0, startBlue = 0, endRed = 0, endGreen = 0, endBlue = 0, redInc = 0,
        greenInc = 0, blueInc = 0, red = 0, green = 0, blue = 0, c = 0;
    // intermediate terms to speed up loop
    long t1 = (long) rx * rx, t2 = t1 << 1, t3 = t2 << 1;
    long t4 = (long) ry * ry, t5 = t4 << 1, t6 = t5 << 1;
    long t7 = (long) rx * t5, t8 = t7 << 1, t9 = 0;
    long d1 = t2 - t7 + (t4 >> 1); // error terms
    long d2 = (t1 >> 1) - t8 + t5;
    int x = rx, y = 0; // ellipse points
    if (rx < 0 || ry < 0) {
      return;
    }

    if (gradient) {
      numSteps = ry + ry; // guich@tc110_11: support horizontal gradient
      startRed = (c1 >> 16) & 0xFF;
      startGreen = (c1 >> 8) & 0xFF;
      startBlue = c1 & 0xFF;
      endRed = (c2 >> 16) & 0xFF;
      endGreen = (c2 >> 8) & 0xFF;
      endBlue = c2 & 0xFF;
      redInc = ((endRed - startRed) << 16) / numSteps;
      greenInc = ((endGreen - startGreen) << 16) / numSteps;
      blueInc = ((endBlue - startBlue) << 16) / numSteps;
      red = startRed << 16;
      green = startGreen << 16;
      blue = startBlue << 16;
    } else {
      c = c1;
    }

    while (d2 < 0) { // til slope = -1
      if (gradient) {
        c = (red & 0xFF0000) | ((green >> 8) & 0x00FF00) | ((blue >> 16) & 0xFF);
        red += redInc;
        green += greenInc;
        blue += blueInc;
      }
      if (fill) {
    	int w = x + x + 1;
	    drawHLine(xc - x, yc - y, w, c);
	    drawHLine(xc - x, yc + y, w, c);
      } else {
      	setPixel(xc + x, yc + y, c);
  	    setPixel(xc + x, yc - y, c);
  	    setPixel(xc - x, yc + y, c);
  	    setPixel(xc - x, yc - y, c);
      }
      y++; // always move up here
      t9 += t3;
      if (d1 < 0) { // move straight up
        d1 += t9 + t2;
        d2 += t9;
      } else { // move up and left
        x--;
        t8 -= t6;
        d1 += t9 + t2 - t8;
        d2 += t9 + t5 - t8;
      }
    }

    do { // rest of top right quadrant
      if (gradient) {
        c = (red & 0xFF0000) | ((green >> 8) & 0x00FF00) | ((blue >> 16) & 0xFF);
        red += redInc;
        green += greenInc;
        blue += blueInc;
      }
      // draw 4 points using symmetry
      if (fill) {
    	int w = x + x + 1;
  	    drawHLine(xc - x, yc - y, w, c);
  	    drawHLine(xc - x, yc + y, w, c);
      } else {
    	setPixel(xc + x, yc + y, c);
	    setPixel(xc + x, yc - y, c);
	    setPixel(xc - x, yc + y, c);
	    setPixel(xc - x, yc - y, c);
      }
      x--; // always move left here
      t8 -= t6;
      if (d2 < 0) { // move up and left
        y++;
        t9 += t3;
        d2 += t9 + t5 - t8;
      } else {
        d2 += t5 - t8;
      }
    } while (x >= 0);
  }

  // draw an elliptical arc from startAngle to endAngle. c is the fill color and c2 is the outline color (if in fill
  // mode - otherwise, c = outline color)
  private void arcPiePointDrawAndFill(int xc, int yc, int rx, int ry, double startAngle, double endAngle, int c, int c2,
      boolean fill, boolean pie, boolean gradient) {
    // make sure the values are -359 <= x <= 359
    while (startAngle <= -360) {
      startAngle += 360;
    }
    while (endAngle <= -360) {
      endAngle += 360;
    }
    while (startAngle > 360) {
      startAngle -= 360;
    }
    while (endAngle > 360) {
      endAngle -= 360;
    }

    if (startAngle == endAngle) {
      return;
    }
    if (startAngle > endAngle) {
      startAngle -= 360; // set to -45 to 45 so we can handle it correctly
    }
    if (startAngle >= 0 && endAngle <= 0) {
      endAngle += 360; // set to 135 to 225
    }

    int nPoints = 0;
    // this algorithm was created by Guilherme Campos Hazan
    double ppd;
    int startIndex, endIndex, index, i, oldX1 = 0, oldY1 = 0, oldX2 = 0, oldY2 = 0;
    int nq, size = 0;
    boolean checkClipX = (xc + rx + transX) > clipX2 || (xc - rx + transX) < clipX1; // guich@340_3 - guich@401_2:
    // added TRANSX/Y
    boolean checkClipY = (yc + ry + transY) > clipY2 || (yc - ry + transY) < clipY1;
    if (rx < 0 || ry < 0) {
      return;
    }
    // step 0: correct angle values
    if (startAngle < 0.1 && endAngle > 359.9) { // full circle? use the fastest routine instead
      if (fill) {
        ellipseDrawAndFill(xc, yc, rx, ry, c, c2, true, gradient); // guich@201_2: corrected colors. - guich@401_3: changed c->c2 and vice versa (line below)
      }
      ellipseDrawAndFill(xc, yc, rx, ry, c, c, false, gradient);
      return;
    }
    int[] xPoints = gxPoints;
    int[] yPoints = gyPoints;
    // step 0: if possible, use cached results
    int clipFactor = clipX1 * 1000000000 + clipX2 * 10000000 + clipY1 * 100000 + clipY2;
    boolean sameClip = clipFactor == lastClipFactor;
    boolean sameC = sameClip && xc == lastXC && yc == lastYC;
    boolean sameR = sameClip && rx == lastRX && ry == lastRY;
    if (!sameC || !sameR) {
      long t1 = (long) rx * rx, t2 = t1 << 1, t3 = t2 << 1;
      long t4 = (long) ry * ry, t5 = t4 << 1, t6 = t5 << 1;
      long t7 = (long) rx * t5, t8 = t7 << 1, t9 = 0;
      long d1 = t2 - t7 + (t4 >> 1); // error terms
      long d2 = (t1 >> 1) - t8 + t5;
      int x = rx, y = 0; // ellipse points

      if (sameR) {
        size = (lastSize - 2) / 4;
      } else {
        // step 1: computes how many points the circle has (computes only 45 degrees and mirrors the rest)
        // intermediate terms to speed up loop
        while (d2 < 0) { // til slope = -1
          t9 += t3;
          if (d1 < 0) { // move straight up
            d1 += t9 + t2;
            d2 += t9;
          } else { // move up and left
            x--;
            t8 -= t6;
            d1 += t9 + t2 - t8;
            d2 += t9 + t5 - t8;
          }
          size++;
        }

        do { // rest of top right quadrant
          x--; // always move left here
          t8 -= t6;
          if (d2 < 0) { // move up and left
            t9 += t3;
            d2 += t9 + t5 - t8;
          } else {
            d2 += t5 - t8;
          }
          size++;

        } while (x >= 0);
      }
      nq = size;
      size *= 4;
      // step 2: computes how many points per degree
      ppd = (double) size / 360.0;
      // step 3: create space in the buffer so it can save all the circle
      size += 2;
      if (nPoints < size) {
        gxPoints = gyPoints = null;
        xPoints = gxPoints = new int[size];
        yPoints = gyPoints = new int[size];
      }
      // step 4: stores all the circle in the array. the odd arcs are drawn in reverse order
      // intermediate terms to speed up loop
      if (!sameR) {
        t2 = t1 << 1;
        t3 = t2 << 1;
        t8 = t7 << 1;
        t9 = 0;
        d1 = t2 - t7 + (t4 >> 1); // error terms
        d2 = (t1 >> 1) - t8 + t5;
        x = rx;
      }
      i = 0;
      while (d2 < 0) { // til slope = -1
        // save 4 points using symmetry
        // guich@340_3: added clipping
        index = nq * 0 + i; // 0/3
        xPoints[index] = checkClipX ? doClipX(xc + x) : (xc + x);
        yPoints[index] = checkClipY ? doClipY(yc - y) : (yc - y);

        index = nq * 2 - i - 1; // 1/3
        xPoints[index] = checkClipX ? doClipX(xc - x) : (xc - x);
        yPoints[index] = checkClipY ? doClipY(yc - y) : (yc - y);

        index = nq * 2 + i; // 2/3
        xPoints[index] = checkClipX ? doClipX(xc - x) : (xc - x);
        yPoints[index] = checkClipY ? doClipY(yc + y) : (yc + y);

        index = nq * 4 - i - 1; // 3/3
        xPoints[index] = checkClipX ? doClipX(xc + x) : (xc + x);
        yPoints[index] = checkClipY ? doClipY(yc + y) : (yc + y);
        i++;

        y++; // always move up here
        t9 += t3;
        if (d1 < 0) { // move straight up
          d1 += t9 + t2;
          d2 += t9;
        } else { // move up and left
          x--;
          t8 -= t6;
          d1 += t9 + t2 - t8;
          d2 += t9 + t5 - t8;
        }
      }

      do { // rest of top right quadrant
        // save 4 points using symmetry
        // guich@340_3: added clipping
        index = nq * 0 + i; // 0/3
        xPoints[index] = checkClipX ? doClipX(xc + x) : (xc + x);
        yPoints[index] = checkClipY ? doClipY(yc - y) : (yc - y);

        index = nq * 2 - i - 1; // 1/3
        xPoints[index] = checkClipX ? doClipX(xc - x) : (xc - x);
        yPoints[index] = checkClipY ? doClipY(yc - y) : (yc - y);

        index = nq * 2 + i; // 2/3
        xPoints[index] = checkClipX ? doClipX(xc - x) : (xc - x);
        yPoints[index] = checkClipY ? doClipY(yc + y) : (yc + y);

        index = nq * 4 - i - 1; // 3/3
        xPoints[index] = checkClipX ? doClipX(xc + x) : (xc + x);
        yPoints[index] = checkClipY ? doClipY(yc + y) : (yc + y);
        i++;

        x--; // always move left here
        t8 -= t6;
        if (d2 < 0) { // move up and left
          y++;
          t9 += t3;
          d2 += t9 + t5 - t8;
        } else {
          d2 += t5 - t8;
        }
      } while (x >= 0);
      // save last arguments
      lastPPD = ppd;
      lastSize = size;
      lastXC = xc;
      lastYC = yc;
      lastRX = rx;
      lastRY = ry;
      lastClipFactor = clipFactor;
    } else {
      ppd = lastPPD;
      size = lastSize;
    }
    // step 5: computes the start and end indexes that will become part of the arc
    if (startAngle < 0) {
      startAngle += 360;
    }
    if (endAngle < 0) {
      endAngle += 360;
    }
    startIndex = (int) (ppd * startAngle);
    endIndex = (int) (ppd * endAngle);
    int last = size - 2;
    if (endIndex >= last) {
      endIndex--;
    }
    // step 6: fill or draw the polygons
    endIndex++;
    if (pie) {
      // connect two lines from the center to the two edges of the arc
      oldX1 = xPoints[endIndex];
      oldY1 = yPoints[endIndex];
      oldX2 = xPoints[endIndex + 1];
      oldY2 = yPoints[endIndex + 1];
      xPoints[endIndex] = xc;
      yPoints[endIndex] = yc;
      xPoints[endIndex + 1] = xPoints[startIndex];
      yPoints[endIndex + 1] = yPoints[startIndex];
      endIndex += 2;
    }

    // drawing from angle -30 to +30 ? (startIndex = 781, endIndex = 73, size=854)
    if (startIndex > endIndex) {
      int p1 = last - startIndex;
      if (fill) {
        fillPolygon(xPoints, yPoints, startIndex, p1, gradient ? c : c2, c2, gradient); // lower half, upper half
        fillPolygon(xPoints, yPoints, 0, endIndex, gradient ? c : c2, c2, gradient); // lower half, upper half
      }
      if (!gradient) {
        drawPolygon(xPoints, yPoints, startIndex, p1, c);
        drawPolygon(xPoints, yPoints, 0, endIndex, c);

      }
    } else {
      if (fill) {
        fillPolygon(xPoints, yPoints, startIndex, endIndex - startIndex, gradient ? c : c2, c2, gradient);
      }
      if (!gradient) {
        drawPolygon(xPoints, yPoints, startIndex, endIndex - startIndex, c);
      }
    }
    if (pie) { // restore saved points
      endIndex -= 2;
      xPoints[endIndex] = oldX1;
      yPoints[endIndex] = oldY1;
      xPoints[endIndex + 1] = oldX2;
      yPoints[endIndex + 1] = oldY2;
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  // guich@tc130: now stuff for Android ui style
  // inside points are negative values, outside points are positive values
  private static final int IN = 0;
  private static final int OUT = 0x100;

  static final int[][][] windowBorderAlpha = { { // thickness 1
      { 190, 190, 152, 89, OUT, OUT, OUT }, { 255, 255, 255, 255, 220, OUT, OUT }, { IN, IN, -32, -110, 255, 174, OUT },
      { IN, IN, IN, -11, 255, 245, 62 }, { IN, IN, IN, IN, -110, 255, 81 }, { IN, IN, IN, IN, -26, 255, 152 },
      { IN, IN, IN, IN, IN, 255, 190 }, },
      { // thickness 2
          { 255, 229, 163, 95, OUT, OUT, OUT }, { 255, 255, 255, 255, 215, OUT, OUT },
          { IN, -36, -102, -197, 255, 215, OUT }, { IN, IN, IN, -36, -191, 255, 122 },
          { IN, IN, IN, IN, -77, 255, 179 }, { IN, IN, IN, IN, -32, 255, 229 }, { IN, IN, IN, IN, IN, 255, 255 }, },
      { // thickness 3
          { 255, 199, 128, 10, OUT, OUT, OUT }, { 255, 255, 255, 223, 59, OUT, OUT },
          { 255, 255, 255, 255, 255, 81, OUT }, { IN, -79, -234, 255, 255, 245, 16 },
          { IN, IN, -32, -215, 255, 255, 133 }, { IN, IN, IN, -77, 255, 255, 207 },
          { IN, IN, IN, -15, 255, 255, 245 }, } };

  private static int interpolate(int color1r, int color1g, int color1b, int color2, int factor) {
    int m = 255 - factor;
    int color2r = (color2 >> 16) & 0xFF;
    int color2g = (color2 >> 8) & 0xFF;
    int color2b = (color2) & 0xFF;

    int r = (color1r * factor + color2r * m) / 255;
    int g = (color1g * factor + color2g * m) / 255;
    int b = (color1b * factor + color2b * m) / 255;
    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }

  private static int interpolate(int color1r, int color1g, int color1b, int color2r, int color2g, int color2b, int factor) {
    int m = 255 - factor;
    int r = (color1r * factor + color2r * m) / 255;
    int g = (color1g * factor + color2g * m) / 255;
    int b = (color1b * factor + color2b * m) / 255;
    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }

  public void drawWindowBorder(int xx, int yy, int ww, int hh, int titleH, int footerH, int borderColor, int titleColor,
      int bodyColor, int footerColor, int thickness, boolean drawSeparators) {
    int kx, ky, a, c;

    if (thickness < 1) {
      thickness = 1;
    } else if (thickness > 3) {
      thickness = 3;
    }
    int[][] aa = windowBorderAlpha[thickness - 1];
    int y2 = yy + hh - 1;
    int x2 = xx + ww - 1;
    int x1l = xx + 7;
    int y1l = yy + 7;
    int x2r = x2 - 6;
    int y2r = y2 - 6;

    int borderColorR = (borderColor >> 16) & 0xFF;
    int borderColorG = (borderColor >> 8) & 0xFF;
    int borderColorB = (borderColor) & 0xFF;

    int titleColorR = (titleColor >> 16) & 0xFF;
    int titleColorG = (titleColor >> 8) & 0xFF;
    int titleColorB = (titleColor) & 0xFF;

    int footerColorR = (footerColor >> 16) & 0xFF;
    int footerColorG = (footerColor >> 8) & 0xFF;
    int footerColorB = (footerColor) & 0xFF;

    // horizontal and vertical lines
    for (int i = 0; i < 3; i++) {
      a = aa[i][0];
      if (a == OUT || a == IN) {
        continue;
      }
      kx = x1l;
      ky = yy + i;
      c = getPixel(kx, ky);
      drawLine(kx, ky, x2r, yy + i, interpolate(borderColorR, borderColorG, borderColorB, c, a)); // top

      ky = y2 - i;
      c = getPixel(kx, ky);
      drawLine(kx, ky, x2r, y2 - i, interpolate(borderColorR, borderColorG, borderColorB, c, a)); // bottom

      kx = xx + i;
      ky = y1l;
      c = getPixel(kx, ky);
      drawLine(kx, ky, xx + i, y2r, interpolate(borderColorR, borderColorG, borderColorB, c, a)); // left

      kx = x2 - i;
      c = getPixel(kx, ky);
      drawLine(kx, ky, x2 - i, y2r, interpolate(borderColorR, borderColorG, borderColorB, c, a)); // right
    }
    // round corners
    for (int j = 0; j < 7; j++) {
      int top = yy + j, bot = y2r + j;
      for (int i = 0; i < 7; i++) {
        int left = xx + i, right = x2r + i;
        // top left
        a = aa[j][6 - i];
        if (a != OUT) {
          if (a <= 0) {
            setPixel(left, top,
                interpolate(borderColorR, borderColorG, borderColorB, titleColorR, titleColorG, titleColorB, -a));
          } else {
            setPixel(left, top, interpolate(borderColorR, borderColorG, borderColorB, getPixel(left, top), a));
          }
        }

        // top right
        a = aa[j][i];
        if (a != OUT) {
          if (a <= 0) {
            setPixel(right, top,
                interpolate(borderColorR, borderColorG, borderColorB, titleColorR, titleColorG, titleColorB, -a));
          } else {
            setPixel(right, top, interpolate(borderColorR, borderColorG, borderColorB, getPixel(right, top), a));
          }
        }
        // bottom left
        a = aa[i][j];
        if (a != OUT) {
          if (a <= 0) {
            setPixel(left, bot,
                interpolate(borderColorR, borderColorG, borderColorB, footerColorR, footerColorG, footerColorB, -a));
          } else {
            setPixel(left, bot, interpolate(borderColorR, borderColorG, borderColorB, getPixel(left, bot), a));
          }
        }
        // bottom right
        a = aa[6 - i][j];
        if (a != OUT) {
          if (a <= 0) {
            setPixel(right, bot,
                interpolate(borderColorR, borderColorG, borderColorB, footerColorR, footerColorG, footerColorB, -a));
          } else {
            setPixel(right, bot, interpolate(borderColorR, borderColorG, borderColorB, getPixel(right, bot), a));
          }
        }
      }
    }
    // now fill text, body and footer
    int t0 = thickness <= 2 ? 2 : 3;
    int ty = t0 + yy;
    int rectX1 = xx + t0;
    int rectX2 = x2 - t0;
    int rectW = ww - t0 * 2;
    int bodyH = hh - (titleH == 0 ? 7 : titleH) - (footerH == 0 ? 7 : footerH);
    // remove corners from title and footer heights
    titleH -= 7;
    if (titleH < 0) {
      titleH = 0;
    }
    footerH -= 7;
    if (footerH < 0) {
      footerH = 0;
    }

    // text
    backColor = titleColor | alpha;
    fillRect(x1l, ty, x2r - x1l, 7 - t0);
    ty += 7 - t0; // corners
    fillRect(rectX1, ty, rectW, titleH);
    ty += titleH; // non-corners
    // separator
    if (drawSeparators && titleH > 0 && titleColor == bodyColor) {
      drawLine(rectX1, ty - 1, rectX2, ty - 1,
          interpolate(borderColorR, borderColorG, borderColorB, titleColorR, titleColorG, titleColorB, 64));
    }
    // body
    backColor = bodyColor | alpha;
    fillRect(rectX1, ty, rectW, bodyH);
    ty += bodyH;
    // separator
    if (drawSeparators && footerH > 0 && bodyColor == footerColor) {
      drawLine(rectX1, ty, rectX2, ty,
          interpolate(borderColorR, borderColorG, borderColorB, titleColorR, titleColorG, titleColorB, 64));
      ty++;
      footerH--;
    }
    // footer
    backColor = footerColor | alpha;
    fillRect(rectX1, ty, rectW, footerH);
    ty += footerH; // non-corners
    fillRect(x1l, ty, x2r - x1l, 7 - t0); // corners
  }

  /**
   * Draws a cylindric shaded rectangle. Use it like:
   * 
   * <pre>
   * public void onPaint(Graphics g) {
   *    g.drawCylindricShade(0xB6D3E8, 0xF2F4F6, 0, 0, width, height);
   * }
   * </pre>
   * 
   * @since TotalCross 1.53
   */
  public void drawCylindricShade(int startColor, int endColor, int x, int y, int w, int h) {
    if (!translateAndClip(x, y, w, h)) {
      return;
    }
    int[] results = translateAndClipResults;
    int startX = results[0];
    int startY = results[1];
    int endX = startX + results[2];
    int endY = startY + results[3];
    int numSteps = Math.max(1, Math.min((endY - startY) / 2, (endX - startX) / 2)); // guich@tc110_11: support horizontal gradient - guich@gc114_41: prevent div by 0 if numsteps is 0
    int startRed = (startColor >> 16) & 0xFF;
    int startGreen = (startColor >> 8) & 0xFF;
    int startBlue = startColor & 0xFF;
    int endRed = (endColor >> 16) & 0xFF;
    int endGreen = (endColor >> 8) & 0xFF;
    int endBlue = endColor & 0xFF;
    int redInc = (((endRed - startRed) * 2) << 16) / numSteps;
    int greenInc = (((endGreen - startGreen) * 2) << 16) / numSteps;
    int blueInc = (((endBlue - startBlue) * 2) << 16) / numSteps;
    int red = startRed << 16;
    int green = startGreen << 16;
    int blue = startBlue << 16;
    for (int i = 0; i < numSteps; i++) {
      int rr = (red + i * redInc >> 16) & 0xFFFFFF;
      if (rr > endRed) {
        rr = endRed;
      }
      int gg = (green + i * greenInc >> 16) & 0xFFFFFF;
      if (gg > endGreen) {
        gg = endGreen;
      }
      int bb = (blue + i * blueInc >> 16) & 0xFFFFFF;
      if (bb > endBlue) {
        bb = endBlue;
      }
      foreColor = (rr << 16) | (gg << 8) | bb | alpha;
      int sx = startX + i, sy = startY + i;
      drawRect(sx, sy, endX - i - sx, endY - i - sy);
      int ii = i - 8;
      rr = (red + ii * redInc >> 16) & 0xFFFFFF;
      if (rr > endRed) {
        rr = endRed;
      }
      gg = (green + ii * greenInc >> 16) & 0xFFFFFF;
      if (gg > endGreen) {
        gg = endGreen;
      }
      bb = (blue + ii * blueInc >> 16) & 0xFFFFFF;
      if (bb > endBlue) {
        bb = endBlue;
      }
      foreColor = (rr << 16) | (gg << 8) | bb | alpha;
      int i2 = i / 8;
      drawLine(sx - i2, sy + i2, sx + i2, sy - i2);
      sx = endX - i;
      drawLine(sx - i2, sy - i2, sx + i2, sy + i2);
      sy = endY - i;
      drawLine(sx - i2, sy + i2, sx + i2, sy - i2);
      sx = startX + i;
      drawLine(sx - i2, sy - i2, sx + i2, sy + i2);
    }
    if (Settings.screenBPP < 24) {
      dither(startX, startY, endX - startX, endY - startY);
    }
  }

  /** Draws a thick line. You should use odd values for t(hickness). */
  public void drawThickLine(int x1, int y1, int x2, int y2, int t) {
    if (tlx == null) {
      tlx = new int[4];
      tly = new int[4];
    }
    int dx = x2 - x1;
    if (dx < 0) {
      dx = -dx;
    }
    int dy = y2 - y1;
    if (dy < 0) {
      dy = -dy;
    }
    t /= 2;
    if (dx > dy) {
      tlx[0] = tlx[1] = x1;
      tlx[2] = tlx[3] = x2;
      tly[0] = y1 + t;
      tly[1] = y1 - t;
      tly[2] = y2 - t;
      tly[3] = y2 + t;
    } else {
      tlx[0] = x1 + t;
      tlx[1] = x1 - t;
      tlx[2] = x2 - t;
      tlx[3] = x2 + t;
      tly[0] = tly[1] = y1;
      tly[2] = tly[3] = y2;
    }
    int c = backColor;
    backColor = foreColor;
    fillPolygon(tlx, tly, 4);
    backColor = c;
  }

  /** Draws a set of connected lines from the given x,y coordinates array. You must provide at least 2 points (4 int numbers). */
  public void drawLines(int... p) {
    for (int i = 0, n = p.length - 2; i < n; i += 2) {
      drawLine(p[i], p[i + 1], p[i + 2], p[i + 3]);
    }
  }

  
  /**
   * @deprecated TotalCross 2 no longer uses parameter ignoreColor.
   */
  @Deprecated
  public void dither(int x, int y, int w, int h, int ignoreColor) {
    dither(x, y, w, h);
  }
  
  /**
   * Apply a 16-bit Floyd-Steinberg dithering on the give region of the surface. Don't use dithering if
   * Settings.screenBPP is not equal to 16, like on desktop computers. In OpenGL platforms, does not work if the
   * surface is the screen (in other words, works only for images).
   * 
   * @since TotalCross 1.53
   */
  @ReplacedByNativeOnDeploy
  public void dither(int x, int y, int w, int h) {
    if (!translateAndClip(x, y, w, h)) {
      return;
    }
    int[] results = translateAndClipResults;
    x = results[0];
    y = results[1];
    w = results[2];
    h = results[3];
    // based on http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
    int[] pixels = (int[]) getSurfacePixels(surface);
    int p, oldR, oldG, oldB, newR, newG, newB, errR, errG, errB;
    for (int yy = y; yy < h; yy++) {
      for (int xx = x; xx < w; xx++) {
        p = pixels[yy * w + xx];
        // get current pixel values
        oldR = (p >> 16) & 0xFF;
        oldG = (p >> 8) & 0xFF;
        oldB = p & 0xFF;
        // convert to 565 component values
        newR = oldR >> 3 << 3;
        newG = oldG >> 2 << 2;
        newB = oldB >> 3 << 3;
        // compute error
        errR = oldR - newR;
        errG = oldG - newG;
        errB = oldB - newB;
        // set new pixel
        pixels[yy * w + xx] = (p & 0xFF000000) | (newR << 16) | (newG << 8) | newB;

        addError(pixels, xx + 1, yy, w, h, errR, errG, errB, 7, 16);
        addError(pixels, xx - 1, yy + 1, w, h, errR, errG, errB, 3, 16);
        addError(pixels, xx, yy + 1, w, h, errR, errG, errB, 5, 16);
        addError(pixels, xx + 1, yy + 1, w, h, errR, errG, errB, 1, 16);
      }
    }
    if (isControlSurface) {
      needsUpdate = true;
    }
  }
  // used for the dither method
  private void addError(int[] pixel, int x, int y, int w, int h, int errR, int errG, int errB, int j, int k) {
    if (x >= w || y >= h || x < 0) {
      return;
    }
    int i = y * w + x;
    int p = pixel[i];
    int r = (p >> 16) & 0xFF;
    int g = (p >> 8) & 0xFF;
    int b = p & 0xFF;
    r += j * errR / k;
    g += j * errG / k;
    b += j * errB / k;
    if (r > 255) {
      r = 255;
    } else if (r < 0) {
      r = 0;
    }
    if (g > 255) {
      g = 255;
    } else if (g < 0) {
      g = 0;
    }
    if (b > 255) {
      b = 255;
    } else if (b < 0) {
      b = 0;
    }
    pixel[i] = (p & 0xFF000000) | (r << 16) | (g << 8) | b;
  }
}
