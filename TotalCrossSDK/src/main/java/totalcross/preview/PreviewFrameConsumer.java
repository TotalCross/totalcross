// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

/** Receives owned, runtime-neutral preview frames. */
@FunctionalInterface
public interface PreviewFrameConsumer {
  void present(PreviewFrame frame);
}
