// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross;

/** This is the main class of a TotalCross package. It is useful to create headless applications. Note that totalcross.ui.MainWindow implements this
 * interface, so you must implement it yourself only if you're creating a headless (no user interface) application.
 */

public interface MainClass {
  /** Called by the vm when an event is posted */
  public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp);

  /** Called by the vm when the application is starting */
  public void appStarting(int timeAvail);

  /** Called by the vm when the application is ending */
  public void appEnding();

  /** Called by the vm when a timer event has been triggered. */
  public void _onTimerTick(boolean canUpdate);
}
