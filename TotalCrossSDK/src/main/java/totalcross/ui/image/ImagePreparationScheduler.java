// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import totalcross.sys.runtime.ImageRuntimePolicy;
import totalcross.ui.MainWindow;

/** One process-wide FIFO whose active request remains owned through UI adoption. */
final class ImagePreparationScheduler {
  enum TerminalState {
    READY,
    STALE,
    NOT_PREFETCHABLE,
    DETERMINISTIC_FAILURE,
    TRANSIENT_FAILURE
  }

  private enum RequestState {
    DISCOVERED,
    QUEUED,
    PREPARING,
    WAITING_ADOPTION,
    READY,
    NOT_PREFETCHABLE,
    STALE,
    DETERMINISTIC_FAILURE,
    TRANSIENT_FAILURE
  }

  private enum WorkerLifecycle {
    NOT_STARTED,
    RUNNING_OR_BLOCKED,
    SHUTDOWN
  }

  private static final Object LOCK = new Object();
  private static final int PENDING_LIMIT = 128;
  private static final int READY_LIMIT = 16;
  private static final ArrayList<Work> QUEUE = new ArrayList<Work>();
  private static final ArrayList<Work> PENDING = new ArrayList<Work>();
  private static final ArrayList<ReadyEntry> READY = new ArrayList<ReadyEntry>();
  private static Work active;
  private static boolean failNextWorkerStartForTest;
  private static WorkerLifecycle workerLifecycle = WorkerLifecycle.NOT_STARTED;
  private static Semaphore workerWake;
  private static Semaphore shutdownCompleteForTest;
  private static Thread processWorker;
  private static int processWorkerStartCountForTest;
  private static int semaphoreWakeCountForTest;
  private static int preparationStartCountForTest;

  private ImagePreparationScheduler() {
  }

  static void submit(ImagePreparationRequest request, Runnable completion) {
    if (request == null) {
      ImagePreparationMetrics.notPrefetchable();
      completion.run();
      return;
    }
    if (request.source.decodeFailure() != null) {
      ImagePreparationMetrics.deterministicFailure();
      completion.run();
      return;
    }
    if (request.target.isDisplayPreparationReady(request)) {
      ImagePreparationMetrics.deduplicated();
      ImagePreparationMetrics.ready();
      completion.run();
      return;
    }

    Work toStart = null;
    ReadyEntry readyMetadata = null;
    boolean attachedToPending = false;
    Work queued = null;
    boolean queueFull = false;
    synchronized (LOCK) {
      for (Work pending : PENDING) {
        if (pending.request.equivalentTo(request)) {
          pending.completions.add(completion);
          attachedToPending = true;
          break;
        }
      }
      if (!attachedToPending) {
        readyMetadata = findReadyLocked(request);
      }
      if (!attachedToPending && readyMetadata == null) {
        queued = enqueueLocked(request, completion);
        if (queued == null) {
          queueFull = true;
        } else if (active == null) {
          toStart = activateNextLocked();
        }
      }
    }
    updateQueueGauges();
    if (attachedToPending) {
      ImagePreparationMetrics.deduplicated();
      return;
    }
    if (readyMetadata != null) {
      if (request.target.isDisplayPreparationReady(request)) {
        synchronized (LOCK) {
          touchReadyLocked(readyMetadata);
        }
        ImagePreparationMetrics.deduplicated();
        ImagePreparationMetrics.ready();
        completion.run();
        return;
      }
      synchronized (LOCK) {
        READY.remove(readyMetadata);
        for (Work pending : PENDING) {
          if (pending.request.equivalentTo(request)) {
            pending.completions.add(completion);
            attachedToPending = true;
            break;
          }
        }
        if (!attachedToPending) {
          queued = enqueueLocked(request, completion);
          if (queued == null) {
            queueFull = true;
          } else if (active == null) {
            toStart = activateNextLocked();
          }
        }
      }
      updateQueueGauges();
      if (attachedToPending) {
        ImagePreparationMetrics.deduplicated();
        return;
      }
    }
    if (queueFull) {
      ImagePreparationMetrics.transientFailure();
      completion.run();
      return;
    }
    if (queued != null) {
      ImagePreparationMetrics.enqueued();
    }
    if (toStart != null) {
      start(toStart);
    }
  }

