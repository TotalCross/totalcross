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
import totalcross.ui.font.*;
import totalcross.ui.image.*;

/**
 * The game animated logo.<br>
 */

public final class AnimLogo extends Animation
{
   public AnimLogo() throws ImageException, IOException
   {
      super(new Image("tc/samples/game/scape/hockey.png").getHwScaledInstance(Font.NORMAL_SIZE*4*8,Font.NORMAL_SIZE*4), 8, Font.NORMAL_SIZE*4);
   }
}
