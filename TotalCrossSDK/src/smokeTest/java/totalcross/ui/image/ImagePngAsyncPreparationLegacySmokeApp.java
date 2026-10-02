// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Deployed macOS smoke that leaves PNG preparation on the production legacy worker default. */
public final class ImagePngAsyncPreparationLegacySmokeApp extends ImagePngAsyncPreparationSmokeApp {
  @Override
  protected boolean forceSemaphoreWorker() {
    return false;
  }
}
