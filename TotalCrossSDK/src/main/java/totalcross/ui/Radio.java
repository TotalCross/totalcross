/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Settings;
import totalcross.ui.effect.MaterialEffect;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Hashtable;

/**
 * Radio is a radio control.
 * Radios can be grouped together using a RadioGroupController.
 * <p>
 * Here is an example showing a radio being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    RadioGroupController rgGender;
 *
 *    public void initUI()
 *    {
 *       rgGender = new RadioGroupController();
 *       add(new Radio("Male", rgGender), LEFT, AFTER);
 *       add(new Radio("Female", rgGender), AFTER+2, SAME);
 *       rgGender.setSelectedIndex(radioMale); // activate the specified one.
 *    }
 *
 *    public void onEvent(Event event)
 *    {
 *       if (event.type == ControlEvent.PRESSED && (event.target instanceof Radio) && ((Radio)event.target).getRadioGroup() == rgGender)
 *       {
 *          boolean male = rgGender.getSelectedIndex() == 0;
 *          ... handle radio Male being pressed
 * </pre>
 * @see totalcross.ui.RadioGroupController
 */

public class Radio extends Control implements TextControl, MaterialEffect.SideEffect {
  private String text;
  private boolean checked, checked0;
  RadioGroupController radioGroup;
  private int colors[] = new int[4];
  private int cColor, bColor;
  private int textW;
  private int alphaSel = 255;
  private boolean animating;

  /** Sets the text color of the check. Defaults to the foreground color. 
   * @since TotalCross 2.0.
   */
  public int textColor = -1;

  /** Set to the color of the check, if you want to make it different of the foreground color.
   * @since TotalCross 1.3
   */
  public int checkColor = -1;

  /** Set to true to left justify this control if the width is above the preferred one. */
  public boolean leftJustify;
  private Image imgSel, imgUnsel;
  private static Hashtable imgs; // cache the images
  private static Image vistaSelected, vistaUnselected;
  private boolean sendPressAfterEffect;

  /** Creates a radio control displaying the given text. */
  public Radio(String text) {
    this.text = text;
    textW = fm.stringWidth(text);
    effect = UIEffects.get(this);
  }

  /** Creates a radio control with the given text attached to the given RadioGroupController */
  public Radio(String text, RadioGroupController radioGroup) {
    this(text);
    this.radioGroup = radioGroup;
    radioGroup.add(this);
  }

  /** Returns the RadioGroupController that this radio belongs to, or null if none.
   */
  public RadioGroupController getRadioGroup() {
    return radioGroup;
  }

  /** "Merge" the colors between the original grayscale image and the current foreground. */
  private Image getImage(boolean isSelected) throws Exception {
    if (vistaSelected == null) {
      vistaSelected = new Image("totalcross/res/radioon_vista.png");
      vistaUnselected = new Image("totalcross/res/radiooff_vista.png");
    }
    String key = (isSelected ? "*" : "") + foreColor + "|" + backColor + "|" + fmH + (isEnabled() ? "*" : ""); // guich@tc110a_110: added backColor.
    Image img;
    if (imgs == null) {
      imgs = new Hashtable(4);
    } else if ((img = (Image) imgs.get(key)) != null) {
      return img;
    }
    img = isSelected ? vistaSelected.getFrameInstance(0) : vistaUnselected.getFrameInstance(0);
    int h = height == 0 ? getPreferredHeight() : height;
    img = img.getSmoothScaledInstance(h, h);
    img.applyColor(foreColor);
    if (!isEnabled()) {
      img = img.getFadedInstance();
    }
    imgs.put(key, img);
    return img;
  }

  /** Sets the text. */
  @Override
  public void setText(String text) {
    this.text = text;
    Window.needsPaint = true;
    onFontChanged();
  }

