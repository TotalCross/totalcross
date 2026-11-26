// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
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
    System.out.println("registryRegistrationInspections: 500=" + at500 + ", 1510=" + at1510 + ", 3020="
        + at3020);

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
    ImagePipeline scaledPipeline;
    mainWindow.add(screen);
    try {
      assertEquals(1, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(1, changedPipeline.persistentDrawingOwnerCountForSmoke());

      mainWindow.remove(screen);
      assertEquals(0, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, changedPipeline.persistentDrawingOwnerCountForSmoke());

      existing.setImage(changed);
      existing.setBackground(background);
      existing.setRect(0, 0, 4, 4);
      existing.scaleToFit = true;
      existing.strechImage = true;
      existing.setImage(changed);
      Image scaled = existing.getImage();
      scaledPipeline = scaled.pipelineForSmoke();
      assertNotSame(changed, scaled);
      ImageControl addedWhileSuspended = new ImageControl(original);
      addedWhileSuspended.setBackground(background);
      nested.add(addedWhileSuspended);
      assertEquals(0, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, changedPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, backgroundPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, scaledPipeline.persistentDrawingOwnerCountForSmoke());

      mainWindow.add(screen);
      assertEquals(1, originalPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(1, changedPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(2, backgroundPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(1, scaledPipeline.persistentDrawingOwnerCountForSmoke());
    } finally {
      mainWindow.remove(screen);
      screen.removeAll();
      nested.removeAll();
    }
    assertEquals(0, originalPipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, changedPipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, backgroundPipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(0, scaledPipeline.persistentDrawingOwnerCountForSmoke());
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

  @Test
  void identityHashCollisionsAndContentEqualImagesRemainDistinct() throws Exception {
    Image first = new ContentEqualImage(png());
    Image second = new ContentEqualImage(png());
    ImagePipeline firstPipeline = first.pipelineForSmoke();
    ImagePipeline secondPipeline = second.pipelineForSmoke();
    Object owner = new Object();
    Object otherOwner = new Object();
    boolean ownerHasFirst = false;
    boolean ownerHasSecond = false;
    boolean otherHasFirst = false;
    ImageDrawingBridge.useIdentityHashOverrideForTest(17);
    try {
      assertNotSame(first, second);
      assertTrue(first.equals(second));
      ImageDrawingBridge.retainPersistentOwner(owner, first);
      ownerHasFirst = true;
      ImageDrawingBridge.retainPersistentOwner(owner, second);
      ownerHasSecond = true;
      ImageDrawingBridge.retainPersistentOwner(otherOwner, first);
      otherHasFirst = true;

      assertEquals(2, firstPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(1, secondPipeline.persistentDrawingOwnerCountForSmoke());

      ImageDrawingBridge.releasePersistentOwner(owner, second);
      ownerHasSecond = false;
      ImageDrawingBridge.releasePersistentOwner(owner, first);
      ownerHasFirst = false;
      assertEquals(1, firstPipeline.persistentDrawingOwnerCountForSmoke());
      assertEquals(0, secondPipeline.persistentDrawingOwnerCountForSmoke());
    } finally {
      if (ownerHasFirst) {
        ImageDrawingBridge.releasePersistentOwner(owner, first);
      }
      if (ownerHasSecond) {
        ImageDrawingBridge.releasePersistentOwner(owner, second);
      }
      if (otherHasFirst) {
        ImageDrawingBridge.releasePersistentOwner(otherOwner, first);
      }
      ImageDrawingBridge.restoreIdentityHashForTest();
    }
  }

  @Test
  void oneOwnerCanRetainManyImagesAndDuplicateOrMissingCallsStayExact() throws Exception {
    Object owner = new Object();
    Image[] images = new Image[5];
    ImagePipeline[] pipelines = new ImagePipeline[images.length];
    for (int i = 0; i < images.length; i++) {
      images[i] = new Image(png());
      pipelines[i] = images[i].pipelineForSmoke();
      ImageDrawingBridge.retainPersistentOwner(owner, images[i]);
      ImageDrawingBridge.retainPersistentOwner(owner, images[i]);
      assertEquals(1, pipelines[i].persistentDrawingOwnerCountForSmoke());
    }

    for (int i = 0; i < images.length; i++) {
      Image current = images[i];
      ImageDrawingBridge.releasePersistentOwner(owner, current);
      assertEquals(0, pipelines[i].persistentDrawingOwnerCountForSmoke());
      assertThrows(IllegalStateException.class,
          () -> ImageDrawingBridge.releasePersistentOwner(owner, current));
    }
  }

  @Test
  void pipelineTransferMovesAnAggregateCountWithoutOwnerIteration() throws Exception {
    Image image = new Image(png());
    ImagePipeline previous = image.pipelineForSmoke();
    List<Object> owners = retainOwners(image, SCREEN_SIZE);
    ImageDrawingBridge.startRegistryInspectionCountForTest();
    image.applyFade(120);
    long inspections = ImageDrawingBridge.stopRegistryInspectionCountForTest();
    ImagePipeline replacement = image.pipelineForSmoke();
    try {
      assertNotSame(previous, replacement);
      assertEquals(0, previous.persistentDrawingOwnerCountForSmoke());
      assertEquals(SCREEN_SIZE, replacement.persistentDrawingOwnerCountForSmoke());
      assertTrue(inspections < 40,
          "pipeline transfer should inspect bounded maintenance and one image group; observed " + inspections);
    } finally {
      releaseOwners(image, owners);
    }
    assertEquals(0, replacement.persistentDrawingOwnerCountForSmoke());
  }

  @Test
  void weakOwnerReclamationUsesBoundedSweepsAndCursorDeletionDoesNotDoubleRelease() throws Exception {
    ImageDrawingBridge.completePersistentOwnerSweepForTest();
    Image image = new Image(png());
    ImagePipeline pipeline = image.pipelineForSmoke();
    int initialRelations = ImageDrawingBridge.persistentOwnerRelationCountForTest();
    List<Object> ownersToDrop = new ArrayList<>(80);
    List<WeakReference<Object>> staleOwners = new ArrayList<>(80);
    for (int i = 0; i < 80; i++) {
      Object staleOwner = new Object();
      staleOwners.add(new WeakReference<>(staleOwner));
      ownersToDrop.add(staleOwner);
      ImageDrawingBridge.retainPersistentOwner(staleOwner, image);
    }
    Object liveOwner = new Object();
    ImageDrawingBridge.retainPersistentOwner(liveOwner, image);
    assertEquals(81, pipeline.persistentDrawingOwnerCountForSmoke());
    ownersToDrop.clear();
    for (WeakReference<Object> staleOwner : staleOwners) {
      awaitCollection(staleOwner);
    }

    int boundedInspections = ImageDrawingBridge.maintainPersistentOwnersForTest();
    assertEquals(PersistentImageOwnershipRegistry.MAX_SWEEP_RELATIONSHIPS, boundedInspections);
    ImageDrawingBridge.completePersistentOwnerSweepForTest();
    assertEquals(1, pipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(initialRelations + 1, ImageDrawingBridge.persistentOwnerRelationCountForTest());

    ImageDrawingBridge.setPersistentOwnerSweepCursorForTest(liveOwner, image);
    ImageDrawingBridge.releasePersistentOwner(liveOwner, image);
    ImageDrawingBridge.completePersistentOwnerSweepForTest();
    assertEquals(0, pipeline.persistentDrawingOwnerCountForSmoke());
    assertEquals(initialRelations, ImageDrawingBridge.persistentOwnerRelationCountForTest());
  }

  @Test
  void weakImageReclamationUnlinksItsRelation() throws Exception {
    ImageDrawingBridge.completePersistentOwnerSweepForTest();
    int initialRelations = ImageDrawingBridge.persistentOwnerRelationCountForTest();
    WeakReference<Image> imageReference = retainEphemeralImage(new Object());
    awaitCollection(imageReference);

    ImageDrawingBridge.completePersistentOwnerSweepForTest();

    assertNull(imageReference.get());
    assertEquals(initialRelations, ImageDrawingBridge.persistentOwnerRelationCountForTest());
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

  private static WeakReference<Image> retainEphemeralImage(Object owner) throws Exception {
    Image image = new Image(png());
    ImageDrawingBridge.retainPersistentOwner(owner, image);
    return new WeakReference<>(image);
  }

  private static void awaitCollection(WeakReference<?> reference) throws InterruptedException {
    for (int i = 0; i < 40 && reference.get() != null; i++) {
      System.gc();
      Thread.sleep(10);
    }
    assertNull(reference.get(), "test referent should be reclaimed before the explicit sweep");
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

  private static final class ContentEqualImage extends Image {
    ContentEqualImage(byte[] source) throws ImageException {
      super(source);
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof ContentEqualImage;
    }

    @Override
    public int hashCode() {
      throw new AssertionError("the ownership registry must not call Image.hashCode()");
    }
  }
}
