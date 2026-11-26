// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.gfx.Graphics;
import totalcross.util.concurrent.Lock;

/**
 * Internal, unsupported bridge for the graphics package.
 *
 * @hidden
 * @deprecated This class is for TotalCross internals only and is not a supported API.
 */
@Deprecated
public final class ImageDrawingBridge {
  private static final Lock OWNERS_LOCK = new Lock();
  private static final PersistentImageOwnershipRegistry PERSISTENT_OWNERS = new PersistentImageOwnershipRegistry();

  /** Internal admission policy; IMMEDIATE is reserved for persistently owned UI consumers. */
  public enum AdmissionMode {
    SECOND_OBSERVATION,
    IMMEDIATE
  }

  private ImageDrawingBridge() {
  }

  static Lock ownersLock() {
    return OWNERS_LOCK;
  }

  /** @hidden */
  @Deprecated
  public static Image resolveForDrawing(Image image, double destinationScale) throws ImageException {
    return resolveForDrawing(image, destinationScale, AdmissionMode.SECOND_OBSERVATION);
  }

  /** @hidden */
  @Deprecated
  public static Image resolveForDrawing(Image image, double destinationScale, AdmissionMode admissionMode)
      throws ImageException {
    if (image == null) {
      throw new NullPointerException("image");
    }
    if (admissionMode == null) {
      throw new NullPointerException("admissionMode");
    }
    ImageRasterAdmission internalMode = admissionMode == AdmissionMode.IMMEDIATE
        ? ImageRasterAdmission.IMMEDIATE : ImageRasterAdmission.SECOND_OBSERVATION;
    return image.resolveForDrawing(destinationScale, internalMode);
  }

  /**
   * Internal helper for a persistently owned UI consumer. The owner must have been
   * registered through {@link #retainPersistentOwner(Object, Image)} in the UI attachment lifecycle.
   *
   * @hidden
   */
  @Deprecated
  public static void drawPersistentImage(Graphics graphics, Image image, int x, int y, boolean doClip) {
    if (graphics == null || image == null) {
      throw new NullPointerException(graphics == null ? "graphics" : "image");
    }
    graphics.drawImageForPersistentConsumer(image, x, y, doClip);
  }

  /**
   * Internal helper for a persistently owned UI consumer. The owner must have been
   * registered through {@link #retainPersistentOwner(Object, Image)} in the UI attachment lifecycle.
   *
   * @hidden
   */
  @Deprecated
  public static void copyPersistentImage(Graphics graphics, Image image, int x, int y, int width, int height,
      int dstX, int dstY) {
    if (graphics == null || image == null) {
      throw new NullPointerException(graphics == null ? "graphics" : "image");
    }
    graphics.copyRectForPersistentConsumer(image, x, y, width, height, dstX, dstY);
  }

  /**
   * Registers ownership from the persistent UI attachment lifecycle. Internal hook only.
   *
   * @hidden
   */
  @Deprecated
  public static void retainPersistentOwner(Object owner, Image image) {
    if (owner == null) {
      throw new NullPointerException("owner");
    }
    if (image == null) {
      throw new NullPointerException("image");
    }
    synchronized (OWNERS_LOCK) {
      PERSISTENT_OWNERS.retain(owner, image);
    }
  }

  /**
   * Releases ownership previously registered through the persistent UI attachment lifecycle.
   * The final raster is released when this was the last owner. Internal hook only.
   *
   * @hidden
   */
  @Deprecated
  public static void releasePersistentOwner(Object owner, Image image) {
    if (owner == null) {
      throw new NullPointerException("owner");
    }
    if (image == null) {
      throw new NullPointerException("image");
    }
    synchronized (OWNERS_LOCK) {
      PERSISTENT_OWNERS.release(owner, image);
    }
  }

  static int persistentOwnerCount(Image image) {
    synchronized (OWNERS_LOCK) {
      return PERSISTENT_OWNERS.ownerCount(image);
    }
  }

  static void transferPersistentOwnerPipeline(Image image, ImagePipeline previous, ImagePipeline replacement) {
    synchronized (OWNERS_LOCK) {
      PERSISTENT_OWNERS.transferPipeline(image, previous, replacement);
    }
  }

  static void maintainPersistentOwners() {
    synchronized (OWNERS_LOCK) {
      if (PERSISTENT_OWNERS.hasRelations()) {
        PERSISTENT_OWNERS.maintain();
      }
    }
  }

  static void startRegistryInspectionCountForTest() {
    synchronized (OWNERS_LOCK) {
      PERSISTENT_OWNERS.startInspectionCountForTest();
    }
  }

  static long stopRegistryInspectionCountForTest() {
    synchronized (OWNERS_LOCK) {
      return PERSISTENT_OWNERS.stopInspectionCountForTest();
    }
  }

  static int completePersistentOwnerSweepForTest() {
    synchronized (OWNERS_LOCK) {
      return PERSISTENT_OWNERS.completeSweepForTest();
    }
  }

  static int maintainPersistentOwnersForTest() {
    synchronized (OWNERS_LOCK) {
      return PERSISTENT_OWNERS.maintain();
    }
  }

  static int persistentOwnerRelationCountForTest() {
    synchronized (OWNERS_LOCK) {
      return PERSISTENT_OWNERS.relationCountForTest();
    }
  }

  static void setPersistentOwnerSweepCursorForTest(Object owner, Image image) {
    synchronized (OWNERS_LOCK) {
      PERSISTENT_OWNERS.setSweepCursorToOwnerForTest(owner, image);
    }
  }

  static void useIdentityHashOverrideForTest(int hash) {
    synchronized (OWNERS_LOCK) {
      PERSISTENT_OWNERS.useIdentityHashOverrideForTest(hash);
    }
  }

  static void restoreIdentityHashForTest() {
    synchronized (OWNERS_LOCK) {
      PERSISTENT_OWNERS.restoreIdentityHashForTest();
    }
  }

  /** @hidden */
  @Deprecated
  public static Object drawPlanForDrawing(Image image, double destinationScale) throws ImageException {
    if (image == null) {
      throw new NullPointerException("image");
    }
    return image.drawPlanForDrawing(destinationScale);
  }
}
