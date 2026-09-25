// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.preview.PreviewFrame;
import tc.preview.PreviewFrameSink;
import tc.simulator.Launcher;
import totalcross.ui.event.DragEvent;

class FlickDriverTest {
  private static Launcher runtime;
  private static volatile DriverTestApp app;

  @BeforeAll
  static void startPreviewRuntime() throws Exception {
    PreviewFrameSink sink = new PreviewFrameSink() {
      @Override
      public void present(PreviewFrame frame) {
      }
    };
    runtime = Launcher.startPreviewFrames(DriverTestApp.class.getName(), sink,
        FlickDriverTest.class.getClassLoader(), "/scr", "320x480x32");
    long deadline = System.currentTimeMillis() + 5000;
    while (app == null && System.currentTimeMillis() < deadline) {
      Thread.sleep(10);
    }
    assertNotNull(app);
  }

  @AfterAll
  static void stopPreviewRuntime() {
    if (runtime != null) {
      runtime.close();
    }
  }

  @Test
  void defaultRemainsTimerAtFortyFramesPerSecond() {
    TestScrollable target = newTestScrollable();
    assertEquals(Flick.FRAME_DRIVER_TIMER, target.getFlick().frameDriverForTest());
    assertEquals(40, target.getFlick().frameRate);
    assertNull(target.getFlick().timer);
  }

  @Test
  void timerDriversUseIntegerMillisecondIntervalsAndStopOnce() {
    assertTimerDriver(40, 25);
    assertTimerDriver(60, 16);
  }

  @Test
  void updateDriverRegistersNoTimerEventAndUsesTheSharedAdvancement() {
    TestScrollable target = newTestScrollable();
    FlickBenchmarkSupport.DriverHostRecorder host =
        new FlickBenchmarkSupport.DriverHostRecorder();
    FlickBenchmarkSupport.Recording recording = FlickBenchmarkSupport.startForTest(
        target, Flick.FRAME_DRIVER_UPDATE, 0, host, 1000);
    Flick flick = target.getFlick();

    assertNull(flick.timer);
    assertEquals(0, host.timerAddCount);
    assertEquals(1, host.updateAddCount);
    flick.updateListenerTriggered(16);
    assertEquals(1, recording.frameCount());
    assertTrue(target.scrollY > 0);

    flick.stop(false);
    flick.stop(false);
    assertEquals(0, host.timerRemoveCount);
    assertEquals(1, host.updateRemoveCount);
    assertEquals(1, target.flickEndedCount);
  }

  @Test
  void benchmarkMotionStopsAtTheConfiguredDisplacementAndDriverCannotSwitchWhileActive() {
    TestScrollable target = newTestScrollable();
    FlickBenchmarkSupport.DriverHostRecorder host =
        new FlickBenchmarkSupport.DriverHostRecorder();
    FlickBenchmarkSupport.Recording recording = FlickBenchmarkSupport.startForTest(
        target, Flick.FRAME_DRIVER_UPDATE, 0, host, 3000);
    Flick flick = target.getFlick();
    double initialVelocity = flick.benchmarkInitialVelocityForTest();
    double acceleration = flick.benchmarkAccelerationForTest();
    double duration = FlickBenchmarkSupport.MOTION_DURATION_MS;

    assertEquals(FlickBenchmarkSupport.MOTION_DISPLACEMENT,
        initialVelocity * duration + acceleration * duration * duration / 2.0, 0.000001);
    assertEquals(0.0, initialVelocity + acceleration * duration, 0.000001);
    assertTrue(flick.frameDriverRegisteredForTest());
    assertThrows(IllegalStateException.class,
        () -> flick.configureFrameDriverForBenchmark(Flick.FRAME_DRIVER_TIMER, 40));

    flick.updateListenerTriggered(16);
    assertTrue(recording.complete());
    assertEquals(FlickBenchmarkSupport.MOTION_DISPLACEMENT,
        recording.finalFlickPosition());
    assertEquals(-FlickBenchmarkSupport.MOTION_DISPLACEMENT, target.scrollY);
    assertFalse(flick.frameDriverRegisteredForTest());
    assertEquals(1, host.updateRemoveCount);
    assertEquals(1, target.flickEndedCount);
  }

  private static void assertTimerDriver(int fps, int intervalMs) {
    TestScrollable target = newTestScrollable();
    FlickBenchmarkSupport.DriverHostRecorder host =
        new FlickBenchmarkSupport.DriverHostRecorder();
    FlickBenchmarkSupport.Recording recording = FlickBenchmarkSupport.startForTest(
        target, Flick.FRAME_DRIVER_TIMER, fps, host, 1000);
    Flick flick = target.getFlick();

    assertNotNull(flick.timer);
    assertEquals(1, host.timerAddCount);
    assertEquals(intervalMs, host.timerIntervalMs);
    assertEquals(0, host.updateAddCount);
    flick.timerTriggered(flick.timer);
    assertEquals(1, recording.frameCount());
    assertTrue(target.scrollY > 0);
    assertThrows(IllegalStateException.class,
        () -> flick.configureFrameDriverForBenchmark(Flick.FRAME_DRIVER_UPDATE, 0));

    flick.stop(false);
    flick.stop(false);
    assertEquals(1, host.timerRemoveCount);
    assertEquals(0, host.updateRemoveCount);
    assertEquals(1, target.flickEndedCount);
  }

  private static TestScrollable newTestScrollable() {
    AtomicReference<TestScrollable> created = new AtomicReference<TestScrollable>();
    app.runOnMainThread(new Runnable() {
      @Override
      public void run() {
        created.set(new TestScrollable());
      }
    });
    long deadline = System.currentTimeMillis() + 5000;
    while (created.get() == null && System.currentTimeMillis() < deadline) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
        throw new AssertionError(interrupted);
      }
    }
    assertNotNull(created.get(), "timed out creating test Flick target");
    return created.get();
  }

  public static class DriverTestApp extends MainWindow {
    public DriverTestApp() {
      app = this;
    }
  }

  private static final class TestScrollable extends Container implements Scrollable {
    private final Flick flick;
    private int scrollY;
    private int flickEndedCount;

    TestScrollable() {
      flick = new Flick(this);
    }

    @Override
    public boolean flickStarted() {
      return true;
    }

    @Override
    public void flickEnded(boolean atPenDown) {
      flickEndedCount++;
    }

    @Override
    public boolean canScrollContent(int direction, Object eventTarget) {
      return direction == DragEvent.DOWN;
    }

    @Override
    public boolean scrollContent(int xDelta, int yDelta, boolean fromFlick) {
      scrollY += yDelta;
      return true;
    }

    @Override
    public Flick getFlick() {
      return flick;
    }

    @Override
    public int getScrollPosition(int direction) {
      return scrollY;
    }

    @Override
    public boolean wasScrolled() {
      return scrollY != 0;
    }
  }
}
