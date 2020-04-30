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
package totalcross.db.sqlite.ui;

import totalcross.ui.Control;
import totalcross.ui.ListBox;
import totalcross.ui.gfx.Graphics;

/**
 * DBListBox is a simple implementation of a Listbox.
 * You can use the up/down keys to scroll and enter the first
 * letter of an item to select it.
 * <p>
 * Note: the color used in the setBackground method will be used in the scrollbar
 * only. The background color of the control will be a lighter version of the
 * given color.
 * <p>
 * Note: this is a special version for the LitebaseConnection. It accepts
 * a String matrix as the input, and does not allow add/remove/set of elements,
 * since it reflects the database and must be filled again after some row is
 * updated.
 * <p>
 * Here is an example showing how it can be used:
 *
 * <pre>
 * import litebase.ui.*;
 * import litebase.*;
 *
 * public class MyProgram extends totalcross.ui.MainWindow
 * {
 * DBListBox lb;
 *
 * public void initUI()
 * {
 *   ResultSet rs = driver.executeQuery("select rowid,name from person where name > 20");
 *   rs.first();
 *   lb = new DBListBox(rs.getStrings(-1,true,false),1); // display column 1
 *   add(lb,LEFT,TOP);
 * }
 *
 * public void onEvent(totalcross.ui.event.Event event)
 * {
 *    switch (event.type)
 *    {
 *       case totalcross.ui.event.ControlEvent.PRESSED:
 *          if (event.target == lb)
 *             String[] element = (String[])lb.getSelectedItem(); // in most cases, this is just a String and may be casted to such
 *    }
 * }
 * }
 * </pre>
 * The first item has index 0.
 */

public class DBListBox extends ListBox {
  /** The String items */
  protected String[][] sitems;

  /** Creates an empty Listbox. */
  public DBListBox() {
    this(null, -1);
  }

  /** Creates a Listbox with the given items. */
  public DBListBox(String[][] items, int displayCol) {
    super((Object[]) null);
    if (items != null) {
      sitems = items;
      itemCount = items.length;
    }
    this.dataCol = displayCol;
  }

  /** Sets the column of the String matrix that will be used to display the
   * elements.
   */
  public void setDisplayCol(int displayCol) {
    this.dataCol = displayCol;
  }

  /** Returns the number of columns, if there are items.
   */
  public int getColumnCount() {
    return sitems == null || sitems.length == 0 ? 1 : sitems[0].length;
  }

  /** <b>REPLACES</b> the current items with the given ones. */
  @Override
  public void add(Object[] items) {
    if (items instanceof String[][]) {
      sitems = (String[][]) items;
      itemCount = sitems.length;
      sbar.setEnabled(isEnabled() && visibleItems < itemCount);
      sbar.setMaximum(itemCount); // guich@210_12: forgot this line!
    }
  }

  /** <b>REPLACES</b> the current items with the given ones. */
  @Override
  public void add(Object item) {
    if (item instanceof String[][]) {
      add((Object[]) item);
    }
  }

  /** Does nothing */
  @Override
  public void insert(Object item, int index) {
  }

  /** Empties this ListBox, setting all elements of the array to <code>null</code>
       so they can be garbage collected.
       <b>Attention!</b> If you used the same object array
       to initialize two ListBoxes (or ComboBoxes), this method will null both ListBoxes
       ('cos they use the same array reference),
       and you'll get a null pointer exception!
   */
  @Override
  public void removeAll() // guich@210_13
  {
    sitems = null;
    sbar.setMaximum(0);
    itemCount = 0;
    offset = 0; // wolfgang@330_23
    repaint();
  }

  /** Does nothing */
  @Override
  public void remove(int itemIndex) // guich@200final_12: new method
  {
  }

  /** Does nothing */
  @Override
  public void remove(Object item) {
  }

  /** Does nothing */
  @Override
  public void setItemAt(int i, Object s) {
  }

