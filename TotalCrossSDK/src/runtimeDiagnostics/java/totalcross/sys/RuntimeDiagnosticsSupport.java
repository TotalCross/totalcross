// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/** Opt-in implementation; pair this SDK variant with the matching CMake flag. */
final class RuntimeDiagnosticsSupport {
  private static final RuntimeDiagnosticSnapshot EMPTY = RuntimeDiagnosticSnapshot.empty();
  private static volatile boolean runtimeGroupEnabled;
  private static volatile boolean imageGroupEnabled;
  private static volatile boolean schedulingGroupEnabled;
  private static volatile long schedulingGeneration;

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
    if (domain == RuntimeDiagnosticSnapshot.Domain.RUNTIME) {
      runtimeGroupEnabled = enabled;
    } else if (domain == RuntimeDiagnosticSnapshot.Domain.IMAGE) {
      imageGroupEnabled = enabled;
    } else if (domain == RuntimeDiagnosticSnapshot.Domain.SCHEDULING) {
      if (schedulingGroupEnabled != enabled) {
        if (enabled) {
          schedulingGeneration++;
          schedulingGroupEnabled = true;
        } else {
          schedulingGroupEnabled = false;
          schedulingGeneration++;
        }
      }
    }
  }

  static boolean isDomainEnabled(RuntimeDiagnosticSnapshot.Domain domain) {
    if (domain == RuntimeDiagnosticSnapshot.Domain.RUNTIME) {
      return runtimeGroupEnabled;
    }
    if (domain == RuntimeDiagnosticSnapshot.Domain.IMAGE) {
      return imageGroupEnabled;
    }
    return domain == RuntimeDiagnosticSnapshot.Domain.SCHEDULING && schedulingGroupEnabled;
  }

  static long getDomainGeneration(RuntimeDiagnosticSnapshot.Domain domain) {
    return domain == RuntimeDiagnosticSnapshot.Domain.SCHEDULING ? schedulingGeneration : 0L;
  }

  static void recordCounter(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot) {
    if (!isDomainEnabled(domain)) {
      return;
    }
    RuntimeMetrics.recordCounter(domain, featureMetricSlot);
  }

  static void recordTimer(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot, long elapsedNanos) {
    if (!isDomainEnabled(domain)) {
      return;
    }
    RuntimeMetrics.recordTimer(domain, featureMetricSlot, elapsedNanos);
  }

  static void setGauge(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot, long value) {
    if (!isDomainEnabled(domain)) {
      return;
    }
    RuntimeMetrics.setGauge(domain, featureMetricSlot, value);
  }

  static RuntimeDiagnosticSnapshot snapshot() {
    if (!runtimeGroupEnabled && !imageGroupEnabled && !schedulingGroupEnabled) {
      return EMPTY;
    }
    return RuntimeMetrics.snapshot(runtimeGroupEnabled, imageGroupEnabled, schedulingGroupEnabled);
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
    if (!isDomainEnabled(domain)) {
      return;
    }
    RuntimeMetrics.reset(domain);
  }

  private static final class RuntimeMetrics {
    // These bounded synthetic observations exercise the shared snapshot path.
    private static final int JAVA_COUNTER_ID = 0x1001;
    private static final int JAVA_GAUGE_ID = 0x1002;
    private static final int JAVA_TIMER_ID = 0x1003;
    private static final int NATIVE_COUNTER_ID = 0x2001;
    private static final int NATIVE_GAUGE_ID = 0x2002;
    private static final int RUNTIME_GROUP_MASK = 1;
    private static final int[] IMAGE_METRIC_IDS = {
        0x3001, 0x3002, 0x3003, 0x3004, 0x3005, 0x3006, 0x3007, 0x3008, 0x3009,
        0x300A, 0x300B, 0x300C, 0x300D, 0x300E, 0x300F, 0x3010, 0x3011
    };
    private static final int FLICK_CALLBACK_COUNT_ID = 0x4001;
    private static final int FLICK_ADVANCEMENT_COUNT_ID = 0x4002;
    private static final int FLICK_COMPLETION_COUNT_ID = 0x4003;
    private static final int FLICK_ADVANCEMENT_WORK_NANOS_ID = 0x4004;
    private static final int FLICK_POSITIVE_LATENESS_NANOS_ID = 0x4005;
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
    private static final byte[] IMAGE_DOMAINS = {
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal()
    };
    private static final byte[] IMAGE_KINDS = {
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(),
        (byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal()
    };
    private static final long[] NATIVE_VALUES = new long[NATIVE_METRIC_IDS.length];
    private static final Object COLLECTION_LOCK = new Object();
    private static long javaCounter;
    private static long javaGauge;
    private static long javaTimerNanos;
    private static final long[] imageCounters = new long[IMAGE_METRIC_IDS.length];
    private static long flickCallbackCount;
    private static long flickAdvancementCount;
    private static long flickCompletionCount;
    private static long flickAdvancementWorkNanos;
    private static long flickPositiveLatenessNanos;
    private static long epoch;
    private static NativeBridge nativeBridge = new VmNativeBridge();

    private static void initialize() {
      // Calling this method initializes this holder only after the group is enabled.
    }

    private static RuntimeDiagnosticSnapshot snapshot(boolean includeRuntime, boolean includeImage,
        boolean includeScheduling) {
      if (!includeRuntime && !includeImage && !includeScheduling) {
        return EMPTY;
      }
      synchronized (COLLECTION_LOCK) {
        includeRuntime &= runtimeGroupEnabled;
        includeImage &= imageGroupEnabled;
        includeScheduling &= schedulingGroupEnabled;
        if (!includeRuntime && !includeImage && !includeScheduling) {
          return EMPTY;
        }
        if (includeRuntime) {
          nativeBridge.readMetrics(NATIVE_METRIC_IDS, NATIVE_VALUES);
        }
        int runtimeCount = includeRuntime ? METRIC_IDS.length : 0;
        int imageCount = includeImage ? IMAGE_METRIC_IDS.length : 0;
        int schedulingCount = includeScheduling ? 5 : 0;
        int total = runtimeCount + imageCount + schedulingCount;
        int[] metricIds = new int[total];
        byte[] domains = new byte[total];
        byte[] kinds = new byte[total];
        long[] values = new long[total];
        int destination = 0;
        if (includeRuntime) {
          System.arraycopy(METRIC_IDS, 0, metricIds, destination, runtimeCount);
          System.arraycopy(DOMAINS, 0, domains, destination, runtimeCount);
          System.arraycopy(KINDS, 0, kinds, destination, runtimeCount);
          values[destination++] = javaCounter;
          values[destination++] = javaGauge;
          values[destination++] = javaTimerNanos;
          values[destination++] = NATIVE_VALUES[0];
          values[destination++] = NATIVE_VALUES[1];
        }
        if (includeImage) {
          System.arraycopy(IMAGE_METRIC_IDS, 0, metricIds, destination, imageCount);
          System.arraycopy(IMAGE_DOMAINS, 0, domains, destination, imageCount);
          System.arraycopy(IMAGE_KINDS, 0, kinds, destination, imageCount);
          System.arraycopy(imageCounters, 0, values, destination, imageCount);
          destination += imageCount;
        }
        if (includeScheduling) {
          byte schedulingDomain = (byte) RuntimeDiagnosticSnapshot.Domain.SCHEDULING.ordinal();
          destination = put(metricIds, domains, kinds, values, destination, FLICK_CALLBACK_COUNT_ID,
              schedulingDomain, RuntimeDiagnosticSnapshot.Kind.COUNTER, flickCallbackCount);
          destination = put(metricIds, domains, kinds, values, destination, FLICK_ADVANCEMENT_COUNT_ID,
              schedulingDomain, RuntimeDiagnosticSnapshot.Kind.COUNTER, flickAdvancementCount);
          destination = put(metricIds, domains, kinds, values, destination, FLICK_COMPLETION_COUNT_ID,
              schedulingDomain, RuntimeDiagnosticSnapshot.Kind.COUNTER, flickCompletionCount);
          destination = put(metricIds, domains, kinds, values, destination, FLICK_ADVANCEMENT_WORK_NANOS_ID,
              schedulingDomain, RuntimeDiagnosticSnapshot.Kind.TIMER, flickAdvancementWorkNanos);
          put(metricIds, domains, kinds, values, destination, FLICK_POSITIVE_LATENESS_NANOS_ID,
              schedulingDomain, RuntimeDiagnosticSnapshot.Kind.TIMER, flickPositiveLatenessNanos);
        }
        return new RuntimeDiagnosticSnapshot(metricIds, domains, kinds, values, epoch);
      }
    }

    private static int put(int[] metricIds, byte[] domains, byte[] kinds, long[] values, int index,
        int metricId, byte domain, RuntimeDiagnosticSnapshot.Kind kind, long value) {
      metricIds[index] = metricId;
      domains[index] = domain;
      kinds[index] = (byte) kind.ordinal();
      values[index] = value;
      return index + 1;
    }

    private static void recordCounter(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot) {
      if (domain == RuntimeDiagnosticSnapshot.Domain.IMAGE) {
        if (featureMetricSlot < 0 || featureMetricSlot >= imageCounters.length) {
          return;
        }
        synchronized (COLLECTION_LOCK) {
          if (imageGroupEnabled) {
            imageCounters[featureMetricSlot]++;
          }
        }
      } else if (domain == RuntimeDiagnosticSnapshot.Domain.RUNTIME) {
        if (featureMetricSlot == 0) {
          addJavaCounter(1L);
        } else if (featureMetricSlot == 1) {
          addNativeCounter(1L);
        }
      } else if (domain == RuntimeDiagnosticSnapshot.Domain.SCHEDULING) {
        synchronized (COLLECTION_LOCK) {
          if (!schedulingGroupEnabled) {
            return;
          }
          if (featureMetricSlot == 0) {
            flickCallbackCount++;
          } else if (featureMetricSlot == 1) {
            flickAdvancementCount++;
          } else if (featureMetricSlot == 2) {
            flickCompletionCount++;
          }
        }
      }
    }

    private static void recordTimer(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot,
        long elapsedNanos) {
      if (domain == RuntimeDiagnosticSnapshot.Domain.RUNTIME && featureMetricSlot == 0) {
        addJavaTimer(elapsedNanos);
      } else if (domain == RuntimeDiagnosticSnapshot.Domain.SCHEDULING) {
        synchronized (COLLECTION_LOCK) {
          if (!schedulingGroupEnabled) {
            return;
          }
          if (featureMetricSlot == 0) {
            flickAdvancementWorkNanos += Math.max(0L, elapsedNanos);
          } else if (featureMetricSlot == 1) {
            flickPositiveLatenessNanos += Math.max(0L, elapsedNanos);
          }
        }
      }
    }

    private static void setGauge(RuntimeDiagnosticSnapshot.Domain domain, int featureMetricSlot, long value) {
      if (domain == RuntimeDiagnosticSnapshot.Domain.RUNTIME && featureMetricSlot == 0) {
        setJavaGauge(value);
      } else if (domain == RuntimeDiagnosticSnapshot.Domain.RUNTIME && featureMetricSlot == 1) {
        setNativeGauge(value);
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

    private static void reset(RuntimeDiagnosticSnapshot.Domain domain) {
      synchronized (COLLECTION_LOCK) {
        if (domain == RuntimeDiagnosticSnapshot.Domain.RUNTIME && runtimeGroupEnabled) {
          javaCounter = 0L;
          javaTimerNanos = 0L;
          nativeBridge.resetMetrics(RUNTIME_GROUP_MASK);
          epoch++;
        } else if (domain == RuntimeDiagnosticSnapshot.Domain.IMAGE && imageGroupEnabled) {
          for (int i = 0; i < imageCounters.length; i++) {
            imageCounters[i] = 0L;
          }
          epoch++;
        } else if (domain == RuntimeDiagnosticSnapshot.Domain.SCHEDULING && schedulingGroupEnabled) {
          flickCallbackCount = 0L;
          flickAdvancementCount = 0L;
          flickCompletionCount = 0L;
          flickAdvancementWorkNanos = 0L;
          flickPositiveLatenessNanos = 0L;
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
