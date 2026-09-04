// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.Launcher;
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
   for (int i = 0; i &lt; 100; i++)
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
public class Image extends GfxSurface {
  private static boolean decodedRasterAllocationFailureForTest;
  private static boolean materializedFrameBufferAllocationFailureForTest;
  private static boolean targetedDecodeInfrastructureFailureForTest;
  static int targetedDecodeInvocationCountForTest;
  static int targetedDecodeWidthForTest;
  static int targetedDecodeHeightForTest;

  private static final class DeterministicImageDecodeException extends ImageException {
    DeterministicImageDecodeException() {
      super("Could not decode encoded image");
    }

    DeterministicImageDecodeException(String message) {
      super(message);
    }

    DeterministicImageDecodeException(Throwable cause) {
      super(cause.getMessage() == null ? "Could not decode encoded image" : cause.getMessage());
    }
  }

  /** Test-only hook for exercising retryable decoded-raster allocation failures. */
  static synchronized void failNextDecodedRasterAllocationForTest() {
    decodedRasterAllocationFailureForTest = true;
  }

  /** Test-only hook for exercising retryable multi-frame buffer allocation failures. */
  static synchronized void failNextMaterializedFrameBufferAllocationForTest() {
    materializedFrameBufferAllocationFailureForTest = true;
  }

  /** Test-only hook for exercising retryable native decoded-raster allocation failures. */
  static void failNextNativeMaterializationForTest() {
    failNextNativeMaterializationForTestNative();
  }

  static synchronized void resetTargetedDecodeInvocationCountForTest() {
    targetedDecodeInvocationCountForTest = 0;
    targetedDecodeWidthForTest = 0;
    targetedDecodeHeightForTest = 0;
  }

  static synchronized int targetedDecodeInvocationCountForTest() {
    return targetedDecodeInvocationCountForTest;
  }

  static synchronized int targetedDecodeWidthForTest() {
    return targetedDecodeWidthForTest;
  }

  static synchronized int targetedDecodeHeightForTest() {
    return targetedDecodeHeightForTest;
  }

  /** Test-only hook for exercising retryable targeted ImageIO setup failures. */
  static synchronized void failNextTargetedDecodeInfrastructureForTest() {
    targetedDecodeInfrastructureFailureForTest = true;
  }

  private static synchronized boolean consumeTargetedDecodeInfrastructureFailureForTest() {
    boolean failure = targetedDecodeInfrastructureFailureForTest;
    targetedDecodeInfrastructureFailureForTest = false;
    return failure;
  }

  private static synchronized void recordTargetedDecodeInvocationForTest() {
    targetedDecodeInvocationCountForTest++;
  }

  @ReplacedByNativeOnDeploy
  private static void failNextNativeMaterializationForTestNative() {
  }

  private static synchronized boolean consumeDecodedRasterAllocationFailureForTest() {
    boolean failure = decodedRasterAllocationFailureForTest;
    decodedRasterAllocationFailureForTest = false;
    return failure;
  }

  private static synchronized boolean consumeMaterializedFrameBufferAllocationFailureForTest() {
    boolean failure = materializedFrameBufferAllocationFailureForTest;
    materializedFrameBufferAllocationFailureForTest = false;
    return failure;
  }

  // ABI-sensitive: keep storage-category order synchronized with
  // TotalCrossVM/src/nm/instancefields.h.
  // int
  public int surfaceType = 1; // don't move from here! must be static at position 0
  protected int width;
  protected int height;

  /** Contains the pixels of this image. */
  int[] pixels;

  private Object pixelsOfAllFrames;

  /** The number of frames of this image, if derived from a multi-frame gif. */
  private int frameCount = 1;

  /** A textual description stored in the PNG image. */
  public String comment;

  private Graphics gfx;

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

  private int logicalWidth;
  private int logicalHeight;

  /** Used by lockChanges to store the hashCode before discarding the pixels. */
  private int hashCode;

  // object fields addressed by TotalCrossVM/src/nm/instancefields.h
  private boolean[] changed = { true };
  private int[] instanceCount = new int[1];
  private Image[] master = new Image[1];
  private String path;
  /** Non-null only while this image is an immutable, deferred encoded source. */
  private ImagePipeline pipeline;

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
  private double contentScale = 1;

  // statics
  /** Dumb field to keep compilation compatibility with TC 1 */
  public static final int NO_TRANSPARENT_COLOR = -2;

  /*
   * DO NOT REMOVE!
   * Empty default constructor is required for easy native object creation.
   */
  private Image() {
  }

  /** Sets the hwScaleW and hwScaleH fields based on the given new size.
   * Does not work on Win32.
   * @see #hwScaleH
   * @see #hwScaleW
   * @since TotalCross 2.0
   */
  public void setHwScaleFixedAspectRatio(int newSize, boolean isHeight) {
    int w = !isHeight ? newSize : (newSize * width / height);
    int h = isHeight ? newSize : (newSize * height / width);
    hwScaleW = (double) w / width;
    hwScaleH = (double) h / height;
  }

  /** At non OpenGL devices, is the same of smoothScaledFixedAspectRatio;
   * At openGL ones, this method shares all image informations
   * while changing only the hwScaleW/hwScaleH parameters. 
   * @since TotalCross 2.0
   */
  public Image hwScaledFixedAspectRatio(int newSize, boolean isHeight) throws ImageException {
    return smoothScaledFixedAspectRatio(newSize, isHeight);
  }

  /** At non OpenGL devices, is the same of getSmoothScaledInstance;
   * At openGL ones, this method shares all image informations
   * while changing only the hwScaleW/hwScaleH parameters. 
   * @since TotalCross 2.0
   */
  public Image getHwScaledInstance(int width, int height) throws ImageException {
    return getSmoothScaledInstance(width, height);
  }

  /** At non OpenGL devices, is the same of smoothScaledBy;
   * At openGL ones, this method shares all image informations
   * while changing only the hwScaleW/hwScaleH parameters. 
   * @since TotalCross 2.0
   */
  public Image hwScaledBy(double scaleX, double scaleY) throws ImageException {
    return smoothScaledBy(scaleX, scaleY);
  }

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
  public Image(int width, int height) throws ImageException {
    this(width, height, 1);
  }

  private Image(int logicalWidth, int logicalHeight, double contentScale) throws ImageException {
    this(logicalWidth, logicalHeight, contentScale, false);
  }

  private Image(int logicalWidth, int logicalHeight, double contentScale, boolean decodedRaster)
      throws ImageException {
    if (!Double.isFinite(contentScale) || contentScale <= 0 || logicalWidth <= 0 || logicalHeight <= 0) {
      throw new ImageException("Image dimensions and content scale must be positive.");
    }
    long pixelWidth = (long) Math.ceil(logicalWidth * contentScale);
    long pixelHeight = (long) Math.ceil(logicalHeight * contentScale);
    if (pixelWidth > Integer.MAX_VALUE || pixelHeight > Integer.MAX_VALUE || pixelWidth * pixelHeight > Integer.MAX_VALUE) {
      throw new ImageException("Image dimensions are too large.");
    }
    this.logicalWidth = logicalWidth;
    this.logicalHeight = logicalHeight;
    this.contentScale = contentScale;
    width = (int) pixelWidth;
    height = (int) pixelHeight;
    try {
      if (decodedRaster && consumeDecodedRasterAllocationFailureForTest()) {
        throw new TransientImageMaterializationException("Simulated decoded-raster allocation failure");
      }
      pixels = new int[height * width]; // just create the pixels array
    } catch (OutOfMemoryError oome) {
      if (decodedRaster) {
        throw new TransientImageMaterializationException(oome);
      }
      throw new ImageException("Out of memory: cannot allocate " + width + "x" + height + " offscreen image.");
    }
    init();
  }

  private Image(Image src) {
    if (Settings.isOpenGL && src.pipeline == null && src.changed[0]) {
      src.applyChanges();
    }
    this.surfaceType = src.surfaceType;
    this.width = src.width;
    this.height = src.height;
    this.logicalWidth = src.logicalWidth;
    this.logicalHeight = src.logicalHeight;
    this.contentScale = src.contentScale;
    this.hwScaleW = src.hwScaleW;
    this.hwScaleH = src.hwScaleH;
    this.alphaMask = src.alphaMask;
    this.frameCount = src.frameCount;
    this.currentFrame = -1;
    this.widthOfAllFrames = src.widthOfAllFrames;
    this.textureId = src.textureId; // shared among all instances
    this.changed = src.changed;
    this.pixels = src.pixels;
    this.pixelsOfAllFrames = src.pixelsOfAllFrames;
    this.comment = src.comment;
    this.gfx = new Graphics(this);
    this.gfx.refresh(0, 0, getWidth(), getHeight(), 0, 0, null);
    this.transparentColor = src.transparentColor;
    this.useAlpha = src.useAlpha;
    this.instanceCount = src.instanceCount; // shared among all instances
    if (instanceCount[0] == 0) {
      this.master = new Image[] { src }; // keep a copy of the original image
    } else {
      this.master = src.master;
    }
    this.path = src.path;
    this.pipeline = src.pipeline;
    src.instanceCount[0]++;
  }

  /** Creates a logical image with an immutable physical backing scale. */
  public static Image createLogical(int width, int height, double contentScale) throws ImageException {
    return new Image(width, height, contentScale);
  }

  /** Used only at desktop to get the image's pixels. */
  public int[] getPixels() {
    materializeCanonicalUnchecked();
    return pixels;
  }

  /**
   * Loads and constructs an image from a file. The path given is the path to the
   * image file. The file must be in 2, 16, 256, 24bpp color compressed (RLE) or uncompressed BMP bitmap
   * format, or a PNG file, or a GIF file, or a JPEG file. If the image cannot be loaded, an ImageException will be thrown.
   * @throws totalcross.ui.image.ImageException When the file was not found.
   */
  public Image(String path) throws ImageException, IOException {
    this.path = path;
    initializeDeferred(EncodedImageSource.fromPath(path));
  }

  /** Loads a BMP, JPEG, GIF or PNG image from a totalcross.io.Stream. Note that Gif and BMP are supported only at desktop.
   * Note that all the bytes of the given stream will be fetched, even those bytes that may follow this Image.
   * @throws totalcross.io.IOException */
  public Image(Stream s) throws ImageException, totalcross.io.IOException {
    if (s instanceof File) {
      path = ((File) s).getPath();
    }
    initializeDeferred(EncodedImageSource.fromStream(s));
  }

  /** Returns the path used to create the Image. For constructors that don't receive a path, returns null */
  public String getPath() {
    return path;
  }

  /** Sets the transparent color of this image. A new image is NOT created.
   * 
   * @deprecated use the alpha channel instead
   * @return The image itself
   * @since TotalCross 2.0
   */
  @Deprecated
  public Image setTransparentColor(int color) {
    if (pipeline != null) {
      deferInPlaceMutation(ImagePipeline.SET_TRANSPARENT_COLOR, color, 0, 0, 0);
      return this;
    }
    materializeCanonicalUnchecked();
    setTransparentColorEager(color);
    return this;
  }

  private void setTransparentColorEager(int color) {
    if (!Settings.onJavaSE) {
      setTransparentColorNative(color);
      return;
    }
    int[] pixels = (int[]) ((frameCount == 1) ? this.pixels : this.pixelsOfAllFrames); // guich@tc100b5_40
    for (int i = pixels.length; --i >= 0;) {
      int p = pixels[i] & 0xFFFFFF;
      pixels[i] = (p == color) ? color : p | 0xFF000000; // if is the transparent color, set the alpha to 0, otherwise, set to full bright
    }
  }

