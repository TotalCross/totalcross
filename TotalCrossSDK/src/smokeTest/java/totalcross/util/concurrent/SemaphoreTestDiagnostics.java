// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.concurrent;

import java.util.concurrent.Semaphore;

/** Smoke-source-only bridge for observing a Semaphore's native waiter count. */
public final class SemaphoreTestDiagnostics {
  private SemaphoreTestDiagnostics() {
  }

  /** Blocks until the native state reports the requested number of condition waiters. */
  public static native int awaitWaiters(Semaphore semaphore, int minimumWaiters);
}
