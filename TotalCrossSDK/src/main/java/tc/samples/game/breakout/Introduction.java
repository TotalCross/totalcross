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

import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.sys.Settings;
import totalcross.ui.*;
import totalcross.ui.event.*;

final class Introduction extends Container
{
	private Breakout game;
  	private Button button;
  	private static Introduction singleton;

	static void swapTo(Breakout game)
  	{
    	if (singleton==null) singleton=new Introduction(game);
    	game.swap(singleton);
  	}

  	protected Introduction(Breakout game)
  	{
    	this.game=game;
  	}

  	public void initUI()
  	{
    	setRect(game.getRect());

      int bgColor = 0x999900;
		Label label1, label2, label3;
      label1 = new Label("TotalCross Breakout");
      label2 = new Label("© Johannes Wallroth");
      label3 = new Label("www.programming.de");
      label1.setBackForeColors(bgColor, 0xFFFF00);
      label2.setBackForeColors(bgColor, Color.WHITE);
      label3.setBackForeColors(bgColor, 0xCCCCFF);
      Font bigFont = Font.getFont(font.name, true, Font.BIG_SIZE);
      label1.setFont(bigFont); // FONT MUST BE SET ***BEFORE*** ADDING THE CONTROL TO THE SCREEN

      add(label1,CENTER,TOP+15);
    	add(label2,CENTER,AFTER+5);
    	add(label3,CENTER,AFTER+5);

      add(button=new Button("Start Game"));
      button.setRect(CENTER,BOTTOM-15,PREFERRED+Settings.screenWidth/16,PREFERRED+Settings.screenWidth/16);
    	setBackColor(bgColor);
  	}

  	public void onEvent(Event event)
  	{
    	if (event.type == ControlEvent.PRESSED && event.target==button)
    	{
     		game.blankScreen();
         game.start();
    	}
  	}
}
