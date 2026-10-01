// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class RuntimeDiagnosticsConverterTest {
  private static final String FLAG = "TC_ENABLE_RUNTIME_DIAGNOSTICS";
  private static final String[] SYMBOLS = {
      "tsRDS_readMetricNative_i",
      "tsRDS_readMetricsNative_IL",
      "tsRDS_resetMetricsNative_i",
      "tsRDS_addNativeCountNative_l",
      "tsRDS_setNativeGaugeNative_l"
  };

  @Test
  void defaultSdkVariantHasNoOptionalMetricStorageOrNativeMethods() throws Exception {
    String off = Files.readString(Path.of("src/runtimeDiagnosticsOff/java/totalcross/sys/RuntimeDiagnosticsSupport.java"));
    assertTrue(off.contains("return false;"));
    assertTrue(off.contains("return RuntimeDiagnosticSnapshot.empty();"));
    assertFalse(off.contains("static native "));
    assertFalse(off.contains("runtimeGroupEnabled"));
    assertFalse(off.contains("schedulingGroupEnabled"));
    assertFalse(off.contains("renderingGroupEnabled"));
    assertFalse(off.contains("prefetchGroupEnabled"));
    assertFalse(off.contains("schedulingGeneration"));
    assertFalse(off.contains("NATIVE_METRIC_IDS"));
    assertFalse(off.contains("long javaCounter"));
    assertFalse(off.contains("synchronized ("));
  }

  @Test
  void optionalNativeBridgeAndRegistrationAreCompileTimeGated() throws Exception {
    Path vmRoot = Path.of("..", "TotalCrossVM");
    String cmake = Files.readString(vmRoot.resolve("CMakeLists.txt"));
    String declarations = Files.readString(vmRoot.resolve("src/nm/NativeMethods.txt"));
    String prototypes = Files.readString(vmRoot.resolve("src/nm/NativeMethodsPrototypes.txt"));
    String header = Files.readString(vmRoot.resolve("src/nm/NativeMethods.h"));
    String registrations = Files.readString(vmRoot.resolve("src/init/nativeProcAddressesTC.c"));
    String implementation = Files.readString(vmRoot.resolve("src/nm/sys/RuntimeDiagnostics.c"));

    assertTrue(cmake.contains("option(" + FLAG
        + "\n  \"Enable optional runtime diagnostics storage and native bridge\"\n  OFF\n)"));
    assertTrue(cmake.contains("if(" + FLAG + ")\n  target_compile_definitions(tcvm PRIVATE " + FLAG + "=1)\nendif()"));
    assertTrue(cmake.contains("if(" + FLAG + ")\n  list(APPEND SOURCES ${TC_SRCDIR}/nm/sys/RuntimeDiagnostics.c)\nendif()"));
    for (String symbol : SYMBOLS) {
      assertTrue(prototypes.contains("TC_API void " + symbol + "(NMParams p);"), symbol);
      assertTrue(header.contains("TC_API void " + symbol + "(NMParams p);"), symbol);
      assertTrue(registrations.contains("hashCode(\"" + symbol + "\"), &" + symbol), symbol);
      assertTrue(implementation.contains("TC_API void " + symbol + "(NMParams p)"), symbol);
      assertTrue(declarations.contains("totalcross/sys/RuntimeDiagnosticsSupport|"), symbol);
    }

    String defaultRegistration = withoutFlag(registrations, FLAG);
    String defaultImplementation = withoutFlag(implementation, FLAG);
    for (String symbol : SYMBOLS) {
      assertFalse(defaultRegistration.contains(symbol), symbol);
      assertFalse(defaultImplementation.contains(symbol), symbol);
    }
    assertFalse(defaultImplementation.contains("runtimeDiagnosticNativeCounter"));
    assertFalse(defaultImplementation.contains("runtimeDiagnosticNativeGauge"));
    String javaSupport = Files.readString(
        Path.of("src/runtimeDiagnostics/java/totalcross/sys/RuntimeDiagnosticsSupport.java"));
    assertTrue(javaSupport.contains("NATIVE_COUNTER_ID = 0x2001"));
    assertTrue(implementation.contains("RUNTIME_DIAGNOSTIC_NATIVE_COUNTER = 0x2001"));
    assertTrue(javaSupport.contains("NATIVE_GAUGE_ID = 0x2002"));
    assertTrue(implementation.contains("RUNTIME_DIAGNOSTIC_NATIVE_GAUGE = 0x2002"));
    assertTrue(javaSupport.contains("RUNTIME_GROUP_MASK = 1"));
    assertTrue(implementation.contains("RUNTIME_DIAGNOSTIC_RUNTIME_GROUP = 1"));
  }

  @Test
  void runtimeGroupGatePrecedesCollectionLockAllocationAndNativeRead() throws Exception {
    String source = Files.readString(
        Path.of("src/runtimeDiagnostics/java/totalcross/sys/RuntimeDiagnosticsSupport.java"));
    int start = source.indexOf("static RuntimeDiagnosticSnapshot snapshot() {");
    int gate = source.indexOf(
        "if (!runtimeGroupEnabled && !imageGroupEnabled && !schedulingGroupEnabled && !prefetchGroupEnabled",
        start);
    int snapshotCall = source.indexOf("RuntimeMetrics.snapshot(runtimeGroupEnabled, imageGroupEnabled,",
        gate);
    int schedulingArgument = source.indexOf("schedulingGroupEnabled", snapshotCall);
    int prefetchArgument = source.indexOf("prefetchGroupEnabled", schedulingArgument);
    int renderingArgument = source.indexOf("renderingGroupEnabled", prefetchArgument);
    int metricsStart = source.indexOf("private static RuntimeDiagnosticSnapshot snapshot(boolean includeRuntime,",
        snapshotCall);
    int metricsGate = source.indexOf(
        "if (!includeRuntime && !includeImage && !includeScheduling && !includePrefetch && !includeRendering)",
        metricsStart);
    int lock = source.indexOf("synchronized (COLLECTION_LOCK)", metricsStart);
    int batchRead = source.indexOf("nativeBridge.readMetrics(NATIVE_METRIC_IDS, NATIVE_VALUES)", metricsStart);
    int valuesAllocation = source.indexOf("long[] values = new long[total]", metricsStart);
    assertTrue(start >= 0 && gate > start && snapshotCall > gate);
    assertTrue(source.substring(gate, snapshotCall).contains("&& !renderingGroupEnabled)"));
    assertTrue(schedulingArgument >= snapshotCall && prefetchArgument > schedulingArgument
        && renderingArgument > prefetchArgument);
    assertTrue(metricsStart > snapshotCall && metricsGate > metricsStart && lock > metricsGate);
    assertTrue(batchRead > lock && valuesAllocation > batchRead);
    int schedulingAssembly = source.indexOf("if (includeScheduling) {", metricsStart);
    int prefetchAssembly = source.indexOf("if (includePrefetch) {", schedulingAssembly);
    int renderingAssembly = source.indexOf("if (includeRendering) {", prefetchAssembly);
    assertTrue(schedulingAssembly >= metricsStart && prefetchAssembly > schedulingAssembly
        && renderingAssembly > prefetchAssembly);
    assertTrue(source.contains("if (enabled) {\n      RuntimeMetrics.initialize();"));
    assertTrue(source.contains("private static final class RuntimeMetrics"));
    assertTrue(source.contains("nativeBridge.readMetrics(NATIVE_METRIC_IDS, NATIVE_VALUES)"));
    assertTrue(source.contains("0x5001, 0x5002, 0x5003, 0x5004, 0x5005"));
    assertTrue(source.contains("0x5006, 0x5007, 0x5008, 0x5009, 0x500A"));
    assertTrue(source.contains("FLICK_CALLBACK_COUNT_ID = 0x4001"));
    assertTrue(source.contains("FLICK_ADVANCEMENT_COUNT_ID = 0x4002"));
    assertTrue(source.contains("FLICK_COMPLETION_COUNT_ID = 0x4003"));
    assertTrue(source.contains("FLICK_ADVANCEMENT_WORK_NANOS_ID = 0x4004"));
    assertTrue(source.contains("FLICK_POSITIVE_LATENESS_NANOS_ID = 0x4005"));
    assertTrue(source.contains("REUSE_ATTEMPT_ID = 0x6001"));
    assertTrue(source.contains("REUSE_SUCCESS_ID = 0x6002"));
    assertTrue(source.contains("REUSE_FALLBACK_ID = 0x6003"));
    assertTrue(source.contains("MOVE_RECOVERED_ID = 0x6004"));
    assertFalse(Files.readString(Path.of("src/main/java/totalcross/sys/RuntimeDiagnosticSnapshot.java"))
        .contains("0x500"));
    assertTrue(source.contains("0x5001, 0x5002, 0x5003, 0x5004, 0x5005"));
    assertTrue(source.contains("0x5006, 0x5007, 0x5008, 0x5009, 0x500A"));
    assertFalse(Files.readString(Path.of("src/main/java/totalcross/sys/RuntimeDiagnosticSnapshot.java"))
        .contains("0x500"));
    assertFalse(source.contains("System.nanoTime"));
  }

  @Test
  void nativeSignaturesStayPrivateAndThePublicSurfaceHasNoKeysIdsOrMasks() throws Exception {
    String declarations = Files.readString(Path.of("../TotalCrossVM/src/nm/NativeMethods.txt"));
    String support = Files.readString(
        Path.of("src/runtimeDiagnostics/java/totalcross/sys/RuntimeDiagnosticsSupport.java"));
    String publicApi = Files.readString(Path.of("src/main/java/totalcross/sys/RuntimeDiagnostics.java"));
    String publicSnapshot = Files.readString(
        Path.of("src/main/java/totalcross/sys/RuntimeDiagnosticSnapshot.java"));

    for (String method : new String[] {"readMetricNative", "readMetricsNative", "resetMetricsNative",
        "addNativeCountNative", "setNativeGaugeNative"}) {
      assertTrue(support.contains("private static native"));
      assertTrue(declarations.contains("|native private static"));
      assertFalse(publicApi.contains(method));
      assertFalse(publicSnapshot.contains(method));
    }
    assertFalse(publicApi.contains("metricId"));
    assertFalse(publicApi.contains("groupMask"));
    assertFalse(publicSnapshot.contains("getMetricId"));
    assertFalse(publicSnapshot.contains("getMetricKey"));
    assertFalse(publicSnapshot.contains("resetMetrics"));
  }

  @Test
  void flickDiagnosticTimingIsLazyAndDisabledCallbacksSkipItsState() throws Exception {
    String source = Files.readString(Path.of("src/main/java/totalcross/ui/Flick.java"));
    String gate = "RuntimeDiagnosticsFeatureBridge.isDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING)";
    int reset = source.indexOf("private void resetDiagnosticTiming()");
    int resetGate = source.indexOf("if (!" + gate + ")", reset);
    int resetRead = source.indexOf("System.nanoTime()", resetGate);
    int callback = source.indexOf("private DiagnosticTiming recordCallbackDiagnostics()");
    int callbackGate = source.indexOf("if (!" + gate + ")", callback);
    int callbackDisabledReturn = source.indexOf("return null;", callbackGate);
    int callbackRead = source.indexOf("System.nanoTime()", callbackGate);
    int advance = source.indexOf("private void advanceAnimationFromDriver(DiagnosticTiming timing)");
    int advanceGate = source.indexOf("if (timing == null", advance);
    int advanceDisabledReturn = source.indexOf("return;", advanceGate);
    int workStart = source.indexOf("System.nanoTime()", advanceGate);
    int workEnd = source.indexOf("System.nanoTime()", workStart + 1);
    int motion = source.indexOf("private boolean advanceAnimation()");

    assertTrue(source.contains("private DiagnosticTiming diagnosticTiming;"));
    assertFalse(source.contains("private long diagnosticIntervalNanos"));
    assertFalse(source.contains("private long expectedCallbackNanoTime"));
    assertFalse(source.contains("private boolean expectedCallbackTimeSet"));
    assertEquals(4, source.split("System.nanoTime\\(\\)", -1).length - 1);
    assertTrue(reset >= 0 && resetGate > reset && resetRead > resetGate);
    int resetOpeningBrace = source.indexOf('{', reset);
    assertTrue(source.substring(resetOpeningBrace + 1, resetGate).trim().isEmpty());
    assertTrue(source.indexOf("long intervalNanos", resetGate) > resetGate);
    assertTrue(source.indexOf("diagnosticTiming", resetGate) > resetGate);
    assertTrue(source.indexOf("new DiagnosticTiming()", resetGate) > resetGate);
    assertTrue(callback >= 0 && callbackGate > callback && callbackDisabledReturn > callbackGate
        && callbackRead > callbackDisabledReturn);
    String callbackDisabledPath = source.substring(callbackGate, callbackDisabledReturn);
    assertFalse(callbackDisabledPath.contains("diagnosticTiming"));
    assertFalse(callbackDisabledPath.contains("System.nanoTime"));
    assertFalse(callbackDisabledPath.contains("new DiagnosticTiming"));
    assertTrue(advance >= 0 && advanceGate > advance && advanceDisabledReturn > advanceGate
        && workStart > advanceDisabledReturn && workEnd > workStart);
    String advanceDisabledPath = source.substring(advanceGate, advanceDisabledReturn);
    assertTrue(advanceDisabledPath.contains("advanceAnimation();"));
    assertFalse(advanceDisabledPath.contains("diagnosticTiming"));
    assertFalse(advanceDisabledPath.contains("System.nanoTime"));
    assertFalse(advanceDisabledPath.contains("recordCounter"));
    assertTrue(motion > workEnd);
  }

  private static String withoutFlag(String source, String flag) {
    StringBuilder result = new StringBuilder();
    boolean excluded = false;
    for (String line : source.split("\\R")) {
      if (line.trim().equals("#if defined(" + flag + ")")) {
        excluded = true;
      } else if (excluded && line.trim().equals("#endif")) {
        excluded = false;
      } else if (!excluded) {
        result.append(line).append('\n');
      }
    }
    return result.toString();
  }
}
