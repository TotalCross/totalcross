/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

import totalcross.*;
import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;
import totalcross.util.zip.*;


/**
 * Image is a rectangular image.
 * <p>
 * You can draw into an image and copy an image to a surface using a Graphics
 * object. Images are always 24bpp, and TotalCross supports PNG and JPEG formats at device,
 * and PNG/JPEG/GIF/BMP at desktop (the last two are converted to png when deploying).
 * The deployed png may contain transparency information which is correctly handled.
 * <p>
 * This is a code sample of how to run a multi-framed image. Note that it will work
 * in the same way in desktop or in device.
 * <pre>
   Image img = new Image("alligator.gif");
   add(new ImageControl(img),LEFT,TOP,FILL,PREFERRED);
   for (int i = 0; i < 100; i++)
   {
      repaintNow();
      Vm.sleep(200);
      img.nextFrame();
   }
 * </pre>
 * Some transformation methods returns a new instance of this image and other apply to the current instance.
 * To preserve an image with a single frame, use <code>getFrameInstance(0)</code>.
 * 
 * Note: TotalCross does not support grayscale PNG with alpha-channel. Convert the image to true-color with
 * alpha-channel and it will work fine (the only backdraw is that the new image will be bigger).
 *
 * @see Graphics
 */
public class Image extends GfxSurface
{
   protected int width;
   protected int height;
   /** Sets the transparent pixel of this image. Used in many controls.
     * Note that you can set it to -1 to make the image use the background
     * color.
     * Note also that this value is not used by the system, but the controls
     * (E.g.: totalcross.ui.Button)
     * use the value stored here to set the background color when going to
     * draw a transparent image.
     * @see #NO_TRANSPARENT_COLOR
     */
   public int transparentColor = Color.WHITE;

   /** Contains the pixels of this image. */
   Object pixels;

   /** The number of frames of this image, if derived from a multi-frame gif. */
   private int frameCount=1;

   /** A textual description stored in the PNG image. */
   public String comment;

   private Graphics gfx;

   private Object pixelsOfAllFrames;
   private int currentFrame=-1, widthOfAllFrames;

   /** True if this image has an alpha-channel for transparency (higher 8 bits).
	 * Important: if useAlpha is true, the transparentPixel must be set to -1, otherwise, many
	 * Graphics methods will not work.
	 * Also, the only operation that works with alpha-channel is Graphics.DRAW_PAINT; all other
	 * drawing operations ignore the alpha channel.
	 * @since TotalCross 1.27
	 */
   public boolean useAlpha; // guich@tc126_12

   /** The value that will be in <code>transparentColor</code> when image has no transparent color.
    * @see #transparentColor
    * @since TotalCross 1.3
    */
   public static final int NO_TRANSPARENT_COLOR = -2; // guich@tc130: -1 is often used in PNG with alpha-channel (opaque-white color - 0xFFFFFF), so we use another less-used color as no transparent
   
   /**
   * Creates an image of the specified width and height. The image has
   * a color depth (number of bitplanes) and color map that matches the
   * default drawing surface.
   * Here is an example of use:
   * <pre>
   * Image img = new Image(100,100);
   * Graphics g = img.getGraphics();
   * g.backColor = Color.WHITE;
   * g.fillRect(25,25,50,50);
   * ...
   * Graphics screenG = getGraphics();
   * screenG.drawImage(img,CENTER,CENTER);
   * </pre>
   */
   public Image(int width, int height) throws ImageException
   {
      this.width = width;
      this.height = height;
      try
      {
         pixels = new int[height*width]; // just create the pixels array
      } catch (OutOfMemoryError oome) {throw new ImageException("Out of memory: cannot allocate "+width+"x"+height+" offscreen image.");}
      init();
   }

   /** Used only at desktop to get the image's pixels; <b>NOT AVAILABLE</b> at the device (will throw a NoSuchMethodError). */
   public Object getPixels()
   {
      return pixels;
   }

   /**
   * Loads and constructs an image from a file. The path given is the path to the
   * image file. The file must be in 2, 16, 256, 24bpp color compressed (RLE) or uncompressed BMP bitmap
   * format, or a PNG file, or a GIF file, or a JPEG file. If the image cannot be loaded, an ImageException will be thrown.
   * @throws totalcross.ui.image.ImageException When the file was not found.
   */
   public Image(String path) throws ImageException, IOException
   {
      imageLoad(path);
      if (width == 0)
         throw new ImageException("Could not load image, file not found: "+path);
      init();
   }

   /** Loads a BMP, JPEG, GIF or PNG image from a totalcross.io.Stream. Note that Gif and BMP are supported only at desktop.
    * Note that all the bytes of the given stream will be fetched, even those bytes that may follow this Image.
    * @throws totalcross.io.IOException */
   public Image(Stream s) throws ImageException, totalcross.io.IOException
   {
      ByteArrayStream bas = new ByteArrayStream(8192);
      byte[] buf = new byte[1024];
      while (true)
      {
         int n = s.readBytes(buf,0,buf.length);
         if (n <= 0)
            break;
         bas.writeBytes(buf, 0, n);
      }
      byte[] fullDescription = bas.toByteArray();
      imageParse(fullDescription);
      if (width == 0)
         throw new ImageException(fullDescription==null?"Description is null":("Error on bmp with "+fullDescription.length+" bytes length description"));
      init();
   }

   /** Parses an image from the given byte array. Note that the byte array must
     * specify the full JPEG/PNG image, with headers (Gif/Bmp are supported at desktop only).
     * Here is a code example: <pre>
     * // create the image and fill it with something
     * Image img = new Image(160,160);
     * Graphics g = img.getGraphics();
     * for (int i =0; i < 16; i++)
     * {
     *    g.backColor = Color.getRGB(10*i,10*i,10*i);
     *    g.fillRect(i*10,0,10,160);
     * }
     * // save the bmp in a byte stream
     * ByteArrayStream bas = new ByteArrayStream(4096);
     * DataStream ds = new DataStream(bas);
     * int totalBytesWritten = img.createBmp(ds);
     * // parse the saved bmp
     * Image im = new Image(bas.getBuffer()); // Caution! the buffer may be greater than totalBytesWritten, but when parsing theres no problem.
     * if (im.getWidth() > 0) // successfully parsed?
     * {
     *    getGraphics().drawImage(im,CENTER,CENTER);
     *    Vm.sleep(2000);
     * }
     * </pre>
     * @throws totalcross.ui.image.ImageException Thrown when something was wrong with the image.
     */
   public Image(byte []fullDescription) throws ImageException
   {
      imageParse(fullDescription);
      if (width == 0)
         throw new ImageException(fullDescription==null?"Description is null":("Error on image with "+fullDescription.length+" bytes length description"));
      init();
   }

   private void init() throws ImageException
   {
      // frame count information?
      if (comment != null && comment.startsWith("FC="))
         try {setFrameCount(Convert.toInt(comment.substring(3)));} catch (InvalidNumberException ine) {}
      // init the Graphics
      gfx = new Graphics(this);
      gfx.refresh(0,0,width,height,0,0,null);
   }

   /** Sets the frame count for this image. The width must be a multiple of the frame count.
    * After the frame count is set, it cannot be changed.
    * @since TotalCross 1.0
    */
   public void setFrameCount(int n) throws ImageException
   {
      if (n > 1 && frameCount <= 1)
         try
         {
            frameCount = n;
            comment = "FC="+n;
            if ((width % n) != 0) throw new ImageException("The width must be a multiple of the frame count. Current width: "+width+", frame count: "+n);
            widthOfAllFrames = width;
            width /= frameCount;
            // the pixels will hold the pixel of a single frame
            pixelsOfAllFrames = pixels;
            pixels = new int[width * height];
            setCurrentFrame(0);
         }
         catch (OutOfMemoryError oome) {throw new ImageException("Not enough memory to create the single frame");}
   }

   /** Returns the frame count of this image.
    * @since TotalCross 1.0
    */
   public int getFrameCount()
   {
      return frameCount;
   }

   /** Move the contents of the given frame to the currently visible pixels.
    * @since TotalCross 1.0
    */
   final public void setCurrentFrame(int nr)
   {
      if (frameCount <= 1 || nr == currentFrame) return;
      if (nr < 0)
         nr = frameCount-1;
      else
      if (nr >= frameCount)
         nr = 0;
      currentFrame = nr;
      for (int y = height-1; y >= 0; y--)
         Vm.arrayCopy(pixelsOfAllFrames, nr * width + y * widthOfAllFrames, pixels, y * width, width);
   }

   /** Returns the current frame in a multi-frame image.
    * @since TotalCross 1.0
    */
   public int getCurrentFrame()
   {
      return currentFrame;
   }

   /** Move to next frame in a multi-frame image.
    * @since TotalCross 1.0
    */
   public void nextFrame()
   {
      if (frameCount > 1)
         setCurrentFrame(currentFrame+1);
   }

