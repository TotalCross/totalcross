// Copyright (C) 2000-2013 SuperWaba Ltda.
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
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

/** Constructs a Menu with the given items. 
 * A menu can be opened by the user in a couple of ways:
 * <ul>
 * <li> By clicking in the Menu button
 * <li> By clicking in the title of a Window
 * </ul>
 * The Menu supports disabled and checked items.
 * Here is an example of how to build the menu:<br>
 * <pre>
 * MenuItem miBeamEvent,miNewEvent,miDeleteEvent;
 * MenuItem col0[] =
 * {
 *   new MenuItem("Record"),                               // caption for the MenuBar
 *   miNewEvent = new MenuItem("NewEvent",false),          // checked item, starting unchecked
 *   miDeleteEvent = new MenuItem("Delete Event...",true), // checked item, starting checked
 *   new MenuItem("Attach Note"),
 *   new MenuItem("Delete Note..."),
 *   new MenuItem("Purge..."),
 *   beamEvent = new MenuItem("Beam Event"),
 * };
 * MenuItem col1[] =
 * {
 *   new MenuItem("Edit"),           // caption for the MenuBar
 *   new MenuItem("Undo"),
 *   new MenuItem("Cut"),
 *   new MenuItem("Copy"),
 *   new MenuItem("Paste"),
 *   new MenuItem("Select All"),
 *   new MenuItem(),                 // a separator
 *   new MenuItem("Keyboard"),
 *   new MenuItem("Graffiti Help"),
 * };
 * MenuItem col2[] =
 * {
 *   new MenuItem("Options"),        // caption for the MenuBar
 *   new MenuItem("Font..."),
 *   new MenuItem("Preferences..."),
 *   new MenuItem("Display Options..."),
 *   new MenuItem("Phone Lookup"),
 *   new MenuItem("About Date Book"),
 * };
 * setMenuBar(new MenuBar(new MenuItem[][]{col0,col1,col2}));
 * beamEvent.isEnabled = false;
 * ...
 * // at later time, disable the "new event" and enable the "delete event"
 * miNewEvent.isChecked = true;
 * miDeleteEvent.isChecked = false;
 * </pre>
 * The menu can be closed by a click on a valid item or clicking outside of its bounds.
 * A PRESSED event will be thrown when the menu is closed and a menu item was selected.
 * To discover which item was selected, see method getSelectedIndex, which returns -1 if none,
 * or the matrix index otherwise.
 * <p>
 * Note that the separator dotted line doesn't generate events and can't be selected.
 * <p>
 * To convert the old menu form (using a string matrix) into the new form using a MenuItem matrix,
 * you can use this idea:
 * <br>Old format:
 * <pre>
 * // the string arrays menuArq, menuUtil and menuSobre are initialized somewhere else
 * setMenuBar(mbar = new MenuBar(new String[][]{menuArq,menuUtil,menuSobre}));
 * mbar.setChecked(106,true);
 * mbar.setEnabled(106,false);
 * mbar.setChecked(4, true);
 * </pre>
 * New format:
 * <pre>
 * // declare these as global variables:
 * MenuItem mi4, mi106;
 *
 * // at initUI
 * setMenuBar(mbar = new MenuBar(MenuBar.strings2items(new String[][]{menuArq,menuUtil,menuSobre})));
 * MenuItem[][] mis = mbar.getMenuItems();
 * mi4 = mis[0][4];
 * mi106 = mis[1][6];
 * mi106.isChecked = true;
 * mi106.isEnabled = false;
 * mi4.isChecked = true;
 * </pre>
 * Note that, after changing the isChecked and isEnabled states, there's no need to call repaint,
 * bacause they will show up only the next time the menu bar opens.
 */

public class MenuBar extends Window {
  protected MenuItem[][] items;
  private int[] xpos;
  private int xmin, xmax;
  /** Note: if you want to change the spacement between the menu items (maybe to make more items fit in the row), change this gap value. Default is 3 (3 pixels at left and 3 at right). */
  public int gap = 3; // guich@200b4_89
  private int selected = 0;
  private int menuItemSelected;
  private boolean switching;
  private MenuBarDropDown pop;
  private int eColor, dColor, bColor, cursorColor = -1;
  private int fourColors[] = new int[4];
  private int popFore = -1, popBack = -1, popCursor = -1;

  /** Converts a String matrix into a MenuItem matrix. */
  public static MenuItem[][] strings2items(String[][] items) {
    MenuItem[][] its = new MenuItem[items.length][];
    for (int i = 0; i < its.length; i++) {
      its[i] = MenuBarDropDown.strings2items(items[i]);
    }
    return its;
  }

