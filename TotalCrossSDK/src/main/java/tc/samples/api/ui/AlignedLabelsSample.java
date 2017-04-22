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

import tc.samples.api.*;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class AlignedLabelsSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      setTitle("AlignedLabelsContainer");
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.setInsets(gap,gap,gap,gap);
      add(sc,LEFT,TOP,FILL,FILL);

      String[] labels =
      {
         "Name",
         "Born date",
         "Telephone",
         "Address",
         "City",
         "Country",
         "",
      };
      AlignedLabelsContainer c = new AlignedLabelsContainer(labels,4);
      c.setBorderStyle(BORDER_LOWERED);
      c.labelAlign = RIGHT;
      c.foreColors = new int[]{Color.RED,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,Color.BLACK,};
      c.setInsets(2,2,2,2);
      sc.add(c,LEFT+2,TOP+2,FILL-2,PREFERRED+4);
      int i;
      for (i =0; i < labels.length-2; i++)
         c.add(new Edit(),LEFT+2,c.getLineY(i));
      c.add(new ComboBox(new String[]{"Brazil","USA"}),LEFT+2,c.getLineY(i));
      c.add(new Button("Insert data"),RIGHT,SAME);
      c.add(new Button("Clear data"),RIGHT,AFTER+4,SAME,PREFERRED);
   }

}
