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

import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.ui.image.ImageException;

public class Level
{
   public final static int MAX_LEVELS = 6;

   public final static int COLUMNS = 6;
   public final static int ROWS = 6;

	private Tile tiles[][];
   public int tilesLeft;

	private static int[][] LAYOUTS =
	{
		{ // LEVEL 1
			0,0,1,1,0,0,
			0,5,3,3,5,0,
			6,3,4,4,3,6,
			2,3,4,4,3,2,
			0,1,3,3,1,0,
			0,0,1,1,0,0
		},
      { // LEVEL 2
			4,4,5,5,4,4,
			0,6,3,3,6,0,
			0,0,1,1,0,0,
			0,0,1,1,0,0,
			0,3,3,3,3,0,
			4,4,2,2,4,4
		},
      { // LEVEL 3
			0,4,6,6,4,0,
			1,5,0,0,5,1,
			1,0,3,3,0,1,
			1,0,3,3,0,1,
			1,2,0,0,2,1,
			0,4,4,4,4,0
		},
      { // LEVEL 4
			4,3,1,1,3,4,
			3,0,0,0,0,3,
			1,0,0,0,0,1,
			6,5,0,0,5,6,
			1,4,0,0,4,1,
			2,3,1,1,3,2
		},
      { // LEVEL 5
			4,2,0,0,2,4,
			2,5,0,0,5,2,
			0,1,1,1,1,0,
			0,3,3,3,3,0,
			6,4,0,0,4,6,
			4,2,0,0,2,4
		},
      { // LEVEL 6
			0,2,0,0,2,0,
			4,0,3,3,0,4,
			0,5,1,1,5,0,
			2,0,3,3,0,2,
			0,6,1,1,6,0,
			4,6,3,3,6,4
		}
	};

   private int gapX,gapY;
   private int incX,incY;
   private int offsetX,offsetY;

	public Level(int startLine) throws ImageException, IOException
	{
      tiles = new Tile[COLUMNS][ROWS];

      for(int r = 0; r < ROWS; r++)
      {
         for(int c = 0; c < COLUMNS; c++)
            tiles[r][c] = new Tile();
      }
      Tile tile0 = tiles[0][0];
      int tw = tile0.width();
      int th = tile0.height();

      gapX = (Settings.screenWidth - (tw * COLUMNS)) / (COLUMNS + 1);
      gapY = ((Settings.screenHeight / 2) - th * ROWS) / (ROWS + 1);
      incX = gapX + tw;
      incY = gapY + th;
      gapX += tw / 2;
      gapY += th / 2 + startLine;

      offsetX = gapX - (incX >> 1);
      offsetY = gapY - (incY >> 1);
	}

	public void set(int layout)
	{
      tilesLeft = 0;

      int []lay = LAYOUTS[layout - 1];
      for(int r = 0; r < ROWS; r++)
      {
         int rr = r * ROWS;
         int ry = r * incY;
         for(int c = 0; c < COLUMNS; c++)
         {
            int color = lay[rr + c];
            tiles[r][c].set(color, (c * incX) + gapX, (ry) + gapY);
     			if (color > 0) tilesLeft++;
         }
  		}
   }

   public boolean collide(Ball ball)
   {
      boolean dirty = false;
      int brickCellY = (ball.centerY - offsetY) / incY;
      int brickCellX = (ball.centerX - offsetX) / incX;

      if (brickCellY >= 0 && brickCellX >= 0 &&
          brickCellY < ROWS && brickCellX < COLUMNS)
      {
         Tile ctile = tiles[brickCellY][brickCellX];

         if (ctile.color > 0 && ctile.collide(ball))
         {
            if (ctile.hit(ball))
               tilesLeft--;
            dirty = true;
         }
      }
      return dirty;
   }

	public void show()
	{
      for (int r = ROWS-1; r >= 0; r--)
      {
         Tile row[] = tiles[r];
         for (int c = COLUMNS-1; c >= 0; c--)
         {
            Tile ctile = row[c];
   			if(ctile.color >= 0)
               ctile.show();
         }
		}
	}
}
