// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Locates verified shared Android tools and permits a read-only legacy SDK fallback. */
final class AndroidToolLocator {
  private static final String PROTOC_PROPERTY = "totalcross.tooling.android.protoc";
  private static final String BUNDLETOOL_PROPERTY = "totalcross.tooling.android.bundletool";
  private static boolean legacyWarningShown;

  private AndroidToolLocator() {
  }

  static String protoc() {
    String configured = System.getProperty(PROTOC_PROPERTY);
    if (configured != null && !configured.isBlank()) return verified(Path.of(configured), "21.0").toString();
    String executable = DeploySettings.appendDotExe("protoc");
    Path legacy = Path.of(DeploySettings.etcDir, "tools", "android", "protoc", "bin", executable);
    if (Files.isRegularFile(legacy) && probe(List.of(legacy.toString(), "--version"), "21.0")) {
      warnLegacy();
      return legacy.toString();
    }
    throw new RuntimeException("No verified shared protoc was supplied and the SDK-local fallback is unavailable");
  }

  static String bundletool() {
    String configured = System.getProperty(BUNDLETOOL_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      Path path = Path.of(configured);
      if (!Files.isRegularFile(path)
          || !probe(List.of(javaExecutable(), "-jar", path.toString(), "version"), null)) {
        throw new RuntimeException("External Android bundletool failed its version probe: " + path);
      }
      return path.toString();
    }
    File root = new File(DeploySettings.etcDir, "tools/android");
    File[] candidates = root.listFiles((dir, name) -> name.startsWith("bundletool-all-") && name.endsWith(".jar"));
    if (candidates != null) {
      for (File candidate : candidates) {
        if (probe(List.of(javaExecutable(), "-jar", candidate.getAbsolutePath(), "version"), null)) {
          warnLegacy();
          return candidate.getAbsolutePath();
        }
      }
    }
    throw new RuntimeException("No verified shared bundletool was supplied and the SDK-local fallback is unavailable");
  }

  private static Path verified(Path path, String expectedOutput) {
    if (!Files.isRegularFile(path) || !probe(List.of(path.toString(), "--version"), expectedOutput)) {
      throw new RuntimeException("External Android tool failed its version probe: " + path);
    }
    return path;
  }

  private static void warnLegacy() {
    if (!legacyWarningShown) {
      legacyWarningShown = true;
      DeployLogger.warn("Using Android tools from the SDK; this read-only fallback is deprecated. Install the shared tool store.");
    }
  }

  private static boolean probe(List<String> command, String expectedOutput) {
    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      int exit = process.waitFor();
      return exit == 0 && !output.isBlank() && (expectedOutput == null || output.contains(expectedOutput));
    } catch (IOException e) {
      throw new RuntimeException("Could not start Android tool version probe", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted during Android tool version probe", e);
    }
  }

  private static String javaExecutable() {
    String name = DeploySettings.appendDotExe("java");
    return Utils.searchIn(DeploySettings.path, name);
  }
}
