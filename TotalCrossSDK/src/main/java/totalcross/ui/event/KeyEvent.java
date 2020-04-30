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

import totalcross.sys.SpecialKeys;

/**
 * KeyEvent is a key press event.
 */
public class KeyEvent extends Event<KeyListener> {
  /** The event type for a key press event. Device keys are handled in the SPECIAL_KEY_PRESS event. */
  public static final int KEY_PRESS = EventType.KEY_PRESS;
  /** The event type for a focus being transfered to this control
   * with the ENTER or ACTION keys. */
  public static final int ACTION_KEY_PRESS = EventType.ACTION_KEY_PRESS; // guich@550_33
  /** The event type for a device key press event. Note that some keys are posted only when they are released. */
  public static final int SPECIAL_KEY_PRESS = EventType.SPECIAL_KEY_PRESS;

  /**
   * The key pressed or entered by other means (grafitti input). This
   * is either a normal character key (if the value is < 70000) or
   * one of the special keys defined in the DeviceKeys interface.
   * @see SpecialKeys
   */
  public int key;

  /**
   * The state of the modifier keys when the event occured. This is a
   * OR'ed combination of the modifiers present in the DeviceKeys interface.
   * @see SpecialKeys
   */
  public int modifiers;

  /** Constructs a KeyEvent, assigning the type as KEY_PRESS. */
  public KeyEvent() // guich@421_59
  {
    type = KEY_PRESS;
  }

  /**
   * Creates a new instance of KeyEvent with the specified type, key and modifiers.
   * 
   * @param type
   *           the KeyEvent type, must be either <code>KEY_PRESS</code>, <code>ACTION_KEY_PRESS</code> or
   *           <code>SPECIAL_KEY_PRESS</code>
   * @param key
   *           the input key
   * @param modifiers
   *           state of the modifier keys for this event
   * @throws IllegalArgumentException
   *            if the type argument is not equal to <code>KEY_PRESS</code>, <code>ACTION_KEY_PRESS</code> or
   *            <code>SPECIAL_KEY_PRESS</code>.
   * @since TotalCross 1.33
   * @see #KEY_PRESS
   * @see #ACTION_KEY_PRESS
   * @see #SPECIAL_KEY_PRESS
   */
  public KeyEvent(int type, int key, int modifiers) {
    if (type < KEY_PRESS || type > SPECIAL_KEY_PRESS) {
      throw new IllegalArgumentException();
    }

    this.type = type;
    this.key = key;
    this.modifiers = modifiers;
  }

  /** Returns true if the key press is an ACTION or ENTER one.
   * @since SuperWaba 5.5
   */
  public boolean isActionKey() // guich@550_33
  {
    return key == SpecialKeys.ACTION || key == SpecialKeys.ENTER;
  }

  /** Returns true if the key press is any kind of the possible ones that means UP.
   * @since SuperWaba 5.5
   */
  public boolean isUpKey() // guich@550_33
  {
    return key == SpecialKeys.PAGE_UP || key == SpecialKeys.UP;
  }

  /** Returns true if the key press is any kind of the possible ones that means Down.
   * @since SuperWaba 5.5
   */
  public boolean isDownKey() // guich@550_33
  {
    return key == SpecialKeys.PAGE_DOWN || key == SpecialKeys.DOWN;
  }

  /** Returns true if the key press is any kind of the possible ones that means forward
   * (TAB, PAGE_DOWN, DOWN, RIGHT, etc).
   * @since SuperWaba 5.5
   */
  public boolean isNextKey() // guich@550_33
  {
    return key == SpecialKeys.RIGHT || key == SpecialKeys.TAB || isDownKey();
  }

  /** Returns true if the key press is any kind of the possible ones that means previous
   * (PAGE_UP, UP, LEFT, etc)
   * @since SuperWaba 5.5
   */
  public boolean isPrevKey() // guich@550_33
  {
    return key == SpecialKeys.LEFT || isUpKey();
  }

  @Override
  public String toString() {
    String s = "";
    switch (type) {
    case KEY_PRESS:
      s = "KEY_PRESS";
      break;
    case ACTION_KEY_PRESS:
      s = "ACTION_KEY_PRESS";
      break;
    case SPECIAL_KEY_PRESS:
      s = "SPECIAL_KEY_PRESS";
      break;
    }
    return s + " key: " + key + " " + super.toString();
  }

  @Override
  public void dispatch(KeyListener listener) {
    switch (this.type) {
    case KEY_PRESS:
      listener.keyPressed(this);
      break;
    case ACTION_KEY_PRESS:
      listener.actionkeyPressed(this);
      break;
    case SPECIAL_KEY_PRESS:
      listener.specialkeyPressed(this);
      break;
    }
  }
}