  @ReplacedByNativeOnDeploy
  private void setTransparentColorNative(int color) {
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
   * img.applyChanges();
   * // save the bmp in a byte stream
   * ByteArrayStream bas = new ByteArrayStream(4096);
   * DataStream ds = new DataStream(bas);
   * int totalBytesWritten = img.createPng(ds);
   * // parse the saved png
   * Image im = new Image(bas.getBuffer()); // Caution! the buffer may be greater than totalBytesWritten, but when parsing theres no problem.
   * if (im.getWidth() > 0) // successfully parsed?
   * {
   *    getGraphics().drawImage(im,CENTER,CENTER);
   *    Vm.sleep(2000);
   * }
   * </pre>
   * @throws totalcross.ui.image.ImageException Thrown when something was wrong with the image.
   */
  public Image(byte[] fullDescription) throws ImageException {
    this(fullDescription, fullDescription == null ? -1 : fullDescription.length);
  }

  /** Parses an image from the given byte array with the specified length. Note that the byte array must
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
   * img.applyChanges();
   * // save the bmp in a byte stream
   * ByteArrayStream bas = new ByteArrayStream(4096);
   * DataStream ds = new DataStream(bas);
   * int totalBytesWritten = img.createPng(ds);
   * // parse the saved png
   * Image im = new Image(bas.getBuffer()); // Caution! the buffer may be greater than totalBytesWritten, but when parsing theres no problem.
   * if (im.getWidth() > 0) // successfully parsed?
   * {
   *    getGraphics().drawImage(im,CENTER,CENTER);
   *    Vm.sleep(2000);
   * }
   * </pre>
   * The visible encoded range is copied during construction, so later changes
   * to the caller's array do not affect this image.
   * @throws totalcross.ui.image.ImageException Thrown when something was wrong with the image.
   */
  public Image(byte[] fullDescription, int length) throws ImageException {
    if (fullDescription == null || length < 4 || length > fullDescription.length) {
      throw new ImageException("Invalid image description");
    }
    initializeDeferred(EncodedImageSource.fromBytes(fullDescription, length));
  }

  private void initializeDeferred(EncodedImageSource source) {
    pipeline = new ImagePipeline(source);
    width = source.getFrameCount() > 1 ? source.getLogicalWidth() : source.getIntrinsicWidth();
    height = source.getIntrinsicHeight();
    widthOfAllFrames = source.getIntrinsicWidth();
    logicalWidth = source.getLogicalWidth();
    logicalHeight = source.getLogicalHeight();
    frameCount = source.getFrameCount();
    currentFrame = frameCount > 1 ? 0 : -1;
    comment = source.getComment();
    contentScale = 1;
    surfaceType = 1;
    textureId = -1;
    hwScaleW = 1;
    hwScaleH = 1;
    alphaMask = 255;
    gfx = new Graphics(this);
    gfx.setScales(1, 1);
    gfx.refresh(0, 0, logicalWidth, logicalHeight, 0, 0, null);
  }

  /** Package-private inspection hook used by deployed image smoke tests. */
  ImagePipeline pipelineForSmoke() {
    return pipeline;
  }

  private void initializeDeferredTransform(ImagePipeline deferred, Image source) {
    pipeline = deferred;
    width = deferred.width();
    height = deferred.height();
    logicalWidth = deferred.logicalWidth();
    logicalHeight = deferred.logicalHeight();
    widthOfAllFrames = deferred.widthOfAllFrames();
    frameCount = deferred.frameCount();
    currentFrame = frameCount > 1 ? 0 : -1;
    comment = frameCount > 1 ? "FC=" + frameCount : null;
    path = source.path;
    surfaceType = source.surfaceType;
    transparentColor = source.transparentColor;
    useAlpha = source.useAlpha;
    alphaMask = source.alphaMask;
    contentScale = deferred.hasGeometricNode() ? 1 : deferred.contentScale();
    hwScaleW = source.hwScaleW;
    hwScaleH = source.hwScaleH;
    textureId = -1;
    gfx = new Graphics(this);
    gfx.setScales(contentScale, 1);
    gfx.refresh(0, 0, logicalWidth, logicalHeight, 0, 0, null);
  }

  private void deferInPlaceMutation(int operationType, int parameter1, int parameter2, int parameter3,
      int parameter4) {
    ImagePipeline previous = pipeline;
    if (previous == null) {
      throw new IllegalStateException("Deferred image pipeline is missing");
    }
    previous.clearCachedVariants();
    pipeline = previous.append(operationType, parameter1, parameter2, parameter3, parameter4,
        width, height, logicalWidth, logicalHeight, frameCount, widthOfAllFrames);
    hashCode = 0;
    pixels = null;
    pixelsOfAllFrames = null;
    if (frameCount > 1 && (operationType == ImagePipeline.APPLY_COLOR
        || operationType == ImagePipeline.APPLY_COLOR2 || operationType == ImagePipeline.CHANGE_COLORS)) {
      currentFrame = 0;
    }
  }

  private RasterImageSource snapshotRasterSource() {
    int[] allFrames = frameCount > 1 ? (int[]) pixelsOfAllFrames : null;
    int fullWidth = frameCount > 1 ? widthOfAllFrames : width;
    return new RasterImageSource(width, height, logicalWidth, logicalHeight, contentScale, frameCount,
        currentFrame, fullWidth, pixels == null ? null : pixels.clone(),
        allFrames == null ? null : allFrames.clone(), comment, path, surfaceType, transparentColor, useAlpha,
        alphaMask, hwScaleW, hwScaleH);
  }

  static Image materializeRasterSource(RasterImageSource source) throws ImageException {
    Image result = new Image();
    result.width = source.width;
    result.height = source.height;
    result.logicalWidth = source.logicalWidth;
    result.logicalHeight = source.logicalHeight;
    result.contentScale = source.contentScale;
    result.frameCount = source.frameCount;
    result.currentFrame = source.currentFrame;
    result.widthOfAllFrames = source.widthOfAllFrames;
    result.pixels = source.pixels == null ? null : source.pixels.clone();
    result.pixelsOfAllFrames = source.pixelsOfAllFrames == null ? null : source.pixelsOfAllFrames.clone();
    result.comment = source.comment;
    result.path = source.path;
    result.surfaceType = source.surfaceType;
    result.transparentColor = source.transparentColor;
    result.useAlpha = source.useAlpha;
    result.alphaMask = source.alphaMask;
    result.hwScaleW = source.hwScaleW;
    result.hwScaleH = source.hwScaleH;
    result.textureId = -1;
    result.init();
    return result;
  }

  private Image deferTransform(int operationType, int parameter1, int parameter2, int parameter3,
      int parameter4, int outputWidth, int outputHeight) throws ImageException {
    ImagePipeline previous = pipeline;
    if (previous == null) {
      previous = new ImagePipeline(snapshotRasterSource());
    }
    long allFramesWidth = (long) outputWidth * previous.frameCount();
    if (outputWidth <= 0 || outputHeight <= 0 || allFramesWidth > Integer.MAX_VALUE) {
      throw new ImageException("Image dimensions are too large.");
    }
    ImagePipeline deferred = previous.append(operationType, parameter1, parameter2, parameter3, parameter4,
        outputWidth, outputHeight, outputWidth, outputHeight, previous.frameCount(), (int) allFramesWidth);
    Image result = new Image();
    result.initializeDeferredTransform(deferred, this);
    return result;
  }

  private int normalizedFrame(int frame) {
    if (frameCount <= 1) {
      return 0;
    }
    if (frame < 0) {
      return frameCount - 1;
    }
    return frame >= frameCount ? 0 : frame;
  }

  private Image deferFrameSelection(int frame) {
    ImagePipeline previous = pipeline;
    if (previous == null) {
      previous = new ImagePipeline(snapshotRasterSource());
    }
    ImagePipeline deferred = previous.append(ImagePipeline.FRAME_SELECT, frame, 0, 0, 0,
        previous.width(), previous.height(), previous.logicalWidth(), previous.logicalHeight(), 1,
        previous.width());
    Image result = new Image();
    result.initializeDeferredFrameSelection(deferred, this);
    return result;
  }

  private void initializeDeferredFrameSelection(ImagePipeline deferred, Image source) {
    pipeline = deferred;
    width = deferred.width();
    height = deferred.height();
    logicalWidth = deferred.logicalWidth();
    logicalHeight = deferred.logicalHeight();
    widthOfAllFrames = width;
    frameCount = 1;
    currentFrame = -1;
    path = source.path;
    contentScale = deferred.hasGeometricNode() ? 1 : deferred.contentScale();
    hwScaleW = source.hwScaleW;
    hwScaleH = source.hwScaleH;
    textureId = -1;
    gfx = new Graphics(this);
    gfx.setScales(contentScale, 1);
    gfx.refresh(0, 0, logicalWidth, logicalHeight, 0, 0, null);
  }

  private void initializeDeferredCrop(ImagePipeline deferred) {
    pipeline = deferred;
    width = deferred.width();
    height = deferred.height();
    logicalWidth = deferred.logicalWidth();
    logicalHeight = deferred.logicalHeight();
    widthOfAllFrames = width;
    frameCount = 1;
    currentFrame = -1;
    contentScale = deferred.hasGeometricNode() ? 1 : deferred.contentScale();
    textureId = -1;
    gfx = new Graphics(this);
    gfx.setScales(contentScale, 1);
    gfx.refresh(0, 0, logicalWidth, logicalHeight, 0, 0, null);
  }

  private static int[] rotatedDimensions(int inputWidth, int inputHeight, int scale, int angle) {
    if (scale <= 0) {
      scale = 1;
    }
    int rawSine = 0;
    int rawCosine = 0;
    angle %= 360;
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
      default:
        rawSine = -0x10000;
        break;
      }
    } else {
      double rad = angle * 0.0174532925;
      rawSine = (int) (Math.sin(rad) * 0x10000);
      rawCosine = (int) (Math.cos(rad) * 0x10000);
    }

    int[] cornersX = new int[3];
    int[] cornersY = new int[3];
    int xMin = 0;
    int yMin = 0;
    int xMax = 0;
    int yMax = 0;
    cornersX[0] = (inputWidth * rawCosine) >> 16;
    cornersY[0] = (inputWidth * rawSine) >> 16;
    cornersX[2] = (-inputHeight * rawSine) >> 16;
    cornersY[2] = (inputHeight * rawCosine) >> 16;
    cornersX[1] = cornersX[0] + cornersX[2];
    cornersY[1] = cornersY[0] + cornersY[2];
    if (Settings.onJavaSE) {
      for (int i = 2; --i >= 0;) {
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
    } else {
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
    }
    if (inputWidth == inputHeight) {
      xMax = yMax = inputWidth;
      xMin = yMin = 0;
    }
    return new int[] { ((xMax - xMin) * scale) / 100, ((yMax - yMin) * scale) / 100 };
  }

  private void initializeDecodeTarget(EncodedImageSource source) {
    width = source.getIntrinsicWidth();
    height = source.getIntrinsicHeight();
    logicalWidth = source.getLogicalWidth();
    logicalHeight = source.getLogicalHeight();
    comment = source.getComment();
    contentScale = 1;
    surfaceType = 1;
    textureId = -1;
    hwScaleW = 1;
    hwScaleH = 1;
    alphaMask = 255;
  }

  /** Checked canonical barrier used by APIs that already expose ImageException. */
  private synchronized void materializeCanonicalChecked() throws ImageException {
    ImagePipeline deferred = pipeline;
    if (deferred == null) {
      return;
    }
    Image resolved = resolvePipeline(deferred, 1);
    synchronizePresentationState(resolved);
    pixels = resolved.pixels;
    pixelsOfAllFrames = resolved.pixelsOfAllFrames;
    width = resolved.width;
    height = resolved.height;
    frameCount = resolved.frameCount;
    currentFrame = resolved.currentFrame;
    widthOfAllFrames = resolved.widthOfAllFrames;
    logicalWidth = resolved.logicalWidth;
    logicalHeight = resolved.logicalHeight;
    comment = resolved.comment;
    contentScale = resolved.contentScale;
    transparentColor = resolved.transparentColor;
    useAlpha = resolved.useAlpha;
    textureId = -1;
    hashCode = 0;
    changed[0] = true;
    deferred.clearCachedVariants();
    pipeline = null;
  }

  /** Resolves a deferred image for a destination without adopting the result. */
  /** Resolves this image for a destination raster without adopting the result. */
  Image resolveForDrawing(double destinationScale) throws ImageException {
    if (!Double.isFinite(destinationScale) || destinationScale <= 0) {
      throw new ImageException("Image destination scale must be finite and positive.");
    }
    ImagePipeline deferred = pipeline;
    if (deferred == null) {
      return this;
    }
    double effectiveScale = deferred.hasGeometricNode() ? destinationScale : 1;
    long scaleBits = Double.doubleToLongBits(effectiveScale);
    Image cached = deferred.cachedVariant(scaleBits);
    if (cached != null) {
      synchronizePresentationState(cached);
      return cached;
    }
    Image resolved = resolvePipeline(deferred, effectiveScale);
    synchronizePresentationState(resolved);
    deferred.cacheVariant(scaleBits, resolved);
    return resolved;
  }

  private void synchronizePresentationState(Image resolved) {
    resolved.alphaMask = alphaMask;
    resolved.hwScaleW = hwScaleW;
    resolved.hwScaleH = hwScaleH;
    if (resolved.frameCount > 1) {
      selectCurrentFrameEager(resolved, currentFrame);
    }
  }

  /** Selects a frame on an already materialized variant without crossing the deferred barrier. */
  private static void selectCurrentFrameEager(Image image, int frame) {
    if (image.frameCount <= 1) {
      return;
    }
    int normalized = frame < 0 ? image.frameCount - 1 : frame >= image.frameCount ? 0 : frame;
    if (normalized == image.currentFrame) {
      return;
    }
    image.currentFrame = normalized;
    int[] allFrames = (int[]) image.pixelsOfAllFrames;
    for (int y = image.height - 1; y >= 0; y--) {
      Vm.arrayCopy(allFrames, normalized * image.width + y * image.widthOfAllFrames,
          image.pixels, y * image.width, image.width);
    }
  }

  private Image resolvePipeline(ImagePipeline deferred, double destinationScale) throws ImageException {
    ArrayList<ImagePipeline> nodes = new ArrayList<ImagePipeline>();
    for (ImagePipeline node = deferred; node.previous() != null; node = node.previous()) {
      nodes.add(node);
    }

    Image current;
    ImageSource root = deferred.root();
    if (root instanceof EncodedImageSource) {
      EncodedImageSource source = (EncodedImageSource) root;
      ImageException cached = source.decodeFailure();
      if (cached != null) {
        throw cached;
      }
      Image decoded = new Image();
      decoded.initializeDecodeTarget(source);
      ImagePipeline firstNode = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
      int targetWidth = firstNode == null ? 0 : scaledDimensionAllowingZero(firstNode.logicalWidth(), destinationScale);
      int targetHeight = firstNode == null ? 0 : scaledDimension(firstNode.logicalHeight(), destinationScale);
      boolean targeted = isEligibleJpegTargetDecode(source, firstNode, targetWidth, targetHeight);
      try {
        if (targeted) {
          decoded.decodeEncodedSourceTargeted(source, targetWidth, targetHeight);
        } else {
          decoded.decodeEncodedSource(source);
        }
        if (decoded.pixels == null || decoded.width <= 0 || decoded.height <= 0) {
          throw new DeterministicImageDecodeException("Could not decode encoded image");
        }
      } catch (DeterministicImageDecodeException failure) {
        source.cacheDecodeFailure(failure);
        throw failure;
      } catch (TransientImageMaterializationException failure) {
        throw failure;
      }
      decoded.init(true);
      if (!targeted) {
        try {
          verifyDecodedMetadata(source, decoded);
        } catch (DeterministicImageDecodeException failure) {
          source.cacheDecodeFailure(failure);
          throw failure;
        }
      }
      current = decoded;
    } else {
      current = ((RasterImageSource) root).materialize();
    }

    for (int i = nodes.size() - 1; i >= 0; i--) {
      if (nodes.get(i).operationType() == ImagePipeline.APPLY_FADE && current.frameCount > 1) {
        selectCurrentFrameEager(current, nodes.get(i).parameter2());
      }
      current = applyEagerTransform(current, nodes.get(i), destinationScale);
    }
    return current;
  }

