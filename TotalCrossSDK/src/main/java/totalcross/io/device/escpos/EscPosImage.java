// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.escpos;

import java.io.IOException;
import java.io.OutputStream;

import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * Printable ESC/POS image representation.
 *
 * @author Fábio Sobral
 * @since TotalCross 4.2.0
 */
public class EscPosImage implements EscPosPrintObject {

  private Image image;
  private byte align = EscPosConstants.ALIGN_LEFT;
  private boolean dither = true;
  private int width;
  private Image toPrint;

  public EscPosImage(Image image) {
    this.image = image;
  }

  public EscPosImage align(byte align) {
    this.align = align;
    return this;
  }

  public EscPosImage dither(boolean enabled) {
    this.dither = enabled;
    return this;
  }

  public EscPosImage width(int width) throws ImageException {
    this.width = width;
    if (image.getWidth() > width) {
      toPrint = image.smoothScaledFixedAspectRatio(width, false);
    }
    return this;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    if (toPrint == null) {
      toPrint = image;
    }
    this.printImage(out, toPrint, width, toPrint.getHeight(), align, dither, false);
  }

  private void printImage(
      OutputStream out,
      Image image,
      int width,
      int height,
      byte align,
      boolean dither,
      boolean crop)
      throws IOException {
    int[] argb = new int[width * height];
    byte[] row = new byte[width * 4];

    for (int i = 0; i < height; i++) {
      image.getPixelRow(row, i);
      for (int j = 0; j < width; j++) {
        int k = j << 2;
        int r = row[k++] & 0xFF;
        int g = row[k++] & 0xFF;
        int b = row[k++] & 0xFF;
        // convert to grayscale
        int color = r * 19 + g * 38 + b * 7 >> 6 & 0xFF;
        argb[(i * width) + j] = color;
      }
    }

    byte[] buf = null;
    int bufOffs = 0;
    if ((align < 0) || (align > 2)) {
      throw new IllegalArgumentException("The align is illegal");
    }
    if ((width < 1) || (height < 1)) {
      throw new IllegalArgumentException("The size of image is illegal");
    }
    if (dither) {
      ditherImageByFloydSteinberg(argb, width, height);
    }
    if (crop) {
      height = cropImage(argb, width, height);
    }
    buf = new byte[width * 3 + 9];
    synchronized (this) {
      // lineSpacing
      bufOffs = 0;
      buf[(bufOffs++)] = 27;
      buf[(bufOffs++)] = 51;
      buf[(bufOffs++)] = 24;
      out.write(buf, 0, bufOffs);

      bufOffs = 0;
      // align
      buf[(bufOffs++)] = 27;
      buf[(bufOffs++)] = 97;
      buf[(bufOffs++)] = (align);

      // esc *
      buf[(bufOffs++)] = 27;
      buf[(bufOffs++)] = 42;
      buf[(bufOffs++)] = 33;
      buf[(bufOffs++)] = ((byte) (width % 256));
      buf[(bufOffs++)] = ((byte) (width >> 8));
      buf[(buf.length - 1)] = 10;

      int j = 0;
      for (int offs = 0; j < height; j++) {
        if ((j > 0) && (j % 24 == 0)) {
          out.write(buf);
          for (int i = bufOffs; i < buf.length - 1; i++) {
            buf[i] = 0;
          }
        }
        int k = (j % 24 >> 3);
        for (int i = 0; i < width; offs++) {
          int tmp331_330 = (bufOffs + i * 3 + k);
          byte[] tmp331_313 = buf;
          tmp331_313[tmp331_330] =
              ((byte)
                  (tmp331_313[tmp331_330]
                      | (byte) ((argb[offs] < 128 ? 1 : 0) << 7 - (j & (8 - 1)))));
          i++;
        }
      }

      out.write(buf);
      out.write(new byte[1024]);
    }
  }

  private static void ditherImageByFloydSteinberg(int[] grayscale, int width, int height) {
    int stopXM1 = width - 1;
    int stopYM1 = height - 1;
    int[] coef = {3, 5, 1};

    int y = 0;
    for (int offs = 0; y < height; y++) {
      for (int x = 0; x < width; offs++) {
        int v = grayscale[offs];
        int error;
        if (v < 128) {
          grayscale[offs] = 0;
          error = v;
        } else {
          grayscale[offs] = 255;
          error = v - 255;
        }
        if (x != stopXM1) {
          int ed = grayscale[(offs + 1)] + (error * 7 >> 4);
          ed = ed > 255 ? 255 : ed < 0 ? 0 : ed;
          grayscale[(offs + 1)] = ed;
        }
        if (y != stopYM1) {
          int i = -1;
          for (int j = 0; i <= 1; j++) {
            if ((x + i >= 0) && (x + i < width)) {
              int ed = grayscale[(offs + width + i)] + (error * coef[j] >> 4);
              ed = ed > 255 ? 255 : ed < 0 ? 0 : ed;
              grayscale[(offs + width + i)] = ed;
            }
            i++;
          }
        }
        x++;
      }
    }
  }

  private int cropImage(int[] grayscale, int width, int height) {
    int length = width * height;
    int offset = 0;
    while (offset < length) {
      if (grayscale[(length - 1 - offset)] < 128) {
        break;
      }
      offset++;
    }
    int newHeight = height - offset / width;

    return newHeight;
  }
}
