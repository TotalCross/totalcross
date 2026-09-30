// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/**
 * Reads aggregate runtime observations when diagnostics support was enabled at
 * build time. Diagnostics report activity; they do not control runtime policy.
 */
public final class RuntimeDiagnostics {
  private RuntimeDiagnostics() {
  }

  /** Returns whether this SDK build includes optional diagnostics support. */
  public static boolean isSupported() {
    return RuntimeDiagnosticsSupport.isSupported();
  }

  /**
   * Enables or disables observation for a domain. Disabled domains are checked
   * before collection work, allocation, synchronization, or native calls.
   *
   * @throws NullPointerException if {@code domain} is null
   */
  public static void setDomainEnabled(RuntimeDiagnosticSnapshot.Domain domain, boolean enabled) {
    RuntimeDiagnosticsSupport.setDomainEnabled(domain, enabled);
  }

  /**
   * Returns an immutable snapshot. A disabled domain produces the shared empty
   * snapshot without collecting Java or native values.
   */
  public static RuntimeDiagnosticSnapshot snapshot() {
    return RuntimeDiagnosticsSupport.snapshot();
  }
}
