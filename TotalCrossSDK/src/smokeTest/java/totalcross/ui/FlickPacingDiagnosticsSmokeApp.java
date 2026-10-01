// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import java.util.ArrayList;
import java.util.List;

import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnostics;
import totalcross.sys.Vm;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;

/** Measures the internal Flick drivers with a real macOS VM event loop. */
public class FlickPacingDiagnosticsSmokeApp extends MainWindow implements TimerListener {
  private final List<String> measurementLines = new ArrayList<String>();
  private final RunSpec[] runs = {
      new RunSpec("TimerEvent", Flick.PacingDriver.TIMER_EVENT, 40, false, true, 0),
      new RunSpec("TimerEvent", Flick.PacingDriver.TIMER_EVENT, 40, true, false, 1),
      new RunSpec("TimerEvent", Flick.PacingDriver.TIMER_EVENT, 40, true, false, 2),
      new RunSpec("TimerEvent", Flick.PacingDriver.TIMER_EVENT, 60, true, false, 1),
      new RunSpec("TimerEvent", Flick.PacingDriver.TIMER_EVENT, 60, true, false, 2),
      new RunSpec("UpdateListener", Flick.PacingDriver.UPDATE_LISTENER, 40, true, false, 1),
      new RunSpec("UpdateListener", Flick.PacingDriver.UPDATE_LISTENER, 40, true, false, 2),
      new RunSpec("TimerEvent", Flick.PacingDriver.TIMER_EVENT, 40, false, true, 3)
  };
  private int nextRun;
  private int baselineScrollPosition;
  private boolean baselineSet;
  private RunTarget activeTarget;
  private RunTarget completedTarget;
  private TimerEvent sequenceTimer;

  @Override
  public void initUI() {
    try {
      require(RuntimeDiagnostics.isSupported(), "diagnostics-enabled SDK required");
      require(Flick.defaultFrameRate == 40, "default Flick frame rate changed");
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
      startNextRun();
    } catch (Throwable failure) {
      fail(failure);
    }
  }

  @Override
  public void timerTriggered(TimerEvent event) {
    if (event == sequenceTimer) {
      removeTimer(sequenceTimer);
      removeTimerListener(this);
      sequenceTimer = null;
      try {
        RunTarget finished = completedTarget;
        completedTarget = null;
        if (finished == null) {
          startNextRun();
        } else {
          finishRun(finished);
        }
      } catch (Throwable failure) {
        fail(failure);
      }
    }
  }

  private void startNextRun() {
    if (nextRun >= runs.length) {
      finishSmoke();
      return;
    }
    RunSpec spec = runs[nextRun++];
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, spec.diagnosticsEnabled);
    RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
    RunTarget target = new RunTarget(this);
    Flick flick;
    if (spec.defaultConstructor) {
      flick = new Flick(target);
    } else {
      flick = new MeasuredFlick(target, spec.driver, spec.frameRate);
    }
    flick.frameRate = spec.frameRate;
    flick.longestFlick = 400;
    flick.setScrollDistance(100);
    target.flick = flick;
    activeTarget = target;

    if (spec.defaultConstructor) {
      require(flick.frameRate == 40, "default Flick frame rate is not 40");
      require(1000 / flick.frameRate == 25, "default Flick cadence is not 25 ms");
    }

    DragEvent start = new DragEvent();
    start.dragId = nextRun;
    start.direction = DragEvent.DOWN;
    start.absoluteY = target.scrollY;
    start.target = target;
    flick.penDragStart(start);

    DragEvent end = new DragEvent();
    end.dragId = nextRun;
    end.direction = DragEvent.DOWN;
    end.absoluteY = target.scrollY + 100;
    end.yTotal = 100;
    end.target = target;
    flick.penDragEnd(end);

