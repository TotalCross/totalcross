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



/*KeyWord.java
  This class is the dialog box that prompts the user
  to enter the keyword that will be used for encryption and
  decryption for the repeating ceasar cypher
*/
package tc.samples.io.device.irchat;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.Color;

class KeyWord extends Window
{
	//user-interface specific variables
	private Button OK;
 	private Edit keyword;
	//the actual keyword for encryption and decryption
	public String key = "";

   public KeyWord()
   {
      super("Keyword",RECT_BORDER);
      setRect(CENTER,CENTER,140,65);
   	setBackColor(Color.getRGB(205,205,210));
   	add(new Label("Keyword:"),LEFT,TOP);
   	add(keyword=new Edit(""),AFTER+5,SAME);
   	add(OK = new Button("OK"), RIGHT, BOTTOM);
   }//end of KeyWord constructor

   /*onEvent captures events from the user and acts on them*/
   public void onEvent(Event event)
   {
      //if a button was pressed
      if (event.type == ControlEvent.PRESSED)
      {
         //if the button was the OK button
         if(event.target == OK)
         {
            //get the keyword and intialize the key variable
            key = keyword.getText();
            //close the window
            unpop();
         }
      }
   }
}
