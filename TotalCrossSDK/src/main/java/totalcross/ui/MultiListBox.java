// Copyright (C) 2003-2008 Greg Ouzounian
// Copyright (C) 2008-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.util.ElementNotFoundException;
import totalcross.util.IntHashtable;
import totalcross.util.IntVector;

/** MultiListBox is a listbox that allows more than one item to be selected.
 * The maximum number of selections can be defined using setMaxSelections.
 * <br><br>
 * To create a ComboBox with a MultiListBox, use:
 * <pre>
 * MultiListBox mlb;
 * new ComboBox(mlb = new MultiListBox())
 * </pre>
 * Be sure to save a reference to the MultiListBox so you can call the specific
 * methods of this class. For instance, getSelectedIndex returns just the last selected
 * index; to retrieve all indexes, use getSelectedIndexes.
 * <br><br>
 * In penless devices, there will be a cursor which will be used to highlight an item;
 * to select or unselect it, you must press the left key.
 * <br><br>
 * MultiListBox requires the useFullWidthOnSelection on penless devices.
 * @since TotalCross 1.0.
 */

public class MultiListBox extends ListBox {
  protected static final int NO_OLD_SELECTION = -99;
  protected int maxSelections = 10000000;
  protected int oldSelection = NO_OLD_SELECTION;
  protected IntHashtable selectedIndexes = new IntHashtable(5);
  protected IntVector order;
  /** Internal use only. */
  public boolean changed;
  private boolean drawingSel, hasFocus;
  private int cursorColor, mixedCursorColor;
  /** Fill this IntVector with the values that will be selected when the clear method is called. 
   */
  public IntVector clearValues = new IntVector(1); // guich@tc112_33

  /** Set to false to don't show the number of selected elements as they are clicked. 
   * @since TotalCross 1.3
   */
  public boolean showOrderInTip = true;

  /** Set to true if you want to unselect the first element once max is reached.
   * Note that this implies that setOrderIsImportant(true) and also setMaxSelections were both called, otherwise it has no effects.
   */
  public boolean unselectFirstWhenMaxIsReached;

  /** Global suffix used to display the number of items in a ComboBox. Defaults to " items". */
  public static String itemsText = " items";

  /** Local suffix to display the number of items in a ComboBox. Defaults to " items". */
  public String localItemsText = " items";

  /** The amount of time used to show how many itens. */
  public int tipDelay = 250;

  /** Constructs an empty MultiListBox. */
  public MultiListBox() {
    useFullWidthOnSelection = true; // required
  }

  /** Constructs a MultiListBox with the given items. */
  public MultiListBox(Object[] items) {
    super(items);
    useFullWidthOnSelection = true; // required
  }

  /** Call this method if you want to keep track of the order in which the items were selected.
   * Note that this makes the listbox slower. Calling this method clears all selected items.
   */
  public void setOrderIsImportant(boolean set) {
    order = set ? new IntVector(50) : null;
    selectedIndexes.clear();
  }

  /** Returns true if you requested that the order is important. */
  public boolean isOrderImportant() {
    return order != null;
  }

  /** Returns the selected index. If more than one item is selected, returns the last one. */
  @Override
  public int getSelectedIndex() {
    try {
      return /*Settings.keyboardFocusTraversable ? selectedIndex : */order != null ? order.peek()
          : selectedIndexes.size() > 0 ? selectedIndexes.getKey(0) : -1; // guich@tc110_100: fixed problem in penless mode: a highlighted item was shown in the combobox instead of the selected item
    } catch (ElementNotFoundException e) {
      return -1;
    }
  }

  /** Returns the last selected item if you had set <i>order is important</i>, otherwise returns null. */
  public Object getLastSelectedItem() {
    if (order == null) {
      return null;
    }
    int n = selectedIndexes.size();
    if (n == 0) {
      return null;
    }
    try {
      return items.items[order.peek()];
    } catch (ElementNotFoundException e) {
      return null;
    }
  }

  /** Defines the maximum number of items that can be selected.
   * If currently there are more selected than the allowed, all
   * selections are cleared.
   */
  public void setMaxSelections(int max) {
    this.maxSelections = max;
    if (selectedIndexes.size() > max) {
      selectedIndexes.clear();
      if (order != null) {
        order.removeAllElements();
      }
    }
  }

