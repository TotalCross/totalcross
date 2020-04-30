// Copyright (C) 2001 Daniel Tauchke 
// Copyright (C) 2001-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Settings;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/**
 * ScrollBar is an implementation of a Scrollbar.
 * The scrollbar orientation can be horizontal or vertical.
 * It implements auto scroll when pressing and holding a button or the gap area of the scrollBar.
 * Here is an example of how to use it.
 * <pre>
 *  // declarations
 *  ScrollBar sb1;
 *  ScrollBar sb2;
 *  ScrollBar sb3;
 *  ScrollBar sb4;
 *  // init
 *  public void testScrollBars()
 *  {
 *     add(sb1 = new ScrollBar(ScrollBar.VERTICAL), RIGHT, CENTER, PREFERRED, totalcross.sys.Settings.screenHeight/2);
 *     add(sb2 = new ScrollBar(ScrollBar.VERTICAL), BEFORE, SAME, PREFERRED, SAME);
 *     sb2.setLiveScrolling(true);
 *     add(sb3 = new ScrollBar(ScrollBar.HORIZONTAL), LEFT,CENTER,totalcross.sys.Settings.screenWidth/2, PREFERRED);
 *     add(sb4 = new ScrollBar(ScrollBar.HORIZONTAL), SAME, AFTER, SAME, PREFERRED);
 *  }
 *
 *  public void onEvent(Event event)
 *  {
 *     if (event.type == ControlEvent.PRESSED && event.target == sb2)
 *     {
 *        int value = sb2.getValue();
 *        sb1.setValue(value);
 *        sb3.setValue(value);
 *        sb4.setValue(value);
 *     }
 *  }
 * </pre>
 */

public class ScrollBar extends Container implements PathAnimation.SetPosition, ControlAnimation.AnimationFinished {
  /** To be passed in the constructor */
  public static final byte VERTICAL = 1;
  /** To be passed in the constructor */
  public static final byte HORIZONTAL = 2;

  /** Set to true to disable block increments, which occurs when the user clicks outside the bar and buttons.
   * @since TotalCross 1.3.4 
   */
  public boolean disableBlockIncrement;

  protected int maximum = 100;
  protected int minimum;
  protected int blockIncrement = 50;
  protected int unitIncrement = 1;
  protected int value;
  protected int visibleItems = 50;

  protected double valuesPerPixel;
  protected int dragBarSize;
  protected int btnWH;

  protected ArrowButton btnInc, btnDec;
  protected int startDragPos = -1;
  protected boolean verticalBar;

  protected int dragBarPos;
  protected boolean liveScrolling;
  protected int size;
  protected int dragBarMax, dragBarMin;
  protected int bColor, sbColor, sfColor, sbColorDis;
  protected int fourColors[] = new int[4];
  
  protected NinePatch.Parts npPartsHandle;
  // material ui
  protected boolean directMove;
  protected int midBarSize;
  private int oldPos = -1;
  private PathAnimation pathanim;
  int thumbSize; // guich@tc134: used in ScrollPosition
  /** The minimum dragbar size in pixels. By default, 5. This has no effect if the ui style is Palm OS. */
  public int minDragBarSize = 5 * Settings.screenHeight / 160; // guich@510_26

  /* luciana@570_22: Implements auto scroll when pressing and holding button or scrollBar */
  protected boolean enableAutoScroll = true; // guich@tc134
  private TimerEvent autoScrollTimer;
  private Control autoScrollTarget;
  private boolean buttonScroll;
  private int autoScrollBarPos;
  /** The initial delay to start the automatic scroll. */
  public static int INITIAL_DELAY = 600;
  /** The frequency in which the scroll will be done. */
  public static int AUTO_DELAY = 100;

  /** The extra size (width for vertical ScrollBars or height for horizontal ones) used in all ScrollBars.
   * Note that this member is static so it will affect all ScrollBars created afterwards, unless you reset it to 0.
   * @since TotalCross 1.14
   */
  public static int extraSize; // guich@tc114_48   

  /** Creates Scrollbar with default values:<br>
   *  maximum = 100 <br>
   *  minimum = 0<br>
   *  orientation = VERTICAL<br>
   *  blockIncrement = 50<br>
   *  unitIncrement = 1<br>
   *  value = 0<br>
   *  visibleItems = blockIncrement<br>
   */
  public ScrollBar() {
    this(VERTICAL);
  }

