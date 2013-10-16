package tc.samples.io.device.btprinter;

import totalcross.io.*;
import totalcross.ui.image.*;

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
            rowInv[x] = rowIn[x+x+x];
         
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
