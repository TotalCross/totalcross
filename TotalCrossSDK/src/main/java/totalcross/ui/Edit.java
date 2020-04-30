// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.dialog.CalculatorBox;
import totalcross.ui.dialog.CalendarBox;
import totalcross.ui.dialog.KeyboardBox;
import totalcross.ui.dialog.TimeBox;
import totalcross.ui.dialog.keyboard.VirtualKeyboard;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PressListener;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.UpdateListener;
import totalcross.ui.event.ValueChangeEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Date;
import totalcross.util.InvalidDateException;
import totalcross.util.UnitsConverter;

/**
 * Edit is a text entry control.
 * <p>
 * Here is an example showing an edit control being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    Edit edit;
 *
 *    public void initUI()
 *    {
 *       edit = new Edit();
 *       add(edit,LEFT,TOP);
 *    }
 * </pre>
 * Important: if you wish to open a popup window after a FOCUS_OUT event has occured,
 * you must open the window with popupNonBlocking, never with popup. Otherwise,
 * the window will be openned twice.
 * Here's a sample code on how to proceed:
 * <pre>
 * public class Test extends MainWindow
 * {
      Edit ed;
      MessageBox mb;

      public void initUI()
      {
         add(ed = new Edit(""), LEFT, CENTER);
         add(new Button("btn"), LEFT, AFTER+5);
      }

      public void onEvent(Event event)
      {
         switch (event.type)
         {
            case ControlEvent.FOCUS_OUT:
               if (event.target == ed)
               {
                  event.consumed = true; // this is important!
                  (mb=new MessageBox("Hi","Verinha")).popupNonBlocking();
               }
               break;
            case ControlEvent.WINDOW_CLOSED:
               if (event.target == mb)
                  ed.setText("Window closed.");
               break;
         }
      }
   }
 * </pre>
 * A long click on an Edit will result in a menu with copy/paste options to be displayed.
 * @see #clipboardDelay
 */

public class Edit extends Control implements TextControl, HasValue<String> {
  private int blinkTime = 0;
  protected UpdateListener blinkListener = new UpdateListener() {
		
		@Override
		public void updateListenerTriggered(int elapsedMilliseconds) {
			blinkTime  += elapsedMilliseconds;
			if(blinkTime > 350) {
				if (!isTopMost()) {
			          focusOut();
			        } else if (parent != null) {
			          Window.needsPaint = true;
			          // guich@tc130: show the copy/paste menu
			          /*                  if (editable && enabled && lastPenDown != -1 && clipboardDelay != -1 && (Vm.getTimeStamp() - lastPenDown) >= clipboardDelay)
			                   if (showClipboardMenu())
			                   {
			                      event.consumed = true;
			                      break;
			                   }
			           */ }
				cursorShowing = !cursorShowing;
				blinkTime = 0;
			}
		}
	}; // only valid while the edit has focus
  private static int xMins[] = { 4, 1, 3, 3, 4, 4, UnitsConverter.toPixels(DP + 12) };
  public static final int prefH = uiAndroid ? 4 : 2;

  protected boolean hasFocus;
  protected boolean cursorShowing;
  /** Specifies if the control accepts input from the user.
   * Note: do not change this directly; use the setEditable method instead.
   * @see #setEditable(boolean)
   */
  protected boolean editable = true;
  /** Specifies if new chars should overwrite existing ones. */
  public boolean overwrite;
  /**
   * The height of the image based on the edit's height, ranging from 1 to 100.
   * Used when an image is passed as parameter in one of the constructors.
   */
  private int captionIconHeightFactor = 0;

/** Sets the alignment of this Edit, which can be LEFT (default), CENTER or RIGHT.
   *  Note that it will always edit at left, but on focus lost, it will drawn aligned.
   *  @since SuperWaba 5.03
   */
  public int alignment = LEFT;

  /** The caption to draw when this Edit is empty.
   * In Material UI, you must set the caption BEFORE you add this control to the container; also remember to set the height to at least PREFERRED, it is 75% taller.
   * @see #captionColor 
   */
  public String caption;

  /** The caption's color. */
  public int captionColor = -1;
  
  /** The line's color. */
  public int lineColor = 0;

  /** An optional caption's icon */
  public Image captionIcon;
  /** The padding between the captionIcon and the left border. If there's no captionIcon this will do nothing.*/
  private int captionIconPadding;

/** @see CalculatorBox#rangeCheck */
  public CalculatorBox.RangeCheck rangeCheck;

  /** Set to true on Android devices to use the native numeric pad when mode is set to CURRENCY. 
   * Note that the numeric keybaord will probably appear only on the default keyboard.
   */
  public static boolean useNativeNumericPad;

  private ControlEvent cursorChangedEvent;
  protected StringBuffer chars = new StringBuffer(10);
  protected boolean hasBorder = true;
  protected int xMax;
protected int xMin;
protected int gap;
  protected int fColor, back0, back1;
  protected int fourColors[] = new int[4];

  protected boolean isMaskedEdit;
  private int decimalPlaces = 2;
  protected int insertPos;
  protected int startSelectPos;
  protected int xOffset;
  private boolean wasFocusIn, wasFocusInOnPenDown; // jairocg@450_31: used to verify if the event was focusIn
  private int oldTabIndex = -1;
  private boolean ignoreSelect;
  private int wildW;
  protected boolean useFillAsPreferred;
  protected StringBuffer masked = new StringBuffer(20);
  private boolean isNegative;
  protected int cursorX;
  private int lastCommand;
  private String mapFrom, mapTo; // guich@tc110_56
  protected Image npback;
  public boolean showKeyboardOnNextEvent;
  static PushButtonGroup clipboardMenu;
  /** Used to inform that a <i>copy</i> operation has been made. You can localize this message if you wish. */
  public static String copyStr = "copy";
  /** Used to inform that a <i>cut</i> operation has been made. You can localize this message if you wish. */
  public static String cutStr = "cut";
  /** Used to inform that a <i>paste</i> operation has been made. You can localize this message if you wish. */
  public static String pasteStr = "paste";
  /** Used to inform that a <i>replace</i> operation has been made. You can localize this message if you wish. */
  public static String replaceStr = "replace";
  /** Used to inform that a <i>command</i> operation has been made. You can localize this message if you wish. */
  public static String commandStr = "command";

  /** Set to false to disable focus change on this Edit */
  protected boolean canMoveFocus = true;

  /** Handler for the CaptionPress */
  public CaptionPress captionPress;
  
  public boolean drawLine = true;

  /** Defines an optional value to be used in the CalculatorBox when the keyboard type is KBD_NUMERIC or KBD_CALCULATOR. 
   * Replaces the decimal separator / 00 char.
   * @since TotalCross 1.5 
   */
  public String optionalValue4CalculatorBox;

  /** Defines a title that can be used in the Keyboards.
   * @since TotalCross 1.53
   */
  public String keyboardTitle;

  /** Defines the time that the user will have to press to see a popup menu with copy/paste options.
   * Set to -1 to disable it; defaults to 1500 (1.5 seconds) in pen devices, -1 in finger devices (because in these devices . Also affects MultiEdit.
   * @since TotalCross 1.3
   */
  public static int clipboardDelay = 1500;

  protected String validChars; // guich
  /** The KeyboardBox used in all Edits. */
  public static VirtualKeyboard keyboard; // guich
  /** The CalendarBox used in all Edits. */
  public static CalendarBox calendar; // guich
  /** The CalculatorBox used in all Edits. */
  public static CalculatorBox calculator; // guich@200
  /** The NumericBox used in all Edits. */
  public static CalculatorBox numeric;
  /** The TimeBox used in all Edits. */
  public static TimeBox time;
  protected byte mode; // guich
  protected int maxLength; // guich@200b4
  /** used only to compute the preferred width of this edit. If the mask is empty, the edit fills to width. */
  protected char[] mask;
  /** Sets the capitalise settings for this Edit. Text entered will made as is, uppercase or
   * lowercase
   * @see #ALL_NORMAL
   * @see #ALL_UPPER
   * @see #ALL_LOWER
   */
  public byte capitalise; // guich@320_26
  public static boolean removeFocusOnAction = true; // as per MBertrand - guich@580

  /** Color to apply to the Edit when it has focus (works only on Android user interface style). 
   * By default, there's only a blinking cursor.
   * @since TotalCross 1.3
   */
  public int focusColor = -1;

  /** Use the NumericBox instead of the Calculator in all Edits that have mode equal to CURRENCY.
   * Note that you can set for each control by calling <code>ed.setKeyboard(Edit.KBD_NUMERIC)</code>.
   * @since TotalCross 1.3
   */
  public static boolean useNumericBoxInsteadOfCalculator;

  protected byte kbdType = KBD_DEFAULT;

  /** Set to false if you don't want the cursor to blink when the edit is not editable */
  public boolean hasCursorWhenNotEditable = true; // guich@340_23

  /** If set to true, the text will be auto-selected when the focus enters.
   * True by default on penless devices. */
  public boolean autoSelect = Settings.keyboardFocusTraversable; // guic@550_20

