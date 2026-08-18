// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.Settings;

class SimulatorLauncherTest {
  private final boolean keyboardFocusTraversable = Settings.keyboardFocusTraversable;
  private final boolean fingerTouch = Settings.fingerTouch;
  private final boolean unmovableSIP = Settings.unmovableSIP;
  private final boolean geographicalFocus = Settings.geographicalFocus;
  private final boolean virtualKeyboard = Settings.virtualKeyboard;
  private final boolean showMousePosition = Settings.showMousePosition;
  private final boolean showDebugMessages = Settings.showDebugMessages;
  private final int screenWidth = Settings.screenWidth;
  private final int screenHeight = Settings.screenHeight;
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
    Settings.screenWidth = screenWidth;
    Settings.screenHeight = screenHeight;
    Settings.screenDensity = screenDensity;
    totalcross.ui.gfx.Graphics.configureMainWindowSurface(Math.max(0, screenWidth), Math.max(0, screenHeight),
        screenDensity > 0 ? screenDensity : 1);
    Settings.dataPath = dataPath;
    Launcher.userFontSize = userFontSize;
    Launcher.instance = launcherInstance;
  }

  @Test
  void parseArgumentsKeepsDefensiveCopy() {
    Launcher runtime = new Launcher();
    String[] args = new String[] { "/scr", "320x480x32" };

    runtime.parseArguments("com.example.App", args);
    args[1] = "changed";
    String[] storedArgs = runtime.getLauncherArgs();
    storedArgs[0] = "changed";

    assertArrayEquals(new String[] { "/scr", "320x480x32" }, runtime.getLauncherArgs());
  }

  @Test
  void configureKeepsImmutableSimulatorConfiguration() {
    String[] args = new String[] { "/scr", "320x480x32" };
    SimulatorConfiguration config = new SimulatorConfiguration("com.example.App", args);
    args[1] = "changed";
    Launcher runtime = new Launcher();

    runtime.configure(config);

    assertEquals("com.example.App", runtime.getMainWindowClass());
    assertArrayEquals(new String[] { "/scr", "320x480x32" }, runtime.getLauncherArgs());
  }

  @Test
  void configureClearsParsedConfig() {
    Launcher runtime = new Launcher();
    runtime.setParsedConfig(new LaunchOptions("com.example.App"));

    runtime.configure(new SimulatorConfiguration("com.example.Other"));

    assertNull(runtime.getParsedConfig());
  }

  @Test
  void startRequiresParsedMainWindowClass() {
    Launcher runtime = new Launcher();

    assertThrows(IllegalStateException.class, runtime::startApplication);
  }

  @Test
  void setNewMainWindowRequiresStartedRuntime() {
    Launcher runtime = new Launcher();

    assertThrows(IllegalStateException.class, () -> runtime.setNewMainWindow(null, ""));
  }

  @Test
  void startWindowBackendRequiresParsedConfig() {
    Launcher runtime = new Launcher();

    assertThrows(IllegalStateException.class, () -> runtime.startWindowBackend(null, "title", null, null, null));
  }

  @Test
  void normalizesMainWindowClassNames() {
    assertEquals("tc.samples.Main", Launcher.normalizeMainWindowClassName("tc/samples/Main.class"));
  }

  @Test
  void applyParsedConfigUpdatesRuntimeSettings() {
    Launcher runtime = new Launcher();
    LaunchOptions parsedConfig = new LaunchOptions("com.example.App");
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

  @Test
  void inputCoordinatesRemoveOnlyHostPresentationScale() {
    for (int density = 1; density <= 3; density++) {
      Settings.screenDensity = density;
      assertEquals(80, InputDispatcher.toLogicalInputCoordinate(120, 1.5));
      assertEquals(120, InputDispatcher.toLogicalInputCoordinate(120, 1));
    }
  }

  @Test
  void settingsStayLogicalWhileSurfaceBackingUsesDensity() throws Exception {
    Launcher runtime = new Launcher();
    Launcher core = new Launcher(null, true);
    SimulatorConfiguration config = new SimulatorConfiguration("com.example.App", "/scr", "393x852x32",
        "/density", "3", "/safeAreaPortrait", "10,3,7,4");

    runtime.parseSimulatorArguments(core, config, true, 0, 0);
    runtime.initializeSettings(core);

    assertEquals(393, Settings.screenWidth);
    assertEquals(852, Settings.screenHeight);
    assertEquals(3, Settings.screenDensity);
    assertEquals(1179, totalcross.ui.gfx.Graphics.getMainWindowPixelWidth());
    assertEquals(2556, totalcross.ui.gfx.Graphics.getMainWindowPixelHeight());
    assertEquals(10, core.toInsetsPortrait.top);
    assertEquals(3, core.toInsetsPortrait.left);
    assertEquals(7, core.toInsetsPortrait.bottom);
    assertEquals(4, core.toInsetsPortrait.right);
  }
}
