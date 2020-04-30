// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
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
