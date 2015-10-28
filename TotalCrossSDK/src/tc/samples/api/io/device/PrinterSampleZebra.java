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

package tc.samples.api.io.device;

import totalcross.io.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class PrinterSampleZebra extends PrinterSampleBase
{
   public class MonoPCXImage extends Image
   {
      /** Creates an Image with the given width and height in pixels. */
      public MonoPCXImage(int w, int h) throws ImageException
      {
         super(w,h);
      }
      
      /** Saves a PCX in inverse/swapped mode to the given Stream. */
      public void createPCX(Stream stream) throws IOException
      {
         int bytesPerLine = (width+7)/8;
         int xEnd = width-1;
         int yEnd = height-1;

         byte[] header = 
         {
            0x0A,           // "PCX File"
            0x05,           // "Version 5"
            0x01,           // RLE Encoding
            0x01,           // 1 bit per pixel
            0x00, 0x00,     // XStart at 0
            0x00, 0x00,     // YStart at 0
            (byte)(xEnd&0xFF), (byte)((xEnd>>8) & 0xFF),      // Xend
            (byte)(yEnd&0xFF), (byte)((yEnd>>8) & 0xFF),      // Yend
            (byte)(xEnd&0xFF), (byte)((xEnd>>8) & 0xFF),      // Xend
            (byte)(yEnd&0xFF), (byte)((yEnd>>8) & 0xFF),      // Yend
            0x0F, 0x0F, 0x0F, 0x0E, 0x0E, 0x0E, 0x0D, 0x0D, 0x0D, 0x0C, 0x0C, 0x0C,   //48-byte EGA palette info
            0x0B, 0x0B, 0x0B, 0x0A, 0x0A, 0x0A, 0x09, 0x09, 0x09, 0x08, 0x08, 0x08,  
            0x07, 0x07, 0x07, 0x06, 0x06, 0x06, 0x05, 0x05, 0x05, 0x04, 0x04, 0x04,  
            0x03, 0x03, 0x03, 0x02, 0x02, 0x02, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00,  
            0x00,          // Reserved byte, always x00
            0x01,          // 1 bit plane
            (byte)(bytesPerLine&0xFF), (byte)((bytesPerLine>>8) & 0xFF),      // Bytes per scan line: (XEnd - XStart,  1) / 8
            0x01, 0x00,    // Palette type: 1 means color or monochrome
            0x00, 0x00,    // Horizontal screen size (not used)
            0x00, 0x00     // Vertical screen size (not used)
         };     
         stream.writeBytes(header);      // Write most of header data.
         stream.writeBytes(new byte[54]); // pad the 128-byte header

         byte rowIn[] = new byte[width*4];
         byte rowInv[] = new byte[width];
         int []bits = {128, 64, 32, 16, 8, 4, 2, 1};
         
         byte[] bytes = new byte[2];
         int last = 0;
         int count = 0;
         // note: the printer prints swapped
         // for (int y = 0; y < height; y++)
         for (int y = 0; y < height; y++)
         {
            getPixelRow(rowIn,y);
            for (int x =0; x < width; x++)
               rowInv[x] = rowIn[x*4];
            
            for (int x=0; x < width; x+=8) 
            {
               int n = x+8; 
               if (n > width) n = width;
               int b = 0;
               for (int j=x; j < n; j++)
                  //if (rowIn[j+j+j] != 0)
                  if (rowInv[j] != 0)
                     b |= bits[j-x];
               if (last==b && count < 63) 
                  count++;
               else 
               {
                  if (count > 0) 
                  {
                     bytes[0] = (byte)(count | 0xC0);
                     bytes[1] = (byte)last;
                     stream.writeBytes(bytes,0,2);
                  }
                  last = b;
                  count = 1;
               }
            }
            if (count > 0) 
            {
               bytes[0] = (byte)(count | 0xC0);
               bytes[1] = (byte)last;
               stream.writeBytes(bytes,0,2);               
               count = 0;
               last = 0;
            }
         }
      }
   }

   private static ByteArrayStream pcxBAS = new ByteArrayStream(65000);

   protected void printSample(Stream s) throws Exception
   {
      int w = 500, h = 800;
      MonoPCXImage pcx = new MonoPCXImage(w,h);
      
      prepareImage(pcx);
      
      pcxBAS.reset();
      pcx.createPCX(pcxBAS);
      int pcxLen = pcxBAS.getPos();

      ByteArrayStream bas = new ByteArrayStream(pcxLen+100);
      bas.writeBytes("! 0 200 200 " + (h+50) + " 1\r\n");
      bas.writeBytes("PCX 0 0\r\n");
      bas.writeBytes(pcxBAS.getBuffer(), 0, pcxLen);
      bas.writeBytes("ENDPCX.LBL\r\n");
      bas.writeBytes("PRINT\r\n");
      s.writeBytes(bas.getBuffer(),0,bas.getPos());
   }

   private void prepareImage(Image img) throws Exception
   {
      int w = img.getWidth();
      int h = img.getHeight();
      int y = 10;
      Graphics g = img.getGraphics();

      // white paper, white background
      g.backColor = Color.WHITE;
      g.fillRect(0,0,w,h);
      // black pen
      g.foreColor = Color.BLACK;
      g.drawRect(0,0,w,h);
      
      // draw a sample image
      Image baby = new Image("tc/samples/api/io/device/barbara.png");
      g.drawImage(baby, (w-baby.getWidth())/2,y); y += baby.getHeight();
      
      // draw some text
      Font f = Font.getFont("arialnoaa",false,30);
      g.setFont(f);
      y += f.fm.height;
      String text = "Bárbara Hazan";
      int tw = f.fm.stringWidth(text);
      g.drawText(text, (w-tw)/2, y);
   }
}
