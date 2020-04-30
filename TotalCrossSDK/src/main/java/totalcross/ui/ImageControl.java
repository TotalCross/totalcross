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

import java.util.List;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.EventHandler;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.SizeChangeEvent;
import totalcross.ui.event.SizeChangeHandler;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageControlTarget;
import totalcross.ui.image.ImageException;

/** A control that can show an image bigger than its area and that can be dragged using a pen to show the hidden parts.
  Note that, by default, events (and dragging) are disabled. You must call setEventsEnabled to allow dragging.
 */

public class ImageControl extends Control {
  /** The amount to scroll when in penless mode. Defaults to 10. */
  public static int scrollValue = 10;
  private Image img, img0, imgBack;
  private int startX, startY;
  private Coord c = new Coord();
  private boolean isEventEnabled, canDrag, isPressedEventsEnabled;
  private double lasthwScale;
  /** Set to true to center the image in the control when it is loaded */
  public boolean centerImage;

  /** The last position used for X and Y. */
  public int lastX, lastY;

  /** Change this member to set the border color.
   * You may also set it to -1 if you don't want a border color.
   * Note: starting on TotalCross 3.1, the border is drawn around the color and no longer around the image,
   * because it was not working on OpenGL devices.
   */
  public int borderColor = -1;
  /** Set to true to let the image be dragged beyond container limits. 
   * Should be false for open gl. */
  public boolean allowBeyondLimits;

  /** Dumb field to keep compilation compatibility with TC 1 */
  public int drawOp;

  /** Set to true to enable zooming in open gl devices. */
  public boolean hwScale = Settings.isOpenGL || Settings.onJavaSE;

  private static final double NOTEMP = Convert.MIN_DOUBLE_VALUE;
  /** Temporary values to set the hwScaleW/hwScaleH to during draw. */
  public double tempHwScale = NOTEMP;

  /** Set to true to scale the image to fit the bounds. */
  public boolean scaleToFit;

  /** Set to true, with scaleToFit, to strech the image. */
  public boolean strechImage;
  
  public ImageControlTarget target;

  /** Constructs an ImageControl using the given image. */
  public ImageControl(Image img) {
    this();
    setImage(img);
  }

  /** Constructs with no initial image. You must set the image with the setImage method. */
  public ImageControl() {
    isEventEnabled = false;
    focusTraversable = true;
  }

  /** Pass true to enable dragging and events on the image. */
  public void setEventsEnabled(boolean enabled) {
    focusTraversable = isEventEnabled = enabled;
  }

  /** Pass true to enable this ImageControl to send Press events. Note that it will disable drag and resize of the image. */
  public void setPressedEventsEnabled(boolean enabled) {
    focusTraversable = isPressedEventsEnabled = enabled;
    if (isEventEnabled && enabled) {
      isEventEnabled = false;
    }
    effect = enabled ? UIEffects.get(this) : null;
    if (effect != null) {
      effect.alphaValue = 0xA0;
    }
  }

  /** Sets the image to the given one. If the image size is different, you must explicitly call
   * setRect again if you want to resize the control.
   */
  public void setImage(Image img) {
    setImage(img, true);
  }

  /** Sets the image to the given one, optionally resetting the image position. If the image size is different, you must explicitly call
   * setRect again if you want to resize the control.
   */
  public void setImage(Image img, boolean resetPositions) {
    this.img = this.img0 = img;
    if (resetPositions) {
      c.x = c.y = lastX = lastY = 0;
      tempHwScale = NOTEMP;
    }
    // test if it is really loaded.
    if (img != null && getImageWidth() > 0) {
      scaleImage();
      if (centerImage && resetPositions) {
        lastX = (width - getImageWidth()) / 2;
        lastY = (height - getImageHeight()) / 2;
      }
    }
    Window.needsPaint = true;
  }

