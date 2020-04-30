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
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.util.ElementNotFoundException;

/** Constructs a MenuBarDropDown with the given items.
 * This class is used in conjunction with the MenuBar. However, you can also
 * use it to create a stand alone "right click" menu.
 * Here is an example of how to build the MenuBarDropDown for the DateBook:<br>
 * <pre>
 * MenuItem col1[] = // <b>note that the first String is always skipped</b>
 * {
 *   new MenuItem("Record"),
 *   new MenuItem("NewEvent"),
 *   new MenuItem("Delete Note..."),
 *   new MenuItem("Purge..."),
 *   new MenuItem(),           // create a dotted line
 *   new MenuItem("Beam Event"),
 * };
 * MenuBarDropDown pop = new MenuBarDropDown(10,10,col1);
 * pop.popupNonBlocking();
 * // a PRESSED event is generated when a item is selected. The Window
 * // is closed when the user clicks outside or selects an item. Use the getSelectedIndex
 * // method to discover the index of the MenuItem array that was selected (-1 if none).
 * </pre>
 * <b>Note</b>: the menu items must fit on screen. No clipping is applied.
 * Also, the font and colors can be changed if desired.
 */

public class MenuBarDropDown extends Window {
  private MenuItem[] items;
  private int[] ypos;
  private int selected = -1;
  private int popX, popY;
  private PenEvent pe = new PenEvent();
  private int lineHeight;
  private int dColor, bColor, fColor, cursorColor = -1;
  private int fourColors[] = new int[4];

  /** Converts an old Menu format into the new one. */
  public static MenuItem[] strings2items(String[] strings) {
    MenuItem[] mi = new MenuItem[strings.length];
    MenuItem m;
    for (int i = mi.length - 1; i > 0; i--) {
      String caption = strings[i];
      switch (caption.charAt(0)) {
      case '*':
        m = new MenuItem(caption.substring(1));
        m.isEnabled = false;
        break; // disabled
      case '!':
        m = new MenuItem(caption.substring(1), true);
        break; // checked
      case '?':
        m = new MenuItem(caption.substring(1), false);
        break; // unchecked
      case '-':
        m = new MenuItem();
        break; // separator
      default:
        m = new MenuItem(caption);
      }
      mi[i] = m;
    }
    mi[0] = new MenuItem(strings[0]);
    return mi;
  }

  /** Constructs a MenuBarDropDown that will show at the given x,y position the given items. */
  public MenuBarDropDown(int x, int y, MenuItem[] items) {
    focusTraversable = true; //	timo@tc100b3 - must be focusTraversable for geographical focus to work
    this.items = items;
    canDrag = false;
    popX = x;
    popY = y;
    setFont(getFont().asBold()); // use a bold font.
    setBackColor(Color.WHITE);
    borderStyle = -1; // guich@220_48
    started = true; // avoid calling the initUI method

    pe.target = this;
    pe.type = PenEvent.PEN_DOWN;
  }

  private MenuBar getMenuBar() {
    Window w = (Window) zStack.items[zStack.size() - 2];
    if (w instanceof MenuBar) {
      return (MenuBar) w;
    }
    return null;
  }

  /** Change the font and recompute some parameters */
  @Override
  protected void onFontChanged() // guich@200b4_153
  {
    // all this is recalculated because the font had changed
    int w = 0;
    lineHeight = fmH + 1 + titleGap;
    int n = items.length;
    ypos = new int[n];
    // compute the maximum width
    boolean haveCheckable = false;
    for (int i = 1; i < n; i++) {
      MenuItem mi = items[i];
      if (mi.isCheckable) {
        haveCheckable = true;
      }
      w = Math.max(w, fm.stringWidth(mi.caption));
    }
    if (haveCheckable) {
      double factor = (double) fmH / 11f;
      w += (int) (factor * 8) + 1; // guich@300_1
    }
    // compute the rects
    int y = 1;
    int remH = (lineHeight - fmH) / 2;
    ypos[0] = y + remH;
    for (int i = 1; i < n; i++) {
      y += items[i].isSeparator ? 1 : lineHeight;
      ypos[i] = y + remH;
    }
    // Check if we're not after the screen limits
    w += 6;
    if (popX + w > Settings.screenWidth) {
      popX = Settings.screenWidth - w;
    }
    if (popX < 0) {
      popX = 0;
    }
    setRect(popX, popY, w, y + 2);
  }

