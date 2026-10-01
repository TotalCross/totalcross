// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/** Default build implementation: no optional metric storage or native bridge. */
final class RuntimeDiagnosticsSupport {
  private RuntimeDiagnosticsSupport() {
  }

  static boolean isSupported() {
    return false;
  }

  static void setDomainEnabled(RuntimeDiagnosticSnapshot.Domain domain, boolean enabled) {
    if (domain == null) {
      throw new NullPointerException("domain is required");
    }
  }

  static boolean isDomainEnabled(RuntimeDiagnosticSnapshot.Domain domain) {
    return false;
  }

  static long getDomainGeneration(RuntimeDiagnosticSnapshot.Domain domain) {
    return 0L;
  }

  static void recordCounter(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot) {
    // The default SDK variant has no diagnostic storage or synchronization.
  }

  static void recordTimer(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot, long elapsedNanos) {
    // The default SDK variant has no diagnostic storage or synchronization.
  }

  static void setGauge(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot, long value) {
    // The default SDK variant has no diagnostic storage or synchronization.
  }

  static RuntimeDiagnosticSnapshot snapshot() {
    return RuntimeDiagnosticSnapshot.empty();
  }

}
