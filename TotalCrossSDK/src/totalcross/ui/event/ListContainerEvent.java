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

import totalcross.ui.*;

/** An event generated when the user clicks on ListContainer.Item.
 * @since SuperWaba 5.54
 * */

public class ListContainerEvent extends Event
{
   /** Event generated when a new item was selected. */
   public static final int ITEM_SELECTED_EVENT = 510;
   /** Event generated when the left image was clicked.
    * Verify the isImage2 member to determine the current image that's displayed. */
   public static final int LEFT_IMAGE_CLICKED_EVENT = 511;
   /** Event generated when the right image was clicked.
    * Verify the isImage2 member to determine the current image that's displayed. */
   public static final int RIGHT_IMAGE_CLICKED_EVENT = 512;

   /** True if the new image is the second one. If there are two images, one
    * as an unselected image and a second one as the selected, using this field you
    * can find which one is being displayed. 
    */
   public boolean isImage2;
   
   /** The control that originated the event. When the user clicks on an Image, 
    * the <code>target</code> is the Image, and the source is the control where the image is. */
   public Control source;

   public String toString()
   {
      String s = "";
      switch (type)
      {
         case ITEM_SELECTED_EVENT: s = "ITEM_SELECTED_EVENT"; break;
         case LEFT_IMAGE_CLICKED_EVENT: s = "LEFT_IMAGE_CLICKED_EVENT"; break;
         case RIGHT_IMAGE_CLICKED_EVENT: s = "RIGHT_IMAGE_CLICKED_EVENT"; break;
      }
      return s+" isImage2:"+isImage2+" "+super.toString();
   }
}
