// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.io.ByteArrayStream;
import totalcross.io.CRC32Stream;
import totalcross.io.DataStream;
import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.PDBFile;
import totalcross.io.ResizeRecord;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.GfxSurface;
import totalcross.ui.gfx.Graphics;
import totalcross.util.zip.ZLib;

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
 * The hwScale methods should not be used in images that are shown using transition effects.
 *
 * @see Graphics
 */
public class Image4D extends GfxSurface {
  // int
  public int surfaceType = 1; // don't move from here! must be static at position 0
  protected int width;
  protected int height;
  /** The number of frames of this image, if derived from a multi-frame gif. */
  private int frameCount = 1;
  private int currentFrame = -1, widthOfAllFrames;

  /** Dumb field to keep compilation compatibility with TC 1 */
  public int transparentColor = Color.WHITE;
  /** Dumb field to keep compilation compatibility with TC 1 */
  public boolean useAlpha; // guich@tc126_12
  /** A global alpha mask to be applied to the whole image when drawing it, ranging from 0 to 255.
   */
  public int alphaMask = 255;
  public int lastAccess = -1;
  int textureId = -1;
  
  // object
  private int[] pixels; // must be at Object position 0
  protected int[] pixelsOfAllFrames;
  public String comment;
  private Graphics gfx;
  private boolean[] changed = { true };
  private int[] instanceCount = new int[1];
  private Image4D[] master = new Image4D[1];
  private String path;

  // double
  /** Hardware accellerated scaling. The original image is scaled up or down
   * by the video card when its displayed. In high end devices, the quality
   * is the same of the algorithm used in smooth instances. 
   * 
   * Works only if <code>Settings.isOpenGL</code> or on JavaSE. 
   * If you set this in non-opengl environments, nothing will happen; you should use the 
   * hwScaledBy, getHwScaledInstance and hwScaledFixedAspectRatio methods.
   * 
   * To apply the changes, just call <code>repaint()</code>.
   * @see #setHwScaleFixedAspectRatio(int,boolean)
   * @see #hwScaledBy(double, double)
   * @see #hwScaledFixedAspectRatio(int, boolean)
   * @see #getHwScaledInstance(int, int)
   * @since TotalCross 2.0
   */
  public double hwScaleW = 1, hwScaleH = 1;

  // statics
  /** Dumb field to keep compilation compatibility with TC 1 */
  public static final int NO_TRANSPARENT_COLOR = -2;

  /*
   * DO NOT REMOVE!
   * Empty default constructor is required for easy native object creation.
   */
  private Image4D() {
  }

  public Image4D(int width, int height) throws ImageException {
    this.width = width;
    this.height = height;
    try {
      pixels = new int[height * width]; // just create the pixels array
    } catch (OutOfMemoryError oome) {
      throw new ImageException("Out of memory: cannot allocated " + width + "x" + height + " offscreen image.");
    }
    init();
  }

  public Image4D(String path) throws ImageException {
    this.path = path;
    imageLoad(path);
    if (width == 0) {
      throw new ImageException("Could not load image, file not found: " + path);
    }
    init();
  }

  public Image4D(Stream s) throws ImageException, totalcross.io.IOException {
    if (s instanceof File) {
      path = ((File) s).getPath();
    }
    // the buffer must go initially filled so that the native parser can discover if this is a jpeg or png image
    byte[] buf = new byte[512];
    int n = s.readBytes(buf, 0, 4);
    if (n < 4) {
      throw new ImageException("Can't read from Stream");
    }
    imageParse(s, buf);
    if (width == 0) {
      throw new ImageException("Error when loading image from stream");
    }
    init();
  }

  public Image4D(byte[] fullDescription) throws ImageException {
    this(fullDescription, fullDescription.length);
  }