  /** Creates Scrollbar with the given orientation and these default values:<br>
   *  maximum = 100 <br>
   *  minimum = 0<br>
   *  blockIncrement = 50<br>
   *  unitIncrement = 1<br>
   *  value = 0<br>
   *  visibleItems = blockIncrement<br>
   */
  public ScrollBar(byte orientation) {
    ignoreOnAddAgain = ignoreOnRemove = true;
    this.verticalBar = orientation == VERTICAL;
    int extra = Settings.fingerTouch ? 3 : 0;
    btnDec = new ArrowButton(verticalBar ? Graphics.ARROW_UP : Graphics.ARROW_LEFT, fmH * 3 / 11 + extra, Color.BLACK);
    btnInc = new ArrowButton(verticalBar ? Graphics.ARROW_DOWN : Graphics.ARROW_RIGHT, fmH * 3 / 11 + extra,
        Color.BLACK);
    btnDec.focusTraversable = btnInc.focusTraversable = false;
    add(btnDec);
    add(btnInc);
    btnDec.setBorder(Button.BORDER_3D);
    btnInc.setBorder(Button.BORDER_3D);
    started = true; // avoid calling the initUI method
    this.focusTraversable = true; // kmeehl@tc100
    onFontChanged();
  }

  /** Sets the value, visibleItems, minimum and maximum values */
  public void setValues(int newValue, int newVisibleItems, int newMinimum, int newMaximum) {
    maximum = newMaximum;
    minimum = newMinimum;
    visibleItems = Math.max(newVisibleItems, 1);
    blockIncrement = visibleItems; // /  4
    if (visibleItems + newValue + minimum <= maximum) {
      value = newValue;
    } else {
      value = maximum - visibleItems; // msicotte@502_5: don't let value get more than maximum
    }
    if (value < minimum) {
      value = minimum; // neither less than minimum
    }
    recomputeParams(false);
    Window.needsPaint = true;
  }

  /** Set the maximum value. Note that you must explicitly call repaint. */
  public void setMaximum(int i) {
    if (i != maximum) // guich@320_30
    {
      maximum = i;
      if (value > i) {
        value = i; // guich@421_56
      }
      recomputeParams(false);
    }
  }

  /** Get the maximum value */
  public int getMaximum() {
    return maximum;
  }

  /** Set the minimum value */
  public void setMinimum(int i) {
    if (i != minimum) {
      minimum = i;
      if (value < i) {
        value = i; // guich@521_56
      }
      recomputeParams(false);
      Window.needsPaint = true;
    }
  }

  /** Get the minimum value */
  public int getMinimum() {
    return minimum;
  }

  /** Set the amount to increment the value when clicking above the bar.
       This value is set as default to be equal to visibleItems. */
  public void setBlockIncrement(int i) {
    blockIncrement = i;
  }

  /** Get the amount to increment the value when clicking above the bar. */
  public int getBlockIncrement() {
    return blockIncrement;
  }

  /** Set the amount to increment the value when clicking the up or down buttons */
  public void setUnitIncrement(int i) {
    unitIncrement = i;
    Window.needsPaint = true;
  }

  /** Get the amount to increment the value when clicking the up or down buttons */
  public int getUnitIncrement() {
    return unitIncrement;
  }

  /** Sets the value. */
  public void setValue(int i) {
    if (i != value) // guich@320_30
    {
      value = (visibleItems + i/*+minimum*/ <= maximum) ? i : (maximum - visibleItems); // guich@510_25: if greater than the maximum, set to the max. guich@tc100: removed +minimum, because if min=6,max=22,vis.items=1,value=18, it would fail
      if (value < minimum) {
        value = minimum; // guich@tc100: don't let the value get under the minimum
      }
      recomputeParams(true); // can't remove this line.
      Window.needsPaint = true;
    }
  }

  /** Get the value. This is the value minus the visible items. */
  public int getValue() {
    return value;
  }

  /** Set the count of visible items for the scrollbar. This value cannot be zero.
   * It also sets the blockIncrement to be equal to the given value.
   */
  public void setVisibleItems(int i) {
    visibleItems = i;
    if (visibleItems <= 0) {
      visibleItems = 1;
    }
    blockIncrement = visibleItems;
    recomputeParams(false);
    Window.needsPaint = true;
  }

