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
import totalcross.ui.Edit;
import totalcross.ui.Label;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.Color;

/** This class pops up a Window with a Label, an Edit and some buttons. Good
    to input some text from the user.
    Here is an example (taken from examples/apps/GuiBuilder):
    <pre>
    // on some event...
      if (renameDialog == null)
         renameDialog = new InputBox("Project Rename","Please enter the new name which will be used for the project:","");
      renameDialog.setValue(projectName);
      renameDialog.popup();
      // when window closes...
      if (renameDialog.getPressedButtonIndex() == 0) // ok?
      {
         projectName = renameDialog.getValue();
      }
    </pre>
 */

public class InputBox extends Window {
  private Label msg;
  private PushButtonGroup btns;
  private Edit ed;
  private int selected = -1;
  private String originalText;
  private String[] buttonCaptions;
  private int labelAlign = CENTER;
  private int gap, insideGap;

  /** Set to true to automatically open the keyboard once the InputBox is open.
   * @since TotalCross 1.53
   */
  public static boolean openKeyboardOnPopup;
  /**
   * Set at the object creation. if true, all the buttons will have the same width, based on the width of the largest
   * one.<br>
   * Default value is false.
   * 
   * @since TotalCross 1.27
   */
  private boolean allSameWidth;

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

  /** Creates a new InputBox with the given window Title,
   * the given label Text, the given Default value for the Edit,
   * and two buttons "Ok" and "Cancel".
   * The text used in a Label can be multi-line. If the text is too big, it will be splitted.
   */
  public InputBox(String title, String text, String defaultValue) {
    this(title, text, defaultValue, new String[] { "Ok", "Cancel" }, false, 4, 6);
    buttonKeys = new int[] { SpecialKeys.ENTER, SpecialKeys.ESCAPE };
  }

  /** Creates a new InputBox with the given window Title,
   * the given label Text, the given Default value for the Edit
   * and with the given buttons.
   * The text used in a Label can be multi-line. If the text is too big, it will be splitted.
   */
  public InputBox(String title, String text, String defaultValue, String[] buttonCaptions) {
    this(title, text, defaultValue, buttonCaptions, false, 4, 6);
  }

  /** Creates a new InputBox with the given window Title,
   * the given label Text, the given Default value for the Edit
   * and with the given buttons.
   * The text used in a Label can be multi-line. If the text is too big, it will be splitted.
   */
  public InputBox(String title, String text, String defaultValue, String[] buttonCaptions, boolean allSameWidth,
      int gap, int insideGap) {
    super(title, ROUND_BORDER);
    this.buttonCaptions = buttonCaptions;
    this.gap = gap;
    this.insideGap = insideGap;
    this.allSameWidth = allSameWidth;
    fadeOtherWindows = Settings.fadeOtherWindows;
    uiAdjustmentsBasedOnFontHeightIsSupported = false;
    this.originalText = text;
    ed = new Edit();
    if (defaultValue != null) {
      ed.setText(defaultValue);
    }
  }

  @Override
  protected void onPopup() {
    removeAll();
    String text = originalText;
    if (text.indexOf('\n') < 0 && fm.stringWidth(text) > Settings.screenWidth - 6) {
      text = Convert.insertLineBreak(Settings.screenWidth - 6, fm, text.replace('\n', ' '));
    }
    msg = new Label(text, labelAlign);
    msg.setFont(font);
    ed.setFont(font);
    btns = new PushButtonGroup(buttonCaptions, false, -1, gap, insideGap, 1, allSameWidth || uiAndroid,
        PushButtonGroup.BUTTON);
    btns.setFont(font);
    int wb = btns.getPreferredWidth();
    if (wb > Settings.screenWidth - 10) // guich@tc123_38: buttons too large? place them in a single column
    {
      btns = new PushButtonGroup(buttonCaptions, false, -1, gap, insideGap, buttonCaptions.length, true,
          PushButtonGroup.BUTTON);
      btns.setFont(font);
      wb = btns.getPreferredWidth();
    }

    int androidGap = uiAndroid ? fmH / 3 : 0;
    if (androidGap > 0 && (androidGap & 1) == 1) {
      androidGap++;
    }
    int hb = btns.getPreferredHeight() + androidGap;
    int wm = Math.min(msg.getPreferredWidth() + (uiAndroid ? fmH : 1), Settings.screenWidth - 6);
    int hm = msg.getPreferredHeight();
    if (uiAndroid) {
      hb += fmH / 2;
    }
    int we = ed.getPreferredWidth();
    int he = ed.getPreferredHeight();
    FontMetrics fm2 = titleFont.fm; // guich@220_28
    int captionH = fm2.height + 10 + titleGap;

    int h = captionH + hb + hm + he;
    int w = Convert.max(wb, wm, we, fm2.stringWidth(title != null ? title : "")) + 6; // guich@200b4_29
    w = Math.min(w, Settings.screenWidth); // guich@200b4_28: dont let the window be greater than the screen size
    setRect(CENTER, yPosition, w, h);
    add(msg);
    add(btns);
    add(ed);
    msg.setRect(4, TOP, wm, hm);
    ed.setRect(LEFT + 4, AFTER + 2, FILL - 4, he);
    if (uiAndroid) {
      btns.setRect(buttonCaptions.length > 1 ? LEFT + 3 : CENTER, AFTER + 3,
          buttonCaptions.length > 1 ? FILL - 3 : Math.max(w / 3, wb), FILL - 3);
    } else {
      btns.setRect(CENTER, AFTER + 2, wb, hb);
    }
    setBackForeColors(UIColors.inputboxBack, UIColors.inputboxFore);
    msg.setBackForeColors(backColor, foreColor); // guich@tc115_9: moved to here
    if (btns != null) {
      btns.setBackForeColors(UIColors.inputboxAction,
          Color.getBetterContrast(UIColors.inputboxAction, foreColor, backColor)); // guich@tc123_53
    }
  }

  /** Sets the alignment for the text. Must be CENTER (default), LEFT or RIGHT */
  public void setTextAlignment(int align) {
    labelAlign = align;
  }

  @Override
  public void reposition() {
    onPopup();
  }

  @Override
  protected void postPopup() {
    ed.requestFocus();
    if (openKeyboardOnPopup) {
      ed.popupKCC();
    }
    if (Settings.keyboardFocusTraversable) {
      isHighlighting = false; // allow a direct click to dismiss this dialog
    }
  }

  @Override
  protected void postUnpop() {
    if (Settings.keyboardFocusTraversable) {
      isHighlighting = true;
    }
    if (selected != -1) {
      postPressedEvent(); // guich@580_27
    }
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
            btns.setSelectedIndex(i);
            close();
            break;
          }
        }
      }
      break;
    case ControlEvent.PRESSED:
      if (e.target == btns && btns.getSelectedIndex() != -1) {
        close();
      }
      break;
    }
  }

  private void close() {
    selected = btns.getSelectedIndex();
    btns.requestFocus(); // remove focus from the edit
    btns.setSelectedIndex(-1);
    unpop();
  }

  /** Returns the pressed button index, starting from 0 */
  public int getPressedButtonIndex() {
    return selected;
  }

  /** Returns the value entered */
  public String getValue() {
    return ed.getText();
  }

  /** Sets the default value on the Edit field */
  public void setValue(String value) {
    ed.setText(value);
  }

  /** Returns the Edit so you can set its properties. */
  public Edit getEdit() // guich@310_23
  {
    return ed;
  }

  /** Sets the Edit to the given one. */
  public void setEdit(Edit ed) {
    this.ed = ed;
  }
}