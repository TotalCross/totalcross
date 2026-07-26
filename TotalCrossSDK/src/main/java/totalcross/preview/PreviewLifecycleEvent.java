// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.preview;

/** Structured lifecycle notification for an IDE preview host. */
public final class PreviewLifecycleEvent {
  public enum Kind {
    START,
    READY,
    FRAME,
    RELOAD_READY,
    ERROR,
    CLOSED
  }

  private final Kind kind;
  private final String message;
  private final Throwable error;

  private PreviewLifecycleEvent(Kind kind, String message, Throwable error) {
    this.kind = kind;
    this.message = message;
    this.error = error;
  }

  public static PreviewLifecycleEvent of(Kind kind, String message) {
    return new PreviewLifecycleEvent(kind, message, null);
  }

  public static PreviewLifecycleEvent error(Throwable error) {
    return new PreviewLifecycleEvent(Kind.ERROR, error == null ? null : error.getMessage(), error);
  }

  public Kind getKind() {
    return kind;
  }

  public String getMessage() {
    return message;
  }

  public Throwable getError() {
    return error;
  }
}
