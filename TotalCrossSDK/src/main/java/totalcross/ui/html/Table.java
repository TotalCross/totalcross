// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.html;

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!!!!  REMEMBER THAT ANY CHANGE YOU MAKE IN THIS CODE MUST BE SENT BACK TO SUPERWABA COMPANY     !!!!
//!!!!  LEMBRE-SE QUE QUALQUER ALTERACAO QUE SEJA FEITO NESSE CODIGO DEVERÁ SER ENVIADA PARA NOS  !!!!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.ScrollContainer;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.html.Document.CustomLayout;
import totalcross.ui.html.Document.SizeDelimiter;
import totalcross.ui.html.Document.StopLayout;
import totalcross.util.IntVector;
import totalcross.xml.AttributeList;

/**
 * <code>Table</code> is the Tile associated to the &lt;TABLE&gt; tag.
 * 
 * @author Pierre G. Richard
 */

class Table extends ScrollContainer implements CustomLayout, StopLayout, SizeDelimiter {
  int colCount;
  int borderWidth;
  int spacing; // distance between cell borders
  int padding; // distance between cell content and cell border
  IntVector colsPerRow = new IntVector();
  int[] colMaxWidths, maxRowHeights;
  int maxColspan, curColspan, maxRowspan, curRowspan;
  int prefW;
  Style style;

  /**
   * Constructor
   *
   * @param tagHashId tag associated with the Table (i.e. Table)
   * @param atts tag attributes
   * @param style associated style
   */
  Table(AttributeList atts, Style style) {
    super(false);
    this.style = style;
    borderWidth = atts.getAttributeValueAsInt("border", 0);
    spacing = atts.getAttributeValueAsInt("cellspacing", 0);
    padding = atts.getAttributeValueAsInt("cellpadding", 0);
    String s = atts.getAttributeValue("width");
    if (s != null) {
      try {
        int perc = s.indexOf('%');
        if (perc > 0) {
          prefW = -Convert.toInt(s.substring(0, perc));
        } else {
          prefW = Convert.toInt(s);
        }
      } catch (Exception e) {
        if (Settings.onJavaSE) {
          Vm.debug("Exception on Table's constructor: " + e + " " + e.getMessage() + "");
        }
      }
    }
  }

  /**
   * Add a row to the table
   *
   * @param atts tag attributes
   * @param style associated style
   */
  void startRow(AttributeList atts, Style style) {
  }

  void endRow() {
    colsPerRow.addElement(colCount);
    maxColspan = Convert.max(maxColspan, curColspan);
    maxRowspan = Convert.max(maxRowspan, colsPerRow.size(), curRowspan);
    curColspan = colCount = 0;
  }

  void endTable() {
  }

  Container startCell(AttributeList atts, Style style) {
    colCount++;
    Cell cell = new Cell(style);
    int pad = padding;
    if (borderWidth >= 1) {
      cell.setBorderStyle(borderWidth == 1 ? BORDER_LOWERED : borderWidth == 2 ? BORDER_SIMPLE : BORDER_RAISED);
      pad++;
    }
    cell.setInsets(pad, pad, pad, pad);
    add(cell);
    curColspan += 1 + cell.colspan;
    curRowspan = Math.max(curRowspan, colsPerRow.size() + cell.rowspan);
    return cell;
  }

  static class CellSpan extends Control {
    Cell owner;

    CellSpan(Cell owner) {
      this.owner = owner;
    }
  }

  private Control[][] adjustSpan() {
    Control[] cs = bag.getChildren();
    int rows = colsPerRow.size();
    Control[][] matrix = new Control[maxRowspan][maxColspan];
    // first, colspan: fill the span columns with CellSpan objects
    for (int r = 0, i = cs.length, jj = 0; r < rows; r++, jj++) {
      for (int c = 0, ii = 0, maxCols = colsPerRow.items[r]; c < maxCols; c++, ii++) {
        Cell cell = (Cell) (matrix[jj][ii] = cs[--i]);
        if (cell.colspan > 1) {
          for (int z = 1; z < cell.colspan; z++) {
            matrix[jj][ii + z] = new CellSpan(cell);
          }
          ii += cell.colspan - 1;
        }
      }
    }

    /* now, rowspan: shift columns to right when a rowspan is found (C=Cell, S=CellSpan, 0=null - see Sample6 in HtmlBrowser)
            C C C C      C C C C 
            C C C 0      S C C C 
            C S C 0  =>  S C S C 
            C C C C      C C C C 
            C S C 0      S C S C 
            C 0 0 0      S S S C      */
    for (int c = 0; c < maxColspan; c++) {
      for (int r = 0; r < maxRowspan; r++) {
        Control control = (Control) matrix[r][c];
        Cell cell = control instanceof CellSpan ? ((CellSpan) control).owner : (Cell) control;
        if (cell == null) {
          continue;
        }
        int rowspan = cell.rowspan;
        if (rowspan > 1) {
          for (int z = 1; z < rowspan; z++) {
            Control[] row = matrix[r + z];
            Vm.arrayCopy(row, c, row, c + 1, row.length - c - 1);
            matrix[r + z][c] = new CellSpan(cell);
          }
          r += rowspan - 1;
        }
      }
    }
    // dump - for (int i = 0; i < rows; i++) for (int j =0; j < maxColspan; j++) System.out.print((matrix[i][j] == null ? "0 " : matrix[i][j] instanceof Cell ? "C " : "S ")+(j==maxColspan-1?"\n":""));
    return matrix;
  }

