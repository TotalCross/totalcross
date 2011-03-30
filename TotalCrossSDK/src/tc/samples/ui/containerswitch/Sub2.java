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

// $Id: Sub2.java,v 1.10 2011-01-04 13:19:17 guich Exp $

package tc.samples.ui.containerswitch;

/** An example that shows how to switch between
  * containers in the same application
  */

import totalcross.ui.*;
import totalcross.ui.event.*;

public class Sub2 extends Container
{
	Button btnGoBack;

   public Sub2()
   {
  		setBorderStyle(BORDER_RAISED);
      transitionEffect = TRANSITION_CLOSE;
      setBackColor(0xAAAAFF);
  	}

  	public void initUI()
  	{
      add(new Label("Container 2"),CENTER,CENTER);
      add(btnGoBack = new Button("Back to main"), CENTER,BOTTOM-5);
   }

   // Called by the system to pass events to the application.
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == btnGoBack)
         {
            nextTransitionEffect = TRANSITION_OPEN;
            MainWindow.getMainWindow().swap(null);
         }
      }
   }
}