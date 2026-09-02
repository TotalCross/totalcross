// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Internal marker for resource failures that must remain retryable. */
final class TransientImageMaterializationException extends ImageException {
  TransientImageMaterializationException() {
    super("Transient image materialization failure");
  }

  TransientImageMaterializationException(String message) {
    super(message);
  }

  TransientImageMaterializationException(Throwable cause) {
    super(cause.getMessage() == null ? "Transient image materialization failure" : cause.getMessage());
    initCause(cause);
  }
}
