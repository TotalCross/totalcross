/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.ui.event;

import totalcross.ui.*;

/**
 * ControlEvent is an event posted by a control.
 */

public class ControlEvent extends Event
{
   /** The event type for a pressed event. */
   public static final int PRESSED = 300;
   /** The event type for a focus in event. */
   public static final int FOCUS_IN = 301;
   /** The event type for a focus out event. */
   public static final int FOCUS_OUT = 302;
   /** The event type for a closing window. */
   public static final int WINDOW_CLOSED = 303;
   /** The event type for the control focus indicator changing to a new control. */
   public static final int HIGHLIGHT_IN = 304;
   /** The event type for the control focus indicator leaving a control. */
   public static final int HIGHLIGHT_OUT = 305;
   /** The event type fot the SIP being closed by the system. Works on Android and iOS. 
    * The application cannot see this event since it is interpected by the topmost Window.
    * @since TotalCross 1.3
    */
   public static final int SIP_CLOSED = 306;
   /** Event sent when user called Edit.setCursorPos
    * @since TotalCross 1.5 
    */
   public static final int CURSOR_CHANGED = 307;

   /** Constructs an empty ControlEvent. */
   public ControlEvent()
   {
   }

   /**
   * Constructs a control event of the given type.
   * @param type the type of event
   * @param c the target control
   */
   public ControlEvent(int type, Control c)
   {
      this.type = type;
      target = c;
      timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
   }

   /** Updates the control event setting the timestamp, consumed and target.
    * @since TotalCross 1.0
    */
   public ControlEvent update(Control c)
   {
      timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
      consumed = false;
      target = c;
      return this;
   }
   
   public String toString()
   {
      String s = "";
      switch (type)
      {
         case PRESSED      : s = "PRESSED"; break;
         case FOCUS_IN     : s = "FOCUS_IN"; break;
         case FOCUS_OUT    : s = "FOCUS_OUT"; break;
         case WINDOW_CLOSED: s = "WINDOW_CLOSED"; break;
         case HIGHLIGHT_IN : s = "HIGHLIGHT_IN"; break;
         case HIGHLIGHT_OUT: s = "HIGHLIGHT_OUT"; break;
      }
      return s+" "+super.toString();
   }
}
