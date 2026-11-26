// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import totalcross.sys.Vm;

/** Weak identity index for persistent ImageControl-to-Image relations. */
final class PersistentImageOwnershipRegistry {
  static final int MAX_SWEEP_RELATIONSHIPS = 32;

  private static final class ImageGroup {
    final int imageHash;
    final WeakReference<Image> image;
    ImageGroup nextForHash;
    ImageGroup previousForHash;
    OwnerRelation firstOwner;
    int ownerCount;
    WeakReference<ImagePipeline> pipeline;

    ImageGroup(Image image, int imageHash, ImagePipeline pipeline) {
      this.imageHash = imageHash;
      this.image = new WeakReference<>(image);
      this.pipeline = pipeline == null ? null : new WeakReference<>(pipeline);
    }

    ImagePipeline pipeline() {
      return pipeline == null ? null : pipeline.get();
    }
  }

  private static final class OwnerRelation {
    final int ownerHash;
    final WeakReference<Object> owner;
    final ImageGroup imageGroup;
    OwnerRelation nextForOwner;
    OwnerRelation previousForOwner;
    OwnerRelation nextForImage;
    OwnerRelation previousForImage;
    OwnerRelation nextForSweep;
    OwnerRelation previousForSweep;
    boolean linked;

    OwnerRelation(Object owner, int ownerHash, ImageGroup imageGroup) {
      this.owner = new WeakReference<>(owner);
      this.ownerHash = ownerHash;
      this.imageGroup = imageGroup;
    }
  }

  private final HashMap<Integer, OwnerRelation> ownersByHash = new HashMap<>();
  private final HashMap<Integer, ImageGroup> imagesByHash = new HashMap<>();
  private OwnerRelation firstForSweep;
  private OwnerRelation lastForSweep;
  private OwnerRelation sweepCursor;
  private int relationCount;

  private boolean countInspectionsForTest;
  private long inspectionsForTest;
  private boolean useIdentityHashOverrideForTest;
  private int identityHashOverrideForTest;

  // All methods are called while ImageDrawingBridge.OWNERS_LOCK is held.
  void retain(Object owner, Image image) {
    int ownerHash = identityHash(owner);
    int imageHash = identityHash(image);
    if (findOwnerRelation(owner, image, ownerHash) != null) {
      return;
    }
    maintain();

    ImageGroup group = findImageGroup(image, imageHash);
    if (relationCount == Integer.MAX_VALUE || (group != null && group.ownerCount == Integer.MAX_VALUE)) {
      throw new IllegalStateException("Too many persistent image drawing owners");
    }
    ImagePipeline pipeline = group == null ? image.pipelineForDrawingOwnership() : group.pipeline();
    if (pipeline != null) {
      pipeline.retainPersistentDrawingOwners(1);
    }
    if (group == null) {
      group = new ImageGroup(image, imageHash, pipeline);
      linkImageGroup(group);
    }

    OwnerRelation relation = new OwnerRelation(owner, ownerHash, group);
    linkOwnerRelation(relation);
    linkImageRelation(relation);
    linkSweepRelation(relation);
    group.ownerCount++;
    relationCount++;
  }

  void release(Object owner, Image image) {
    int ownerHash = identityHash(owner);
    OwnerRelation relation = findOwnerRelation(owner, image, ownerHash);
    if (relation == null) {
      maintain();
      relation = findOwnerRelation(owner, image, ownerHash);
      if (relation == null) {
        throw new IllegalStateException("Persistent image drawing owner underflow");
      }
    }
    unlink(relation, true);
    maintain();
  }

  int ownerCount(Image image) {
    ImageGroup group = findImageGroup(image, identityHash(image));
    return group == null ? 0 : group.ownerCount;
  }

  void transferPipeline(Image image, ImagePipeline previous, ImagePipeline replacement) {
    maintain();
    ImageGroup group = findImageGroup(image, identityHash(image));
    if (group == null) {
      return;
    }
    ImagePipeline associated = group.pipeline();
    if (associated != previous) {
      throw new IllegalStateException("Persistent image drawing pipeline mismatch");
    }
    int count = group.ownerCount;
    if (associated != null) {
      associated.checkPersistentDrawingOwnerRelease(count);
    }
    if (replacement != null) {
      replacement.checkPersistentDrawingOwnerRetain(count);
    }
    if (associated != null) {
      associated.releasePersistentDrawingOwners(count);
    }
    if (replacement != null) {
      replacement.retainPersistentDrawingOwners(count);
    }
    group.pipeline = replacement == null ? null : new WeakReference<>(replacement);
  }

  int maintain() {
    if (firstForSweep == null) {
      sweepCursor = null;
      return 0;
    }
    if (sweepCursor == null) {
      sweepCursor = firstForSweep;
    }
    int inspected = 0;
    while (sweepCursor != null && inspected < MAX_SWEEP_RELATIONSHIPS) {
      OwnerRelation relation = sweepCursor;
      sweepCursor = relation.nextForSweep;
      inspected++;
      recordInspection();
      if (relation.owner.get() == null || relation.imageGroup.image.get() == null) {
        unlink(relation, true);
      }
    }
    return inspected;
  }

  int completeSweepForTest() {
    sweepCursor = firstForSweep;
    int inspected = 0;
    while (sweepCursor != null) {
      inspected += maintain();
    }
    return inspected;
  }

