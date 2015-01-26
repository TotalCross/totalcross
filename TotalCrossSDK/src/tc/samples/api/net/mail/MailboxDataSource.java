
package tc.samples.api.net.mail;

import totalcross.net.mail.Folder;
import totalcross.net.mail.Message;
import totalcross.net.mail.MessagingException;
import totalcross.ui.Grid;
import totalcross.ui.dialog.MessageBox;

public class MailboxDataSource implements Grid.DataSource
{
   Folder folder;
   String[][] items;

   public MailboxDataSource(Folder folder)
   {
      this.folder = folder;
   }

   public String[][] getItems(int startingRow, int count)
   {
      try
      {
         Message[] msgs = folder.getMessages(startingRow + 1, startingRow + count);
         items = new String[msgs.length][2];
         for (int i = 0; i < msgs.length; i++)
         {
            items[i][0] = msgs[i].subject;
            items[i][1] = msgs[i].uidl;
         }
      }
      catch (MessagingException e)
      {
         MessageBox.showException(e, true);
      }
      return items;
   }
}
