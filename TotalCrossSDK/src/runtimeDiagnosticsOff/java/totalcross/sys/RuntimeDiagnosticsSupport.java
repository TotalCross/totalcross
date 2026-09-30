// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/** Default build implementation: no optional metric storage or native bridge. */
final class RuntimeDiagnosticsSupport {
  private RuntimeDiagnosticsSupport() {
  }

  static boolean isSupported() {
    return false;
  }

  static void setDomainEnabled(RuntimeDiagnosticSnapshot.Domain domain, boolean enabled) {
    if (domain == null) {
      throw new NullPointerException("domain is required");
    }
  }

  static RuntimeDiagnosticSnapshot snapshot() {
    return RuntimeDiagnosticSnapshot.empty();
  }
}
