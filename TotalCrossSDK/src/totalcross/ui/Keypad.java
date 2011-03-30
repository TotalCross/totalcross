/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: Keypad.java,v 1.26 2011-01-13 15:27:22 guich Exp $

package totalcross.ui;

import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.sys.*;

/** This class is used to handle letters and numbers input on keypadOnly devices.
 * @since SuperWaba 5.5
 */
public class Keypad extends Control // guich@550_16
{
   private static Keypad kp;

   /** Returns the single instance of this class. */
   public static Keypad getInstance() // guich@573_29
   {
      if (kp == null)
         kp = new Keypad();
      return kp;
   }

   /** Only numbers: 0 ... 9 */
   public static String[] numberKeyset =
   {
      "0","1","2","3","4","5","6","7","8","9"
   };

   /** The default char set. All letters must be in lowercase */
   public static String[] defaultKeyset =
   {
      " 0",             // 0
      ".,'?!\"1-()@/:", // 1
      "abc2·„‚‡Á",      // 2
      "def3ÈÍ",         // 3
      "ghi4Ì",          // 4
      "jkl5",           // 5
      "mno6ÛıÙ",        // 6
      "pqrs7$",         // 7
      "tuv8˙¸",         // 8
      "wxyz9",          // 9
   };
   
   /** The default symbol char set */
   public static String defaultSymbolKeyset;

   /** Amount of time until the control closes. */
   public static int CLOSE_TIMEOUT = 1200;
   /** The x position of the control on screen. It is CENTER by default. Use only LEFT,CENTER,RIGHT constants. */
   public static int X_ALIGN = CENTER;
   /** The y position of the control on screen. It is BOTTOM-3 by default. Use only TOP,CENTER,BOTTOM constants. */
   public static int Y_ALIGN = BOTTOM-3;
   /** The y alternate position of the control on screen. It is TOP+3 by default. Use only TOP,CENTER,BOTTOM constants. */
   public static int Y_ALIGN_ALT = TOP+3;

   /** Used with the <code>mode</code> member. */
   public static final int MODE_LOWER  = 0;
   /** Used with the <code>mode</code> member. */
   public static final int MODE_UPPER  = 1;
   /** Used with the <code>mode</code> member. */
   public static final int MODE_NUMBER = 2;

   /** The current mode. Use the MODE_xxx constants */
   public static int mode = MODE_LOWER;
   
   /** The ASCII code of the first char represented in the keyset **/
   public static int firstChar = '0';
   
   /** The ASCII code of the last char represented in the keyset **/
   public static int lastChar = '9';
   
   /** The ASCII code of the char corresponding to a backspace **/
   public static int backspaceChar = '*';
   
   /** The ASCII code of the char that triggers the symbols char mode **/
   public static int symbolsTriggerChar = '#';

   // private members
   private char[][]charsL;
   private char[][]charsU;
   private char[][]chars; // the current one
   private short[][]posxL;
   private short[][]posxU;
   private short[][]posx;
   private int lastKey=-1,lastIndex;
   private TimerEvent timer;
   private KeyEvent ke = new KeyEvent();
   private Image backImg; // used to save the background
   private Graphics backG;
   private int hashW; // width of #
   private char[] symbolKeys; // guich@573_33
   private short[] symbolKeyX;

   /** Constructs the default keypad */
   private Keypad()
   {
      setFocusLess(true);
      setVisible(false);
      ke.type = KeyEvent.KEY_PRESS;
      setKeys(defaultKeyset);
      modeChanged();
      backColor = UIColors.keypadBack;
      foreColor = UIColors.keypadFore;
   }

   /** Call this, passing a new set, to change the keys associated with each pad number. Or pass null to restore the default key set. */
   public void setKeys(String[] newKeyset)
   {
      if (newKeyset == null)
         newKeyset = defaultKeyset;
      int ni = newKeyset == null ? 0 : newKeyset.length,i,j,w;
      posxL = new short[ni][];
      posxU = new short[ni][];
      charsL = new char[ni][];
      charsU = new char[ni][];

      for (i = ni-1; i >= 0; i--)
      {
         String keys = newKeyset[i];
         if (keys != null)
         {
            int nj = keys.length();
            posxL[i] = new short[nj+1];
            posxU[i] = new short[nj+1];
            charsL[i] = keys.toCharArray();
            charsU[i] = keys.toUpperCase().toCharArray();
            short[]pxl = posxL[i];
            short[]pxu = posxU[i];
            for (j = 0; j < nj; j++)
            {
               w = fm.charWidth(charsL[i][j]);
               pxl[j+1] = (short)(pxl[j]+w+2);
               w = fm.charWidth(charsU[i][j]);
               pxu[j+1] = (short)(pxu[j]+w+2);
            }
         }
      }
      hashW = fm.charWidth('#');

      modeChanged(); // guich@573_31
   }

