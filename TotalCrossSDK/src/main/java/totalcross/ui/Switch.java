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
import totalcross.util.UnitsConverter;

/** This class implements the on/off switch present in many platforms
 * 
 */
public class Switch extends Control implements PathAnimation.SetPosition, AnimationFinished {
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
      colorBarOff = 0x9b9b9b;
      colorBarOn = 0xa472ea;
      colorBallOn = 0x6200ee;
    }
    this.isIos = !androidType;
    setDoEffect(false);
    effect = UIEffects.get(this);
  }

  @Override
  public void onBoundsChanged(boolean b) {
    dragBarSize = height;
    dragBarMin = 0;
    dragBarMax = width - dragBarSize + (uiMaterial ? 0 : -1);
  }

  @Override
  public void onColorsChanged(boolean b) {
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

      final int perc = dragBarPos * 100 / dragBarMax;
      if (getDoEffect() && effect != null) {
        effect.paintEffect(g);
      }
      boolean on = isOn();
      buildBar(on);
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
        ballOn.alphaMask = alphaValue * perc / 100;
        ballOff.alphaMask = 255 - ballOn.alphaMask;
        if (perc != 100) {
          g.drawImage(ballOff, dragBarPos, 0);
        }
        if (perc != 0) {
          g.drawImage(ballOn, dragBarPos, 0);
        }
        ballOff.alphaMask = ballOn.alphaMask = 255;
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

  private void buildBar(boolean on) throws Exception {
    int h = uiMaterial ? height / 3 * 2 : height;
    int barY = (height - h)/2;
    int fillB = !isEnabled() ? Color.interpolate(backColor, parent.backColor) : backColor;
    Graphics g = getGraphics();
    int gap = UnitsConverter.toPixels(DP + 2);
    g.backColor = on && uiMaterial? colorBarOn : fillB;
    g.fillRoundRect(gap, barY, width - 2 * gap, h, h);
    g.foreColor = Color.getCursorColor(g.backColor);
    g.drawRoundRect(gap, barY, width - 2 * gap, h, h);
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
    return UnitsConverter.toPixels(20 + DP);
  }

  @Override
  public int getPreferredWidth() {
    int textW = Math.max(textBackOn == null ? 0 : fm.stringWidth(textBackOn),
        textBackOff == null ? 0 : fm.stringWidth(textBackOff));
    int btW = UnitsConverter.toPixels(36 + DP);
    return btW + textW;
  }

  @Override
  public void onAnimationFinished(ControlAnimation anim) {
    if (isOn() != wasChecked) {
      postPressedEvent();
    }
  }
}
