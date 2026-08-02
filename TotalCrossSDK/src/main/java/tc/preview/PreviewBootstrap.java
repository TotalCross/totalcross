// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.preview;

import tc.preview.internal.SimulatorPreviewSession;

/** Entry point used by reflective preview workers. */
public final class PreviewBootstrap {
  private PreviewBootstrap() {
  }

  public static PreviewSession start(String mainClass, String[] arguments, ClassLoader applicationLoader,
      PreviewFrameSink frameSink) {
    return new SimulatorPreviewSession(mainClass, arguments, applicationLoader, frameSink);
  }
}
