package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Settings;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/** This class implements the on/off switch present in many platforms
 * 
 */
public class Switch extends Control implements PathAnimation.SetPosition, AnimationFinished {
  private Image barOn, barOff;
  private Image ballOn, ballOff;
  private boolean isIos, dragged, wasChecked;
  private int startDragPos, dragBarPos, dragBarSize, dragBarMin, dragBarMax;
  /** The animation time. Set to 0 to disable animations in all Switches. */
  public static int ANIMATION_TIME = 200;

  /** Text to draw when on background or foreground, and when this switch is on or off */
  public String textBackOn, textForeOn, textBackOff, textForeOff;

  /** Text color to draw when on background or foreground, and when this switch is on or off */
  public int colorBackOn, colorForeOn, colorBackOff, colorForeOff;

  /** Colors used in material */
  public int colorBallOn, colorBallOff, colorBarOn, colorBarOff;

  /** Set to true to center text instead of moving it to left or right */
  public boolean centerText;

  /** Constructs a switch of iOS type. In material UI, there is only one type */
  public Switch() {
    this(false);
  }

  /** Constructs a switch of the given type. In material UI, there is only one type */
  public Switch(boolean androidType) {
    foreColor = 0x05B6EE;
    backColor = 0xDDDDDD;
    if (uiMaterial) {
      colorBallOff = Color.WHITE;
      colorBarOff = 0x9E9E9E;
      colorBarOn = 0xFDA6A6;
      colorBallOn = 0xFF5252;
    }
    this.isIos = !androidType;
    effect = UIEffects.get(this);
  }

  @Override
  public void onBoundsChanged(boolean b) {
    barOn = ballOn = ballOff = null;
    dragBarSize = height;
    dragBarMin = 0;
    dragBarMax = width - dragBarSize + (uiMaterial ? 1 : -1);
  }

  @Override
  public void onColorsChanged(boolean b) {
    barOn = ballOn = ballOff = null;
    //effect.color = Color.brighter(backColor);
  }

  /** Change the on/off state. */
  public void setOn(boolean b) {
    if (b != isOn()) {
      moveSwitch(!b);
    }
  }

  /** Returns true if this switch is ON. Note that, if this Switch has animations, you must call this method only after the animation finishes */
  public boolean isOn() {
    return dragBarPos + dragBarSize / 2 >= width / 2;
  }

  public void moveSwitch(boolean toLeft) {
    int destPos = toLeft ? dragBarMin : dragBarMax;
    if (ANIMATION_TIME == 0) {
      dragBarPos = destPos;
      if (isOn() != wasChecked) {
        postPressedEvent();
      }
    } else {
      if (dragBarPos != destPos) {
        try {
          PathAnimation p = PathAnimation.create(this, dragBarPos, 0, destPos, 0, this, ANIMATION_TIME);
          p.useOffscreen = false;
          p.setpos = this;
          p.start();
        } catch (Exception ee) {
          if (Settings.onJavaSE) {
            ee.printStackTrace();
          }
          dragBarPos = destPos;
        }
      } else if (isOn() != wasChecked) {
        postPressedEvent();
      }
    }
  }

  /** Used by animation */
  @Override
  public void setPos(int x, int y) // PathAnimation.SetPosition
  {
    dragBarPos = x;
    Window.needsPaint = true;
  }

