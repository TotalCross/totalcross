// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.util.concurrent.Lock;

/** Internal serialized coordinator for detached display preparation. */
final class ImagePreparation {
  static final int READY = 0;
  static final int FAILED = 1;
  static final int NOT_PREFETCHABLE = 2;
  static final String PREFETCH_THREAD_MODE_LEGACY = "legacy";
  static final String PREFETCH_THREAD_MODE_WORKER_POLL = "worker-poll";
  static final String PREFETCH_THREAD_MODE_WORKER = PREFETCH_THREAD_MODE_WORKER_POLL;
  static final String PREFETCH_THREAD_MODE_WORKER_SEMAPHORE = "worker-semaphore";

  private static final Lock LOCK = new Lock();
  private static Semaphore workerWakeSemaphore;
  private static final ArrayList<Entry> entries = new ArrayList<Entry>();
  private static final ArrayList<Entry> pending = new ArrayList<Entry>();
  private static Entry activeEntry;
  private static Thread prefetchWorker;
  private static boolean workerShutdownRequested;
  private static boolean workerCanClaim = true;
  private static boolean workerWakePending;
  private static boolean workerWaitingOnSemaphore;
  private static String prefetchThreadMode = PREFETCH_THREAD_MODE_LEGACY;
  private static int prefetchWorkerSleepMs;
  private static volatile Runnable beforeAdoptionHookForTest;
  private static volatile boolean runUiInlineForTest;
  private static long requestCount;
  private static long readyCount;
  private static long failedCount;
  private static long notPrefetchableCount;
  private static long preparationEntryCount;
  private static long preparationEntryTotalNs;
  private static long threadCreateCount;
  private static long threadObjectCreateNs;
  private static long threadStartCount;
  private static long threadStartCallNs;
  private static long threadStartLatencyNs;
  private static long decodeEntryCount;
  private static long decodeWorkerNs;
  private static long uiDispatchCount;
  private static long uiDispatchWaitNs;
  private static long adoptNs;
  private static long finishPreparationNs;
  private static long finishBookkeepingNs;
  private static long workerPollCount;
  private static long workerSleepRequestedNs;
  private static long workerIdleElapsedNs;
  private static long workerSemaphoreReleaseCount;
  private static long workerSemaphoreAcquireCount;
  private static long workerSemaphoreWakeCount;

  private ImagePreparation() {
  }

  static void resetAccountingForTest() {
    synchronized (LOCK) {
      requestCount = 0;
      readyCount = 0;
      failedCount = 0;
      notPrefetchableCount = 0;
      preparationEntryCount = 0;
      preparationEntryTotalNs = 0;
      threadCreateCount = 0;
      threadObjectCreateNs = 0;
      threadStartCount = 0;
      threadStartCallNs = 0;
      threadStartLatencyNs = 0;
      decodeEntryCount = 0;
      decodeWorkerNs = 0;
      uiDispatchCount = 0;
      uiDispatchWaitNs = 0;
      adoptNs = 0;
      finishPreparationNs = 0;
      finishBookkeepingNs = 0;
      workerPollCount = 0;
      workerSleepRequestedNs = 0;
      workerIdleElapsedNs = 0;
      workerSemaphoreReleaseCount = 0;
      workerSemaphoreAcquireCount = 0;
      workerSemaphoreWakeCount = 0;
    }
  }

  static long requestCountForTest() {
    synchronized (LOCK) {
      return requestCount;
    }
  }

  static long readyCountForTest() {
    synchronized (LOCK) {
      return readyCount;
    }
  }

  static long failedCountForTest() {
    synchronized (LOCK) {
      return failedCount;
    }
  }

  static long notPrefetchableCountForTest() {
    synchronized (LOCK) {
      return notPrefetchableCount;
    }
  }

  static long preparationEntryCountForTest() {
    synchronized (LOCK) {
      return preparationEntryCount;
    }
  }

  static long preparationEntryTotalNsForTest() {
    synchronized (LOCK) {
      return preparationEntryTotalNs;
    }
  }

