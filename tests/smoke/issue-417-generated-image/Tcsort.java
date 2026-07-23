// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.io.device.printer.MonoImage;
import totalcross.sys.Settings;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.util.zip.CRC32;

/**
 * Issue 417 regression smoke test. It intentionally keeps the original
 * MonoImage/getGraphics/fillRect/drawRect/createPng flow from Tcsort.zip.
 */
public class Tcsort extends MainWindow {
  private static final int WIDTH = 576;
  private static final int HEIGHT = 576;
  private static final int BORDER = 10;
  private static final int BORDER_WIDTH = WIDTH - (BORDER * 2);
  private static final String PNG_NAME = "nome.png";
  private static final String RESULT_NAME = "issue-417-result.json";

  public Tcsort() {
    super("", Window.NO_BORDER);
    setDeviceTitle("issue-417-generated-image");
    setUIStyle(Settings.ANDROID_UI);
  }

  @Override
  public void initUI() {
    super.initUI();
    testGraphics();
  }

  private void testGraphics() {
    boolean pass = false;
    String failure = "";
    int background = Color.WHITE;
    int border = Color.BLACK;
    int interior = Color.WHITE;
    int rowR = -1;
    int rowG = -1;
    int rowB = -1;
    int rowA = -1;
    int pngSize = 0;
    long pngCrc32 = 0;

    try {
      MonoImage image = new MonoImage(WIDTH, HEIGHT);
      Graphics graphics = image.getGraphics();

      // These are the issue's original drawing operations and coordinates.
      graphics.backColor = Color.WHITE;
      graphics.fillRect(0, 0, WIDTH, HEIGHT);
      graphics.foreColor = Color.BLACK;
      graphics.drawRect(BORDER, BORDER, BORDER_WIDTH, BORDER_WIDTH);

      int[] selectedPixels = {
          graphics.getPixel(0, 0) & 0xFFFFFF,
          graphics.getPixel(BORDER, BORDER) & 0xFFFFFF,
          graphics.getPixel(BORDER + 1, BORDER + 1) & 0xFFFFFF,
          graphics.getPixel(WIDTH - 1, HEIGHT - 1) & 0xFFFFFF
      };
      assertPixel("background", selectedPixels[0], background);
      assertPixel("border", selectedPixels[1], border);
      assertPixel("interior", selectedPixels[2], interior);
      assertPixel("background-corner", selectedPixels[3], background);

      byte[] row = new byte[WIDTH * 4];
      image.getPixelRow(row, BORDER + 1);
      int interiorOffset = (BORDER + 1) * 4;
      rowR = row[interiorOffset] & 0xFF;
      rowG = row[interiorOffset + 1] & 0xFF;
      rowB = row[interiorOffset + 2] & 0xFF;
      rowA = row[interiorOffset + 3] & 0xFF;
      assertEquals("row red", 255, rowR);
      assertEquals("row green", 255, rowG);
      assertEquals("row blue", 255, rowB);
      assertEquals("row alpha", 255, rowA);

      ByteArrayStream encoded = new ByteArrayStream(WIDTH * HEIGHT + HEIGHT);
      image.createPng(encoded);
      pngSize = encoded.getPos();
      CRC32 crc = new CRC32();
      crc.update(encoded.getBuffer(), 0, pngSize);
      pngCrc32 = crc.getValue();
      if (pngSize <= 0 || encoded.getBuffer()[0] != (byte) 0x89
          || encoded.getBuffer()[1] != 'P' || encoded.getBuffer()[2] != 'N'
          || encoded.getBuffer()[3] != 'G') {
        throw new IllegalStateException("encoded PNG is missing its signature");
      }

      saveBytes(PNG_NAME, encoded.getBuffer(), pngSize);
      String pngPath = PNG_NAME;
      pass = true;
      writeResult(pass, failure, pngPath, selectedPixels, background, border, interior,
          rowR, rowG, rowB, rowA, pngSize, pngCrc32);
    } catch (Throwable failureCause) {
      failure = failureCause.toString();
      try {
        writeResult(false, failure, PNG_NAME,
            new int[] { background, border, interior, background }, background, border, interior,
            rowR, rowG, rowB, rowA, pngSize, pngCrc32);
      } catch (Throwable ignored) {
        failure = failure + "; result-write=" + ignored;
      }
    }

    if (!pass) {
      throw new RuntimeException("issue-417 failed: " + failure);
    }
  }

  private static void assertPixel(String name, int actual, int expected) {
    if (actual != expected) {
      throw new IllegalStateException(name + " expected 0x" + Integer.toHexString(expected)
          + " but was 0x" + Integer.toHexString(actual));
    }
  }

  private static void assertEquals(String name, int expected, int actual) {
    if (expected != actual) {
      throw new IllegalStateException(name + " expected " + expected + " but was " + actual);
    }
  }

  private static String saveBytes(String name, byte[] bytes, int length) throws IOException {
    String path = Settings.appPath + "/" + name;
    try (File file = new File(path, File.CREATE_EMPTY)) {
      file.writeBytes(bytes, 0, length);
    }
    return path;
  }

  private static void writeResult(boolean pass, String failure, String pngPath, int[] selectedPixels,
      int background, int border, int interior, int rowR, int rowG, int rowB, int rowA,
      int pngSize, long pngCrc32) throws IOException {
    String json = "{\n"
        + "  \"platform\": \"" + (Settings.platform == null ? "unknown" : Settings.platform) + "\",\n"
        + "  \"implementationPath\": \"" + (Settings.onJavaSE ? "java-byte-array" : "native-skia") + "\",\n"
        + "  \"dimensions\": {\"width\": " + WIDTH + ", \"height\": " + HEIGHT + "},\n"
        + "  \"expectedPixels\": {\"background\": " + background + ", \"border\": " + border
        + ", \"interior\": " + interior + "},\n"
        + "  \"observedPixels\": [" + selectedPixels[0] + ", " + selectedPixels[1] + ", "
        + selectedPixels[2] + ", " + selectedPixels[3] + "],\n"
        + "  \"rowInteriorRgba\": [" + rowR + ", " + rowG + ", " + rowB + ", " + rowA + "],\n"
        + "  \"outputPath\": \"" + pngPath + "\",\n"
        + "  \"encodedSize\": " + pngSize + ",\n"
        + "  \"encodedCrc32\": \"" + Long.toHexString(pngCrc32) + "\",\n"
        + "  \"assertionCount\": 8,\n"
        + "  \"pass\": " + pass + ",\n"
        + "  \"failure\": \"" + failure.replace('\\', '/').replace('"', '\'') + "\"\n"
        + "}\n";
    saveBytes(RESULT_NAME, json.getBytes(), json.length());
  }
}
