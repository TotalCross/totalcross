// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import totalcross.ui.MainWindow;

/** Exercises the opt-in generic Java/native diagnostics bridge on macOS. */
public class RuntimeDiagnosticsSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      require(RuntimeDiagnostics.isSupported(), "diagnostics-enabled SDK required");
      RuntimeDiagnosticSnapshot.Domain domain = RuntimeDiagnosticSnapshot.Domain.RUNTIME;
      RuntimeDiagnostics.setDomainEnabled(domain, true);
      RuntimeDiagnosticsSupport.resetForTest(domain);
      RuntimeDiagnosticsSupport.addJavaCounterForTest(2L);
      RuntimeDiagnosticsSupport.addNativeCounterForTest(3L);
      RuntimeDiagnosticsSupport.setJavaGaugeForTest(7L);
      RuntimeDiagnosticsSupport.setNativeGaugeForTest(9L);
      RuntimeDiagnosticsSupport.addJavaTimerForTest(13L);

      RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
      require(value(before, RuntimeDiagnosticSnapshot.Kind.COUNTER) == 5L, "mixed counter snapshot");
      require(value(before, RuntimeDiagnosticSnapshot.Kind.GAUGE) == 16L, "mixed gauge snapshot");
      require(value(before, RuntimeDiagnosticSnapshot.Kind.TIMER) == 13L, "timer snapshot");
      require(RuntimeDiagnosticsSupport.readSingleNativeMetricForTest() == 3L, "single native read");

      RuntimeDiagnosticsSupport.addJavaCounterForTest(4L);
      RuntimeDiagnosticsSupport.addNativeCounterForTest(5L);
      RuntimeDiagnosticsSupport.addJavaTimerForTest(2L);
      RuntimeDiagnosticSnapshot after = RuntimeDiagnostics.snapshot();
      RuntimeDiagnosticSnapshot delta = after.deltaSince(before);
      require(value(delta, RuntimeDiagnosticSnapshot.Kind.COUNTER) == 9L, "counter delta");
      require(value(delta, RuntimeDiagnosticSnapshot.Kind.TIMER) == 2L, "timer delta");
      require(value(delta, RuntimeDiagnosticSnapshot.Kind.GAUGE) == 16L, "absolute gauge delta");

      RuntimeDiagnosticsSupport.resetForTest(domain);
      RuntimeDiagnosticSnapshot reset = RuntimeDiagnostics.snapshot();
      require(value(reset, RuntimeDiagnosticSnapshot.Kind.COUNTER) == 0L, "selective counter reset");
      require(value(reset, RuntimeDiagnosticSnapshot.Kind.TIMER) == 0L, "selective timer reset");
      require(value(reset, RuntimeDiagnosticSnapshot.Kind.GAUGE) == 16L, "live gauges preserved");
      boolean epochMismatchRejected = false;
      try {
        reset.deltaSince(after);
      } catch (IllegalArgumentException expected) {
        epochMismatchRejected = true;
      }
      require(epochMismatchRejected, "reset epoch mismatch accepted");
      RuntimeDiagnostics.setDomainEnabled(domain, false);
      require(RuntimeDiagnostics.snapshot().size() == 0, "disabled domain snapshot");

      System.out.println("fixture=RuntimeDiagnosticsSmokeApp,overallPass=true");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("fixture=RuntimeDiagnosticsSmokeApp,overallPass=false,error=" + failure.getMessage());
      failure.printStackTrace();
      System.out.flush();
      exit(1);
    }
  }

  private static long value(RuntimeDiagnosticSnapshot snapshot, RuntimeDiagnosticSnapshot.Kind kind) {
    return snapshot.getValue(RuntimeDiagnosticSnapshot.Domain.RUNTIME, kind);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
