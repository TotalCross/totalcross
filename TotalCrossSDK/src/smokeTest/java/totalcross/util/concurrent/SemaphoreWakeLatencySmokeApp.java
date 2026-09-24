// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.concurrent;

import java.util.concurrent.Semaphore;

import totalcross.ui.MainWindow;

/** Measures sequential wake latency after confirming a blocked Semaphore waiter. */
public class SemaphoreWakeLatencySmokeApp extends MainWindow {
  private static final int WARMUP_COUNT = 20;
  private static final int SAMPLE_COUNT = 200;
  private static final int TOTAL_COUNT = WARMUP_COUNT + SAMPLE_COUNT;

  @Override
  public void initUI() {
    try {
      int multiWaiterConfirmed = verifyMultipleWaiters();
      LatencySummary summary = measure(multiWaiterConfirmed);
      System.out.println("fixture=SemaphoreWakeLatencySmokeApp,overallPass=true"
          + ",method=blocked-waiter-release-to-acquire,count=" + SAMPLE_COUNT
          + ",blockedConfirmed=" + summary.blockedConfirmed
          + ",confirmedHandshakes=" + summary.confirmedHandshakes
          + ",multiWaiterConfirmed=" + summary.multiWaiterConfirmed + ",minNs=" + summary.minimum
          + ",p50Ns=" + summary.median + ",p95Ns=" + summary.p95
          + ",maxNs=" + summary.maximum + ",meanNs=" + summary.mean);
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("fixture=SemaphoreWakeLatencySmokeApp,overallPass=false,error="
          + failure.getClass().getName() + ":" + failure.getMessage());
      System.out.flush();
      exit(1);
    }
  }

  private static int verifyMultipleWaiters() {
    final int waiterCount = 3;
    Semaphore tested = new Semaphore(0);
    Semaphore ready = new Semaphore(0);
    Semaphore completed = new Semaphore(0);
    DiagnosticWaiter[] waiters = new DiagnosticWaiter[waiterCount];
    for (int i = 0; i < waiters.length; i++) {
      waiters[i] = new DiagnosticWaiter(tested, ready, completed);
      new Thread(waiters[i]).start();
    }

    for (int i = 0; i < waiters.length; i++) ready.acquireUninterruptibly();
    int confirmed = SemaphoreTestDiagnostics.awaitWaiters(tested, waiterCount);
    if (confirmed != waiterCount) {
      throw new IllegalStateException("native diagnostics confirmed " + confirmed
          + " of " + waiterCount + " concurrent waiters");
    }
    for (int i = 0; i < waiters.length; i++) {
      tested.release();
      completed.acquireUninterruptibly();
    }
    for (int i = 0; i < waiters.length; i++) {
      if (waiters[i].failure != null) {
        throw new IllegalStateException("diagnostic waiter failed: " + waiters[i].failure);
      }
    }
    return confirmed;
  }

  private static LatencySummary measure(int multiWaiterConfirmed) {
    Semaphore tested = new Semaphore(0);
    Semaphore consumerStart = new Semaphore(0);
    Semaphore sampleCompleted = new Semaphore(0);
    Consumer consumer = new Consumer(tested, consumerStart, sampleCompleted);
    new Thread(consumer).start();

    long[] samples = new long[SAMPLE_COUNT];
    int confirmedHandshakes = 0;
    int blockedMeasuredSamples = 0;
    for (int i = 0; i < TOTAL_COUNT; i++) {
      consumerStart.release();
      int waiterCount = SemaphoreTestDiagnostics.awaitWaiters(tested, 1);
      if (waiterCount != 1) {
        throw new IllegalStateException("diagnostic expected one blocked waiter but confirmed " + waiterCount);
      }
      confirmedHandshakes++;
      consumer.releaseTimeNs = System.nanoTime();
      tested.release();
      sampleCompleted.acquireUninterruptibly();
      if (consumer.failure != null) {
        throw new IllegalStateException("latency consumer failed: " + consumer.failure);
      }
      long elapsed = consumer.elapsedNs;
      if (elapsed < 0) throw new IllegalStateException("negative wake duration: " + elapsed);
      if (i >= WARMUP_COUNT) {
        samples[i - WARMUP_COUNT] = elapsed;
        blockedMeasuredSamples++;
      }
    }
    if (confirmedHandshakes != TOTAL_COUNT || blockedMeasuredSamples != SAMPLE_COUNT) {
      throw new IllegalStateException("blocked waiter confirmations were incomplete");
    }
    return summarize(samples, blockedMeasuredSamples, confirmedHandshakes, multiWaiterConfirmed);
  }

