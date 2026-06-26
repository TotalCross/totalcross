// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.Settings;

class LauncherRuntimeTest {
  private final boolean keyboardFocusTraversable = Settings.keyboardFocusTraversable;
  private final boolean fingerTouch = Settings.fingerTouch;
  private final boolean unmovableSIP = Settings.unmovableSIP;
  private final boolean geographicalFocus = Settings.geographicalFocus;
  private final boolean virtualKeyboard = Settings.virtualKeyboard;
  private final boolean showMousePosition = Settings.showMousePosition;
  private final boolean showDebugMessages = Settings.showDebugMessages;
  private final double screenDensity = Settings.screenDensity;
  private final String dataPath = Settings.dataPath;
  private final int userFontSize = Launcher.userFontSize;
  private final Launcher launcherInstance = Launcher.instance;

  @AfterEach
  void restoreSettings() {
    Settings.keyboardFocusTraversable = keyboardFocusTraversable;
    Settings.fingerTouch = fingerTouch;
    Settings.unmovableSIP = unmovableSIP;
    Settings.geographicalFocus = geographicalFocus;
    Settings.virtualKeyboard = virtualKeyboard;
    Settings.showMousePosition = showMousePosition;
    Settings.showDebugMessages = showDebugMessages;
    Settings.screenDensity = screenDensity;
    Settings.dataPath = dataPath;
    Launcher.userFontSize = userFontSize;
    Launcher.instance = launcherInstance;
  }

  @Test
  void parseArgumentsKeepsDefensiveCopy() {
    LauncherRuntime runtime = new LauncherRuntime();
    String[] args = new String[] { "/scr", "320x480x32" };

    runtime.parseArguments("com.example.App", args);
    args[1] = "changed";
    String[] storedArgs = runtime.getLauncherArgs();
    storedArgs[0] = "changed";

    assertArrayEquals(new String[] { "/scr", "320x480x32" }, runtime.getLauncherArgs());
  }

  @Test
  void configureKeepsImmutableLauncherConfig() {
    String[] args = new String[] { "/scr", "320x480x32" };
    LauncherConfig config = new LauncherConfig("com.example.App", args);
    args[1] = "changed";
    LauncherRuntime runtime = new LauncherRuntime();

    runtime.configure(config);

    assertEquals("com.example.App", runtime.getMainWindowClass());
    assertArrayEquals(new String[] { "/scr", "320x480x32" }, runtime.getLauncherArgs());
  }

  @Test
  void configureClearsParsedConfig() {
    LauncherRuntime runtime = new LauncherRuntime();
    runtime.setParsedConfig(new LauncherParsedConfig("com.example.App"));

    runtime.configure(new LauncherConfig("com.example.Other"));

    assertNull(runtime.getParsedConfig());
  }

  @Test
  void startRequiresParsedMainWindowClass() {
    LauncherRuntime runtime = new LauncherRuntime();

    assertThrows(IllegalStateException.class, runtime::startApplication);
  }

  @Test
  void setNewMainWindowRequiresStartedRuntime() {
    LauncherRuntime runtime = new LauncherRuntime();

    assertThrows(IllegalStateException.class, () -> runtime.setNewMainWindow(null, ""));
  }

  @Test
  void startWindowBackendRequiresParsedConfig() {
    LauncherRuntime runtime = new LauncherRuntime();

    assertThrows(IllegalStateException.class, () -> runtime.startWindowBackend(null, "title", null, null, null));
  }

  @Test
  void normalizesMainWindowClassNames() {
    assertEquals("tc.samples.Main", LauncherRuntime.normalizeMainWindowClassName("tc/samples/Main.class"));
  }

  @Test
  void applyParsedConfigUpdatesRuntimeSettings() {
    LauncherRuntime runtime = new LauncherRuntime();
    LauncherParsedConfig parsedConfig = new LauncherParsedConfig("com.example.App");
    parsedConfig.userFontSize = 18;
    parsedConfig.keyboardFocusTraversable = true;
    parsedConfig.fingerTouch = true;
    parsedConfig.unmovableSIP = true;
    parsedConfig.geographicalFocus = true;
    parsedConfig.virtualKeyboard = true;
    parsedConfig.showMousePosition = true;
    parsedConfig.showDebugMessages = true;
    parsedConfig.densityValue = 2;
    parsedConfig.dataPath = "data";

    runtime.applyParsedConfig(new Launcher(null, true), parsedConfig);

    assertEquals(18, Launcher.userFontSize);
    assertEquals(true, Settings.keyboardFocusTraversable);
    assertEquals(true, Settings.fingerTouch);
    assertEquals(true, Settings.unmovableSIP);
    assertEquals(true, Settings.geographicalFocus);
    assertEquals(true, Settings.virtualKeyboard);
    assertEquals(true, Settings.showMousePosition);
    assertEquals(true, Settings.showDebugMessages);
    assertEquals(2, Settings.screenDensity);
    assertEquals("data", Settings.dataPath);
  }
}
