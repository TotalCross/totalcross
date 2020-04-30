// Copyright (C) 2000-2012 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.game;

import totalcross.ui.font.Font;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Hashtable;

/**
 * A game specific, fast text renderer (no memory allocs during runtime). <br>
 * Important! The text displayed must not reach the screen limits!<br>
 * <br>
 * It renders the text and the digits in image buffers and the display
 * functions of this class just copies the image buffers back to the
 * graphic context.<br>
 * <br>
 * The text buffer is copied first, then the configured amount of digits
 * of the optional integer are copied next to the text with leading zeros.<br>
 * <br>
 * This class can be used to display game information such as scores, levels, etc.
 * <br>
 * <p>
 * You can find a complete game API sample named 'Scape' in the TotalCross examples folder.<br>
 * Here is some sample code:
 *
 * <pre>
 * import totalcross.game.*;
 * import totalcross.util.props.*;
 * ...
 *
 * <i>public class Ping extends <U>GameEngine</U></i> {
 *
 *   // 2 text renderers to quickly display level and score values
 *
 *   private TextRenderer levelRenderer;
 *   private TextRenderer scoreRenderer;
 *
 * //---------------------------------------------------------
 * // overload the API's game init event.
 * // this function is called when the game is launched.
 * //---------------------------------------------------------
 *
 * <i>public void onGameInit()</i> {
 *
 *     // create two text renderers, one for the 'level' and one for the 'score'
 *     // we use the current font, the text should be black on the game background
 *
 *     // level display has 2 digits: max value is 99
 *
 *     levelRenderer=createTextRenderer(getFont(),Color.BLACK,"level:",2);
 *
 *     // score display has 5 digits: max value is 99999
 *
 *     scoreRenderer=createTextRenderer(getFont(),Color.BLACK,"score:",5);
 *   }
 *
 * //---------------------------------------------------------
 * // overload TotalCross' onPaint function.
 * //---------------------------------------------------------
 *
 * <i>public void onPaint(Graphics gfx)</i> {
 *
 *   ...
 *   if (gameIsRunning) {  // game engine's running state
 *
 *     ...
 *     // display the game level & score, both enable transparency
 *
 *     levelRenderer.display(15,2,level,true);
 *     scoreRenderer.display(80,2,score,true);
 *     ...
 *   }
 *   ...
 * }
 * @author Frank Diebolt
 * @author Guilherme Campos Hazan
 * @version 1.1
 */
@Deprecated
public class TextRenderer {
  /*
   * share all digit renderings by font.
   */
  private static Hashtable byFont = new Hashtable(13);
  private static String strDigits[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

  private Image[] digits;
  private Image textImg;
  private int w0;

  private int maxDigits;
  private Graphics gfx;

  /**
   * height in pixels of the drawing (= font metrics height).
   */
  protected int fmH;
  /**
   * width in pixels of the text.
   */
  protected int textWidth;

  /**
   * enable left zero padding.
   */
  protected boolean zeroPadding; // fdie@420_27

  /**
   * Creates a text renderer with the given parameters.
   * 
   * @param font
   *           to use
   * @param foreColor
   *           text foreground color
   * @param backColor
   *           text background color
   * @param text
   *           to be displayed before the digits (or null if none)
   * @param maxDigits
   *           the number of digits to display. E.g.: 4 means the max value shown will be 9999.
   * @param zeroPadding
   *           pad with leading zeros.
   * @throws ImageException
   */
  protected TextRenderer(Font font, int foreColor, int backColor, String text, int maxDigits, boolean zeroPadding)
      throws ImageException // fdie@420_27
  {
    this(font, foreColor, backColor, text, maxDigits);
    this.zeroPadding = zeroPadding;
  }

  /**
   * Creates a text renderer with the given parameters.
   * 
   * @param font
   *           to use
   * @param foreColor
   *           text foreground color
   * @param backColor
   *           text background color
   * @param text
   *           to be displayed before the digits (or null if none)
   * @param maxDigits
   *           the number of digits to display. E.g.: 4 means the max value shown will be 9999.
   * @throws ImageException
   */
  protected TextRenderer(Font font, int foreColor, int backColor, String text, int maxDigits) throws ImageException {
    this.maxDigits = maxDigits;

    gfx = GameEngineMainWindow.getEngineGraphics();
    gfx.setFont(font);
    gfx.backColor = backColor;
    gfx.foreColor = foreColor;

    FontMetrics fm = font.fm;

    this.textWidth = text != null ? fm.stringWidth(text) : 0;
    this.w0 = fm.charWidth('0');
    this.fmH = fm.height;

    // lookup the digits in the shared space or build a new digits rendering
    Object o = byFont.get(font);
    if (o == null) {
      // store the new digits renderings
      o = buildDigits(font, backColor, foreColor);
      byFont.put(font, o);
    }
    digits = (Image[]) o;
    if (text != null) {
      textImg = render(text, textWidth, font, backColor, foreColor); // guich@340_10
    }
  }

  /**
   * Display a text rendering.
   * This function just draws the text to the
   * GameEngineMainWindow at the specified x,y position.
   * @param x position.
   * @param y position.
   * @param transparent should the background be preserved.
   */

  public void display(int x, int y, boolean transparent) {
    if (textImg != null) {
      gfx.drawImage(textImg, x, y, false);
    }
  }

  /**
   * Display a text rendering.
   * This function just copies back the image buffer of the text to the
   * GameEngineMainWindow at the specified x,y position and displays the
   * integer value next to the text by using pre-rendered digit image buffers.
   * @param x position.
   * @param y position.
   * @param value positive integer value to draw next to the text.
   */

  public void display(int x, int y, int value) {
    if (maxDigits < 1) {
      return;
    }

    if (textImg != null) {
      gfx.drawImage(textImg, x, y, false);
    }

    if (value < 0) {
      value = 0;
    }
    int numDigits = 0;

    if (zeroPadding) // fdie@420_27
    {
      numDigits = maxDigits;
    } else {
      // compute how many digits intValue has so we can left justify the text
      int tempValue = value;
      do {
        numDigits++;
        tempValue /= 10;
      } while (tempValue != 0);
    }

    x += textWidth + numDigits * w0;

    // scan from least to most significant digit
    while (numDigits-- > 0) {
      x -= w0;
      int d = value % 10;
      gfx.drawImage(digits[d], x, y, false);
      value /= 10;
      if (value == 0 && !zeroPadding) {
        break;
      }
    }
  }

  private Image[] buildDigits(Font font, int backColor, int foreColor) throws ImageException {
    Image array[] = new Image[10];
    for (int i = 0; i < 10; i++) {
      array[i] = render(strDigits[i], w0, font, backColor, foreColor);
    }
    return array;
  }

  private Image render(String text, int w, Font font, int backColor, int foreColor) throws ImageException {
    Image image = new Image(w, fmH);
    Graphics gfx = image.getGraphics();
    if (backColor != -1) {
      gfx.backColor = backColor;
    }
    gfx.fillRect(0, 0, totalcross.sys.Settings.screenWidth, totalcross.sys.Settings.screenHeight);
    gfx.foreColor = foreColor != -1 ? foreColor : Color.WHITE;
    gfx.setFont(font);
    gfx.drawText(text, 0, 0);
    return image;
  }

  /** Returns the maximum width of the text plus the number of digits */
  public int getWidth() {
    return textWidth + (maxDigits * w0);
  }

  /** Returns the height of the current font */
  public int getHeight() {
    return fmH;
  }
}
