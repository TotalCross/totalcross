package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Convert;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.UpdateListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.UnitsConverter;

public class OutlinedEdit extends Edit implements UpdateListener
{
  int spaceX;
  boolean test = false;
  int holeY;
  int executedTime;
  boolean drawLine = true;
  Line lineLeft, lineRight;
  boolean drawSpace;
  int textSpace;
  public int totalTime;
  private int lineColor;
  private boolean focusIn;
  private boolean focusOut;
  private boolean isPlaying;
  public static final int ANIMATION_TIME = 250;

  /**
   * The height, in pixels, of the border of the image used in the Ninepatch, if
   * you won't change the ninepatch then you shouldn't change this field
   * */
  protected int borderHeight;

  public OutlinedEdit()
  {
    cursorShowing = false;
    lineColor = -1;
    setNinePatch(Resources.outlinededit);
    borderHeight = 3;
    this.totalTime = ANIMATION_TIME;
    this.setFont(
      Font.getFont(this.getFont().name, this.getFont().isBold(), 16));
    materialCaption = new FloatingLabel<Edit>(this);
    materialCaption.setTopY(UnitsConverter.toPixels(DP + 14));
    materialCaption.setTopX(0);
  }

  /**
   * Set the NinePatch of the control. This is used to draw the background
   * image. If you change the Ninepatch you should then change the borderHeight
   * using {@link #setBorderHeight(int) setBorderHeight()}.
   * @param img The image with guides to make the NinePatch. The image must be
   *     on a NinePatch format
   * */
  @Override public void setNinePatch(Image img)
  {
    // TODO Auto-generated method stub
    super.setNinePatch(img);
  }

  /**
   * Set the NinePatch of the control. This is used to draw the background
   * image. This also changes the borderHeight.
   * @param img The image with guides to make the NinePatch. The image must be
   *     on a NinePatch format
   * @param borderHeight The height, in pixels, of the border of the image used
   *     in the Ninepatch.
   * */
  public void setNinePatch(Image img, int borderHeight)
  {
    setNinePatch(img);
    this.borderHeight = borderHeight;
  }

  public OutlinedEdit(String mask)
  {
    this();
    this.mask = mask.toCharArray();
    useFillAsPreferred = mask.length() == 0;
  }

