/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api.ui;

import tc.samples.api.BaseContainer;
import totalcross.io.File;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.Flick;
import totalcross.ui.GridContainer;
import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.Radio;
import totalcross.ui.RadioGroupController;
import totalcross.ui.Window;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.BulkImageLoader;
import totalcross.ui.image.Image;

/** See onRemove to prevent memory leak! */

public class ImageBookSample extends BaseContainer {
  GridContainer gc;
  static boolean isVert;
  RadioGroupController rg;
  public static String[] images = { "tc/samples/api/ui/books/book1.png", "tc/samples/api/ui/books/book2.png",
      "tc/samples/api/ui/books/book3.png", "tc/samples/api/ui/books/book4.png", "tc/samples/api/ui/books/book5.png",
      "tc/samples/api/ui/books/book6.png", "tc/samples/api/ui/books/book7.png", "tc/samples/api/ui/books/book8.png", };

  // saves the files that contains the images
  // always store the reference to the file, NEVER the image bytes! can also be the rowid or primary key if the image is inside a database
  String[] imageNames;
  private String imageFolder;

  // parameters that you can tweak

  // controls the gaps within the Cell
  private static final int CELL_GAP = 0;// uncomment to change the space - Settings.uiAdjustmentsBasedOnFontHeight ? 20 : 2;
  private static final int IMAGE_GAP = CELL_GAP * 2;

  // controls the image quality
  private static final int TOTAL_ITEMS = 1000;

  private static final int GRID_LINES = 3;
  private static final int GRID_COLS = 4;
  private static final int CACHE_PAGES = 3; // big value = more memory consumed
  private static final int LINES_PER_PAGE = 6; // auto-adjust the font size; if set to 0, the size is unchanged

  // thread that loads and unloads the images dynamically
  BulkImageLoader imgload;

  // class that represents a Grid's cell

  class ImgCell extends GridContainer.Cell {
    int idx;
    boolean highQuality;
    Label l1, l2;
    Control img;

    public ImgCell(int idx) {
      this(idx, false);
    }

    public ImgCell(int idx, boolean highQuality) {
      this.idx = idx;
      this.highQuality = highQuality;
      setBackColor(Color.WHITE);
      setBorderStyle(BORDER_SIMPLE); // uncomment to remove border
    }

    @Override
    public void initUI() {
      setInsets(CELL_GAP, CELL_GAP, CELL_GAP, CELL_GAP);
      l1 = new Label("book " + (idx + 1), CENTER);
      l1.autoSplit = true;
      l2 = new Label("Written by J.R.Olling", CENTER);
      l2.autoSplit = true;
      add(l1, LEFT, TOP, FILL, PREFERRED);
      l1.reposition();
      add(l2, LEFT, BOTTOM, FILL, PREFERRED);
      l2.reposition();
      try {
        img = imgload.new DynImage(idx);
        if (highQuality) {
          Image image = imgload.getImage(idx);
          double ratio = Math.min((double) width / image.getWidth(), (double) height / image.getHeight());
          ImageControl ic = new ImageControl(
              image.getSmoothScaledInstance((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio)));
          img = ic;
          ic.setEventsEnabled(true);
          ic.centerImage = true;
        }
        add(img, LEFT, AFTER + IMAGE_GAP, FILL, FIT - IMAGE_GAP, l1);
      } catch (Throwable t) {
        img = null;
      }
    }

    @Override
    public void reposition() {
      super.reposition();
      l1.reposition();
      l2.reposition();
      img.reposition();
    }
  }

  // class used to show a selected cell in a window
  class CellBox extends Window {
    int idx;

    public CellBox(int idx) {
      this.idx = idx;
      titleGap = 0;
    }

    @Override
    public void initUI() {
      fadeOtherWindows = true;
      add(new ImgCell(idx, true), LEFT, TOP, FILL, FILL);
    }

    @Override
    protected boolean onClickedOutside(PenEvent event) {
      unpop();
      return true;
    }
  }

