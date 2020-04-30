// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Convert;
import totalcross.ui.event.Event;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

/**
 * Label is a text label control. It supports multiline,
 * but you need to separate the text with \n. A Label never receives focus neither dispatches events.
 * <p>
 * Note: this class does not do automatic parse of the text.
 * However, you can use a handy method to parse the text that you want to display: see the
 * totalcross.sys.Convert.insertLineBreak method.
 * <p>
 * Here is an example showing a label being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    public void initUI()
 *    {
 *       add(new Label("Value:"), LEFT, TOP);
 *    }
 * </pre>
 */

public class Label extends Control implements TextControl {
  /** The String with the full text (not splitted) */
  protected String text;
  protected int alpha;
  /** The text alignment; possible values are LEFT, RIGHT, CENTER, FILL (justifies the text).
   * If align is CENTER and the text is wider than the Label, only the right portion will be lost. */
  public int align; // guich@400_71: made public instead of protected
  /** Set to false if you want to scroll the text a line at a time instead of a page at a time. */
  public boolean pageScroll = true;
  static final String[] emptyStringArray = { "" };
  protected String[] lines = emptyStringArray;
  private int[] linesW;
  private int linesPerPage, currentLine;
  private boolean invert;
  private boolean is3d;
  private boolean highlighted;
  private int fColor; // sholtzer@450_21: added support for setEnabled(false)
  private int dColor = -1; // guich@450: darker color if in 3d mode
  /** Set automatically to true when an empty string is passed in the constructor. */
  public boolean useFillAsPreferred;
  private int marqueeCount, marqueeStep, marqueeX;
  private TimerEvent marqueeTimer;
  private int highlightColor = -1, userHighlightColor = -1;
  private Insets insets; // guich@tc114_71
  private int lastASW;
  private String originalText;

  /** By default, the getPreferredWidth uses the current text to compute the width.
   * However, if you create a Label with a predefined text that will be changed
   * later, in the advent of a reposition, the preferred width will be recomputed
   * again using the current text and not the predefined one.
   * 
   * For example, if you do:
   * <pre>
   * Label l = new Label("99");
   * l.setText("0");
   * </pre>
   * ... then the preferred width will be computed based on "0", not in "99".
   * 
   * To change this behaviour, assign to this field the text that you want to be 
   * used to compute the preferred width.
   * @since TotalCross 1.3
   */
  public String preferredWidthText;

  /** Set to a color to let this Label have a border with that color. Defaults to -1, which means no border.
   * Note that the border affects the Label's size (width and height are increased by 4), so you must set 
   * it this field before setting the rect.
   * If you want a bigger gap between border and text, you can use setInsets.
   * @see #setInsets(int, int, int, int)
   * @since TotalCross 1.27
   */
  public int borderColor = -1; // guich@tc126_51

  /** Specifies a solid background for this button (the default).
   * @see #VERTICAL_GRADIENT_BACKGROUND
   * @see #HORIZONTAL_GRADIENT_BACKGROUND
   * @since TotalCross 1.15
   */
  public static final byte SOLID_BACKGROUND = 0;

  /** Specifies a vertical gradient background for this Label.
   * @see #SOLID_BACKGROUND
   * @see #HORIZONTAL_GRADIENT_BACKGROUND
   * @since TotalCross 1.15
   */
  public static final byte VERTICAL_GRADIENT_BACKGROUND = 1;

  /** Specifies a horizontal 3d-gradient background for this Label. Used in the setBorder method.
   * @see #VERTICAL_GRADIENT_BACKGROUND
   * @see #SOLID_BACKGROUND
   * @since TotalCross 1.15
   */
  public static final byte HORIZONTAL_GRADIENT_BACKGROUND = 2; // guich@tc110_11

  /** The type of background of this Label (defaults to SOLID_BACKGROUND).
   * One color is the background color, and the other color is defined by 
   * <code>firstGradientColor</code> and <code>secondGradientColor</code>.
   * @see #HORIZONTAL_GRADIENT_BACKGROUND
   * @see #VERTICAL_GRADIENT_BACKGROUND
   * @see #SOLID_BACKGROUND
   * @see #firstGradientColor
   * @see #secondGradientColor
   * @since TotalCross 1.15
   */
  public int backgroundType = SOLID_BACKGROUND;

