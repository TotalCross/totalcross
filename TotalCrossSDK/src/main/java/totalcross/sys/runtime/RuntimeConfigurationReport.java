// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

/** Human-readable diagnostic report for the current typed runtime configuration. */
public final class RuntimeConfigurationReport {
  private RuntimeConfigurationReport() {
  }

  /**
   * Describes the current runtime configuration for diagnostics.
   *
   * <p>The returned text is intended for diagnostics and may gain, reorder, or
   * rename fields between releases. Applications must not parse it as a
   * configuration protocol.
   *
   * <p>The Image section describes the resolved typed policy, including defaults
   * reserved for future consumers. A field is operational only once its owning
   * feature implementation consumes that policy.
   *
   * @return a deterministic human-readable snapshot of environment and registered feature sections
   */
  public static String describe() {
    RuntimeEnvironment environment = RuntimeEnvironment.current();
    StringBuilder output = new StringBuilder(512);
    output.append("Runtime configuration\n\nEnvironment\n");
    output.append("  platform: ").append(fact(environment.platform())).append('\n');
    output.append("  family: ").append(fact(environment.runtimeFamily())).append('\n');
    output.append("  architecture: ").append(fact(environment.architecture())).append('\n');
    output.append("  graphicsBackend: ").append(fact(environment.graphicsBackend())).append('\n');
    RuntimeConfigurationFeatureBridge.appendDescriptionSections(output);
    return output.toString();
  }

  private static String fact(Enum<?> value) {
    return value == null ? "unavailable" : value.name();
  }
}