  @Override
  public void initUI() {
    try {
      super.initUI();
      setTitle("Image Book");

      //if (Settings.isIOS() || Settings.isWindowsCE()) // hack for ios: extract the files and write into the folder
      {
        File dir = new File("device/books");
        if (!dir.exists()) {
          try {
            dir.createDir();
          } catch (Exception e) {
          }
          for (int i = 0; i < 8; i++) {
            byte[] b = Vm.getFile(images[i]);
            if (b != null) {
              try {
                new File("device/books/" + Convert.getFileName(images[i]), File.CREATE_EMPTY).writeAndClose(b);
              } catch (Exception e) {
                e.printStackTrace();
              }
            } else {
              Vm.debug(images[i] + " not found in pkg!");
            }
          }
        }
      }

      rg = new RadioGroupController();
      add(new Label("Orientation: "), LEFT + gap, TOP + gap);
      add(new Radio("Horizontal", rg), AFTER + gap, SAME);
      add(new Radio("Vertical", rg), AFTER + gap, SAME);
      rg.setSelectedIndex(isVert ? 1 : 0);

      // folder that contains the images. can be "/sdcard", for example
      imageFolder = Settings.appPath;
      //if (Settings.onJavaSE)
      //   imageFolder += "/src/tc/samples/api/ui";
      imageFolder += "/books/";
      // list images in the folder
      String[] arqs0 = new File(imageFolder).listFiles();
      // IMPORTANT: creates a single array with all image NAMES
      imageNames = new String[TOTAL_ITEMS];
      for (int i = 0; i < imageNames.length; i++) {
        imageNames[i] = arqs0[i % arqs0.length];
      }
      // defines orientation
      // starts the ImageLoader
      imgload = new BulkImageLoader(CACHE_PAGES, GRID_LINES * GRID_COLS, imageFolder, imageNames);
      imgload.start();

      setBackColor(Color.WHITE);

      addBook();
      info = "Click a book for details";
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  private void addBook() {
    if (gc != null) {
      remove(gc);
    }
    // defines the orientation: VERTICAL_ORIENTATION ou HORIZONTAL_ORIENTATION
    int dir = isVert ? GridContainer.VERTICAL_ORIENTATION : GridContainer.HORIZONTAL_ORIENTATION;
    add(gc = new GridContainer(dir), LEFT, TOP + fmH + gap * 2, FILL, FILL);
    gc.setBackColor(Color.WHITE);
    Flick f = gc.getFlick();
    f.shortestFlick = 1000; // increases because the image's load time affects the flick
    f.longestFlick = TOTAL_ITEMS > 500 ? 6000 : 2500;
    gc.setPageSize(GRID_COLS, GRID_LINES);
    gc.setRowsPerPage(LINES_PER_PAGE);
    addCells();
  }

  private void addCells() {
    ImgCell[] cels = new ImgCell[TOTAL_ITEMS];
    for (int i = 0; i < cels.length; i++) {
      cels[i] = new ImgCell(i);
    }
    gc.setCells(cels);
  }

  // intercepts the cell pressed event and shows it in a popup screen
  @Override
  public void onEvent(Event e) {
    super.onEvent(e);
    if (e.type == ControlEvent.PRESSED) {
      if (e.target instanceof ImgCell) {
        int idx = ((ImgCell) e.target).idx;
        CellBox mc = new CellBox(idx);
        mc.setRect(CENTER, CENTER, SCREENSIZE + 80, SCREENSIZE + 80);
        mc.popup();
      } else if (e.target instanceof Radio) {
        isVert = rg.getSelectedIndex() == 1;
        addBook();
      }
    }
  }

  @Override
  public void onRemove() {
    if (imgload != null) {
      imgload.running = false; // WITHOUT THIS, A HUGE MEMORY LEAK WILL OCCUR
    }
  }

  public ImageBookSample() {
    super.isSingleCall = true;
  }
}
