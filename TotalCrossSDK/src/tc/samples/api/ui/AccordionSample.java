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

import totalcross.ui.*;

import tc.samples.api.*;

public class AccordionSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      AccordionContainer.Group gr = new AccordionContainer.Group();
      AccordionContainer ac = new AccordionContainer(gr);
      int gap = fmH/2;
      add(ac, LEFT+gap,TOP+gap,FILL-gap,PREFERRED);
      ac.add(ac.new Caption("Type text 1"), LEFT,TOP,FILL,PREFERRED);
      ac.add(new MultiEdit(),LEFT+gap,AFTER+gap,FILL-gap,fmH*7);

      ac = new AccordionContainer(gr);
      add(ac, LEFT+gap,AFTER+gap,FILL-gap,PREFERRED);
      ac.add(ac.new Caption("Type text 2"), LEFT,AFTER,FILL,PREFERRED);
      ac.add(new MultiEdit(),LEFT+gap,AFTER+gap,FILL-gap,fmH*7);

      ac = new AccordionContainer(gr);
      add(ac, LEFT+gap,AFTER+gap,FILL-gap,PREFERRED);
      ac.add(ac.new Caption("Type text 3"), LEFT,AFTER,FILL,PREFERRED);
      ac.add(new MultiEdit(),LEFT+gap,AFTER+gap,FILL-gap,fmH*7);
   }
}
