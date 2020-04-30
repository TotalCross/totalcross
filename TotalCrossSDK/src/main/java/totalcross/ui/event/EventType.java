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
 * EventType holds all TotalCross Event types
 */
public interface EventType {
    /** The direction constant for a drag or flick right. */
    public static final int RIGHT = 1;
    /** The direction constant for a drag or flick left. */
    public static final int LEFT = 2;
    /** The direction constant for a drag or flick up. */
    public static final int UP = 3;
    /** The direction constant for a drag or flick down. */
    public static final int DOWN = 4;
    /** The event type for a key press event. Device keys are handled in the SPECIAL_KEY_PRESS event. */
    public static final int KEY_PRESS = 100;
    /** The event type for a focus being transfered to this control
     * with the ENTER or ACTION keys. */
    public static final int ACTION_KEY_PRESS = 101; // guich@550_33
    /** The event type for a device key press event. Note that some keys are posted only when they are released. */
    public static final int SPECIAL_KEY_PRESS = 102;
    /** The event type for a pen or mouse down. */
    public static final int PEN_DOWN = 200;
    /** The event type for a pen or mouse up. */
    public static final int PEN_UP = 201;
    /** The event type for a pen or mouse drag. */
    public static final int PEN_DRAG = 202;
    /** The event type for a pen or mouse drag start. */
    public static final int PEN_DRAG_START = 203; // kmeehl@tc100
    /** The event type for a pen or mouse drag end. */
    public static final int PEN_DRAG_END = 204; // kmeehl@tc100

    /** The event type for a mouse moving over a control.
     * This is a hardware event.
     */
    public static final int MOUSE_MOVE = 205;

    /** The event type for a mouse moving into a control.
     * This is a software event (computed internally).
     */
    public static final int MOUSE_IN = 206;

    /** The event type for a mouse moving outside a control.
     * This is a software event (computed internally).
     */
    public static final int MOUSE_OUT = 207;

    /** The event type for a mouse wheel.
     * This is a hardware event.
     */
    public static final int MOUSE_WHEEL = 208;
    /** The event type for a pen or mouse down. */
    public static final int SCALE = 250;
    /** The event type for a pressed event. */
    public static final int PRESSED = 300;
    /** The event type for a focus in event. */
    public static final int FOCUS_IN = 301;
    /** The event type for a focus out event. */
    public static final int FOCUS_OUT = 302;
    /** The event type for a closing window. */
    public static final int WINDOW_CLOSED = 303;
    /** The event type for the control focus indicator changing to a new control. */
    public static final int HIGHLIGHT_IN = 304;
    /** The event type for the control focus indicator leaving a control. */
    public static final int HIGHLIGHT_OUT = 305;

    /** The event type fot the SIP being closed by the system. Works on Android and iOS.
     * The application cannot see this event since it is interpected by the topmost Window.
     * @since TotalCross 1.3
     */
    public static final int SIP_CLOSED = 306;
    /** Event sent when user called Edit.setCursorPos
     * @since TotalCross 1.5
     */
    public static final int CURSOR_CHANGED = 307;

    public static final int ENABLED_STATE_CHANGE = 308;
    /** The event type for a triggered timer */
    public static final int TRIGGERED = 350;
    /** The event type for a token for this device being received by server */
    public static final int TOKEN_RECEIVED = 360;
    /** The event type for a message being received by server */
    public static final int MESSAGE_RECEIVED = 361;

    /** Event generated when a new row was selected. In penless devices, the user must press 0-9 to dispatch the event. */
    public static final int GRID_SELECTED_EVENT = 501;
    /** Event generated when a grid row was checked or unchecked.
     * Verify the checked member to determine the current state. */
    public static final int GRID_CHECK_CHANGED_EVENT = 502;
    /** Generated when an editable column had its text changed. */
    public static final int GRID_TEXT_CHANGED_EVENT = 503;
    /** Event generated when a new item was selected. */
    public static final int LIST_CONTAINER_ITEM_SELECTED_EVENT = 510;
    /** Event generated when the left image was clicked.
     * Verify the isImage2 member to determine the current image that's displayed. */
    public static final int LIST_CONTAINER_LEFT_IMAGE_CLICKED_EVENT = 511;
    /** Event generated when the right image was clicked.
     * Verify the isImage2 member to determine the current image that's displayed. */
    public static final int LIST_CONTAINER_RIGHT_IMAGE_CLICKED_EVENT = 512;


    static final int SCROLL_START = 513;
    static final int SCROLL_END = 514;
    static final int SCROLL_ON_TOP = 515;
    static final int SCROLL_ON_LEFT = 516;
    static final int SCROLL_ON_RIGHT = 517;
    static final int SCROLL_ON_BOTTOM = 518;
    static final int SCROLL_UP = 519;
    static final int SCROLL_LEFT = 520;
    static final int SCROLL_RIGHT = 521;
    static final int SCROLL_DOWN = 522;

}
