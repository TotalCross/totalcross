// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000-2012 SuperWaba Ltda.
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
import totalcross.ui.effect.MaterialEffect;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.StringUtils;
import totalcross.util.UnitsConverter;

/**
 * Check is a control with a box and a check inside of it when the state is checked.
 * <p>
 * Here is an example showing a check being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    Check check;
 *
 *    public void initUI()
 *    {
 *       add(check = new Check("Check me"), LEFT, AFTER);
 *    }
 *
 *    public void onEvent(Event event)
 *    {
 *       if (event.type == ControlEvent.PRESSED && event.target == check)
 *       {
 *          bool checked = check.isChecked();
 *          ... handle check being pressed
 * </pre>
 */

public class Check extends Control implements TextControl, MaterialEffect.SideEffect {
  private String text;
  private boolean checked, checked0;
  private int cbColor, cfColor;
  private int fourColors[] = new int[4];
  private String[] lines = Label.emptyStringArray;
  private int[] linesW;
  private int lastASW;
  private String originalText;
  private int alphaSel = 255;
  private boolean animating;
  private boolean sendPressAfterEffect;
  private Insets insets;
  /** Set to true to left-justify the text in the control. The default is right-justified,
   * if the control's width is greater than the preferred one.
   * @since TotalCross 1.0
   * @deprecated Now the align is always at left
   */
  @Deprecated
  public boolean leftJustify;

  /** Sets the text color of the check. Defaults to the foreground color. 
   * @since TotalCross 2.0.
   */
  public int textColor = -1;

  /** Set to the color of the check, if you want to make it different of the foreground color.
   * @since TotalCross 1.3
   */
  public int checkColor = -1;
  
  /**The disable color used to fill the background color when disabled.
   * @since TotalCross 5.0*/
  public int disabledColor = Color.getRGB("5c5c5c");

  /** Set to true to let the Check split its text based on the width every time its width
   * changes. If the height is PREFERRED, the Label will change its size accordingly.
   * You may change the height again calling setRect.
   * @since TotalCross 1.14
   */
  public boolean autoSplit; // guich@tc114_74
  
  /**Change to modify the gap value between the checkbox and the text.
   * Tip: Use "UnitsConverter.toPixels(textLeftGap + DP) to set the size in DP.
   * @since TotalCross 5.0*/
  public int textLeftGap = UnitsConverter.toPixels(6 + DP);
  
  /**Sets the size of the check. Changing its value will only have effect if the variable
   * "autoResizeCheckSize" is false.
   * @since TotalCross 5.0*/
  public int checkSize = UnitsConverter.toPixels(16 + DP);
  
  /**Modify the check insets.*/
  public void setInsets(int left, int right, int top, int bottom) {
	  insets.set(top, left, bottom, right);
  }
  
  /** Creates a check control. The size is controlled by the height of the element. 
   * You can change it by using the "setRect" or "add" methods.*/
  public Check() {
	  this("");
  }
  
  /** Creates a check control displaying the given text. The size is controlled by the height of the element. 
   * You can change it by using the "setRect" or "add" methods.*/
  public Check(String text) {
	this.setFont(Font.getFont(false, 14));
    setText(text);
    effect = UIEffects.get(this);
    backColor = Color.WHITE;
    int insetsValues = UnitsConverter.toPixels(4 + DP);
    insets = new Insets(insetsValues, insetsValues, insetsValues, insetsValues);
  }

  /** Called by the system to pass events to the check control. */
  @Override
  public void onEvent(Event event) {
    if (event.target != this || !isEnabled()) {
      return;
    }
    switch (event.type) {
    case KeyEvent.ACTION_KEY_PRESS:
      checked = !checked;
      repaintNow();
      postPressedEvent();
      break;
    default:
      if (uiMaterial && event.type == PenEvent.PEN_DOWN) {
        checked0 = checked;
      }
      if (!isActionEvent(event)) {
        break;
      }
      PenEvent pe = (PenEvent) event;
      if (isInsideOrNear(pe.x, pe.y)) {
        Window.needsPaint = true;
        checked0 = checked;
        checked = !checked;
        if (effect == null || !getDoEffect()) {
          postPressedEvent();
        } else {
          sendPressAfterEffect = true;
        }
      }
      break;
    }
  }

  /** Sets the text that is displayed in the check. */
  @Override
  public void setText(String text) {
    originalText = text;
    this.text = text;
    lines = text.equals("") ? new String[] { "" } : Convert.tokenizeString(text, '\n'); // guich@tc100: now we use \n
    onFontChanged();
    Window.needsPaint = true;
  }

