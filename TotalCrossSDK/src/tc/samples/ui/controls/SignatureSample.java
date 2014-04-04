package tc.samples.ui.controls;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class SignatureSample extends BaseContainer
{
   Button save;
   Button clear;
   Button load;
   Whiteboard paint;
   ComboBox cbColors;
   String fileName;

   public void initUI()
   {
      super.initUI();
      
      uiAdjustmentsBasedOnFontHeightIsSupported = true;
      setTitle("Signature");
      
      fileName = Settings.platform.equals(Settings.ANDROID) ? "/sdcard/handwrite.png" : "device/handwrite.png";

      save = new Button("Save");
      clear = new Button("Clear");
      load = new Button("Load");
      paint = new Whiteboard();

      // center the save/load/clear buttons on screen
      add(load, CENTER, BOTTOM-50,PARENTSIZE+25,PREFERRED+50);
      add(save, LEFT+50, SAME,PARENTSIZE+25,PREFERRED+50); // before the load button
      add(clear, RIGHT-50, SAME,PARENTSIZE+25,PREFERRED+50, load); // after the load button add the paint control with a specified width
      add(paint);
      paint.borderColor = Color.BLACK;
      paint.setRect(CENTER, TOP+50, PARENTSIZEMIN+90, PARENTSIZEMIN+50); // add the status
      add(cbColors = new ComboBox(new ColorList()), CENTER, AFTER + 50);
      cbColors.setSelectedIndex(cbColors.indexOf(new ColorList.Item(Color.BLACK))); // select color 0
   }

   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {
         if (event.target == cbColors)
            paint.setPenColor(((ColorList.Item) cbColors.getSelectedItem()).value);
         else
         if (event.target == clear)
            paint.clear();
         else
         if (event.target == save)
         {
            // note: we could use the saveTo method of totalcross.ui.image.Image, but for
            // example purposes, we will show how to use the other method, createPng
            // create a buffer to store the image
            try
            {
               // create
               File f = new File(fileName, File.CREATE_EMPTY);
               paint.getImage().createPng(f);
               int s = f.getSize();
               f.close();
               setInfo("Saved " + f.getPath() + " ("+ s + " bytes)");
            }
            catch (Exception e)
            {
               setInfo("Unable to create file: "+e.getMessage());
               MessageBox.showException(e, false);
            }
         }
         else
         if (event.target == load)
         {
            try
            {
               try
               {
                  File f = new File(fileName, File.READ_ONLY);
                  int size = f.getSize();
                  byte test[] = new byte[size];
                  f.readBytes(test, 0, size);
                  f.close();
                  Image img = new Image(test);
                  paint.setImage(img);
                  setInfo(img.getWidth() > paint.getWidth() || img.getHeight() > paint.getHeight() ? "Loaded. Click and drag picture" : "Loaded.");
               }
               catch (totalcross.io.FileNotFoundException e)
               {
                  setInfo("You must first save the image.");
                  return;
               }
            }
            catch (Exception e)
            {
               setInfo(""+e.getMessage());
               MessageBox.showException(e, false);
            }
         }
      }
   }
}
