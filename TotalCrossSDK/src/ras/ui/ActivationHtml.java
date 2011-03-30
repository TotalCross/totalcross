package ras.ui;

import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.ScrollBar;
import totalcross.ui.Window;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;
import totalcross.ui.html.Document;
import totalcross.ui.html.HtmlContainer;
import totalcross.util.Hashtable;
import totalcross.xml.SyntaxException;
import totalcross.xml.XmlReadableByteArray;

public class ActivationHtml extends Window
{
   private static ActivationHtml instance;

   private HtmlContainer htmlCnr;
   private Document doc;

   private Hashtable userDefinedParams;

   private ActivationHtml(byte[] source) throws IOException, SyntaxException
   {
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
      add(htmlCnr = new HtmlContainer(), LEFT, TOP, FILL, FILL);
      htmlCnr.setBackForeColors(Color.WHITE, Color.BLACK);
      htmlCnr.setDocument(doc);
      ScrollBar.extraSize = scrollBarExtraSize;
      Button.commonGap = buttonCommonGap;
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
