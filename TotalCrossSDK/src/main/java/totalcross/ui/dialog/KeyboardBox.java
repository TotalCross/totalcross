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
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.gfx.Rect;

/** A popup keyboard to be used with the Edit and MultiEdit class. */

public class KeyboardBox extends Window // guich@102
{
  private Control destControl;
  private Rect destRect;
  private Container destCont;
  private PushButtonGroup pbs[] = new PushButtonGroup[4];
  private KeyEvent ke = new KeyEvent();

  /** Event issued by this keybard on the onUnpop method. The edit must call pushPosState. */
  public static final int KEYBOARD_ON_UNPOP = 1001; // guich@320_34
  /** Event issued by this keybard on the postUnpop method. The edit must call popPosState. */
  public static final int KEYBOARD_POST_UNPOP = 1002; // guich@320_34

  private boolean isCaps, isShift;
  /** Used to access the names member. */
  public final static int TEXT_PAD = 0;
  /** Used to access the names member. */
  public final static int CAPS_PAD = 1;
  /** Used to access the names member. */
  public final static int SPACE_PAD = 2;
  /** Used to access the names member. */
  public final static int SPECIAL_PAD = 3;

  public static String names[][] = // guich@400_4: made static and public
      { { "123 +$&() áàãà456 -#@[] âéêè789 *%|{} íóôõ.0, /=\\<> öúçñ               qwertyuiop'_  asdfghjkl:~^  zxcvbnm!?;\"` " }, // will be parsed later
          { "Caps", "Shift" }, { " " }, { "«", "Done" }, };

  public static String namesUp[] = { null, null, };

  public KeyboardBox() {
    super(" Keyboard ", RECT_BORDER);
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    fadeOtherWindows = Settings.fadeOtherWindows;

    setBackColor(UIColors.keyboardBack); // before control definitions!

    if (names[0].length == 1) // still not parsed?
    {
      String s = names[0][0];
      String[] to = names[0] = new String[s.length()];
      for (int i = s.length() - 1; i >= 0; i--) {
        char c = s.charAt(i);
        if (c != ' ') {
          to[i] = Convert.toString(c);
        }
      }
    }
  }

  private void convertToUpper() // guich@564_8
  {
    String[] from = names[TEXT_PAD];
    String[] to = new String[from.length];
    for (int i = from.length - 1; i >= 0; i--) {
      try {
        to[i] = from[i].toUpperCase();
      } catch (NullPointerException e) {
      }
    }
    namesUp = to;
  }

  @Override
  protected void onWindowPaintFinished() {
    destControl.requestFocus();
  }

  @Override
  protected void onPopup() {
    destControl = Window.topMost.getFocus();
    destRect = destControl.getRect();
    destCont = destControl.getParent();

    if (pbs[0] == null) {
      int glue = -1;
      for (int i = 0; i < pbs.length; i++) {
        add(pbs[i] = new PushButtonGroup(names[i], false, -1, i == TEXT_PAD ? glue : 1, i >= CAPS_PAD ? 8 : 4,
            i == TEXT_PAD ? 8 : 1, i != SPECIAL_PAD, (i != CAPS_PAD) ? PushButtonGroup.BUTTON : PushButtonGroup.CHECK));
        pbs[i].setBackColor(i < CAPS_PAD ? UIColors.keyboardFore : UIColors.keyboardAction);
        pbs[i].appId = i;
        pbs[i].setFocusLess(true); // guich@320_32
        pbs[i].setFont(font);
      }
      pbs[CAPS_PAD].checkAppearsRaised = true;
      convertToUpper();
    }
    add(destControl);
    // repositions the window and the controls based in the control's size. Note that a MultiEdit has a different height of an Edit, so this must be dinamically computed
    int hh = pbs[TEXT_PAD].getPreferredHeight() + pbs[CAPS_PAD].getPreferredHeight() + destControl.getPreferredHeight()
        + getPreferredHeight() + 4 * 3 + fmH / 2;
    int ww = Math.min(Settings.screenWidth, Settings.screenHeight) - 4;
    setRect(CENTER, CENTER, ww, hh);
    destControl.setRect(CENTER, TOP + 2, Math.min(destRect.width, width - 10), PREFERRED);
    pbs[TEXT_PAD].setRect(LEFT + 2, AFTER + 4, FILL - 3, PREFERRED);

    pbs[CAPS_PAD].setRect(LEFT + 2, BOTTOM - 2, PREFERRED, PREFERRED);
    pbs[SPECIAL_PAD].setRect(RIGHT - 2, SAME, PREFERRED, SAME);
    pbs[SPACE_PAD].setRect(AFTER + 1, SAME, FIT - 2, SAME, pbs[CAPS_PAD]);
    ke.target = destControl;
  }

  @Override
  protected void onUnpop() {
    destControl._onEvent(new ControlEvent(KEYBOARD_ON_UNPOP, destControl)); // guich@320_34
    destControl.setRect(destRect.x, destRect.y, destRect.width, destRect.height);
    destCont.add(destControl);
  }

  @Override
  protected void postUnpop() {
    destControl._onEvent(new ControlEvent(KEYBOARD_POST_UNPOP, destControl)); // guich@320_34
    postPressedEvent(); // guich@580_27
  }

  @Override
  public void onEvent(Event event) {
    switch (event.type) {
    case KeyEvent.SPECIAL_KEY_PRESS:
      switch (((KeyEvent) event).key) {
      case SpecialKeys.KEYBOARD_ABC:
      case SpecialKeys.KEYBOARD_123:
        unpop();
        break;
      }
      break;
    case ControlEvent.PRESSED:
      if (event.target instanceof PushButtonGroup) {
        PushButtonGroup pb = (PushButtonGroup) event.target;
        int sel = pb.getSelectedIndex();
        if (sel >= 0) // guich@200b4_105: fix ArrayIndexOutOfBounds exception
        {
          String st = pb.getSelectedItem();
          if (pb.appId == SPECIAL_PAD || pb.appId == CAPS_PAD || pb.appId == SPACE_PAD) // special char?
          {
            int key = -1;
            switch (st.charAt(0)) {
            case 'D':
              pb.setSelectedIndex(-1);
              unpop();
              break;
            case ' ':
              key = ' ';
              break;
            case 'S':
              isShift = !isShift;
              isCaps = false;
              break;
            case 'C':
              isCaps = !isCaps;
              isShift = false;
              break;
            case '\u00AB': // guich@200b4: char « is not being recognized correctly in the SPT1700.
            default:
              key = SpecialKeys.BACKSPACE;
              break;
            }
            if (key != -1) {
              insertKey(key);
            } else if (pb.appId == CAPS_PAD) {
              updateShiftPad(isShift || isCaps);
            }
          } else {
            insertKey(st.charAt(0));
            cancelShift();
          }
        }
      }
      break;
    }
  }

  private void updateShiftPad(boolean shift) // guich@564_8
  {
    // we assume that the uppercase will have almost the same width of the lowercase
    pbs[TEXT_PAD].names = shift ? namesUp : names[TEXT_PAD];
    Window.needsPaint = true;
  }

  private void cancelShift() {
    if (isShift) {
      isShift = false;
      pbs[CAPS_PAD].setSelectedIndex(-1);
      updateShiftPad(false);
    }
  }

  private void insertKey(int key) {
    ke.key = key;
    destControl._onEvent(ke);
  }
}