  public Image4D(byte[] fullDescription, int len) throws ImageException {
    if (len < 4) {
      throw new ImageException("Invalid image description");
    }
    ByteArrayStream bas = new ByteArrayStream(fullDescription);
    if (len != fullDescription.length) {
      try {
        bas.setPos(len);
        bas.mark();
      } catch (Exception e) {
      }
    }
    bas.skipBytes(4); // first 4 bytes are read directly from the fullDescription buffer
    imageParse(bas, fullDescription);
    if (width == 0) {
      throw new ImageException("Error when loading image from stream");
    }
    init();
  }

  private Image4D(Image4D src) {
    if (Settings.isOpenGL && src.changed[0]) {
      src.applyChanges();
    }
    this.surfaceType = src.surfaceType;
    this.width = src.width;
    this.height = src.height;
    this.frameCount = src.frameCount;
    this.currentFrame = -1;
    this.widthOfAllFrames = src.widthOfAllFrames;
    this.textureId = src.textureId; // shared among all instances
    this.changed = src.changed;
    this.pixels = src.pixels;
    this.pixelsOfAllFrames = src.pixelsOfAllFrames;
    this.comment = src.comment;
    gfx = new Graphics(this);
    gfx.refresh(0, 0, getWidth(), getHeight(), 0, 0, null);
    this.transparentColor = src.transparentColor;
    this.useAlpha = src.useAlpha; // guich@tc126_12
    this.instanceCount = src.instanceCount; // shared among all instances
    if (instanceCount[0] == 0) {
      this.master = new Image4D[] { src }; // must keep a copy of the original image
    } else {
      this.master = src.master;
    }
    src.instanceCount[0]++;
  }

  public String getPath() {
    return path;
  }

  private void init() throws ImageException {
	textureId = -1;
    if (comment != null && comment.startsWith("FC=")) {
      try {
        setFrameCount(Convert.toInt(comment.substring(3)));
      } catch (InvalidNumberException ine) {
      }
    }
    // init the Graphics
    gfx = new Graphics(this);
    gfx.refresh(0, 0, width, height, 0, 0, null);
  }

  native private void imageLoad(String path);

  native private void imageParse(totalcross.io.Stream in, byte[] buf);

  public void setFrameCount(int n) throws IllegalArgumentException, IllegalStateException, ImageException {
    if (frameCount > 1 && n != frameCount) {
      throw new IllegalStateException("The frame count can only be set once.");
    }
    if (n < 1) {
      throw new IllegalArgumentException("Argument 'n' must have a positive value");
    }

    if (n != frameCount && n > 1 && frameCount <= 1) {
      try {
        frameCount = n;
        comment = "FC=" + n;
        widthOfAllFrames = width;
        width /= frameCount;
        // the pixels will hold the pixel of a single frame
        pixelsOfAllFrames = pixels;
        pixels = new int[width * height];
        setCurrentFrame(0);
      } catch (OutOfMemoryError oome) {
        throw new ImageException("Not enough memory to create the single frame");
      }
    }
  }

  public int getFrameCount() {
    return frameCount;
  }

  native public void setCurrentFrame(int nr);

  public int getCurrentFrame() {
    return currentFrame;
  }

  public void nextFrame() {
    if (frameCount > 1) {
      setCurrentFrame(currentFrame + 1);
    }
  }

  public void prevFrame() {
    if (frameCount > 1) {
      setCurrentFrame(currentFrame - 1);
    }
  }

  @Override
  public int getHeight() {
    return (int) (height * hwScaleH);
  }

  @Override
  public int getWidth() {
    return (int) (width * hwScaleW);
  }

  public Graphics getGraphics() {
    if (pixels == null) {
      return null;
    }
    gfx.setFont(MainWindow.getDefaultFont());
    gfx.refresh(0, 0, width, height, 0, 0, null);
    return gfx;
  }

  native public void applyChanges();

  native public void changeColors(int from, int to);