  private static LatencySummary summarize(long[] samples, int blockedConfirmed, int confirmedHandshakes,
      int multiWaiterConfirmed) {
    long[] sorted = new long[samples.length];
    long quotientSum = 0;
    long remainderSum = 0;
    for (int i = 0; i < samples.length; i++) {
      sorted[i] = samples[i];
      quotientSum += samples[i] / SAMPLE_COUNT;
      remainderSum += samples[i] % SAMPLE_COUNT;
    }
    for (int i = 1; i < sorted.length; i++) {
      long value = sorted[i];
      int j = i - 1;
      while (j >= 0 && sorted[j] > value) {
        sorted[j + 1] = sorted[j];
        j--;
      }
      sorted[j + 1] = value;
    }

    long median = sorted[(SAMPLE_COUNT / 2) - 1]
        + (sorted[SAMPLE_COUNT / 2] - sorted[(SAMPLE_COUNT / 2) - 1]) / 2;
    int p95Index = ((SAMPLE_COUNT * 95 + 99) / 100) - 1;
    long roundedMean = quotientSum + ((remainderSum + (SAMPLE_COUNT / 2)) / SAMPLE_COUNT);
    return new LatencySummary(sorted[0], median, sorted[p95Index], sorted[sorted.length - 1], roundedMean,
        blockedConfirmed, confirmedHandshakes, multiWaiterConfirmed);
  }

  private static final class Consumer implements Runnable {
    private final Semaphore tested;
    private final Semaphore start;
    private final Semaphore completed;
    private volatile long releaseTimeNs;
    private volatile long elapsedNs;
    private volatile Throwable failure;

    Consumer(Semaphore tested, Semaphore start, Semaphore completed) {
      this.tested = tested;
      this.start = start;
      this.completed = completed;
    }

    @Override
    public void run() {
      for (int i = 0; i < TOTAL_COUNT; i++) {
        try {
          start.acquireUninterruptibly();
          tested.acquireUninterruptibly();
          elapsedNs = System.nanoTime() - releaseTimeNs;
        } catch (Throwable error) {
          failure = error;
          completed.release();
          return;
        }
        completed.release();
      }
    }
  }

  private static final class DiagnosticWaiter implements Runnable {
    private final Semaphore tested;
    private final Semaphore ready;
    private final Semaphore completed;
    private volatile Throwable failure;

    DiagnosticWaiter(Semaphore tested, Semaphore ready, Semaphore completed) {
      this.tested = tested;
      this.ready = ready;
      this.completed = completed;
    }

    @Override
    public void run() {
      try {
        ready.release();
        tested.acquireUninterruptibly();
      } catch (Throwable error) {
        failure = error;
      } finally {
        completed.release();
      }
    }
  }

  private static final class LatencySummary {
    final long minimum;
    final long median;
    final long p95;
    final long maximum;
    final long mean;
    final int blockedConfirmed;
    final int confirmedHandshakes;
    final int multiWaiterConfirmed;

    LatencySummary(long minimum, long median, long p95, long maximum, long mean, int blockedConfirmed,
        int confirmedHandshakes, int multiWaiterConfirmed) {
      this.minimum = minimum;
      this.median = median;
      this.p95 = p95;
      this.maximum = maximum;
      this.mean = mean;
      this.blockedConfirmed = blockedConfirmed;
      this.confirmedHandshakes = confirmedHandshakes;
      this.multiWaiterConfirmed = multiWaiterConfirmed;
    }
  }
}