  /** Get the Object at the given Index. Returns a String array, or null if i is out of range. */
  @Override
  public Object getItemAt(int i) {
    if (0 <= i && i < itemCount) {
      return sitems[i];
    }
    return null;
  }

  /** Returns the selected item of the Listbox or null if none is selected */
  @Override
  public Object getSelectedItem() {
    return selectedIndex >= 0 ? sitems[selectedIndex] : null;
  }

  /** Returns the position of the selected item of the Listbox or -1 if the listbox has no selected index yet. */
  @Override
  public int getSelectedIndex() {
    return selectedIndex;
  }

  /** Returns all items in this ListBox. The array can be casted to String[][].
   */
  @Override
  public Object[] getItems() {
    return sitems;
  }

  @Override
  protected Object[] getItemsArray() {
    return sitems;
  }

  /** Returns the index of the object at the given column. */
  public int indexOf(Object name, int col) {
    for (int i = 0; i < sitems.length; i++) {
      if (sitems[i][col].equals(name)) {
        return i;
      }
    }
    return -1;
  }

  /** Select an item and scroll to it if necessary. Note: select must be called only after the control has been added to the container and its rect has been set. */
  @Override
  public void setSelectedIndex(int i) {
    if (0 <= i && i < itemCount && i != selectedIndex/* && height != 0*/) // flsobral@220_14: commented height!=0 otherwise combobox are not properly set. (same problem observed with listbox)
    {
      offset = i;
      int vi = sbar.getVisibleItems();
      int ma = sbar.getMaximum();
      if (offset + vi > ma) {
        offset = Math.max(ma - vi, 0); // guich@220_4: fixed bug when the listbox is greater than the current item count
      }

      selectedIndex = i;
      sbar.setValue(offset); // guich@210_9: fixed scrollbar update when selecting items
      repaint();
    } else if (i == -1) // guich@200b4_191: unselect all items
    {
      offset = 0;
      sbar.setValue(0);
      selectedIndex = -1;
      repaint();
    }
  }

  /** Returns the number of items */
  @Override
  public int size() {
    return itemCount;
  }

  /** Does nothing */
  @Override
  public void add(Control control) {
  }

  /** Does nothing */
  @Override
  public void remove(Control control) {
  }

  /** Does nothing */
  @Override
  protected void find(char c) {
  }

  /** You can extend ListBox and overide this method to draw the items */
  @Override
  protected void drawItem(Graphics g, int index, int dx, int dy) {
    //Vm. debug(this+" index: "+index+", items.size: "+items.size()+", dx,dy = "+dx+","+dy);
    if (0 <= index && index < itemCount) {
      g.drawText(sitems[index][dataCol], dx, dy);
    }
  }

  /** Returns the width of the given item index with the current fontmetrics. Note: if you overide this class you must implement this method. */
  @Override
  protected int getItemWidth(int index) {
    if (sitems == null) {
      return 0;
    }
    return fm.stringWidth(sitems[index][dataCol]);
  }

  private void qsort(int first, int last) // guich@220_34
  {
    String[][] sitems = this.sitems;
    int low = first;
    int high = last;
    if (first >= last) {
      return;
    }
    String mid = sitems[(first + last) >> 1][dataCol];
    while (true) {
      while (high >= low && mid.compareTo(sitems[low][dataCol]) > 0) {
        low++;
      }
      while (high >= low && mid.compareTo(sitems[high][dataCol]) < 0) {
        high--;
      }
      if (low <= high) {
        String[] temp = sitems[low];
        sitems[low++] = sitems[high];
        sitems[high--] = temp;
      } else {
        break;
      }
    }
    if (first < high) {
      qsort(first, high);
    }
    if (low < last) {
      qsort(low, last);
    }
  }

  /** Sorts the elements of this ListBox. The current selection is cleared. */
  @Override
  public void qsort() {
    qsort(0, itemCount - 1);
    setSelectedIndex(-1);
  }

  @Override
  public String getText() {
    return selectedIndex < 0 ? "" : sitems[selectedIndex][dataCol];
  }

}