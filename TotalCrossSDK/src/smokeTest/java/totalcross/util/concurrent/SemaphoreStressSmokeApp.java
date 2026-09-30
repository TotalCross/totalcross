// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.concurrent;

import java.util.concurrent.Semaphore;

import totalcross.ui.MainWindow;

/** Deterministic producer/consumer stress for the deployed Semaphore v1 API. */
public class SemaphoreStressSmokeApp extends MainWindow {
  private static final int THREADS_PER_SIDE = 4;
  private static final int HANDOFFS_PER_THREAD = 5000;
  private static final int TOTAL_HANDOFFS = THREADS_PER_SIDE * HANDOFFS_PER_THREAD;

  @Override
  public void initUI() {
    try {
      StressResult result = runStress();
      System.out.println("fixture=SemaphoreStressSmokeApp,overallPass=true,producers="
          + THREADS_PER_SIDE + ",consumers=" + THREADS_PER_SIDE + ",expected=" + TOTAL_HANDOFFS
          + ",produced=" + result.produced + ",acquired=" + result.acquired);
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("fixture=SemaphoreStressSmokeApp,overallPass=false,error="
          + failure.getClass().getName() + ":" + failure.getMessage());
      System.out.flush();
      exit(1);
    }
  }

  private static StressResult runStress() {
    Semaphore handoffs = new Semaphore(0);
    Semaphore ready = new Semaphore(0);
    Semaphore start = new Semaphore(0);
    Semaphore completed = new Semaphore(0);
    Producer[] producers = new Producer[THREADS_PER_SIDE];
    Consumer[] consumers = new Consumer[THREADS_PER_SIDE];

    for (int i = 0; i < THREADS_PER_SIDE; i++) {
      producers[i] = new Producer(handoffs, ready, start, completed);
      consumers[i] = new Consumer(handoffs, ready, start, completed);
      new Thread(producers[i]).start();
      new Thread(consumers[i]).start();
    }

    for (int i = 0; i < THREADS_PER_SIDE * 2; i++) ready.acquireUninterruptibly();
    for (int i = 0; i < THREADS_PER_SIDE * 2; i++) start.release();
    for (int i = 0; i < THREADS_PER_SIDE * 2; i++) completed.acquireUninterruptibly();

    int produced = 0;
    int acquired = 0;
    for (int i = 0; i < THREADS_PER_SIDE; i++) {
      requireNoFailure(producers[i].failure);
      requireNoFailure(consumers[i].failure);
      produced += producers[i].produced;
      acquired += consumers[i].acquired;
    }
    if (produced != TOTAL_HANDOFFS || acquired != TOTAL_HANDOFFS || produced != acquired) {
      throw new IllegalStateException("handoff mismatch: expected=" + TOTAL_HANDOFFS
          + ", produced=" + produced + ", acquired=" + acquired);
    }
    return new StressResult(produced, acquired);
  }

  private static void requireNoFailure(Throwable failure) {
    if (failure != null) throw new IllegalStateException("worker failed: " + failure);
  }

  private static final class Producer implements Runnable {
    private final Semaphore handoffs;
    private final Semaphore ready;
    private final Semaphore start;
    private final Semaphore completed;
    private volatile int produced;
    private volatile Throwable failure;

    Producer(Semaphore handoffs, Semaphore ready, Semaphore start, Semaphore completed) {
      this.handoffs = handoffs;
      this.ready = ready;
      this.start = start;
      this.completed = completed;
    }

    @Override
    public void run() {
      try {
        ready.release();
        start.acquireUninterruptibly();
        for (int i = 0; i < HANDOFFS_PER_THREAD; i++) {
          handoffs.release();
          produced++;
        }
      } catch (Throwable error) {
        failure = error;
      } finally {
        completed.release();
      }
    }
  }

  private static final class Consumer implements Runnable {
    private final Semaphore handoffs;
    private final Semaphore ready;
    private final Semaphore start;
    private final Semaphore completed;
    private volatile int acquired;
    private volatile Throwable failure;

    Consumer(Semaphore handoffs, Semaphore ready, Semaphore start, Semaphore completed) {
      this.handoffs = handoffs;
      this.ready = ready;
      this.start = start;
      this.completed = completed;
    }

    @Override
    public void run() {
      try {
        ready.release();
        start.acquireUninterruptibly();
        for (int i = 0; i < HANDOFFS_PER_THREAD; i++) {
          handoffs.acquireUninterruptibly();
          acquired++;
        }
      } catch (Throwable error) {
        failure = error;
      } finally {
        completed.release();
      }
    }
  }

  private static final class StressResult {
    final int produced;
    final int acquired;

    StressResult(int produced, int acquired) {
      this.produced = produced;
      this.acquired = acquired;
    }
  }
}
