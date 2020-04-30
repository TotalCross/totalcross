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

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

/** 
 * This class is used to load and release lots of images when used for a book, for example.
 * It loads the images in a thread and release old images to keep memory usage low.
 * Use it with DynImage. See the ImageBook and the Product Store samples.
 */

public class BulkImageLoader extends Thread {
  /** class used to show the Image on screen. It loads the image dynamically. */
  public class DynImage extends Control {
    int idx;

    public DynImage(int idx) {
      this.idx = idx;
    }

    @Override
    public void onPaint(Graphics g) {
      Image img = null;
      try {
        img = getImage(idx);
        if (img != null) {
          double ratio = Math.min((double) width / img.getWidth(), (double) height / img.getHeight());
          double oldH = img.hwScaleH, oldW = img.hwScaleW;
          img.hwScaleH = img.hwScaleW = ratio;
          g.drawImage(img, (width - img.getWidth()) / 2, (height - img.getHeight()) / 2);
          img.hwScaleH = oldH;
          img.hwScaleW = oldW;
        }
      } catch (Exception ioe) {
      }
      if (img == null) {
        // no memory, draw a rect with a x
        g.foreColor = Color.BLACK;
        g.drawRect(0, 0, width, height);
        g.drawLine(0, 0, width, height);
        g.drawLine(width, 0, 0, height);
      }
    }
  }

  public boolean running;
  public int pagesInMemory;
  public int pageSize;
  public int current = -1;
  public String[] arqs;
  public Image[] loaded;
  private int lastIni, lastEnd;
  private IOException ex;
  private String imageFolder;

  public BulkImageLoader(int pagesInMemory, int pageSize, String imageFolder, String[] arqs) {
    this.pagesInMemory = pagesInMemory;
    this.pageSize = pageSize;
    this.arqs = arqs;
    this.imageFolder = imageFolder;
    loaded = new Image[arqs.length];
    if (pagesInMemory > 6) {
      Vm.debug("Caution: use a smaller value for pagesInMemory (up to 6) because it takes lot of memory!");
    }
  }

  @Override
  public void run() {
    running = true;
    int lastcur = -1;
    while (true) {
      while (lastcur == current && running) {
        Vm.sleep(10);
      }
      if (!running) {
        break;
      }
      int range = pageSize * pagesInMemory;
      lastcur = current;
      int lastMinIdx = lastcur - range;
      if (lastMinIdx < 0) {
        lastMinIdx = 0;
      }
      int lastMaxIdx = lastcur + range;
      if (lastMaxIdx >= arqs.length) {
        lastMaxIdx = arqs.length - 1;
      }
      if (lastIni != lastEnd) {
        for (int i = lastIni; i < lastMinIdx; i++) {
          loaded[i] = null;
        }
        for (int i = lastMaxIdx + 1; i <= lastEnd; i++) {
          loaded[i] = null;
        }
      }
      lastIni = lastMinIdx;
      lastEnd = lastMaxIdx;
      ex = null;
      //Vm.tweak(Vm.TWEAK_DISABLE_GC,true); -- uncommenting these lines will load faster but will take much more memory
      for (int i = lastMinIdx; i <= lastMaxIdx && lastcur == current; i++) {
        if (loaded[i] == null) {
          loaded[i] = loadImage(i);
          //Vm.tweak(Vm.TWEAK_DISABLE_GC,false);
        }
      }
    }
  }

  private byte[] buf = new byte[1];

  private Image loadImage(int i) {
    Image img = null;
    try {
      File f = new File(imageFolder + arqs[i], File.READ_WRITE);
      int s = f.getSize();
      if (buf.length < s) {
        buf = new byte[s];
      }
      f.readBytes(buf, 0, s);
      f.close();
      img = new Image(buf, s);
    } catch (IOException ioe) {
      Vm.debug(ioe.getMessage());
      ioe.printStackTrace();
      ex = ioe;
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return img;
  }

  private void setCurrent(int idx) {
    if (loaded[idx] != null && ((idx % pageSize) != 0 || idx == current)) {
      return;
    }
    current = idx;
  }

  /** Returns the image at the given index */
  public Image getImage(int idx) throws IOException {
    if (ex != null) {
      throw ex;
    }
    try {
      setCurrent(idx);
      while (loaded[idx] == null) {
        Vm.sleep(10);
      }
      return loaded[idx];
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
