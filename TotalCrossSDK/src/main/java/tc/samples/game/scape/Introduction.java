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



package tc.samples.game.scape;

import totalcross.game.*;
import totalcross.io.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

final class Introduction extends Container implements ProdConfig
{
   private Check chkSound;
   private Button btnNewGame;
   private static Animation anim;
   private ComboBox levelSelect;

   private static Introduction singleton;

   static void swapTo()
   {
      if (singleton == null)
         singleton = new Introduction();
      Scape.game.swap(singleton);
      if (!anim.isPlaying)
         anim.start(Animation.LOOPS_UNLIMITED);
   }

   protected Introduction()
   {
      try {anim = new AnimLogo();} catch (ImageException e) {} catch (IOException e) {}
   }

   public void initUI()
   {
      setRect(Scape.game.getRect());

      add(new Label("Scape - written by Frank Diebolt"), CENTER, TOP + 5);
      add(new Label("__________________"), CENTER, AFTER);

      add(anim, CENTER,AFTER+fmH*2);
      anim.enableEvents(Animation.eventNone);
      anim.start(Animation.LOOPS_UNLIMITED);

      chkSound = new Check("Sound enabled    ");
      add(chkSound, LEFT,AFTER+fmH,PREFERRED,PREFERRED+fmH/2);
      chkSound.setChecked(Scape.optSound.value);

      levelSelect = new ComboBox(new String[]{"easy", "medium", "hard"});
      add(levelSelect, RIGHT, SAME,PREFERRED,PREFERRED+fmH/2);
      levelSelect.setSelectedIndex(Scape.optDifficulty.value);
      add(new Label("Level "), BEFORE,SAME,PREFERRED,PREFERRED+fmH/2);
      
      add(btnNewGame = new Button("Start Game"), CENTER, BOTTOM - fmH, PARENTSIZE+80,SAME);
      btnNewGame.setBackColor(Color.ORANGE);
   }

   public void onEvent(Event event)
   {
      if (event.type != ControlEvent.PRESSED)
         return;

      Scape game = Scape.game;

      if (event.target == levelSelect)
      {
         Scape.optDifficulty.value = levelSelect.getSelectedIndex();
      }
      else if (event.target == chkSound)
      {
         Scape.optSound.value = !Scape.optSound.value;
         chkSound.setChecked(Scape.optSound.value);
      }
      else if (event.target == btnNewGame)
      {
         anim.stop();
         game.blankScreen();
         game.start();
      }
   }
}