  @Override
  public void onPaint(Graphics g) {
    try {
      if (ballOn == null) {
        buildBall();
      }
      if (barOn == null) {
        buildBar();
      }

      int perc = dragBarPos * 255 / (width - dragBarSize);
      if (effect != null) {
        effect.paintEffect(g);
      }
      boolean on = isOn();
      int dy = uiMaterial ? height / 4 : 0;
      if (!uiMaterial) {
        barOn.alphaMask = alphaValue;
        g.drawImage(barOn, 0, dy);
        barOn.alphaMask = 255;
      } else {
        barOff.alphaMask = (255 - perc) * alphaValue / 255;
        barOn.alphaMask = perc * alphaValue / 255;
        if (perc != 255) {
          g.drawImage(barOff, 0, dy);
        }
        if (perc != 0) {
          g.drawImage(barOn, 0, dy);
        }
        barOff.alphaMask = barOn.alphaMask = 255;
      }

      // text
      if (on && textBackOn != null) // text at left
      {
        g.foreColor = colorBackOn;
        int ww = fm.stringWidth(textBackOn);
        g.drawText(textBackOn, centerText ? (width - ww) / 2 : (width - dragBarSize - ww) / 2, (height - fmH) / 2);
      } else if (!on && textBackOff != null) // text at right
      {
        g.foreColor = colorBackOff;
        int ww = fm.stringWidth(textBackOn);
        g.drawText(textBackOff, centerText ? (width - ww) / 2 : (width - dragBarSize - ww) / 2 + dragBarSize,
            (height - fmH) / 2);
      }
      // ball
      if (!uiMaterial) {
        g.drawImage(ballOn, dragBarPos, 0);
      } else {
        ballOff.alphaMask = (255 - perc) * alphaValue / 255;
        ballOn.alphaMask = perc * alphaValue / 255;
        if (perc != 255) {
          g.drawImage(ballOff, dragBarPos, 0);
        }
        if (perc != 0) {
          g.drawImage(ballOn, dragBarPos, 0);
        }
        ballOff.alphaMask = ballOff.alphaMask = 255;
      }
      // text
      if (on && textForeOn != null) // text at left
      {
        g.foreColor = colorForeOn;
        g.drawText(textForeOn, dragBarPos + (dragBarSize - fm.stringWidth(textForeOn)) / 2, (height - fmH) / 2);
      } else if (!on && textForeOff != null) // text at right
      {
        g.foreColor = colorForeOff;
        g.drawText(textForeOff, dragBarPos + (dragBarSize - fm.stringWidth(textForeOff)) / 2, (height - fmH) / 2);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    // draw button
  }

  private void buildBall() throws Exception {
    int h = uiMaterial || isIos ? height : height - 2;
    ballOn = (uiMaterial ? Resources.switchBtn : isIos ? Resources.switchBtnIos : Resources.switchBtnAnd)
        .getSmoothScaledInstance(h, h);
    if (uiMaterial) {
      ballOff = ballOn.getCopy();
    }
    ballOn.applyColor2(uiMaterial ? colorBallOn : foreColor);
    if (uiMaterial) {
      ballOff.applyColor2(uiMaterial ? colorBallOff : backColor);
    }
  }

  private void buildBar() throws Exception {
    int h = uiMaterial ? height / 2 : height;
    barOn = new Image(width, h);
    Image barLR = (uiMaterial ? Resources.switchBrd : isIos ? Resources.switchBrdIos : Resources.switchBrdAnd)
        .smoothScaledFixedAspectRatio(uiMaterial || isIos ? h : h - 2, true); // left/right
    int bw = barLR.getWidth() / 2;
    Image barMid = uiMaterial ? null : Resources.switchBack.getSmoothScaledInstance(width - 2 * bw, h); // mid

    Graphics gg = barOn.getGraphics();
    if (!uiMaterial) {
      gg.drawImage(barMid, bw, isIos ? 0 : -1);
    } else {
      gg.backColor = Color.WHITE;
      gg.fillRect(bw, 2, width - 2 * bw, h - 3);
    }
    // draw left
    gg.setClip(0, 0, bw, h);
    gg.drawImage(barLR, 0, 0);
    // draw right
    gg.setClip(width - bw - 1, 0, bw, h);
    gg.drawImage(barLR, width - 2 * bw - 1, 0);
    gg.clearClip();

    if (uiMaterial) {
      barOff = barOn.getCopy();
    }

    int fillB = !isEnabled() ? Color.interpolate(backColor, parent.backColor) : backColor;
    barOn.applyColor2(uiMaterial ? colorBarOn : fillB);
    if (uiMaterial) {
      barOff.applyColor2(uiMaterial ? colorBarOff : fillB);
    }
  }

  @Override
  public void onEvent(Event event) {
    if (isEnabled()) {
      switch (event.type) {
      case PenEvent.PEN_DRAG_START:
      case PenEvent.PEN_DRAG_END:
        break;
      case PenEvent.PEN_DRAG:
        if (startDragPos != -1) {
          int newDragBarPos = getPos(event);
          if (newDragBarPos != dragBarPos) {
            dragged = true;
            dragBarPos = newDragBarPos;
            Window.needsPaint = true;
          }
        }
        break;
      case PenEvent.PEN_DOWN:
        wasChecked = isOn();
        dragged = false;
        startDragPos = ((PenEvent) event).x - dragBarPos;
        break;
      case PenEvent.PEN_UP:
        Window.needsPaint = true;
        if (!hadParentScrolled()) {
          if (!dragged) {
            moveSwitch(isOn());
          } else {
            dragBarPos = getPos(event);
            boolean nowAtMidLeft = dragBarPos + dragBarSize / 2 < width / 2;
            moveSwitch(nowAtMidLeft);
          }
        }
        startDragPos = -1;
        break;
      }
    }
  }

  private int getPos(Event event) {
    int newDragBarPos = ((PenEvent) event).x - startDragPos;
    if (newDragBarPos < dragBarMin) {
      newDragBarPos = dragBarMin;
    } else if (newDragBarPos > dragBarMax) {
      newDragBarPos = dragBarMax;
    }
    return newDragBarPos;
  }

  @Override
  public int getPreferredHeight() {
    return fmH + Edit.prefH;
  }

  @Override
  public int getPreferredWidth() {
    int textW = Math.max(textBackOn == null ? 0 : fm.stringWidth(textBackOn),
        textBackOff == null ? 0 : fm.stringWidth(textBackOff));
    int btW = fmH + Edit.prefH;
    return textW == 0 ? btW * 2 : btW * 3 / 2 + textW;
  }

  @Override
  public void onAnimationFinished(ControlAnimation anim) {
    if (isOn() != wasChecked) {
      postPressedEvent();
    }
  }
}
