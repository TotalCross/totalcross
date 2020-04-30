// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

/**
 * Class that implements a Grid that each cell is a container.
 * See the ImageBook sample, which also shows how to dynamically 
 * load and unload images. Here's a piece of it:
 * <pre>
 * add(gc = new GridContainer(GridContainer.HORIZONTAL_ORIENTATION),LEFT,TOP,FILL,FILL);
   gc.setBackColor(Color.WHITE);
   Flick f = gc.getFlick();
   f.shortestFlick = 1000;
   f.longestFlick = 6000;
   gc.setPageSize(linhas,colunas);
   gc.setRowsPerPage(linhasPorPagina);
   Celula []cels = new Celula[TOTAL_ITEMS];
   for (int i = 0; i < cels.length; i++)
      cels[i] = new Celula(i);
   gc.setCells(cels);
 * </pre>
 * @since TotalCross 1.53
 */

public class GridContainer extends Container {
  /** Defines a horizontal orientation scroll. */
  public static final int HORIZONTAL_ORIENTATION = 0;
  /** Defines a vertical orientation scroll. */
  public static final int VERTICAL_ORIENTATION = 1;

  private int orientation, cols, rows, rpp, pageCount;
  /** The ScrollContainer used in this control. */
  public ScrollContainer sc;
  private Cell[] cells;
  private ArrowButton btFirst, btLast;
  private ArrowButton btPrev, btNext;

  /** The container that has the page number and first/last arrows.
   * Only works when orientation is horizontal, and is null otherwise. */
  public NumericPagePosition pagepos;

  /** A percentage that defines the heihgt of the arrow buttons. Defaults to 200 in penless devices, 100 otherwise.
   */
  public int buttonsHeight = Settings.fingerTouch ? Control.isTablet ? 200 : 150 : 100;

  /** Constructs a GridContainer with the given orientation
   * @see #HORIZONTAL_ORIENTATION
   * @see #VERTICAL_ORIENTATION
   */
  public GridContainer(int orientation) {
    this.orientation = orientation;
    sc = new ScrollContainer(orientation == HORIZONTAL_ORIENTATION, orientation == VERTICAL_ORIENTATION) {
      @Override
      public int getScrollDistance() {
        return GridContainer.this.orientation == HORIZONTAL_ORIENTATION && cells != null && cells.length >= cols
            ? cells[cols - 1].getX2() + 1 : 0;
      }
    };
    pagepos = new NumericPagePosition();
    if (orientation == HORIZONTAL_ORIENTATION) {
      sc.flick.setPagePosition(pagepos);
    }
  }

  /** Returns the flick attached to the ScrollContainer. */
  public Flick getFlick() {
    return sc.flick;
  }

  /** A Grid's cell. All cells must extend this class so they can be added. */
  public static class Cell extends Container {
    int inX, inY;

    public Cell() {
      focusTraversable = true;
    }

    @Override
    public void onEvent(Event e) {
      switch (e.type) {
      case PenEvent.PEN_DOWN: {
        PenEvent pe = (PenEvent) e;
        inX = pe.x;
        inY = pe.y;
        break;
      }
      case PenEvent.PEN_UP: {
        PenEvent pe = (PenEvent) e;
        int threeshold = width / 4;
        int dx = inX - pe.x;
        if (dx < 0) {
          dx = -dx;
        }
        int dy = inY - pe.y;
        if (dy < 0) {
          dy = -dy;
        }
        if (dx < threeshold && dy < threeshold && !hadParentScrolled()) {
          postPressedEvent();
        }
        break;
      }
      }
    }
  }

  @Override
  public void onFontChanged() {
    sc.setFont(font);
  }

  private class FLArrowButton extends ArrowButton {
    public FLArrowButton(byte direction, int prefWH, int arrowColor) {
      super(direction, prefWH, arrowColor);
      setBorder(BORDER_NONE);
    }

    @Override
    public void onPaint(Graphics g) {
      super.onPaint(g);
      g.backColor = arrowColor;
      if (direction == Graphics.ARROW_LEFT) {
        g.fillRect(xx - 2, yy, 2, height);
      } else {
        g.fillRect(xx + fmH * buttonsHeight / 100 / 2 + 1, yy, 2, height);
      }
    }
  }

