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



package tc.samples.api.net;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.net.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

public class FTPSample extends BaseContainer // TO BE FINISHED
{
   Button btGo;
   ComboBox cbCmd,cbLog;
   ListBox lbLocal,lbRemote;
   MenuBar mbar;
   MenuItem miTest, miLogin, miLogout;

   String url, user, pass;
   private int port = 21;
   FTP ftp;

   public void initUI()
   {
      super.initUI();
      MenuItem []m0 =
      {
         new MenuItem("File"),
         new MenuItem("Help"),
         new MenuItem("About"),
         new MenuItem(),
         miTest = new MenuItem("Automatic Test"),
         new MenuItem(),
         new MenuItem("Clear log"),
      };
      miTest.isEnabled = false;
      MenuItem []m1 =
      {
         new MenuItem("Parameters"),
         new MenuItem("URL"),
         new MenuItem("User"),
         new MenuItem("Password"),
         new MenuItem("Port"),
         new MenuItem(),
         miLogin  = new MenuItem("Login"),
         miLogout = new MenuItem("Logout"),
         new MenuItem(),
         new MenuItem("Disconnect")
      };
      miLogout.isEnabled = false;
      getParentWindow().setMenuBar(mbar = new MenuBar(new MenuItem[][]{m0,m1}));
      mbar.setAlternativeStyle(Color.BLUE,Color.WHITE);

      Label l;
      String [] commands = {"List","Change dir","Send - just a sample","Receive - just a sample"};
      add(l=new Label("Command: "),LEFT,TOP+1);
      add(btGo = new Button("Go"));
      btGo.setRect(RIGHT, SAME, PREFERRED,PREFERRED+2);
      btGo.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            try
            {
               switch (cbCmd.getSelectedIndex())
               {
                  case 0: // List
                  {
                     refreshDir();
                     break;
                  }
                  case 1: // Change dir
                  {
                     String dir = (String)lbRemote.getSelectedItem();
                     if (dir == null || dir == "")
                        break;
                     dir = dir.substring(55).trim();
                     ftp.setCurrentDir(dir);
                     refreshDir();
                     break;
                  }
                  case 2: // Put
                  {
                     log("Preparing string...");
                     byte []bytes = "1234567890".getBytes();
                     ByteArrayStream bas = new ByteArrayStream(60000);
                     for (int i =60000/10; i > 0; i--)
                        bas.writeBytes(bytes,0,bytes.length);
                     bas.reset();
                     log("Sending 60000 bytes...");
                     ftp.sendFile(bas, "bigstring.txt");
                     log("File bigstring.txt sent!");
                     Vm.safeSleep(500);
                     refreshDir();
                     break;
                  }
                  case 3: // Get
                  {
                     log("Receiving bigstring.txt from server...");
                     ByteArrayStream bas = new ByteArrayStream(60000);
                     ftp.receiveFile("bigstring.txt", bas);
                     log("File received!");
                     break;
                  }
               }
            }
            catch (Exception ex)
            {
               MessageBox.showException(ex, true);
               handleException(ex);
            }
         }
      });
      add(cbCmd = new ComboBox(commands));
      cbCmd.setRect(AFTER,SAME,FIT-1,PREFERRED,l);
      add(cbLog = new ComboBox());
      cbLog.enableHorizontalScroll();
      cbLog.fullWidth = true;
      cbLog.setRect(LEFT,BOTTOM,FILL,PREFERRED+1);

      int h = ((cbLog.getPos().y-cbCmd.getRect().y2())>>1) -2;
      add(lbLocal = new ListBox());
      lbLocal.enableHorizontalScroll();
      lbLocal.setRect(LEFT,AFTER+1,FILL,h,btGo);
      add(lbRemote = new ListBox());
      lbRemote.enableHorizontalScroll();
      lbRemote.setRect(LEFT,AFTER+1,FILL,h);
      cbCmd.setSelectedIndex(0);
      lbLocal.add("Local");
      lbRemote.add("Remote");
      btGo.setEnabled(false);

      if (Settings.appSettings != null)
      {
         String []uup = Convert.tokenizeString(Settings.appSettings,'|');
         if (uup.length == 4)
         {
            url  = uup[0]; if (url.equals ("null")) url  = null;
            user = uup[1]; if (user.equals("null")) user = null;
            pass = uup[2]; if (pass.equals("null")) pass = null;
            try
				{
					port = Convert.toInt(uup[3]);
				}
				catch (InvalidNumberException e)
				{
					MessageBox.showException(e, true);
				}
         }
      }
      repaintNow();
      getParentWindow().popupMenuBar(); // activate the menu
      mbar.moveBy(1); // show connection menu
      mbar.addPressListener(new PressListener()
      {
         public void controlPressed(ControlEvent e)
         {
            switch (mbar.getSelectedIndex())
            {
               case 1:
                  new MessageBox("Help","This sample is used to\nsend and get files from\na remote server. Select\nthe command and click in\nthe Go button. A\ndouble-click in a file\ninside the listbox will also\napply the command for it\n(if applicable).").popupNonBlocking();
                  break;
               case 2:
                  new MessageBox("About","This sample was created\nin one day with TotalCross.\n\nThis program is dedicated to\nVera Nardelli Campos\n(in memorium)").popupNonBlocking();
                  break;
               case 4:
                  automaticTest();
                  break;
               case 6:
                  cbLog.removeAll();
                  break;
               case 101:
               {
                  InputBox id = new InputBox("Connection Parameters", "Please enter the server's URL",url!=null?url:"");
                  id.popup();
                  if (id.getPressedButtonIndex() == 0)
                     url = id.getValue();
                  else
                     break; // user cancelled
                  if (user != null) // if no user was entered, go directly to it
                     break;
               }
               case 102:
               {
                  InputBox id = new InputBox("Connection Parameters", "Please enter the username",user!=null?user:"");
                  id.popup();
                  if (id.getPressedButtonIndex() == 0)
                     user = id.getValue();
                  else
                     break; // user cancelled
                  if (pass != null) // if no pass was entered, go directly to it
                     break;
               }
               case 103:
               {
                  InputBox id = new InputBox("Connection Parameters", "Please enter the password",pass!=null?pass:"");
                  id.getEdit().setMode(Edit.PASSWORD); // set to password
                  id.popup();
                  if (id.getPressedButtonIndex() == 0)
                     pass = id.getValue();
                  break;
               }
               case 104:
               {
                  InputBox id = new InputBox("Connection Parameters", "Please enter the port", Convert.toString(port));
                  id.getEdit().setValidChars("0123456789");
                  id.popup();
                  if (id.getPressedButtonIndex() == 0)
                     try
                     {
                        port = Convert.toInt(id.getValue());
                     }
                     catch (InvalidNumberException e1)
                     {
                        MessageBox.showException(e1, true);
                     }
                  break;
               }
               case 106:
                  if (url == null || user == null || pass == null)
                     new MessageBox("Attention","You must set the url,\nuser name and password\nbefore trying to connect!").popupNonBlocking();
                  else
                     login();
                  break;
               case 107:
                  logout();
                  break;
               case 109:
                  try
                  {
                     if (ftp != null)
                        ftp.forceClose();
                     new MessageBox("Attention","FTP disconnected").popupNonBlocking();
                  }
                  catch (IOException e1)
                  {
                     MessageBox.showException(e1, true);
                  }
                  enableLogoutItems();
                  break;
            }
         }
      });
   }

   public void onRemove()
   {
      if (ftp != null)
      {
         try
         {
            ftp.quit();
         }
         catch (FTPConnectionClosedException e)
         {
         }
         catch (IOException e)
         {
         }
      }

      Settings.appSettings = url+"|"+user+"|"+pass+"|"+port;
   }

   private void refreshDir() throws Exception
   {
      lbRemote.removeAll();
      String []ret = ftp.list("");
      if (ret != null)
         lbRemote.add(ret);
   }

   private void handleException(Exception ex)
   {
      log(">>>EXCEPTION INFO");
      log(ex.getMessage());
      String c = ex.getClass().toString();
      int dot = c.lastIndexOf('/');
      if (dot == -1) dot = c.lastIndexOf('.');
      String msg = c.substring(dot+1)+"\n"+ex.getMessage();
      if (Settings.onJavaSE)
         Vm.debug(msg);
      new MessageBox("Error!",msg).popupNonBlocking();
   }

   private void logout()
   {
      if (ftp != null)
      {
         try
         {
            ftp.quit();
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         enableLogoutItems();
      }
   }

   private void enableLogoutItems()
   {
      miLogout.isEnabled = miTest.isEnabled = false;
      miLogin.isEnabled = true;
      btGo.setEnabled(false);
   }
   private void openFTP() throws Exception
   {
      ftp = new FTP(url, user, pass, port, 30000, 20000, 20000, cbLog);
   }

   private void login()
   {
      MessageBox mb = new MessageBox("Connecting","Please wait",null);
      mb.popupNonBlocking();
      try
      {
         try
         {
            openFTP();
         }
         catch (FTPConnectionClosedException e)
         {
            openFTP(); // if this throws an exception again, just ignore and handle the new exception
         }
         miTest.isEnabled = miLogout.isEnabled = true;
         miLogin.isEnabled = false;
         btGo.setEnabled(true);

         lbRemote.removeAll();
         lbRemote.add("Current dir:");
         lbRemote.add(ftp.getCurrentDir());
         repaint();
         mb.unpop();
      }
      catch (Exception e)
      {
         mb.unpop();
         try
         {
            handleException(e);
            ftp.quit();
            ftp = null;
            clear();

         } catch (Exception t) {}
      }
      repaint();
   }
   private void log(String s)
   {
      cbLog.add(s);
      cbLog.selectLast();
   }
   private void automaticTest()
   {
      String msg = "before try/catch";
      try
      {
         log("> Running automatic test...");
         log("> Making directory 'tempdir'");
         msg = ">>> Exception in mkDir";
         ftp.createDir("tempdir");

         log("> Changing to that directory");
         msg = ">>> Exception in chgDir1";
         ftp.setCurrentDir("tempdir");

         String textfile = "Viva Verinha!";
         log("> Sending text '"+textfile+"' as file 'verinha.txt'...");
         ByteArrayStream bas = new ByteArrayStream(textfile.getBytes());
         msg = ">>> Exception in sendFile";
         ftp.sendFile(bas, "verinha.txt");

         log("> Listing only txt files...");
         msg = ">>> Exception in list";
         String []files = ftp.list("*.txt");
         if (files == null || files.length != 1)
            cbLog.add("> Something went wrong in sending files...");

         log("> Renaming verinha.txt to vivaverinha.txt");
         msg = ">>> Exception in rename";
         ftp.rename("verinha.txt","vivaverinha.txt");

         log("> Receiving vivaverinha.txt");
         bas = new ByteArrayStream(50);
         msg = ">>> Exception in receiveFile";
         ftp.receiveFile("vivaverinha.txt",bas);

         cbLog.add("> Contents: "+new String(bas.toByteArray()));
         log("> Deleting vivaverinha.txt");
         msg = ">>> Exception in delete";
         ftp.delete("vivaverinha.txt");

         log("> Moving diretory to one level up");
         msg = ">>> Exception in chgDir2";
         ftp.setCurrentDir("..");

         log("> Removing 'tempdir'");
         msg = ">>> Exception in rmDir";
         ftp.removeDir("tempdir");

         log("> Test finished with success!");
      }
      catch (Exception ex)
      {
         log(msg);
         handleException(ex);
      }
   }
   public void clear()
   {
      btGo.setEnabled(false);
      cbCmd.setSelectedIndex(0);
      lbLocal.removeAll();
      lbRemote.removeAll();
   }
}
