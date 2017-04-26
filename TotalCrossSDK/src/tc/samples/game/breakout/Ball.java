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

import totalcross.game.Sprite;
import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.ui.gfx.Coord;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Random;

public final class Ball extends Sprite
{
   private Breakout game;
   private Level level;
   private Racket racket;
   private int ballHitY;
   private Random rand = new Random();
   protected int ispeedx, ispeedy;

   public Ball(Breakout game, Racket racket, Level level) throws ImageException, IOException
   {
      super(new Image("tc/samples/game/breakout/ball.png").setTransparentColor(0x000099).smoothScaledFromResolution(320), -1, true, null);

      this.game = game;
      this.racket = racket;
      this.level = level;

      speed = 100;
      reinit(level);
   }

   public void reinit(Level level)
   {
      this.level = level;
      Coord pos = racket.getBallPosition();
      ballHitY = pos.y - height / 2;
      if (Settings.screenHeight == 160)
         ballHitY--;

      setPos(pos.x, pos.y, false);

      int amp = Settings.screenWidth / 40;
      ispeedx = rand.between(-amp, amp);
      if (ispeedx == 0)
         ispeedx = -amp / 2;
      ispeedy = -(4 * Settings.screenHeight / 100);
   }

   public boolean onPositionChange()
   {
      if (centerX < regionMinx) // hits left border
      {
         centerX = regionMinx;
         ispeedx = -ispeedx;
         return false;
      }
      if (centerX > regionMaxx) // hits right border
      {
         centerX = regionMaxx;
         ispeedx = -ispeedx;
         return false;
      }
      if (centerY < regionMiny) // hits top border
      {
         centerY = regionMiny;
         ispeedy = -ispeedy;
         return false;
      }
      if (centerY > regionMaxy) // hits bottom border
      {
         centerY = regionMaxy;
         game.stop();
         return false;
      }
      if (collide(racket))
      {
         ispeedx = racket.hit(centerX);
         ispeedy = -ispeedy;
         centerY = ballHitY;
         // guich: avoids trash when the ball is below the racket
         racket.hide();
         hide();
         show();
         racket.show();
         return false;
      }
      return !level.collide(this);
   }

   public void move()
   {
      towardPos(centerX + ispeedx, centerY + ispeedy, true);
      show();
   }
}
