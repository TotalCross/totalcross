package tc.samples.app.btchat;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

public class ChatScreen extends Container
{
   DataStream connection;

   ChatScreen(Stream connection)
   {
      this.connection = new DataStream(connection);
   }

   ListBox chatArea;
   Edit msgField;
   Button btSend;

   public void initUI()
   {
      btSend = new Button(" Send! ");

      add(btSend, RIGHT, BOTTOM,PREFERRED,PREFERRED+fmH);

      msgField = new Edit();
      add(msgField, LEFT, SAME, FIT, PREFERRED);

      chatArea = new ListBox();
      add(chatArea, LEFT, TOP, FILL, FIT, msgField);

      msgField.requestFocus();
      new Thread(new Listener()).start();
   }

   public void onEvent(Event event)
   {
      if (event.type == KeyEvent.SPECIAL_KEY_PRESS)
      {
         btSend.simulatePress();
         btSend.postPressedEvent();
      }
      else if (event.type == ControlEvent.PRESSED && event.target == btSend)
      {
         String msg = msgField.getText();
         if (msg != null && msg.trim().length() > 0)
         {
            try
            {
               connection.writeString(msg);
               // send message
               chatArea.add(msg);
               chatArea.selectLast();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
         msgField.clear();
      }
   }

   class Listener implements Runnable
   {
      public void run()
      {
         try
         {
            while (true)
            {
               String s = connection.readString();
               if (s != null)
               {
                  chatArea.add(s);
                  chatArea.selectLast();
               }
               Vm.sleep(50);
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
}
