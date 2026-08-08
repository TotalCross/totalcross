// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.event.DragEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.gfx.Graphics;

final class PresentationBarrier extends Control implements PenListener {
  private final PresentationHandle handle;
  private final boolean dismissOnOutsidePress;

  PresentationBarrier(PresentationHandle handle, PresentationEntry entry) {
    this.handle = handle;
    dismissOnOutsidePress = entry.dismissOnOutsidePress;
    transparentBackground = true;
    if (entry.barrierAlpha > 0) {
      setTranslucent(TranslucentShape.RECT);
      setBackColor(entry.barrierColor);
      alphaValue = entry.barrierAlpha;
    }
    addPenListener(this);
  }

  @Override
  public void onPaint(Graphics g) {
    drawTranslucentBackground(g, alphaValue);
  }

  @Override
  public void penUp(PenEvent event) {
    if (dismissOnOutsidePress) {
      handle.dismiss();
    }
    if (event != null) {
      event.consumed = true;
    }
  }

  @Override
  public void penDown(PenEvent event) {
    if (event != null) {
      event.consumed = true;
    }
  }

  @Override
  public void penDrag(DragEvent event) {
  }

  @Override
  public void penDragStart(DragEvent event) {
  }

  @Override
  public void penDragEnd(DragEvent event) {
  }
}
