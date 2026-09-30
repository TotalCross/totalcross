// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Settings;

class RuntimeEnvironmentTest {
  @Test
  void mapsHostOperatingSystemsToTypedPlatformsAndFamilies() {
    assertEnvironment("Win32", true, "Windows 11", "amd64", 0, 0, 1, Platform.WINDOWS,
        RuntimeFamily.DESKTOP, GraphicsBackend.RASTER);
    assertEnvironment("Java", true, "Mac OS X", "aarch64", 0, 0, 1, Platform.MACOS, RuntimeFamily.DESKTOP,
        GraphicsBackend.RASTER);
    assertEnvironment("Java", true, "Linux", "x86_64", 0, 0, 1, Platform.LINUX, RuntimeFamily.DESKTOP,
        GraphicsBackend.RASTER);
  }

  @Test
  void mapsNativePlatformsAndFamiliesIndependently() {
    assertEnvironment("Android", false, null, null, 4, 4, 2, Platform.ANDROID, RuntimeFamily.MOBILE,
        GraphicsBackend.GPU);
    assertEnvironment("iPhone", false, null, null, 5, 4, 2, Platform.IOS, RuntimeFamily.MOBILE,
        GraphicsBackend.GPU);
    assertEnvironment("WindowsCE", false, null, null, 1, 3, 1, Platform.WINDOWS, RuntimeFamily.EMBEDDED,
        GraphicsBackend.RASTER);
    assertEnvironment("Linux_ARM", false, null, null, 3, 3, 0, Platform.LINUX, RuntimeFamily.EMBEDDED, null);
  }

  @Test
  void mapsSupportedArchitectureNamesAndNativeCodes() {
    assertEquals(Architecture.X86, RuntimeEnvironment.architectureFromName("i686"));
    assertEquals(Architecture.X86_64, RuntimeEnvironment.architectureFromName("AMD64"));
    assertEquals(Architecture.ARM32, RuntimeEnvironment.architectureFromName("armv7l"));
    assertEquals(Architecture.ARM32, RuntimeEnvironment.architectureFromName("armeabi-v7a"));
    assertEquals(Architecture.ARM64, RuntimeEnvironment.architectureFromName("aarch64"));
    assertEquals(Architecture.ARM64, RuntimeEnvironment.architectureFromName("arm64e"));
    assertNull(RuntimeEnvironment.architectureFromName("mips"));
    assertNull(RuntimeEnvironment.architectureFromName("arm_unknown"));
    assertNull(RuntimeEnvironment.architectureFromName("arm64_unknown"));
    assertEquals(Architecture.ARM32, RuntimeEnvironment.fromRuntimeSettings("Android", false, null, null, 4, 3, 2)
        .architecture());
    assertEquals(Architecture.ARM64, RuntimeEnvironment.fromRuntimeSettings("Android", false, null, null, 4, 4, 2)
        .architecture());
  }

  @Test
  void environmentEnumsContainOnlyRealFacts() {
    assertArrayEquals(new Platform[] { Platform.WINDOWS, Platform.MACOS, Platform.LINUX, Platform.ANDROID,
        Platform.IOS }, Platform.values());
    assertArrayEquals(new RuntimeFamily[] { RuntimeFamily.DESKTOP, RuntimeFamily.MOBILE, RuntimeFamily.EMBEDDED },
        RuntimeFamily.values());
    assertArrayEquals(new Architecture[] { Architecture.X86, Architecture.X86_64, Architecture.ARM32,
        Architecture.ARM64 }, Architecture.values());
    assertArrayEquals(new GraphicsBackend[] { GraphicsBackend.RASTER, GraphicsBackend.GPU },
        GraphicsBackend.values());
  }

