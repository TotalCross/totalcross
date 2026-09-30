// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

/** Helpers for constructing native environment snapshots in JVM-only tests. */
final class RuntimeEnvironmentTestSupport {
  private RuntimeEnvironmentTestSupport() {
  }

  static RuntimeEnvironment environment(String runtimePlatform, int architectureCode, boolean gpu) {
    int platformCode;
    if ("Win32".equalsIgnoreCase(runtimePlatform) || "WindowsCE".equalsIgnoreCase(runtimePlatform)) {
      platformCode = 1;
    } else if ("MacOS".equalsIgnoreCase(runtimePlatform) || "Mac".equalsIgnoreCase(runtimePlatform)) {
      platformCode = 2;
    } else if ("Linux".equalsIgnoreCase(runtimePlatform) || "Linux_ARM".equalsIgnoreCase(runtimePlatform)) {
      platformCode = 3;
    } else if ("Android".equalsIgnoreCase(runtimePlatform)) {
      platformCode = 4;
    } else if ("iPhone".equalsIgnoreCase(runtimePlatform) || "iPad".equalsIgnoreCase(runtimePlatform)) {
      platformCode = 5;
    } else {
      platformCode = 0;
    }
    return RuntimeEnvironment.fromRuntimeSettings(runtimePlatform, false, null, null, platformCode,
        architectureCode, gpu ? 2 : 1);
  }
}
