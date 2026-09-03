// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageDeferredFrameExtractionTest {
  private static final int COLOR = 0xFF204060;

  @Test
  void encodedFrameSelectionStaysLazyAndUsesKnownSingleFrameMetadata() throws Exception {
    Image source = new Image(twoFramePng());

    Image result = source.getFrameInstance(1);

    assertNull(source.backing);
    assertNull(result.backing);
    assertEquals(1, result.getFrameCount());
    assertEquals(4, result.getWidth());
    assertEquals(4, result.getPixelWidth());
    assertEquals(-1, result.getCurrentFrame());
    assertEquals(Arrays.asList(ImagePipeline.FRAME_SELECT), operationTypes(pipeline(result)));
    assertNotNull(pipeline(result));
  }

  @Test
  void frameNormalizationMatchesSetCurrentFrameAndSingleFrameIgnoresArgument() throws Exception {
    byte[] encoded = twoFramePng();
    Image expectedLast = new Image(encoded);
    expectedLast.getPixels();
    expectedLast.setCurrentFrame(-1);
    Image expectedFirst = new Image(encoded);
    expectedFirst.getPixels();
    expectedFirst.setCurrentFrame(0);

    assertArrayEquals(expectedLast.getPixels(), new Image(encoded).getFrameInstance(-1).getPixels());
    assertArrayEquals(expectedFirst.getPixels(), new Image(encoded).getFrameInstance(2).getPixels());

    Image single = new Image(png(5, 3));
    assertArrayEquals(single.getPixels(), single.getFrameInstance(-1).getPixels());
    assertArrayEquals(single.getPixels(), single.getFrameInstance(99).getPixels());
  }

  @Test
  void selectedFrameIsIndependentOfLaterSourceStateChanges() throws Exception {
    byte[] encoded = twoFramePng();
    Image source = new Image(encoded);
    Image selected = source.getFrameInstance(1);
    int[] expected = new Image(encoded).getFrameInstance(1).getPixels().clone();

    source.setCurrentFrame(0);
    assertArrayEquals(expected, selected.getPixels());
    assertEquals(0, source.getCurrentFrame());
  }

  @Test
  void materializedSourceIsDeepSnapshottedAndFrameResultKeepsLegacyMetadata() throws Exception {
    Image source = materializedMultiFrame();
    setField(source, "path", "frame-fixture.png");
    source.hwScaleW = 0.75;
    source.hwScaleH = 0.5;
    source.alphaMask = 91;
    source.transparentColor = 0x123456;
    source.useAlpha = true;
    int[] expected = source.getFrameInstance(1).getPixels().clone();
    Image result = source.getFrameInstance(1);

    source.setCurrentFrame(0);
    source.getPixels()[0] = 0xFFFFFFFF;

    assertArrayEquals(expected, result.getPixels());
    assertEquals("frame-fixture.png", result.getPath());
    assertEquals(0.75, result.hwScaleW);
    assertEquals(0.5, result.hwScaleH);
    assertEquals(255, result.alphaMask);
    assertEquals(ColorDefaults.WHITE, result.transparentColor);
    assertFalse(result.useAlpha);
  }

  @Test
  void getCopySelectsFrameZeroAndRemainsDeferred() throws Exception {
    Image source = new Image(twoFramePng());
    Image copy = source.getCopy();

    assertNull(copy.backing);
    assertEquals(1, copy.getFrameCount());
    assertEquals(Arrays.asList(ImagePipeline.FRAME_SELECT), operationTypes(pipeline(copy)));
    Image expected = new Image(twoFramePng());
    expected.getPixels();
    assertArrayEquals(expected.getPixels(), copy.getPixels());
  }

  @Test
  void frameSelectionComposesInCallOrderWithColorAndScaleNodes() throws Exception {
    byte[] encoded = twoFramePng();

    Image colorThenFrame = new Image(encoded);
    colorThenFrame.applyColor(COLOR);
    Image colorThenFrameResult = colorThenFrame.getFrameInstance(1);
    assertEquals(Arrays.asList(ImagePipeline.APPLY_COLOR, ImagePipeline.FRAME_SELECT),
        operationTypes(pipeline(colorThenFrameResult)));

    Image frameThenColor = new Image(encoded).getFrameInstance(1);
    frameThenColor.applyColor(COLOR);
    assertEquals(Arrays.asList(ImagePipeline.FRAME_SELECT, ImagePipeline.APPLY_COLOR),
        operationTypes(pipeline(frameThenColor)));

    Image expectedColorThenFrame = new Image(encoded);
    expectedColorThenFrame.getPixels();
    expectedColorThenFrame.applyColor(COLOR);
    expectedColorThenFrame.setCurrentFrame(1);
    assertArrayEquals(expectedColorThenFrame.getPixels(), colorThenFrameResult.getPixels());

    Image expectedFrameThenColor = new Image(encoded).getFrameInstance(1);
    expectedFrameThenColor.getPixels();
    expectedFrameThenColor.applyColor(COLOR);
    assertArrayEquals(expectedFrameThenColor.getPixels(), frameThenColor.getPixels());

    Image scaled = new Image(encoded).getSmoothScaledInstance(2, 2).getFrameInstance(1);
    assertEquals(Arrays.asList(ImagePipeline.SMOOTH_SCALE, ImagePipeline.FRAME_SELECT),
        operationTypes(pipeline(scaled)));
    assertEquals(2, scaled.getWidth());
    assertTrue(scaled.getPixels().length > 0);
  }

  private static Image materializedMultiFrame() throws Exception {
    Image image = new Image(8, 2);
    for (int i = 0; i < image.getPixels().length; i++) {
      image.getPixels()[i] = i < 8 ? 0xFF102030 : 0xFF405060;
    }
    image.setFrameCount(2);
    image.setCurrentFrame(1);
    return image;
  }

  private static List<Integer> operationTypes(ImagePipeline leaf) {
    List<Integer> result = new ArrayList<Integer>();
    for (ImagePipeline node = leaf; node != null && node.previous() != null; node = node.previous()) {
      result.add(0, node.operationType());
    }
    return result;
  }

  private static ImagePipeline pipeline(Image image) throws Exception {
    Field field = Image.class.getDeclaredField("pipeline");
    field.setAccessible(true);
    return (ImagePipeline) field.get(image);
  }

  private static void setField(Object object, String name, Object value) throws Exception {
    Field field = object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(object, value);
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        source.setRGB(x, y, 0xFF000000 | ((x * 29) & 0xFF) << 16 | ((y * 41) & 0xFF) << 8 | (x + y));
      }
    }
    return writePng(source, null);
  }

  private static byte[] twoFramePng() throws Exception {
    BufferedImage source = new BufferedImage(8, 3, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        int color = x < 4 ? 0xFF000000 | (x * 20) << 16 | (y * 30) << 8
            : 0xFF004000 | ((x - 4) * 25) | (y * 15);
        source.setRGB(x, y, color);
      }
    }
    return writePng(source, "FC=2");
  }

  private static byte[] writePng(BufferedImage source, String comment) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "png", output));
    byte[] bytes = output.toByteArray();
    if (comment == null) {
      return bytes;
    }
    int iend = bytes.length - 12;
    ByteArrayOutputStream withComment = new ByteArrayOutputStream(bytes.length + comment.length() + 32);
    withComment.write(bytes, 0, iend);
    byte[] text = ("Comment\0" + comment).getBytes("ISO-8859-1");
    writeInt(withComment, text.length);
    byte[] type = "tEXt".getBytes("ISO-8859-1");
    withComment.write(type);
    withComment.write(text);
    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
    crc.update(type);
    crc.update(text);
    writeInt(withComment, (int) crc.getValue());
    withComment.write(bytes, iend, 12);
    return withComment.toByteArray();
  }

  private static void writeInt(ByteArrayOutputStream output, int value) {
    output.write(value >> 24);
    output.write(value >> 16);
    output.write(value >> 8);
    output.write(value);
  }

  private static final class ColorDefaults {
    static final int WHITE = 0xFFFFFF;
  }
}
