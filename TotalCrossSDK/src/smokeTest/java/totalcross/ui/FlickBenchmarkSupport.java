// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import java.util.ArrayList;

/** Benchmark-side setup and recording for deterministic Flick motion. */
public final class FlickBenchmarkSupport {
  public static final int MOTION_DURATION_MS = 3000;
  public static final int MOTION_DISPLACEMENT = -22440;

  private FlickBenchmarkSupport() {
  }

  public interface FrameListener {
    void frameCompleted(Frame frame);
  }

  public static final class Frame {
    private final long callbackStartNs;
    private final long elapsedNs;
    private final long intervalNs;
    private final long callbackLatenessNs;
    private final long callbackDeltaErrorNs;
    private final long scrollWorkNs;
    private final long paintWorkNs;
    private final long workNs;
    private final int flickPosition;
    private final boolean finished;

    private Frame(long callbackStartNs, long elapsedNs, long intervalNs,
        long callbackLatenessNs, long callbackDeltaErrorNs, long scrollWorkNs,
        long paintWorkNs, long workNs, int flickPosition, boolean finished) {
      this.callbackStartNs = callbackStartNs;
      this.elapsedNs = elapsedNs;
      this.intervalNs = intervalNs;
      this.callbackLatenessNs = callbackLatenessNs;
      this.callbackDeltaErrorNs = callbackDeltaErrorNs;
      this.scrollWorkNs = scrollWorkNs;
      this.paintWorkNs = paintWorkNs;
      this.workNs = workNs;
      this.flickPosition = flickPosition;
      this.finished = finished;
    }

    public long callbackStartNs() {
      return callbackStartNs;
    }

    public long elapsedNs() {
      return elapsedNs;
    }

    public long intervalNs() {
      return intervalNs;
    }

    public long callbackLatenessNs() {
      return callbackLatenessNs;
    }

    public long callbackDeltaErrorNs() {
      return callbackDeltaErrorNs;
    }

    public long scrollWorkNs() {
      return scrollWorkNs;
    }

    public long paintWorkNs() {
      return paintWorkNs;
    }

    public long workNs() {
      return workNs;
    }

    public int flickPosition() {
      return flickPosition;
    }

    public boolean finished() {
      return finished;
    }
  }

  public static final class Recording implements Flick.FrameObserver {
    private final Control target;
    private final long startedNs;
    private final long expectedIntervalNs;
    private final boolean repaint;
    private final FrameListener listener;
    private final ArrayList<Frame> frames = new ArrayList<Frame>();
    private long previousCallbackStartNs;
    private long completedNs;
    private boolean complete;

    private Recording(Control target, long expectedIntervalNs, boolean repaint,
        FrameListener listener) {
      this.target = target;
      this.expectedIntervalNs = expectedIntervalNs;
      this.repaint = repaint;
      this.listener = listener;
      startedNs = System.nanoTime();
    }

    @Override
    public void frameAdvanced(long callbackStartNs, long callbackEndNs, long scrollWorkNs,
        int flickPosition, boolean finished) {
      long paintStartNs = System.nanoTime();
      if (repaint) {
        target.repaintNow();
      }
      long completedFrameNs = System.nanoTime();
      int index = frames.size();
      long intervalNs = index == 0 ? callbackStartNs - startedNs
          : callbackStartNs - previousCallbackStartNs;
      long expectedDeadlineNs = startedNs + (index + 1L) * expectedIntervalNs;
      long latenessNs = Math.max(0L, callbackStartNs - expectedDeadlineNs);
      long deltaErrorNs = Math.abs(intervalNs - expectedIntervalNs);
      long paintWorkNs = Math.max(0L, completedFrameNs - paintStartNs);
      long workNs = Math.max(0L, completedFrameNs - callbackStartNs);
      Frame frame = new Frame(callbackStartNs, callbackStartNs - startedNs, intervalNs,
          latenessNs, deltaErrorNs, scrollWorkNs, paintWorkNs, workNs, flickPosition, finished);
      frames.add(frame);
      previousCallbackStartNs = callbackStartNs;
      if (finished) {
        complete = true;
        completedNs = completedFrameNs;
      }
      if (listener != null) {
        listener.frameCompleted(frame);
      }
    }

