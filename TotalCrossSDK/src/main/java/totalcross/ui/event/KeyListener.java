// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.event;

/** Interface used to listen to Key events. */

public interface KeyListener extends EventHandler {
  /** A KEY_PRESS event was dispatched.
   * @see KeyEvent 
   */
  public void keyPressed(KeyEvent e);

  /** An ACTION_KEY_PRESS event was dispatched.
   * @see KeyEvent 
   */
  public void actionkeyPressed(KeyEvent e);

  /** A SPECIAL_KEY_PRESS event was dispatched.
   * @see KeyEvent 
   */
  public void specialkeyPressed(KeyEvent e);
}
