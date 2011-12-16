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

import totalcross.game.Animation;
import totalcross.io.IOException;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Check;
import totalcross.ui.ComboBox;
import totalcross.ui.Container;
import totalcross.ui.Label;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.image.ImageException;

final class Introduction extends Container implements ProdConfig
{
   private Check chkSound;
   private Button btnNewGame;
   private Button btnQuit;
   private static Animation anim;
   private ComboBox levelSelect;

   private static Introduction singleton;

   static void swapTo()
   {
      if (singleton == null)
      {
         singleton = new Introduction();
      }
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

      add(new Label("Scape v" + PC_VERSION), CENTER, TOP + 5);
      add(new Label("written by Frank Diebolt."), CENTER, AFTER + 4);
      add(new Label("__________________"), CENTER, AFTER);

      add(btnQuit = new Button("Game exit"), RIGHT - 20, BOTTOM - 10);
      add(btnNewGame = new Button("start Game"), SAME, BEFORE - 7);

      add(anim, RIGHT - 10, BEFORE - 15, btnNewGame);
      anim.enableEvents(Animation.eventNone);
      anim.start(15);

      chkSound = new Check("Sound enabled");
      add(chkSound, BEFORE - 10, SAME, anim);
      chkSound.setChecked(Scape.optSound.value);

      Label difficulty = new Label("Level");
      add(difficulty, SAME, AFTER + 7, chkSound);

      levelSelect = new ComboBox(new String[]
      {
            "easy", "medium", "hard"
      });
      add(levelSelect, AFTER + 3, SAME, difficulty);
      levelSelect.setSelectedIndex(Scape.optDifficulty.value);
   }

   public void onEvent(Event event)
   {
      if (event.type != ControlEvent.PRESSED)
         return;

      Scape game = Scape.game;

      if (event.target == levelSelect)
      {
         Scape.optDifficulty.value = levelSelect.getSelectedIndex();
         if (DEBUG) Vm.debug("level is " + Scape.optDifficulty.value);
      }
      else if (event.target == chkSound)
      {
         Scape.optSound.value = !Scape.optSound.value;
         chkSound.setChecked(Scape.optSound.value);
         if (DEBUG) Vm.debug("sound is " + Scape.optSound.value);
      }
      else if (event.target == btnNewGame)
      {
         anim.stop();
         game.blankScreen();
         game.start();
      }
      else if (event.target == btnQuit)
      {
         totalcross.ui.MainWindow.exit(0);
      }
   }
}