  /** The first color used in GRADIENT backgrounds. Defaults to Color.WHITE.
   * @since TotalCross 1.15
   */
  public int firstGradientColor = Color.WHITE;

  /** The second color used in GRADIENT backgrounds. Defaults to Color.BLACK.
   * @since TotalCross 1.15
   */
  public int secondGradientColor;

  /** Set to true to let the label split its text based on the width every time its width
   * changes. If the height is PREFERRED, the Label will change its size accordingly.
   * You may change the height again calling setRect.
   * @since TotalCross 1.14
   */
  public boolean autoSplit; // guich@tc114_74

  /** The vertical alignment. Defaults to CENTER 
   * @see #TOP
   * @see #CENTER
   * @see #BOTTOM
   * @since TotalCross 1.14
   */
  public int vAlign = CENTER;

  /**
   * Creates an empty label, using FILL as the preferred width.
   */
  public Label() {
    this("", LEFT);
  }

  /**
   * Creates a label displaying the given text. Alignment is set to LEFT by default.<br>
   * Supports inverted text, multiple lines and is scrollable by default.
   * 
   * @param text
   *           the to be text displayed
   * 
   * @throws NullPointerException
   *            if text is null.
   */
  public Label(String text) {
    this(text, LEFT);
  }
  
  /** Creates a label with the given text, alignment, foreground color, and if the font is bold or not. */
  public Label(String text, int align, int fore, boolean bold) {
    this(text, align);
    setForeColor(fore);
    if (bold) {
      setFont(font.asBold());
    }
    setAlpha(255);
  }

  /**
   * Creates a label displaying the given text with the given alignment.<br>
   * Supports inverted text, multiple lines and is scrollable by default.
   * 
   * @param text
   *           the text displayed; cannot be null, but can be an empty string.
   * @param align
   *           the alignment
   * @throws NullPointerException
   *            if text is null.
   * @throws IllegalArgumentException
   *            if align value is not either LEFT, RIGHT, CENTER or FILL.
   * @see #align
   */
  public Label(String text, int align) {
    if (align != LEFT && align != RIGHT && align != CENTER && align != FILL) {
      throw new IllegalArgumentException("Argument 'align' value must be either LEFT, RIGHT, CENTER or FILL");
    }
    this.useFillAsPreferred = false;//text == null || text.length() == 0;
    this.align = align;
    setText(text);
    clearValueStr = null; // guich@573_3
    focusTraversable = false;
    setAlpha(255);
  }

  /** Defines a space to be placed around the text of this label.
   * @since TotalCross 1.14 
   */
  public void setInsets(int left, int right, int top, int bottom) // guich@tc114_71
  {
    if (insets == null) {
      insets = new Insets();
    }
    insets.left = left;
    insets.right = right;
    insets.top = top;
    insets.bottom = bottom;
  }

  /** Shows this label as a horizontal marquee. The text must have a single line.
   * To stop the marquee, just call setText. When a window covers the marquee, it
   * is halted, and resumed when the window closes.
   * @param text the text to be displayed
   * @param delay the timer delay in ms used to scroll the marquee. 100 is a good value.
   * @param loopCount the number of times the text will loop. Set to -1 to loop forever.
   * When the loop count is reached, the text is cleared.
   * @param step the step in pixels in which the text will be scrolled. If &gt; 0, will
   * scroll from left to right; if &lt; 0, will scroll from right to left.
   * @since TotalCross 1.0 beta 4
   */
  public void setMarqueeText(String text, int delay, int loopCount, int step) {
    if (step != 0) {
      marqueeX = step > 0 ? -linesW[0] : width;
    }
    if (marqueeTimer != null) {
      stopMarquee();
    }
    this.text = text;
    lines = new String[] { text };
    currentLine = 0;
    onFontChanged();
    if (step != 0) {
      marqueeCount = loopCount;
      marqueeStep = step;
      marqueeTimer = addTimer(delay);
    }
  }