   /** Move to the previous frame in a multi-frame image.
    * @since TotalCross 1.0
    */
   public void prevFrame()
   {
      if (frameCount > 1)
         setCurrentFrame(currentFrame-1);
   }

   /** Returns the height of the image. You can check if the image is ok comparing this with zero. */
   public int getHeight()
   {
      return height;
   }

   /** Returns the width of the image. You can check if the image is ok comparing this with zero. */
   public int getWidth()
   {
      return width;
   }

   /** Returns a new Graphics instance that can be used to drawing in this image. */
   public Graphics getGraphics()
   {
      if (Launcher.instance.mainWindow != null) gfx.setFont(MainWindow.getDefaultFont()); // avoid loading the font if running from tc.Deploy
      return gfx;
   }

   /** Changes all the pixels of the image from one color to the other.
   * Note that the current value of the transparent color is not changed.
   * When you load a mono bmp (consumes less memory), it is stored internally
   * as grayScale (or color).
   * Using this routine, you can change the colors to any other you want.
   */
   final public void changeColors(int from, int to)
   {
      int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
      if (useAlpha)
      {
         for (int n = pixels.length; --n >= 0;)
            if ((pixels[n] & 0xFFFFFF) == from) 
               pixels[n] = (pixels[n] & 0xFF000000) | to;
      }
      else
      {
         for (int n = pixels.length; --n >= 0;)
            if (pixels[n] == from) pixels[n] = to;
      }
      if (frameCount != 1) {currentFrame = 2; setCurrentFrame(0);}
   }
   
   /** Saves this image as a Windows .png file format to the given PDBFile.
     * <ul>
     * <li>The stored image size is limited to near 64Kb. Note that a stored image size has no 
     * relation to its size in pixels. For example, a 1300x1200 completely-white PNG file takes 7Kb 
     * of storage size but 6MB of RAM when loaded.
     * <li>The PDBFile can save multiple images, but the record must
     * be prefixed with the image's name and must be sorted.
     * <li>This method finds the exact place where to insert the png and puts it there.
     * <li>If you want to create a png to be transfered by a stream to serial or socket
     * then you must use the method createPng instead.
     * <li>If a record with this name already exists, it will be replaced.
     * <li>The name is always converted to lowercase and the method makes sure that
     * .png is appended to it.
     * <li>To get the list of images in a PDBFile, just do a readString at the beginning of
     * each record.
     * <li>To retrieve the image, use loadFrom method.
     * </ul>
     * <p>Here is a sample code: 
     * <pre>
     * // create the image and paint over it
     * Image img = new Image(100,100);
     * Graphics g = img.getGraphics();
     * g.backColor = Color.getRGB(100,150,200);
     * g.fillRect(25,25,50,50);
     * g.foreColor = Color.WHITE;
     * g.drawCircle(50,50,20);
     * // create the PDBFile to save the image. You must change CRTR to match your apps creator ID
     * String pdbName = "images.CRTR.TYPE";
     * PDBFile pdb = new PDBFile(pdbName, PDBFile.CREATE);
     * img.saveTo(pdb, "boxcircle.png");
     * pdb.close();
     * // load the previously created image
     * PDBFile pdb = new PDBFile(pdbName, PDBFile.READ_WRITE);
     * add(new ImageControl(Image.loadFrom(pdb,"boxcircle.png")),CENTER,CENTER);
     * pdb.close();
     * </pre>
     * Here's a code that lists the images in a PDB (saved using this method).
     * <pre>
     * public static String[] list(PDBFile cat) throws IOException
     * {
     *    DataStream ds = new DataStream(cat);
     *    int n = cat.getRecordCount();
     *    String[] names = new String[n];
     *    for (int i =0; i < n; i++)
     *    {
     *       cat.setRecordPos(i);
     *       names[i] = ds.readString();
     *    }
     *    return names;
     * }
     * </pre>
     * @see #createPng(totalcross.io.Stream)
     * @see #loadFrom(PDBFile, String)
     */
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
   
   /** Loads an image from a PDB file, if it was previously saved using saveTo method.
    * @see #saveTo(PDBFile, String)
    * @since TotalCross 1.22
    */
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

