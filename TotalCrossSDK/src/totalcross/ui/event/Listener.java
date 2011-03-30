/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2009-2011 SuperWaba Ltda.                                      *
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

// $Id: Listener.java,v 1.3 2011-02-02 09:02:15 guich Exp $

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
   
   /** The type of the event.
    * @see #PEN
    * @see #WINDOW
    * @see #GRID
    * @see #FOCUS
    * @see #PRESS
    * @see #TIMER
    * @see #KEY
    * @see #HIGHLIGHT
    */
   public int type;
   
   /** The control that's listening to the event. */ 
   public Object listener;
   
   public Listener(int type, Object listener)
   {
      this.type = type;
      this.listener = listener;
   }
}