  static long threadCreateCountForTest() {
    synchronized (LOCK) {
      return threadCreateCount;
    }
  }

  static long threadObjectCreateNsForTest() {
    synchronized (LOCK) {
      return threadObjectCreateNs;
    }
  }

  static long threadStartCountForTest() {
    synchronized (LOCK) {
      return threadStartCount;
    }
  }

  static long threadStartCallNsForTest() {
    synchronized (LOCK) {
      return threadStartCallNs;
    }
  }

  static long threadStartLatencyNsForTest() {
    synchronized (LOCK) {
      return threadStartLatencyNs;
    }
  }

  static long decodeEntryCountForTest() {
    synchronized (LOCK) {
      return decodeEntryCount;
    }
  }

  static long decodeWorkerNsForTest() {
    synchronized (LOCK) {
      return decodeWorkerNs;
    }
  }

  static long uiDispatchCountForTest() {
    synchronized (LOCK) {
      return uiDispatchCount;
    }
  }

  static long uiDispatchWaitNsForTest() {
    synchronized (LOCK) {
      return uiDispatchWaitNs;
    }
  }

  static long adoptNsForTest() {
    synchronized (LOCK) {
      return adoptNs;
    }
  }

  static long finishPreparationNsForTest() {
    synchronized (LOCK) {
      return finishPreparationNs;
    }
  }

  static long finishBookkeepingNsForTest() {
    synchronized (LOCK) {
      return finishBookkeepingNs;
    }
  }

  static long workerPollCountForTest() {
    synchronized (LOCK) {
      return workerPollCount;
    }
  }

  static long workerSleepRequestedNsForTest() {
    synchronized (LOCK) {
      return workerSleepRequestedNs;
    }
  }

  static long workerIdleElapsedNsForTest() {
    synchronized (LOCK) {
      return workerIdleElapsedNs;
    }
  }

  static long workerSemaphoreReleaseCountForTest() {
    synchronized (LOCK) {
      return workerSemaphoreReleaseCount;
    }
  }

  static long workerSemaphoreAcquireCountForTest() {
    synchronized (LOCK) {
      return workerSemaphoreAcquireCount;
    }
  }

  static long workerSemaphoreWakeCountForTest() {
    synchronized (LOCK) {
      return workerSemaphoreWakeCount;
    }
  }

  static long workerSemaphoreOutstandingWakeCountForTest() {
    synchronized (LOCK) {
      return workerSemaphoreReleaseCount - workerSemaphoreAcquireCount;
    }
  }

  static String prefetchThreadModeForTest() {
    synchronized (LOCK) {
      return prefetchThreadMode;
    }
  }

  static int prefetchWorkerSleepMsForTest() {
    synchronized (LOCK) {
      return prefetchWorkerSleepMs;
    }
  }

  static boolean workerRunningForTest() {
    synchronized (LOCK) {
      return prefetchWorker != null;
    }
  }

  static boolean activeEntryInFlightForTest() {
    synchronized (LOCK) {
      return activeEntry != null;
    }
  }

  static boolean workerWaitingOnSemaphoreForTest() {
    synchronized (LOCK) {
      return workerWaitingOnSemaphore;
    }
  }