   /** Saves this image as a 24 BPP .png file format (if useAlpha is true, it saves as 32 BPP), 
     * to the given stream.
     * If you're sending the png through a stream but not saving to a PDBFile,
     * you can use this method. If you're going to save it to a PDBFile, then
     * you must use the saveTo method.
     * @throws totalcross.io.IOException
     * @see #saveTo(totalcross.io.PDBFile, java.lang.String)
     */
   public void createPng(Stream s) throws ImageException, totalcross.io.IOException
   {
      try
      {
         // based in a code from J. David Eisenberg of PngEncoder, version 1.5
         byte[]  pngIdBytes = {(byte)-119, (byte)80, (byte)78, (byte)71, (byte)13, (byte)10, (byte)26, (byte)10};

         CRC32Stream crc = new CRC32Stream(s);
         DataStream ds = new DataStream(crc);

         int w = frameCount > 1 ? this.widthOfAllFrames : this.width;
         int h = this.height;

         ds.writeBytes(pngIdBytes);
         // write the header
         ds.writeInt(13);
         crc.reset();
         ds.writeBytes("IHDR".getBytes());
         ds.writeInt(w);
         ds.writeInt(h);
         ds.writeByte(8); // bit depth of each rgb component
         ds.writeByte(useAlpha ? 6 : 2); // alpha or direct model
         ds.writeByte(0); // compression method
         ds.writeByte(0); // filter method
         ds.writeByte(0); // no interlace
         int c = (int)crc.getValue();
         ds.writeInt(c);

         // write transparent pixel information, if any
         if (transparentColor >= 0) // transparency bit set?
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
         int bytesPerPixel = useAlpha ? 4 : 3;
         byte[] row = new byte[bytesPerPixel * w];
         byte[] filterType = new byte[1];
         ByteArrayStream databas = new ByteArrayStream(bytesPerPixel * w * h + h);

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
         c = (int)crc.getValue();
         ds.writeInt(c);

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
   /** Used in saveTo method. Fills in the y row into the fillIn array.
     * there must be enough space for the full line be filled, with width*3 bytes 
     * (or width*4 bytes, if useAlpha is true). 
     * The alpha channel is NOT stripped off. */
   final protected void getPixelRow(byte []fillIn, int y)
   {
      int[] row = (int[]) (frameCount > 1 ? this.pixelsOfAllFrames : this.pixels);
      int w = frameCount > 1 ? this.widthOfAllFrames : this.width;
      for (int x=0,n=w,i=y*w; n-- > 0;)
      {
         int p = row[i++];
         fillIn[x++] = (byte)((p >> 16) & 0xFF); // r
         fillIn[x++] = (byte)((p >> 8) & 0xFF); // g
         fillIn[x++] = (byte)(p & 0xFF); // b
         if (useAlpha)
            fillIn[x++] = (byte)((p >>> 24) & 0xFF); // a
      }
   }

   /** Returns the scaled instance for this image. The algorithm used is
     * the replicate scale: not good quality, but fast.
     * @since SuperWaba 3.5
     */
   public Image getScaledInstance(int newWidth, int newHeight) throws ImageException // guich@350_22
   {
      // Based on the ImageProcessor class on "KickAss Java Programming" (Tonny Espeset)
      newWidth *= frameCount; // guich@tc100b5_40
      Image scaledImage;
      try
      {
         scaledImage = new Image(newWidth, newHeight);
      }
      catch (Throwable t)
      {
         throw new ImageException("Out of memory");
      }
      scaledImage.transparentColor = this.transparentColor;
      scaledImage.useAlpha = this.useAlpha;

      int[] dstImageData = (int[]) scaledImage.pixels;
      int[] srcImageData = (int[]) ((frameCount == 1) ? this.pixels : this.pixelsOfAllFrames); // guich@tc100b5_40

      int fw = frameCount == 1 ? this.width : this.widthOfAllFrames; // guich@tc100b5_40
      // guich: a modified version of the replicate scale algorithm.
      int h = newHeight << 1;
      int hi = this.height << 1;
      int hf = this.height / h;
      int wf = 0;
      int w = newWidth << 1;
      int wi = fw << 1;

      for (int y = 0; y < newHeight; y++, hf += hi)
      {
         wf = fw / w;
         int dstImage = y*newWidth;
         int srcImage = (hf / h) * fw;
         for (int x = newWidth; x > 0; x--, wf += wi)
            dstImageData[dstImage++] = srcImageData[srcImage + wf / w];
      }
      if (frameCount > 1) // guich@tc100b5_40
         scaledImage.setFrameCount(frameCount);

      return scaledImage;
   }

   private static final int BIAS_BITS = 16;
   private static final int BIAS = (1<<BIAS_BITS);
   
   /** Returns the scaled instance using the area averaging algorithm for this image.
    * The backColor replaces the transparent pixel of the current image to produce a smooth border.
    * Example: <pre>
    * Image img2 = img.getSmoothScaledInstance(200,200, getBackColor());
    * </pre>
    * In device and JavaSE it uses a Catmull-rom resampling, and in Blackberry it uses an area-average resampling.
    * The reason is that the Catmull-rom consumes more memory and is also slower than the area-average, although the 
    * final result is much better. 
    * @since TotalCross 1.0
    */
   public Image getSmoothScaledInstance(int newWidth, int newHeight, int backColor) throws ImageException // guich@350_22
   {
      // image preparation
      if (newWidth==width && newHeight==height) return this;
      newWidth *= frameCount;
      Image scaledImage;
      try
      {
         scaledImage = new Image(newWidth, newHeight);
      }
      catch (Throwable t)
      {
         throw new ImageException("Out of memory");
      }
      scaledImage.transparentColor = this.transparentColor;
      scaledImage.useAlpha = this.useAlpha;

      int width = this.width * frameCount;
      int height = this.height;
      int[] pixels = (int[]) (frameCount==1 ? this.pixels : this.pixelsOfAllFrames);
      int[] pixels2= (int[]) scaledImage.pixels;
      int transp = transparentColor & 0xFFFFFF;
      backColor &= 0xFFFFFF;

      // algorithm start
      
      int i, j, n;
      double xScale, yScale;
      int a,r,g,b;

      // Temporary values
      int val; 

      int []v_weight; // Weight contribution    [ow][MAX_CONTRIBS]
      int []v_pixel; // Pixel that contributes [ow][MAX_CONTRIBS]
      int []v_count; // How many contribution for the pixel [ow]
      int []v_wsum;   // Sum of weights [ow]
                                  
      int []tb;        // Temporary (intermediate buffer)
      
      double center;       // Center of current sampling 
      double weight;       // Current wight
      int left;            // Left of current sampling
      int right;           // Right of current sampling

      int p_weight;      // Temporary pointer
      int p_pixel;       // Temporary pointer

      int maxContribs,maxContribsXY;     // Almost-const: max number of contribution for current sampling
      double scaledRadius,scaledRadiusY; // Almost-const: scaled radius for downsampling operations
      double filterFactor; // Almost-const: filter factor for downsampling operations

      /* Aliasing buffers */
      
      xScale = ((double)newWidth / width);
      yScale = ((double)newHeight / height);

      if (xScale > 1.0)
      {
         /* Horizontal upsampling */
         filterFactor = 1;
         scaledRadius = 2;
      }
      else
      { 
         /* Horizontal downsampling */ 
         filterFactor = xScale;
         scaledRadius = 2 / xScale;
      }
      maxContribs = (int) (2 * scaledRadius  + 1);

      scaledRadiusY = yScale > 1.0 ? 2 : 2 / yScale;
      maxContribsXY = (int) (2 * Math.max(scaledRadiusY,scaledRadius) + 1);

      /* Pre-allocating all of the needed memory */
      int s = newWidth > newHeight ? newWidth : newHeight;
      try
      {
         tb       = new int[newWidth * height];
         v_weight = new int[s * maxContribsXY]; /* weights */
         v_pixel  = new int[s * maxContribsXY]; /* the contributing pixels */
         v_count  = new int[s]; /* how may contributions for the target pixel */
         v_wsum   = new int[s]; /* sum of the weights for the target pixel */
      }
      catch (Throwable t)
      {
         throw new ImageException("Out of memory");
      }
      
      /* Pre-calculate weights contribution for a row */
      for (i = 0; i < newWidth; i++)
      {
         p_weight = i * maxContribs;
         p_pixel  = i * maxContribs;

         v_count[i] = 0;
         v_wsum[i] =  0;

         center = ((double)i)/xScale;
         left = (int)(center + 0.5 - scaledRadius);
         right = (int)(left + 2 * scaledRadius);
         
         for (j = left; j <= right; j++)
         {
            if (j < 0 || j >= width) continue;
            // Catmull-rom resampling
            double cc = (center-j) * filterFactor;
            if (cc < 0.0) cc = - cc;
            if (cc <= 1.0) weight = 1.5f * cc * cc * cc - 2.5f * cc * cc + 1; else
            if (cc <= 2.0) weight = -0.5f * cc * cc * cc + 2.5f * cc * cc - 4 * cc + 2;
            else continue;
            if (weight == 0)
               continue;
            int iweight = (int)(weight * BIAS);

            n = v_count[i]; /* Since v_count[i] is our current index */
            v_pixel[p_pixel+n] = j;
            v_weight[p_weight+n] = iweight;
            v_wsum[i] += iweight;
            v_count[i]++; /* Increment contribution count */
         }
      }

      /* Filter horizontally from input to temporary buffer */
      for ( i = 0; i < newWidth; i++)
      {
         int count = v_count[i];
         int wsum = v_wsum[i];
         /* Here 'n' runs on the vertical coordinate */
         for (n = 0; n < height; n++)
         {
            /* i runs on the horizontal coordinate */
            p_weight = i * maxContribs;
            p_pixel  = i * maxContribs;

            val = a = r = g = b = 0;
            for (j=0; j < count; j++)
            {
               int iweight = v_weight[p_weight++];
               val = pixels[v_pixel[p_pixel++] + n * width]; /* Using val as temporary storage */
               /* Acting on color components */
               a += ((val>>24)&0xFF) * iweight;
               if ((val&0xFFFFFF) == transp)
               {
                  r += ((backColor>>16)&0xFF) * iweight;
                  g += ((backColor>> 8)&0xFF) * iweight;
                  b += ((backColor    )&0xFF) * iweight;
               }
               else
               {
                  r += ((val>>16)&0xFF) * iweight;
                  g += ((val>> 8)&0xFF) * iweight;
                  b += ((val    )&0xFF) * iweight;
               }
            }
            a /= wsum; if (a > 255) a = 255; else if (a < 0) a = 0;
            r /= wsum; if (r > 255) r = 255; else if (r < 0) r = 0;
            g /= wsum; if (g > 255) g = 255; else if (g < 0) g = 0;
            b /= wsum; if (b > 255) b = 255; else if (b < 0) b = 0;
            tb[i+n*newWidth] = (a<<24) | (r << 16) | (g << 8) | b; /* Temporary buffer */
         }
      }

      /* Going to vertical stuff */
      if ( yScale > 1.0)
      {
         filterFactor = 1;
         scaledRadius = 2;
      }
      else
      {
         filterFactor = yScale;
         scaledRadius = 2 / yScale;
      }
      maxContribs  = (int) (2 * scaledRadius  + 1);

      /* Pre-calculate filter contributions for a column */
      for (i = v_weight.length; --i >= 0;) 
         v_weight[i] = v_pixel[i] = 0;

      for (i = 0; i < newHeight; i++)
      {
         p_weight = i * maxContribs;
         p_pixel  = i * maxContribs;
         
         v_count[i] = 0;
         v_wsum[i] = 0;

         center = ((double) i) / yScale;
         left = (int) (center+0.5 - scaledRadius);
         right = (int)( left + 2 * scaledRadius);

         for (j = left; j <= right; j++)
         {
            if (j < 0 || j >= height) continue;
            // Catmull-rom resampling
            double cc = (center-j) * filterFactor;
            if (cc < 0.0) cc = -cc;
            if (cc <= 1.0) weight = 1.5f * cc * cc * cc - 2.5f * cc * cc + 1; else
            if (cc <= 2.0) weight = -0.5f * cc * cc * cc + 2.5f * cc * cc - 4 * cc + 2;
            else continue;
            if (weight == 0)
               continue;
            int iweight = (int)(weight * BIAS);

            n = v_count[i]; /* Our current index */
            v_pixel[p_pixel+n] = j;
            v_weight[p_weight+n] = iweight;
            v_wsum[i]+= iweight;
            v_count[i]++; /* Increment the contribution count */
         }
      }
      
      int idx = 0;

      /* Filter vertically from work to output */
      for (i = 0; i < newHeight; i++)
      {
         int count = v_count[i];
         int wsum = v_wsum[i];
         for (n = 0; n < newWidth; n++)
         {
            p_weight = i * maxContribs;
            p_pixel  = i * maxContribs;

            val = a = r = g = b = 0;
            for (j = 0; j < count; j++)
            {
               int iweight = v_weight[p_weight++];
               val = tb[ n + newWidth * v_pixel[p_pixel++]]; /* Using val as temporary storage */
               /* Acting on color components */
               a += ((val>>24)&0xFF) * iweight;
               r += ((val>>16)&0xFF) * iweight;
               g += ((val>> 8)&0xFF) * iweight;
               b += ((val    )&0xFF) * iweight;
            }
            if (wsum == 0) continue;
            a /= wsum; if (a > 255) a = 255; else if (a < 0) a = 0;
            r /= wsum; if (r > 255) r = 255; else if (r < 0) r = 0;
            g /= wsum; if (g > 255) g = 255; else if (g < 0) g = 0;
            b /= wsum; if (b > 255) b = 255; else if (b < 0) b = 0;
            pixels2[idx++] = (a<<24) | (r << 16) | (g << 8) | b;
         }
      }
      
      if (frameCount > 1) // guich@tc100b5_40
         scaledImage.setFrameCount(frameCount);

      return scaledImage;
   }

   /** Returns the scaled instance for this image, given the scale arguments. The algorithm used is
     * the replicate scale, not good quality, but fast. Given values must be &gt; 0.
     * @since SuperWaba 4.1
     */
   public Image scaledBy(double scaleX, double scaleY) throws ImageException  // guich@402_6
   {
      return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0)?this:getScaledInstance((int)(width*scaleX), (int)(height*scaleY)); // guich@400_23: now test if the width/height are the same, what returns the original image
   }

   /** Returns the scaled instance for this image, given the scale arguments and scale type. Given values must be &gt; 0.
    * The backColor replaces the transparent pixel of the current image to produce a smooth border.
    * Example: <pre>
    * Image img2 = img.smoothScaledBy(0.75,0.75, getBackColor());
    * </pre>
    * @since TotalCross 1.0
    */
   public Image smoothScaledBy(double scaleX, double scaleY, int backColor) throws ImageException  // guich@402_6
   {
      return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0)?this:getSmoothScaledInstance((int)(width*scaleX), (int)(height*scaleY), backColor); // guich@400_23: now test if the width/height are the same, what returns the original image
   }

