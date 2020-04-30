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

package totalcross.util.concurrent;

import java.util.LinkedList;

import totalcross.sys.Vm;

/**
 * Pool that executes each submitted task using one of possibly several pooled
 * threads. <br>
 * <br>
 * Thread pools address two different problems: they usually provide improved
 * performance when executing large numbers of asynchronous tasks, due to
 * reduced per-task invocation overhead, and they provide a means of bounding
 * and managing the resources, including threads, consumed when executing a
 * collection of tasks.
 *
 * @since TotalCross 4.3.9
 */
public class ThreadPool {
    private boolean keepRunning = true;
    private final int corePoolSize;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    private final Lock lock = new Lock();

    /**
     * Creates a new ThreadPool with the given initial parameters.
     * 
     * @param corePoolSize - the number of threads to keep in the pool, even if they
     *                     are idle
     */
    public ThreadPool(int corePoolSize) {
	this.corePoolSize = corePoolSize;
	queue = new LinkedList<>();
	threads = new PoolWorker[corePoolSize];

	for (int i = 0; i < corePoolSize; i++) {
	    threads[i] = new PoolWorker();
	    threads[i].start();
	}
    }

    /**
     * Executes the given task sometime in the future. The task may execute in a new
     * thread or in an existing pooled thread. If the task cannot be submitted for
     * execution, either because this executor has been shutdown or because its
     * capacity has been reached, the task is handled by the current
     * RejectedExecutionHandler.
     * 
     * @param command
     * @throws NullPointerException if command is null
     */
    public void execute(Runnable command) throws NullPointerException {
	if (command == null) {
	    throw new NullPointerException();
	}
	synchronized (lock) {
	    if (keepRunning) {
		queue.addLast(command);
	    }
	}
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted. Invocation has no additional
     * effect if already shut down.
     */
    public void shutdown() {
	this.keepRunning = false;
    }

    /**
     * Invokes shutdown when this executor is no longer referenced and it has no
     * threads.
     */
    @Override
    protected void finalize() throws Throwable {
	this.shutdown();
    }

    private class PoolWorker extends Thread {
	public void run() {
	    while (true) {
		Runnable r = null;
		synchronized (lock) {
		    r = queue.poll();
		}
		if (r != null) {
		    try {
			r.run();
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		} else if (!keepRunning) {
		    break;
		}
		Vm.safeSleep(40);
	    }
	}
    }
}