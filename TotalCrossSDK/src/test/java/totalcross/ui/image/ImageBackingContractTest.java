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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageBackingContractTest {
  @Test
  void encodedImagesStartDeferredWithoutMaterializedBacking() throws Exception {
    Image image = new Image(png(2, 1));

    assertNull(image.backing);
    assertNotNull(image.pipelineForSmoke());
  }

  @Test
  void rasterImagesExposeTheirLiveRasterBacking() throws Exception {
    Image image = new Image(2, 1);

    assertNotNull(image.backing);
    assertTrue(image.backing.isRaster());
    assertTrue(image.backing.isValid());
    assertSame(image.getPixels(), ((RasterImageBacking) image.backing).pixels());
  }

  @Test
  void snapshotsDetachResultProducingPipelineRoots() throws Exception {
    Image image = new Image(2, 1);
    image.getPixels()[0] = 0xFF102030;

    BackingImageSource snapshot = image.snapshotRasterSource();
    image.getPixels()[0] = 0xFFFFFFFF;
    Image materialized = snapshot.materialize();

    assertArrayEquals(new int[] { 0xFF102030, 0 }, materialized.getPixels());
    assertNotNull(snapshot.backing);
    assertTrue(snapshot.backing.isRaster());
    assertFalse(snapshot.backing == image.backing);
  }

  @Test
  void metadataRemainsIndependentFromBackingRepresentation() throws Exception {
    NativeImageBacking nativeBacking = NativeImageBacking.fromHandle(1, 3, 2);
    BackingImageSource source = new BackingImageSource(nativeBacking, 7, 5, 7, 5, 1.25,
        1, -1, 7, "comment", "path", 1, 0x123456, true, 91, 0.5, 0.75);

    assertTrue(source.backing.isNative());
    assertEquals(7, source.width);
    assertEquals(5, source.height);
    assertEquals(1.25, source.contentScale);
    assertEquals("comment", source.comment);
    assertEquals("path", source.path);
    nativeBacking.release();
  }

  @Test
  void nativeBackingReleaseIsIdempotentWithoutMonitorLocking() throws Exception {
    Method release = NativeImageBacking.class.getDeclaredMethod("release");
    assertFalse(Modifier.isSynchronized(release.getModifiers()));

    NativeImageBacking backing = NativeImageBacking.fromHandle(1, 1, 1);
    backing.release();
    backing.release();
    assertFalse(backing.isValid());
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "png", output));
    return output.toByteArray();
  }
}
