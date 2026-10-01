// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.RuntimeDiagnosticSnapshot;
import totalcross.sys.RuntimeDiagnostics;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.TimerEvent;

class FlickPacingTest {
  private static MainWindow originalMainWindow;
  private static Window originalTopMost;
  private static totalcross.util.Vector originalWindowStack;
  private static Object[] originalWindowStackContents;
  private static tc.simulator.Launcher originalLauncher;

  @BeforeAll
  static void initializeUi() {
    originalMainWindow = MainWindow.getMainWindow();
    originalTopMost = Window.topMost;
    originalWindowStack = Window.zStack;
    originalWindowStackContents = originalWindowStack.toObjectArray();
    if (originalWindowStackContents == null) {
      originalWindowStackContents = new Object[0];
    }
    originalLauncher = Launcher.instance;
    if (MainWindow.getMainWindow() == null) {
      new Launcher();
      new MainWindow();
    }
  }

  @AfterAll
  static void restoreUi() {
    if (originalMainWindow == null) {
      originalWindowStack.removeAllElements();
      Window.zStack = originalWindowStack;
      for (Object window : originalWindowStackContents) {
        Window.zStack.push(window);
      }
      Window.topMost = originalTopMost;
      MainWindow.mainWindowInstance = null;
      Launcher.instance = originalLauncher;
    }
  }

  @AfterEach
  void cleanUp() {
    if (Flick.currentFlick != null) {
      Flick.currentFlick.stop(false);
    }
    Flick.currentFlick = null;
    Flick.isDragging = false;
    totalcross.unit.UIRobot.abort = false;
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
  }

  @Test
  void defaultDriverKeepsFortyFpsAndTwentyFiveMillisecondTimer() {
    Fixture fixture = start(Flick.PacingDriver.TIMER_EVENT, DragEvent.DOWN, 0, 0);

    assertEquals(40, Flick.defaultFrameRate);
    assertEquals(40, fixture.flick.frameRate);
    assertEquals(25, fixture.flick.timer.millis);
    assertTrue(hasTimer(fixture.flick.timer));
  }

  @Test
  void sixtyFpsKeepsTheExistingIntegerSixteenMillisecondTimer() {
    Fixture fixture = start(Flick.PacingDriver.TIMER_EVENT, DragEvent.DOWN, 0, 0, 60);

    assertEquals(60, fixture.flick.frameRate);
    assertEquals(16, fixture.flick.timer.millis);
  }

  @Test
  void timerAndUpdateListenerProduceIdenticalVerticalAndHorizontalMotion() {
    for (int direction : new int[] {DragEvent.DOWN, DragEvent.RIGHT}) {
      Result timerResult = runSequence(Flick.PacingDriver.TIMER_EVENT, direction);
      Result updateResult = runSequence(Flick.PacingDriver.UPDATE_LISTENER, direction);

      assertEquals(List.of(109, 78, 47, 15), timerResult.motion);
      assertEquals(timerResult.motion, updateResult.motion);
      assertEquals(timerResult.scrollX, updateResult.scrollX);
      assertEquals(timerResult.scrollY, updateResult.scrollY);
      assertEquals(timerResult.completed, updateResult.completed);
      assertTrue(timerResult.completed);
    }
  }

  @Test
  void updateListenerDriverDoesNotArmTimerOrAdvanceTwice() {
    Fixture fixture = start(Flick.PacingDriver.UPDATE_LISTENER, DragEvent.DOWN, 0, 0);
    TimerEvent timer = fixture.flick.timer;

    assertFalse(hasTimer(timer));
    assertEquals(1, updateListenerRegistrations(fixture.flick));
    timer.consumed = false;
    fixture.flick.timerTriggered(timer);
    assertFalse(timer.consumed);
    assertTrue(fixture.target.deltas.isEmpty());

    fixture.clock.now = 1250;
    fixture.flick.updateListenerTriggered(250);
    assertEquals(1, fixture.target.deltas.size());
    assertArrayEquals(new int[] {0, -109}, fixture.target.deltas.get(0));
  }

