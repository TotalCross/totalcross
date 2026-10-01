// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImagePreparationFeatureBridge;

/** Completion accounting for one explicit ScrollContainer preparation call. */
final class DisplayPreparationBatch {
  private final ScrollContainer owner;
  private final long generation;
  private final Runnable callback;
  private int pending;
  private boolean discovering = true;
  private boolean completed;

  DisplayPreparationBatch(ScrollContainer owner, long generation, Runnable callback) {
    this.owner = owner;
    this.generation = generation;
    this.callback = callback;
  }

  void add(Image image, double destinationScale) {
    if (image == null) {
      return;
    }
    pending++;
    ImagePreparationFeatureBridge.prepareForDisplay(image, destinationScale, generation, new Runnable() {
      @Override
      public void run() {
        requestSettled();
      }
    });
  }

  Rect intersection(Rect first, Rect second) {
    return new DisplayPreparationContext(this).intersection(first, second);
  }

  void finishDiscovery() {
    discovering = false;
    settleIfReady();
  }

  private void requestSettled() {
    if (pending > 0) {
      pending--;
    }
    settleIfReady();
  }

  private void settleIfReady() {
    if (discovering || pending != 0 || completed) {
      return;
    }
    completed = true;
    if (callback != null && owner.isCurrentDisplayPreparationBatch(generation)) {
      callback.run();
    }
  }
}
