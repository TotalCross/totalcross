// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.ArrayList;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Internal single-worker coordinator for detached display preparation. */
final class ImagePreparation {
  static final int READY = 0;
  static final int FAILED = 1;
  static final int NOT_PREFETCHABLE = 2;

  private static final Object LOCK = new Object();
  private static final ArrayList<Entry> entries = new ArrayList<Entry>();
  private static final ArrayList<Work> workQueue = new ArrayList<Work>();
  private static Thread worker;

  private ImagePreparation() {
  }

  static void request(final Image image, final double destinationScale, final Runnable onComplete) {
    if (image == null) {
      postCompletion(onComplete);
      return;
    }

    final Request request;
    try {
      request = image.createPreparationRequest(destinationScale, NativeImageBacking.isAvailable());
    } catch (Throwable failure) {
      postCompletion(onComplete);
      return;
    }
    if (request.status != -1) {
      postCompletion(onComplete);
      return;
    }
    Entry entry = null;
    boolean enqueue = false;
    synchronized (LOCK) {
      entry = findEntry(request);
      if (entry == null) {
        entry = new Entry(request);
        entries.add(entry);
        entry.callbacks.add(onComplete);
        if (request.alreadyDecoded) {
          entry.state = State.ADOPTING;
          enqueue = true;
        } else {
          entry.state = State.QUEUED;
          workQueue.add(new Work(entry));
          ensureWorkerLocked();
        }
      } else if (entry.state == State.QUEUED || entry.state == State.DECODING
          || entry.state == State.ADOPTING) {
        entry.callbacks.add(onComplete);
      } else {
        postCompletion(onComplete);
      }
    }
    if (enqueue) {
      postUi(new Runnable() {
        @Override
        public void run() {
          adoptAlreadyDecoded(request);
        }
      });
    }
  }

  private static Entry findEntry(Request request) {
    for (int i = entries.size() - 1; i >= 0; i--) {
      Entry entry = entries.get(i);
      if (entry.request.image == request.image && entry.request.pipeline == request.pipeline
          && entry.request.scaleBits == request.scaleBits
          && entry.request.sourceGeneration == request.sourceGeneration) {
        return entry;
      }
    }
    return null;
  }

  private static void ensureWorkerLocked() {
    if (worker != null) {
      return;
    }
    worker = new Thread(new Runnable() {
      @Override
      public void run() {
        workerLoop();
      }
    });
    worker.start();
  }

  private static void workerLoop() {
    while (true) {
      Work work = null;
      synchronized (LOCK) {
        if (!workQueue.isEmpty()) {
          work = workQueue.remove(0);
          work.entry.state = State.DECODING;
        }
      }
      if (work != null) {
        decode(work.entry);
      } else {
        Vm.safeSleep(20);
      }
    }
  }

  private static void decode(final Entry entry) {
    JavaResult javaResult = null;
    long nativeHandle = 0;
    try {
      if (entry.request.nativeAvailable) {
        nativeHandle = entry.request.image.createNativePreparationHandle(entry.request);
      } else {
        javaResult = entry.request.image.createJavaPreparationResult(entry.request);
      }
      final JavaResult decoded = javaResult;
      final long decodedNativeHandle = nativeHandle;
      synchronized (LOCK) {
        if (entry.state != State.DECODING) {
          if (decodedNativeHandle != 0) {
            NativeImageBacking.releaseDetachedNative(decodedNativeHandle);
          }
          return;
        }
        entry.state = State.ADOPTING;
      }
      postUi(new Runnable() {
        @Override
        public void run() {
          adopt(entry, decoded, decodedNativeHandle);
        }
      });
    } catch (Throwable failure) {
      if (nativeHandle != 0) {
        NativeImageBacking.releaseDetachedNative(nativeHandle);
      }
      finish(entry, FAILED);
    }
  }

  private static void adoptAlreadyDecoded(final Request request) {
    Entry entry;
    synchronized (LOCK) {
      entry = findEntry(request);
    }
    if (entry == null) {
      return;
    }
    try {
      if (request.image.sourceDecodeGenerationForPreparation(request) != request.sourceGeneration) {
        finish(entry, FAILED);
        return;
      }
      request.image.finishPreparation(request);
      finish(entry, READY);
    } catch (Throwable failure) {
      finish(entry, FAILED);
    }
  }

  private static void adopt(final Entry entry, JavaResult javaResult, long nativeHandle) {
    try {
      if (entry.request.image.sourceDecodeGenerationForPreparation(entry.request)
          != entry.request.sourceGeneration) {
        if (nativeHandle != 0) {
          NativeImageBacking.releaseDetachedNative(nativeHandle);
        }
        finish(entry, FAILED);
        return;
      }
      if (javaResult != null) {
        entry.request.image.adoptJavaPreparationResult(entry.request, javaResult);
      } else {
        entry.request.image.adoptNativePreparationHandle(entry.request, nativeHandle);
      }
      nativeHandle = 0;
      entry.request.image.finishPreparation(entry.request);
      finish(entry, READY);
    } catch (Throwable failure) {
      if (nativeHandle != 0) {
        NativeImageBacking.releaseDetachedNative(nativeHandle);
      }
      finish(entry, FAILED);
    }
  }

  private static void finish(final Entry entry, int state) {
    ArrayList<Runnable> callbacks;
    synchronized (LOCK) {
      entry.state = state == READY ? State.READY : State.FAILED;
      callbacks = new ArrayList<Runnable>(entry.callbacks);
      entry.callbacks.clear();
    }
    for (int i = 0; i < callbacks.size(); i++) {
      postCompletion(callbacks.get(i));
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
    if (mainWindow == null) {
      runnable.run();
    } else {
      mainWindow.runOnMainThread(runnable, false);
    }
  }

  private static final class Work {
    final Entry entry;

    Work(Entry entry) {
      this.entry = entry;
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

  private static final class Entry {
    final Request request;
    final ArrayList<Runnable> callbacks = new ArrayList<Runnable>();
    int state = State.QUEUED;

    Entry(Request request) {
      this.request = request;
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
    final long sourceGeneration;
    final int targetWidth;
    final int targetHeight;
    final int denominator;
    final boolean alreadyDecoded;
    final boolean nativeAvailable;
    final int status;

    Request(Image image, ImagePipeline pipeline, EncodedImageSource source, double destinationScale,
        long scaleBits, long sourceGeneration, int targetWidth, int targetHeight, int denominator,
        boolean alreadyDecoded, boolean nativeAvailable, int status) {
      this.image = image;
      this.pipeline = pipeline;
      this.source = source;
      this.destinationScale = destinationScale;
      this.scaleBits = scaleBits;
      this.sourceGeneration = sourceGeneration;
      this.targetWidth = targetWidth;
      this.targetHeight = targetHeight;
      this.denominator = denominator;
      this.alreadyDecoded = alreadyDecoded;
      this.nativeAvailable = nativeAvailable;
      this.status = status;
    }
  }
}
