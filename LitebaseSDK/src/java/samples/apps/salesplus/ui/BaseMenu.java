// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.apps.salesplus.ui;

import samples.apps.salesplus.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.res.Resources;
import totalcross.sys.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.Color;
import totalcross.util.*;

/**
 * Base class for menus (buttons placed vertically).
 */
public abstract class BaseMenu extends Container
{
   /** 
    * Button captions.
    */
   private String[] captions;
   
   /**
    * Button ids.
    */
   private int[] ids;
   
   /** 
    * Indicates if a menu is the root menu or an intermediate menu. 
    */
   private boolean isRoot;
   
   /**
    * A header bar.
    */
   private Bar headerBar;
   
   /**
    * A container stack for going from and back windows.
    */
   private static Vector containerStack = new Vector(4);
   
   /**
    * Constructor for a base menu.
    * 
    * @param newCaptions The button captions.
    * @param newIds The button ids.
    * @param newIsRoot Indicates if the menu is a root or an intermediate menu.
    */
   public BaseMenu(String[] newCaptions, int[] newIds, boolean newIsRoot)
   {
      captions = newCaptions;
      ids = newIds;
      isRoot = newIsRoot;
   }

   /** 
    * Initializes the user interface. 
    */
   public void initUI()
   {
      Button button = null;
      Container container = new Container();
      Font usedFont = Font.getFont(font.name, true, Font.BIG_SIZE);
      int length = ids.length,
          width = (int)(Settings.screenWidth * 0.75), // Buttons fill 75% of the page. 
          gap = Settings.screenWidth == 160 ? 2 : 4,
          i = -1;
      
      headerBar = new Bar("Sales Plus");
      headerBar.setFont(font.adjustedBy(2, true));
      headerBar.setBackForeColors(0x0A246A, Color.WHITE);
      headerBar.addButton(SalesPlus.infoImg);
      headerBar.addButton(isRoot? Resources.exit : Resources.back);
      add(headerBar, LEFT, 0, FILL, PREFERRED);
      headerBar.appId = isRoot? 999 : 0;
      
      add(container);
      
      // Places all controls inside another container so that this container can be centered on screen.
      Button.commonGap = 4;
      setFont(usedFont.asBold());

      // Adds the buttons.
      container.setRect(0, 0, width, 1000); // Creates a container with a huge height.
            
      while (++i < length)
      {
         container.add(button = new Button(captions[i]));
         button.setFont(usedFont);
         button.setRect(0, AFTER + gap, width, PREFERRED);
         button.appId = ids[i];
      }
      
      container.setRect(CENTER, CENTER, width, button.getRect().y2() + gap); // Sets the height to the correct size.
      Button.commonGap = 0;
   }

   /**
    * Called to process posted events.
    * 
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {        
         if (event.target instanceof Bar)
         {
            switch (headerBar.getSelectedIndex())
            {
               case 1:
                  SalesPlus.info.popup();
                  break;
               case 2:
                  back(); // Goes to the last container or exists the application. 
            }
         }
         else if (event.target instanceof Button)
         {
            Container container = SalesPlus.screens[((Button)event.target).appId];
            if (container instanceof BaseMenu)
               ((BaseMenu)container).show();
            else
               Window.getTopMost().swap(container);               
         }
            
      }
   }
   
   /**
    * Shows a window.
    */
   public void show()
   {
      containerStack.push(this); // Pushes itself.
      Window.getTopMost().swap(this);
   }
   
   /**
    * Goes back to the previous window.
    */
   public void back()
   {
      try
      {
         containerStack.pop(); // Pops itself.
         Window.getTopMost().swap((Container)containerStack.peek());
      }
      catch (ElementNotFoundException exception)
      {
         MainWindow.exit(0); // This is the last screen, so just exits the application.
      }
   }
}