  static void configurePrefetchThreadModeForDiagnostic(String mode, int workerSleepMs) {
    final String normalizedMode;
    final int normalizedSleepMs;
    if (PREFETCH_THREAD_MODE_LEGACY.equals(mode)) {
      normalizedMode = PREFETCH_THREAD_MODE_LEGACY;
      normalizedSleepMs = 0;
    } else if (PREFETCH_THREAD_MODE_WORKER.equals(mode) || "worker".equals(mode)) {
      if (workerSleepMs <= 0) {
        throw new IllegalArgumentException("worker sleep must be positive");
      }
      normalizedMode = PREFETCH_THREAD_MODE_WORKER;
      normalizedSleepMs = workerSleepMs;
    } else if (PREFETCH_THREAD_MODE_WORKER_SEMAPHORE.equals(mode)) {
      if (workerSleepMs != 0) {
        throw new IllegalArgumentException("semaphore worker sleep must be zero");
      }
      normalizedMode = PREFETCH_THREAD_MODE_WORKER_SEMAPHORE;
      normalizedSleepMs = 0;
    } else {
      throw new IllegalArgumentException("unknown prefetch thread mode");
    }
    synchronized (LOCK) {
      if (activeEntry != null || !pending.isEmpty() || prefetchWorker != null) {
        throw new IllegalStateException("cannot change prefetch thread mode while busy");
      }
      prefetchThreadMode = normalizedMode;
      prefetchWorkerSleepMs = normalizedSleepMs;
      if (PREFETCH_THREAD_MODE_WORKER_SEMAPHORE.equals(normalizedMode)
          && workerWakeSemaphore == null) {
        workerWakeSemaphore = new Semaphore(0);
      }
      workerShutdownRequested = false;
      workerCanClaim = true;
      workerWakePending = false;
      workerWaitingOnSemaphore = false;
    }
  }

  static void shutdownWorkerForTest() {
    final Thread worker;
    synchronized (LOCK) {
      workerShutdownRequested = true;
      worker = prefetchWorker;
      if (worker != null && PREFETCH_THREAD_MODE_WORKER_SEMAPHORE.equals(prefetchThreadMode)) {
        releaseSemaphoreWakeLocked();
      }
    }
    if (worker != null && worker != Thread.currentThread()) {
      for (int remainingMs = 5000; remainingMs > 0; remainingMs--) {
        synchronized (LOCK) {
          if (prefetchWorker != worker) {
            break;
          }
        }
        Vm.sleep(1);
      }
    }
    synchronized (LOCK) {
      if (prefetchWorker == null) {
        workerShutdownRequested = false;
      }
    }
  }

  static void resetThreadModeForTest() {
    shutdownWorkerForTest();
    synchronized (LOCK) {
      prefetchThreadMode = PREFETCH_THREAD_MODE_LEGACY;
      prefetchWorkerSleepMs = 0;
      workerCanClaim = true;
      if (prefetchWorker == null) {
        workerShutdownRequested = false;
        workerWaitingOnSemaphore = false;
      }
    }
  }

  static int activeEntryCountForTest() {
    synchronized (LOCK) {
      return entries.size();
    }
  }

  static void setBeforeAdoptionHookForTest(Runnable hook) {
    beforeAdoptionHookForTest = hook;
  }

  static void setRunUiInlineForTest(boolean runInline) {
    runUiInlineForTest = runInline;
  }

  static void request(final Image image, final double destinationScale, final Runnable onComplete) {
    request(image, destinationScale, ImageDrawingBridge.DRAW_READY, onComplete);
  }

  static void request(final Image image, final double destinationScale, final int requirement,
      final Runnable onComplete) {
    synchronized (LOCK) {
      if (Image.diagnosticAccountingEnabledForTest()) {
        requestCount++;
      }
    }
    if (image == null) {
      recordOutcome(NOT_PREFETCHABLE, 1);
      postCompletion(onComplete);
      return;
    }

    final Request request;
    try {
      request = image.createPreparationRequest(destinationScale, NativeImageBacking.isAvailable(), requirement);
    } catch (Throwable failure) {
      recordOutcome(FAILED, 1);
      postCompletion(onComplete);
      return;
    }
    if (request.status != -1) {
      recordOutcome(request.status, 1);
      postCompletion(onComplete);
      return;
    }
    Entry entry = null;
    boolean schedule = false;
    synchronized (LOCK) {
      entry = findEntry(request);
      if (entry == null) {
        entry = new Entry(request, request.requirement);
        entries.add(entry);
        entry.callbacks.add(onComplete);
        entry.state = State.QUEUED;
        pending.add(entry);
        schedule = true;
      } else if (entry.state == State.QUEUED || entry.state == State.DECODING
          || entry.state == State.ADOPTING) {
        if (request.requirement > entry.requirement) {
          entry.requirement = request.requirement;
        }
        entry.callbacks.add(onComplete);
      }
    }
    if (schedule) {
      scheduleNext();
    }
  }

