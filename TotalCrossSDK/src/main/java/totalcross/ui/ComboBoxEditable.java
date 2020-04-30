// Copyright (C) 2001 Daniel Tauchke 
// Copyright (C) 2001-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.event.PressListener;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.util.Vector;

/** The ComboBoxEditable is a control usually used as an Edit that holds
 * old typed values. When the user types a word, it is automatically selected in
 * the ComboBox. Here's a sample of how to use it:
 * <pre>
   String[] items = {"Ana","Barbara","Raul","Marcelo","Eduardo","Denise","Michelle","Guilherme","Vera","Dulce","Leonardo","Andre","Gustavo","Anne","Renato","Zelia","Helio"};
   ComboBoxEditable cbe = new ComboBoxEditable(items);
   cbe.qsort();
   add(cbe, LEFT,BOTTOM-100);
 * </pre>
 */

public class ComboBoxEditable extends ComboBox implements PressListener, KeyListener // guich@tc113_3
{
  /** The edit used in this ComboBox. You can customize it if you need. */
  public Edit edit;

  private boolean autoAdd, keepSorted;

  private String oldText;

  public ComboBoxEditable() {
    this((Object[]) null);
  }

  public ComboBoxEditable(Object[] items) {
    this(new ListBox(items));
  }

  public ComboBoxEditable(ListBox userListBox) {
    this(new ComboBoxDropDown(userListBox));
  }

  public ComboBoxEditable(ComboBoxDropDown userPopList) {
    super(userPopList);
    pop.lb.setFocusLess(true);
    super.add(edit = new Edit(), true);
    edit.transparentBackground = true;
    edit.hasBorder = false;
    pop.lb.addPressListener(this);
    edit.addKeyListener(this);
    edit.addPressListener(this);
    tabOrder.removeElement(edit);
    pop.dontHideParent = true;
  }

  /** Set to true to add automatically new names that were typed in the edit.
   * @param on Flag indicating if autoAdd must be set
   * @param keepSorted If the list must be sorted after a new item is added by the autoAdd. 
   */
  public void setAutoAdd(boolean on, boolean keepSorted) {
    autoAdd = on;
    this.keepSorted = keepSorted;
  }

  @Override
  protected void onFontChanged() {
    edit.setFont(font);
    pop.lb.setFont(font);
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    super.onColorsChanged(colorsChanged);
    edit.setBackForeColors(backColor, foreColor);
  }

  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    super.onBoundsChanged(screenChanged);
    Rect r = btn.getRect();
    int yy = uiAndroid ? 2 : -2;
    int hh = fmH + Edit.prefH;
    if (r.x < width / 2) {
      edit.setRect(r.x2(), yy, width - r.x2(), hh);
    } else {
      edit.setRect(0, yy, r.x, hh);
    }
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case KeyEvent.ACTION_KEY_PRESS: // focus is not here yet
      edit.requestFocus();
      break;
    case KeyEvent.KEY_PRESS:
    case KeyEvent.SPECIAL_KEY_PRESS:
      KeyEvent ke = (KeyEvent) e;
      if (ke.key == SpecialKeys.ESCAPE) {
        unpop();
      } else if (ke.isActionKey()) {
        actionkeyPressed(null);
      } else if (e.target == edit) {
        selectFromEdit();
      }
      break;
    case ControlEvent.FOCUS_OUT:
      unpop();
      break;
    case ControlEvent.FOCUS_IN:
      if (e.target != this) {
        popup();
      }
      return;
    }
    super.onEvent(e);
  }

  @Override
  public void popup() {
    if (!pop.lb.isDisplayed()) {
      oldText = edit.getText();
      // we can't open a Window, otherwise the user will not be able
      // to write in the Edit. So, we add the ListBox to our parent.
      updatePopRect();
      parent.add(pop.lb);

      // guich@tc130: fix window position
      boolean toTop = pop.y < this.y;
      int h = pop.height;
      int ph = parent.getClientRect().height;
      if (h + this.height > ph) {
        h = ph - this.height;
      }
      pop.lb.setRect(SAME, toTop ? BEFORE : AFTER, pop.width, h, this);
    }
    if (getParentWindow().getFocus() != edit) {
      edit.requestFocus();
    }
  }

  /** Closes the open ListBox. */
  @Override
  public void unpop() {
    String newText = edit.getText();
    if (autoAdd && pop.lb.getSelectedIndex() == -1 && newText.length() > 0) {
      pop.lb.add(newText);
      if (keepSorted) {
        pop.lb.qsort(true);
      }
      pop.lb.setSelectedItem(newText);
    }
    boolean match = newText.equals(oldText);
    oldText = newText;
    if (!match) {
      postPressedEvent();
    }
    parent.remove(pop.lb);
  }

  @Override
  protected void drawSelectedItem(Graphics g) {
    // do nothing
  }

  private void setTextFromList() {
    if (getSelectedIndex() >= 0) {
      edit.setText(getSelectedItem().toString());
    }
    getParentWindow().removeFocus();
    getParentWindow().swapFocus(this);
  }

  private void selectFromEdit() {
    if (edit.getLength() == 0 || !pop.lb.setSelectedItemStartingWith(edit.getText(), true)) {
      pop.lb.setSelectedIndex(-1);
    }
  }

  @Override
  public void controlPressed(ControlEvent e) {
    if (e.target == edit) {
      selectFromEdit();
    } else {
      setTextFromList();
    }
  }

  @Override
  public void actionkeyPressed(KeyEvent e) {
    setTextFromList();
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void specialkeyPressed(KeyEvent ke) {
    if (ke.isUpKey() || ke.isDownKey()) {
      int idx = pop.lb.getSelectedIndex();
      if (Settings.circularNavigation) {
        pop.lb.setSelectedIndex(
            ke.isDownKey() ? (idx + 1) % pop.lb.itemCount : idx <= 0 ? pop.lb.itemCount - 1 : idx - 1);
      } else {
        pop.lb.setSelectedIndex(ke.isDownKey() ? Math.min((idx + 1), pop.lb.itemCount - 1) : Math.max(0, idx - 1));
      }
      ke.consumed = true;
      return;
    }
  }

  @Override
  public void getFocusableControls(Vector v) // kmeehl@tc100
  {
    if (visible && isEnabled()) {
      v.addElement(this);
    }
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // kmeehl@tc100
  {
    return this;
  }

}