  @Override
  public void initUI() {
    boolean isHoriz = orientation == HORIZONTAL_ORIENTATION;
    int hh = fmH * buttonsHeight / 100;
    if (isHoriz) {
      pagepos.setBackColor(Color.darker(backColor, 16));
      add(pagepos, CENTER, BOTTOM, PARENTSIZE + 30, hh);
      pagepos.setPosition(1);
      sc.flick.setScrollDistance(width);

      btFirst = new FLArrowButton(Graphics.ARROW_LEFT, hh / 2, foreColor);
      add(btFirst, LEFT, BOTTOM, PARENTSIZE + 18, hh);

      btPrev = new ArrowButton(Graphics.ARROW_LEFT, hh / 2, foreColor);
      btPrev.setBorder(Button.BORDER_NONE);
      add(btPrev, AFTER, BOTTOM, PARENTSIZE + 17, hh);
      btPrev.autoRepeat = true;

      add(sc, LEFT, TOP, FILL, FIT);

      btLast = new FLArrowButton(Graphics.ARROW_RIGHT, hh / 2, foreColor);
      add(btLast, RIGHT, BOTTOM, PARENTSIZE + 18, hh);

      btNext = new ArrowButton(Graphics.ARROW_RIGHT, hh / 2, foreColor);
      btNext.setBorder(Button.BORDER_NONE);
      add(btNext, BEFORE, BOTTOM, PARENTSIZE + 17, hh);
      btNext.autoRepeat = true;

      sc.flick.forcedFlickDirection = orientation == HORIZONTAL_ORIENTATION ? Flick.HORIZONTAL_DIRECTION_ONLY
          : Flick.VERTICAL_DIRECTION_ONLY;
    } else {
      btFirst = new ArrowButton(Graphics.ARROW_UP, hh / 2, foreColor);
      btFirst.setBorder(Button.BORDER_NONE);
      add(btFirst, LEFT, BOTTOM, PARENTSIZE + 50, hh);
      btFirst.setArrowSize(hh / 2);

      add(sc, LEFT, TOP, FILL, FIT);

      btLast = new ArrowButton(Graphics.ARROW_DOWN, hh / 2, foreColor);
      btLast.setBorder(Button.BORDER_NONE);
      add(btLast, RIGHT, BOTTOM, PARENTSIZE + 50, hh);
      btLast.setArrowSize(hh / 2);
    }
    sc.flick.forcedFlickDirection = orientation == HORIZONTAL_ORIENTATION ? Flick.HORIZONTAL_DIRECTION_ONLY
        : Flick.VERTICAL_DIRECTION_ONLY;
  }

  /** Sets the rows per page. Changing this value changes the font size dynamically. */
  public void setRowsPerPage(int rpp) {
    this.rpp = rpp;
  }

  /** Sets the page size in columns and rows. */
  public void setPageSize(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;
  }

  /** Sets the cells of this GridContainer. Note that you cannot delete or add cells, only change the whole
   * set of cells. You must call setRowsPerPage and/or setPageSize before calling this method.
   */
  public void setCells(Cell[] cells) {
    sc.removeAll();
    sc.setRect(KEEP, KEEP, KEEP, KEEP); // reset bag positions
    if (rpp != 0) {
      setFont(Font.getFont(font.isBold(), Math.min(height, width) / rows / rpp));
    }
    this.cells = cells;
    boolean singleCell = cols == 1 && rows == 1;
    int percX = PARENTSIZE - cols;
    int percY = PARENTSIZE - rows;
    int px = LEFT, py = TOP;
    int cr = cols * rows;
    pageCount = cells.length / cr;
    if ((cells.length % cr) != 0) {
      pageCount++;
    }
    if (orientation == VERTICAL_ORIENTATION) {
      for (int z = 1; z <= cells.length; z++) {
        sc.add(cells[z - 1], px, py, percX, percY);
        if ((z % cols) != 0) {
          px = AFTER;
          py = SAME;
        } else {
          px = LEFT;
          py = AFTER;
        }
      }
    } else {
      pagepos.setCount(pageCount);
      pagepos.setPosition(1);
      Control last = null;
      for (int z = 1, idx = cols - 1; z <= cells.length; z++) {
        sc.add(cells[z - 1], px, py, percX, percY, last);
        last = null;
        if (singleCell || (z % cols) != 0) // same row
        {
          px = AFTER;
          py = SAME;
        } else // change row
        if (z < cr) {
          px = LEFT;
          py = AFTER;
        } else {
          last = cells[idx];
          idx += cols;
        }
      }
      // fill with spacers the rest of the columns if the last page has less than one column filled
      int remains = cols - cells.length % cr;
      if (remains > 0 && remains < cols) {
        for (int i = 0; i < remains; i++) {
          sc.add(new Spacer(0, 0), AFTER, SAME, percX, 1);
        }
      }
      if (cells.length >= cols) {
        sc.flick.setScrollDistance(cells[cols - 1].getX2() + 1);
      }
    }
  }

  @Override
  public void onEvent(Event e) {
    if (e.type == ControlEvent.PRESSED) {
      if (e.target == btFirst) {
        sc.scrollToPage(1);
      } else if (e.target == btLast) {
        sc.scrollToPage(pageCount);
      } else if (e.target == btPrev) {
        sc.scrollPage(true);
      } else if (e.target == btNext) {
        sc.scrollPage(false);
      }
    }
  }

  @Override
  public void onColorsChanged(boolean colorsChanged) {
    super.onColorsChanged(colorsChanged);
    sc.setBackForeColors(getBackColor(), getForeColor());
  }
}
