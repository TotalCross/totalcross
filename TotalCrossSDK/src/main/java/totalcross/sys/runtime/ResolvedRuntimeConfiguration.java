// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

/** Immutable startup snapshot for an application with declared configuration. */
final class ResolvedRuntimeConfiguration {
  private final RuntimeEnvironment environment;
  private final int matchedRuleCount;

  ResolvedRuntimeConfiguration(RuntimeEnvironment environment, int matchedRuleCount) {
    if (environment == null || matchedRuleCount < 0) {
      throw new IllegalArgumentException("a resolved runtime configuration requires an environment and rule count");
    }
    this.environment = environment;
    this.matchedRuleCount = matchedRuleCount;
  }

  /** Returns the immutable environment snapshot used for resolution. */
  RuntimeEnvironment environment() {
    return environment;
  }

  /** Returns the number of declared selectors that matched this snapshot. */
  int matchedRuleCount() {
    return matchedRuleCount;
  }
}