  private static Entry findEntry(Request request) {
    for (int i = entries.size() - 1; i >= 0; i--) {
      Entry entry = entries.get(i);
      if (entry.request.image == request.image && entry.request.pipeline == request.pipeline
          && entry.request.scaleBits == request.scaleBits
          && entry.request.sourceContentIdentity == request.sourceContentIdentity) {
        return entry;
      }
    }
    return null;
  }

  private static void scheduleNext() {
    final Entry entry;
    final boolean alreadyDecoded;
    final boolean workerMode;
    boolean startWorker = false;
    synchronized (LOCK) {
      final boolean persistentWorkerMode = isPersistentWorkerMode(prefetchThreadMode);
      if (activeEntry != null || pending.isEmpty()
          || persistentWorkerMode && !workerCanClaim) {
        return;
      }
      workerMode = persistentWorkerMode;
      if (workerMode && !pending.get(0).request.alreadyDecoded) {
        entry = null;
        alreadyDecoded = false;
        startWorker = true;
      } else {
        entry = pending.remove(0);
        alreadyDecoded = entry.request.alreadyDecoded;
        activateEntryLocked(entry, alreadyDecoded);
      }
    }
    if (startWorker) {
      try {
        ensureWorkerStarted();
      } catch (Throwable failure) {
        failNextPendingWorkerStart();
      }
      return;
    }
    if (alreadyDecoded) {
      try {
        postUiAsync(new Runnable() {
          @Override
          public void run() {
            adoptAlreadyDecoded(entry);
          }
        });
      } catch (Throwable failure) {
        finish(entry, FAILED);
      }
      return;
    }
    if (workerMode) {
      return;
    }
    try {
      startPreparationThread(new Runnable() {
        @Override
        public void run() {
          decode(entry);
        }
      });
    } catch (Throwable failure) {
      finish(entry, FAILED);
    }
  }

  private static void activateEntryLocked(Entry entry, boolean alreadyDecoded) {
    activeEntry = entry;
    entry.state = alreadyDecoded ? State.ADOPTING : State.DECODING;
    if (Image.diagnosticAccountingEnabledForTest()) {
      preparationEntryCount++;
      entry.preparationAccountingEnabled = true;
      entry.preparationStartNs = System.nanoTime();
    }
  }

  private static void ensureWorkerStarted() {
    synchronized (LOCK) {
      if (!isPersistentWorkerMode(prefetchThreadMode) || workerShutdownRequested) {
        return;
      }
      if (prefetchWorker != null) {
        if (PREFETCH_THREAD_MODE_WORKER_SEMAPHORE.equals(prefetchThreadMode)
            && activeEntry == null && workerCanClaim && !pending.isEmpty()
            && !pending.get(0).request.alreadyDecoded) {
          releaseSemaphoreWakeLocked();
        }
        return;
      }
      prefetchWorker = startPreparationThread(new Runnable() {
        @Override
        public void run() {
          runPrefetchWorker();
        }
      });
    }
  }

  private static void failNextPendingWorkerStart() {
    final Entry failed;
    synchronized (LOCK) {
      if (activeEntry != null || pending.isEmpty()) {
        return;
      }
      failed = pending.remove(0);
      activateEntryLocked(failed, false);
    }
    finish(failed, FAILED);
  }