  /** Gets the text displayed in the check. */
  @Override
  public String getText() {
    return text;
  }

  /** Returns the checked state of the control. */
  public boolean isChecked() {
    return checked;
  }

  /** Sets the checked state of the control. */
  public void setChecked(boolean checked) {
    setChecked(checked, Settings.sendPressEventOnChange);
  }

  /** Sets the checked state of the control, and send the press event if desired. */
  public void setChecked(boolean checked, boolean sendPress) {
    if (this.checked != checked) {
      checked0 = this.checked;
      this.checked = checked;
      Window.needsPaint = true;
      if (getDoEffect() && effect != null) {
        sendPressAfterEffect = sendPress;
        effect.startEffect();
      } else if (sendPress) {
        postPressedEvent();
      }
    }
  }

  /** Returns the maximum text width for the lines of this Label. */
  public int getMaxTextWidth() {
    int w = 0;
    for (int i = lines.length - 1; i >= 0; i--) {
      if (linesW[i] > w) {
        w = linesW[i];
      }
    }
    return w;
  }
 
  
  /**Returns the preferred width of this control.*/
  @Override
  public int getPreferredWidth() {
	  int maxTextWidth = getMaxTextWidth();
	  return Math.max(1, checkSize + (maxTextWidth == 0 ? 0 : maxTextWidth + textLeftGap) + insets.left*3/2 + insets.right*3/2);
  }