  /** Create a MenuBar with the given menu items. */
  public MenuBar(MenuItem[][] items) {
    started = true; // avoid calling the initUI method
    canDrag = false;
    setBackColor(Color.WHITE);
    borderStyle = -1; // guich@220_48

    this.items = items;
    setFont(getFont().asBold()); // use a bold font.
  }

  /** Returns the matrix of Menuitems passed in the constructor. */
  public MenuItem[][] getMenuItems() {
    return this.items;
  }

  /** Returns the MenuItem at the given index. E.g.:
   * <pre>
   * MenuItem file = mbar.getMenuItem(102);
   * </pre>
   */
  public MenuItem getMenuItem(int index) {
    return items[index / 100][index % 100];
  }

  /** Set the colors for the popup windows. You can pass <code>-1</code>
   * to any parameter to keep the current settings.
   */
  public void setPopColors(int back, int fore, int cursor) {
    this.popFore = fore;
    this.popBack = back;
    this.popCursor = cursor;
  }

  /** Sets the cursor color. By default, it is based in the background color */
  public void setCursorColor(int c) // guich@220_49
  {
    cursorColor = c;
  }

  /** Called by the Window class to popup this MenuBar */
  @Override
  public void setVisible(boolean b) {
    if (b) {
      setRect(0, 0, FILL, fmH + 4 + titleGap); // update the bounds, because the screen may have been changed
      popupNonBlocking();
    }
  }

  /** Change the font and recompute some parameters */
  @Override
  protected void onFontChanged() // guich@200b4_153
  {
    // all this is recalculated because the font had changed
    int n = items.length;
    xpos = new int[n + 1];
    xpos[0] = xmax = xmin = 4;
    int temp = (gap << 1) - 1;
    for (int i = 0; i < n; i++) {
      xmax += fm.stringWidth(items[i][0].caption) + temp;
      xpos[i + 1] = xmax;
    }
    xmax--;
  }

  /** Returns the current menu item selected.
   * @return -1 if no item was selected, or a number with format <i>xyy</i>, where x is the index of the item
   * selected at the MenuBar and yy is the index+1 ( = the index of the String array that defines the items)
   * of the item selected in the MenuBarDropDown.
   * EG: suppose that the col1/"Cut" of the
   * example at the top of this page was clicked, then 102 is returned.
   */
  public int getSelectedIndex() {
    return menuItemSelected;
  }

  /** Setup some important variables */
  @Override
  protected void onPopup() {
    menuItemSelected = -1;
    enableUpdateScreen = false; // avoid flicker
  }

  /** Close the popup list with a click outside its bounds */
  @Override
  protected boolean onClickedOutside(PenEvent event) {
    close();
    return true;
  }

  void close() {
    if (pop != null) {
      pop.unpop();
    }
    unpop();
  }

