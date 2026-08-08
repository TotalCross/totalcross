// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.gfx.Rect;

final class PresentationHandle {
  enum State {
    PRESENTING,
    PRESENTED,
    DISMISSING,
    DISMISSED
  }

  final PresentationEntry entry;
  final Container entryHost = new Container();
  final PresentationBarrier barrier;
  final Container viewport = new Container();
  final Container frame = new Container();

  private final PresentationHost host;
  private final Rect viewportBounds = new Rect();
  private final Rect finalBounds = new Rect();
  private ControlAnimation animation;
  private Runnable dismissedAction;
  private State state = State.PRESENTING;

  PresentationHandle(PresentationHost host, PresentationEntry entry) {
    this.host = host;
    this.entry = entry;
    barrier = new PresentationBarrier(this, entry);
    viewport.setClipChildrenToBounds(true);
    frame.transparentBackground = true;
    entryHost.add(barrier);
    entryHost.add(viewport);
    viewport.add(frame);
    frame.add(entry.content);
  }

  void attach(Rect safeBounds) {
    layout(safeBounds);
    animation = entry.transition.start(this, true, entry.duration, new Runnable() {
      @Override
      public void run() {
        animation = null;
        state = State.PRESENTED;
        setFramePosition(finalBounds.x, finalBounds.y);
      }
    });
  }

  void layout(Rect safeBounds) {
    if (state == State.DISMISSED) {
      return;
    }
    if (animation != null) {
      animation.setAnimationFinishedAction(null);
      animation.stop(true);
      animation = null;
      if (state == State.DISMISSING) {
        finishDismiss();
        return;
      }
      state = State.PRESENTED;
    }

    entryHost.resetSetPositions();
    entryHost.setRect(0, 0, host.getWidth(), host.getHeight());
    barrier.resetSetPositions();
    barrier.setRect(0, 0, entryHost.getWidth(), entryHost.getHeight());
    viewportBounds.set(safeBounds.x, safeBounds.y, safeBounds.width, safeBounds.height);
    viewport.resetSetPositions();
    viewport.setRect(viewportBounds);

    Rect localViewport = new Rect(0, 0, safeBounds.width, safeBounds.height);
    entry.boundsResolver.resolve(localViewport, finalBounds);
    frame.resetSetPositions();
    frame.setRect(finalBounds);
    if (entry.content.setX == Control.SETX_NOT_SET) {
      entry.content.setRect(Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    }
    frame.repositionChildren();
  }

  void dismiss() {
    if (state == State.DISMISSED || state == State.DISMISSING) {
      return;
    }
    if (state == State.PRESENTING && animation != null) {
      return;
    }
    if (animation != null) {
      animation.setAnimationFinishedAction(null);
      animation.stop(true);
      animation = null;
      setFramePosition(finalBounds.x, finalBounds.y);
    }
    state = State.DISMISSING;
    animation = entry.transition.start(this, false, entry.duration, new Runnable() {
      @Override
      public void run() {
        animation = null;
        finishDismiss();
      }
    });
  }

  private void finishDismiss() {
    if (state == State.DISMISSED) {
      return;
    }
    state = State.DISMISSED;
    host.remove(this);
    if (dismissedAction != null) {
      Runnable action = dismissedAction;
      dismissedAction = null;
      action.run();
    }
  }

  void setDismissedAction(Runnable action) {
    dismissedAction = action;
  }

  void requestRelayout() {
    host.ownerLayoutChanged();
  }

  boolean isActive() {
    return state != State.DISMISSED;
  }

  State state() {
    return state;
  }

  ControlAnimation animation() {
    return animation;
  }

  Container frame() {
    return frame;
  }

  Rect viewportBounds() {
    return new Rect(0, 0, viewportBounds.width, viewportBounds.height);
  }

  Rect finalBounds() {
    return new Rect(finalBounds.x, finalBounds.y, finalBounds.width, finalBounds.height);
  }

  void setFramePosition(int x, int y) {
    frame.x = x;
    frame.y = y;
    Window.needsPaint = true;
  }
}
