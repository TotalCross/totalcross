// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

  @BeforeEach
  void setUp() throws IOException {
    tempDirectory = Files.createTempDirectory("android-tool-locator-test-");
    originalEtcDir = DeploySettings.etcDir;
    originalOsName = DeploySettings.osName;
    originalOsArch = System.getProperty("os.arch");
    DeploySettings.etcDir = tempDirectory.resolve("etc").toString();
    DeploySettings.osName = "linux";
    System.setProperty("os.arch", "x86_64");
  }

  @AfterEach
  void tearDown() throws IOException {
    AndroidToolLocator.resetTestHooks();
    DeploySettings.etcDir = originalEtcDir;
    DeploySettings.osName = originalOsName;
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
      writeProtocArchive(output);
    }, (command, expected) -> true);

    String actual = AndroidToolLocator.protoc();

    assertEquals(AndroidToolLocator.protocPath("linux-x86_64").toString(), actual);
    assertEquals(List.of(AndroidToolLocator.protocDownloadUrl("linux-x86_64")), downloads);
    assertTrue(Files.isRegularFile(AndroidToolLocator.protocPath("linux-x86_64")));
    assertFalse(Files.exists(AndroidToolLocator.protocPath("win64")));
    assertFalse(Files.exists(AndroidToolLocator.protocPath("linux-aarch_64")));
    assertFalse(Files.exists(AndroidToolLocator.protocPath("osx-universal_binary")));
  }

  private static void writeProtocArchive(Path output) throws IOException {
    Files.createDirectories(output.getParent());
    try (OutputStream stream = Files.newOutputStream(output);
        ZipOutputStream zip = new ZipOutputStream(stream)) {
      zip.putNextEntry(new ZipEntry("bin/protoc"));
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
