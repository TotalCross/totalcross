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



package tc.samples.ui.containerswitch;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;

/** An example that shows how to switch between
  * containers in the same application
  */

public class MainContainer extends MainWindow
{
	Button btnGo1, btnGo2, btnGo3;
	Sub1 sub1;
	Sub2 sub2;
	Sub3 sub3;
	MenuBar menu;

   public MainContainer()
   {
      super("Container Switch",TAB_ONLY_BORDER);
   }

   public void initUI()
   {
   	// Note: we add stuff to another container instead of
   	// to this MainWindow because Containers in TotalCross are
   	// "transparent". So, adding another container to the current
   	// one would make the controls in the MainWindow AND in the added
   	// container visible.
   	Container main = new Container();
      swap(main);
      main.add(btnGo2 = new Button("Go Sub2"), CENTER,CENTER);
      main.add(btnGo1 = new Button("Go Sub1"), BEFORE-5,SAME);
      main.add(btnGo3 = new Button("Go Sub3"), AFTER+5,SAME,btnGo2);
      main.tabOrder = new Vector(new Control[]{btnGo1,btnGo2,btnGo3}); // reassign the order

      // make this the main container for swapping

      MenuItem col0[] = {new MenuItem("File"), new MenuItem("Exit")};
      menu = new MenuBar(new MenuItem[][]{col0});
      setMenuBar(menu);
   }

   // Called by the system to pass events to the application.
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == menu)
         {
            switch (menu.getSelectedIndex())
            {
               case 1:
                  exit(0);
                  break;
               default : break;
            }
         }
         else
         if (event.target == btnGo1)
         {
         	if (sub1 == null)	sub1 = new Sub1();
         	swap(sub1);
         }
         else
         if (event.target == btnGo2)
         {
         	if (sub2 == null) sub2 = new Sub2();
         	swap(sub2);
         }
         else
         if (event.target == btnGo3)
         {
         	if (sub3 == null) sub3 = new Sub3();
         	swap(sub3);
         }
      }
   }
}