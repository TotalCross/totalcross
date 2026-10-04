// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.ui.Container;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;

class ImagePersistentRasterAdmissionTest {
  private static MainWindow mainWindow;

  @BeforeAll
  static void initializeUi() {
    new tc.simulator.Launcher();
    if (MainWindow.getMainWindow() == null) {
      new MainWindow();
    }
    mainWindow = MainWindow.getMainWindow();
  }

  @Test
  void firstAttachedPaintAdmitsImmediatelyAndLaterPaintUsesThatRaster() throws Exception {
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    ImageControl control = attach(new ImageControl(image));
    Image target = Image.createLogical(64, 64, 1);
    try {
      assertEquals(1, pipeline.persistentDrawingOwnerCountForSmoke());
      control.onPaint(target.getGraphics());
      Image cachedAfterFirstPaint = image.cachedFinalRasterForDrawing(1);
      assertNotNull(cachedAfterFirstPaint);

      control.onPaint(target.getGraphics());

      assertSame(cachedAfterFirstPaint, image.cachedFinalRasterForDrawing(1));
      assertEquals(1, pipeline.cachedVariantCountForSmoke());
    } finally {
      mainWindow.remove(control);
    }
    assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, pipeline.cachedVariantCountForSmoke());
  }

  @Test
  void replacementAndDetachReleaseOnlyTheImagesNoLongerOwned() throws Exception {
    Image oldImage = new Image(png());
    ImagePipeline oldPipeline = oldImage.pipelineForSmoke();
    Image replacement = new Image(png());
    ImagePipeline replacementPipeline = replacement.pipelineForSmoke();
    ImageControl control = attach(new ImageControl(oldImage));
    Image target = Image.createLogical(64, 64, 1);
    try {
      control.onPaint(target.getGraphics());
      assertNotNull(oldImage.cachedFinalRasterForDrawing(1));

      control.setImage(replacement);
      assertEquals(0, oldPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, oldPipeline.cachedVariantCountForSmoke());
      assertEquals(1, replacementPipeline.persistentDrawingOwnerCountForSmoke());

      control.onPaint(target.getGraphics());
      assertNotNull(replacement.cachedFinalRasterForDrawing(1));
    } finally {
      mainWindow.remove(control);
    }
    assertEquals(0, replacementPipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, replacementPipeline.cachedVariantCountForSmoke());
  }

  @Test
  void sharedImagesRemainCachedUntilTheLastControlIsRemoved() throws Exception {
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    ImageControl first = attach(new ImageControl(image));
    ImageControl second = attach(new ImageControl(image));
    Image target = Image.createLogical(64, 64, 1);
    try {
      assertEquals(2, pipeline.persistentDrawingOwnerCountForSmoke());
      first.onPaint(target.getGraphics());
      Image cached = image.cachedFinalRasterForDrawing(1);
      assertNotNull(cached);

      mainWindow.remove(first);
      assertEquals(1, pipeline.persistentDrawingOwnerCountForSmoke());
      assertSame(cached, image.cachedFinalRasterForDrawing(1));

      mainWindow.remove(second);
      assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, pipeline.cachedVariantCountForSmoke());
    } finally {
      mainWindow.remove(first);
      mainWindow.remove(second);
    }
  }

  @Test
  void movingAControlBetweenContainersPreservesItsPersistentOwnership() throws Exception {
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    ImageControl control = attach(new ImageControl(image));
    Container nextParent = new Container();
    mainWindow.add(nextParent);
    try {
      Image cached = ImageDrawingBridge.resolveForDrawing(image, 1,
          ImageDrawingBridge.AdmissionMode.IMMEDIATE);

      nextParent.add(control);

      assertEquals(1, pipeline.persistentDrawingOwnerCountForSmoke());
      assertSame(cached, image.cachedFinalRasterForDrawing(1));
      nextParent.remove(control);
      assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, pipeline.cachedVariantCountForSmoke());
    } finally {
      nextParent.remove(control);
      mainWindow.remove(control);
      mainWindow.remove(nextParent);
    }
  }

  @Test
  void imageMutationMovesOwnershipAndScaleKeysRemainExact() throws Exception {
    Image image = new Image(png()).getSmoothScaledInstance(8, 6);
    ImagePipeline originalPipeline = image.pipelineForSmoke();
    ImageControl control = attach(new ImageControl(image));
    try {
      Image atTwo = ImageDrawingBridge.resolveForDrawing(image, 2,
          ImageDrawingBridge.AdmissionMode.IMMEDIATE);
      assertSame(atTwo, image.cachedFinalRasterForDrawing(2));
      assertNull(image.cachedFinalRasterForDrawing(1));

      Image atOne = ImageDrawingBridge.resolveForDrawing(image, 1,
          ImageDrawingBridge.AdmissionMode.IMMEDIATE);
      assertNotSame(atTwo, atOne);
      assertSame(atOne, image.cachedFinalRasterForDrawing(1));
      assertNull(image.cachedFinalRasterForDrawing(2));
      assertEquals(1, originalPipeline.cachedVariantCountForSmoke());

      image.applyFade(120);
      ImagePipeline mutatedPipeline = image.pipelineForSmoke();
      assertNotSame(originalPipeline, mutatedPipeline);
      assertEquals(0, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(1, mutatedPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, originalPipeline.cachedVariantCountForSmoke());
    } finally {
      mainWindow.remove(control);
    }
  }

  @Test
  void scrollContainerRemovalEndsTheImageControlOwnership() throws Exception {
    ScrollContainer scroll = new ScrollContainer(false, false);
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    ImageControl control = new ImageControl(image);

    scroll.add(control);
    assertEquals(1, pipeline.persistentDrawingOwnerCountForSmoke());

    scroll.remove(control);

    assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, pipeline.cachedVariantCountForSmoke());

    Image floatingImage = new Image(png());
    ImagePipeline floatingPipeline = floatingImage.pipelineForSmoke();
    FloatingImageControl floating = new FloatingImageControl(floatingImage);
    floating.markFloating();
    scroll.add(floating);
    assertEquals(1, floatingPipeline.persistentDrawingOwnerCountForSmoke());

    scroll.removeAll();

    assertEquals(0, floatingPipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, floatingPipeline.cachedVariantCountForSmoke());
  }

  private static final class FloatingImageControl extends ImageControl {
    FloatingImageControl(Image image) {
      super(image);
    }

    void markFloating() {
      floating = true;
    }
  }

  private static ImageControl attach(ImageControl control) {
    mainWindow.add(control);
    control.setRect(0, 0, 32, 32);
    return control;
  }

  private static byte[] png() throws Exception {
    BufferedImage image = new BufferedImage(16, 12, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        image.setRGB(x, y, 0xFF204060 | (x << 8) | y);
      }
    }
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(image, "png", output);
    return output.toByteArray();
  }
}
