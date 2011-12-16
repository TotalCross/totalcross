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



package tc.samples.lang.thread.bouncingbox;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

class Box extends Thread
{
   int[][] matrix =
   {
      {  1,-1,  -1, 1,   1, 1 },
      { -1,-1,   1, 1,  -1, 1 },
      null,
      {  1, 1,  -1,-1,   1,-1 },
      { -1, 1,   1,-1,  -1,-1 }
   };

   private static final int radius = 10;
   int top, left, right,bottom;
   int posX, posY;
   int deltaX;
   int deltaY;
   boolean vert;
   public boolean running = true;

   public Box(boolean low)
   {
      totalcross.util.Random rand = new totalcross.util.Random();
      left = 5;
      top  = 5;
      right  = totalcross.sys.Settings.screenWidth  - 5 - radius;
      bottom = totalcross.sys.Settings.screenHeight - 5 - radius;

      posX = rand.between(left, right);
      posY = rand.between(top, bottom);

      deltaX = rand.between(0,1) == 1 ? 1 : -1;
      deltaY = rand.between(0,1) == 1 ? 1 : -1;
      this.vert = !low;
      setPriority(low ? 1 : 8);
   }

   private void updatePos()
   {
      // calculate a direction of the ball as an integer in the range
      // -2 .. 2 (excluding 0)
      int direction = deltaX + deltaY;
      if (direction == 0) direction = deltaX + 2*deltaY;

      // is the current position colliding with any wall
      int collision = 0;
      if (posX <= left || posX >= right) collision++;
      if (posY <= top || posY >= bottom) collision += 2;

      // change the direction appropriately if there was a collision
      if (collision != 0)
      {
         collision = (collision - 1) * 2;

         deltaX = matrix[direction+2][collision];
         deltaY = matrix[direction+2][collision+1];
      }
      // calculate the new position and draw the ball there
      if (!vert)
         posX += deltaX;
      else
         posY += deltaY;
   }

   public void run()
   {
      MainWindow w = MainWindow.getMainWindow();
      int fore = w.getForeColor();
      int back = w.getBackColor();
      Graphics g = w.getGraphics();
      boolean first = true;
      while (running)
      {
         if (!first)
         {
            g.foreColor = back;
            g.drawRect(posX,posY,radius,radius); // remove the last one from screen
         }
         else
            first = false;

         updatePos();

         g.foreColor = fore;
         g.drawRect(posX,posY,radius,radius);
         totalcross.sys.Vm.sleep(10);
      }
   }
}
