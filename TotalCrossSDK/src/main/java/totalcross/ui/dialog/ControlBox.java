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
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.Control;
import totalcross.ui.Label;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;

/** A popup window that displays any Control, given as parameter to the constructor. */

public class ControlBox extends Window {
  protected Label msg;
  private PushButtonGroup btns;
  protected Control cb;
  private int selected = -1;
  protected int prefW, prefH;
  private String originalText;
  /** Defines the y position on screen where this window opens. Can be changed to TOP or BOTTOM. Defaults to CENTER.
   * @see #CENTER
   * @see #TOP
   * @see #BOTTOM
   */
  public int yPosition = CENTER; // guich@tc110_7

  /** If you set the buttonCaptions array in the construction, you can also set this
   * public field to an int array of the keys that maps to each of the buttons.
   * For example, if you set the buttons to {"Ok","Cancel"}, you can map the enter key
   * for the Ok button and the escape key for the Cancel button by assigning:
   * <pre>
   * buttonKeys = new int[]{SpecialKeys.ENTER,SpecialKeys.ESCAPE};
   * </pre>
   * Note that if you use the default Ok/Cancel buttons, this mapping is already done.
   * @since TotalCross 1.27
   */
  public int[] buttonKeys; // guich@tc126_40

  /** Constructs a ControlBox with the given parameters, and an Ok and a Cancel buttons.
   * The control may have at least one item, which will be used to determine the preferred size.
   * @param title The window's title.
   * @param text The text that will be displayed in a Label above the control.
   * @param cb The control that will be used to get input from the user.
   */
  public ControlBox(String title, String text, Control cb) {
    this(title, text, cb, PREFERRED, PREFERRED, new String[] { "Ok", "Cancel" }, 1);
    buttonKeys = new int[] { SpecialKeys.ENTER, SpecialKeys.ESCAPE };
  }

  /** Constructs a ControlBox with the given parameters.
   * The control may have at least one item, which will be used to determine the preferred size.
   * @param title The window's title.
   * @param text The text that will be displayed in a Label above the control.
   * @param cb The control that will be used to get input from the user.
   * @param buttonCaptions The button captions that will be used in the PushButtonGroup.
   */
  public ControlBox(String title, String text, Control cb, String[] buttonCaptions) {
    this(title, text, cb, PREFERRED, PREFERRED, buttonCaptions, 1);
  }

  /** Constructs a ControlBox with the given parameters.
   * The control may have at least one item, which will be used to determine the preferred size.
   * @param title The window's title.
   * @param text The text that will be displayed in a Label above the control.
   * @param cb The control that will be used to get input from the user.
   * @param buttonCaptions The button captions that will be used in the PushButtonGroup, or null to hide them.
   * @param prefW The preferred width for the control. You can also use FILL or PREFERREED
   * @param prefH The preferred height for the control.
   */
  public ControlBox(String title, String text, Control cb, int prefW, int prefH, String[] buttonCaptions) {
    this(title, text, cb, prefW, prefH, buttonCaptions, 1);
  }

  /** Constructs a ControlBox with the given parameters.
   * The control may have at least one item, which will be used to determine the preferred size.
   * @param title The window's title.
   * @param text The text that will be displayed in a Label above the control.
   * @param cb The control that will be used to get input from the user.
   * @param buttonCaptions The button captions that will be used in the PushButtonGroup, or null to hide them.
   * @param prefW The preferred width for the control. You can also use FILL or PREFERREED
   * @param prefH The preferred height for the control.
   * @param buttonRows The number of rows for the buttons.
   */
  public ControlBox(String title, String text, Control cb, int prefW, int prefH, String[] buttonCaptions,
      int buttonRows) {
    super(title, ROUND_BORDER);
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    fadeOtherWindows = Settings.fadeOtherWindows;
    if (buttonCaptions != null) {
      btns = new PushButtonGroup(buttonCaptions, false, -1, uiAndroid ? fmH / 2 : 4, 6, buttonRows, uiAndroid,
          PushButtonGroup.BUTTON);
    }
    msg = new Label(originalText = text, Control.CENTER);
    this.cb = cb;
    this.prefW = prefW;
    this.prefH = prefH;
  }

  @Override
  protected void onPopup() // guich@tc100b5_28
  {
    if (children != null) {
      return;
    }

    if (btns != null) {
      btns.setFont(font);
    }
    cb.setFont(font);
    msg.setFont(font);
    int maxW = Settings.screenWidth - fmH * 2;
    String text = originalText;
    if (text.indexOf('\n') < 0 && fm.stringWidth(text) > maxW) {
      text = Convert.insertLineBreak(maxW, fm, text.replace('\n', ' '));
    }
    msg.setText(text);
    int wb = btns == null ? 0 : btns.getPreferredWidth();
    int hb = btns == null ? 0 : btns.getPreferredHeight();
    if (uiAndroid && wb > 0) {
      wb += fmH * btns.names.length;
      hb += fmH;
    }
    int wm = Math.min(msg.getPreferredWidth() + 1, Settings.screenWidth - 6);
    int hm = msg.getPreferredHeight();
    int we = prefW == PREFERRED ? cb.getPreferredWidth() : prefW;
    int he = prefH == PREFERRED ? cb.getPreferredHeight() : prefH;
    int captionH = titleFont.fm.height + 10;

    int h = captionH + hb + hm + he + fmH;
    if (uiAndroid) {
      h += fmH;
    }
    int w = Convert.max(wb, wm, we, titleFont.fm.stringWidth(title != null ? title : "")) + 6; // guich@200b4_29
    w = Math.min(w, Settings.screenWidth); // guich@200b4_28: dont let the window be greater than the screen size
    h = Math.min(h, Settings.screenHeight);
    setRect(CENTER, yPosition, w, h);
    add(msg);
    if (btns != null) {
      add(btns);
    }
    add(cb);
    msg.setRect(LEFT, TOP, FILL, hm);
    if (btns != null) {
      btns.setRect(CENTER, BOTTOM - (uiAndroid ? fmH / 2 : 2), wb, hb);
    }
    msg.setBackForeColors(backColor, foreColor);
    int gap = we == FILL ? fmH / 2 : 0;
    cb.setRect(we == FILL ? LEFT + gap : CENTER, AFTER + fmH / 2, we - gap, he, msg);
  }

  /** handle scroll buttons and normal buttons */
  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case KeyEvent.KEY_PRESS:
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (buttonKeys != null) {
        int k = ((KeyEvent) e).key;
        for (int i = buttonKeys.length; --i >= 0;) {
          if (buttonKeys[i] == k) {
            btns.setSelectedIndex(selected = i);
            unpop();
            break;
          }
        }
      }
      break;
    case ControlEvent.PRESSED:
      if (e.target == btns && (selected = btns.getSelectedIndex()) != -1) {
        btns.setSelectedIndex(-1);
        unpop();
      }
      break;
    }
  }

  /** Returns the pressed button index, starting from 0 */
  public int getPressedButtonIndex() {
    return selected;
  }

  /** returns the control associated */
  public Control getControl() {
    return cb;
  }

  @Override
  protected void postUnpop() {
    if (selected != -1) {
      postPressedEvent();
    }
  }
}