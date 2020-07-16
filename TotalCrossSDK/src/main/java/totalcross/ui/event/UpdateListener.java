// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
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