  private static void runPrefetchWorker() {
    try {
      while (true) {
        Entry entry = null;
        int sleepMs = 0;
        boolean waitOnSemaphore = false;
        synchronized (LOCK) {
          if (workerShutdownRequested || !isPersistentWorkerMode(prefetchThreadMode)) {
            return;
          }
          if (activeEntry == null && workerCanClaim && !pending.isEmpty()
              && !pending.get(0).request.alreadyDecoded) {
            entry = pending.remove(0);
            activateEntryLocked(entry, false);
          } else if (PREFETCH_THREAD_MODE_WORKER_SEMAPHORE.equals(prefetchThreadMode)) {
            workerWaitingOnSemaphore = true;
            waitOnSemaphore = true;
          } else {
            sleepMs = prefetchWorkerSleepMs;
          }
        }
        if (entry != null) {
          decode(entry);
        } else if (waitOnSemaphore) {
          awaitSemaphoreWake();
        } else {
          sleepWorkerPoll(sleepMs);
        }
      }
    } finally {
      synchronized (LOCK) {
        if (prefetchWorker == Thread.currentThread()) {
          prefetchWorker = null;
          workerWaitingOnSemaphore = false;
        }
        if (prefetchWorker == null) {
          workerShutdownRequested = false;
        }
      }
    }
  }

  private static void awaitSemaphoreWake() {
    workerWakeSemaphore.acquireUninterruptibly();
    synchronized (LOCK) {
      if (Image.diagnosticAccountingEnabledForTest()) {
        workerSemaphoreAcquireCount++;
      }
      workerWakePending = false;
      workerWaitingOnSemaphore = false;
      if (!workerShutdownRequested && Image.diagnosticAccountingEnabledForTest()) {
        workerSemaphoreWakeCount++;
      }
    }
  }

  private static void releaseSemaphoreWakeLocked() {
    if (!workerWaitingOnSemaphore || workerWakePending) {
      return;
    }
    workerWakePending = true;
    if (Image.diagnosticAccountingEnabledForTest()) {
      workerSemaphoreReleaseCount++;
    }
    workerWakeSemaphore.release();
  }

  private static boolean isPersistentWorkerMode(String mode) {
    return PREFETCH_THREAD_MODE_WORKER.equals(mode)
        || PREFETCH_THREAD_MODE_WORKER_SEMAPHORE.equals(mode);
  }

  private static void sleepWorkerPoll(int sleepMs) {
    final boolean accounting = Image.diagnosticAccountingEnabledForTest();
    final long sleepStartNs = accounting ? System.nanoTime() : 0;
    if (accounting) {
      synchronized (LOCK) {
        if (Image.diagnosticAccountingEnabledForTest()) {
          workerPollCount++;
          workerSleepRequestedNs += (long) sleepMs * 1000000L;
        }
      }
    }
    Vm.sleep(sleepMs);
    if (accounting && Image.diagnosticAccountingEnabledForTest()) {
      long sleepElapsedNs = System.nanoTime() - sleepStartNs;
      synchronized (LOCK) {
        if (Image.diagnosticAccountingEnabledForTest()) {
          workerIdleElapsedNs += sleepElapsedNs;
        }
      }
    }
  }

  private static void decode(final Entry entry) {
    final boolean accounting = Image.diagnosticAccountingEnabledForTest();
    final long decodeStartNs = accounting ? System.nanoTime() : 0;
    if (accounting) {
      synchronized (LOCK) {
        if (Image.diagnosticAccountingEnabledForTest()) {
          decodeEntryCount++;
        }
      }
    }
    boolean decodeTimeRecorded = false;
    DetachedCandidate candidate = null;
    try {
      if (entry.request.nativeAvailable) {
        candidate = DetachedCandidate.fromNativeHandle(
            entry.request.image.createNativePreparationHandle(entry.request));
      } else {
        candidate = DetachedCandidate.fromJavaResult(
            entry.request.image.createJavaPreparationResult(entry.request));
      }
      Runnable adoptionHook = beforeAdoptionHookForTest;
      beforeAdoptionHookForTest = null;
      if (adoptionHook != null) {
        adoptionHook.run();
      }
      boolean shouldAdopt;
      synchronized (LOCK) {
        shouldAdopt = entry.state == State.DECODING;
        if (shouldAdopt) {
          entry.state = State.ADOPTING;
        }
      }
      if (!shouldAdopt) {
        candidate.release();
        recordDecodeWorkerTime(accounting, decodeStartNs);
        decodeTimeRecorded = true;
        return;
      }
      recordDecodeWorkerTime(accounting, decodeStartNs);
      decodeTimeRecorded = true;
      final DetachedCandidate detached = candidate;
      postUiAdoption(new Runnable() {
        @Override
        public void run() {
          adopt(entry, detached);
        }
      });
    } catch (Throwable failure) {
      if (!decodeTimeRecorded) {
        recordDecodeWorkerTime(accounting, decodeStartNs);
      }
      if (candidate != null) {
        candidate.release();
      }
      finish(entry, FAILED);
    }
  }

