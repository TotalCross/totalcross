// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/** Internal runtime seam for feature-owned aggregate observations. */
public final class RuntimeDiagnosticsFeatureBridge {
  private RuntimeDiagnosticsFeatureBridge() {
  }

  /** @hidden */
  public static boolean isDomainEnabled(RuntimeDiagnosticSnapshot.Domain domain) {
    return RuntimeDiagnosticsSupport.isDomainEnabled(domain);
  }

  /** @hidden */
  public static long getDomainGeneration(RuntimeDiagnosticSnapshot.Domain domain) {
    return RuntimeDiagnosticsSupport.getDomainGeneration(domain);
  }

  /** @hidden The metric slot is internal to the calling feature. */
  public static void recordCounter(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot) {
    RuntimeDiagnosticsSupport.recordCounter(domain, featureMetricSlot);
  }

  /** @hidden The metric slot is internal to the calling feature. */
  public static void recordTimer(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot, long nanos) {
    RuntimeDiagnosticsSupport.recordTimer(domain, featureMetricSlot, nanos);
  }

  /** @hidden The metric slot is internal to the calling feature. */
  public static void setGauge(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot, long value) {
    RuntimeDiagnosticsSupport.setGauge(domain, featureMetricSlot, value);
  }
}
