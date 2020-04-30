// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

import totalcross.sys.Vm;
import totalcross.util.concurrent.Lock;

/** 
 * A simple <b>preemptive</b> thread model.
 * <b>Important:</b> the <code>synchronized</code> keyword is not implemented by
 * the TotalCross VM.
 * Note that in PalmOS while the user is writing in the grafitti area all events
 * (including threads) are blocked (this is a Operating System behaviour).
 * <br><br>
 * IMPORTANT: the totalcross.lang package is the java.lang that will be used in the device.
 * You CANNOT use nor import totalcross.lang package in desktop. When tc.Deploy is called,
 * all references to java.lang are replaced by totalcross.lang automatically. Given this,
 * you must use only the classes and methods that exists BOTH in java.lang and totalcross.lang.
 * For example, you can't use java.lang.ClassLoader because there are no totalcross.lang.ClassLoader.
 * Another example, you can't use java.lang.String.indexOfIgnoreCase because there are no
 * totalcross.lang.String.indexOfIgnoreCase method. Trying to use a class or method from the java.lang package
 * that has no correspondence with totalcross.lang will make the tc.Deploy program to abort, informing
 * where the problem occured. A good idea is to always refer to this javadoc to know what is and what isn't
 * available.
 * @see Runnable
 */
public abstract class Thread4D implements Runnable {
  byte[] taskID;
  private Runnable r;
  int priority = 5;
  private boolean alive = true;
  private String name;
  private final long tid;

  private static long threadSeqNumber;
  private static Lock threadSeqNumberLock = new Lock();

  /** The minimum priority that a thread can have. */
  public final static int MIN_PRIORITY = 1;

  /** The default priority that is assigned to a thread. */
  public final static int NORM_PRIORITY = 5;

  /** The maximum priority that a thread can have. */
  public final static int MAX_PRIORITY = 10;

  /**
   * Creates a new thread using a newly generated name in the format "Thread-n",
   * where "n" is the thread's ID.
   */
  public Thread4D() {
    this(null, null);
  }

  /**
   * Creates a new thread with the given name.
   * @param name the name of the new thread or <code>null</code> to use a newly
   * generated name in the format "Thread-n", where "n" is the thread's ID.
   * @since TotalCross 1.22
   */
  public Thread4D(String name) {
    this(null, name);
  }

  /**
   * Creates a new thread with the given runnable object and using a newly generated
   * name in the format "Thread-n", where "n" is the thread's ID.
   * @param r the object whose run method is called.
   */
  public Thread4D(Runnable r) {
    this(r, null);
  }

  /**
   * Creates a new thread with the given runnable object and name.
   * @param r the object whose run method is called.
   * @param name the name of the new thread or <code>null</code> to use a newly
   * generated name in the format "Thread-n", where "n" is the thread's ID.
   * @since TotalCross 1.22
   */
  public Thread4D(Runnable r, String name) {
    synchronized (threadSeqNumberLock) {
      tid = ++threadSeqNumber;
    }

    this.r = r;
    this.name = name == null ? "Thread-" + tid : name;
  }

  /** Returns the thread id, which is a number generated automatically. */
  public long getId() {
    return tid;
  }

  /**
   * Returns a string representation of this thread, including the 
   * thread's name and priority.
   * @return a string representation of this thread.
   */
  @Override
  public String toString() {
    return "Thread[" + name + "," + priority + "]";
  }

  /**
   * Returns this thread's name.
   * @return this thread's name.
   * @since TotalCross 1.22
   */
  public final String getName() {
    return name;
  }

  /**
   * Sets this thread's name.
   * @param name the new name for this thread.
   * @since TotalCross 1.22
   */
  public final void setName(String name) {
    this.name = name;
  }

  /**
   * Returns this thread's priority.
   * @return this thread's priority.
   * @since TotalCross 1.22
   */
  public final int getPriority() {
    return priority;
  }

  /** Sets the thread priority, ranging from #MIN_PRIORITY to #MAX_PRIORITY.
   * Must be set BEFORE <code>start</code> is called, otherwise it will have no effect
   * (some platforms do not allow the priority change after it is started).
   * @see #MIN_PRIORITY
   * @see #NORM_PRIORITY
   * @see #MAX_PRIORITY
   */
  public final void setPriority(int priority) {
    this.priority = priority;
  }

  /** Returns true if this thread is alive. */
  public final boolean isAlive() {
    return alive;
  }

  /** Called by the vm to run the thread. */
  @Override
  public void run() {
    if (r != null) {
      r.run();
    }
  }

  /** Give up on the timeslice of the current thread and goes to the next thread in the round-robin queue. */
  native public static void yield();
  
  public static void sleep(long millis) throws InterruptedException {
	  if (millis < 0) {
		  return;
	  } else if (millis == 0) {
		  Thread.yield();
	  }
	  
	  int loops = (int) (millis <<31); // quantidade de vezes em que 2**31 foi ultrapassado, incluindo 2**31
	  int _31in = ~(1 <<31); // 31 bits menos significativos ligados, em 32 bits; equivalente a MAXINT
	  for (int i = loops; i > 0; i--) {
	    Vm.sleep(_31in);
	  }
	  Vm.sleep(loops); // para cada loop, eu deixei de levar em consideração 1 milissegundo
	  Vm.sleep((int) (millis & _31in)); // dorme milis%2**31 milissegundos
  }

  /** Starts a thread. The same thread cannot be started twice. */
  native public void start();

  /** Returns a reference to the currently running thread.
   * @since TotalCross 1.22
   */
  native public static Thread currentThread(); // guich@tc122_6
}