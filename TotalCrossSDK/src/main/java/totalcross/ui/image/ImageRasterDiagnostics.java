// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnosticsFeatureBridge;

/** Internal event bridge; storage is compiled out by the default SDK variant. */
final class ImageRasterDiagnostics {
  static final int DIRECT_DECODE_SUCCESS = 0;
  static final int DIRECT_DECODE_FALLBACK = 1;
  static final int OPAQUE_WRITE_SUCCESS = 2;
  static final int BOUNDED_READ_SUCCESS = 3;
  static final int DIRECT_COLOR_SUCCESS = 4;
  static final int RASTER_FALLBACK = 5;
  static final int PHYSICAL_IDENTITY_ATTEMPT = 6;
  static final int PHYSICAL_IDENTITY_HIT = 7;
  static final int PHYSICAL_IDENTITY_FALLBACK = 8;

  static final int DRAW_HANDLED = 1;
  static final int DRAW_IDENTITY_ATTEMPT = 1 << 1;
  static final int DRAW_IDENTITY_HIT = 1 << 2;
  static final int DRAW_IDENTITY_FALLBACK = 1 << 3;

  private ImageRasterDiagnostics() {
  }

  static void record(int event) {
    if (RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE)) {
      RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE, event);
    }
  }

  static void recordDrawEvents(int status) {
    if (!RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE)) {
      return;
    }
    if ((status & DRAW_IDENTITY_ATTEMPT) != 0) {
      RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE,
          PHYSICAL_IDENTITY_ATTEMPT);
    }
    if ((status & DRAW_IDENTITY_HIT) != 0) {
      RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE,
          PHYSICAL_IDENTITY_HIT);
    }
    if ((status & DRAW_IDENTITY_FALLBACK) != 0) {
      RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE,
          PHYSICAL_IDENTITY_FALLBACK);
    }
  }
}
