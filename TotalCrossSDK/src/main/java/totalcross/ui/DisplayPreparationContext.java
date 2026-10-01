// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;
import java.util.ArrayList;

/** UI-thread-only state shared by one ScrollContainer discovery pass. */
final class DisplayPreparationContext {
  private final DisplayPreparationBatch batch;
  private final ArrayList<Image> testCandidates;

  DisplayPreparationContext(DisplayPreparationBatch batch) {
    this.batch = batch;
    this.testCandidates = null;
  }

  DisplayPreparationContext() {
    this.batch = null;
    this.testCandidates = new ArrayList<Image>();
  }

  boolean intersects(Rect control, Rect clip) {
    return control != null && clip != null && control.width > 0 && control.height > 0
        && clip.width > 0 && clip.height > 0
        && (long) control.x < (long) clip.x + clip.width
        && (long) clip.x < (long) control.x + control.width
        && (long) control.y < (long) clip.y + clip.height
        && (long) clip.y < (long) control.y + control.height;
  }

  Rect intersection(Rect first, Rect second) {
    if (first == null || second == null) {
      return null;
    }
    long x0 = Math.max((long) first.x, second.x);
    long y0 = Math.max((long) first.y, second.y);
    long x1 = Math.min((long) first.x + first.width, (long) second.x + second.width);
    long y1 = Math.min((long) first.y + first.height, (long) second.y + second.height);
    if (x1 <= x0 || y1 <= y0) {
      return null;
    }
    return new Rect((int) x0, (int) y0, (int) (x1 - x0), (int) (y1 - y0));
  }

  void add(Image image, double destinationScale) {
    if (image == null) {
      return;
    }
    if (testCandidates != null) {
      testCandidates.add(image);
    } else {
      batch.add(image, destinationScale);
    }
  }

  int candidateCountForTest() {
    return testCandidates == null ? 0 : testCandidates.size();
  }

  Image candidateForTest(int index) {
    return testCandidates.get(index);
  }
}