  /** Keep selection of last character */
  public boolean selectLast;
  /** Keep the selection persistent; otherwise, it is reset if you change the letter */
  public boolean persistentSelection;

  /** No keyboard will be popped up for this Edit */
  public static final byte KBD_NONE = 0;
  /** The default keyboard for the current mode will be used for this Edit */
  public static final byte KBD_DEFAULT = 1;
  /** The Keyboard class (or the internal virtual keyboard) will be used for this Edit. 
   * To change the window colors that may open, make sure to see the UIColors static attributes.*/
  public static final byte KBD_KEYBOARD = 2;
  /** The Calculator will be used for this Edit. 
   * To change the window colors that may open, make sure to see the UIColors static attributes.*/
  public static final byte KBD_CALCULATOR = 3;
  /** The Calendar will be used for this Edit. 
   * To change the window colors that may open, make sure to see the UIColors static attributes.*/
  public static final byte KBD_CALENDAR = 4;
  /** The NumericBox will be used for this Edit */
  public static final byte KBD_NUMERIC = 5;
  /** The TimeBox will be used for this Edit */
  public static final byte KBD_TIME = 6;

  /** to be used in the setValidChars method */
  public static final String numbersSet = "0123456789";
  /** to be used in the setValidChars method */
  public static final String currencyCharsSet = "0123456789.+-";
  /** to be used in the setValidChars method */
  public static final String dateSet = numbersSet + Settings.dateSeparator;

  /** to be used in the setMode method */
  public static final byte NORMAL = 0;
  /** to be used in the setMode method */
  public static final byte DATE = 1;
  /** to be used in the setMode method */
  public static final byte CURRENCY = 2;
  /** to be used in the setMode method. The last char will be always shown */
  public static final byte PASSWORD = 3;
  /** to be used in the setMode method. All chars are replaced by '*' */
  public static final byte PASSWORD_ALL = 4;

  /** to be used in the capitalise property */
  public static final byte ALL_NORMAL = 0;

  /** to be used in the capitalise property */
  public static final byte ALL_UPPER = 1;

  /** to be used in the capitalise property */
  public static final byte ALL_LOWER = 2;

  /** Defines if this Edit can behave as with virtual keyboard or not. 
   * @since TotalCross 1.6
   */
  public boolean virtualKeyboard = Settings.virtualKeyboard;

  /** Cursor thickness */
  public static int cursorThickness = Math.max(Settings.screenWidth, Settings.screenHeight) > 2000 ? 4
      : Math.max(Settings.screenWidth, Settings.screenHeight) > 1500 ? 3
          : Math.max(Settings.screenWidth, Settings.screenHeight) > 700 ? 2 : 1;

  // changes for material design
  /** Font used for the caption when hovering above the edit */
  
  protected FloatingLabel<Edit> materialCaption;
  