  /** Get the count of visible items for the scrollbar */
  public int getVisibleItems() {
    return visibleItems;
  }

  /** Set the live scrolling. If "true" an event is thrown during dragging or when the button is held. */
  public void setLiveScrolling(boolean liveScrolling) {
    this.liveScrolling = liveScrolling;
  }
  
  /**Set the Handle NinePatch image*/
  public void setNinePatchHandle(Image img, int corner, int side) {
	  npPartsHandle = NinePatch.getInstance().load(img, corner, side);
  }

  protected void recomputeParams(boolean justValue) {
    if (size <= 0) {
      return;
    }
    if (!justValue) {
      // Calculate and draw the slider button
      int delta = Math.max(visibleItems, maximum - minimum);
      int barArea = size - (btnWH << 1);
      if (thumbSize != 0) {
        barArea -= thumbSize - (barArea * visibleItems / delta);
      }
      if (uiFlat) {
        barArea += 2; // on the flat mode, the bar area starts over the buttons
      }
      valuesPerPixel = (double) barArea / (double) delta;
      dragBarSize = thumbSize != 0 ? thumbSize
          : Math.max(minDragBarSize, Math.min(barArea, (int) (valuesPerPixel * visibleItems) + 1)); // guich@300_13
      dragBarMin = uiFlat ? (btnWH - 1) : btnWH;
      dragBarMax = size - dragBarMin - dragBarSize;
    }
    // guich@20170628 - ensure that value is in the correct range
    if (value > maximum - visibleItems) {
      value = maximum - visibleItems;
    }
    if (value < minimum) {
      value = minimum;
    }
    dragBarPos = Math.min(dragBarMax, dragBarMin + (int) (valuesPerPixel * (value - minimum) + 0.5d)); // round value - guich@512_12: subtract minimum from value
    enableButtons();
  }

