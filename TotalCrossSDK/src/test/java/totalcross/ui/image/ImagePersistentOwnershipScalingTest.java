// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.ui.Container;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;

/** Regression probes for the persistent image owner registry and its UI lifecycle. */
class ImagePersistentOwnershipScalingTest {
  private static final int SCREEN_SIZE = 1510;
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
  void registrationWorkShouldScaleLinearlyAtRepresentativeSizes() throws Exception {
    long at500 = registrationInspections(500);
    long at1510 = registrationInspections(1510);
    long at3020 = registrationInspections(3020);

    assertAll(
        () -> assertTrue(at500 <= 40L * 500,
            "500 owner-image pairs should need near-linear inspections; observed " + at500),
        () -> assertTrue(at1510 <= 40L * 1510,
            "1,510 owner-image pairs should need near-linear inspections; observed " + at1510),
        () -> assertTrue(at3020 <= 40L * 3020,
            "3,020 owner-image pairs should need near-linear inspections; observed " + at3020));
  }

  @Test
  void removingAnAncestorContainerShouldReleaseDescendantImagesWithoutGc() throws Exception {
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    Container firstScreen = screen(image, SCREEN_SIZE);
    Container secondScreen = null;
    try {
      assertEquals(SCREEN_SIZE, pipeline.persistentDrawingOwnerCountForSmoke());
      mainWindow.add(firstScreen);
      mainWindow.remove(firstScreen);
      int afterFirstDetach = pipeline.persistentDrawingOwnerCountForSmoke();

      secondScreen = screen(image, SCREEN_SIZE);
      mainWindow.add(secondScreen);
      int afterSecondScreen = pipeline.persistentDrawingOwnerCountForSmoke();

      assertAll(
          () -> assertEquals(0, afterFirstDetach,
              "detaching an ancestor must release all descendant owners immediately"),
          () -> assertEquals(SCREEN_SIZE, afterSecondScreen,
              "reconstructed screens must not accumulate stale plus current owners"));
    } finally {
      mainWindow.remove(firstScreen);
      firstScreen.removeAll();
      if (secondScreen != null) {
        mainWindow.remove(secondScreen);
        secondScreen.removeAll();
      }
    }
  }

  @Test
  void imageResolutionAndCacheProbeShouldNotScanTheGlobalOwnerRegistry() throws Exception {
    Image image = new Image(png());
    List<Object> owners = retainOwners(image, SCREEN_SIZE);
    try {
      ImageDrawingBridge.startRegistryInspectionCountForTest();
      image.cachedFinalRasterForDrawing(1);
      image.resolveForDrawing(1);
      long inspections = ImageDrawingBridge.stopRegistryInspectionCountForTest();

      assertEquals(0, inspections,
          "image resolution and cache lookup should not inspect global owner entries");
    } finally {
      ImageDrawingBridge.stopRegistryInspectionCountForTest();
      releaseOwners(image, owners);
    }
  }