  private static boolean isEligibleJpegTargetDecode(EncodedImageSource source, ImagePipeline firstNode,
      int targetWidth, int targetHeight) {
    if (firstNode == null || firstNode.operationType() != ImagePipeline.SMOOTH_SCALE
        || source.getFormat() != ImageEncodedStructure.Format.JPEG || targetWidth <= 0 || targetHeight <= 0) {
      return false;
    }
    int intrinsicWidth = source.getIntrinsicWidth();
    int intrinsicHeight = source.getIntrinsicHeight();
    return targetWidth < intrinsicWidth && targetHeight < intrinsicHeight
        && jpegTargetDecodeScaleDenominator(intrinsicWidth, intrinsicHeight, targetWidth, targetHeight) > 1;
  }

  private Image applyEagerTransform(Image source, ImagePipeline node, double destinationScale) throws ImageException {
    boolean geometric = node.operationType() == ImagePipeline.SCALE
        || node.operationType() == ImagePipeline.SMOOTH_SCALE
        || node.operationType() == ImagePipeline.ROTATE_SCALE;
    int targetWidth = node.operationType() == ImagePipeline.FRAME_LAYOUT && node.logicalWidth() == 0
        ? 0 : scaledDimension(node.logicalWidth(), destinationScale);
    int targetHeight = scaledDimension(node.logicalHeight(), destinationScale);
    Image result;
    switch (node.operationType()) {
    case ImagePipeline.FRAME_SELECT:
      result = source.eagerFrameSelection(node.parameter1());
      break;
    case ImagePipeline.FRAME_LAYOUT:
      int physicalFrameWidth = node.previous().hasGeometricNode()
          ? targetWidth : node.parameter2();
      result = source.eagerFrameLayout(node.parameter1(), node.logicalWidth(), physicalFrameWidth);
      break;
    case ImagePipeline.CROP:
      result = source.eagerCrop(node.parameter1(), node.parameter2(), node.parameter3(), node.parameter4());
      break;
    case ImagePipeline.SCALE:
      result = source.eagerScaledInstance(targetWidth, targetHeight);
      break;
    case ImagePipeline.SMOOTH_SCALE:
      result = source.eagerSmoothScaledInstance(targetWidth, targetHeight);
      break;
    case ImagePipeline.ROTATE_SCALE:
      int physicalScale = scaledRotationPercentage(node.parameter1(), destinationScale, source.contentScale);
      result = source.eagerRotatedScaledInstance(physicalScale, node.parameter2(), node.parameter3());
      int expectedWidth = checkedFrameWidth(targetWidth, result.frameCount);
      if (result.width != expectedWidth || result.height != targetHeight) {
        result = result.eagerSmoothScaledInstance(expectedWidth, targetHeight);
      }
      break;
    case ImagePipeline.TOUCH_UP:
      result = source.eagerTouchedUpInstance((byte) node.parameter1(), (byte) node.parameter2());
      break;
    case ImagePipeline.FADE:
      result = source.eagerFadedInstance(node.parameter1());
      break;
    case ImagePipeline.ALPHA:
      result = source.eagerAlphaInstance(node.parameter1());
      break;
    case ImagePipeline.APPLY_COLOR:
      source.applyColorEager(node.parameter1());
      result = source;
      break;
    case ImagePipeline.APPLY_COLOR2:
      source.applyColor2Eager(node.parameter1());
      result = source;
      break;
    case ImagePipeline.APPLY_FADE:
      source.applyFadeEager(node.parameter1());
      result = source;
      break;
    case ImagePipeline.CHANGE_COLORS:
      source.changeColorsEager(node.parameter1(), node.parameter2());
      result = source;
      break;
    case ImagePipeline.SET_TRANSPARENT_COLOR:
      source.setTransparentColorEager(node.parameter1());
      result = source;
      break;
    default:
      throw new IllegalStateException("Unknown image pipeline operation: " + node.operationType());
    }
    result.logicalWidth = node.logicalWidth();
    result.logicalHeight = node.logicalHeight();
    result.contentScale = geometric ? destinationScale : source.contentScale;
    if (node.operationType() != ImagePipeline.FRAME_LAYOUT) {
      result.widthOfAllFrames = result.frameCount > 1 ? result.width * result.frameCount : result.width;
    }
    return result;
  }

  private Image eagerFrameLayout(int outputFrameCount, int logicalFrameWidth, int visibleWidth) throws ImageException {
    if (visibleWidth < 0) {
      throw new ImageException("Image dimensions are too large.");
    }
    long visibleLength = (long) visibleWidth * height;
    if (visibleLength > Integer.MAX_VALUE) {
      throw new ImageException("Image dimensions are too large.");
    }
    if (consumeMaterializedFrameBufferAllocationFailureForTest()) {
      throw new TransientImageMaterializationException("Simulated multi-frame buffer allocation failure");
    }

    Image result = new Image();
    result.width = visibleWidth;
    result.height = height;
    result.logicalWidth = logicalFrameWidth;
    result.logicalHeight = logicalHeight;
    result.contentScale = contentScale;
    result.frameCount = outputFrameCount;
    result.currentFrame = -1;
    result.widthOfAllFrames = width;
    result.pixelsOfAllFrames = pixels;
    try {
      result.pixels = new int[(int) visibleLength];
    } catch (OutOfMemoryError oome) {
      throw new TransientImageMaterializationException(oome);
    }
    result.comment = "FC=" + outputFrameCount;
    result.path = path;
    result.surfaceType = surfaceType;
    result.transparentColor = transparentColor;
    result.useAlpha = useAlpha;
    result.alphaMask = alphaMask;
    result.hwScaleW = hwScaleW;
    result.hwScaleH = hwScaleH;
    result.textureId = -1;
    selectCurrentFrameEager(result, 0);
    result.init();
    return result;
  }

  private Image eagerFrameSelection(int frame) throws ImageException {
    materializeCanonicalChecked();
    if (frameCount > 1) {
      setCurrentFrame(frame);
    }
    Image result = new Image(logicalWidth, logicalHeight, contentScale);
    Vm.arrayCopy(pixels, 0, result.pixels, 0, pixels.length);
    return result;
  }

  private Image eagerCrop(int x, int y, int w, int h) throws ImageException {
    materializeCanonicalChecked();
    Image result = new Image(w, h, contentScale);
    if (contentScale != 1 && x >= 0 && y >= 0 && x + w <= logicalWidth && y + h <= logicalHeight) {
      int sourceX = scaledCropEdge(x, contentScale);
      int sourceY = scaledCropEdge(y, contentScale);
      int sourceRight = sourceX + result.width;
      int sourceBottom = sourceY + result.height;
      if (sourceRight <= width && sourceBottom <= height && canCopyCropDirect(sourceX, sourceY, sourceRight, sourceBottom)) {
        for (int row = sourceY; row < sourceBottom; row++) {
          Vm.arrayCopy(pixels, row * width + sourceX, result.pixels, (row - sourceY) * result.width, result.width);
        }
        return result;
      }
    }
    result.gfx.copyImageRect(this, x, y, w, h, true);
    return result;
  }

  private boolean canCopyCropDirect(int sourceX, int sourceY, int sourceRight, int sourceBottom) {
    if (alphaMask != 255 || hwScaleW != 1 || hwScaleH != 1) {
      return false;
    }
    for (int row = sourceY; row < sourceBottom; row++) {
      for (int column = sourceX; column < sourceRight; column++) {
        if ((pixels[row * width + column] >>> 24) != 255) {
          return false;
        }
      }
    }
    return true;
  }

  private static int scaledCropEdge(int logicalEdge, double contentScale) {
    return (int) Math.round(logicalEdge * contentScale);
  }

  private static int scaledDimension(int logicalDimension, double scale) throws ImageException {
    double physical = Math.ceil(logicalDimension * scale);
    if (!Double.isFinite(physical) || physical <= 0 || physical > Integer.MAX_VALUE) {
      throw new ImageException("Image dimensions are too large.");
    }
    return (int) physical;
  }

  private static int scaledDimensionAllowingZero(int logicalDimension, double scale) throws ImageException {
    return logicalDimension == 0 ? 0 : scaledDimension(logicalDimension, scale);
  }

  private static int checkedFrameWidth(int width, int frameCount) throws ImageException {
    long total = (long) width * frameCount;
    if (total > Integer.MAX_VALUE) {
      throw new ImageException("Image dimensions are too large.");
    }
    return width;
  }

  private static int scaledRotationPercentage(int percentage, double destinationScale, double sourceScale)
      throws ImageException {
    double physical = percentage * destinationScale / sourceScale;
    if (!Double.isFinite(physical) || physical <= 0 || physical > Integer.MAX_VALUE) {
      throw new ImageException("Image rotation scale is too large.");
    }
    return Math.max(1, (int) Math.round(physical));
  }

  private void verifyDecodedMetadata(EncodedImageSource source, Image decoded) throws ImageException {
    int expectedAllFramesWidth = source.getFormat() == ImageEncodedStructure.Format.GIF
        ? source.getIntrinsicWidth() * source.getFrameCount() : source.getIntrinsicWidth();
    if (decoded.width != (source.getFrameCount() > 1 ? source.getLogicalWidth() : source.getIntrinsicWidth())
        || decoded.height != source.getIntrinsicHeight()
        || decoded.logicalWidth != source.getLogicalWidth()
        || decoded.logicalHeight != source.getLogicalHeight()
        || decoded.frameCount != source.getFrameCount()
        || (decoded.frameCount > 1 && decoded.widthOfAllFrames != expectedAllFramesWidth)) {
      throw new DeterministicImageDecodeException("Decoded image metadata does not match its encoded source");
    }
  }

  /** Unchecked barrier for APIs whose public compatibility signature cannot change. */
  private void materializeCanonicalUnchecked() {
    try {
      materializeCanonicalChecked();
    } catch (ImageException failure) {
      throw new IllegalStateException("Could not materialize encoded image", failure);
    }
  }

