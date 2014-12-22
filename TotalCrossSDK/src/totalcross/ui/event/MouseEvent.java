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

/**
 * MouseEvent is a mouse move, in a control or out a control events.
 * Only occurs on devices with a mouse, like Windows 32 and Linux desktop.
 * 
 * @since TotalCross 1.27
 */

public class MouseEvent extends PenEvent
{
   /** @see totalcross.ui.event.DragEvent 
    */
   public int wheelDirection;
   
   /** The event type for a mouse moving over a control.
    * This is a hardware event. 
    */
   public static final int MOUSE_MOVE = 205;
   
   /** The event type for a mouse moving into a control.
    * This is a software event (computed internally). 
    */
   public static final int MOUSE_IN = 206;
   
   /** The event type for a mouse moving outside a control. 
    * This is a software event (computed internally). 
    */
   public static final int MOUSE_OUT = 207;

   /** The event type for a mouse wheel.
    * This is a hardware event. 
    */
   public static final int MOUSE_WHEEL = 208;
}

