package tc.samples.api.util;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;
import totalcross.util.pdf.*;

public class PDFWriterSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      try
      {
         PDFWriter mPDFWriter = new PDFWriter(PaperSize.FOLIO_WIDTH, PaperSize.FOLIO_HEIGHT);

         // note that to make this images snippet work
         // you have to uncompress the assets.zip file
         // included into your project assets folder
         try
         {
            Image img = Resources.comboArrow;
            mPDFWriter.addImage(400, 600, img, Transformation.DEGREES_315_ROTATION);
            mPDFWriter.addImage(300, 500, img);
            mPDFWriter.addImage(200, 400, 135, 75, img);
            mPDFWriter.addImage(150, 300, 130, 70, img);
            mPDFWriter.addImageKeepRatio(100, 200, 50, 25, img);
            mPDFWriter.addImageKeepRatio(50, 100, 30, 25, img, Transformation.DEGREES_270_ROTATION);
            mPDFWriter.addImageKeepRatio(25, 50, 30, 25, img);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

         mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.TIMES_ROMAN);
         mPDFWriter.addRawContent("1 0 0 rg\n");
         mPDFWriter.addTextAsHex(70, 50, 12, "68656c6c6f20776f726c6420286173206865782921");
         mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.COURIER, StandardFonts.WIN_ANSI_ENCODING);
         mPDFWriter.addRawContent("0 0 0 rg\n");
         mPDFWriter.addText(30, 90, 10, "© CRL", Transformation.DEGREES_270_ROTATION);

         mPDFWriter.newPage();
         mPDFWriter.addRawContent("[] 0 d\n");
         mPDFWriter.addRawContent("1 w\n");
         mPDFWriter.addRawContent("0 0 1 RG\n");
         mPDFWriter.addRawContent("0 1 0 rg\n");
         mPDFWriter.addRectangle(40, 50, 280, 50);
         mPDFWriter.addText(85, 75, 18, "Code Research Laboratories");

         mPDFWriter.newPage();
         mPDFWriter.setFont(StandardFonts.SUBTYPE, StandardFonts.COURIER_BOLD);
         mPDFWriter.addText(150, 150, 14, "http://coderesearchlabs.com");
         mPDFWriter.addLine(150, 140, 270, 140);

         int pageCount = mPDFWriter.getPageCount();
         for (int i = 0; i < pageCount; i++)
         {
            mPDFWriter.setCurrentPage(i);
            mPDFWriter.addText(10, 10, 8, Integer.toString(i + 1) + " / " + Integer.toString(pageCount));
         }

         String s = mPDFWriter.asString();
         final String name = Settings.appPath+"test.pdf";
         File f = new File(name,File.CREATE_EMPTY);
         f.writeBytes(s.getBytes());
         f.close();
                  
         Button b = new Button(" Open it ");
         add(b, CENTER,TOP+5);
         addLog(LEFT,AFTER+10,FILL,FILL,null);
         log("PDF Writter created by Javier Santo Domingo"); 
         log("PDF Generated at "+name);
         b.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               Vm.exec("viewer",name,0,true);
            }
         });
      }
      catch (Exception e)
      {
         MessageBox.showException(e, true);
      }
   }

}
