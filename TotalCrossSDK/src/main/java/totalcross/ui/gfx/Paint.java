// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

/**
 * Describes low-level drawing parameters in a way similar to Skia's paint state.
 * <p>
 * This class is intentionally mutable so a single instance can be configured and
 * reused across multiple drawing operations.
 */
public final class Paint {
  /**
   * Stroke and fill styles.
   */
  public static final class Style {
    /** Fills the geometry interior. */
    public static final int FILL = 0;
    /** Draws only the geometry outline. */
    public static final int STROKE = 1;
    /** Fills and also draws the geometry outline. */
    public static final int STROKE_AND_FILL = 2;

    private Style() {
    }
  }

  /**
   * Stroke cap styles.
   */
  public static final class Cap {
    /** Flat cap that ends exactly at the segment boundary. */
    public static final int BUTT = 0;
    /** Rounded cap centered at the segment boundary. */
    public static final int ROUND = 1;
    /** Square cap that extends half the stroke width beyond the segment boundary. */
    public static final int SQUARE = 2;

    private Cap() {
    }
  }

  /**
   * Stroke join styles.
   */
  public static final class Join {
    /** Sharp corner limited by the stroke miter. */
    public static final int MITER = 0;
    /** Rounded corner between segments. */
    public static final int ROUND = 1;
    /** Beveled corner between segments. */
    public static final int BEVEL = 2;

    private Join() {
    }
  }

  private int color;
  private int style;
  private double strokeWidth;
  private int strokeCap;
  private int strokeJoin;
  private double strokeMiter;
  private boolean antiAlias;
  private boolean dither;
  private PathEffect pathEffect;

  /**
   * Creates a paint with defaults equivalent to a freshly constructed SkPaint.
   */
  public Paint() {
    reset();
  }

  /**
   * Creates a paint copying the state from another instance.
   *
   * @param source the paint to copy from
   * @throws NullPointerException if {@code source} is {@code null}
   */
  public Paint(Paint source) {
    set(source);
  }

  /**
   * Restores this paint to its default state.
   *
   * @return this paint
   */
  public Paint reset() {
    color = 0xFF000000;
    style = Style.FILL;
    strokeWidth = 0;
    strokeCap = Cap.BUTT;
    strokeJoin = Join.MITER;
    strokeMiter = 4;
    antiAlias = false;
    dither = false;
    pathEffect = null;
    return this;
  }

  /**
   * Copies the state from another paint.
   *
   * @param source the paint to copy from
   * @return this paint
   * @throws NullPointerException if {@code source} is {@code null}
   */
  public Paint set(Paint source) {
    if (source == null) {
      throw new NullPointerException("source");
    }
    color = source.color;
    style = source.style;
    strokeWidth = source.strokeWidth;
    strokeCap = source.strokeCap;
    strokeJoin = source.strokeJoin;
    strokeMiter = source.strokeMiter;
    antiAlias = source.antiAlias;
    dither = source.dither;
    pathEffect = source.pathEffect;
    return this;
  }

  /**
   * Returns the ARGB color used by this paint.
   */
  public int getColor() {
    return color;
  }

  /**
   * Sets the ARGB color used by this paint.
   *
   * @param color the color in {@code 0xAARRGGBB} format
   * @return this paint
   */
  public Paint setColor(int color) {
    this.color = color;
    return this;
  }

  /**
   * Sets the color using separate alpha, red, green and blue channels.
   *
   * @param alpha the alpha channel in the range 0-255
   * @param red the red channel in the range 0-255
   * @param green the green channel in the range 0-255
   * @param blue the blue channel in the range 0-255
   * @return this paint
   * @throws IllegalArgumentException if any component is outside the range 0-255
   */
  public Paint setARGB(int alpha, int red, int green, int blue) {
    validateChannel("alpha", alpha);
    validateChannel("red", red);
    validateChannel("green", green);
    validateChannel("blue", blue);
    this.color = (alpha << 24) | Color.getRGB(red, green, blue);
    return this;
  }

  /**
   * Returns the alpha channel in the range 0-255.
   */
  public int getAlpha() {
    return (color >>> 24) & 0xFF;
  }

  /**
   * Sets the alpha channel in the range 0-255.
   *
   * @param alpha the alpha channel value
   * @return this paint
   * @throws IllegalArgumentException if {@code alpha} is outside the range 0-255
   */
  public Paint setAlpha(int alpha) {
    validateChannel("alpha", alpha);
    this.color = (color & 0x00FFFFFF) | (alpha << 24);
    return this;
  }

  /**
   * Returns the drawing style.
   */
  public int getStyle() {
    return style;
  }

