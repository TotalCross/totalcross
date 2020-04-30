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
package totalcross.io.device.printer;

import totalcross.io.ByteArrayStream;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/** Image class that can be used to save 1-bpp images and print on monochromatic printers.
 * Only black pixels are printed, non-black are ignored. 
 */
public class MonoImage extends Image {
  /** If set to true, the image's width is limited to 384 (double density) or 192 (single density).
   * If false, it will use the actual image's width. */
  public boolean doClip = true;

  /** Makes a copy of the given image. */
  public MonoImage(Image other) throws ImageException {
    super(other.getWidth(), other.getHeight());
    getGraphics().drawImage(other, 0, 0);
  }

  /** Creates a MonoImage read from the given Stream. */
  public MonoImage(Stream s) throws ImageException, IOException {
    super(s);
  }

  /** Creates a MonoImage based on the given full description. */
  public MonoImage(byte[] fullDescription) throws ImageException {
    super(fullDescription);
  }

  /** Creates a MonoImage with the given width and height.
   * You can draw into it by retrieving the Graphics using img.getGraphics(). 
   */
  public MonoImage(int width, int height) throws ImageException {
    super(width, height);
  }

  /** Creates a MonoImage, loading the given bmp file. */
  public MonoImage(String path) throws ImageException, IOException {
    super(path);
  }

  /** Prints a 1-bpp bitmap from this image. When writting, all non-white pixels are written. */
  protected void printTo(BluetoothPrinter pad) throws IOException {
    int w = width;
    int h = height;
    final int WHITE_PIXEL = -1;
    ByteArrayStream bas = new ByteArrayStream(w * h / 7);
    bas.writeBytes(new byte[] { 27, 64 }); // init
    bas.writeBytes(new byte[] { 0x1D, 0x45, 8 }); // set density 

    byte[] header = { 0x1D, 0x2A, (byte) (w / 8), (byte) 8 };
    byte[] footer = { 0x1D, 0x2F, 0x00 };
    byte[] dots = new byte[w * h];
    byte[] line = new byte[w * 4];
    final byte[] bits = { (byte) 128, 64, 32, 16, 8, 4, 2, 1 };
    byte[] slice = new byte[8];

    for (int i = 0, j = 0; i < h; i++) // get all pixels from image
    {
      getPixelRow(line, i);
      for (int k = 0; k < w; k++) {
        dots[j++] = line[k * 4] == WHITE_PIXEL ? (byte) 0 : (byte) 1;
      }
    }
    for (int offset = 0; offset < h; offset += 64) {
      bas.writeBytes(header);
      for (int x = 0; x < w; x++) {
        for (int k = 0, y = offset; k < 8; k++) {
          slice[k] = 0;
          for (int b = 0; b < 8; b++) {
            int i = (y++ * w) + x;
            if (i < dots.length && dots[i] == 1) {
              slice[k] |= bits[b];
            }
          }
        }
        bas.writeBytes(slice);
      }
      bas.writeBytes(footer);
    }
    pad.write(bas.getBuffer(), 0, bas.getPos());
  }
}