  @Override protected void draw(Graphics g)
  {
    try {
      int labelAscentMiddleY =
        (materialCaption.getCaptionFontSmall().fm.ascent - borderHeight) / 2;
      if (npParts != null && (npback == null || focusColor != -1)) {

        npback = NinePatch.getInstance().getNormalInstance(
          npParts,
          width,
          height - labelAscentMiddleY,
          isEnabled() ? hasFocus && focusColor != -1 ? focusColor : back0
                      : (back0 == parent.backColor
                           ? Color.darker(back0, 32)
                           : Color.interpolate(back0, parent.backColor)),
          false);
        npback.alphaMask = alphaValue;
      }
      NinePatch.tryDrawImage(g, npback, 0, labelAscentMiddleY);
    } catch (ImageException e) {
      e.printStackTrace();
    }
    if (lineLeft != null) {
      int old = g.backColor;
      g.backColor = lineColor;
      g.fillRect(lineLeft.x, lineLeft.y, lineLeft.width, lineLeft.height);
      g.fillRect(lineRight.x, lineRight.y, lineRight.width, lineRight.height);
      g.backColor = old;
    }

    int len = chars.length();
    boolean drawCaption = caption != null && !hasFocus && len == 0;
    if (len > 0 || drawCaption || captionIcon != null) {
      if ((selectLast || startSelectPos != -1) &&
          editable) // moved here to avoid calling g.eraseRect (call
                    // fillRect instead) - guich@tc113_38: only if
                    // editable
      {
        // character regions are:
        // 0 to (sel1-1) .. sel1 to (sel2-1) .. sel2 to last_char
        int sel1 =
          selectLast ? insertPos - 1 : Math.min(startSelectPos, insertPos);
        int sel2 = selectLast ? insertPos : Math.max(startSelectPos, insertPos);
        int sel1X = charPos2x(sel1);
        int sel2X = charPos2x(sel2);

        if (sel1X != sel2X) {
          int old = g.backColor;
          g.backColor = back1 == backColor ? Color.brighter(back1) : back1;
          g.fillRect(sel1X, y, sel2X - sel1X + 1, fmH);
          g.backColor = old;
        }
      }

      g.foreColor = fColor;
      int xx = xOffset;
      if (captionIcon != null) {
        xx += getX0();
        g.drawImage(captionIcon, uiMaterial ? 0 : fmH, y);
      }

      if (!hasFocus && !drawCaption) {
        switch (alignment) {
          case RIGHT:
            xx = this.width - getTotalCharWidth() - xOffset;
            break;
          case CENTER:
            xx = (this.width - getTotalCharWidth()) >> 1;
            break;
        }
      }
      if (hasBorder) {
        g.setClip(xMin + (captionIcon != null ? captionIcon.getWidth() : 0),
                  0,
                  xMax - Edit.prefH,
                  height);
      }
      if (drawCaption && !uiMaterial) {
        g.foreColor = captionColor != -1 ? captionColor : this.foreColor;
        g.drawText(caption, xx, y, textShadowColor != -1, textShadowColor);
      } else {
        switch (mode) {
          case PASSWORD: // password fields usually have small text, so this
                         // method does not have to be
            // very optimized
            if (len > 0) {
              g.drawText(Convert.dup('*', len - 1) + chars.charAt(len - 1),
                         xx,
                         materialCaption.ycap0,
                         textShadowColor != -1,
                         textShadowColor);
            }
            break;
          case PASSWORD_ALL:
            g.drawText(Convert.dup('*', len),
                       xx,
                       materialCaption.ycap0,
                       textShadowColor != -1,
                       textShadowColor);
            break;
          case CURRENCY:
            if (isMaskedEdit) {
              xx = this.width - getTotalCharWidth() - xOffset - 1;
            }
          default:
            if (masked.length() > 0) {
              g.drawText(masked,
                         0,
                         masked.length(),
                         xx,
                         materialCaption.ycap0,
                         textShadowColor != -1,
                         textShadowColor);
            } else {
              g.drawText(chars,
                         0,
                         len,
                         xx,
                         materialCaption.ycap0,
                         textShadowColor != -1,
                         textShadowColor);
            }
        }
      }
      if (hasBorder) {
        g.clearClip();
      }
    }
    cursorX = charPos2x(insertPos);
    if (hasFocus && isEnabled() &&
        (editable || hasCursorWhenNotEditable)) // guich@510_18: added check to
                                                // see if it is enabled
    {
      // draw cursor
      if (xMin <= cursorX && cursorX <= xMax) { // guich@200b4_155
        if (cursorShowing) {
          g.clearClip();
          g.backColor = Color.interpolate(backColor, foreColor);
          g.fillRect(cursorX - 1 +
                       (uiMaterial ? UnitsConverter.toPixels(DP + 2) : 0),
                     materialCaption.ycap0,
                     cursorThickness,
                     this.fmH);
        }
      }
    } else {
      cursorShowing = false;
    }

    g.foreColor = captionColor != -1
                    ? captionColor
                    : hasFocus ? backColor : Color.getGray(backColor);
    g.setFont(materialCaption.getFcap());
    g.drawText(caption,
               materialCaption.xcap,
               materialCaption.ycap - materialCaption.getFcap().fm.descent);
  }

  @Override public void onEvent(Event event)
  {
    super.onEvent(event);
    switch (event.type) {
      case ControlEvent.FOCUS_IN:
        if (!isPlaying && materialCaption.isRunning) {
          executedTime = 0;
          setupBorder();
          totalTime = ANIMATION_TIME;
          focusIn = true;
          if (chars.length() > 0) {
            isPlaying = false;
          } else {
            isPlaying = true;
          }

          MainWindow.getMainWindow().addUpdateListener(this);
        }
        break;
      case ControlEvent.FOCUS_OUT:
        if (!isPlaying && materialCaption.isRunning) {
          executedTime = 0;
          focusOut = true;
          MainWindow.getMainWindow().removeUpdateListener(blinkListener);
          if (chars.length() > 0) {
            isPlaying = false;
          } else {
            isPlaying = true;
            MainWindow.getMainWindow().addUpdateListener(this);
          }
        }
        break;
    }
  }

  @Override public void setText(String s)
  {
    if (s == null || s.equals(this.getText())) {
      return;
    }

    super.setText(s);
    setupBorder();
    lineLeft = new Line(
      UnitsConverter.toPixels(DP + 10), holeY, textSpace >> 1, borderHeight);
    lineRight = new Line((textSpace >> 1) + UnitsConverter.toPixels(DP + 10),
                         holeY,
                         textSpace >> 1,
                         borderHeight);
  }

