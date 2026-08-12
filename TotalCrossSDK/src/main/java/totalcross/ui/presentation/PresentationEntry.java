// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.presentation;

import totalcross.ui.Control;

import totalcross.ui.gfx.Rect;

final class PresentationEntry {
  enum Layer {
    ROUTE,
    OVERLAY
  }

  interface BoundsResolver {
    void resolve(Rect viewport, Rect bounds);
  }

  final Control content;
  final Layer layer;
  final BoundsResolver boundsResolver;
  final PresentationTransition transition;
  final boolean blocksInput;
  final boolean dismissOnOutsidePress;
  final boolean dismissOnBack;
  final int barrierColor;
  final int barrierAlpha;
  final int duration;

  PresentationEntry(Control content, Layer layer, BoundsResolver boundsResolver,
      PresentationTransition transition, boolean blocksInput, boolean dismissOnOutsidePress,
      boolean dismissOnBack, int barrierColor, int barrierAlpha, int duration) {
    if (content == null || layer == null || boundsResolver == null || transition == null) {
      throw new NullPointerException();
    }
    this.content = content;
    this.layer = layer;
    this.boundsResolver = boundsResolver;
    this.transition = transition;
    this.blocksInput = blocksInput;
    this.dismissOnOutsidePress = dismissOnOutsidePress;
    this.dismissOnBack = dismissOnBack;
    this.barrierColor = barrierColor;
    this.barrierAlpha = barrierAlpha;
    this.duration = duration;
  }

  static BoundsResolver fillViewport() {
    return new BoundsResolver() {
      @Override
      public void resolve(Rect viewport, Rect bounds) {
        bounds.set(0, 0, viewport.width, viewport.height);
      }
    };
  }
}
