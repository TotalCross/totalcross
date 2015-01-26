/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
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

package totalcross.ui.dialog;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.*;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/** This class implements a scrollable message box window with customized buttons, delayed
  * unpop and scrolling text.
  * <br>for example, to create an automatic unpop after 5 seconds, do:
  * <pre>
  *   MessageBox mb = new MessageBox("TotalCross","TotalCross is the most exciting tool for developing totally cross-platform programs.",null);
  *   mb.setUnpopDelay(5000);
  *   mb.popup(mb);
  * </pre>
  */

public class MessageBox extends Window
{
   protected Label msg;
   public PushButtonGroup btns;
   private int selected = -1;
   private boolean hasScroll;
   protected int xa,ya,wa,ha; // arrow coords
   private TimerEvent unpopTimer,buttonTimer;
   private boolean oldHighlighting;
   private static String[] ok = {"Ok"};
   private int captionCount;
   private String originalText;
   private int labelAlign = CENTER;
   private String[] buttonCaptions;
   private int gap, insideGap;
   private Image icon;
   private ScrollContainer sc;
   protected int lgap;
   
   /**
    * Set at the object creation. if true, all the buttons will have the same width, based on the width of the largest
    * one.<br>
    * Default value is false.
    * 
    * @since TotalCross 1.27
    */
   private boolean allSameWidth; //flsobral@tc126_50: set to make all buttons to always have the same width.
   
   /** Defines the y position on screen where this window opens. Can be changed to TOP or BOTTOM. Defaults to CENTER.
    * @see #CENTER
    * @see #TOP
    * @see #BOTTOM
    */
   public int yPosition = CENTER; // guich@tc110_7

   /** If you set the buttonCaptions array in the construction, you can also set this
    * public field to an int array of the keys that maps to each of the buttons.
    * For example, if you set the buttons to {"Ok","Cancel"}, you can map the enter key
    * for the Ok button and the escape key for the Cancel button by assigning:
    * <pre>
    * buttonKeys = new int[]{SpecialKeys.ENTER,SpecialKeys.ESCAPE};
    * </pre>
    * Note that ENTER is also handled as ACTION, since the ENTER key is mapped to ACTION under some platforms.
    * @since TotalCross 1.27
    */
   public int[] buttonKeys; // guich@tc126_40
   
   /**
    * Constructs a message box with the text and one "Ok" button. The text may be separated by '\n' as the line
    * delimiters; otherwise, it is automatically splitted if its too big to fit on screen.
    */
   public MessageBox(String title, String msg)
   {
      this(title, msg, ok, false, 4, 6);
   }

   /**
    * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. if buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method
    */
   public MessageBox(String title, String text, String[] buttonCaptions)
   {
       this(title, text, buttonCaptions, false, 4, 6);
   }
   
   /**
    * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method. The parameters allSameWidth is the same as in the constructor for PushButtonGroup.
    * 
    * @since TotalCross 1.27
    */   
   public MessageBox(String title, String text, String[] buttonCaptions, boolean allSameWidth)
   {
      this(title, text, buttonCaptions, allSameWidth, 4, 6);
   }
   
   /**
    * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method. The new parameters gap and insideGap are the same as in the constructor for PushButtonGroup.
    * 
    * @since SuperWaba 4.11
    */   
   public MessageBox(String title, String text, String[] buttonCaptions, int gap, int insideGap)
   {
      this(title, text, buttonCaptions, false, gap, insideGap);
   }

   /**
    * Constructs a message box with the text and the specified button captions. The text may be separated by '\n' as the
    * line delimiters; otherwise, it is automatically splitted if its too big to fit on screen. If buttonCaptions is
    * null, no buttons are displayed and you must dismiss the dialog by calling unpop or by setting the delay using
    * setUnpopDelay method. The parameters allSameWidth, gap and insideGap are the same as in the constructor for PushButtonGroup.
    * 
    * @since TotalCross 1.27
    */
   public MessageBox(String title, String text, String[] buttonCaptions, boolean allSameWidth, int gap, int insideGap) // andrew@420_5
   {
      super(title,ROUND_BORDER);
      this.buttonCaptions = buttonCaptions;
      this.gap = gap;
      this.insideGap = insideGap;
      this.allSameWidth = allSameWidth;
      if (!Settings.onJavaSE && Settings.vibrateMessageBox) // guich@tc122_51
         Vm.vibrate(200);
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      fadeOtherWindows = Settings.fadeOtherWindows;
      transitionEffect = Settings.enableWindowTransitionEffects ? TRANSITION_FADE : TRANSITION_NONE;
      ha = 6 * Settings.screenHeight/160; // guich@450_24: increase arrow size if screen size change
      wa = ha*2+1; // guich@570_52: now wa is computed from ha
      if (text == null)
         text = "";
      this.originalText = text; // guich@tc100: now we use \n instead of |
      if ((Settings.onJavaSE && Settings.screenWidth == 240) || Settings.isWindowsDevice()) // guich@tc110_53
         setFont(font.asBold());
   }

