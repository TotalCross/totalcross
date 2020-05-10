// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

public class EnabledStateChangeEvent extends ControlEvent {

  public static final int ENABLED_STATE_CHANGE = EventType.ENABLED_STATE_CHANGE;

  /** Constructs an empty EnabledStateChangeEvent. */
  public EnabledStateChangeEvent() {
    super();
    type = ENABLED_STATE_CHANGE;
  }

  @Override
  public String toString() {
    return "ENABLED_STATE_CHANGE " + super.toString();
  }
}