  /**Returns the preferred height of this control.*/
  @Override
  public int getPreferredHeight() {
    return (uiMaterial ? checkSize : (fmH*lines.length > checkSize ? fmH*lines.length : checkSize)) + insets.top + insets.bottom;
  }
  
  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    cbColor = UIColors.sameColors ? backColor : Color.brighter(getBackColor()); // guich@572_15
    cfColor = getForeColor();
    if (!uiAndroid) {
      Graphics.compute3dColors(isEnabled(), backColor, foreColor, fourColors);
    }
  }

  /** Called by the system to draw the check control. */
  @Override
  public void onPaint(Graphics g) {
    boolean enabled = isEnabled();
    
    int yy = height/2 - checkSize/2;
    // guich@200b4_126: repaint the background of the whole control
    g.backColor = backColor;
    if (!transparentBackground && enabled) {
      if(uiMaterial) {
    	  g.fillRect(insets.left, insets.top + 2, checkSize, checkSize);
      } else if (!uiAndroid){
    	  g.fillRect(0, 0, checkSize, checkSize);
      }
    }
    // square paint
    if (!uiAndroid && uiVista && enabled) {
      g.fillVistaRect(0, 0, checkSize, checkSize, cbColor, true, false);
    } else if (!uiAndroid || !transparentBackground && enabled) {
      g.backColor = uiAndroid ? backColor : cbColor;
      if(uiMaterial) {
    	  g.fillRect(insets.left, yy + insets.top - insets.bottom, checkSize, checkSize); // guich@220_28
      } else if (!uiAndroid) {
    	  g.fillRect(0, 0, checkSize, checkSize); // guich@220_28
      }
    }

    if (getDoEffect() && effect != null) {
    	effect.alphaValue = 70;
    	effect.color = Color.darker(foreColor, 3);
    	effect.paintEffect(g);
    }

    if (uiAndroid) {
    	//Color.getRGB("3e3e3e") fore
      try {
        Image img = uiMaterial
            ? Resources.checkBkg.getPressedInstance(checkSize, checkSize, backColor, enabled ? (checkColor != -1 ? checkColor : foreColor) : disabledColor,
                enabled)
            : enabled ? Resources.checkBkg.getNormalInstance(checkSize, checkSize, checkColor != -1 ? checkColor : foreColor)
                : Resources.checkBkg.getDisabledInstance(checkSize, checkSize, disabledColor);
        img.alphaMask = alphaValue;
        if (!uiMaterial || !checked || animating) {
          NinePatch.tryDrawImage(g, img, insets.left, yy + insets.top - insets.bottom);
        }
        img.alphaMask = 255;
        if (checked || animating) {
          img = Resources.checkSel.getPressedInstance(checkSize, checkSize, backColor, enabled ? (checkColor != -1 ? checkColor : foreColor) : disabledColor,
              enabled);
          img.alphaMask = alphaSel;
          NinePatch.tryDrawImage(g, img, insets.left, yy + insets.top - insets.bottom);
          img.alphaMask = 255;
        }
      } catch (ImageException ie) {
      }
    } else {
      g.draw3dRect(0, 0, checkSize, checkSize, Graphics.R3D_CHECK, false, false, fourColors); // guich@220_28//
    }
    g.foreColor = checkColor != -1 ? checkColor : uiAndroid ? foreColor : cfColor;

    if (!uiAndroid && checked) {
      paintCheck(g, fmH, checkSize);
    }

    // draw label
    //TODO
    yy = height/2 - (fmH*lines.length)/2;
    int xx = insets.left + checkSize + textLeftGap; // guich@300_69
    g.foreColor = textColor != -1 ? textColor : foreColor;
    for (int i = 0; i < lines.length; i++, yy += fmH) {
      String text = StringUtils.shortText(lines[i], font.fm, this.width - xx - insets.right);
      g.drawText(text, xx, yy, textShadowColor != -1, textShadowColor);
    }
  }

  /** Paints a check in the given coordinates. The g must have been translated to destination x,y coordinates.
   * @since SuperWaba 5.5
   * @param g The desired Graphics object where to paint. The forecolor must already be set.
   * @param fmH The fmH member
   * @param height The height of the control. The check will be vertical aligned based on this height.
   */
  public static void paintCheck(Graphics g, int fmH, int height) // guich@550_29
  {
    if (uiAndroid) {
      try {
        g.drawImage(Resources.checkSel.getPressedInstance(height, height, 0, g.foreColor, true), 0, 0);
      } catch (ImageException ie) // just paint something 
      {
        g.backColor = g.foreColor;
        g.fillRect(0, 0, height, height);
      }
    } else {
      int wh = height;
      int m = 2 * wh / 5;
      int yy = m;
      int xx = 3;
      wh -= xx;
      if (fmH <= 10) // guich@tc110_18
      {
        g.backColor = g.foreColor;
        g.fillRect(2, 2, wh + xx - 4, wh + xx - 4);
      } else {
        for (int i = xx; i < wh; i++) {
          g.drawLine(xx, yy, xx, yy + 2);
          xx++;
          if (i < m) {
            yy++;
          } else {
            yy--;
          }
        }
      }
    }
  }

  /** Clears this control, checking it if clearValueInt is 1. */
  @Override
  public void clear() // guich@572_19
  {
    setChecked(clearValueInt == 1);
  }

  /** Splits the text to the given width. Remember to set the font (or add the Label to its parent) 
   * before calling this method.
   * @since TotalCross 1.14
   * @see #autoSplit
   */
  public void split(int maxWidth) // guich@tc114_73
  {
    String text = originalText; // originalText will be changed by setText
    setText(Convert.insertLineBreak(maxWidth, fm, text)); // guich@tc126_18: text cannot be assigned here or originalText will be overwritten
    originalText = text;
  }

  @Override
  protected void onFontChanged() {
    int i;
    if (linesW == null || linesW.length != lines.length) {
      linesW = new int[lines.length];
    }
    int[] linesW = this.linesW; // guich@450_36: use local var
    for (i = lines.length - 1; i >= 0; i--) {
      linesW[i] = fm.stringWidth(lines[i]);
    }
  }

  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    if (autoSplit && this.width > 0 && this.width != lastASW) // guich@tc114_74 - guich@tc120_5: only if PREFERRED was choosen in first setRect - guich@tc126_35
    {
      lastASW = this.width;
      int wh = lines.length == 1 ? height : fmH + Edit.prefH;
      split((this.width < wh - 2 ? getPreferredWidth() : this.width) - wh - 2);
      if (PREFERRED - RANGE <= setH && setH <= PREFERRED + RANGE) {;
        setRect(KEEP, KEEP, KEEP, getPreferredHeight() + setH - PREFERRED);
      }
    }
//    int preferredWidth = insets.left + this.height - insets.top - insets.bottom + (getMaxTextWidth() == 0 ? 0 : textLeftGap + getMaxTextWidth()) + insets.right;
//    if(this.width < preferredWidth)
//    	this.width = preferredWidth;
  }

  @Override
  public void sideStart() {
    animating = true;
  }

  @Override
  public void sideStop() {
    if (animating) {
      animating = false;
      Window.needsPaint = true;
      alphaSel = alphaValue;
      if (sendPressAfterEffect) {
        postPressedEvent();
      }
      sendPressAfterEffect = false;
    }
  }

  @Override
  public void sidePaint(Graphics g, int alpha) {
    if (!checked0) {
      alpha = 255 - alpha;
    }
    alphaSel = alpha * alphaValue / 255; // limits on current alpha set by user
  }
}
