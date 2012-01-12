package ras.ui;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.html.*;
import totalcross.util.*;
import totalcross.xml.*;

public class ActivationHtml extends Window
{
   private static ActivationHtml instance;

   private HtmlContainer htmlCnr;
   private Document doc;

   private Hashtable userDefinedParams;

   private ActivationHtml(byte[] source) throws IOException, SyntaxException
   {
      super("",NO_BORDER);
      doc = new Document(new XmlReadableByteArray(source));
      userDefinedParams = new Hashtable(30);
   }

   public static ActivationHtml getInstance()
   {
      if (instance == null)
      {
         byte[] source = Vm.getFile("activation.html");
         if (source != null)
            try
            {
               instance = new ActivationHtml(source);
            }
            catch (Exception e)
            {
               Vm.debug("Failed to load activation.html!");
               MessageBox.showException(e, true);
            }
      }
      return instance;
   }

   protected void onPopup()
   {
      int scrollBarExtraSize = ScrollBar.extraSize;
      int buttonCommonGap = Button.commonGap;
      ScrollBar.extraSize = 4;
      Button.commonGap = 2;
      add(htmlCnr = new HtmlContainer(), 0,0, FILL, FILL);
      htmlCnr.setBackForeColors(Color.WHITE, Color.BLACK);
      htmlCnr.setDocument(doc);
      ScrollBar.extraSize = scrollBarExtraSize;
      Button.commonGap = buttonCommonGap;
   }
   
   protected void postPopup()
   {
      // search for the topmost edit
      Container co = htmlCnr;
      Control c;
      while (true)
      {
         c = co.getFirstChild();
         if (c == null)
            break;
         if (!(c instanceof Container))
         {
            int minY = 100000;
            Control[] cc = co.getChildren();
            if (cc != null)
               for (int i = cc.length; --i >= 0;)
                  if (cc[i] instanceof Edit)
                  {
                     if (cc[i].getY() < minY)
                     {
                        minY = cc[i].getY();
                        c = cc[i];
                     }
                  }
            break;
         }
         co = (Container)c;
      }
      System.out.println(c);
      if (c != null)
         c.requestFocus();
      else
         htmlCnr.requestFocus();
   }

   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED && event.target == htmlCnr)
      {
         String link = htmlCnr.pressedLink;
         saveUserParams(link.substring(link.indexOf('?') + 1));
         this.unpop();
      }
   }

   private void saveUserParams(String link)
   {
      String[] fields = Convert.tokenizeString(link, '&');
      for (int i = 0; i < fields.length; i++)
      {
         String[] fieldAndValue = Convert.tokenizeString(fields[i], '=');
         if (!"SubmitActivation".equals(fieldAndValue[0]))
            userDefinedParams.put(fieldAndValue[0], fieldAndValue.length == 1 ? "" : fieldAndValue[1]);
      }
   }

   public Hashtable getUserDefinedParams()
   {
      return userDefinedParams;
   }

}
