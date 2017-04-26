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

package totalcross.unit;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

/** Event sent when a Robot runs.
 * The target is always the MainWindow's instance.
 */
public class UIRobotEvent extends Event
{
   /** Event indicating that the robot has succeed. */
   public static final int ROBOT_SUCCEED = 350;
   /** Event indicating that the robot has failed. Check the failureReason field for more information. */
   public static final int ROBOT_FAILED = 351;

   static final int ROBOT_EOF = 352;
   
   /** The name of the robot that was running. */
   public String robotName;
   
   /** The reason of the failure, if any. */
   public String failureReason;
   
   UIRobotEvent(int type, String robotName, String reason)
   {
      super(type, MainWindow.getMainWindow(), Vm.getTimeStamp());
      this.robotName = robotName;
      this.failureReason = reason;
   }
}