  @Override
  public void onEvent(Event event) {
    int oldValue = value, pos;
    boolean mustPostEvent = false;
    switch (event.type) {
    case TimerEvent.TRIGGERED: // luciana@570_22
      if (autoScrollTimer == null) {
        break; // vinicius@584_7
      }
      if (autoScrollTarget == this) {
        autoScrollTimer.millis = AUTO_DELAY;
        onAutoScroll();
      } else {
        autoScrollTimer.millis = AUTO_DELAY;
        postEvent(getPressedEvent(autoScrollTarget));
      }
      break;
    case ControlEvent.PRESSED:
      if (event.target == btnDec) {
        value = Math.max(minimum, value - unitIncrement);
        event.consumed = true;
        if (!buttonScroll) {
          requestFocus();
        }
      } // guich@220_12: unitIncrement now is being used
      else if (event.target == btnInc) {
        value = Math.min(maximum, value + unitIncrement);
        event.consumed = true;
        if (!buttonScroll) {
          requestFocus();
        }
      }
      //mustPostEvent = true; afarine@350_15: avoid extra 2 events when pressing a button - the event will be posted in the penUp event
      break;
    case PenEvent.PEN_DRAG_START:
    case PenEvent.PEN_DRAG_END:
      pos = verticalBar ? ((PenEvent) event).y : ((PenEvent) event).x;
      if (disableBlockIncrement && (pos < dragBarPos || pos > dragBarPos + dragBarSize)) {
        event.consumed = true;
      }
      return;
    case PenEvent.PEN_DOWN:
      if (pathanim != null) {
        pathanim.stop(true);
        pathanim = null;
      }
      oldPos = dragBarPos;
      if (event.target == this) // can be the buttons
      {
        if (enableAutoScroll) {
          autoScrollTimer = addTimer(INITIAL_DELAY);
          autoScrollTarget = this;
        }
        pos = verticalBar ? ((PenEvent) event).y : ((PenEvent) event).x;
        this.autoScrollBarPos = pos;
        int margin = Settings.fingerTouch ? fmH / 2 : 0;
        if (!directMove && pos < dragBarPos - margin) {
          if (!disableBlockIncrement) {
            value -= blockIncrement;
          } else {
            event.consumed = true;
          }
        } else if (!directMove && pos > dragBarPos + dragBarSize + margin) {
          if (!disableBlockIncrement) {
            value += blockIncrement;
          } else {
            event.consumed = true;
          }
        } else {
          startDragPos = pos - dragBarPos + midBarSize; // point inside drag bar
          Window.needsPaint = true;
        }
      } else if (enableAutoScroll && (event.target == btnInc || event.target == btnDec)) // luciana@570_22
      {
        autoScrollTimer = addTimer(INITIAL_DELAY);
        autoScrollTarget = (Control) event.target;
        buttonScroll = true;
      }
      break;
    case PenEvent.PEN_DRAG:
      oldPos = -1;
      if (event.target == btnDec || event.target == btnInc) // kmeehl@tc100: cancel the scroll timer when the user drags off of the buttons
      {
        Control btn = (Control) event.target;
        PenEvent pe = (PenEvent) event;
        if (!btn.isInsideOrNear(pe.x, pe.y) && autoScrollTimer != null) // guich@tc114_51: must add control's coordinates
        {
          disableAutoScroll();
          break;
        }
      }
      pos = verticalBar ? ((PenEvent) event).y : ((PenEvent) event).x;
      if (disableBlockIncrement && (pos < dragBarPos || pos > dragBarPos + dragBarSize)) {
        event.consumed = true;
        return;
      }
      if (startDragPos != -1) {
        updateValue(pos);
      }
      autoScrollBarPos = verticalBar ? ((PenEvent) event).y : ((PenEvent) event).x;
      event.consumed = true;
      Event.clearQueue(PenEvent.PEN_DRAG);
      break;
    case PenEvent.PEN_UP:
      if (autoScrollTimer != null) {
        disableAutoScroll();
      }
      Window.needsPaint = true;
      mustPostEvent = btnDec.isEnabled() || btnInc.isEnabled();
      pos = verticalBar ? ((PenEvent) event).y : ((PenEvent) event).x;
      startDragPos = -1; // requested by ScrollPosition 
      if (directMove && oldPos >= 0) {
        int value = (int) ((pos - dragBarMin) / valuesPerPixel);
        if (unitIncrement != 1) {
          value = ((int) value / unitIncrement) * unitIncrement;
        }
        pos = (int) (value * valuesPerPixel) + midBarSize;
        if (pos < dragBarMin + midBarSize) {
          pos = dragBarMin + midBarSize;
        } else if (pos > dragBarMax - midBarSize - 1) {
          pos = dragBarMax - midBarSize - 1;
        }

        pathanim = PathAnimation.create(this, oldPos, 0, pos, 0, this, UIEffects.duration);
        pathanim.useOffscreen = false;
        pathanim.setpos = this;
        pathanim.start();
        event.consumed = true;
        return;
      }
      if (disableBlockIncrement && (pos < dragBarPos || pos > dragBarPos + dragBarSize)) {
        event.consumed = true;
        return;
      }
      recomputeParams(true); // guich@tc100: otherwise, Slider may fall into a wrong position on pen up
      break;
    case KeyEvent.SPECIAL_KEY_PRESS:
      KeyEvent ke = (KeyEvent) event;
      if (ke.isPrevKey()) // guich@330_45
      {
        if (btnDec.isEnabled()) {
          value -= Settings.keyboardFocusTraversable ? unitIncrement : blockIncrement;
        }
        event.consumed = true;
        mustPostEvent = true; // guich@240_21
      } else if (ke.isNextKey()) // guich@330_45
      {
        if (btnInc.isEnabled()) {
          value += Settings.keyboardFocusTraversable ? unitIncrement : blockIncrement;
        }
        event.consumed = true;
        mustPostEvent = true; // guich@240_21
      } else if (ke.isActionKey()) {
        if (parent != null) {
          parent.requestFocus();
        }
        isHighlighting = true;
      }
      break;
    }

    // only repaint if necessary
    if (value != oldValue) {
      setNewValue(oldValue, mustPostEvent);
    } else if (mustPostEvent) {
      if (parent != null) {
        parent.postEvent(getPressedEvent(this));
      }
      isHighlighting = false; // don't let postEvent steal our focus!
    }
  }

