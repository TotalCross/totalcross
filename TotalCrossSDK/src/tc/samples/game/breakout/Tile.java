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
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

public class Tile
{
   private static final int SKIN = 2;

   private static Sprite animSprite;
   private static int halfHeight,halfWidth;

  	public int color;
   protected int centerX,centerY;

   public Tile() throws ImageException, IOException
   {
      if (animSprite == null)
      {
         Image src = new Image("tc/samples/game/breakout/tiles.png");
         if (Settings.screenWidth != 320)
            src = src.scaledBy(Settings.screenWidth / 320d, Settings.screenWidth / 320d); // do NOT use smooth resize!
         animSprite = new Sprite(src, 7, -1, false, null);
         halfHeight = animSprite.height >> 1;
         halfWidth = animSprite.width >> 1;
      }
   }

   public int width()
   {
      return animSprite.width;
   }
   public int height()
   {
      return animSprite.height;
   }

  	public void set(int index, int x, int y)
  	{
  		centerX = x;
   	centerY = y;
    	color = index;
  	}

  	public boolean hit(Ball ball)
  	{
      int ball_hw = ball.width/2;
      int ball_hh = ball.height/2;

      if(ball.centerY-ball_hh <= centerY+halfHeight && ball.centerY-ball_hh > centerY+halfHeight-SKIN)
      {
         // ball hit tile's bottom side
         ball.centerY = centerY + halfHeight + ball_hh;
         ball.ispeedy = -ball.ispeedy;
      }
      else if(ball.centerY+ball_hh >= centerY-halfHeight && ball.centerY+ball_hh < centerY-halfHeight+SKIN)
      {
         // ball hit tile's top side
         ball.centerY = centerY - halfHeight - ball_hh;
         ball.ispeedy = -ball.ispeedy;
      }
      else if(ball.centerX+ball_hw >= centerX-halfWidth && ball.centerX+ball_hw < centerX-halfWidth+SKIN)
      {
         // ball hit tile's left side
         ball.centerX = centerX - halfWidth - ball_hw;
         ball.ispeedx = -ball.ispeedx;
      }
      else if(ball.centerX-ball_hw <= centerX+halfWidth && ball.centerX-ball_hw > centerX+halfWidth-SKIN)
      {
         // ball hit tile's right side
         ball.centerX = centerX + halfWidth + ball_hw;
         ball.ispeedx = -ball.ispeedx;
      }
		return (--color <= 0);
	}

   public boolean collide(Sprite s)
   {
      animSprite.centerX = centerX;
      animSprite.centerY = centerY;
      return animSprite.collide(s);
   }

   public void show()
   {
      animSprite.image.setCurrentFrame(color);
      animSprite.centerX = centerX;
      animSprite.centerY = centerY;
      animSprite.show();
   }
}
