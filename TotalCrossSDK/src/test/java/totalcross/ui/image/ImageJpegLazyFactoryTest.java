// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.junit.jupiter.api.Test;

import totalcross.Launcher;

class ImageJpegLazyFactoryTest {
  @Test
  void bestFitMaterializationKeepsFullAndTieredDecodeResults() throws Exception {
    Path file = Files.createTempFile("totalcross-jpeg-best-fit", ".jpg");
    tc.simulator.Launcher previous = (tc.simulator.Launcher) Launcher.instance;
    try {
      byte[] encoded = jpeg(800, 600, false, false);
      Files.write(file, encoded);
      new tc.simulator.Launcher();
      int[][] cases = {
          {100, 75, 8},
          {200, 150, 4},
          {400, 300, 2},
          {800, 600, 1}
      };

      for (int[] testCase : cases) {
        int denominator = testCase[2];
        Image expected = Image.decodeJpegAtDenominatorForTest(encoded, denominator);
        Image.resetImageOperationAccountingForTest();
        Image actual = Image.getJpegBestFit(file.toString(), testCase[0], testCase[1]);
        assertEquals(800 / denominator, actual.getWidth());
        assertEquals(600 / denominator, actual.getHeight());
        assertEquals(denominator, actual.pipelineForSmoke().decodePolicy().decodeDenominator());

        assertArrayEquals(expected.getPixels(), actual.getPixels());
        assertEquals(denominator == 1 ? 0 : 1, Image.targetedDecodeInvocationCountForTest());
        assertEquals(denominator == 1 ? 1 : 0, Image.fullDecodeInvocationCountForTest());
        assertNull(actual.pipelineForSmoke());
      }
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  @Test
  void explicitRatiosKeepCeilingDimensionsAndUseConservativeNativeTiers() throws Exception {
    Path file = Files.createTempFile("totalcross-jpeg-explicit-ratio", ".jpg");
    tc.simulator.Launcher previous = (tc.simulator.Launcher) Launcher.instance;
    try {
      byte[] encoded = jpeg(800, 600, false, false);
      Files.write(file, encoded);
      new tc.simulator.Launcher();
      int[][] cases = {
          {1, 8, 100, 75, 8},
          {1, 5, 160, 120, 4},
          {2, 7, 229, 172, 2},
          {3, 4, 600, 450, 1},
          {3, 2, 1200, 900, 1}
      };

      for (int[] testCase : cases) {
        int width = testCase[2];
        int height = testCase[3];
        int denominator = testCase[4];
        Image expected = Image.decodeJpegAtDenominatorForTest(encoded, denominator)
            .getSmoothScaledInstance(width, height);
        Image.resetImageOperationAccountingForTest();
        Image actual = Image.getJpegScaled(file.toString(), testCase[0], testCase[1]);
        ImageDecodePolicy policy = actual.pipelineForSmoke().decodePolicy();
        assertEquals(width, actual.getWidth());
        assertEquals(height, actual.getHeight());
        assertEquals(testCase[0], policy.scaleNumerator());
        assertEquals(testCase[1], policy.scaleDenominator());
        assertEquals(denominator, policy.decodeDenominator());

        assertArrayEquals(expected.getPixels(), actual.getPixels());
        assertEquals(denominator == 1 ? 0 : 1, Image.targetedDecodeInvocationCountForTest());
        assertEquals(denominator == 1 ? 1 : 0, Image.fullDecodeInvocationCountForTest());
      }
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  @Test
  void progressiveColorAndGrayscaleJpegsMaterializeThroughFactories() throws Exception {
    Path file = Files.createTempFile("totalcross-jpeg-progressive", ".jpg");
    tc.simulator.Launcher previous = (tc.simulator.Launcher) Launcher.instance;
    try {
      new tc.simulator.Launcher();
      for (boolean grayscale : new boolean[] {false, true}) {
        byte[] encoded = jpeg(64, 32, grayscale, true);
        assertTrue(hasProgressiveFrameMarker(encoded));
        Files.write(file, encoded);
        EncodedImageSource captured = EncodedImageSource.fromBytes(encoded);
        assertEquals(ImageEncodedStructure.Format.JPEG, captured.getFormat());
        assertEquals(64, captured.getIntrinsicWidth());
        assertEquals(32, captured.getIntrinsicHeight());

        Image actual = Image.getJpegScaled(file.toString(), 1, 2);
        assertEquals(32, actual.getWidth());
        assertEquals(16, actual.getHeight());
        int[] pixels = actual.getPixels();
        assertEquals(32 * 16, pixels.length);
        assertTrue(Arrays.stream(pixels).allMatch(pixel -> (pixel >>> 24) == 0xFF));
      }
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  @Test
  void corruptJpegFailureIsCachedAndTransientDecodeFailureRetries() throws Exception {
    Path file = Files.createTempFile("totalcross-jpeg-failure", ".jpg");
    tc.simulator.Launcher previous = (tc.simulator.Launcher) Launcher.instance;
    try {
      new tc.simulator.Launcher();
      byte[] encoded = jpeg(128, 96, false, false);
      Files.write(file, corruptJpegEntropy(encoded));
      Image corrupt = Image.getJpegScaled(file.toString(), 1, 4);
      EncodedImageSource corruptSource = (EncodedImageSource) corrupt.pipelineForSmoke().root();
      Image.resetImageOperationAccountingForTest();

      ImageException first = assertThrows(ImageException.class, () -> corrupt.resolveForDrawing(1));
      ImageException second = assertThrows(ImageException.class, () -> corrupt.resolveForDrawing(1));
      assertSame(first, second);
      assertSame(first, corruptSource.decodeFailure());
      assertEquals(1, Image.targetedDecodeInvocationCountForTest());
      assertNotNull(corrupt.pipelineForSmoke());

      Files.write(file, encoded);
      Image retry = Image.getJpegScaled(file.toString(), 1, 4);
      EncodedImageSource retrySource = (EncodedImageSource) retry.pipelineForSmoke().root();
      Image.resetImageOperationAccountingForTest();
      Image.failNextTargetedDecodeInfrastructureForTest();

      ImageException transientFailure = assertThrows(ImageException.class, () -> retry.resolveForDrawing(1));
      assertTrue(transientFailure instanceof TransientImageMaterializationException);
      assertNull(retrySource.decodeFailure());
      assertNotNull(retry.pipelineForSmoke());
      assertEquals(1, Image.targetedDecodeInvocationCountForTest());

      Image resolved = retry.resolveForDrawing(1);
      assertEquals(32, resolved.getPixelWidth());
      assertNull(retrySource.decodeFailure());
      assertEquals(2, Image.targetedDecodeInvocationCountForTest());
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  @Test
  void factoryMaterializationReusesBackingAndKeepsLaterMutationIsolated() throws Exception {
    Path file = Files.createTempFile("totalcross-jpeg-reuse", ".jpg");
    tc.simulator.Launcher previous = (tc.simulator.Launcher) Launcher.instance;
    try {
      Files.write(file, jpeg(128, 96, false, false));
      new tc.simulator.Launcher();
      Image.resetImageOperationAccountingForTest();
      Image base = Image.getJpegScaled(file.toString(), 1, 4);
      EncodedImageSource source = (EncodedImageSource) base.pipelineForSmoke().root();
      Image first = base.resolveForDrawing(1);
      int[] firstPixels = first.getPixels().clone();
      assertNotNull(source.decodedBackingForReuse(4));

      Image sibling = base.getSmoothScaledInstance(16, 12);
      sibling.resolveForDrawing(1);
      assertEquals(1, Image.targetedDecodeInvocationCountForTest());

      sibling.applyColor2(0x804080C0);
      int[] changedPixels = sibling.getPixels();
      assertFalse(Arrays.equals(firstPixels, changedPixels));
      assertArrayEquals(firstPixels, first.getPixels());
      assertNotNull(source.decodedBackingForReuse(4));
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  @Test
  void factoryValidationAndPathErrorsKeepTheirPublicExceptionTypes() throws Exception {
    Path file = Files.createTempFile("totalcross-jpeg-invalid-source", ".bin");
    tc.simulator.Launcher previous = (tc.simulator.Launcher) Launcher.instance;
    try {
      new tc.simulator.Launcher();
      Files.delete(file);

      assertThrows(ImageException.class, () -> Image.getJpegBestFit(file.toString(), 0, 1));
      assertThrows(ImageException.class, () -> Image.getJpegScaled(file.toString(), 1, 0));
      assertThrows(IOException.class, () -> Image.getJpegBestFit(file.toString(), 1, 1));
      assertThrows(IOException.class, () -> Image.getJpegScaled(file.toString(), 1, 1));

      ByteArrayOutputStream pngBytes = new ByteArrayOutputStream();
      assertTrue(ImageIO.write(new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB), "png", pngBytes));
      Files.write(file, pngBytes.toByteArray());
      assertThrows(ImageException.class, () -> Image.getJpegBestFit(file.toString(), 1, 1));
      assertThrows(ImageException.class, () -> Image.getJpegScaled(file.toString(), 1, 2));
    } finally {
      Files.deleteIfExists(file);
      Launcher.instance = previous;
    }
  }

  private static byte[] jpeg(int width, int height, boolean grayscale, boolean progressive) throws Exception {
    int type = grayscale ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB;
    BufferedImage image = new BufferedImage(width, height, type);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int value = (x * 29 + y * 17) & 0xFF;
        image.setRGB(x, y, grayscale ? (value << 16) | (value << 8) | value
            : ((value << 16) | (((value * 3) & 0xFF) << 8) | ((value * 7) & 0xFF)));
      }
    }

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
    assertTrue(writers.hasNext());
    ImageWriter writer = writers.next();
    try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
      writer.setOutput(output);
      ImageWriteParam parameters = writer.getDefaultWriteParam();
      if (progressive && parameters.canWriteProgressive()) {
        parameters.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
      }
      writer.write(null, new IIOImage(image, null, null), parameters);
    } finally {
      writer.dispose();
    }
    return bytes.toByteArray();
  }

  private static byte[] corruptJpegEntropy(byte[] source) {
    int sos = -1;
    for (int i = 0; i + 1 < source.length; i++) {
      if ((source[i] & 0xFF) == 0xFF && (source[i + 1] & 0xFF) == 0xDA) {
        sos = i;
        break;
      }
    }
    assertTrue(sos >= 0);
    int segmentLength = ((source[sos + 2] & 0xFF) << 8) | (source[sos + 3] & 0xFF);
    int entropy = sos + 2 + segmentLength;
    byte[] invalidEntropyTail = new byte[] {
        (byte) 0xFF, (byte) 0xC3, 0, 8, 8, 0, 1, 0, 1, 1, (byte) 0xFF, (byte) 0xD9
    };
    byte[] result = new byte[entropy + invalidEntropyTail.length];
    System.arraycopy(source, 0, result, 0, entropy);
    System.arraycopy(invalidEntropyTail, 0, result, entropy, invalidEntropyTail.length);
    return result;
  }

  private static boolean hasProgressiveFrameMarker(byte[] bytes) {
    for (int i = 0; i + 1 < bytes.length; i++) {
      if ((bytes[i] & 0xFF) == 0xFF && (bytes[i + 1] & 0xFF) == 0xC2) {
        return true;
      }
    }
    return false;
  }
}