  /** Gets the text displayed in the radio. */
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
      if (radioGroup != null) {
        radioGroup.setSelectedItem(this, checked);
      }
      Window.needsPaint = true;
      if (effect != null) {
        sendPressAfterEffect = sendPress;
        effect.startEffect();
      }
      else
      if (sendPress) {
        postPressedEvent();
      }
    }
  }

  /** returns the preffered width of this control. */
  @Override
  public int getPreferredWidth() {
    return textW + 2 + getPreferredHeight(); // use getPreferredHeight since it may be overloaded
  }

  /** returns the preffered height of this control. */
  @Override
  public int getPreferredHeight() {
    return fmH + Edit.prefH;
  }

  /** Called by the system to pass events to the radio control. */
  @Override
  public void onEvent(Event event) {
    if (event.target != this || !isEnabled()) {
      return;
    }
    switch (event.type) {
    case KeyEvent.ACTION_KEY_PRESS: // guich@550_15
      checked = !checked;
      repaintNow();
      if (radioGroup != null) {
        radioGroup.setSelectedItem(this);
      }
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
        if (radioGroup != null) {
          radioGroup.setSelectedItem(this);
        }
        if (effect == null) {
           postPressedEvent();
        } else {
          sendPressAfterEffect = true;
        }
      }
      break;
    }
  }

  private final static int coords1[] = { 4, 0, 7, 0, 2, 1, 3, 1, 8, 1, 9, 1, // dark grey top
      0, 4, 0, 7, 1, 2, 1, 3, 1, 8, 1, 9, // dark grey left
      4, 1, 7, 1, 2, 2, 3, 2, 8, 2, 9, 2, // black top
      1, 4, 1, 7, 2, 3, 2, 3, 2, 8, 2, 8, // black left
      2, 9, 3, 9, 8, 9, 9, 9, 4, 10, 7, 10, // light grey bottom
      9, 3, 9, 3, 9, 8, 9, 8, 10, 4, 10, 7, // light grey right
      2, 10, 3, 10, 8, 10, 9, 10, 4, 11, 7, 11, // bottom white
      10, 2, 10, 3, 10, 8, 10, 9, 11, 4, 11, 7 // right white
  };
  private final static int coords2[] = { 5, 0, 9, 0, 3, 1, 4, 1, 10, 1, 11, 1, 12, 2, 12, 2, // dark grey top
      0, 5, 0, 9, 1, 3, 1, 4, 1, 10, 1, 11, 2, 2, 2, 2, // dark grey left
      5, 1, 9, 1, 3, 2, 4, 2, 10, 2, 11, 2, 12, 3, 12, 3, // black top
      1, 5, 1, 9, 2, 3, 2, 4, 2, 10, 2, 11, 2, 10, 2, 11, // black left
      3, 12, 4, 12, 5, 13, 9, 13, 10, 12, 11, 12, 10, 12, 11, 12, // light grey bottom
      12, 10, 12, 11, 13, 5, 13, 9, 12, 4, 12, 4, 12, 4, 12, 4, // light grey right
      2, 12, 2, 12, 3, 13, 4, 13, 5, 14, 9, 14, 10, 13, 11, 13, // bottom white
      12, 12, 12, 12, 13, 10, 13, 11, 14, 5, 14, 9, 13, 3, 13, 4 // right white
  };

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    cColor = getForeColor();
    bColor = UIColors.sameColors ? backColor : Color.brighter(getBackColor()); // guich@572_15
    colors[0] = colors[2] = Color.brighter(cColor);
    colors[3] = bColor;
    colors[1] = cColor;
    if (uiVista) {
      try {
        imgSel = getImage(true);
        imgUnsel = getImage(false);
      } catch (Exception e) {
        imgSel = imgUnsel = null;
      }
    }
  }

  @Override
  protected void onFontChanged() {
    textW = fm.stringWidth(this.text);
    onColorsChanged(false);
  }

  /** Called by the system to draw the radio control. */
  @Override
  public void onPaint(Graphics g) {
    boolean enabled = isEnabled();
    int xx, yy;
    // guich@200b4_126: erase the back always
    if (!transparentBackground) {
      g.backColor = backColor;
      g.fillRect(0, 0, width, height);
    }
    boolean big = fmH >= 20;

    if (effect != null) {
      effect.paintEffect(g);
    }

    if (uiAndroid) {
      try {
        int hh = Math.max(fmH, Math.min(width - (textW + 2), uiMaterial ? fmH + Edit.prefH : height));
        if (hh == height) {
          hh -= Edit.prefH;
        }
        Image img = uiMaterial
            ? Resources.radioBkg.getPressedInstance(hh, hh, backColor, checkColor != -1 ? checkColor : foreColor,
                enabled)
            : enabled ? Resources.radioBkg.getNormalInstance(hh, hh, foreColor)
                : Resources.radioBkg.getDisabledInstance(hh, hh, foreColor);
        yy = (height - hh) / 2;
        img.alphaMask = alphaValue;
        g.drawImage(img, 0, yy);
        img.alphaMask = 255;
        if (checked || animating) {
          img = Resources.radioSel.getPressedInstance(hh, hh, backColor, checkColor != -1 ? checkColor : foreColor,
              enabled);
          img.alphaMask = alphaSel;
          NinePatch.tryDrawImage(g, img, 0, yy);
          img.alphaMask = 255;
        }
      } catch (ImageException ie) {
      }
    } else if (uiVista && imgSel != null) {
      g.drawImage(checked ? imgSel : imgUnsel, 0, (height - imgSel.getHeight()) / 2); // guich@tc122_50: /2
    } else {
      drawBack(big, enabled, g);
    }

    // draw label
    yy = (this.height - fmH) >> 1;
    xx = leftJustify ? (uiFlat ? fmH / 2 + 4 : getPreferredHeight() + 1) : (this.width - textW); // guich@300_69 - guich@tc122_42: use preferred height
    g.foreColor = textColor != -1 ? (enabled ? textColor : Color.interpolate(textColor, backColor)) : cColor;
    g.drawText(text, xx, yy, textShadowColor != -1, textShadowColor);
  }

  private void drawBack(boolean big, boolean enabled, Graphics g) {
    int i = 0, k, j = 0;
    int kk = big ? 8 : 6; // number of elements per arc
    int xx = 0; // guich@tc100: can't be -1, now we have real clipping that will cut out if draw out of bounds
    int yy = (this.height - (big ? 15 : 12)) >> 1; // guich@tc114_69: always 14
    g.translate(xx, yy);

    int[] coords = big ? coords2 : coords1;
    // white center
    g.backColor = bColor;
    if (big) {
      g.fillCircle(7, 7, 7);
    } else {
      g.fillCircle(5, 6, 4);
    }
    if (uiVista && enabled) // guich@573_6: shade diagonally
    {
      g.foreColor = Color.darker(bColor, UIColors.vistaFadeStep * 2);
      for (k = 9, j = 6; j >= 0; j--) // bigger k -> darker
      {
        g.foreColor = Color.darker(g.foreColor, UIColors.vistaFadeStep);
        g.drawLine(2, 4 + j, 4 + j, 2);
      }
    }

    // 3d borders
    if (uiFlat) {
      g.foreColor = colors[j];
      if (big) {
        g.drawCircle(7, 7, 7);
      } else {
        g.drawCircle(5, 6, 4);
      }
    } else {
      for (j = 0; j < 4; j++) {
        if (colors[j] != -1) {
          g.foreColor = colors[j];
          for (k = kk; k > 0; k--) {
            g.drawLine(coords[i++], coords[i++], coords[i++], coords[i++]);
          }
        } else {
          i += kk << 2;
        }
      }
    }

    // checked
    g.foreColor = cColor;
    if (checked) {
      g.backColor = cColor;
      if (uiVista) // guich@573_6
      {
        if (big) {
          g.backColor = Color.darker(cColor, UIColors.vistaFadeStep * 9);
          g.fillCircle(7, 7, 4);
          g.backColor = cColor;
          g.fillCircle(7, 7, 2);
        } else {
          g.backColor = cColor;
          g.fillRect(5, 4, 2, 4);
          g.foreColor = Color.darker(cColor, UIColors.vistaFadeStep * 9);
          g.drawLine(4, 5, 4, 6);
          g.drawLine(7, 5, 7, 6);
        }
      } else if (big) {
        g.fillCircle(7, 7, 3);
      } else if (uiFlat) {
        g.fillCircle(5, 6, 2);
      } else {
        g.fillRect(5, 4, 2, 4);
        g.drawLine(4, 5, 4, 6);
        g.drawLine(7, 5, 7, 6);
      }
    }
    g.translate(-xx, -yy);
  }

  /** Clears this control, checking it if clearValueInt is 1. */
  @Override
  public void clear() // guich@572_19
  {
    setChecked(clearValueInt == 1);
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
