// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import totalcross.ui.gfx.Graphics;

/**
 * Internal, unsupported bridge for the graphics package.
 *
 * @hidden
 * @deprecated This class is for TotalCross internals only and is not a supported API.
 */
@Deprecated
public final class ImageDrawingBridge {
  private static final ArrayList<PersistentOwnerEntry> PERSISTENT_OWNERS = new ArrayList<>();

  private static final class PersistentOwnerEntry {
    final WeakReference<Object> owner;
    final WeakReference<Image> image;
    WeakReference<ImagePipeline> pipeline;

    PersistentOwnerEntry(Object owner, Image image, ImagePipeline pipeline) {
      this.owner = new WeakReference<>(owner);
      this.image = new WeakReference<>(image);
      this.pipeline = new WeakReference<>(pipeline);
    }
  }

  /** Internal admission policy; IMMEDIATE is reserved for persistently owned UI consumers. */
  public enum AdmissionMode {
    SECOND_OBSERVATION,
    IMMEDIATE
  }

  private ImageDrawingBridge() {
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
    synchronized (image) {
      retainPersistentOwnerForLockedImage(owner, image);
    }
  }

  private static synchronized void retainPersistentOwnerForLockedImage(Object owner, Image image) {
    cleanPersistentOwners();
    if (findPersistentOwner(owner, image) == null) {
      addPersistentOwner(owner, image);
    }
  }

  private static void addPersistentOwner(Object owner, Image image) {
    ImagePipeline pipeline = image.pipelineForDrawingOwnership();
    if (pipeline != null) {
      pipeline.retainPersistentDrawingOwner();
    }
    PERSISTENT_OWNERS.add(new PersistentOwnerEntry(owner, image, pipeline));
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
    synchronized (image) {
      releasePersistentOwnerForLockedImage(owner, image);
    }
  }

  private static synchronized void releasePersistentOwnerForLockedImage(Object owner, Image image) {
    cleanPersistentOwners();
    PersistentOwnerEntry entry = findPersistentOwner(owner, image);
    if (entry == null) {
      throw new IllegalStateException("Persistent image drawing owner underflow");
    }
    ImagePipeline pipeline = image.pipelineForDrawingOwnership();
    if (pipeline != null) {
      pipeline.releasePersistentDrawingOwner();
    }
    PERSISTENT_OWNERS.remove(entry);
  }

  static synchronized int persistentOwnerCount(Image image) {
    cleanPersistentOwners();
    int count = 0;
    for (PersistentOwnerEntry entry : PERSISTENT_OWNERS) {
      if (entry.image.get() == image) {
        count++;
      }
    }
    return count;
  }

  static synchronized void updatePersistentOwnerPipeline(Image image, ImagePipeline pipeline) {
    cleanPersistentOwners();
    for (PersistentOwnerEntry entry : PERSISTENT_OWNERS) {
      if (entry.image.get() == image) {
        entry.pipeline = new WeakReference<>(pipeline);
      }
    }
  }

  static synchronized void cleanupPersistentOwnersForDrawing() {
    cleanPersistentOwners();
  }

  private static PersistentOwnerEntry findPersistentOwner(Object owner, Image image) {
    for (PersistentOwnerEntry entry : PERSISTENT_OWNERS) {
      if (entry.owner.get() == owner && entry.image.get() == image) {
        return entry;
      }
    }
    return null;
  }

  private static void cleanPersistentOwners() {
    for (int i = PERSISTENT_OWNERS.size() - 1; i >= 0; i--) {
      PersistentOwnerEntry entry = PERSISTENT_OWNERS.get(i);
      if (entry.owner.get() == null || entry.image.get() == null) {
        PERSISTENT_OWNERS.remove(i);
        ImagePipeline pipeline = entry.pipeline.get();
        if (pipeline != null) {
          pipeline.releasePersistentDrawingOwner();
        }
      }
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
