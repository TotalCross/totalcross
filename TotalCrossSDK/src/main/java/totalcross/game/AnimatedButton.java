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

import totalcross.sys.SpecialKeys;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * An animated button control.
 * <br>
 * This control displays an animated button which can take <code>'S'</code> different states and each state is fades in or out in <code>'F'</code> 
 * frames. <code>'S'</code> and <code>'F'</code> represent the two first constructor arguments. The frames of this special animation have to be
 * ordered to be supported by this class. The states are numbered from <code>0</code> to <code>'S'-1</code> and the frames order is the following 
 * depending on the layoutType value:<br>
 * 
 * <pre>
 * FADE_OUT_LAYOUT    :  S0F0,S0F1,S0F2,S1F0,S1F1,S1F2,S2F0,S2F1,S2F2<br>
 * FADE_IN_LAYOUT     :  S0F2,S0F1,S0F0,S1F2,S1F1,S1F0,S2F2,S2F1,S2F0<br>
 * FADE_OUT_IN_LAYOUT :  S0F0,S0F1,S1F1,S1F0,S1F1,S2F1,S2F0,S2F1,S0F1<br>
 * </pre>
 * 
 * where <code>S</code> stands for state, <code>F</code> for frame; and where <code>S0F0</code>, <code>S1F0</code>, and <code>S2F0</code> are the 
 * full states and the others are transition frames. Open the <code>onOff.bmp</code> in the Scape game sample and you will understand ;-) .
 * 
 * @author Frank Diebolt
 * @author Guilherme Campos Hazan
 * @version 1.1
 */
@Deprecated
public class AnimatedButton extends Animation {
  /**
   * Defines the frames animation order. In the case of <code>S</code> states button of <code>F</code> frames per state, 
   * <code>FADE_OUT_LAYOUT</code> means that the frames are a <code>S</code> set of <code>F</code> frames that are fading out the state, which means 
   * the first frame of each set is the full state image.
   * In the <code>FADE_IN_LAYOUT</code> layout, it's the opposite, namely the last frame of each set represents the state ending position.
   * Finally the <code>FADE_OUT_IN_LAYOUT</code> is a mix of the two others, because inter-frames represent successively fading out from one state to
   * fading in to next state.
   */
  public static final int FADE_OUT_LAYOUT = 0;

  /** 
   * Frames fading in mode.  
   * 
   * @see #FADE_OUT_LAYOUT 
   */
  public static final int FADE_IN_LAYOUT = 1;

  /** 
   * Frames fading out then fading in mode.
   *  
   * @see #FADE_OUT_LAYOUT 
   */
  public static final int FADE_OUT_IN_LAYOUT = 2;

  /** current animated button state */
  protected int state;

  protected int layoutType;
  protected int fadeInState;
  protected int framesPerState;
  protected int maxStates;
  protected int statesIndexes[];

  private final static int IDLE = -1;

  /**
   * Animated button constructor.
   * 
   * @param frames Button different states frames in multi-frame BMP format.
   * @param states Number of states of the button.
   * @param framesPerState Number of frames for each state.
   * @param layoutType <code>FADE_OUT_LAYOUT</code>, <code>FADE_IN_LAYOUT</code>, or <code>FADE_OUT_IN_LAYOUT</code>.
   * @param framePeriod Delay in milliseconds between two frames.
   * @throws ImageException If an internal method throws it.
   * 
   * @see #FADE_OUT_LAYOUT
   * @see #FADE_IN_LAYOUT
   * @see #FADE_OUT_IN_LAYOUT
   */
  public AnimatedButton(Image frames, int states, int framesPerState, int layoutType, int framePeriod)
      throws ImageException // fdie@341_2
  {
    super(frames, states * framesPerState, framePeriod);

    this.framesPerState = framesPerState;
    this.layoutType = layoutType;
    this.maxStates = states;
    statesIndexes = new int[states];
    for (int s = 0; s < states; s++) {
      statesIndexes[s] = (layoutType == FADE_IN_LAYOUT) ? ((s + 1) * framesPerState) - 1 : s * framesPerState;
    }

    curFrame = statesIndexes[state = 0];
    fadeInState = IDLE;
    eventsMask = eventFinish;
  }

  /**
   * Sets the animated button state.
   * 
   * @param state Value between <code>0</code> and <code>states-1</code>.
   */
  public void setState(int state) {
    if (isPlaying) {
      stop();
      fadeInState = IDLE;
    }

    this.state = state;
    curFrame = statesIndexes[state];
    repaintNow();
  }

  /**
   * Gets the animated button state.
   * 
   * @return Value between <code>0</code> and <code>states-1</code>.
   */
  public int getState() {
    return state;
  }

  /**
   * Animated button event handler.
   * 
   * @param event The event being handled.
   */
  @Override
  public void onEvent(Event event) {
    switch (event.type) {
    case PenEvent.PEN_DOWN:
      if (fadeInState == IDLE) {
        inc(((PenEvent) event).x >= (width >> 1));
      }
      break;
    case KeyEvent.SPECIAL_KEY_PRESS:
      if (fadeInState == IDLE) {
        int key = ((KeyEvent) event).key;
        if (key == SpecialKeys.ACTION || key == SpecialKeys.ENTER) {
          inc(true);
        }
      }
      break;
    case AnimationEvent.FINISH:
      if (fadeInState != IDLE) {
        state = fadeInState;
        fadeInState = IDLE;
        if (layoutType == FADE_OUT_IN_LAYOUT) {
          postPressedEvent();
          return;
        }
        int dest = statesIndexes[state];
        if (layoutType != FADE_IN_LAYOUT) {
          start(dest + framesPerState - 1, dest, -1, 1);
        } else {
          start(dest - framesPerState + 1, dest, 1, 1);
        }
      }
      break;
    case ControlEvent.PRESSED:
      postPressedEvent();
      break;
    default: // pass timer events to the parent
      super.onEvent(event);
    }
  }

  /**
   * Increases/decreases the animated button state.
   * 
   * @param up Boolean with a <code>true</code> value to increase the value; decrease otherwise.
   */
  protected void inc(boolean up) {
    int dir = up ? 1 : -1;
    int dest = (state + maxStates + dir) % maxStates;
    int src = statesIndexes[state];
    if (layoutType == FADE_OUT_IN_LAYOUT) {
      start(src, statesIndexes[dest], dir, 1);
    } else if (layoutType != FADE_IN_LAYOUT) {
      start(src, src + framesPerState - 1, 1, 1);
    } else {
      start(src, src - framesPerState + 1, -1, 1);
    }
    fadeInState = dest;
  }
}