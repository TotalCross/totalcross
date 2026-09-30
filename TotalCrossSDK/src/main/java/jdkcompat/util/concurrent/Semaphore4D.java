// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util.concurrent;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

/**
 * Non-fair, native-backed Semaphore compatibility surface for deployed apps.
 *
 * <p>V1 supports one-argument construction, single-permit acquire and release,
 * and untimed tryAcquire. Initial permit values may be negative. Fairness,
 * timed and multi-permit operations, queue inspection, and serialization are
 * not supported. On TotalCross, acquire declares InterruptedException for
 * Java compatibility but native waits cannot currently be interrupted.
 */
public class Semaphore4D {
  private byte[] nativeState;

  public Semaphore4D(int permits) {
    create(permits);
  }

  /** Acquires one permit, blocking until one is available. */
  @ReplacedByNativeOnDeploy
  public void acquire() throws InterruptedException {
  }

  /** Acquires one permit without exposing interruption. */
  @ReplacedByNativeOnDeploy
  public void acquireUninterruptibly() {
  }

  /** Acquires one permit immediately when the permit count is positive. */
  @ReplacedByNativeOnDeploy
  public boolean tryAcquire() {
    return false;
  }

  /** Adds one permit. */
  @ReplacedByNativeOnDeploy
  public void release() {
  }

  @Override
  protected void finalize() {
    destroy();
  }

  @ReplacedByNativeOnDeploy
  private void create(int permits) {
  }

  @ReplacedByNativeOnDeploy
  private void destroy() {
  }
}
