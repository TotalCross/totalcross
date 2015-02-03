/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2009-2012 SuperWaba Ltda.                                      *
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

/** A class that represents an event Listener. Holds the type of the event and the listener itself.
 * @since Totalcross 1.22
 */
public class Listener // guich@tc122_11: have to distinguish the type of event of a listener, NOT only based on the instanceof operator.
{
   public static final int PEN = 1;
   public static final int WINDOW = 2;
   public static final int GRID = 3;
   public static final int FOCUS = 4;
   public static final int PRESS= 5;
   public static final int TIMER = 6;
   public static final int KEY = 7;
   public static final int HIGHLIGHT = 8;
   public static final int MOUSE = 9;
   public static final int LISTCONTAINER = 10;
   public static final int ENABLED = 11;
   public static final int MULTITOUCH = 12;
   
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
   public Object listener;
   
   /** The target control. */
   public Object target;
   
   public Listener(Object target, int type, Object listener)
   {
      this.target = target;
      this.type = type;
      this.listener = listener;
   }
   
   public boolean equals(Object o)
   {
      return o instanceof Listener && ((Listener)o).type == this.type && ((Listener)o).listener == this.listener;
   }
}

