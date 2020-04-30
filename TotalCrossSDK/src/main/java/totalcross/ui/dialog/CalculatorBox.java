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
import totalcross.sys.SpecialKeys;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.SpinList;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;

/** This class is used by the Edit class when its mode is set to CURRENCY and displays
 * a calculator with six basic operations and a numeric pad.
 */

public class CalculatorBox extends Window {
  /** Implement this interface to check if the given value is a valid value to be returned.
   * Return null if the value is correct, or a message that will be displayed to the user if not.
   * 
   * Note that the range check only occurs when the user do NOT press Cancel.
   * 
   * <pre>
   *  Edit ed = new Edit();
   *  ed.rangeCheck = new CalculatorBox.RangeCheck()
   *  {
   *     public String check(double value)
   *     {
   *        if (value > 0 && value < 10)
   *           return null;
   *        return "The value must be between 0 and 10";
   *     }
   *  };
   *  ed.setMode(Edit.CURRENCY,true);
   *  add(ed,LEFT,TOP);
   * </pre>
   * 
   * @since TotalCross 2.0
   */
  public static interface RangeCheck {
    public String check(double value);
  }

  /** The RangeCheck instance that will be called before the user closes the box. */
  public RangeCheck rangeCheck;

  /** The Edit used to show the number. */
  public Edit edNumber;
  private PushButtonGroup pbgAction, numericPad, pbgArrows, pbgOp, pbgOp2, pbgEq;
  private String answer;
  private KeyEvent ke = new KeyEvent(), backspace; // guich@421_59
  private boolean showOperations;
  private String ds;
  // calculator computations
  private String last = null;
  private boolean clearNext;
  private int lastSel = -1;
  private boolean callNext;
  /** Strings used to display the action messages. You can localize these strings if you wish. */
  public static String[] actions = { "Clear", "Ok", "Cancel" }; // guich@320_44: added reuse button

  /** The default title. */
  public static String defaultTitle = "Numeric Pad";

  /** Defines an optional character to be used in the NumericBox. Replaces the decimal separator / 00 char.
   * @since TotalCross 1.5 
   */
  public String optionalValue;

  /** The maximum length for the edit that will be created. */
  public int maxLength = -2;

  /** The default value of the edit. */
  public String defaultValue;

  /** The control that had focus when this CalculatorBox was popped up. */
  public Control cOrig;

  /** The desired control that will be the original one. */
  public Control cOrigDefault;

  /** Set to true to don't replace the original value in the Edit if user pressed Ok. */
  public boolean keepOriginalValue;

  /** Set to true to replace the "Clear" button by the "Next" button. This
   * button is equivalent to the "Ok" button, but it also changes the focus to 
   * the next field. The user can still clean the edit by clicking the backspace &lt;&lt; button.
   * 
   * The default behaviour calls moveFocusToNextControl. You can change it by overriding the 
   * method <code>gotoNext</code>.
   * 
   * @since TotalCross 1.53
   */
  public static boolean showNextButtonInsteadOfClear;

  /** Constructs a CalculatorBox with the 6 basic operations visible. */
  public CalculatorBox() {
    this(true);
  }

  /** Constructs a CalculatorBox with the 6 basic operations hidden. */
  public CalculatorBox(boolean showOperations) {
    super(defaultTitle, uiAndroid ? ROUND_BORDER : RECT_BORDER); // with caption and borders
    fadeOtherWindows = Settings.fadeOtherWindows;
    started = true;
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    backspace = new KeyEvent();
    backspace.type = KeyEvent.SPECIAL_KEY_PRESS;
    backspace.key = SpecialKeys.BACKSPACE;
    this.showOperations = showOperations;
  }

