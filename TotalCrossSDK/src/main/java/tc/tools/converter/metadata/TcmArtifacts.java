// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import tc.tools.converter.metadata.TcmFile.Artifact;

final class TcmArtifacts {
  private static final int HASH_BUFFER_SIZE = 16 * 1024;

  private TcmArtifacts() {
  }

  static Path sidecarFor(Path primaryTcz) {
    Path primary = primaryTcz.toAbsolutePath().normalize();
    String fileName = primary.getFileName().toString();
    String base = fileName.toLowerCase().endsWith(".tcz")
        ? fileName.substring(0, fileName.length() - 4) : fileName;
    return primary.resolveSibling(base + ".tcm");
  }

  static List<Artifact> fromPaths(List<Path> paths) throws IOException {
    List<Artifact> artifacts = new ArrayList<Artifact>(paths.size());
    for (Path supplied : paths) {
      Path path = supplied.toAbsolutePath().normalize();
      artifacts.add(new Artifact(path.getFileName().toString(), sha256(path)));
    }
    return artifacts;
  }

  static void validate(List<Artifact> expected, List<Path> suppliedPaths) throws IOException {
    List<Artifact> supplied = fromPaths(suppliedPaths);
    if (expected.size() != supplied.size()) {
      throw new IllegalArgumentException("TCM artifact count mismatch: expected " + expected.size()
          + ", received " + supplied.size());
    }
    for (int i = 0; i < supplied.size(); i++) {
      Artifact expectedArtifact = expected.get(i);
      Artifact suppliedArtifact = supplied.get(i);
      if (!expectedArtifact.relativeName.equals(suppliedArtifact.relativeName)) {
        throw new IllegalArgumentException("TCM artifact name mismatch at index " + i + ": expected "
            + expectedArtifact.relativeName + ", received " + suppliedArtifact.relativeName);
      }
      if (!MessageDigest.isEqual(expectedArtifact.sha256, suppliedArtifact.sha256)) {
        throw new IllegalArgumentException("TCM SHA-256 mismatch for " + expectedArtifact.relativeName);
      }
    }
  }

  static byte[] sha256(Path path) throws IOException {
    MessageDigest digest = sha256Digest();
    byte[] buffer = new byte[HASH_BUFFER_SIZE];
    try (InputStream input = Files.newInputStream(path)) {
      int count;
      while ((count = input.read(buffer)) != -1) {
        digest.update(buffer, 0, count);
      }
    }
    return digest.digest();
  }

  private static MessageDigest sha256Digest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }
}
