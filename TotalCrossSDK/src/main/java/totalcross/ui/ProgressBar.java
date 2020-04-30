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

import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.UnitsConverter;

/** A basic progress bar, with the bar and a text.
 * The text is comprised of a prefix and a suffix.
 * <p>
 * You can create a horizontal endless ProgressBar, always going from left to right, 
 * by setting the given parameters:
 * <ul>
 * <li> call setEndless()
 * <li> max-min: used to compute the width of the bar
 * <li> prefix and suffix: displayed, but the current value is not displayed
 * <li> setValue(n): n used to increment the current value, not to set the value to n.
 * </ul>
 * Then set a timer to update the value. See the UIGadgets sample.
 */

public class ProgressBar extends Control {
  /** The minimum value of a progress bar. */
  public int min;

  /** The maximum value of a progress bar. */
  public int max;

  /** The current value of a progress bar. */
  private int value;

  /** The string prefix. The displayed label will be prefix+value+sufix. The default is an empty string. */
  public String prefix = "";

  /** The string prefix. The displayed label will be prefix+value+sufix. The default is the percentage symbol. */
  public String suffix = "%";

  /** Set to false to don't let the text be drawn.
   * @since TotalCross 1.0
   */
  public boolean drawText = true;

  /** Set to false to don't let the value be drawn. Note that the prefix and suffix will still be drawn.
   * @since TotalCross 1.0
   */
  public boolean drawValue = true;

  /** The text color */
  public int textColor = Color.BLACK; // guich@340_5

  /** If false, no border is drawn. */
  public boolean drawBorder = true;

  /** If true, the text is highlighted. */
  public boolean highlight;

  /** The highlight color, or -1 to use one based on textColor. */
  public int highlightColor = -1;

  /** If false, use horizontal mode (default)
   * @since TotalCross 1.15 
   */
  public boolean vertical; // guich@tc115_67

  /** Used for String concatenations */
  private StringBuffer sb = new StringBuffer(10);
  
  /**The filled bar size (used when it's a endless bar)*/
  public int filledBarSize = -1;

  private boolean endless;

  private Image npback, npfore;
  
//  private NinePatch.Parts types[];

  /**
   * Creates a progress bar, with minimum and maximum set as 0 and 100, respectively.
   */
  public ProgressBar() {
    this(0, 100);
  }

  /**
   * Creates a progress bar, String is set to <code>null</code>, which is the default.
   * Uses the specified minimum for the initial value of the progress bar.
   * @param min The minimum value
   * @param max The maximum value
   */
  public ProgressBar(int min, int max) {
    this.min = value = min;
    this.max = max;
    backColor = Color.getRGB(215, 183, 253);
    foreColor = Color.getRGB(98, 0, 238);
    drawText = false;
    focusTraversable = false;
  }

  /** Call this method to make this ProgressBar a horizontal endless progressbar; 
   * just keep calling <code>setValue(step)</code>
   * to increase the value of the progressbar.
   */
  public void setEndless() // guich@tc125_19
  {
    if (vertical) {
      throw new RuntimeException("Endless progressbar cannot be vertical, only horizontal)");
    }
    suffix = "";
    endless = true;
  }

  /** Gets the preferred width. */
  @Override
  public int getPreferredWidth() {
    if (vertical) {
      return UnitsConverter.toPixels(4 + DP) + (!endless && drawText ? fm.stringWidth(max + prefix + suffix) : 0);
    }
    return UnitsConverter.toPixels(240 + DP);
  }

  /** Returns the preferred height. */
  @Override
  public int getPreferredHeight() {
    if (vertical) {
      return UnitsConverter.toPixels(240 + DP);
    }
    return (!endless && drawText ? fmH : 0) + UnitsConverter.toPixels(4 + DP);
  }

  /**
   * Sets the current value. Due to performance reasons,
   * min and max are not verified.
   * If the value was not changed, nothing happens.
   * The progress bar is repainted immediately.
   * <p>
   * If this is an endless ProgressBar, the given number is used as 
   * an increment to the current value. Note that n must be greater than 0.
   * 
   * If you call this method and the bar isnt updated, you can try to call <code>MainWindow.pumpEvents()</code>.
   *
   * @param   n The new value
   * @see     #getValue
   */
  public void setValue(int n) {
    if (endless || value != n) {
      value = endless ? value + n : n;
      repaintNow();
    }
  }

  /**
   * Sets the current value and the prefix and suffix. Note that, due to performance reasons,
   * min and max are not verified. This does not check if the value had changed; it always
   * repaint the progress bar immediately.
   *
   * @param  value The new value
   * @see     #getValue
   */
  public void setValue(int value, String prefix, String suffix) {
    this.value = endless ? this.value + value : value;
    this.prefix = prefix;
    this.suffix = suffix;
    repaintNow();
  }

  /** Returns the current value */
  public int getValue() {
    return value;
  }

