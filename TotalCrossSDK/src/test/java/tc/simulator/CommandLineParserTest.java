// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CommandLineParserTest {
  @Test
  void parsesScreenScaleAndFlags() throws Exception {
    SimulatorConfiguration config = new SimulatorConfiguration("com.example.App", "/scr", "320x480x16", "/scale", "1",
        "/fastscale", "/demo", "/cmdline", "one", "two");

    LaunchOptions result = CommandLineParser.parse(config, true, 0, 0);

    assertEquals("com.example.App", result.className);
    assertEquals(320, result.width);
    assertEquals(480, result.height);
    assertEquals(16, result.bpp);
    assertEquals(1, result.scaleValue);
    assertEquals(1, result.densityValue);
    assertTrue(result.fastScale);
    assertTrue(result.demo);
    assertEquals("one two", result.commandLine);
  }

  @Test
  void leavesAutomaticScaleUnresolved() throws Exception {
    SimulatorConfiguration config = new SimulatorConfiguration("com.example.App", "/scr", "1920x1080x32");

    LaunchOptions result = CommandLineParser.parse(config, true, 0, 0);

    assertEquals(-1, result.scaleValue);
    assertEquals(1, result.densityValue);
  }

  @Test
  void densityLeavesScreenAndSafeAreaInLogicalUnits() throws Exception {
    for (int density = 1; density <= 3; density++) {
      SimulatorConfiguration config = new SimulatorConfiguration("com.example.App", "/scr", "393x852x32",
          "/density", Integer.toString(density), "/safeAreaPortrait", "10,3,7,4", "/safeAreaLandscape",
          "2,11,5,13");

      LaunchOptions result = CommandLineParser.parse(config, true, 0, 0);

      assertEquals(393, result.width);
      assertEquals(852, result.height);
      assertEquals(density, result.densityValue);
      assertInsets(result.insetsPortrait, 10, 3, 7, 4);
      assertInsets(result.insetsLandscape, 2, 11, 5, 13);
    }
  }

  @Test
  void storesSettingsFlagsWithoutApplyingThem() throws Exception {
    SimulatorConfiguration config = new SimulatorConfiguration("com.example.App", "/scr", "320x480x16", "/fingertouch",
        "/geofocus", "/virtualKeyboard", "/showmousepos", "/dbginfo");

    LaunchOptions result = CommandLineParser.parse(config, true, 0, 0);

    assertTrue(result.fingerTouch);
    assertTrue(result.geographicalFocus);
    assertTrue(result.keyboardFocusTraversable);
    assertTrue(result.virtualKeyboard);
    assertTrue(result.showMousePosition);
    assertTrue(result.showDebugMessages);
  }

  @Test
  void reportsInvalidArgumentDetails() {
    SimulatorConfiguration config = new SimulatorConfiguration("com.example.App", "/bpp", "7");

    CommandLineParser.InvalidArgumentException error = assertThrows(
        CommandLineParser.InvalidArgumentException.class,
        () -> CommandLineParser.parse(config, true, 0, 0));

    assertEquals(1, error.getIndex());
    assertEquals("7", error.getArgument());
    assertEquals("/bpp 7", error.getFullCommandLine());
  }

  private static void assertInsets(totalcross.ui.Insets insets, int top, int left, int bottom, int right) {
    assertEquals(top, insets.top);
    assertEquals(left, insets.left);
    assertEquals(bottom, insets.bottom);
    assertEquals(right, insets.right);
  }
}
