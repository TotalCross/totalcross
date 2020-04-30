// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.effect;

import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Control;
import totalcross.ui.PushButtonGroup;
import totalcross.ui.Window;
import totalcross.ui.Flick;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;

public class MaterialEffect extends UIEffects implements PenListener, TimerListener {
  public interface SideEffect {
    public void sideStart();
    public void sideStop();
    public void sidePaint(Graphics g, int alpha);
  }

  public SideEffect sideEffect;

  private static final int TIMER_INTERVAL = 10;
  private TimerEvent te;
  private final Control target;
  private int px, py, maxRippleRadius, alpha, penDownTimestamp, penUpTimestamp;
  private boolean isPenDown, sideEffectsOnly;
  private int x = X_UNSET, y, w, h;
  private static final int X_UNSET = X_UNKNOWN + 1; // first time x will be set
  private static final int X_WAS_UNKNOWN = X_UNKNOWN + 2; // x was set but was unknown, dont ask to set again until animation stops

  public MaterialEffect(Control target) {
    this.target = target;
    target.addPenListener(this);
    if (target instanceof SideEffect) {
      sideEffect = (SideEffect) target;
    }
    
    darkSideOnPress = target instanceof Button || target instanceof PushButtonGroup;
  }

  @Override
  public boolean isRunning() {
    return te != null;
  }
  
  @Override
  protected void finalize() {
    target.removePenListener(this);
  }

  @Override
  public void paintEffect(Graphics g) {
    if (te != null && enabled) {
      int oldAlpha = g.alpha;
      int oldBackColor = g.backColor;
      if (x == X_UNSET && x != X_WAS_UNKNOWN) {
        x = target.getEffectX();
        if (x == X_UNKNOWN) {
          x = X_WAS_UNKNOWN;
          return;
        }
        y = target.getEffectY();
        w = target.getEffectW();
        h = target.getEffectH();
      }
			
      g.setClip(x, y, w, h);

      int ts = Vm.getTimeStamp();
      int curDn = ts - penDownTimestamp; // total elapsed
      int curUp = ts - penUpTimestamp;
      int rad = Math.min(maxRippleRadius, curDn * maxRippleRadius / duration);
      int rest = duration - (penUpTimestamp - penDownTimestamp);
      if (isPenDown) { // pen down
        alpha = alphaValue;
      } else if (rad < maxRippleRadius) { // pen up and didnt reach max radius
        alpha = alphaValue - curUp * alphaValue / rest; // fading in?
      } else if ((penUpTimestamp - penDownTimestamp) < duration) { // reached max radius and if there is still
                                     // time to fade out...
        alpha = (duration - curDn) * alphaValue / duration; // fading out
      } else {
        alpha = (penUpTimestamp + duration - ts) * alphaValue / duration; // fading out
      }
      if (alpha < 0) {
        alpha = 0;
      }

      if (!sideEffectsOnly) {
        g.alpha = alpha << 24;
//        if (darkSideOnPress) {
//          g.expandClipLimits(2, 2, -2, -2);
//        }
        int bc = target.getBackColor();
        int olda = g.alpha;
        if (isPenDown) {
          g.alpha = alphaValue << 24;
        }
        g.backColor = color != -1 && color != bc ? color
            : Color.getBrightness(bc) < 127 ? Color.brighter(bc, 64) : Color.darker(bc, 64);
            
        g.backColor = Color.PremultiplyAlpha(target.getBackColor(), g.backColor, g.alpha);
        g.fillCircle(px, py, rad - 2);
        g.alpha = olda;
//        if (isPenDown && darkSideOnPress) // make darker area at sides and bottom
//        {
//          g.alpha = 0x40000000;
//          g.backColor = 0;
//          g.backColor = Color.PremultiplyAlpha(target.getBackColor(), g.backColor, g.alpha);
//          g.fillRect(2, h - 2, w - 4, 2);
//          g.fillRect(0, h / 10, 2, h);
//          g.fillRect(w - 2, h / 10, 2, h);
//        }
      }
      if (sideEffect != null) {
        sideEffect.sidePaint(g, alpha);
      }
      
      g.alpha = oldAlpha;
      g.backColor = oldBackColor;
    }
  }
  

  @Override
  public void penDown(PenEvent e) {
      if (!sideEffectsOnly && Flick.currentFlick == null) { // guich@20171004 - if material is applied during a flick, it halts the flick making a strange effect
        if (isRunning()) {
          postEvent();
        }
        x = X_UNSET;
        px = e.x;
        py = e.y;
        isPenDown = true;
        if (this.target.getDoEffect()) {
            start(false);
        }
      }
  }

  // Used to postpone the penUp event until the effect ends
  PenEvent savedPenUp;

  @Override
  public void penUp(PenEvent e) {
    if (target.isInsideOrNear(e.x, e.y)) {
        savedPenUp = e.clone();
    }
    
    if (sideEffectsOnly) {
      start(true);
    }
    if (sideEffect == null && enabled && this.target.effect == this && this.target.getDoEffect()) {
      e.consumed = true; // post pressed event only when effect finishes
    }
    penUpTimestamp = Vm.getTimeStamp();
    isPenDown = false;
  }

  @Override
  public void timerTriggered(TimerEvent e) {
    if (te.triggered) {
      if (alpha == 0) {
        stop();
      } else {
       Window.needsPaint = true;
      }
    }
  }

  @Override
  public void startEffect() {
    if (target.isDisplayed()) {
      px = py = 0;
      start(true);
    }
  }

  private void start(boolean useSideEffectsOnly) {
    sideEffectsOnly = useSideEffectsOnly;
    penDownTimestamp = Vm.getTimeStamp();
    if (useSideEffectsOnly) {
      penUpTimestamp = penDownTimestamp;
    }
    // computes the maximum ripple radius needed
    int rpx = Math.abs(px - target.getEffectX());
    int rpy = Math.abs(py - target.getEffectY());
    int w = Math.max(rpx, target.getEffectW() - rpx);
    int h = Math.max(rpy, target.getEffectH() - rpy);
    maxRippleRadius = (int) Math.sqrt(w * w + h * h) + 1;
    Window.needsPaint = true;
    
    te = target.addTimer(TIMER_INTERVAL);
    target.addTimerListener(this);
    if (sideEffect != null) {
      sideEffect.sideStart();
    }
  }

  private void stop() {
    if (sideEffect != null) {
      sideEffect.sideStop();
    }
    sideEffectsOnly = false;
    target.removeTimer(te);
    target.removeTimerListener(this);
    te = null;
    isPenDown = false;
    postEvent();
  }

  private void postEvent() {
    if (savedPenUp != null && sideEffect == null && !target.hadParentScrolled()) {
      target.onEvent(savedPenUp);
    }
  }

  @Override
  public void penDrag(DragEvent e) { }

  @Override
  public void penDragStart(DragEvent e) { }

  @Override
  public void penDragEnd(DragEvent e) { }
}
