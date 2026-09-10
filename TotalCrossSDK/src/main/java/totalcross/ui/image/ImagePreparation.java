// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.ArrayList;

import totalcross.ui.MainWindow;
import totalcross.util.concurrent.Lock;

/** Internal serialized coordinator for detached display preparation. */
final class ImagePreparation {
  static final int READY = 0;
  static final int FAILED = 1;
  static final int NOT_PREFETCHABLE = 2;

  private static final Lock LOCK = new Lock();
  private static final ArrayList<Entry> entries = new ArrayList<Entry>();
  private static final ArrayList<Entry> pending = new ArrayList<Entry>();
  private static Entry activeEntry;
  private static volatile Runnable beforeAdoptionHookForTest;
  private static volatile boolean runUiInlineForTest;
  private static long requestCount;
  private static long readyCount;
  private static long failedCount;
  private static long notPrefetchableCount;

  private ImagePreparation() {
  }

  static void resetAccountingForTest() {
    synchronized (LOCK) {
      requestCount = 0;
      readyCount = 0;
      failedCount = 0;
      notPrefetchableCount = 0;
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
      requestCount++;
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
    synchronized (LOCK) {
      if (activeEntry != null || pending.isEmpty()) {
        return;
      }
      entry = pending.remove(0);
      activeEntry = entry;
      alreadyDecoded = entry.request.alreadyDecoded;
      entry.state = alreadyDecoded ? State.ADOPTING : State.DECODING;
    }
    if (alreadyDecoded) {
      try {
        postUi(new Runnable() {
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
    try {
      new Thread(new Runnable() {
        @Override
        public void run() {
          decode(entry);
        }
      }).start();
    } catch (Throwable failure) {
      finish(entry, FAILED);
    }
  }

  private static void decode(final Entry entry) {
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
      synchronized (LOCK) {
        if (entry.state != State.DECODING) {
          candidate.release();
          return;
        }
        entry.state = State.ADOPTING;
      }
      final DetachedCandidate detached = candidate;
      postUi(new Runnable() {
        @Override
        public void run() {
          adopt(entry, detached);
        }
      });
    } catch (Throwable failure) {
      if (candidate != null) {
        candidate.release();
      }
      finish(entry, FAILED);
    }
  }

  private static void adoptAlreadyDecoded(final Entry entry) {
    try {
      entry.request.image.finishPreparation(entry.request, entry.requirement);
      finish(entry, READY);
    } catch (Throwable failure) {
      finish(entry, FAILED);
    }
  }

  private static void adopt(final Entry entry, DetachedCandidate candidate) {
    try {
      if (!entry.request.image.isPreparationCurrent(entry.request)) {
        candidate.release();
        finish(entry, FAILED);
        return;
      }
      if (candidate.javaResult != null) {
        entry.request.image.adoptJavaPreparationResult(entry.request, candidate.javaResult);
        candidate.javaResult = null;
      } else {
        entry.request.image.adoptNativePreparationHandle(entry.request, candidate.takeNativeHandle());
      }
      entry.request.image.finishPreparation(entry.request, entry.requirement);
      finish(entry, READY);
    } catch (Throwable failure) {
      candidate.release();
      finish(entry, FAILED);
    }
  }

  private static void finish(final Entry entry, int state) {
    ArrayList<Runnable> callbacks;
    synchronized (LOCK) {
      if (entry.state == State.READY || entry.state == State.FAILED) {
        return;
      }
      entry.state = state == READY ? State.READY : State.FAILED;
      entries.remove(entry);
      if (activeEntry == entry) {
        activeEntry = null;
      }
      callbacks = new ArrayList<Runnable>(entry.callbacks);
      entry.callbacks.clear();
      recordOutcomeLocked(state == READY ? READY : FAILED, callbacks.size());
    }
    for (int i = 0; i < callbacks.size(); i++) {
      postCompletion(callbacks.get(i));
    }
    scheduleNext();
  }

  private static void recordOutcome(int state, long count) {
    synchronized (LOCK) {
      recordOutcomeLocked(state, count);
    }
  }

  private static void recordOutcomeLocked(int state, long count) {
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

  private static final class DetachedCandidate {
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

    Entry(Request request, int requirement) {
      this.request = request;
      this.requirement = requirement;
    }
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
