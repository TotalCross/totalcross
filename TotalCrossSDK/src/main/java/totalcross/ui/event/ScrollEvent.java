// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.event;

/**
 * Event that decribes Every scroll event
 */
public class ScrollEvent extends Event {

    /**
     * Contructor
     * @param type This param must be one the type defined
     *             , i.e, ScrollEvent.SCROLL_ON_TOP
     */
    public ScrollEvent(int type) {
        super.type = type;
    }

    /**
     * type thrown when scroll reachs the content top
     */
    public static final int SCROLL_ON_TOP = EventType.SCROLL_ON_TOP;
    /**
     * type thrown on scroll starts
     */
    public static final int SCROLL_START = EventType.SCROLL_START;
    /**
     * type thrown when scroll ends
     */
    public static final int SCROLL_END = EventType.SCROLL_END;
    /**
     * type thrown when scroll reachs content left side
     */
    public static final int SCROLL_ON_LEFT = EventType.SCROLL_ON_LEFT;
    /**
     * type thrown when scroll reachs the content right side
     */
    public static final int SCROLL_ON_RIGHT = EventType.SCROLL_ON_RIGHT;
    /**
     * type thrown when scroll reachs the content bottom
     */
    public static final int SCROLL_ON_BOTTOM = EventType.SCROLL_ON_BOTTOM;
    /**
     * type thrown when scroll up
     */
    public static final int SCROLL_UP = EventType.SCROLL_UP;
    /**
     * type thrown when scroll left
     */
    public static final int SCROLL_LEFT = EventType.SCROLL_LEFT;
    /**
     * type thrown when scroll right
     */
    public static final int SCROLL_RIGHT = EventType.SCROLL_RIGHT;
    /**
     * type thrown when scroll down
     */
    public static final int SCROLL_DOWN = EventType.SCROLL_DOWN;

    public void dispatch(EventHandler listener) {

    }
}