  @Override
  public void onColorsChanged(boolean b) {
    npback = npfore = null;
  }

  @Override
  public void onBoundsChanged(boolean b) {
    npback = npfore = null;
    if(filledBarSize == -1)
    	filledBarSize = (int)(width * 0.466);
  }
  
  @Override
  public void setNinePatch(Image img, int side, int corner) {
	  super.setNinePatch(img, side, corner);
	  npback = npfore = null;
  }

  /** Paint the Progress Bar. The filled part of the bar is painted with the foreground color;
   * the empty part of the bar is painted with the background color; the text is painted with
   * the defined textColor color; no border is drawn.
   */
  @Override
  public void onPaint(Graphics g) {
    // computes the current width of the bar
    int dif = max - min;
    int size = vertical ? height : width;
    int s = (dif == 0 || value == max) ? size
        : (value == 0) ? 0 : (int) ((long) size * (long) (value - min) / (long) dif);
    if (s > size) {
      s = size;
    }
    
    if(filledBarSize > width)
    	filledBarSize = width - 1;
    
    // draw the filled part
    int bc = getBackColor();
    int fc = getForeColor();
    
    if(npParts != null && !transparentBackground) {
	    try {
	    	if (npback == null) {
	    		npback = NinePatch.getInstance().getNormalInstance(npParts, width, height, bc, false);
	    		npfore = NinePatch.getInstance().getNormalInstance(npParts, width, height, fc, false);
	    	}
	    } catch (ImageException e) {}
		if (endless) // only horizontal
		{
		  int d = value - filledBarSize;
		  g.copyRect(npback, 0, 0, d, height, 0, 0);
		  g.copyRect(npback, value, 0, width - value, height, value, 0);
		  g.copyRect(npfore, d, 0, filledBarSize, height, d, 0);
		  if(value > width)
			  g.copyRect(npfore, 0, 0, value - width, height, 0, 0);
		} else {
		  if (vertical) {
		    int r = height - s;
		    g.copyRect(npback, 0, 0, width, r, 0, 0);
		    g.copyRect(npfore, 0, r, width, s, 0, r);
		  } else {
		    int r = width - s;
		    g.copyRect(npfore, 0, 0, s, height, 0, 0);
		    g.copyRect(npback, s, 0, r, height, s, 0);
		  }
		}
    } else {
      if (s > 0) {
        if (endless || uiMaterial) {
          g.backColor = bc;
          g.fillRect(0, 0, width, height);
        }
        if ((uiMaterial || uiVista) && isEnabled()) // guich@573_6
        {
          if (uiMaterial) {
            g.backColor = fc;
            if (vertical) {
              g.fillRect(0, height - s, width, s);
            } else if (!endless) {
              g.fillRect(0, 0, s, height);
            } else {
              g.fillRect(value - filledBarSize, 0, filledBarSize, height);
              if(endless && value > width)
            	  g.fillRect(0, 0, value - width, height);
            }
          } else {
            if (vertical) {
              g.fillVistaRect(0, height - s, width, s, fc, false, false);
            } else if (!endless) {
              g.fillVistaRect(0, 0, s, height, fc, false, false);
            } else {
              g.fillVistaRect(value - filledBarSize, 0, filledBarSize, height, fc, false, false);
            }
          }
        } else {
          g.backColor = fc;
          if (vertical) {
            g.fillRect(0, height - s, width, s);
          } else if (!endless) {
            g.fillRect(0, 0, s, height);
          } else {
            g.fillRect(value - dif, 0, dif, height);
          }
        }
      }
      // draw the empty part
      g.backColor = bc;
      int ss = size - s;
      if (ss > 0 && !transparentBackground) {
        if (vertical) {
          g.fillRect(0, 0, width, ss);
        } else {
          g.fillRect(s, 0, ss, height);
        }
      }
    }
    if (endless && value - filledBarSize >= width) {
    	value = filledBarSize;
    }
    
    // draw the text
    if (drawText) {
      StringBuffer sb = this.sb; // get a local reference
      sb.setLength(0);
      sb.append(prefix);
      if (!endless && drawValue) {
        sb.append(value);
      }
      String st = sb.append(suffix).toString();
      int x = (width - fm.stringWidth(st)) >> 1;
      int y = ((height - fmH) >> 1) - 1;
      int shadow = uiMaterial ? -1
          : textShadowColor != -1 ? textShadowColor
              : !highlight ? -1 : highlightColor == -1 ? Color.getCursorColor(textColor) : highlightColor;
      g.foreColor = textColor;
      g.drawText(st, x, y, shadow != -1, shadow);
    }
    g.foreColor = textColor;
    if (drawBorder && !uiAndroid) {
      g.drawRect(0, 0, width, height);
    }
  }

  /** Clears this control, setting the value to clearValueInt. */
  @Override
  public void clear() // guich@572_19
  {
    setValue(clearValueInt);
  }
}
