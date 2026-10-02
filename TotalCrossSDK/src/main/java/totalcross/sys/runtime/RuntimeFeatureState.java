// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

/** Tri-state assignment for an optional runtime feature in a typed configuration rule. */
public enum RuntimeFeatureState {
  /** Leave this property unassigned so another matching rule or the runtime default can decide it. */
  DEFAULT,
  /** Explicitly request that this feature be enabled. */
  ENABLED,
  /** Explicitly request that this feature be disabled. */
  DISABLED
}
