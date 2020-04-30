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

import totalcross.sys.Settings;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.GfxSurface;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * This class implements a game Sprite. <br>
 * <br>
 * A sprite is a graphical object typically used in games that can be moved,
 * with collision detection feature and background saving/restoring capability.<p>
 * The sprite attributes are:<br>
 * <p>
 * <li>position<br>
 * The sprite position <U>centerX,centerY</U> is in fact its center position.<br>
 * <br>
 * <li>size<br>
 * <U>width/height</U> contains respectively the image horizontal/vertical dimensions.<br>
 * <br>
 * <li>half size<br>
 * <U>halfWidth/halfHeight</U> contains respectively the half horizontal/vertical dimensions.<br>
 * <br>
 * <li>valid region<br>
 * The valid region of a sprite are relative to the sprite center. The valid region
 * can be provided in the Sprite <i>constructor</i> or by the <i>setRegion</i> call.
 * Both are totalcross.ui.gfx.Rect objects. If null is passed in the constructor the default region
 * is computed to prevent the sprite living even partially the screen.<br>
 * Once set the region can be modified by incrementing/decrementing the values of
 * <U>regionMinx</U>, <U>regionMiny</U>, <U>regionMaxx</U> and <U>regionMaxy</U><br>
 * <br>
 * <li>draw operation<br>
 * The default value of <U>drawOp</U> is DRAW_PAINT
 * if its value is DRAW_PAINT the image is copied "as is" to the buffer, and<br>
 * if its value is DRAW_SPRITE the transparency color specified in the <i>constructor</i>
 * is used to prevent the copy of image pixels of this color in order to preserve the
 * background.
 * <p>
 * You can find a complete game API sample named 'Scape' in the TotalCross samples folder.<br>
 * Here is some sample code:
 *
 * <pre>
 * // Ball is a the Sprite bouncing on the screen.
 * // The ball bounces on the left, the top and the bottom border and
 * // must be hit by the racket on the right border to keep the ball inside the screen.
 * // If the racket misses the ball, the game is over.
 *
 * <i>public final class Ball extends Sprite</i> {
 *
 *   // random generator for the starting speeds
 *   private Random rand=new Random();
 *
 *   // keep a reference to the game mainclass for data access & game control.
 *   private Ping game;
 *
 *   // other important sprite the ball sprite must know about for interaction...
 *   private Racket racket;
 *
 *  // ball movement speed in both directions in double values for acceleration precision
 *  private double speedx,speedy;
 *
 *  // and in integer format for quicker move computing.
 *  private int ispeedx,ispeedy;
 *
 *   //----------------------------------------------------------------
 *   // Ball constructor.
 *   // @param game the Ping game mainclass.
 *   // @param racket Sprite that interacts with the ball.
 *   //----------------------------------------------------------------
 *
 *   <i>public Ball(Ping game,Racket racket)</i> {
 *
 *     // setup the sprite by loading the bitmap, defining the transparency color.
 *     // The ball element does not fill the whole bitmap, the corners are filled
 *     // with WHITE pixels. By setting WHITE as the transparency color and selecting the
 *     // DRAW_SPRITE draw mode, the bitmap white pixels are not shown and the background
 *     // is not affected by the bitmap display. The background saving feature is disabled
 *     // because the whole screen is cleared and redrawn at each frame. No valid region
 *     // (null) means, default valid region is right. Except that the miny value must be
 *     // incremented to prevent the ball hiding the s
 *
 *     super(new Image("ball.bmp"),Color.WHITE,false,null);
 *     drawOp=Graphics.DRAW_SPRITE;
 *
 *     // the ball may go out of the window when the user misses it
 *     doClip = true;
 *
 *     // by default the valid area of a sprite is the screen.
 *     // reduce the playfield of the racket by the level & score display zone
 *     regionMiny+=Ping.GAME_MINY;
 *
 *     this.game=game;
 *     this.racket=racket;
 *
 *     // initialize the ball
 *     reinit();
 *   }
 *
 *   // initialization needed each time a game starts.
 *   // The racket is placed in the middle of the right border and the ball
 *   // is placed next to it.
 *   // Also defines the initial ball speed.
 *
 *   <i>public void reinit()</i> {
 *
 *     Coord pos=racket.getBallPosition(halfWidth);
 *     setPos(ballHitX=pos.x,pos.y,false);
 *
 *     speedx=4+rand.nextFloat()*6.0f;
 *     speedy=2+rand.nextFloat()*3.0f;
 *     ispeedx=-1;
 *     ispeedy=-1;
 *
 *     // the toward position specified in the towardPos call is not a
 *     // direction but an ending position, so speed is irrelevant.
 *
 *     speed=1000;
 *     ...
 *   }
 *
 *   //----------------------------------------------------------------
 *   // Move the ball to next position depending on the current speed.
 *   //----------------------------------------------------------------
 *
 *   <i>public void move()</i> {
 *
 *     // add the speed vector and enable position validation.
 *     // see the "Scape" sample code for more details about position validation.
 *     // this towardPos() moves the sprite toward a point, the third parameter
 *     // enables position validation which is needed for each position change to
 *     // precisely detect the racket or the borders hits.
 *     // This call could be optimized to disable the time consuming position
 *     // validation when the ball is not near the screen border.
 *
 *     towardPos(centerX+ispeedx,centerY+ispeedy,true);
 *   }
 * </pre>
 * @author Frank Diebolt
 * @author Guilherme Campos Hazan
 * @version 1.1
 */
@Deprecated
public class Sprite {
  /**
   * Sprite center position.
   */
  public int centerX, centerY;

  /**
   * Sprite width/height dimensions. <b>READ-ONLY</b> attributes.
   */
  public int width, height;

  /**
   * Sprite valid region. <br>
   * The sprite cannot leave this area if the default position validation function is not overriden.
   * 
   * @see #onPositionChange
   */
  protected int regionMinx, regionMiny, regionMaxx, regionMaxy;

  /**
   * Sprite image.
   */
  public Image image;

  /**
   * Graphics to draw at.
   */
  protected Graphics gfx;

  /**
   * Surface to draw at.
   */
  protected GfxSurface surface;

  private boolean multiFrame;

  /**
   * Speed in pixels used by the function towardPos.
   * 
   * @see #towardPos
   */
  public int speed = 8;

  // cached double buffering with screen erasing
  protected boolean screenErased;
  /** set to false if this sprite never reaches the screen boundaries. Default is true. */
  public boolean doClip = true;

  protected final static int INVALID = -500;

  /**
   * Sprite constructor. <br>
   * 
   * @param image
   *           sprite image.
   * @param transColor
   *           sprite's transparency color or -1 if none<br>
   *           (needed in DRAW_SPRITE mode to keep the current background).
   * @param saveBckgd
   *           true if the background should be saved each time the sprite is drawn to restore it once the sprite
   *           moves.
   * @param region
   *           defines the sprite valid area.<br>
   *           If null, a default region is set to prevent the sprite to leave even partially the screen.
   * @throws ImageException
   * @throws IllegalStateException
   * @throws IllegalArgumentException
   */
  public Sprite(Image image, int transColor, boolean saveBckgd, Rect region)
      throws IllegalArgumentException, IllegalStateException, ImageException {
    this(image, image.getFrameCount(), transColor, saveBckgd, region);
  }

  /**
   * Sprite constructor. <br>
   * 
   * @param image
   *           sprite image.
   * @param transColor
   *           sprite's transparency color or -1 if none<br>
   *           (needed in DRAW_SPRITE mode to keep the current background).
   * @param saveBckgd
   *           true if the background should be saved each time the sprite is drawn to restore it once the sprite
   *           moves.
   * @param region
   *           defines the sprite valid area.<br>
   *           If null, a default region is set to prevent the sprite to leave even partially the screen.
   * @throws ImageException
   * @throws IllegalStateException
   * @throws IllegalArgumentException
   */
  public Sprite(Image image, int nrFrames, int transColor, boolean saveBckgd, Rect region)
      throws IllegalArgumentException, IllegalStateException, ImageException {
    gfx = GameEngineMainWindow.getEngineGraphics();
    surface = GameEngineMainWindow.getSurface();
    image.setFrameCount(nrFrames);

    screenErased = true;
    /* GameEngineMainWindow.engine.gameIsDoubleBuffered */;
    multiFrame = image.getFrameCount() > 1;

    this.image = image;
    width = image.getWidth();
    height = image.getHeight();
    if (width <= 0 || height <= 0) {
      throw new GameEngineException("bad Sprite bitmap");
    }

    saveBackground(saveBckgd);
    if (region == null) {
      regionMinx = width / 2;
      regionMiny = height / 2;
      regionMaxx = Settings.screenWidth - regionMinx;
      regionMaxy = Settings.screenHeight - regionMiny;
    } else {
      setRegion(region);
    }
  }

  /**
   * Background image.
   */
  protected Image background;

  /**
   * Background graphic context.
   */
  protected Graphics bgGfx;

  /**
   * Background restoring position.
   */
  protected int bgX, bgY;

  /**
   * Enable/disable background saving. <br>
   * 
   * @param enable
   *           true if background have to be saved and restored at each drawing.
   * @throws ImageException
   */
  private void saveBackground(boolean enable) throws ImageException {
    if (enable && !Settings.isOpenGL) {
      background = new Image(width, height);
      bgGfx = background.getGraphics();
    } else if (background != null) {
      bgGfx = null;
      background = null;
    }
    bgX = INVALID;
  }

  /**
   * Retrieve the sprite valid region. <br>
   * <B>NOTE</B>: should be called cautiously due to a Rect object alloc.
   * 
   * @return positions validity region
   */
  public final Rect getRegion() {
    return new Rect(regionMinx, regionMiny, regionMaxx - regionMinx + 1, regionMaxx - regionMiny + 1);
  }

  /**
   * Change the sprite valid region.
   * 
   * @param region new positions validity area
   */
  public final void setRegion(Rect region) {
    regionMinx = region.x;
    regionMiny = region.x;
    regionMaxx = region.x2();
    regionMaxy = region.y2();
  }

  /**
   * Default position validation function. <br>
   * This function can be overloaded to define your own sprite position validation or collision detection.<br>
   * The overloaded function may use the sprite data members and should update the sprite position centerX and centerY
   * if needed.
   * 
   * @return true if the (centerX,centerY) is a valid position, false if it's not and the position has been corrected.<br>
   *         <B>NOTE</B>: false returns will stop the movements of the towardPos function.
   * @see #towardPos
   */
  public boolean onPositionChange() {
    boolean b = true;
    if (centerX < regionMinx) {
      centerX = regionMinx;
      b = false;
    } else if (centerX > regionMaxx) {
      centerX = regionMaxx;
      b = false;
    }
    if (centerY < regionMiny) {
      centerY = regionMiny;
      b = false;
    } else if (centerY > regionMaxy) {
      centerY = regionMaxy;
      b = false;
    }
    return b;
  }

  /**
   * Sets the sprite position (actually its center). <br>
   * Note: if doValidate is false, you may consider just setting centerX and centerY attributes directly (it is 60%
   * faster).
   * 
   * @param x position.
   * @param y position.
   * @param doValidate if true the position is validated which means that the onPositionChange() function is called.
   * @return true if the defined position has been set
   * @see #onPositionChange
   */
  public final boolean setPos(int x, int y, boolean doValidate) {
    centerX = x;
    centerY = y;
    if (doValidate) {
      return onPositionChange();
    }
    return true;
  }

  /**
   * Retrieve the sprite position (actually its center). <br>
   * <B>NOTE</B>: should be called cautiously due to a Coord object alloc. You may also consider accessing the centerX
   * and centerY directly.
   * 
   * @return sprite's center position
   */
  public final Coord getPos() {
    return new Coord(centerX, centerY);
  }

  /**
   * Moves the sprite toward the specified position. <br>
   * This function is typically used when the user points the end position with the pen. The object position is
   * computed to move smoothly from it's current position to the target position.
   * 
   * @param x position.
   * @param y position.
   * @param doValidate if true the position is validated which means that the onPositionChange() function is called.<br>
   *           <B>NOTE</B>: a false return of onPositionChange() will stop the towardPos function.
   * @return true if the defined position has been set
   * @see #onPositionChange
   */
  public boolean towardPos(int x, int y, boolean doValidate) {
    int dx = x - centerX;
    int dy = y - centerY;
    int steps;

    if (dx == 0) // vertical move
    {
      steps = Math.min(dy >= 0 ? dy : -dy, speed);
      if (dy < 0) {
        centerY -= steps;
      } else if (dy > 0) {
        centerY += steps;
      }
      if (doValidate) {
        return onPositionChange();
      }
    } else if (dy == 0) // horizontal move
    {
      steps = Math.min(dx >= 0 ? dx : -dx, speed);
      if (dx < 0) {
        centerX -= steps;
      } else if (dx > 0) {
        centerX += steps;
      }
      if (doValidate) {
        return onPositionChange();
      }
    } else {
      // diagonal moves
      // derived from TOTALCROSS drawLine algorithm, thx to Guich.
      // It's Bresenham's fastest implementation!

      dx = dx >= 0 ? dx : -dx; // store the change in X and Y of the line endpoints
      dy = dy >= 0 ? dy : -dy;

      int CurrentX = centerX; // store the starting point (just point A)
      int CurrentY = centerY;

      // DETERMINE "DIRECTIONS" TO INCREMENT X AND Y (REGARDLESS OF DECISION)
      int Xincr = (centerX > x) ? -1 : 1; // which direction in X?
      int Yincr = (centerY > y) ? -1 : 1; // which direction in Y?

      // DETERMINE INDEPENDENT VARIABLE (ONE THAT ALWAYS INCREMENTS BY 1 (OR -1) )
      // AND INITIATE APPROPRIATE LINE DRAWING ROUTINE (BASED ON FIRST OCTANT
      // ALWAYS). THE X AND Y'S MAY BE FLIPPED IF Y IS THE INDEPENDENT VARIABLE.

      steps = speed;

      if (dx >= dy) // if X is the independent variable
      {
        int dPr = dy << 1; // amount to increment decision if right is chosen (always)
        int dPru = dPr - (dx << 1); // amount to increment decision if up is chosen
        int P = dPr - dx; // decision variable start value

        for (; dx >= 0 && steps > 0; dx--) // process each point in the line one at a time (just use dX)
        {
          centerX = CurrentX; // update the sprite's position
          centerY = CurrentY;

          if (doValidate && !onPositionChange()) {
            return false;
          }

          CurrentX += Xincr; // increment independent variable
          steps--;
          if (P > 0) // is the pixel going right AND up?
          {
            CurrentY += Yincr; // increment dependent variable
            steps--;
            P += dPru; // increment decision (for up)
          } else {
            // is the pixel just going right?
            P += dPr; // increment decision (for right)
          }
        }
      } else
      // if Y is the independent variable
      {
        int dPr = dx << 1; // amount to increment decision if right is chosen (always)
        int dPru = dPr - (dy << 1); // amount to increment decision if up is chosen
        int P = dPr - dy; // decision variable start value

        for (; dy >= 0 && steps > 0; dy--) // process each point in the line one at a time (just use dY)
        {
          centerX = CurrentX; // update the sprite's position
          centerY = CurrentY;

          if (doValidate && !onPositionChange()) {
            return false;
          }

          CurrentY += Yincr; // increment independent variable
          steps--;
          if (P > 0) // is the pixel going up AND right?
          {
            CurrentX += Xincr; // increment dependent variable
            steps--;
            P += dPru; // increment decision (for up)
          } else {
            // is the pixel just going up?
            P += dPr; // increment decision (for right)
          }
        }
      }
    }
    return true;
  }

  /**
   * Draw the sprite at it's current position using the defined drawOp. <br>
   * If the Sprite has been created with enabled background saving (usefull only when the screen is not cleared before
   * the new frame display) the previously stored background will be restored first and the sprite is displayed in a
   * second step.<br>
   * <B>NOTE</B>: if several sprite moves simultaneously and may overlap the background restoring may erase a
   * previously drawn sprite. In this case, the only solution is to call explicitly the hide() function on all the
   * sprites in the DRAWN REVERSE ORDER.<br>
   * By doing so, you always restore a clean image without sprites before starting a new draw cycle of all the sprites.
   */
  public void show() {
    int w2 = width >> 1;
    int h2 = height >> 1;
    // are we in background restoring mode ?
    if (!Settings.isOpenGL && background != null) {
      // Is it not the first paint ?
      if (bgX != INVALID) {
        // position didn't change, what are we doing here ?
        if (!screenErased && bgX == centerX && bgY == centerY) {
          return;
        }
        // buffer -> screen
        gfx.copyRect(background, 0, 0, width, height, bgX - w2, bgY - h2);
      }
      // screen -> buffer
      bgX = centerX;
      bgY = centerY;
      bgGfx.copyRect(surface, bgX - w2, bgY - h2, width, height, 0, 0);
    }
    // copy the sprite image to the graphic context
    gfx.drawImage(image, centerX - w2, centerY - h2, doClip);
    if (multiFrame) {
      image.nextFrame();
    }
  }

  /**
   * Restore the sprite's saved background. <br>
   * <B>NOTE</B>: the position may have change, the saved background position has been memorized also, to restore it at
   * the right place.
   */
  public void hide() {
    if (background == null || bgX == INVALID) {
      return;
    }
    gfx.copyRect(background, 0, 0, width, height, bgX - width / 2, bgY - height / 2); // buffer -> screen
    bgX = INVALID;
  }

  /**
   * Test if the sprite collides with another one. <br>
   * 
   * @param s sprite to test with
   * @return true if both sprite overlaps, false otherwise
   */
  public boolean collide(Sprite s) {
    int w2 = width >> 1;
    int h2 = height >> 1;
    int sw2 = s.width >> 1;
    int sh2 = s.height >> 1;
    return !((s.centerX + sw2 <= centerX - w2) || (s.centerY + sh2 <= centerY - h2) || (s.centerX - sw2 >= centerX + w2)
        || (s.centerY - sh2 >= centerY + h2));
  }
}
