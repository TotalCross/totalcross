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
import totalcross.sys.Vm;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.Graphics;

/**
 * The <code>TextSpan</code> class holds consecutive characters of text having
 * the same font metrics.  These characters are broken into lines.  Each Line
 * -- a static inner class -- defines the Rect suitable for the portion of text
 * it contains.
 * <P>
 * On the display, a <code>TextSpan</code> ressembles to:
 * <PRE>
 *
 *                 +------------------------------+
 *              HH | xxxxxxxx                     |  <--- Line
 *      +----------+------------------------------+
 *      | yyyyyyyyyy                              |  <--- Line
 *      +-----------------------------+-----------+
 *      | zzzzzzzzzz                  | FF           <--- Line
 *      +-----------------------------+
 * </PRE>
 * where "HH"  and "FF" are Header (OL and UL) and Footer (not yet implemented)
 * pertaining to this <code>TextSpan</code>.
 */

class TextSpan extends Control implements Document.CustomLayout, Document.SizeDelimiter {
  private static final boolean DEBUG = false;
  private char[] text;
  private Style style;
  private int maxWordWidth;
  public boolean finishIdent, openIdent;

  /** Static class to hold each line of text within this span. */
  private class TextLine extends totalcross.ui.Control {
    private int pos;
    private int len;

    TextLine(int start, int end) {
      pos = start;
      len = end - start;
      focusTraversable = style.href != null;
    }

    @Override
    public int getPreferredWidth() {
      return fm.stringWidth(text, pos, len);
    }

    @Override
    public int getPreferredHeight() {
      return fmH + Edit.prefH;
    }

    @Override
    public void onPaint(Graphics g) {
      int deltaY;
      if ((style.fontBits & Style.SUBSCRIPT) != 0) {
        deltaY = 3;
      } else if ((style.fontBits & Style.SUPERSCRIPT) != 0) {
        deltaY = -3;
      } else {
        deltaY = 0;
      }
      g.foreColor = style.fontColor;
      g.backColor = style.backColor;
      if (style.backColor != getParent().getBackColor()) {
        g.fillRect(0, 0, width, height);
      }
      g.drawText(text, pos, len, 0, (height - fmH) / 2 + deltaY);
      if ((style.fontBits & (Style.STRIKETHROUGH | Style.UNDERLINE)) != 0) {
        int ypos = (style.fontBits & (Style.STRIKETHROUGH)) != 0 ? fm.height / 2 : fm.ascent + 2;
        g.drawLine(0, ypos, width, ypos);
      }
    }

    @Override
    public void onEvent(Event e) {
      if ((e.type == PenEvent.PEN_DOWN || e.type == KeyEvent.ACTION_KEY_PRESS) && style.href != null) {
        HtmlContainer.getHtmlContainer(this).postLinkEvent(style.href);
      }
    }
  }

  /**
   * Constructor.
   * @param currTile 
   *
   * @param doc containing document
   * @param text the text this TextSpan holds
   * @param style associated style
   */
  TextSpan(Container currTile, String text, Style style) {
    focusTraversable = false;
    if (style.alignment != Style.ALIGN_NONE) {
      text = Convert.replace(text, "  ", " ");
    }
    this.text = text.toCharArray();
    this.style = style;
    currTile.add(this);
    computeMaxWidth(text, this.text);
  }

  private int computeMaxWidthPerLine(String input, char[] inputChars) {
    FontMetrics fm = style.getFont().fm;
    int maxWidth = 0;
    // in a pre tag, we need to get the width of the biggest text line
    int newPosition, position = 0;
    while ((newPosition = input.indexOf('\n', position)) >= 0) {
      maxWidth = Math.max(maxWidth, fm.stringWidth(inputChars, position, newPosition - position));
      position = newPosition + 1;
    }
    if (position == 0) {
      maxWidth = fm.stringWidth(input);
    }
    return maxWidth;
  }

  private int computeMaxWordWidth(String input, char[] inputChars) {
    FontMetrics fm = style.getFont().fm;
    int maxWidth = 0;
    // in a standard text (free-flow), we get the maximum width of a single word
    int newPosition, position = 0;
    input = input.replace('\n', ' ');
    while ((newPosition = input.indexOf(' ', position)) >= 0) {
      maxWidth = Math.max(maxWidth, fm.stringWidth(inputChars, position, newPosition - position));
      position = newPosition + 1;
    }
    if (position == 0) {
      maxWidth = fm.stringWidth(input);
    }
    return maxWidth;
  }

  private void computeMaxWidth(String input, char[] inputChars) {
    if (style.alignment == Style.ALIGN_NONE) {
      maxWordWidth = computeMaxWidthPerLine(input, inputChars);
    } else {
      maxWordWidth = computeMaxWordWidth(input, inputChars);
    }
  }

