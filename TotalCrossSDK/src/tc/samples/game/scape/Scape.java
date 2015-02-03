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
import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.sys.*;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.*;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.ui.media.*;
import totalcross.util.Properties;

/**
 * Scape game
 */
public class Scape extends GameEngine implements ProdConfig
{
   protected final static boolean ARCADE_GAME = true;

   protected final static boolean CLEAR_SCREEN = true;
   protected final static boolean SPRITE_BKGD_SAVING = false;
   protected final static boolean HAS_UI = false;

   protected final static int BLOCKS = 4;
   protected final static int RADIUS = Settings.screenWidth/2;
   protected final static int PEN_OFFSET = 24;

   protected final static int MAX_DIFFICULTIES = 3;

   // Increase the game level each time the scores is a multiple of 128
   private final static int SCORE_NEXT_LEVEL = (128 - 1);

   // Ball speed increase of 10% at each level
   private final static int SPEED_INCR_PERC = 10;

   // Racket move speed when controlled with the keyboard
   private final static int BLOCK_SPEED_PERC1000 = 22;

   // reduce the game playground to display the game level and score
   protected static int miny;
   protected static int maxw, maxh;

   protected static int borderWidth = 2;
   protected static int blockSizePerc = 24;
   protected static int moveDist;

   private int midx, midy;

   // game level & score
   private int level, score;

   // sound support version
   private static MediaClip lostClip;

   // define the game sprites
   private Block blocks[];
   private Ball ball;

   // 2 text renderers to quickly display level and score values
   private TextRenderer levelRenderer;
   private TextRenderer scoreRenderer;

   // define 2 game settings,
   // by declaring them static they can be accessed in all classes.

   protected static Properties.Boolean optSound;
   protected static Properties.Int optDifficulty;

   // the high scores and settings
   Options settings;

   protected static Scape game;

   public Scape()
   {
      setUIStyle(Settings.Android);

      // adjust attributes
      gameName = "Scape";
      // when not run on device, appCreatorId does not always return the same value.
      gameCreatorID = !Settings.onJavaSE ? totalcross.sys.Settings.applicationId
            : PC_CREATOR_ID;
      gameVersion = 100;
      gameHighscoresSize = 0;
      gameRefreshPeriod = ARCADE_GAME ? (Settings.keyboardFocusTraversable?100:100) : NO_AUTO_REFRESH;
      gameDoClearScreen = CLEAR_SCREEN;
      gameHasUI = HAS_UI;
      setBackColor(0x66FFFF);

      MainWindow.setDefaultFont(font.asBold());
      game = this;
   }

   public void onOtherEvent(Event evt)
   {
      if (evt.type == MediaClipEvent.END_OF_MEDIA)
         showIntroduction();
   }
   
   public void onGameInit()
   {
      // access the game settings
      settings = getOptions();

      // if the properties do not yet exist, use the specified default values
      optSound = settings.declareBoolean("sound", false);
      optDifficulty = settings.declareInteger("difficulty", 0);

      try
      {
         levelRenderer = createTextRenderer(getFont(), Color.BLACK, "level: ", 2, false);
         scoreRenderer = createTextRenderer(getFont(), Color.BLACK, "score: ", 5, true);
      }
      catch (ImageException e)
      {
         MessageBox.showException(e,true);
      }

      // set the screen dimensions
      maxw = Settings.screenWidth;
      maxh = Settings.screenHeight;

      // keyboard moves distance
      moveDist = maxw / 20;

      midx = maxw >> 1;
      midy = maxh >> 1;
      miny = scoreRenderer.getHeight() + 2;
      maxh -= miny;

      frameSizes = new int[MAX_DIFFICULTIES];
      for (int i = 0; i < MAX_DIFFICULTIES; i++)
      {
         frameSizes[i] = maxw * i / 16;
      }

      // this is the hunted object
      try {ball = new Ball();} catch (ImageException e) {} catch (IOException e) {}

      try
      {
         lostClip = new MediaClip(new ByteArrayStream(Vm.getFile("tc/samples/game/scape/lost.wav")));
      }
      catch (IOException e)
      {
         MessageBox.showException(e, true);
      }

      showIntroduction();
   }

   private static int frameSizes[];

