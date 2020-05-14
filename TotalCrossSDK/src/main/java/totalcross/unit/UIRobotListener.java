// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.unit;

import totalcross.ui.event.EventHandler;

/** Interface used to listen to UIRobot events. 
 */

public interface UIRobotListener extends EventHandler {
  /** The UIRobot has succeed. */
  public void robotSucceed(UIRobotEvent e);

  /** The UIRobot has failed. Check the failureReason field for more information. */
  public void robotFailed(UIRobotEvent e);
}
