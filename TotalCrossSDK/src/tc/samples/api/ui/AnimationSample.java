/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.game.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class AnimationSample extends BaseContainer
{
   private Button btnStartStop;
   private Animation anim;
   private ComboBox cbEffect;
   private int effect;

   public void initUI()
   {
      super.initUI();
      setTitle("Image Animation");
      
      add(btnStartStop = new Button(" Start/Stop "), CENTER, TOP+gap);
      add(new Label("Effect: "), LEFT+gap, BOTTOM-gap);
      String[] items  = {"normal","scaledBy","smoothScaledBy","getRotatedScaledInstance","getTouchedUpInstance","changeColors","fadedInstance","applyColor2/dither"};
      ComboBox.usePopupMenu = false;
      add(cbEffect = new ComboBox(items), AFTER+gap,SAME,FILL-gap,PREFERRED);
      cbEffect.setSelectedIndex(0);
      next(false);
   }
   
   public void onAddAgain()
   {
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
            break;
         }
      }
   }

   /**
    * shows next frame
    */
   private void next(boolean changeEffect)
   {
      try
      {
         onRemove();
         Image img = new Image("ui/images/alligator.gif");
         effect = cbEffect.getSelectedIndex();
         int ini = Vm.getTimeStamp();
         double scale = Settings.isIOS() ? 1.5 : 2; // ios has less opengl memory
         switch (effect)
         {
            case 1: img = img.scaledBy(scale,scale); break;
            case 2: img = img.smoothScaledBy(scale,scale); break;
            case 3: img = img.getRotatedScaledInstance(50,90, -1); break;
            case 4: img = img.getTouchedUpInstance((byte)50,(byte)100); break;
            case 5: img.changeColors(0xFF31CE31, 0xFFFF00FF); break;
            case 6: img = img.getFadedInstance(); break;
            case 7: img.applyColor2(Color.RED); img.getGraphics().dither(0,0,img.getWidth(),img.getHeight()); break;
         }
         if (Settings.isOpenGL) img.applyChanges();
         int fim = Vm.getTimeStamp();
         setInfo((fim-ini)+"ms");
         anim = new Animation(img, 200);
         anim.pauseIfNotVisible = true;
         add(anim, CENTER,CENTER,PREFERRED,PREFERRED);
         anim.start(Animation.LOOPS_UNLIMITED);
      }
      catch (Throwable e)
      {
         MessageBox.showException(e, true);
      }
   }
   
   public void onRemove()
   {
      if (anim != null)
      {
         anim.stop();
         remove(anim);
         anim = null;
      }
   }
}