  int relationCountForTest() {
    return relationCount;
  }

  boolean hasRelations() {
    return relationCount != 0;
  }

  void setSweepCursorToOwnerForTest(Object owner, Image image) {
    sweepCursor = findOwnerRelation(owner, image, identityHash(owner));
  }

  void startInspectionCountForTest() {
    inspectionsForTest = 0;
    countInspectionsForTest = true;
  }

  long stopInspectionCountForTest() {
    countInspectionsForTest = false;
    return inspectionsForTest;
  }

  void useIdentityHashOverrideForTest(int hash) {
    identityHashOverrideForTest = hash;
    useIdentityHashOverrideForTest = true;
  }

  void restoreIdentityHashForTest() {
    useIdentityHashOverrideForTest = false;
  }

  private int identityHash(Object referent) {
    return useIdentityHashOverrideForTest ? identityHashOverrideForTest : Vm.identityHashCode(referent);
  }

  private void recordInspection() {
    if (countInspectionsForTest) {
      inspectionsForTest++;
    }
  }

  private OwnerRelation findOwnerRelation(Object owner, Image image, int ownerHash) {
    for (OwnerRelation relation = ownersByHash.get(Integer.valueOf(ownerHash)); relation != null;
        relation = relation.nextForOwner) {
      recordInspection();
      if (relation.owner.get() == owner && relation.imageGroup.image.get() == image) {
        return relation;
      }
    }
    return null;
  }

  private ImageGroup findImageGroup(Image image, int imageHash) {
    for (ImageGroup group = imagesByHash.get(Integer.valueOf(imageHash)); group != null;
        group = group.nextForHash) {
      recordInspection();
      if (group.image.get() == image) {
        return group;
      }
    }
    return null;
  }

  private void linkImageGroup(ImageGroup group) {
    Integer key = Integer.valueOf(group.imageHash);
    ImageGroup oldHead = imagesByHash.get(key);
    group.nextForHash = oldHead;
    if (oldHead != null) {
      oldHead.previousForHash = group;
    }
    imagesByHash.put(key, group);
  }

  private void linkOwnerRelation(OwnerRelation relation) {
    Integer key = Integer.valueOf(relation.ownerHash);
    OwnerRelation oldHead = ownersByHash.get(key);
    relation.nextForOwner = oldHead;
    if (oldHead != null) {
      oldHead.previousForOwner = relation;
    }
    ownersByHash.put(key, relation);
  }

  private void linkImageRelation(OwnerRelation relation) {
    OwnerRelation oldHead = relation.imageGroup.firstOwner;
    relation.nextForImage = oldHead;
    if (oldHead != null) {
      oldHead.previousForImage = relation;
    }
    relation.imageGroup.firstOwner = relation;
  }

  private void linkSweepRelation(OwnerRelation relation) {
    relation.previousForSweep = lastForSweep;
    if (lastForSweep == null) {
      firstForSweep = relation;
    } else {
      lastForSweep.nextForSweep = relation;
    }
    lastForSweep = relation;
    relation.linked = true;
  }

  private void unlink(OwnerRelation relation, boolean releasePipeline) {
    if (!relation.linked) {
      return;
    }
    ImageGroup group = relation.imageGroup;
    ImagePipeline pipeline = group.pipeline();
    if (releasePipeline && pipeline != null) {
      pipeline.releasePersistentDrawingOwners(1);
    }

    if (relation.previousForOwner == null) {
      Integer key = Integer.valueOf(relation.ownerHash);
      if (relation.nextForOwner == null) {
        ownersByHash.remove(key);
      } else {
        ownersByHash.put(key, relation.nextForOwner);
      }
    } else {
      relation.previousForOwner.nextForOwner = relation.nextForOwner;
    }
    if (relation.nextForOwner != null) {
      relation.nextForOwner.previousForOwner = relation.previousForOwner;
    }

    if (relation.previousForImage == null) {
      group.firstOwner = relation.nextForImage;
    } else {
      relation.previousForImage.nextForImage = relation.nextForImage;
    }
    if (relation.nextForImage != null) {
      relation.nextForImage.previousForImage = relation.previousForImage;
    }
    group.ownerCount--;
    if (group.ownerCount == 0) {
      unlinkImageGroup(group);
    }

    OwnerRelation next = relation.nextForSweep;
    if (sweepCursor == relation) {
      sweepCursor = next;
      if (sweepCursor == null && firstForSweep != relation) {
        sweepCursor = firstForSweep;
      }
    }
    if (relation.previousForSweep == null) {
      firstForSweep = relation.nextForSweep;
    } else {
      relation.previousForSweep.nextForSweep = relation.nextForSweep;
    }
    if (relation.nextForSweep == null) {
      lastForSweep = relation.previousForSweep;
    } else {
      relation.nextForSweep.previousForSweep = relation.previousForSweep;
    }
    relation.linked = false;
    relationCount--;
  }

  private void unlinkImageGroup(ImageGroup group) {
    if (group.previousForHash == null) {
      Integer key = Integer.valueOf(group.imageHash);
      if (group.nextForHash == null) {
        imagesByHash.remove(key);
      } else {
        imagesByHash.put(key, group.nextForHash);
      }
    } else {
      group.previousForHash.nextForHash = group.nextForHash;
    }
    if (group.nextForHash != null) {
      group.nextForHash.previousForHash = group.previousForHash;
    }
  }
}
