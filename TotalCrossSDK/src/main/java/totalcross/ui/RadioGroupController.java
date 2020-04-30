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
import totalcross.util.Vector;

/**
 * RadioGroupController is a Radio control grouper.
 * It handles the click on a radio, unchecking the last selected one.
 * <p>Use it as:
 * <pre>
 * RadioGroupController rg = new RadioGroupController();
 * Radio r1 = new Radio("radio 1",rg);
 * Radio r2 = new Radio("radio 2",rg);
 * Radio r3 = new Radio("radio 3",rg);
 * rg.setSelectedItem(r1); // activate r1 only
 * </pre>
 * Note: there is no need (and you can't) add a RadioGroupController to a container,
 * since a RadioGroupController is a class that doesn't extend Control.
 * <p>
 */

public class RadioGroupController {
  private Radio last; // guich@402_21
  private Vector members = new Vector();

  /** Set to false to disable sending PRESSED events to the previous control. */
  public boolean sendPressOnLast = Settings.sendPressEventOnChange;

  /** Adds a new Radio to the list of Radios this controller handles. 
   * This method is called by the Radio's constructor.
   */
  public void add(Radio newMember) {
    members.addElement(newMember);
    newMember.radioGroup = this;
  }

  /** Removes the given Radio from the list of Radios this controller handles. 
   * You must explicitly call this method, if needed. 
   */
  public void remove(Radio oldMember) {
    if (members.removeElement(oldMember)) {
      oldMember.radioGroup = null;
    }
  }

  /** Called by the Radio when a click was made */
  public void setSelectedItem(Radio who) {
    if (last != null) {
      last.setChecked(false, sendPressOnLast);
    }
    (last = who).setChecked(true);
  }

  /** Selects a radio whose text matches the given caption */
  public void setSelectedItem(String text) {
    if (last != null) {
      last.setChecked(false, sendPressOnLast);
    }
    for (int i = 0, n = members.size(); i < n; i++) {
      Radio r = (Radio) members.items[i];
      if (r.getText().equals(text)) {
        (last = r).setChecked(true);
        break;
      }
    }
  }

  /** Selects a radio whose text starts with the given caption */
  public void setSelectedItemStartingWith(String text, boolean caseInsensitive) {
    if (caseInsensitive) {
      text = text.toLowerCase();
    }
    if (last != null) {
      last.setChecked(false, sendPressOnLast);
    }
    for (int i = 0, n = members.size(); i < n; i++) {
      Radio r = (Radio) members.items[i];
      String s = r.getText();
      if (caseInsensitive) {
        s = s.toLowerCase();
      }
      if (s.startsWith(text)) {
        (last = r).setChecked(true);
        break;
      }
    }
  }

  protected void setSelectedItem(Radio who, boolean checked) // guich@402_21
  {
    if (checked) {
      last = who;
    } else if (last == who) {
      last = null;
    }
  }

  /** Returns the currently selected index (in the order that the Radios were added to the container),
   * or -1 if none.
   * @since SuperWaba 4.02
   */
  public int getSelectedIndex() // guich@402_21
  {
    return last == null ? -1 : members.indexOf(last);
  }

  /** Returns the currently selected Radio,
   * or null if none.
   * @since SuperWaba 4.02
   */
  public Radio getSelectedItem() // guich@402_21
  {
    return last;
  }

  /** Selects the given radio and deselects the other one.
   * @param i the zero-based index of the radio to be set, or -1 to disable all.
   */
  public void setSelectedIndex(int i) // guich@421_32
  {
    setSelectedIndex(i, Settings.sendPressEventOnChange);
  }

  /** Selects the given radio and deselects the other one.
   * @param i the zero-based index of the radio to be set, or -1 to disable all.
   */
  public void setSelectedIndex(int i, boolean sendPressEvent) // guich@421_32
  {
    if (last != null) {
      last.setChecked(false, sendPressEvent);
    }
    if (i < 0) {
      last = null;
    } else if (i < members.size()) {
      last = (Radio) members.items[i];
      last.setChecked(true, sendPressEvent);
    }
  }

  /** Returns the Radio at the given index.
   * @since TotalCross 1.5 
   */
  public Radio getRadio(int idx) {
    return (Radio) members.items[idx];
  }

  /** Returns the number of Radio's. */
  public int getSize() {
    return members.size();
  }
}