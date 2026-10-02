// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.sys.runtime.ImagePrefetchWorkerTestSupport;
import totalcross.ui.MainWindow;

class ImagePreparationSchedulerTest {
  @BeforeAll
  static void initializeRuntime() {
    new tc.simulator.Launcher();
    if (MainWindow.getMainWindow() == null) {
      new MainWindow();
    }
  }

  @AfterEach
  void cleanupWorker() throws Exception {
    if (!ImagePreparationScheduler.idleForTest()) {
      awaitSchedulerIdle();
    }
    ImagePreparationSchedulerTestSupport.shutdownAndReset();
    ImagePrefetchWorkerTestSupport.restore();
  }

  @Test
  void semaphoreWorkerStartsLazilyAndSerializesRequestsThroughUiAdoption() throws Exception {
    awaitSchedulerIdle();
    ImagePrefetchWorkerTestSupport.useSemaphoreWorker();
    assertEquals("NOT_STARTED", ImagePreparationScheduler.workerLifecycleForTest());
    assertNull(ImagePreparationScheduler.processWorkerForTest());

    final Thread uiThread = Thread.currentThread();
    Image first = lazyImage(jpeg(96, 64));
    Image second = lazyImage(jpeg(96, 64));
    Image third = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    final ImagePreparationRequest firstRequest = first.captureDisplayPreparationRequest(scale, 101L);
    final ImagePreparationRequest secondRequest = second.captureDisplayPreparationRequest(scale, 102L);
    final ImagePreparationRequest thirdRequest = third.captureDisplayPreparationRequest(scale, 103L);
    final ArrayList<Integer> order = new ArrayList<Integer>();
    final Thread[] callbackThreads = new Thread[4];
    final int[] maxActive = {0};
    Image.resetImageOperationAccountingForTest();

    ImagePreparationScheduler.submit(firstRequest, new Runnable() {
      @Override
      public void run() {
        callbackThreads[0] = Thread.currentThread();
        maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
        assertEquals(1, ImagePreparationScheduler.preparationStartCountForTest(),
            "the next decode must wait until this adoption and callback finish");
        order.add(1);
        ImagePreparationScheduler.submit(thirdRequest, new Runnable() {
          @Override
          public void run() {
            callbackThreads[3] = Thread.currentThread();
            maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
            order.add(3);
          }
        });
      }
    });
    ImagePreparationScheduler.submit(firstRequest, new Runnable() {
      @Override
      public void run() {
        callbackThreads[1] = Thread.currentThread();
        maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
        order.add(11);
      }
    });
    ImagePreparationScheduler.submit(secondRequest, new Runnable() {
      @Override
      public void run() {
        callbackThreads[2] = Thread.currentThread();
        maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
        assertEquals(2, ImagePreparationScheduler.preparationStartCountForTest(),
            "the third decode must wait until the second adoption and callbacks finish");
        order.add(2);
      }
    });

    assertEquals(1, ImagePreparationScheduler.activeCountForTest());
    assertEquals(1, ImagePreparationScheduler.queueDepthForTest());
    assertEquals(1, ImagePreparationScheduler.semaphoreWakeCountForTest(),
        "deduplication and queueing must not release extra permits");
    awaitWithoutUi(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return "WAITING_ADOPTION".equals(ImagePreparationScheduler.activeStateForTest());
      }
    });
    Thread worker = ImagePreparationScheduler.processWorkerForTest();
    assertNotNull(worker);
    assertEquals(1, ImagePreparationScheduler.preparationStartCountForTest());
    assertEquals(1, Image.targetedDecodeInvocationCountForTest());
    assertEquals(1, ImagePreparationScheduler.activeCountForTest());

    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return order.size() == 4 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertEquals(1, order.get(0));
    assertEquals(11, order.get(1));
    assertEquals(2, order.get(2));
    assertEquals(3, order.get(3));
    for (Thread callbackThread : callbackThreads) {
      assertSame(uiThread, callbackThread);
    }
    assertEquals(1, maxActive[0]);
    assertEquals(3, Image.targetedDecodeInvocationCountForTest());
    assertEquals(1, ImagePreparationScheduler.processWorkerStartCountForTest());
    assertEquals(3, ImagePreparationScheduler.semaphoreWakeCountForTest());
    assertSame(worker, ImagePreparationScheduler.processWorkerForTest());
    assertEquals("RUNNING_OR_BLOCKED", ImagePreparationScheduler.workerLifecycleForTest());
    awaitWorkerBlocked(worker);
  }

  @Test
  void readyAndNotPrefetchableRequestsDoNotCreateOrWakeWorker() throws Exception {
    awaitSchedulerIdle();
    ImagePrefetchWorkerTestSupport.useSemaphoreWorker();
    Image image = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest initial = image.captureDisplayPreparationRequest(scale, 104L);
    PreparedImageResult prepared = initial.prototype.prepareDetachedForDisplay(initial);
    assertEquals(ImagePreparationScheduler.TerminalState.READY,
        image.adoptPreparedForDisplay(initial, prepared));
    prepared.releaseDetachedEncodedSource();
    final ImagePreparationRequest ready = image.captureDisplayPreparationRequest(scale, 104L);
    assertTrue(image.isDisplayPreparationReady(ready));
    final int[] callbacks = {0};

    ImagePreparationScheduler.submit(ready, new Runnable() {
      @Override
      public void run() { callbacks[0]++; }
    });
    ImagePreparationScheduler.submit(null, new Runnable() {
      @Override
      public void run() { callbacks[0]++; }
    });

    assertEquals(2, callbacks[0]);
    assertNull(ImagePreparationScheduler.processWorkerForTest());
    assertEquals(0, ImagePreparationScheduler.processWorkerStartCountForTest());
    assertEquals(0, ImagePreparationScheduler.semaphoreWakeCountForTest());
  }

  @Test
  void semaphoreWorkerStartFailureIsTransientAndExplicitRetryStartsWorker() throws Exception {
    awaitSchedulerIdle();
    ImagePrefetchWorkerTestSupport.useSemaphoreWorker();
    Image image = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest first = image.captureDisplayPreparationRequest(scale, 105L);
    final int[] completions = {0};
    ImagePreparationScheduler.failNextWorkerStartForTest();
    ImagePreparationScheduler.submit(first, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 1 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertNull(first.source.decodeFailure());
    assertNull(first.source.decodedBackingForReuse(first.decodeDenominator));
    assertNull(ImagePreparationScheduler.processWorkerForTest());
    assertEquals(0, ImagePreparationScheduler.semaphoreWakeCountForTest());

    ImagePreparationRequest retry = image.captureDisplayPreparationRequest(scale, 106L);
    ImagePreparationScheduler.submit(retry, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 2 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertTrue(image.isDisplayPreparationReady(retry));
    assertEquals(1, ImagePreparationScheduler.processWorkerStartCountForTest());
    assertEquals(1, ImagePreparationScheduler.semaphoreWakeCountForTest());
  }

  @Test
  void shutdownWakesBlockedWorkerAndResetAllowsFreshProcessWorker() throws Exception {
    awaitSchedulerIdle();
    ImagePrefetchWorkerTestSupport.useSemaphoreWorker();
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    Image first = lazyImage(jpeg(64, 48));
    ImagePreparationRequest firstRequest = first.captureDisplayPreparationRequest(scale, 107L);
    final int[] completions = {0};
    ImagePreparationScheduler.submit(firstRequest, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 1 && ImagePreparationScheduler.idleForTest();
      }
    });
    Thread firstWorker = ImagePreparationScheduler.processWorkerForTest();
    awaitWorkerBlocked(firstWorker);

    ImagePreparationSchedulerTestSupport.shutdownAndReset();
    assertFalse(firstWorker.isAlive());
    assertEquals("NOT_STARTED", ImagePreparationScheduler.workerLifecycleForTest());

    Image second = lazyImage(jpeg(64, 48));
    ImagePreparationRequest secondRequest = second.captureDisplayPreparationRequest(scale, 108L);
    ImagePreparationScheduler.submit(secondRequest, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 2 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertTrue(ImagePreparationScheduler.processWorkerForTest() != firstWorker);
    assertEquals(1, ImagePreparationScheduler.processWorkerStartCountForTest());
  }

  @Test
  void semaphoreWorkDiscoveryBlocksWithoutSleepOrPolling() throws Exception {
    Path schedulerSource = Path.of("src/main/java/totalcross/ui/image/ImagePreparationScheduler.java");
    String source = Files.readString(schedulerSource);
    String workerLoop = methodBody(source, "private static void runSemaphoreWorker(");
    assertTrue(workerLoop.contains("wake.acquireUninterruptibly()"));
    assertFalse(workerLoop.contains("Thread.sleep"));
    assertFalse(workerLoop.contains("Vm.sleep"));
    assertFalse(workerLoop.contains("tryAcquire"));
    assertFalse(workerLoop.contains("availablePermits"));
    assertFalse(workerLoop.contains("Thread.yield"));
    assertFalse(workerLoop.contains("LockSupport"));
  }

  @Test
  void workerStartFailureIsTransientAndCanRetry() throws Exception {
    awaitSchedulerIdle();
    Image image = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest first = image.captureDisplayPreparationRequest(scale, 17L);
    final int[] completions = {0};
    ImagePreparationScheduler.failNextWorkerStartForTest();
    ImagePreparationScheduler.submit(first, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 1 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertNull(first.source.decodeFailure());
    assertNull(first.source.decodedBackingForReuse(first.decodeDenominator));

    ImagePreparationRequest retry = image.captureDisplayPreparationRequest(scale, 18L);
    ImagePreparationScheduler.submit(retry, new Runnable() {
      @Override
      public void run() { completions[0]++; }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return completions[0] == 2 && ImagePreparationScheduler.idleForTest();
      }
    });
    assertTrue(image.isDisplayPreparationReady(retry));
  }

  @Test
  void globalFifoStaysSingleActiveThroughAdoptionAndAllowsCallbackReentrancy() throws Exception {
    awaitSchedulerIdle();
    final Thread uiThread = Thread.currentThread();
    Image first = lazyImage(jpeg(96, 64));
    Image second = lazyImage(jpeg(96, 64));
    Image third = lazyImage(jpeg(96, 64));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest firstRequest = first.captureDisplayPreparationRequest(scale, 10L);
    ImagePreparationRequest secondRequest = second.captureDisplayPreparationRequest(scale, 11L);
    ImagePreparationRequest thirdRequest = third.captureDisplayPreparationRequest(scale, 12L);
    final ImagePreparationRequest queuedSecond = secondRequest;
    final ImagePreparationRequest queuedThird = thirdRequest;
    final ImagePreparationRequest firstPending = firstRequest;
    final ArrayList<Integer> order = new ArrayList<Integer>();
    final Thread[] callbackThreads = new Thread[4];
    final int[] maxActive = {0};
    Image.resetImageOperationAccountingForTest();

    try {
      ImagePreparationScheduler.submit(firstPending, new Runnable() {
        @Override
        public void run() {
          callbackThreads[0] = Thread.currentThread();
          maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
          order.add(1);
          ImagePreparationScheduler.submit(queuedThird, new Runnable() {
            @Override
            public void run() {
              callbackThreads[3] = Thread.currentThread();
              maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
              order.add(3);
            }
          });
        }
      });
      ImagePreparationScheduler.submit(firstPending, new Runnable() {
        @Override
        public void run() {
          callbackThreads[1] = Thread.currentThread();
          maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
          order.add(11);
        }
      });
      assertEquals(0, ImagePreparationScheduler.queueDepthForTest());
      ImagePreparationScheduler.submit(queuedSecond, new Runnable() {
        @Override
        public void run() {
          callbackThreads[2] = Thread.currentThread();
          maxActive[0] = Math.max(maxActive[0], ImagePreparationScheduler.activeCountForTest());
          order.add(2);
        }
      });
      assertEquals(1, ImagePreparationScheduler.activeCountForTest());
      assertEquals(1, ImagePreparationScheduler.queueDepthForTest());
      pumpUntil(new CompletionCheck() {
        @Override
        public boolean isComplete() {
          return order.size() == 4 && ImagePreparationScheduler.idleForTest();
        }
      });

      assertEquals(1, order.get(0));
      assertEquals(11, order.get(1));
      assertEquals(2, order.get(2));
      assertEquals(3, order.get(3));
      assertSame(uiThread, callbackThreads[0]);
      assertSame(uiThread, callbackThreads[1]);
      assertSame(uiThread, callbackThreads[2]);
      assertSame(uiThread, callbackThreads[3]);
      assertEquals(1, maxActive[0]);
      assertEquals(3, Image.targetedDecodeInvocationCountForTest());
      assertEquals(0, ImagePreparationScheduler.activeCountForTest());
    } finally {
      pumpUntil(new CompletionCheck() {
        @Override
        public boolean isComplete() {
          return ImagePreparationScheduler.idleForTest();
        }
      });
    }
  }

  @Test
  void callbackExceptionDoesNotPreventNextFifoRequest() throws Exception {
    awaitSchedulerIdle();
    Image first = lazyImage(jpeg(64, 48));
    Image second = lazyImage(jpeg(64, 48));
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ImagePreparationRequest firstRequest = first.captureDisplayPreparationRequest(scale, 30L);
    ImagePreparationRequest secondRequest = second.captureDisplayPreparationRequest(scale, 31L);
    final boolean[] secondCompleted = {false};
    ImagePreparationScheduler.submit(firstRequest, new Runnable() {
      @Override
      public void run() { throw new IllegalStateException("expected callback failure"); }
    });
    ImagePreparationScheduler.submit(secondRequest, new Runnable() {
      @Override
      public void run() { secondCompleted[0] = true; }
    });

    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
    MainWindow mainWindow = MainWindow.getMainWindow();
    while ((!secondCompleted[0] || !ImagePreparationScheduler.idleForTest())
        && System.nanoTime() < deadline) {
      try {
        mainWindow._onTimerTick(false);
      } catch (IllegalStateException expectedCallbackFailure) {
        assertEquals("expected callback failure", expectedCallbackFailure.getMessage());
      }
      Thread.sleep(4);
    }
    assertTrue(secondCompleted[0], "the FIFO must continue after a callback throws");
    assertTrue(ImagePreparationScheduler.idleForTest());
  }

  @Test
  void pendingRegistryIsBoundedAndOverflowCanRetryLater() throws Exception {
    awaitSchedulerIdle();
    int limit = ImagePreparationScheduler.pendingLimitForTest();
    int requestCount = limit + 2;
    byte[] encoded = jpeg(32, 24);
    double scale = MainWindow.getMainWindow().getGraphics().getContentScale();
    ArrayList<ImagePreparationRequest> requests = new ArrayList<ImagePreparationRequest>(requestCount);
    for (int i = 0; i < requestCount; i++) {
      requests.add(lazyImage(encoded).captureDisplayPreparationRequest(scale, 20L + i));
    }

    final int[] callbacks = {0};
    for (ImagePreparationRequest request : requests) {
      ImagePreparationScheduler.submit(request, new Runnable() {
        @Override
        public void run() {
          callbacks[0]++;
        }
      });
    }

    assertEquals(limit, ImagePreparationScheduler.pendingCountForTest());
    assertEquals(limit - 1, ImagePreparationScheduler.queueDepthForTest());
    assertEquals(1, ImagePreparationScheduler.activeCountForTest());
    assertEquals(2, callbacks[0]);
    for (Field field : Class.forName(ImagePreparationScheduler.class.getName() + "$ReadyEntry")
        .getDeclaredFields()) {
      assertFalse(Image.class.isAssignableFrom(field.getType()),
          "ready metadata must not retain a second materialized Image variant");
    }
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return callbacks[0] == requestCount && ImagePreparationScheduler.idleForTest();
      }
    }, 30);
    assertEquals(ImagePreparationScheduler.readyLimitForTest(), ImagePreparationScheduler.readyCountForTest());
    assertTrue(ImagePreparationScheduler.readyMetadataRetainsNoPrototypeForTest(),
        "ready metadata must not retain worker prototype Images");

    ImagePreparationScheduler.submit(requests.get(limit), new Runnable() {
      @Override
      public void run() {
        callbacks[0]++;
      }
    });
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return callbacks[0] == requestCount + 1 && ImagePreparationScheduler.idleForTest();
      }
    });
  }

  private static Image lazyImage(byte[] encoded) throws Exception {
    Path path = writeJpeg(encoded);
    try {
      return Image.getJpegScaled(path.toString(), 1, 2);
    } finally {
      Files.deleteIfExists(path);
    }
  }

  private static Path writeJpeg(byte[] encoded) throws Exception {
    Path path = Files.createTempFile("tc-async-image", ".jpg");
    Files.write(path, encoded);
    return path;
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int red = (x * 29 + y * 17) & 0xFF;
        image.setRGB(x, y, (red << 16) | (((red * 3) & 0xFF) << 8) | ((red * 7) & 0xFF));
      }
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
    ImageWriter writer = writers.next();
    try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
      writer.setOutput(output);
      writer.write(null, new IIOImage(image, null, null), writer.getDefaultWriteParam());
    } finally {
      writer.dispose();
    }
    return bytes.toByteArray();
  }

  private static void awaitWithoutUi(CompletionCheck condition) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
    while (!condition.isComplete() && System.nanoTime() < deadline) {
      Thread.sleep(2L);
    }
    assertTrue(condition.isComplete(), "Timed out waiting for the preparation worker");
  }

  private static void awaitWorkerBlocked(Thread worker) throws Exception {
    assertNotNull(worker);
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
    while (worker.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
      Thread.sleep(2L);
    }
    assertEquals(Thread.State.WAITING, worker.getState(), "the idle worker must block in Semaphore acquire");
  }

  private static String methodBody(String source, String signature) {
    int start = source.indexOf(signature);
    assertTrue(start >= 0, "Missing source method " + signature);
    int body = source.indexOf('{', start);
    assertTrue(body >= 0, "Missing method body " + signature);
    int depth = 0;
    for (int i = body; i < source.length(); i++) {
      if (source.charAt(i) == '{') {
        depth++;
      } else if (source.charAt(i) == '}' && --depth == 0) {
        return source.substring(body + 1, i);
      }
    }
    throw new AssertionError("Unclosed method body " + signature);
  }

  private static void awaitSchedulerIdle() throws Exception {
    pumpUntil(new CompletionCheck() {
      @Override
      public boolean isComplete() {
        return ImagePreparationScheduler.idleForTest();
      }
    });
  }

  private static void pumpUntil(CompletionCheck condition) throws Exception {
    pumpUntil(condition, 8);
  }

  private static void pumpUntil(CompletionCheck condition, int timeoutSeconds) throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
    MainWindow mainWindow = MainWindow.getMainWindow();
    while (!condition.isComplete() && System.nanoTime() < deadline) {
      mainWindow._onTimerTick(false);
      Thread.sleep(4);
    }
    assertTrue(condition.isComplete(), "Timed out waiting for image preparation callbacks");
  }

  private interface CompletionCheck {
    boolean isComplete();
  }

}
