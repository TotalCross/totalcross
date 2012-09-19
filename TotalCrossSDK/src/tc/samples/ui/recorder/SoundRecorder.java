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

package tc.samples.ui.recorder;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.*;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.ui.media.MediaClip;
import totalcross.ui.media.MediaClipEvent;

public class SoundRecorder extends MainWindow
{
   Edit edTime;
   Button btnRecord, btnStop, btnPlay;
   MediaClip clip;
   TimerEvent timer;
   int initial;

   public SoundRecorder()
   {
      super("Sound recorder", TAB_ONLY_BORDER);
      setUIStyle(Settings.Flat);
   }

   public void initUI()
   {
      try
      {
         int wh = 30;
         // create the record, stop and play image buttons
         // record: a red circle
         Image imgRec = new Image(wh, wh);
         Graphics g = imgRec.getGraphics();
         g.backColor = Color.RED;
         g.foreColor = 1;
         g.fillCircle(wh / 2, wh / 2, wh / 2 - 1);
         g.drawCircle(wh / 2, wh / 2, wh / 2 - 1);
         // stop: a blue rectangle
         Image imgStop = new Image(wh, wh);
         g = imgStop.getGraphics();
         g.backColor = Color.BLUE;
         g.foreColor = 1;
         g.fillRect(0, 0, wh, wh);
         g.drawRect(0, 0, wh, wh);
         // play: a green right triangle
         Image imgPlay = new Image(wh, wh);
         g = imgPlay.getGraphics();
         g.drawArrow(wh / 3, 0, wh / 2, Graphics.ARROW_RIGHT, false, 0x00AA00);

         Font big = Font.getFont(false, Font.NORMAL_SIZE + 2);
         Label l = new Label("Length recorded: ");
         l.setFont(big);
         add(l, LEFT, TOP + 5);
         edTime = new Edit();
         edTime.setFont(big);
         add(edTime, AFTER + 5, SAME);
         edTime.setEnabled(false);

         add(btnStop = new Button(imgStop), CENTER, CENTER, PREFERRED + 10, PREFERRED + 10);
         add(btnRecord = new Button(imgRec), BEFORE - 20, SAME, SAME, SAME);
         add(btnPlay = new Button(imgPlay), AFTER + 20, SAME, SAME, SAME, btnStop);
         btnPlay.setEnabled(false);
         btnStop.setEnabled(false);
      }
      catch (ImageException e)
      {
         add(new Label("Error: " + e.getMessage()), CENTER, CENTER);
      }
   }

   File f;

   public void onEvent(Event e)
   {
      if (e.type == MediaClipEvent.END_OF_MEDIA)
      {
         try
         {
            clip.close();
            clip = null;
            f.close();
            f = null;
         }
         catch (IOException e1)
         {
            MessageBox.showException(e1, true);
         }
      }
      else if (e.type == TimerEvent.TRIGGERED && timer != null && timer.triggered)
         edTime.setText((Vm.getTimeStamp() - initial) + " ms");
      else if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == btnPlay)
         {
            try
            {
               f = new File("device/testRecord.wav", File.READ_WRITE);
               new MediaClip(f).start();
            }
            catch (Exception ee)
            {
               MessageBox.showException(ee, true);
            }
         }
         else if (e.target == btnStop)
         {
            if (clip != null)
            {
               try
               {
                  removeTimer(timer);
                  timer = null;
                  btnStop.setEnabled(false);
                  btnRecord.setEnabled(true);
                  clip.close();
                  f.close();
                  clip = null;
                  f = null;
                  btnPlay.setEnabled(true);
               }
               catch (Exception ee)
               {
                  MessageBox.showException(ee, true);
               }
            }
         }
         else if (e.target == btnRecord)
         {
            try
            {
               f = new File("device/testRecord.wav", File.CREATE_EMPTY);
               clip = new MediaClip(f);
               clip.record(44100, 16, true);
               btnRecord.setEnabled(false);
               btnPlay.setEnabled(false);
               btnStop.setEnabled(true);
               initial = Vm.getTimeStamp();
               timer = addTimer(100);
            }
            catch (Exception ee)
            {
               MessageBox.showException(ee, true);
            }
         }
      }
   }
}
