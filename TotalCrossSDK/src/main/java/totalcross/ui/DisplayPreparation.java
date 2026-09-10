// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.image.Image;

/** Internal context shared by one display-preparation traversal. */
final class DisplayPreparationContext {
  private final double destinationScale;
  private final long batchGeneration;

  DisplayPreparationContext(double destinationScale, long batchGeneration) {
    this.destinationScale = destinationScale;
    this.batchGeneration = batchGeneration;
  }

  double destinationScale() {
    return destinationScale;
  }

  long batchGeneration() {
    return batchGeneration;
  }
}

/** Internal image request sink used while traversing controls. */
interface DisplayPreparationSink {
  void request(Image image, int requirement);
}
