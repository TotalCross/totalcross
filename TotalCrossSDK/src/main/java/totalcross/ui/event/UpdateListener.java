/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.   
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
 * Interface used to listen for Update events.
 * 
 * These events fire regularly and should be used to animate elements
 * Note that the frequency in which update events fire is not guaranteed
 * to be constant or within any given thereshold of the expected frequency.
 */
public interface UpdateListener {
   /**
    * Called regularly by the system.
    * @param elapsedMilliseconds number of milliseconds since the last UpdateListenerTriggered call
    */
   public void updateListenerTriggered(int elapsedMilliseconds);
}
