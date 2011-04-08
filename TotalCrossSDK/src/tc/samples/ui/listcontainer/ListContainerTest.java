/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

package tc.samples.ui.listcontainer;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class ListContainerTest extends MainWindow
{
   public ListContainerTest()
   {
      super("ListContainer Test", VERTICAL_GRADIENT);
   }
   
   public void initUI()
   {
      ListContainer lc = new ListContainer();
      add(lc, LEFT,TOP,FILL,FILL);
      ListContainer.Layout layout = lc.getLayout(5,2);
      layout.boldItems[1] = true;
      layout.controlGap = 150;
      layout.defaultItemColors[1] = Color.RED;
      layout.lineGap = 25; // 1/4 font's height
      layout.relativeFontSizes[2] = layout.relativeFontSizes[3] = -1;
      layout.positions[3] = RIGHT;
      layout.positions[4] = CENTER;
      layout.setup();
      
      ListContainer.Item c = new ListContainer.Item(layout);
      c.items = new String[]{"00011","BAR LANCHONETE CONRADO","Rio de Janeiro/Centro","99999,99","Brasil"};
      lc.addContainer(c);
      
      c = new ListContainer.Item(layout);
      c.items = new String[]{"00015","BARITMOS RESTAURANTE","Rio de Janeiro/Copacabana","80000,00","Também Brasil"};
      lc.addContainer(c);
   }
}
