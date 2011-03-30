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



package totalcross.ui.image;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import totalcross.Launcher4B;
import totalcross.io.ByteArrayStream;
import totalcross.io.CRC32Stream;
import totalcross.io.DataStream;
import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.PDBFile;
import totalcross.io.ResizeRecord;
import totalcross.io.Stream;
import totalcross.sys.*;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.GfxSurface;
import totalcross.ui.gfx.Graphics4B;
import totalcross.util.zip.ZLib;

public class Image4B extends GfxSurface
{
   protected int width;
   protected int height;
   public int transparentColor = Color.WHITE; // default if the bitmap is monochromatic (WHITE color) - petrus@402_02
   Object pixels;
   private Bitmap[] frames;
   public int frameCount;
   public String comment;
   public boolean useAlpha; // guich@tc126_12
   private Graphics4B gfx;
   private int currentFrame = -1;
   private int[] tempRowBuf1;
   private int[] tempRowBuf2;
   private int maxWidth,maxHeight;

   public Image4B(int width, int height) throws ImageException
   {
      this.width = this.maxWidth = width;
      this.height = this.maxHeight = height;

      try
      {
         frames = new Bitmap[1];
         pixels = frames[0] = new Bitmap(width, height); // just create the bitmap
         frameCount = 1;
         currentFrame = 0;
      } catch (OutOfMemoryError oome) {throw new ImageException("Out of memory: cannot allocate "+width+"x"+height+" offscreen image.");}
      init();
   }

   public Image4B(String path) throws ImageException, IOException
   {
      File f = null;
      byte[] fullDescription = null;

      try
      {
         f = new File(path, File.READ_WRITE);
         fullDescription = Launcher4B.readStream(f);
         f.close();
      }
      catch (IOException ex)
      {
         if (f != null) // file was found, so we do not have to search in resources
         {
            try { f.close(); } catch (IOException ex2) { }
            throw ex;
         }

         fullDescription = Vm.getFile(path);
         if (fullDescription == null) // resource not found, search in the main window dir
         {
            path = Launcher4B.mainWindowPath + "/" + path;
            fullDescription = Vm.getFile(path);
            if (fullDescription == null) // resource not found, throw original exception
               throw ex;
         }
      }

      load(fullDescription);
      if (width == 0)
         throw new ImageException(fullDescription==null?"Description is null":("Error on bmp with "+fullDescription.length+" bytes length description"));
      init();
   }

   public Image4B(Stream s) throws ImageException, IOException
   {
      byte[] fullDescription = Launcher4B.readStream(s);
      load(fullDescription);
      if (width == 0)
         throw new ImageException(fullDescription==null?"Description is null":("Error on bmp with "+fullDescription.length+" bytes length description"));
      init();
   }

   public Image4B(byte[] fullDescription) throws ImageException
   {
      load(fullDescription);
      if (width == 0)
         throw new ImageException(fullDescription==null?"Description is null":("Error on bmp with "+fullDescription.length+" bytes length description"));
      init();
   }

   private Image4B(Image4B other) throws ImageException
   {
      useAlpha = other.useAlpha;
      transparentColor = other.transparentColor;
      frameCount = other.frameCount;
      frames = new Bitmap[frameCount];
   }

   private void load(byte[] data)
   {
      EncodedImage image = EncodedImage.createEncodedImage(data, 0, data.length);

      frameCount = image.getFrameCount();
      frames = new Bitmap[frameCount];
      for (int i = 0; i < frameCount; i++)
         frames[i] = image.getBitmap(i);
      maxWidth = image.getWidth();
      maxHeight = image.getHeight();
      useAlpha = image.hasTransparency();
      transparentColor = useAlpha ? -1 : Color.WHITE; // guich@tc120_65
      currentFrame = -1;
      setCurrentFrame(0);
   }

   private void init() throws ImageException
   {
      // frame count information?
      if (comment != null && comment.startsWith("FC="))
         try {setFrameCount(Convert.toInt(comment.substring(3)));} catch (InvalidNumberException ine) {}

      // init the Graphics
      gfx = new Graphics4B(this);
      gfx.refresh(0,0,width,height,0,0,null);
   }

   private int[] getRowBuf(boolean one)
   {
      if (one)
         return tempRowBuf1 != null ? tempRowBuf1 : (tempRowBuf1 = new int[maxWidth]);
      return tempRowBuf2 != null ? tempRowBuf2 : (tempRowBuf2 = new int[maxWidth]);
   }

   public Object getPixels()
   {
      return pixels;
   }

   public int getHeight()
   {
      return height;
   }

   public int getWidth()
   {
      return width;
   }

