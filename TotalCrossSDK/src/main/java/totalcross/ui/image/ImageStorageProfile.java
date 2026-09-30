// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.image;

/** Requested image storage policy. */
public enum ImageStorageProfile {
  /** Use standard image storage; this is the default when no rule matches. */
  STANDARD,
  /** Request best-effort reduced image storage where supported. */
  COMPACT
}