  @Override
  public void onEvent(Event event) {
    if (img == null) {
      return;
    }
    if (event instanceof SizeChangeEvent && !event.consumed) {
        if (target != null) {
          target.sizeChanged(width, height);
        } else {
          scaleImage();
          if (centerImage && img != null) // guich@100_1: reset the image's position if bounds changed
          {
            lastX = (width - getImageWidth()) / 2;
            lastY = (height - getImageHeight()) / 2;
          } else {
            lastX = lastY = 0;
          }
        }
        return;
    }
    
    PenEvent pe;
    if (isPressedEventsEnabled && event.type == PenEvent.PEN_UP && !hadParentScrolled()
        && isInsideOrNear(((PenEvent) event).x, ((PenEvent) event).y)) {
      postPressedEvent();
    } else if (isEventEnabled) {
      switch (event.type) {
      case MultiTouchEvent.SCALE:
        if (hwScale) {
          if (tempHwScale == NOTEMP) {
            tempHwScale = 1;
          }
          double step = ((MultiTouchEvent) event).scale;
          double newScale = tempHwScale * step;
          if (newScale > 0) {
            tempHwScale = newScale;
            // always centers on screen
            lastX = (width - getImageWidth()) / 2;
            lastY = (height - getImageHeight()) / 2;
            repaintNow();
          }
        }
        break;
      case KeyEvent.ACTION_KEY_PRESS:
        canDrag = !canDrag;
        break;
      case KeyEvent.SPECIAL_KEY_PRESS:
        KeyEvent ke = (KeyEvent) event;
        if (ke.isDownKey()) {
          moveTo(lastX, lastY - scrollValue);
        } else if (ke.isUpKey()) {
          moveTo(lastX, lastY + scrollValue);
        } else if (ke.key == SpecialKeys.RIGHT) {
          moveTo(lastX - scrollValue, lastY);
        } else if (ke.key == SpecialKeys.LEFT) {
          moveTo(lastX + scrollValue, lastY);
        } else if (ke.isActionKey()) {
          parent.setHighlighting();
        }
        break;
      case PenEvent.PEN_DOWN:
        pe = (PenEvent) event;
        startX = pe.x - lastX; // save the start relative to the last point clicked
        startY = pe.y - lastY;
        break;
      case PenEvent.PEN_DRAG:
        if (getImageWidth() > this.width || getImageHeight() > this.height || allowBeyondLimits) {
          pe = (PenEvent) event;
          if (moveTo(pe.x - startX, pe.y - startY)) {
            pe.consumed = true;
          }
        }
        break;
      case ControlEvent.FOCUS_OUT:
        canDrag = false;
        break;
      }
    }
  }

  /** Moves to the given coordinates, respecting the current moving policy regarding <i>allowBeyondLimits</i>.
   * @return True if the image's position was changed. 
   */
  public boolean moveTo(int newX, int newY) {
    int lx = lastX;
    int ly = lastY;
    if (allowBeyondLimits) {
      lastX = newX;
      lastY = newY;
    } else {
      if (getImageWidth() > width) {
        lastX = Math.max(width - getImageWidth(), Math.min(newX, 0)); // don't let it move the image beyond its bounds
      }
      if (getImageHeight() > height) {
        lastY = Math.max(height - getImageHeight(), Math.min(newY, 0));
      }
    }
    if (lx != lastX || ly != lastY) {
      repaintNow();
      return true;
    }
    return false;
  }

  private static Coord getSize(Image img, int newSize, boolean isHeight) {
    int w = !isHeight ? newSize : (newSize * img.getWidth() / img.getHeight());
    int h = isHeight ? newSize : (newSize * img.getHeight() / img.getWidth());
    return new Coord(w, h);
  }

  private void scaleImage() {
    if (scaleToFit) {
      try {
        if (img0 == null) {
          this.img = null;
        } else if (strechImage) {
          this.img = safeScale(this.width, this.height);
        } else {
          Coord onW = getSize(img0, width, false);
          Coord onH = getSize(img0, height, true);
          if (onW.x <= width && onW.y <= height) {
            this.img = safeScale(onW.x, onW.y);
          } else {
            this.img = safeScale(onH.x, onH.y);
          }
        }
        if (this.img != null) {
          this.img.alphaMask = img0.alphaMask;
        }
      } catch (ImageException e) {
        // keep original image
      }
    }
  }