  @Test
  void unavailableFactsAreNullAndNullSnapshotsRemainValueObjects() {
    RuntimeEnvironment unavailable = RuntimeEnvironment.fromRuntimeSettings("unsupported", false, null, null, 0, 0,
        0);
    RuntimeEnvironment sameUnavailable = RuntimeEnvironment.fromRuntimeSettings("unsupported", false, null, null, 0,
        0, 0);

    assertNull(unavailable.platform());
    assertNull(unavailable.runtimeFamily());
    assertNull(unavailable.architecture());
    assertNull(unavailable.graphicsBackend());
    assertFalse(unavailable.isGraphicsBackendFinalized());
    assertEquals(unavailable, sameUnavailable);
    assertEquals(unavailable.hashCode(), sameUnavailable.hashCode());
    assertTrue(unavailable.toString().contains("platform=null"));
    assertTrue(unavailable.toString().contains("runtimeFamily=null"));
    assertTrue(unavailable.toString().contains("architecture=null"));

    RuntimeEnvironment unsupportedHost = RuntimeEnvironment.fromRuntimeSettings(null, true, "Plan 9", "mips", 0, 0,
        0);
    assertNull(unsupportedHost.platform());
    assertNull(unsupportedHost.architecture());
    assertEquals(RuntimeFamily.DESKTOP, unsupportedHost.runtimeFamily());
  }

  @Test
  void familyIsNullWhenKnownPlatformCannotDetermineItsFamily() {
    RuntimeEnvironment environment = RuntimeEnvironment.fromRuntimeSettings("WindowsFuture", false, null, null, 1, 0,
        0);

    assertEquals(Platform.WINDOWS, environment.platform());
    assertNull(environment.runtimeFamily());
  }

  @Test
  void backendCanBeUnresolvedThenFinalizedWithoutMutatingEarlierSnapshot() {
    RuntimeEnvironment unresolved = RuntimeEnvironment.fromRuntimeSettings("Android", false, null, null, 4, 4, 0);
    RuntimeEnvironment finalized = RuntimeEnvironment.fromRuntimeSettings("Android", false, null, null, 4, 4, 2);

    assertFalse(unresolved.isGraphicsBackendFinalized());
    assertNull(unresolved.graphicsBackend());
    assertTrue(finalized.isGraphicsBackendFinalized());
    assertEquals(GraphicsBackend.GPU, finalized.graphicsBackend());
    assertNotEquals(unresolved, finalized);
  }

  @Test
  void snapshotsAreValueObjects() {
    RuntimeEnvironment first = RuntimeEnvironment.fromRuntimeSettings("Win32", false, null, null, 1, 2, 1);
    RuntimeEnvironment same = RuntimeEnvironment.fromRuntimeSettings("Win32", false, null, null, 1, 2, 1);
    RuntimeEnvironment different = RuntimeEnvironment.fromRuntimeSettings("Win32", false, null, null, 1, 1, 1);

    assertEquals(first, same);
    assertEquals(first.hashCode(), same.hashCode());
    assertNotEquals(first, different);
  }

  @Test
  void currentEnvironmentMapsInitializedJavaSeSettings() {
    RuntimeConfigurationStartup.initializeForSimulator(null, System.getProperty("os.name"),
        System.getProperty("os.arch"));
    RuntimeEnvironment environment = RuntimeEnvironment.current();

    assertTrue(Settings.onJavaSE);
    assertEquals(RuntimeEnvironment.fromRuntimeSettings(Settings.platform, true, System.getProperty("os.name"),
        System.getProperty("os.arch"), 0, 0, 0), environment);
  }

  private static void assertEnvironment(String runtimePlatform, boolean onJavaSE, String osName, String osArch,
      int platformCode, int architectureCode, int backendCode, Platform expectedPlatform, RuntimeFamily expectedFamily,
      GraphicsBackend expectedBackend) {
    RuntimeEnvironment environment = RuntimeEnvironment.fromRuntimeSettings(runtimePlatform, onJavaSE, osName, osArch,
        platformCode, architectureCode, backendCode);
    assertEquals(expectedPlatform, environment.platform());
    assertEquals(expectedFamily, environment.runtimeFamily());
    assertEquals(expectedBackend, environment.graphicsBackend());
  }
}
