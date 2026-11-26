// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Narrow internal entry point for bounded ownership maintenance from Window. */
@Deprecated
public final class ImageOwnershipMaintenanceBridge {
  private static volatile boolean disabledForTest;

  private ImageOwnershipMaintenanceBridge() {
  }

  /** @hidden */
  @Deprecated
  public static void maintainPersistentImageOwnership() {
    if (!disabledForTest) {
      ImageDrawingBridge.maintainPersistentOwners();
    }
  }

  static void setDisabledForTest(boolean disabled) {
    disabledForTest = disabled;
  }
}