  private static void adoptAlreadyDecoded(final Entry entry) {
    try {
      finishImagePreparation(entry);
      finish(entry, READY);
    } catch (Throwable failure) {
      finish(entry, FAILED);
    }
  }

  private static void adopt(final Entry entry, DetachedCandidate candidate) {
    final boolean accounting = Image.diagnosticAccountingEnabledForTest();
    final long adoptStartNs = accounting ? System.nanoTime() : 0;
    boolean adopted = false;
    try {
      if (!entry.request.image.isPreparationCurrent(entry.request)) {
        candidate.release();
      } else {
        if (candidate.javaResult != null) {
          candidate.adoptJavaResult(entry.request);
        } else {
          entry.request.image.adoptNativePreparationHandle(entry.request, candidate.takeNativeHandle());
        }
        adopted = true;
      }
    } catch (Throwable failure) {
      candidate.release();
    }
    recordAdoptTime(accounting, adoptStartNs);
    if (!adopted) {
      finish(entry, FAILED);
      return;
    }
    try {
      finishImagePreparation(entry);
      finish(entry, READY);
    } catch (Throwable failure) {
      finish(entry, FAILED);
    }
  }

  private static void finish(final Entry entry, int state) {
    final boolean accounting = Image.diagnosticAccountingEnabledForTest();
    final long finishStartNs = accounting ? System.nanoTime() : 0;
    ArrayList<Runnable> callbacks;
    boolean releaseWorkerClaim = false;
    synchronized (LOCK) {
      if (entry.state == State.READY || entry.state == State.FAILED) {
        return;
      }
      entry.state = state == READY ? State.READY : State.FAILED;
      entries.remove(entry);
      if (activeEntry == entry) {
        activeEntry = null;
        if (isPersistentWorkerMode(prefetchThreadMode)) {
          workerCanClaim = false;
          releaseWorkerClaim = true;
        }
      }
      callbacks = new ArrayList<Runnable>(entry.callbacks);
      entry.callbacks.clear();
      recordOutcomeLocked(state == READY ? READY : FAILED, callbacks.size());
    }
    for (int i = 0; i < callbacks.size(); i++) {
      postCompletion(callbacks.get(i));
    }
    recordFinishBookkeepingTime(accounting, finishStartNs, entry);
    if (releaseWorkerClaim) {
      synchronized (LOCK) {
        workerCanClaim = true;
      }
    }
    scheduleNext();
  }

  private static Thread startPreparationThread(final Runnable runnable) {
    final boolean accounting = Image.diagnosticAccountingEnabledForTest();
    if (!accounting) {
      Thread thread = new Thread(runnable);
      thread.start();
      return thread;
    }
    final ThreadStartTiming timing = new ThreadStartTiming();
    Runnable measuredRunnable = new Runnable() {
      @Override
      public void run() {
        if (Image.diagnosticAccountingEnabledForTest()) {
          recordThreadStartLatency(System.nanoTime() - timing.startNs);
        }
        runnable.run();
      }
    };
    long createStartNs = System.nanoTime();
    Thread thread = new Thread(measuredRunnable);
    long createElapsedNs = System.nanoTime() - createStartNs;
    recordThreadCreated(createElapsedNs);
    timing.startNs = System.nanoTime();
    long startCallStartNs = timing.startNs;
    boolean started = false;
    try {
      thread.start();
      started = true;
    } finally {
      if (Image.diagnosticAccountingEnabledForTest()) {
        long startCallElapsedNs = System.nanoTime() - startCallStartNs;
        synchronized (LOCK) {
          threadStartCallNs += startCallElapsedNs;
          if (started) {
            threadStartCount++;
          }
        }
      }
    }
    return thread;
  }