  private static Work enqueueLocked(ImagePreparationRequest request, Runnable completion) {
    if (PENDING.size() >= PENDING_LIMIT) {
      return null;
    }
    Work work = new Work(request, completion);
    work.state = RequestState.QUEUED;
    PENDING.add(work);
    QUEUE.add(work);
    return work;
  }

  private static ReadyEntry findReadyLocked(ImagePreparationRequest request) {
    long generation = request.source.decodedGeneration();
    for (int i = READY.size() - 1; i >= 0; i--) {
      ReadyEntry entry = READY.get(i);
      if (entry.sourceGeneration == generation && entry.request.equivalentExceptSourceGeneration(request)) {
        return entry;
      }
    }
    return null;
  }

  private static void touchReadyLocked(ReadyEntry entry) {
    READY.remove(entry);
    READY.add(entry);
  }

  private static Work activateNextLocked() {
    if (QUEUE.isEmpty()) {
      active = null;
      return null;
    }
    active = QUEUE.remove(0);
    active.state = RequestState.PREPARING;
    return active;
  }

  static int activeCountForTest() {
    synchronized (LOCK) {
      return active == null ? 0 : 1;
    }
  }

  static int queueDepthForTest() {
    synchronized (LOCK) {
      return QUEUE.size();
    }
  }

  static int pendingCountForTest() {
    synchronized (LOCK) {
      return PENDING.size();
    }
  }

  static int pendingLimitForTest() {
    return PENDING_LIMIT;
  }

  static int readyCountForTest() {
    synchronized (LOCK) {
      return READY.size();
    }
  }

  static int readyLimitForTest() {
    return READY_LIMIT;
  }

  static boolean readyMetadataRetainsNoPrototypeForTest() {
    synchronized (LOCK) {
      for (ReadyEntry entry : READY) {
        if (entry.request.prototype != null) {
          return false;
        }
      }
      return true;
    }
  }

  static void failNextWorkerStartForTest() {
    synchronized (LOCK) {
      failNextWorkerStartForTest = true;
    }
  }

  static boolean idleForTest() {
    synchronized (LOCK) {
      return active == null && QUEUE.isEmpty() && PENDING.isEmpty();
    }
  }

  static String workerLifecycleForTest() {
    synchronized (LOCK) {
      return workerLifecycle.name();
    }
  }

  static Thread processWorkerForTest() {
    synchronized (LOCK) {
      return processWorker;
    }
  }

  static int processWorkerStartCountForTest() {
    synchronized (LOCK) {
      return processWorkerStartCountForTest;
    }
  }

  static int semaphoreWakeCountForTest() {
    synchronized (LOCK) {
      return semaphoreWakeCountForTest;
    }
  }

  static int preparationStartCountForTest() {
    synchronized (LOCK) {
      return preparationStartCountForTest;
    }
  }

  static String activeStateForTest() {
    synchronized (LOCK) {
      return active == null ? null : active.state.name();
    }
  }

  static void shutdownSemaphoreWorkerForTest() {
    synchronized (LOCK) {
      if (active != null || !QUEUE.isEmpty() || !PENDING.isEmpty()) {
        throw new IllegalStateException("the image preparation scheduler must be idle before worker shutdown");
      }
      if (workerLifecycle == WorkerLifecycle.NOT_STARTED) {
        return;
      }
      workerLifecycle = WorkerLifecycle.SHUTDOWN;
      shutdownCompleteForTest = new Semaphore(0);
      workerWake.release();
    }
    Semaphore stopped;
    synchronized (LOCK) {
      stopped = shutdownCompleteForTest;
    }
    stopped.acquireUninterruptibly();
  }

  static void resetSemaphoreWorkerForTest() {
    synchronized (LOCK) {
      if (workerLifecycle == WorkerLifecycle.NOT_STARTED) {
        return;
      }
      if (workerLifecycle != WorkerLifecycle.SHUTDOWN || processWorker == null || processWorker.isAlive()) {
        throw new IllegalStateException("the image prefetch worker must exit before test reset");
      }
      processWorker = null;
      workerWake = null;
      shutdownCompleteForTest = null;
      workerLifecycle = WorkerLifecycle.NOT_STARTED;
      processWorkerStartCountForTest = 0;
      semaphoreWakeCountForTest = 0;
      preparationStartCountForTest = 0;
    }
  }