  /**
   * Return a vector with the indexes which have been selected.  The elements of the Vector are the
   * indexes in the Vector of the items.  So if this Vector is holding
   * [3, 12, 5] it means that the items 3, 5 and 12 have been selected.
   * If order is important, then the order intvector is returned (caution: do not change the returned array!).
   * Note that the indexes are not in order; to order them, call <code>qsort</code>.
   */
  public IntVector getSelectedIndexes() {
    return order != null ? order : selectedIndexes.getKeys();
  }

  /** Returns if given index is selected. */
  public boolean isSelected(int index) {
    return selectedIndexes.exists(index);
  }

  @Override
  public void removeAll() {
    selectedIndexes.clear();
    if (order != null) {
      order.removeAllElements();
    }
    super.removeAll();
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    super.onColorsChanged(colorsChanged);
    if (colorsChanged) {
      cursorColor = Color.brighter(back1, 48);
      if (cursorColor == Color.WHITE) {
        cursorColor = Color.BRIGHT;
      }
      mixedCursorColor = Color.interpolate(back1, cursorColor);
    }
  }

  /** Draw all selected indexes */
  @Override
  protected void drawSelectedItem(Graphics g, int from, int to) {
    drawingSel = true;
    for (int i = from; i < to; i++) {
      if (selectedIndexes.exists(i)) {
        drawCursor(g, i, true);
      }
    }
    drawingSel = false;
    if (Settings.keyboardFocusTraversable) {
      super.drawSelectedItem(g, from, to);
    }
  }

  @Override
  protected void drawItems(Graphics g, int dx, int dy, int greatestVisibleItemIndex) {
    for (int i = offset; i < greatestVisibleItemIndex; dy += getItemHeight(i++)) {
      if (!selectedIndexes.exists(i)) {
        drawItem(g, i, dx, dy); // guich@200b4: let the user extend ListBox and draw the items himself
      }
    }
    drawSelectedItem(g, offset, greatestVisibleItemIndex);
  }

  @Override
  protected int getCursorColor(int index) {
    boolean exists = selectedIndexes.exists(index);
    if (Settings.keyboardFocusTraversable && !drawingSel) {
      if (!hasFocus) {
        if (!exists) {
          return back0;
        }
      } else if (index == selectedIndex && exists) {
        return mixedCursorColor;
      }
    } else if (!exists) {
      return back1;
    }
    return cursorColor;
  }

  /** In this MultiListBox, inverts the status of the given index, or clears all if i is -1.
   * @see #setSelectedIndex(int, boolean)
   */
  @Override
  public void setSelectedIndex(int index) {
    if (!Settings.keyboardFocusTraversable) {
      handleClick(index);
    }
    if (index == -1) {
      oldSelection = NO_OLD_SELECTION; // no old selection after unselecting everything
      selectedIndexes.clear();
      if (order != null) {
        order.removeAllElements();
      }
    }
    super.setSelectedIndex(index, false);
  }

  @Override
  protected void leftReached() {
    handleClick(selectedIndex);
  }

  /** Sets or clear an index. Can also be used to set or clear all indexes, passing -1 as the index.
   * Both operations are limited by the defined max selections.
   */
  @Override
  public void setSelectedIndex(int index, boolean set) {
    if (0 <= index && index < itemCount) {
      if (set != isSelected(index)) {
        setSelectedIndex(index);
      }
    } else // clear or set all
    if (index < 0) {
      if (!set) {
        setSelectedIndex(-1);
      } else {
        selectedIndexes.clear();
        if (order != null) {
          order.removeAllElements();
        }
        if (unselectFirstWhenMaxIsReached && order != null && selectedIndexes.size() == maxSelections) {
          setSelectedIndex(order.items[0], false);
        }
        int n = Math.min(itemCount, maxSelections);
        for (int i = 0; i < n; i++) {
          selectedIndexes.put(i, selectedIndexes.size());
          if (order != null) {
            order.addElement(i);
          }
        }

      }
      oldSelection = NO_OLD_SELECTION;
    }
    Window.needsPaint = true;
  }

  @Override
  protected void handleSelection(int newSelection) {
    if (newSelection != oldSelection && newSelection < itemCount) // then they dragged outside the current selection
    {
      handleClick(newSelection);
      oldSelection = selectedIndex = newSelection;
      drawCursor(getGraphics(), newSelection, selectedIndexes.exists(newSelection));
    }
  }

