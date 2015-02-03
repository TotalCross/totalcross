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



package totalcross.io.device.printer;

import totalcross.io.*;
import totalcross.ui.image.*;

/** Image class that can be used to save 1-bpp images and print on monochromatic printers.
 * Only black pixels are printed, non-black are ignored. 
 */
public class MonoImage extends Image
{
   /** Makes a copy of the given image. */
   public MonoImage(Image other) throws ImageException
   {
      super(other.getWidth(), other.getHeight());
      getGraphics().drawImage(other, 0,0);
   }
   
   /** Creates a MonoImage read from the given Stream. */
   public MonoImage(Stream s) throws ImageException, IOException
   {
      super(s);
   }
   
   /** Creates a MonoImage based on the given full description. */
   public MonoImage(byte[] fullDescription) throws ImageException
   {
      super(fullDescription);
   }

   /** Creates a MonoImage with the given width and height.
    * You can draw into it by retrieving the Graphics using img.getGraphics(). 
    */
   public MonoImage(int width, int height) throws ImageException
   {
      super(width, height);
   }

   /** Creates a MonoImage, loading the given bmp file. */
   public MonoImage(String path) throws ImageException, IOException
   {
      super(path);
   }
   
   /** Creates a 1-bpp bitmap from this image. When writting, only black pixels are written. */ 
   void printTo(BluetoothPrinter pad, byte imageMode) throws IOException
   {
      // the image must be stored in vertical stripes
      boolean doubleDensity = (imageMode & 1) == 1;
      int rowW = Math.min(width, doubleDensity ? 384 : 192);
      int rowH = imageMode >= BluetoothPrinter.IMAGE_MODE_24_SINGLE ? 24 : 8;
      int bytes = rowH / 8;

      // data
      byte rowIn[] = new byte[width*4];
      byte rowOut[] = new byte[rowW * rowH / 8];
      byte []bits = 
      {
         (byte)128, (byte)64, (byte)32, (byte)16, (byte)8, (byte)4, (byte)2, (byte)1,
         (byte)128, (byte)64, (byte)32, (byte)16, (byte)8, (byte)4, (byte)2, (byte)1,
         (byte)128, (byte)64, (byte)32, (byte)16, (byte)8, (byte)4, (byte)2, (byte)1,
      };
      
      for (int y = 0,ry=0,h=height; y < h; y++,ry++) // rows are stored upside down.
      {
         if ((y % rowH) == 0)
         {
            if (y > 0) 
            {
               pad.write(rowOut);
               pad.newLine();
               for (int i = rowOut.length-1; i >= 0; i--)
                  rowOut[i] = 0;
            }
            pad.write(new byte[]{BluetoothPrinter.ESC, (byte)'*', imageMode, (byte)(rowW % 256), (byte)(rowW / 256)});
            ry = 0;
         }
            
         getPixelRow(rowIn,y);
         for (int x =0,ry8 = ry/8, i=0; x < rowW; x++,i+=3)
            if (rowIn[i] == 0)
               rowOut[x*bytes+ry8] |= bits[ry];
      }
      pad.write(rowOut);
      pad.newLine();
   }
}  