   /** This method can be used to set the text AFTER the dialog was shown. However, the dialog will not be resized.
    * @since TotalCross 1.3
    */
   public void setText(String text)
   {
      int maxW = Settings.screenWidth-fmH - lgap;
      originalText = text;
      if (text.indexOf('\n') < 0 && fm.stringWidth(text) > maxW) // guich@tc100: automatically split the text if its too big to fit screen
         text = Convert.insertLineBreak(maxW, fm, text.replace('\n',' '));
      msg.setText(text);
      msg.repaintNow();
   }
   
   protected void onPopup()
   {
      removeAll();
      int maxW = Settings.screenWidth-fmH - lgap;
      String text = originalText;
      if (text.indexOf('\n') < 0 && fm.stringWidth(text) > maxW) // guich@tc100: automatically split the text if its too big to fit screen
         text = Convert.insertLineBreak(maxW, fm, text.replace('\n',' '));
      msg = new Label(text,labelAlign);
      msg.setFont(font);
      int wb,hb;
      int androidGap = uiAndroid ? fmH/3 : 0;
      if (androidGap > 0 && (androidGap&1) == 1) androidGap++;
      boolean multiRow = false;
      if (buttonCaptions == null)
         wb = hb = 0;
      else
      {
         captionCount = buttonCaptions.length;
         btns = new PushButtonGroup(buttonCaptions,false,-1,gap,insideGap,1,allSameWidth || uiAndroid,PushButtonGroup.BUTTON);
         btns.setFont(font);
         wb = btns.getPreferredWidth();
         if (wb > Settings.screenWidth-10) // guich@tc123_38: buttons too large? place them in a single column
         {
            multiRow = true;
            btns = new PushButtonGroup(buttonCaptions,false,-1,gap,insideGap,captionCount,true,PushButtonGroup.BUTTON);
            btns.setFont(font);
            wb = btns.getPreferredWidth();
         }
         hb = btns.getPreferredHeight() + (multiRow ? insideGap*buttonCaptions.length : insideGap);
         hb += androidGap;
      }
      int wm = Math.min(msg.getPreferredWidth()+(uiAndroid?fmH:1),maxW);
      int hm = msg.getPreferredHeight();
      FontMetrics fm2 = titleFont.fm; // guich@220_28
      int iconH = icon == null ? 0 : icon.getHeight();
      int iconW = icon == null ? 0 : icon.getWidth();
      boolean removeTitleLine = uiAndroid && borderStyle == ROUND_BORDER && (title == null || title.length() == 0);
      if (removeTitleLine) titleGap = 0;
      else
      if (uiAndroid) hm += fmH;
      int captionH = (removeTitleLine ? 0 : Math.max(iconH,fm2.height)+titleGap)+8;
      int ly = captionH - 6;
      if (captionH+hb+hm > Settings.screenHeight) // needs scroll?
      {
         if (hb == 0) hb = ha;
         hm = Math.max(fmH,Settings.screenHeight - captionH - hb - ha);
         hasScroll = true;
      }
      else 
      if (removeTitleLine) 
         ly = androidBorderThickness+1;
      int h = captionH + hb + hm;
      if (uiAndroid) h += fmH/2;
      int w = lgap + Math.max(Math.max(wb,wm),(iconW > 0 ? iconW+fmH : 0) + fm2.stringWidth(title!=null?title:""))+7; // guich@200b4_29 - guich@tc100: +7 instead of +6, to fix 565_11
      w = Math.min(w,Settings.screenWidth); // guich@200b4_28: dont let the window be greater than the screen size
      setRect(CENTER,yPosition,w,h);
      if (!removeTitleLine && icon != null)
      {
         titleAlign = LEFT+fmH/2+iconW+fmH/2;
         ImageControl ic = new ImageControl(icon);
         ic.transparentBackground = true;
         add(ic,LEFT+fmH/2,(captionH-iconH)/2 - titleFont.fm.descent);
      }
      if (!uiAndroid || !hasScroll)
         add(msg);
      else
      {
         add(sc = new ScrollContainer(false,true), LEFT+2+lgap,btns == null ? CENTER : ly+2,FILL-2,hm-2);
         sc.add(msg,LEFT,TOP,FILL,PREFERRED);
         hasScroll = false;
      }
      if (btns != null) add(btns);
      if (sc == null)
         msg.setRect(LEFT+2+lgap,btns == null ? CENTER : ly,FILL-2,hm); // guich@350_17: replaced wm by client_rect.width - guich@565_11: -2
      if (btns != null)
      {
         if (uiAndroid && !multiRow)
            btns.setRect(buttonCaptions.length > 1 ? LEFT+3 : CENTER,ly+hm+androidGap/2,buttonCaptions.length > 1 ? FILL-3 : Math.max(w/3,wb),FILL-2);
         else
            btns.setRect(CENTER,ly+2+hm+androidGap/2,wb,hb-androidGap);
      }
      Rect r = sc != null ? sc.getRect() : msg.getRect();
      xa = r.x+r.width-(wa << 1);
      ya = btns != null ? (btns.getY()+(btns.getHeight()-ha)/2) : (r.y2()+3); // guich@570_52: vertically center the arrow buttons if the ok button is present
      if (backColor == UIColors.controlsBack) // guich@tc110_8: only change if the color was not yet set by the user
         setBackColor(UIColors.messageboxBack);
      if (foreColor == UIColors.controlsFore)
         setForeColor(UIColors.messageboxFore);
      msg.setBackForeColors(backColor, foreColor);
      if (btns != null)
      {
         btns.setBackForeColors(UIColors.messageboxAction,Color.getBetterContrast(UIColors.messageboxAction, foreColor, backColor)); // guich@tc123_53
         if (uiAndroid && !removeTitleLine) footerH = height - (sc != null ? sc.getY2()+2 : msg.getY2()) - 1;
         if (buttonTimer != null)
            btns.setVisible(false);
      }
   }

