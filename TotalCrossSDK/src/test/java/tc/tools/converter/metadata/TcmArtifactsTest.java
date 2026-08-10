// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.metadata.TcmFile.Artifact;

class TcmArtifactsTest {
  @TempDir
  Path workDir;

  @Test
  void hashesWithSha256AndPreservesSuppliedOrder() throws Exception {
    Path primary = workDir.resolve("Sample.tcz");
    Path split = workDir.resolve("Sample_1.tcz");
    Files.write(primary, "abc".getBytes(StandardCharsets.UTF_8));
    Files.write(split, "split".getBytes(StandardCharsets.UTF_8));

    List<Artifact> artifacts = TcmArtifacts.fromPaths(Arrays.asList(split, primary));
    assertEquals("Sample_1.tcz", artifacts.get(0).relativeName);
    assertEquals("Sample.tcz", artifacts.get(1).relativeName);
    assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
        hex(TcmArtifacts.sha256(primary)));
    assertArrayEquals(TcmArtifacts.sha256(primary), artifacts.get(1).sha256);
    assertEquals(workDir.resolve("Sample.tcm").toAbsolutePath().normalize(), TcmArtifacts.sidecarFor(primary));
  }

  @Test
  void validatesCountNameOrderAndHashWithExistingDiagnostics() throws Exception {
    Path primary = workDir.resolve("Sample.tcz");
    Path split = workDir.resolve("Sample_1.tcz");
    Files.write(primary, new byte[] { 1, 2, 3 });
    Files.write(split, new byte[] { 4, 5, 6 });
    List<Path> paths = Arrays.asList(primary, split);
    List<Artifact> expected = TcmArtifacts.fromPaths(paths);
    TcmArtifacts.validate(expected, paths);

    IllegalArgumentException count = assertThrows(IllegalArgumentException.class,
        () -> TcmArtifacts.validate(expected, Collections.singletonList(primary)));
    assertTrue(count.getMessage().contains("artifact count mismatch"));
    IllegalArgumentException name = assertThrows(IllegalArgumentException.class,
        () -> TcmArtifacts.validate(expected, Arrays.asList(split, primary)));
    assertTrue(name.getMessage().contains("artifact name mismatch at index 0"));

    Files.write(split, new byte[] { 9 });
    IllegalArgumentException hash = assertThrows(IllegalArgumentException.class,
        () -> TcmArtifacts.validate(expected, paths));
    assertTrue(hash.getMessage().contains("SHA-256 mismatch for Sample_1.tcz"));
  }

  private static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) result.append(String.format("%02x", value & 0xff));
    return result.toString();
  }
}