  /**
   * Sets the drawing style.
   *
   * @param style one of {@link Style#FILL}, {@link Style#STROKE}, or {@link Style#STROKE_AND_FILL}
   * @return this paint
   * @throws IllegalArgumentException if {@code style} is invalid
   */
  public Paint setStyle(int style) {
    if (!isValidStyle(style)) {
      throw new IllegalArgumentException("Invalid paint style: " + style);
    }
    this.style = style;
    return this;
  }

  /**
   * Returns the stroke width.
   */
  public double getStrokeWidth() {
    return strokeWidth;
  }

  /**
   * Sets the stroke width.
   *
   * @param strokeWidth the stroke width, which must be non-negative
   * @return this paint
   * @throws IllegalArgumentException if {@code strokeWidth} is negative
   */
  public Paint setStrokeWidth(double strokeWidth) {
    validateNonNegative("strokeWidth", strokeWidth);
    this.strokeWidth = strokeWidth;
    return this;
  }

  /**
   * Returns the stroke cap.
   */
  public int getStrokeCap() {
    return strokeCap;
  }

  /**
   * Sets the stroke cap.
   *
   * @param strokeCap one of {@link Cap#BUTT}, {@link Cap#ROUND}, or {@link Cap#SQUARE}
   * @return this paint
   * @throws IllegalArgumentException if {@code strokeCap} is invalid
   */
  public Paint setStrokeCap(int strokeCap) {
    if (!isValidCap(strokeCap)) {
      throw new IllegalArgumentException("Invalid stroke cap: " + strokeCap);
    }
    this.strokeCap = strokeCap;
    return this;
  }

  /**
   * Returns the stroke join.
   */
  public int getStrokeJoin() {
    return strokeJoin;
  }

  /**
   * Sets the stroke join.
   *
   * @param strokeJoin one of {@link Join#MITER}, {@link Join#ROUND}, or {@link Join#BEVEL}
   * @return this paint
   * @throws IllegalArgumentException if {@code strokeJoin} is invalid
   */
  public Paint setStrokeJoin(int strokeJoin) {
    if (!isValidJoin(strokeJoin)) {
      throw new IllegalArgumentException("Invalid stroke join: " + strokeJoin);
    }
    this.strokeJoin = strokeJoin;
    return this;
  }

  /**
   * Returns the stroke miter limit.
   */
  public double getStrokeMiter() {
    return strokeMiter;
  }

  /**
   * Sets the stroke miter limit.
   *
   * @param strokeMiter the miter limit, which must be non-negative
   * @return this paint
   * @throws IllegalArgumentException if {@code strokeMiter} is negative
   */
  public Paint setStrokeMiter(double strokeMiter) {
    validateNonNegative("strokeMiter", strokeMiter);
    this.strokeMiter = strokeMiter;
    return this;
  }

  /**
   * Returns whether anti-aliasing is enabled.
   */
  public boolean isAntiAlias() {
    return antiAlias;
  }

  /**
   * Enables or disables anti-aliasing.
   *
   * @param antiAlias whether anti-aliasing should be enabled
   * @return this paint
   */
  public Paint setAntiAlias(boolean antiAlias) {
    this.antiAlias = antiAlias;
    return this;
  }

  /**
   * Returns whether dithering is enabled.
   */
  public boolean isDither() {
    return dither;
  }

  /**
   * Enables or disables dithering.
   *
   * @param dither whether dithering should be enabled
   * @return this paint
   */
  public Paint setDither(boolean dither) {
    this.dither = dither;
    return this;
  }

  /**
   * Returns the path effect applied to geometry drawn with this paint, or {@code null} if none is configured.
   */
  public PathEffect getPathEffect() {
    return pathEffect;
  }

  /**
   * Sets the path effect applied to geometry drawn with this paint.
   *
   * @param pathEffect the path effect to use, or {@code null} to clear it
   * @return this paint
   */
  public Paint setPathEffect(PathEffect pathEffect) {
    this.pathEffect = pathEffect;
    return this;
  }

  /**
   * Returns whether the given value is a valid paint style.
   */
  public static boolean isValidStyle(int style) {
    return style == Style.FILL || style == Style.STROKE || style == Style.STROKE_AND_FILL;
  }

  /**
   * Returns whether the given value is a valid stroke cap.
   */
  public static boolean isValidCap(int cap) {
    return cap == Cap.BUTT || cap == Cap.ROUND || cap == Cap.SQUARE;
  }

  /**
   * Returns whether the given value is a valid stroke join.
   */
  public static boolean isValidJoin(int join) {
    return join == Join.MITER || join == Join.ROUND || join == Join.BEVEL;
  }

  private static void validateChannel(String name, int value) {
    if (value < 0 || value > 0xFF) {
      throw new IllegalArgumentException(name + " must be in the range 0-255: " + value);
    }
  }

  private static void validateNonNegative(String name, double value) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " must be >= 0: " + value);
    }
  }
}