   /** Creates a rotated and/or scaled version of this Image.
    * A new <code>Image</code> object is returned which will render
    * the image at the specified <code>scale</code> ratio and
    * rotation <code>angle</code>.&nbsp;
    * After rotation, the empty parts of the rectangular area of
    * the resulting image are filled with the <code>fill</code> color.
    * If <code>color</code> is <code><i>-1</i></code>, then
    * the fill color is the transparent color, or white if none.
    * <p>Notes
    * <ul>
    * <li> the new image will probably have a different size of this image.
    * <li> if you want just to scale, use the getScaledInstance or
    *   scaleBy (and the smooth ones) instead, because they are faster.
    * <li> If you need a smooth rotate and scale, scale it first with
    * getScaledInstance then rotate without scale (or vice-versa)
    * <li> In multiframe images, each image is rotated/scaled independently.
    * </ul>
    * @param scale a number greater than or equal to 0 stating the percentage
    * of scaling to be performed.&nbsp;
    * 100 is not scaling, 200 doubles the size, 50 shrinks the image by 2
    * @param angle the rotation angle, expressed in trigonometric degrees
    * @param fillColor the fill color; -1 indicates the transparent color of this image or
    * Color.WHITE if the transparentColor was not set.
    */
   public Image getRotatedScaledInstance(int scale, int angle, int fillColor) throws ImageException
   {
      if (scale <= 0) scale = 1;
      if (fillColor < 0 && transparentColor < 0)
         fillColor = Color.WHITE;
      int backColor = (fillColor < 0) ? this.transparentColor : fillColor;

      /* xplying by 0x10000 allow integer math, while not loosing much prec. */

      int rawSine=0;
      int rawCosine=0;
      int sine=0;
      int cosine=0;

      angle = angle % 360;
      if ((angle % 90) == 0)
      {
         if (angle < 0) angle += 360;
         switch (angle)
         {
            case 0:
               rawCosine = 0x10000;
               cosine = 0x640000 / scale;
               break;
            case 90:
               rawSine = 0x10000;
               sine = 0x640000 / scale;
               break;
            case 180:
               rawCosine = -0x10000;
               cosine = -0x640000 / scale;
               break;
            default: // case 270:
               rawSine = -0x10000;
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

      int hIn = this.height;
      int wIn = this.width;

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

      for (int i = 2; --i >= 0;)
      {
         if (cornersX[i] < xMin)
            xMin = cornersX[i];
         else if (cornersX[i] > xMax) xMax = cornersX[i];

         if (cornersY[i] < yMin)
            yMin = cornersY[i];
         else if (cornersY[i] > yMax) yMax = cornersY[i];
      }
      if (width == height)
      {
         xMax = yMax = width;
         xMin = yMin = 0;
      }
      int wOut = ((xMax - xMin) * scale) / 100;
      int hOut = ((yMax - yMin) * scale) / 100;
      
         
      Image imageOut;
      try
      {
         imageOut = new Image(wOut * frameCount, hOut);
         if (frameCount > 1) imageOut.setFrameCount(frameCount);
         imageOut.transparentColor = transparentColor;
         imageOut.useAlpha = useAlpha;
      }
      catch (Throwable t)
      {
         throw new ImageException("Out of memory");
      }

      for (int f = 0; f < frameCount; f++)
      {
         if (frameCount != 1)
         {
            setCurrentFrame(f);
            imageOut.setCurrentFrame(f);
         }
         int[] pixelsIn = (int[]) this.pixels;

         /* center */
         int x0 = ((wIn << 16) - (((xMax - xMin) * rawCosine) - ((yMax - yMin) * rawSine)) - 1) / 2;
         int y0 = ((hIn << 16) - (((xMax - xMin) * rawSine) + ((yMax - yMin) * rawCosine)) - 1) / 2;
         /* and draw! */
         int[] lineOut = (int[]) imageOut.pixels;
         for (int l = 0; l < hOut; l++)
         {
            int x = x0;
            int y = y0;
            int iOut = l * imageOut.width;
            for (int i = wOut; --i >= 0; x += cosine, y += sine)
            {
               int u = x >> 16;
               int v = y >> 16;
               if (0 <= u && u < wIn && 0 <= v && v < hIn)
                  lineOut[iOut++] = pixelsIn[v*this.width+u];
               else
                  lineOut[iOut++] = backColor;
            }
            x0 -= sine;
            y0 += cosine;
         }
         if (frameCount != 1)
            for (int y = imageOut.height-1; y >= 0; y--)
               Vm.arrayCopy(imageOut.pixels, y * imageOut.width, imageOut.pixelsOfAllFrames, f * imageOut.width + y * imageOut.widthOfAllFrames, imageOut.width);
      }
      if (frameCount != 1)
      {
         setCurrentFrame(0);
         imageOut.setCurrentFrame(0);
      }
      return imageOut; // success
   }

   /** Creates a faded instance of this image, interpolating all pixels with the given background color.
    * The pixels that match the transparent color will not be changed
    * @since TotalCross 1.01
    */
   public Image getFadedInstance(int backColor) throws ImageException // guich@tc110_50
   {
      Image imageOut = new Image(frameCount > 1 ? widthOfAllFrames : width, height);
      if (frameCount > 1) imageOut.setFrameCount(frameCount);
      imageOut.transparentColor = transparentColor;
      imageOut.useAlpha = useAlpha;

      int[] from = (int[])(frameCount > 1 ? pixelsOfAllFrames : pixels);
      int[] to = (int[])(frameCount > 1 ? imageOut.pixelsOfAllFrames : imageOut.pixels);
      int t = transparentColor;
      if (useAlpha)
         for (int i = from.length; --i >= 0;)
            to[i] = (from[i] & 0xFF000000) | Color.interpolate(backColor,from[i]); // keep the alpha channel unchanged
      else
         for (int i = from.length; --i >= 0;)
            to[i] = from[i] == t ? t : Color.interpolate(backColor,from[i]);
      if (frameCount != 1)
      {
         imageOut.currentFrame = -1;
         imageOut.setCurrentFrame(0);
      }
      return imageOut;
   }

   /** Creates a touched-up version of this Image with
    * the specified brightness and contrast.
    * A new <code>Image</code> object is returned which will render
    * the image at the specified <code>brigthness</code>and
    * the specified <code>contrast</code>.
    * @param brightness a number between -128 and 127 stating the desired
    * level of brightness.&nbsp;
    * 127 is the highest brightness level (white image),
    * -128 is no brightness (darkest image).
    * @param contrast a number between -128 and 127 stating the desired
    * level of contrast.&nbsp;
    * 127 is the highest contrast level,
    * -128 is no contrast.
    */
   public Image getTouchedUpInstance(byte brightness, byte contrast) throws totalcross.ui.image.ImageException
   {
      final int NO_TOUCHUP = 0;
      final int BRITE_TOUCHUP = 1;
      final int CONTRAST_TOUCHUP = 2;
      int touchup = NO_TOUCHUP;
      int[] pixelsIn = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
      int w = frameCount == 1 ? this.width : this.widthOfAllFrames;
      int h = this.height;

      Image imageOut;
      try
      {
         imageOut = new Image(w, h);
      }
      catch (Throwable t)
      {
         throw new ImageException("Out of memory");
      }

      imageOut.useAlpha = useAlpha;
      int[] pixelsOut = (int[]) imageOut.pixels;
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

      // no palette
      int in[] = pixelsIn;
      int out[] = pixelsOut;
      switch (touchup)
      {
         case NO_TOUCHUP:
            Vm.arrayCopy(in, 0, out, 0, w*h);
            imageOut.transparentColor = transparentColor;
            break;
         case BRITE_TOUCHUP:
            for (int i = w*h-1; i >= 0; i--)
            {
               int p = in[i];
               int a = p & 0xFF000000;
               int r = (p >> 16) & 0xFF;
               int g = (p >> 8) & 0xFF;
               int b = p & 0xFF;
               out[i] = a | Color.getRGBEnsureRange(((m * r) + k) >> 16, ((m * g) + k) >> 16, ((m * b) + k) >> 16);
            }
            imageOut.transparentColor = Color.getRGBEnsureRange(((m * Color.getRed(transparentColor)) + k) >> 16, ((m * Color.getGreen(transparentColor)) + k) >> 16, ((m * Color.getBlue(transparentColor)) + k) >> 16); // guich@tc100b5_41: use the same algorithm for the transparent color
            break;
         case CONTRAST_TOUCHUP:
            for (int i = w*h-1; i >= 0; i--)
            {
               int p = in[i];
               int a = p & 0xFF000000;
               int r = (p >> 16) & 0xFF;
               int g = (p >> 8) & 0xFF;
               int b = p & 0xFF;
               out[i] = a | Color.getRGBEnsureRange(table[r], table[g], table[b] & 0xFF);
            }
            imageOut.transparentColor = Color.getRGBEnsureRange(table[Color.getRed(transparentColor)], table[Color.getGreen(transparentColor)], table[Color.getBlue(transparentColor)] & 0xFF); // guich@tc100b5_41
            break;
         default: // case CTRSTBRITE_TOUCHUP:
            for (int i = w*h-1; i >= 0; i--)
            {
               int p = in[i];
               int a = p & 0xFF000000;
               int r = table[(p >> 16) & 0xFF];
               int g = table[(p >> 8) & 0xFF];
               int b = table[p & 0xFF];
               out[i] = a | Color.getRGBEnsureRange(((m * r) + k) >> 16, ((m * g) + k) >> 16, ((m * b) + k) >> 16);
            }
            imageOut.transparentColor = Color.getRGBEnsureRange(((m * Color.getRed(transparentColor)) + k) >> 16, ((m * Color.getGreen(transparentColor)) + k) >> 16, ((m * Color.getBlue(transparentColor)) + k) >> 16); // guich@tc100b5_41
            break;
      }
      if (imageOut.useAlpha)
         imageOut.transparentColor = NO_TRANSPARENT_COLOR;
      if (frameCount > 1) // guich@tc100b5_40
         imageOut.setFrameCount(frameCount);
      return imageOut;
   }

   /** Internal use only. */
   private void copyFrom(Image img)
   {
      this.width  = img.getWidth();
      this.height = img.getHeight();
      this.pixels = img.pixels;
      this.transparentColor = img.transparentColor;
      this.useAlpha = img.useAlpha;
      this.frameCount = img.frameCount;
      this.comment = img.comment;
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

   private void imageLoad(String path) throws ImageException
   {
      byte[] bytes = Launcher.instance.readBytes(path);
      // NOTE: we could use the following to read out of an applet's JAR file
      // if we could get a pathObject which was in the root directory (the
      // App for example). However, if we don't have one. If we loaded an
      // image in the App constructor we wouldn't have the App object yet...
      // if (!isApp)
      // try
      // {
      // Object pathObject = Applet.currentApplet;
      // stream = pathObject.getClass().getResourceAsStream(path);
      // }
      // catch (Exception e) {};

      if (bytes == null)
         throw new ImageException("ERROR: can't open image file " + path);
      try
      {
         if (new String(bytes, 0, 2).equals("BM"))
            ImageLoadBMPCompressed(bytes);
         else
            imageLoad(bytes);
      }
      catch (ImageException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ImageException("ERROR: when loading Image " + path + ". Trace appears below.");
      }
   }

   private void imageParse(byte[] fullBmpDescription) throws ImageException
   {
      try
      {
         if (new String(fullBmpDescription, 0, 2).equals("BM"))
            ImageLoadBMPCompressed(fullBmpDescription);
         else
            imageLoad(fullBmpDescription);
      }
      catch (ImageException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ImageException("ERROR: when parsing Image with " + fullBmpDescription.length + " bytes. Trace appears below.");
      }
   }

   // ///////////////// METHODS TAKEN FROM THE TOTALCROSS VM ////////////////////
   // //////////////////////////////////////////////////////////////////////////
   /*
    * Floyd/Steinberg error diffusion dithering algorithm in color. The array * line[][] contains the RGB values for the
    * current line being processed; * line[0][x] = red, line[1][x] = green, line[2][x] = blue.
    */
   // ps: this algorithm was heavily modified and optimized by guich

   private int[] colorTable;

   // readRGB reads in pixels values that are stored uncompressed.
   // The bits represent indices into the color table.
   private void readRGB(int width, int height, int bpp, byte[] in, int offset)
   {
      //totalcross.JavaBridge.print("reading " + (doDither ? "and dithering " : "") + "rgb " + bpp + "bpp");
      // How many pixels can be stored in a byte?
      int pixelsPerByte = 8 / bpp;
      // A bit mask containing the number of bits in a pixel
      int bitMask = (1 << bpp) - 1;
      int bitShifts[] = new int[8];
      int i, x, y, row=0;
      int whichBit = 0;
      int currByte;
      int div = 32 / bpp;

      // The shift values that will move each pixel to the far right
      for (i = 0; i < pixelsPerByte; i++)
         bitShifts[i] = 8 - ((i + 1) * bpp);

      int[] pix = (int[]) this.pixels;
      int pitch = ((width + div - 1) / div) * div; // make sure are in a 4 byte boundary - those extra pixels will be stripped off by the current clip
      int dif = pitch - width;
      // Start at the bottom of the pixel array and work up
      switch (bpp)
      {
         case 16: // guich@tc111_1
            pitch = (width*2+3) & ~3; // guich@tc114_30: bmp with w=41 has 84 bytes per row
            for (dif = pitch - width*2, y=height-1, row = y * width; y >= 0; y--, offset += dif, row -= width+width) // guich@tc110_107
               for (x=width; x > 0; x--)
               {
                  int pixel = (in[offset++] & 0xFF) | ((in[offset++] & 0xFF) << 8);
                  int r = (pixel >> 10) & 0x1f;
                  int g = (pixel >> 5) & 0x1f;
                  int b = pixel & 0x1f;
                  pix[row++] = (r << 19) | (g << 11) | (b << 3);
               }
            break;
         case 32: // guich@tc114_15
            for (y=height-1, row = y * width; y >= 0; y--, row -= width+width)
               for (x=width; x > 0; x--)
                  pix[row++] = ((in[offset++] & 0xFF) /*<< 0*/) | ((in[offset++] & 0xFF) << 8) | ((in[offset++] & 0xFF) << 16) | ((in[offset++] & 0xFF) << 24);
            break;
         case 24:
            pitch = (width*3+3) & ~3; // guich@tc110_107: must consider the width in bytes, not in pixels
            for (dif = pitch - width*3, y=height-1, row = y * width; y >= 0; y--, offset += dif, row -= width+width) // guich@tc110_107
               for (x=width; x > 0; x--)
                  pix[row++] = ((in[offset++] & 0xFF) /*<< 0*/) | ((in[offset++] & 0xFF) << 8) | ((in[offset++] & 0xFF) << 16); // guich@tc114:20: fixed order
            break;
         case 8: // guich@200b3: if 8bpp, use a faster routine
            for (y=height-1, row = y * width; y >= 0; y--, offset += dif, row -= width+width)
               for (x=width; x > 0; x--)
                  pix[row++] = colorTable[in[offset++] & 0xFF];
            break;
         default:
            // Read in the first byte
            currByte = in[offset++] & 0xFF;
            // Start at the bottom of the pixel array and work up
            for (y = height-1, row = y * width; y >= 0; y--, row -= width+width)
               for (x = 0; x < pitch; x++)
               {
                  // Get the next pixel from the current byte
                  if (x < width)
                     pix[row++] = colorTable[(currByte >> bitShifts[whichBit]) & bitMask];
                  // If the current bit position is past the number of pixels in a byte, advance to the next byte
                  if (++whichBit >= pixelsPerByte)
                  {
                     whichBit = 0;
                     if (offset < in.length) currByte = in[offset++] & 0xFF;
                  }
               }
            break;
      }
   }

   private void readRLE(int width, int height, byte[] in, int offset, boolean rle8)
   {
      int val;
      int len, esc, r;
      int x, y;
      int colors0 = 0, colors1 = 0;
      int []pix = (int[]) this.pixels;

      x = 0;
      y = height - 1;

      while (true)
      {
         esc = in[offset++] & 0xFF;
         // encoded mode starts with a run length, and then a byte with two colour indexes to alternate between for the
         // run
         if (esc != 0)
         {
            if (rle8)
            {
               colors0 = colorTable[in[offset++] & 0xFF];
               for (r = y * width +x; esc-- > 0; x++, r++)
                  if (x < width)
                     pix[r] = colors0;
            }
            else
            {
               val = in[offset++] & 0xFF;
               colors0 = colorTable[(val >> 4) & 0x0f];
               colors1 = colorTable[val & 0x0f];
               for (len = 1, r = y * width + x; len <= esc; len++, r++, x++)
                  if (x < width)
                     pix[r] = ((len & 1) == 1) ? colors0 : colors1; // odd count, low nybble
            }
         }
         else
         // A leading zero is an escape; it may signal the end of the bitmap, a cursor move, or some absolute data.
         {
            // zero tag may be absolute mode or an escape
            esc = in[offset++] & 0xFF;
            switch (esc)
            {
               case 0: // end of line
                  x = 0;
                  y--;
                  break;
               case 1: // end of bitmap
                  return;
               case 2: // delta
                  x += in[offset++] & 0xFF;
                  y -= in[offset++] & 0xFF;
                  break;
               default: // no compression
                  if (rle8)
                  {
                     len = esc;
                     for (r = y * width + x; len-- > 0; r++, x++, offset++)
                        if (x < width)
                           pix[r] = colorTable[in[offset] & 0xFF];
                     if ((esc & 1) != 0) // Must have even nunber of bytes
                        offset++;
                  }
                  else
                  // guich@421_6: fixed this algorithm.
                  {
                     for (r = y * width + x, len = 1; len <= esc; len++, r++, x++)
                     {
                        if ((len & 1) == 1)
                        {
                           val = in[offset++] & 0xFF;
                           colors0 = colorTable[(val >> 4) & 0x0f];
                           colors1 = colorTable[val & 0x0f];
                           if (x < width)
                              pix[r] = colors0;
                        }
                        else
                        if (x < width)
                           pix[r] = colors1; // odd count, low nybble
                     }
                     if ((((esc + 1) >> 1) & 1) != 0) // Must have even nunber of words
                        offset++;
                  }
                  break;
            }
         }
      }
   }

   // Intel architecture getUInt16
   private int inGetUint16(byte bytes[], int off)
   {
      return ((bytes[off + 1] & 0xFF) << 8) | (bytes[off] & 0xFF);
   }

   // Intel architecture getUInt32
   private int inGetUint(byte bytes[], int off)
   {
      return ((bytes[off + 3] & 0xFF) << 24) | ((bytes[off + 2] & 0xFF) << 16) | ((bytes[off + 1] & 0xFF) << 8) | (bytes[off] & 0xFF);
   }

   // created by guich to handle all types of modern bitmaps,
   private static final int BI_RGB = 0;
   private static final int BI_RLE8 = 1;

   private void ImageLoadBMPCompressed(byte[] p) throws ImageException
   {
      int bitmapOffset, infoSize;
      int compression, usedColors;

      // header (54 bytes)
      // 0-1 magic chars 'BM'
      // 2-5 uint filesize (not reliable)
      // 6-7 uint16 0
      // 8-9 uint16 0
      // 10-13 uint bitmapOffset
      // 14-17 uint info size
      // 18-21 int width
      // 22-25 int height
      // 26-27 uint16 nplanes
      // 28-29 uint16 bits per pixel
      // 30-33 uint compression flag
      // 34-37 uint image size in bytes
      // 38-41 int biXPixelsPerMeter
      // 42-45 int biYPixelsPerMeter
      // 46-49 uint colors used
      // 50-53 uint important color count
      // 54- uchar bitmap bytes depending to type
      // Each scan line of image data is padded to the next four byte boundary

      if (p[0] != 'B' || p[1] != 'M')
         throw new ImageException("Error in Image: not a bmp file!");
      bitmapOffset = inGetUint(p, 10);
      infoSize = inGetUint(p, 14);
      if (infoSize != 40)
         throw new ImageException("Error in Image: old style bmp");
      this.width = inGetUint(p, 18);
      this.height = inGetUint(p, 22);
      if (this.width > 65535 || this.height > 65535 || this.width <= 0 || this.height <= 0)
         throw new ImageException("Error in Image: bad width/height");
      int bmpBPP = inGetUint16(p, 28);
      compression = inGetUint(p, 30);
      /* imageSize = */inGetUint(p, 34);
      usedColors = inGetUint(p, 46);
      if (usedColors == 0 && bmpBPP <= 8) usedColors = 1 << bmpBPP;

      colorTable = bmpBPP >= 16 ? null : new int[1 << bmpBPP]; // guich@340_59: in some bitmaps, colorsUsed may be
                                                               // smaller than the number of possible colors, but the bitmap may still
                                                               // have indexes greater than the  usedColors, thus making the bitmap
                                                               // appear black. To avoid this, we always allocate 1<<BPP
      // Read the bitmap's color table
      for (int i = 0, j = 54; i < usedColors; i++, j += 4)
         colorTable[i] = (inGetUint(p, j) & 0xFFFFFF);

      // prepares the color table for this screen bpp.
      if (bmpBPP == 1 && colorTable[0] == 0xFFFFFF) // the bitmap is monochrome, are the colors iverted?
      {
         colorTable[1] = 0xFFFFFF;
         colorTable[0] = 0x000000;
      }
      /*
       * if (storePalette) { GRHANDLE_PaletteSize(imgHandle) = 256; GRHANDLE_Palette(imgHandle) = colorTable; }
       */

      // Create space for the pixels
      this.pixels = new int[this.height*this.width];

      // Read the pixels from the stream based on the compression type directly into the selected offscreen image
      if (compression == BI_RGB)
         readRGB(this.width, this.height, bmpBPP, p, bitmapOffset);
      else
      if (bmpBPP == 16)
         throw new ImageException("16-bpp BMP compressed RLE images is not supported! Use 24-bpp instead.");
      else
         readRLE(this.width, this.height, p, bitmapOffset, compression == BI_RLE8);
   }

   /**
    * Fill the required fields of the GifImage <code>image</code> from reading the stream <code>input</code>
    *
    * @param image
    *           totalcross.ui.image.Image to construct
    * @param maxColors
    *           maxColors for the device
    * @param isPalettized
    *           true if indexing into a web safe palette is required
    * @param input
    *           totalcross.io.Stream that contains the GIF encoded bytes of the image
    * @param buffer
    *           array of bytes used as a working buffer - its size should be between 512 and 2048 bytes
    * @param imageNo
    *           position of the image in a multi-image file must start (and default to) zero.
    */
   private void imageLoad(byte[] input) throws Exception
   {
      try
      {
         ImageLoader loader = new ImageLoader(input);
         loader.load(this, 20000000);
         if (!loader.isSupported)
            throw new ImageException("");
      }
      catch (ImageException ie)
      {
         throw new ImageException("TotalCross does not support grayscale+alpha PNG images. Save the image as color (24 bpp).");
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   static class ImageLoader implements java.awt.image.ImageConsumer
   {
      private java.awt.image.ImageProducer producer;
      private int width, height;
      private int transparentPixel;
      private boolean useAlpha; // guich@tc126_12
      private Image imageCur;
      private boolean isImageComplete;
      private byte[] imgBytes;
      private boolean isGif;
      private Vector frames = new Vector(5);
      private boolean paletteWithAlpha; // guich@tc130
      boolean isSupported = true;

      /**
       * Create a ImageLoader object to grab frames from the image <code>img</code>
       *
       * @param input
       *           the input stream where the image to retrieve the image data comes from
       */
      public ImageLoader(byte[] input)
      {
         this.imgBytes = input;
         this.isImageComplete = true;
         try
         {
            java.awt.Component component = new java.awt.Component() {};
            java.awt.MediaTracker tracker = new java.awt.MediaTracker(component);
            java.awt.Image image = java.awt.Toolkit.getDefaultToolkit().createImage(input);

            tracker.addImage(image, 0);
            tracker.waitForAll();
            if (!tracker.isErrorAny())
            {
               this.isImageComplete = false;
               this.producer = image.getSource();
               this.width = -1;
               this.height = -1;
            }
         }
         catch (InterruptedException e)
         {
         }
      }

      private void getPNGInformations(byte[] input, Image imgCur) // a shame that Java doesn't support retrieving the comments!
      {
         byte[] bytes = new byte[4];
         int colorType = 0;
         try
         {
            ByteArrayStream bas = new ByteArrayStream(input);
            DataStream ds = new DataStream(bas);
            ds.skipBytes(8);
            int pltePos = -1;
            int plteLen = -1;
            while (true)
            {
               int len = ds.readInt();
               ds.readBytes(bytes);
               String id = new String(bytes);
               if (id.equals("IHDR"))
               {
                  ds.skipBytes(9);
                  colorType = ds.readByte();
                  bas.skipBytes(-10);
                  useAlpha = imgCur.useAlpha = colorType == 4 || colorType == 6;
                  isSupported = colorType != 4;
               }
               else
               if (id.equals("PLTE")) // guich@tc100b5_4
               {
                  pltePos = bas.getPos();
                  plteLen = len;
               }
               else
               if (id.equals("tRNS")) // guich@tc100b5_4
               {
                  switch (len)
                  {
                     case 6: // RGB
                        imgCur.transparentColor = Color.getRGBEnsureRange(ds.readUnsignedShort(),ds.readUnsignedShort(),ds.readUnsignedShort());
                        bas.skipBytes(-6);
                        break;
                     case 256: // palettized? find the color that is transparent (0)
                        if (colorType == 3) // guich@tc130: palettized with alpha-channel palette
                           paletteWithAlpha = useAlpha = imgCur.useAlpha = true;
                        for (int i = 0, pos = bas.getPos(); i < 256; i++,pos++)
                           if (input[pos] == 0)
                           {
                              if (plteLen == 768) // RGB?
                                 imgCur.transparentColor = Color.getRGB(input[pltePos+i*3] & 0xFF,input[pltePos+i*3+1] & 0xFF,input[pltePos+i*3+2] & 0xFF);
                              break;
                           }
                        break;
                  }
               }
               else
               if (id.equals("IEND"))
                  break;
               else
               if (id.equals("tEXt"))
               {
                  String type = ds.readCString();
                  if (type.equals("Comment"))
                  {
                     bytes = new byte[len-type.length()-1];
                     ds.readBytes(bytes);
                     imageCur.comment = new String(bytes);
                  }
                  else bas.skipBytes(-type.length()-1); // guich@tc100b5_31: go back if its not our comment
               }
               ds.skipBytes(len+4); // skip data and crc
            }
            if (useAlpha) transparentPixel = Image.NO_TRANSPARENT_COLOR;
         }
         catch (Exception e) {}
      }

      /**
       * Fill the fields of an empty image
       *
       * @param image
       *           Image in which the fields need to be filled
       * @param millis
       *           time out - if 0, it waits forever
       */
      public synchronized void load(Image image, int millis) throws InterruptedException
      {
         Image loaded = load(millis);
         if (loaded != null)
         {
            int fc = loaded.frameCount;
            loaded.frameCount = 1; // guich@tc100b5: cannot be 0
            image.copyFrom(loaded);
            if (fc > 0)
               image.comment = "FC="+fc;
         }
      }

      /**
       * Create the array of Image
       *
       * @param millis
       *           time out - if 0, it waits forever
       * @return an array of Image, an image per frame
       */
      public synchronized Image load(int millis) throws InterruptedException
      {
         if (!isImageComplete)
         {
            int stopTime = millis + Vm.getTimeStamp();
            producer.startProduction(this);
            while (!isImageComplete)
            {
               if (millis <= 0)
               {
                  wait(0);
               }
               else
               {
                  long remainTime = stopTime - Vm.getTimeStamp();
                  if (remainTime <= 0)
                  {
                     break;
                  }
                  wait(remainTime);
               }
            }
         }
         return imageCur;
      }

      public void setDimensions(int width, int height)
      {
         this.width = width;
         this.height = height;
      }

      public void setHints(int hints)
      {
      }

      public void setProperties(java.util.Hashtable props)
      {
      }

      public void setColorModel(java.awt.image.ColorModel model)
      {
         int index;
         if ((model instanceof java.awt.image.IndexColorModel) && (-1 != (index = ((java.awt.image.IndexColorModel) model).getTransparentPixel())))
            transparentPixel = model.getRGB(index & 0xFF) & 0xFFFFFF;
         else
            transparentPixel = Color.WHITE; // guich@tc120_65
      }

      public final void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel model, byte pixels[], int off, int scansize)
      {
         if (imageStarted())
         {
            int p[] = (int[]) imageCur.pixels;
            int jMax = y + h;
            int iMax = x + w;
            if (useAlpha)
               for (int j = y; j < jMax; ++j, off += scansize)
                  for (int i = j*width+x,ii=x, k = off; ii < iMax; ii++)
                     p[i++] = model.getRGB(pixels[k++] & 0xFF);
            else
               for (int j = y; j < jMax; ++j, off += scansize)
                  for (int i = j*width+x,ii=x, k = off; ii < iMax; ii++)
                     p[i++] = model.getRGB(pixels[k++] & 0xFF) & 0xFFFFFF;
         }
      }

      public final void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel model, int pixels[], int off, int scansize)
      {
         if (imageStarted())
         {
            int[] p = (int[]) imageCur.pixels;
            int jMax = y + h;
            int iMax = x + w;
            if (useAlpha)
               for (int j = y; j < jMax; ++j, off += scansize)
                  for (int i = j*width+x,ii=x, k = off; ii < iMax; ii++)
                     p[i++] = model.getRGB(pixels[k++]);
            else
            if (paletteWithAlpha)
               for (int j = y; j < jMax; ++j, off += scansize)
                  for (int i = j*width+x,ii=x, k = off; ii < iMax; ii++)
                     p[i++] = model.getRGB(pixels[k++]);
            else
               for (int j = y; j < jMax; ++j, off += scansize)
                  for (int i = j*width+x,ii=x, k = off; ii < iMax; ii++)
                     p[i++] = model.getRGB(pixels[k++]) & 0xFFFFFF;
         }
      }

      /**
       * Create a new current Image if necessary
       *
       * @return true if the image was created, false otherwise.
       */
      private final boolean imageStarted()
      {
         if (imageCur == null)
         {
            if (width < 0 || height < 0)
               return false;
            else
            {
               try
               {
                  imageCur = new Image(width, height);
               }
               catch (ImageException e)
               {
                  return false;
               }
               if (new String(imgBytes,1,3).equals("PNG"))
                  getPNGInformations(imgBytes, imageCur);
               else
               if (new String(imgBytes,0,3).equals("GIF"))
                  isGif = true;
               if (transparentPixel >= 0)
               {
                  // fill all pixels with the transparent color
                  int[] p = (int[])imageCur.pixels;
                  Convert.fill(p, 0, p.length, transparentPixel);
               }
               imageCur.transparentColor = transparentPixel;
               imageCur.useAlpha = useAlpha;
            }
         }
         return true;
      }

      private boolean arraysEquals(int[] pix1, int[] pix2)
      {
         if (pix1.length != pix2.length)
            return false;
         try
         {
            return java.util.Arrays.equals(pix1,pix2); // jdk 1.2.x available?
         }
         catch (Throwable t) {}
         for (int i =0; i < pix1.length; i++)
            if (pix1[i] != pix2[i])
               return false;
         return true;
      }

      private void joinImages()
      {
         int n = frames.size();
         if (n == 1) // a single image?
            imageCur = (Image)frames.items[0];
         else
            try
            {
               int totalW = 0;
               int totalH = imageCur.height;
               for (int i =0; i < n; i++)
                  totalW += ((Image)frames.items[i]).width;
               Image temp = new Image(totalW, totalH);
               temp.transparentColor = imageCur.transparentColor;
               temp.useAlpha = useAlpha;
               temp.frameCount = n;
               temp.comment = imageCur.comment;
               int[] dest = (int[])temp.pixels;
               int xx = 0;
               for (int i =0; i < n; i++)
               {
                  Image img = (Image)frames.items[i];
                  int[] src = (int[])img.pixels;
                  int w = img.width;
                  for (int yy = 0; yy < totalH; yy++)
                     Vm.arrayCopy(src, yy*w, dest, xx+yy*totalW, w);
                  xx += w;
               }
               imageCur = temp;
            }
            catch (Exception e)
            {
               imageCur = (Image)frames.items[0]; // if an error occurs, we assume only the first frame
            }
      }

      public synchronized void imageComplete(int status)
      {
         switch (status)
         {
            default:
            case IMAGEERROR:
            case IMAGEABORTED:
               if (isGif && frames.size() > 0)
                  joinImages();
               else
                  Vm.warning("ImageLoader: error");
               isImageComplete = true;
               break;
            case STATICIMAGEDONE:
            case SINGLEFRAMEDONE:
               if (!isGif) // all other image types has a single frame
                  isImageComplete = true;
               else
               {
                  // since jdk can't correctly tell when the last frame of a multi-frame GIF
                  // was reached, we have to keep loading until we repeat the first one.
                  if (frames.size() > 0 && arraysEquals((int[])imageCur.pixels, (int[])((Image)frames.items[0]).pixels))
                  {
                     joinImages();
                     isImageComplete = true;
                  }
                  else
                  {
                     frames.push(imageCur);
                     imageCur = null;
                  }
               }
               break;
         }
         if (isImageComplete)
         {
            producer.removeConsumer(this);
            notifyAll();
         }
      }
   }

   /** Returns 0 */
   public int getX()
   {
      return 0;
   }

   /** Returns 0 */
   public int getY()
   {
      return 0;
   }

   /** Returns true if the given filename is a supported image: Png or Jpeg. Gif and Bmp are supported on JavaSE only.
    * @since TotalCross 1.0
    */
   public static boolean isSupported(String filename)
   {
      if (filename == null) return false;
      filename = filename.toLowerCase();
      return filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".gif") || filename.endsWith(".bmp");
   }
   
   /** In a multi-frame image, returns a copy of the given frame.
    * In a single-frame image, gets a copy of the image.
    * @since TotalCross 1.12 
    */
   final public Image getFrameInstance(int frame) throws ImageException // guich@tc112_7
   {
      Image img = new Image(width,height);
      setCurrentFrame(frame);
      int[] from = (int[])this.pixels;
      int[] to = (int[])img.pixels;
      Vm.arrayCopy(from, 0, to, 0, from.length);
      img.transparentColor = this.transparentColor;
      img.useAlpha = this.useAlpha;
      return img;
   }

   /** Applies the given color r,g,b values to all pixels of this image, 
    * preserving the transparent color and alpha channel, if set.
    * @param color The color to be applied
    * @since TotalCross 1.12
    */
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
      boolean useAlpha = this.useAlpha;

      int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
      for (int n = pixels.length; --n >= 0;)
      {
         int p = pixels[n];
         if (useAlpha || p != transparentColor)
         {
            int r = (mr * Color.getRed(p)) >> 16;
            int g = (mg * Color.getGreen(p)) >> 16;
            int b = (mb * Color.getBlue(p)) >> 16;
            if (r > 255) r = 255;
            if (g > 255) g = 255;
            if (b > 255) b = 255;
            pixels[n] = (p & 0xFF000000) | (r<<16) | (g<<8) | b;
         }
      }
      if (frameCount != 1) {currentFrame = 2; setCurrentFrame(0);}
   }

   /** Returns a smooth scaled instance of this image with a fixed aspect ratio
    * based on the given resolution (which is the resolution that you used to MAKE the image). The target size is computed as 
    * <code>image_size*min(screen_size)/original_resolution</code>
    * @param originalRes The original resolution that the image was developed for. Its a good idea to create images for 320x320 and then scale them down.
    * @param backColor The background color.
    * @since TotalCross 1.12
    */
   final public Image smoothScaledFromResolution(int originalRes, int backColor) throws ImageException // guich@tc112_23
   {
      int k = Math.min(Settings.screenWidth,Settings.screenHeight);
      return getSmoothScaledInstance(width*k/originalRes, height*k/originalRes, backColor);
   }
   
   /** Returns true if the given Image object has the same size and RGB pixels of this one. 
    * The alpha-channel is ignored.
    * @since TotalCross 1.3
    */
   public boolean equals(Object o)
   {
      if (o instanceof Image)
      {
         Image img = (Image)o;
         int w = this.frameCount > 1 ? this.widthOfAllFrames : this.width;
         int w2 = img.frameCount > 1 ? img.widthOfAllFrames : img.width;
         int h = this.height;
         int h2 = img.height;
         if (w != w2 || h != h2)
            return false;
         
         byte[] row1 = new byte[useAlpha ? 4*w : 3*w];
         byte[] row2 = new byte[useAlpha ? 4*w : 3*w];

         for (int y = 0; y < h; y++)
         {
            this.getPixelRow(row1, y);
            img .getPixelRow(row2, y);
            for (int k = row1.length; --k >= 0;)
               if (row1[k] != row2[k])
                  return false;
         }
         return true;
      }
      return false;
   }
   
   /** Applies the given color r,g,b values to all pixels of this image, 
    * preserving the transparent color and alpha channel, if set.
    * This method is used to colorize the Android buttons.
    * @param color The color to be applied
    * @since TotalCross 1.3
    */
   final public void applyColor2(int color)
   {
      int r2 = Color.getRed(color);
      int g2 = Color.getGreen(color);
      int b2 = Color.getBlue(color);
      boolean useAlpha = this.useAlpha;
      int transp = transparentColor,m,p;

      int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);

      // the given color argument will be equivalent to the brighter color of this image. Here we search for that color
      int hi=0, hip=0;
      if (!useAlpha)
      {
         if (transp == -1)
         {
            for (int n = pixels.length; --n >= 0;)
            {
               p = pixels[n];
               int r = (p >> 16) & 0xFF;
               int g = (p >>  8) & 0xFF;
               int b = (p      ) & 0xFF;
               m = (r + g + b) / 3;
               if (m > hi) 
               {
                  hi = m; 
                  hip = p; 
                  if ((p&0xFFFFFF) == 0xFFFFFF) 
                     break;
               }
            }
         }
         else
         {
            for (int n = pixels.length; --n >= 0;)
               if ((p = pixels[n]) != transp)
               {
                  int r = (p >> 16) & 0xFF;
                  int g = (p >>  8) & 0xFF;
                  int b = (p      ) & 0xFF;
                  m = (r + g + b) / 3;
                  if (m > hi) 
                  {
                     hi = m; 
                     hip = p; 
                     if ((p&0xFFFFFF) == 0xFFFFFF) 
                        break;
                  }
               }
         }
      }
      else
         for (int n = pixels.length; --n >= 0;)
            if (((p = pixels[n]) & 0xFF000000) == 0xFF000000) // consider only opaque pixels
            {
               p &= 0x00FFFFFF;
               int r = (p >> 16) & 0xFF;
               int g = (p >>  8) & 0xFF;
               int b = (p      ) & 0xFF;
               m = (r + g + b) / 3;
               if (m > hi) {hi = m; hip = p;}
            }
      
      int hiR = (hip >> 16) & 0xFF;
      int hiG = (hip >>  8) & 0xFF;
      int hiB = (hip      ) & 0xFF;
      if (hip == 0)
         hiR = hiG = hiB = 255;
      
      for (int n = pixels.length; --n >= 0;)
      {
         p = pixels[n];
         if (useAlpha || p != transparentColor)
         {
            int pr = (p >> 16) & 0xFF;
            int pg = (p >>  8) & 0xFF;
            int pb = p & 0xFF;
            int r = pr * r2 / hiR;
            int g = pg * g2 / hiG;
            int b = pb * b2 / hiB;
            if (r > 255) r = 255;
            if (g > 255) g = 255;
            if (b > 255) b = 255;
            pixels[n] = (p & 0xFF000000) | (r<<16) | (g<<8) | b;
         }
      }
      if (frameCount != 1) {currentFrame = 2; setCurrentFrame(0);}
   }
   
