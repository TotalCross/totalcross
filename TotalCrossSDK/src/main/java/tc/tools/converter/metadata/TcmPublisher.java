// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class TcmPublisher {
  private static final FileOperations NIO = new NioFileOperations();

  private TcmPublisher() {
  }

  static void publish(Path sidecar, byte[] bytes) throws IOException {
    publish(sidecar, bytes, NIO);
  }

  static void publish(Path sidecar, byte[] bytes, FileOperations files) throws IOException {
    Path temporary = sidecar.resolveSibling(sidecar.getFileName().toString() + ".tmp");
    try {
      files.deleteIfExists(temporary);
      files.write(temporary, bytes);
      try {
        files.atomicReplace(temporary, sidecar);
      } catch (AtomicMoveNotSupportedException e) {
        files.replace(temporary, sidecar);
      }
    } catch (IOException failure) {
      try {
        files.deleteIfExists(temporary);
      } catch (IOException cleanupFailure) {
        failure.addSuppressed(cleanupFailure);
      }
      throw failure;
    }
  }

  interface FileOperations {
    void deleteIfExists(Path path) throws IOException;

    void write(Path path, byte[] bytes) throws IOException;

    void atomicReplace(Path source, Path target) throws IOException;

    void replace(Path source, Path target) throws IOException;
  }

  private static final class NioFileOperations implements FileOperations {
    @Override
    public void deleteIfExists(Path path) throws IOException {
      Files.deleteIfExists(path);
    }

    @Override
    public void write(Path path, byte[] bytes) throws IOException {
      Files.write(path, bytes);
    }

    @Override
    public void atomicReplace(Path source, Path target) throws IOException {
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void replace(Path source, Path target) throws IOException {
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