  @Test
  void scrollDistanceCapsMotionAndUpdatesPagePosition() {
    for (Flick.PacingDriver driver : Flick.PacingDriver.values()) {
      Fixture fixture = start(driver, DragEvent.DOWN, 100, 190);
      PagePosition pagePosition = new PagePosition(4);
      fixture.flick.setPagePosition(pagePosition);

      fixture.clock.now = 3500;
      tick(fixture, driver, 2500);
      assertEquals(1, fixture.target.deltas.size());
      assertArrayEquals(new int[] {0, -90}, fixture.target.deltas.get(0));
      assertEquals(2, pagePosition.getPosition());

      fixture.clock.now = 3501;
      tick(fixture, driver, 2501);
      assertNull(Flick.currentFlick);
      assertEquals(2, pagePosition.getPosition());
    }
  }

  @Test
  void listenerReceivesTheSameScrollDeltasAsTheTarget() {
    for (Flick.PacingDriver driver : Flick.PacingDriver.values()) {
      Fixture fixture = start(driver, DragEvent.RIGHT, 0, 0);
      RecordingScrollable listener = new RecordingScrollable();
      fixture.flick.addScrollableListener(listener);

      fixture.clock.now = 1250;
      tick(fixture, driver, 250);

      assertEquals(fixture.target.deltas.size(), listener.deltas.size());
      for (int i = 0; i < fixture.target.deltas.size(); i++) {
        assertArrayEquals(fixture.target.deltas.get(i), listener.deltas.get(i));
      }
      fixture.flick.stop(false);
    }
  }

  @Test
  void targetRefusalAndElapsedEndStopBothDrivers() {
    for (Flick.PacingDriver driver : Flick.PacingDriver.values()) {
      Fixture refused = start(driver, DragEvent.DOWN, 0, 0);
      refused.target.acceptScroll = false;
      refused.clock.now = 1250;
      tick(refused, driver, 250);
      assertNull(Flick.currentFlick);
      assertEquals(1, refused.target.endedCount);

      Fixture elapsed = start(driver, DragEvent.DOWN, 0, 0);
      elapsed.clock.now = 2001;
      tick(elapsed, driver, 1001);
      assertNull(Flick.currentFlick);
      assertEquals(1, elapsed.target.endedCount);
    }
  }

  @Test
  void stopAndPenDownRemoveSelectedDriver() {
    for (Flick.PacingDriver driver : Flick.PacingDriver.values()) {
      Fixture stopped = start(driver, DragEvent.DOWN, 0, 0);
      stopped.flick.stop(false);
      assertNull(Flick.currentFlick);
      assertEquals(1, stopped.target.endedCount);
      assertFalse(hasTimer(stopped.flick.timer));
      assertEquals(0, updateListenerRegistrations(stopped.flick));

      Fixture penDown = start(driver, DragEvent.DOWN, 0, 0);
      penDown.flick.penDown(new PenEvent());
      assertNull(Flick.currentFlick);
      assertTrue(penDown.target.lastEndedAtPenDown);
      assertFalse(hasTimer(penDown.flick.timer));
      assertEquals(0, updateListenerRegistrations(penDown.flick));
    }
  }

  @Test
  void timerAdapterPreservesUiRobotAbortBehavior() {
    Fixture fixture = start(Flick.PacingDriver.TIMER_EVENT, DragEvent.DOWN, 0, 0);
    TimerEvent event = fixture.flick.timer;
    event.consumed = false;
    fixture.clock.now = 1250;
    totalcross.unit.UIRobot.abort = true;

    fixture.flick.timerTriggered(event);

    assertFalse(event.consumed);
    assertTrue(fixture.target.deltas.isEmpty());
    assertSame(fixture.flick, Flick.currentFlick);
  }

  @Test
  void repeatedStartsAndStopsDoNotLeakTimerOrUpdateListenerRegistration() {
    for (int i = 0; i < 3; i++) {
      for (Flick.PacingDriver driver : Flick.PacingDriver.values()) {
        Fixture fixture = start(driver, DragEvent.DOWN, 0, 0);
        assertEquals(driver == Flick.PacingDriver.UPDATE_LISTENER ? 1 : 0,
            updateListenerRegistrations(fixture.flick));
        assertEquals(driver == Flick.PacingDriver.TIMER_EVENT, hasTimer(fixture.flick.timer));
        fixture.flick.stop(false);
        assertEquals(0, updateListenerRegistrations(fixture.flick));
        assertFalse(hasTimer(fixture.flick.timer));
      }
    }
  }

