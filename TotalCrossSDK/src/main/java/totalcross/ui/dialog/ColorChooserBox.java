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

package totalcross.ui.dialog;

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.ui.AlignedLabelsContainer;
import totalcross.ui.ColorList;
import totalcross.ui.ComboBox;
import totalcross.ui.Container;
import totalcross.ui.Edit;
import totalcross.ui.ImageControl;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/** Shows a color dialog that can be used to select a color. There are several ways to choose a color:
 * <ul>
 * <li> Using a color matrix
 * <li> Using a websafe palette
 * <li> Writting the red, green and blue components
 * <li> Writting the color in hexdecimal.
 * </ul>
 * Here's a sample code:
 * <pre>
 * ColorChooserBox ccb = new ColorChooserBox(getBackColor());
 * ccb.popup();
 * if (ccb.choosenColor != -1) // user pressed cancel?
 * {
 *    int color = ccb.choosenColor; // no, color was selected
 * }
 * </pre>
 * @since TotalCross 
 */
public class ColorChooserBox extends Window implements PenListener {
  private Image colorMatrix;
  private Graphics gcolors;
  private ImageControl ic;
  private ComboBox cbColors;
  private Edit edR, edG, edB, edRGB;
  private PushButtonGroup pbgNum, pbgAlpha, pbgAction;
  private Container cColor;
  private ColorList.Item colorItem;
  private AlignedLabelsContainer alc;
  private KeyEvent ke = new KeyEvent();

  /** The choosen color or -1 if the user cancelled. */
  public int choosenColor = -1;

  public ColorChooserBox(int defaultColor) {
    super("Color Chooser", RECT_BORDER);
    choosenColor = defaultColor;
    fadeOtherWindows = Settings.fadeOtherWindows;
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    setBackColor(UIColors.colorchooserboxBack);
    setForeColor(UIColors.colorchooserboxFore);
    setRect(LEFT, TOP, FILL, FILL);
  }

  @Override
  public void initUI() {
    colorItem = new ColorList.Item(0);
    int extra = Settings.screenHeight < 320 ? 0 : 8;

    alc = new AlignedLabelsContainer(new String[] { "Palette: ", "Red: ", "Green: ", "Blue: ", "Hex: " });
    alc.foreColors = new int[] { 0xAA00AA, 0xAA0000, 0x007700, 0x0000AA, 0x222222 };
    add(alc, LEFT, TOP + 2, WILL_RESIZE, WILL_RESIZE);
    alc.add(cbColors = new ComboBox(new ColorList()), LEFT, alc.getLineY(0), PREFERRED, fmH + Edit.prefH);
    alc.add(edR = new Edit("999"), LEFT, alc.getLineY(1));
    alc.add(edG = new Edit("999"), LEFT, alc.getLineY(2));
    alc.add(edB = new Edit("999"), LEFT, alc.getLineY(3));
    alc.add(edRGB = new Edit("999999"), LEFT, alc.getLineY(4));
    edR.setMode(Edit.KBD_NONE, true);
    edR.autoSelect = true;
    edG.setMode(Edit.KBD_NONE, true);
    edG.autoSelect = true;
    edB.setMode(Edit.KBD_NONE, true);
    edB.autoSelect = true;
    edRGB.setMode(Edit.KBD_NONE, true);
    edRGB.autoSelect = true;
    edRGB.setValidChars("01234567890ABCDEF");
    edRGB.capitalise = Edit.ALL_UPPER;
    alc.resize();
    alc.resetSetPositions();
    alc.setRect(RIGHT - 2, KEEP, KEEP, KEEP); // right can't be used with WILL_RESIZE

    addColorMatrix();

    add(pbgNum = new PushButtonGroup(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" }, 2, 2), SAME,
        AFTER + 4, SAME, PREFERRED + extra, alc);
    pbgNum.setFocusLess(true);
    pbgNum.setVisible(false);
    add(pbgAlpha = new PushButtonGroup(new String[] { "A", "B", "C", "D", "E", "F" }, 2, 1), SAME, AFTER + 2, SAME,
        PREFERRED + extra / 2);
    pbgAlpha.setFocusLess(true);
    pbgAlpha.setVisible(false);

    add(pbgAction = new PushButtonGroup(new String[] { "Ok", "Cancel" }, 2, 1), SAME, BOTTOM - 2, SAME,
        PREFERRED + extra / 2);
    pbgAction.setBackColor(UIColors.colorchooserboxAction);

    cColor = new Container();
    cColor.setBorderStyle(BORDER_SIMPLE);
    add(cColor, SAME, AFTER + extra, SAME, FIT - 4, pbgAlpha);
    colorChanged(choosenColor, -1, -1, -1);
  }

  private void addColorMatrix() {
    try {
      if (colorMatrix != null) {
        colorMatrix = null;
        gcolors = null;
        ic.removePenListener(this);
        remove(ic);
        ic = null;
      }
      Rect r = getClientRect();
      colorMatrix = createColorMatrix(alc.getX() - 5, r.height - 4);
      gcolors = colorMatrix.getGraphics();
      add(ic = new ImageControl(colorMatrix), LEFT + 2, TOP + 2, colorMatrix.getWidth(), colorMatrix.getHeight());
      ic.addPenListener(this);
    } catch (ImageException e) {
      e.printStackTrace();
      unpop();
    }
  }

  @Override
  protected void reposition(boolean recursive) {
    super.reposition(recursive);
    addColorMatrix();
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case ControlEvent.PRESSED:
      if (e.target == pbgAction) {
        switch (pbgAction.getSelectedIndex()) {
        case 1: // cancel
          choosenColor = -1; // no break
        case 0:
          unpop();
          break;
        }
      } else if (e.target instanceof PushButtonGroup && _focus != null && _focus instanceof Edit) {
        String s = ((PushButtonGroup) e.target).getSelectedItem();
        if (s != null) {
          ke.key = s.charAt(0);
          ke.target = _focus;
          _focus.onEvent(ke);
        }
      } else if (e.target == cbColors) {
        ColorList.Item it = (ColorList.Item) cbColors.getSelectedItem();
        if (it != null) {
          colorChanged(it.value, -1, -1, -1);
        }
      } else if (e.target instanceof Edit) {
        int v = 0;
        if (e.target == edR || e.target == edG || e.target == edB) {
          boolean err = false;
          Edit ed = (Edit) e.target;
          try {
            v = Convert.toInt(ed.getText());
            if (v > 255) {
              err = true;
            }
          } catch (InvalidNumberException e1) {
            err = true;
          }
          if (err) {
            v = 0;
            ed.setText("0");
          } else {
            ((Edit) e.target).setText(Convert.toString(v));
          }
          colorChanged(-1, e.target == edR ? v : -1, e.target == edG ? v : -1, e.target == edB ? v : -1);
          if (err) {
            ed.setCursorPos(0, 1);
          }
        } else if (e.target == edRGB) {
          try {
            String s = ((Edit) e.target).getText();
            if (s.length() > 0) {
              v = (int) Convert.toLong(s, 16);
            }
          } catch (InvalidNumberException e1) {
          }
          colorChanged(v, -1, -1, -1);
        }
      }
      break;
    case ControlEvent.FOCUS_IN:
      pbgAlpha.setVisible(e.target == edRGB);
      pbgNum.setVisible(e.target == edRGB || e.target == edR || e.target == edG || e.target == edB);
      break;
    case ControlEvent.FOCUS_OUT:
      if (e.target instanceof Edit) {
        pbgAlpha.setVisible(false);
        pbgNum.setVisible(false);
      }
      break;
    }
  }