  private static void finishImagePreparation(Entry entry) throws ImageException {
    final boolean accounting = Image.diagnosticAccountingEnabledForTest();
    if (!accounting) {
      entry.request.image.finishPreparation(entry.request, entry.requirement);
      return;
    }
    long startNs = System.nanoTime();
    try {
      entry.request.image.finishPreparation(entry.request, entry.requirement);
    } finally {
      if (Image.diagnosticAccountingEnabledForTest()) {
        long elapsedNs = System.nanoTime() - startNs;
        synchronized (LOCK) {
          finishPreparationNs += elapsedNs;
        }
      }
    }
  }

  private static void postUiAdoption(final Runnable adoption) {
    final boolean accounting = Image.diagnosticAccountingEnabledForTest();
    if (!accounting) {
      postUi(adoption);
      return;
    }
    synchronized (LOCK) {
      if (Image.diagnosticAccountingEnabledForTest()) {
        uiDispatchCount++;
      }
    }
    final long dispatchStartNs = System.nanoTime();
    postUi(new Runnable() {
      @Override
      public void run() {
        if (Image.diagnosticAccountingEnabledForTest()) {
          recordUiDispatchWait(System.nanoTime() - dispatchStartNs);
        }
        adoption.run();
      }
    });
  }

  private static void recordThreadCreated(long elapsedNs) {
    synchronized (LOCK) {
      if (!Image.diagnosticAccountingEnabledForTest()) {
        return;
      }
      threadCreateCount++;
      threadObjectCreateNs += elapsedNs;
    }
  }

  private static void recordThreadStartLatency(long elapsedNs) {
    synchronized (LOCK) {
      if (Image.diagnosticAccountingEnabledForTest()) {
        threadStartLatencyNs += elapsedNs;
      }
    }
  }

  private static void recordDecodeWorkerTime(boolean accounting, long startNs) {
    if (!accounting || !Image.diagnosticAccountingEnabledForTest()) {
      return;
    }
    long elapsedNs = System.nanoTime() - startNs;
    synchronized (LOCK) {
      if (Image.diagnosticAccountingEnabledForTest()) {
        decodeWorkerNs += elapsedNs;
      }
    }
  }

  private static void recordUiDispatchWait(long elapsedNs) {
    synchronized (LOCK) {
      if (Image.diagnosticAccountingEnabledForTest()) {
        uiDispatchWaitNs += elapsedNs;
      }
    }
  }

  private static void recordAdoptTime(boolean accounting, long startNs) {
    if (!accounting || !Image.diagnosticAccountingEnabledForTest()) {
      return;
    }
    long elapsedNs = System.nanoTime() - startNs;
    synchronized (LOCK) {
      if (Image.diagnosticAccountingEnabledForTest()) {
        adoptNs += elapsedNs;
      }
    }
  }

  private static void recordFinishBookkeepingTime(boolean accounting, long startNs, Entry entry) {
    if (!accounting || !Image.diagnosticAccountingEnabledForTest()) {
      return;
    }
    long endNs = System.nanoTime();
    synchronized (LOCK) {
      if (!Image.diagnosticAccountingEnabledForTest()) {
        return;
      }
      finishBookkeepingNs += endNs - startNs;
      if (entry.preparationAccountingEnabled) {
        preparationEntryTotalNs += endNs - entry.preparationStartNs;
        entry.preparationAccountingEnabled = false;
      }
    }
  }

  private static void recordOutcome(int state, long count) {
    synchronized (LOCK) {
      recordOutcomeLocked(state, count);
    }
  }

  private static void recordOutcomeLocked(int state, long count) {
    if (!Image.diagnosticAccountingEnabledForTest()) {
      return;
    }
    if (state == READY) {
      readyCount += count;
    } else if (state == NOT_PREFETCHABLE) {
      notPrefetchableCount += count;
    } else {
      failedCount += count;
    }
  }

