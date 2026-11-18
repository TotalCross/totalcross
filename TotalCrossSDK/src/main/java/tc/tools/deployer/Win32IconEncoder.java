// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.deployer;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import totalcross.ui.image.Image;

/**
 * Writes an adaptive-color 32x32x8 DIB into the existing Win32 launcher icon slot.
 * No PE resources are resized, moved or added.
 */
final class Win32IconEncoder {
  private static final int SIZE = 32;
  private static final int HEADER = 40;
  private static final int COLORS = 256;
  private static final int PALETTE_BYTES = COLORS * 4;
  private static final int PIXEL_OFFSET = HEADER + PALETTE_BYTES;
  private static final int PIXEL_BYTES = SIZE * SIZE;
  private static final int MASK_OFFSET = PIXEL_OFFSET + PIXEL_BYTES;
  private static final int MASK_BYTES = SIZE * 4; // 1-bit AND mask, 32-bit-aligned rows
  private static final int RESOURCE_BYTES = MASK_OFFSET + MASK_BYTES;

  private Win32IconEncoder() {
  }

  static void write(byte[] executable, int iconOffset, Image source) {
    write(executable, iconOffset, source.getPixels(), source.getWidth(), source.getHeight());
  }

  // Package-private to allow testing the resource writer without initializing deploy settings.
  static void write(byte[] executable, int iconOffset, int[] pixels, int width, int height) {
    validateResource(executable, iconOffset);
    if (pixels == null || width <= 0 || height <= 0
        || (long) width * height > pixels.length) {
      throw new IllegalArgumentException("Invalid Win32 icon source image");
    }

    boolean hasSourceTransparency = false;
    for (int i = 0; i < width * height; i++) {
      if ((pixels[i] >>> 24) != 255) {
        hasSourceTransparency = true;
        break;
      }
    }

    int[] scaled = scale(pixels, width, height);
    boolean[] transparent = new boolean[PIXEL_BYTES];
    int visibleCount = 0;
    for (int i = 0; i < PIXEL_BYTES; i++) {
      int argb = scaled[i];
      int a = argb >>> 24;
      // Historically fully opaque white backgrounds were used as a transparency key.
      // Apply that compatibility rule only when the source has no real alpha.
      boolean whiteKey = !hasSourceTransparency
          && ((argb >>> 16) & 255) >= 247
          && ((argb >>> 8) & 255) >= 247
          && (argb & 255) >= 247;
      transparent[i] = a < 128 || whiteKey;
      if (!transparent[i]) {
        visibleCount++;
      }
    }

    int[] visible = new int[visibleCount];
    for (int i = 0, p = 0; i < PIXEL_BYTES; i++) {
      if (!transparent[i]) {
        visible[p++] = scaled[i] & 0xFFFFFF;
      }
    }
    int[] palette = createPalette(visible);
    byte[] indexed = quantize(scaled, transparent, palette);

    // Windows RGBQUAD entries use B, G, R, reserved (in this order).
    int paletteOffset = iconOffset + HEADER;
    for (int i = 0; i < COLORS; i++) {
      int rgb = i < palette.length ? palette[i] : 0;
      int p = paletteOffset + i * 4;
      executable[p] = (byte) rgb;
      executable[p + 1] = (byte) (rgb >>> 8);
      executable[p + 2] = (byte) (rgb >>> 16);
      executable[p + 3] = 0;
    }

    // DIB pixel rows and AND mask rows are bottom-up.
    int pixelOffset = iconOffset + PIXEL_OFFSET;
    int maskOffset = iconOffset + MASK_OFFSET;
    Arrays.fill(executable, maskOffset, maskOffset + MASK_BYTES, (byte) 0);
    for (int y = 0; y < SIZE; y++) {
      int dstRow = SIZE - 1 - y;
      for (int x = 0; x < SIZE; x++) {
        int src = y * SIZE + x;
        executable[pixelOffset + dstRow * SIZE + x] = indexed[src];
        if (transparent[src]) {
          executable[maskOffset + dstRow * 4 + x / 8] |= (byte) (0x80 >>> (x & 7));
        }
      }
    }
  }

  private static void validateResource(byte[] executable, int offset) {
    if (executable == null || offset < 0 || offset > executable.length - RESOURCE_BYTES
        || readInt(executable, offset) != HEADER
        || readInt(executable, offset + 4) != SIZE
        || readInt(executable, offset + 8) != SIZE * 2
        || readShort(executable, offset + 12) != 1
        || readShort(executable, offset + 14) != 8
        || readInt(executable, offset + 16) != 0) {
      throw new IllegalArgumentException("Launcher icon is not a 32x32x8 uncompressed DIB");
    }
  }

  private static int readShort(byte[] data, int offset) {
    return (data[offset] & 255) | ((data[offset + 1] & 255) << 8);
  }

  private static int readInt(byte[] data, int offset) {
    return readShort(data, offset) | (readShort(data, offset + 2) << 16);
  }

  private static int[] scale(int[] pixels, int width, int height) {
    // ARGB_PRE is important: colors in fully transparent pixels must not bleed
    // into the edges when a large icon is downscaled.
    BufferedImage current = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
    current.setRGB(0, 0, width, height, pixels, 0, width);
    while (current.getWidth() > 2 * SIZE || current.getHeight() > 2 * SIZE) {
      current = resample(current, Math.max(SIZE, current.getWidth() / 2),
          Math.max(SIZE, current.getHeight() / 2));
    }
    if (current.getWidth() != SIZE || current.getHeight() != SIZE) {
      current = resample(current, SIZE, SIZE);
    }
    return current.getRGB(0, 0, SIZE, SIZE, null, 0, SIZE);
  }