  @Override
  public void screenResized() {
    onFontChanged();
  }

  /** Selects the given index. */
  public int setSelectedIndex(int index) {
    if (index <= -1) {
      index = selected = 1;
    } else if (index == 1000) {
      selected = -1;
    } else if (selected > 0) // && (index > 0))
    {
      selected = index;
      Window.needsPaint = true;
    } else {
      selected = index >= 1 ? index : 1;
      Window.needsPaint = true;
    }
    return selected;
  }

  public int getYPos(int index) {
    return ypos[index];
  }

  /** Returns the selected index when this window was closed or -1 if non was selected */
  public int getSelectedIndex() {
    return selected;
  }

  /** Setup some important variables */
  @Override
  protected void onPopup() {
    selected = -1;
    onFontChanged(); // a screen rotation may have enlarged the screen
    highlighted = this; //	timo@tc100b3 - if highlighted != _focus, geographical focus doesn't work
  }

  @Override
  protected void postPopup() {
    isHighlighting = Settings.keyboardFocusTraversable; // guich@573_26
  }

  @Override
  protected void postUnpop() {
    if (selected != -1) {
      postPressedEvent();
    }
  }

  /** Close the popup list with a click outside its bounds */
  @Override
  protected boolean onClickedOutside(PenEvent event) {
    Window w;
    selected = -1;
    if (event.y < this.y && ((w = (Window) zStack.items[zStack.size() - 2]) instanceof totalcross.ui.MenuBar)) // clicked on MenuBar? propagate the event to it
    {
      pe.absoluteX = event.absoluteX;
      pe.x = event.x;
      pe.absoluteY = event.absoluteY;
      pe.y = event.y;

      w._onEvent(pe);
      if (topMost == this) {
        requestFocus(); // we must get back the focus
      }
    } else if (event.x < this.x || event.y < this.y || event.x >= (this.x + this.width)
        || event.y >= (this.y + this.height)) // guich@300_39: added the last condition to fix a problem when this is used not with a menubar.
    {
      if (getTopMost() instanceof MenuBar) {
        enableUpdateScreen = false; // avoids that the screen is updated in next event tick
      }
      unpop();
    }
    return true;
  }

  @Override
  protected void loadBehind() // guich@tc120_38
  {
    try {
      if (!(zStack.peek() instanceof MenuBar)) {
        super.loadBehind();
      }
    } catch (ElementNotFoundException e) {
      MessageBox.showException(e, true);
    }
  }

  private int getItemAt(int y) {
    for (int i = 1; i < ypos.length; i++) {
      MenuItem mi = items[i];
      if (ypos[i - 1] <= y && y <= ypos[i] && mi.isEnabled && !mi.isSeparator) {
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
        selected = -1; // user canceled
        unpop();
      } else if (handleFocusChangeKeys((KeyEvent) event)) {
        break;
      }
    case KeyEvent.KEY_PRESS:
      Window w = Window.topMost;
      int idx = ((KeyEvent) event).key - '0'; // guich@tc112_27: allow select using numbers
      if (0 < idx && idx < items.length) {
        MenuItem mi = items[idx];
        if (!mi.isSeparator && mi.isEnabled) // skip invalid lines
        {
          setSelectedIndex(idx);
          unpop();
        }
      } else if (w instanceof MenuBar) {
        ((MenuBar) w).close(); // close all menu windows
      }
      break;
    case KeyEvent.ACTION_KEY_PRESS: // kmeehl@tc100
      if (selected != -1) {
        unpop();
      }
      break;
    case PenEvent.PEN_DOWN:
    case PenEvent.PEN_DRAG:
    case PenEvent.PEN_UP:
      PenEvent pe = (PenEvent) event;
      if (0 < pe.y && pe.y < height) {
        int newSelected = getItemAt(pe.y);
        if (newSelected != selected) {
          selected = newSelected;
          Window.needsPaint = true;
        }
        if (event.type == PenEvent.PEN_UP && selected != -1) {
          event.consumed = true;
          unpop();
        }
      } else if (selected != -1) // outside valid area?
      {
        Window.needsPaint = true;
        selected = -1;
      }
      break;
    }
  }