  /** Returns true if the marquee is running.
   * @since TotalCross 1.0 beta 4
   */
  public boolean isMarqueeRunning() {
    return marqueeTimer != null;
  }

  @Override
  public void onEvent(Event e) {
    if (marqueeTimer != null && marqueeTimer.triggered) {
      Window w = getParentWindow();
      if (w != null && w.isTopMost()) // update only if our window is the one being shown
      {
        marqueeX += marqueeStep;
        repaintNow(); // using this instead of repaint prevents an Edit to be painted too often, because repaint affects all controls on screen, while repaintNow affects only this control
      }
    }
  }

  /** Draws the label with a 3d effect. Automatically turns off invert. */
  public void set3d(boolean on) {
    is3d = on;
    if (on) {
      invert = false;
    }
    if (on && dColor == -1) {
      onColorsChanged(true); // guich@500_3: create dColor
    }
  }

  /** Inverts the back and fore colors. Automatically turns off 3d. */
  public void setInvert(boolean on) {
    invert = on;
    if (on) {
      is3d = false;
    }
  }

  /** Highlights the text, i.e., paints the text in all directions with a brighter color, then centered, with the foreground color.
   * @see #setHighlightedColor(int)
   * @since TotalCross 1.01
   */
  public void setHighlighted(boolean on) // guich@tc110_14
  {
    highlighted = on;
    if (on) {
      is3d = invert = false;
    }
    highlightColor = userHighlightColor != -1 ? userHighlightColor : Color.brighter(backColor);
  }

  /** The color used when highlighting is on. Defaults to a brighter foreground color.
   * @param c The color to be used as highlighted color. Pass -1 to use the default one.
   * @see #setHighlighted(boolean)
   * @since TotalCross 1.01
   */
  public void setHighlightedColor(int c) // guich@tc110_14
  {
    highlightColor = userHighlightColor = c;
  }

  /** Splits the text to the given width. Remember to set the font (or add the Label to its parent) 
   * before calling this method.
   * @since TotalCross 1.14
   * @see #autoSplit
   */
  public void split(int maxWidth) // guich@tc114_73
  {
    String text = originalText; // originalText will be changed by setText
    setText(Convert.insertLineBreak(maxWidth, fm, text)); // guich@tc126_18: text cannot be assigned here or originalText will be overwritten
    originalText = text;
  }

  /** Sets the text that is displayed in the label. Newline is represented as \n. */
  @Override
  public void setText(String text) {
    originalText = text;
    if (marqueeTimer != null) {
      stopMarquee();
    }
    this.text = autoSplit && width > 0 ? Convert.insertLineBreak(this.width, fm, text) : text;
    lines = this.text.equals("") ? new String[] { "" } : Convert.tokenizeString(this.text, '\n'); // guich@tc100: now we use \n
    currentLine = 0;
    onFontChanged();
    Window.needsPaint = true;
  }

  /** Stops the marquee, but does not change the current text. */
  public void stopMarquee() {
    if (marqueeTimer != null) {
      TimerEvent t = marqueeTimer;
      marqueeTimer = null;
      removeTimer(t);
    }
    marqueeStep = 0;
  }

  /** Gets the text that is displayed in the label. */
  @Override
  public String getText() {
    return text;
  }

  /** Returns the preffered width of this control. */
  @Override
  public int getPreferredWidth() {
    int ret = useFillAsPreferred ? FILL
        : preferredWidthText != null ? fm.stringWidth(preferredWidthText)
            : (getMaxTextWidth() + (insets == null ? 0 : insets.left + insets.right)) + (borderColor == -1 ? 0 : 4);
    if (highlighted || textShadowColor != 0) {
      ret += 2;
    }
    return ret;
  }

  /** Returns the maximum text width for the lines of this Label. */
  public int getMaxTextWidth() {
    int w = 0;
    for (int i = lines.length - 1; i >= 0; i--) {
      if (linesW[i] > w) {
        w = linesW[i];
      }
    }
    return w + (invert || highlighted ? 2 : 0);
  }

