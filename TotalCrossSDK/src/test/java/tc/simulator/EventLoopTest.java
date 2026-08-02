// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import totalcross.MainClass;

class EventLoopTest {
  private static final long WAIT_MILLIS = 2000;

  @Test
  void deliversEventsInFifoOrderWithAllFieldsUnchanged() throws Exception {
    RecordingMainClass target = new RecordingMainClass(2);
    EventLoop eventLoop = new EventLoop(target);
    try {
      eventLoop.postEvent(11, 12, 13, 14, 15, 16);
      eventLoop.postEvent(21, 22, 23, 24, 25, 26);

      assertTrue(target.events.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
      assertEquals(List.of("11:12:13:14:15:16", "21:22:23:24:25:26"), target.receivedEvents);
    } finally {
      eventLoop.shutdown();
    }
  }

  @Test
  void postIsAsynchronousAndInvokeWaitsForCompletion() throws Exception {
    EventLoop eventLoop = new EventLoop(new RecordingMainClass(0));
    try {
      CountDownLatch posted = new CountDownLatch(1);
      eventLoop.post(posted::countDown);
      assertTrue(posted.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

      CountDownLatch invoked = new CountDownLatch(1);
      assertTrue(eventLoop.invoke(() -> invoked.countDown(), WAIT_MILLIS));
      assertEquals(0, invoked.getCount());
    } finally {
      eventLoop.shutdown();
    }
  }

  @Test
  void invocationFromEventLoopRunsDirectly() throws Exception {
    EventLoop eventLoop = new EventLoop(new RecordingMainClass(0));
    try {
      CountDownLatch completed = new CountDownLatch(1);
      eventLoop.post(() -> {
        assertTrue(eventLoop.invoke(completed::countDown, WAIT_MILLIS));
      });
      assertTrue(completed.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
    } finally {
      eventLoop.shutdown();
    }
  }

  @Test
  void invokeReturnsFalseOnTimeout() throws Exception {
    EventLoop eventLoop = new EventLoop(new RecordingMainClass(0));
    CountDownLatch release = new CountDownLatch(1);
    try {
      eventLoop.post(() -> await(release));
      assertFalse(eventLoop.invoke(() -> { }, 50));
      release.countDown();
    } finally {
      release.countDown();
      eventLoop.shutdown();
    }
  }

  @Test
  void waitingCallerIsReleasedWhenCommandFailsAndLoopContinues() throws Exception {
    EventLoop eventLoop = new EventLoop(new RecordingMainClass(0));
    try {
      assertTrue(eventLoop.invoke(() -> {
        throw new RuntimeException("boom");
      }, WAIT_MILLIS));

      CountDownLatch continued = new CountDownLatch(1);
      eventLoop.post(continued::countDown);
      assertTrue(continued.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
    } finally {
      eventLoop.shutdown();
    }
  }

  @Test
  void setTargetDirectsSubsequentEventsToReplacementTarget() throws Exception {
    RecordingMainClass first = new RecordingMainClass(1);
    RecordingMainClass second = new RecordingMainClass(1);
    EventLoop eventLoop = new EventLoop(first);
    try {
      eventLoop.postEvent(1, 2, 3, 4, 5, 6);
      assertTrue(first.events.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

      eventLoop.setTarget(second);
      eventLoop.postEvent(7, 8, 9, 10, 11, 12);
      assertTrue(second.events.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
      assertEquals(List.of("7:8:9:10:11:12"), second.receivedEvents);
    } finally {
      eventLoop.shutdown();
    }
  }

  @Test
  void reportsPendingEventsWithoutCountingCommands() throws Exception {
    EventLoop eventLoop = new EventLoop(new RecordingMainClass(0));
    CountDownLatch release = new CountDownLatch(1);
    try {
      eventLoop.post(() -> await(release));
      eventLoop.postEvent(41, 0, 0, 0, 0, 0);
      eventLoop.postEvent(42, 0, 0, 0, 0, 0);
      assertTrue(eventLoop.hasPendingEvents());
      assertTrue(eventLoop.hasPendingEvent(41));
      assertFalse(eventLoop.hasPendingEvent(99));
      release.countDown();
    } finally {
      release.countDown();
      eventLoop.shutdown();
    }
  }

  @Test
  void shutdownWakesIdleLoopAndDrainsQueuedWork() throws Exception {
    EventLoop idleLoop = new EventLoop(new RecordingMainClass(0));
    assertTrue(idleLoop.isAlive());
    idleLoop.shutdown();
    idleLoop.join(WAIT_MILLIS);
    assertFalse(idleLoop.isAlive());

    EventLoop queuedLoop = new EventLoop(new RecordingMainClass(0));
    CountDownLatch queuedWork = new CountDownLatch(1);
    queuedLoop.post(queuedWork::countDown);
    queuedLoop.shutdown();
    assertTrue(queuedWork.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
    queuedLoop.join(WAIT_MILLIS);
    assertFalse(queuedLoop.isAlive());
  }

  @Test
  void rejectsSubmissionsAfterShutdown() {
    EventLoop eventLoop = new EventLoop(new RecordingMainClass(0));
    eventLoop.shutdown();

    assertThrows(IllegalStateException.class, () -> eventLoop.post(() -> { }));
    assertThrows(IllegalStateException.class, () -> eventLoop.postEvent(1, 2, 3, 4, 5, 6));
    assertThrows(IllegalStateException.class, () -> eventLoop.invoke(() -> { }, WAIT_MILLIS));
  }

  private static void await(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static final class RecordingMainClass implements MainClass {
    private final CountDownLatch events;
    private final List<String> receivedEvents = Collections.synchronizedList(new ArrayList<String>());

    private RecordingMainClass(int expectedEvents) {
      events = new CountDownLatch(expectedEvents);
    }

    @Override
    public void _postEvent(int type, int key, int x, int y, int modifiers, int timestamp) {
      receivedEvents.add(type + ":" + key + ":" + x + ":" + y + ":" + modifiers + ":" + timestamp);
      events.countDown();
    }

    @Override
    public void appStarting(int timeAvail) {
    }

    @Override
    public void appEnding() {
    }

    @Override
    public void _onTimerTick(boolean canUpdate) {
    }
  }
}
