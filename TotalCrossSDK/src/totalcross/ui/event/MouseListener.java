/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
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

/** Interface used to listen to mouse move events.
 * @since TotalCross 1.27 
 */

public interface MouseListener
{
   /** A MOUSE_MOVE event was dispatched.
    * @see PenEvent 
    */
   public void mouseMove(MouseEvent e);
   
   /** A MOUSE_IN event was dispatched when the mouse was going into a control.
    * @see PenEvent 
    */
   public void mouseIn(MouseEvent e);
   
   /** A MOUSE_OUT event was dispatched when the mouse was going out of a control.
    * @see PenEvent 
    */
   public void mouseOut(MouseEvent e);
   
   /** The event type for a mouse wheel moving down.
    * This is a hardware event. 
    */
   public void mouseWheel(MouseEvent e);

}
