package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/** MultiButton is a control that displays a single line button with a set of titles.
 * Can be used to replace a Check (with on/off) or a Radio (with their options).
 * Sample:
 * <pre>
 * MultiButton b = new MultiButton(new String[]{"+","-"});
   b.setBackColor(Color.CYAN);
   add(b,LEFT+50,TOP+50,200,FONTSIZE+150);
 * </pre>
 * @since TotalCross 2.0
 */

public class MultiButton extends Control {
  private Image npback, npsel;
  private String[] tits;
  private int sel = -1;
  private boolean[] disabled;

  /** The color used to divide the texts. */
  public int divColor = -1;
  /** The selection color. */
  public int selColor = -1;

  /** Defines if the text will have a 3d style. */
  public boolean is3dText;

  /** Set to true to behave like a Radio, false like a Button */
  public boolean isSticky;

  private int tempIndex;

  /** Constructs a MultiButton with the given captions. */
  public MultiButton(String[] captions) {
    this.tits = captions;
    effect = UIEffects.get(this);
  }

  @Override
  public void onColorsChanged(boolean colorsChanged) {
    npback = null;
  }

  @Override
  public void onBoundsChanged(boolean colorsChanged) {
    npback = null;
  }

  /** Sets the selected index, or -1 to unset it. Only an enabled index can be selected. */
  public void setSelectedIndex(int sel) {
    if (sel == -1 || (0 <= sel && sel < tits.length && (disabled == null || !disabled[sel]))) {
      this.sel = sel;
      repaintNow();
      postPressedEvent();
      if (!isSticky) {
        if (Settings.onJavaSE) {
          Vm.sleep(20);
        }
        this.sel = -1;
        repaint();
      }
    }
  }

  /** Enables or disables a caption */
  public void setEnabled(int idx, boolean enabled) {
    if (disabled == null) {
      disabled = new boolean[tits.length];
    }
    disabled[idx] = !enabled;
    repaint();
  }

  /** Returns if a caption is enabled or not */
  public boolean isEnabled(int idx) {
    return disabled == null || !disabled[idx];
  }

  /** Returns the selected index */
  public int getSelectedIndex() {
    return sel;
  }

  @Override
  public void onPaint(Graphics g) {
    try {
      int bc = getBackColor();
      int tcolor = Color.darker(bc, 32);
      if (npback == null) {
        int c = isEnabled() ? bc : Color.getCursorColor(tcolor);
        if (divColor == -1) {
          divColor = Color.darker(c, 92);
        }
        if (selColor == -1) {
          selColor = Color.darker(backColor, 64);
        }
        npback = NinePatch.getInstance().getNormalInstance(NinePatch.MULTIBUTTON, width, height, c, false);
        npsel = NinePatch.getInstance().getPressedInstance(npback, backColor, selColor);
        npback.alphaMask = alphaValue;
      }
      // without this, clicking will make the button fade out
      g.backColor = parent.getBackColor();
      if (alphaValue == 255) {
        g.fillRect(0, 0, width, height);
      }

      NinePatch.tryDrawImage(g, npback, 0, 0);
      int w = width / tits.length;
      if (sel != -1 && (!uiMaterial || isSticky)) {
        g.copyRect(npsel, sel * w + 2, 0, w - 2, height, sel * w + 2, 1);
      }
      if (effect != null) {
        effect.paintEffect(g);
      }
      for (int i = 0, x0 = 0, n = tits.length - 1; i <= n; i++, x0 += w) {
        String s = tits[i];
        int tw = fm.stringWidth(s);
        int tx = (w - tw) / 2 + x0;
        int ty = (height - fmH) / 2 - 1;
        boolean textEnabled = isEnabled() && (disabled == null || !disabled[i]);

        if (is3dText && textEnabled) {
          g.foreColor = tcolor;
          g.drawText(s, tx + 1, ty - 1);
          g.foreColor = i == sel ? tcolor : bc;
          g.drawText(s, tx - 1, ty + 1);
        }
        g.foreColor = textEnabled ? getForeColor() : Color.brighter(foreColor);
        g.drawText(s, tx, ty);

        g.drawText(tits[i], tx, ty);
        if (i < n) {
          g.foreColor = isEnabled() ? divColor : Color.brighter(divColor);
          int y1 = (height - fmH) / 2, y2 = y1 + fmH, xx = x0 + w;
          g.drawLine(xx, y1, xx, y2);
          xx++;
          g.foreColor = tcolor;
          g.drawLine(xx, y1, xx, y2);
        }
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  @Override
  public int getEffectW() {
    int w = width / tits.length;
    return tempIndex == -1 ? 0 : w - 2;
  }

  @Override
  public int getEffectH() {
    return height;
  }

  @Override
  public int getEffectX() {
    if (tempIndex == -1 || (disabled != null && disabled[tempIndex])) {
      return UIEffects.X_UNKNOWN;
    }
    int w = width / tits.length;
    return tempIndex * w + 2;
  }

  @Override
  public int getEffectY() {
    return 0;
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case PenEvent.PEN_DOWN: {
      PenEvent pe = (PenEvent) e;
      tempIndex = isInsideOrNear(pe.x, pe.y) ? pe.x / (width / tits.length) : -1;
      break;
    }
    case PenEvent.PEN_UP:
      if (isEnabled() && !hadParentScrolled()) {
        PenEvent pe = (PenEvent) e;
        int sel = isInsideOrNear(pe.x, pe.y) ? pe.x / (width / tits.length) : -1;
        if (sel != this.sel && (sel == -1 || disabled == null || (sel < disabled.length && !disabled[sel]))) {
          setSelectedIndex(sel);
        }
      }
      break;
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (isEnabled()) {
        KeyEvent ke = (KeyEvent) e;
        if (ke.isPrevKey()) {
          for (int i = tits.length; --i >= 0;) {
            if (--sel < 0) {
              sel = tits.length - 1;
            }
            if (disabled == null || !disabled[sel]) {
              break;
            }
          }
          if (!Settings.keyboardFocusTraversable) {
            postPressedEvent();
          }
          repaint();
        } else if (ke.isNextKey()) {
          for (int i = tits.length; --i >= 0;) {
            if (++sel >= tits.length) {
              sel = 0;
            }
            if (disabled == null || !disabled[sel]) {
              break;
            }
          }
          if (!Settings.keyboardFocusTraversable) {
            postPressedEvent();
          }
          repaint();
        } else if (ke.isActionKey()) {
          int sel = this.sel;
          sel = -1;
          setSelectedIndex(sel);
        }
      }
      break;
    }
  }

  @Override
  public int getPreferredHeight() {
    return fmH + Edit.prefH;
  }

  @Override
  public int getPreferredWidth() {
    int w = 0;
    for (int i = tits.length; --i >= 0;) {
      w += fm.stringWidth(tits[i]);
    }
    return w + fmH * 2;
  }
}
