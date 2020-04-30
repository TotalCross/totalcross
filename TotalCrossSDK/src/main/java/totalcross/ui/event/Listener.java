// Copyright (C) 2009-2013 SuperWaba Ltda. 
// Copyright (C) 2013-2020 TotalCross Global Mobile Platform Ltda.   
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** A class that represents an event Listener. Holds the type of the event and the listener itself.
 * @since Totalcross 1.22
 */
public class Listener implements EventHandler // guich@tc122_11: have to distinguish the type of event of a listener, NOT only based on the instanceof operator.
{
  public static final int PEN = 1;
  public static final int WINDOW = 2;
  public static final int GRID = 3;
  public static final int FOCUS = 4;
  public static final int PRESS = 5;
  public static final int TIMER = 6;
  public static final int KEY = 7;
  public static final int HIGHLIGHT = 8;
  public static final int MOUSE = 9;
  public static final int LISTCONTAINER = 10;
  public static final int ENABLED = 11;
  public static final int MULTITOUCH = 12;
  public static final int PUSHNOTIFICATION = 13;

  /** The type of the event.
   * @see #PEN
   * @see #WINDOW
   * @see #GRID
   * @see #FOCUS
   * @see #PRESS
   * @see #TIMER
   * @see #KEY
   * @see #HIGHLIGHT
   * @see #LISTCONTAINER
   * @see #ENABLED
   */
  public int type;

  /** The control that's listening to the event. */
  public EventHandler listener;

  /** The target control. */
  public Object target;

  public Listener(Object target, int type, EventHandler listener) {
    this.target = target;
    this.type = type;
    this.listener = listener;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Listener && ((Listener) o).type == this.type && ((Listener) o).listener == this.listener;
  }
}