   public int getX()
   {
      return 0;
   }

   public int getY()
   {
      return 0;
   }

   public Graphics4B getGraphics()
   {
      gfx.setFont(MainWindow.getDefaultFont());
      return gfx;
   }

   public void setFrameCount(int n) throws ImageException
   {
      if (n > 1 && frameCount <= 1)
      {
         if ((width % n) != 0)
            throw new ImageException("The width must be a multiple of the frame count");

         try
         {
            Bitmap pixelsOfAllFrames = frames[0];
            frameCount = n;
            comment = "FC="+n;
            width /= frameCount;
            maxWidth = width;
            frames = new Bitmap[frameCount];
            for (int i = 0; i < frameCount; i++) // create all frames in memory to speed up setCurrentFrame
            {
               if (frames[i] == null || frames[i].getWidth() != width)
                  frames[i] = new Bitmap(width, height);
               net.rim.device.api.ui.Graphics g = new net.rim.device.api.ui.Graphics(frames[i]);
               g.drawBitmap(0, 0, width, height, pixelsOfAllFrames, i * width, 0);
            }
            setCurrentFrame(0);
         }
         catch (OutOfMemoryError oome) {throw new ImageException("Not enough memory to create the single frame");}
      }
   }

   public int getFrameCount()
   {
      return frameCount;
   }

   public int getCurrentFrame()
   {
      return currentFrame;
   }

   public final void setCurrentFrame(int nr)
   {
      if (nr < 0)
         nr = frameCount - 1;
      else if (nr >= frameCount)
         nr = 0;

      if (nr != currentFrame)
      {
         currentFrame = nr;
         Bitmap frame = frames[nr];

         pixels = frame;
         width = frame.getWidth();
         height = frame.getHeight();
         //transparentColor = frame.getTransColor();
      }
   }

   public void nextFrame()
   {
      if (frameCount > 1)
         setCurrentFrame(currentFrame + 1);
   }

   public void prevFrame()
   {
      if (frameCount > 1)
         setCurrentFrame(currentFrame - 1);
   }

   public final void changeColors(int from, int to)
   {
      int[] buff = getRowBuf(true);
      from = totalcross.ui.gfx.Graphics4B.getDeviceColor(from); // get the "real" color
      for (int i =0; i < frameCount; i++)
      {
         Bitmap in = frames[i];
         int w = in.getWidth();
         for (int y = in.getHeight(); --y >= 0;)
         {
            getRGB(in, buff, 0, w, 0, y, w, 1, useAlpha);
            if (useAlpha)
            {
               for (int x = w; --x >= 0;)
                  if ((buff[x] & 0xFFFFFF) == from)
                     buff[x] = (buff[x] & 0xFF000000) | to;
            }
            else
            {
               for (int x = w; --x >= 0;)
                  if (buff[x] == from)
                     buff[x] = to;
            }
            setRGB(in, buff, 0, w, 0, y, w, 1, useAlpha);
         }
      }
      if (frameCount != 1)
      {
         currentFrame = -1;
         setCurrentFrame(0);
      }
   }

   public Image4B getFadedInstance(int backColor) throws ImageException // guich@tc110_50
   {
      try
      {
         backColor = Graphics4B.getDeviceColor(backColor);
         Image4B newImage;
         newImage = new Image4B(this);
         newImage.width = newImage.maxWidth = this.maxWidth;
         newImage.height = newImage.maxHeight = this.maxHeight;
         int t = newImage.transparentColor = Graphics4B.getDeviceColor(newImage.transparentColor);
         int[] buff = getRowBuf(true);
         for (int j =0; j < frameCount; j++)
         {
            Bitmap src = frames[j];
            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap dst = newImage.frames[j] = new Bitmap(w, h);
            while (--h >= 0)
            {
               int i = w;
               src.getARGB(buff, 0, w, 0, h, w, 1);
               while (--i >= 0)
                  if (buff[i] != t)
                     buff[i] = (buff[i] & 0xFF000000) | Color.interpolate(backColor,buff[i]);
               dst.setARGB(buff, 0, w, 0, h, w, 1);
            }
         }
         newImage.pixels = newImage.frames[0];
         newImage.init();
         if (frameCount > 1)
            newImage.setCurrentFrame(0);
         return newImage;
      }
      catch (OutOfMemoryError oome)
      {
         throw new ImageException("Out of memory");
      }
      catch (Throwable t)
      {
         throw new ImageException(t.getMessage());
      }
   }

