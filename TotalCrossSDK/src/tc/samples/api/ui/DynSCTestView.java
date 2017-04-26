/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api.ui;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;

public class DynSCTestView extends DynamicScrollContainer.AbstractView
{
	public static boolean dynamicHeight;

	private int id;

	public String text;

	private Font f;

	public DynSCTestView(int id, Font f) {
		super();
		this.f = f;
		this.id = id;
	}

	public int getHeight()
	{
		text = "Row id: " + id + "\n\n";
		if (dynamicHeight)
		{
			/*
			 * if your views height can only be determined on rendering then you will need to work out the height
			 * dynamically based on font, line wrapping etc
			 */
			if (id % 5 == 0)
			{
				text += "Lorem ipsum pretium class porta gravida lobortis ipsum, etiam posuere elit torquent duis nostra elit, sagittis etiam dapibus libero tellus facilisis curabitur facilisis sed sagittis posuere imperdiet bibendum.";

			}
			else if (id % 4 == 0)
			{
				text += "Felis turpis mauris per nullam donec lobortis maecenas metus orci viverra vulputate non nostra platea conubia, lectus proin mauris ligula aenean, adipiscing purus accumsan commodo laoreet facilisis tellus nisl litora vehicula.";

			}
			else if (id % 3 == 0)
			{
				text += "Venenatis consequat adipiscing ullamcorper etiam tellus nam mattis vehicula nostra leo rutrum lorem at suscipit, suspendisse aliquam aptent netus nisi rutrum fringilla arcu vehicula ut ante nam in.";

			}
			else if (id % 2 == 0)
			{
				text += "Hendrerit fermentum blandit conubia fringilla class duis non neque blandit condimentum, maecenas neque vel justo magna ornare tempor purus quis porttitor quisque, et nullam turpis ullamcorper a donec consequat auctor ornare.";

			}
			else
			{
				text += "Lorem ipsum pretium class porta gravida lobortis ipsum, etiam posuere elit torquent.";
			}

			int lines = Convert.numberOf(Convert.insertLineBreak(parentWidth - 10, f.fm, text),'\n')+1;
			height = (f.fm.height * lines) + Edit.prefH;
			return height;
		}
		else
			return height;
	}

	public void initUI()
	{
		Container ui = new Container();
		ui.setBackColor((id % 2 == 0) ? Color.darker(Color.YELLOW,32) : Color.darker(Color.GREEN,32));
		ui.setRect(0, yStart, parentWidth, height);
		try
		{
			Label l = new Label(Convert.insertLineBreak(parentWidth - 10, f.fm, text));
			ui.add(l, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			// MessageBox.showException(e, true);
		}
		this.c = ui;
	}
}
