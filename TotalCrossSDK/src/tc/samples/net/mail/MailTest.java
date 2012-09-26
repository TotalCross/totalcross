
package tc.samples.net.mail;

import totalcross.net.*;
import totalcross.net.mail.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;

public class MailTest extends MainWindow
{
   Container mailboxPanel;
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
      swap(mailboxPanel = new Container());

      mailboxPanel.add(lblPage = new Label("Inbox", Label.CENTER), CENTER, TOP);
      mailboxPanel.add(btConnect = new Button("Synchronize"), RIGHT, TOP);
      mailboxPanel.add(btReset = new Button("Reset"), LEFT, BOTTOM);
      mailboxPanel.add(btOpen = new Button("Open"), CENTER, BOTTOM);
      Grid.useHorizontalScrollBar = true;
      inbox = new Grid(new String[] { "Subject" }, new int[] { -400 }, new int[] { LEFT }, false);
      mailboxPanel.add(inbox, LEFT, AFTER, FILL, FIT, lblPage);

      swap(new ConfigurationContainer());
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
         this.swap(new MessageContainer(message));
   }

   public void onExit()
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
