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

  private ImageRasterDiagnostics() {
  }

  static void record(int event) {
    if (RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE)) {
      RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE, event);
    }
  }
}
