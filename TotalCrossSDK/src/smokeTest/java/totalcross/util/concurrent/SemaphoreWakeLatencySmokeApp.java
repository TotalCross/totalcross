// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.concurrent;

import java.util.concurrent.Semaphore;

import totalcross.ui.MainWindow;

/** Measures sequential release-to-acquire wake latency for the Semaphore v1 API. */
public class SemaphoreWakeLatencySmokeApp extends MainWindow {
  private static final int WARMUP_COUNT = 20;
  private static final int SAMPLE_COUNT = 200;
  private static final int TOTAL_COUNT = WARMUP_COUNT + SAMPLE_COUNT;

  @Override
  public void initUI() {
    try {
      LatencySummary summary = measure();
      System.out.println("fixture=SemaphoreWakeLatencySmokeApp,overallPass=true,count=" + SAMPLE_COUNT
          + ",minNs=" + summary.minimum + ",p50Ns=" + summary.median + ",p95Ns=" + summary.p95
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

  private static LatencySummary measure() {
    Semaphore tested = new Semaphore(0);
    Semaphore consumerReady = new Semaphore(0);
    Semaphore sampleCompleted = new Semaphore(0);
    Consumer consumer = new Consumer(tested, consumerReady, sampleCompleted);
    new Thread(consumer).start();

    long[] samples = new long[SAMPLE_COUNT];
    for (int i = 0; i < TOTAL_COUNT; i++) {
      consumerReady.acquireUninterruptibly();
      consumer.releaseTimeNs = System.nanoTime();
      tested.release();
      sampleCompleted.acquireUninterruptibly();
      if (consumer.failure != null) {
        throw new IllegalStateException("latency consumer failed: " + consumer.failure);
      }
      long elapsed = consumer.elapsedNs;
      if (elapsed < 0) throw new IllegalStateException("negative wake duration: " + elapsed);
      if (i >= WARMUP_COUNT) samples[i - WARMUP_COUNT] = elapsed;
    }
    return summarize(samples);
  }

  private static LatencySummary summarize(long[] samples) {
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
    return new LatencySummary(sorted[0], median, sorted[p95Index], sorted[sorted.length - 1], roundedMean);
  }

  private static final class Consumer implements Runnable {
    private final Semaphore tested;
    private final Semaphore ready;
    private final Semaphore completed;
    private volatile long releaseTimeNs;
    private volatile long elapsedNs;
    private volatile Throwable failure;

    Consumer(Semaphore tested, Semaphore ready, Semaphore completed) {
      this.tested = tested;
      this.ready = ready;
      this.completed = completed;
    }

    @Override
    public void run() {
      for (int i = 0; i < TOTAL_COUNT; i++) {
        try {
          ready.release();
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

  private static final class LatencySummary {
    final long minimum;
    final long median;
    final long p95;
    final long maximum;
    final long mean;

    LatencySummary(long minimum, long median, long p95, long maximum, long mean) {
      this.minimum = minimum;
      this.median = median;
      this.p95 = p95;
      this.maximum = maximum;
      this.mean = mean;
    }
  }
}