  @Test
  void suspendedSubtreeTracksMutationsAndRestoresDistinctSlotsOnReattachment() throws Exception {
    Image original = new Image(png());
    Image changed = new Image(png());
    Image background = new Image(png());
    ImagePipeline originalPipeline = original.pipelineForSmoke();
    ImagePipeline changedPipeline = changed.pipelineForSmoke();
    ImagePipeline backgroundPipeline = background.pipelineForSmoke();
    Container screen = new Container();
    Container nested = new Container();
    ImageControl existing = new ImageControl(original);
    existing.setBackground(changed);
    nested.add(existing);
    screen.add(nested);
    mainWindow.add(screen);
    try {
      assertEquals(1, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(1, changedPipeline.persistentDrawingOwnerCountForSmoke());

      mainWindow.remove(screen);
      assertEquals(0, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, changedPipeline.persistentDrawingOwnerCountForSmoke());

      existing.setImage(changed);
      existing.setBackground(background);
      ImageControl addedWhileSuspended = new ImageControl(original);
      addedWhileSuspended.setBackground(background);
      nested.add(addedWhileSuspended);
      assertEquals(0, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, changedPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, backgroundPipeline.persistentDrawingOwnerCountForSmoke());

      mainWindow.add(screen);
      assertEquals(1, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(1, changedPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(2, backgroundPipeline.persistentDrawingOwnerCountForSmoke());
    } finally {
      mainWindow.remove(screen);
      screen.removeAll();
      nested.removeAll();
    }
    assertEquals(0, originalPipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, changedPipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, backgroundPipeline.persistentDrawingOwnerCountForSmoke());
  }

  @Test
  void activeAndSuspendedReparentingReconcilesWithoutActiveEviction() throws Exception {
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    ImageControl control = new ImageControl(image);
    Container firstActive = new Container();
    Container secondActive = new Container();
    Container suspended = new Container();
    mainWindow.add(firstActive);
    mainWindow.add(secondActive);
    mainWindow.add(suspended);
    mainWindow.remove(suspended);
    firstActive.add(control);
    Image cached = ImageDrawingBridge.resolveForDrawing(image, 1,
        ImageDrawingBridge.AdmissionMode.IMMEDIATE);
    try {
      secondActive.add(control);
      assertEquals(1, pipeline.persistentDrawingOwnerCountForSmoke());
      assertSame(cached, image.cachedFinalRasterForDrawing(1));

      suspended.add(control);
      assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, pipeline.cachedVariantCountForSmoke());

      secondActive.add(control);
      assertEquals(1, pipeline.persistentDrawingOwnerCountForSmoke());
    } finally {
      secondActive.remove(control);
      firstActive.remove(control);
      suspended.remove(control);
      mainWindow.remove(firstActive);
      mainWindow.remove(secondActive);
      mainWindow.remove(suspended);
    }
    assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
  }

  @Test
  void visibilityAndPopupHidingDoNotSuspendValidOwners() throws Exception {
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    ImageControl mainControl = new ImageControl(image);
    mainWindow.add(mainControl);
    Window popup = new Window();
    ImageControl popupControl = new ImageControl(image);
    popup.add(popupControl);
    try {
      mainControl.setVisible(false);
      assertEquals(2, pipeline.persistentDrawingOwnerCountForSmoke());
      mainControl.setVisible(true);

      popup.popupNonBlocking();
      assertEquals(2, pipeline.persistentDrawingOwnerCountForSmoke());
      popup.unpop();
      assertEquals(2, pipeline.persistentDrawingOwnerCountForSmoke());
    } finally {
      popup.unpop();
      popup.removeAll();
      mainWindow.remove(mainControl);
    }
    assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
  }

  private static long registrationInspections(int count) throws Exception {
    Image image = new Image(png());
    List<Object> owners = new ArrayList<>(count);
    ImageDrawingBridge.startRegistryInspectionCountForTest();
    for (int i = 0; i < count; i++) {
      Object owner = new Object();
      owners.add(owner);
      ImageDrawingBridge.retainPersistentOwner(owner, image);
    }
    long inspections = ImageDrawingBridge.stopRegistryInspectionCountForTest();
    releaseOwners(image, owners);
    return inspections;
  }

  private static List<Object> retainOwners(Image image, int count) {
    List<Object> owners = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      Object owner = new Object();
      owners.add(owner);
      ImageDrawingBridge.retainPersistentOwner(owner, image);
    }
    return owners;
  }

  private static void releaseOwners(Image image, List<Object> owners) {
    for (int i = owners.size() - 1; i >= 0; i--) {
      ImageDrawingBridge.releasePersistentOwner(owners.get(i), image);
    }
  }

  private static Container screen(Image image, int controls) {
    Container screen = new Container();
    for (int i = 0; i < controls; i++) {
      screen.add(new ImageControl(image));
    }
    return screen;
  }

  private static byte[] png() throws Exception {
    BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, 0xFF204060);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(image, "png", output);
    return output.toByteArray();
  }
}
