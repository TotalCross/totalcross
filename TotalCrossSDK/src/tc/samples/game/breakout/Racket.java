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

public class Racket extends Sprite
{
  	private Coord ballPos=new Coord();
   private int adjust1, adjust2, adjust3, adjust4, adjust5, adjust6, adjust7;

	public Racket() throws ImageException, IOException
  	{
    	super(new Image("tc/samples/game/breakout/racket.png").setTransparentColor(0x000099).smoothScaledBy(Settings.screenWidth/160d, 1), -1, true, null);

      int halfWidth = width / 2;
   	adjust1 = halfWidth - ((width * 1) / 8);
   	adjust2 = halfWidth - ((width * 2) / 8);
   	adjust3 = halfWidth - ((width * 3) / 8);
   	adjust4 = halfWidth - ((width * 4) / 8);
   	adjust5 = halfWidth - ((width * 5) / 8);
   	adjust6 = halfWidth - ((width * 6) / 8);
   	adjust7 = halfWidth - ((width * 7) / 8);

     	speed=10;
  	}

  	public final Coord getBallPosition()
  	{
    	ballPos.x=centerX;
    	ballPos.y=centerY - height/2;
    	return ballPos;
  	}

  	public final int hit(int x)
  	{
      int xx = centerX-x; // x < centerX-adjust -> x-centerX < -adjust (x-1) -> centerX-x >= adjust

      //return -4 .. +4;
  		if(xx >= adjust1) return -4;
  		if(xx >= adjust2)	return -3;
  		if(xx >= adjust3)	return -2;
  		if(xx >= adjust4)	return -1;
  		if(xx >= adjust5)	return  1;
  		if(xx >= adjust6)	return  2;
  		if(xx >= adjust7)	return  3;
		return 4;
  	}

  	public final void move(boolean left, int speed)
  	{
    	if (left)
    	   centerX -= speed;
    	else
      	centerX += speed;

    	setPos(centerX,centerY,true);
  	}
}