   public void saveTo(PDBFile cat, String name) throws ImageException, IOException
   {
      name = name.toLowerCase();
      if (!name.endsWith(".png"))
         name += ".png";
      int index = findPosition(cat, name, true);
      if (index == -1) 
         index = cat.getRecordCount();
      ResizeRecord rs = new ResizeRecord(cat,Math.min(65500, width*height*3+200)); // guich@tc114_17: make sure is not bigger than 64k
      DataStream ds = new DataStream(rs);
      rs.startRecord(index);
      ds.writeString(name); // write the name
      createPng(rs);
      rs.endRecord();
   }
   
   public static Image loadFrom(PDBFile cat, String name) throws IOException, ImageException
   {
      name = name.toLowerCase();
      if (!name.endsWith(".png"))
         name += ".png";
      int idx = findPosition(cat, name, false);
      if (idx == -1)
         throw new IOException("The image "+name+" is not inside "+cat.getName());
      
      cat.setRecordPos(idx);
      DataStream ds = new DataStream(cat);
      cat.skipBytes(ds.readUnsignedShort());
      Image img = new Image(cat);
      cat.setRecordPos(-1);
      return img;
   }
   
   private static int findPosition(PDBFile cat, String name, boolean isWrite) throws IOException
   {
      DataStream ds = new DataStream(cat);
      // guich@200b4_45: fixed the insert_in_order routine
      int n = cat.getRecordCount();
      for (int i =0; i < n; i++) // find the correct position to insert the record. the records must be sorted
      {
         cat.setRecordPos(i);
         String recName = ds.readString();
         if (recName.compareTo(name) >= 0) // is recName greater than name
         {
            if (isWrite && name.equals(recName)) // same name? delete it
               cat.deleteRecord();
            return i;
         }
      }
      return -1;
   }

   public void createPng(Stream s) throws ImageException, totalcross.io.IOException
   {
      try
      {
         // based in a code from J. David Eisenberg of PngEncoder, version 1.5
         byte[]  pngIdBytes = {(byte)-119, (byte)80, (byte)78, (byte)71, (byte)13, (byte)10, (byte)26, (byte)10};

         CRC32Stream crc = new CRC32Stream(s);
         DataStream ds = new DataStream(crc);

         int w = this.maxWidth * frameCount;
         int h = this.maxHeight;

         ds.writeBytes(pngIdBytes);
         // write the header
         ds.writeInt(13);
         crc.reset();
         ds.writeBytes("IHDR".getBytes());
         ds.writeInt(w);
         ds.writeInt(h);
         ds.writeByte(8); // bit depth of each rgb component
         ds.writeByte(2); // direct model
         ds.writeByte(0); // compression method
         ds.writeByte(0); // filter method
         ds.writeByte(0); // no interlace
         ds.writeInt((int)crc.getValue());

         // write transparent pixel information, if any
         if (transparentColor != -1) // transparency bit set?
         {
            ds.writeInt(6);
            crc.reset();
            ds.writeBytes("tRNS".getBytes());
            ds.writeShort((transparentColor >> 16) & 0xFF);
            ds.writeShort((transparentColor >> 8) & 0xFF);
            ds.writeShort((transparentColor ) & 0xFF);
            ds.writeInt((int)crc.getValue());
         }
         if (comment != null && comment.length() > 0)
         {
            ds.writeInt("Comment".length() + 1 + comment.length());
            crc.reset();
            ds.writeBytes("tEXt".getBytes());
            ds.writeBytes("Comment".getBytes());
            ds.writeByte(0);
            ds.writeBytes(comment.getBytes());
            ds.writeInt((int)crc.getValue());
         }

         // write the image data
         crc.reset();
         byte[] row = new byte[3*w];
         byte[] filterType = new byte[1];
         ByteArrayStream databas = new ByteArrayStream(3*w*h+h);

         for (int y = 0; y < h; y++)
         {
            getPixelRow(row, y);
            databas.writeBytes(filterType,0,1);
            databas.writeBytes(row,0,row.length);
         }
         databas.mark();
         ByteArrayStream compressed = new ByteArrayStream(w*h+h);
         int ncomp = ZLib.deflate(databas, compressed, 9);
         ds.writeInt(ncomp);
         crc.reset();
         ds.writeBytes("IDAT".getBytes());
         ds.writeBytes(compressed.getBuffer(), 0, ncomp);
         ds.writeInt((int)crc.getValue());

         // write the footer
         ds.writeInt(0);
         crc.reset();
         ds.writeBytes("IEND".getBytes());
         ds.writeInt((int)crc.getValue());
      }
      catch (OutOfMemoryError oome)
      {
         throw new ImageException(oome.getMessage()+"");
      }
   }

