/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2002 Shaun O'Brien                                             *
 *  Copyright (C) 2002-2012 SuperWaba Ltda.                                      *
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



/*Login.java
  This class handles the user login process along with
  the database activity involved in the logon process.
 */
package tc.samples.io.device.irchat;

import totalcross.io.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.util.*;

class Login extends Window
{

   // user interface specific variables
   private Button           OK, Cancel;
   private Edit             passWord;
   private ComboBox         userNames;
   // user verification specific variables
   public boolean           CANCEL;
   public static boolean    goodUser;
   public String            user_ID = "", password = "";
   // database management variables
   public static PDBFile    names;                      // The databases the
   // contain the list
   // values
   public static DataStream ds;                         // The iostreams that
   // read and write the
   // data
   // to the database
   /*
    * these vectors are the containers of all possible userIDs and passwords
    * that the user of a specific Palm Pilot can enter
    */
   private Vector           userIDs = new Vector(), id = new Vector(), passwd = new Vector();
   // the new user dialog
   private NewUser          newUser;

   public Login()
   {
      super("Login", RECT_BORDER); // with caption and borders
      setRect(CENTER, CENTER, 140, 95);
      setBackColor(0xCDCDD2);
      add(new Label("UserID:"), LEFT + 2, TOP + 4);
      // contains all possible usernames for specific Palm Pilot
      add(userNames = new ComboBox());
      userNames.setRect(AFTER + 6, SAME - 2, FILL - 4, PREFERRED);
      add(new Label("Password:"), LEFT + 2, AFTER + 6);
      // user to enter password with corresponding userID
      add(passWord = new Edit());
      passWord.setRect(AFTER + 6, SAME - 2, FILL - 4, PREFERRED);
      Button.commonGap = 2;
      add(OK = new Button("OK"), RIGHT - 4, BOTTOM - 4);
      add(Cancel = new Button("Cancel"), BEFORE - 4, SAME);
      Button.commonGap = 0;
      try
      {
         populateUserNames();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      userNames.setSelectedIndex(0);
      // the passWord edit box will enter 'stars'
      passWord.setMode(Edit.PASSWORD_ALL);
   }// end of Login constructor

   /*
    * populateUserNames() opens and reads the database and retreaves all the
    * possible user names for this specific Palm Pilot and loads them into a
    * combo box for the user to choose
    */
   private void populateUserNames() throws totalcross.io.IOException
   {
      // Check for databases
      String nextID;

      try
      {
         // The PDBFile class is a database
         names = new PDBFile("USID.DATA.usid", PDBFile.READ_WRITE);
      }
      catch(totalcross.io.FileNotFoundException e)
      {
         // if the database does not exist just add new user option
         names = new PDBFile("USID.DATA.usid", PDBFile.CREATE);
         names.close();
         userNames.add("new user");
         repaint();
      }

      // the database exists and open it in read only mode
      ds = new DataStream(names);
      names.setRecordPos(0);
      /*
       * now traverse through the "records" in the database and the usernames
       * to the userIDs Vector
       */
      for (int x = 0; x < names.getRecordCount(); x++)
      {
         names.setRecordPos(x);
         nextID = ds.readString();
         userIDs.addElement(nextID);
      }
      /*
       * if the userIDs Vector has userIDs in it, split up the user id and
       * password associated with it and then store the userID in the
       * userNames Vector and the password in the passwd Vector.
       */
      if (userIDs.size() > 0)
      {
         int n = userIDs.size();
         for (int i = 0; i < n; i++)
         {
            String uid = userIDs.items[i].toString();
            id.addElement(uid.substring(0, uid.indexOf('#', 0)));
            userNames.add(id.items[i].toString());
            passwd.addElement(uid.substring(uid.indexOf('#', 0) + 1, userIDs.items[i].toString().length()));
         }
      }
      // always append the new user option to the user names combo box
      userNames.add("new user");
      repaint();
      // close the database
      names.close();
   }// end of populateUserNames

   /* onEvent captures events from the user and acts on them */
   public void onEvent(Event event)
   {
      // if a top-level window was closed
      switch (event.type)
      {
         case ControlEvent.WINDOW_CLOSED:
            // if the NewUser dialog window was closed
            if (event.target == newUser)
            {
               // this is a good user
               goodUser = true;
               // the current user for chatting
               IRChat.currentUser = NewUser.newUser;
               // close the window
               unpop();
            }
            break;// end of window closed
         // if a button was pressed
         case ControlEvent.PRESSED:
            // if the OK button was pressed
            if (event.target == OK)
            {
               // did not hit cancel
               CANCEL = false;
               password = passWord.getText();
               user_ID = userNames.getSelectedItem().toString();
               if (user_ID.equals("new user"))
               {
                  (newUser = new NewUser()).popupNonBlocking();
               }
               else
               {
                  /*
                   * if not a new user try and find the user in the userID
                   * Vector and if found check the corresponding index in the
                   * passwd Vector. If they match then it is a good user
                   */
                  int index = id.indexOf(user_ID);
                  if (password.equals(passwd.items[index].toString()))
                  {
                     IRChat.currentUser = user_ID;
                     goodUser = true;
                     unpop();
                  }
                  else
                     new MessageBox("Error", "Invalid password").popupNonBlocking();
               }
            }// end of OK event
            // if Cancel button was pressed
            else
               if (event.target == Cancel)
               {
                  // did press cancel
                  CANCEL = true;
                  // close the window
                  unpop();
               }
      }// end of switch
   }// end of onEvent
}// end of Login class

