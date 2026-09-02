// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import totalcross.Launcher;

class JpegBestFitTest {
  private static final int SOURCE_WIDTH = 1601;
  private static final int SOURCE_HEIGHT = 901;

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
    assertBestFit(201, 113, 201, 113); // 1/8, ceil(1601/8) x ceil(901/8)
    assertBestFit(401, 226, 401, 226); // 1/4
    assertBestFit(801, 451, 801, 451); // 1/2
    assertBestFit(1601, 901, 1601, 901); // 1/1
  }

  @Test
  void selectsNextScaleImmediatelyAboveEachBoundary() throws Exception {
    assertBestFit(202, 114, 401, 226); // above 1/8 -> 1/4
    assertBestFit(402, 227, 801, 451); // above 1/4 -> 1/2
    assertBestFit(802, 452, 1601, 901); // above 1/2 -> 1/1
  }

  @Test
  void preservesAspectRatioForDifferentTargetAspectRatios() throws Exception {
    assertBestFit(200, 200, 201, 113); // width-limited 1/8
    assertBestFit(402, 200, 401, 226); // height-limited 1/4
  }

  @Test
  void scalesJpegWithLibjpegCeiling() throws Exception {
    assertScaled(1, 8, 201, 113);
    assertScaled(1, 2, 801, 451);
    assertScaled(3, 4, 1201, 676);
  }

  @Test
  void rejectsNonPositiveBestFitTargets() {
    assertThrows(ImageException.class, () -> Image.getJpegBestFit(jpegPath.toString(), 0, 113));
    assertThrows(ImageException.class, () -> Image.getJpegBestFit(jpegPath.toString(), 201, 0));
    assertThrows(ImageException.class, () -> Image.getJpegBestFit(jpegPath.toString(), -1, 113));
    assertThrows(ImageException.class, () -> Image.getJpegBestFit(jpegPath.toString(), 201, -1));
  }

  @Test
  void rejectsNonPositiveJpegScaleArguments() {
    assertThrows(ImageException.class, () -> Image.getJpegScaled(jpegPath.toString(), 0, 1));
    assertThrows(ImageException.class, () -> Image.getJpegScaled(jpegPath.toString(), 1, 0));
    assertThrows(ImageException.class, () -> Image.getJpegScaled(jpegPath.toString(), -1, 1));
    assertThrows(ImageException.class, () -> Image.getJpegScaled(jpegPath.toString(), 1, -1));
    assertThrows(ImageException.class, () -> Image.getJpegScaled(jpegPath.toString(), Integer.MIN_VALUE, 1));
  }

  @Test
  void rejectsJpegScaleWhenDimensionExceedsIntegerRange() {
    assertThrows(ImageException.class,
        () -> Image.getJpegScaled(jpegPath.toString(), Integer.MAX_VALUE, 1));
  }

  @Test
  void rejectsMissingJpegWithJavaIoException() {
    String missingPath = tempDir.resolve("missing.jpg").toString();
    assertThrows(java.io.IOException.class, () -> Image.getJpegBestFit(missingPath, 1, 1));
    assertThrows(java.io.IOException.class, () -> Image.getJpegScaled(missingPath, 1, 2));
  }

  @Test
  void rejectsExistingNonJpegWithImageException() throws Exception {
    Path pngPath = tempDir.resolve("not-a-jpeg.png");
    BufferedImage source = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
    assertTrue(ImageIO.write(source, "png", pngPath.toFile()));

    assertThrows(ImageException.class, () -> Image.getJpegBestFit(pngPath.toString(), 1, 1));
    assertThrows(ImageException.class, () -> Image.getJpegScaled(pngPath.toString(), 1, 2));
  }

  private static void assertBestFit(int targetWidth, int targetHeight, int expectedWidth, int expectedHeight)
      throws Exception {
    Image image = Image.getJpegBestFit(jpegPath.toString(), targetWidth, targetHeight);
    assertEquals(expectedWidth, image.getWidth());
    assertEquals(expectedHeight, image.getHeight());
    assertEquals(expectedWidth, image.getPixelWidth());
    assertEquals(expectedHeight, image.getPixelHeight());
  }

  private static void assertScaled(int numerator, int denominator, int expectedWidth, int expectedHeight)
      throws Exception {
    Image image = Image.getJpegScaled(jpegPath.toString(), numerator, denominator);
    assertEquals(expectedWidth, image.getWidth());
    assertEquals(expectedHeight, image.getHeight());
    assertEquals(expectedWidth, image.getPixelWidth());
    assertEquals(expectedHeight, image.getPixelHeight());
  }
}