  @Override
  protected void onFontChanged() {
    computeMaxWidth(new String(text), text);
  }

  @Override
  public int getMaxWidth() {
    return maxWordWidth;
  }

  /**
   * Implement the layout method to break text into lines and
   * position those lines updating the layout context along the way.
   * @param lc layout context to update.
   */
  @Override
  public void layout(LayoutContext lc) {
    //String s = new String(text);
    setFont(style.getFont());

    if (style.hasInitialValues()) {
      if (style.isDisjoint) {
        lc.disjoin();
      }
      Style.Header h = style.getHeader();
      if (h != null) {
        h.setFont(font);
        if (openIdent) {
          lc.setIdentation(style.indent, true);
        }
        parent.add((Control) h, lc.nextX, lc.nextY);
        lc.update(h.getWidth());
      }
    }

    boolean isPRE = style.alignment == Style.ALIGN_NONE;
    if (isPRE) {
      makePreformattedLines(lc);
    } else {
      makeLines(lc);
    }

    boolean disjoin = isPRE;
    boolean wasDisjoin = lc.lastStyle != null && lc.lastStyle.alignment == Style.ALIGN_NONE;
    lc.lastStyle = style;

    if (disjoin && wasDisjoin) {
      disjoin = false;
    } else if (!disjoin && wasDisjoin) {
      disjoin = true;
    }

    //debug(lc,style,new String(text));
    if (disjoin) {
      lc.disjoin();
    } else if (!disjoin && wasDisjoin) {
      lc.update(0);
    }
    if (finishIdent) {
      lc.setIdentation(0, false);
    }
  }

  /**
   * Break up the text at word breaks, so the lines can fit (hopefully),
   * avoiding the need of a horizontal scrollbar.
   *
   * @param fm font metrics to use to perform text size calculations
   * @param lc layout context to update
   */
  private void makeLines(LayoutContext lc) {
    //String s = new String(text);
    boolean isLastLine = false;
    int tries = 0;
    int curWidth, wordWidth, lineStart, wordEnd, wordStart, newWidth, textLen, beg, maxM2, spaceW;
    curWidth = lineStart = wordEnd = beg = 0;
    char[] text = this.text;
    textLen = text.length;
    maxM2 = textLen - 2;
    spaceW = fm.charWidth(' ');
    boolean glue = false;

    do {
      beg = wordEnd;

      // find next word
      for (wordStart = beg;; wordStart++) {
        if (wordStart >= textLen) // trailing blanks?
        {
          if (tries > 0) // guich@tc114_81
          {
            lc.disjoin();
            addLine(lineStart, wordEnd, false, lc, false);
            tries = 0;
          }
          wordEnd = wordStart;
          isLastLine = true;
          break;
        }
        if (text[wordStart] != ' ') // is this the first non-space char?
        {
          wordEnd = wordStart;
          do {
            if (++wordEnd >= textLen) {
              isLastLine = true;
              break;
            }
          } while (text[wordEnd] != ' ' && text[wordEnd] != '/'); // loop until the next space/slash char
          // use slashes as word delimiters (useful for URL addresses).
          if (maxM2 > wordEnd && text[wordEnd] == '/' && text[wordEnd + 1] != '/') {
            wordEnd++;
          }
          break;
        }
      }
      if (!lc.atStart() && wordStart > 0 && text[wordStart - 1] == ' ') {
        wordStart--;
      }
      wordWidth = fm.stringWidth(text, wordStart, wordEnd - wordStart);
      if (curWidth == 0) {
        lineStart = beg = wordStart; // no spaces at start of a line
        newWidth = wordWidth;
      } else {
        newWidth = curWidth + wordWidth;
      }

      if (lc.x + newWidth <= lc.maxWidth) {
        curWidth = newWidth + spaceW;
      } else // split: line length now exceeds the maximum allowed
      {
        //if (text[wordStart] == ' ') {wordStart++; wordWidth -= spaceW;}
        if (curWidth > 0) {
          // At here, wordStart and wordEnd refer to the word that overflows. So, we have to stop at the previous word
          wordEnd = wordStart;
          if (text[wordEnd - 1] == ' ') {
            wordEnd--;
          }
          if (DEBUG) {
            Vm.debug("1. \"" + new String(text, lineStart, wordEnd - lineStart) + "\": " + curWidth + " " + isLastLine);
          }
          addLine(lineStart, wordEnd, true, lc, glue);
          curWidth = 0;
          isLastLine = false; // must recompute the last line, since there's at least a word left.
        } else if (!lc.atStart()) // case of "this is a text at the end <b>oftheline</b>" -> oftheline will overflow the screen
        {
          if (++tries == 2) {
            break;
          }
          if (DEBUG) {
            Vm.debug("2 " + isLastLine);
          }
          // Nothing was gathered in, but the current line has characters left by a previous TextSpan.  This occurs only once.
          addLine(0, 0, false, lc, glue);
          curWidth = 0;
          isLastLine = false; // must recompute the last line, since there's at least a word left.
        } else {
          // Rare case where we both have nothing gathered in, and the physical line is empty.  Had this not been made, then we
          // woud have generated an extra-line at the top of the block.
          if (DEBUG) {
            Vm.debug("3. \"" + new String(text, lineStart, wordEnd - lineStart) + '"');
          }
          if (lineStart != wordEnd) {
            addLine(lineStart, wordEnd, true, lc, glue);
          }
        }
        glue = true;
      }
    } while (!isLastLine);

    if (wordEnd != lineStart) {
      //curWidth = fm.stringWidth(text, lineStart, wordEnd-lineStart);
      boolean split = lc.x + curWidth > lc.maxWidth && style.hasInitialValues() && style.isDisjoint;
      if (DEBUG) {
        Vm.debug("4. \"" + new String(text, lineStart, wordEnd - lineStart) + "\" " + split);
      }
      addLine(lineStart, wordEnd, split, lc, glue);
    }
  }

