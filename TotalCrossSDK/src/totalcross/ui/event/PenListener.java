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

/** Interface used to listen to Pen events. */

public interface PenListener
{
   /** A PEN_DOWN event was dispatched.
    * @see PenEvent 
    */
   public void penDown(PenEvent e);
   /** A PEN_UP event was dispatched.
    * @see PenEvent 
    */
   public void penUp(PenEvent e);
   /** A PEN_DRAG event was dispatched.
    * @see PenEvent 
    */
   public void penDrag(DragEvent e); // guich@tc122_11: now a DragEvent
   /** A PEN_DRAG_START event was dispatched.
    * @see PenEvent 
    */
   public void penDragStart(DragEvent e); // guich@tc122_11: now a DragEvent
   /** A PEN_DRAG_END event was dispatched.
    * @see PenEvent 
    */
   public void penDragEnd(DragEvent e); // guich@tc122_11: now a DragEvent
}
