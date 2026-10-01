// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.sys.Settings;

class ImageBackingContractTest {
  @Test
  void encodedImagesStartDeferredWithoutMaterializedBacking() throws Exception {
    Image image = new Image(png(2, 1));

    assertNull(image.backing);
    assertNotNull(image.pipelineForSmoke());
  }

  @Test
  void rasterImagesExposeTheirLiveRasterBacking() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = true;
      Image image = new Image(2, 1);

      assertNotNull(image.backing);
      assertTrue(image.backing.isRaster());
      assertTrue(image.backing.isValid());
      assertSame(image.getPixels(), ((RasterImageBacking) image.backing).pixels());
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  @Test
  void exposingLiveRasterPixelsInvalidatesCachingStabilityOnce() throws Exception {
    Image image = new Image(2, 1);
    RasterImageBacking backing = (RasterImageBacking) image.backing;
    backing.setOpacityState(ImageBacking.OPACITY_OPAQUE);
    long beforeExposure = image.backingMutationGenerationForP2();

    assertTrue(image.backingIdentityStableForCaching());
    int[] pixels = image.getPixels();

    assertSame(backing.pixels(), pixels);
    assertTrue(backing.isMutableStorageEscaped());
    assertFalse(image.backingIdentityStableForCaching());
    assertEquals(beforeExposure + 1, image.backingMutationGenerationForP2());
    assertEquals(ImageBacking.OPACITY_UNKNOWN, image.opacityStateForP2());

    assertSame(pixels, image.getPixels());
    assertEquals(beforeExposure + 1, image.backingMutationGenerationForP2());
    pixels[0] = 0xFF123456;
    assertEquals(0xFF123456, backing.pixels()[0]);
    assertEquals(beforeExposure + 1, image.backingMutationGenerationForP2());
    assertFalse(image.backingIdentityStableForCaching());
  }

  @Test
  void snapshotAndReplacementRestoreCachingStabilityForNewStorage() throws Exception {
    Image image = new Image(2, 1);
    RasterImageBacking escaped = (RasterImageBacking) image.backing;
    image.getPixels();

    RasterImageBacking snapshot = (RasterImageBacking) escaped.snapshot();
    assertNotSame(escaped.pixels(), snapshot.pixels());
    assertFalse(snapshot.isMutableStorageEscaped());
    assertTrue(snapshot.backingIdentityStableForCaching());

    Method replace = Image.class.getDeclaredMethod("replaceBacking", ImageBacking.class);
    replace.setAccessible(true);
    replace.invoke(image, snapshot);

    assertSame(snapshot, image.backing);
    assertFalse(snapshot.isMutableStorageEscaped());
    assertTrue(image.backingIdentityStableForCaching());
  }

  @Test
  void encodedCacheBackingIsDetachedBeforeLivePixelsEscape() throws Exception {
    EncodedImageSource source = EncodedImageSource.fromBytes(png(2, 1));
    RasterImageBacking shared = new RasterImageBacking(2, 1, 1, 2,
        new int[] { 0xFF010203, 0xFF040506 }, null);
    source.installDecodedBacking(shared, 2, 1, 1);
    long decodedGeneration = source.decodedGeneration();
    Method materializeCached = Image.class.getDeclaredMethod(
        "materializeCachedEncodedSource", EncodedImageSource.class, ImageBacking.class);
    materializeCached.setAccessible(true);
    Image owner = new Image(1, 1);
    Image image = (Image) materializeCached.invoke(owner, source, shared);

    assertSame(shared, image.backing);
    int[] pixels = image.getPixels();

    assertNotSame(shared, image.backing);
    assertFalse(shared.isMutableStorageEscaped());
    assertTrue(((RasterImageBacking) image.backing).isMutableStorageEscaped());
    pixels[0] = 0xFFABCDEF;
    int[] cachedPixel = new int[1];
    assertTrue(shared.readPixels(cachedPixel, 0, 0, 0, 1, 1));
    assertEquals(0xFF010203, cachedPixel[0]);
    assertEquals(decodedGeneration, source.decodedGeneration());
  }

  @Test
  void sharedEncodedNativeReadbackRemainsPureAndDetached() throws Exception {
    EncodedImageSource source = EncodedImageSource.fromBytes(png(1, 1));
    DetachedNativeReadback backing = new DetachedNativeReadback(1, 1, new int[] { 0xFF123456 });
    backing.setOpacityState(ImageBacking.OPACITY_OPAQUE);
    source.installDecodedBacking(backing, 1, 1, 1);
    Method materializeCached = Image.class.getDeclaredMethod(
        "materializeCachedEncodedSource", EncodedImageSource.class, ImageBacking.class);
    materializeCached.setAccessible(true);
    Image image = (Image) materializeCached.invoke(new Image(1, 1), source, backing);
    long decodedGeneration = source.decodedGeneration();
    long generation = image.backingMutationGenerationForP2();

    int[] pixels = image.getPixels();

    assertSame(backing, image.backing);
    assertSame(backing, source.decodedBackingForReuse(1));
    assertNotSame(backing.storage, pixels);
    assertTrue(backing.backingIdentityStableForCaching());
    assertFalse(backing.isMutableStorageEscaped());
    assertTrue(image.backingIdentityStableForCaching());
    assertEquals(generation, image.backingMutationGenerationForP2());
    assertEquals(ImageBacking.OPACITY_OPAQUE, image.opacityStateForP2());

    pixels[0] = 0xFFABCDEF;
    assertArrayEquals(new int[] { 0xFF123456 }, backing.storage);
    assertEquals(decodedGeneration, source.decodedGeneration());
  }

