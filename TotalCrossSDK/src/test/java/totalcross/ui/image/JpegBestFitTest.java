// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    for (int y = 0; y < SOURCE_HEIGHT; y++) {
      for (int x = 0; x < SOURCE_WIDTH; x++) {
        int red = (x * 180 / SOURCE_WIDTH + y * 40 / SOURCE_HEIGHT) & 0xFF;
        int green = (x * 60 / SOURCE_WIDTH + y * 180 / SOURCE_HEIGHT) & 0xFF;
        int blue = (x * 30 / SOURCE_WIDTH + y * 90 / SOURCE_HEIGHT) & 0xFF;
        source.setRGB(x, y, (red << 16) | (green << 8) | blue);
      }
    }
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
    assertScaled(1, 1, 1601, 901);
  }

  @Test
  void factoriesKeepDistinctPoliciesAndDecodeOnlyAtARealBarrier() throws Exception {
    Image.resetImageOperationAccountingForTest();
    Image bestFit = Image.getJpegBestFit(jpegPath.toString(), 201, 113);
    ImagePipeline bestFitPipeline = bestFit.pipelineForSmoke();
    assertEquals(ImageDecodePolicy.BEST_FIT, bestFitPipeline.decodePolicy().kind());
    assertEquals(201, bestFitPipeline.decodePolicy().parameter1());
    assertEquals(113, bestFitPipeline.decodePolicy().parameter2());
    assertEquals(0, Image.fullDecodeInvocationCountForTest());
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());

    Image scaled = Image.getJpegScaled(jpegPath.toString(), 3, 4);
    ImagePipeline scaledPipeline = scaled.pipelineForSmoke();
    assertEquals(ImageDecodePolicy.EXPLICIT_RATIO, scaledPipeline.decodePolicy().kind());
    assertEquals(3, scaledPipeline.decodePolicy().parameter1());
    assertEquals(4, scaledPipeline.decodePolicy().parameter2());
    assertEquals(1201, scaled.getPixelWidth());
    assertEquals(676, scaled.getPixelHeight());
    assertEquals(0, Image.fullDecodeInvocationCountForTest());
    assertEquals(0, Image.targetedDecodeInvocationCountForTest());

    Image chained = bestFit.getSmoothScaledInstance(100, 100).getAlphaInstance(-20);
    assertEquals(ImageDecodePolicy.BEST_FIT, chained.pipelineForSmoke().decodePolicy().kind());
    assertEquals(0, Image.materializationCountForTest());
    assertEquals(10000, chained.getPixels().length);
    assertEquals(1, Image.materializationCountForTest());
  }

  @Test
  void decodesBestFitBoundariesAgainstIndependentFullDecodeReference() throws Exception {
    int[][] cases = {
        { 201, 113, 201, 113 },
        { 401, 226, 401, 226 },
        { 801, 451, 801, 451 },
        { 1601, 901, 1601, 901 }
    };
    for (int[] testCase : cases) {
      Image actual = Image.getJpegBestFit(jpegPath.toString(), testCase[0], testCase[1]);
      int[] pixels = actual.getPixels();
      assertEquals(testCase[2], actual.getPixelWidth());
      assertEquals(testCase[3], actual.getPixelHeight());
      assertJpegQuality("best-fit " + testCase[0] + "x" + testCase[1], pixels,
          independentReference(testCase[2], testCase[3]));
    }
  }

  @Test
  void decodesExplicitRatiosAgainstIndependentFullDecodeReference() throws Exception {
    int[][] cases = {
        { 1, 8, 201, 113 },
        { 1, 2, 801, 451 },
        { 3, 4, 1201, 676 },
        { 1, 1, 1601, 901 }
    };
    for (int[] testCase : cases) {
      Image actual = Image.getJpegScaled(jpegPath.toString(), testCase[0], testCase[1]);
      int[] pixels = actual.getPixels();
      assertEquals(testCase[2], actual.getPixelWidth());
      assertEquals(testCase[3], actual.getPixelHeight());
      assertJpegQuality("explicit " + testCase[0] + "/" + testCase[1], pixels,
          independentReference(testCase[2], testCase[3]));
    }
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

  private static int[] independentReference(int width, int height) throws Exception {
    BufferedImage source = ImageIO.read(jpegPath.toFile());
    BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = scaled.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.drawImage(source, 0, 0, width, height, null);
    graphics.dispose();
    int[] pixels = new int[width * height];
    scaled.getRGB(0, 0, width, height, pixels, 0, width);
    return pixels;
  }

  private static void assertJpegQuality(String label, int[] actual, int[] expected) {
    assertEquals(expected.length, actual.length);
    long squared = 0;
    int max = 0;
    int maxIndex = -1;
    long samples = 0;
    for (int i = 0; i < actual.length; i++) {
      int actualPixel = actual[i];
      int expectedPixel = expected[i];
      int error = ((actualPixel >>> 16) & 0xFF) - ((expectedPixel >>> 16) & 0xFF);
      if (Math.abs(error) > max) {
        max = Math.abs(error);
        maxIndex = i;
      }
      squared += (long) error * error;
      error = ((actualPixel >>> 8) & 0xFF) - ((expectedPixel >>> 8) & 0xFF);
      if (Math.abs(error) > max) {
        max = Math.abs(error);
        maxIndex = i;
      }
      squared += (long) error * error;
      error = (actualPixel & 0xFF) - (expectedPixel & 0xFF);
      if (Math.abs(error) > max) {
        max = Math.abs(error);
        maxIndex = i;
      }
      squared += (long) error * error;
      samples += 3;
    }
    double rmse = Math.sqrt((double) squared / samples);
    assertTrue(max <= 128 && rmse <= 24.0,
        label + " JPEG raster mismatch max=" + max + ", index=" + maxIndex + ", rmse=" + rmse);
  }
}