  private static void postCompletion(final Runnable callback) {
    if (callback == null) {
      return;
    }
    postUi(callback);
  }

  private static void postUi(final Runnable runnable) {
    MainWindow mainWindow = MainWindow.getMainWindow();
    if (runUiInlineForTest || MainWindow.isMainThread() || mainWindow == null) {
      runnable.run();
    } else {
      mainWindow.runOnMainThread(runnable, false);
    }
  }

  private static void postUiAsync(final Runnable runnable) {
    MainWindow mainWindow = MainWindow.getMainWindow();
    if (mainWindow == null) {
      runnable.run();
    } else {
      mainWindow.runOnMainThread(runnable, false);
    }
  }

  static final class JavaResult {
    final ImageBacking backing;
    final int width;
    final int height;
    final int denominator;

    JavaResult(ImageBacking backing, int width, int height, int denominator) {
      this.backing = backing;
      this.width = width;
      this.height = height;
      this.denominator = denominator;
    }
  }

  static final class DetachedCandidate {
    JavaResult javaResult;
    long nativeHandle;

    static DetachedCandidate fromJavaResult(JavaResult result) {
      DetachedCandidate candidate = new DetachedCandidate();
      candidate.javaResult = result;
      return candidate;
    }

    static DetachedCandidate fromNativeHandle(long handle) {
      DetachedCandidate candidate = new DetachedCandidate();
      candidate.nativeHandle = handle;
      return candidate;
    }

    long takeNativeHandle() {
      long handle = nativeHandle;
      nativeHandle = 0;
      return handle;
    }

    void adoptJavaResult(Request request) throws ImageException {
      request.image.adoptJavaPreparationResult(request, javaResult);
      javaResult = null;
    }

    void release() {
      if (javaResult != null && javaResult.backing instanceof NativeImageBacking) {
        ((NativeImageBacking) javaResult.backing).release();
      }
      javaResult = null;
      if (nativeHandle != 0) {
        NativeImageBacking.releaseDetachedNative(nativeHandle);
        nativeHandle = 0;
      }
    }
  }

  private static final class Entry {
    final Request request;
    final ArrayList<Runnable> callbacks = new ArrayList<Runnable>();
    int requirement;
    int state = State.QUEUED;
    long preparationStartNs;
    boolean preparationAccountingEnabled;

    Entry(Request request, int requirement) {
      this.request = request;
      this.requirement = requirement;
    }
  }

  private static final class ThreadStartTiming {
    long startNs;
  }

  private static final class State {
    static final int QUEUED = 0;
    static final int DECODING = 1;
    static final int ADOPTING = 2;
    static final int READY = 3;
    static final int FAILED = 4;

    private State() {
    }
  }

  static final class Request {
    final Image image;
    final ImagePipeline pipeline;
    final EncodedImageSource source;
    final double destinationScale;
    final long scaleBits;
    final long sourceContentIdentity;
    final int targetWidth;
    final int targetHeight;
    final int denominator;
    final boolean alreadyDecoded;
    final boolean nativeAvailable;
    final int requirement;
    final int optimizationMask;
    int status;

    Request(Image image, ImagePipeline pipeline, EncodedImageSource source, double destinationScale,
        long scaleBits, long sourceContentIdentity, int targetWidth, int targetHeight, int denominator,
        boolean alreadyDecoded, boolean nativeAvailable, int requirement, int optimizationMask, int status) {
      this.image = image;
      this.pipeline = pipeline;
      this.source = source;
      this.destinationScale = destinationScale;
      this.scaleBits = scaleBits;
      this.sourceContentIdentity = sourceContentIdentity;
      this.targetWidth = targetWidth;
      this.targetHeight = targetHeight;
      this.denominator = denominator;
      this.alreadyDecoded = alreadyDecoded;
      this.nativeAvailable = nativeAvailable;
      this.requirement = requirement;
      this.optimizationMask = optimizationMask;
      this.status = status;
    }
  }
}