  /** Called by ComboBoxDropDown when its being popped. */
  protected void cbddOnPopup() {
    changed = false;
  }

  /** Called by ComboBoxDropDown when its being unpopped. Sends a PRESSED event if a change was made in the selected indexes. */
  protected void cbddOnUnpop() {
    if ((Settings.geographicalFocus || !Settings.keyboardFocusTraversable) && changed) {
      super.postPressedEvent();
    }
  }

  @Override
  public void postPressedEvent() {
    if (Settings.keyboardFocusTraversable || !(parent instanceof ComboBoxDropDown)) {
      super.postPressedEvent();
    }
  }

  @Override
  protected void endSelection() {
    oldSelection = NO_OLD_SELECTION; // reset this for next pen down/drag
  }

  protected void handleClick(int index) {
    if (index >= 0) {
      if (!selectedIndexes.exists(index)) // not yet selected?
      {
        if (unselectFirstWhenMaxIsReached && order != null && selectedIndexes.size() == maxSelections) {
          setSelectedIndex(order.items[0], false);
        }
        if (selectedIndexes.size() < maxSelections) {
          selectedIndexes.put(index, 0);
          if (order != null) {
            order.addElement(index);
            if (showOrderInTip) {
              showTip(this, Convert.toString(order.size()), tipDelay, getIndexY(index));
            }
          }
          changed = true;
        }
      } else {
        try {
          selectedIndexes.remove(index);
          if (order != null) {
            order.removeElement(index);
          }
          changed = true;
        } catch (ElementNotFoundException e) {
        }
      }
    } else {
      selectedIndexes.clear(); // reset everything
      if (order != null) {
        order.removeAllElements();
      }
      oldSelection = NO_OLD_SELECTION; // reset this for next pen down/drag
    }
    Window.needsPaint = true;
  }

  /** Returns the String with the selected item (if single) or a string with the number of selected items.
   * You can change the suffix itemsText or localItemsText to another one.
   * If order is important, returns the last selected item.
   */
  @Override
  public String getText() {
    int size = selectedIndexes.size();
    if (size <= 1) {
      return super.getText();
    }
    if (order != null) {
      return getLastSelectedItem().toString();
    }
    return Convert.toString(size) + (localItemsText != null ? localItemsText : itemsText);
  }

  @Override
  public void onEvent(Event e) {
    if (e.target == this) {
      if (e instanceof PenEvent && Settings.keyboardFocusTraversable) // if in kft and the user clicked in the control, handle the event as if we were not in kft
      {
        Settings.keyboardFocusTraversable = false;
        super.onEvent(e);
        Settings.keyboardFocusTraversable = true;
        return;
      }
      switch (e.type) {
      case KeyEvent.KEY_PRESS:
        if (((KeyEvent) e).key == ' ') {
          setSelectedIndex(-1, selectedIndexes.size() <= 1);
          e.consumed = true;
        } // no break here!
      case ControlEvent.FOCUS_IN:
      case KeyEvent.SPECIAL_KEY_PRESS:
        if (!hasFocus) {
          changed = false;
          hasFocus = true;
        }
        break;
      case ControlEvent.HIGHLIGHT_OUT:
        hasFocus = false;
        Window.needsPaint = true;
        break;
      }
    }
    super.onEvent(e);
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) {
    if ((ke.isPrevKey() && !ke.isUpKey()) || (ke.isNextKey() && !ke.isDownKey())) {
      if (!hasFocus) {
        changed = false;
      }
      hasFocus = true;
      int oldXOffset = xOffset;
      _onEvent(ke);
      if (oldXOffset != xOffset || ke.isPrevKey()) {
        return this;
      }
      hasFocus = false;
      Window.needsPaint = true;
      cbddOnUnpop(); // return the event
      return null;
    }
    if ((ke.isUpKey() && selectedIndex <= 0) || (ke.isDownKey() && selectedIndex == itemCount - 1)) {
      return null;
    }
    _onEvent(ke);
    return this;
  }

  @Override
  public void clear() {
    if (clearValues.isEmpty()) {
      super.clear();
    } else {
      setSelectedIndex(-1, false);
      for (int i = clearValues.size(); --i >= 0;) {
        setSelectedIndex(clearValues.items[i], true);
      }
    }
  }
}