    require(Flick.currentFlick == flick, "Flick did not start");
    int intervalMillis = 1000 / spec.frameRate;
    if (spec.driver == Flick.PacingDriver.TIMER_EVENT) {
      require(flick.timer.millis == intervalMillis, "TimerEvent interval mismatch");
      require(hasTimer(flick.timer), "TimerEvent driver was not registered");
    } else {
      require(!hasTimer(flick.timer), "UpdateListener driver also armed a Flick timer");
    }
    target.before = before;
  }

  private boolean hasTimer(TimerEvent wanted) {
    for (TimerEvent timer = firstTimer; timer != null; timer = timer.next) {
      if (timer == wanted) {
        return true;
      }
    }
    return false;
  }

  private void onRunComplete(RunTarget target) {
    if (target != activeTarget) {
      fail(new IllegalStateException("unexpected Flick completion"));
      return;
    }
    completedTarget = target;
    sequenceTimer = addTimer(1);
    addTimerListener(this);
  }

  private void finishRun(RunTarget target) {
    try {
      require(Flick.currentFlick == null, "previous Flick did not stop");
      RunSpec spec = runs[nextRun - 1];
      int finalPosition = target.scrollY;
      if (spec.defaultConstructor && !baselineSet) {
        baselineScrollPosition = finalPosition;
        baselineSet = true;
      } else {
        require(finalPosition == baselineScrollPosition, "driver changed final scroll position");
      }
      if (spec.diagnosticsEnabled) {
        RuntimeDiagnosticSnapshot delta = RuntimeDiagnostics.snapshot().deltaSince(target.before);
        require(delta.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
            RuntimeDiagnosticSnapshot.Kind.COUNTER) > 0L, "scheduling counters did not move");
        require(delta.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
            RuntimeDiagnosticSnapshot.Kind.TIMER) > 0L, "scheduling timers did not move");
        require(target.metrics != null && target.metrics.advancements > 0,
            "Flick advancement observations are missing");
        int intervalMillis = 1000 / spec.frameRate;
        measurementLines.add(target.metrics.format(spec, intervalMillis,
            target.endedAtMillis - target.startedAtMillis, finalPosition));
      } else if (spec.defaultConstructor) {
        measurementLines.add("driver=TimerEvent,requestedFps=40,requestedIntervalMs=25,"
            + "diagnostics=off,completed=true,finalScrollPosition=" + finalPosition);
      } else {
        measurementLines.add("driver=TimerEvent,requestedFps=40,requestedIntervalMs=25,"
            + "diagnostics=off,completed=true,finalScrollPosition=" + finalPosition);
      }
      activeTarget = null;
      RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
      startNextRun();
    } catch (Throwable failure) {
      fail(failure);
    }
  }

  private void finishSmoke() {
    for (String line : measurementLines) {
      System.out.println(line);
    }
    System.out.println("fixture=FlickPacingDiagnosticsSmokeApp,overallPass=true,"
        + "defaultFps=40,defaultTimerMs=25,finalScrollPosition=" + baselineScrollPosition);
    System.out.flush();
    exit(0);
  }

  private void fail(Throwable failure) {
    System.out.println("fixture=FlickPacingDiagnosticsSmokeApp,overallPass=false,error="
        + failure.getMessage());
    failure.printStackTrace();
    System.out.flush();
    exit(1);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static final class RunSpec {
    final String driverName;
    final Flick.PacingDriver driver;
    final int frameRate;
    final boolean diagnosticsEnabled;
    final boolean defaultConstructor;
    final int repetition;

    RunSpec(String driverName, Flick.PacingDriver driver, int frameRate,
        boolean diagnosticsEnabled, boolean defaultConstructor, int repetition) {
      this.driverName = driverName;
      this.driver = driver;
      this.frameRate = frameRate;
      this.diagnosticsEnabled = diagnosticsEnabled;
      this.defaultConstructor = defaultConstructor;
      this.repetition = repetition;
    }
  }

  private static final class RunTarget extends Container implements Scrollable {
    private final FlickPacingDiagnosticsSmokeApp app;
    private RuntimeDiagnosticSnapshot before;
    private int scrollX;
    private int scrollY = 20;
    private int startedAtMillis;
    private int endedAtMillis;
    private Flick flick;
    private MeasuredFlick.Metrics metrics;

    RunTarget(FlickPacingDiagnosticsSmokeApp app) {
      this.app = app;
    }

    @Override
    public boolean flickStarted() {
      startedAtMillis = Vm.getTimeStamp();
      if (flick instanceof MeasuredFlick) {
        metrics = ((MeasuredFlick) flick).metrics;
      }
      return true;
    }

    @Override
    public void flickEnded(boolean atPenDown) {
      endedAtMillis = Vm.getTimeStamp();
      app.onRunComplete(this);
    }

    @Override
    public boolean canScrollContent(int direction, Object target) {
      return true;
    }

    @Override
    public boolean scrollContent(int dx, int dy, boolean fromFlick) {
      scrollX += dx;
      scrollY += dy;
      return true;
    }

    @Override
    public Flick getFlick() {
      return flick;
    }

    @Override
    public int getScrollPosition(int direction) {
      return direction == DragEvent.LEFT || direction == DragEvent.RIGHT ? scrollX : scrollY;
    }

    @Override
    public boolean wasScrolled() {
      return scrollX != 0 || scrollY != 20;
    }
  }

  private static final class MeasuredFlick extends Flick {
    final Metrics metrics = new Metrics();

    MeasuredFlick(Scrollable target, PacingDriver driver, int frameRate) {
      super(target, driver, null);
      this.frameRate = frameRate;
    }

    @Override
    void onDiagnosticCallback(long positiveLatenessNanos) {
      metrics.callbacks++;
      metrics.totalPositiveLatenessNanos += positiveLatenessNanos;
      metrics.maxPositiveLatenessNanos = Math.max(metrics.maxPositiveLatenessNanos, positiveLatenessNanos);
    }

    @Override
    void onDiagnosticAdvancement(long workNanos, boolean completed) {
      metrics.advancements++;
      metrics.totalAdvancementWorkNanos += workNanos;
      if (completed) {
        metrics.completions++;
      }
    }

    private static final class Metrics {
      long callbacks;
      long advancements;
      long completions;
      long totalAdvancementWorkNanos;
      long totalPositiveLatenessNanos;
      long maxPositiveLatenessNanos;

      String format(RunSpec spec, int intervalMillis, int durationMillis, int finalPosition) {
        return "driver=" + spec.driverName + ",requestedFps=" + spec.frameRate
            + ",requestedIntervalMs=" + intervalMillis + ",repetition=" + spec.repetition
            + ",cadenceBasis=" + (spec.driver == Flick.PacingDriver.UPDATE_LISTENER
                ? "UpdateListenerObservation" : "TimerEventRequest")
            + ",callbackCount=" + callbacks + ",advancementCount=" + advancements
            + ",completionCount=" + completions + ",animationDurationMillis=" + durationMillis
            + ",totalAdvancementWorkNanos=" + totalAdvancementWorkNanos
            + ",averageAdvancementWorkNanos=" + average(totalAdvancementWorkNanos, advancements)
            + ",totalPositiveLatenessNanos=" + totalPositiveLatenessNanos
            + ",averagePositiveLatenessNanos=" + average(totalPositiveLatenessNanos, callbacks)
            + ",maxPositiveLatenessNanos=" + maxPositiveLatenessNanos
            + ",finalScrollPosition=" + finalPosition + ",completed=true";
      }

      private static long average(long total, long count) {
        return count == 0 ? 0 : total / count;
      }
    }
  }
}