  /** Construct an Edit with FILL as preferred width. Note that you cannot use RIGHT or CENTER at the x coordinate if you use this constructor. */
  public Edit() {
	blinkTime = 0;
	backColor = 0xFFFFFF;
	captionIconPadding = UnitsConverter.toPixels(DP + 12);
	if(uiAndroid) {
		try {
			setNinePatch(Resources.edit.scaledBy(Settings.screenDensity/4, Settings.screenDensity/4));
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    clearPosState();
    useFillAsPreferred = true;
    if (uiMaterial) {
      this.setFont(Font.getFont(this.getFont().name, this.getFont().isBold(), 16));
      materialCaption = new FloatingLabel<>(this);
    }
    onFontChanged();
  }
  
  /** Construct an Edit with the default width computed based in the specified
   * mask and in the control's font. In order to allow the mask to be used as
   * a real mask, you must call the setMode method.
   * If mask is "", the FILL width is choosen.
   * @see #setMode(byte, boolean)
   */
  public Edit(String mask) {
    this();
    this.mask = mask.toCharArray();
    this.isMaskedEdit = true;
    useFillAsPreferred = mask.length() == 0;
  }

  /** Maps the keys in the from char array into the keys in the to char array. For example enable a 'numeric pad'
   * on devices that has the 1 in the u character, you can use this:
   * <pre>
   * ed.mapKeys("uiojklnm!.","1234567890");
   * </pre>
   * To make sure that lowercase characters are also handled, you should also change the capitalise mode:
   * <pre>
   * ed.capitalise = Edit.ALL_LOWER;
   * </pre>
   * If you want to disable a set of keys, use the setValidChars method. Note that mapKeys have precendence over setValidChars.
   * @param from The source keys. Must have the same length of <code>to</code>. Set to null to disable mapping.
   * @param to The destination keys. Must have the same length of <code>from</code>
   * @since TotalCross 1.01
   * @see #setValidChars(String)
   */
  public void mapKeys(String from, String to) {
    if (from == null || to == null) {
      from = to = null;
    } else if (from.length() != to.length()) {
      throw new IllegalArgumentException("from.length must match to.length");
    }
    this.mapFrom = from;
    this.mapTo = to;
  }
  
  /** Returns the materialCaption. Returns null if Material Design is not set.
   * */
  public FloatingLabel<Edit> getMaterialCaption() {
	  return materialCaption;
  }
  
  /** Set the number of decimal placed if a masked edit with CURRENCY mode. Default is 2 decimal places.
   * It cannot be used with masked Edits; pass the number of decimal places in the mask itself.
   * The only exception is when you want to use the default CURRENCY mask, passing a null mask in the constructor;
   * in this situation, you can call setDecimalPlaces before calling setMode, and a mask will be constructed with
   * the given number of decimals.
   */
  public void setDecimalPlaces(int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count must be >= 0");
    }
    if (isMaskedEdit) {
      throw new RuntimeException("Edit.setDecimalPlaces can't be used after the mask is applied using setMode.");
    }
    this.decimalPlaces = count;
  }

  /** Returns the number of decimal places. */
  public int getDecimalPlaces() {
    return decimalPlaces;
  }

  /** Used to change the default keyboard to be used with this Edit control.
   * To change the window colors that may open, make sure to see the UIColors static attributes.
   * Note that setMode calls setKeyboard(KBD_DEFAULT), so be sure to set the mode before calling setKeyboard.
   * @see #KBD_NONE
   * @see #KBD_DEFAULT
   * @see #KBD_KEYBOARD
   * @see #KBD_CALCULATOR
   * @see #KBD_CALENDAR
   * @see #KBD_NUMERIC
   * @see #KBD_TIME
   * @see #useNumericBoxInsteadOfCalculator
   */
  public void setKeyboard(byte kbd) // guich@310_19
  {
    this.kbdType = kbd;
    if (kbd == KBD_DEFAULT) {
      switch (mode) {
      case DATE:
        kbdType = KBD_CALENDAR;
        break;
      case CURRENCY:
        kbdType = useNumericBoxInsteadOfCalculator ? KBD_NUMERIC : KBD_CALCULATOR;
        break;
      default:
        kbdType = KBD_KEYBOARD;
        break;
      }
    }
  }

  /** Returns the keyboard type of this Edit control.
   * @see #KBD_NONE
   * @see #KBD_DEFAULT
   * @see #KBD_KEYBOARD
   * @see #KBD_CALCULATOR
   * @see #KBD_CALENDAR
   * @see #KBD_NUMERIC
   * @see #KBD_TIME
   * @since SuperWaba 5.67
   */
  public byte getKeyboardType() // guich@567_6
  {
    return kbdType;
  }

  @Override
  protected void onFontChanged() {
    wildW = fm.charWidth('*');
  }

  /** Returns the mask passed on the constructor. */
  public String getMask() {
    return mask == null ? "" : new String(mask);
  }

  /** Used to set the valid characters that can be entered by using one of the mode constants,
   * <b>without</b> masking. To enable masking the input, you have to call
   * the setMode method passing the mode and <code>true</code> as parameter.
   * Note that setMode calls setKeyboard(KBD_DEFAULT), so be sure to set the mode before calling setKeyboard.
   * @see #NORMAL
   * @see #DATE
   * @see #CURRENCY
   * @see #PASSWORD
   * @see #PASSWORD_ALL
   */
  public void setMode(byte mode) {
    setMode(mode, false);
  }

  /** Return the current mode.
   * @since TotalCross 1.27
   */
  public int getMode() {
    return mode;
  }

  /** Used to set the valid characters that can be entered by using one of the mode constants,
   * optionally enabling the mask to be applied to the input.
   * Note that setMode calls setKeyboard(KBD_DEFAULT), so be sure to set the mode before calling setKeyboard.
   * @see #NORMAL
   * @see #DATE
   * @see #CURRENCY
   * @see #PASSWORD
   * @see #PASSWORD_ALL
   */
  public void setMode(byte mode, boolean maskedEdit) {
    this.mode = mode;
    this.isMaskedEdit = maskedEdit;
    this.useFillAsPreferred = this.useFillAsPreferred && !maskedEdit;
    switch (mode) {
    case DATE:
      setValidChars(maskedEdit ? numbersSet : (numbersSet + Settings.dateSeparator));
      if (maskedEdit) {
        maxLength = 8;
        mask = Settings.dateFormat == Settings.DATE_YMD
            ? ("9999" + Settings.dateSeparator + "99" + Settings.dateSeparator + "99").toCharArray()
            : ("99" + Settings.dateSeparator + "99" + Settings.dateSeparator + "9999").toCharArray();
      }
      break;
    case CURRENCY:
      setValidChars(currencyCharsSet);
      if (maskedEdit) {
        if (mask == null || mask.length == 0) {
          mask = getDefaultCurrencyMask(decimalPlaces);
        }
        applyMaxLengthBasedOnMask();
        alignment = RIGHT;
      }
      break;
    default:
      setValidChars(null);
      if (maskedEdit && mask != null && mask.length > 0) {
        applyMaxLengthBasedOnMask();
      }
      break;
    }
    if (kbdType != KBD_NONE) {
      setKeyboard(KBD_DEFAULT);
    }
  }

  public static char[] getDefaultCurrencyMask(int decimalPlaces) {
    String s = decimalPlaces == 0 ? "999a999a999a999a999" : ("999a999a999a999a999b" + Convert.dup('9', decimalPlaces));
    s = s.replace('a', Settings.thousandsSeparator);
    s = s.replace('b', Settings.decimalSeparator);
    return s.toCharArray();
  }

  private void applyMaxLengthBasedOnMask() {
    int nines = 0;
    for (int i = 0; i < mask.length; i++) {
      if (mask[i] == '9') {
        nines++;
      } else if (mode == CURRENCY && mask[i] == Settings.decimalSeparator) {
        decimalPlaces = mask.length - i - 1;
      }
    }
    maxLength = nines;
  }

  /** Sets the valid chars that can be entered in this edit
   * (they are converted to uppercase to make the verification easy).
   * if null is passed, any char can be entered. The chars are case insensitive.
   * If you pass "" (empty string), no chars will be able to be inputted, and movement, 
   * delete and copy/paste operations will also be disabled. 
   * @see #mapKeys
   */
  public void setValidChars(String validCharsString) {
    if (validCharsString != null) {
      validChars = validCharsString.toUpperCase();
    } else {
      validChars = null;
    }
  }

  /** Return true if the given char exists in the set of valid characters for this Edit */
  protected boolean isCharValid(char c) {
    return validChars == null || validChars.indexOf(Convert.toUpperCase(c)) != -1;
  }

  /** Sets the desired maximum length for text entered in the Edit.
   * Does nothing if the edit has a mask.
   @since SuperWaba 2.0 beta 4 */
  public void setMaxLength(int length) {
    //if (!isMaskedEdit) // guich@tc115_83: ignore if using masks
    {
      maxLength = length;
      if (length != 0 && maxLength < chars.length()) {
        chars.setLength(length);
      }
    }
  }

  public int getMaxLength() {
    return maxLength;
  }

  private void clearPosState() {
    insertPos = 0;
    startSelectPos = -1;
    xOffset = xMins[Settings.uiStyle];
  }

  protected int pushedInsertPos;
  protected int pushedStartSelectPos;
  protected int pushedxOffset;

  protected void pushPosState() {
    pushedInsertPos = insertPos;
    pushedStartSelectPos = startSelectPos;
    pushedxOffset = xOffset;
  }

  protected void popPosState() {
    int len = chars.length();
    insertPos = Math.min(len, pushedInsertPos); // guich@571_5: make sure the insert position isn't bigger than the size of the text.
    startSelectPos = Math.min(len, pushedStartSelectPos); // guich@571_5
    xOffset = pushedxOffset;
  }

  protected int getX0() {
	int textStartX = 0;
	StringBuffer s;
	s = isMaskedEdit ? masked : chars;
	switch(alignment) {
	case RIGHT:
		textStartX = this.width - fm.sbWidth(s, 0, s.length() ) - 2*xOffset;
		if(captionIcon != null) {
			textStartX -= (captionIcon == null ? 0 : captionIcon.getWidth() + fmH / 4);
		}
		break;
	case CENTER:
		textStartX = (this.width - fm.sbWidth(s, 0, s.length())) / 2 - xOffset;
		if(captionIcon != null) {
			textStartX -= (captionIcon == null ? 0 : captionIcon.getWidth() + fmH / 4);
		}
		if(getTotalCharWidth() > xMax - xMin - (captionIcon == null ? 0 : captionIcon.getWidth() + fmH / 4)) {
			textStartX = this.width - fm.sbWidth(s, 0, s.length() ) - 2*xOffset;
			if(captionIcon != null) {
				textStartX -= (captionIcon == null ? 0 : captionIcon.getWidth() + fmH / 4);
			}
		}
		break;
	}
    return (captionIcon == null ? 0 : captionIcon.getWidth() + fmH / 4) + textStartX;
  }

  protected int charPos2x(int n) {
    int extra = getX0();
    if (!isMaskedEdit) {
      if (n == 0) {
        return extra + xOffset;
      }
      if (n >= chars.length()) {
        return extra + xOffset + getTotalCharWidth();
      }
    } else if (n > chars.length()) {
      n = chars.length();
    }
    switch (mode) {
    case PASSWORD_ALL:
      return extra + xOffset + wildW * n;
    case PASSWORD:
      return extra + xOffset + wildW * (n - 1) + fm.charWidth(chars, chars.length() - 1);
    case CURRENCY:
      if (isMaskedEdit) // in currency, we go from right to left
      {
        int xx = xMax, i, pos = masked.length();
        n = chars.length() - n;
        for (i = n; i > 0 && --pos >= 0;) {
          char c = masked.charAt(pos);
          xx -= fm.charWidth(c);
          if ('0' <= c && c <= '9') {
            i--;
          }
        }
        return extra + xx;
      } else {
        break;
      }
    default://case DATE:
      if (masked.length() > 0) {
        int i = 0, pos = 0;
        for (; i < n; pos++) {
          if (pos >= mask.length) {
            break;
          } else if (mask[pos] == '9') {
            i++;
          }
        }
        while (pos < mask.length && mask[pos] != '9') {
        	pos++; // skip next non-numeric chars
          if(mask[pos - 1] == ' ' && masked.length() == pos - 1) {
        	  masked.append(" ");
          }
        }
        return extra + xOffset + fm.sbWidth(masked, 0, Math.min(pos, masked.length()));//Math.min(pos,masked.length())); // guich@tc152: changed mask to masked, otherwise, using old font and 1's will make the cursor appear incorrectly
      }
    }
    return extra + xOffset + fm.sbWidth(chars, 0, n);
  }

  /** Returns the text displayed in the edit control. If masking is enabled, the text with the mask is returned;
   * to get the text without the mask, use the getTextWithoutMask method.
   * @see #getTextWithoutMask()
   */
  @Override
  public String getText() {
    return isMaskedEdit ? masked.toString() : chars.toString();
  }

  /** Returns the text without the mask. For non-currency mode, only chars whose corresponding mask is '9' are returned.
   * @see #getText()
   */
  public String getTextWithoutMask() {
    if (chars.length() == 0) {
      return "";
    }
    String str = chars.toString();
    if (isMaskedEdit) {
      if (mode == CURRENCY) {
        if (!hasSignificantDigits()) {
          if (str.indexOf('.') < 0 && str.indexOf(',') < 0) {
            str = Convert.toString(0d, decimalPlaces);//"0";
          }
        } else {
          if (decimalPlaces > 0) // for currency mode, remove the , and . and put it in Java's format (xxxx.yy)
          {
            int k = str.length() - decimalPlaces; // get the number of decimal places
            if (k <= 0) {
              str = "0.".concat(Convert.zeroPad(str, decimalPlaces));
            } else {
              str = str.substring(0, k) + "." + str.substring(k);
            }
          }
          if (isNegative) {
            str = "-".concat(str);
          }
        }
      } else {
        StringBuffer sbuf = new StringBuffer(str.length());
        if (mask.length == str.length()) // totally formatted? faster algorithm
        {
          // 25/03/1970 -> 25031970
          for (int i = 0; i < mask.length; i++) {
            if (mask[i] == '9') {
              sbuf.append(chars.charAt(i));
            }
          }
        } else {
          // 25031970 -> 25031970
          int max = chars.length();
          for (int i = 0, j = 0; i < mask.length; i++) {
            if ('0' <= mask[i] && mask[i] <= '9' && j < max) {
              sbuf.append(chars.charAt(j++));
            }
          }
        }
        str = sbuf.toString();
      }
    }
    return str;
  }

  /** Returns the text's buffer. Do NOT change the buffer contents, since changing it
   * will not affect the char widths array, thus, leading to a wrong display.
   * @since TotalCross 1.0
   */
  public StringBuffer getTextBuffer() {
    return chars;
  }

  String backup;
  public void setText(String s, boolean postPressed) {
	if (this.setX == Control.SETX_NOT_SET) {
		backup = s;
		return;
	}
	
    int len, dot, decimals;
    chars.setLength(0);
    if (s != null && (len = s.length()) > 0) {
      chars.append(s);
      if (mode == CURRENCY && isMaskedEdit) // correct the number if this is a numeric edit
      {
        isNegative = s.startsWith("-");
        if (isNegative) {
          len--;
          s = s.substring(1);
          chars.setLength(0);
          chars.append(s);
        } // guich@tc168 - if user sends a negative value, remove it from start and set the flag
        if (s.indexOf(',') >= 0 || Convert.numberOf(s, '.') > 1) {
          s = Convert.replace(s, ".", "").replace(',', '.');
        }

        dot = s.indexOf('.');
        decimals = len - dot - 1;
        if (decimalPlaces == 0) // setText("12.34") -> "12"
        {
          if (dot >= 0) {
            chars.setLength(dot); // cut
          }
        } else if (dot < 0) {
          chars.append(Convert.dup('0', decimalPlaces));
        } else {
          if (decimals == decimalPlaces) {
            ;
          } else if (decimals > decimalPlaces) {
            chars.setLength(len - (decimals - decimalPlaces));
          } else {
            chars.append(Convert.dup('0', decimalPlaces - decimals));
          }
          int delpos = len - decimals - 1;
          chars.delete(delpos, delpos + 1); // remove the . - guich@tc100b4_11: added +1
        }
      } else if (mode != CURRENCY && isMaskedEdit && chars.length() >= mask.length) {
        String unmasked = getTextWithoutMask();
        chars.setLength(0);
        chars.append(unmasked);
      }
    }
    applyMaskToInput();
    clearPosState();
    Window.needsPaint = true;
    if (postPressed) {
      postPressedEvent();
    }
    fireValueChangeEvent();
  }
  
  void fireValueChangeEvent() {
    ValueChangeEvent<String> e = new ValueChangeEvent<String>(this, this.getValue());
    e.target = this;
    postEvent(e);
  }

  /**
   * Sets the text displayed in the edit control.
   * If you're setting the text in CURRENCY mode,
   * the text must be set <b>not</b> formatted (unmasked).
   */
  @Override
  public void setText(String s) {
    setText(s, Settings.sendPressEventOnChange);
  }

  /** Sets if the control accepts input from the user.
   * If set to false, you must explicitly call the clear method of this edit.
   */
  public void setEditable(boolean on) {
    focusTraversable = editable = on;
  }

  /** Gets if the control accepts input from the user */
  public boolean isEditable() {
    return editable;
  }

  public Rect oldBounds;
  @Override
  protected void onBoundsChanged(boolean screenChanged) // guich
  {
	xMin = xMins[Settings.uiStyle];
    if(captionIconHeightFactor != 0 && captionIcon != null) {
    	try {
			captionIcon = captionIcon.smoothScaledFixedAspectRatio(height * captionIconHeightFactor / 100, true);
		} catch (ImageException e) {
			e.printStackTrace();
		}
    }
    if (captionIconHeightFactor != 0 && captionIcon != null) {
		try {
			captionIcon = captionIcon.hwScaledFixedAspectRatio(height * captionIconHeightFactor / 100, true);
			if (captionIcon.getWidth() > this.width - fm.stringWidth(chars.toString())) {
				captionIcon = captionIcon.hwScaledFixedAspectRatio(width - fm.stringWidth(chars.toString()), false);
			}
		} catch (Throwable t) {
		}
	}
    xMax = this.width - xMin;
    gap = hasBorder ? (xMin >> 1) : 0;
    npback = null;
    
    // material
    if (this.getRect().equals(oldBounds)) {
    	return;
    } else {
    	oldBounds = this.getRect();
	    if (materialCaption != null) {
	      materialCaption.xcap0 = materialCaption.xcap = chars.length() == 0 ? xMin : 0;
	      materialCaption.ycap0 = materialCaption.ycap = uiMaterial ? this.height/2  - this.fmH/2 : chars.length() == 0 ?  getTextY() : 0;
	      if (this instanceof OutlinedEdit) {
	    	  OutlinedEdit oe = (OutlinedEdit)this;
			int labelAscentMiddleY = (materialCaption.getCaptionFontSmall().fm.ascent - oe.borderHeight)/2;
		   	materialCaption.ycap0 += labelAscentMiddleY;
		   	materialCaption.ycap += labelAscentMiddleY;
	      }
	    }
    }
  }

  @Override
  public int getPreferredWidth() {
	if(uiMaterial) {
		return UnitsConverter.toPixels(DP + 280);
	}
    return (mask == null || useFillAsPreferred) ? FILL
        : (fm.stringWidth(new String(mask)) + (uiAndroid ? 10 : (uiFlat || uiVista) ? 8 : 4)); // guich@200b4_202: from 2 -> 4 is PalmOS style - guic@300_52: empty mask means FILL - guich@570_88: fixed width when uiFlat
  }

  @Override
  public int getPreferredHeight() {
    int ret = fmH + prefH;
    if (uiMaterial) {
      if(caption != null) {
    	  ret = fmH + prefH > UnitsConverter.toPixels(DP + 56) ? ret + materialCaption.getExtraHeight() : UnitsConverter.toPixels(DP + 56);
      } else {
    	  ret = UnitsConverter.toPixels(DP + 56);
      }
    }
    return ret;
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    npback = null;
    fColor = getForeColor();
    back0 = UIColors.sameColors ? backColor : Color.brighter(getBackColor()); // guich@572_15
    back1 = back0 != Color.WHITE ? (UIColors.sameColors ? Color.darker(getBackColor()) : backColor)
        : Color.getCursorColor(back0);//guich@300_20: use backColor instead of: back0.getCursorColor();
    if (!uiAndroid) {
      Graphics.compute3dColors(isEnabled(), backColor, foreColor, fourColors);
    }
  }

  protected int getTotalCharWidth() {
    int len = chars.length();
    switch (mode) {
    case PASSWORD_ALL:
      return len == 0 ? 0 : wildW * len;
    case PASSWORD:
      return len == 0 ? 0 : wildW * (len - 1) + fm.charWidth(chars, len - 1);
    default:
      if (isMaskedEdit) {
        int pos = masked.length();
        int n = Math.max(pos, len), i;
        int ww = 0;
        for (i = n; i > 0 && --pos >= 0;) {
          char c = masked.charAt(pos);
          ww += fm.charWidth(c);
          if ('0' <= c && c <= '9') {
            i--;
          }
        }
        return ww;
      } else {
        return fm.sbWidth(chars, 0, len);
      }
    }
  }

  protected int getTextY() {
    int y = this.height - fmH - gap;
    if (uiAndroid) {
      y--;
    }
    if (uiHolo) {
      y = (height - fmH - gap) / 2;
    }
    if (uiMaterial) {
      y = materialCaption.ycap0;
    }
    return y;
  }

	protected void draw(Graphics g) {
		if (g == null || !isDisplayed()) {
			return; // guich@tc114_65: check if its displayed
		}

		boolean uiAndroid = Control.uiAndroid || uiHolo;
		int y = getTextY();

		// background
		g.backColor = back0;
		if (!transparentBackground) {
			try {
	  			if(npParts != null && (npback == null || focusColor != -1)) {
					npback = NinePatch.getInstance().getNormalInstance(npParts, width, height,
							isEnabled() ? hasFocus && focusColor != -1 ? focusColor : back0
									: (back0 == parent.backColor ? Color.darker(back0, 32)
											: Color.interpolate(back0, parent.backColor)),
							false);
					npback.alphaMask = alphaValue;
				}
				NinePatch.tryDrawImage(g, npback, 0, 0);
			} catch (ImageException e) {
				e.printStackTrace();
			}
		}

	      if(drawLine) {
			if (uiMaterial) {
				final int lineHeight = Math.max(UnitsConverter.toPixels(DP + (hasFocus ? 2 : 1)), 1);
				int h = fmH / 10;
				if (h < 2) {
					h = 2;
				}
				if (focusColor != -1 && hasFocus) {
					g.backColor = focusColor;
					g.fillRect(0, 0, width, height - h);
				}
				int c;
	          if(hasFocus) {
					c = captionColor != -1 ? captionColor : foreColor;
				} else {
					c = captionColor != -1 ? captionColor : Color.getRGB("7f7f7f");
				}
				if (isEnabled()) {
					if (fillColor != -1) {
						g.backColor = fillColor;
						g.fillRect(0, 0, width, height - h);
					}
					g.backColor = c;
					int leftPadding = 0;
					int rightPadding = 0;
	      	  if(npback != null ) {
	      		  byte[] colors = new byte[npback.getWidth()*4];
	      		  npback.getPixelRow(colors, height - UnitsConverter.toPixels(DP + (hasFocus ? 2 : 1)));
						int npbackWidth = npback.getWidth();
	      		  for(int i = 0; i < npbackWidth; i++) {
	      			  if(colors[i*4+3] != -1){
								leftPadding += i;
							}
						}
	      		  for(int i = npback.getWidth() - 1; i >0 ; i--) {
	      			  if(colors[i*4+3] != -1){
								rightPadding += i;
							}
						}
					}
					if (leftPadding < -1 || leftPadding > width) {
						leftPadding = rightPadding = 0;
					}
					g.fillRect(leftPadding, height - lineHeight, width - (leftPadding + rightPadding), lineHeight);
				} else {
					g.foreColor = c;
					g.backColor = Color.getGray(getForeColor());
					for (int i = 0; i < h; i++) {
						g.drawDots(i & 1, height - 1 - i, width, height - 1 - i);
					}
				}
			} else {
				int gg = gap;
				if (uiAndroid) {
					g.backColor = parent.backColor;
					gg = 0;
				}
				if (!uiAndroid || !hasBorder) {
					g.fillRect(gg, gg, this.width - (gg << 1), this.height - (gg << 1));
				}
			}
		}

		// draw the text and/or the selection
		int len = chars.length();
		boolean drawCaption = caption != null && !hasFocus && len == 0;
		if (len > 0 || drawCaption || captionIcon != null) {
			if ((selectLast || startSelectPos != -1) && editable) // moved here to avoid calling g.eraseRect (call
																	// fillRect instead) - guich@tc113_38: only if
																	// editable
			{
				// character regions are:
				// 0 to (sel1-1) .. sel1 to (sel2-1) .. sel2 to last_char
				int sel1 = selectLast ? insertPos - 1 : Math.min(startSelectPos, insertPos);
				int sel2 = selectLast ? insertPos : Math.max(startSelectPos, insertPos);
				int sel1X = charPos2x(sel1);
				int sel2X = charPos2x(sel2);

				if (sel1X != sel2X) {
					int old = g.backColor;
					g.backColor = back1 == backColor ? Color.brighter(back1) : back1;
					g.fillRect(sel1X, y, sel2X - sel1X + 1, fmH);
					g.backColor = old;
				}
			}

			g.foreColor = fColor;
			int xx = xOffset;
			if (captionIcon != null) {
				xx += getX0();
				g.drawImage(captionIcon, uiMaterial ? captionIconPadding : fmH,
						captionIconHeightFactor == 0 ? y : height / 2 - captionIcon.getHeight() / 2);
			}

	        if (!drawCaption) {
	          switch (alignment) {
	          case RIGHT:
	            xx = this.width - getTotalCharWidth() - xOffset;
	            break;
	          case CENTER:
	            xx = (this.width - getTotalCharWidth()) >> 1;
					if(getTotalCharWidth() > xMax - xMin - (captionIcon == null ? 0 : captionIcon.getWidth() + fmH / 4)) {
						xx = this.width - getTotalCharWidth() - xOffset;
					}
	            break;
	          }
	        }
	        if (hasBorder) {
	          g.setClip(xMin + (captionIcon != null ? captionIcon.getWidth() : 0), 0, xMax - Edit.prefH, height);
	        }
	        if (drawCaption && !uiMaterial) {
	          g.foreColor = captionColor != -1 ? captionColor : this.foreColor;
	          g.drawText(caption, xx, y, textShadowColor != -1, textShadowColor);
	        } else {
	          switch (mode) {
				case PASSWORD: // password fields usually have small text, so this method does not have to be
								// very optimized
					if (len > 0) {
						g.drawText(Convert.dup('*', len - 1) + chars.charAt(len - 1), xx,
								y + (uiMaterial ? materialCaption.ycap0 - y : 0), textShadowColor != -1,
								textShadowColor);
					}
					break;
				case PASSWORD_ALL:
					g.drawText(Convert.dup('*', len), xx, y + (uiMaterial ? materialCaption.ycap0 - y : 0),
							textShadowColor != -1, textShadowColor);
					break;
				case CURRENCY:
					if (isMaskedEdit) {
						switch (alignment) {
						case RIGHT:
							xx = this.width - getTotalCharWidth() - xOffset;
							break;
						case CENTER:
							xx = (this.width - getTotalCharWidth()) >> 1;
							break;
						}

					}
				default:
					int textY = y;// + (uiMaterial ? materialCaption.ycap0 - y : 0);
//					
					if (masked.length() > 0) {
						g.drawText(masked, 0, masked.length(), xx, textY, textShadowColor != -1, textShadowColor);
					} else {
						g.drawText(chars, 0, len, xx, textY, textShadowColor != -1, textShadowColor);// xx wrong in this case?
					}
				}
			}
			if (hasBorder) {
				g.clearClip();
			}
		}
		if (hasBorder && !uiAndroid) {
			g.draw3dRect(0, 0, this.width, this.height, Graphics.R3D_EDIT, false, false, fourColors); // draw the border
																										// and erase the
																										// rect
		}
		cursorX = charPos2x(insertPos);
		if (hasFocus && isEnabled() && (editable || hasCursorWhenNotEditable)) // guich@510_18: added check to see if it
																				// is enabled
		{
			// draw cursor
			if (xMin <= cursorX && cursorX <= xMax) // guich@200b4_155
			{
				if (cursorShowing) {
					g.clearClip();
					g.backColor = Color.interpolate(backColor, foreColor);
					g.fillRect(cursorX - 1 + (uiMaterial ? UnitsConverter.toPixels(DP + 2) : 0),
							uiMaterial ? materialCaption.ycap0 + font.fm.descent : y, cursorThickness,
							fmH - font.fm.descent);
				}
			}
		} else {
			cursorShowing = false;
		}

		// material
		if (uiMaterial) {
			int c;
	        if(hasFocus) {
				c = captionColor != -1 ? captionColor : foreColor;
			} else {
				c = captionColor != -1 ? captionColor : Color.getRGB("7f7f7f");
			}
			g.foreColor = c;
			g.setFont(materialCaption.getFcap());
			g.drawText(caption, materialCaption.xcap + (captionIcon == null ? 0 : captionIcon.getWidth() + fmH / 4), materialCaption.ycap);
		}
	}

  private void applyMaskToInput() {
    int len = chars.length();
    StringBuffer masked = this.masked; // cache instance field
    masked.setLength(0);
    if (len == 0) {
      isNegative = false;
    } else if ((mode == DATE || mode == NORMAL) && isMaskedEdit) // date must go forward
    {
      int n = Math.min(len, mask.length), i = 0, pos = 0;
      while (i < n) {
        if (pos >= mask.length) {
          break;
        } else if (mask[pos] == '9') {
          masked.append(chars.charAt(i++));
          pos++;
        } else {
          masked.append(mask[pos]);
          if (mask[pos] == chars.charAt(i)) {
            i++;
          }
          pos++;
        }
      }
      if (pos < mask.length && mask[pos] != '9') {
        masked.append(mask[pos]);
      }
    } else if (mode == CURRENCY && isMaskedEdit && len > 0) // currency must go backward
    {
      for (int i = len - 1, pos = mask.length - 1; i >= 0 && pos >= 0;) {
        if (mask[pos] == '9') {
          masked.append(chars.charAt(i--));
          pos--;
        } else {
          masked.append(mask[pos]);
          if (chars.charAt(i) == mask[pos]) {
            i--;
          }
          pos--;
        }
      }
      if (hasSignificantDigits()) {
        if (decimalPlaces > 0) {
          int k = masked.length() - decimalPlaces; // get the number of decimal places
          if (k <= 0) {
            masked.append(Convert.zeroPad("", -k)).append(Settings.decimalSeparator).append('0');
          }
        }
        if (isNegative) {
          masked.append('-');
        }
      } else {
        isNegative = false;
      }
      masked.reverse();
    }
  }

  private boolean hasSignificantDigits() {
    for (int i = chars.length() - 1; i >= 0; i--) {
      if (chars.charAt(i) != '0') {
        return true;
      }
    }
    return false;
  }

  /** Sets the selected text of this Edit (if start != end). Can be used to set the cursor position,
   * if start equals end. Start must be less or equal to end, and both must be >= 0.
   * It can also be used to clear the selectedText, calling <code>setCursorPos(-1,0)</code>.
   * Note: if you're setting the cursor position before the edit is drawn for the first
   * time, the edit will not be scrolled if the end position goes beyond the limits.
   * Important! No bounds checking is made. Be sure to not call this method with invalid positions!
   * Example:
   * <pre>
   * ed.setText("1234567890123456");
   * ed.setCursorPos(3,14);
   * ed.requestFocus();
   * </pre>
   */
  public void setCursorPos(int start, int end) // guich@400_18
  {
    startSelectPos = (start != end) ? start : -1;
    insertPos = end;
    if (cursorChangedEvent == null) {
      cursorChangedEvent = new ControlEvent(ControlEvent.CURSOR_CHANGED, this);
    }
    onEvent(cursorChangedEvent);
    Window.needsPaint = true;
  }

  /** Sets the cursor position */
  public void setCursorPos(int pos) // guich@400_18
  {
    setCursorPos(pos, pos);
  }

  /** Returns an array with the cursor positions. You can use it with getText
   * to find the selected text String.
   * E.g.:
   * <pre>
   * int []cursorPos = ed.getCursorPos();
   * int start = cursorPos[0];
   * int end = cursorPos[1];
   * String text = ed.getText();
   * if (start != -1) // is the text selected?
   * {
   *    String selectedText = text.substring(start,end);
   *    ...
   * </pre>
   */
  public int[] getCursorPos() // guich@400_18
  {
    return new int[] { startSelectPos, insertPos };
  }

  /** User method to popup the keyboard/calendar/calculator for this edit. */
  public void popupKCC() {
    if (kbdType == KBD_NONE || !editable || !isEnabled()) {
      return;
    }
    if (!popupsHidden()) {
      // check if the keyboard is already popped up
      if (Settings.fingerTouch && kbdType != KBD_TIME && kbdType != KBD_CALENDAR && kbdType != KBD_CALCULATOR
          && kbdType != KBD_NUMERIC) {
        return;
      }
    }

    Window w = getParentWindow();
    if (w != null) {
      w.swapFocus(this);//requestFocus(); // guich@200b4: bring focus back -  guich@401_15: changed to swapFocus
    }

    switch (kbdType) {
    case KBD_TIME:
      if (time == null) {
        time = new TimeBox();
      }
      time.tempTitle = keyboardTitle;
      try {
        time.setTime(new Time(getText(), false, false, false, true, true, true));
      } catch (Exception e) {
        time.setTime(new Time(0));
        if (chars.length() > 0 && Settings.onJavaSE) {
          e.printStackTrace();
        }
      }
      hideSip();
      time.popup();

      setText(time.getTime().toString(), true);
      break;

    case KBD_CALENDAR:
      try {
    	calendar = new CalendarBox(this.foreColor, new Date(getText())); //Reinitializing the window every time with the actual date
    	calendar.tempTitle = keyboardTitle;
        calendar.setSelectedDate(new Date(getText()));
      } catch (InvalidDateException ide) {
    	  calendar = new CalendarBox(this.foreColor);	//if the date is invalid, start the date with the system date
      } // if the date is invalid, just ignore it
      hideSip();
      calendar.popupNonBlocking();
      break;

    case KBD_CALCULATOR:
      if (useNativeNumericPad) {
        showVirtualKeyboard();
      } else {
        if (calculator == null) {
          calculator = new CalculatorBox();
        }
        calculator.rangeCheck = this.rangeCheck;
        calculator.tempTitle = keyboardTitle;
        calculator.optionalValue = optionalValue4CalculatorBox;
        hideSip();
        calculator.popupNonBlocking();
      }
      break;

    case KBD_NUMERIC:
      if (useNativeNumericPad) {
        showVirtualKeyboard();
      } else {
        if (numeric == null) {
          numeric = new CalculatorBox(false);
        }
        numeric.rangeCheck = this.rangeCheck;
        numeric.tempTitle = keyboardTitle;
        numeric.optionalValue = optionalValue4CalculatorBox;
        hideSip();
        numeric.popupNonBlocking();
      }
      break;

    default:
      showVirtualKeyboard();
    }
  }

  private static boolean lastWasNumeric;

  private void showVirtualKeyboard() {
    if (!Settings.enableVirtualKeyboard) {
      ;
    } else if (virtualKeyboard && editable && !"".equals(validChars)) {
      if (Settings.customKeyboard != null) {
        Settings.customKeyboard.show(this, validChars);
      } else {
        shiftScreen(false);
        boolean isNumeric = useNativeNumericPad && kbdType == KBD_NUMERIC;
        if (!Window.isSipShown() || lastWasNumeric != isNumeric) {
          lastWasNumeric = isNumeric;
          int sbl = Settings.SIPBottomLimit;
          if (sbl == -1) {
            sbl = Settings.screenHeight / 2;
          }
          boolean onBottom = Settings.unmovableSIP || getAbsoluteRect().y < sbl;
          Window.setSIP(onBottom ? Window.SIP_BOTTOM : Window.SIP_TOP, this, isNumeric); // if running on a PocketPC device, set the bounds of Sip in a way to not cover the edit
        }
      }
    } else {
		keyboard = VirtualKeyboard.getInstance(this);
		keyboard.setValidChars(validChars);
		keyboard.tempTitle = keyboardTitle;
		showInputWindow(keyboard);
    }
  }

  protected void shiftScreen(boolean force) {
    if (Settings.unmovableSIP && (force || !Window.isSipShown())) { // guich@tc126_21
      Window ww = getParentWindow();
      if (ww != null) {
        ww.shiftScreen(this, this.height - (fmH + prefH));
      }
    }
  }

  protected void hideSip() {
    lastWasNumeric = false;
    if (Window.isSipShown()) {
      Window.setSIP(Window.SIP_HIDE, null, false);
    }
  }

  private void showInputWindow(Window w) {
    oldTabIndex = parent.tabOrder.indexOf(this);
    pushPosState();
    MainWindow.getMainWindow().removeUpdateListener(blinkListener);
    w.popupNonBlocking();
    popPosState();
    requestFocus();
  }

  private void focusOut() {
    hasFocus = false;
    clearPosState();
    MainWindow.getMainWindow().removeUpdateListener(blinkListener);
  }

  /** Called by the system to pass events to the edit control. */
  @Override
  public void onEvent(Event event) {
    if (calendar != null && event.type == ControlEvent.WINDOW_CLOSED && event.target == calendar) // called from the keyboard and from the calendar
    {
      Date d = null;
      if(!calendar.canceled) {
    	  d = calendar.getSelectedDate();
      }
      if (d != null) {
        setText(d.toString(), true);
      }
      return;
    }
    boolean extendSelect = false;
    boolean clearSelect = false;
    boolean reapplyMask = false;
    int len = chars.length();
    if (len == 0) {
      insertPos = startSelectPos = 0;
    }
    int newInsertPos = insertPos;
    switch (event.type) {
    case ControlEvent.CURSOR_CHANGED:
      break;
    case TimerEvent.TRIGGERED:
      if (showKeyboardOnNextEvent) {
        event.consumed = true;
        showKeyboardOnNextEvent = false;
        popupKCC();
        return;
      }
      return;
    case ControlEvent.FOCUS_IN:
      isHighlighting = false; // guich@573_28: after closing a KCC, don't let the focus move from here.
      wasFocusIn = true; // jairocg@450_31: set it so we can validate later
      hasFocus = true;
      MainWindow.getMainWindow().addUpdateListener(blinkListener);
      if (len > 0) // guich@550_20: autoselect the text
      {
        if (autoSelect && !ignoreSelect) // guich@570_112: changed to !ignoreSelect
        {
          startSelectPos = 0;
          newInsertPos = len;
        } else if (Settings.moveCursorToEndOnFocus) {
          newInsertPos = len;
        }
      }
      break;
    case ControlEvent.FOCUS_OUT:
      if (cursorShowing) {
        Window.needsPaint = true; //draw(drawg=getGraphics(), true); // erase cursor at old insert position
      }
      newInsertPos = 0;
      focusOut();
      break;
    case KeyEvent.KEY_PRESS:
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (editable && isEnabled()) {
        KeyEvent ke = (KeyEvent) event;
        if (PreprocessKey.instance != null) {
          PreprocessKey.instance.preprocess(this, ke);
        }
        if (event.type == KeyEvent.SPECIAL_KEY_PRESS && ke.key == SpecialKeys.ESCAPE) {
          event.consumed = true; // don't let the back key be passed to the parent
        }
        if (insertPos == 0 && ke.key == ' ' && (mode == CURRENCY || mode == DATE)) // guich@tc114_34
        {
          popupKCC();
          break;
        }
        boolean moveFocus = canMoveFocus && !Settings.geographicalFocus
            && (ke.isActionKey() || ke.key == SpecialKeys.TAB);
        if (event.target == this && moveFocus) // guich@tc100b2: move to the next edit in the same container
        {
          Control next;
          if (parent != null && (next = parent.moveFocusToNextEditable(this, ke.modifiers == 0)) != null) {
            shiftTo(next);
            return;
          }
        }
        boolean loseFocus = moveFocus || ke.key == SpecialKeys.ESCAPE;
        if (event.target == this && loseFocus) {
          //isHighlighting = true; // kmeehl@tc100: set isHighlighting first, so that Window.removeFocus() wont trample Window.highlighted
          if (removeFocusOnAction) {
            Window w = getParentWindow(); // guich@tc114_32: restore the highlight to this control...
            if (w != null) // guich@tc123_16
            {
              if (w.getFocus() == this) {
                w.removeFocus();
              }
              w.setHighlighted(this);
            }
          }
          break;
        }
        // print state
        if ((ke.key == SpecialKeys.KEYBOARD_ABC || ke.key == SpecialKeys.KEYBOARD_123) && popupsHidden()) // guich@102: pop's up the keyboard
        {
          popupKCC();
          break;
        }
        boolean isControl = (ke.modifiers & SpecialKeys.CONTROL) != 0; // guich@320_46
        if (Settings.onJavaSE) // guich@tc100b4_26: if the user pressed the command and then a key, assume is a control
        {
          if (lastCommand > 0 && (Vm.getTimeStamp() - lastCommand) < 2500) {
            ke.modifiers |= SpecialKeys.CONTROL;
            isControl = true;
            lastCommand = 0;
          } else if (ke.key == SpecialKeys.COMMAND) // just a single COMMAND? break
          {
            showTip(this, Edit.commandStr, 2500, -1);
            lastCommand = Vm.getTimeStamp();
            break;
          }
        }
        if ("".equals(validChars)) {
          break;
        }
        boolean isDelete = (ke.key == SpecialKeys.DELETE);
        boolean isBackspace = (ke.key == SpecialKeys.BACKSPACE);
        boolean isPrintable = ke.key > 0 && event.type == KeyEvent.KEY_PRESS && (ke.modifiers & SpecialKeys.ALT) == 0
            && (ke.modifiers & SpecialKeys.CONTROL) == 0;
        int del1 = -1;
        int del2 = -1;
        int sel1 = startSelectPos;
        int sel2 = insertPos;
        if (sel1 > sel2) {
          int temp = sel1;
          sel1 = sel2;
          sel2 = temp;
        }
        // clipboard
        if (isControl) {
          if (0 < ke.key && ke.key < 32) {
            ke.key += 64;
          }
          ke.modifiers &= ~SpecialKeys.CONTROL; // remove control
          int key = Convert.toUpperCase((char) ke.key);
          switch (key) {
          case ' ': // guich@320_47
            setText("");
            return;
          case 'X':
          case 'C':
            if (key == 'X') {
              clipboardCut();
            } else {
              clipboardCopy();
            }
            return;
          case 'P':
          case 'V':
            clipboardPaste();
            return;
          }
        }
        if (mapFrom != null) // guich@tc110_56
        {
          int idx = mapFrom.indexOf(Convert.toLowerCase((char) ke.key));
          if (idx != -1) {
            ke.key = mapTo.charAt(idx);
          }
        }
        if (isPrintable) {
          if (capitalise == ALL_UPPER) {
            ke.key = Convert.toUpperCase((char) ke.key);
          } else if (capitalise == ALL_LOWER) {
            ke.key = Convert.toLowerCase((char) ke.key);
          }

          if (!isCharValid((char) ke.key)) {
            break;
          }
        }
        if (sel1 != -1 && (isPrintable || isDelete || isBackspace)) {
          del1 = sel1;
          del2 = sel2 - 1;
        } else if (isDelete) {
          del1 = insertPos;
          del2 = insertPos;
        } else if (isBackspace) {
          del1 = insertPos - 1;
          del2 = insertPos - 1;
        }
        if (del1 >= 0 && del2 < len) {
          if (cursorShowing) {
            Window.needsPaint = true; //draw(drawg == null ? (drawg = getGraphics()) : drawg, true); // erase cursor at old insert position
          }
          if (len > del2 - 1) {
            chars.delete(del1, del2 + 1);
            reapplyMask = true;
          }
          newInsertPos = del1;
          clearSelect = true;
        }
        if (isPrintable) {
          if (maxLength == 0 || len < maxLength || clearSelect) // guich@tc125_34
          {
            char c = (char) ke.key;
            boolean append = true;
            if (isMaskedEdit && masked.length() > 0) // put or remove '-' at the beginning of a string
            {
              char first = masked.charAt(0);
              if (c == '+' || c == '-') {
                if (first == '-') {
                  isNegative = false; // typed + and is negative (or neg of neg)?
                } else if (c == '+') {
                  break; // else, if its already positive, just ignore
                } else {
                  isNegative = true;
                }
                append = false;
              }
            }
            if (append) {
              if (newInsertPos >= chars.length()) {
                chars.append(c);
              } else {
                Convert.insertAt(chars, newInsertPos, c);
              }
            }
            reapplyMask = true;
            newInsertPos++;
            clearSelect = true;
          }
        }
        boolean isMove = true;
        switch (ke.key) {
        case SpecialKeys.HOME:
          newInsertPos = 0;
          break;
        case SpecialKeys.END:
          newInsertPos = len;
          break;
        case SpecialKeys.LEFT:
        case SpecialKeys.UP:
          newInsertPos--;
          break;
        case SpecialKeys.RIGHT:
        case SpecialKeys.DOWN:
          newInsertPos++;
          break;
        default:
          isMove = false;
        }
        if (isMove && newInsertPos != insertPos) {
          if ((ke.modifiers & SpecialKeys.SHIFT) > 0) {
            extendSelect = true;
          } else {
            clearSelect = true;
          }
        }
      }
            if (reapplyMask && mask != null && (mode == CURRENCY || mode == DATE || mode == NORMAL)) {
                applyMaskToInput();
            }
      fireValueChangeEvent();
      break;
    case PenEvent.PEN_DOWN: {
      wasFocusInOnPenDown = wasFocusIn;
      PenEvent pe = (PenEvent) event;
      if (!autoSelect || !wasFocusIn) // jairocg@450_31: if the event was focusIn, do not change the selected text
      {
        for (newInsertPos = 0; newInsertPos < chars.length() && charPos2x(newInsertPos) < pe.x - 3; newInsertPos++) {
        }
        if ((pe.modifiers & SpecialKeys.SHIFT) > 0) {
          extendSelect = true;
        } else {
          clearSelect = !persistentSelection;
        }
      } else {
        wasFocusIn = false; // guich@570_98: let the user change cursor location after the first focus_in event.
      }
      break;
    }
    case PenEvent.PEN_DRAG: {
      PenEvent pe = (PenEvent) event;
      for (newInsertPos = 0; newInsertPos < chars.length() && charPos2x(newInsertPos) <= pe.x; newInsertPos++) {
      }
      if (newInsertPos != insertPos && isEnabled()) {
        extendSelect = true;
      }
      break;
    }
    case PenEvent.PEN_UP: {
      PenEvent pe = (PenEvent) event;
      if (captionPress != null && caption != null && pe.y <= materialCaption.getExtraHeight()) {
        captionPress.onCaptionPress();
      } else if (captionPress != null && captionIcon != null && pe.x <= captionIcon.getWidth()) {
        captionPress.onIconPress();
      } else {
        if (kbdType != KBD_NONE && virtualKeyboard && !hadParentScrolled()) {
          if (!autoSelect && clipboardDelay != -1 && startSelectPos != -1 && startSelectPos != insertPos) {
            showClipboardMenu();
          } else if (wasFocusInOnPenDown || !Window.isScreenShifted()) {
              popupKCC();
          }
        }
      }
      break;
    }
    case KeyboardBox.KEYBOARD_ON_UNPOP: // guich@320_34
      pushPosState();
      ignoreSelect = true; // guich@570_112: don't autoselect when keyboard is called
      return;
    case KeyboardBox.KEYBOARD_POST_UNPOP:
      popPosState();
      if (oldTabIndex != -1) // reinsert this control in the previous position
      {
        parent.tabOrder.removeElement(this);
        parent.tabOrder.insertElementAt(this, oldTabIndex);
        oldTabIndex = -1;
      }
      isHighlighting = false;
      ignoreSelect = false; // guich@570_112: after the keyboard is definetively gone, its safe to set this to false.
      wasFocusIn = false; // guich@570_112: fix first click on edit being ignored after keyboard is closed.
      startSelectPos = -1;
      return;
    default:
      return;
    }
    if (extendSelect) {
      if (startSelectPos == -1) {
        startSelectPos = insertPos;
      } else if (newInsertPos == startSelectPos) {
        startSelectPos = -1;
      }
    }

    if (!clearSelect && wasFocusIn && startSelectPos != -1 && insertPos > startSelectPos) {
      wasFocusIn = false;
    } else if (clearSelect && startSelectPos != -1) {
      startSelectPos = -1;
    }
    newInsertPos = Math.min(newInsertPos, chars.length());
    if (newInsertPos < 0) {
      newInsertPos = 0;
    }
    boolean insertChanged = event.type == ControlEvent.CURSOR_CHANGED || (newInsertPos != insertPos);
    if (reapplyMask && mask != null && (mode == CURRENCY || mode == DATE || mode == NORMAL)) {
      applyMaskToInput();
    }
    if (insertChanged) {
      int x = charPos2x(newInsertPos);
      if (cursorShowing) {
        Window.needsPaint = true;//draw(drawg == null ? (drawg = getGraphics()) : drawg, true); // erase cursor at old insert position
      }
      if (x - 3 < xMin) {
        // characters hidden on left - jump
        xOffset += (xMin - x) + fmH;
        if (xOffset > xMin) {
          xOffset = xMin;
        }
      }
			if (alignment == LEFT) {
      int totalCharWidth = getTotalCharWidth();
      int cw = captionIcon != null ? captionIcon.getWidth() + fmH / 4 : 0;
      int xMax = this.xMax - cw;
      if (x > xMax) {
        // characters hidden on right - jump
        xOffset -= (x - xMax) + fmH;
        int minOfs = xMax - totalCharWidth;
        if (xOffset < minOfs) {
          xOffset = minOfs;
        }
      }
      if (totalCharWidth < xMax - xMin && xOffset != xMin) {
        xOffset = xMin;
      }
      cursorX = x;
    }
      }
    if (reapplyMask) {
      postPressedEvent(); // guich@tc113_1
    }

    insertPos = newInsertPos;
    if (isTopMost()) {
      Window.needsPaint = true; // must repaint everything due to a possible background image
    }
  }

  protected void shiftTo(Control next) {
    for (Container p = parent; p != null; p = p.parent) { // if next is inside a ScrollContainer, scroll to it
      if (p instanceof ScrollContainer) {
        ((ScrollContainer) p).scrollToControl(next);
        break;
      }
    }
    if (next instanceof Edit) {
      ((Edit) next).shiftScreen(true); // update screen shift position when user press ENTER
    }
  }

  private boolean showClipboardMenu() {
    int idx = showClipboardMenu(this);
    if (0 <= idx && idx <= 3) {
      if (idx != 3 && startSelectPos == -1) // if nothing was selected, select everything
      {
        startSelectPos = 0;
        insertPos = chars.length();
      }
      if (idx == 0) {
        clipboardCut();
      } else if (idx == 1) {
        clipboardCopy();
      } else {
        clipboardPaste();
        return true;
      }
    }
    return false;
  }

  private static class ClipboardMenuListener implements PressListener {
    @Override
    public void controlPressed(ControlEvent e) {
      clipSel = clipboardMenu.selectedIndex;
    }
  }

  static int clipSel = -2;

  static int showClipboardMenu(Control host) {
    try {
      if (clipboardMenu == null) {
        String[] names = { cutStr, copyStr, replaceStr, pasteStr };
        clipboardMenu = new PushButtonGroup(names, false, -1, 0, 3, 2, true, PushButtonGroup.BUTTON) {
          @Override
          protected boolean willOpenKeyboard() {
            return true;
          }
        };
        clipboardMenu.setFocusLess(true);
      }
      Container w = host.getParentWindow();
      clipboardMenu.setSelectedIndex(-1);
      clipboardMenu.setFont(w.getFont());
      Rect cli = host.getAbsoluteRect();
      int ph = clipboardMenu.getPreferredHeight();
      w.add(clipboardMenu, LEFT + 2, host instanceof MultiEdit ? cli.y + cli.height - ph
          : (cli.y > w.height / 2 ? cli.y - ph : cli.y + cli.height), PREFERRED + 4, PREFERRED + 4, host);
      clipboardMenu.setBackForeColors(UIColors.clipboardBack, UIColors.clipboardFore);
      clipboardMenu.bringToFront();
      w.repaintNow();
      clipSel = -2;
      PressListener pl;
      clipboardMenu.addPressListener(pl = new ClipboardMenuListener());
      int end = Vm.getTimeStamp() + 3000; // make sure we will elapse only 3 seconds
      while (Vm.getTimeStamp() < end) {
        Thread.yield();
        if (Event.isAvailable()) {
          Window.pumpEvents();
          if (clipSel != -2) {
            break;
          }
        }
      }
      clipboardMenu.removePressListener(pl);
      w.remove(clipboardMenu);

      return clipSel;
    } catch (Exception e) {
      if (Settings.onJavaSE) {
        e.printStackTrace();
      }
    }
    return -1;
  }

  public static boolean popupsHidden() {
    return (keyboard == null || !keyboard.isVisible()) && (calendar == null || !calendar.isVisible())
        && (calculator == null || !calculator.isVisible()) && (time == null || !time.isVisible())
        && (numeric == null || !numeric.isVisible());
  }

  @Override
  protected void onWindowPaintFinished() {
    if (!hasFocus) {
      _onEvent(new ControlEvent(ControlEvent.FOCUS_IN, this)); // this event is called on the focused control of the parent window. so, if we are not in FOCUS state, set it now. - guich@tc112_
    }
  }

  /** Called by the system to draw the edit control. */
  @Override
  public void onPaint(Graphics g) {
    draw(g);
  }

  /** Returns the length of the text.
   * @since SuperWaba 4.21
   */
  public int getLength() {
    return chars.length();
  }

  /** Returns the length of the text after applying a trim to it. 
   * This method consumes less memory than <code>getText().trim().length()</code>.
   * @since TotalCross 1.3
   */
  public int getTrimmedLength() {
    StringBuffer sb = isMaskedEdit ? masked : chars;
    int l = sb.length();
    int s = 0;
    while (s < l && sb.charAt(s) <= ' ') {
      s++;
    }
    while (l > s && sb.charAt(l - 1) <= ' ') {
      l--;
    }
    return l - s;
  }

  /** Clears this control, settings the text to clearValueStr. Note that if the Edit
   * is not editable, you will have to explicitly call the clear method of this Edit. */
  @Override
  public void clear() // guich@572_19
  {
    setText(clearValueStr, Settings.sendPressEventOnChange);
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // kmeehl@tc100
  {
    if (ke.isUpKey() || ke.isDownKey() || (ke.isNextKey() && insertPos == chars.length())
        || (ke.isPrevKey() && insertPos == 0)) {
      return null;
    }
    _onEvent(ke);
    return this;
  }

  /** Copies the text to the clipboard. 
   * If there's a selected text, copies the portion selected, otherwise, copies the whole text. 
   * @since TotalCross 1.14
   */
  public void clipboardCopy() // guich@tc114_66
  {
    int sel1 = startSelectPos;
    int sel2 = insertPos;
    if (sel1 > sel2) {
      int temp = sel1;
      sel1 = sel2;
      sel2 = temp;
    }

    try {
      String s = chars.toString();
      Vm.clipboardCopy(sel1 != -1 ? s.substring(sel1, sel2) : s);
      showTip(this, copyStr, 500, -1);
    } catch (Exception e) {
      /* just ignore */}
  }

  /** Cuts the selected text to the clipboard. 
   * @since TotalCross 1.14
   */
  public void clipboardCut() // guich@tc114_66
  {
    int sel1 = startSelectPos;
    int sel2 = insertPos;
    if (sel1 > sel2) {
      int temp = sel1;
      sel1 = sel2;
      sel2 = temp;
    }

    if (sel1 != -1) // cut/copy
    {
      Vm.clipboardCopy(chars.toString().substring(sel1, sel2)); // brunosoares@tc100: Changed from chars.substring to chars.toString().substring
      showTip(this, cutStr, 500, -1);
      // simulate a backspace to erase the selected text
      KeyEvent ke = new KeyEvent();
      ke.type = KeyEvent.SPECIAL_KEY_PRESS;
      ke.key = SpecialKeys.BACKSPACE;
      _onEvent(ke);
    }
  }

  /** Paste from the clipboard into the Edit at the current insert position. 
   * @since TotalCross 1.14
   */
  public void clipboardPaste() // guich@tc114_66
  {
    String pasted = Vm.clipboardPaste();
    if (pasted == null || pasted.length() == 0) {
      ;
    } else {
      showTip(this, pasteStr, 500, -1);
      KeyEvent ke = new KeyEvent();
      if (startSelectPos != insertPos) // if a text is selected, replace the value
      {
        ke.type = SpecialKeys.BACKSPACE;
        _onEvent(ke);
      }

      ke.type = KeyEvent.KEY_PRESS;
      int n = pasted.length();
      for (int i = 0; i < n; i++) {
        ke.key = pasted.charAt(i);
        _onEvent(ke);
      }
      try {
        setCursorPos(insertPos + n, insertPos + n);
      } catch (Exception e) {
      }
    }
  }

  /** Returns a copy of this Edit with almost all features. Used by Keyboard and SIPBox classes.
   * @since TotalCross 1.27
   */
  public Edit getCopy() {
    Edit ed = mask == null ? new Edit() : new Edit(new String(mask));
    boolean maskState = isMaskedEdit; // Saving the isMaskedEdit state so it can be set to false and setDecimalPlaces can be called
    ed.isMaskedEdit = false;
    ed.setDecimalPlaces(decimalPlaces);
    ed.isMaskedEdit = maskState;
    ed.setMode(mode, isMaskedEdit);
    ed.startSelectPos = startSelectPos;
    ed.insertPos = insertPos;
    ed.setBackForeColors(backColor, foreColor);
    if (validChars != null) {
      ed.setValidChars(validChars);
    }
    ed.capitalise = capitalise;
    ed.alignment = alignment;
    ed.maxLength = maxLength;
    ed.autoSelect = autoSelect;
    ed.kbdType = kbdType;
    ed.editable = editable;
    return ed;
  }

  @Override
  protected boolean willOpenKeyboard() {
    return editable && (kbdType == KBD_DEFAULT || kbdType == KBD_KEYBOARD);
  }

  @Override
  public String getValue() {
    StringBuffer sb = isMaskedEdit ? masked : chars;
    return sb.length() == 0 ? null : sb.toString();
  }

  @Override
  public void setValue(String value) {
    this.setText(value);
  }
  
  public int getCaptionIconHeightFactor() {
	  return captionIconHeightFactor;
  }
  
  public void setCaptionIconHeightFactor(int captionIconHeightFactor) {
	  this.captionIconHeightFactor = captionIconHeightFactor;
  }
  
  public int getCaptionIconPadding() {
	  return captionIconPadding;
  }

  public void setCaptionIconPadding(int captionIconPadding) {
      this.captionIconPadding = captionIconPadding;
  }
  

  
  @Override
	public void setRect(int x, int y, int width, int height, Control relative, boolean screenChanged) {
	    if (this.setX == Control.SETX_NOT_SET) {
	    	super.setRect(x, y, width, height, relative, screenChanged);
	    	this.setText(backup);
	    } else {
	    	super.setRect(x, y, width, height, relative, screenChanged);
	    }
	}
}