   /** Apply a 16-bit Floyd-Steinberg dithering on the current image.
    * Don't use dithering if Settings.screenBPP is not equal to 16, like on desktop computers.
    * @since TotalCross 1.3
    */
   public void dither()
   {
      // based on http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
      int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
      int w = this.frameCount > 1 ? this.widthOfAllFrames : this.width;
      int h = height;
      int[] pixel = (int[])pixels;
      int p,oldR,oldG,oldB, newR,newG,newB, errR, errG, errB;
      for (int y=0; y < h; y++) 
         for (int x=0; x < w; x++)
         {
            p = pixel[y*w+x];
            if (p == transparentColor) continue;
            // get current pixel values
            oldR = (p>>16) & 0xFF;
            oldG = (p>>8) & 0xFF;
            oldB = p & 0xFF;
            // convert to 565 component values
            newR = oldR >> 3 << 3; 
            newG = oldG >> 2 << 2;
            newB = oldB >> 3 << 3;
            // compute error
            errR = oldR-newR;
            errG = oldG-newG;
            errB = oldB-newB;
            // set new pixel
            pixel[y*w+x] = (p & 0xFF000000) | (newR<<16) | (newG<<8) | newB;
            

            addError(pixel, x+1, y ,w, errR,errG,errB,7,16);
            addError(pixel, x-1,y+1,w, errR,errG,errB,3,16);
            addError(pixel, x,y+1  ,w, errR,errG,errB,5,16);
            addError(pixel, x+1,y+1,w, errR,errG,errB,1,16);
         }
      if (frameCount != 1) {currentFrame = 2; setCurrentFrame(0);}
   }

   private void addError(int[] pixel, int x, int y, int w, int errR, int errG, int errB, int j, int k)
   {
      if (x >= w || y >= height || x < 0) return;
      int i = y*w+x;
      int p = pixel[i];
      int r = (p>>16) & 0xFF;
      int g = (p>>8) & 0xFF;
      int b = p & 0xFF;
      r += j*errR/k;
      g += j*errG/k;
      b += j*errB/k;
      if (r > 255) r = 255; else if (r < 0) r = 0;
      if (g > 255) g = 255; else if (g < 0) g = 0;
      if (b > 255) b = 255; else if (b < 0) b = 0;
      pixel[i] = (p & 0xFF000000) | (r << 16) | (g << 8) | b;
   }
}