  @Override
  public void layout(LayoutContext lc) {
    this.width = this.height = 4096; // temporary
    lc.disjoin();
    int k = borderWidth + spacing;
    setInsets(k, k, k, k);
    // 1. we fill the matrix with the cells, adjusting based on colspan/rowspan
    Control[][] matrix = adjustSpan();
    // 2. we place the cells along the table
    int xx = 0, yy = 0, ww, hh;
    for (int r = 0; r < maxRowspan; r++) {
      for (int c = 0; c < maxColspan; c++) {
        Control control = (Control) matrix[r][c];
        if (control instanceof CellSpan) {
          Cell cell = ((CellSpan) control).owner;
          if (c == 0) {
            ww = cell.getWidth();
          } else {
            Control c1 = matrix[r][c - 1];
            Cell cell1 = c1 instanceof CellSpan ? ((CellSpan) c1).owner : (Cell) c1;
            ww = cell1 == cell ? 0 : cell.getWidth(); // if its the same owner, the width was already computed, so assign 0 to ww 
          }
          hh = cell.getHeight();
        } else {
          Cell cell = (Cell) control;
          ww = colMaxWidths[c];
          hh = maxRowHeights[r];
          if (cell != null) // guich@tc114_22: google.com.br?
          {
            if (cell.colspan > 1) {
              for (int cs = cell.colspan; --cs >= 1;) {
                ww += colMaxWidths[c + cs];
              }
            }
            if (cell.rowspan > 1) {
              hh = 0;
              for (int cs = cell.rowspan; --cs >= 0;) {
                hh += maxRowHeights[r + cs];
              }
              hh = Math.max(cell.getPreferredHeight(), hh); // if the cell has a height bigger than the sum of the other rows, use it
            }
            cell.setRect(xx, yy, ww, hh);
          }
        }
        xx += ww + spacing;
        if (c == maxColspan - 1) {
          yy += hh + spacing;
          xx = 0;
        }
      }
    }
    resize();
    setRect(lc.nextX, lc.nextY, PREFERRED + k + k, PREFERRED + k + k);
    lc.disjoin();
  }

  @Override
  public void onPaint(Graphics g) {
    g.backColor = style.backColor;
    g.fillRect(0, 0, width, height);
    super.onPaint(g);
    // paint Table's border
    for (int i = borderWidth; --i >= 0;) {
      g.drawRect(i, i, width - i - i, height - i - i);
    }
  }

  @Override
  public int getMaxWidth() {
    colMaxWidths = new int[maxColspan];
    maxRowHeights = new int[colsPerRow.size()];
    Control[] cs = bag.getChildren();
    int rows = colsPerRow.size();
    int maxRowSize = 0, currentRowSize;
    for (int r = 0, i = cs.length; r < rows; r++) {
      currentRowSize = 0;
      for (int c = 0, n = colsPerRow.items[r]; c < n; c++) {
        Cell cell = (Cell) cs[--i];
        // compute max width
        int w = cell.getMaxWidth();
        if (w > colMaxWidths[c]) {
          colMaxWidths[c] = w;
        }
        // compute max height - don't move from here!
        int h = cell.getPreferredHeight();
        if (h > maxRowHeights[r] && cell.rowspan <= 1) {
          maxRowHeights[r] = h;
        }
        // also consider border and gaps
        if (borderWidth >= 1) {
          w += 2;
        }
        if (padding > 0) {
          w += padding;
        }
        if (spacing > 0) {
          w += spacing;
        }
        currentRowSize += w;
      }
      if (currentRowSize > maxRowSize) {
        maxRowSize = currentRowSize;
      }
    }
    return maxRowSize;
  }
}
