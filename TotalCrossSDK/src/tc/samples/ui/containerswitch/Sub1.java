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



package tc.samples.ui.containerswitch;

/** An example that shows how to switch between
  * containers in the same application
  */

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class Sub1 extends Container
{
	Button btnGoBack;

   public Sub1()
   {
  		setBorderStyle(BORDER_LOWERED);
  		transitionEffect = TRANSITION_OPEN;
      setBackColor(Color.CYAN);
  	}

  	public void initUI()
  	{
      add(new Label("Container 1"),CENTER,CENTER);
      // without the setRect above, adding this control to the
      // right would make it disappear!
      add(btnGoBack = new Button("Back to main"), RIGHT,BOTTOM-5);
   }

   // Called by the system to pass events to the application.
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == btnGoBack)
         {
            nextTransitionEffect = TRANSITION_CLOSE;
         	MainWindow.getMainWindow().swap(null);
         }
      }
   }
}