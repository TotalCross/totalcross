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

/** An event generated when the user clicks on a grid or checks it.
 * @since SuperWaba 5.54
 * */

public class GridEvent extends Event
{
   /** Event generated when a new row was selected. In penless devices, the user must press 0-9 to dispatch the event. */
   public static final int SELECTED_EVENT = 501;
   /** Event generated when a grid row was checked or unchecked.
    * Verify the checked member to determine the current state. */
   public static final int CHECK_CHANGED_EVENT = 502;
   /** Generated when an editable column had its text changed. */
   public static final int TEXT_CHANGED_EVENT = 503;

   /** True if the column is checked. On grid that has no check column, this member is useless. */
   public boolean checked;

   /** Stores the target row of this grid event. If the user checked all checks (by clicking in the header check),
    * it is Grid.ALL_CHECKED; otherwise, if the user unchecked all lines, it is Grid.ALL_UNCHECKED. */
   public int row;
   /** Stores the target column of this grid event. On grid that has a check column, the columns text starts from 1,
    * otherwise, if no check, it starts from 0.
    */
   public int col;

   public String toString()
   {
      String s = "";
      switch (type)
      {
         case SELECTED_EVENT: s = "SELECTED_EVENT"; break;
         case CHECK_CHANGED_EVENT: s = "CHECK_CHANGED_EVENT"; break;
         case TEXT_CHANGED_EVENT: s = "TEXT_CHANGED_EVENT"; break;
      }
      return s+" row:"+row+", col: "+col+", checked: "+checked+" "+super.toString();
   }
}
