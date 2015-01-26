
package tc.samples.api.net.mail;

import totalcross.io.*;
import totalcross.net.mail.MailSession;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.util.Properties;
import totalcross.util.Properties.Value;

public class ConfigurationContainer extends Window
{
   Edit edLogin;
   Edit edPassword;
   Edit edPopHost;
   Edit edPopPort;
   Edit edSmtpHost;
   Edit edSmtpPort;

   Button btOk;

   MailSession mp = MailSession.getDefaultInstance();
   String filePath = Settings.appPath + "/" + "MailTestConfig.dat";
   boolean loaded;

   public ConfigurationContainer()
   {
      super("Configuration",ROUND_BORDER);
      try
      {
         File f = new File(filePath, File.CREATE);
         if (f.getSize() > 0)
            mp.load(new DataStream(f));
         if (mp.size() > 0)
            loaded = true;
         f.close();
      }
      catch (Exception e)
      {
         MessageBox.showException(e, true);
      }
      setRect(CENTER,CENTER,SCREENSIZE+80,fmH*10);
   }

   public void initUI()
   {
      add(new Label("Login: "), LEFT, TOP,PARENTSIZE+40,PREFERRED);
      add(edLogin = new Edit(), AFTER, SAME);

      add(new Label("Password: "), LEFT, AFTER,PARENTSIZE+40,PREFERRED);
      add(edPassword = new Edit(), AFTER, SAME);
      edPassword.setMode(Edit.PASSWORD);

      add(new Label("SMTP host: "), LEFT, AFTER,PARENTSIZE+40,PREFERRED);
      add(edSmtpHost = new Edit(), AFTER, SAME);

      add(new Label("SMTP port: "), LEFT, AFTER,PARENTSIZE+40,PREFERRED);
      add(edSmtpPort = new Edit("12345"), AFTER, SAME);
      edSmtpPort.setValidChars("0123456789");
      edSmtpPort.setMaxLength(5);
      edSmtpPort.setText("25");

      add(new Label("POP3 host: "), LEFT, AFTER,PARENTSIZE+40,PREFERRED);
      add(edPopHost = new Edit(), AFTER, SAME);

      add(new Label("POP3 port: "), LEFT, AFTER,PARENTSIZE+40,PREFERRED);
      add(edPopPort = new Edit("12345"), AFTER, SAME);
      edPopPort.setValidChars("0123456789");
      edPopPort.setMaxLength(5);
      edPopPort.setText("110");

      add(btOk = new Button(" Ok "), RIGHT, BOTTOM);

      if (loaded)
      {
         Value value;
         if ((value = mp.get(MailSession.POP3_USER)) != null)
            edLogin.setText(value.toString());
         if ((value = mp.get(MailSession.POP3_PASS)) != null)
            edPassword.setText(value.toString());
         if ((value = mp.get(MailSession.SMTP_HOST)) != null)
            edSmtpHost.setText(value.toString());
         if ((value = mp.get(MailSession.SMTP_PORT)) != null)
            edSmtpPort.setText(value.toString());
         if ((value = mp.get(MailSession.POP3_HOST)) != null)
            edPopHost.setText(value.toString());
         if ((value = mp.get(MailSession.POP3_PORT)) != null)
            edPopPort.setText(value.toString());
      }
   }

   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED && event.target == btOk)
      {
         mp.put(MailSession.SMTP_USER, new Properties.Str(edLogin.getText()));
         mp.put(MailSession.POP3_USER, new Properties.Str(edLogin.getText()));
         mp.put(MailSession.SMTP_PASS, new Properties.Str(edPassword.getText()));
         mp.put(MailSession.POP3_PASS, new Properties.Str(edPassword.getText()));

         mp.put(MailSession.SMTP_HOST, new Properties.Str(edSmtpHost.getText()));
         mp.put(MailSession.POP3_HOST, new Properties.Str(edPopHost.getText()));

         try
         {
            mp.put(MailSession.SMTP_PORT, new Properties.Int(Convert.toInt(edSmtpPort.getText())));
            mp.put(MailSession.POP3_PORT, new Properties.Int(Convert.toInt(edPopPort.getText())));
         }
         catch (InvalidNumberException e)
         {
            // Should never happen, since the user may only use numbers for the port.
            MessageBox.showException(e, true);
         }

         try
         {
            File f = new File(filePath, File.CREATE_EMPTY);
            mp.save(new DataStream(f));
            f.close();
         }
         catch (Exception e)
         {
            MessageBox.showException(e, true);
         }
         unpop();
      }
   }
}