   public Image4B getScaledInstance(int newWidth, int newHeight) throws ImageException // guich@350_22
   {
      // Based on the ImageProcessor class on "KickAss Java Programming" (Tonny Espeset)
      try
      {
         Image4B scaledImage;
         scaledImage = new Image4B(this);
         int[] dstImageY = new int[newWidth];
         int[] srcImageY = new int[this.maxWidth];
         scaledImage.width  = scaledImage.maxWidth  = newWidth;
         scaledImage.height = scaledImage.maxHeight = newHeight;
         for (int i =0; i < frameCount; i++)
         {
            Bitmap src = frames[i];
            Bitmap dst = scaledImage.frames[i] = new Bitmap(newWidth, newHeight);
            int thisWidth = src.getWidth();
            int thisHeight = src.getHeight();
            // guich: a modified version of the replicate scale algorithm.
            int h = newHeight << 1;
            int hi = thisHeight << 1;
            int hf = thisHeight / h;
            int wf = 0;
            int w = newWidth << 1;
            int wi = thisWidth << 1;

            for (int y = 0; y < newHeight; y++, hf += hi)
            {
               wf = thisWidth / w;
               src.getARGB(srcImageY, 0, thisWidth, 0, hf/h, thisWidth, 1);
               for (int x=0; x < newWidth; x++,wf += wi)
                  dstImageY[x] = srcImageY[wf / w];
               dst.setARGB(dstImageY, 0, newWidth, 0, y, newWidth, 1);
            }
         }
         scaledImage.pixels = scaledImage.frames[0];
         scaledImage.init();
         if (frameCount > 1)
            scaledImage.setCurrentFrame(0);
         return scaledImage;
      }
      catch (OutOfMemoryError oome)
      {
         throw new ImageException("Out of memory");
      }
      catch (Throwable t)
      {
         throw new ImageException(t.getMessage());
      }
   }

