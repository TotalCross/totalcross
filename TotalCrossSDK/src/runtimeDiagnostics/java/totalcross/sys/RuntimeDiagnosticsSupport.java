// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/** Opt-in implementation; pair this SDK variant with the matching CMake flag. */
final class RuntimeDiagnosticsSupport {
  private static final RuntimeDiagnosticSnapshot EMPTY = RuntimeDiagnosticSnapshot.empty();
  private static volatile boolean runtimeGroupEnabled;

  private RuntimeDiagnosticsSupport() {
  }

  static boolean isSupported() {
    return true;
  }

  static void setDomainEnabled(RuntimeDiagnosticSnapshot.Domain domain, boolean enabled) {
    if (domain == null) {
      throw new NullPointerException("domain is required");
    }
    if (enabled) {
      RuntimeMetrics.initialize();
    }
    runtimeGroupEnabled = enabled;
  }

  static RuntimeDiagnosticSnapshot snapshot() {
    if (!runtimeGroupEnabled) {
      return EMPTY;
    }
    return RuntimeMetrics.snapshot();
  }

  static void addJavaCounterForTest(long delta) {
    if (!runtimeGroupEnabled) {
      return;
    }
    RuntimeMetrics.addJavaCounter(delta);
  }

  static void setJavaGaugeForTest(long value) {
    if (!runtimeGroupEnabled) {
      return;
    }
    RuntimeMetrics.setJavaGauge(value);
  }

  static void addJavaTimerForTest(long elapsedNanos) {
    if (!runtimeGroupEnabled) {
      return;
    }
    RuntimeMetrics.addJavaTimer(elapsedNanos);
  }

  static void addNativeCounterForTest(long delta) {
    if (!runtimeGroupEnabled) {
      return;
    }
    RuntimeMetrics.addNativeCounter(delta);
  }

  static void setNativeGaugeForTest(long value) {
    if (!runtimeGroupEnabled) {
      return;
    }
    RuntimeMetrics.setNativeGauge(value);
  }

  static long readSingleNativeMetricForTest() {
    if (!runtimeGroupEnabled) {
      return 0L;
    }
    return RuntimeMetrics.readSingleNativeMetric();
  }

  static void resetForTest(RuntimeDiagnosticSnapshot.Domain domain) {
    if (domain == null) {
      throw new NullPointerException("domain is required");
    }
    if (!runtimeGroupEnabled) {
      return;
    }
    RuntimeMetrics.reset();
  }

  private static final class RuntimeMetrics {
    // These bounded synthetic observations exercise the shared snapshot path.
    private static final int JAVA_COUNTER_ID = 0x1001;
    private static final int JAVA_GAUGE_ID = 0x1002;
    private static final int JAVA_TIMER_ID = 0x1003;
    private static final int NATIVE_COUNTER_ID = 0x2001;
    private static final int NATIVE_GAUGE_ID = 0x2002;
    private static final int RUNTIME_GROUP_MASK = 1;
    private static final int[] METRIC_IDS = {
        JAVA_COUNTER_ID, JAVA_GAUGE_ID, JAVA_TIMER_ID, NATIVE_COUNTER_ID, NATIVE_GAUGE_ID
    };
    private static final byte[] DOMAINS = {
        (byte) RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal()
    };
    private static final byte[] KINDS = {
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.GAUGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.TIMER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.GAUGE.ordinal()
    };
    private static final int[] NATIVE_METRIC_IDS = {NATIVE_COUNTER_ID, NATIVE_GAUGE_ID};
    private static final long[] NATIVE_VALUES = new long[NATIVE_METRIC_IDS.length];
    private static final Object COLLECTION_LOCK = new Object();
    private static long javaCounter;
    private static long javaGauge;
    private static long javaTimerNanos;
    private static long epoch;
    private static NativeBridge nativeBridge = new VmNativeBridge();

    private static void initialize() {
      // Calling this method initializes this holder only after the group is enabled.
    }

    private static RuntimeDiagnosticSnapshot snapshot() {
      if (!runtimeGroupEnabled) {
        return EMPTY;
      }
      synchronized (COLLECTION_LOCK) {
        if (!runtimeGroupEnabled) {
          return EMPTY;
        }
        nativeBridge.readMetrics(NATIVE_METRIC_IDS, NATIVE_VALUES);
        long[] values = {javaCounter, javaGauge, javaTimerNanos, NATIVE_VALUES[0], NATIVE_VALUES[1]};
        return new RuntimeDiagnosticSnapshot(METRIC_IDS, DOMAINS, KINDS, values, epoch);
      }
    }

    private static void addJavaCounter(long delta) {
      synchronized (COLLECTION_LOCK) {
        if (runtimeGroupEnabled) {
          javaCounter += delta;
        }
      }
    }

    private static void setJavaGauge(long value) {
      synchronized (COLLECTION_LOCK) {
        if (runtimeGroupEnabled) {
          javaGauge = value;
        }
      }
    }

    private static void addJavaTimer(long elapsedNanos) {
      synchronized (COLLECTION_LOCK) {
        if (runtimeGroupEnabled) {
          javaTimerNanos += elapsedNanos;
        }
      }
    }

    private static void addNativeCounter(long delta) {
      synchronized (COLLECTION_LOCK) {
        if (runtimeGroupEnabled) {
          nativeBridge.addCounterForTest(delta);
        }
      }
    }

    private static void setNativeGauge(long value) {
      synchronized (COLLECTION_LOCK) {
        if (runtimeGroupEnabled) {
          nativeBridge.setGaugeForTest(value);
        }
      }
    }

    private static long readSingleNativeMetric() {
      synchronized (COLLECTION_LOCK) {
        return runtimeGroupEnabled ? nativeBridge.readMetric(NATIVE_COUNTER_ID) : 0L;
      }
    }

    private static void reset() {
      synchronized (COLLECTION_LOCK) {
        if (runtimeGroupEnabled) {
          javaCounter = 0L;
          javaTimerNanos = 0L;
          nativeBridge.resetMetrics(RUNTIME_GROUP_MASK);
          epoch++;
        }
      }
    }
  }

  interface NativeBridge {
    long readMetric(int metricId);

    void readMetrics(int[] metricIds, long[] values);

    void resetMetrics(int groupMask);

    void addCounterForTest(long delta);

    void setGaugeForTest(long value);
  }

  private static final class VmNativeBridge implements NativeBridge {
    @Override
    public long readMetric(int metricId) {
      return readMetricNative(metricId);
    }

    @Override
    public void readMetrics(int[] metricIds, long[] values) {
      readMetricsNative(metricIds, values);
    }

    @Override
    public void resetMetrics(int groupMask) {
      resetMetricsNative(groupMask);
    }

    @Override
    public void addCounterForTest(long delta) {
      addNativeCountNative(delta);
    }

    @Override
    public void setGaugeForTest(long value) {
      setNativeGaugeNative(value);
    }
  }

  private static native long readMetricNative(int metricId);

  private static native void readMetricsNative(int[] metricIds, long[] values);

  private static native void resetMetricsNative(int groupMask);

  private static native void addNativeCountNative(long delta);

  private static native void setNativeGaugeNative(long value);
}
