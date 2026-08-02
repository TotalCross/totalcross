// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.simulator;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import totalcross.MainClass;

final class EventLoop extends Thread {
  private static final int DEFAULT_POLL_TIMEOUT_MILLIS = 5;

  private final LinkedBlockingQueue<QueuedItem> queue = new LinkedBlockingQueue<QueuedItem>();
  private final Object lifecycleLock = new Object();
  private final int pollTimeoutMillis;
  private final QueuedItem shutdownItem = QueuedItem.shutdown();
  private volatile MainClass target;
  private boolean accepting = true;

  EventLoop(MainClass target) {
    super("TotalCross Simulator Event Loop");
    this.target = Objects.requireNonNull(target, "target");
    pollTimeoutMillis = DEFAULT_POLL_TIMEOUT_MILLIS;
    setPriority(Thread.MAX_PRIORITY); // event thread should have maximum priority
    setDaemon(true);
    start();
  }

  @Override
  public void run() {
    while (true) {
      try {
        if (!dispatchNext()) {
          return;
        }
      } catch (Throwable t) {
        reportFailure(t);
      }
    }
  }

  boolean hasPendingEvents() {
    for (QueuedItem item : queue) {
      if (item.isEvent()) {
        return true;
      }
    }
    return false;
  }

  boolean hasPendingEvent(int type) {
    for (QueuedItem item : queue) {
      if (item.isEvent() && item.type == type) {
        return true;
      }
    }
    return false;
  }

  boolean dispatchNext() {
    if (Thread.currentThread() != this) {
      return true;
    }
    QueuedItem item;
    try {
      item = pollTimeoutMillis <= 0
          ? queue.take()
          : queue.poll(pollTimeoutMillis, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      return true;
    }
    if (item == null) {
      return true;
    }
    if (item.shutdown) {
      return false;
    }
    dispatch(item);
    return true;
  }

  void postEvent(int type, int key, int x, int y, int modifiers, int timestamp) {
    submit(QueuedItem.event(type, key, x, y, modifiers, timestamp));
  }

  void post(Runnable command) {
    submit(QueuedItem.command(Objects.requireNonNull(command, "command"), false));
  }

  void invoke(Runnable command) {
    invoke(command, 0);
  }

  boolean invoke(Runnable command, long timeoutMillis) {
    Objects.requireNonNull(command, "command");
    ensureAccepting();
    if (Thread.currentThread() == this) {
      command.run();
      return true;
    }

    QueuedItem item = QueuedItem.command(command, true);
    submit(item);
    return item.await(timeoutMillis);
  }

  void setTarget(MainClass target) {
    this.target = Objects.requireNonNull(target, "target");
  }

  void shutdown() {
    synchronized (lifecycleLock) {
      if (!accepting) {
        return;
      }
      accepting = false;
      queue.offer(shutdownItem);
    }
  }

  private void submit(QueuedItem item) {
    synchronized (lifecycleLock) {
      if (!accepting) {
        throw new IllegalStateException("TotalCross simulator event loop is shut down");
      }
      queue.add(item);
    }
  }

  private void ensureAccepting() {
    synchronized (lifecycleLock) {
      if (!accepting) {
        throw new IllegalStateException("TotalCross simulator event loop is shut down");
      }
    }
  }

  private void dispatch(QueuedItem item) {
    try {
      if (item.isCommand()) {
        item.command.run();
      } else {
        target._postEvent(item.type, item.key, item.x, item.y, item.modifiers, item.timestamp);
      }
    } catch (Throwable t) {
      item.failure = t;
      reportFailure(t);
    } finally {
      if (item.completion != null) {
        item.completion.countDown();
      }
    }
  }

  private static void reportFailure(Throwable t) {
    // There's no vm.debug in Android!
    System.out.println("---------------------------");
    System.out.println(">>>>>>> CAUGHT UNHANDLED EXCEPTION IN EVENT THREAD:");
    t.printStackTrace();
  }

  private static final class QueuedItem {
    private final boolean shutdown;
    private final boolean commandItem;
    private final int type;
    private final int key;
    private final int x;
    private final int y;
    private final int modifiers;
    private final int timestamp;
    private final Runnable command;
    private final CountDownLatch completion;
    private volatile Throwable failure;

    private QueuedItem(boolean shutdown, boolean commandItem, int type, int key, int x, int y, int modifiers,
        int timestamp, Runnable command, CountDownLatch completion) {
      this.shutdown = shutdown;
      this.commandItem = commandItem;
      this.type = type;
      this.key = key;
      this.x = x;
      this.y = y;
      this.modifiers = modifiers;
      this.timestamp = timestamp;
      this.command = command;
      this.completion = completion;
    }

    private static QueuedItem event(int type, int key, int x, int y, int modifiers, int timestamp) {
      return new QueuedItem(false, false, type, key, x, y, modifiers, timestamp, null, null);
    }

    private static QueuedItem command(Runnable command, boolean waitForCompletion) {
      return new QueuedItem(false, true, 0, 0, 0, 0, 0, 0, command,
          waitForCompletion ? new CountDownLatch(1) : null);
    }

    private static QueuedItem shutdown() {
      return new QueuedItem(true, false, 0, 0, 0, 0, 0, 0, null, null);
    }

    private boolean isEvent() {
      return !shutdown && !commandItem;
    }

    private boolean isCommand() {
      return commandItem;
    }

    private boolean await(long timeoutMillis) {
      boolean interrupted = false;
      boolean completed = false;
      try {
        if (timeoutMillis <= 0) {
          while (!completed) {
            try {
              completion.await();
              completed = true;
            } catch (InterruptedException e) {
              interrupted = true;
            }
          }
        } else {
          long remainingNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
          long deadline = System.nanoTime() + remainingNanos;
          while (!completed && remainingNanos > 0) {
            try {
              completed = completion.await(remainingNanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
              interrupted = true;
            }
            remainingNanos = deadline - System.nanoTime();
          }
        }
        return completed;
      } finally {
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