  private Image safeScale(int w, int h) throws ImageException {
    return img0.getHwScaledInstance(w, h);
  }

  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    translateFromOrigin(c);
    postEvent(new SizeChangeEvent(this, width, height));
  }

  @Override
  public void onPaint(Graphics g) {
    paint(g, true);
  }

  private void paint(Graphics g, boolean drawBack) {
    g.backColor = isEnabled() ? backColor : Color.interpolate(backColor, parent.backColor);
    if (!transparentBackground) {
      g.fillRect(0, 0, width, height);
    }
    if (img != null) {
      drawImage(g, false);
    }
    if (drawBack && imgBack != null) {
      drawImage(g, true);
    }
    if (borderColor != -1) {
      g.foreColor = borderColor;
      g.drawRect(0, 0, width, height);
    }
    if (getDoEffect() && effect != null && img != null) {
      effect.paintEffect(g);
    }
  }

  private void drawImage(Graphics g, boolean isBack) {
    Image temp = isBack ? imgBack : tempHwScale == NOTEMP ? img : img0;
    double dw = temp.hwScaleW, dh = temp.hwScaleH;
    double scaleX = 1, scaleY = 1;
    if (!isBack && tempHwScale != NOTEMP) { // scale the original image instead of the resized one
      double w = temp.getWidth(), h = temp.getHeight();
      scaleX = w / width;
      scaleY = h / height;
      if (!strechImage) {
        Coord onW = getSize(img0, width, false);
        if (onW.x <= width && onW.y <= height) {
          scaleY = scaleX;
        } else {
          scaleX = scaleY;
        }
      }
      if (lasthwScale != tempHwScale) { // if scale changed, center image again
        lasthwScale = tempHwScale;
        lastX = (int) (width - w * tempHwScale / scaleX) / 2;
        lastY = (int) (height - h * tempHwScale / scaleY) / 2;
      }
    }
    if (tempHwScale != NOTEMP) {
        try {
          /** TODO
           * The correct solution must use hwScale.
           * However hwScale is not working correctly on totalcross skia version yet.
           * This might consumes more memory to scale
           */
          temp = temp.scaledBy(tempHwScale/scaleX, tempHwScale/scaleY);
        } catch (ImageException e) {
            e.printStackTrace();
        }
    }
    if (allowBeyondLimits) {
      g.drawImage(temp, lastX, lastY, true);
    } else {
      g.copyRect(temp, 0, 0, temp.getWidth(), temp.getHeight(), lastX, lastY);
    }
    temp.hwScaleW = dw;
    temp.hwScaleH = dh;
  }

  /** Returns the image's width; when scaling, returns the scaled width. */
  public int getImageWidth() {
    return img == null ? 0 : tempHwScale != NOTEMP ? (int) (img.getWidth() * tempHwScale) : img.getWidth();
  }

  /** Returns the image's height; when scaling, returns the scaled height. */
  public int getImageHeight() {
    return img == null ? 0 : tempHwScale != NOTEMP ? (int) (img.getHeight() * tempHwScale) : img.getHeight();
  }

  @Override
  public int getPreferredWidth() {
    return img != null ? Math.min(getImageWidth(), Settings.screenWidth)
        : imgBack != null ? imgBack.getWidth() : Settings.screenWidth; // guich@tc115_35
  }

  @Override
  public int getPreferredHeight() {
    return img != null ? Math.min(getImageHeight(), Settings.screenHeight)
        : imgBack != null ? imgBack.getHeight() : Settings.screenHeight; // guich@tc115_35
  }

  /** Returns the current image assigned to this ImageControl. */
  public Image getImage() {
    return img;
  }

  /** Returns the original image assigned to this ImageControl. Note that getImage() returns the image scaled, while getOriginalImage() returns the unscaled image. */
  public Image getOriginalImage() {
    return img0;
  }

  /** Sets the given image as a freezed background of this image control. */
  public void setBackground(Image img) {
    imgBack = img;
  }

  /** Returns the background image set with setBackground */
  public Image getBackground() {
    return imgBack;
  }

  /** Gets an image representing the portion being shown. If all image is being shown,
   * returns the currently assigned image. */
  public Image getVisibleImage(boolean includeBackground) throws ImageException {
    Rect rImg = new Rect(lastX, lastY, getImageWidth(), getImageHeight());
    Rect rArea = getRect();
    rArea.x = rArea.y = 0;
    Image ret = img;
    if (!rArea.contains(rImg.x, rImg.y) || !rArea.contains(rImg.x2(), rImg.y2())) {
      rImg.intersectWith(rArea);
      ret = new Image(rImg.width, rImg.height);
      Graphics g = ret.getGraphics();
      if (!includeBackground) {
        paint(getGraphics(), false); // remove the background image
      }
      g.copyRect(this, rImg.x, rImg.y, rImg.width, rImg.height, 0, 0);
      if (!includeBackground) {
        paint(getGraphics(), true);
      }
    }
    return ret;
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) // guich@tc111_9
  {
    if (!canDrag) {
      return null;
    }
    if (ke.isDownKey()) {
      moveTo(lastX, lastY - scrollValue);
    } else if (ke.isUpKey()) {
      moveTo(lastX, lastY + scrollValue);
    } else if (ke.key == SpecialKeys.RIGHT) {
      moveTo(lastX - scrollValue, lastY);
    } else if (ke.key == SpecialKeys.LEFT) {
      moveTo(lastX + scrollValue, lastY);
    }
    return this;
  }
}