  /** Sets the cursor color. By default, it is based in the background color */
  public void setCursorColor(int c) // guich@220_49
  {
    cursorColor = c;
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    fColor = getForeColor();
    bColor = getBackColor();
    dColor = Color.interpolate(bColor, fColor);//getCursorColor(fColor);
    if (colorsChanged) {
      Graphics.compute3dColors(true, backColor, foreColor, fourColors);
      if (cursorColor == -1) {
        cursorColor = 0x0000F0;
      }
    }
  }

  @Override
  public void onPaint(Graphics g) {
    // paint border
    g.foreColor = fColor;
    g.backColor = bColor;

    // changed drawing of popup menu border to look more native
    // PMD 25Oct2001
    if (borderStyle == -1) {
      g.draw3dRect(0, 0, width, height, Graphics.R3D_SHADED, false, true, fourColors);
    }

    g.setFont(font);
    int halfTG = titleGap / 2;
    // paing captions
    for (int i = 1; i < items.length; i++) {
      if (i == selected) {
        g.backColor = cursorColor != -1 ? cursorColor : Color.getCursorColor(bColor); // guich@220_49
        g.fillRect(1, ypos[i - 1] - titleGap / 2, width - 3, lineHeight);
        g.backColor = bColor;
      }
      MenuItem mi = items[i];
      if (mi.isSeparator) {
        g.drawDots(0, ypos[i - 1] - halfTG, width - 3, ypos[i - 1] - halfTG);
      } else {
        // is the menu item disabled?
        if (!mi.isEnabled) {
          g.foreColor = dColor;
        }
        // can the menu item be checkable?
        if (mi.isChecked) {
          // draws the check
          int x = width - 9 - fmH / 11; // guich@580_8: space a bit on highres devices
          int y = ypos[i - 1] + 5;
          int my = 2;
          int mx = 7;
          int hh = 2 * fmH / 11; // guich@300_1
          if (fmH > 11) {
            x -= hh;
            y += (fmH == 14 ? hh - 1 : hh);
            my += hh >> 1;
            mx += hh;
          }
          for (int j = 0; j < mx; j++) {
            g.drawLine(x, y, x, y + hh);
            x++;
            if (j < my) {
              y++;
            } else {
              y--;
            }
          }
        }
        g.drawText(mi.caption, 3, ypos[i - 1], textShadowColor != -1, textShadowColor);
        if (!mi.isEnabled) {
          g.foreColor = fColor;
        }
      }
    }
  }

  @Override
  protected boolean handleFocusChangeKeys(KeyEvent ke) // guich@512_1: transfer focus on tab keys - fdie@550_15 : transfer also on arrow keys
  {
    if (ke.isActionKey()) {
      unpop();
    } else if (ke.isUpKey() || ke.isDownKey()) {
      // timo@tc100b3 - moved getNextSelectionIndex into its own method
      int newSelected = getNextSelectionIndex(ke);
      if (newSelected != selected && newSelected != -1) {
        setSelectedIndex(newSelected);
      }
    } else if (ke.key == SpecialKeys.LEFT || ke.key == SpecialKeys.RIGHT || ke.key == SpecialKeys.TAB) {
      MenuBar mb = getMenuBar();
      if (mb != null) {
        mb.moveBy(ke.key == SpecialKeys.LEFT ? -1 : 1);
      }
    } else {
      return false;
    }
    return true;
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // kmeehl@tc100
  {
    handleFocusChangeKeys(ke);
    return this;
  }

  /**
   * Returns the index of the next menu item that is to be selected based on the direction of the KeyEvent.
   * @param ke The key event
   * @return   The index of the next menu item based on the direction of the key event.
   */
  protected int getNextSelectionIndex(KeyEvent ke) {
    int newSelected = selected == -1 ? 0 : selected;
    boolean isDown = ke.isDownKey();
    for (int i = items.length; i > 0; i--) // to avoid infinite loop if all items are non-clickable
    {
      if (isDown) {
        if (++newSelected == items.length) {
          newSelected = Settings.circularNavigation ? 1 : newSelected - 1; // guich@tc115_19
        }
      } else {
        if (--newSelected <= 0) {
          newSelected = Settings.circularNavigation ? items.length - 1 : 1; // guich@tc115_19
        }
      }
      MenuItem mi = items[newSelected];
      if (!mi.isSeparator && mi.isEnabled) {
        return newSelected;
      }
    }
    return -1;
  }
}