  /** Returns the preffered height of this control. */
  @Override
  public int getPreferredHeight() {
    return fmH * lines.length + Edit.prefH + (highlighted ? 2 : 0) + (insets == null ? 0 : insets.top + insets.bottom)
        + (borderColor == -1 ? 0 : 4); // if inverted, make sure the string is surrounded by the black box - guich@401_18: added commonVGap
  }
  
  /** Gets the alpha applied to the text color of this object */
  public int getAlpha() {
     return alpha;
  }
  /** Set the alpha value to be used with the text of this object (0 to 255) */
  public void setAlpha(int alpha) {
     if (alpha != this.alpha) {
        this.alpha = alpha;
        onColorsChanged(true);
     }
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    fColor = Color.interpolateA(foreColor, backColor, alpha); // sholtzer@450_21: added support for setEnabled(false)
    if (is3d) {
      dColor = Color.darker(backColor);
    }
    if (highlighted) {
      highlightColor = userHighlightColor != -1 ? userHighlightColor : Color.brighter(backColor);
    }
  }

  @Override
  protected void onFontChanged() {
    int i;
    if (linesW == null || linesW.length != lines.length) {
      linesW = new int[lines.length];
    }
    int inv = (invert && align == RIGHT) ? 1 : 0; // guich@400_88
    int[] linesW = this.linesW; // guich@450_36: use local var
    for (i = lines.length - 1; i >= 0; i--) {
      linesW[i] = fm.stringWidth(lines[i]) + inv;
    }
  }

  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    if (autoSplit && this.width > 0 && this.width != lastASW) // guich@tc114_74 - guich@tc120_5: only if PREFERRED was choosen in first setRect - guich@tc126_35
    {
      lastASW = this.width;
      split(this.width - (insets == null ? 0 : insets.left + insets.right));
      if (PREFERRED - RANGE <= setH && setH <= PREFERRED + RANGE) {
        setRect(KEEP, KEEP, KEEP, getPreferredHeight() + setH - PREFERRED);
      }
    }
    linesPerPage = height / fmH;
    if (linesPerPage < 1) {
      linesPerPage = 1;
    }
  }

  /** Returns if the label can scroll in the given direction. A Label can scroll if the number of
   * text lines is greater than the actual height. */
  public boolean canScroll(boolean down) // guich@200b4_142
  {
    if (lines.length > linesPerPage) {
      return down ? (currentLine + linesPerPage < lines.length) : (currentLine >= linesPerPage);
    }
    return false;
  }

  /** Scrolls the text to the begining. */
  public void scrollToBegin() {
    currentLine = 0;
  }

  /** Scroll the text to the end. */
  public void scrollToEnd() {
    currentLine = lines.length - linesPerPage;
  }


  /** Scroll to the given line.
   * Can be used to scroll the Label using a ScrollBar. Here's a sample, assuming that sbVert has been added to the container:
   * <pre>
   * sbVert.setMaximum(lab.getLineCount());
   * sbVert.setVisibleItems(lab.getLinesPerPage());
   * sbVert.setUnitIncrement(1);
   * sbVert.setLiveScrolling(true);
   * </pre>
   * At the onEvent:
   * <pre>
   * if (event.type == ControlEvent.PRESSED && event.target == sbVert)
   *    lab.scrollTo(sbVert.getValue());
   * </pre>
   * @since TotalCross 1.2
   */
  public void scrollTo(int line) {
    if (lines.length > linesPerPage && line != linesPerPage && 0 <= line && line < lines.length) {
      currentLine = line;
      Window.needsPaint = true;
    }
  }

  /** Return the number of lines per page of this label.
   * @since TotalCross 1.2
   */
  public int getLinesPerPage() {
    return this.linesPerPage;
  }

  /** Return the number of lines of this label.
   * @since TotalCross 1.2
   */
  public int getLineCount() {
    return lines.length;
  }

  /** Scroll one page. returns true if success, false if no scroll possible
   * @see #pageScroll 
   */
  public boolean scroll(boolean down) {
    int n = pageScroll ? linesPerPage : 1;
    if (lines.length > linesPerPage) {
      int lastLine = currentLine;
      if (down) {
        if (currentLine + linesPerPage < lines.length) {
          currentLine += n;
        }
      } else {
        currentLine -= n;
        if (currentLine < 0) {
          currentLine = 0;
        }
      }
      if (lastLine != currentLine) {
        Window.needsPaint = true;
        return true;
      }
    }
    return false;
  }

  /** Called by the system to draw the button. */
  @Override
  public void onPaint(Graphics g) {
    // draw label
    if (invert) {
      g.foreColor = backColor;
      g.backColor = fColor;
    } else {
      g.foreColor = fColor;
      g.backColor = backColor;
    }
    // guich@200b4_126: repaint the background always.
    if (drawTranslucentBackground(g, alphaValue)) {
      ;
    } else if (!transparentBackground) {
      if (backgroundType == SOLID_BACKGROUND || !isEnabled()) {
        g.fillRect(0, 0, width, height); // guich@200b4_120: make sure the label is painted with the correct color
      } else {
        g.fillShadedRect(0, 0, width, height, true, backgroundType == HORIZONTAL_GRADIENT_BACKGROUND,
            firstGradientColor, secondGradientColor, 100);
        g.foreColor = invert ? backColor : fColor;
        g.backColor = invert ? fColor : backColor;
      }
    }
    if (borderColor != -1) {
      g.translate(2, 2);
    }
    if (text.length() > 0) {
      int y;
      switch (vAlign) {
      case TOP:
        y = (insets == null ? 0 : insets.top);
        break;
      case BOTTOM:
        y = this.height - fmH * Math.min(lines.length, linesPerPage) - (insets == null ? 0 : insets.bottom);
        break;
      default:
        y = ((this.height - fmH * Math.min(lines.length, linesPerPage)) >> 1)/* + (insets == null ? 0 : insets.top)*/;
        break; // guich@tc115_34: min of lines.length and linesPerPage
      }
      if (marqueeStep != 0) {
        int shadow = textShadowColor != -1 ? textShadowColor : highlighted ? highlightColor : -1;
        g.drawText(lines[0], marqueeX, y, shadow != -1, shadow);
        if (marqueeStep < 0) {
          if (marqueeX <= -linesW[0]) {
            if (--marqueeCount == 0) {
              setText("");
            } else {
              marqueeX = width - (insets == null ? 0 : insets.right);
            }
          }
        } else // > 0
        if (marqueeX >= width) {
          if (--marqueeCount == 0) {
            setText("");
          } else {
            marqueeX = -linesW[0] + (insets == null ? 0 : insets.left);
          }
        }
      } else if (lines != null && lines.length > 0) {
        int n = Math.min(currentLine + linesPerPage, lines.length);
        int x0 = (insets == null ? 0 : insets.left);
        int xx = invert || highlighted ? 1 : 0/* + x0*/;
        int fmH = this.fmH; // guich@450_36: use local var
        int[] linesW = this.linesW; // same
        for (int i = currentLine; i < n; i++, y += fmH) {
          int justify = align == FILL && (i < lines.length - 1)
              ? this.width - 1 - (insets == null ? 0 : insets.left + insets.right) : 0; // don't justify the text line
          int x = x0;
          if (align != LEFT) {
            if (align == CENTER) {
              x = (width - linesW[i]) >> 1;
              if (x < x0) {
                x = x0; // guich@tc114_70
              }
            } else if (align == RIGHT) {
              x = width - linesW[i] - (insets == null ? 0 : insets.right);
            }
          }

          if (is3d) // if 3d, invert = false
          {
            g.foreColor = dColor;
            g.drawText(lines[i], xx + x + 1, y + 1, justify);
            g.foreColor = fColor;
            g.drawText(lines[i], xx + x, y, justify);
          } else if (0 <= i && i < lines.length) {
            g.drawText(lines[i], xx + x, y, justify, textShadowColor != -1, textShadowColor);
          }
        }
      }
    }
    if (borderColor != -1) {
      g.translate(-2, -2);
      g.foreColor = borderColor;
      g.drawRect(0, 0, width, height);
    }
  }

  /** This method does nothing: clear a label is usually not a desired action.
   * However, if you really want to clear it, set its clearValueStr property. */
  @Override
  public void clear() // guich@572_19
  {
    if (clearValueStr != null) {
      setText(clearValueStr);
    }
  }
}
