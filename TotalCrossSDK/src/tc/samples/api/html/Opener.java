/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003-2004 Pierre G. Richard                                    *
 *  Copyright (C) 2004-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.samples.api.html;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.net.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;
import totalcross.xml.*;

/**
 * UI for creating an XmlParsable.
 *
 * @author  Pierre G. Richard
 */
public class Opener extends Window
{
   private static final String PDBFileName = "HtmlBrowserDB.TcTc.DATA";
   private static final String httpLongProtocol = "http://";
   private static final String fileLongProtocol = "dm://";
   private IntVector recordNos;
   private Button btnOpen;
   private Button btnCancel;
   private Button btnErrorsOK;
   private ListBox lbMessages;
   private Edit edAddress;
   private Edit edPort;
   private Edit edBauds;
   private Edit edRecNo;
   private RadioGroupController rgProtocols;
   private Radio rbHttp;
   private Radio rbFile;
   private Radio rbSerial;
   private Label llPort;
   private Label llBauds;
   private Label llRecNo;
   private RadioGroupController rgSerialType;
   private Radio rbCradle;
   private Radio rbBeamer;
   private XmlReadable readable;
   private ComboBox cbKnownUrls;

   public Opener()
   {
      super("Go to...", RECT_BORDER);
      int regionColor = 0xFFFF99;
      setRect(CENTER, CENTER, Settings.screenWidth*150/160, Settings.screenHeight*100/160);
      setBackForeColors(Color.WHITE, HtmlBrowser.bgColor);
      rgSerialType = new RadioGroupController();
      add(llPort = new Label("Port:"), LEFT + 2, TOP + 18);
      add(edPort = new Edit("8080"), AFTER + 4, SAME);
      add(llBauds = new Label("Speed:"), AFTER + 20, SAME);
      add(edBauds = new Edit("19200"), AFTER + 4, SAME);
      add(llRecNo = new Label("Record #:"), LEFT + 2, SAME);
      add(edRecNo = new Edit("000"), AFTER + 4, SAME);
      add(rbCradle = new Radio("Cradle", rgSerialType), LEFT, SAME);
      add(rbBeamer = new Radio("Beamer", rgSerialType), LEFT, AFTER);
      add(edAddress = new Edit(""), LEFT, SAME + 5);
      rbCradle.setChecked(true);
      add(cbKnownUrls = new ComboBox());
      cbKnownUrls.setRect(LEFT, AFTER, FILL, PREFERRED);
      edPort.setText("80");
      edPort.setValidChars(Edit.numbersSet);
      edBauds.setText("19200");
      edBauds.setValidChars(Edit.numbersSet);
      edRecNo.setText("0");
      edRecNo.setValidChars(Edit.numbersSet);
      rgProtocols = new RadioGroupController()
      {
         public void setSelectedItem(Radio who)
         {
            super.setSelectedItem(who);
            boolean isVisible = (who == rbHttp);
            llPort.setVisible(isVisible);
            edPort.setVisible(isVisible);
            isVisible = (who == rbSerial);
            llBauds.setVisible(isVisible);
            edBauds.setVisible(isVisible);
            isVisible = (who == rbSerial);
            edAddress.setVisible(!isVisible);
            rbCradle.setVisible(isVisible);
            rbBeamer.setVisible(isVisible);
            isVisible = (who == rbFile);
            llRecNo.setVisible(isVisible);
            edRecNo.setVisible(isVisible);
         }
      };
      add(rbHttp = new Radio("Http", rgProtocols), LEFT, TOP + 2);
      rbHttp.setBackColor(regionColor);
      add(rbFile = new Radio("File", rgProtocols), AFTER + 6, SAME);
      rbFile.setBackColor(regionColor);
      add(rbSerial = new Radio("Serial", rgProtocols), AFTER + 6, SAME);
      rbSerial.setBackColor(regionColor);
      rgProtocols.setSelectedItem(rbHttp);
      add(btnOpen = new Button("Open"), RIGHT - 1, BOTTOM - 1);
      add(btnCancel = new Button("Cancel"), BEFORE - 3, SAME);
      add(lbMessages = new ListBox());
      lbMessages.setRect(LEFT, TOP, FILL, FILL);
      lbMessages.setVisible(false);
      add(btnErrorsOK = new Button("OK"), RIGHT - 12, BOTTOM - 3);
      btnErrorsOK.setVisible(false);
      populateAddresses();
      cbKnownUrls.setSelectedIndex(0);
   }

   /**
    * Get the XmlReadable produces by this Opener, or null when no XmlReadable
    * was created.
    *
    * @return the XmlReadable produces by this Opener,
    *         or null when no XmlReadable was created.
    */
   public XmlReadable getXmlReadable()
   {
      return readable;
   }

