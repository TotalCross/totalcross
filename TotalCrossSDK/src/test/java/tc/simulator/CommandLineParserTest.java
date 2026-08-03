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
}