  /**
   * Break up preformatted text at each user-entered "newline".
   *
   * @param fm font metrics to use to perform text size calculations
   * @param lc layout context to update
   */
  private void makePreformattedLines(LayoutContext lc) {
    int end = -1;
    int max = text.length;
    boolean isLastLine = false;

    do {
      int beg = end + 1;
      do {
        if (++end >= max) {
          isLastLine = true;
          break;
        }
      } while (text[end] != '\n');
      addLine(beg, end, true, lc, false);
    } while (!isLastLine);
  }

  /**
   * Add a line to the list of lines.
   *
   * @param name description
   */
  private void addLine(int textStart, int textEnd, boolean isDisjoin, LayoutContext lc, boolean glue) {
    //String s = new String(text, textStart,textEnd-textStart);
    int xx = style.getControlAlignment(true); //flsobral@tc126: use the new method to get the line's alignment.
    if (xx == 0 || xx == Control.LEFT) {
      xx = lc.nextX; //flsobral@tc126: always use nextX when left aligned.
    }
    int yy = lc.nextY;
    if (glue) {
      yy -= Edit.prefH;
    }
    if (lc.atStart()) {
      yy += style.topMargin;
    }
    TextLine l = new TextLine(textStart, textEnd);
    parent.add(l);
    l.setFont(font);
    if (style.alignment == Style.ALIGN_LEFT) {
      lc.verify(l.getPreferredWidth());
    }
    l.setRect(xx, yy, PREFERRED, PREFERRED);
    if (style.alignment == Style.ALIGN_CENTER || style.alignment == Style.ALIGN_RIGHT) {
      l.setRect(xx, KEEP, KEEP, KEEP, lc.parentContainer); //flsobral@tc126: make line relative to the layout context parent container when aligned with center or right.
    }
    if (isDisjoin) {
      lc.disjoin();
    } else {
      lc.update(l.getWidth());
    }
    lc.lastControl = l;
  }

  // debuggint routines
  /*   static int conta;
   static void debug(LayoutContext lc, Style style, String text)
   {
      System.err.println(conta++ +" "+positionName(lc.nextX + ((lc.nextX == LEFT) ? style.indent : 0))+" "+positionName(lc.nextY + ((lc.nextY == AFTER) ? style.topMargin : 0))+" "+style.alignment+"/"+lc.x+" "+text);
   }
   static String positionName(int xx)
   {
      String s = "???";
      switch (xx / 1000000 * 1000000)
      {
         case PREFERRED: s = "PREFERRED+"+(xx-PREFERRED); break;
         case LEFT: s = "LEFT+"+(xx-LEFT); break;
         case CENTER: s = "CENTER+"+(xx-CENTER); break;
         case RIGHT: s = "RIGHT+"+(xx-RIGHT); break;
         case TOP: s = "TOP+"+(xx-TOP); break;
         case BOTTOM: s = "BOTTOM+"+(xx-BOTTOM); break;
         case FILL: s = "FILL+"+(xx-FILL); break;
         case BEFORE: s = "BEFORE+"+(xx-BEFORE); break;
         case SAME: s = "SAME+"+(xx-SAME); break;
         case AFTER: s = "AFTER+"+(xx-AFTER); break;
         case FIT: s = "FIT+"+(xx-FIT); break;
         case CENTER_OF: s = "CENTER_OF+"+(xx-CENTER_OF); break;
         case RIGHT_OF: s = "RIGHT_OF+"+(xx-RIGHT_OF); break;
         case BOTTOM_OF: s = "BOTTOM_OF+"+(xx-BOTTOM_OF); break;
      }      
      if (s.endsWith("+0"))
         s = s.substring(0,s.length()-2);
      return s;
   }*/
}