   /** Call this to change the default # to a set of chars to be selected.
    * Remember to set it to null afterward to restore the original behaviour!
    */
   public void setSymbolKeys(String nk) // guich@573_33
   {
      if (nk == null)
         nk = defaultSymbolKeyset;
      
      if (nk == null)
      {
         symbolKeyX = null;
         symbolKeys = null;
      }
      else
      {
         symbolKeys = nk.toCharArray();
         symbolKeyX = new short[symbolKeys.length+1];
         short[] pxl = symbolKeyX;
         for (int j= 0; j < symbolKeys.length; j++)
         {
            int w = fm.charWidth(symbolKeys[j]);
            pxl[j+1] = (short)(pxl[j]+w+4);
         }
      }
   }

   /** Assigns the set based on the current mode (upper or lower) */
   private void modeChanged()
   {
      chars = (mode == MODE_LOWER) ? charsL : charsU;
      posx  = (mode == MODE_LOWER) ? posxL  : posxU;
   }

   /** Saves the screen under this control */
   private void saveBack()
   {
      if (backImg == null || backImg.getWidth() != Settings.screenWidth) // create image if not created yet or if screen width has changed
      {
         // create the offscreen image that will hold the background
         try
         {
            backImg = new Image(Settings.screenWidth, fmH+2);
            backG = backImg.getGraphics();
         } catch (ImageException oome) {backImg = null;}
      }
      
      if (backImg != null)
         backG.copyRect(MainWindow.mainWindowInstance, 0, this.y, Settings.screenWidth, this.height, 0,0); // save the whole row, fdie@57_23
   }

   /** Restores the saved screen */
   private void restoreBack()
   {
      if (backImg != null)
         MainWindow.getMainWindow().getGraphics().copyRect(backImg, this.x, 0, this.width, this.height, this.x, this.y); // restore only the changed area, fdie@57_23
      else
         MainWindow.repaintActiveWindows();
   }

   /** Removes the close timeout timer */
   private void removeTimer()
   {
      if (timer != null)
      {
         removeTimer(timer);
         timer = null;
      }
   }
   
   private void addTimer()
   {
      if (timer != null)
         removeTimer(timer);
      timer = addTimer(CLOSE_TIMEOUT);
   }

   /** Closes this control, removing the timer and restoring the back */
   private void close()
   {
      removeTimer();
      setVisible(false);
      restoreBack();
   }

   /** Repositions this control, based on the current mode. */
   private void reposition(int chance) // guich@573_34: replaced the boolean by a counter, to avoid infinite recursion
   {
      if (chance == 0)
         MainWindow.getMainWindow().add(this); // always re-add the keypad when repositioning
      
   	int ypos = chance == 1 ? Y_ALIGN_ALT : Y_ALIGN; // use the standard y position or the alternate y position if the keypad overlaps the focused control
      if (mode == MODE_NUMBER)
         setRect(X_ALIGN, ypos, hashW+4, fmH+2,null,true);
      else
      {
         short[]px = lastKey == symbolsTriggerChar ? symbolKeyX : posx[lastKey - firstChar]; // guich@573_33
         setRect(X_ALIGN, ypos, px[px.length-1]+2, fmH+2,null,true);
      }

      // now check an overlapping between the keypad control and the control that has the focus. Note: the control that has focus is not always the area that is modified
      Control focus = Window.topMost.getFocus();
      if (focus != null && focus.getRect().intersects(getRect()) && chance < 3)
      	reposition(chance+1); // force alternate position
      else
      {
	      if (!visible)
	         saveBack();
	      setVisible(true);
	      Window.getTopMost().validate(); // freeze the refresh of the parent's window
	      repaintNow();
      }
   }

