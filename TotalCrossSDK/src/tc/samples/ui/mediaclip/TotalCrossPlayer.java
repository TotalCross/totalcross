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



package tc.samples.ui.mediaclip;

import totalcross.io.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.ui.media.*;

/** A sample showing how to play audio. */

public class TotalCrossPlayer extends MainWindow
{
   MenuBar playerMenuBar;

   Edit edFilePathPlay;
   Label placeholder;

   Image imgPlay;
   Image imgStop;
   Image imgPause;

   Button btPlay;
   Button btPause;
   Button btStop;

   MediaClip media;
   File file;

   int wh = 25;

   public TotalCrossPlayer()
   {
      super("TotalCrossPlayer", NO_BORDER);
   }

   public void initUI()
   {
      MenuItem col0[] = { new MenuItem("File"), new MenuItem("Open"), new MenuItem(), new MenuItem("Exit"), };
      setMenuBar(playerMenuBar = new MenuBar(new MenuItem[][] { col0 }));

      add(edFilePathPlay = new Edit(), LEFT, TOP, FILL, PREFERRED);

      try
      {
         createIcons();
      }
      catch (ImageException e)
      {
         MessageBox.showException(e, true);
         exit(0);
      }

      Button.commonGap = 5;
      add(btPlay = new Button(imgPlay), CENTER, BOTTOM);
      add(btPause = new Button(imgPause), CENTER, BOTTOM);
      add(btStop = new Button(imgStop), BEFORE - 3, SAME);
      btPlay.setBorder(Button.BORDER_NONE);
      btPause.setBorder(Button.BORDER_NONE);
      btStop.setBorder(Button.BORDER_NONE);
      btPause.setVisible(false);

      add(placeholder = new Label(), LEFT, AFTER + 2, FILL, FIT - 2, edFilePathPlay);
      placeholder.setBackColor(Color.BLACK);
   }

   private String selectFile() throws IOException
   {
      FileChooserBox fcb = new FileChooserBox(new FileChooserBox.Filter()
      {
         public boolean accept(File f) throws IOException
         {
            return f.isDir() || f.getPath().toLowerCase().endsWith(".wav");
         }
      });
      fcb.mountTree("device/");
      fcb.popup();
      return fcb.getAnswer();
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
         {
            if (event.target == playerMenuBar)
               handleMenuEvent();
            else if (event.target instanceof Button)
               handleButtonsEvent((Control) event.target);
         }
         break;
         case MediaClipEvent.STARTED:
         {
            btPlay.setVisible(false);
            btPause.setVisible(true);
            btStop.setEnabled(true);
         }
         break;
         case MediaClipEvent.STOPPED:
         {
            btPlay.setVisible(true);
            btPause.setVisible(false);
         }
         break;
         case MediaClipEvent.CLOSED:
         {
            btPlay.setVisible(true);
            btPause.setVisible(false);
            btStop.setEnabled(false);
            if (media != null)
               try
               {
                  media.close();
               }
               catch (IOException e)
               {
                  MessageBox.showException(e, true);
               }
            if (file != null)
               try
               {
                  file.close();
               }
               catch (IOException e)
               {
                  MessageBox.showException(e, true);
               }
            media = null;
            file = null;
         }
         break;
         case MediaClipEvent.END_OF_MEDIA:
         {
            btPlay.setVisible(true);
            btPause.setVisible(false);
            btStop.setEnabled(false);
         }
         break;
      }
   }

   private void handleButtonsEvent(Control target)
   {
      try
      {
         if (target == btPlay)
         {
            if (media == null)
            {
               try
               {
                  String path = edFilePathPlay.getText();
                  file = new File(path.length() == 0 ? "foo" : path, File.READ_WRITE);
                  media = new MediaClip(file);
               }
               catch (FileNotFoundException feof)
               {
                  new MessageBox("Error", "Invalid file path or file name.").popup();
               }
            }
            if (media != null)
               media.start();
         }
         else if (target == btPause)
            media.stop();
         else if (target == btStop)
            media.close();
      }
      catch (IOException e1)
      {
         MessageBox.showException(e1, true);
      }
   }

   private void handleMenuEvent()
   {
      switch (playerMenuBar.getSelectedIndex())
      {
         case 1:
            try
            {
               String path = selectFile();
               if (path != null)
                  edFilePathPlay.setText(path);
            }
            catch (IOException e)
            {
               MessageBox.showException(e, true);
            }
         break;
         case 3:
            exit(0);
         break;
      }
   }

   /**
    * Initializes the image objects we'll be using in our application.
    * 
    * @throws ImageException
    */
   private void createIcons() throws ImageException
   {
      // stop: a blue rectangle
      imgStop = new Image(wh, wh);
      Graphics g = imgStop.getGraphics();
      g.backColor = Color.RED;
      g.foreColor = 1;
      g.fillRect(0, 0, wh, wh);
      g.drawRect(0, 0, wh, wh);
      // play: a green right triangle
      imgPlay = new Image(wh, wh);
      g = imgPlay.getGraphics();
      g.drawArrow(wh / 3, 0, wh / 2, Graphics.ARROW_RIGHT, false, 0x00AA00);
      // pause: two vertical yellow rectangles
      imgPause = new Image(wh, wh);
      g = imgPause.getGraphics();
      g.backColor = Color.YELLOW;
      g.foreColor = 1;
      g.fillRect(0, 0, wh / 5 * 2, wh);
      g.drawRect(0, 0, wh / 5 * 2, wh);
      g.fillRect(3 * wh / 5, 0, wh / 5 * 2, wh);
      g.drawRect(3 * wh / 5, 0, wh / 5 * 2, wh);
   }
}
