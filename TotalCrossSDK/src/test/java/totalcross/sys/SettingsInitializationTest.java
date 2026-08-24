// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class SettingsInitializationTest {
  @Test
  void initializesLocaleSettingsWithoutChangingGraphics() {
    Locale previousLocale = Locale.getDefault();
    int previousWidth = Settings.screenWidth;
    int previousHeight = Settings.screenHeight;
    int previousWidthInDpi = Settings.screenWidthInDPI;
    int previousHeightInDpi = Settings.screenHeightInDPI;
    int previousBpp = Settings.screenBPP;

    try {
      Locale.setDefault(Locale.GERMANY);
      Settings.screenWidth = 321;
      Settings.screenHeight = 654;
      Settings.screenWidthInDPI = 111;
      Settings.screenHeightInDPI = 222;
      Settings.screenBPP = 24;

      Settings.initializeJavaSESettings();

      assertEquals(Settings.DATE_DMY, Settings.dateFormat);
      assertEquals('.', Settings.dateSeparator);
      assertTrue(Settings.is24Hour);
      assertEquals(':', Settings.timeSeparator);
      assertEquals('.', Settings.thousandsSeparator);
      assertEquals(',', Settings.decimalSeparator);
      assertTrue(Settings.onJavaSE);
      assertEquals(Settings.JAVA, Settings.platform);
      assertEquals(321, Settings.screenWidth);
      assertEquals(654, Settings.screenHeight);
      assertEquals(111, Settings.screenWidthInDPI);
      assertEquals(222, Settings.screenHeightInDPI);
      assertEquals(24, Settings.screenBPP);
    } finally {
      Locale.setDefault(previousLocale);
      Settings.screenWidth = previousWidth;
      Settings.screenHeight = previousHeight;
      Settings.screenWidthInDPI = previousWidthInDpi;
      Settings.screenHeightInDPI = previousHeightInDpi;
      Settings.screenBPP = previousBpp;
      Settings.initializeJavaSESettings();
    }
  }

  @Test
  void loadingSettingsInitializesJavaSEValuesWithoutStartingAwt() throws Exception {
    String executable = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java")
        .toString();
    Process process = new ProcessBuilder(executable, "-cp", System.getProperty("java.class.path"),
        SettingsInitializationProbe.class.getName()).redirectErrorStream(true).start();

    boolean finished = process.waitFor(10, TimeUnit.SECONDS);
    if (!finished) {
      process.destroyForcibly();
    }
    assertTrue(finished, "Settings initialization probe timed out");
    String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    assertEquals(0, process.exitValue(), output);
    assertTrue(output.contains("settings-initialized"), output);
  }

  private static boolean isWindows() {
    return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
  }

  public static final class SettingsInitializationProbe {
    public static void main(String[] args) throws Exception {
      Class.forName(Settings.class.getName());
      Thread.sleep(200);

      boolean awtStarted = Thread.getAllStackTraces().keySet().stream().map(Thread::getName)
          .anyMatch(name -> name.equals("AppKit Thread") || name.startsWith("AWT-"));
      if (awtStarted || !Settings.onJavaSE || !Settings.JAVA.equals(Settings.platform)
          || Settings.dateSeparator == 0 || Settings.timeSeparator == 0 || Settings.decimalSeparator == 0) {
        System.exit(1);
      }
      System.out.println("settings-initialized");
    }
  }
}