    public Frame[] frames() {
      return frames.toArray(new Frame[frames.size()]);
    }

    public int frameCount() {
      return frames.size();
    }

    public long durationNs() {
      return complete ? completedNs - startedNs : System.nanoTime() - startedNs;
    }

    public long expectedIntervalNs() {
      return expectedIntervalNs;
    }

    public int finalFlickPosition() {
      return frames.size() == 0 ? 0 : frames.get(frames.size() - 1).flickPosition();
    }

    public boolean complete() {
      return complete;
    }
  }

  static final class DriverHostRecorder implements Flick.FrameDriverHost {
    int timerAddCount;
    int timerRemoveCount;
    int timerIntervalMs;
    int updateAddCount;
    int updateRemoveCount;

    @Override
    public void addTimer(Flick flick, int intervalMs) {
      timerAddCount++;
      timerIntervalMs = intervalMs;
    }

    @Override
    public void removeTimer(Flick flick) {
      timerRemoveCount++;
    }

    @Override
    public void addUpdateListener(Flick flick) {
      updateAddCount++;
    }

    @Override
    public void removeUpdateListener(Flick flick) {
      updateRemoveCount++;
    }
  }

  public static Recording start(Scrollable target, String driver, int timerFps,
      FrameListener listener) {
    return start(target, driver, timerFps, Flick.FRAME_CLOCK_MILLIS,
        listener, null, 0, true);
  }

  public static Recording start(Scrollable target, String driver, int timerFps,
      String clock, FrameListener listener) {
    return start(target, driver, timerFps, clock, listener, null, 0, true);
  }

  static Recording startForTest(Scrollable target, String driver, int timerFps,
      DriverHostRecorder host, int startOffsetMs) {
    return startForTest(target, driver, timerFps, Flick.FRAME_CLOCK_MILLIS,
        host, startOffsetMs);
  }

  static Recording startForTest(Scrollable target, String driver, int timerFps,
      String clock, DriverHostRecorder host, int startOffsetMs) {
    return start(target, driver, timerFps, clock, null, host, startOffsetMs, false);
  }

  private static Recording start(Scrollable target, String driver, int timerFps,
      String clock, FrameListener listener, DriverHostRecorder host,
      int startOffsetMs, boolean repaint) {
    if (target == null) {
      throw new NullPointerException("scrollable target");
    }
    Flick flick = target.getFlick();
    if (flick == null) {
      if (!(target instanceof ScrollContainer)) {
        throw new IllegalArgumentException("benchmark Flick target has no Flick instance");
      }
      flick = new Flick(target);
      ((ScrollContainer) target).flick = flick;
    }
    long expectedIntervalNs;
    if (Flick.FRAME_DRIVER_TIMER.equals(driver)) {
      if (timerFps != 40 && timerFps != 60) {
        throw new IllegalArgumentException("benchmark timer fps must be 40 or 60");
      }
      expectedIntervalNs = (1000 / timerFps) * 1000000L;
    } else if (Flick.FRAME_DRIVER_UPDATE.equals(driver)) {
      if (timerFps != 0) {
        throw new IllegalArgumentException("UpdateListener driver has no timer fps");
      }
      expectedIntervalNs = 16000000L;
    } else {
      throw new IllegalArgumentException("benchmark driver must be timer or update");
    }
    Recording recording = new Recording((Control) target, expectedIntervalNs, repaint, listener);
    if (host != null) {
      flick.setFrameDriverHostForTest(host);
    }
    flick.configureFrameDriverForBenchmark(driver, timerFps);
    flick.configureFrameClockForBenchmark(clock);
    flick.startBenchmarkMotion(MOTION_DURATION_MS, MOTION_DISPLACEMENT, startOffsetMs,
        recording);
    return recording;
  }
}