  private void setupBorder()
  {
    textSpace = UnitsConverter.toPixels(DP + 8) +
                (int)((materialCaption.getCaptionFontSmall().fm.stringWidth(
                  this.caption)));
    if (lineColor == -1) {
      try {
        if (npParts != null && (npback == null || focusColor != -1)) {
          int labelAscentMiddleY =
            (materialCaption.getCaptionFontSmall().fm.ascent - borderHeight) /
            2;
          npback = NinePatch.getInstance().getNormalInstance(
            npParts,
            width,
            height - labelAscentMiddleY,
            isEnabled() ? hasFocus && focusColor != -1 ? focusColor : back0
                        : (back0 == parent.backColor
                             ? Color.darker(back0, 32)
                             : Color.interpolate(back0, parent.backColor)),
            false);
        }
      } catch (ImageException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      npback.alphaMask = alphaValue;
      // Calculates the Y for the hole on the border and it's color
      final int imgHeight = npback.getHeight();
      byte[] colors = new byte[npback.getWidth() * 4];
      final int position = materialCaption.getTopX();
      int lastColor = 0;
      npback.getPixelRow(colors, 0);
      if (colors[position * 4 + 3] < 0 ||
          colors[position * 4 + 3] >
            10) { // When the alpha isn't between 0 and 10, inclusive,  there
                  // is some color a this pixel, which
        // means this is the top of the border.
        holeY = height - npback.getHeight();
      } else {
        for (int i = 0; i < imgHeight; i++) {
          npback.getPixelRow(colors, i);
          if (colors[position * 4 + 3] < 0 || colors[position * 4 + 3] > 10) {
            holeY = i + height - npback.getHeight();
            break;
          } else if (i > 0 &&
                     npback.getGraphics().getPixel(position, i) != lastColor) {
            holeY = i + height - npback.getHeight() - 1;
            break;
          } else {
            lastColor = npback.getGraphics().getPixel(position, i);
          }
        }
      }
      npback.getPixelRow(colors, holeY + borderHeight);
      if (colors[position * 4 + 3] < 0 ||
          colors[position * 4 + 3] >
            10) { // When the alpha is low enough it gets the parent's color
                  // to give the trasnsparency.
        lineColor =
          npback.getGraphics().getPixel(position, holeY + borderHeight);
      } else {
        lineColor = this.parent.backColor;
      }
    }
  }

  private class Line
  {
    int x;
    int y;
    int width;
    int height;

    Line(int x, int y, int width, int height)
    {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
  }

  @Override public void updateListenerTriggered(int elapsedMilliseconds)
  {
    executedTime += elapsedMilliseconds;
    int lineWidth = 0;
    float percentage = (float)executedTime / totalTime;
    if (totalTime < executedTime) {
      percentage = 1;
    }
    if (focusIn) {
      lineWidth = ((int)(percentage * textSpace)) >> 1;
    } else if (focusOut) {
      lineWidth = (textSpace - ((int)(percentage * textSpace))) >> 1;
    }
    if (totalTime >= executedTime) {
      if (isPlaying) {
        lineLeft = new Line((textSpace >> 1) +
                              UnitsConverter.toPixels(DP + 10) - lineWidth,
                            holeY,
                            lineWidth,
                            borderHeight);
        lineRight =
          new Line((textSpace >> 1) + UnitsConverter.toPixels(DP + 10),
                   holeY,
                   lineWidth,
                   borderHeight);
      }
    } else {
      if (lineLeft == null || lineWidth != lineLeft.width) {
        lineLeft = new Line((textSpace >> 1) +
                              UnitsConverter.toPixels(DP + 10) - lineWidth,
                            holeY,
                            lineWidth,
                            borderHeight);
        lineRight =
          new Line((textSpace >> 1) + UnitsConverter.toPixels(DP + 10),
                   holeY,
                   lineWidth,
                   borderHeight);
      } else {
        if (focusOut) {
          if (isPlaying) {
            lineLeft = null;
            lineRight = null;
          }
        }
        if (focusIn) {
          MainWindow.getMainWindow().addUpdateListener(blinkListener);
        }
        focusIn = false;
        focusOut = false;
        executedTime = 0;
        isPlaying = false;
        MainWindow.getMainWindow().removeUpdateListener(this);
      }
    }
    Window.needsPaint = true;
  }
  /**
   * Returns the borderHeight.
   * */
  public int getBorderHeight() { return borderHeight; }
  /**
   * Sets the borderHeight, this number should be the height of the border of
   * your ninepatch <b>in pixels</b>.
   * */
  public void setBorderHeight(int borderHeight)
  {
    this.borderHeight = borderHeight;
  }
}
