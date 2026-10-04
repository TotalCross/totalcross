// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.lang.ref.ReferenceQueue;
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
  private static final ReferenceQueue<Object> PERSISTENT_OWNER_QUEUE = new ReferenceQueue<>();
  private static final ArrayList<PersistentOwnerEntry> PERSISTENT_OWNERS = new ArrayList<>();

  private static final class PersistentOwnerReference extends WeakReference<Object> {
    PersistentOwnerEntry entry;

    PersistentOwnerReference(Object owner) {
      super(owner, PERSISTENT_OWNER_QUEUE);
    }
  }

  private static final class PersistentOwnerEntry {
    final PersistentOwnerReference owner;
    final WeakReference<Image> image;
    WeakReference<ImagePipeline> pipeline;

    PersistentOwnerEntry(Object owner, Image image, ImagePipeline pipeline) {
      this.owner = new PersistentOwnerReference(owner);
      this.owner.entry = this;
      this.image = new WeakReference<>(image);
      this.pipeline = new WeakReference<>(pipeline);
    }
  }

  /** Internal final-raster cache admission policy used by drawing entry points. */
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

  /** Draws through the regular Graphics path while allowing immediate final-raster admission. */
  @Deprecated
  public static void drawPersistentImage(Graphics graphics, Image image, int x, int y, boolean doClip) {
    if (graphics == null || image == null) {
      throw new NullPointerException(graphics == null ? "graphics" : "image");
    }
    graphics.drawImageForPersistentConsumer(image, x, y, doClip);
  }

  /** Copies through the regular Graphics path while allowing immediate final-raster admission. */
  @Deprecated
  public static void copyPersistentImage(Graphics graphics, Image image, int x, int y, int width, int height,
      int dstX, int dstY) {
    if (graphics == null || image == null) {
      throw new NullPointerException(graphics == null ? "graphics" : "image");
    }
    graphics.copyRectForPersistentConsumer(image, x, y, width, height, dstX, dstY);
  }

  /** Retains the image for an attached persistent UI consumer. */
  @Deprecated
  public static void retainPersistentOwner(Object owner, Image image) {
    if (owner == null) {
      throw new NullPointerException("owner");
    }
    if (image == null) {
      throw new NullPointerException("image");
    }
    synchronized (image) {
      synchronized (ImageDrawingBridge.class) {
        cleanPersistentOwners();
        if (findPersistentOwner(owner, image) != null) {
          return;
        }
        ImagePipeline pipeline = image.pipelineForDrawingOwnership();
        if (pipeline != null) {
          pipeline.retainPersistentDrawingOwner();
        }
        PERSISTENT_OWNERS.add(new PersistentOwnerEntry(owner, image, pipeline));
      }
    }
  }

  /** Releases a persistent UI consumer and its final raster when it was the last owner. */
  @Deprecated
  public static void releasePersistentOwner(Object owner, Image image) {
    if (owner == null) {
      throw new NullPointerException("owner");
    }
    if (image == null) {
      throw new NullPointerException("image");
    }
    synchronized (image) {
      synchronized (ImageDrawingBridge.class) {
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
    }
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
    PersistentOwnerReference owner;
    while ((owner = (PersistentOwnerReference) PERSISTENT_OWNER_QUEUE.poll()) != null) {
      PersistentOwnerEntry entry = owner.entry;
      if (entry != null && PERSISTENT_OWNERS.remove(entry)) {
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
