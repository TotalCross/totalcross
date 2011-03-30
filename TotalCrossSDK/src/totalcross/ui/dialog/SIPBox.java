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

// $Id: SIPBox.java,v 1.7 2011-03-24 18:41:38 guich Exp $

package totalcross.ui.dialog;

import totalcross.ui.*;
import totalcross.ui.event.*;

/** A place where an Edit or other control will be placed when a SIP is shown. 
 * Controls placed under the SIP will be displayed inside this box.
 * <br><br>
 * Only Edit and MultiEdit controls are supported.
 * <br><br>
 * The window background color is the same of the control's parent.
 * 
 * @since TotalCross 1.27 
 */

public class SIPBox extends Window // guich@tc126_21
{
   private Control destControl, thisControl;
   /** The title of the SIPBox. */
   public static String title = " Soft Input Panel ";
   private int kbdType,mode=-1;

   public SIPBox()
   {
      super(title,RECT_BORDER);
      fadeOtherWindows = false;
      transitionEffect = TRANSITION_NONE;
      highResPrepared = true;
   }

   // 1
   protected void onPopup()
   {
      removeAll();
      mode = -1;
      destControl = Window.topMost.getFocus();
      if (destControl instanceof Edit)
      {
         Edit destEdit = (Edit)destControl;
         thisControl = destEdit.getCopy();
         Edit thisEdit = (Edit)thisControl;
         
         mode = destEdit.getMode();
         kbdType = destEdit.getKeyboardType();
         thisEdit.setKeyboard(Edit.KBD_NONE); // without this, the keyboard is closed
         destEdit.setKeyboard(Edit.KBD_NONE);
         thisEdit.setText(destEdit.getTextWithoutMask());
      }
      else
      {
         MultiEdit destEdit = (MultiEdit)destControl;
         thisControl = destEdit.getCopy();
         MultiEdit thisEdit = (MultiEdit)thisControl;
         
         kbdType = destEdit.getKeyboardType();
         thisEdit.setKeyboard(Edit.KBD_NONE); // without this, the keyboard is closed
         destEdit.setKeyboard(Edit.KBD_NONE);
         thisEdit.setText(destEdit.getText());
      }
      
      setBackColor(destControl.getParent().getBackColor());

      setRect(LEFT,TOP,FILL,FILL);
      add(thisControl, LEFT,TOP+2,FILL,destControl.getHeight());
   }

   // 2
   protected void postPopup()
   {
      thisControl.requestFocus();
      Window.setSIP(Window.SIP_SHOW, thisControl, mode == Edit.PASSWORD || mode == Edit.PASSWORD_ALL);
   }

   // 3
   protected void onUnpop()
   {
      destControl._onEvent(new ControlEvent(KeyboardBox.KEYBOARD_ON_UNPOP, destControl)); // guich@320_34
      if (thisControl instanceof Edit)
      {
         Edit destEdit = (Edit)destControl;
         destEdit.setKeyboard((byte)kbdType);
         destEdit.setText(((Edit)thisControl).getTextWithoutMask());
      }
      else
      {
         MultiEdit destEdit = (MultiEdit)destControl;
         destEdit.setKeyboard((byte)kbdType);
         destEdit.setText(((MultiEdit)thisControl).getText());
      }
      Window.setSIP(Window.SIP_HIDE,null,false);
   }

   // 4
   protected void postUnpop()
   {
      destControl._onEvent(new ControlEvent(KeyboardBox.KEYBOARD_POST_UNPOP, destControl)); // guich@320_34
      postPressedEvent();
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case KeyEvent.SPECIAL_KEY_PRESS: // close if Edit with ENTER key
            if (thisControl instanceof Edit && ((KeyEvent)event).isActionKey())
               unpop();
            break;
         case PenEvent.PEN_UP:
            if (event.target != null && event.target != thisControl && ((Control)event.target).getParent() != thisControl)
               unpop();
            break;
      }
   }
}