  public void saveTo(PDBFile cat, String name) throws ImageException, IOException {
    name = name.toLowerCase();
    if (!name.endsWith(".png")) {
      name += ".png";
    }
    int index = findPosition(cat, name, true);
    if (index == -1) {
      index = cat.getRecordCount();
    }
    ResizeRecord rs = new ResizeRecord(cat, Math.min(65500, width * height * 3 + 200)); // guich@tc114_17: make sure is not bigger than 64k
    DataStream ds = new DataStream(rs);
    rs.startRecord(index);
    ds.writeString(name); // write the name
    createPng(rs);
    rs.endRecord();
  }

  public static Image loadFrom(PDBFile cat, String name) throws IOException, ImageException {
    name = name.toLowerCase();
    if (!name.endsWith(".png")) {
      name += ".png";
    }
    int idx = findPosition(cat, name, false);
    if (idx == -1) {
      throw new IOException("The image " + name + " is not inside " + cat.getName());
    }

    cat.setRecordPos(idx);
    DataStream ds = new DataStream(cat);
    cat.skipBytes(ds.readUnsignedShort());
    Image img = new Image(cat);
    cat.setRecordPos(-1);
    return img;
  }

  private static int findPosition(PDBFile cat, String name, boolean isWrite) throws IOException {
    DataStream ds = new DataStream(cat);
    // guich@200b4_45: fixed the insert_in_order routine
    int n = cat.getRecordCount();
    for (int i = 0; i < n; i++) // find the correct position to insert the record. the records must be sorted
    {
      cat.setRecordPos(i);
      String recName = ds.readString();
      if (recName.compareTo(name) >= 0) // is recName greater than name
      {
        if (isWrite && name.equals(recName)) {
          cat.deleteRecord();
        }
        return i;
      }
    }
    return -1;
  }

  public void createPng(Stream s) throws ImageException, totalcross.io.IOException {
    try {
      // based in a code from J. David Eisenberg of PngEncoder, version 1.5
      byte[] pngIdBytes = { (byte) -119, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10 };

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
      ds.writeByte(6); // alpha or direct model
      ds.writeByte(0); // compression method
      ds.writeByte(0); // filter method
      ds.writeByte(0); // no interlace
      int c = (int) crc.getValue();
      ds.writeInt(c);

      // write transparent pixel information, if any
      if (comment != null && comment.length() > 0) {
        ds.writeInt("Comment".length() + 1 + comment.length());
        crc.reset();
        ds.writeBytes("tEXt".getBytes());
        ds.writeBytes("Comment".getBytes());
        ds.writeByte(0);
        ds.writeBytes(comment.getBytes());
        ds.writeInt((int) crc.getValue());
      }

      // write the image data
      crc.reset();
      final int bytesPerPixel = 4;
      byte[] row = new byte[bytesPerPixel * w];
      byte[] filterType = new byte[1];
      ByteArrayStream databas = new ByteArrayStream(bytesPerPixel * w * h + h);

      for (int y = 0; y < h; y++) {
        getPixelRow(row, y);
        databas.writeBytes(filterType, 0, 1);
        databas.writeBytes(row, 0, row.length);
      }
      databas.mark();
      ByteArrayStream compressed = new ByteArrayStream(w * h + h);
      int ncomp = ZLib.deflate(databas, compressed, -1);
      ds.writeInt(ncomp);
      crc.reset();
      ds.writeBytes("IDAT".getBytes());
      ds.writeBytes(compressed.getBuffer(), 0, ncomp);
      c = (int) crc.getValue();
      ds.writeInt(c);

      // write the footer
      ds.writeInt(0);
      crc.reset();
      ds.writeBytes("IEND".getBytes());
      ds.writeInt((int) crc.getValue());
    } catch (OutOfMemoryError oome) {
      throw new ImageException(oome.getMessage() + "");
    }
  }

  native public void getPixelRow(byte[] fillIn, int y);