  /** Deploy replacement decodes directly from the source's native bag. */
  @ReplacedByNativeOnDeploy
  private void decodeEncodedSource(EncodedImageSource source) throws ImageException {
    byte[] input = source.bytesForInternalDecode();
    if (input == null) {
      throw new ImageException("Encoded source has no Java backing");
    }
    try {
      imageLoad(input, source.getEncodedLength());
    } catch (ImageException e) {
      if (e instanceof TransientImageMaterializationException) {
        throw e;
      }
      throw new DeterministicImageDecodeException(e);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (Throwable e) {
      throw new TransientImageMaterializationException(e);
    }
  }

  /** Deploy replacement uses the encoded native bag and jpegLoad's target sizing. */
  @ReplacedByNativeOnDeploy
  private void decodeEncodedSourceTargeted(EncodedImageSource source, int targetWidth, int targetHeight)
      throws ImageException {
    byte[] input = source.bytesForInternalDecode();
    if (input == null) {
      throw new ImageException("Encoded source has no Java backing");
    }
    recordTargetedDecodeInvocationForTest();

    ImageInputStream stream = null;
    ImageReader reader = null;
    boolean decodeFailureObserved = false;
    try {
      try {
        stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(input, 0, source.getEncodedLength()));
        java.util.Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
        if (!readers.hasNext()) {
          throw new java.io.IOException("No ImageIO reader available");
        }
        reader = readers.next();
        reader.setInput(stream);
        ImageReadParam parameters = reader.getDefaultReadParam();
        if (consumeTargetedDecodeInfrastructureFailureForTest()) {
          throw new TransientImageMaterializationException("Simulated targeted ImageIO setup failure");
        }

        int intrinsicWidth;
        int intrinsicHeight;
        try {
          intrinsicWidth = reader.getWidth(0);
          intrinsicHeight = reader.getHeight(0);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (java.io.IOException e) {
          throw new DeterministicImageDecodeException(e);
        } catch (RuntimeException e) {
          throw new TransientImageMaterializationException(e);
        }
        try {
          int scaleDenominator = jpegTargetDecodeScaleDenominator(
              intrinsicWidth, intrinsicHeight, targetWidth, targetHeight);
          parameters.setSourceSubsampling(scaleDenominator, scaleDenominator, 0, 0);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (RuntimeException e) {
          throw new TransientImageMaterializationException(e);
        }

        BufferedImage frame;
        try {
          frame = reader.read(0, parameters);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (java.io.IOException e) {
          throw new DeterministicImageDecodeException(e);
        } catch (RuntimeException e) {
          throw new TransientImageMaterializationException(e);
        }
        width = frame.getWidth();
        height = frame.getHeight();
        logicalWidth = width;
        logicalHeight = height;
        pixels = new int[width * height];
        frame.getRGB(0, 0, width, height, pixels, 0, width);
        synchronized (Image.class) {
          targetedDecodeWidthForTest = width;
          targetedDecodeHeightForTest = height;
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (java.io.IOException e) {
        throw new TransientImageMaterializationException(e);
      } catch (RuntimeException e) {
        throw new TransientImageMaterializationException(e);
      }
    } catch (ImageException e) {
      decodeFailureObserved = true;
      throw e;
    } catch (Error e) {
      decodeFailureObserved = true;
      throw e;
    } catch (RuntimeException e) {
      decodeFailureObserved = true;
      throw new TransientImageMaterializationException(e);
    } finally {
      Throwable cleanupFailure = null;
      if (reader != null) {
        try {
          reader.dispose();
        } catch (Throwable failure) {
          cleanupFailure = failure;
        }
      }
      if (stream != null) {
        try {
          stream.close();
        } catch (Throwable failure) {
          if (cleanupFailure == null) {
            cleanupFailure = failure;
          }
        }
      }
      if (!decodeFailureObserved && cleanupFailure != null) {
        if (cleanupFailure instanceof OutOfMemoryError) {
          throw (OutOfMemoryError) cleanupFailure;
        }
        if (cleanupFailure instanceof Error) {
          throw (Error) cleanupFailure;
        }
        throw new TransientImageMaterializationException("Targeted ImageIO cleanup failure");
      }
    }
  }

  private void init() throws IllegalArgumentException, IllegalStateException, ImageException {
    init(false);
  }

  private void init(boolean materializingEncodedSource)
      throws IllegalArgumentException, IllegalStateException, ImageException {
    surfaceType = 1;
    textureId = -1;
    if (hwScaleW == 0) {
      hwScaleW = 1;
    }
    if (hwScaleH == 0) {
      hwScaleH = 1;
    }
    if (contentScale == 0) {
      contentScale = 1;
    }
    if (alphaMask == 0) {
      alphaMask = 255;
    }
    if (logicalWidth == 0) {
      logicalWidth = width;
      logicalHeight = height;
    }
    // frame count information?
    if (comment != null && comment.startsWith("FC=")) {
      try {
        setFrameCount(Convert.toInt(comment.substring(3)), materializingEncodedSource);
      } catch (InvalidNumberException ine) {
      }
    }
    // init the Graphics
    gfx = new Graphics(this);
    gfx.setScales(contentScale, 1);
    gfx.refresh(0, 0, logicalWidth, logicalHeight, 0, 0, null);
  }

  /**
   * Sets the frame count for this image. The width may be a multiple of the frame count. After the frame count is
   * set, it cannot be changed.
   * 
   * @throws IllegalArgumentException
   * @throws IllegalStateException
   * @throws ImageException
   * @since TotalCross 1.0
   */
  public void setFrameCount(int n) throws IllegalArgumentException, IllegalStateException, ImageException {
    setFrameCount(n, false);
  }

  private void setFrameCount(int n, boolean materializingEncodedSource)
      throws IllegalArgumentException, IllegalStateException, ImageException {
    if (n < 1) {
      throw new IllegalArgumentException("Argument 'n' must have a positive value");
    }
    if (frameCount > 1 && n != frameCount) {
      throw new IllegalStateException("The frame count can only be set once.");
    }
    if (n == frameCount) {
      return;
    }

    if (pipeline != null) {
      int oldFullWidth = pipeline.widthOfAllFrames();
      int physicalFrameWidth = oldFullWidth / n;
      double canonicalScale = pipeline.hasGeometricNode() ? 1 : pipeline.contentScale();
      int logicalFrameWidth = (int) Math.ceil(physicalFrameWidth / canonicalScale);
      pipeline.clearCachedVariants();
      pipeline = pipeline.append(ImagePipeline.FRAME_LAYOUT, n, physicalFrameWidth, 0, 0,
          physicalFrameWidth, height, logicalFrameWidth, logicalHeight, n, oldFullWidth);
      width = physicalFrameWidth;
      widthOfAllFrames = oldFullWidth;
      logicalWidth = logicalFrameWidth;
      frameCount = n;
      currentFrame = 0;
      comment = "FC=" + n;
      hashCode = 0;
      pixels = null;
      pixelsOfAllFrames = null;
      return;
    }

    materializeCanonicalChecked();

    if (n != frameCount && n > 1 && frameCount <= 1) {
      try {
        frameCount = n;
        comment = "FC=" + n;
        widthOfAllFrames = width;
        width /= frameCount;
        logicalWidth = (int) Math.ceil(width / contentScale);
        // the pixels will hold the pixel of a single frame
        pixelsOfAllFrames = pixels;
        if (materializingEncodedSource && consumeMaterializedFrameBufferAllocationFailureForTest()) {
          throw new TransientImageMaterializationException("Simulated multi-frame buffer allocation failure");
        }
        pixels = new int[width * height];
        setCurrentFrame(0);
      } catch (OutOfMemoryError oome) {
        if (materializingEncodedSource) {
          throw new TransientImageMaterializationException(oome);
        }
        throw new ImageException("Not enough memory to create the single frame");
      }
    }
  }

  /** Returns the frame count of this image.
   * @since TotalCross 1.0
   */
  public int getFrameCount() {
    return frameCount;
  }

  /** Move the contents of the given frame to the currently visible pixels.
   * @since TotalCross 1.0
   */
  final public void setCurrentFrame(int nr) {
    if (pipeline != null) {
      if (frameCount <= 1) {
        return;
      }
      int normalized = normalizedFrame(nr);
      if (normalized != currentFrame) {
        currentFrame = normalized;
      }
      return;
    }
    materializeCanonicalUnchecked();
    if (!Settings.onJavaSE) {
      setCurrentFrameNative(nr);
      return;
    }
    if (frameCount <= 1 || nr == currentFrame) {
      return;
    }
    if (nr < 0) {
      nr = frameCount - 1;
    } else if (nr >= frameCount) {
      nr = 0;
    }
    selectCurrentFrameEager(this, nr);
  }

  @ReplacedByNativeOnDeploy
  private void setCurrentFrameNative(int nr) {
  }

  /** Returns the current frame in a multi-frame image.
   * @since TotalCross 1.0
   */
  public int getCurrentFrame() {
    return currentFrame;
  }

  /** Move to next frame in a multi-frame image.
   * @since TotalCross 1.0
   */
  public void nextFrame() {
    if (frameCount > 1) {
      setCurrentFrame(currentFrame + 1);
    }
  }

  /** Move to the previous frame in a multi-frame image.
   * @since TotalCross 1.0
   */
  public void prevFrame() {
    if (frameCount > 1) {
      setCurrentFrame(currentFrame - 1);
    }
  }

  /** Returns the height of the image. You can check if the image is ok comparing this with zero. */
  @Override
  public int getHeight() {
    return (int) (logicalHeight * hwScaleH);
  }

  /** Returns the width of the image. You can check if the image is ok comparing this with zero. */
  @Override
  public int getWidth() {
    return (int) (logicalWidth * hwScaleW);
  }

  public int getPixelWidth() { return width; }
  public int getPixelHeight() { return height; }
  public double getContentScale() { return contentScale; }

  /** Returns a new Graphics instance that can be used to drawing in this image. */
  public Graphics getGraphics() {
    materializeCanonicalUnchecked();
    if (pixels == null) {
      return null;
    }

    gfx.setFont(MainWindow.getDefaultFont());
    gfx.refresh(0, 0, logicalWidth, logicalHeight, 0, 0, null);
    return gfx;
  }

  /** Applies any pending changes made in this image.
   * In Open GL platforms, creates a texture for this image. This is already done, lazily, when the image
   * is going to be painted. If you want to speedup paint, call this method as soon as any changes in the image
   * are finished.
   * 
   * In non-open gl platforms, does nothing.
   * @since TotalCross 2
   */
  public void applyChanges() {
    if (!Settings.onJavaSE && Settings.isOpenGL) {
      materializeCanonicalUnchecked();
      applyChangesNative();
    }
  }

  @ReplacedByNativeOnDeploy
  private void applyChangesNative() {
  }

  /**
   * In OpenGL platforms, attempts to free the texture associated with this
   * object, if any. Does nothing if no texture is associated with this object, or
   * if the texture associated is shared among multiple objects.
   * This method provides a way of releasing memory resources without having to
   * wait for garbage collection, but it can impact negatively on performance if
   * used on objects that are still in use, causing the textures to be recreated.
   * 
   * In non-OpenGL, does nothing.
   * 
   * AVOID USING THIS METHOD IF UNSURE ABOUT IT.
   */
  public void freeTexture() {
    if (pipeline != null) {
      pipeline.releaseCachedVariantTextures();
      return;
    }
    freeTextureNative();
  }

  /** Releases only native texture state; it never forces deferred materialization. */
  void releaseTextureOnly() {
    freeTextureNative();
  }

  @ReplacedByNativeOnDeploy
  private void freeTextureNative() {
  }

  @Override
  public void finalize() {
    instanceCount[0]--;
    freeTexture();
  }

  /** In OpenGL platforms, apply changes to the current texture and
   * frees the memory used for the pixels in internal memory (the 
   * image can, however, be drawn on screen because the texture will
   * be ready). Calling getGraphics after this method will return a 
   * null reference.
   * 
   * In non-OpenGL, does nothing.
   * @since TotalCross 2.0
   */
  public void lockChanges() {
    if (Settings.isOpenGL) {
      if (changed[0]) {
        applyChanges();
      }
      int[] p = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
      if (p != null) {
        hashCode = 0;
        hashCode = this.hashCode();
      }
      pixels = null;
      pixelsOfAllFrames = null;
    }
  }

  /** Changes all the pixels of the image from one color to the other.
   * The current value of the transparent color is not changed.
   * Using this routine, you can change the colors to any other you want.
   * 
   * Note this replaces a single solid color by another solid color. If you want to change
   * a gradient, or colorize an image, use the applyColor method instead.
   * 
   * You must pass the color with the alpha channel (usually, 0xFF).
   * For example, to change a red to green, use from=0xFFFF0000 (0xFF0000 with alpha=0xFF), to=0xFF00FF00.
   * 
   * @see #applyColor(int)
   * @see #applyColor2(int)
   */
  final public void changeColors(int from, int to) {
    if (pipeline != null) {
      deferInPlaceMutation(ImagePipeline.CHANGE_COLORS, from, to, 0, 0);
      return;
    }
    materializeCanonicalUnchecked();
    changeColorsEager(from, to);
  }

  private void changeColorsEager(int from, int to) {
    if (!Settings.onJavaSE) {
      changeColorsNative(from, to);
      return;
    }
    int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
    for (int n = pixels.length; --n >= 0;) {
      if (pixels[n] == from) {
        pixels[n] = to;
      }
    }
    if (frameCount != 1) {
      currentFrame = 2;
      setCurrentFrame(0);
    }
  }

  @ReplacedByNativeOnDeploy
  private void applyColor2Native(int color) {
  }

  @ReplacedByNativeOnDeploy
  private void changeColorsNative(int from, int to) {
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
  public void saveTo(PDBFile cat, String name) throws ImageException, IOException {
    materializeCanonicalChecked();
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

  /** Loads an image from a PDB file, if it was previously saved using saveTo method.
   * @see #saveTo(PDBFile, String)
   * @since TotalCross 1.22
   */
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

  /**
   * Saves this image as a jpeg file to the given stream.<br>
   * NOT supported on Blackberry.
   * 
   * @param s The output stream used to write the jpeg.
   * @param quality The quality of the image; 100 = no compression, 90 = medium compression, 
   * 80 = high compression. Anything below 80 may greatly redude the image's quality. 85 is a common value. In JavaSE, the quality argument is ignored.
   * @throws ImageException
   * @throws IOException
   */
  public void createJpg(Stream s, int quality) throws ImageException, IOException {
    materializeCanonicalChecked();
    if (!Settings.onJavaSE) {
      createJpgNative(s, quality);
      return;
    }
    createJpgJava(s, quality);
  }

  @ReplacedByNativeOnDeploy
  private void createJpgJava(Stream s, int quality) throws ImageException, IOException {
    try {
      java.awt.image.MemoryImageSource screenMis = new java.awt.image.MemoryImageSource(width, height,
          new java.awt.image.DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0), (int[]) pixels, 0, width);
      screenMis.setAnimated(true);
      screenMis.setFullBufferUpdates(true);
      java.awt.Image screenImg = java.awt.Toolkit.getDefaultToolkit().createImage(screenMis);
      screenMis.newPixels();

      java.awt.image.BufferedImage dest = new java.awt.image.BufferedImage(width, height,
          java.awt.image.BufferedImage.TYPE_INT_RGB);
      java.awt.Graphics2D g2 = dest.createGraphics();
      g2.drawImage(screenImg, 0, 0, null);
      g2.dispose();

      java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(width);
      javax.imageio.ImageIO.write(dest, "jpg", bos);
      s.writeBytes(bos.toByteArray());
    } catch (Throwable e) {
      throw new IOException(e.getMessage());
    }
  }

  @ReplacedByNativeOnDeploy
  private void createJpgNative(Stream s, int quality) throws ImageException, IOException {
  }

  /** Saves this image as a 24 BPP .png file format (if useAlpha is true, it saves as 32 BPP), 
   * to the given stream.
   * If you're sending the png through a stream but not saving to a PDBFile,
   * you can use this method. If you're going to save it to a PDBFile, then
   * you must use the saveTo method.
   * @throws ImageException
   * @throws IOException
   * @see #saveTo(totalcross.io.PDBFile, java.lang.String)
   */
  public void createPng(Stream s) throws ImageException, IOException {
    materializeCanonicalChecked();
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

  /** Used in saveTo method. Fills in the y row into the fillIn array.
   * there must be enough space for the full line be filled, with width*4 bytes. 
   * The alpha channel is NOT stripped off. */
  final public void getPixelRow(byte[] fillIn, int y) {
    materializeCanonicalUnchecked();
    if (!Settings.onJavaSE) {
      getPixelRowNative(fillIn, y);
      return;
    }
    int[] row = (int[]) (frameCount > 1 ? this.pixelsOfAllFrames : this.pixels);
    int w = frameCount > 1 ? this.widthOfAllFrames : this.width;
    for (int x = 0, n = w, i = y * w; n-- > 0;) {
      int p = row[i++];
      fillIn[x++] = (byte) ((p >> 16) & 0xFF); // r
      fillIn[x++] = (byte) ((p >> 8) & 0xFF); // g
      fillIn[x++] = (byte) (p & 0xFF); // b
      fillIn[x++] = (byte) ((p >>> 24) & 0xFF); // a
    }
  }

  @ReplacedByNativeOnDeploy
  private void getPixelRowNative(byte[] fillIn, int y) {
  }

  private static final int SCALED_INSTANCE = 0;
  private static final int SMOOTH_SCALED_INSTANCE = 1;
  private static final int ROTATED_SCALED_INSTANCE = 2;
  private static final int TOUCHEDUP_INSTANCE = 3;
  private static final int FADED_INSTANCE = 4;
  private static final int ALPHA_INSTANCE = 5;

  /** JavaSE implementation; deployed builds call the private native bridge. */
  private void getModifiedInstance(Image newImg, int angle, int percScale, int color,
      int brightness, int contrast, int type) throws ImageException {
    materializeCanonicalChecked();
    if (!Settings.onJavaSE) {
      getModifiedNative(newImg, angle, percScale, color, brightness, contrast, type);
      return;
    }
    Image modified;
    switch (type) {
    case SCALED_INSTANCE:
      modified = eagerScaledInstance(newImg.width / frameCount, newImg.height);
      break;
    case SMOOTH_SCALED_INSTANCE:
      modified = eagerSmoothScaledInstance(newImg.width / frameCount, newImg.height);
      break;
    case ROTATED_SCALED_INSTANCE:
      // The historical native ABI receives scale in angle and rotation in percScale.
      modified = eagerRotatedScaledInstance(angle, percScale, color);
      break;
    case TOUCHEDUP_INSTANCE:
      modified = eagerTouchedUpInstance((byte) brightness, (byte) contrast);
      break;
    case FADED_INSTANCE:
      modified = eagerFadedInstance(color);
      break;
    case ALPHA_INSTANCE:
      modified = eagerAlphaInstance(color);
      break;
    default:
      throw new IllegalArgumentException("Unknown image modification type: " + type);
    }
    newImg.copyFrom(modified);
  }

  @ReplacedByNativeOnDeploy
  private void getModifiedNative(Image newImg, int angle, int percScale, int color,
      int brightness, int contrast, int type) throws ImageException {
  }

  private Image getModifiedInstance(int newW, int newH, int angle, int percScale, int color,
      int brightness, int contrast, int type) throws ImageException {
    if (type != ALPHA_INSTANCE && type != FADED_INSTANCE && contentScale == 1 && newW == width && newH == height
        && (angle % 360) == 0 && brightness == 0 && contrast == 0) {
      return this;
    }

    newW *= frameCount;
    Image imageOut = getCopy(newW, newH);
    if (type == ROTATED_SCALED_INSTANCE && frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }
    getModifiedInstance(imageOut, angle, percScale, color, brightness, contrast, type);
    if (type != ROTATED_SCALED_INSTANCE && frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }
    return imageOut;
  }

  private Image getNativeRotatedScaledInstance(int percScale, int angle, int fillColor) throws ImageException {
    if (percScale <= 0) {
      percScale = 1;
    }

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
      default:
        rawSine = -0x10000;
        break;
      }
    } else {
      double rad = angle * 0.0174532925;
      rawSine = (int) (Math.sin(rad) * 0x10000);
      rawCosine = (int) (Math.cos(rad) * 0x10000);
    }

    int hIn = height;
    int wIn = width;
    int[] cornersX = new int[3];
    int[] cornersY = new int[3];
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
    return getModifiedInstance(wOut, hOut, percScale, angle, fillColor, x0, y0, ROTATED_SCALED_INSTANCE);
  }

  /**
   * Returns the scaled instance for this image. The algorithm used is the replicate scale: not good quality, but fast.
   * 
   * @since SuperWaba 3.5
   */
  public Image getScaledInstance(int newWidth, int newHeight) throws ImageException // guich@350_22
  {
    if (newWidth <= 0 || newHeight <= 0) {
      throw new ImageException("Image dimensions and content scale must be positive.");
    }
    if (!Settings.onJavaSE && contentScale == 1 && newWidth == width && newHeight == height) {
      return this;
    }
    return deferTransform(ImagePipeline.SCALE, newWidth, newHeight, 0, 0, newWidth, newHeight);
  }

  private Image eagerScaledInstance(int newWidth, int newHeight) throws ImageException {
    if (!Settings.onJavaSE) {
      return getModifiedInstance(newWidth, newHeight, 0, 0, -1, 0, 0, SCALED_INSTANCE);
    }
    // Based on the ImageProcessor class on "KickAss Java Programming" (Tonny Espeset)
    newWidth *= frameCount; // guich@tc100b5_40
    Image scaledImage = getCopy(newWidth, newHeight);

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

    for (int y = 0; y < newHeight; y++, hf += hi) {
      wf = fw / w;
      int dstImage = y * newWidth;
      int srcImage = (hf / h) * fw;
      for (int x = newWidth; x > 0; x--, wf += wi) {
        dstImageData[dstImage++] = srcImageData[srcImage + wf / w];
      }
    }
    if (frameCount > 1) {
      scaledImage.setFrameCount(frameCount);
    }

    return scaledImage;
  }

  private static final int BIAS_BITS = 16;
  private static final int BIAS = (1 << BIAS_BITS);

  /** Returns the scaled instance using the area averaging algorithm for this image.
   * Example: <pre>
   * Image img2 = img.getSmoothScaledInstance(200,200);
   * </pre>
   * In device and JavaSE it uses a Catmull-rom resampling, and in Blackberry it uses an area-average resampling.
   * The reason is that the Catmull-rom consumes more memory and is also slower than the area-average, although the 
   * final result is much better. 
   * @since TotalCross 1.0
   */
  public Image getSmoothScaledInstance(int newWidth, int newHeight) throws ImageException // guich@350_22
  {
    if (newWidth <= 0 || newHeight <= 0) {
      throw new ImageException("Image dimensions and content scale must be positive.");
    }
    if (newWidth == width && newHeight == height && (Settings.onJavaSE || contentScale == 1)) {
      return this;
    }
    return deferTransform(ImagePipeline.SMOOTH_SCALE, newWidth, newHeight, 0, 0, newWidth, newHeight);
  }

  private Image eagerSmoothScaledInstance(int newWidth, int newHeight) throws ImageException {
    if (!Settings.onJavaSE) {
      return getModifiedInstance(newWidth, newHeight, 0, 0, 0, 0, 0, SMOOTH_SCALED_INSTANCE);
    }
    // image preparation
    if (newWidth == width && newHeight == height) {
      return this;
    }
    newWidth *= frameCount;
    Image scaledImage = getCopy(newWidth, newHeight);

    int width = this.width * frameCount;
    int height = this.height;
    int[] pixels = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
    int[] pixels2 = (int[]) scaledImage.pixels;

    // algorithm start

    int i, j, n;
    double xScale, yScale;
    int a, r, g, b;

    // Temporary values
    int val;

    int[] v_weight; // Weight contribution    [ow][MAX_CONTRIBS]
    int[] v_pixel; // Pixel that contributes [ow][MAX_CONTRIBS]
    int[] v_count; // How many contribution for the pixel [ow]
    int[] v_wsum; // Sum of weights [ow]

    int[] tb; // Temporary (intermediate buffer)

    double center; // Center of current sampling 
    double weight; // Current wight
    int left; // Left of current sampling
    int right; // Right of current sampling

    int p_weight; // Temporary pointer
    int p_pixel; // Temporary pointer

    int maxContribs, maxContribsXY; // Almost-const: max number of contribution for current sampling
    double scaledRadius, scaledRadiusY; // Almost-const: scaled radius for downsampling operations
    double filterFactor; // Almost-const: filter factor for downsampling operations

    /* Aliasing buffers */

    xScale = ((double) newWidth / width);
    yScale = ((double) newHeight / height);

    if (xScale > 1.0) {
      /* Horizontal upsampling */
      filterFactor = 1;
      scaledRadius = 2;
    } else {
      /* Horizontal downsampling */
      filterFactor = xScale;
      scaledRadius = 2 / xScale;
    }
    maxContribs = (int) (2 * scaledRadius + 1);

    scaledRadiusY = yScale > 1.0 ? 2 : 2 / yScale;
    maxContribsXY = (int) (2 * Math.max(scaledRadiusY, scaledRadius) + 1);

    /* Pre-allocating all of the needed memory */
    int s = newWidth > newHeight ? newWidth : newHeight;
    try {
      tb = new int[newWidth * height];
      v_weight = new int[s * maxContribsXY]; /* weights */
      v_pixel = new int[s * maxContribsXY]; /* the contributing pixels */
      v_count = new int[s]; /* how may contributions for the target pixel */
      v_wsum = new int[s]; /* sum of the weights for the target pixel */
    } catch (OutOfMemoryError t) {
      throw new ImageException("Out of memory");
    }

    /* Pre-calculate weights contribution for a row */
    for (i = 0; i < newWidth; i++) {
      p_weight = i * maxContribs;
      p_pixel = i * maxContribs;

      v_count[i] = 0;
      v_wsum[i] = 0;

      center = ((double) i) / xScale;
      left = (int) (center + 0.5 - scaledRadius);
      right = (int) (left + 2 * scaledRadius);

      for (j = left; j <= right; j++) {
        if (j < 0 || j >= width) {
          continue;
        }
        // Catmull-rom resampling
        double cc = (center - j) * filterFactor;
        if (cc < 0.0) {
          cc = -cc;
        }
        if (cc <= 1.0) {
          weight = 1.5f * cc * cc * cc - 2.5f * cc * cc + 1;
        } else if (cc <= 2.0) {
          weight = -0.5f * cc * cc * cc + 2.5f * cc * cc - 4 * cc + 2;
        } else {
          continue;
        }
        if (weight == 0) {
          continue;
        }
        int iweight = (int) (weight * BIAS);

        n = v_count[i]; /* Since v_count[i] is our current index */
        v_pixel[p_pixel + n] = j;
        v_weight[p_weight + n] = iweight;
        v_wsum[i] += iweight;
        v_count[i]++; /* Increment contribution count */
      }
    }

    /* Filter horizontally from input to temporary buffer */
    for (i = 0; i < newWidth; i++) {
      int count = v_count[i];
      int wsum = v_wsum[i];
      /* Here 'n' runs on the vertical coordinate */
      for (n = 0; n < height; n++) {
        /* i runs on the horizontal coordinate */
        p_weight = i * maxContribs;
        p_pixel = i * maxContribs;

        val = a = r = g = b = 0;
        for (j = 0; j < count; j++) {
          int iweight = v_weight[p_weight++];
          val = pixels[v_pixel[p_pixel++] + n * width]; /* Using val as temporary storage */
          /* Acting on color components */
          a += ((val >> 24) & 0xFF) * iweight;
          r += ((val >> 16) & 0xFF) * iweight;
          g += ((val >> 8) & 0xFF) * iweight;
          b += ((val) & 0xFF) * iweight;
        }
        a /= wsum;
        if (a > 255) {
          a = 255;
        } else if (a < 0) {
          a = 0;
        }
        r /= wsum;
        if (r > 255) {
          r = 255;
        } else if (r < 0) {
          r = 0;
        }
        g /= wsum;
        if (g > 255) {
          g = 255;
        } else if (g < 0) {
          g = 0;
        }
        b /= wsum;
        if (b > 255) {
          b = 255;
        } else if (b < 0) {
          b = 0;
        }
        tb[i + n * newWidth] = (a << 24) | (r << 16) | (g << 8) | b; /* Temporary buffer */
      }
    }

    /* Going to vertical stuff */
    if (yScale > 1.0) {
      filterFactor = 1;
      scaledRadius = 2;
    } else {
      filterFactor = yScale;
      scaledRadius = 2 / yScale;
    }
    maxContribs = (int) (2 * scaledRadius + 1);

    /* Pre-calculate filter contributions for a column */
    for (i = v_weight.length; --i >= 0;) {
      v_weight[i] = v_pixel[i] = 0;
    }

    for (i = 0; i < newHeight; i++) {
      p_weight = i * maxContribs;
      p_pixel = i * maxContribs;

      v_count[i] = 0;
      v_wsum[i] = 0;

      center = ((double) i) / yScale;
      left = (int) (center + 0.5 - scaledRadius);
      right = (int) (left + 2 * scaledRadius);

      for (j = left; j <= right; j++) {
        if (j < 0 || j >= height) {
          continue;
        }
        // Catmull-rom resampling
        double cc = (center - j) * filterFactor;
        if (cc < 0.0) {
          cc = -cc;
        }
        if (cc <= 1.0) {
          weight = 1.5f * cc * cc * cc - 2.5f * cc * cc + 1;
        } else if (cc <= 2.0) {
          weight = -0.5f * cc * cc * cc + 2.5f * cc * cc - 4 * cc + 2;
        } else {
          continue;
        }
        if (weight == 0) {
          continue;
        }
        int iweight = (int) (weight * BIAS);

        n = v_count[i]; /* Our current index */
        v_pixel[p_pixel + n] = j;
        v_weight[p_weight + n] = iweight;
        v_wsum[i] += iweight;
        v_count[i]++; /* Increment the contribution count */
      }
    }

    int idx = 0;

    /* Filter vertically from work to output */
    for (i = 0; i < newHeight; i++) {
      int count = v_count[i];
      int wsum = v_wsum[i];
      for (n = 0; n < newWidth; n++) {
        p_weight = i * maxContribs;
        p_pixel = i * maxContribs;

        val = a = r = g = b = 0;
        for (j = 0; j < count; j++) {
          int iweight = v_weight[p_weight++];
          val = tb[n + newWidth * v_pixel[p_pixel++]]; /* Using val as temporary storage */
          /* Acting on color components */
          a += ((val >> 24) & 0xFF) * iweight;
          r += ((val >> 16) & 0xFF) * iweight;
          g += ((val >> 8) & 0xFF) * iweight;
          b += ((val) & 0xFF) * iweight;
        }
        if (wsum == 0) {
          continue;
        }
        a /= wsum;
        if (a > 255) {
          a = 255;
        } else if (a < 0) {
          a = 0;
        }
        r /= wsum;
        if (r > 255) {
          r = 255;
        } else if (r < 0) {
          r = 0;
        }
        g /= wsum;
        if (g > 255) {
          g = 255;
        } else if (g < 0) {
          g = 0;
        }
        b /= wsum;
        if (b > 255) {
          b = 255;
        } else if (b < 0) {
          b = 0;
        }
        pixels2[idx++] = (a << 24) | (r << 16) | (g << 8) | b;
      }
    }

    if (frameCount > 1) {
      scaledImage.setFrameCount(frameCount);
    }

    return scaledImage;
  }

  /** Returns the scaled instance for this image, given the scale arguments. The algorithm used is
   * the replicate scale, not good quality, but fast. Given values must be &gt; 0.
   * @since SuperWaba 4.1
   */
  public Image scaledBy(double scaleX, double scaleY) throws ImageException // guich@402_6
  {
    return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0) ? this
        : getScaledInstance((int) (width * scaleX), (int) (height * scaleY)); // guich@400_23: now test if the width/height are the same, what returns the original image
  }

  /** Returns the scaled instance for this image, given the scale arguments. Given values must be &gt; 0.
   * The backColor replaces the transparent pixel of the current image to produce a smooth border.
   * Example: <pre>
   * Image img2 = img.smoothScaledBy(0.75,0.75, getBackColor());
   * </pre>
   * @since TotalCross 1.0
   */
  public Image smoothScaledBy(double scaleX, double scaleY) throws ImageException // guich@402_6
  {
    return ((scaleX == 1 && scaleY == 1) || scaleX <= 0 || scaleY <= 0) ? this
        : getSmoothScaledInstance((int) (width * scaleX), (int) (height * scaleY)); // guich@400_23: now test if the width/height are the same, what returns the original image
  }

  /** Returns the scaled instance using fixed aspect ratio for this image, given the scale arguments. Given values must be &gt; 0.
   * This method is useful to resize an image, specifying only one of its sides: the width or the height. The other side
   * is computed to keep the aspect ratio.
   * 
   * @param newSize The new size (width or height) for the image
   * @param isHeight If true, the newSize is considered as the new height of the image. If false, the newSize is considered the new width of the image.
   * 
   * Example: <pre>
   * Image img2 = img.smoothScaledFixed(fmH, true, -1);
   * </pre>
   * @since TotalCross 1.53
   */
  public Image smoothScaledFixedAspectRatio(int newSize, boolean isHeight) throws ImageException // guich@402_6
  {
    int w = !isHeight ? newSize : (newSize * width / height);
    int h = isHeight ? newSize : (newSize * height / width);
    return getSmoothScaledInstance(w, h);
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
   * Color.WHITE if the transparentColor was not set; use 0 for a transparent background, or 0xFF000000 for the BLACK color.
   */
  public Image getRotatedScaledInstance(int scale, int angle, int fillColor) throws ImageException {
    int normalizedScale = scale <= 0 ? 1 : scale;
    int normalizedAngle = angle % 360;
    int[] dimensions = rotatedDimensions(width, height, normalizedScale, normalizedAngle);
    if (!Settings.onJavaSE && normalizedScale == 100 && normalizedAngle == 0 && contentScale == 1
        && dimensions[0] == width && dimensions[1] == height) {
      return this;
    }
    return deferTransform(ImagePipeline.ROTATE_SCALE, normalizedScale, normalizedAngle, fillColor, 0,
        dimensions[0], dimensions[1]);
  }

  private Image eagerRotatedScaledInstance(int scale, int angle, int fillColor) throws ImageException {
    materializeCanonicalChecked();
    if (!Settings.onJavaSE) {
      return getNativeRotatedScaledInstance(scale, angle, fillColor);
    }
    if (scale <= 0) {
      scale = 1;
    }

    /* xplying by 0x10000 allow integer math, while not loosing much prec. */

    int rawSine = 0;
    int rawCosine = 0;
    int sine = 0;
    int cosine = 0;

    angle = angle % 360;
    if ((angle % 90) == 0) {
      if (angle < 0) {
        angle += 360;
      }
      switch (angle) {
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
    } else {
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

    for (int i = 2; --i >= 0;) {
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
    int wOut = ((xMax - xMin) * scale) / 100;
    int hOut = ((yMax - yMin) * scale) / 100;

    Image imageOut = getCopy(wOut * frameCount, hOut);
    if (frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }

    for (int f = 0; f < frameCount; f++) {
      if (frameCount != 1) {
        setCurrentFrame(f);
        imageOut.setCurrentFrame(f);
      }
      int[] pixelsIn = (int[]) this.pixels;

      /* center */
      int x0 = ((wIn << 16) - (((xMax - xMin) * rawCosine) - ((yMax - yMin) * rawSine)) - 1) / 2;
      int y0 = ((hIn << 16) - (((xMax - xMin) * rawSine) + ((yMax - yMin) * rawCosine)) - 1) / 2;
      /* and draw! */
      int[] lineOut = (int[]) imageOut.pixels;
      for (int l = 0; l < hOut; l++) {
        int x = x0;
        int y = y0;
        int iOut = l * imageOut.width;
        for (int i = wOut; --i >= 0; x += cosine, y += sine) {
          int u = x >> 16;
          int v = y >> 16;
          if (0 <= u && u < wIn && 0 <= v && v < hIn) {
            lineOut[iOut++] = pixelsIn[v * this.width + u];
          } else {
            lineOut[iOut++] = fillColor;
          }
        }
        x0 -= sine;
        y0 += cosine;
      }
      if (frameCount != 1) {
        for (int y = imageOut.height - 1; y >= 0; y--) {
          Vm.arrayCopy(imageOut.pixels, y * imageOut.width, imageOut.pixelsOfAllFrames,
              f * imageOut.width + y * imageOut.widthOfAllFrames, imageOut.width);
        }
      }
    }
    if (frameCount != 1) {
      setCurrentFrame(0);
      imageOut.setCurrentFrame(0);
    }
    return imageOut; // success
  }

  /** Creates a faded instance of this image, interpolating all pixels with the given background color.
   * @deprecated Use getFadedInstance() instead
   * @see #getFadedInstance()
   * @since TotalCross 1.01
   */
  @Deprecated
  public Image getFadedInstance(int backColor) throws ImageException // guich@tc110_50
  {
    return deferTransform(ImagePipeline.FADE, backColor, 0, 0, 0, width, height);
  }

  private Image eagerFadedInstance(int backColor) throws ImageException {
    materializeCanonicalChecked();
    if (!Settings.onJavaSE) {
      return getModifiedInstance(width, height, 0, 0, backColor, 0, 0, FADED_INSTANCE);
    }
    Image imageOut = getCopy(frameCount > 1 ? widthOfAllFrames : width, height);
    if (frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }

    int[] from = (int[]) (frameCount > 1 ? pixelsOfAllFrames : pixels);
    int[] to = (int[]) (frameCount > 1 ? imageOut.pixelsOfAllFrames : imageOut.pixels);
    for (int i = from.length; --i >= 0;) {
      to[i] = (from[i] & 0xFF000000) | Color.interpolate(backColor, from[i]); // keep the alpha channel unchanged
    }
    if (frameCount != 1) {
      imageOut.currentFrame = -1;
      imageOut.setCurrentFrame(0);
    }
    return imageOut;
  }

  private Image getCopy(int w, int h) throws ImageException {
    Image i = new Image(w, h);
    i.path = path;
    i.hwScaleH = hwScaleH;
    i.hwScaleW = hwScaleW;
    // copy other attributes
    return i;
  }

  /** Used in getFadedInstance(). */
  public static int FADE_VALUE = -96;

  /** Creates a faded instance of this image, decreasing the alpha-channel by 128 for all pixels.
   * @since TotalCross 2.0
   * @see #FADE_VALUE
   */
  public Image getFadedInstance() throws ImageException // guich@tc110_50
  {
    return getAlphaInstance(FADE_VALUE);
  }

  /** Adds the given value to each pixel's alpha-channel of this image.
   * Only the pixels that don't have a 0 alpha are changed.
   * @since TotalCross 2.0
   */
  public Image getAlphaInstance(int delta) throws ImageException {
    return deferTransform(ImagePipeline.ALPHA, delta, 0, 0, 0, width, height);
  }

  private Image eagerAlphaInstance(int delta) throws ImageException {
    materializeCanonicalChecked();
    if (!Settings.onJavaSE) {
      return getModifiedInstance(width, height, 0, 0, delta, 0, 0, ALPHA_INSTANCE);
    }
    Image imageOut = getCopy(frameCount > 1 ? widthOfAllFrames : width, height);
    if (frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }

    int[] from = (int[]) (frameCount > 1 ? pixelsOfAllFrames : pixels);
    int[] to = (int[]) (frameCount > 1 ? imageOut.pixelsOfAllFrames : imageOut.pixels);
    for (int i = from.length; --i >= 0;) {
      int p = from[i];
      if ((p & 0xFF000000) == 0) {
        to[i] = p;
      } else {
        int a = (p >>> 24) & 0xFF;
        a += delta;
        if (a < 0) {
          a = 0;
        } else if (a > 255) {
          a = 255;
        }
        to[i] = (p & 0x00FFFFFF) | (a << 24);
      }
    }
    if (frameCount != 1) {
      imageOut.currentFrame = -1;
      imageOut.setCurrentFrame(0);
    }
    return imageOut;
  }

  /**
   * Creates a touched-up version of this Image with the specified brightness and contrast. A new <code>Image</code>
   * object is returned which will render the image at the specified <code>brigthness</code>and the specified
   * <code>contrast</code>.
   * 
   * @param brightness
   *           a number between -128 and 127 stating the desired level of brightness.&nbsp; 127 is the highest
   *           brightness level (white image), -128 is no brightness (darkest image).
   * @param contrast
   *           a number between -128 and 127 stating the desired level of contrast.&nbsp; 127 is the highest contrast
   *           level, -128 is no contrast.
   */
  public Image getTouchedUpInstance(byte brightness, byte contrast) throws ImageException {
    if (!Settings.onJavaSE && brightness == 0 && contrast == 0) {
      return this;
    }
    return deferTransform(ImagePipeline.TOUCH_UP, brightness, contrast, 0, 0, width, height);
  }

  private Image eagerTouchedUpInstance(byte brightness, byte contrast) throws ImageException {
    materializeCanonicalChecked();
    if (!Settings.onJavaSE) {
      return getModifiedInstance(width, height, 0, 0, 0, brightness, contrast, TOUCHEDUP_INSTANCE);
    }
    final int NO_TOUCHUP = 0;
    final int BRITE_TOUCHUP = 1;
    final int CONTRAST_TOUCHUP = 2;
    int touchup = NO_TOUCHUP;
    int[] pixelsIn = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
    int w = frameCount == 1 ? this.width : this.widthOfAllFrames;
    int h = this.height;

    Image imageOut = getCopy(w, h);

    int[] pixelsOut = (int[]) imageOut.pixels;
    short table[] = null;
    int m = 0, k = 0;

    if (contrast != 0) {
      touchup |= CONTRAST_TOUCHUP;
      table = computeContrastTable(contrast);
    }
    if (brightness != 0) {
      touchup |= BRITE_TOUCHUP;
      double eBrightness = (brightness + 128.0) / 128.0; // [0.0 ... 2.0]
      if (brightness <= 1.0) {
        m = (int) (Math.sqrt(eBrightness) * 0x10000);
        k = 0;
      } else {
        double f = eBrightness - 1.0;
        f = f * f;
        k = (int) (f * 0xFF0000);
        m = (int) ((1.0 - f) * eBrightness * 0x10000);
      }
    }

    // no palette
    int in[] = pixelsIn;
    int out[] = pixelsOut;
    switch (touchup) {
    case NO_TOUCHUP:
      Vm.arrayCopy(in, 0, out, 0, w * h);
      break;
    case BRITE_TOUCHUP:
      for (int i = w * h - 1; i >= 0; i--) {
        int p = in[i];
        int a = p & 0xFF000000;
        int r = (p >> 16) & 0xFF;
        int g = (p >> 8) & 0xFF;
        int b = p & 0xFF;
        out[i] = a | Color.getRGBEnsureRange(((m * r) + k) >> 16, ((m * g) + k) >> 16, ((m * b) + k) >> 16);
      }
      break;
    case CONTRAST_TOUCHUP:
      for (int i = w * h - 1; i >= 0; i--) {
        int p = in[i];
        int a = p & 0xFF000000;
        int r = (p >> 16) & 0xFF;
        int g = (p >> 8) & 0xFF;
        int b = p & 0xFF;
        out[i] = a | Color.getRGBEnsureRange(table[r], table[g], table[b] & 0xFF);
      }
      break;
    default: // case CTRSTBRITE_TOUCHUP:
      for (int i = w * h - 1; i >= 0; i--) {
        int p = in[i];
        int a = p & 0xFF000000;
        int r = table[(p >> 16) & 0xFF];
        int g = table[(p >> 8) & 0xFF];
        int b = table[p & 0xFF];
        out[i] = a | Color.getRGBEnsureRange(((m * r) + k) >> 16, ((m * g) + k) >> 16, ((m * b) + k) >> 16);
      }
      break;
    }
    if (frameCount > 1) {
      imageOut.setFrameCount(frameCount);
    }
    return imageOut;
  }

  /** Internal use only. */
  private void copyFrom(Image img) {
    this.width = img.width;
    this.height = img.height;
    this.logicalWidth = img.logicalWidth;
    this.logicalHeight = img.logicalHeight;
    this.contentScale = img.contentScale;
    this.pixels = img.pixels;
    this.frameCount = img.frameCount;
    this.comment = img.comment;
  }

  private short[] computeContrastTable(byte level) {
    double factor;
    short[] table = new short[256];
    if (level < 0) {
      factor = (level + 128) / 128.0;
    } else {
      factor = 127.0 / Math.max(127 - level, 1);
    }
    for (int i = 0; i <= 127; i++) {
      int v = ((int) (127.0 * Math.pow(i / 127.0, factor))) & 0xff;
      table[i] = (short) v;
      table[255 - i] = (short) (255 - v);
    }
    return table;
  }

  @ReplacedByNativeOnDeploy
  private void imageLoad(String path) throws ImageException {
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

    if (bytes == null) {
      throw new ImageException("ERROR: can't open image file " + path);
    }

    if (new String(bytes, 0, 2).equals("BM")) {
      ImageLoadBMPCompressed(bytes, bytes.length);
    } else {
      imageLoad(bytes, bytes.length);
    }
  }

  private void imageParse(byte[] fullBmpDescription, int length) throws ImageException {
    if (new String(fullBmpDescription, 0, 2).equals("BM")) {
      ImageLoadBMPCompressed(fullBmpDescription, length);
    } else {
      imageLoad(fullBmpDescription, length);
    }
  }

  /** JavaSE bridge for the deployed stream/initial-buffer native signature. */
  @ReplacedByNativeOnDeploy
  private void imageParse(Stream in, byte[] buf) throws ImageException {
    ByteArrayStream bas = new ByteArrayStream(8192);
    bas.writeBytes(buf, 0, 4);
    byte[] rest = new byte[1024];
    try {
      while (true) {
        int n = in.readBytes(rest, 0, rest.length);
        if (n <= 0) {
          break;
        }
        bas.writeBytes(rest, 0, n);
      }
    } catch (IOException e) {
      throw new ImageException(e.getMessage());
    }
    imageParse(bas.getBuffer(), bas.getPos());
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
  private void readRGB(int width, int height, int bpp, byte[] in, int offset) {
    //totalcross.JavaBridge.print("reading " + (doDither ? "and dithering " : "") + "rgb " + bpp + "bpp");
    // How many pixels can be stored in a byte?
    int pixelsPerByte = 8 / bpp;
    // A bit mask containing the number of bits in a pixel
    int bitMask = (1 << bpp) - 1;
    int bitShifts[] = new int[8];
    int i, x, y, row = 0;
    int whichBit = 0;
    int currByte;
    int div = 32 / bpp;

    // The shift values that will move each pixel to the far right
    for (i = 0; i < pixelsPerByte; i++) {
      bitShifts[i] = 8 - ((i + 1) * bpp);
    }

    int[] pix = (int[]) this.pixels;
    int pitch = ((width + div - 1) / div) * div; // make sure are in a 4 byte boundary - those extra pixels will be stripped off by the current clip
    int dif = pitch - width;
    // Start at the bottom of the pixel array and work up
    switch (bpp) {
    case 16: // guich@tc111_1
      pitch = (width * 2 + 3) & ~3; // guich@tc114_30: bmp with w=41 has 84 bytes per row
      for (dif = pitch - width * 2, y = height - 1, row = y * width; y >= 0; y--, offset += dif, row -= width + width) {
        for (x = width; x > 0; x--) {
          int pixel = (in[offset++] & 0xFF) | ((in[offset++] & 0xFF) << 8);
          int r = (pixel >> 10) & 0x1f;
          int g = (pixel >> 5) & 0x1f;
          int b = pixel & 0x1f;
          pix[row++] = 0xFF000000 | (r << 19) | (g << 11) | (b << 3);
        }
      }
      break;
    case 32: // guich@tc114_15
      for (y = height - 1, row = y * width; y >= 0; y--, row -= width + width) {
        for (x = width; x > 0; x--) {
          pix[row++] = 0xFF000000 | ((in[offset++] & 0xFF) /*<< 0*/) | ((in[offset++] & 0xFF) << 8)
              | ((in[offset++] & 0xFF) << 16) | ((in[offset++] & 0xFF) << 24);
        }
      }
      break;
    case 24:
      pitch = (width * 3 + 3) & ~3; // guich@tc110_107: must consider the width in bytes, not in pixels
      for (dif = pitch - width * 3, y = height - 1, row = y * width; y >= 0; y--, offset += dif, row -= width + width) {
        for (x = width; x > 0; x--) {
          pix[row++] = 0xFF000000
              | (((in[offset++] & 0xFF) /*<< 0*/) | ((in[offset++] & 0xFF) << 8) | ((in[offset++] & 0xFF) << 16)); // guich@tc114:20: fixed order
        }
      }
      break;
    case 8: // guich@200b3: if 8bpp, use a faster routine
      for (y = height - 1, row = y * width; y >= 0; y--, offset += dif, row -= width + width) {
        for (x = width; x > 0; x--) {
          pix[row++] = 0xFF000000 | colorTable[in[offset++] & 0xFF];
        }
      }
      break;
    default:
      // Read in the first byte
      currByte = in[offset++] & 0xFF;
      // Start at the bottom of the pixel array and work up
      for (y = height - 1, row = y * width; y >= 0; y--, row -= width + width) {
        for (x = 0; x < pitch; x++) {
          // Get the next pixel from the current byte
          if (x < width) {
            pix[row++] = 0xFF000000 | colorTable[(currByte >> bitShifts[whichBit]) & bitMask];
          }
          // If the current bit position is past the number of pixels in a byte, advance to the next byte
          if (++whichBit >= pixelsPerByte) {
            whichBit = 0;
            if (offset < in.length) {
              currByte = in[offset++] & 0xFF;
            }
          }
        }
      }
      break;
    }
  }

  private void readRLE(int width, int height, byte[] in, int offset, boolean rle8) {
    int val;
    int len, esc, r;
    int x, y;
    int colors0 = 0, colors1 = 0;
    int[] pix = (int[]) this.pixels;

    x = 0;
    y = height - 1;

    while (true) {
      esc = in[offset++] & 0xFF;
      // encoded mode starts with a run length, and then a byte with two colour indexes to alternate between for the
      // run
      if (esc != 0) {
        if (rle8) {
          colors0 = colorTable[in[offset++] & 0xFF];
          for (r = y * width + x; esc-- > 0; x++, r++) {
            if (x < width) {
              pix[r] = colors0;
            }
          }
        } else {
          val = in[offset++] & 0xFF;
          colors0 = colorTable[(val >> 4) & 0x0f];
          colors1 = colorTable[val & 0x0f];
          for (len = 1, r = y * width + x; len <= esc; len++, r++, x++) {
            if (x < width) {
              pix[r] = ((len & 1) == 1) ? colors0 : colors1; // odd count, low nybble
            }
          }
        }
      } else
      // A leading zero is an escape; it may signal the end of the bitmap, a cursor move, or some absolute data.
      {
        // zero tag may be absolute mode or an escape
        esc = in[offset++] & 0xFF;
        switch (esc) {
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
          if (rle8) {
            len = esc;
            for (r = y * width + x; len-- > 0; r++, x++, offset++) {
              if (x < width) {
                pix[r] = colorTable[in[offset] & 0xFF];
              }
            }
            if ((esc & 1) != 0) {
              offset++;
            }
          } else
          // guich@421_6: fixed this algorithm.
          {
            for (r = y * width + x, len = 1; len <= esc; len++, r++, x++) {
              if ((len & 1) == 1) {
                val = in[offset++] & 0xFF;
                colors0 = colorTable[(val >> 4) & 0x0f];
                colors1 = colorTable[val & 0x0f];
                if (x < width) {
                  pix[r] = colors0;
                }
              } else if (x < width) {
                pix[r] = colors1; // odd count, low nybble
              }
            }
            if ((((esc + 1) >> 1) & 1) != 0) {
              offset++;
            }
          }
          break;
        }
      }
    }
  }

  // Intel architecture getUInt16
  private int inGetUint16(byte bytes[], int off) {
    return ((bytes[off + 1] & 0xFF) << 8) | (bytes[off] & 0xFF);
  }

  // Intel architecture getUInt32
  private int inGetUint(byte bytes[], int off) {
    return ((bytes[off + 3] & 0xFF) << 24) | ((bytes[off + 2] & 0xFF) << 16) | ((bytes[off + 1] & 0xFF) << 8)
        | (bytes[off] & 0xFF);
  }

  // created by guich to handle all types of modern bitmaps,
  private static final int BI_RGB = 0;
  private static final int BI_RLE8 = 1;

  private void ImageLoadBMPCompressed(byte[] p, int length) throws ImageException {
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

    if (p[0] != 'B' || p[1] != 'M') {
      throw new ImageException("Error in Image: not a bmp file!");
    }
    bitmapOffset = inGetUint(p, 10);
    infoSize = inGetUint(p, 14);
    if (infoSize != 40) {
      throw new ImageException("Error in Image: old style bmp");
    }
    this.width = inGetUint(p, 18);
    this.height = inGetUint(p, 22);
    if (this.width > 65535 || this.height > 65535 || this.width <= 0 || this.height <= 0) {
      throw new ImageException("Error in Image: bad width/height");
    }
    int bmpBPP = inGetUint16(p, 28);
    compression = inGetUint(p, 30);
    /* imageSize = */inGetUint(p, 34);
    usedColors = inGetUint(p, 46);
    if (usedColors == 0 && bmpBPP <= 8) {
      usedColors = 1 << bmpBPP;
    }

    colorTable = bmpBPP >= 16 ? null : new int[1 << bmpBPP]; // guich@340_59: in some bitmaps, colorsUsed may be
    // smaller than the number of possible colors, but the bitmap may still
    // have indexes greater than the  usedColors, thus making the bitmap
    // appear black. To avoid this, we always allocate 1<<BPP
    // Read the bitmap's color table
    for (int i = 0, j = 54; i < usedColors; i++, j += 4) {
      colorTable[i] = (inGetUint(p, j) & 0xFFFFFF);
    }

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
    this.pixels = new int[this.height * this.width];

    // Read the pixels from the stream based on the compression type directly into the selected offscreen image
    if (compression == BI_RGB) {
      readRGB(this.width, this.height, bmpBPP, p, bitmapOffset);
    } else if (bmpBPP == 16) {
      throw new ImageException("16-bpp BMP compressed RLE images is not supported! Use 24-bpp instead.");
    } else {
      readRLE(this.width, this.height, p, bitmapOffset, compression == BI_RLE8);
    }
    setTransparentColor(Color.WHITE); // every bmp image has white as default transparent color
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
  private void imageLoad(byte[] input, int len) throws ImageException {
      Image loaded = new ImageLoader().load(input, len);
      if (loaded != null) {
        int fc = loaded.frameCount;
        loaded.frameCount = 1; // guich@tc100b5: cannot be 0
        this.copyFrom(loaded);
        if (fc > 0) {
          this.comment = loaded.comment == null ? "FC=" + fc : loaded.comment;
        }
      }
  }

  static class ImageLoader {
    private int transparentColor = -3;

    private int[] convertBufferedImageToPixels(BufferedImage image, int[] pixelsOut, final int width,
        final int height) {
      int[] result = pixelsOut != null ? pixelsOut : new int[width * height];

      final java.awt.image.ColorModel colorModel = image.getColorModel();
      int index;
      if (transparentColor == -3) { // guich@tc130: not already changed?
        if ((colorModel instanceof java.awt.image.IndexColorModel)
            && (-1 != (index = ((java.awt.image.IndexColorModel) colorModel).getTransparentPixel()))) {
          transparentColor = colorModel.getRGB(index & 0xFF) & 0xFFFFFF;
        }
      }
      if (transparentColor >= 0) {
        // fill all pixels with the transparent color
        Convert.fill(result, 0, result.length, transparentColor | 0xFF000000);
      }
      image.getRGB(0, 0, image.getWidth(), image.getHeight(), result, 0, width);

      return result;
    }

    private static final short readShort(byte buf[], int off) {
      return (short) ((buf[off] & 0xFF) | ((buf[off + 1] & 0xFF) << 8));
    }

    public ImageLoader() {
    }

    /**
     * Grab frames from the image <code>img</code>
     *
     * @param input the input stream where the image to retrieve the image data
     *              comes from
     * @throws ImageException
     */
    public Image load(byte[] input, int len) throws ImageException {
      Image image = null;
      final List<Image> frames = new ArrayList<>(5);
      try {
        final ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(input, 0, len));
        final ImageReader reader = ImageIO.getImageReaders(stream).next();
        reader.setInput(stream);

        if (new String(input, 0, 3).equals("GIF")) {
          /*
           * The width and height of each frame may differ, so we must first get the
           * dimensions of the entire file.
           */
          final int width = readShort(input, 6);
          final int height = readShort(input, 8);

          try {
            /* final int count = reader.getNumImages(true); */
            for (int index = 0; /* index < count */; index++) {
              final BufferedImage frame = reader.read(index);
              image = new Image(width, height, 1, true);
              image.pixels = convertBufferedImageToPixels(frame, image.pixels, width, height);
              frames.add(image);
            }
          } catch (IndexOutOfBoundsException e) {
            /*
             * Getting the number of frames may require a full scan of the file. It's better
             * just to keep reading and stop at the exception.
             */
          }
          image = joinImages(frames);
        } else {
          final BufferedImage frame = reader.read(0);
          final int width = frame.getWidth();
          final int height = frame.getHeight();

          image = new Image(width, height, 1, true);
          if (new String(input, 1, 3).equals("PNG")) {
            fillPNGInformations(input, image);
          }
          image.pixels = convertBufferedImageToPixels(frame, image.pixels, width, height);
        }
      } catch (java.io.IOException e) {
        // should never happen
        e.printStackTrace();
      }
      return image;
    }

    private void fillPNGInformations(byte[] input, Image img) throws ImageException {
      // a shame that Java doesn't support retrieving the comments!
      boolean useAlpha = false;
      boolean isSupported = true;

      byte[] bytes = new byte[4];
      int colorType = 0;
      try {
        ByteArrayStream bas = new ByteArrayStream(input);
        DataStream ds = new DataStream(bas);
        ds.skipBytes(8);
        int pltePos = -1;
        int plteLen = -1;
        while (true) {
          int len = ds.readInt();
          ds.readBytes(bytes);
          String id = new String(bytes);
          if (id.equals("IHDR")) {
            ds.skipBytes(9);
            colorType = ds.readByte();
            bas.skipBytes(-10);
            useAlpha = colorType == 4 || colorType == 6;
            isSupported = colorType != 4;
          } else if (id.equals("PLTE")) // guich@tc100b5_4
          {
            pltePos = bas.getPos();
            plteLen = len;
          } else if (id.equals("tRNS")) // guich@tc100b5_4
          {
            switch (len) {
              case 6: // RGB
                transparentColor = Color.getRGBEnsureRange(ds.readUnsignedShort(), ds.readUnsignedShort(),
                    ds.readUnsignedShort());
                bas.skipBytes(-6);
                break;
              case 256: // palettized? find the color that is transparent (0)
                if (colorType == 3) {
                  useAlpha = true;
                }
                for (int i = 0, pos = bas.getPos(); i < 256; i++, pos++) {
                  if (input[pos] == 0) {
                    if (plteLen == 768) {
                      transparentColor = Color.getRGB(input[pltePos + i * 3] & 0xFF, input[pltePos + i * 3 + 1] & 0xFF,
                          input[pltePos + i * 3 + 2] & 0xFF);
                    }
                    break;
                  }
                }
                break;
            }
          } else if (id.equals("IEND")) {
            break;
          } else if (id.equals("tEXt")) {
            String type = ds.readCString();
            if (type.equals("Comment")) {
              bytes = new byte[len - type.length() - 1];
              ds.readBytes(bytes);
              img.comment = new String(bytes);
            } else {
              bas.skipBytes(-type.length() - 1); // guich@tc100b5_31: go back if its not our comment
            }
          }
          ds.skipBytes(len + 4); // skip data and crc
        }
        if (!useAlpha && transparentColor != -3) {
          img.setTransparentColor(transparentColor);
        }
      } catch (Exception e) {
      }

      if (!isSupported) {
        throw new ImageException(
            "TotalCross does not support grayscale+alpha PNG images. Save the image as color (24 bpp).");
      }
    }

    private Image joinImages(List<Image> images) throws ImageException {
      int n = images.size();
      Image resultImage = images.get(n - 1);
      if (n > 1) {
          int totalW = 0;
          int totalH = resultImage.height;
          for (int i = 0; i < n; i++) {
            totalW += images.get(i).width;
          }
          Image temp = new Image(totalW, totalH, 1, true);
          temp.frameCount = n;
          temp.comment = resultImage.comment;
          int[] dest = (int[]) temp.pixels;
          int xx = 0;
          for (int i = 0; i < n; i++) {
            Image img = images.get(i);
            int[] src = (int[]) img.pixels;
            int w = img.width;
            for (int yy = 0; yy < totalH; yy++) {
              Vm.arrayCopy(src, yy * w, dest, xx + yy * totalW, w);
            }
            xx += w;
          }
          resultImage = temp;
      }
      return resultImage;
    }
  }

  /** Deploy replacement for the JavaSE-only image reader. */
  static class ImageLoader4D {
    public ImageLoader4D() {
    }

    public Image load(byte[] input, int len) {
      return null;
    }
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
  final public Image getFrameInstance(int frame) throws ImageException {
    return deferFrameSelection(normalizedFrame(frame));
  }

  /** Applies the given color r,g,b values to all pixels of this image, 
   * preserving the transparent color and alpha channel, if set.
   * @param color The color to be applied
   * @since TotalCross 1.12
   */
  final public void applyColor(int color) // guich@tc112_24
  {
    if (pipeline != null) {
      deferInPlaceMutation(ImagePipeline.APPLY_COLOR, color, 0, 0, 0);
      return;
    }
    materializeCanonicalUnchecked();
    applyColorEager(color);
  }

  private void applyColorEager(int color)
  {
    if (!Settings.onJavaSE) {
      applyColorNative(color);
      return;
    }
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

  @ReplacedByNativeOnDeploy
  private void applyColorNative(int color) {
  }

  /** Returns a smooth scaled instance of this image with a fixed aspect ratio
   * based on the given resolution (which is the resolution that you used to MAKE the image). The target size is computed as 
   * <code>image_size*min(screen_size)/original_resolution</code>
   * @param originalRes The original resolution that the image was developed for. Its a good idea to create images for 320x320 and then scale them down.
   * @since TotalCross 1.12
   */
  final public Image smoothScaledFromResolution(int originalRes) throws ImageException // guich@tc112_23
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
    if (!(o instanceof Image)) {
      return false;
    }
    materializeCanonicalUnchecked();
    Image other = (Image) o;
    other.materializeCanonicalUnchecked();
    return nativeEquals(other);
  }

  private boolean nativeEquals(Image other) {
    if (!Settings.onJavaSE) {
      return nativeEqualsNative(other);
    }
    Image img = other;
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

  @ReplacedByNativeOnDeploy
  private boolean nativeEqualsNative(Image other) {
    return false;
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
  final public void applyColor2(int color) {
    if (pipeline != null) {
      deferInPlaceMutation(ImagePipeline.APPLY_COLOR2, color, 0, 0, 0);
      return;
    }
    materializeCanonicalUnchecked();
    applyColor2Eager(color);
  }

  private void applyColor2Eager(int color) {
    if (!Settings.onJavaSE) {
      applyColor2Native(color);
      return;
    }
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
  public Image getSmoothScaledInstance(int newWidth, int newHeight, int backColor) throws ImageException // guich@350_22
  {
    return getSmoothScaledInstance(newWidth, newHeight);
  }

  /** @deprecated TotalCross 2 no longer uses the backColor parameter. */
  @Deprecated
  public Image smoothScaledBy(double scaleX, double scaleY, int backColor) throws ImageException // guich@402_6
  {
    return smoothScaledBy(scaleX, scaleY);
  }

  /** @deprecated TotalCross 2 no longer uses the backColor parameter. */
  @Deprecated
  public Image smoothScaledFixedAspectRatio(int newSize, boolean isHeight, int backColor) throws ImageException // guich@402_6
  {
    return smoothScaledFixedAspectRatio(newSize, isHeight);
  }

  /** @deprecated TotalCross 2 no longer uses the backColor parameter. */
  @Deprecated
  final public Image smoothScaledFromResolution(int originalRes, int backColor) throws ImageException // guich@tc112_23
  {
    return smoothScaledFromResolution(originalRes);
  }

  /** Applies the given fade value to r,g,b of this image while preserving the alpha value. */
  public void applyFade(int fadeValue) {
    if (pipeline != null) {
      deferInPlaceMutation(ImagePipeline.APPLY_FADE, fadeValue,
          frameCount > 1 ? normalizedFrame(currentFrame) : 0, 0, 0);
      return;
    }
    materializeCanonicalUnchecked();
    applyFadeEager(fadeValue);
  }

  private void applyFadeEager(int fadeValue) {
    if (!Settings.onJavaSE) {
      applyFadeNative(fadeValue);
      return;
    }
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

  @ReplacedByNativeOnDeploy
  private void applyFadeNative(int fadeValue) {
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
  public Image getCopy() throws ImageException {
    return getFrameInstance(0);
  }

  /** Returns a clipped image from the current position. Note that you must ensure that the values are correct or
   * an exception will be thrown
   */
  public Image getClippedInstance(int x, int y, int w, int h) throws ImageException {
    if (w <= 0 || h <= 0) {
      throw new ImageException("Image dimensions and content scale must be positive.");
    }
    if ((long) w * h > Integer.MAX_VALUE) {
      throw new ImageException("Image dimensions are too large.");
    }
    ImagePipeline previous = pipeline;
    if (previous == null) {
      previous = new ImagePipeline(snapshotRasterSource());
    }
    if (frameCount > 1) {
      previous = previous.append(ImagePipeline.FRAME_SELECT, normalizedFrame(currentFrame), 0, 0, 0,
          previous.width(), previous.height(), previous.logicalWidth(), previous.logicalHeight(), 1,
          previous.width());
    }
    boolean destinationAware = previous.hasGeometricNode();
    double canonicalScale = destinationAware ? 1 : previous.contentScale();
    int physicalWidth = destinationAware ? w : scaledDimension(w, canonicalScale);
    int physicalHeight = destinationAware ? h : scaledDimension(h, canonicalScale);
    ImagePipeline deferred = previous.append(ImagePipeline.CROP, x, y, w, h, physicalWidth, physicalHeight, w, h, 1,
        physicalWidth);
    Image result = new Image();
    result.initializeDeferredCrop(deferred);
    return result;
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

  @ReplacedByNativeOnDeploy
  public static native void nativeResizeJpeg(String inputPath, String outputPath, int maxPixelSize);
  
  private static final int JPEG_SCALE_1_8 = 8;
  private static final int JPEG_SCALE_1_4 = 4;
  private static final int JPEG_SCALE_1_2 = 2;

  private static void validateJpegScaleArguments(int numerator, int denominator) throws ImageException {
    if (numerator <= 0 || denominator <= 0) {
      throw new ImageException(null);
    }
  }

  private static int jpegScaledDimension(int sourceDimension, int scaleNumerator, int scaleDenominator)
      throws ImageException {
    long scaled = (long) sourceDimension * scaleNumerator;
    long dimension = (scaled + scaleDenominator - 1) / scaleDenominator;
    if (dimension <= 0 || dimension > Integer.MAX_VALUE) {
      throw new ImageException("Image dimensions are too large.");
    }
    return (int) dimension;
  }
  private static boolean jpegBestFitFits(int sourceDimension, int scaleDenominator, int targetDimension) {
    long decodedDimension = ((long) sourceDimension + scaleDenominator - 1) / scaleDenominator;
    return decodedDimension >= targetDimension;
  }

  private static int jpegTargetDecodeScaleDenominator(int sourceWidth, int sourceHeight,
      int targetWidth, int targetHeight) {
    if (jpegBestFitFits(sourceWidth, JPEG_SCALE_1_8, targetWidth)
        && jpegBestFitFits(sourceHeight, JPEG_SCALE_1_8, targetHeight)) {
      return JPEG_SCALE_1_8;
    }
    if (jpegBestFitFits(sourceWidth, JPEG_SCALE_1_4, targetWidth)
        && jpegBestFitFits(sourceHeight, JPEG_SCALE_1_4, targetHeight)) {
      return JPEG_SCALE_1_4;
    }
    if (jpegBestFitFits(sourceWidth, JPEG_SCALE_1_2, targetWidth)
        && jpegBestFitFits(sourceHeight, JPEG_SCALE_1_2, targetHeight)) {
      return JPEG_SCALE_1_2;
    }
    return 1;
  }

  private static int jpegBestFitScaleDenominatorForDimension(int sourceDimension, int targetDimension) {
    if (jpegBestFitFits(sourceDimension, JPEG_SCALE_1_8, targetDimension)) {
      return JPEG_SCALE_1_8;
    }
    if (jpegBestFitFits(sourceDimension, JPEG_SCALE_1_4, targetDimension)) {
      return JPEG_SCALE_1_4;
    }
    if (jpegBestFitFits(sourceDimension, JPEG_SCALE_1_2, targetDimension)) {
      return JPEG_SCALE_1_2;
    }
    return 1;
  }

  private static int jpegBestFitScaleDenominator(int sourceWidth, int sourceHeight,
      int targetWidth, int targetHeight) {
    if ((long) targetWidth * sourceHeight <= (long) targetHeight * sourceWidth) {
      return jpegBestFitScaleDenominatorForDimension(sourceWidth, targetWidth);
    }
    return jpegBestFitScaleDenominatorForDimension(sourceHeight, targetHeight);
  }

  private static EncodedImageSource captureJpegSource(String path)
      throws java.io.IOException, ImageException {
    if (path == null) {
      throw new java.io.IOException();
    }
    byte[] encoded = Vm.getFile(path);
    if (encoded == null) {
      try (File file = new File(path, File.READ_ONLY)) {
        encoded = file.read();
      }
    }
    EncodedImageSource source = EncodedImageSource.fromOwnedBytes(encoded);
    if (source.getFormat() != ImageEncodedStructure.Format.JPEG) {
      throw new ImageException(null);
    }
    return source;
  }

  private static Image imageForCapturedSource(String path, EncodedImageSource source) {
    Image image = new Image();
    image.path = path;
    image.initializeDeferred(source);
    return image;
  }

  @ReplacedByNativeOnDeploy
  public static Image getJpegBestFit(String path, int targetWidth, int targetHeight)
      throws java.io.IOException, ImageException {
    validateJpegScaleArguments(targetWidth, targetHeight);
    EncodedImageSource source = captureJpegSource(path);

    final int scaleDenominator = jpegBestFitScaleDenominator(
        source.getIntrinsicWidth(), source.getIntrinsicHeight(), targetWidth, targetHeight);
    final int scaledWidth = jpegScaledDimension(source.getIntrinsicWidth(), 1, scaleDenominator);
    final int scaledHeight = jpegScaledDimension(source.getIntrinsicHeight(), 1, scaleDenominator);
    Image image = imageForCapturedSource(path, source);
    Image result = image.getSmoothScaledInstance(scaledWidth, scaledHeight);
    result.materializeCanonicalChecked();
    return result;
  }

  @ReplacedByNativeOnDeploy
  public static Image getJpegScaled(String path, int scaleNumerator, int scaleDenominator)
      throws java.io.IOException, ImageException {
    validateJpegScaleArguments(scaleNumerator, scaleDenominator);
    EncodedImageSource source = captureJpegSource(path);

    final int scaledWidth = jpegScaledDimension(source.getIntrinsicWidth(), scaleNumerator, scaleDenominator);
    final int scaledHeight = jpegScaledDimension(source.getIntrinsicHeight(), scaleNumerator, scaleDenominator);
    Image image = imageForCapturedSource(path, source);
    Image result = image.getSmoothScaledInstance(scaledWidth, scaledHeight);
    result.materializeCanonicalChecked();
    return result;
  }

  @Override
  public int hashCode() {
    materializeCanonicalUnchecked();
    if (hashCode != 0) {
      return hashCode;
    }
    int[] p = (int[]) (frameCount == 1 ? this.pixels : this.pixelsOfAllFrames);
    if (p != null) {
      try {
        /*
         * If bigger than 64x64, scale down for faster hashing.
         * getScaledInstance is faster because it has native implementation.
         */
        if (p.length > 4096) {
          Image i = eagerScaledInstance(64, 64);
          return Arrays.hashCode(i.pixels);
        }
      } catch (ImageException e) {
        e.printStackTrace();
      }
      return Arrays.hashCode(p);
    }
    return super.hashCode();
  }
}
