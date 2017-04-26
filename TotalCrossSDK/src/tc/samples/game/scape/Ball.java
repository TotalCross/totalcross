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
import totalcross.sys.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class Ball extends Sprite
{
   private int _regionMinx;
   private int _regionMiny;
   private int _regionMaxx;
   private int _regionMaxy;

   public Ball() throws ImageException, IOException
   {
      super(new Image("tc/samples/game/scape/ball.png").getSmoothScaledInstance(26*Math.min(Settings.screenWidth,Settings.screenHeight)/320*7, 26*Math.min(Settings.screenWidth,Settings.screenHeight)/320),
            7, Color.WHITE, Scape.SPRITE_BKGD_SAVING, null);

      this.doClip = false;

      regionMiny += Scape.miny;

      _regionMinx = regionMinx;
      _regionMiny = regionMiny;
      _regionMaxx = regionMaxx;
      _regionMaxy = regionMaxy;
   }

   public void reduceZone(int size)
   {
      regionMinx = _regionMinx + size;
      regionMiny = _regionMiny + size;
      regionMaxx = _regionMaxx - size;
      regionMaxy = _regionMaxy - size;
   }

   public void show()
   {
      image.nextFrame();
      super.show();
   }

   public boolean place(int x, int y, boolean doValidate)
   {
      return setPos(xpos = x, ypos = y, doValidate);
   }

   public boolean move(int dx, int dy)
   {
      return setPos(xpos += dx, ypos += dy, true);
   }

   private int xpos, ypos;
}
