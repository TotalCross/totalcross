
package tc.samples.api.net.mail;

import tc.samples.api.*;

import totalcross.net.*;
import totalcross.net.mail.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

/**
Although not implemented here, here's now to send email to a GMAIL account:
<pre>
   Message m = new Message();
   m.addFrom(new Address[] { new Address("me@gmail.com", "Me") });
   m.addRecipient(RecipientType.TO, new Address("you@gmail.com", "You"));
   m.subject = "me and you";
   m.setText("we're good friends");
   
   MailSession s = MailSession.getDefaultInstance();
   s.put(MailSession.SMTP_HOST, new Properties.Str("smtp.gmail.com"));
   s.put(MailSession.SMTP_USER, new Properties.Str("postmaster@gmail.com"));
   s.put(MailSession.SMTP_PASS, new Properties.Str("a good password!));
   s.put(MailSession.SMTP_PORT, new Properties.Int(587));
   s.put(MailSession.SMTP_AUTH, new Properties.Boolean(true));
   s.put(MailSession.SMTP_STARTTLS, new Properties.Boolean(true));
   SMTPSSLTransport.send(m, s);
</pre>
If you receive error 534, see:
https://accounts.google.com/b/0/DisplayUnlockCaptcha

 */

public class MailSample extends BaseContainer
{
   MessageContainer messagePanel;

   Label lblInbox;
   Label lblPage;
   Button btDelete;
   Button btReset;
   Button btOpen;
   Button btConnect;
   Grid inbox;

   Store store;
   Folder folder;

   MailboxDataSource mds;

   public void initUI()
   {
      super.initUI();
      add(lblPage = new Label("Inbox", Label.CENTER), CENTER, TOP);
      add(btConnect = new Button("Synchronize"), RIGHT, TOP);
      add(btReset = new Button("Reset"), LEFT, BOTTOM);
      add(btOpen = new Button("Open"), CENTER, BOTTOM);
      Grid.useHorizontalScrollBar = true;
      inbox = new Grid(new String[] { "Subject" }, new int[] { -400 }, new int[] { LEFT }, false);
      add(inbox, LEFT, AFTER, FILL, FIT, lblPage);

      new ConfigurationContainer().popup();
   }

   Message[] msgs;

   public void onEvent(Event event)
   {
      try
      {
         if (event.target == btReset && event.type == ControlEvent.PRESSED)
         {
            inbox.removeAllElements();
            folder.reset();
            inbox.setDataSource(mds, folder.getMessageCount());
         }
         else if (event.target == btOpen && event.type == ControlEvent.PRESSED)
         {
            int selectedIndex = inbox.getSelectedIndex();
            if (selectedIndex != -1)
            {
               String[] item = inbox.getSelectedItem();
               showMessage(folder.getMessage(item[1]));
            }
         }
         else if (event.target == btConnect && event.type == ControlEvent.PRESSED)
         {
            connect();
         }
      }
      catch (MessagingException e)
      {
         MessageBox.showException(e, true);
      }
   }

   private void connect()
   {
      if (store != null)
         return;

      try
      {
         store = MailSession.getDefaultInstance().getStore("pop3");
         store.connect();
         folder = store.getFolder("INBOX");
         folder.open();
         inbox.liveScrolling = true;
         int messageCount = folder.getMessageCount();
         inbox.setDataSource(mds = new MailboxDataSource(folder), messageCount);
         inbox.scrollTo(messageCount);
      }
      catch (AuthenticationException e)
      {
         MessageBox.showException(e, true);
      }
      catch (MessagingException e)
      {
         MessageBox.showException(e, true);
      }
   }

   private void showMessage(Message message)
   {
      if (message != null)
         new MessageContainer(message).popup();
   }

   public void onRemove()
   {
      try
      {
         if (folder != null)
            folder.close(false);
         if (store != null)
            store.close();
      }
      catch (MessagingException e)
      {
         MessageBox.showException(e, true);
      }
   }
}
