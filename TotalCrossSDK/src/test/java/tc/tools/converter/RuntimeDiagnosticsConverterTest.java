// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

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
    int gate = source.indexOf("if (!runtimeGroupEnabled && !imageGroupEnabled)", start);
    int lock = source.indexOf("synchronized (COLLECTION_LOCK)", start);
    int batchRead = source.indexOf("nativeBridge.readMetrics(NATIVE_METRIC_IDS, NATIVE_VALUES)", start);
    int valuesAllocation = source.indexOf("long[] values =", start);
    assertTrue(start >= 0 && gate > start && lock > gate);
    assertTrue(batchRead > lock && valuesAllocation > batchRead);
    assertTrue(source.contains("if (enabled) {\n      RuntimeMetrics.initialize();"));
    assertTrue(source.contains("private static final class RuntimeMetrics"));
    assertTrue(source.contains("nativeBridge.readMetrics(NATIVE_METRIC_IDS, NATIVE_VALUES)"));
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