  private void setupUI(boolean isReposition) // guich@tc100b5_28
  {
    setBackColor(showOperations ? UIColors.calculatorBack : UIColors.numericboxBack); // before control definitions!

    setRect(LEFT, TOP, WILL_RESIZE, WILL_RESIZE);

    int hh = fmH * 2;

    if (!isReposition || edNumber == null) {
      if (edNumber != null) {
        remove(edNumber);
      }
      edNumber = cOrig != null && cOrig instanceof Edit ? ((Edit) cOrig).getCopy() : new Edit();
      edNumber.setKeyboard(Edit.KBD_NONE);
      edNumber.autoSelect = true;
      if (cOrig != null && cOrig instanceof SpinList) {
        edNumber.setDecimalPlaces(0);
        edNumber.setMode(Edit.CURRENCY, true);
      }
      if (maxLength != -2) {
        edNumber.setMaxLength(maxLength);
      }
      backspace.target = edNumber;
      add(edNumber);
    }
    Font f = font.adjustedBy(3, false);
    edNumber.setFont(f);
    edNumber.setRect(LEFT + 2, TOP + 4, Math.min(hh * 5, Settings.screenWidth - 20), PREFERRED);

    // positioning arrows
    if (pbgArrows == null) {
      pbgArrows = new PushButtonGroup(new String[] { "<", ">", "<<" }, false, -1, 2, 12, 1, true,
          PushButtonGroup.BUTTON);
      pbgArrows.autoRepeat = true;
      pbgArrows.setFocusLess(true);
      pbgArrows.clearValueInt = -1;
      add(pbgArrows);
    }
    pbgArrows.setRect(SAME, AFTER + 4, SAME, hh);

    // numeric pad
    if (numericPad == null) {
      add(numericPad = new PushButtonGroup(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "\u00b1" },
          false, -1, 2, 10, 4, true, PushButtonGroup.BUTTON));
      numericPad.setFont(font.adjustedBy(2));
      numericPad.setFocusLess(true); // guich@320_32
      numericPad.clearValueInt = -1;
    }
    numericPad.setRect(SAME, AFTER + 4, SAME, Settings.screenHeight > hh * 8 ? hh * 6 : hh * 4); // guich@571_9
    String[] names = numericPad.names;
    ds = Convert.toString(Settings.decimalSeparator);
    names[9] = optionalValue != null ? optionalValue
        : edNumber.getMode() == Edit.CURRENCY && edNumber.getDecimalPlaces() > 0 ? "00" : ds;
    numericPad.setNames(names);

    if (pbgAction == null) {
      pbgAction = new PushButtonGroup(actions, false, -1, 2, 12, 1, true, PushButtonGroup.BUTTON);
      pbgAction.setFocusLess(true);
      pbgAction.clearValueInt = -1;
      add(pbgAction);
    }
    pbgAction.setRect(SAME, AFTER + 2, SAME, hh);

    if (showOperations && pbgOp == null) {
      Font ff = numericPad.getFont();
      String[] opers = { "+", "-", "*", "÷" };
      pbgOp = new PushButtonGroup(opers, false, -1, 2, 12, opers.length, true, PushButtonGroup.NORMAL);
      pbgOp.setFont(ff);
      pbgOp.setFocusLess(true);
      pbgOp.clearValueInt = -1;
      pbgOp.setCursorColor(Color.ORANGE);
      add(pbgOp);

      pbgOp2 = new PushButtonGroup(new String[] { "%" }, false, -1, 2, 12, 1, true, PushButtonGroup.BUTTON);
      pbgOp2.setFont(ff);
      pbgOp2.setFocusLess(true);
      pbgOp2.setCursorColor(Color.ORANGE);
      pbgOp2.clearValueInt = -1;
      add(pbgOp2);

      pbgEq = new PushButtonGroup(new String[] { "=" }, false, -1, 2, 12, 1, true, PushButtonGroup.BUTTON);
      pbgEq.setFont(ff);
      pbgEq.setFocusLess(true);
      pbgEq.clearValueInt = -1;
      add(pbgEq);

    }
    if (pbgOp != null) {
      pbgOp.setRect(AFTER + 2, SAME, hh, SAME, numericPad);
      pbgOp2.setRect(AFTER + 2, SAME, hh, SAME, pbgArrows);
      pbgEq.setRect(AFTER + 2, SAME, hh, SAME, pbgAction);
      edNumber.setRect(KEEP, KEEP, edNumber.getWidth() + 2 + hh, KEEP);
    }

    setInsets(2, 2, 2, 2);
    resize();
    setRect(CENTER, CENTER, KEEP, KEEP);

