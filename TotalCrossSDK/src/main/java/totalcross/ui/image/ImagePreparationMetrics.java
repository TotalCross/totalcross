// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnosticsFeatureBridge;

/** Package-private adapter from P8 events to the shared diagnostics feature bridge. */
final class ImagePreparationMetrics {
  private static final RuntimeDiagnosticSnapshot.Domain DOMAIN = RuntimeDiagnosticSnapshot.Domain.PREFETCH;
  private static final int DISCOVERED = 0;
  private static final int ENQUEUED = 1;
  private static final int DEDUPLICATED = 2;
  private static final int READY = 3;
  private static final int STALE = 4;
  private static final int NOT_PREFETCHABLE = 5;
  private static final int DETERMINISTIC_FAILURE = 6;
  private static final int TRANSIENT_FAILURE = 7;
  private static final int QUEUE_DEPTH = 8;
  private static final int ACTIVE_COUNT = 9;

  private ImagePreparationMetrics() {
  }

  static void discovered() { record(DISCOVERED); }
  static void enqueued() { record(ENQUEUED); }
  static void deduplicated() { record(DEDUPLICATED); }
  static void ready() { record(READY); }
  static void stale() { record(STALE); }
  static void notPrefetchable() { record(NOT_PREFETCHABLE); }
  static void deterministicFailure() { record(DETERMINISTIC_FAILURE); }
  static void transientFailure() { record(TRANSIENT_FAILURE); }

  static void queueState(int depth, int active) {
    if (!RuntimeDiagnosticsFeatureBridge.isDomainEnabled(DOMAIN)) {
      return;
    }
    RuntimeDiagnosticsFeatureBridge.setGauge(DOMAIN, QUEUE_DEPTH, depth);
    RuntimeDiagnosticsFeatureBridge.setGauge(DOMAIN, ACTIVE_COUNT, active);
  }

  private static void record(int slot) {
    if (RuntimeDiagnosticsFeatureBridge.isDomainEnabled(DOMAIN)) {
      RuntimeDiagnosticsFeatureBridge.recordCounter(DOMAIN, slot);
    }
  }
}
