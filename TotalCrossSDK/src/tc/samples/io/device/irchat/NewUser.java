/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2002 Shaun O'Brien                                             *
 *  Copyright (C) 2002-2011 SuperWaba Ltda.                                      *
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

// $Id: NewUser.java,v 1.11 2011-01-04 13:19:24 guich Exp $

/*NewUser.java
  This class handles the event that a new user is
  logging into the application for the first time.
 */
package tc.samples.io.device.irchat;

import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.io.PDBFile;
import totalcross.ui.Button;
import totalcross.ui.Edit;
import totalcross.ui.Label;
import totalcross.ui.Window;
import totalcross.ui.dialog.*;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;

class NewUser extends Window
{
   // user interface specific variables
   private Button       OK;
   private Edit         userID, passWord;
   // the new user text field
   public static String newUser = "";
   // a datastream used to insert the new user into the database
   private DataStream   ds;

   public NewUser()
   {
      super("New User Info", RECT_BORDER);
      setRect(CENTER, CENTER, 140, 90);
      setBackColor(Color.getRGB(205, 205, 210));
      add(new Label("UserID:"), LEFT + 5, TOP + 5);
      add(userID = new Edit(""), AFTER + 5, SAME - 2);
      add(new Label("Password:"), LEFT + 5, AFTER + 5);
      add(passWord = new Edit(""), AFTER, SAME - 2);
      passWord.setMode(Edit.PASSWORD_ALL);
      add(OK = new Button("OK"), RIGHT, BOTTOM);
   }

   /* UpdateCatalog is used to save values to the database */
   private void UpdateCatalog() throws IOException
   {
      // if the userID edit or the password edit is blank
      if (userID.getLength() == 0 || passWord.getLength() == 0)
      {
         new MessageBox("Error", "Invalid userID or password").popupNonBlocking();
      }
      else
      {
         // open up the main database and write the values to it
         Login.names = new PDBFile("USID.DATA.usid", PDBFile.CREATE);
         // now put the string in a format I will remember (record form)
         String data = userID.getText() + "#" + passWord.getText();
         newUser = userID.getText();
         // get the size in bytes of the userID
         byte b[] = data.getBytes();
         // add the record to the database
         Login.names.addRecord(b.length + 2);
         int count = Login.names.getRecordCount(), current = 0;
         if (count == 0)
         {
            // FIXME: Does nothing? is that right?
         }
         else
            current = count - 1;
         Login.names.setRecordPos(current);
         ds = new DataStream(Login.names);
         ds.writeString(data);
         // close the database
         Login.names.close();
         // close the window
         unpop();
      }
   }// end of UpdateCatalog

   /* onEvent captures events from the user and acts on them */
   public void onEvent(Event event)
   {
      // if a button was pressed
      if (event.type == ControlEvent.PRESSED)
      {
         // if the button was OK
         if (event.target == OK)
         {
            try
            {
               UpdateCatalog();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }// end of button OK event
   }// end of onEvent
}// end of class NewUser

