// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.image;

/** Internal startup bridge for native compact-backing capability resolution. */
public final class ImageCompactStorageCapabilityBridge {
  private ImageCompactStorageCapabilityBridge() {
  }

  /** @hidden */
  public static boolean isAvailable() {
    return NativeImageBacking.isCompactStorageAvailable();
  }
}