  @Override
  public void onPopup() {
    if (choosenColor != -1) {
      colorChanged(choosenColor, -1, -1, -1);
    }
  }

  private void colorChanged(int rgb, int r, int g, int b) {
    if (rgb != -1) {
      choosenColor = rgb;
    }
    if (r == -1) {
      r = Color.getRed(choosenColor);
    }
    if (g == -1) {
      g = Color.getGreen(choosenColor);
    }
    if (b == -1) {
      b = Color.getBlue(choosenColor);
    }
    if (rgb == -1) {
      rgb = choosenColor = (r << 16) | (g << 8) | b;
      edRGB.setText(Convert.unsigned2hex(rgb, 6).toUpperCase());
    } else {
      edRGB.setText(Convert.toString(rgb, 16).toUpperCase());
    }

    edR.setText(Convert.toString(r));
    edG.setText(Convert.toString(g));
    edB.setText(Convert.toString(b));

    cColor.setBackColor(rgb);

    // changes the title colors
    setForeColor(rgb);
    titleColor = Color.getBetterContrast(rgb, Color.BLACK, Color.WHITE);

    colorItem.set(choosenColor);
    int idx = cbColors.indexOf(colorItem);
    cbColors.setSelectedIndex(idx);

    repaintNow();
  }

  private static final int BIAS = 16;
  private static final int ONE = 1 << BIAS;

  private Image createColorMatrix(int w, int h) throws ImageException {
    Image img = new Image(w, h);
    Graphics g = img.getGraphics();
    w -= 2; // remove border from computation
    h -= 2;
    int hueInc = (360 << BIAS) / (h - 1) / 360;
    int satInc = (100 << BIAS) / (w - 1) / 100;
    int hue = 0;
    int sat = 0;
    for (int yy = 0; yy < h; yy++) {
      sat = 0;
      for (int xx = w; --xx >= 0; sat += satInc) {
        g.foreColor = hsb2rgb(hue, sat);
        g.setPixel(xx, yy);
      }
      hue += hueInc;
    }
    return img;
  }

  private int hsb2rgb(int ihue, int isat) {
    int red = 255, green = 255, blue = 255;
    if (isat != 0) {
      int df = ihue >> BIAS << BIAS;
      int h = (ihue - df) * 6;
      int hf = h >> BIAS << BIAS;
      int f = h - hf;
      int p = (255 * (ONE - isat)) >> BIAS;
      int q = (255 * (ONE - (isat * f >> BIAS))) >> BIAS;
      int t = (255 * (ONE - (isat * (ONE - f) >> BIAS))) >> BIAS;
      switch (h >> BIAS) {
      case 0:
        green = t;
        blue = p;
        break;
      case 1:
        red = q;
        blue = p;
        break;
      case 2:
        red = p;
        blue = t;
        break;
      case 3:
        red = p;
        green = q;
        break;
      case 4:
        red = t;
        green = p;
        break;
      case 5:
        green = p;
        blue = q;
        break;
      }
    }
    return (red << 16) | (green << 8) | blue;
  }

  @Override
  public void penDown(PenEvent e) {
    int c = gcolors.getPixel(e.x, e.y);
    colorChanged(c, -1, -1, -1);
  }

  @Override
  public void penUp(PenEvent e) {
  }

  @Override
  public void penDrag(DragEvent e) {
    penDown(e);
  }

  @Override
  public void penDragStart(DragEvent e) {
    // TODO Auto-generated method stub

  }

  @Override
  public void penDragEnd(DragEvent e) {
    // TODO Auto-generated method stub

  }
}
