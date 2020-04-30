// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2010 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross;

public class TCEventThread extends Thread {
  Queue eventQueue;
  static private final int INVOKE_IN_EVENT_THREAD = -99998;
  MainClass win;
  public boolean running = true;
  public int popTime = 5;

  public TCEventThread(MainClass win) {
    super("TC Event Thread");
    this.win = win;
    eventQueue = new Queue();
    nativeCreate();
  }

  void nativeCreate() {
    setPriority(Thread.MAX_PRIORITY); // event thread should have maximum priority
    setDaemon(true);
    start();
  }

  @Override
  public void run() {
    while (running) {
      try {
        privatePumpEvents();
      } catch (java.lang.Throwable t) {
        // There's no vm.debug in Android!
        System.out.println("---------------------------");
        System.out.println(">>>>>>> CAUGHT UNHANDLED EXCEPTION IN EVENT THREAD:");
        t.printStackTrace();
      }
    }
  }

  boolean eventAvailable() {
    return eventQueue.getSize() > 0;
  }

  void pumpEvents() {
    if (Thread.currentThread() == this) {
      privatePumpEvents();
    }
  }

  void privatePumpEvents() {
    // This gives the system CPU some breathing room when in a tight event
    // loop and no events are posted.
    final TCEvent event = popTime <= 0 ? (TCEvent) eventQueue.pop() : (TCEvent) eventQueue.popWait(popTime);
    if (event != null) {
      if (event.type == INVOKE_IN_EVENT_THREAD) {
        event.r.run();
        // If they are waiting for this, then notify.
        if (event.synch != null) {
          synchronized (event.synch) {
            event.synch.notify();
          }
        }
      } else {
        win._postEvent(event.type, event.key, event.x, event.y, event.modifiers, event.timestamp);
      }
    }
  }

  public void pushEvent(int type, int key, int x, int y, int modifiers, int timestamp) {
    eventQueue.push(new TCEvent(type, key, x, y, modifiers, timestamp));
  }

  boolean hasEvent(int type) {
    Node n = eventQueue.queue;
    while (n != null) {
      TCEvent ev = (TCEvent) n.o;
      if (ev.type == type) {
        return true;
      }
      n = n.next;
    }
    return false;
  }

  public void invokeInEventThread(boolean wait, Runnable r) {
    if (!wait) {
      eventQueue.push(new TCEvent(INVOKE_IN_EVENT_THREAD, r));
    } else {
      // 9/16/02 - Andy - I have modified the code below so that it now checks
      // to see if we are currently in the event thread.  If we are in the
      // event thread, then we will simply call r.run() directly, thus
      // avoiding a deadlock if invokeInEventThread() was called from an event handler.

      // Are we currently in the event thread?
      java.lang.Thread current = java.lang.Thread.currentThread();
      if (current.equals(this)) {
        r.run(); // Execute directly.
      } else {
        // We are not in the event thread, so push to event thread.
        // We have to create a new Object, in case there is more than one
        // thread waiting on an invoke.
        Object synch = new Object();
        synchronized (synch) {
          eventQueue.push(new TCEvent(INVOKE_IN_EVENT_THREAD, r, synch));
          try {
            synch.wait();
          } catch (Exception ie) {
          }
        }
      }
    }
  }

  private class Node {
    Object o;
    Node next;

    Node(Object obj) {
      o = obj;
    }
  }

  /** Implements a simple FIFO queue of unlimited size.  This queue is thread safe, including
   * functionality that allows one thread to wait for another to push an object onto the queue.    */
  private class Queue {
    private Node queue;
    private Node end;
    private int size;

    /** Push an object on the queue and notify any waiting threads. */
    synchronized void push(Object o) {
      Node node = new Node(o);
      if (queue == null) {
        queue = end = node;
      } else {
        end.next = node;
        end = node;
      }
      size++;
      notify();
    }

    /** Returns the oldest object in the queue or null if no objects in the queue */
    synchronized Object pop() {
      if (queue == null || size == 0) {
        return null;
      }

      Object ret = queue.o;
      queue = queue.next;
      size--;
      return ret;
    }

    /** Returns the oldest object in the queue.  If there are no object in the queue, then this method will block until an object is placed in the queue from another
     * thread, or the specified time expires.  If the time expires and no object has been placed in the queue, this method will return null.
     */
    synchronized Object popWait(int maxTime) {
      long begin;

      try {
        begin = System.currentTimeMillis();
        int timeLeft = (int) (maxTime - (System.currentTimeMillis() - begin));
        while (queue == null && timeLeft > 0) {
          this.wait(timeLeft);
        }
      } catch (Exception ie) {
      }

      return pop();
    }

    synchronized int getSize() {
      return size;
    }
  }

  private class TCEvent {
    int type;
    int key;
    int x;
    int y;
    int modifiers;
    int timestamp;
    Runnable r;
    Object synch;

    TCEvent(int type, int key, int x, int y, int modifiers, int timestamp) {
      this.type = type;
      this.key = key;
      this.x = x;
      this.y = y;
      this.modifiers = modifiers;
      this.timestamp = timestamp;
    }

    TCEvent(int type, Runnable r) {
      this.type = type;
      this.r = r;
    }

    TCEvent(int type, Runnable r, Object synch) {
      this(type, r);
      this.synch = synch;
    }
  }
}
