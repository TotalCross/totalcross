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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipInputStream;

/** Locates verified Android tools and downloads missing SDK-local tools. */
final class AndroidToolLocator {
  private static final String PROTOC_PROPERTY = "totalcross.tooling.android.protoc";
  private static final String BUNDLETOOL_PROPERTY = "totalcross.tooling.android.bundletool";

  static final String PROTOC_NAME = "protoc";
  static final String PROTOC_VERSION = "21.0";
  private static final String PROTOC_BASE_URL =
      "https://github.com/protocolbuffers/protobuf/releases/download/v";
  static final List<String> PROTOC_PLATFORMS = List.of(
      "win64", "linux-x86_64", "linux-aarch_64", "osx-universal_binary");

  static final String BUNDLETOOL_NAME = "bundletool-all";
  static final String BUNDLETOOL_VERSION = "1.10.0";
  private static final String BUNDLETOOL_FILE_NAME =
      BUNDLETOOL_NAME + "-" + BUNDLETOOL_VERSION + ".jar";
  private static final String BUNDLETOOL_DOWNLOAD_URL =
      "https://github.com/google/bundletool/releases/download/"
          + BUNDLETOOL_VERSION + "/" + BUNDLETOOL_FILE_NAME;

  private static boolean legacyWarningShown;
  private static Downloader downloader = AndroidToolLocator::downloadFileFromNetwork;
  private static Prober prober = AndroidToolLocator::probeProcess;

  @FunctionalInterface
  interface Downloader {
    void download(String fileUrl, Path outputFile) throws IOException;
  }

  @FunctionalInterface
  interface Prober {
    boolean probe(List<String> command, String expectedOutput);
  }

  private AndroidToolLocator() {
  }