  private static BufferedImage resample(BufferedImage input, int width, int height) {
    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics2D g = output.createGraphics();
    try {
      g.setComposite(AlphaComposite.Src);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_RENDERING,
          RenderingHints.VALUE_RENDER_QUALITY);
      g.drawImage(input, 0, 0, width, height, null);
    } finally {
      g.dispose();
    }
    return output;
  }

  private static int[] createPalette(int[] colors) {
    if (colors.length == 0) {
      return new int[] { 0xFFFFFF };
    }
    List<ColorBox> boxes = new ArrayList<ColorBox>();
    boxes.add(new ColorBox(colors));
    while (boxes.size() < COLORS) {
      int best = -1;
      long bestScore = 0;
      for (int i = 0; i < boxes.size(); i++) {
        long score = boxes.get(i).splitScore();
        if (score > bestScore) {
          best = i;
          bestScore = score;
        }
      }
      if (best < 0) {
        break;
      }
      ColorBox[] halves = boxes.remove(best).split();
      boxes.add(halves[0]);
      boxes.add(halves[1]);
    }
    int[] palette = new int[boxes.size()];
    for (int i = 0; i < boxes.size(); i++) {
      palette[i] = boxes.get(i).average();
    }
    return palette;
  }

  private static byte[] quantize(int[] scaled, boolean[] transparent, int[] palette) {
    byte[] indices = new byte[PIXEL_BYTES];
    double[][] errors = new double[3][PIXEL_BYTES];
    for (int y = 0; y < SIZE; y++) {
      for (int x = 0; x < SIZE; x++) {
        int pos = y * SIZE + x;
        if (transparent[pos]) {
          continue;
        }
        int source = scaled[pos];
        int r = clamp(((source >>> 16) & 255) + errors[0][pos]);
        int g = clamp(((source >>> 8) & 255) + errors[1][pos]);
        int b = clamp((source & 255) + errors[2][pos]);
        int index = nearest(palette, r, g, b);
        indices[pos] = (byte) index;
        int chosen = palette[index];
        diffuse(errors, x, y, r - ((chosen >>> 16) & 255),
            g - ((chosen >>> 8) & 255), b - (chosen & 255));
      }
    }
    return indices;
  }

  private static int clamp(double value) {
    return (int) Math.max(0, Math.min(255, Math.round(value)));
  }

  private static int nearest(int[] palette, int r, int g, int b) {
    int best = 0;
    int minDistance = Integer.MAX_VALUE;
    for (int i = 0; i < palette.length; i++) {
      int c = palette[i];
      int dr = r - ((c >>> 16) & 255);
      int dg = g - ((c >>> 8) & 255);
      int db = b - (c & 255);
      int distance = dr * dr + dg * dg + db * db;
      if (distance < minDistance) {
        minDistance = distance;
        best = i;
      }
    }
    return best;
  }

  private static void diffuse(double[][] errors, int x, int y,
      double er, double eg, double eb) {
    if (x + 1 < SIZE) {
      addError(errors, y * SIZE + x + 1, er, eg, eb, 7.0 / 16);
    }
    if (y + 1 < SIZE) {
      if (x > 0) {
        addError(errors, (y + 1) * SIZE + x - 1, er, eg, eb, 3.0 / 16);
      }
      addError(errors, (y + 1) * SIZE + x, er, eg, eb, 5.0 / 16);
      if (x + 1 < SIZE) {
        addError(errors, (y + 1) * SIZE + x + 1, er, eg, eb, 1.0 / 16);
      }
    }
  }

  private static void addError(double[][] errors, int position,
      double r, double g, double b, double factor) {
    errors[0][position] += r * factor;
    errors[1][position] += g * factor;
    errors[2][position] += b * factor;
  }

  private static final class ColorBox {
    final int[] colors;
    final int[] min = { 255, 255, 255 };
    final int[] max = { 0, 0, 0 };

    ColorBox(int[] colors) {
      this.colors = colors;
      for (int color : colors) {
        for (int c = 0; c < 3; c++) {
          int v = channel(color, c);
          min[c] = Math.min(min[c], v);
          max[c] = Math.max(max[c], v);
        }
      }
    }

    long splitScore() {
      int range = Math.max(max[0] - min[0], Math.max(max[1] - min[1], max[2] - min[2]));
      return colors.length < 2 || range == 0 ? 0 : (long) range * colors.length;
    }

    ColorBox[] split() {
      int channel = 0;
      for (int c = 1; c < 3; c++) {
        if (max[c] - min[c] > max[channel] - min[channel]) {
          channel = c;
        }
      }
      final int component = channel;
      Integer[] sorted = new Integer[colors.length];
      for (int i = 0; i < colors.length; i++) {
        sorted[i] = colors[i];
      }
      Arrays.sort(sorted, new Comparator<Integer>() {
        @Override
        public int compare(Integer a, Integer b) {
          return Integer.compare(channel(a, component), channel(b, component));
        }
      });
      int half = sorted.length / 2;
      int[] left = new int[half];
      int[] right = new int[sorted.length - half];
      for (int i = 0; i < sorted.length; i++) {
        if (i < half) {
          left[i] = sorted[i];
        } else {
          right[i - half] = sorted[i];
        }
      }
      return new ColorBox[] { new ColorBox(left), new ColorBox(right) };
    }

    int average() {
      long r = 0, g = 0, b = 0;
      for (int color : colors) {
        r += (color >>> 16) & 255;
        g += (color >>> 8) & 255;
        b += color & 255;
      }
      int count = colors.length;
      return (((int) ((r + count / 2) / count)) << 16)
          | (((int) ((g + count / 2) / count)) << 8)
          | ((int) ((b + count / 2) / count));
    }

    private static int channel(int color, int index) {
      return (color >>> (16 - index * 8)) & 255;
    }
  }
}
