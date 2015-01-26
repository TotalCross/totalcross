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

/** Interface used to listen to ListContainer events. */

public interface ListContainerListener
{
   /** An ITEM_SELECTED_EVENT event was dispatched.
    * @see ListContainerEvent 
    */
   public void itemSelected(ListContainerEvent e);
   /** An LEFT_IMAGE_CLICKED_EVENT event was dispatched.
    * @see ListContainerEvent 
    */
   public void leftImageClicked(ListContainerEvent e);
   /** An RIGHT_IMAGE_CLICKED_EVENT event was dispatched.
    * @see ListContainerEvent 
    */
   public void rightImageClicked(ListContainerEvent e);
}
