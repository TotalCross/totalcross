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



package totalcross;

/** This is the main class of a TotalCross package. It is useful to create headless applications. Note that totalcross.ui.MainWindow implements this
 * interface, so you must implement it yourself only if you're creating a headless (no user interface) application.
 */

public interface MainClass
{
   /** Called by the vm when an event is posted */
   public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp);
   /** Called by the vm when the application is starting */
   public void appStarting(int timeAvail);
   /** Called by the vm when the application is ending */
   public void appEnding();
   /** Called by the vm when a timer event has been triggered. */
   public void _onTimerTick(boolean canUpdate);
}
