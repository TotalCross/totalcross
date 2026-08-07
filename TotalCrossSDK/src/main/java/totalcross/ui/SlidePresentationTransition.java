// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.gfx.Rect;

final class SlidePresentationTransition implements PresentationTransition {
  private final int direction;

  SlidePresentationTransition(int direction) {
    this.direction = direction;
  }

  void getOutsideBounds(Rect viewport, Rect finalBounds, Rect outside) {
    outside.set(finalBounds.x, finalBounds.y, finalBounds.width, finalBounds.height);
    switch (direction) {
    case Control.LEFT:
      outside.x = -finalBounds.width;
      break;
    case Control.RIGHT:
      outside.x = viewport.width;
      break;
    case Control.TOP:
      outside.y = -finalBounds.height;
      break;
    case Control.BOTTOM:
      outside.y = viewport.height;
      break;
    default:
      throw new IllegalArgumentException("unsupported slide direction: " + direction);
    }
  }

  @Override
  public ControlAnimation start(final PresentationHandle handle, boolean entering, int duration,
      final Runnable finished) {
    Rect outside = new Rect();
    getOutsideBounds(handle.viewportBounds(), handle.finalBounds(), outside);
    Rect from = entering ? outside : handle.finalBounds();
    Rect to = entering ? handle.finalBounds() : outside;
    handle.setFramePosition(from.x, from.y);
    if (duration <= 0) {
      handle.setFramePosition(to.x, to.y);
      finished.run();
      return null;
    }

    PathAnimation animation = PathAnimation.create(handle.frame(), from.x, from.y, to.x, to.y,
        new AnimationFinished() {
          @Override
          public void onAnimationFinished(ControlAnimation animation) {
            finished.run();
          }
        }, duration);
    animation.setpos = new PathAnimation.SetPosition() {
      @Override
      public void setPos(int x, int y) {
        handle.setFramePosition(x, y);
      }
    };
    animation.start();
    return animation;
  }
}
