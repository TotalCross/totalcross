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

public class MultiButtonSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.borderColor = 0x00AA00;//headerBar.getBackColor();
      sc.setBorderStyle(BORDER_ROUNDED); // sample of the new rounded border
      sc.setInsets(gap, gap, gap, gap);

      sc.add(new Label("Normal"),LEFT,TOP+fmH/2);
      MultiButton b = new MultiButton(new String[]{"+","-"});
      b.setBackColor(Color.ORANGE);
      b.is3dText = true;
      sc.add(b,SAME,AFTER,PREFERRED,fmH*3/2);

      sc.add(new Label("Sticky, 3d text, center disabled"),LEFT,AFTER+fmH);
      b = new MultiButton(new String[]{"Left","Center","Right"});
      b.setBackColor(Color.GREEN);
      b.isSticky = b.is3dText = true;
      sc.add(b,SAME,AFTER,PREFERRED+fmH*2,fmH*2);
      
      b.setEnabled(1,false);

      add(sc, LEFT+fmH, TOP+fmH, FILL-fmH, FILL-fmH);
   }
}