   public void reposition()
   {
      onPopup();
   }

   /** Set an icon to be shown in the MessageBox's title, at left. 
    * It only works if there's a title. If you really need an empty title, pass as title a 
    * String with a couple of spaces, like " ".
    * 
    * The icon's width and height will be set to title's font ascent.
    * @since TotalCross 1.3
    */
   public void setIcon(Image icon) throws ImageException
   {
      this.icon = icon.getSmoothScaledInstance(titleFont.fm.ascent,titleFont.fm.ascent);
   }
   
   /** Sets the alignment for the text. Must be CENTER (default), LEFT or RIGHT */
   public void setTextAlignment(int align)
   {
      labelAlign = align; // guich@241_4
   }

   /** sets a delay for the unpop of this dialog */
   public void setUnpopDelay(int unpopDelay)
   {
      if (unpopDelay <= 0)
         throw new IllegalArgumentException("Argument 'unpopDelay' must have a positive value");
      if (unpopTimer != null)
         removeTimer(unpopTimer);
      unpopTimer = addTimer(unpopDelay);
   }

   public void onPaint(Graphics g)
   {
      if (hasScroll)
      {
         g.drawArrow(xa,ya,ha,Graphics.ARROW_UP,false,msg.canScroll(false) ? foreColor : Color.getCursorColor(foreColor)); // guich@200b4_143: msg.canScroll
         g.drawArrow(xa+wa,ya,ha,Graphics.ARROW_DOWN,false,msg.canScroll(true) ? foreColor : Color.getCursorColor(foreColor));
      }
   }

