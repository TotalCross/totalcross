// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

/**
 * Immutable launcher startup configuration.
 * <p>
 * This is intentionally small for the transition: it captures the entry point
 * and raw launcher arguments first, while the legacy Launcher parser still owns
 * detailed screen/window settings.
 */
public final class LauncherConfig {
  private final String mainWindowClass;
  private final String[] launcherArgs;

  public LauncherConfig(String mainWindowClass, String... launcherArgs) {
    if (mainWindowClass == null || mainWindowClass.length() == 0) {
      throw new IllegalArgumentException("mainWindowClass cannot be empty");
    }
    this.mainWindowClass = mainWindowClass;
    this.launcherArgs = launcherArgs == null ? new String[0] : launcherArgs.clone();
  }

  public String getMainWindowClass() {
    return mainWindowClass;
  }

  public String[] getLauncherArgs() {
    return launcherArgs.clone();
  }
}