   public void onGameStart()
   {
      if (blocks == null)
         try
         {
            Image blockImg;
            try {blockImg = new Image("tc/samples/game/scape/block.png");} catch (Exception e) {blockImg = new Image(40,40);}

            int baseSize = Math.min(maxw, maxh);
            int blockSize = baseSize * blockSizePerc / 100;
            int speed = baseSize * BLOCK_SPEED_PERC1000 / 1000;

            // these are the hunting blocks
            blocks = new Block[BLOCKS];
            for (int i = 0; i < BLOCKS; i++)
            {
               int q = (i << 1) + 1;
               int vecx = (int) (RADIUS * Math.cos(2 * Math.PI * q / (BLOCKS << 1)));
               int vecy = (int) (RADIUS * Math.sin(2 * Math.PI * q / (BLOCKS << 1)));
               

               int xx = (int) (midx + vecx);
               int yy = (int) (midy + vecy);

               // 14, 26, 46, 62
               // 62, 46, 26, 14
               int bs4 = blockSize >> 2;
               int sx = bs4 + i * bs4;
               int sy = bs4 + bs4 * (BLOCKS - 1 - i);

               blocks[i] = new Block(speed, xx, yy, vecx < 0 ? -1 : 1, vecy < 0 ? -1 : 1, blockImg.getHwScaledInstance(sx, sy), ball);
            }
         }
         catch (ImageException e)
         {
            MessageBox.showException(e,true);
         }

      ball.reduceZone(frameSizes[optDifficulty.value]);

      for (int i = 0; i < BLOCKS; i++)
      {
         blocks[i].reinit();
      }
      ball.place(midx, midy, false);

      level = 1;
      score = 0;
   }

   public void onGameStop()
   {
      if (Scape.optSound.value)
      {
         if (lostClip != null)
         {
            try
            {
               lostClip.start();
            }
            catch (IOException e)
            {
               MessageBox.showException(e, true);
            }
            if (!Settings.onJavaSE) 
               return; // showIntroduction will be displayed when the sound stops - in Java, stop is never called
         }
      }
      // when the game stops, popup the GameOver window
      showIntroduction();
   }

   private boolean setcolor;

   public final void onPaint(Graphics gfx)
   {
      if (gameIsRunning)
      {
         if (!setcolor)
         {
            setcolor = true;
            gfx.foreColor = 0x0000FF;
         }

         for (int i = borderWidth; i >= 1; i--)
         {
            int o = i + frameSizes[optDifficulty.value];
            int w = o << 1;
            gfx.drawRect(o, miny + o, maxw - w, maxh - w);
         }

         for (int i = 0; i < BLOCKS; i++)
         {
            Block b = blocks[i];
            b.move();
            b.show();
         }

         ball.show();

         score++;

         // Increase the game level each time the scores is a multiple of SCORE_NEXT_LEVEL
         if ((score & SCORE_NEXT_LEVEL) == 0)
         {
            // increase the level and the ball speed
            level++;
            for (int i = 0; i < BLOCKS; i++)
               blocks[i].increaseSpeed(SPEED_INCR_PERC);
         }

         // render level & score
         levelRenderer.display(16, 2, level);
         scoreRenderer.display(Settings.screenWidth >> 1, 2, score);
      }
   }

   public final void onPenDown(PenEvent evt)
   {
      if (!ball.place(evt.x - PEN_OFFSET, evt.y - PEN_OFFSET, true))
         stop();
      // if non arcade game is selected, redrawings have to be called explicitly
      if (!ARCADE_GAME)
         refresh();
   }

   public final void onPenDrag(PenEvent evt)
   {
      if (!ball.place(evt.x - PEN_OFFSET, evt.y - PEN_OFFSET, true))
         stop();
      // if non arcade game is selected, redrawings have to be called explicitly
      if (!ARCADE_GAME)
         refresh();
   }

   Container blankContainer;
   /** Creates and places a blank container in the screen. */
   public void blankScreen()
   {
      if (blankContainer == null)
      {
         blankContainer = new Container();
         blankContainer.setRect(getRect());
         blankContainer.setBackColor(backColor);
      }
      swap(blankContainer);
   }

   /**
    * display the game introduction screen.
    */
   public final void showIntroduction()
   {
      Introduction.swapTo();
   }
}
