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



package tc.samples.xml.rpc.client;

import totalcross.io.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;
import totalcross.xml.rpc.*;

public class HelloClient extends MainWindow
{
   private Button btn1;
   private Button btn2;
   private ListBox lb;
   private Edit ed;
   private Check chCompress;

   public HelloClient()
   {
      super("XML-RPC Tester", TAB_ONLY_BORDER);
   }

   public void initUI()
   {
      try
      {
         Class.forName("totalcross.xml.rpc.XmlRpcClient");
         Button.commonGap = 2;
         add(btn1 = new Button(" Say Hello "), LEFT+5, TOP+2);
         add(btn2 = new Button(" Say Hello to you "), AFTER+5, SAME);
         Button.commonGap = 0;
         add(new Label("Your name: ", LEFT), LEFT+2, AFTER + 2);
         add(ed = new Edit(""), AFTER + 3, SAME);
         add(chCompress = new Check("Compress"),LEFT+2,SAME);
         chCompress.setEnabled(false); // guich: compression is not yet supported by the server
         add(lb = new ListBox());
         lb.setRect(LEFT, AFTER + 2, FILL, FILL);
      }
      catch (ClassNotFoundException e)
      {
         Label l;
         add(l = new Label("This sample must be run at\ndesktop, as a win32 TotalCross app\nor as a java app. It\nrequires a server to be running\n(/TotalCross3/src/samples/\n/xml/webservice/server/HelloServer).\nCheck XmlRpcClient javadocs for\ndetailed install information."));
         l.setRect(LEFT,TOP,FILL,FILL);
      }
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && (e.target == btn1 || e.target == btn2))
         doRemoteFunctionCall(e.target == btn2);
   }

   private void doRemoteFunctionCall(boolean withName)
   {
      try
      {
         boolean compress = chCompress.isChecked();
         // Specify which server to connect to.
         XmlRpcClient client = new XmlRpcClient("localhost", 9090, "/RPC2", compress);
         Vector parameters = new Vector();
         if (withName) // Create a request to get the greeting when supplying a name.
            parameters.addElement(ed.getText());
         String cmd = "hello.sayHello";
         Vector results = (Vector) client.execute(cmd, parameters);

         lb.add("Response from the server: ");
         if (results != null)
            lb.add(results.items[0]);
         else
            lb.add("Error! Nothing returned from server!");
         lb.selectLast();
      }
      catch (XmlRpcException e)
      {
         lb.add("Client: XML-RPC exception: ");
         lb.add(e.getMessage());
      }
      catch (IllegalArgumentIOException e)
      {
         lb.add("Client: IllegalArgumentIOException: ");
         lb.add(e.getMessage());
      }
      catch (IOException e)
      {
         lb.add("Client: IOException: ");
         lb.add(e.getMessage());
      }
   }
}