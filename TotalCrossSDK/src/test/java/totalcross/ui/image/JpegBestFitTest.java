// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import totalcross.Launcher;

class JpegBestFitTest {
  private static final int SOURCE_WIDTH = 1600;
  private static final int SOURCE_HEIGHT = 800;

  @TempDir
  static Path tempDir;

  private static Path jpegPath;

  @BeforeAll
  static void createJpegFixture() throws Exception {
    new Launcher();
    jpegPath = tempDir.resolve("best-fit.jpg");
    BufferedImage source = new BufferedImage(SOURCE_WIDTH, SOURCE_HEIGHT, BufferedImage.TYPE_INT_RGB);
    assertTrue(ImageIO.write(source, "jpg", jpegPath.toFile()));
  }

  @Test
  void selectsExpectedScaleAtInclusiveBoundaries() throws Exception {
    assertBestFit(200, 100, 200, 100); // 1/8
    assertBestFit(400, 200, 400, 200); // 1/4
    assertBestFit(800, 400, 800, 400); // 1/2
    assertBestFit(1600, 800, 1600, 800); // 1/1
  }

  @Test
  void selectsNextScaleImmediatelyAboveEachBoundary() throws Exception {
    assertBestFit(201, 101, 400, 200); // above 1/8 -> 1/4
    assertBestFit(401, 201, 800, 400); // above 1/4 -> 1/2
    assertBestFit(801, 401, 1600, 800); // above 1/2 -> 1/1
  }

  @Test
  void scalesJpegByDoubleRatio() throws Exception {
    Image image = Image.getJpegScaled(jpegPath.toString(), 1, 2);
    assertEquals(SOURCE_WIDTH / 2, image.getWidth());
    assertEquals(SOURCE_HEIGHT / 2, image.getHeight());
    assertEquals(SOURCE_WIDTH / 2, image.getPixelWidth());
    assertEquals(SOURCE_HEIGHT / 2, image.getPixelHeight());
  }

  private static void assertBestFit(int targetWidth, int targetHeight, int expectedWidth, int expectedHeight)
      throws Exception {
    Image image = Image.getJpegBestFit(jpegPath.toString(), targetWidth, targetHeight);
    assertEquals(expectedWidth, image.getWidth());
    assertEquals(expectedHeight, image.getHeight());
    assertEquals(expectedWidth, image.getPixelWidth());
    assertEquals(expectedHeight, image.getPixelHeight());
  }
}
