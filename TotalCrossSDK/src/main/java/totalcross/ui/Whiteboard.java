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
import totalcross.sys.Vm;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/** This is a whiteboard that can be used to draw something. 
 * It uses a special event flag in order to improve the accuracy.
 */

public class Whiteboard extends Control {
  //to draw Lines
  private int oldX;
  private int oldY;
  private Image img;
  private Graphics gImg;
  private Graphics gScr;
  /** Set this to some color so a frame can be drawn around the image */
  public int borderColor = -1;
  /** Set to true to enable antialiase on the line drawing. It must be set right after the constructor.
   * @deprecated This field is useless in OpenGL platforms 
   */
  @Deprecated
  public boolean useAA;
  private boolean isEmpty = true;

  /** Set to true to draw a thick line.
   * @since TotalCross 1.14
   */
  public boolean thick; // guich@tc114_78

  private int desiredPenColor = -1;

  /** Constructs a new whiteboard, setting the back color to white. */
  public Whiteboard() {
    backColor = Color.WHITE;
    focusTraversable = false; // guich@tc123_13
  }

  /** Now that we know our bounds, we can create the image that will hold the drawing */
  @Override
  public void onBoundsChanged(boolean screenChanged) {
    //if (!screenChanged)
    if (isEmpty) {
      try {
        setImage(null); // resize to width and height
      } catch (ImageException e) {
        Vm.debug("Not enough memory to resize the whiteboard");
      }
    }
  }

  /** Returns the image where the drawing is taking place. */
  public Image getImage() {
    return this.img;
  }

  /** Returns the preferred width: FILL */
  @Override
  public int getPreferredWidth() {
    return FILL;
  }

  /** Returns the preferred height: FILL */
  @Override
  public int getPreferredHeight() {
    return FILL;
  }

  /**
   * Sets the image for this WhiteBoard. Pass null to create an empty image.
   * 
   * @throws ImageException
   */
  public void setImage(Image image) throws ImageException {
    isEmpty = image == null;
    this.img = image == null ? new Image(width, height) : image;
    isEmpty = false;
    this.gImg = img.getGraphics();
    gImg.foreColor = desiredPenColor != -1 ? desiredPenColor : Color.BLACK;
    //gImg.useAA = useAA;
    int lastColor = gImg.foreColor;
    if (image == null) {
      gImg.backColor = backColor;
      if (!transparentBackground) {
        gImg.fillRect(0, 0, width, height);
      }
    }
    if (borderColor != -1) {
      gImg.foreColor = borderColor;
      gImg.drawRect(0, 0, width, height);
    }
    gImg.foreColor = lastColor;

    Window.needsPaint = true;
  }

  /** Clears the WhiteBoard to the current background color. */
  @Override
  public void clear() {
    try {
      setImage(null);
    } catch (Exception e) {
    }
  }

  /** Sets the drawing pen color */
  public void setPenColor(int c) // guich@300_65
  {
    desiredPenColor = c;
    if (gImg != null) {
      gImg.foreColor = c;
    }
    if (gScr != null) {
      gScr.foreColor = c;
    }
  }

  /** Returns the drawing pen color. */
  public int getPenColor() {
    return desiredPenColor;
  }

  @Override
  public void onPaint(Graphics g) {
    if (!Settings.isOpenGL && gScr == null) {
      gScr = getGraphics(); // create the graphics object that will be used to repaint the image
      gScr.setClip(0, 0, width, height);
      //gScr.useAA = useAA;
      if (desiredPenColor != -1) {
        gScr.foreColor = desiredPenColor;
      }
    }
    g.drawImage(img, 0, 0); // draw the image...
  }

  TimerEvent te;

  private void drawTo(Graphics g, int pex, int pey) {
    g.drawLine(oldX, oldY, pex, pey); // guich@580_34: draw directly on screen
    if (thick) {
      g.drawLine(oldX + 1, oldY + 1, pex + 1, pey + 1);
      g.drawLine(oldX - 1, oldY - 1, pex - 1, pey - 1);
      g.drawLine(oldX + 1, oldY + 1, pex - 1, pey - 1);
      g.drawLine(oldX - 1, oldY - 1, pex + 1, pey + 1);
    }
  }

  @Override
  public void onEvent(Event event) {
    PenEvent pe;
    switch (event.type) {
    case TimerEvent.TRIGGERED:
      if (te != null && te.triggered) {
        Window.needsPaint = true;
      }
      break;
    case PenEvent.PEN_DOWN:
      pe = (PenEvent) event;
      oldX = pe.x;
      oldY = pe.y;
      drawTo(gImg, pe.x, pe.y); // after
      if (gScr != null) {
        drawTo(gScr, pe.x, pe.y);
      }
      getParentWindow().setGrabPenEvents(this); // guich@tc100: redirect all pen events to here, bypassing other processings
      if (Settings.isOpenGL) {
        te = addTimer(100);
      }
      break;
    case PenEvent.PEN_DRAG:
      pe = (PenEvent) event;
      drawTo(gImg, pe.x, pe.y); // before
      if (gScr != null) {
        drawTo(gScr, pe.x, pe.y);
      }
      oldX = pe.x;
      oldY = pe.y;
      if (!Settings.isOpenGL) {
        Control.safeUpdateScreen(); // important at desktop!
      }
      break;
    case PenEvent.PEN_UP:
      getParentWindow().setGrabPenEvents(null);
      removeTimer(te);
      Window.needsPaint = true;
      break;
    }
  }
}