   /**
    * Populate the combo box of known URLs from reading
    * the HtmlBrowserDB PDBFile
    */
   private void populateAddresses()
   {
      try
      {
         PDBFile pdb = null;
         
         try
         {
            pdb = new PDBFile(PDBFileName, PDBFile.READ_WRITE);
         }
         catch (FileNotFoundException fnfe)
         {
            writePDBFile("www.google.com.br");
            writePDBFile("dm://memo.DATA/HtmlSampleDB/html/0");
            writePDBFile("dm://memo.DATA/HtmlSampleDB/html/1");
            writePDBFile("dm://memo.DATA/HtmlSampleDB/html/2");
            writePDBFile("dm://memo.DATA/HtmlSampleDB/html/3");
            pdb = new PDBFile(PDBFileName, PDBFile.READ_WRITE);
         }
         DataStream ds = new DataStream(pdb);
         pdb.setRecordPos(0);
         int max = pdb.getRecordCount();
         recordNos = new IntVector(max);
         for(int i = 0;i < max;++i)
         {
            pdb.setRecordPos(i);
            String s = ds.readString();
            if(s.startsWith(httpLongProtocol))
            {
               recordNos.addElement(-1);
               cbKnownUrls.add(s.substring(httpLongProtocol.length(), s.length()));
            }
            else
               if(s.startsWith(fileLongProtocol))
               {
                  // strip off the owner.type and category until I convince Guich
                  int end = s.lastIndexOf('/');
                  int beg = s.indexOf('/', fileLongProtocol.length()) + 1;
                  recordNos.addElement(Convert.toInt(s.substring(end + 1)));
                  //          end = s.lastIndexOf('/', end-1);   // skip over the category!
                  cbKnownUrls.add(s.substring(beg, s.length()));
               }
               else
               {
                  pdb.deleteRecord();
                  --max;
                  --i;
               }
         }
         repaint();
         pdb.close();
      }
      catch (Exception e)
      {
         MessageBox.showException(e,true);
      }
   }

   public void onEvent(Event event)
   {
      switch(event.type)
      {
         case ControlEvent.PRESSED:
            try
            {
               if(event.target == cbKnownUrls)
               {
                  String address = (String)cbKnownUrls.getSelectedItem();
                  int recNo = recordNos.items[cbKnownUrls.getSelectedIndex()];
                  if(recNo < 0)
                  {
                     edAddress.setText(address);
                     rgProtocols.setSelectedItem(rbHttp);
                  }
                  else
                  {
                     edAddress.setText(address.substring(0, address.indexOf('/')));
                     edRecNo.setText(Convert.toString(recNo));
                     rgProtocols.setSelectedItem(rbFile);
                  }
               }
               else
                  if(event.target == btnOpen)
                  {
                     String s;
                     lbMessages.setVisible(true);
                     if(rbFile.isChecked())
                        readable = new XmlReadablePDBFile(new URI("dm://memo.DATA/" + edAddress.getText() + '/' + edRecNo.getText()));
                     else
                        if(rbSerial.isChecked())
                        {
                           s = edBauds.getText();
                           int bauds = (s.length() == 0) ? -1 : Convert.toInt(s);
                           //Vm.debug("=> open XmlReadableSerial(" + rbCradle.isChecked() + ", " + bauds + ");");
                           readable = new XmlReadablePort(rbCradle.isChecked() ? PortConnector.DEFAULT : PortConnector.IRCOMM, bauds,  /*>>>>>>>>        theURI!*/null);
                        }
                        else
                        {
                           XmlReadableSocket stream;
                           URI uri = new URI("http://" + edAddress.getText());
                           s = edPort.getText();
                           if(s.length() > 0)
                              uri.port = Convert.toInt(s);
                           stream = new XmlReadableSocket(uri);
                           if(!stream.isOk())
                           {
                              showMessage(stream.getStatus());
                              readable = null;
                           }
                           else
                              readable = stream;
                        }
                     if(readable != null)
                     {
                        addNewUrlToPDBFile();
                        unpop();
                     }
                     else
                        btnErrorsOK.setVisible(true);
                  }
                  else
                     if(event.target == btnCancel)
                        unpop();
                     else
                        if(event.target == btnErrorsOK)
                        {
                           btnErrorsOK.setVisible(false);
                           lbMessages.setVisible(false);
                           lbMessages.removeAll();
                        }
            }
            catch (Exception e)
            {
               MessageBox.showException(e, true);
            }
            break;
      }
   }

   /**
    * Add a new URL into the HtmlBrowserDB PDBFile
    */
   private void addNewUrlToPDBFile() throws IOException, InvalidNumberException
   {
      int recordNo;
      String address = edAddress.getText();
      String url;
      if(rbHttp.isChecked())
      {
         url = httpLongProtocol + address.toLowerCase();
         recordNo = -1;
      }
      else
         if(rbFile.isChecked())
         {
            String s = edRecNo.getText();
            url = fileLongProtocol + "memo.DATA/" + address + "/html/" + s;
            recordNo = Convert.toInt(s);
         }
         else
            return ;
      // find if this url exists.  If not, add it.
      int max = cbKnownUrls.size();
      for(int i = 0;;++i)
      {
         if(i == max)
         {
            writePDBFile(url);
            break;
         }
         else
         {
            String s = (String)cbKnownUrls.getItemAt(i);
            if(recordNo >= 0)
               s = s.substring(0, s.indexOf('/'));
            if(address.equals(s) && (recordNo == recordNos.items[i]))
               break;
         }
      }
   }

   /**
    * Write a string to the PDBFile
    *
    * @param s String to write
    */
   private static void writePDBFile(String s) throws IOException
   {
      PDBFile pdb = new PDBFile(PDBFileName, PDBFile.CREATE);
      pdb.addRecord(s.getBytes().length + 2);
      int count = pdb.getRecordCount();
      if(count > 0)
         --count;
      //Vm.debug("==> writing: " + s + " count=" + count);
      pdb.setRecordPos(count);
      DataStream ds = new DataStream(pdb);
      ds.writeString(s);
      pdb.close();
   }

   /**
    * Add a message in the message ListBox
    *
    * @param s the message to add
    */
   private void showMessage(String s)
   {
      lbMessages.add(s);
      lbMessages.setSelectedIndex(lbMessages.size() - 1);
      lbMessages.repaintNow();
   }
}

