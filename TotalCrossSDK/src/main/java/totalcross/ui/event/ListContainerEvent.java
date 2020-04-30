// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

import totalcross.ui.Control;

/** An event generated when the user clicks on ListContainer.Item.
 * @since SuperWaba 5.54
 * */

public class ListContainerEvent extends Event<ListContainerListener> {
  /** Event generated when a new item was selected. */
  public static final int ITEM_SELECTED_EVENT = EventType.LIST_CONTAINER_ITEM_SELECTED_EVENT;
  /** Event generated when the left image was clicked.
   * Verify the isImage2 member to determine the current image that's displayed. */
  public static final int LEFT_IMAGE_CLICKED_EVENT = EventType.LIST_CONTAINER_LEFT_IMAGE_CLICKED_EVENT;
  /** Event generated when the right image was clicked.
   * Verify the isImage2 member to determine the current image that's displayed. */
  public static final int RIGHT_IMAGE_CLICKED_EVENT = EventType.LIST_CONTAINER_RIGHT_IMAGE_CLICKED_EVENT;

  /** True if the new image is the second one. If there are two images, one
   * as an unselected image and a second one as the selected, using this field you
   * can find which one is being displayed. 
   */
  public boolean isImage2;

  /** The control that originated the event. When the user clicks on an Image, 
   * the <code>target</code> is the Image, and the source is the control where the image is. */
  public Control source;

  @Override
  public String toString() {
    String s = "";
    switch (type) {
    case ITEM_SELECTED_EVENT:
      s = "ITEM_SELECTED_EVENT";
      break;
    case LEFT_IMAGE_CLICKED_EVENT:
      s = "LEFT_IMAGE_CLICKED_EVENT";
      break;
    case RIGHT_IMAGE_CLICKED_EVENT:
      s = "RIGHT_IMAGE_CLICKED_EVENT";
      break;
    }
    return s + " isImage2:" + isImage2 + " " + super.toString();
  }

  @Override
  public void dispatch(ListContainerListener listener) {
    switch (this.type) {
    case ITEM_SELECTED_EVENT:
      listener.itemSelected(this);
      break;
    case LEFT_IMAGE_CLICKED_EVENT:
      listener.leftImageClicked(this);
      break;
    case RIGHT_IMAGE_CLICKED_EVENT:
      listener.rightImageClicked(this);
      break;
    }
  }
}
