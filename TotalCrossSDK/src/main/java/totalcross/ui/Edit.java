/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

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

public class Edit extends Control implements TextControl
{
   private TimerEvent blinkTimer; // only valid while the edit has focus
   private static int xMins[] = {4,1,3,3,4,4};
   public static final int prefH = uiAndroid ? 4 : 2;

   protected boolean hasFocus;
   private boolean cursorShowing;
   /** Specifies if the control accepts input from the user.
    * Note: do not change this directly; use the setEditable method instead.
    * @see #setEditable(boolean)
    */
   protected boolean editable = true;
   /** Specifies if new chars should overwrite existing ones. */
   public boolean overwrite;
   /** Sets the alignment of this Edit, which can be LEFT (default), CENTER or RIGHT.
    *  Note that it will always edit at left, but on focus lost, it will drawn aligned.
    *  @since SuperWaba 5.03
    */
   public int alignment=LEFT;

   /** The caption to draw when this Edit is empty.
    * @see #captionColor 
    */
   public String caption;
   
   /** The caption's color. */
   public int captionColor = -1;
   
   /** An optional caption's icon */
   public Image captionIcon;
   
   /** @see CalculatorBox#rangeCheck */
   public CalculatorBox.RangeCheck rangeCheck;

   private ControlEvent cursorChangedEvent;
   private StringBuffer chars = new StringBuffer(10);
   protected boolean hasBorder=true;
   private int xMax,xMin,gap;
   private int fColor,back0,back1;
   private int fourColors[] = new int[4];

   protected boolean isMaskedEdit;
   private int decimalPlaces = 2;
   private int insertPos;
   private int startSelectPos;
   private int xOffset;
   private boolean wasFocusIn,wasFocusInOnPenDown; // jairocg@450_31: used to verify if the event was focusIn
   private int oldTabIndex=-1;
   private boolean ignoreSelect;
   private int wildW;
   private boolean useFillAsPreferred;
   private StringBuffer masked = new StringBuffer(20);
   private boolean isNegative;
   private int cursorX;
   private int lastCommand;
   private String mapFrom,mapTo; // guich@tc110_56
   private Image npback;
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
   public static KeyboardBox keyboard; // guich
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
   private char[] mask;
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

   protected byte kbdType=KBD_DEFAULT;

   /** Set to false if you don't want the cursor to blink when the edit is not editable */
   public boolean hasCursorWhenNotEditable = true; // guich@340_23

   /** If set to true, the text will be auto-selected when the focus enters.
    * True by default on penless devices. */
   public boolean autoSelect = Settings.keyboardFocusTraversable; // guic@550_20

   /** No keyboard will be popped up for this Edit */
   public static final byte KBD_NONE = 0;
   /** The default keyboard for the current mode will be used for this Edit */
   public static final byte KBD_DEFAULT = 1;
   /** The Keyboard class (or the internal virtual keyboard) will be used for this Edit */
   public static final byte KBD_KEYBOARD = 2;
   /** The Calculator will be used for this Edit */
   public static final byte KBD_CALCULATOR = 3;
   /** The Calendar will be used for this Edit */
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
   public static final String dateSet = numbersSet+Settings.dateSeparator;

   /** to be used in the setMode method */
   public static final byte NORMAL   = 0;
   /** to be used in the setMode method */
   public static final byte DATE     = 1;
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
   public static int cursorThickness = Math.max(Settings.screenWidth,Settings.screenHeight) > 1500 ? 3 : Math.max(Settings.screenWidth,Settings.screenHeight) > 700 ? 2 : 1;

   /** Construct an Edit with FILL as preferred width. Note that you cannot use RIGHT or CENTER at the x coordinate if you use this constructor. */
   public Edit()
   {
      clearPosState();
      onFontChanged();
      useFillAsPreferred = true;
   }