  private static void updateQueueGauges() {
    int depth;
    int activeCount;
    synchronized (LOCK) {
      depth = QUEUE.size();
      activeCount = active == null ? 0 : 1;
    }
    ImagePreparationMetrics.queueState(depth, activeCount);
  }

  private static void start(final Work work) {
    if (work.request.effectivePolicy.prefetchWorker()
        == ImageRuntimePolicy.PrefetchWorkerPolicy.SEMAPHORE_PROCESS_WORKER) {
      startSemaphoreWork(work);
    } else {
      startLegacyWork(work);
    }
  }

  private static void startLegacyWork(final Work work) {
    try {
      synchronized (LOCK) {
        if (active != work || work.dispatched) {
          return;
        }
        work.dispatched = true;
        failWorkerStartForTestLocked();
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          prepareAndPostAdoption(work);
        }
      }).start();
    } catch (RuntimeException | OutOfMemoryError startFailure) {
      finishStartFailureOnUi(work);
    }
  }

  private static void startSemaphoreWork(final Work work) {
    try {
      synchronized (LOCK) {
        if (active != work || work.dispatched) {
          return;
        }
        failWorkerStartForTestLocked();
        if (workerLifecycle == WorkerLifecycle.NOT_STARTED) {
          startProcessWorkerLocked();
        }
        if (workerLifecycle != WorkerLifecycle.RUNNING_OR_BLOCKED || workerWake == null) {
          throw new IllegalStateException("the image prefetch worker is not available");
        }
        work.dispatched = true;
        workerWake.release();
        semaphoreWakeCountForTest++;
      }
    } catch (RuntimeException | OutOfMemoryError startFailure) {
      finishStartFailureOnUi(work);
    }
  }

  private static void failWorkerStartForTestLocked() {
    if (failNextWorkerStartForTest) {
      failNextWorkerStartForTest = false;
      throw new IllegalStateException("Simulated image preparation worker start failure");
    }
  }

  private static void startProcessWorkerLocked() {
    final Semaphore wake = new Semaphore(0);
    final Thread worker = new Thread(new Runnable() {
      @Override
      public void run() {
        runSemaphoreWorker(wake);
      }
    });
    workerWake = wake;
    processWorker = worker;
    workerLifecycle = WorkerLifecycle.RUNNING_OR_BLOCKED;
    try {
      worker.start();
      processWorkerStartCountForTest++;
    } catch (RuntimeException | OutOfMemoryError startFailure) {
      workerWake = null;
      processWorker = null;
      workerLifecycle = WorkerLifecycle.NOT_STARTED;
      throw startFailure;
    }
  }

  private static void runSemaphoreWorker(Semaphore wake) {
    while (true) {
      wake.acquireUninterruptibly();
      Work work;
      synchronized (LOCK) {
        if (workerLifecycle == WorkerLifecycle.SHUTDOWN) {
          Semaphore stopped = shutdownCompleteForTest;
          if (stopped != null) {
            stopped.release();
          }
          return;
        }
        work = active;
        if (work == null || work.state != RequestState.PREPARING
            || work.request.effectivePolicy.prefetchWorker()
                != ImageRuntimePolicy.PrefetchWorkerPolicy.SEMAPHORE_PROCESS_WORKER) {
          continue;
        }
      }
      prepareAndPostAdoption(work);
    }
  }

  private static void prepareAndPostAdoption(final Work work) {
    synchronized (LOCK) {
      preparationStartCountForTest++;
    }
    final PreparedImageResult result = work.request.prototype.prepareDetachedForDisplay(work.request);
    synchronized (LOCK) {
      work.state = RequestState.WAITING_ADOPTION;
    }
    MainWindow mainWindow = MainWindow.getMainWindow();
    if (mainWindow == null) {
      abandonWithoutUi(work, result);
      return;
    }
    mainWindow.runOnMainThread(new Runnable() {
      @Override
      public void run() {
        finishOnUi(work, result);
      }
    }, false);
  }

  private static void finishStartFailureOnUi(final Work work) {
    MainWindow mainWindow = MainWindow.getMainWindow();
    if (mainWindow == null) {
      finishOnUi(work, PreparedImageResult.transientFailure(null));
    } else {
      mainWindow.runOnMainThread(new Runnable() {
        @Override
        public void run() {
          finishOnUi(work, PreparedImageResult.transientFailure(null));
        }
      }, false);
    }
  }

  private static void abandonWithoutUi(Work work, PreparedImageResult result) {
    result.releaseDetachedEncodedSource();
    synchronized (LOCK) {
      PENDING.remove(work);
      if (active == work) {
        active = null;
      }
      QUEUE.clear();
      PENDING.clear();
      READY.clear();
    }
    updateQueueGauges();
  }

  private static void finishOnUi(Work work, PreparedImageResult result) {
    TerminalState terminal;
    try {
      terminal = work.request.target.adoptPreparedForDisplay(work.request, result);
    } catch (OutOfMemoryError allocationFailure) {
      terminal = TerminalState.TRANSIENT_FAILURE;
    } catch (RuntimeException adoptionFailure) {
      terminal = TerminalState.TRANSIENT_FAILURE;
    }
    ArrayList<Runnable> completions;
    Work next;
    long sourceGeneration = work.request.source.decodedGeneration();
    boolean ready = terminal == TerminalState.READY
        && work.request.target.isDisplayPreparationReady(work.request);
    synchronized (LOCK) {
      work.state = stateFor(terminal);
      PENDING.remove(work);
      if (ready) {
        READY.add(new ReadyEntry(work.request.metadataOnly(), sourceGeneration));
        while (READY.size() > READY_LIMIT) {
          READY.remove(0);
        }
      }
      if (active == work) {
        active = null;
      }
      completions = new ArrayList<Runnable>(work.completions);
      next = activateNextLocked();
    }
    recordTerminal(terminal);
    updateQueueGauges();
    result.releaseDetachedEncodedSource();
    invokeCompletions(completions, next);
  }

  private static void recordTerminal(TerminalState state) {
    switch (state) {
    case READY: ImagePreparationMetrics.ready(); break;
    case STALE: ImagePreparationMetrics.stale(); break;
    case DETERMINISTIC_FAILURE: ImagePreparationMetrics.deterministicFailure(); break;
    case TRANSIENT_FAILURE: ImagePreparationMetrics.transientFailure(); break;
    default: break;
    }
  }

  private static RequestState stateFor(TerminalState state) {
    switch (state) {
    case READY: return RequestState.READY;
    case STALE: return RequestState.STALE;
    case NOT_PREFETCHABLE: return RequestState.NOT_PREFETCHABLE;
    case DETERMINISTIC_FAILURE: return RequestState.DETERMINISTIC_FAILURE;
    default: return RequestState.TRANSIENT_FAILURE;
    }
  }

  private static void invokeCompletions(ArrayList<Runnable> completions, Work next) {
    Throwable firstFailure = null;
    try {
      for (Runnable completion : completions) {
        try {
          completion.run();
        } catch (Throwable failure) {
          if (firstFailure == null) {
            firstFailure = failure;
          }
        }
      }
    } finally {
      if (next != null) {
        start(next);
      }
    }
    if (firstFailure instanceof Error) {
      throw (Error) firstFailure;
    }
    if (firstFailure instanceof RuntimeException) {
      throw (RuntimeException) firstFailure;
    }
  }

  private static final class Work {
    final ImagePreparationRequest request;
    final ArrayList<Runnable> completions = new ArrayList<Runnable>();
    RequestState state = RequestState.DISCOVERED;
    boolean dispatched;

    Work(ImagePreparationRequest request, Runnable completion) {
      this.request = request;
      completions.add(completion);
    }
  }

  private static final class ReadyEntry {
    final ImagePreparationRequest request;
    final long sourceGeneration;

    ReadyEntry(ImagePreparationRequest request, long sourceGeneration) {
      this.request = request;
      this.sourceGeneration = sourceGeneration;
    }
  }
}
