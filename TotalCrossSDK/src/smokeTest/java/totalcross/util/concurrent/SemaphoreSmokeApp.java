// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.concurrent;

import java.util.concurrent.Semaphore;

import totalcross.ui.MainWindow;

/** Deployed correctness smoke for the Semaphore v1 blocking surface. */
public class SemaphoreSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      verifyInitialPermitAndStoredRelease();
      verifyBlockedAcquire(false);
      verifyBlockedAcquire(true);
      verifyNegativePermits();
      verifyThreeWaiters();
      verifyOverflow();
      System.out.println("fixture=SemaphoreSmokeApp,overallPass=true,checks=7");
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("fixture=SemaphoreSmokeApp,overallPass=false,error="
          + failure.getMessage());
      failure.printStackTrace();
      System.out.flush();
      exit(1);
    }
  }

  private static void verifyInitialPermitAndStoredRelease() throws InterruptedException {
    Semaphore one = new Semaphore(1);
    require(one.tryAcquire(), "initial permit was unavailable");
    require(!one.tryAcquire(), "tryAcquire consumed a missing permit");
    one.release();
    require(one.tryAcquire(), "release did not restore a permit");
    require(!one.tryAcquire(), "permit was consumed more than once");

    Semaphore stored = new Semaphore(0);
    stored.release();
    stored.acquireUninterruptibly();
    require(!stored.tryAcquire(), "release before acquire was not consumed once");
  }

  private static void verifyBlockedAcquire(boolean interruptible) throws InterruptedException {
    Semaphore gate = new Semaphore(0);
    Semaphore ready = new Semaphore(0);
    Semaphore completed = new Semaphore(0);
    AcquireWorker worker = new AcquireWorker(gate, ready, completed, interruptible);
    new Thread(worker).start();

    ready.acquireUninterruptibly();
    gate.release();
    completed.acquireUninterruptibly();
    require(worker.failure == null, "worker acquire failed: " + worker.failure);
    require(!completed.tryAcquire(), "worker reported completion more than once");
    require(!gate.tryAcquire(), "zero-permit acquire did not consume the release");
  }

  private static void verifyNegativePermits() throws InterruptedException {
    Semaphore gate = new Semaphore(-2);
    Semaphore ready = new Semaphore(0);
    Semaphore completed = new Semaphore(0);
    AcquireWorker worker = new AcquireWorker(gate, ready, completed, false);
    new Thread(worker).start();

    ready.acquireUninterruptibly();
    gate.release();
    require(!gate.tryAcquire(), "negative permit count became available at -1");
    gate.release();
    require(!gate.tryAcquire(), "negative permit count became available at zero");
    gate.release();
    completed.acquireUninterruptibly();
    require(worker.failure == null, "negative-permit worker failed: " + worker.failure);
    require(!gate.tryAcquire(), "negative-permit acquire did not consume the release");
  }

  private static void verifyThreeWaiters() throws InterruptedException {
    Semaphore gate = new Semaphore(0);
    Semaphore ready = new Semaphore(0);
    Semaphore completed = new Semaphore(0);
    AcquireWorker[] workers = new AcquireWorker[3];
    for (int i = 0; i < workers.length; i++) {
      workers[i] = new AcquireWorker(gate, ready, completed, false);
      new Thread(workers[i]).start();
    }

    for (int i = 0; i < workers.length; i++) {
      ready.acquireUninterruptibly();
    }
    for (int i = 0; i < workers.length; i++) {
      gate.release();
      completed.acquireUninterruptibly();
    }
    require(!gate.tryAcquire(), "three-waiter test left an extra permit");
    for (int i = 0; i < workers.length; i++) {
      require(workers[i].failure == null, "waiter failed: " + workers[i].failure);
    }
  }

  private static void verifyOverflow() {
    Semaphore maximum = new Semaphore(Integer.MAX_VALUE);
    boolean overflowed = false;
    try {
      maximum.release();
    } catch (Error expected) {
      overflowed = "Maximum permit count exceeded".equals(expected.getMessage());
    }
    require(overflowed, "release did not throw the expected overflow Error");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static final class AcquireWorker implements Runnable {
    private final Semaphore gate;
    private final Semaphore ready;
    private final Semaphore completed;
    private final boolean interruptible;
    private volatile Throwable failure;

    AcquireWorker(Semaphore gate, Semaphore ready, Semaphore completed, boolean interruptible) {
      this.gate = gate;
      this.ready = ready;
      this.completed = completed;
      this.interruptible = interruptible;
    }

    @Override
    public void run() {
      try {
        ready.release();
        if (interruptible) {
          gate.acquire();
        } else {
          gate.acquireUninterruptibly();
        }
      } catch (Throwable error) {
        failure = error;
      } finally {
        completed.release();
      }
    }
  }
}