  private static final int SCALED_INSTANCE = 0;
  private static final int SMOOTH_SCALED_INSTANCE = 1;
  private static final int ROTATED_SCALED_INSTANCE = 2;
  private static final int TOUCHEDUP_INSTANCE = 3;
  private static final int FADED_INSTANCE = 4; // guich@tc110_50
  private static final int ALPHA_INSTANCE = 5; // guich@tc110_50

  native private void getModifiedInstance(totalcross.ui.image.Image4D newImg, int angle, int percScale, int color,
      int brightness, int contrast, int type);

  private totalcross.ui.image.Image4D getModifiedInstance(int newW, int newH, int angle, int percScale, int color,
      int brightness, int contrast, int type) throws totalcross.ui.image.ImageException {
    if (type != ALPHA_INSTANCE && type != FADED_INSTANCE && newW == width && newH == height && (angle % 360) == 0
        && brightness == 0 && contrast == 0) {
      return this;
    }

    newW *= frameCount;
    Image4D imageOut = getCopy(newW, newH);
    if (type == ROTATED_SCALED_INSTANCE && frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }
    getModifiedInstance(imageOut, angle, percScale, color, brightness, contrast, type);
    if (type != ROTATED_SCALED_INSTANCE && frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }
    return imageOut;
  }

  public Image4D getFadedInstance(int backColor) throws ImageException // guich@tc110_50
  {
    return getModifiedInstance(width, height, 0, 0, backColor, 0, 0, FADED_INSTANCE);
  }

  public static int FADE_VALUE = -96;

  public Image4D getFadedInstance() throws ImageException // guich@tc110_50
  {
    return getAlphaInstance(FADE_VALUE);
  }

  public Image4D getAlphaInstance(int delta) throws ImageException {
    return getModifiedInstance(width, height, 0, 0, delta, 0, 0, ALPHA_INSTANCE);
  }

  public totalcross.ui.image.Image4D smoothScaledFixedAspectRatio(int newSize, boolean isHeight) throws ImageException // guich@402_6
  {
    int w = !isHeight ? newSize : (newSize * width / height);
    int h = isHeight ? newSize : (newSize * height / width);
    return getModifiedInstance(w, h, 0, 0, 0, 0, 0, SMOOTH_SCALED_INSTANCE);
  }

  public totalcross.ui.image.Image4D getScaledInstance(int newWidth, int newHeight)
      throws totalcross.ui.image.ImageException // guich@350_22
  {
    return getModifiedInstance(newWidth, newHeight, 0, 0, -1, 0, 0, SCALED_INSTANCE);
  }

  public totalcross.ui.image.Image4D getSmoothScaledInstance(int newWidth, int newHeight)
      throws totalcross.ui.image.ImageException // guich@350_22
  {
    return getModifiedInstance(newWidth, newHeight, 0, 0, 0, 0, 0, SMOOTH_SCALED_INSTANCE);
  }