  protected void updateValue(int pos) {
    dragBarPos = pos - startDragPos;
    if (dragBarPos == dragBarMax) {
      value = Math.max(0, maximum - visibleItems); // guich@556_7: fixed the correct value, subtracting by visibleItems
    } else {
      value = (int) ((dragBarPos - dragBarMin) / valuesPerPixel);
      if (unitIncrement != 1) {
        value = ((int) value / unitIncrement) * unitIncrement;
      }
      value += minimum; // msicotte@502_5: fixes problem when minimum is different of zero
    }
  }

  private void setNewValue(int oldValue, boolean mustPostEvent) {
    if (value > maximum - visibleItems) {
      value = maximum - visibleItems;
      removeTimer(autoScrollTimer);
    }
    // else - guich@tc113_16: commented out this else to make sure we're not below min if value is changed in the if above
    if (value <= minimum) {
      value = minimum;
      removeTimer(autoScrollTimer);
    }

    if (value != oldValue) {
      recomputeParams(true);
      if (liveScrolling || mustPostEvent) {
        postPressedEvent();
      }
      Window.needsPaint = true;
    }
  }

  /** Scrolls a block, and post the PRESSED event if the value changes. */
  public void blockScroll(boolean inc) {
    int oldValue = value;
    this.value = inc ? value + blockIncrement : value - blockIncrement;
    setNewValue(oldValue, true);
  }

  private void disableAutoScroll() // luciana@570_22
  {
    removeTimer(autoScrollTimer);
    autoScrollTimer = null;
    autoScrollTarget = null;
    if (buttonScroll) {
      buttonScroll = false;
      enableButtons();
    }
  }

  private void enableButtons() // luciana@570_22
  {
    boolean b = true;
    Window win = getParentWindow();
    if (!buttonScroll) {
      btnDec.setEnabled(b = (isEnabled() && value > minimum));
    }
    if (!b && win != null && win.getFocus() == btnDec) {
      requestFocus();
    }
    if (!buttonScroll) {
      btnInc.setEnabled(b = (isEnabled() && value + visibleItems < maximum));
    }
    Control foc = win == null ? null : win.getFocus();
    if (!b && (foc == btnDec || foc == btnInc)) // guich@572_16: only if the focus is one of the buttons - bruno@tc114: avoid auto-setting the focus to a multi-edit
    {
      requestFocus();
      if (!b && win != null && win.getFocus() == btnInc) {
        requestFocus();
      }
    }
  }

