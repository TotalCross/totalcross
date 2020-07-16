// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to ListContainer events. */

public interface ListContainerListener extends EventHandler {
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
