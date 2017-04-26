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
import totalcross.ui.image.*;
import totalcross.util.*;

public final class Block extends Sprite
{
   // random generator for the starting speeds
   private static Random rand = new Random(1);

   // other important sprite the ball sprite must know about for interaction...
   private Ball ball;

   // ball movement speed in both directions in double values for acceleration
   // precision
   private double speedx, speedy;

   // and in integer format for quicker move computing.
   private int ispeedx, ispeedy;
   private int ispeed;

   private int ix, iy;
   private int isx, isy;

   public Block(int ispeed, int ix, int iy, int isx, int isy, Image images,
         Ball ball) throws IllegalArgumentException, IllegalStateException, ImageException
   {
      super(images, -1, false, null);

      // reduce the playfield by the level & score display zone
      regionMiny += Scape.miny;

      this.ix = ix;
      this.iy = iy;
      this.isx = isx;
      this.isy = isy;

      this.ispeed = ispeed;

      this.ball = ball;
   }

   public void reinit()
   {
      setPos(ix, iy, false);
      speedx = ispeed + rand.nextDouble() * 1.5f * ispeed;
      speedy = (ispeed >> 1) + rand.nextDouble() * ispeed;
      ispeedx = isx;
      ispeedy = isy;
      speed = 1000;
      increaseSpeed(0);
   }

   public void increaseSpeed(int perc)
   {
      speedx *= (100.0 + perc) / 100;
      speedy *= (100.0 + perc) / 100;

      if (ispeedx >= 0)
         ispeedx = (int) speedx;
      else
         ispeedx = -(int) speedx;

      if (ispeedy >= 0)
         ispeedy = (int) speedy;
      else
         ispeedy = -(int) speedy;
   }

   public boolean onPositionChange()
   {
      boolean pval = true;

      image.nextFrame();

      if (centerX < regionMinx) // hits left border
      {
         centerX = regionMinx;
         ispeedx = -ispeedx;
         pval = false;
      }
      else if (centerX > regionMaxx) // hits right border
      {
         centerX = regionMaxx;
         ispeedx = -ispeedx;
         pval = false;
      }

      if (centerY < regionMiny) // hits top border
      {
         centerY = regionMiny;
         ispeedy = -ispeedy;
         pval = false;
      }
      else if (centerY > regionMaxy) // hits bottom border
      {
         centerY = regionMaxy;
         ispeedy = -ispeedy;
         pval = false;
      }

      if (collide(ball))
      {
         if (!Scape.NEVER_LOSE)
         {
            Scape.game.stop();
            return false;
         }
      }
      return pval;
   }

   public void move()
   {
      towardPos(centerX + ispeedx, centerY + ispeedy, true);
   }
}
