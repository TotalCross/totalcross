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



package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.html.*;
import totalcross.xml.*;

/**
 * A simple Html browser to demonstrate the HtmlContainer class.
 * 
 * @author  Pierre G. Richard
 */
public class HtmlContainerSample extends BaseContainer
{
   private static final String stringSample = "<html><head><title></title></head><body><H1>Home Screen</H1><p>This is the main screen of the application and will be displayed whenever you start up the application. You can get to this screen at any time by tapping the 'Home' Button. </p><p><b>Choosing a guideline:</b><br/><OL><LI>Tap on the down arrow on the right side of the Guideline Selection box to open up a list of all the guidelines available.</LI><LI>Tap on the guideline. If it does not open immediately tap on the 'Go' button at the right side of the box.</LI></OL></p><p><b>To filter the list of guidelines:</b><br/><OL><LI>Tap on the down arrow on the right side of the Topic Selection box to open up a list of topics.</LI><LI>Tap on the topic to which the guideline you are interested belongs. This will filter the number of guidelines available in the Guideline Selection box.</LI></OL><p>(To expand the list of guidelines again, choose 'All Guidelines' in the Topic Selection box)</p></p><p><b>Searching for keywords among all guidelines:</b><br/><OL><LI>Enter the keyword(s) into the Search box.</LI><LI>Tap 'Go'.</LI></OL><UL><LI>To enter keyword(s) from previous searches tap the down arrow at the right side of the search box and choose the words for which you want to search again.</LI><LI>For more search options tap on the Advanced Search button.</LI></UL></p><p><b>Toolbar:</b><br/><UL><LI>Back button: Opens the last guideline you viewed at the last page you were reading.</LI><LI>Bookmark button: Opens the list of all bookmarks you have saved.</LI><LI>Help button: Opens this screen.</LI></UL></p><p><b>Menu:</b><br/><UL><LI>Tap on the top left-hand side of the screen or on the devices menu button to open the CliniPEARLS menu bar.</LI><LI>File:<UL><LI>Exit - quit the program.</LI></UL></LI><LI>Tools:<UL><LI>Manage subscription - Edit the number of guidelines you want on your device.</LI></UL></LI><LI>Options:<UL><LI>++ Font - Increase the size of the font in the guideline pages only.</LI><LI>--Font - Decrease the size of the font in the guideline pages only.</LI><LI>Update user - Update your CliniPEARLS username and password.</LI></UL></LI></UL></p></body></html>";
   private HtmlContainer htmlCnr;
   private ComboBox cb;
   private String[] samples = 
   {
      "",
      "tc/samples/api/ui/data/Sample0.html",
      "tc/samples/api/ui/data/Sample1.html",
      "tc/samples/api/ui/data/Sample2.html",
      "tc/samples/api/ui/data/Sample3.html",
      "tc/samples/api/ui/data/Sample4.html",
      "tc/samples/api/ui/data/Sample5.html",
      "tc/samples/api/ui/data/Sample6.html",
   };
   String gif = "tc/samples/api/ui/data/totalcross.gif"; // in the html we didnt put the full path, so we put here so tc.Deploy can store the gif in the tcz
   
   public void initUI()
   {
      super.initUI();
      String[] items =
      {
         "Sample str",
         "Sample 0",
         "Sample 1",
         "Sample 2",
         "Sample 3",
         "Sample 4",
         "Sample 5",
         "Sample 6"
      };
      add(cb = new ComboBox(items),LEFT,TOP,FILL,PREFERRED);
      add(htmlCnr = new HtmlContainer(),LEFT,AFTER,FILL,FILL);
      htmlCnr.setBackForeColors(Color.WHITE, Color.BLACK);
   }

   public void onEvent(Event event)
   {
      switch(event.type)
      {
         case ControlEvent.PRESSED:
            try
            {
               if (event.target == htmlCnr) // called when a link is pressed
                  setInfo("Link: "+htmlCnr.pressedLink);
               else
               if (event.target == cb)
               {
                  int sel = cb.getSelectedIndex();
                  switch(sel)
                  {
                     case -1: break;
                     case 0:
                        render(new XmlReadableString(stringSample));
                        break;
                     default:
                        loadSample(sel);
                        break;
                  }
               }
            }
            catch (Exception e)
            {
               MessageBox.showException(e,true);
            }
            break;
      }
   }
   
   private void loadSample(int n) throws Exception
   {
      render(new XmlReadableByteArray(Vm.getFile(samples[n])));
   }
   
   private XmlReadable lastSource;
   
   void renderLast() throws IOException, SyntaxException
   {
      render(lastSource);
   }
   
   private void render(XmlReadable source) throws IOException, SyntaxException
   {
      lastSource = source;
      if(source != null)
      {
         Document doc = new Document(source);
         setInfo(doc.title);
         htmlCnr.setDocument(doc);
      }
   }
}

