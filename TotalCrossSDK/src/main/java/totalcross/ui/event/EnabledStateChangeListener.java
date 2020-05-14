// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to enable/disable states. */

public interface EnabledStateChangeListener extends EventHandler {
  /** The state has changed.
   */
  public void enabledStateChange(EnabledStateChangeEvent e);
}
