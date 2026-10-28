// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.deployer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class Win32IconEncoderTest {
  private static final int OFFSET = 19;
  private static final int ICON_BYTES = 40 + 1024 + 1024 + 128;
  private static final int PALETTE = OFFSET + 40;
  private static final int PIXELS = PALETTE + 1024;
  private static final int MASK = PIXELS + 1024;

  @Test
  void writesOnlyReservedBytesAndLeavesThePeLayoutUntouched() {
    byte[] launcher = launcherTemplate();
    byte[] before = launcher.clone();
    int[] image = new int[32 * 32];
    Arrays.fill(image, 0xFF1986E4);

    Win32IconEncoder.write(launcher, OFFSET, image, 32, 32);

    assertEquals(before.length, launcher.length);
    assertArrayEquals(Arrays.copyOfRange(before, 0, PALETTE),
        Arrays.copyOfRange(launcher, 0, PALETTE));
    assertArrayEquals(Arrays.copyOfRange(before, OFFSET + ICON_BYTES, before.length),
        Arrays.copyOfRange(launcher, OFFSET + ICON_BYTES, launcher.length));
    assertEquals(0x1986E4, paletteColor(launcher, launcher[PIXELS] & 255));
  }

  @Test
  void retainsExactVisibleColorsAndWritesPixelsBottomUp() {
    int[] image = new int[1024];
    Arrays.fill(image, 0xFF204060);
    image[0] = 0xFFB32953;     // top-left
    image[31 * 32] = 0xFF15D4AA; // bottom-left
    byte[] launcher = launcherTemplate();

    Win32IconEncoder.write(launcher, OFFSET, image, 32, 32);

    assertEquals(0xB32953, paletteColor(launcher, launcher[PIXELS + 31 * 32] & 255));
    assertEquals(0x15D4AA, paletteColor(launcher, launcher[PIXELS] & 255));
    for (int i = 0; i < 128; i++) {
      assertEquals(0, launcher[MASK + i] & 255);
    }
  }

  @Test
  void usesAlphaForTheAndMaskAndKeepsOpaqueWhiteVisible() {
    int[] image = new int[1024];
    Arrays.fill(image, 0xFFFFFFFF);
    image[0] = 0x0000FF00; // alpha 0, hidden RGB must not affect the palette
    image[1] = 0x4000FF00; // alpha below the binary cutoff
    image[2] = 0xFF123456;
    byte[] launcher = launcherTemplate();

    Win32IconEncoder.write(launcher, OFFSET, image, 32, 32);

    assertEquals(0xC0, launcher[MASK + 31 * 4] & 255);
    assertEquals(0, launcher[MASK + 31 * 4 + 1] & 255);
    assertEquals(0xFFFFFF, paletteColor(launcher, launcher[PIXELS + 31 * 32 + 3] & 255));
  }

  @Test
  void preservesWhiteKeyForOpaqueLegacySourceImages() {
    int[] image = new int[1024];
    Arrays.fill(image, 0xFF202020);
    image[0] = 0xFFFFFFFF;
    byte[] launcher = launcherTemplate();

    Win32IconEncoder.write(launcher, OFFSET, image, 32, 32);

    assertEquals(0x80, launcher[MASK + 31 * 4] & 255);
    assertEquals(0, launcher[MASK] & 255);
  }

  @Test
  void premultipliedResizeDoesNotLeakInvisibleRgbIntoVisibleEdges() {
    int[] image = new int[64 * 64];
    Arrays.fill(image, 0x00FF0000);
    for (int y = 10; y < 54; y++) {
      for (int x = 10; x < 54; x++) {
        image[y * 64 + x] = 0xFF0000FF;
      }
    }
    byte[] launcher = launcherTemplate();

    Win32IconEncoder.write(launcher, OFFSET, image, 64, 64);

    for (int y = 0; y < 32; y++) {
      for (int x = 0; x < 32; x++) {
        int masked = (launcher[MASK + (31 - y) * 4 + x / 8] >>> (7 - x % 8)) & 1;
        if (masked == 0) {
          int rgb = paletteColor(launcher, launcher[PIXELS + (31 - y) * 32 + x] & 255);
          assertTrue((rgb & 255) >= (rgb >>> 16), "transparent red polluted a blue edge");
        }
      }
    }
  }

  @Test
  void rejectsUnexpectedLauncherLayoutBeforeWriting() {
    byte[] launcher = launcherTemplate();
    byte[] previous = launcher.clone();
    launcher[OFFSET + 14] = 32; // wrong bits per pixel
    byte[] bad = launcher.clone();

    assertThrows(IllegalArgumentException.class,
        () -> Win32IconEncoder.write(launcher, OFFSET, new int[1024], 32, 32));
    assertArrayEquals(bad, launcher);
    assertFalse(Arrays.equals(previous, launcher));

    assertThrows(IllegalArgumentException.class,
        () -> Win32IconEncoder.write(new byte[100], 0, new int[1024], 32, 32));
  }

  private static byte[] launcherTemplate() {
    byte[] exe = new byte[OFFSET + ICON_BYTES + 23];
    Arrays.fill(exe, (byte) 0x5A);
    int p = OFFSET;
    putInt(exe, p, 40);
    putInt(exe, p + 4, 32);
    putInt(exe, p + 8, 64);
    exe[p + 12] = 1;
    exe[p + 13] = 0;
    exe[p + 14] = 8;
    exe[p + 15] = 0;
    putInt(exe, p + 16, 0);
    return exe;
  }

  private static void putInt(byte[] exe, int offset, int value) {
    for (int i = 0; i < 4; i++) {
      exe[offset + i] = (byte) (value >>> (i * 8));
    }
  }

  private static int paletteColor(byte[] exe, int index) {
    int p = PALETTE + index * 4;
    return ((exe[p + 2] & 255) << 16) | ((exe[p + 1] & 255) << 8)
        | (exe[p] & 255);
  }
}
