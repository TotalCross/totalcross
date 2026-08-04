// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipInputStream;

/** Locates verified Android tools and downloads the legacy SDK fallback when needed. */
final class AndroidToolLocator {
  private static final String PROTOC_PROPERTY = "totalcross.tooling.android.protoc";
  private static final String BUNDLETOOL_PROPERTY = "totalcross.tooling.android.bundletool";

  private static final String PROTOC_NAME = "protoc";
  private static final String PROTOC_VERSION = "21.0";
  private static final String PROTOC_BASE_URL =
      "https://github.com/protocolbuffers/protobuf/releases/download/v";

  private static final String BUNDLETOOL_NAME = "bundletool-all";
  private static final String BUNDLETOOL_VERSION = "1.10.0";
  private static final String BUNDLETOOL_FILE_NAME =
      BUNDLETOOL_NAME + "-" + BUNDLETOOL_VERSION + ".jar";
  private static final String BUNDLETOOL_DOWNLOAD_URL =
      "https://github.com/google/bundletool/releases/download/"
          + BUNDLETOOL_VERSION + "/" + BUNDLETOOL_FILE_NAME;

  private static boolean legacyWarningShown;

  private AndroidToolLocator() {
  }

  static String protoc() {
    String configured = System.getProperty(PROTOC_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      return verifiedProtoc(Path.of(configured)).toString();
    }

    Path base = Path.of(DeploySettings.etcDir, "tools", "android", "protoc");
    Path executable = base.resolve(DeploySettings.appendDotExe("bin/protoc"));
    prepareProtoc(executable);
    if (isValidProtoc(executable)) {
      warnLegacy();
      return executable.toString();
    }

    String downloadUrl = PROTOC_BASE_URL + PROTOC_VERSION + "/"
        + PROTOC_NAME + '-' + PROTOC_VERSION + '-' + protocPlatform() + ".zip";
    try {
      DeployLogger.normal("Downloading protoc...");
      downloadAndUnzip(downloadUrl, base);
      prepareProtoc(executable);
    } catch (Exception e) {
      throw new RuntimeException("Failed to download protoc at: " + downloadUrl
          + " ; You may download it yourself and unzip the contents into the folder: "
          + base.toAbsolutePath(), e);
    }

    if (!isValidProtoc(executable)) {
      throw new RuntimeException("Downloaded protoc failed its version probe: " + executable);
    }
    warnLegacy();
    return executable.toString();
  }

  static String bundletool() {
    String configured = System.getProperty(BUNDLETOOL_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      return verifiedBundletool(Path.of(configured)).toString();
    }

    Path root = Path.of(DeploySettings.etcDir, "tools", "android");
    Path preferred = root.resolve(BUNDLETOOL_FILE_NAME);
    Path local = findValidBundletool(root, preferred);
    if (local != null) {
      warnLegacy();
      return local.toString();
    }

    try {
      DeployLogger.normal("Downloading bundletool...");
      downloadFile(BUNDLETOOL_DOWNLOAD_URL, preferred);
    } catch (Exception e) {
      throw new RuntimeException("Failed to download bundletool at: " + BUNDLETOOL_DOWNLOAD_URL
          + " ; You may download it yourself and place the jar into the folder: "
          + root.toAbsolutePath(), e);
    }

    if (!isValidBundletool(preferred)) {
      throw new RuntimeException("Downloaded bundletool failed its version probe: " + preferred);
    }
    warnLegacy();
    return preferred.toString();
  }

  private static Path verifiedProtoc(Path path) {
    prepareProtoc(path);
    if (!isValidProtoc(path)) {
      throw new RuntimeException("External Android protoc failed its version probe: " + path);
    }
    return path;
  }

  private static Path verifiedBundletool(Path path) {
    if (!isValidBundletool(path)) {
      throw new RuntimeException("External Android bundletool failed its version probe: " + path);
    }
    return path;
  }

  private static boolean isValidProtoc(Path path) {
    return Files.isRegularFile(path)
        && probe(List.of(path.toString(), "--version"), PROTOC_VERSION);
  }

  private static boolean isValidBundletool(Path path) {
    return Files.isRegularFile(path)
        && probe(List.of(javaExecutable(), "-jar", path.toString(), "version"), null);
  }

  private static Path findValidBundletool(Path root, Path preferred) {
    if (isValidBundletool(preferred)) {
      return preferred;
    }
    File[] candidates = root.toFile().listFiles(
        (dir, name) -> name.startsWith(BUNDLETOOL_NAME + "-") && name.endsWith(".jar"));
    if (candidates == null) {
      return null;
    }
    Arrays.sort(candidates, Comparator.comparing(File::getName).reversed());
    for (File candidate : candidates) {
      Path path = candidate.toPath();
      if (!path.equals(preferred) && isValidBundletool(path)) {
        return path;
      }
    }
    return null;
  }

