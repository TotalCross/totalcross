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
import totalcross.ui.event.PenEvent;

/**
 * ComboBoxDropDown is a simple implementation of a PopUp Listbox. Used by the ComboBox class.
 */

public class ComboBoxDropDown extends Window {
  /** The assigned ListBox. */
  public ListBox lb;
  /** Set to true if want to make the control popup below or above always, and not only if WinCE */
  public boolean dontHideParent = true; // guich@200b4_205: let the user choose to not hide the parent control
  /** Set to true to make this pop have the screen height */
  public boolean fullHeight; // guich@330_52
  /** Set to true to make this popup have the screen width */
  public boolean fullWidth; // guich@550_19

  private int selected; // guich@580_27

  /** Creates a ComboBoxDropDown with coordinates that will be set later via the setRect method. */
  public ComboBoxDropDown() {
    this(new ListBox());
  }

  /** Creates a ComboBoxDropDown with the given ListBox. You can extend the ListBox to
   * draw the items by yourself and use this constructor so the ComboBoxDropDown will
   * use your class and not the default ListBox one. Note that this constructor forces
   * the ListBox.simpleBorder to true.
   */
  public ComboBoxDropDown(ListBox lb) {
    if (uiAndroid) {
      transparentBackground = lb.transparentBackground = true;
    }
    started = true; // avoid calling the initUI method
    this.lb = lb;
    lb.simpleBorder = true;
    super.add(lb);
    focusTraversable = false; // kmeehl@tc100 lb.focusTraversable = false;
  }

  /** Sets the font of the controls inside this window */
  @Override
  protected void onFontChanged() // guich@200b4_153
  {
    if (lb != null) {
      lb.setFont(font);
    }
  }

  /** Sets the absolute rect of the parent's control. The ComboBoxDropDown
   * rect will be computed based on that rectangle. */
  @Override
  public void setRect(int x, int y, int width, int height, Control relative, boolean screenChanged) {
    if (lb.size() > 0) {
      // Now calculate the position on the screen
      // set the x any y coordinate to fit to
      // the screen by the given width and height.

      if (fullWidth) {
        x = 0;
        width = Settings.screenWidth;
      } else {
        int origW = width;
        // width fit to screen - keep same size of parent control if possible, and less than the screen width
        width = Math.min(Math.max(uiFlat ? width : (width - 2), lb.getPreferredWidth()), Settings.screenWidth - 10);
        if (x + width > Settings.screenWidth) // guich@220_24: make sure the pop doesnt go beyond the screen
        {
          x += origW - width; // guich@tc115_57
          if (x + width > Settings.screenWidth || x < 0) {
            x = Settings.screenWidth - width;
          }
        }
      }

      // height fit to screen
      int ih = lb.itemCount > 0 ? lb.getItemHeight(0) : fmH;
      int prefH = lb.getPreferredHeight();
      int remainsAtBottom = Settings.screenHeight - (y + height);
      int remainsAtTop = y;
      if (prefH <= remainsAtBottom) // can fit at bottom?
      {
        if (dontHideParent) {
          y += height - 1; // put window below parent's
        }
        height = prefH;
      } else if (prefH <= remainsAtTop) // can fit at top?
      {
        if (dontHideParent) {
          y -= prefH - 1; // put window above parent's
        } else {
          y += height - prefH - 1; // put window below parent's - guich@210_5: fix popping up a PalmOS Combo placed in the LEFT,BOTTOM.
        }
        height = prefH;
      } else // cant fit on screen
      if (fullHeight/* && prefH > Settings.screenHeight*/) // guich@330_52 - guich@402_7: commented 2nd part
      {
        height = Math.min(prefH, ((int) ((Settings.screenHeight - 6) / ih)) * ih + 6);
        y = (Settings.screenHeight - height) >> 1;
      } else if (remainsAtBottom >= remainsAtTop) // more area at bottom?
      {
        if (dontHideParent) {
          y += height - 1; // put win below parent's using parent's height
        }
        height = ((int) ((remainsAtBottom - 6) / ih)) * ih + 6;
        if (!dontHideParent) {
          height += ih; // guich@220_24
        }
      } else {
        height = ((int) ((remainsAtTop - 6) / ih)) * ih + 6;
        if (!dontHideParent) // guich@220_24: hide parent? add one more row to the height
        {
          height += ih;
          y += ih;
        }
        y -= height - 1;
      }
      // End of the "fit to screen part"
    }
    super.setRect(x, y, width, height, null, screenChanged);
    lb.setRect(0, 0, width, height, null, screenChanged);
  }

  /** Close the popup list with a click outside its bounds */
  @Override
  protected boolean onClickedOutside(PenEvent event) {
    if (event.type == PenEvent.PEN_DOWN) // kmeehl@tc100: only unpop for pen_down, to make it easier to manipulate
    {
      setHighlighting(); // kmeehl@tc100: set highlighting back to allow navigation through controls again
      unpop();
    }
    return true;
  }

  @Override
  public void onEvent(Event event) {
    switch (event.type) {
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (event.target == lb && ((KeyEvent) event).key == SpecialKeys.ESCAPE) // brunos@tc120: ESCAPE means: cancel selection and close dropdown
      {
        lb.setSelectedIndex(selected); // restore original selection, since the user is canceling
        requestFocus(); // guich@tc123_15
        unpop();
      }
      break;
    //case KeyEvent.ACTION_KEY_PRESS: // kmeehl@tc100 - guich@tc113_9: commented out
    case ControlEvent.PRESSED:
      if (event.target == lb && topMost == this) {
        unpop();
      }
      break;
    }
  }

  @Override
  public int getPreferredWidth() {
    return lb.getPreferredWidth() + insets.left + insets.right;
  }

  @Override
  public int getPreferredHeight() {
    return lb.getPreferredHeight() + insets.top + insets.bottom;
  }

  @Override
  protected void onPopup() // guich@321_13
  {
    selected = lb.getSelectedIndex();
    if (lb instanceof MultiListBox) {
      ((MultiListBox) lb).cbddOnPopup();
    }
  }

  @Override
  protected void postUnpop() {
    if (lb instanceof MultiListBox) {
      ((MultiListBox) lb).cbddOnUnpop();
    } else if (selected != lb.getSelectedIndex()) {
      postPressedEvent();
    }
  }
}