    numericPad.setBackColor(showOperations ? UIColors.calculatorBack : UIColors.numericboxBack);
    pbgAction.setBackColor(showOperations ? UIColors.calculatorAction : UIColors.numericboxAction);
    pbgArrows.setBackColor(showOperations ? UIColors.calculatorAction : UIColors.numericboxAction);
    edNumber.setBackColor(backColor);
    if (pbgOp != null) {
      pbgEq.setBackColor(showOperations ? UIColors.calculatorAction : UIColors.numericboxAction);
    }
  }

  /** Gets the answer that the user selected to be pasted.
   * It can be the first operator, the total computed or null if the user canceled.
   */
  public String getAnswer() // guich@200b4_193: get the 'pasted' answer
  {
    return answer;
  }

  @Override
  public void clear() {
    super.clear();
    last = answer = null;
  }

  @Override
  public void onUnpop() {
    setFocus(this);
  }

  @Override
  public void onPopup() {
    Control c = topMost.getFocus();
    cOrig = cOrigDefault != null ? cOrigDefault : c instanceof Edit || c instanceof SpinList ? (Control) c : null;
    setupUI(false);
    clear();
    if (cOrig != null) {
      String s = cOrig instanceof Edit ? ((Edit) cOrig).getTextWithoutMask() : ((SpinList) cOrig).getSelectedItem();
      if (s.length() == 0 || "+-0123456789".indexOf(s.charAt(0)) != -1) {
        edNumber.setText(s);
      }
    }
    if (defaultValue != null) {
      edNumber.setText(defaultValue);
    }
  }

  @Override
  public void postPopup() {
    setFocus(edNumber);
  }

  @Override
  protected void postUnpop() {
    if (answer != null) {
      postPressedEvent();
    }
    if (cOrig != null && callNext) {
      callNext = false;
      Control next = gotoNext();
      if (next != null && next instanceof Edit) {
        ((Edit) next).showKeyboardOnNextEvent = true;
      }
    }
  }

  /** Returns the next control to be focused when Next is clicked.
   * By default, calls moveFocusToNextControl.
   * @since TotalCross 1.53
   */
  protected Control gotoNext() {
    return cOrig.getParent().moveFocusToNextControl(cOrig, true);
  }

  @Override
  public void onEvent(Event event) {
    try {
      switch (event.type) {
      case KeyEvent.SPECIAL_KEY_PRESS:
        if (clearNext) {
          clearNext = false;
          edNumber.clear();
        }
        int key = ((KeyEvent) event).key;
        if (key == SpecialKeys.CALC) {
          unpop();
        }
        break;
      case ControlEvent.PRESSED:
        if (pbgEq != null && event.target == pbgEq && pbgEq.getSelectedIndex() != -1) {
          compute(-2);
        } else if (pbgOp != null && event.target == pbgOp && pbgOp.getSelectedIndex() != -1) {
          compute(pbgOp.getSelectedIndex());
        } else if (pbgOp2 != null && event.target == pbgOp2 && pbgOp2.getSelectedIndex() != -1) {
          compute(-3);
        } else if (event.target == pbgArrows && pbgArrows.getSelectedIndex() != -1) {
          switch (pbgArrows.getSelectedIndex()) {
          case 0: {
            int p = edNumber.getCursorPos()[1] - 1;
            if (p >= 0) {
              edNumber.setCursorPos(p, p);
            }
            break;
          }
          case 1: {
            int p = edNumber.getCursorPos()[1] + 1;
            if (p <= edNumber.getLength()) {
              edNumber.setCursorPos(p, p);
            }
            break;
          }
          case 2: {
            edNumber.onEvent(backspace);
            break;
          }
          }
        } else if (event.target == pbgAction && pbgAction.getSelectedIndex() != -1) {
          switch (pbgAction.getSelectedIndex()) {
          case 0:
            if (showNextButtonInsteadOfClear) {
              callNext = true;
              ok();
            } else {
              clear();
              if (pbgOp != null) {
                pbgOp.clear();
              }
            }
            break;
          case 1:
            ok();
            break;
          case 2:
            clear();
            unpop();
            break;
          }
        } else if (event.target == numericPad) {
          String s = numericPad.getSelectedItem();
          if (s != null) {
            if (s.equals(ds) && (clearNext || edNumber.getLength() == 0 || edNumber.getText().indexOf(ds) != -1)) {
              return;
            }
            if (s.equals("±")) {
              String t = unformat(edNumber.getTextWithoutMask());
              if (t.length() > 0) {
                if (t.startsWith("-")) {
                  t = t.substring(1);
                } else {
                  t = "-".concat(t);
                }
                edNumber.setText(t);
                edNumber.setCursorPos(t.length(), t.length());
              }
            } else {
              if (clearNext) {
                clearNext = false;
                edNumber.clear();
              }
              for (int i = 0, n = s.length(); i < n; i++) {
                ke.key = s.charAt(i);
                ke.target = edNumber;
                edNumber._onEvent(ke);
              }
            }
          }
        }
        break;
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  private void ok() throws InvalidNumberException {
    answer = unformat(edNumber.getTextWithoutMask());
    String msg;
    if (answer != null && answer.length() > 0 && rangeCheck != null
        && (msg = rangeCheck.check(Convert.toDouble(answer))) != null) {
      new MessageBox(title, msg).popup();
    } else {
      if (cOrig != null && !keepOriginalValue) {
        if (cOrig instanceof Edit) {
          ((Edit) cOrig).setText(answer, true);
        } else {
          ((SpinList) cOrig).setSelectedItem(answer);
        }
      }
      unpop();
    }
  }

  @Override
  public void reposition() {
    setupUI(true);
  }

  private void compute(int selectedIndex) throws Exception {
    try {
      switch (selectedIndex) {
      case -3: // %
        if (edNumber.getLength() > 0) {
          double d2 = Convert.toDouble(unformat(edNumber.getTextWithoutMask()));
          if (last == null) {
            last = showResult(d2 / 100);
          } else // apply the % to the previous number
          {
            double d1 = Convert.toDouble(last);
            double res = d1 * d2 / 100;
            showResult(res); // keep last
          }
        }
        return;
      case -2: // =
      case 0: // +
      case 1: // -
      case 2: // *
      case 3: // /
      case 4: // ^
        if (last == null && edNumber.getLength() == 0) {
          pbgOp.clear();
          return;
        }
        if (last != null && lastSel != -1) {
          double d1 = Convert.toDouble(last);
          double d2 = Convert.toDouble(unformat(edNumber.getTextWithoutMask()));
          double res = 0;
          switch (lastSel) {
          case 0:
            res = d1 + d2;
            break;
          case 1:
            res = d1 - d2;
            break;
          case 2:
            res = d1 * d2;
            break;
          case 4:
            res = Math.pow(d1, d2);
            break;
          case 3:
            if (d2 == 0) {
              new MessageBox("Error", "Division by 0").popup();
            } else {
              res = d1 / d2;
            }
            break;
          }
          showResult(res);
          if (selectedIndex == -2) {
            pbgOp.clear();
          }
        }
        if (edNumber.getLength() == 0) {
          last = null;
          clearNext = false;
        } else {
          last = unformat(edNumber.getTextWithoutMask());
          if (last.endsWith(".")) {
            last = last.substring(0, last.length() - 1);
            edNumber.setText(last);
          }
          clearNext = true;
        }
        break;
      }
      lastSel = selectedIndex == -2 ? -1 : selectedIndex;
    } catch (InvalidNumberException ine) {
      new MessageBox("Message", "Overflow or underflow error!");
    }
  }

  private static String unformat(String s) {
    if (s.indexOf(',') >= 0) {
      return Convert.replace(s, ".", "").replace(',', '.');
    }
    return s;
  }

  private String showResult(double res) {
    int dc = res == (double) (int) res ? 0 : edNumber.getDecimalPlaces();
    String s = Convert.toString(res, dc);
    int p = s.indexOf('.');
    if (p != -1) {
      while (s.length() > p + 1 && s.endsWith("0")) {
        s = s.substring(0, s.length() - 1);
      }
    }
    if (edNumber.getMode() != Edit.CURRENCY && p != -1 && Settings.decimalSeparator != '.') {
      s = s.replace('.', Settings.decimalSeparator);
    }
    edNumber.setText(s);
    return s;
  }
}
