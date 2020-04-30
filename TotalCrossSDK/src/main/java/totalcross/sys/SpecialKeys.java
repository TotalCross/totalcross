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

package totalcross.sys;

/**
 * SpecialKeys is an interface containing values for special keys and modifiers.
 * <p>
 * Below is an example of SpecialKeys being used.
 *
 * <pre>
 * public void onEvent(Event event)
 * {
 *  if (event.type == KeyEvent.SPECIAL_KEY_PRESS)
 *  {
 *     KeyEvent ke = (KeyEvent)event;
 *     if ((ke.modifiers & SpecialKeys.CONTROL) != 0)
 *        ... control key was held down
 *     if (ke.key == SpecialKeys.PAGE_DOWN)
 *        ... page down key pressed
 *     if (ke.key == SpecialKeys.PAGE_UP)
 *        ... page up key pressed
 * </pre>
 */
public interface SpecialKeys {
  // NOTE: The TotalCross VM indexes directly to these values

  /** modifier for alt key */
  public static final int ALT = (1 << 0);
  /** modifier for control key */
  public static final int CONTROL = (1 << 1);
  /** modifier for shift key */
  public static final int SHIFT = (1 << 2);
  /** modifier used in Android that states the key came from a trusted part of the system (ie, physical keyboard) */
  public static final int SYSTEM = (1 << 3);

  /** special key */
  public static final int PAGE_UP = -1000;
  /** special key */
  public static final int PAGE_DOWN = -1001;
  /** special key */
  public static final int HOME = -1002;
  /** special key. Used in Smartphones */
  public static final int END = -1003;
  /** special key */
  public static final int UP = -1004;
  /** special key */
  public static final int DOWN = -1005;
  /** special key */
  public static final int LEFT = -1006;
  /** special key */
  public static final int RIGHT = -1007;
  /** special key */
  public static final int INSERT = -1008;
  /** special key. On Windows, the enter key maps to ACTION
   * @see #ACTION
   */
  public static final int ENTER = -1009;
  /** special key */
  public static final int TAB = -1010;
  /** special key. Used in Smartphones */
  public static final int BACKSPACE = -1011;
  /** special key */
  public static final int ESCAPE = -1012;
  /** special key */
  public static final int DELETE = -1013;
  /** special key */
  public static final int MENU = -1014;
  /** special key */
  public static final int COMMAND = -1015;
  /** Pressed abc in Palm OS, and the ALT key in Treo 600. */
  public static final int KEYBOARD_ABC = -1016;
  /** Pressed 123 in Palm OS or case convertion in Smartphones */
  public static final int KEYBOARD_123 = -1017;
  /** special key */
  public static final int KEYBOARD = -1018;
  /** Also used in Smartphones */
  public static final int HARD1 = -1019;
  /** Also used in Smartphones */
  public static final int HARD2 = -1020;
  /** special key */
  public static final int HARD3 = -1021;
  /** special key */
  public static final int HARD4 = -1022;
  /** CALC button under PalmOS, NOTES button under WindowsCE */
  public static final int CALC = -1023;
  /** Valid only on PalmOS */
  public static final int FIND = -1024;
  /** Valid only on PalmOS */
  public static final int LAUNCH = -1025;
  /** Valid only on WindowsCE. If not registered by the user, it is replaced by MENU. */
  public static final int ACTION = -1026;
  /** Valid only on PalmOS */
  public static final int CONTRAST = -1027;
  /** Valid only on PalmOS */
  public static final int CLOCK = -1028;
  /** Valid only on PalmOS. Equivalent to the HotSync button. */
  public static final int SYNC = -1029;
  /** Screen change: can be a rotation or a collapse/expand. Settings.screenWidth/Height are already changed when this key event is posted. */
  public static final int SCREEN_CHANGE = -1030;
  /** The device was turned off and now on while the application was running. This is not a 
   * hardware key and cannot be intercepted; all you can do is handle the SPECIAL_KEY_PRESS event and check if this key was typed. 
   * Currently supported on Windows CE and Palm OS devices. 
   */
  public static final int POWER_ON = -1031;

  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F1 = -1041;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F2 = -1042;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F3 = -1043;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F4 = -1044;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F5 = -1045;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F6 = -1046;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F7 = -1047;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F8 = -1048;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F9 = -1049;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F10 = -1050;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F11 = -1051;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F12 = -1052;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F13 = -1053;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F14 = -1054;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F15 = -1055;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F16 = -1056;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F17 = -1057;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F18 = -1058;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F19 = -1059;
  /** Used on some Windows CE devices. Not used at Win32 or JavaSE. */
  public static final int F20 = -1060;
}