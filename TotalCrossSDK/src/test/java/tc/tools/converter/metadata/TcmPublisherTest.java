// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TcmPublisherTest {
  @TempDir
  Path workDir;

  @Test
  void replacesExistingSidecarAndRemovesTemporary() throws Exception {
    Path sidecar = workDir.resolve("Sample.tcm");
    Files.write(sidecar, new byte[] { 1, 2, 3 });
    TcmPublisher.publish(sidecar, new byte[] { 4, 5, 6 });
    assertArrayEquals(new byte[] { 4, 5, 6 }, Files.readAllBytes(sidecar));
    assertFalse(Files.exists(temporary(sidecar)));
  }

  @Test
  void fallsBackWhenAtomicReplacementIsUnsupported() throws Exception {
    Path sidecar = workDir.resolve("Sample.tcm");
    Files.write(sidecar, new byte[] { 1 });
    TcmPublisher.publish(sidecar, new byte[] { 2 }, new DelegatingOperations() {
      @Override
      public void atomicReplace(Path source, Path target) throws IOException {
        throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "injected");
      }
    });
    assertArrayEquals(new byte[] { 2 }, Files.readAllBytes(sidecar));
    assertFalse(Files.exists(temporary(sidecar)));
  }

  @Test
  void preReplacementFailurePreservesPriorSidecarAndCleansTemporary() throws Exception {
    Path sidecar = workDir.resolve("Sample.tcm");
    byte[] previous = { 1, 2, 3 };
    Files.write(sidecar, previous);
    IOException failure = assertThrows(IOException.class,
        () -> TcmPublisher.publish(sidecar, new byte[] { 9, 9 }, new DelegatingOperations() {
          @Override
          public void atomicReplace(Path source, Path target) throws IOException {
            throw new IOException("injected before replacement");
          }
        }));
    assertArrayEquals(previous, Files.readAllBytes(sidecar));
    assertFalse(Files.exists(temporary(sidecar)));
    assertEquals("injected before replacement", failure.getMessage());
  }

  private static Path temporary(Path sidecar) {
    return sidecar.resolveSibling(sidecar.getFileName().toString() + ".tmp");
  }

  private static class DelegatingOperations implements TcmPublisher.FileOperations {
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
