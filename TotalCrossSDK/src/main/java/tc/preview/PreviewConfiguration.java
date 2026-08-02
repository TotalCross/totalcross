// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.preview;

/** Internal preview configuration holder; not part of the tooling contract. */
final class PreviewConfiguration {
  final String mainClass;
  final String[] arguments;

  PreviewConfiguration(String mainClass, String[] arguments) {
    this.mainClass = mainClass;
    this.arguments = arguments == null ? new String[0] : arguments.clone();
  }
}
