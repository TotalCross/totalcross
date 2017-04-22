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



package tc.samples.game.breakout;

//////////////////////////////// TotalCross Breakout ///////////////////////////////////////
///////////// v1.2 © 2004, April 16st, Johannes Wallroth, Germany////////////////////////
////////////////////////////// www.programming.de /////////////////////////////////////////
/// Congratulations to Frank Diebolt for his game library and some great optimizations! ///
///////////////////////////////////////////////////////////////////////////////////////////

import totalcross.game.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

/**<pre>
///////////////////////////// TotalCross Breakout ///////////////////////////////////
///////// v1.01 © 2004, January 28th, Johannes Wallroth, Germany////////////////////
/////////////////////////// www.programming.de /////////////////////////////////////

A very simple Breakout clone, made with Frank Diebolt's Game Library for TotalCross.
The game has three levels and different blocks - the yellow ones need 3 hits to break and change
their color from yellow to orange to red - the other ones are destroyed at the first hit.

The game can be played with the stylus and (not very good) also with the keys.

The program works only on the new 320x320 high resolution OS 5 color devices!

Many thanks for TotalCross - http://www.TotalCross.com.br
and the SuperWabaJump Toolkit http://sourceforge.net/project/shownotes.php?release_id=204193
which were used for this program.

Sourcecode for this game available under Public Domain license.
 </pre>
 */
public class Breakout extends GameEngine
{
	public int currentLevel = 1;
	private int racketY;

	private Racket racket;
	private Ball ball;
	private Level level;

	private TextRenderer levelRenderer, tilesRenderer;
   private static final int BACKG = 0x000099;

   private boolean levelChanged=true;

   public Breakout()
  	{
      setUIStyle(Settings.Flat);
   	gameName = "Breakout";
      gameCreatorID = "tCbA";
      gameVersion = 140;
   	gameRefreshPeriod = (Settings.keyboardFocusTraversable?70:50);
   	gameDoClearScreen = false;
   	gameHasUI = true;

   	MainWindow.setDefaultFont(MainWindow.getDefaultFont().asBold());
  	}

   private static final int PERC = 10;
   private int levelX,tilesX;

	public void onGameInit()
	{
      setBackColor(BACKG);

      try
      {
         levelRenderer=createTextRenderer(getFont(),0x9999FF,"Level: ",1,true);
         tilesRenderer=createTextRenderer(getFont(),0x9999FF,"Remaining: ",2,true);
       	racket = new Racket();
         level = new Level(2 + levelRenderer.getHeight());
         ball = new Ball(this,racket,level);
      } catch (Exception e) {MessageBox.showException(e,true); MainWindow.exit(0);}

      levelX = Settings.screenWidth * PERC / 100;
      tilesX = Settings.screenWidth - tilesRenderer.getWidth() - levelX;
      racketY = Settings.screenHeight - racket.height - 2;

      Introduction.swapTo(this);
   }

   public void onGameStart()
   {
   	racket.setPos(Settings.screenWidth / 2,racketY,false);
      if (levelChanged)
         level.set(currentLevel);
 		ball.reinit(level);
      levelChanged = true;
   }

   public void onGameStop()
   {
      if (level.tilesLeft == 0)
      {
         if (currentLevel < Level.MAX_LEVELS)
            currentLevel++;
         else
            currentLevel = 1;
         levelChanged = true;
      }

      Vm.sleep(350);
      GameOver go = new GameOver(this);
      go.announce(currentLevel);
   }

   public final void onPaint(Graphics gfx)
   {
      if (gameIsRunning)
      {
         gfx.backColor = BACKG;
         gfx.fillRect(0,0,Settings.screenWidth, Settings.screenHeight);
         levelRenderer.display(levelX, 2, currentLevel);
         tilesRenderer.display(tilesX, 2, level.tilesLeft);
         level.show();
         if (level.tilesLeft == 0)
            stop();
         else
         {
            if (!levelChanged) ball.show();
            levelChanged = false; // don't remove this from here or the racket will get dirty by the ball when the game inits
            racket.show();
            ball.move();
         }
      }
   }

   public final void onPenDown(PenEvent evt)
   {
      if (gameIsRunning)
   	   racket.setPos(evt.x, racketY, true);
   }
   public final void onPenDrag(PenEvent evt)
   {
      if (gameIsRunning)
   	   racket.setPos(evt.x, racketY, true);
   }

   Container blankContainer;
   public void blankScreen()
   {
      if (blankContainer == null)
      {
         blankContainer = new Container();
         blankContainer.setRect(10000,0,0,0); // guich@512_7: we don't want this to overwrite the game's window, so just set all to 0.
         blankContainer.setBackColor(backColor);
      }
      swap(blankContainer);
   }
}
