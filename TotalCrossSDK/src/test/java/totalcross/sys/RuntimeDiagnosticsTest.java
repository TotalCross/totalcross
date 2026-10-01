// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import totalcross.ui.image.ImagePreparationFeatureBridge;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeDiagnosticsTest {
  private Class<?> supportClass;
  private Class<?> runtimeMetricsClass;
  private Field bridgeField;
  private Object originalBridge;
  private NativeValues nativeValues;

  @BeforeEach
  void setUp() throws Exception {
    if (!RuntimeDiagnostics.isSupported()) {
      return;
    }
    supportClass = Class.forName("totalcross.sys.RuntimeDiagnosticsSupport");
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, false);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RUNTIME, true);
    runtimeMetricsClass = Class.forName("totalcross.sys.RuntimeDiagnosticsSupport$RuntimeMetrics");
    bridgeField = runtimeMetricsClass.getDeclaredField("nativeBridge");
    bridgeField.setAccessible(true);
    originalBridge = bridgeField.get(null);
    nativeValues = new NativeValues(
        privateInt("NATIVE_COUNTER_ID"), privateInt("NATIVE_GAUGE_ID"));
    Object fakeBridge = Proxy.newProxyInstance(supportClass.getClassLoader(),
        new Class<?>[] {bridgeField.getType()}, nativeValues);
    bridgeField.set(null, fakeBridge);

    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.RUNTIME);
    invoke("setJavaGaugeForTest", new Class<?>[] {long.class}, 0L);
    invoke("setNativeGaugeForTest", new Class<?>[] {long.class}, 0L);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RUNTIME, false);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (supportClass != null) {
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RUNTIME, false);
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, false);
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
      bridgeField.set(null, originalBridge);
    }
  }

  @Test
  void publicMetadataContainsDomainsAndKindsWithoutMetricKeys() {
    assertEquals(4, RuntimeDiagnosticSnapshot.Domain.values().length);
    assertEquals("RUNTIME", RuntimeDiagnosticSnapshot.Domain.RUNTIME.name());
    assertEquals("IMAGE", RuntimeDiagnosticSnapshot.Domain.IMAGE.name());
    assertEquals("SCHEDULING", RuntimeDiagnosticSnapshot.Domain.SCHEDULING.name());
    assertEquals(0, RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal());
    assertEquals(1, RuntimeDiagnosticSnapshot.Domain.IMAGE.ordinal());
    assertEquals(2, RuntimeDiagnosticSnapshot.Domain.SCHEDULING.ordinal());
    assertEquals("PREFETCH", RuntimeDiagnosticSnapshot.Domain.PREFETCH.name());
    assertEquals(3, RuntimeDiagnosticSnapshot.Domain.PREFETCH.ordinal());
    assertEquals(3, RuntimeDiagnosticSnapshot.Kind.values().length);
    assertEquals(RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal(), 0);
    assertEquals(RuntimeDiagnosticSnapshot.Kind.GAUGE.ordinal(), 1);
    assertEquals(RuntimeDiagnosticSnapshot.Kind.TIMER.ordinal(), 2);
  }

  @Test
  void prefetchDomainCollectsOnlyAggregateFeatureValues() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.PREFETCH);
    final int[] completions = {0};
    ImagePreparationFeatureBridge.prepareForDisplay(null, 1.0, 1L, new Runnable() {
      @Override
      public void run() {
        completions[0]++;
      }
    });

    RuntimeDiagnosticSnapshot snapshot = RuntimeDiagnostics.snapshot();
    assertEquals(1, completions[0]);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 0);
    RuntimeDiagnosticsFeatureBridge.setGauge(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 8, 12L);
    RuntimeDiagnosticsFeatureBridge.setGauge(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 9, 1L);
    snapshot = RuntimeDiagnostics.snapshot();
    assertEquals(3L, snapshot.getValue(RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(13L, snapshot.getValue(RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.GAUGE));
    assertEquals(0, nativeValues.batchReads);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
  }

  @Test
  void defaultBuildReturnsTheSharedEmptySnapshot() {
    assumeFalse(RuntimeDiagnostics.isSupported());
    RuntimeDiagnosticSnapshot first = RuntimeDiagnostics.snapshot();
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RUNTIME, true);
    RuntimeDiagnosticSnapshot second = RuntimeDiagnostics.snapshot();
    assertSame(first, second);
    assertEquals(0, first.size());
    assertFalse(RuntimeDiagnostics.isSupported());
  }

  @Test
  void snapshotCopiesItsInputAndExposesNoMetricIdentifiers() {
    int[] ids = {17};
    byte[] domains = {(byte) RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal()};
    byte[] kinds = {(byte) RuntimeDiagnosticSnapshot.Kind.COUNTER.ordinal()};
    long[] values = {9L};
    RuntimeDiagnosticSnapshot snapshot = new RuntimeDiagnosticSnapshot(ids, domains, kinds, values, 3L);
    ids[0] = 99;
    domains[0] = -1;
    kinds[0] = -1;
    values[0] = -1;
    assertEquals(9L, snapshot.getValue(RuntimeDiagnosticSnapshot.Domain.RUNTIME,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
  }

  @Test
  void readsJavaOwnedMetricsDirectlyIntoAJavaAndNativeSnapshot() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableDomain();
    invoke("addJavaCounterForTest", new Class<?>[] {long.class}, 3L);
    invoke("setJavaGaugeForTest", new Class<?>[] {long.class}, 5L);
    invoke("addJavaTimerForTest", new Class<?>[] {long.class}, 7L);

    RuntimeDiagnosticSnapshot snapshot = RuntimeDiagnostics.snapshot();
    assertEquals(3L, value(snapshot, RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(5L, value(snapshot, RuntimeDiagnosticSnapshot.Kind.GAUGE));
    assertEquals(7L, value(snapshot, RuntimeDiagnosticSnapshot.Kind.TIMER));
    assertEquals(1, nativeValues.batchReads);
    assertEquals(0, nativeValues.singleReads);
    assertEquals(2, nativeValues.lastIds.length);
    assertEquals(false, contains(nativeValues.lastIds, privateInt("JAVA_COUNTER_ID")));
  }

  @Test
  void nativeOwnedSnapshotUsesOneBatchAndSupportsSingleReads() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableDomain();
    invoke("addNativeCounterForTest", new Class<?>[] {long.class}, 11L);

    RuntimeDiagnosticSnapshot snapshot = RuntimeDiagnostics.snapshot();
    assertEquals(11L, value(snapshot, RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(1, nativeValues.batchReads);
    assertEquals(11L, invoke("readSingleNativeMetricForTest", new Class<?>[0]));
    assertEquals(1, nativeValues.singleReads);
  }

  @Test
  void mixedJavaAndNativeValuesShareTheSnapshotContract() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableDomain();
    invoke("addJavaCounterForTest", new Class<?>[] {long.class}, 2L);
    invoke("addNativeCounterForTest", new Class<?>[] {long.class}, 3L);
    RuntimeDiagnosticSnapshot snapshot = RuntimeDiagnostics.snapshot();
    assertEquals(5L, value(snapshot, RuntimeDiagnosticSnapshot.Kind.COUNTER));
  }

  @Test
  void snapshotsAreImmutableAndCounterTimerDeltasAreCumulative() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableDomain();
    invoke("addJavaCounterForTest", new Class<?>[] {long.class}, 5L);
    invoke("addNativeCounterForTest", new Class<?>[] {long.class}, 6L);
    invoke("addJavaTimerForTest", new Class<?>[] {long.class}, 9L);
    RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();

    invoke("addJavaCounterForTest", new Class<?>[] {long.class}, 3L);
    invoke("addNativeCounterForTest", new Class<?>[] {long.class}, 4L);
    invoke("addJavaTimerForTest", new Class<?>[] {long.class}, 5L);
    RuntimeDiagnosticSnapshot after = RuntimeDiagnostics.snapshot();
    RuntimeDiagnosticSnapshot delta = after.deltaSince(before);

    assertEquals(11L, value(before, RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(7L, value(delta, RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(5L, value(delta, RuntimeDiagnosticSnapshot.Kind.TIMER));
  }

  @Test
  void gaugesRemainAbsoluteInDeltasAndAreNotReset() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableDomain();
    invoke("setJavaGaugeForTest", new Class<?>[] {long.class}, 6L);
    invoke("setNativeGaugeForTest", new Class<?>[] {long.class}, 4L);
    RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
    invoke("setJavaGaugeForTest", new Class<?>[] {long.class}, 10L);
    invoke("setNativeGaugeForTest", new Class<?>[] {long.class}, 2L);
    RuntimeDiagnosticSnapshot after = RuntimeDiagnostics.snapshot();
    assertEquals(12L, value(after.deltaSince(before), RuntimeDiagnosticSnapshot.Kind.GAUGE));

    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.RUNTIME);
    assertEquals(12L, value(RuntimeDiagnostics.snapshot(), RuntimeDiagnosticSnapshot.Kind.GAUGE));
  }

  @Test
  void resetIsSelectiveAndInvalidatesDeltasAcrossEpochs() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableDomain();
    invoke("addJavaCounterForTest", new Class<?>[] {long.class}, 4L);
    invoke("addNativeCounterForTest", new Class<?>[] {long.class}, 6L);
    invoke("addJavaTimerForTest", new Class<?>[] {long.class}, 15L);
    invoke("setJavaGaugeForTest", new Class<?>[] {long.class}, 7L);
    invoke("setNativeGaugeForTest", new Class<?>[] {long.class}, 8L);
    RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();

    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.RUNTIME);
    RuntimeDiagnosticSnapshot after = RuntimeDiagnostics.snapshot();
    assertEquals(0L, value(after, RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0L, value(after, RuntimeDiagnosticSnapshot.Kind.TIMER));
    assertEquals(15L, value(after, RuntimeDiagnosticSnapshot.Kind.GAUGE));
    assertThrows(IllegalArgumentException.class, () -> after.deltaSince(before));
  }

  @Test
  void disabledRuntimeGroupReturnsBeforeCallingTheNativeProvider() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnosticSnapshot first = RuntimeDiagnostics.snapshot();
    RuntimeDiagnosticSnapshot second = RuntimeDiagnostics.snapshot();
    assertSame(first, second);
    assertEquals(0, first.size());
    assertEquals(0, nativeValues.batchReads);
    assertEquals(0, nativeValues.singleReads);
  }

  @Test
  void imageCountersAreIsolatedAndResetByTheirDomain() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.IMAGE, true);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.IMAGE);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE, 0);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE, 5);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE, 8);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE, 16);

    RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
    assertEquals(21, before.size());
    assertEquals(4L, before.getValue(RuntimeDiagnosticSnapshot.Domain.IMAGE,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0L, before.getValue(RuntimeDiagnosticSnapshot.Domain.RUNTIME,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0, nativeValues.batchReads);

    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.IMAGE);
    RuntimeDiagnosticSnapshot after = RuntimeDiagnostics.snapshot();
    assertEquals(0L, after.getValue(RuntimeDiagnosticSnapshot.Domain.IMAGE,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertThrows(IllegalArgumentException.class, () -> after.deltaSince(before));
  }

  @Test
  void imageEventsAreIgnoredBeforeTheImageDomainIsEnabled() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.IMAGE, 0);
    assertSame(RuntimeDiagnostics.snapshot(), RuntimeDiagnostics.snapshot());
    assertEquals(0, RuntimeDiagnostics.snapshot().size());
    assertEquals(0, nativeValues.batchReads);
  }

  @Test
  void schedulingMetricsRecordBoundedCountersAndNonnegativeNanosecondTotals() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.SCHEDULING);

    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 2);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 11L);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 13L);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1, -10L);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1, 7L);
    RuntimeDiagnosticSnapshot snapshot = RuntimeDiagnostics.snapshot();

    assertEquals(5L, value(snapshot, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(31L, value(snapshot, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER));
    assertEquals(0L, value(snapshot, RuntimeDiagnosticSnapshot.Domain.RUNTIME,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0, nativeValues.batchReads);
  }

  @Test
  void schedulingDeltaAndResetLeaveRuntimeDomainValuesAlone() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableDomain();
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    invoke("addJavaCounterForTest", new Class<?>[] {long.class}, 4L);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 2);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 5L);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1, 3L);
    RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();

    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 7L);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1, 2L);
    RuntimeDiagnosticSnapshot after = RuntimeDiagnostics.snapshot();
    RuntimeDiagnosticSnapshot delta = after.deltaSince(before);
    assertEquals(2L, value(delta, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(9L, value(delta, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER));
    long runtimeCounterBeforeReset = value(after, RuntimeDiagnosticSnapshot.Domain.RUNTIME,
        RuntimeDiagnosticSnapshot.Kind.COUNTER);

    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.SCHEDULING);
    RuntimeDiagnosticSnapshot reset = RuntimeDiagnostics.snapshot();
    assertEquals(runtimeCounterBeforeReset, value(reset, RuntimeDiagnosticSnapshot.Domain.RUNTIME,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0L, value(reset, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertThrows(IllegalArgumentException.class, () -> reset.deltaSince(after));
  }

  @Test
  void schedulingEventsAreIgnoredBeforeTheSchedulingDomainIsEnabled() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 17L);
    assertSame(RuntimeDiagnostics.snapshot(), RuntimeDiagnostics.snapshot());
    assertEquals(0, RuntimeDiagnostics.snapshot().size());
    assertEquals(0, nativeValues.batchReads);
  }

  @Test
  void schedulingAndPrefetchTogglesKeepMetricValuesIndependent() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableAndResetFeatureDomains();
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 17L);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1, 2L);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 0);
    RuntimeDiagnosticsFeatureBridge.setGauge(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 8, 7L);
    RuntimeDiagnosticsFeatureBridge.setGauge(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 9, 3L);

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    assertTrue(RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING));
    assertTrue(RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH));
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnosticSnapshot afterSchedulingToggle = RuntimeDiagnostics.snapshot();
    assertEquals(1L, value(afterSchedulingToggle, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(10L, value(afterSchedulingToggle, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.GAUGE));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    assertTrue(RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING));
    assertTrue(RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH));
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
    RuntimeDiagnosticSnapshot afterPrefetchToggle = RuntimeDiagnostics.snapshot();
    assertEquals(2L, value(afterPrefetchToggle, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(19L, value(afterPrefetchToggle, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnosticSnapshot prefetchValuesAfterBothToggles = RuntimeDiagnostics.snapshot();
    assertEquals(1L, value(prefetchValuesAfterBothToggles, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(10L, value(prefetchValuesAfterBothToggles, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.GAUGE));
  }

  @Test
  void resettingSchedulingAndPrefetchPreservesTheOtherDomain() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableAndResetFeatureDomains();
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 23L);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 0);
    RuntimeDiagnosticsFeatureBridge.setGauge(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 8, 5L);

    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.SCHEDULING);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnosticSnapshot prefetchAfterSchedulingReset = RuntimeDiagnostics.snapshot();
    assertEquals(1L, value(prefetchAfterSchedulingReset, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(5L, value(prefetchAfterSchedulingReset, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.GAUGE));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
    RuntimeDiagnosticSnapshot schedulingAfterOwnReset = RuntimeDiagnostics.snapshot();
    assertEquals(0L, value(schedulingAfterOwnReset, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0L, value(schedulingAfterOwnReset, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 2);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1, 31L);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.PREFETCH);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
    RuntimeDiagnosticSnapshot schedulingAfterPrefetchReset = RuntimeDiagnostics.snapshot();
    assertEquals(1L, value(schedulingAfterPrefetchReset, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(31L, value(schedulingAfterPrefetchReset, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnosticSnapshot prefetchAfterOwnReset = RuntimeDiagnostics.snapshot();
    assertEquals(0L, value(prefetchAfterOwnReset, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0L, value(prefetchAfterOwnReset, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.GAUGE));
  }

  @Test
  void disabledSchedulingOrPrefetchDoesNotCollectWhileTheOtherIsEnabled() throws Exception {
    assumeTrue(RuntimeDiagnostics.isSupported());
    enableAndResetFeatureDomains();
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 0, 17L);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 0);
    RuntimeDiagnosticsFeatureBridge.setGauge(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 8, 4L);
    RuntimeDiagnosticSnapshot prefetchWithSchedulingDisabled = RuntimeDiagnostics.snapshot();
    assertEquals(1L, value(prefetchWithSchedulingDisabled, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(4L, value(prefetchWithSchedulingDisabled, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.GAUGE));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
    RuntimeDiagnosticSnapshot schedulingAfterDisabledEvents = RuntimeDiagnostics.snapshot();
    assertEquals(0L, value(schedulingAfterDisabledEvents, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0L, value(schedulingAfterDisabledEvents, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.SCHEDULING);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.PREFETCH);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, false);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 0);
    RuntimeDiagnosticsFeatureBridge.setGauge(RuntimeDiagnosticSnapshot.Domain.PREFETCH, 9, 6L);
    RuntimeDiagnosticsFeatureBridge.recordCounter(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 2);
    RuntimeDiagnosticsFeatureBridge.recordTimer(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, 1, 29L);
    RuntimeDiagnosticSnapshot schedulingWithPrefetchDisabled = RuntimeDiagnostics.snapshot();
    assertEquals(1L, value(schedulingWithPrefetchDisabled, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(29L, value(schedulingWithPrefetchDisabled, RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER));

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    RuntimeDiagnosticSnapshot prefetchAfterDisabledEvents = RuntimeDiagnostics.snapshot();
    assertEquals(0L, value(prefetchAfterDisabledEvents, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(0L, value(prefetchAfterDisabledEvents, RuntimeDiagnosticSnapshot.Domain.PREFETCH,
        RuntimeDiagnosticSnapshot.Kind.GAUGE));
  }

  @Test
  void deltaRejectsDifferentMetricSetsAndPreservesGaugeMeaning() {
    int[] ids = {41};
    byte[] domains = {(byte) RuntimeDiagnosticSnapshot.Domain.RUNTIME.ordinal()};
    byte[] kinds = {(byte) RuntimeDiagnosticSnapshot.Kind.GAUGE.ordinal()};
    RuntimeDiagnosticSnapshot first = new RuntimeDiagnosticSnapshot(ids, domains, kinds, new long[] {4L}, 2L);
    RuntimeDiagnosticSnapshot second = new RuntimeDiagnosticSnapshot(new int[] {42}, domains, kinds,
        new long[] {9L}, 2L);
    assertThrows(IllegalArgumentException.class, () -> second.deltaSince(first));
    assertEquals(9L, second.deltaSince(
        new RuntimeDiagnosticSnapshot(new int[] {42}, domains, kinds, new long[] {4L}, 2L))
        .getValue(RuntimeDiagnosticSnapshot.Domain.RUNTIME, RuntimeDiagnosticSnapshot.Kind.GAUGE));
  }

  private void enableDomain() {
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.RUNTIME, true);
  }

  private void enableAndResetFeatureDomains() throws Exception {
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.PREFETCH, true);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.SCHEDULING);
    invoke("resetForTest", new Class<?>[] {RuntimeDiagnosticSnapshot.Domain.class},
        RuntimeDiagnosticSnapshot.Domain.PREFETCH);
  }

  private static long value(RuntimeDiagnosticSnapshot snapshot, RuntimeDiagnosticSnapshot.Kind kind) {
    return snapshot.getValue(RuntimeDiagnosticSnapshot.Domain.RUNTIME, kind);
  }

  private static long value(RuntimeDiagnosticSnapshot snapshot, RuntimeDiagnosticSnapshot.Domain domain,
      RuntimeDiagnosticSnapshot.Kind kind) {
    return snapshot.getValue(domain, kind);
  }

  private static boolean contains(int[] values, int sought) {
    for (int value : values) {
      if (value == sought) {
        return true;
      }
    }
    return false;
  }

  private static int privateInt(String name) throws Exception {
    Field field = Class.forName("totalcross.sys.RuntimeDiagnosticsSupport$RuntimeMetrics").getDeclaredField(name);
    field.setAccessible(true);
    return field.getInt(null);
  }

  private static Object invoke(String name, Class<?>[] parameterTypes, Object... args) throws Exception {
    Class<?> support = Class.forName("totalcross.sys.RuntimeDiagnosticsSupport");
    Method method = support.getDeclaredMethod(name, parameterTypes);
    method.setAccessible(true);
    try {
      return method.invoke(null, args);
    } catch (InvocationTargetException failure) {
      Throwable cause = failure.getCause();
      if (cause instanceof Exception) {
        throw (Exception) cause;
      }
      throw failure;
    }
  }

  private static final class NativeValues implements InvocationHandler {
    private final int counterId;
    private final int gaugeId;
    private long counter;
    private long gauge;
    private int batchReads;
    private int singleReads;
    private int[] lastIds = new int[0];

    private NativeValues(int counterId, int gaugeId) {
      this.counterId = counterId;
      this.gaugeId = gaugeId;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
      switch (method.getName()) {
      case "readMetric":
        singleReads++;
        return counter;
      case "readMetrics":
        batchReads++;
        int[] ids = (int[]) args[0];
        lastIds = ids.clone();
        long[] values = (long[]) args[1];
        for (int i = 0; i < ids.length; i++) {
          values[i] = ids[i] == counterId ? counter : ids[i] == gaugeId ? gauge : 0L;
        }
        return null;
      case "resetMetrics":
        if (((Integer) args[0]) != 0) {
          counter = 0L;
        }
        return null;
      case "addCounterForTest":
        counter += (Long) args[0];
        return null;
      case "setGaugeForTest":
        gauge = (Long) args[0];
        return null;
      default:
        throw new UnsupportedOperationException(method.getName());
      }
    }
  }
}