  static String protoc() {
    String configured = System.getProperty(PROTOC_PROPERTY);
    if (configured != null && !configured.isBlank()) {
      return verifiedProtoc(Path.of(configured)).toString();
    }

    String platform = protocPlatform();
    Path executable = protocPath(platform);
    prepareProtoc(executable, platform);
    if (isValidProtoc(executable)) {
      return executable.toString();
    }

    Path legacy = legacyProtocPath(platform);
    prepareProtoc(legacy, platform);
    if (isValidProtoc(legacy)) {
      warnLegacy();
      return legacy.toString();
    }

    return downloadProtoc(platform).toString();
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
      downloadTo(BUNDLETOOL_DOWNLOAD_URL, preferred);
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
    prepareProtoc(path, protocPlatform());
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

  static Path protocPath(String platform) {
    requireSupportedProtocPlatform(platform);
    return androidToolsRoot()
        .resolve("protoc")
        .resolve(PROTOC_VERSION)
        .resolve(platform)
        .resolve("bin")
        .resolve(protocExecutableName(platform));
  }

  static String protocPlatform() {
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

  private static Path androidToolsRoot() {
    if (DeploySettings.etcDir == null || DeploySettings.etcDir.isBlank()) {
      throw new RuntimeException("Could not locate the SDK etc directory for Android tools");
    }
    return Path.of(DeploySettings.etcDir, "tools", "android").toAbsolutePath().normalize();
  }

  private static Path legacyProtocPath(String platform) {
    return androidToolsRoot()
        .resolve("protoc")
        .resolve("bin")
        .resolve(protocExecutableName(platform));
  }

  private static String protocExecutableName(String platform) {
    return "win64".equals(platform) ? "protoc.exe" : PROTOC_NAME;
  }

  private static void requireSupportedProtocPlatform(String platform) {
    if (!PROTOC_PLATFORMS.contains(platform)) {
      throw new IllegalArgumentException("Unsupported protoc platform: " + platform);
    }
  }

  private static Path downloadProtoc(String platform) {
    Path executable = protocPath(platform);
    String downloadUrl = protocDownloadUrl(platform);
    try {
      DeployLogger.normal("Downloading protoc " + PROTOC_VERSION + " for " + platform + "...");
      downloadAndUnzip(downloadUrl, executable.getParent().getParent(), executable);
      prepareProtoc(executable, platform);
    } catch (Exception e) {
      throw toolDownloadFailure(PROTOC_NAME, PROTOC_VERSION, platform, downloadUrl, e);
    }

    if (!isValidProtoc(executable)) {
      throw new RuntimeException("Downloaded protoc " + PROTOC_VERSION + " for " + platform
          + " failed its version probe: " + executable);
    }
    return executable;
  }

  static String protocDownloadUrl(String platform) {
    requireSupportedProtocPlatform(platform);
    return PROTOC_BASE_URL + PROTOC_VERSION + "/"
        + PROTOC_NAME + '-' + PROTOC_VERSION + '-' + platform + ".zip";
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

  private static void prepareProtoc(Path executable, String platform) {
    if (!Files.isRegularFile(executable)) {
      return;
    }
    if (DeploySettings.isMac() && "osx-universal_binary".equals(platform)) {
      removeMacQuarantine(executable);
    }
    if (!"win64".equals(platform)) {
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

  private static void downloadAndUnzip(
      String fileUrl, Path outputDirectory, Path expectedExecutable) throws IOException {
    Path parent = outputDirectory.toAbsolutePath().getParent();
    Files.createDirectories(parent);
    Path temporaryZip = Files.createTempFile(parent, "totalcross-protoc-", ".zip");
    Path temporaryDirectory = Files.createTempDirectory(parent, ".totalcross-protoc-");
    try {
      downloadTo(fileUrl, temporaryZip);
      unzip(temporaryZip, temporaryDirectory);
      Path relativeExecutable = outputDirectory.relativize(expectedExecutable);
      Path extractedExecutable = temporaryDirectory.resolve(relativeExecutable);
      if (!isRegularNonEmptyFile(extractedExecutable)) {
        throw new IOException("Downloaded archive did not contain a non-empty executable at "
            + relativeExecutable);
      }
      replaceDirectory(temporaryDirectory, outputDirectory);
      temporaryDirectory = null;
    } finally {
      Files.deleteIfExists(temporaryZip);
      if (temporaryDirectory != null) {
        deleteTree(temporaryDirectory);
      }
    }
  }

  private static void downloadTo(String fileUrl, Path outputFile) throws IOException {
    downloader.download(fileUrl, outputFile);
  }

  private static void downloadFileFromNetwork(String fileUrl, Path outputFile) throws IOException {
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
      moveAtomically(temporaryFile, outputFile);
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

  private static void replaceDirectory(Path source, Path destination) throws IOException {
    if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
      deleteTree(destination);
    }
    moveAtomically(source, destination);
  }

  private static void moveAtomically(Path source, Path destination) throws IOException {
    try {
      Files.move(source, destination,
          StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (java.nio.file.AtomicMoveNotSupportedException e) {
      Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void deleteTree(Path path) throws IOException {
    if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
      try (java.nio.file.DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
        for (Path entry : entries) {
          deleteTree(entry);
        }
      }
    }
    Files.deleteIfExists(path);
  }

  private static boolean isRegularNonEmptyFile(Path path) {
    try {
      return Files.isRegularFile(path) && Files.size(path) > 0;
    } catch (IOException e) {
      return false;
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
    return prober.probe(command, expectedOutput);
  }

  private static boolean probeProcess(List<String> command, String expectedOutput) {
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

  private static RuntimeException toolDownloadFailure(
      String tool, String version, String platform, String url, Exception cause) {
    return new RuntimeException("Failed to prepare " + tool + " " + version + " for " + platform
        + " from " + url, cause);
  }

  static void setTestHooks(Downloader testDownloader, Prober testProber) {
    downloader = testDownloader == null ? AndroidToolLocator::downloadFileFromNetwork : testDownloader;
    prober = testProber == null ? AndroidToolLocator::probeProcess : testProber;
  }

  static void resetTestHooks() {
    setTestHooks(null, null);
  }
}
