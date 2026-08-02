// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.preview;

/** Receives owned, runtime-neutral preview frames. */
@FunctionalInterface
public interface PreviewFrameSink {
  void present(PreviewFrame frame);
}