  private static String protocPlatform() {
    if (DeploySettings.isWindows()) {
      return "win64";
    }
    if (DeploySettings.isMac()) {
      return "osx-universal_binary";
    }

    String architecture = System.getProperty("os.arch");
    if ("aarch64".equals(architecture) || "arm64".equals(architecture)) {
      architecture = "aarch_64";
    } else if ("x86_64".equals(architecture) || "amd64".equals(architecture)) {
      architecture = "x86_64";
    } else {
      DeployLogger.warn("Couldn't detect system architecture, trying with x86_64");
      architecture = "x86_64";
    }
    return "linux-" + architecture;
  }

  private static void prepareProtoc(Path executable) {
    if (!Files.isRegularFile(executable)) {
      return;
    }
    if (DeploySettings.isMac()) {
      removeMacQuarantine(executable);
    }
    if (!DeploySettings.isWindows()) {
      try {
        Files.setPosixFilePermissions(
            executable,
            PosixFilePermissions.fromString("rwxr-xr-x"));
      } catch (IOException | UnsupportedOperationException e) {
        throw new RuntimeException("Failed to set execution permission to: " + executable, e);
      }
    }
  }

  private static void removeMacQuarantine(Path executable) {
    try {
      Process query = new ProcessBuilder(
          "/usr/bin/xattr", "-p", "com.apple.quarantine", executable.toString()).start();
      if (query.waitFor() == 0) {
        Process remove = new ProcessBuilder(
            "/usr/bin/xattr", "-d", "com.apple.quarantine", executable.toString()).start();
        if (remove.waitFor() != 0) {
          DeployLogger.warn(
              "Could not remove the macOS quarantine attribute from protoc; continuing with deployment.");
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(
          "Interrupted while removing the macOS quarantine attribute from protoc", e);
    } catch (IOException e) {
      DeployLogger.warn(
          "Could not start xattr to remove the macOS quarantine attribute from protoc; continuing with deployment.");
    }
  }

  private static void downloadAndUnzip(String fileUrl, Path outputDirectory) throws IOException {
    Path temporaryZip = Files.createTempFile("totalcross-protoc-", ".zip");
    try {
      downloadFile(fileUrl, temporaryZip);
      Files.createDirectories(outputDirectory);
      unzip(temporaryZip, outputDirectory);
    } finally {
      Files.deleteIfExists(temporaryZip);
    }
  }

  private static void downloadFile(String fileUrl, Path outputFile) throws IOException {
    Path parent = outputFile.toAbsolutePath().getParent();
    Files.createDirectories(parent);
    Path temporaryFile = Files.createTempFile(parent, outputFile.getFileName().toString(), ".download");

    HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    connection.setConnectTimeout(30_000);
    connection.setReadTimeout(120_000);

    try {
      try (InputStream input = connection.getInputStream();
          FileOutputStream output = new FileOutputStream(temporaryFile.toFile())) {
        byte[] buffer = new byte[8192];
        int length;
        while ((length = input.read(buffer)) != -1) {
          output.write(buffer, 0, length);
        }
      }
      Files.move(temporaryFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      connection.disconnect();
      Files.deleteIfExists(temporaryFile);
    }
  }

  private static void unzip(Path zipFile, Path destination) throws IOException {
    Path normalizedDestination = destination.toAbsolutePath().normalize();
    try (ZipInputStream input = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
      java.util.zip.ZipEntry entry;
      while ((entry = input.getNextEntry()) != null) {
        Path target = normalizedDestination.resolve(entry.getName()).normalize();
        if (!target.startsWith(normalizedDestination)) {
          throw new IOException("Refusing to extract file outside Android tools directory: " + entry.getName());
        }
        if (entry.isDirectory()) {
          Files.createDirectories(target);
        } else {
          Files.createDirectories(target.getParent());
          try (BufferedOutputStream output =
              new BufferedOutputStream(new FileOutputStream(target.toFile()))) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) != -1) {
              output.write(buffer, 0, length);
            }
          }
        }
        input.closeEntry();
      }
    }
  }

  private static void warnLegacy() {
    if (!legacyWarningShown) {
      legacyWarningShown = true;
      DeployLogger.warn(
          "Using Android tools from the SDK fallback; install the shared tooling-managed tool store.");
    }
  }

  private static boolean probe(List<String> command, String expectedOutput) {
    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      int exit = process.waitFor();
      return exit == 0 && !output.isBlank()
          && (expectedOutput == null || output.contains(expectedOutput));
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted during Android tool version probe", e);
    }
  }

  private static String javaExecutable() {
    String name = DeploySettings.appendDotExe("java");
    String executable = Utils.searchIn(DeploySettings.path, name);
    if (executable == null || executable.isBlank()) {
      throw new RuntimeException("Could not locate Java to verify Android bundletool");
    }
    return executable;
  }
}