   public Image4B getSmoothScaledInstance(int newWidth, int newHeight, int backColor) throws ImageException // guich@350_22
   {
      if (newWidth==width && newHeight==height) return this;
      try
      {
         Image4B scaledImage = new Image4B(this);
         scaledImage.width  = scaledImage.maxWidth  = newWidth;
         scaledImage.height = scaledImage.maxHeight = newHeight;
         int []pixelsThisLine = getRowBuf(true);
         int []pixelsNextLine = getRowBuf(false);
         int []newPixelsLine = scaledImage.getRowBuf(true);
         for (int i =0; i < frameCount; i++)
         {
            Bitmap src = frames[i];
            Bitmap dst = scaledImage.frames[i] = new Bitmap(newWidth, newHeight);
            int thisWidth = src.getWidth();
            int thisHeight = src.getHeight();

            // Based on the ImageProcessor class on "KickAss Java Programming" (Tonny Espeset)
            int width = thisWidth;
            int height = thisHeight;
            boolean shrinkW = newWidth < width;
            boolean shrinkH = newHeight < height;
            int srcCenterX = width << 8;
            int srcCenterY = height << 8;
            int dstCenterX = (shrinkW ? newWidth-1 : newWidth) << 8; // without -1, the imageScale buttons will miss the bottom/right pixels
            int dstCenterY = (shrinkH ? newHeight-1 : newHeight) << 8;
            int xScale = ((shrinkW ? newWidth-1 : newWidth)<<9)/(shrinkW ? width : width-1); // guich@tc112_28
            int yScale = ((shrinkH ? newHeight-1 : newHeight)<<9)/(shrinkH ? height : height-1);
            int xlimit = (width-1)<<9,  xlimit2 = (int)((width-1.001) * (1<<9));
            int ylimit = (height-1)<<9, ylimit2 = (int)((height-1.001) * (1<<9));
            int xs, ys;
            int xbase,ybase,offset,x,y;
            int xFraction,yFraction;
            int upperAverage, lowerAverage, p=0;
            int transp = Graphics4B.getDeviceColor(transparentColor);
            int lowerLeft,lowerRight,upperRight,upperLeft,r,g,b,a;
            dstCenterX += xScale>>1;
            dstCenterY += yScale>>1;

            for (y=0; y < newHeight; y++)
            {
               ys = (((((y<<9)-dstCenterY))<<9)/yScale) + srcCenterY;
               if (ys < 0) ys = 0; else
               if (ys >= ylimit) ys = ylimit2;

               ybase = (ys>>9)<<9;
               yFraction = ys - ybase;
               offset = ybase>>9;
               getRGB(src, pixelsThisLine, 0, thisWidth, 0, offset, thisWidth, 1, useAlpha); // this row
               getRGB(src, pixelsNextLine, 0, thisWidth, 0, offset + 1, thisWidth, 1, useAlpha); // upper row

               for (x=0,p=0; x < newWidth; x++)
               {
                  xs = (((((x<<9)-dstCenterX))<<9)/xScale) + srcCenterX;
                  if (xs < 0) xs = 0; else
                  if (xs >= xlimit) xs = xlimit2;

                  xbase = (xs>>9)<<9;
                  xFraction = xs - xbase;
                  offset = xbase >> 9;

                  lowerLeft  = pixelsThisLine[offset];      if (lowerLeft  == transp) lowerLeft  = backColor;
                  lowerRight = pixelsThisLine[offset + 1];  if (lowerRight == transp) lowerRight = backColor;
                  upperRight = pixelsNextLine[offset + 1];  if (upperRight == transp) upperRight = backColor;
                  upperLeft  = pixelsNextLine[offset];      if (upperLeft  == transp) upperLeft  = backColor;

                  upperAverage = (((upperLeft>>>24)&0xff)<<9)  + xFraction * (((upperRight>>>24)&0xff) - ((upperLeft>>>24)&0xff));
                  lowerAverage = (((lowerLeft>>>24)&0xff)<<9)  + xFraction * (((lowerRight>>>24)&0xff) - ((lowerLeft>>>24)&0xff));
                  a = ((lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9);
                  upperAverage = (((upperLeft>>16)&0xff)<<9)  + xFraction * (((upperRight>>16)&0xff) - ((upperLeft>>16)&0xff));
                  lowerAverage = (((lowerLeft>>16)&0xff)<<9)  + xFraction * (((lowerRight>>16)&0xff) - ((lowerLeft>>16)&0xff));
                  r = ((lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9);
                  upperAverage = (((upperLeft>>8)&0xff)<<9)  + xFraction * (((upperRight>>8)&0xff) - ((upperLeft>>8)&0xff));
                  lowerAverage = (((lowerLeft>>8)&0xff)<<9)  + xFraction * (((lowerRight>>8)&0xff) - ((lowerLeft>>8)&0xff));
                  g = ((lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9);
                  upperAverage = ((upperLeft&0xff)<<9)  + xFraction * ((upperRight&0xff) - (upperLeft&0xff));
                  lowerAverage = ((lowerLeft&0xff)<<9)  + xFraction * ((lowerRight&0xff) - (lowerLeft&0xff));
                  b = ((lowerAverage + ((yFraction * (upperAverage - lowerAverage)) >> 9)) >> 9);
                  newPixelsLine[p++] = ((a&0xff) << 24) | ((r&0xff)<<16) | ((g&0xff)<<8) | b&0xff;
               }
               setRGB(dst, newPixelsLine, 0, newWidth, 0, y, newWidth, 1, useAlpha);
            }
         }
         scaledImage.pixels = scaledImage.frames[0];
         scaledImage.init();
         if (frameCount > 1)
            scaledImage.setCurrentFrame(0);
         return scaledImage;
      }
      catch (OutOfMemoryError oome)
      {
         throw new ImageException("Out of memory");
      }
      catch (Throwable t)
      {
         throw new ImageException(t.getMessage());
      }
   }

   public Image4B scaledBy(double scaleX, double scaleY) throws ImageException  // guich@402_6
   {
      return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0)?this:getScaledInstance((int)(maxWidth*scaleX), (int)(maxHeight*scaleY)); // guich@400_23: now test if the width/height are the same, what returns the original image
   }

   public Image4B smoothScaledBy(double scaleX, double scaleY, int backColor) throws ImageException  // guich@402_6
   {
      return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0)?this:getSmoothScaledInstance((int)(maxWidth*scaleX), (int)(maxHeight*scaleY), backColor); // guich@400_23: now test if the width/height are the same, what returns the original image
   }

   public Image4B getRotatedScaledInstance(int scale, int angle, int fillColor) throws ImageException
   {
      if (scale <= 0) scale = 1;
      if (fillColor < 0 && transparentColor < 0)
         fillColor = Color.WHITE;
      return imageGetRotatedScaledInstance(scale, angle, fillColor);
   }

   public Image4B getTouchedUpInstance(byte brightness, byte contrast) throws ImageException
   {
      final int NO_TOUCHUP = 0;
      final int BRITE_TOUCHUP = 1;
      final int CONTRAST_TOUCHUP = 2;
      int touchup = NO_TOUCHUP;
      short table[] = null;
      int m = 0, k = 0;

      if (contrast != 0)
      {
         touchup |= CONTRAST_TOUCHUP;
         table = computeContrastTable(contrast);
      }
      if (brightness != 0)
      {
         touchup |= BRITE_TOUCHUP;
         double eBrightness = (brightness + 128.0) / 128.0; // [0.0 ... 2.0]
         if (brightness <= 1.0)
         {
            m = (int) (Math.sqrt(eBrightness) * 0x10000);
            k = 0;
         }
         else
         {
            double f = eBrightness - 1.0;
            f = f * f;
            k = (int) (f * 0xFF0000);
            m = (int) ((1.0 - f) * eBrightness * 0x10000);
         }
      }

      try
      {
         Image4B newImage;
         newImage = new Image4B(this);
         newImage.width = newImage.maxWidth = this.maxWidth;
         newImage.height = newImage.maxHeight = this.maxHeight;
         int[] buff = getRowBuf(true);
         for (int j =0; j < frameCount; j++)
         {
            Bitmap src = frames[j];
            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap dst = newImage.frames[j] = new Bitmap(w, h);
            while (--h >= 0)
            {
               int i = w;
               src.getARGB(buff, 0, w, 0, h, w, 1);

               switch (touchup)
               {
                  case NO_TOUCHUP:
                     break;
                  case BRITE_TOUCHUP:
                     while (--i >= 0)
                     {
                        int p = buff[i];
                        int r = (p >> 16) & 0xFF;
                        int g = (p >> 8) & 0xFF;
                        int b = p & 0xFF;
                        buff[i] = (buff[i] & 0xFF000000) | Color.getRGBEnsureRange(((m * r) + k) >> 16, ((m * g) + k) >> 16, ((m * b) + k) >> 16);
                     }
                     newImage.transparentColor = Color.getRGBEnsureRange(((m * Color.getRed(transparentColor)) + k) >> 16, ((m * Color.getGreen(transparentColor)) + k) >> 16, ((m * Color.getBlue(transparentColor)) + k) >> 16); // guich@tc100b5_41: use the same algorithm for the transparent color
                     break;
                  case CONTRAST_TOUCHUP:
                     while (--i >= 0)
                     {
                        int p = buff[i];
                        int r = (p >> 16) & 0xFF;
                        int g = (p >> 8) & 0xFF;
                        int b = p & 0xFF;
                        buff[i] = (buff[i] & 0xFF000000) | Color.getRGBEnsureRange(table[r], table[g], table[b] & 0xFF);
                     }
                     newImage.transparentColor = Color.getRGBEnsureRange(table[Color.getRed(transparentColor)], table[Color.getGreen(transparentColor)], table[Color.getBlue(transparentColor)] & 0xFF); // guich@tc100b5_41
                     break;
                  default: // case CTRSTBRITE_TOUCHUP:
                     while (--i >= 0)
                     {
                        int p = buff[i];
                        int r = table[(p >> 16) & 0xFF];
                        int g = table[(p >> 8) & 0xFF];
                        int b = table[p & 0xFF];
                        buff[i] = (buff[i] & 0xFF000000) | Color.getRGBEnsureRange(((m * r) + k) >> 16, ((m * g) + k) >> 16, ((m * b) + k) >> 16);
                     }
                     newImage.transparentColor = Color.getRGBEnsureRange(((m * Color.getRed(transparentColor)) + k) >> 16, ((m * Color.getGreen(transparentColor)) + k) >> 16, ((m * Color.getBlue(transparentColor)) + k) >> 16); // guich@tc100b5_41
                     break;
               }
               dst.setARGB(buff, 0, w, 0, h, w, 1);
            }
         }
         newImage.transparentColor = useAlpha ? -1 : Graphics4B.getDeviceColor(newImage.transparentColor);
         newImage.pixels = newImage.frames[0];
         newImage.init();
         if (frameCount > 1)
            newImage.setCurrentFrame(0);
         return newImage;
      }
      catch (OutOfMemoryError oome)
      {
         throw new ImageException("Out of memory");
      }
      catch (Throwable t)
      {
         throw new ImageException(t.getMessage());
      }
   }

   ////////////////////////////////////////////////////////////////////////////////////
   //                                 totalcross.ui.image.Image                      //
   ////////////////////////////////////////////////////////////////////////////////////
   Image4B imageGetRotatedScaledInstance(int scale, int angle, int fillColor) throws ImageException
   {
      int backColor = (fillColor < 0) ? this.transparentColor : fillColor;

      /* xplying by 0x10000 allow integer math, while not loosing much prec. */

      int rawSine;
      int rawCosine;
      int sine;
      int cosine;

      angle = angle % 360;
      if ((angle % 90) == 0)
      {
         if (angle < 0)
            angle += 360;
         switch (angle)
         {
            case 0:
               rawSine = sine = 0;
               rawCosine = 0x10000;
               cosine = 0x640000 / scale;
               break;
            case 90:
               rawSine = 0x10000;
               rawCosine = cosine = 0;
               sine = 0x640000 / scale;
               break;
            case 180:
               rawSine = sine = 0;
               rawCosine = -0x10000;
               cosine = -0x640000 / scale;
               break;
            default: // case 270:
               rawSine = -0x10000;
               rawCosine = cosine = 0;
               sine = -0x640000 / scale;
               break;
            }
      }
      else
      {
         double rad = angle * 0.0174532925;
         rawSine = (int) (Math.sin(rad) * 0x10000);
         rawCosine = (int) (Math.cos(rad) * 0x10000);
         sine = (rawSine * 100) / scale;
         cosine = (rawCosine * 100) / scale;
      }

      int hIn = this.maxHeight;
      int wIn = this.maxWidth;

      /* create imageOut */
      int cornersX[] = new int[3];
      int cornersY[] = new int[3];
      int xMin = 0;
      int yMin = 0;
      int xMax = 0;
      int yMax = 0;
      cornersX[0] = (wIn * rawCosine) >> 16;
      cornersY[0] = (wIn * rawSine) >> 16;
      cornersX[2] = (-hIn * rawSine) >> 16;
      cornersY[2] = (hIn * rawCosine) >> 16;
      cornersX[1] = cornersX[0] + cornersX[2];
      cornersY[1] = cornersY[0] + cornersY[2];

      for (int i = 2; i >= 0; i--)
      {
         if (cornersX[i] < xMin)
            xMin = cornersX[i];
         else if (cornersX[i] > xMax)
            xMax = cornersX[i];

         if (cornersY[i] < yMin)
            yMin = cornersY[i];
         else if (cornersY[i] > yMax)
            yMax = cornersY[i];
      }
      if (width == height)
      {
         xMax = yMax = width;
         xMin = yMin = 0;
      }
      int wOut = ((xMax - xMin) * scale) / 100;
      int hOut = ((yMax - yMin) * scale) / 100;

      try
      {
         Image4B scaledImage;
         scaledImage = new Image4B(this);
         scaledImage.maxWidth = scaledImage.width = wOut;
         scaledImage.maxHeight = scaledImage.height = hOut;
         int[][] pixelsIn = new int[this.maxHeight][this.maxWidth];
         int[] lineOut = scaledImage.getRowBuf(true);
         for (int j =0; j < frameCount; j++)
         {
            Bitmap src = frames[j];
            Bitmap dst = scaledImage.frames[j] = new Bitmap(wOut, hOut);
            wIn = src.getWidth();
            hIn = src.getHeight();
            for (int i = 0; i < hIn; i ++)
               src.getARGB(pixelsIn[i], 0, wIn, 0, i, wIn, 1);
            int x0 = ((wIn << 16) - (((xMax - xMin) * rawCosine) - ((yMax - yMin) * rawSine)) - 1) / 2;
            int y0 = ((hIn << 16) - (((xMax - xMin) * rawSine) + ((yMax - yMin) * rawCosine)) - 1) / 2;
            for (int l = 0; l < hOut; l++)
            {
               int x = x0;
               int y = y0;
               int iOut = 0;
               for (int i = wOut; --i >= 0; x += cosine, y += sine)
               {
                  int u = x >> 16;
                  int v = y >> 16;
                  if (0 <= u && u < wIn && 0 <= v && v < hIn)
                     lineOut[iOut++] = pixelsIn[v][u];
                  else
                     lineOut[iOut++] = backColor;
               }
               x0 -= sine;
               y0 += cosine;

               dst.setARGB(lineOut, 0, wOut, 0, l, wOut, 1);
            }
         }
         scaledImage.transparentColor = transparentColor;
         scaledImage.useAlpha = useAlpha;
         scaledImage.pixels = scaledImage.frames[0];
         scaledImage.init();
         if (frameCount > 1)
            scaledImage.setCurrentFrame(0);
         return scaledImage;
      }
      catch (OutOfMemoryError oome)
      {
         throw new ImageException("Out of memory");
      }
      catch (Throwable t)
      {
         throw new ImageException(t.getMessage());
      }
   }

   private short[] computeContrastTable(byte level)
   {
      double factor;
      short[] table = new short[256];
      if (level < 0) // byte ranges -128 to +127
         factor = (level+128) / 128.0;
      else
         factor = 127.0 / Math.max(127 - level,1);
      for (int i = 0; i <= 127; i++)
      {
         int v = ((int) (127.0 * Math.pow(i / 127.0, factor))) & 0xff;
         table[i] = (short)v;
         table[255 - i] = (short) (255 - v);
      }
      return table;
   }

   protected final void getPixelRow(byte []fillIn, int y)
   {
      int[] buff = getRowBuf(true);
      int x = 0, i;
      int ww = maxWidth;
      byte pr = (byte)((transparentColor >> 16) & 0xFF);
      byte pg = (byte)((transparentColor >> 8) & 0xFF);
      byte pb = (byte)(transparentColor & 0xFF);
      for (int j = 0; j < frameCount; j++)
      {
         Bitmap in = frames[j];
         int w = in.getWidth();
         getRGB(in, buff, 0, w, 0, y, w, 1, useAlpha);

         for (i = 0; i < w;)
         {
            int p = buff[i++];
            fillIn[x++] = (byte)((p >> 16) & 0xFF); // r
            fillIn[x++] = (byte)((p >> 8) & 0xFF); // g
            fillIn[x++] = (byte)(p & 0xFF); // b
         }
         for (; i < ww; i++) // align the frames
         {
            fillIn[x++] = pr; // r
            fillIn[x++] = pg; // g
            fillIn[x++] = pb; // b
         }
      }
   }

   private static void setRGB(Bitmap b, int[] buff, int off, int scanLength, int x, int y, int w, int h, boolean useAlpha)
   {
      // Append transparency information
      int n = w * h;
      if (!useAlpha)
         for (int i = off; --n >= 0; i ++)
            buff[i] |= 0xFF000000;

      int imgW = b.getWidth();
      if (x + w > imgW)
         w = imgW - x;

      b.setARGB(buff, off, scanLength, x, y, w, h);
   }

   private static void getRGB(Bitmap b, int[] buff, int off, int scanLength, int x, int y, int w, int h, boolean useAlpha)
   {
      int imgW = b.getWidth();
      if (x + w > imgW)
         w = imgW - x;

      b.getARGB(buff, off, scanLength, x, y, w, h);

      // Discard transparency information
      int n = w * h;
      if (!useAlpha)
         for (int i = off; --n >= 0; i ++)
            buff[i] &= 0x00FFFFFF;
   }

   public static boolean isSupported(String filename)
   {
      if (filename == null) return false;
      filename = filename.toLowerCase();
      return filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png");
   }

   final public Image4B getFrameInstance(int frame) throws ImageException
   {
      Image4B img = new Image4B(width,height);
      setCurrentFrame(frame);
      img.gfx.drawImage(this,0,0);      
      img.transparentColor = this.transparentColor;
      img.useAlpha = this.useAlpha;
      return img;
   }

   final public void applyColor(int color) // guich@tc112_24
   {
      int r2 = Color.getRed(color);
      int g2 = Color.getGreen(color);
      int b2 = Color.getBlue(color);
      double k = 128;
      int mr,mg,mb;
      mr = (int) (Math.sqrt((r2 + k) / k) * 0x10000);
      mg = (int) (Math.sqrt((g2 + k) / k) * 0x10000);
      mb = (int) (Math.sqrt((b2 + k) / k) * 0x10000);
      
      int[] buff = getRowBuf(true);
      for (int i =0; i < frameCount; i++)
      {
         Bitmap in = frames[i];
         int w = in.getWidth(),p;
         for (int y = in.getHeight(); --y >= 0;)
         {
            getRGB(in, buff, 0, w, 0, y, w, 1, useAlpha);
            for (int x = w; --x >= 0;)
               if ((p = buff[x]) != transparentColor)
               {
                  int r = (p >> 16) & 0xFF;
                  int g = (p >> 8) & 0xFF;
                  int b = p & 0xFF;
                  r = (mr * r) >> 16;
                  g = (mg * g) >> 16;
                  b = (mb * b) >> 16;
                  if (r > 255) r = 255;
                  if (g > 255) g = 255;
                  if (b > 255) b = 255;
                  buff[x] = (buff[x] & 0xFF000000) | (r<<16) | (g<<8) | b;
               }
            setRGB(in, buff, 0, w, 0, y, w, 1, useAlpha);
         }
      }
      if (frameCount != 1)
      {
         currentFrame = -1;
         setCurrentFrame(0);
      }
   }
   
   final public Image4B smoothScaledFromResolution(int originalRes, int backColor) throws ImageException // guich@tc112_23
   {
      int k = Math.min(Settings.screenWidth,Settings.screenHeight);
      return getSmoothScaledInstance(width*k/originalRes, height*k/originalRes, backColor);
   }
}
