// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.ui.gfx.Color;

/** This class holds the colors used in some user interface dialogs, such as MessageBox,
 * InputBox, CalculatorBox, KeyboardBox, CalendarBox, ToolTip, KeyPad and others.
 * You can customize it as needed.
 * <p>
 * To correctly change the colors to your own, you must do it in the constructor of your
 * application.
 *
 * @since SuperWaba 5.64
 */
public final class UIColors // guich@564_6
{
  private UIColors() {
  }

  /** The border color of all windows. */
  public static int windowBorder = Color.BLACK;

  /** KeyboardBox background color. */
  public static int keyboardBack = 0xFFFFFF;
  /** KeyboardBox foreground color. */
  public static int keyboardFore = Color.BLACK;
  /** KeyboardBox action color. */
  public static int keyboardAction = 0xFAB428;

  /** CalculatorBox background color. */
  public static int calculatorBack = keyboardBack;
  /** CalculatorBox foreground color. */
  public static int calculatorFore = keyboardFore;
  /** CalculatorBox action color. */
  public static int calculatorAction = keyboardAction;

  /** CalendarBox background color. */
  public static int calendarBack = keyboardBack;
  /** CalendarBox foreground color. */
  public static int calendarFore = Color.BLACK;
  /** CalendarBox action color. */
  public static int calendarAction = keyboardAction;
  /** CalendarBox arrow colors. */
  public static int calendarArrows = Color.BLACK;

  /** MessageBox background color. */
  public static int messageboxBack = Color.getRGB("fcfcfc");
  /** MessageBox foreground color. */
  public static int messageboxFore = Color.BLACK;
  /** MessageBox message foreground*/
  public static int messageboxMsgFore = Color.getRGB("707070");
  /** MessageBox action color. */
  public static int messageboxAction = Color.getRGB("6709ee");

  /** InputBox background color. */
  public static int inputboxBack = keyboardBack;
  /** InputBox foreground color. */
  public static int inputboxFore = Color.BLACK;
  /** InputBox action color. */
  public static int inputboxAction = keyboardAction;

  /** ColorChooserBox background color. */
  public static int colorchooserboxBack = keyboardBack;
  /** ColorChooserBox foreground color. */
  public static int colorchooserboxFore = Color.BLACK;
  /** ColorChooserBox action color. */
  public static int colorchooserboxAction = keyboardAction;

  /** ToolTip background color. */
  public static int tooltipBack = Color.YELLOW;
  /** ToolTip foreground color. */
  public static int tooltipFore = Color.BLACK;

  /** Keypad background color. */
  public static int keypadBack = Color.YELLOW;
  /** Keypad foreground color. */
  public static int keypadFore = Color.BLACK;

  /** Default control foreground color. */
  public static int controlsFore = Color.BLACK;
  /** Default control background color. */
  public static int controlsBack = Color.BRIGHT;

  /** Make the edit area have the same color of the background setting this to true */
  public static boolean sameColors; // guich@572_15

  /** These are the colors used to draw the highlight rectangle. */
  public static int[] highlightColors = (Settings.screenWidth > 200) // guich@573_23  - guich@580_
      ? new int[] { Color.GREEN, Color.GREEN, Color.CYAN, Color.CYAN, Color.WHITE, Color.WHITE }
      : new int[] { Color.GREEN, Color.CYAN, Color.WHITE };
  /** The default step used on Vista buttons to make the fade. Decrease the step to make the button lighter. */
  public static int vistaFadeStep = Settings.screenBPP == 16 ? 8 : 5;

  /** FileChooser foreground color. */
  public static int fileChooserFore = Color.BLACK;
  /** FileChooser background color. */
  public static int fileChooserBack = 0xEEEEAA;

  /** HtmlContainer background color for the Form controls. */
  public static int htmlContainerControlsFore = Color.BLACK;
  /** HtmlContainer foreground color for the Form controls. */
  public static int htmlContainerControlsBack = Color.WHITE;
  /** HtmlContainer link foreground color. */
  public static int htmlContainerLink = Color.BLUE;

  /** TimeBox visor's background color. */
  public static int timeboxVisorBack = -1;  
  /** TimeBox visor's background color. */
  public static int timeboxVisorFore = Color.BLACK;
  /** TimeBox visor's cursor color. */
  public static int timeboxVisorCursor = -1;
  /** TimeBox OK button color. */
  public static int timeboxOk = -1;
  /** TimeBox Clear button color. */
  public static int timeboxClear = 0xf78865;
  /** TimeBox background color. */
  public static int timeboxBack = keyboardBack;
  /** TimeBox foreground color. */
  public static int timeboxFore = keyboardFore;

  /** Default value to be used in all textShadowColor(s) set in the constructor of a control. Defaults to -1.
   * Note that it does not affect the shadow when you call setBackColor or setForeColor.
   * @see Control#BRIGHTER_BACKGROUND
   * @see Control#DARKER_BACKGROUND
   * @since TotalCross 1.27
   */
  public static int textShadowColor = -1;

  /** The color of the PositionBar for all places that use it. */
  public static int positionbarColor = Color.DARK;

  /** The color to fill the background of the PositionBar. Defaults to -1 (don't fill). */
  public static int positionbarBackgroundColor = -1;

  /** Spinner foreground color. */
  @Deprecated
  public static int spinnerFore = controlsFore;

  /** Spinner background color. */
  @Deprecated
  public static int spinnerBack = -1;

  /** The shaded factor (0 - 100) used to fill a background when the backgroundStyle is BACKGROUND_SHADED */
  public static int shadeFactor = 30;

  /** NumericBox background color. */
  public static int numericboxBack = keyboardBack;
  /** NumericBox foreground color. */
  public static int numericboxFore = keyboardFore;
  /** NumericBox action color. */
  public static int numericboxAction = keyboardAction;

  /** The color that will be used to paint the disabled area when a screen shift occurs. 
   * @since TotalCross 1.3
   */
  public static int shiftScreenColor = 0x808080;

  /** Background color of the clipboard menu that is opened at Edit and MultiEdit. */
  public static int clipboardBack = Color.YELLOW;
  /** Foreground color of the clipboard menu that is opened at Edit and MultiEdit. */
  public static int clipboardFore = Color.BLACK;

  /** Background color of the TopMenu */
  public static int topmenuBack = 0x2C3337;
  /** Foreground color of the TopMenu */
  public static int topmenuFore = Color.WHITE;
  /** Separator color of the TopMenu */
  public static int separatorFore = 0x61666A;

  /** The selected color for material UI */
  public static int materialSelectedColor = Color.BLUE;
}
