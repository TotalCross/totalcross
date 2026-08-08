// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.FadeAnimation;

final class FadePresentationTransition implements PresentationTransition {
  @Override
  public ControlAnimation start(PresentationHandle handle, boolean entering, int duration,
      final Runnable finished) {
    if (duration <= 0) {
      finished.run();
      return null;
    }
    ControlAnimation animation = FadeAnimation.create(handle.frame(), entering, new AnimationFinished() {
      @Override
      public void onAnimationFinished(ControlAnimation animation) {
        finished.run();
      }
    }, duration);
    animation.start();
    return animation;
  }
}
