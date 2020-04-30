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

/**
 * An event that represents a pen drag.
 */
public class DragEvent extends PenEvent {
  /** The direction constant for a drag or flick right. */
  public static final int RIGHT = 1;
  /** The direction constant for a drag or flick left. */
  public static final int LEFT = 2;
  /** The direction constant for a drag or flick up. */
  public static final int UP = 3;
  /** The direction constant for a drag or flick down. */
  public static final int DOWN = 4;

  public final static String[] DIRECTIONS = { "", "RIGHT", "LEFT", "UP", "DOWN" };

  public int xDelta, yDelta, xTotal, yTotal;

  public int direction;

  /** Unique id for the entire physical drag. */
  public int dragId;

  /** Constructs an empty DragEvent. */
  public DragEvent() {
  }

  /**
   * Constructs a new DragEvent from a PenEvent, setting a new timestamp and setting consumed to false.
   */
  public DragEvent(PenEvent evt) {
    update(evt);
  }

  /**
   * Updates this DragEvent from a PenEvent, setting a new timestamp and setting consumed to false.
   */
  public DragEvent update(PenEvent evt) {
    this.absoluteX = evt.absoluteX;
    this.x = evt.x;
    this.absoluteY = evt.absoluteY;
    this.y = evt.y;
    this.type = evt.type;
    timeStamp = totalcross.sys.Vm.getTimeStamp();
    target = evt.target;
    this.modifiers = evt.modifiers;
    return this;
  }

  @Override
  public String toString() {
    return EVENT_NAME[type - 200] + ", direction: " + DIRECTIONS[direction] + ", pos: " + x + "," + y + ", delta: "
        + xDelta + "," + yDelta + ", total: " + xTotal + "," + yTotal + ". " + super.toString();
  }

  public static int getInverseDirection(int direction) {
    switch (direction) {
    case UP:
      return DOWN;
    case DOWN:
      return UP;
    case LEFT:
      return RIGHT;
    case RIGHT:
      return LEFT;
    default:
      return 0;
    }
  }
}