  @Test
  void enabledSchedulingDiagnosticsCountBothDriversAndTheirAdvancementWork() {
    org.junit.jupiter.api.Assumptions.assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);

    for (Flick.PacingDriver driver : Flick.PacingDriver.values()) {
      RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
      Fixture fixture = start(driver, DragEvent.DOWN, 0, 0);
      Object timing = diagnosticTiming(fixture.flick);
      assertTrue(timing != null);
      setLongField(timing, "expectedCallbackNanoTime", System.nanoTime() - 1000000000L);
      fixture.clock.now = 1250;
      tick(fixture, driver, 250);
      fixture.clock.now = 2001;
      tick(fixture, driver, 1001);
      RuntimeDiagnosticSnapshot delta = RuntimeDiagnostics.snapshot().deltaSince(before);

      assertEquals(5L, delta.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
          RuntimeDiagnosticSnapshot.Kind.COUNTER));
      assertTrue(delta.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
          RuntimeDiagnosticSnapshot.Kind.TIMER) > 0L);
      assertEquals(2, fixture.flick.callbackLateness.size());
      assertEquals(2, fixture.flick.advancementCompletions.size());
      assertTrue(fixture.flick.callbackLateness.get(0) > 0L);
      assertTrue(fixture.flick.advancementWorkNanos.get(0) >= 0L);
      assertTrue(fixture.flick.advancementCompletions.get(1));
      assertNull(Flick.currentFlick);
    }
  }

  @Test
  void disabledSchedulingKeepsNoTimingStateAndPreservesBothDrivers() {
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    RuntimeDiagnosticSnapshot before = RuntimeDiagnostics.snapshot();
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);

    for (Flick.PacingDriver driver : Flick.PacingDriver.values()) {
      Fixture fixture = start(driver, DragEvent.DOWN, 0, 0);
      assertNull(diagnosticTiming(fixture.flick));
      List<Integer> motion = new ArrayList<>();
      int[] absoluteTimes = {1250, 1500, 1750, 2001};
      for (int absoluteTime : absoluteTimes) {
        fixture.clock.now = absoluteTime;
        tick(fixture, driver, absoluteTime - 1000);
        int[] delta = fixture.target.deltas.get(fixture.target.deltas.size() - 1);
        motion.add(-delta[1]);
      }

      assertEquals(List.of(109, 78, 47, 15), motion);
      assertEquals(1, fixture.target.endedCount);
      assertNull(Flick.currentFlick);
      assertTrue(fixture.flick.callbackLateness.isEmpty());
      assertTrue(fixture.flick.advancementCompletions.isEmpty());
      assertNull(diagnosticTiming(fixture.flick));
    }
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    RuntimeDiagnosticSnapshot after = RuntimeDiagnostics.snapshot();
    assertEquals(before.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER), after.getValue(
            RuntimeDiagnosticSnapshot.Domain.SCHEDULING, RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(before.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER), after.getValue(
            RuntimeDiagnosticSnapshot.Domain.SCHEDULING, RuntimeDiagnosticSnapshot.Kind.TIMER));
  }

  @Test
  void disablingAndReenablingSchedulingLeavesActiveFlickTimingClean() {
    org.junit.jupiter.api.Assumptions.assumeTrue(RuntimeDiagnostics.isSupported());
    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    Result reference = runSequence(Flick.PacingDriver.TIMER_EVENT, DragEvent.DOWN);

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    Fixture fixture = start(Flick.PacingDriver.TIMER_EVENT, DragEvent.DOWN, 0, 0);
    Object timing = diagnosticTiming(fixture.flick);
    assertTrue(timing != null);
    fixture.clock.now = 1250;
    tick(fixture, Flick.PacingDriver.TIMER_EVENT, 250);
    assertEquals(1, fixture.flick.callbackLateness.size());
    RuntimeDiagnosticSnapshot beforeDisable = RuntimeDiagnostics.snapshot();
    long[] timingBeforeDisable = timingState(timing);

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, false);
    for (int absoluteTime : new int[] {1500, 1750}) {
      fixture.clock.now = absoluteTime;
      tick(fixture, Flick.PacingDriver.TIMER_EVENT, absoluteTime - 1000);
    }
    assertSame(timing, diagnosticTiming(fixture.flick));
    assertArrayEquals(timingBeforeDisable, timingState(timing));
    assertEquals(1, fixture.flick.callbackLateness.size());
    assertEquals(1, fixture.flick.advancementCompletions.size());

    RuntimeDiagnostics.setDomainEnabled(RuntimeDiagnosticSnapshot.Domain.SCHEDULING, true);
    RuntimeDiagnosticSnapshot afterReenable = RuntimeDiagnostics.snapshot();
    assertEquals(beforeDisable.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.COUNTER), afterReenable.getValue(
            RuntimeDiagnosticSnapshot.Domain.SCHEDULING, RuntimeDiagnosticSnapshot.Kind.COUNTER));
    assertEquals(beforeDisable.getValue(RuntimeDiagnosticSnapshot.Domain.SCHEDULING,
        RuntimeDiagnosticSnapshot.Kind.TIMER), afterReenable.getValue(
            RuntimeDiagnosticSnapshot.Domain.SCHEDULING, RuntimeDiagnosticSnapshot.Kind.TIMER));

    fixture.clock.now = 2001;
    tick(fixture, Flick.PacingDriver.TIMER_EVENT, 1001);
    assertEquals(2, fixture.flick.callbackLateness.size());
    assertEquals(0L, fixture.flick.callbackLateness.get(1));
    assertTrue(fixture.flick.advancementCompletions.get(1));
    List<Integer> motion = new ArrayList<>();
    for (int[] delta : fixture.target.deltas) {
      motion.add(-delta[1]);
    }
    assertEquals(reference.motion, motion);
    assertEquals(reference.scrollY, fixture.target.scrollY);
    assertNull(Flick.currentFlick);
  }

  private static Result runSequence(Flick.PacingDriver driver, int direction) {
    Fixture fixture = start(driver, direction, 0, 0);
    int[] absoluteTimes = {1250, 1500, 1750, 2001};
    List<Integer> motion = new ArrayList<>();
    for (int absoluteTime : absoluteTimes) {
      fixture.clock.now = absoluteTime;
      tick(fixture, driver, absoluteTime - 1000);
      int[] delta = fixture.target.deltas.get(fixture.target.deltas.size() - 1);
      motion.add(direction == DragEvent.DOWN ? -delta[1] : -delta[0]);
    }
    return new Result(motion, fixture.target.scrollX, fixture.target.scrollY, Flick.currentFlick == null);
  }

  private static Fixture start(Flick.PacingDriver driver, int direction, int scrollDistance,
      int initialScrollPosition) {
    return start(driver, direction, scrollDistance, initialScrollPosition, 40);
  }

  private static Fixture start(Flick.PacingDriver driver, int direction, int scrollDistance,
      int initialScrollPosition, int frameRate) {
    ManualClock clock = new ManualClock();
    clock.now = 1000;
    RecordingScrollable target = new RecordingScrollable();
    if (direction == DragEvent.DOWN) {
      target.scrollY = initialScrollPosition;
    } else {
      target.scrollX = initialScrollPosition;
    }
    RecordingFlick flick = new RecordingFlick(target, driver, clock);
    flick.frameRate = frameRate;
    if (scrollDistance != 0) {
      flick.setScrollDistance(scrollDistance);
    }
    setInt(flick, "dragId", 7);
    setInt(flick, "dragT0", 0);
    setInt(flick, "dragX0", 0);
    setInt(flick, "dragY0", 0);
    setInt(flick, "flickDirection", direction);
    setDouble(flick, "a", -0.0005);

    DragEvent end = new DragEvent();
    end.dragId = 7;
    end.direction = direction;
    end.absoluteX = direction == DragEvent.RIGHT ? 500 : 0;
    end.absoluteY = direction == DragEvent.DOWN ? 500 : 0;
    end.xTotal = direction == DragEvent.RIGHT ? 500 : 0;
    end.yTotal = direction == DragEvent.DOWN ? 500 : 0;
    end.target = target;
    flick.penDragEnd(end);
    assertSame(flick, Flick.currentFlick);
    return new Fixture(flick, target, clock);
  }

  private static void tick(Fixture fixture, Flick.PacingDriver driver, int elapsed) {
    if (driver == Flick.PacingDriver.TIMER_EVENT) {
      fixture.flick.timer.consumed = false;
      fixture.flick.timerTriggered(fixture.flick.timer);
      assertTrue(fixture.flick.timer.consumed);
    } else {
      fixture.flick.updateListenerTriggered(elapsed);
    }
  }

  private static boolean hasTimer(TimerEvent wanted) {
    for (TimerEvent timer = MainWindow.mainWindowInstance.firstTimer; timer != null; timer = timer.next) {
      if (timer == wanted) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private static int updateListenerRegistrations(Flick flick) {
    try {
      Field field = MainWindow.class.getDeclaredField("updateListeners");
      field.setAccessible(true);
      List<WeakReference<?>> listeners = (List<WeakReference<?>>) field.get(MainWindow.getMainWindow());
      int count = 0;
      for (WeakReference<?> reference : listeners) {
        if (reference.get() == flick) {
          count++;
        }
      }
      return count;
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static void setInt(Flick flick, String name, int value) {
    setField(flick, name, Integer.valueOf(value));
  }

  private static Object diagnosticTiming(Flick flick) {
    return getField(flick, "diagnosticTiming");
  }

  private static long[] timingState(Object timing) {
    return new long[] {
        getLongField(timing, "intervalNanos"),
        getLongField(timing, "expectedCallbackNanoTime"),
        getLongField(timing, "generation")
    };
  }

  private static long getLongField(Object target, String name) {
    Object value = getField(target, name);
    return ((Long) value).longValue();
  }

  private static void setLongField(Object target, String name, long value) {
    try {
      Field field = target instanceof Flick ? Flick.class.getDeclaredField(name)
          : target.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.setLong(target, value);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static Object getField(Object target, String name) {
    try {
      Field field = target instanceof Flick ? Flick.class.getDeclaredField(name)
          : target.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return field.get(target);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static void setDouble(Flick flick, String name, double value) {
    setField(flick, name, Double.valueOf(value));
  }

  private static void setField(Flick flick, String name, Object value) {
    try {
      Field field = Flick.class.getDeclaredField(name);
      field.setAccessible(true);
      field.set(flick, value);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static final class ManualClock implements Flick.TestClock {
    int now;

    @Override
    public int getTimeStamp() {
      return now;
    }
  }

  private static final class RecordingFlick extends Flick {
    final List<Long> callbackLateness = new ArrayList<>();
    final List<Boolean> advancementCompletions = new ArrayList<>();
    final List<Long> advancementWorkNanos = new ArrayList<>();

    RecordingFlick(Scrollable target, PacingDriver driver, TestClock clock) {
      super(target, driver, clock);
    }

    @Override
    void onDiagnosticCallback(long positiveLatenessNanos) {
      callbackLateness.add(positiveLatenessNanos);
    }

    @Override
    void onDiagnosticAdvancement(long workNanos, boolean completed) {
      advancementCompletions.add(Boolean.valueOf(completed));
      advancementWorkNanos.add(Long.valueOf(workNanos));
    }
  }

  private static final class RecordingScrollable extends Container implements Scrollable {
    final List<int[]> deltas = new ArrayList<>();
    int scrollX;
    int scrollY;
    int startedCount;
    int endedCount;
    boolean lastEndedAtPenDown;
    boolean acceptScroll = true;
    Flick flick;

    @Override
    public boolean flickStarted() {
      startedCount++;
      return true;
    }

    @Override
    public void flickEnded(boolean atPenDown) {
      endedCount++;
      lastEndedAtPenDown = atPenDown;
    }

    @Override
    public boolean canScrollContent(int direction, Object target) {
      return true;
    }

    @Override
    public boolean scrollContent(int dx, int dy, boolean fromFlick) {
      deltas.add(new int[] {dx, dy});
      if (!acceptScroll) {
        return false;
      }
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
      return false;
    }
  }

  private static final class Fixture {
    final RecordingFlick flick;
    final RecordingScrollable target;
    final ManualClock clock;

    Fixture(RecordingFlick flick, RecordingScrollable target, ManualClock clock) {
      this.flick = flick;
      this.target = target;
      this.clock = clock;
      target.flick = flick;
    }
  }

  private static final class Result {
    final List<Integer> motion;
    final int scrollX;
    final int scrollY;
    final boolean completed;

    Result(List<Integer> motion, int scrollX, int scrollY, boolean completed) {
      this.motion = motion;
      this.scrollX = scrollX;
      this.scrollY = scrollY;
      this.completed = completed;
    }
  }
}
