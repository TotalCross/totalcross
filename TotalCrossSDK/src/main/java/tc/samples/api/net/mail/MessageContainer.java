
package tc.samples.api.net.mail;

import totalcross.net.AuthenticationException;
import totalcross.net.mail.Address;
import totalcross.net.mail.AddressException;
import totalcross.net.mail.Message;
import totalcross.net.mail.MessagingException;
import totalcross.net.mail.Transport;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;

public class MessageContainer extends Window
{
   Button btBack;
   Button btReply;
   Button btDelete;
   Edit edFrom;
   Edit edSubject;

   Message msg;

   public MessageContainer(Message msg)
   {
      this.msg = msg;
      setRect(CENTER,CENTER,SCREENSIZE+80,SCREENSIZE+80);
   }

   public void initUI()
   {
      add(new Label("From: "), LEFT, TOP);
      add(edFrom = new Edit(), AFTER, SAME);
      Address[] from = msg.getFrom();
      StringBuffer fromBuffer = new StringBuffer(30);
      for (int i = 0 ; i < from.length ; i++)
      {
         fromBuffer.append(from[i].personal != null ? from[i].personal : from[i].address);
         if (i < from.length - 1)
            fromBuffer.append(", ");
      }
      edFrom.setText(fromBuffer.toString());
      edFrom.setEditable(false);

      add(new Label("Subject: "), LEFT, AFTER);
      add(edSubject = new Edit(), AFTER, SAME);
      edSubject.setText(msg.subject);
      edSubject.setEditable(false);

      add(btBack = new Button("Back"), RIGHT, BOTTOM);
      add(btReply = new Button("Reply"), LEFT, BOTTOM);

      Object content = msg.getContent();
      if (content instanceof String)
      {
         MultiEdit me = new MultiEdit();
         add(me, LEFT, AFTER, FILL, FIT, edSubject);
         me.setText((String) content);
      }
   }

   public void onEvent(Event event)
   {
      if (event.target == btBack && event.type == ControlEvent.PRESSED)
      {
         MainWindow.getMainWindow().swap(null);
      }
      else if (event.target == btReply && event.type == ControlEvent.PRESSED)
      {
         try
         {
            Message reply = msg.reply(false);
            reply.setText("Reply prototype");
            Transport.send(reply);
            new MessageBox("Reply test", "Reply sent").popup();
         }
         catch (AddressException e)
         {
            MessageBox.showException(e, true);
         }
         catch (MessagingException e)
         {
            MessageBox.showException(e, true);
         }
         catch (AuthenticationException e)
         {
            MessageBox.showException(e, true);
         }
      }
   }
}
