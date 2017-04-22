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
 * PenEvent is a pen down, up, move or drag event.
 * <p>
 * A pen drag occurs when the pen moves while the screen is pressed.
 */

public class PenEvent extends Event
{
   /** The event type for a pen or mouse down. */
   public static final int PEN_DOWN = 200;
   /** The event type for a pen or mouse up. */
   public static final int PEN_UP = 201;
   /** The event type for a pen or mouse drag. */
   public static final int PEN_DRAG = 202;
   /** The event type for a pen or mouse drag start. */
   public static final int PEN_DRAG_START = 203; // kmeehl@tc100
   /** The event type for a pen or mouse drag end. */
   public static final int PEN_DRAG_END = 204; // kmeehl@tc100
   
   protected static final String[] EVENT_NAME = {"PEN_DOWN","PEN_UP","PEN_DRAG","PEN_DRAG_START","PEN_DRAG_END","MOUSE_MOVE","MOUSE_IN","MOUSE_OUT","MOUSE_WHEEL"};

   /** The x location of the event. */
   public int x;

   /** The y location of the event. */
   public int y;

   /** The absolute x location of the event. */
   public int absoluteX;
   
   /** The absolute y location of the event. */
   public int absoluteY;

   /**
    * The state of the modifier keys when the event occured. This is a
    * OR'ed combination of the modifiers present in the DeviceKeys interface.
    * @see totalcross.sys.SpecialKeys
    */
   public int modifiers;


   /** Updates this event setting also the timestamp, consumed and target.
    * @since TotalCross 1.0
    */
   public PenEvent update(Control c, int absoluteX, int x, int absoluteY, int y, int type, int modifiers)
   {
      this.absoluteX = absoluteX;
      this.x = x;
      this.absoluteY = absoluteY;
      this.y = y;
      this.type = type;
      timeStamp = totalcross.sys.Vm.getTimeStamp(); // guich@200b4: removed this from the other subclasses and putted here.
      consumed = false;
      target = c;
      this.modifiers = modifiers;
      return this;
   }
   
   /** Returns the event name. Used to debugging. */
   public static String getEventName(int type)
   {
      return PEN_DOWN <= type && type <= PEN_DRAG_END ? EVENT_NAME[type-200] : "Not a PEN_EVENT";
   }
   
   public String toString()
   {
      return EVENT_NAME[type-200]+" pos: "+x+","+y+" "+super.toString();
   }
}

