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
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.sys.Settings;

final class GameOver extends Container
{
	private Breakout game;
  	private Button button;
  	private Label label;

  	protected GameOver(Breakout game)
  	{
    	this.game=game;
  	}

  	public void initUI()
  	{
    	setRect(game.getRect());

    	int bgColor = 0x999900;
    	label.setBackForeColors(bgColor, Color.YELLOW);
    	Font bigFont = Font.getFont(font.name, true, Font.BIG_SIZE);
		label.setFont(bigFont);

      add(label,CENTER,CENTER); // same bug: fonts must be set before placing the control on screen
      add(button);
      button.setRect(CENTER,AFTER+10,PREFERRED+Settings.screenWidth/16,PREFERRED+Settings.screenWidth/16);
      setBackColor(bgColor);
   }

	public void announce(int nextLevel)
	{
		if(nextLevel > 0)
		{
			label = new Label("Level " + nextLevel);
			button = new Button("Start");
		}
		else
		{
			label = new Label("GAME OVER");
			button = new Button("New Game");
		}

      game.swap(this);
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
