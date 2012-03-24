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



package tc.samples.ui.image.gifanimated;

import totalcross.game.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class GifAnimatedTest extends MainWindow
{
   static
   {
      Settings.useNewFont = true;
   }

   private boolean no;
   private Button btnStartStop;
   private Button btnNext;
   private Animation anim;
   private ComboBox cbEffect;
   private int effect;
   private Label lab;

   public void initUI()
   {
      add(btnNext = new Button(" Next "), CENTER, AFTER+3);
      add(btnStartStop = new Button("Start/Stop"), RIGHT, SAME);
      add(lab = new Label("99999999ms"),LEFT,SAME);
      lab.setText("");
      add(new Label("Effect: "), LEFT, BOTTOM);
      String[] items  = {"normal","scaledBy","smoothScaledBy","getRotatedScaledInstance","getTouchedUpInstance","changeColors","fadedInstance","applyColor1","applyColor2/dither"};      
      add(cbEffect = new ComboBox(items), AFTER+2,SAME,FILL,PREFERRED);
      cbEffect.setSelectedIndex(0);
      next(false);
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
         {
            if (event.target == cbEffect && effect != cbEffect.getSelectedIndex())
               next(true);
            else
            if (event.target == btnStartStop)
            {
               if (anim.isPaused)
                  anim.resume();
               else
                  anim.pause();
            }
            else
            if (event.target == btnNext)
               next(false);
            break;
         }
      }
   }

   /**
    * shows next frame
    */
   private void next(boolean changeEffect)
   {
      if (!changeEffect)
         no = !no;
      try
      {
         if (anim != null)
         {
            anim.stop();
            remove(anim);
            anim = null;
         }
         Image img = new Image(no ? "tc/samples/ui/image/gifanimated/wolf.gif" : "tc/samples/ui/image/gifanimated/alligator.gif");
         effect = cbEffect.getSelectedIndex();
         int ini = Vm.getTimeStamp();
         switch (effect)
         {
            case 1: img = img.scaledBy(2,2); break;
            case 2: img = img.smoothScaledBy(2,2,img.transparentColor); break;
            case 3: img = img.getRotatedScaledInstance(50,90, -1); break;
            case 4: img = img.getTouchedUpInstance((byte)50,(byte)100); break;
            case 5: img.changeColors(no ? 0xA5B500 : 0x31CE31, 0x0077E5); break;
            case 6: img = img.getFadedInstance(backColor); break;
            case 7: img.applyColor(Color.RED); break;
            case 8: img.applyColor2(Color.RED); img.dither(); break;
         }
         int fim = Vm.getTimeStamp();
         lab.setText((fim-ini)+"ms");
         anim = new Animation(img, 200);
         anim.pauseIfNotVisible = true;
         add(anim, CENTER,CENTER,PREFERRED,PREFERRED,btnNext);
         anim.start(Animation.LOOPS_UNLIMITED);
      }
      catch (Exception e)
      {
         MessageBox.showException(e, true);
      }
   }
}
