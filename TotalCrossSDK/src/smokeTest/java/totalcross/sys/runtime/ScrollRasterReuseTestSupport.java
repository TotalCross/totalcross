// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys.runtime;

/** Test-only policy fixture. This class is staged only with smoke applications. */
public final class ScrollRasterReuseTestSupport {
  private ScrollRasterReuseTestSupport() {
  }

  public static boolean setEnabled(boolean enabled) {
    ImageRuntimePolicy current = ImageRuntimeConfigurationStartup.currentPolicy();
    boolean previous = current.scrollRasterReuse().enabled();
    ImageRuntimePolicy replacement = current.withScrollRasterReusePolicy(
        new ImageRuntimePolicy.ScrollRasterReusePolicy(enabled));
    ImageRuntimeConfigurationStartup.installInternalPolicy(replacement);
    return previous;
  }
}
