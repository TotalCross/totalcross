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

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.html.*;
import totalcross.xml.*;

/**
 * A simple Html browser to demonstrate the HtmlContainer class.
 * 
 * @author  Pierre G. Richard
 */
public class HtmlBrowser extends BaseContainer
{
   private static final String title = "Html Browser";
   private static final String stringSample = "<html><head><title></title></head><body><H1>Home Screen</H1><p>This is the main screen of the application and will be displayed whenever you start up the application. You can get to this screen at any time by tapping the 'Home' Button. </p><p><b>Choosing a guideline:</b><br/><OL><LI>Tap on the down arrow on the right side of the Guideline Selection box to open up a list of all the guidelines available.</LI><LI>Tap on the guideline. If it does not open immediately tap on the 'Go' button at the right side of the box.</LI></OL></p><p><b>To filter the list of guidelines:</b><br/><OL><LI>Tap on the down arrow on the right side of the Topic Selection box to open up a list of topics.</LI><LI>Tap on the topic to which the guideline you are interested belongs. This will filter the number of guidelines available in the Guideline Selection box.</LI></OL><p>(To expand the list of guidelines again, choose 'All Guidelines' in the Topic Selection box)</p></p><p><b>Searching for keywords among all guidelines:</b><br/><OL><LI>Enter the keyword(s) into the Search box.</LI><LI>Tap 'Go'.</LI></OL><UL><LI>To enter keyword(s) from previous searches tap the down arrow at the right side of the search box and choose the words for which you want to search again.</LI><LI>For more search options tap on the Advanced Search button.</LI></UL></p><p><b>Toolbar:</b><br/><UL><LI>Back button: Opens the last guideline you viewed at the last page you were reading.</LI><LI>Bookmark button: Opens the list of all bookmarks you have saved.</LI><LI>Help button: Opens this screen.</LI></UL></p><p><b>Menu:</b><br/><UL><LI>Tap on the top left-hand side of the screen or on the devices menu button to open the CliniPEARLS menu bar.</LI><LI>File:<UL><LI>Exit - quit the program.</LI></UL></LI><LI>Tools:<UL><LI>Manage subscription - Edit the number of guidelines you want on your device.</LI></UL></LI><LI>Options:<UL><LI>++ Font - Increase the size of the font in the guideline pages only.</LI><LI>--Font - Decrease the size of the font in the guideline pages only.</LI><LI>Update user - Update your CliniPEARLS username and password.</LI></UL></LI></UL></p></body></html>";
   public static int bgColor;
   private MenuBar mbar;
   private Button btnPlus,btnMinus;
   private HtmlContainer htmlCnr;
   private String[] samples = 
   {
      "data/Sample0.html",
      "data/Sample1.html",
      "data/Sample2.html",
      "data/Sample3.html",
      "data/Sample4.html",
      "data/Sample5.html",
      "data/Sample6.html",
   };
   
   public void initUI()
   {
      MenuItem[][] menus =
      {
         {
            new MenuItem("Options"),
            new MenuItem("Go to..."),
         },
         {
            new MenuItem("Tests"),
            new MenuItem("Sample str"),
            new MenuItem("Sample 0"),
            new MenuItem("Sample 1"),
            new MenuItem("Sample 2"),
            new MenuItem("Sample 3"),
            new MenuItem("Sample 4"),
            new MenuItem("Sample 5"),
            new MenuItem("Sample 6")
         },
         {
            new MenuItem("Help"),
            new MenuItem("About"),
            new MenuItem("Info")
         }
      };
      bgColor = 0x006688;
      setBackForeColors(bgColor, Color.WHITE);
      getParentWindow().setMenuBar(mbar = new MenuBar(menus));
      add(htmlCnr = new HtmlContainer(),LEFT,TOP,FILL,FILL);
      htmlCnr.setBackForeColors(Color.WHITE, Color.BLACK);
      btnMinus = new Button(" - ");
      btnPlus  = new Button(" + ");
      btnMinus.setBorder(Button.BORDER_NONE);
      btnPlus.setBorder(Button.BORDER_NONE);
      add(btnMinus, RIGHT, 0, PREFERRED,fm.height);
      add(btnPlus, BEFORE-2,0,PREFERRED,SAME);
      add(new Label("Font: "),BEFORE-2,0,PREFERRED,SAME);
      repaintNow(); // update background color
      getParentWindow().popupMenuBar();
   }
   
   public void onEvent(Event event)
   {
      switch(event.type)
      {
         case ControlEvent.PRESSED:
            try
            {
               if (event.target == htmlCnr) // called when a link is pressed
               {
                  setInfo("Link: "+htmlCnr.pressedLink);
               }
               else
               if (event.target == btnPlus || event.target == btnMinus)
               {
                  Style.defaultFontSize += event.target == btnPlus ? 1 : -1;
                  btnMinus.setEnabled(Style.defaultFontSize > Font.MIN_FONT_SIZE);
                  btnPlus.setEnabled(Style.defaultFontSize < Font.MAX_FONT_SIZE);                     
                  renderLast();
               }
               else
               if (event.target == mbar)
               {
                  int sel = mbar.getSelectedIndex();
                  switch(sel)
                  {
                     case 1: // Go to...
                        Opener opener = new Opener();
                        opener.popup();
                        render(opener.getXmlReadable());
                        break;
   
                     case 101:
                        render(new XmlReadableString(stringSample));
                        break;
   
                     case 102:
                     case 103:
                     case 104:
                     case 105:
                     case 106:
                     case 107:
                     case 108:
                        loadSample(sel-102);
                        break;
   
                     case 201:
                        about();
                        break;
   
                     case 202:
                        info();
                        break;
   
                     case -1:
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
   
   private void about()
   {
      MessageBox mb = new MessageBox(title, "Copyright Jaxo Systems\ndeveloped by Pierre G. Richard");
      mb.setBackForeColors(Color.WHITE, bgColor);
      mb.popup();
   }
   private void info()
   {
      MessageBox mb = new MessageBox(title, "This program uses an HtmlContainer,\nwhich is a Container having its\ncontents described in HTML.\nThis isn't yet a full-featured browser.\nAlpha version is a proof-of-concept\nand does not handle all HTML tags or\nprocess any HTML events.");
      mb.setTextAlignment(LEFT);
      mb.setBackForeColors(Color.WHITE, bgColor);
      mb.popup();
   }
   
   private XmlReadable lastSource;
   
   private void renderLast() throws IOException, SyntaxException
   {
      render(lastSource);
   }
   
   private void render(XmlReadable source) throws IOException, SyntaxException
   {
      lastSource = source;
      if(source != null)
      {
         Document doc = new Document(source);
         setTitle(doc.title);
         htmlCnr.setDocument(doc);
      }
   }
}

