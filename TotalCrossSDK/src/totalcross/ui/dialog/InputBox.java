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



package totalcross.ui.dialog;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

/** This class pops up a Window with a Label, an Edit and some buttons. Good
    to input some text from the user.
    Here is an example (taken from examples/apps/GuiBuilder):
    <pre>
    // on some event...
      if (renameDialog == null)
         renameDialog = new InputDialog("Project Rename","Please enter the new name which will be used for the project:","");
      renameDialog.setValue(projectName);
      renameDialog.popup();
      // when window closes...
      if (renameDialog.getPressedButtonIndex() == 0) // ok?
      {
         projectName = renameDialog.getValue();
      }
    </pre>
  */

public class InputBox extends Window
{
   private Label msg;
   private PushButtonGroup btns;
   private Edit ed;
   private int selected = -1;
   private String originalText;
   private String[] buttonCaptions;
   
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
    * Note that if you use the default Ok/Cancel buttons, this mapping is already done.
    * @since TotalCross 1.27
    */
   public int[] buttonKeys; // guich@tc126_40

   /** Creates a new InputDialog with the given window Title,
     * the given label Text, the given Default value for the Edit,
     * and two buttons "Ok" and "Cancel".
     * The text used in a Label can be multi-line. If the text is too big, it will be splitted.
     */
   public InputBox(String title, String text, String defaultValue)
   {
      this(title, text, defaultValue, new String[]{"Ok","Cancel"});
      buttonKeys = new int[]{SpecialKeys.ENTER,SpecialKeys.ESCAPE};
   }

   /** Creates a new InputDialog with the given window Title,
     * the given label Text, the given Default value for the Edit
     * and with the given buttons.
     * The text used in a Label can be multi-line. If the text is too big, it will be splitted.
     */
   public InputBox(String title, String text, String defaultValue, String[] buttonCaptions)
   {
      super(title,ROUND_BORDER);
      this.buttonCaptions = buttonCaptions;
      fadeOtherWindows = Settings.fadeOtherWindows;
      transitionEffect = Settings.enableWindowTransitionEffects ? TRANSITION_OPEN : TRANSITION_NONE;
      highResPrepared = true;
      this.originalText = text.replace('|','\n'); // guich@tc100: now we use \n instead of |
      ed = new Edit("@@@@@@@@@@");
      if (defaultValue != null) ed.setText(defaultValue);
   }

   protected void onPopup()
   {
      removeAll();
      String text = originalText;
      if (text.indexOf('\n') < 0 && fm.stringWidth(text) > Settings.screenWidth-6) // guich@tc100: automatically split the text if its too big to fit screen
         text = Convert.insertLineBreak(Settings.screenWidth-6, fm, text.replace('\n',' '));
      msg = new Label(text,Label.LEFT);
      msg.setFont(font);
      ed.setFont(font);
      btns = new PushButtonGroup(buttonCaptions,false,-1,4,6,1,false,PushButtonGroup.BUTTON);
      btns.setFont(font);
      int wb = btns.getPreferredWidth();
      if (wb > Settings.screenWidth-10) // guich@tc123_38: buttons too large? place them in a single column
      {
         btns = new PushButtonGroup(buttonCaptions,false,-1,4,6,buttonCaptions.length,true,PushButtonGroup.BUTTON);
         btns.setFont(font);
         wb = btns.getPreferredWidth();
      }
      
      int hb = btns.getPreferredHeight();
      int wm = Math.min(msg.getPreferredWidth()+1,Settings.screenWidth-6);
      int hm = msg.getPreferredHeight();
      int we = ed.getPreferredWidth();
      int he = ed.getPreferredHeight();
      FontMetrics fm2 = titleFont.fm; // guich@220_28
      int captionH = fm2.height+10;

      int h = captionH + hb + hm + he;
      int w = Math.max(Math.max(Math.max(wb,wm),we),fm2.stringWidth(title!=null?title:""))+6; // guich@200b4_29
      w = Math.min(w,Settings.screenWidth); // guich@200b4_28: dont let the window be greater than the screen size
      setRect(CENTER,yPosition,w,h);
      add(msg);
      add(btns);
      add(ed);
      msg.setRect(4,TOP,wm,hm);
      ed.setRect(CENTER,AFTER+2,we,he);
      btns.setRect(CENTER,AFTER+2,wb,hb);
      if (Settings.isColor)
      {
         setBackForeColors(UIColors.inputboxBack, UIColors.inputboxFore);
         msg.setBackForeColors(backColor, foreColor); // guich@tc115_9: moved to here
         if (btns != null) btns.setBackForeColors(UIColors.inputboxAction,Color.getBetterContrast(UIColors.inputboxAction, foreColor, backColor)); // guich@tc123_53
      }
   }

   public void reposition()
   {
      onPopup();
   }

   protected void postPopup()
   {
      ed.requestFocus();
      if (Settings.keyboardFocusTraversable) // guich@570_39: use this instead of pen less
         isHighlighting = false; // allow a direct click to dismiss this dialog
   }

   protected void postUnpop()
   {
      if (Settings.keyboardFocusTraversable) // guich@573_1: put back the highlighting state
         isHighlighting = true;
      if (selected != -1)
         postPressedEvent(); // guich@580_27
   }

   /** handle scroll buttons and normal buttons */
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case KeyEvent.KEY_PRESS:
         case KeyEvent.SPECIAL_KEY_PRESS:
            if (buttonKeys != null)
            {
               int k = ((KeyEvent)e).key;
               for (int i = buttonKeys.length; --i >= 0;)
                  if (buttonKeys[i] == k)
                  {
                     btns.setSelectedIndex(i);
                     close();
                     break;
                  }
            }
            break;
         case ControlEvent.PRESSED:
            if (e.target == btns && btns.getSelectedIndex() != -1)
               close();
            break;
      }
   }

   private void close()
   {
      selected = btns.getSelectedIndex();
      btns.requestFocus(); // remove focus from the edit
      btns.setSelectedIndex(-1);
      unpop();
   }

   /** Returns the pressed button index, starting from 0 */
   public int getPressedButtonIndex()
   {
      return selected;
   }

   /** Returns the value entered */
   public String getValue()
   {
   	return ed.getText();
   }
   /** Sets the default value on the Edit field */
   public void setValue(String value)
   {
   	ed.setText(value);
   }
   /** Returns the Edit so you can set its properties. */
   public Edit getEdit() // guich@310_23
   {
      return ed;
   }
}