   /** Construct an Edit with the default width computed based in the specified
     * mask and in the control's font. In order to allow the mask to be used as
     * a real mask, you must call the setMode method.
     * If mask is "", the FILL width is choosen.
     * @see #setMode(byte, boolean)
     */
   public Edit(String mask)
   {
      this();
      this.mask = mask.toCharArray();
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
   public void mapKeys(String from, String to)
   {
      if (from == null || to == null)
         from = to = null;
      else
      if (from.length() != to.length())
         throw new IllegalArgumentException("from.length must match to.length");
      this.mapFrom = from;
      this.mapTo = to;
   }

   /** Set the number of decimal placed if a masked edit with CURRENCY mode. Default is 2 decimal places.
    * It cannot be used with masked Edits; pass the number of decimal places in the mask itself.
    * The only exception is when you want to use the default CURRENCY mask, passing a null mask in the constructor;
    * in this situation, you can call setDecimalPlaces before calling setMode, and a mask will be constructed with
    * the given number of decimals.
    */
   public void setDecimalPlaces(int count)
   {
      if (count < 0)
         throw new IllegalArgumentException("count must be >= 0");
      if (isMaskedEdit)
         throw new RuntimeException("Edit.setDecimalPlaces can't be used after the mask is applied using setMode.");
      this.decimalPlaces = count;
   }

   /** Returns the number of decimal places. */
   public int getDecimalPlaces()
   {
      return decimalPlaces;
   }

   /** Used to change the default keyboard to be used with this Edit control.
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
      if (kbd == KBD_DEFAULT)
         switch (mode)
         {
            case DATE:     kbdType = KBD_CALENDAR;   break;
            case CURRENCY: kbdType = useNumericBoxInsteadOfCalculator ? KBD_NUMERIC : KBD_CALCULATOR; break;
            default:       kbdType = KBD_KEYBOARD;   break;
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

   protected void onFontChanged()
   {
      wildW = fm.charWidth('*');
   }

   /** Returns the mask passed on the constructor. */
   public String getMask()
   {
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
   public void setMode(byte mode)
   {
      setMode(mode, false);
   }
   
   /** Return the current mode.
    * @since TotalCross 1.27
    */
   public int getMode()
   {
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
   public void setMode(byte mode, boolean maskedEdit)
   {
      this.mode = mode;
      this.isMaskedEdit = maskedEdit;
      this.useFillAsPreferred = this.useFillAsPreferred && !maskedEdit;
      switch (mode)
      {
         case DATE:
            setValidChars(maskedEdit ? numbersSet : (numbersSet+Settings.dateSeparator));
            if (maskedEdit)
            {
               maxLength = 8;
               mask = Settings.dateFormat == Settings.DATE_YMD ? ("9999"+Settings.dateSeparator+"99"+Settings.dateSeparator+"99").toCharArray()
                                                               : ("99"+Settings.dateSeparator+"99"+Settings.dateSeparator+"9999").toCharArray();
            }
            break;
         case CURRENCY:
            setValidChars(currencyCharsSet);
            if (maskedEdit)
            {
               if (mask == null || mask.length == 0) // use a default mask
                  mask = getDefaultCurrencyMask(decimalPlaces);
               applyMaxLengthBasedOnMask();
               alignment = RIGHT;
            }
            break;
         default:
            setValidChars(null);
            if (maskedEdit && mask != null && mask.length > 0)
               applyMaxLengthBasedOnMask();
            break;
      }
      if (kbdType != KBD_NONE) // guich@tc115_29
         setKeyboard(KBD_DEFAULT);
   }
   
   public static char[] getDefaultCurrencyMask(int decimalPlaces)
   {
      String s = decimalPlaces == 0 ? "999a999a999a999a999" : ("999a999a999a999a999b"+Convert.dup('9',decimalPlaces));
      s = s.replace('a', Settings.thousandsSeparator);
      s = s.replace('b', Settings.decimalSeparator);
      return s.toCharArray();
   }

   private void applyMaxLengthBasedOnMask()
   {
      int nines = 0;
      for (int i =0; i < mask.length; i++)
         if (mask[i] == '9')
            nines++;
         else
         if (mode == CURRENCY && mask[i] == Settings.decimalSeparator)
            decimalPlaces = mask.length-i-1;
      maxLength = nines;
   }
   /** Sets the valid chars that can be entered in this edit
    * (they are converted to uppercase to make the verification easy).
    * if null is passed, any char can be entered. The chars are case insensitive.
    * If you pass "" (empty string), no chars will be able to be inputted, and movement, 
    * delete and copy/paste operations will also be disabled. 
    * @see #mapKeys
    */
   public void setValidChars(String validCharsString)
   {
      if (validCharsString != null)
         validChars = validCharsString.toUpperCase();
      else
         validChars = null;
   }

   /** Return true if the given char exists in the set of valid characters for this Edit */
   protected boolean isCharValid(char c)
   {
      return validChars == null || validChars.indexOf(Convert.toUpperCase(c)) != -1;
   }

   /** Sets the desired maximum length for text entered in the Edit.
    * Does nothing if the edit has a mask.
   @since SuperWaba 2.0 beta 4 */
   public void setMaxLength(int length)
   {
      //if (!isMaskedEdit) // guich@tc115_83: ignore if using masks
      {
         maxLength = length;
         if (length != 0 && maxLength < chars.length())  // jescoto@421_15: resize text if maxLength < len
         	chars.setLength(length);
      }
   }

   public int getMaxLength()
   {
      return maxLength;
   }

   private void clearPosState()
   {
      insertPos = 0;
      startSelectPos = -1;
      xOffset = xMins[Settings.uiStyle];
   }

   protected int pushedInsertPos;
   protected int pushedStartSelectPos;
   protected int pushedxOffset;
   protected void pushPosState()
   {
      pushedInsertPos = insertPos;
      pushedStartSelectPos = startSelectPos;
      pushedxOffset = xOffset;
   }
   protected void popPosState()
   {
      int len = chars.length();
      insertPos = Math.min(len,pushedInsertPos); // guich@571_5: make sure the insert position isn't bigger than the size of the text.
      startSelectPos = Math.min(len,pushedStartSelectPos); // guich@571_5
      xOffset = pushedxOffset;
   }

   protected int charPos2x(int n)
   {
      int extra = captionIcon == null ? 0 : captionIcon.getWidth() + fmH;
      if (!isMaskedEdit)
      {
         if (n == 0) // start of string?
            return extra + xOffset;
         if (n >= chars.length()) // end or beyond end of string?
            return extra + xOffset + getTotalCharWidth();
      }
      else
      if (n > chars.length()) n = chars.length();
      switch (mode)
      {
         case PASSWORD_ALL:
            return extra + xOffset + wildW * n;
         case PASSWORD:
            return extra + xOffset + wildW * (n-1) + fm.charWidth(chars, chars.length()-1);
         case CURRENCY:
            if (isMaskedEdit) // in currency, we go from right to left
            {
               int xx = xMax,i,pos = masked.length();
               n = chars.length() - n;
               for (i = n; i > 0 && --pos >= 0;)
               {
                  char c = masked.charAt(pos);
                  xx -= fm.charWidth(c);
                  if ('0' <= c && c <= '9') // update the position at the main string only when a numeric value is represented
                     i--;
               }
               return extra + xx;
            }
            else break;
         default://case DATE:
            if (masked.length() > 0)
            {
               int i=0,pos=0;
               for (; i < n; pos++)
                  if (pos >= mask.length)
                     break;
                  else
                  if (mask[pos] == '9') // update the position at the main string only when a numeric value is represented
                     i++;
               while (pos < mask.length && mask[pos] != '9') pos++; // skip next non-numeric chars
               return extra + xOffset + fm.sbWidth(masked, 0, pos);//Math.min(pos,masked.length())); // guich@tc152: changed mask to masked, otherwise, using old font and 1's will make the cursor appear incorrectly
            }
      }
      return extra + xOffset + fm.sbWidth(chars, 0, n);
   }

   /** Returns the text displayed in the edit control. If masking is enabled, the text with the mask is returned;
    * to get the text without the mask, use the getTextWithoutMask method.
    * @see #getTextWithoutMask()
    */
   public String getText()
   {
      return isMaskedEdit ? masked.toString() : chars.toString();
   }

   /** Returns the text without the mask. For non-currency mode, only chars whose corresponding mask is '9' are returned.
    * @see #getText()
    */
   public String getTextWithoutMask()
   {
      if (chars.length() == 0)
         return "";
      String str = chars.toString();
      if (isMaskedEdit)
      {
         if (mode == CURRENCY)
         {
            if (!hasSignificantDigits())
            {
               if (str.indexOf('.') < 0 && str.indexOf(',') < 0) // guich@tc130: return "0" instead of "000"
                  str = Convert.toString(0d,decimalPlaces);//"0";
            }
            else
            {
               if (decimalPlaces > 0) // for currency mode, remove the , and . and put it in Java's format (xxxx.yy)
               {
                  int k = str.length() - decimalPlaces; // get the number of decimal places
                  if (k <= 0)
                     str = "0.".concat(Convert.zeroPad(str,decimalPlaces));
                  else
                     str = str.substring(0,k)+"."+str.substring(k);
               }
               if (isNegative)
                  str = "-".concat(str);
            }
         }
         else
         {
            StringBuffer sbuf = new StringBuffer(str.length());
            if (mask.length == str.length()) // totally formatted? faster algorithm
            {
               // 25/03/1970 -> 25031970
               for (int i =0; i < mask.length; i++)
                  if (mask[i] == '9')
                     sbuf.append(chars.charAt(i));
            }
            else
            {
               // 25031970 -> 25031970
               int max = chars.length();
               for (int i =0,j=0; i < mask.length; i++) // guich@tc124_23: must go throught all the mask size
                  if ('0' <= mask[i] && mask[i] <= '9' && j < max)
                     sbuf.append(chars.charAt(j++));
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
   public StringBuffer getTextBuffer()
   {
      return chars;
   }

   public void setText(String s, boolean postPressed)
   {
      int len,dot,decimals;
      chars.setLength(0);
      if (s != null && (len=s.length()) > 0)
      {
         chars.append(s);
         if (mode == CURRENCY && isMaskedEdit) // correct the number if this is a numeric edit
         {
            isNegative = s.startsWith("-");
            if (isNegative) {len--; s = s.substring(1); chars.setLength(0); chars.append(s);} // guich@tc168 - if user sends a negative value, remove it from start and set the flag
            if (s.indexOf(',') >= 0 || Convert.numberOf(s, '.') > 1)
               s = Convert.replace(s,".","").replace(',','.');

            dot = s.indexOf('.');
            decimals = len - dot - 1;
            if (decimalPlaces == 0) // setText("12.34") -> "12"
            {
               if (dot >= 0)
                  chars.setLength(dot); // cut
            }
            else
            if (dot < 0) // setText("12") -> "12.00"
               chars.append(Convert.dup('0',decimalPlaces));
            else
            {
               if (decimals == decimalPlaces)
                  ;
               else
               if (decimals > decimalPlaces) // setText("12.3456") -> "12.34"
                  chars.setLength(len - (decimals - decimalPlaces));
               else // decimals <= decimalPlaces setText("12.1") -> "12.10"
                  chars.append(Convert.dup('0', decimalPlaces - decimals));
               int delpos = len - decimals - 1;
               chars.delete(delpos, delpos+1); // remove the . - guich@tc100b4_11: added +1
            }
         }
         else
         if (mode != CURRENCY && isMaskedEdit && chars.length() >= mask.length)
         {
            String unmasked = getTextWithoutMask();
            chars.setLength(0);
            chars.append(unmasked);
         }
      }
      applyMaskToInput();
      clearPosState();
      Window.needsPaint = true;
      if (postPressed)
         postPressedEvent();
   }
   
   /**
     * Sets the text displayed in the edit control.
     * If you're setting the text in CURRENCY mode,
     * the text must be set <b>not</b> formatted (unmasked).
     */
   public void setText(String s)
   {
      setText(s,Settings.sendPressEventOnChange);
   }

   /** Sets if the control accepts input from the user.
    * If set to false, you must explicitly call the clear method of this edit.
    */
   public void setEditable(boolean on)
   {
      focusTraversable = editable = on;
   }

   /** Gets if the control accepts input from the user */
   public boolean isEditable()
   {
      return editable;
   }

   protected void onBoundsChanged(boolean screenChanged) // guich
   {
      xMin = xMins[Settings.uiStyle];
      xMax = this.width - xMin;
      gap = hasBorder ? (xMin>>1) : 0;
      npback = null;
   }

   public int getPreferredWidth()
   {
      return (mask==null || useFillAsPreferred)?FILL:(fm.stringWidth(new String(mask)) + (uiAndroid?10:(uiFlat||uiVista)?8:4)); // guich@200b4_202: from 2 -> 4 is PalmOS style - guic@300_52: empty mask means FILL - guich@570_88: fixed width when uiFlat
   }

   public int getPreferredHeight()
   {
      return fmH+prefH;
   }

   protected void onColorsChanged(boolean colorsChanged)
   {
      npback = null;
      fColor = getForeColor();
      back0  = UIColors.sameColors ? backColor : Color.brighter(getBackColor()); // guich@572_15
      back1  = back0 != Color.WHITE ?(UIColors.sameColors?Color.darker(getBackColor()):backColor):Color.getCursorColor(back0);//guich@300_20: use backColor instead of: back0.getCursorColor();
      if (!uiAndroid) Graphics.compute3dColors(isEnabled(),backColor,foreColor,fourColors);
   }

   private int getTotalCharWidth()
   {
      int len = chars.length();
      switch (mode)
      {
         case PASSWORD_ALL: return len == 0 ? 0 : wildW * len;
         case PASSWORD: return len == 0 ? 0 : wildW * (len-1) + fm.charWidth(chars, len-1);
         default:
            if (isMaskedEdit)
            {
               int pos = masked.length();
               int n = Math.max(pos,len),i;
               int ww = 0;
               for (i = n; i > 0 && --pos >= 0;)
               {
                  char c = masked.charAt(pos);
                  ww += fm.charWidth(c);
                  if ('0' <= c && c <= '9') // update the position at the main string only when a numeric value is represented
                     i--;
               }
               return ww;
            }
            else
               return fm.sbWidth(chars,0,len);
      }
   }

   protected void draw(Graphics g)
   {
      if (g == null || !isDisplayed()) return; // guich@tc114_65: check if its displayed

      boolean uiAndroid = Control.uiAndroid || uiHolo;
      int y = this.height - fmH - gap;
      if (uiAndroid) y -= 1;
      if (uiHolo) // no else here!
         y = (height-fmH - gap) / 2;

      g.backColor = back0;
      if (!transparentBackground)
      {
         int gg = gap;
         if (uiAndroid) {g.backColor = parent.backColor; gg = 0;}
         if (!uiAndroid || !hasBorder) g.fillRect(gg,gg, this.width - (gg << 1), this.height - (gg << 1));
         if (hasBorder && uiAndroid)
         {
            try
            {
               if (npback == null || focusColor != -1)
               {
                  npback = NinePatch.getInstance().getNormalInstance(NinePatch.EDIT, width, height, isEnabled() ? hasFocus && focusColor != -1 ? focusColor : back0 : (back0 == parent.backColor ? Color.darker(back0,32) : Color.interpolate(back0,parent.backColor)), false);
                  npback.alphaMask = alphaValue;
               }
            }
            catch (ImageException e) {e.printStackTrace();}
            NinePatch.tryDrawImage(g,npback,0,0);
         }
      }
      // draw the text and/or the selection
      int len = chars.length();
      boolean drawCaption = caption != null && !hasFocus && len == 0;
      if (len > 0 || drawCaption || captionIcon != null)
      {
         if (startSelectPos != -1 && editable) // moved here to avoid calling g.eraseRect (call fillRect instead) - guich@tc113_38: only if editable
         {
            // character regions are:
            // 0 to (sel1-1) .. sel1 to (sel2-1) .. sel2 to last_char
            int sel1 = Math.min(startSelectPos,insertPos);
            int sel2 = Math.max(startSelectPos,insertPos);
            int sel1X = charPos2x(sel1);
            int sel2X = charPos2x(sel2);
            
            if (sel1X != sel2X)
            {
               int old = g.backColor;
               g.backColor = back1;
               g.fillRect(sel1X,y,sel2X-sel1X+1,fmH);
               g.backColor = old;
            }
         }

         g.foreColor = fColor;
         int xx = xOffset;
         if (captionIcon != null)
         {
            xx += captionIcon.getWidth() + fmH;
            g.drawImage(captionIcon, fmH, (height-captionIcon.getHeight())/2);
         }
            
         if (!hasFocus && !drawCaption) // guich@503_2: align the edit after it looses focus
            switch (alignment)
            {
               case RIGHT: xx = this.width-getTotalCharWidth()-xOffset; break;
               case CENTER: xx = (this.width-getTotalCharWidth())>>1; break;
            }
         if (hasBorder) g.setClip(xMin,0,xMax-Edit.prefH,height);
         if (drawCaption)
         {
            g.foreColor = captionColor != -1 ? captionColor : this.foreColor;
            g.drawText(caption, xx, y, textShadowColor != -1, textShadowColor);
         }
         else
            switch (mode)
            {
               case PASSWORD: // password fields usually have small text, so this method does not have to be very optimized
                  if (len > 0)
                     g.drawText(Convert.dup('*',len-1)+chars.charAt(len-1), xx, y, textShadowColor != -1, textShadowColor);
                  break;
               case PASSWORD_ALL:
                  g.drawText(Convert.dup('*',len), xx, y, textShadowColor != -1, textShadowColor);
                  break;
               case CURRENCY:
                  if (isMaskedEdit)
                     xx = this.width-getTotalCharWidth()-xOffset-1;
               default:
                  if (masked.length() > 0)
                     g.drawText(masked, 0, masked.length(), xx, y, textShadowColor != -1, textShadowColor);
                  else
                     g.drawText(chars, 0, len, xx, y, textShadowColor != -1, textShadowColor);
            }
         if (hasBorder) g.clearClip();
      }
      if (hasBorder && !uiAndroid)
         g.draw3dRect(0,0,this.width,this.height,Graphics.R3D_EDIT,false,false,fourColors); // draw the border and erase the rect
      cursorX = charPos2x(insertPos);
      if (hasFocus && isEnabled() && (editable || hasCursorWhenNotEditable)) // guich@510_18: added check to see if it is enabled
      {
         // draw cursor
         if (xMin <= cursorX && cursorX <= xMax) // guich@200b4_155
         {
            if (cursorShowing)
            {                  
               g.clearClip();
               g.foreColor = Color.interpolate(backColor,foreColor);
               g.drawRect(cursorX - 1, uiAndroid?y+1:y, cursorThickness, fmH);
            }
         }
         cursorShowing = !cursorShowing;
      }
      else
         cursorShowing = false;
   }

   private void applyMaskToInput()
   {
      int len = chars.length();
      StringBuffer masked = this.masked; // cache instance field
      masked.setLength(0);
      if (len == 0)
         isNegative = false;
      else
      if ((mode == DATE || mode == NORMAL) && isMaskedEdit) // date must go forward
      {
         int n = Math.min(len,mask.length),i=0,pos=0;
         while (i < n)
            if (pos >= mask.length)
               break;
            else
            if (mask[pos] == '9')
            {
               masked.append(chars.charAt(i++));
               pos++;
            }
            else
            {
               masked.append(mask[pos]);
               if (mask[pos] == chars.charAt(i)) // if the user passed 25/03/1970 to a mask 99/99/9999, skip the chars if the char matches
                  i++;
               pos++;
            }
         if (pos < mask.length && mask[pos] != '9') // put the slash if it's next
            masked.append(mask[pos]);
      }
      else
      if (mode == CURRENCY && isMaskedEdit && len > 0) // currency must go backward
      {
         for (int i =len-1,pos=mask.length-1; i >= 0 && pos >= 0;)
            if (mask[pos] == '9')
            {
               masked.append(chars.charAt(i--));
               pos--;
            }
            else
            {
               masked.append(mask[pos]);
               if (chars.charAt(i) == mask[pos])
                  i--;
               pos--;
            }
         if (hasSignificantDigits())
         {
            if (decimalPlaces > 0)
            {
               int k = masked.length() - decimalPlaces; // get the number of decimal places
               if (k <= 0)
                  masked.append(Convert.zeroPad("",-k)).append(Settings.decimalSeparator).append('0');
            }
            if (isNegative)
               masked.append('-');
         }
         else isNegative = false;
         masked.reverse();
      }
   }

   private boolean hasSignificantDigits()
   {
      for (int i=chars.length()-1; i >= 0; i--)
         if (chars.charAt(i) != '0')
            return true;
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
      startSelectPos = (start != end)?start:-1;
      insertPos = end;
      if (cursorChangedEvent == null)
         cursorChangedEvent = new ControlEvent(ControlEvent.CURSOR_CHANGED,this);
      onEvent(cursorChangedEvent);
      Window.needsPaint = true;
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
      return new int[]{startSelectPos,insertPos};
   }

   /** User method to popup the keyboard/calendar/calculator for this edit. */
   public void popupKCC()
   {
      if (kbdType == KBD_NONE || !editable || !isEnabled()) // fdie@ nothing to do if kdb has been disabled
         return;
      if (!popupsHidden())
      {
         // check if the keyboard is already popped up
         if(Settings.fingerTouch && kbdType != KBD_TIME && kbdType != KBD_CALENDAR && kbdType != KBD_CALCULATOR && kbdType != KBD_NUMERIC)
            return;
      }

      Window w = getParentWindow();
      if (w != null) w.swapFocus(this);//requestFocus(); // guich@200b4: bring focus back -  guich@401_15: changed to swapFocus

      switch (kbdType)
      {
         case KBD_TIME: 
            if (time == null) time = new TimeBox();
            time.tempTitle = keyboardTitle;
            try 
            {
               time.setTime(new Time(getText(),false,false,false,true,true,true));
            } 
            catch (Exception e) 
            {
               time.setTime(new Time(0));
               if (chars.length() > 0 && Settings.onJavaSE) e.printStackTrace();
            }
            hideSip();
            time.popup();
            setText(time.getTime().toString(),true);
            break;
            
         case KBD_CALENDAR:
            if (calendar == null) calendar = new CalendarBox();
            calendar.tempTitle = keyboardTitle;
            try {calendar.setSelectedDate(new Date(getText()));} catch (InvalidDateException ide) {} // if the date is invalid, just ignore it
            hideSip();
            calendar.popupNonBlocking();
            break;

         case KBD_CALCULATOR:
            if (calculator == null) calculator = new CalculatorBox();
            calculator.rangeCheck = this.rangeCheck;
            calculator.tempTitle = keyboardTitle;
            calculator.optionalValue = optionalValue4CalculatorBox;
            hideSip();
            calculator.popupNonBlocking();
            break;

         case KBD_NUMERIC:
            if (numeric == null) numeric = new CalculatorBox(false);
            numeric.rangeCheck = this.rangeCheck;
            numeric.tempTitle = keyboardTitle;
            numeric.optionalValue = optionalValue4CalculatorBox;
            hideSip();
            numeric.popupNonBlocking();
            break;

         default:
            if (virtualKeyboard && editable && !"".equals(validChars))
            {
               if (Settings.customKeyboard != null)
               {
                  Settings.customKeyboard.show(this, validChars);
               }
               else
               {
                  int sbl = Settings.SIPBottomLimit;
                  if (sbl == -1) sbl = Settings.screenHeight / 2;
                  boolean onBottom = Settings.unmovableSIP || getAbsoluteRect().y < sbl;
                  if (Settings.unmovableSIP && !Window.isSipShown) // guich@tc126_21
                  {
                     Window ww = getParentWindow();
                     if (ww != null)
                        ww.shiftScreen(this,this.height-(fmH+prefH));
                  }
                  if (!Window.isSipShown)
                  {
                     Window.isSipShown = true;
                     Window.setSIP(onBottom ? Window.SIP_BOTTOM : Window.SIP_TOP, this, mode == PASSWORD || mode == PASSWORD_ALL); // if running on a PocketPC device, set the bounds of Sip in a way to not cover the edit
                  }
               }
            }
            else
            {
               if (keyboard == null) keyboard = new KeyboardBox();
               keyboard.tempTitle = keyboardTitle;
               showInputWindow(keyboard);
            }
            return;
      }
   }
   
   protected void hideSip()
   {
      if (Window.isSipShown) // non-default keyboards gets here
      {
         Vm.debug("==== fechando teclado 3 ====");
         Window.isSipShown = false;
         Window.setSIP(Window.SIP_HIDE,null,false);
      }
   }

   private void showInputWindow(Window w)
   {
      oldTabIndex = parent.tabOrder.indexOf(this);
      pushPosState();
      if (removeTimer(blinkTimer)) // guich@200b4_167
         blinkTimer = null;
      w.popupNonBlocking();
      popPosState();
      requestFocus();
   }

   private void focusOut()
   {
//      if (virtualKeyboard && editable && kbdType != KBD_NONE && Window.isSipShown) // guich@tc126_58: always try to close the sip
//         hideSip();
      hasFocus = false;
      clearPosState();
      if (removeTimer(blinkTimer)) // guich@200b4_167
         blinkTimer = null;
   }

   /** Called by the system to pass events to the edit control. */
   public void onEvent(Event event)
   {
      if (calendar != null && event.type == ControlEvent.WINDOW_CLOSED && event.target == calendar) // called from the keyboard and from the calendar
      {
         Date d = calendar.getSelectedDate();
         if (d != null)
            setText(d.toString(),true);
         else
         if (!calendar.canceled)
            setText("",true);
         return;
      }
      boolean extendSelect = false;
      boolean clearSelect = false;
      boolean reapplyMask = false;
      int len = chars.length();
      if (len == 0) // guich@571_3: make sure the insert position is zero if there's no text.
         insertPos = startSelectPos = 0;
      int newInsertPos = insertPos;
      switch (event.type)
      {
         case ControlEvent.CURSOR_CHANGED:
            break;
         case TimerEvent.TRIGGERED:
            if (showKeyboardOnNextEvent)
            {
               event.consumed=true;
               showKeyboardOnNextEvent = false;
               popupKCC();
               return;
            }
            if (event == blinkTimer) // kmeehl@tc100: make sure its our timer
            {
               if (!isTopMost()) // must check here and not in the onPaint method, otherwise it results in a problem: show an edit field, then popup a window and move it: the edit field of the other window is no longer being drawn
                  focusOut();
               else
               if (parent != null)
               {
                  Window.needsPaint = true;
                  // guich@tc130: show the copy/paste menu
/*                  if (editable && enabled && lastPenDown != -1 && clipboardDelay != -1 && (Vm.getTimeStamp() - lastPenDown) >= clipboardDelay)
                     if (showClipboardMenu())
                     {
                        event.consumed = true;
                        break;
                     }
*/               }
               event.consumed=true;     //astein@230_5: prevent blinking cursor event from propagating
            }
            return;
         case ControlEvent.FOCUS_IN:
            isHighlighting = false; // guich@573_28: after closing a KCC, don't let the focus move from here.
         	wasFocusIn=true; // jairocg@450_31: set it so we can validate later
            hasFocus = true;
            if (blinkTimer == null)
               blinkTimer = addTimer(350);
            if (len > 0) // guich@550_20: autoselect the text
            {
               if (autoSelect && !ignoreSelect) // guich@570_112: changed to !ignoreSelect
               {
                  startSelectPos = len;
                  newInsertPos = 0;
               }
               else 
               if (Settings.moveCursorToEndOnFocus) 
                  newInsertPos = len; 
            }
            break;
         case ControlEvent.FOCUS_OUT:
            if (cursorShowing)
               Window.needsPaint = true; //draw(drawg=getGraphics(), true); // erase cursor at old insert position
            newInsertPos = 0;
            focusOut();
            break;
         case KeyEvent.KEY_PRESS:
         case KeyEvent.SPECIAL_KEY_PRESS:
            if (editable && isEnabled())
            {
               KeyEvent ke = (KeyEvent)event;
               if (event.type == KeyEvent.SPECIAL_KEY_PRESS && ke.key == SpecialKeys.ESCAPE) event.consumed = true; // don't let the back key be passed to the parent
               if (insertPos == 0 && ke.key == ' ' && (mode == CURRENCY || mode == DATE)) // guich@tc114_34
               {
                  popupKCC();
                  break;
               }
               boolean moveFocus = !Settings.geographicalFocus && (ke.isActionKey() || ke.key == SpecialKeys.TAB);
               if (event.target == this && moveFocus) // guich@tc100b2: move to the next edit in the same container
               {
                  if (parent != null && parent.moveFocusToNextEditable(this, ke.modifiers == 0) != null)
                     return;
               }
               boolean loseFocus = moveFocus || ke.key == SpecialKeys.ESCAPE;
               if (event.target == this && loseFocus)
               {
                  //isHighlighting = true; // kmeehl@tc100: set isHighlighting first, so that Window.removeFocus() wont trample Window.highlighted
                  if (removeFocusOnAction) 
                  {
                     Window w = getParentWindow(); // guich@tc114_32: restore the highlight to this control...
                     if (w != null) // guich@tc123_16
                     {
                        if (w.getFocus() == this)
                           w.removeFocus();
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
                  if (lastCommand > 0 && (Vm.getTimeStamp() - lastCommand) < 2500)
                  {
                     ke.modifiers |= SpecialKeys.CONTROL;
                     isControl = true;
                     lastCommand = 0;
                  }
                  else
                  if (ke.key == SpecialKeys.COMMAND) // just a single COMMAND? break
                  {
                     showTip(this, Edit.commandStr, 2500, -1);
                     lastCommand = Vm.getTimeStamp();
                     break;
                  }
               }
               if ("".equals(validChars)) // guich@tc115_33 
                  break;
               boolean isDelete = (ke.key == SpecialKeys.DELETE);
               boolean isBackspace = (ke.key == SpecialKeys.BACKSPACE);
               boolean isPrintable = ke.key > 0 && event.type == KeyEvent.KEY_PRESS && (ke.modifiers & SpecialKeys.ALT) == 0 && (ke.modifiers & SpecialKeys.CONTROL) == 0;
               int del1 = -1;
               int del2 = -1;
               int sel1 = startSelectPos;
               int sel2 = insertPos;
               if (sel1 > sel2)
               {
                  int temp = sel1;
                  sel1 = sel2;
                  sel2 = temp;
               }
               // clipboard
               if (isControl)
               {
                  if (0 < ke.key && ke.key < 32) ke.key += 64;
                  ke.modifiers &= ~SpecialKeys.CONTROL; // remove control
                  int key = Convert.toUpperCase((char)ke.key);
                  switch (key)
                  {
                     case ' ': // guich@320_47
                        setText("");
                        return;
                     case 'X':
                     case 'C':
                        if (key == 'X') // cut
                           clipboardCut();
                        else
                           clipboardCopy();
                        return;
                     case 'P':
                     case 'V':
                        clipboardPaste();
                        return;
                  }
               }
               if (mapFrom != null) // guich@tc110_56
               {
                  int idx = mapFrom.indexOf(Convert.toLowerCase((char)ke.key));
                  if (idx != -1)
                     ke.key = mapTo.charAt(idx);
               }
               if (isPrintable)
               {
                  if (capitalise == ALL_UPPER)
                     ke.key = Convert.toUpperCase((char)ke.key);
                  else
                  if (capitalise == ALL_LOWER)
                     ke.key = Convert.toLowerCase((char)ke.key);

                  if (!isCharValid((char)ke.key)) // guich@101: tests if the key is in the valid char set - moved to here because a valid clipboard char can be an invalid edit char
                     break;
               }
               if (sel1 != -1 && (isPrintable || isDelete || isBackspace))
               {
                  del1 = sel1;
                  del2 = sel2 - 1;
               }
               else if (isDelete)
               {
                  del1 = insertPos;
                  del2 = insertPos;
               }
               else if (isBackspace)
               {
                  del1 = insertPos - 1;
                  del2 = insertPos - 1;
               }
               if (del1 >= 0 && del2 < len)
               {
                  if (cursorShowing)
                     Window.needsPaint = true; //draw(drawg == null ? (drawg = getGraphics()) : drawg, true); // erase cursor at old insert position
                  if (len > del2 - 1)
                  {
                     chars.delete(del1, del2+1);
                     reapplyMask = true;
                  }
                  newInsertPos = del1;
                  clearSelect = true;
               }
               if (isPrintable)
                  if (maxLength == 0 || len < maxLength || clearSelect) // guich@tc125_34
                  {
                     char c = (char)ke.key;
                     boolean append = true;
                     if (isMaskedEdit && masked.length() > 0) // put or remove '-' at the beginning of a string
                     {
                        char first = masked.charAt(0);
                        if (c == '+' || c == '-')
                        {
                           if (first == '-')
                              isNegative = false; // typed + and is negative (or neg of neg)?
                           else
                           if (c == '+')
                              break; // else, if its already positive, just ignore
                           else
                              isNegative = true;
                           append = false;
                        }
                     }
                     if (append) 
                        if (newInsertPos >= chars.length())
                           chars.append(c);
                        else
                           Convert.insertAt(chars, newInsertPos, c);
                     reapplyMask = true;
                     newInsertPos++;
                     clearSelect = true;
                  }
               boolean isMove = true;
               switch (ke.key)
               {
                  case SpecialKeys.HOME  : newInsertPos = 0; break;
                  case SpecialKeys.END   : newInsertPos = len; break;
                  case SpecialKeys.LEFT  :
                  case SpecialKeys.UP    : newInsertPos--; break;
                  case SpecialKeys.RIGHT :
                  case SpecialKeys.DOWN  : newInsertPos++; break;
                  default: isMove = false;
               }
               if (isMove && newInsertPos != insertPos)
               {
                  if ((ke.modifiers & SpecialKeys.SHIFT) > 0)
                     extendSelect = true;
                  else
                     clearSelect = true;
               }
            }
            break;
         case PenEvent.PEN_DOWN:
         {
            wasFocusInOnPenDown = wasFocusIn;
            PenEvent pe = (PenEvent)event;
         	if (!autoSelect || !wasFocusIn) // jairocg@450_31: if the event was focusIn, do not change the selected text
            {
               for (newInsertPos = 0; newInsertPos < chars.length() && charPos2x(newInsertPos) < pe.x-3; newInsertPos++) {}
               if ((pe.modifiers & SpecialKeys.SHIFT) > 0) // shift
                  extendSelect = true;
               else
                  clearSelect = true;
            } else wasFocusIn = false; // guich@570_98: let the user change cursor location after the first focus_in event.
            break;
         }
         case PenEvent.PEN_DRAG:
         {
            PenEvent pe = (PenEvent)event;
            for (newInsertPos = 0; newInsertPos < chars.length() && charPos2x(newInsertPos) <= pe.x; newInsertPos++) {}
            if (newInsertPos != insertPos && isEnabled())
               extendSelect = true;
            break;
         }
         case PenEvent.PEN_UP:
            if (kbdType != KBD_NONE && virtualKeyboard && !hadParentScrolled())
            {
               if (!autoSelect && clipboardDelay != -1 && startSelectPos != -1 && startSelectPos != insertPos)
                  showClipboardMenu();
               else
               if (wasFocusInOnPenDown || !Window.isScreenShifted())
                  popupKCC();
            }
            break;
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
      if (extendSelect)
      {
         if (startSelectPos == -1)
            startSelectPos = insertPos;
         else if (newInsertPos == startSelectPos)
            startSelectPos = -1;
      }

      if (wasFocusIn && startSelectPos != -1 && insertPos>startSelectPos) // jairocg@450_31: event validation with text selection
      	 wasFocusIn=false;
      else
      if (clearSelect && startSelectPos != -1)
         startSelectPos = -1;
      newInsertPos = Math.min(newInsertPos, chars.length());
      if (newInsertPos < 0)
         newInsertPos = 0;
      boolean insertChanged = event.type == ControlEvent.CURSOR_CHANGED || (newInsertPos != insertPos);
      if (reapplyMask && mask != null && (mode == CURRENCY || mode == DATE || mode == NORMAL))
         applyMaskToInput();
      if (insertChanged)
      {
         int x = charPos2x(newInsertPos);
         if (cursorShowing)
            Window.needsPaint = true;//draw(drawg == null ? (drawg = getGraphics()) : drawg, true); // erase cursor at old insert position
         if (x - 3 < xMin)
         {
            // characters hidden on left - jump
            xOffset += (xMin - x) + fmH;
            if (xOffset > xMin)
               xOffset = xMin;
         }
         int totalCharWidth = getTotalCharWidth();
         if (x > xMax)
         {
            // characters hidden on right - jump
            xOffset -= (x - xMax) + fmH;
            int minOfs = xMax - totalCharWidth;
            if (xOffset < minOfs)
               xOffset = minOfs;
         }
         if (totalCharWidth < xMax - xMin && xOffset != xMin)
            xOffset = xMin;
         cursorX = x;
      }
      if (reapplyMask)
         postPressedEvent(); // guich@tc113_1

      insertPos = newInsertPos;
      if (isTopMost()) // guich@tc124_24: prevent screen updates when we're not the topmost window
         Window.needsPaint = true; // must repaint everything due to a possible background image
   }

   private boolean showClipboardMenu()
   {
      int idx = showClipboardMenu(this);
      if (0 <= idx && idx <= 3)
      {
         if (idx != 3 && startSelectPos == -1) // if nothing was selected, select everything
         {
            startSelectPos = 0;
            insertPos = chars.length();
         }
         if (idx == 0)
            clipboardCut();
         else
         if (idx == 1)
            clipboardCopy();
         else
         {
            clipboardPaste();
            return true;
         }
      }             
      return false;
   }
   
   private static class ClipboardMenuListener implements PressListener
   {
      public void controlPressed(ControlEvent e)
      {
         clipSel = clipboardMenu.selectedIndex;
      }
   }
   static int clipSel = -2;
   static int showClipboardMenu(Control host)
   {
      try
      {
         if (clipboardMenu == null)
         {
            String[] names = {cutStr,copyStr,replaceStr,pasteStr};
            clipboardMenu = new PushButtonGroup(names, false, -1, 0,3,2,true,PushButtonGroup.BUTTON)
            {
               protected boolean willOpenKeyboard()
               {
                  return true;
               }
            };
            clipboardMenu.setFocusLess(true);
         }
         Container w = host.getParentWindow();
         clipboardMenu.setSelectedIndex(-1);
         Rect cli = host.getAbsoluteRect();
         int ph = clipboardMenu.getPreferredHeight();
         w.add(clipboardMenu,LEFT+2, host instanceof MultiEdit ? cli.y+cli.height-ph : (cli.y > w.height/2 ? cli.y-ph : cli.y+cli.height), PREFERRED+4,PREFERRED+4,host);
         clipboardMenu.setBackForeColors(UIColors.clipboardBack,UIColors.clipboardFore);
         clipboardMenu.bringToFront();
         w.repaintNow();
         clipSel = -2;
         PressListener pl;
         clipboardMenu.addPressListener(pl = new ClipboardMenuListener());
         int end = Vm.getTimeStamp() + 3000; // make sure we will elapse only 3 seconds
         while (Vm.getTimeStamp() < end)
         {
            Vm.sleep(10);
            if (Event.isAvailable())
            {
               Window.pumpEvents();
               if (clipSel != -2)
                  break;
            }
         }
         clipboardMenu.removePressListener(pl);
         w.remove(clipboardMenu);
         
         return clipSel;
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE) e.printStackTrace();
      }
      return -1;
   }

   public static boolean popupsHidden()
   {
      return (keyboard   == null || !keyboard.isVisible())   &&
             (calendar   == null || !calendar.isVisible())   &&
             (calculator == null || !calculator.isVisible()) &&
             (time == null || !time.isVisible()) && 
             (numeric == null || !numeric.isVisible());
   }

   protected void onWindowPaintFinished()
   {
      if (!hasFocus) _onEvent(new ControlEvent(ControlEvent.FOCUS_IN,this)); // this event is called on the focused control of the parent window. so, if we are not in FOCUS state, set it now. - guich@tc112_
   }

   /** Called by the system to draw the edit control. */
   public void onPaint(Graphics g)
   {
      draw(g);
   }

   /** Returns the length of the text.
     * @since SuperWaba 4.21
     */
   public int getLength()
   {
      return chars.length();
   }
   
   /** Returns the length of the text after applying a trim to it. 
    * This method consumes less memory than <code>getText().trim().length()</code>.
    * @since TotalCross 1.3
    */
   public int getTrimmedLength()
   {
      StringBuffer sb = isMaskedEdit ? masked : chars;
      int l = sb.length();
      int s = 0;
      while (s < l && sb.charAt(s) <= ' ')
         s++;
      while (l > s && sb.charAt(l-1) <= ' ')
         l--;
      return l-s;
   }      

   /** Clears this control, settings the text to clearValueStr. Note that if the Edit
    * is not editable, you will have to explicitly call the clear method of this Edit. */
   public void clear() // guich@572_19
   {
      setText(clearValueStr,Settings.sendPressEventOnChange);
   }

   public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // kmeehl@tc100
   {
      if (ke.isUpKey() || ke.isDownKey() || (ke.isNextKey() && insertPos == chars.length()) || (ke.isPrevKey() && insertPos == 0))
         return null;
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
      if (sel1 > sel2)
      {
         int temp = sel1;
         sel1 = sel2;
         sel2 = temp;
      }
      
      try
      {
         String s = chars.toString();
         Vm.clipboardCopy(sel1 != -1 ? s.substring(sel1,sel2) : s);
         showTip(this, copyStr, 500, -1);
      }
      catch (Exception e) {/* just ignore */}
   }
   
   /** Cuts the selected text to the clipboard. 
    * @since TotalCross 1.14
    */
   public void clipboardCut() // guich@tc114_66
   {
      int sel1 = startSelectPos;
      int sel2 = insertPos;
      if (sel1 > sel2)
      {
         int temp = sel1;
         sel1 = sel2;
         sel2 = temp;
      }
      
      if (sel1 != -1) // cut/copy
      {
         Vm.clipboardCopy(chars.toString().substring(sel1,sel2)); // brunosoares@tc100: Changed from chars.substring to chars.toString().substring
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
      if (pasted == null || pasted.length() == 0)
         ;
      else
      {
         showTip(this, pasteStr, 500, -1);
         KeyEvent ke = new KeyEvent();
         if (startSelectPos != insertPos) // if a text is selected, replace the value
         {
            ke.type = SpecialKeys.BACKSPACE;
            _onEvent(ke);
         }
            
         ke.type = KeyEvent.KEY_PRESS;
         int n = pasted.length();
         for (int i =0; i < n; i++)
         {
            ke.key = pasted.charAt(i);
            _onEvent(ke);
         }
         try {setCursorPos(insertPos+n, insertPos+n);} catch (Exception e) {}
      }
   }

   /** Returns a copy of this Edit with almost all features. Used by Keyboard and SIPBox classes.
    * @since TotalCross 1.27
    */
   public Edit getCopy()
   {
      Edit ed = mask == null ? new Edit() : new Edit(new String(mask));
      ed.setDecimalPlaces(decimalPlaces);
      ed.setMode(mode,isMaskedEdit);
      ed.startSelectPos = startSelectPos;
      ed.insertPos = insertPos;
      ed.setBackForeColors(backColor,foreColor);
      if (validChars != null)
         ed.setValidChars(validChars);
      ed.capitalise = capitalise;
      ed.alignment = alignment;
      ed.maxLength = maxLength;
      ed.autoSelect = autoSelect;
      ed.kbdType = kbdType;
      ed.editable = editable;
      return ed;
   }
   
   protected boolean willOpenKeyboard()
   {
      return editable && (kbdType == KBD_DEFAULT || kbdType == KBD_KEYBOARD);
   }
}