  @Test
  void rasterBackingReadsCheckedRectanglesWithoutChangingGeneration() {
    RasterImageBacking backing = new RasterImageBacking(4, 3, 1, 4,
        new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }, null);
    int[] output = new int[] { -1, -1, -1, -1, -1, -1 };

    assertTrue(backing.readPixels(output, 1, 1, 1, 2, 2));
    assertArrayEquals(new int[] { -1, 6, 7, 10, 11, -1 }, output);
    assertFalse(backing.readPixels(output, 1, -1, 1, 2, 2));
    assertFalse(backing.readPixels(output, 1, 3, 1, 2, 2));
    assertFalse(backing.readPixels(output, 5, 0, 0, 2, 2));
    assertEquals(0, backing.mutationGeneration());
  }

  @Test
  void invalidBackingReplacementLeavesPreviousBackingUsable() throws Exception {
    Image image = new Image(2, 1);
    ImageBacking previous = image.backing;
    long generation = image.backingMutationGenerationForP2();
    Method replace = Image.class.getDeclaredMethod("replaceBacking", ImageBacking.class);
    replace.setAccessible(true);

    InvocationTargetException failure = assertThrows(InvocationTargetException.class,
        () -> replace.invoke(image, new RasterImageBacking(2, 1, 1, 2, null, null)));

    assertTrue(failure.getCause() instanceof IllegalArgumentException);
    assertSame(previous, image.backing);
    assertTrue(image.backing.isValid());
    assertEquals(generation, image.backingMutationGenerationForP2());
  }

  @Test
  void visibleMutationsAdvanceGenerationAndUpdateOpacityConservatively() throws Exception {
    Image image = new Image(1, 1);
    long initial = image.backingMutationGenerationForP2();
    int[] read = new int[1];

    assertTrue(image.backing.readPixels(read, 0, 0, 0, 1, 1));
    assertEquals(initial, image.backingMutationGenerationForP2());

    image.setTransparentColor(0);
    long afterTransparentColor = image.backingMutationGenerationForP2();
    assertTrue(afterTransparentColor > initial);
    assertEquals(ImageBacking.OPACITY_HAS_ALPHA, image.opacityStateForP2());

    image.applyColor(0xFFFFFF);
    long afterApplyColor = image.backingMutationGenerationForP2();
    assertTrue(afterApplyColor > afterTransparentColor);
    assertEquals(ImageBacking.OPACITY_HAS_ALPHA, image.opacityStateForP2());

    image.changeColors(0, 0xFF010203);
    assertTrue(image.backingMutationGenerationForP2() > afterApplyColor);
    assertEquals(ImageBacking.OPACITY_UNKNOWN, image.opacityStateForP2());
  }

  @Test
  void javaSeDirectDecodeAdoptsTheReaderDestination() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = true;
      BufferedImage sourceImage = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
      sourceImage.setRGB(0, 0, 0xFF123456);
      sourceImage.setRGB(1, 0, 0x7FABCDEF);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      assertTrue(ImageIO.write(sourceImage, "png", output));
      EncodedImageSource source = EncodedImageSource.fromBytes(output.toByteArray());
      Image image = new Image(1, 1);
      Method direct = Image.class.getDeclaredMethod("decodeEncodedSourceDirectJavaSe", EncodedImageSource.class);
      direct.setAccessible(true);

      assertTrue((Boolean) direct.invoke(image, source));
      assertTrue(image.backing instanceof RasterImageBacking);
      assertArrayEquals(new int[] { 0xFF123456, 0x7FABCDEF }, image.getPixels());
      assertTrue(image.backingMutationGenerationForP2() > 0);
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  @Test
  void jpegDecodeProvidesAnOpaqueOpacityProof() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = true;
      BufferedImage sourceImage = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
      sourceImage.setRGB(0, 0, 0xFF123456);
      sourceImage.setRGB(1, 0, 0xFFABCDEF);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      assertTrue(ImageIO.write(sourceImage, "jpeg", output));

      Image image = new Image(output.toByteArray());
      Image decoded = image.resolveForDrawing(1);
      assertEquals(2, decoded.backing.width() * decoded.backing.height());

      assertEquals(ImageBacking.OPACITY_OPAQUE, decoded.opacityStateForP2());
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  @Test
  void unsupportedDirectDecodeKeepsThePreviousBacking() throws Exception {
    boolean previousJavaSe = Settings.onJavaSE;
    try {
      Settings.onJavaSE = false;
      Image image = new Image(1, 1);
      ImageBacking previous = image.backing;
      long generation = image.backingMutationGenerationForP2();
      EncodedImageSource source = EncodedImageSource.fromBytes(png(2, 1));
      Method direct = Image.class.getDeclaredMethod("decodeEncodedSourceDirectJavaSe", EncodedImageSource.class);
      direct.setAccessible(true);

      assertFalse((Boolean) direct.invoke(image, source));
      assertSame(previous, image.backing);
      assertTrue(image.backing.isValid());
      assertEquals(generation, image.backingMutationGenerationForP2());
    } finally {
      Settings.onJavaSE = previousJavaSe;
    }
  }

  @Test
  void backingDimensionsUsePhysicalStorageWidthForSingleAndMultiFrameImages() throws Exception {
    Image single = new Image(3, 2);
    assertEquals(3, single.backing.width());
    assertEquals(2, single.backing.height());

    Image multi = new Image(6, 2);
    multi.setFrameCount(3);
    assertEquals(2, multi.getPixelWidth());
    assertEquals(6, multi.backing.width());
    assertEquals(2, multi.backing.height());
  }

  @Test
  void backingSourceRejectsStorageDimensionMismatchesButAllowsZeroVisibleFrameWidth() {
    RasterImageBacking backing = new RasterImageBacking(3, 2, 1, 3, new int[6], null);

    assertThrows(IllegalArgumentException.class,
        () -> new BackingImageSource(backing, 2, 2, 2, 2, 1, 1, -1, 2, null, null, 1, 0, true, 255, 1, 1));
    assertThrows(IllegalArgumentException.class,
        () -> new BackingImageSource(backing, 3, 1, 3, 1, 1, 1, -1, 3, null, null, 1, 0, true, 255, 1, 1));

    RasterImageBacking zeroVisible = new RasterImageBacking(0, 2, 2, 4, new int[0], new int[8]);
    BackingImageSource source = new BackingImageSource(zeroVisible, 0, 2, 0, 2, 1, 2, 0, 4,
        null, null, 1, 0, true, 255, 1, 1);
    assertEquals(0, source.width);
    assertEquals(4, source.backing.width());
  }

  @Test
  void lockingRasterImageKeepsRasterBackingAndDisablesGraphics() throws Exception {
    boolean previousOpenGL = Settings.isOpenGL;
    try {
      Settings.isOpenGL = true;
      Image image = new Image(2, 1);
      assertTrue(image.backing instanceof RasterImageBacking);

      ImageBacking backing = image.backing;
      image.lockChanges();

      assertSame(backing, image.backing);
      assertNull(image.getGraphics());
    } finally {
      Settings.isOpenGL = previousOpenGL;
    }
  }

  @Test
  void lockingNativeImageKeepsNativeBackingAndDisablesGraphics() throws Exception {
    boolean previousOpenGL = Settings.isOpenGL;
    NativeImageBacking nativeBacking = NativeImageBacking.fromHandle(1, 2, 1);
    try {
      Settings.isOpenGL = true;
      Image image = new Image(2, 1);
      image.backing = nativeBacking;

      image.lockChanges();

      assertSame(nativeBacking, image.backing);
      assertNull(image.getGraphics());
    } finally {
      nativeBacking.release();
      Settings.isOpenGL = previousOpenGL;
    }
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
    NativeImageBacking nativeBacking = NativeImageBacking.fromHandle(1, 7, 5);
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

  @Test
  void releasedNativeBackingSnapshotFailsAsInvalidState() throws Exception {
    NativeImageBacking backing = NativeImageBacking.fromHandle(1, 1, 1);
    backing.release();

    assertThrows(IllegalStateException.class, backing::snapshot);
  }

  private static final class DetachedNativeReadback extends ImageBacking {
    private final int width;
    private final int height;
    private final int[] storage;

    private DetachedNativeReadback(int width, int height, int[] storage) {
      this.width = width;
      this.height = height;
      this.storage = storage;
    }

    @Override
    boolean isNative() {
      return true;
    }

    @Override
    int width() {
      return width;
    }

    @Override
    int height() {
      return height;
    }

    @Override
    boolean isValid() {
      return storage != null && (long) width * height <= storage.length;
    }

    @Override
    int[] readVisiblePixels(int visibleWidth, int visibleHeight, int frame) {
      return storage.clone();
    }

    @Override
    int[] readStoragePixels() {
      return storage.clone();
    }

    @Override
    boolean readRgbaRow(byte[] output, int y) {
      return false;
    }

    @Override
    boolean readPixels(int[] output, int offset, int x, int y, int readWidth, int readHeight) {
      return false;
    }

    @Override
    ImageBacking snapshot() {
      return new DetachedNativeReadback(width, height, storage.clone());
    }
  }

  private static byte[] png(int width, int height) throws Exception {
    BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(source, "png", output));
    return output.toByteArray();
  }
}