  public totalcross.ui.image.Image4D getRotatedScaledInstance(int percScale, int angle, int fillColor)
      throws totalcross.ui.image.ImageException {
    if (percScale <= 0) {
      percScale = 1;
    }

    /* xplying by 0x10000 allow integer math, while not loosing much prec. */

    int rawSine = 0;
    int rawCosine = 0;

    angle = angle % 360;
    if ((angle % 90) == 0) {
      if (angle < 0) {
        angle += 360;
      }
      switch (angle) {
      case 0:
        rawCosine = 0x10000;
        break;
      case 90:
        rawSine = 0x10000;
        break;
      case 180:
        rawCosine = -0x10000;
        break;
      default: // case 270:
        rawSine = -0x10000;
        break;
      }
    } else {
      double rad = angle * 0.0174532925;
      rawSine = (int) (Math.sin(rad) * 0x10000);
      rawCosine = (int) (Math.cos(rad) * 0x10000);
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

    for (int i = 2; i >= 0; i--) {
      if (cornersX[i] < xMin) {
        xMin = cornersX[i];
      } else if (cornersX[i] > xMax) {
        xMax = cornersX[i];
      }

      if (cornersY[i] < yMin) {
        yMin = cornersY[i];
      } else if (cornersY[i] > yMax) {
        yMax = cornersY[i];
      }
    }
    if (width == height) {
      xMax = yMax = width;
      xMin = yMin = 0;
    }
    int wOut = ((xMax - xMin) * percScale) / 100;
    int hOut = ((yMax - yMin) * percScale) / 100;
    int x0 = ((wIn << 16) - (((xMax - xMin) * rawCosine) - ((yMax - yMin) * rawSine)) - 1) / 2;
    int y0 = ((hIn << 16) - (((xMax - xMin) * rawSine) + ((yMax - yMin) * rawCosine)) - 1) / 2;
    return getModifiedInstance(wOut, hOut, percScale, angle, 0, x0, y0, ROTATED_SCALED_INSTANCE);
  }

  public totalcross.ui.image.Image4D getTouchedUpInstance(byte brightness, byte contrast)
      throws totalcross.ui.image.ImageException {
    return getModifiedInstance(width, height, 0, 0, 0, brightness, contrast, TOUCHEDUP_INSTANCE);
  }

  public Image4D scaledBy(double scaleX, double scaleY) throws totalcross.ui.image.ImageException // guich@402_6
  {
    return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0) ? this
        : getScaledInstance((int) (width * scaleX), (int) (height * scaleY)); // guich@400_23: now test if the width/height are the same, what returns the original image
  }

