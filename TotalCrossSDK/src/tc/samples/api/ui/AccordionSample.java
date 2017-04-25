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
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

import tc.samples.api.*;

public class AccordionSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      ScrollContainer sc = new ScrollContainer(false,true);
      add(sc,LEFT,TOP,FILL,FILL);
      // using default arrows
      AccordionContainer.Group gr = new AccordionContainer.Group();
      AccordionContainer ac = new AccordionContainer(gr);
      int gap = fmH/2;
      sc.add(ac, LEFT+gap,TOP,FILL-gap,PREFERRED);
      ac.add(ac.new Caption("Type text 1"), LEFT,TOP,FILL,PREFERRED);
      ac.add(new MultiEdit(),LEFT+gap,AFTER+gap,FILL-gap,FONTSIZE+700);

      // using +- from Awesome font
      ac = new AccordionContainer(gr);
      sc.add(ac, LEFT+gap,AFTER,FILL-gap,PREFERRED);
      ac.add(ac.new Caption(new Label("Type text 2"),setAwesome(new Button("\uf146",Button.BORDER_NONE),fmH), setAwesome(new Button("\uf0fe",Button.BORDER_NONE),fmH)), LEFT,AFTER,FILL,PREFERRED);
      ac.add(new MultiEdit(),LEFT+gap,AFTER+gap,FILL-gap,FONTSIZE+700);
      
      try
      {
         // using colorized images
         ac = new AccordionContainer(gr);
         sc.add(ac, LEFT+gap,AFTER,FILL-gap,PREFERRED);
         Image img = new Image("tc/samples/api/ui/images/bt_minus.png").smoothScaledFixedAspectRatio(fmH,true);
         img.applyColor2(Color.BLUE);
         Button b1 = new Button(img, Button.BORDER_NONE);
         img = new Image("tc/samples/api/ui/images/bt_add.png").smoothScaledFixedAspectRatio(fmH,true);
         img.applyColor2(Color.GREEN);
         Button b2 = new Button(img, Button.BORDER_NONE);
         ac.add(ac.new Caption(new Label("Type text 3"),b1,b2), LEFT,AFTER,FILL,PREFERRED);
         ac.add(new MultiEdit(),LEFT+gap,AFTER+gap,FILL-gap,FONTSIZE+700);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         // let sample work without this last one 
      }
   }
}
