// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.util.List;

public final class TcmFile {
  public final String buildIdentity;
  public final List<Artifact> artifacts;
  public final CompilationMetadata metadata;

  TcmFile(String buildIdentity, List<Artifact> artifacts, CompilationMetadata metadata) {
    this.buildIdentity = buildIdentity;
    this.artifacts = CompilationMetadata.immutable(artifacts);
    this.metadata = metadata;
  }

  public static final class Artifact {
    public final String relativeName;
    public final byte[] sha256;

    public Artifact(String relativeName, byte[] sha256) {
      if (relativeName == null || relativeName.indexOf('/') >= 0 || relativeName.indexOf('\\') >= 0) {
        throw new IllegalArgumentException("TCM artifact name must be a relative file name: " + relativeName);
      }
      if (sha256 == null || sha256.length != 32) {
        throw new IllegalArgumentException("TCM artifact SHA-256 must contain 32 bytes");
      }
      this.relativeName = relativeName;
      this.sha256 = sha256.clone();
    }
  }
}