  private void onAutoScroll() // luciana@570_22
  {
    if (autoScrollBarPos < dragBarPos) {
      value -= blockIncrement / 2;
    } else if (autoScrollBarPos > dragBarPos + dragBarSize) {
      value += blockIncrement / 2;
    } else {
      startDragPos = autoScrollBarPos - dragBarPos; // point inside drag bar
      disableAutoScroll();
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (internalSetEnabled(enabled, false)) {
      btnDec.setEnabled(isEnabled() && value > minimum);
      btnInc.setEnabled(isEnabled() && value + visibleItems < maximum);
    }
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    if (colorsChanged) {
      btnDec.setBackForeColors(backColor, foreColor);
      btnInc.setBackForeColors(backColor, foreColor);
    }
    bColor = Color.brighter(getBackColor());
    if (parent != null && bColor == parent.getBackColor()) {
      bColor = Color.getCursorColor(bColor);
    }
    Graphics.compute3dColors(isEnabled(), backColor, foreColor, fourColors);
  }

  @Override
  public void onPaint(Graphics g) {
    // Draw background and borders
    int k = size - (btnWH << 1);
    if (k <= 0) {
      return;
    }
    g.backColor = bColor;
    if (verticalBar) {
      g.fillRect(0, btnWH, btnWH, k);
    } else {
      g.fillRect(btnWH, 0, k, btnWH);
    }
    g.backColor = backColor;
    if (uiFlat) {
      g.foreColor = fourColors[2];
      g.drawRect(0, 0, width, height);
    }
    if (verticalBar) {
      if (uiVista && isEnabled()) {
        g.fillVistaRect(0, dragBarPos, width, dragBarSize, backColor, startDragPos != -1, true); // guich@tc110_51: press the bar if dragging.
      } else {
        g.fillRect(0, dragBarPos, width, dragBarSize);
      }
      g.draw3dRect(0, dragBarPos, width, dragBarSize, uiVista ? Graphics.R3D_CHECK : Graphics.R3D_RAISED, false, false,
          fourColors); // guich@tc110_51: press the bar if dragging.
      if (uiFlat || uiVista) {
        g.foreColor = fourColors[2];
        k = dragBarPos + (dragBarSize >> 1);
        g.drawLine(3, k, width - 4, k);
        if (dragBarSize > minDragBarSize) {
          g.drawLine(3, k - 2, width - 4, k - 2);
          g.drawLine(3, k + 2, width - 4, k + 2);
        }
      }
    } else {
      if (uiVista && isEnabled()) {
        g.fillVistaRect(dragBarPos, 0, dragBarSize, height, backColor, startDragPos != -1, false); // guich@tc110_51: press the bar if dragging.
      } else {
        g.fillRect(dragBarPos, 0, dragBarSize, height);
      }
      g.draw3dRect(dragBarPos, 0, dragBarSize, height, uiVista ? Graphics.R3D_CHECK : Graphics.R3D_RAISED, false, false,
          fourColors); // guich@tc110_51: press the bar if dragging.
      if (uiFlat || uiVista) {
        g.foreColor = fourColors[2];
        k = dragBarPos + (dragBarSize >> 1);
        g.drawLine(k, 3, k, height - 4);
        if (dragBarSize > minDragBarSize) {
          g.drawLine(k - 2, 3, k - 2, height - 4);
          g.drawLine(k + 2, 3, k + 2, height - 4);
        }
      }
    }
  }

  /** If this is a vertical scroll bar, i strongly suggest you use PREFERRED in your control's width (with small adjustments). */
  @Override
  public int getPreferredWidth() {
    return (verticalBar ? btnDec.getPreferredWidth() + extraSize : (btnDec.getPreferredHeight() << 1)) + insets.left
        + insets.right; // guich@240_18 - guich@300_70
  }

  /** If this is a horizontal scroll bar, i strongly suggest you use PREFERRED in your control's height (with small adjustments) */
  @Override
  public int getPreferredHeight() {
    return (verticalBar ? (btnDec.getPreferredWidth() << 1) : btnDec.getPreferredHeight() + extraSize) + insets.top
        + insets.bottom; // guich@300_70: vertical bar always use width; horizontal always use height
  }

  @Override
  public void onFontChanged() {
    if (btnDec != null) {
      btnDec.setFont(font);
    }
    if (btnInc != null) {
      btnInc.setFont(font);
    }
  }

  /* this is needed to recalculate the box size for the selected item if the control is resized by the main application */
  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    size = verticalBar ? height : width;
    btnWH = btnDec.isVisible() ? (verticalBar ? width : height) : 0;
    btnDec.setRect(0, 0, btnWH, btnWH, null, screenChanged);
    btnInc.setRect(verticalBar ? 0 : (size - btnWH), verticalBar ? (size - btnWH) : 0, btnWH, btnWH, null,
        screenChanged);
    dragBarPos = dragBarMin;//btnWH;
    recomputeParams(false);
  }

  @Override
  public void setHighlighting() {
  }

  /** Clears this control, setting the value to clearValueInt. */
  @Override
  public void clear() // guich@572_19
  {
    setValue(clearValueInt);
  }

  @Override
  public Control handleGeographicalFocusChangeKeys(KeyEvent ke) {
    requestFocus();
    if (verticalBar) {
      if ((!ke.isUpKey() && !ke.isDownKey()) || (ke.isUpKey() && value - unitIncrement < minimum)
          || (ke.isDownKey() && value + unitIncrement == maximum)) {
        return null;
      }
    } else {
      if ((ke.isUpKey() || ke.isDownKey()) || (ke.isPrevKey() && value - unitIncrement < minimum)
          || (ke.isNextKey() && value + unitIncrement == maximum)) {
        return null;
      }
    }
    _onEvent(ke);
    return this;
  }

  /** Used by ScrollPosition */
  public void tempShow() {
  }

  @Override
  public void setPos(int x, int y) {
    startDragPos = 0;
    int val = value;
    updateValue(x);
    if (liveScrolling && value != val) {
      postPressedEvent();
    }
    repaintNow();
  }

  @Override
  public void onAnimationFinished(ControlAnimation anim) {
    pathanim = null;
    if (!liveScrolling) {
      postPressedEvent();
    }
    recomputeParams(false);
  }
}
