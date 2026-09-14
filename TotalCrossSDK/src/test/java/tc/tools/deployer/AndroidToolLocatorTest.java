// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AndroidToolLocatorTest {
  private Path tempDirectory;
  private String originalEtcDir;
  private String originalOsName;
  private String originalOsArch;
  private String[] originalPath;

  @BeforeEach
  void setUp() throws IOException {
    tempDirectory = Files.createTempDirectory("android-tool-locator-test-");
    originalEtcDir = DeploySettings.etcDir;
    originalOsName = DeploySettings.osName;
    originalOsArch = System.getProperty("os.arch");
    originalPath = DeploySettings.path;
    DeploySettings.etcDir = tempDirectory.resolve("etc").toString();
    DeploySettings.osName = "linux";
    DeploySettings.path = new String[] { Path.of(System.getProperty("java.home"), "bin").toString() };
    System.setProperty("os.arch", "x86_64");
  }

  @AfterEach
  void tearDown() throws IOException {
    AndroidToolLocator.resetTestHooks();
    DeploySettings.etcDir = originalEtcDir;
    DeploySettings.osName = originalOsName;
    DeploySettings.path = originalPath;
    if (originalOsArch == null) {
      System.clearProperty("os.arch");
    } else {
      System.setProperty("os.arch", originalOsArch);
    }
    deleteTree(tempDirectory);
  }

  @Test
  void exposesVersionedPathForEverySupportedPlatform() {
    assertEquals(List.of("win64", "linux-x86_64", "linux-aarch_64", "osx-universal_binary"),
        AndroidToolLocator.PROTOC_PLATFORMS);

    assertEquals(tempDirectory.resolve("etc/tools/android/protoc/21.0/win64/bin/protoc.exe"),
        AndroidToolLocator.protocPath("win64"));
    assertEquals(tempDirectory.resolve("etc/tools/android/protoc/21.0/linux-x86_64/bin/protoc"),
        AndroidToolLocator.protocPath("linux-x86_64"));
    assertEquals(tempDirectory.resolve("etc/tools/android/protoc/21.0/linux-aarch_64/bin/protoc"),
        AndroidToolLocator.protocPath("linux-aarch_64"));
    assertEquals(tempDirectory.resolve("etc/tools/android/protoc/21.0/osx-universal_binary/bin/protoc"),
        AndroidToolLocator.protocPath("osx-universal_binary"));
  }

  @Test
  void selectsCurrentPlatformUsingExistingPlatformRules() {
    DeploySettings.osName = "windows 11";
    assertEquals("win64", AndroidToolLocator.protocPlatform());

    DeploySettings.osName = "mac os x";
    assertEquals("osx-universal_binary", AndroidToolLocator.protocPlatform());

    DeploySettings.osName = "linux";
    System.setProperty("os.arch", "x86_64");
    assertEquals("linux-x86_64", AndroidToolLocator.protocPlatform());
    System.setProperty("os.arch", "aarch64");
    assertEquals("linux-aarch_64", AndroidToolLocator.protocPlatform());
  }

  @Test
  void prefersVersionedProtocOverLegacyLayout() throws IOException {
    Path versioned = AndroidToolLocator.protocPath("linux-x86_64");
    Path legacy = tempDirectory.resolve("etc/tools/android/protoc/bin/protoc");
    writeNonEmpty(versioned);
    writeNonEmpty(legacy);
    List<List<String>> probes = new ArrayList<>();
    AndroidToolLocator.setTestHooks(null, (command, expected) -> {
      probes.add(command);
      return true;
    });

    assertEquals(versioned.toString(), AndroidToolLocator.protoc());
    assertEquals(versioned.toString(), probes.get(0).get(0));
    assertFalse(probes.stream().anyMatch(command -> command.get(0).equals(legacy.toString())));
  }

  @Test
  void fallsBackToLegacyProtocWithoutWritingNewLayout() throws IOException {
    Path legacy = tempDirectory.resolve("etc/tools/android/protoc/bin/protoc");
    writeNonEmpty(legacy);
    List<String> downloads = new ArrayList<>();
    AndroidToolLocator.setTestHooks((url, output) -> downloads.add(url), (command, expected) -> true);

    assertEquals(legacy.toString(), AndroidToolLocator.protoc());
    assertTrue(downloads.isEmpty());
    assertFalse(Files.exists(AndroidToolLocator.protocPath("linux-x86_64")));
  }

  @Test
  void downloadsOnlyTheCurrentPlatformIntoVersionedLayout() throws IOException {
    List<String> downloads = new ArrayList<>();
    AndroidToolLocator.setTestHooks((url, output) -> {
      downloads.add(url);
      writeProtocArchive(output, url.endsWith("win64.zip"));
    }, (command, expected) -> true);

    String actual = AndroidToolLocator.protoc();

    assertEquals(AndroidToolLocator.protocPath("linux-x86_64").toString(), actual);
    assertEquals(List.of(AndroidToolLocator.protocDownloadUrl("linux-x86_64")), downloads);
    assertTrue(Files.isRegularFile(AndroidToolLocator.protocPath("linux-x86_64")));
    assertFalse(Files.exists(AndroidToolLocator.protocPath("win64")));
    assertFalse(Files.exists(AndroidToolLocator.protocPath("linux-aarch_64")));
    assertFalse(Files.exists(AndroidToolLocator.protocPath("osx-universal_binary")));
  }

  @Test
  void provisionsBundletoolAndAllSupportedProtocPlatforms() throws IOException {
    List<String> downloads = installFakeTooling();

    AndroidToolLocator.prepareForOfflineUse();

    assertEquals(5, downloads.size());
    assertTrue(downloads.get(0).contains("bundletool-all-1.10.0.jar"));
    for (String platform : AndroidToolLocator.PROTOC_PLATFORMS) {
      assertTrue(downloads.contains(AndroidToolLocator.protocDownloadUrl(platform)), platform);
      assertTrue(Files.isRegularFile(AndroidToolLocator.protocPath(platform)), platform);
      assertTrue(Files.size(AndroidToolLocator.protocPath(platform)) > 0, platform);
    }
  }

  @Test
  void provisionsAllPlatformsOnSimulatedWindowsWithoutForeignPreparation() throws IOException {
    assertOfflineProvisioningForHost("windows 11", "x86_64", "win64");
  }

  @Test
  void provisionsAllPlatformsOnSimulatedLinuxWithHostOnlyPreparation() throws IOException {
    assertOfflineProvisioningForHost("linux", "x86_64", "linux-x86_64");
  }

  @Test
  void provisionsAllPlatformsOnSimulatedMacWithHostOnlyPreparation() throws IOException {
    assertOfflineProvisioningForHost("mac os x", "x86_64", "osx-universal_binary");
  }

  @Test
  void reusesPreparedToolsWithoutDownloadingAgain() throws IOException {
    List<String> downloads = installFakeTooling();

    AndroidToolLocator.prepareForOfflineUse();
    AndroidToolLocator.prepareForOfflineUse();

    assertEquals(5, downloads.size());
  }

  @Test
  void doesNotProbeForeignPlatformProtocBinaries() throws IOException {
    List<String> probes = new ArrayList<>();
    installFakeTooling(probes);

    AndroidToolLocator.prepareForOfflineUse();

    for (String platform : AndroidToolLocator.PROTOC_PLATFORMS) {
      if (!platform.equals(AndroidToolLocator.protocPlatform())) {
        assertFalse(probes.stream().anyMatch(command -> command.contains("/" + platform + "/")), platform);
      }
    }
  }

  @Test
  void reportsToolVersionAndPlatformWhenProvisioningFails() {
    AndroidToolLocator.setTestHooks((url, output) -> {
      throw new IOException("offline");
    }, (command, expected) -> true);

    RuntimeException failure = assertThrows(RuntimeException.class, AndroidToolLocator::prepareForOfflineUse);

    assertTrue(failure.getMessage().contains("bundletool 1.10.0"));
    assertTrue(failure.getMessage().contains("all-platforms"));
  }

  @Test
  void rendersTheActualPreparedFilesystemTree() throws IOException {
    installFakeTooling();
    AndroidToolLocator.prepareForOfflineUse();
    Path androidRoot = tempDirectory.resolve("etc/tools/android");
    Files.write(androidRoot.resolve("filesystem-marker.bin"), new byte[] { 1 });

    String tree = AndroidToolLocator.preparedToolsTree();

    assertTrue(tree.startsWith("etc\n└── tools\n    └── android\n"));
    assertTrue(tree.contains("bundletool-all-1.10.0.jar"));
    assertTrue(tree.contains("filesystem-marker.bin"));
    assertTrue(tree.contains("linux-x86_64"));
  }

  private List<String> installFakeTooling() {
    return installFakeTooling(new ArrayList<>());
  }

  private List<String> installFakeTooling(List<String> probes) {
    List<String> downloads = new ArrayList<>();
    AndroidToolLocator.setTestHooks((url, output) -> {
      downloads.add(url);
      if (url.endsWith(".zip")) {
        writeProtocArchive(output, url.endsWith("win64.zip"));
      } else {
        writeNonEmpty(output);
      }
    }, (command, expected) -> {
      probes.add(String.join(" ", command));
      return true;
    });
    return downloads;
  }

  private void assertOfflineProvisioningForHost(
      String osName, String architecture, String currentPlatform) throws IOException {
    DeploySettings.osName = osName;
    System.setProperty("os.arch", architecture);
    if (DeploySettings.isWindows()) {
      Path fakeJava = tempDirectory.resolve("java.exe");
      writeNonEmpty(fakeJava);
      DeploySettings.path = new String[] { tempDirectory.toString() };
    }

    List<String> probes = new ArrayList<>();
    List<Path> permissioned = new ArrayList<>();
    List<Path> quarantined = new ArrayList<>();
    installFakeTooling(probes);
    AndroidToolLocator.setTestPreparationHooks(
        permissioned::add, quarantined::add);

    AndroidToolLocator.prepareForOfflineUse();

    for (String platform : AndroidToolLocator.PROTOC_PLATFORMS) {
      assertTrue(Files.isRegularFile(AndroidToolLocator.protocPath(platform)), platform);
      assertTrue(Files.size(AndroidToolLocator.protocPath(platform)) > 0, platform);
      if (!platform.equals(currentPlatform)) {
        assertFalse(probes.stream().anyMatch(command -> command.contains("/" + platform + "/")), platform);
        assertFalse(permissioned.contains(AndroidToolLocator.protocPath(platform)), platform);
        assertFalse(quarantined.contains(AndroidToolLocator.protocPath(platform)), platform);
      }
    }
    if ("win64".equals(currentPlatform)) {
      assertTrue(permissioned.isEmpty());
    } else {
      assertEquals(List.of(AndroidToolLocator.protocPath(currentPlatform)), permissioned);
    }
    if ("osx-universal_binary".equals(currentPlatform)) {
      assertEquals(List.of(AndroidToolLocator.protocPath(currentPlatform)), quarantined);
    } else {
      assertTrue(quarantined.isEmpty());
    }
  }

  private static void writeProtocArchive(Path output, boolean windows) throws IOException {
    Files.createDirectories(output.getParent());
    try (OutputStream stream = Files.newOutputStream(output);
        ZipOutputStream zip = new ZipOutputStream(stream)) {
      zip.putNextEntry(new ZipEntry("bin/protoc" + (windows ? ".exe" : "")));
      zip.write(1);
      zip.closeEntry();
    }
  }

  private static void writeNonEmpty(Path path) throws IOException {
    Files.createDirectories(path.getParent());
    Files.write(path, new byte[] { 1 });
  }

  private static void deleteTree(Path path) throws IOException {
    if (path == null || !Files.exists(path)) {
      return;
    }
    try (var entries = Files.walk(path)) {
      entries.sorted((left, right) -> right.compareTo(left)).forEach(entry -> {
        try {
          Files.deleteIfExists(entry);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