   /** handle scroll buttons and normal buttons */
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case TimerEvent.TRIGGERED:
            if (buttonTimer != null && buttonTimer.triggered)
            {
               removeTimer(buttonTimer);
               if (btns != null)
                  btns.setVisible(true);
            }
            else  
            if (e.target == this)
            {
               removeTimer(unpopTimer);
               if (popped) // luciana@570_25 - Maybe the unpop was already called (the user can click OK button before the delay expires)
               	unpop();
            }
            break;
         case PenEvent.PEN_DOWN:
            if (hasScroll)
            {
               int px=((PenEvent)e).x;
               int py=((PenEvent)e).y;

               if (ya <= py && py <= ya+ha && xa <= px && px < xa+(wa<<1) && msg.scroll((px-xa)/wa != 0)) // at the arrow points?
                  Window.needsPaint = true;
               else
               if (msg.isInsideOrNear(px,py) && msg.scroll(py > msg.getHeight()/2))
                  Window.needsPaint = true;
            }
            break;
         case KeyEvent.SPECIAL_KEY_PRESS: // guich@200b4_42
            KeyEvent ke = (KeyEvent)e;
            if (ke.isUpKey()) // guich@330_45
            {
               msg.scroll(false);
               Window.needsPaint = true; // guich@300_16: update the arrow's state
            }
            else
            if (ke.isDownKey()) // guich@330_45
            {
               msg.scroll(true);
               Window.needsPaint = true; // guich@300_16: update the arrow's state
            }
            else
            if (!Settings.keyboardFocusTraversable && captionCount == 1 && ke.isActionKey()) // there's a single button and the enter key was pressed?
            {
               selected = 0;
               unpop();
            }
            else
            if (buttonKeys != null && captionCount > 0)
            {
               int k = ke.key;
               for (int i = buttonKeys.length; --i >= 0;)
                  if (buttonKeys[i] == k || (buttonKeys[i] == SpecialKeys.ENTER && k == SpecialKeys.ACTION)) // handle ENTER as ACTION too
                  {
                     selected = i;
                     btns.setSelectedIndex(i);
                     unpop();
                     break;
                  }
            }
            break;
         case ControlEvent.PRESSED:
            if (e.target == btns && (selected=btns.getSelectedIndex()) != -1)
            {
               btns.setSelectedIndex(-1);
               unpop();
            }
            break;
      }
   }

   /** Returns the pressed button index, starting from 0 */
   public int getPressedButtonIndex()
   {
      return selected;
   }

   protected void postPopup()
   {
      if (Settings.keyboardFocusTraversable) // guich@570_39: use this instead of pen less
      {
         if (btns != null) // guich@572_
         {
            btns.requestFocus(); // without a pen, select the first button
            btns.setSelectedIndex(0); // bcao@421_55 Added default control selection at dialog opening
         }
         oldHighlighting = isHighlighting;
         isHighlighting = false; // allow a direct click to dismiss this dialog
      }
   }

   protected void postUnpop()
   {
      if (Settings.keyboardFocusTraversable) // guich@573_1: put back the highlighting state
         isHighlighting = oldHighlighting;
      postPressedEvent(); // guich@580_27
   }

   /** Title shown in the showException dialog. */
   public static String showExceptionTitle = "Exception Thrown"; // guich@tc113_8
   
   /** Shows the exception, with its name, message and stack trace in a new MessageBox.
    * @since TotalCross 1.0
    */
   public static void showException(Throwable t, boolean dumpToConsole)
   {
      String exmsg = t.getMessage();
      exmsg = exmsg == null ? "" : "Message: "+t.getMessage()+"\n";
      String msg = "Exception: "+t.getClass()+"\n"+exmsg+"Stack trace:\n"+Vm.getStackTrace(t);
      if (dumpToConsole)
         Vm.debug(msg);
      MessageBox mb = new MessageBox(showExceptionTitle,"");
      mb.originalText = Convert.insertLineBreak(Settings.screenWidth-mb.fmH, mb.font.fm, msg);
      mb.labelAlign = LEFT;
      mb.popup();
   }

   protected void onFontChanged()
   {

   }

   /** Calling this method will make the buttons initially hidden and will show them after
    * the specified number of milisseconds.
    * 
    * Here's a sample:
    * <pre>
    * MessageBox mb = new MessageBox("Novo Tweet!",tweet);
    * mb.setTimeToShowButton(7000);
    * mb.popup();
    * </pre>
    * @since TotalCross 1.53
    */ 
   public void setDelayToShowButton(int ms)
   {
      buttonTimer = addTimer(ms);      
   }
}