   /** Called by the system to handle the pressed key */
   public boolean handleKey(int key)
   {
      boolean wasVisible = visible;
      if (wasVisible && key != lastKey) // always post pending key events if changing keys
      {
         if (key >= 97 && key <= 122 && (key - 32) == lastKey) // current key is the lower version of last key, so consider both the same (affects BlackBerry only)
            key = lastKey;
         else // current key is different from last key, close keypad and post the pending key event
            timerTriggered();
      }
      
      if (key != backspaceChar && key != symbolsTriggerChar && (key < firstChar || key > lastChar || (key - firstChar) >= chars.length || chars[key - firstChar] == null))
         return false;
      
      // change key to index
      if (key == symbolsTriggerChar && symbolKeys != null && symbolKeys.length == 1) // guich@573_33
         postKeyEvent(symbolKeys[0]);
      else
      if (key == symbolsTriggerChar && symbolKeys == null)
      {
         mode = (mode+1) % 3;
         modeChanged();
         if (wasVisible)
         {
            reposition(0);
            addTimer();
         }
      }
      else
      if (firstChar <= key && key <= lastChar && chars[key - firstChar] != null && chars[key - firstChar].length == 1) // guich@573_32
         postKeyEvent(chars[key - firstChar][0]); // post current key
      else
      if (mode == MODE_NUMBER || key == backspaceChar) // just post the key and exit
         postKeyEvent(key == backspaceChar ? SpecialKeys.BACKSPACE : key); // back has precedence over number
      else
      {
         // restart the timer used to close us
         addTimer();

         // show ourself
         if (!wasVisible)
            lastKey = -1;

         // if we had changed keys
         if (key != lastKey)
         {
            lastIndex = 0;
            lastKey = key;
            reposition(0);
         }
         else
         if (wasVisible)
         {
            drawCursor();
            if (++lastIndex == (key == symbolsTriggerChar ? symbolKeys.length : chars[key - firstChar].length)) // move to the next char - guich@573_33: handle numberKeys
               lastIndex = 0;
            drawCursor();
         }
      }

      return true;
   }

   /** Draws the cursor over the selected letter */
   private void drawCursor()
   {
      short[]px = lastKey == symbolsTriggerChar ? symbolKeyX : posx[lastKey - firstChar]; // guich@573_33: handle numberKeys
      int xx = px[lastIndex];
      MainWindow.getMainWindow().getGraphics().fillCursor(xx+1+this.x, this.y+1, px[lastIndex+1]-xx, fmH); //fdie@57_23
   }

   /** Posts a KeyEvent for the current focused control */
   private void postKeyEvent(int c)
   {
      Control focus = Window.topMost.getFocus();
      if (focus != null)
      {
         ke.target = focus;
         ke.key = c;
         focus.postEvent(ke);
      }
   }

   /** Paints this control */
   public void onPaint(Graphics g)
   {
      g.backColor = backColor;
      g.foreColor = foreColor;
      g.fillRect(0,0,width,height);
      g.drawRect(0,0,width,height);

      if (mode == MODE_NUMBER)
         g.drawText("#",2,2, textShadowColor != -1, textShadowColor);
      else
      if (lastKey >= 0 || lastKey == symbolsTriggerChar)
      {
         char[] chars = lastKey == symbolsTriggerChar ? this.symbolKeys : this.chars[lastKey - firstChar]; // guich@573_33
         short[] px = lastKey == symbolsTriggerChar ? this.symbolKeyX : posx[lastKey - firstChar];

         int n = chars.length;
         for (int i = 0; i < n; i++)
            g.drawText(chars, i, 1, px[i]+3,1, textShadowColor != -1, textShadowColor);
         drawCursor();
      }
   }

   /** On a timeout of the close timer, closes this control */
   public void onEvent(Event e)
   {
      if (e.type == TimerEvent.TRIGGERED && timer != null && timer.triggered)
         timerTriggered();
   }
   
   private void timerTriggered()
   {
      close();
      if (lastKey == symbolsTriggerChar)
         postKeyEvent(symbolKeys[lastIndex]);
      else
      if (mode != MODE_NUMBER && lastKey >= 0 && lastIndex >= 0)
         postKeyEvent(chars[lastKey - firstChar][lastIndex]);
   }
}