  public Image4D smoothScaledBy(double scaleX, double scaleY) throws totalcross.ui.image.ImageException // guich@402_6
  {
    return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0) ? this
        : getSmoothScaledInstance((int) (width * scaleX), (int) (height * scaleY)); // guich@400_23: now test if the width/height are the same, what returns the original image
  }

  private Image4D getCopy(int w, int h) throws ImageException {
    Image4D i = new Image4D(w, h);
    // copy other attributes
    i.path = path;
    i.hwScaleH = this.hwScaleH;
    i.hwScaleW = this.hwScaleW;
    return i;
  }

  native public Image setTransparentColor4D(int color);

  native private void freeTexture();

  @Override
  public void finalize() {
    instanceCount[0]--;
    freeTexture(); // must call always to remove the object from the linked list
  }

  public void lockChanges() {
    if (Settings.isOpenGL) {
      if (changed[0]) {
        applyChanges();
      }
      pixels = pixelsOfAllFrames = null;
    }
  }

  native public void createJpg(Stream s, int quality) throws ImageException, IOException;

  public void setHwScaleFixedAspectRatio(int newSize, boolean isHeight) {
    int w = !isHeight ? newSize : (newSize * width / height);
    int h = isHeight ? newSize : (newSize * height / width);
    hwScaleW = (double) w / width;
    hwScaleH = (double) h / height;
  }

  public Image4D hwScaledFixedAspectRatio(int newSize, boolean isHeight) throws ImageException {
	  return smoothScaledFixedAspectRatio(newSize, isHeight);
  }

  public Image4D getHwScaledInstance(int width, int height) throws ImageException {
	 return getSmoothScaledInstance(width, height);
  }

  public Image4D hwScaledBy(double scaleX, double scaleY) throws ImageException {
	 return smoothScaledBy(scaleX, scaleY);
  }
  
  /** Returns 0 */
  @Override
  public int getX() {
    return 0;
  }

  /** Returns 0 */
  @Override
  public int getY() {
    return 0;
  }
  
  /** Returns true if the given filename is a supported image: Png or Jpeg. Gif and Bmp are supported on JavaSE only.
   * @since TotalCross 1.0
   */
  public static boolean isSupported(String filename) {
    if (filename == null) {
      return false;
    }
    filename = filename.toLowerCase();
    return filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png")
        || (Settings.onJavaSE && (filename.endsWith(".gif") || filename.endsWith(".bmp")));
  }

  /** In a multi-frame image, returns a copy of the given frame.
   * In a single-frame image, gets a copy of the image.
   * @since TotalCross 1.12 
   */  
  public Image4D getFrameInstance(int frame) throws ImageException {
    Image4D img = getCopy(width, height);
    int old = currentFrame;
    setCurrentFrame(frame);
    int[] from = (int[]) this.pixels;
    int[] to = (int[]) img.pixels;
    Vm.arrayCopy(from, 0, to, 0, from.length);
    setCurrentFrame(old);
    return img;
  }
  
  /** Applies the given color r,g,b values to all pixels of this image, 
   * preserving the transparent color and alpha channel, if set.
   * @param color The color to be applied
   * @since TotalCross 1.12
   */
  @ReplacedByNativeOnDeploy
  final public void applyColor(int color) // guich@tc112_24
  {
    int r2 = Color.getRed(color);
    int g2 = Color.getGreen(color);
    int b2 = Color.getBlue(color);
    double k = 128;
    int mr, mg, mb;
    mr = (int) (Math.sqrt((r2 + k) / k) * 0x10000);
    mg = (int) (Math.sqrt((g2 + k) / k) * 0x10000);
    mb = (int) (Math.sqrt((b2 + k) / k) * 0x10000);

    int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
    for (int n = pixels.length; --n >= 0;) {
      int p = pixels[n];
      if ((p & 0xFF000000) != 0) {
        int r = (mr * Color.getRed(p)) >> 16;
        int g = (mg * Color.getGreen(p)) >> 16;
        int b = (mb * Color.getBlue(p)) >> 16;
        if (r > 255) {
          r = 255;
        }
        if (g > 255) {
          g = 255;
        }
        if (b > 255) {
          b = 255;
        }
        pixels[n] = (p & 0xFF000000) | (r << 16) | (g << 8) | b;
      }
    }
    if (frameCount != 1) {
      currentFrame = 2;
      setCurrentFrame(0);
    }
  }

  /** Returns a smooth scaled instance of this image with a fixed aspect ratio
   * based on the given resolution (which is the resolution that you used to MAKE the image). The target size is computed as 
   * <code>image_size*min(screen_size)/original_resolution</code>
   * @param originalRes The original resolution that the image was developed for. Its a good idea to create images for 320x320 and then scale them down.
   * @since TotalCross 1.12
   */  
  final public Image4D smoothScaledFromResolution(int originalRes) throws ImageException // guich@tc112_23
  {
    int k = Math.min(Settings.screenWidth, Settings.screenHeight);
    return getSmoothScaledInstance(width * k / originalRes, height * k / originalRes);
  }

  /** Returns true if the given Image object has the same size and RGB pixels of this one. 
   * The alpha-channel is ignored.
   * @since TotalCross 1.3
   */
  @Override
  public boolean equals(Object o) {
    return (o instanceof Image4D) && nativeEquals((Image4D) o);
  }
  
  @ReplacedByNativeOnDeploy
  private boolean nativeEquals(Image4D other) {
    Image4D img = other;
    int w = this.frameCount > 1 ? this.widthOfAllFrames : this.width;
    int w2 = img.frameCount > 1 ? img.widthOfAllFrames : img.width;
    int h = this.height;
    int h2 = img.height;
    if (w != w2 || h != h2) {
      return false;
    }

    byte[] row1 = new byte[4 * w];
    byte[] row2 = new byte[4 * w];

    for (int y = 0; y < h; y++) {
      this.getPixelRow(row1, y);
      img.getPixelRow(row2, y);
      for (int k = row1.length; --k >= 0;) {
        if (row1[k] != row2[k]) {
          return false;
        }
      }
    }
    return true;
  }

  /** Applies the given color r,g,b values to all pixels of this image, 
   * preserving the transparent color and alpha channel, if set.
   * This method is used to colorize the Android buttons.
   * 
   * If the color's alpha is 0xAA, the image's alpha will also be changed. This is used by the Spinner.
   * 
   * @param color The color to be applied
   * @since TotalCross 1.3
   */
  @ReplacedByNativeOnDeploy
  final public void applyColor2(int color) {
    int r2 = Color.getRed(color);
    int g2 = Color.getGreen(color);
    int b2 = Color.getBlue(color);
    boolean changeA = (color & 0xFF000000) == 0xAA000000;
    int m, p;

    int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);

    // the given color argument will be equivalent to the brighter color of this image. Here we search for that color
    int hi = 0, hip = 0;
    for (int n = 0; n < pixels.length; n++) {    
    //for (int n = pixels.length; --n >= 0;) {
      if (((p = pixels[n]) & 0xFF000000) == 0xFF000000) // consider only opaque pixels
      {
        p &= 0x00FFFFFF;
        m = Color.getBrightness(p);
        if (m > hi) {
          hi = m;
          hip = p;
        }
      }
    }

    int hiR = (hip >> 16) & 0xFF;
    int hiG = (hip >> 8) & 0xFF;
    int hiB = (hip) & 0xFF;
    if (hiR == 0) {
      hiR = 255;
    }
    if (hiG == 0) {
      hiG = 255;
    }
    if (hiB == 0) {
      hiB = 255;
    }
    hi = hiR > hiG ? hiR : hiG;
    hi = hi > hiB ? hi : hiB;

    for (int n = 0; n < pixels.length; n++) {
      p = pixels[n];
      if ((p & 0xFF000000) != 0) {
        int pr = (p >> 16) & 0xFF;
        int pg = (p >> 8) & 0xFF;
        int pb = p & 0xFF;
        int r = pr * r2 / hiR;
        int g = pg * g2 / hiG;
        int b = pb * b2 / hiB;
        if (r > 255) {
          r = 255;
        }
        if (g > 255) {
          g = 255;
        }
        if (b > 255) {
          b = 255;
        }
        if (changeA) {
          int a = pr > pg ? pr : pg;
          if (pb > a) {
            a = pb;
          }
          a = a * 255 / hi;
          if (a > 255) {
            a = 255;
          }
          pixels[n] = (a << 24) | (r << 16) | (g << 8) | b;
        } else {
          pixels[n] = (p & 0xFF000000) | (r << 16) | (g << 8) | b;
        }
      }
    }
    if (frameCount != 1) {
      currentFrame = 2;
      setCurrentFrame(0);
    }
  }
  
  ////////////////////// TOTALCROSS 2 ////////////////////

  /** @deprecated TotalCross 2 no longer uses the backColor parameter. */
  @Deprecated
  public Image4D getSmoothScaledInstance(int newWidth, int newHeight, int backColor) throws ImageException // guich@350_22
  {
    return getSmoothScaledInstance(newWidth, newHeight);
  }
  
  /** @deprecated TotalCross 2 no longer uses the backColor parameter. */
  @Deprecated
  public Image4D smoothScaledBy(double scaleX, double scaleY, int backColor) throws ImageException // guich@402_6
  {
    return smoothScaledBy(scaleX, scaleY);
  }
  
  /** @deprecated TotalCross 2 no longer uses the backColor parameter. */
  @Deprecated
  public Image4D smoothScaledFixedAspectRatio(int newSize, boolean isHeight, int backColor) throws ImageException // guich@402_6
  {
    return smoothScaledFixedAspectRatio(newSize, isHeight);
  }
  
  /** @deprecated TotalCross 2 no longer uses the backColor parameter. */
  @Deprecated
  final public Image4D smoothScaledFromResolution(int originalRes, int backColor) throws ImageException // guich@tc112_23
  {
    return smoothScaledFromResolution(originalRes);
  }
  
  /** Applies the given fade value to r,g,b of this image while preserving the alpha value. */
  @ReplacedByNativeOnDeploy
  public void applyFade(int fadeValue) {
    int[] pixels = (int[]) this.pixels;
    int lastColor = -1, lastFaded = 0;
    for (int j = 0; j < pixels.length; j++) {  
      int rgb = pixels[j];
      if (rgb == lastColor) {
        pixels[j] = lastFaded;
      } else {
        lastColor = rgb;
        int a = ((rgb >> 24) & 0xFF);
        int r = ((rgb >> 16) & 0xFF) * fadeValue / 255;
        int g = ((rgb >> 8) & 0xFF) * fadeValue / 255;
        int b = (rgb & 0xFF) * fadeValue / 255;
        lastFaded = pixels[j] = (a << 24) | (r << 16) | (g << 8) | b;
      }
    }
  }

  /** Utility method used to change the frame count of an image. This method exists in
   * java only, not on device. The frame count of a TotalCross' Image is stored in a
   * comment inside the PNG file. If you create a PNG with many frames and don't want to 
   * keep calling the setFrameCount manually, you can call this method like:
   * <pre>
   * Image.writeFrameCount("c:/project/src/images/people.png",2);
   * </pre>
   * Be careful that this must be done once only; this method does not exist in the device
   * and will abort the vm if you try to call it there!
   * @since TotalCross 3.1
   */
  public static void writeFrameCount(String filePath, int count) {
    try {
      Image img = new Image(filePath);
      if (img.getFrameCount() == count) {
        throw new RuntimeException("The image " + filePath + " already has " + count
            + " frames! Please remove the code that called writeFrameCount!");
      }
      img.setFrameCount(count);
      File f = new File(filePath, File.CREATE_EMPTY);
      img.createPng(f);
      f.close();
      Vm.debug("\n\nSuccess changing frame count of " + filePath + " to " + count
          + "! Now don't forget to comment or remove the code, and refresh your project so your IDE can reload the image.\n\n");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Gets a copy of this image; if the image is multi-framed, returns a copy of the first frame.
   * @since TotalCross 3.1
   */
  public Image4D getCopy() throws ImageException {
    return getFrameInstance(0);
  }

  /** Returns a clipped image from the current position. Note that you must ensure that the values are correct or
   * an exception will be thrown
   */
  public Image4D getClippedInstance(int x, int y, int w, int h) throws ImageException {
    Image4D img = new Image4D(w, h);
    Graphics g = img.getGraphics();
    Object o = this;
    g.copyImageRect((Image) o, x, y, w, h, true);
    return img;
  }

  public static void resizeJpeg(String inputPath, String outputPath, int maxPixelSize) {
    if (hasNativeResizeJpeg()) {
      nativeResizeJpeg(inputPath, outputPath, maxPixelSize);
      return;
    }
    try {
      Image img = new Image(inputPath);

      int height = img.getHeight();
      int width = img.getWidth();

      boolean doResize = false;
      boolean isHeight = height > width;

      if (maxPixelSize > 0) {
        doResize = true;
      }

      Image resizedImage;

      if (doResize) {
        resizedImage = img.smoothScaledFixedAspectRatio(maxPixelSize, isHeight);
      } else {
        resizedImage = img;
      }

      File imageDestiny = null;
      try {
        imageDestiny = new File(outputPath, File.CREATE_EMPTY);
        resizedImage.createJpg(imageDestiny, 85);
      } finally {
        if (imageDestiny != null) {
          imageDestiny.close();
        }
      }
    } catch (IOException | ImageException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private static boolean hasNativeResizeJpeg() {
    return Settings.isIOS();
  }

  native public static void nativeResizeJpeg(String inputPath, String outputPath, int maxPixelSize);

  public static native Image getJpegBestFit(String path, int targetWidth, int targetHeight)
      throws java.io.IOException, ImageException;

  public static native Image getJpegScaled(String path, int scaleNumerator, int scaleDenominator)
      throws java.io.IOException, ImageException;
}
