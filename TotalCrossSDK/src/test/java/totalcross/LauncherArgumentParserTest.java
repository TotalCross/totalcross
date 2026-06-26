// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LauncherArgumentParserTest {
  @Test
  void parsesScreenScaleAndFlags() throws Exception {
    LauncherConfig config = new LauncherConfig("com.example.App", "/scr", "320x480x16", "/scale", "1",
        "/fastscale", "/demo", "/cmdline", "one", "two");

    LauncherParsedConfig result = LauncherArgumentParser.parse(config, true, 0, 0);

    assertEquals("com.example.App", result.className);
    assertEquals(320, result.width);
    assertEquals(480, result.height);
    assertEquals(16, result.bpp);
    assertEquals(1, result.scaleValue);
    assertEquals(1, result.scale);
    assertTrue(result.fastScale);
    assertTrue(result.demo);
    assertEquals("one two", result.commandLine);
  }

  @Test
  void storesSettingsFlagsWithoutApplyingThem() throws Exception {
    LauncherConfig config = new LauncherConfig("com.example.App", "/scr", "320x480x16", "/fingertouch",
        "/geofocus", "/virtualKeyboard", "/showmousepos", "/dbginfo");

    LauncherParsedConfig result = LauncherArgumentParser.parse(config, true, 0, 0);

    assertTrue(result.fingerTouch);
    assertTrue(result.geographicalFocus);
    assertTrue(result.keyboardFocusTraversable);
    assertTrue(result.virtualKeyboard);
    assertTrue(result.showMousePosition);
    assertTrue(result.showDebugMessages);
  }

  @Test
  void reportsInvalidArgumentDetails() {
    LauncherConfig config = new LauncherConfig("com.example.App", "/bpp", "7");

    LauncherArgumentParser.InvalidArgumentException error = assertThrows(
        LauncherArgumentParser.InvalidArgumentException.class,
        () -> LauncherArgumentParser.parse(config, true, 0, 0));

    assertEquals(1, error.getIndex());
    assertEquals("7", error.getArgument());
    assertEquals("/bpp 7", error.getFullCommandLine());
  }
}