  private int getItemAt(int x) {
    int n = xpos.length - 1;
    for (int i = 0; i < n; i++) {
      if (xpos[i] <= x && x < xpos[i + 1] && items[i][0].isEnabled) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public void onEvent(Event event) {
    switch (event.type) {
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (((KeyEvent) event).key == SpecialKeys.MENU) {
        close();
      }
      break;
    case ControlEvent.WINDOW_CLOSED:
      if (!switching && event.target == pop) {
        int row = pop.getSelectedIndex();
        pop = null;
        if (row != -1) // closed bc a item was clicked?
        {
          MenuItem mi = items[selected][row];
          menuItemSelected = selected * 100 + row;
          if (mi.isCheckable) {
            mi.isChecked = !mi.isChecked;
          }
          close();
        } else {
          menuItemSelected = -1;
        }
      }
      break;
    case PenEvent.PEN_DOWN:
    case PenEvent.PEN_DRAG:
      PenEvent pe = (PenEvent) event;
      if (xmin <= pe.x && pe.x <= xmax) {
        int newSelected = getItemAt(pe.x);
        if (newSelected != selected) {
          switchTo(newSelected);
        }
      } else if (selected != -1) // outside valid area?
      {
        selected = -1;
        switchTo(selected);
      }
      break;
    }
  }

  @Override
  protected void postUnpop() {
    if (menuItemSelected != -1) {
      postPressedEvent();
    }
  }

  @Override
  protected void postPopup() {
    if (selected != -1) {
      switchTo(selected);
    }
  }

  protected void switchTo(int index) {
    enableUpdateScreen = false; // avoid flicker
    selected = index;

    switching = true;
    if (pop != null) {
      pop.unpop();
      Window.repaintActiveWindows();
    }

    if (index != -1) {
      pop = new MenuBarDropDown(xpos[index], height - 2, items[index]);
      pop.titleGap = this.titleGap; // propagate to the drop down since user may change our's
      pop.setTextShadowColor(textShadowColor);
      pop.setFont(this.font); // guich@350_8: added a fix when the user changes the font
      if (borderStyle == NO_BORDER) {
        pop.setBorderStyle(NO_BORDER);
      }
      pop.setBackForeColors(popBack != -1 ? popBack : backColor, popFore != -1 ? popFore : foreColor);
      if (popCursor != -1) {
        pop.setCursorColor(popCursor); // guich@220_49
      }
      pop.popupNonBlocking();
    } else {
      pop = null;
    }
    enableUpdateScreen = true;
    switching = false;
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    eColor = getForeColor();
    dColor = Color.getCursorColor(eColor);
    bColor = getBackColor();
    if (colorsChanged) {
      Graphics.compute3dColors(true, backColor, foreColor, fourColors);
      if (cursorColor == -1) {
        cursorColor = 0x0000E0;
      }
    }
  }

  @Override
  public void onPaint(Graphics g) {
    // changed drawing of menu border to look more native
    // PMD 25Oct2001
    g.foreColor = eColor;
    g.backColor = bColor;
    switch (borderStyle) {
    case -1:
      g.draw3dRect(0, 0, width, height, Graphics.R3D_SHADED, false, false, fourColors); // guich@220_48
      break;
    case RECT_BORDER:
      g.drawRect(0, 0, width, height); // guich@402_60
      break;
    case BORDER_NONE:
      if (uiVista) {
        g.fillVistaRect(0, 0, width, height, backColor, false, false);
      }
      break;
    }
    // paint border
    g.foreColor = eColor;
    g.setFont(font);
    if (selected != -1) {
      if (!uiAndroid && uiVista && borderStyle == BORDER_NONE) {
        g.backColor = popCursor != -1 ? popCursor : popBack;
        g.fillRect(xpos[selected], 1, xpos[selected + 1] - xpos[selected], height - 2);
      } else {
        g.backColor = cursorColor != -1 ? cursorColor : Color.getCursorColor(bColor); // guich@220_49
        g.fillRect(xpos[selected], 1, xpos[selected + 1] - xpos[selected], height - 2);
      }
    }
    // paint captions
    int yy = (height - fmH) / 2;
    for (int i = 0; i < items.length; i++) {
      MenuItem mi = items[i][0];
      if (mi.isEnabled) {
        g.drawText(mi.caption, xpos[i] + gap, yy, textShadowColor != -1, textShadowColor);
      } else {
        g.foreColor = dColor;
        g.drawText(mi.caption, xpos[i] + gap, yy, textShadowColor != -1, textShadowColor);
        g.foreColor = eColor;
      }
    }
  }

  /** Moves to the MenuItem array at left or right depending on the passed value (-1 or +1). */
  public void moveBy(int i) {
    int newSelected;
    if (i > 0) {
      newSelected = Settings.circularNavigation ? (selected + 1) % items.length
          : Math.min(selected + 1, items.length - 1);
    } else {
      newSelected = Settings.circularNavigation ? (selected <= 0 ? items.length - 1 : selected - 1)
          : Math.max(selected - 1, 0);
    }
    if (selected != newSelected) {
      switchTo(newSelected);
    }
  }

  @Override
  protected boolean handleFocusChangeKeys(KeyEvent ke) // guich@512_1: transfer focus on tab keys - fdie@550_15 : transfer also on arrow keys
  {
    if (ke.isActionKey() || ke.isUpKey() || ke.isDownKey()) {
      close();
    } else if (ke.key == SpecialKeys.LEFT) {
      moveBy(-1);
    } else if (ke.key == SpecialKeys.RIGHT || ke.key == SpecialKeys.TAB) {
      moveBy(1);
    } else {
      return false;
    }
    return true;
  }

  /** Sets the style of this menubar to an alternative style (no borders, rectangular appearance).
   * Here's a sample, taken from the UIGadgets sample:
   * <pre>
   * mbar.setAlternativeStyle(Color.BLUE,Color.WHITE);
   * </pre>
   * The colors are set using a combination of the given back and fore colors. You can have a more
   * flexible color selection by using the code below, instead of calling this method. Under most
   * situations, tho, this method is enough.
   * <pre>
          mbar.setBackForeColors(Color.BLUE, Color.WHITE);
          mbar.setCursorColor(0x6464FF);
          mbar.setBorderStyle(NO_BORDER);
          mbar.setPopColors(0x0078FF, Color.CYAN, -1); // use the default cursor color for the popup menu (last -1 param)
   * </pre>
   * @since TotalCross 1.0 beta 4
   */
  public void setAlternativeStyle(int back, int fore) {
    setBackForeColors(back, fore);
    int c1, c2;
    setCursorColor(Color.interpolate(back, fore));
    setBorderStyle(NO_BORDER);
    setPopColors(c1 = Color.brighter(back, 32), c2 = Color.darker(fore, 32), Color.interpolate(c1, c2)); // use the default cursor color for the popup menu (last null param)
  }
}