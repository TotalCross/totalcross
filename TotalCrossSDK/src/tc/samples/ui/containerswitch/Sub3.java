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

// $Id: Sub3.java,v 1.15 2011-01-04 13:19:17 guich Exp $

package tc.samples.ui.containerswitch;

import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

/** An example that shows how to switch between
  * containers in the same application
  */

public class Sub3 extends Container
{
	MessageBox mbox;

   public Sub3()
   {
      // set border style here, in the constructor
  		setBorderStyle(BORDER_SIMPLE);
      transitionEffect = TRANSITION_NONE;
      focusTraversable = true; // this line is important so that this container can receive the event indicating that the MessageBox closed.
  	}

  	public void initUI() // since SW3.4
  	{
      add(new Label("Container 3"),CENTER,CENTER);
   }

   // Called by the system to pass events to the application.
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         switch (mbox.getPressedButtonIndex())
         {
            case 0 : MainWindow.getMainWindow().swap(null);
                     break;
         }
         mbox = null; // prepare to the next time we get called
      }
   }

   // here we popup a window as soon as we get called
   // Note: there is no other way to do this.
   public void paintChildren()
   {
      if (mbox == null)
         (mbox = new MessageBox("Alert","Click to unswap")).popupNonBlocking();
   }
}