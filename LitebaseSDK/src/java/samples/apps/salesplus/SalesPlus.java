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

package samples.apps.salesplus;

import litebase.LitebaseConnection;
import samples.apps.salesplus.ui.BaseMenu;
import samples.apps.salesplus.ui.MainMenu;
import samples.apps.salesplus.ui.customer.*;
import samples.apps.salesplus.ui.order.*;
import samples.apps.salesplus.ui.product.*;
import samples.apps.salesplus.ui.report.ReportMenu;
import samples.apps.salesplus.ui.report.byproduct.*;
import samples.apps.salesplus.ui.report.summary.*;
import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.Date;

public class SalesPlus extends MainWindow
{
   /**
    * The default background color.
    */
   public static final int DEFAULT_BACK_COLOR = 0xd9d9ff;
   
   /**
    * The default foreground color.
    */
   public static final int DEFAULT_FORE_COLOR = 0x000099;

   // Constants for swapping between screens. Each one assigns a window to be swapped to.
   /**
    * The main menu window.
    */
   public static final int MAIN_MENU = 0;

   /**
    * The costumer menu window.
    */
   public static final int CUSTOMER_MENU = 1;
   
   /**
    * The order menu window.
    */
   public static final int ORDER_MENU = 2;
   
   /**
    * The product menu window.
    */
   public static final int PRODUCT_MENU = 3;
   
   /**
    * The report menu window.
    */
   public static final int REPORT_MENU = 4;

   /**
    * The summary menu window.
    */
   public static final int SUMMARY_MENU = 5;
   
   /**
    * The by product menu window.
    */
   public static final int BY_PRODUCT_MENU = 6;

   /**
    * The new product window.
    */
   public static final int NEW_PRODUCT = 7;
   
   /**
    * The product search window.
    */
   public static final int PRODUCT_SEARCH = 8;

   /**
    * The new customer window.
    */
   public static final int NEW_CUSTOMER = 9;
   
   /**
    * The customer search window.
    */
   public static final int CUSTOMER_SEARCH = 10;

   /**
    * The new order window.
    */
   public static final int NEW_ORDER = 11;
   
   /**
    * The order search window.
    */
   public static final int ORDER_SEARCH = 12;

   /**
    * The summary report day window.
    */
   public static final int DAY = 13;
   
   /**
    * The summary report month window.
    */
   public static final int MONTH = 14;

   /**
    * The by product day window.
    */
   public static final int DAY2 = 15;
   
   /**
    * The by product month window.
    */
   public static final int MONTH2 = 16;
   
   /**
    * The by product period window.
    */
   public static final int PERIOD = 17;

   /**
    * The total number of classes.
    */
   public static final int TOTAL_CLASSES = 18;
   
   /** 
    * The instantiated classes. 
    */
   public static Container[] screens = new Container[TOTAL_CLASSES];
   
   /**
    * The connection with Litebase.
    */
   public static LitebaseConnection driver = LitebaseConnection.getInstance();
   
   /**
    * An auxiliary date object.
    */
   public static Date dateAux = new Date();
   
   /**
    * An auxiliary <code>StringBuffer</code> for string concatenation.
    */
   public static StringBuffer sBuffer = new StringBuffer();
   
   /**
    * An array of years to be used in the combo box.
    */
   public static final String[] years = {"2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016"};
   
   /**
    * The info image.
    */
   public static Image infoImg;
   
   /**
    * The message box for the info about the application.
    */
   public static MessageBox info;
   
   static
   {
      Settings.useNewFont = true;
   }
   
   /**
    * The application constructor.
    */
   public SalesPlus()
   {
      setUIStyle(Settings.Android);
      setBackColor(UIColors.controlsBack = Color.WHITE);
      UIColors.messageboxBack = Color.brighter(0x0A246A, 64);
      UIColors.messageboxFore = Color.WHITE;

      try
      {
         infoImg = new Image("images/ic_dialog_info.png");
      }
      catch (ImageException exception) {}
      catch (IOException exception) {}
      
      info = new MessageBox("About", "TotalCross Sales Plus | Example program for the|TotalCross SDK. | Created by Celso Marcelo da Silva " 
            + "| and Juliana Carpes Imperial | www.superwaba.com.br");
      info.footerColor = info.headerColor = UIColors.messageboxBack;
      try
      {
         info.setIcon(infoImg);
      }
      catch (ImageException exception) {}
      
      // Instantiates the screens.
      screens[MAIN_MENU] = new MainMenu();
      screens[CUSTOMER_MENU] = new CustomerMenu();
      screens[ORDER_MENU] = new OrderMenu();
      screens[PRODUCT_MENU] = new ProductMenu();
      screens[REPORT_MENU] = new ReportMenu();
      screens[SUMMARY_MENU] = new SummaryMenu();
      screens[BY_PRODUCT_MENU] = new ByProductMenu();
      screens[NEW_PRODUCT] = new NewProduct();
      screens[PRODUCT_SEARCH] = new ProductSearch();
      screens[NEW_CUSTOMER] = new NewCustomer();
      screens[CUSTOMER_SEARCH] = new CustomerSearch();
      screens[NEW_ORDER] = new NewOrder();
      screens[ORDER_SEARCH] = new OrderSearch();
      screens[DAY] = new Day();
      screens[MONTH] = new Month();
      screens[DAY2] = new Day2();
      screens[MONTH2] = new Month2();
      screens[PERIOD] = new Period();                    
   }

   /**
    * Called to initialize the User Interface of this container. 
    */
   public void initUI()
   {
      ((BaseMenu)screens[MAIN_MENU]).show();
   }
}
