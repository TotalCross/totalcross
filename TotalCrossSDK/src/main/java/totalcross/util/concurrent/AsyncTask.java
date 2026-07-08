// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.util.concurrent;

import java.util.LinkedList;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/**
 * Provides a simple background task abstraction that can publish progress and
 * marshal callbacks back to the UI thread.
 *
 * <p>Example:</p>
 * <pre>{@code
 * new AsyncTask<Void, Integer, Void>() {
 *   @Override
 *   protected Void doInBackground(Void... params) {
 *     publishProgress(50);
 *     return null;
 *   }
 * }.execute();
 * }</pre>
 *
 * @param <Params> input parameter types for {@link #doInBackground(Object[])}
 * @param <Progress> progress values published to {@link #onProgressUpdate(Object[])}
 * @param <Result> result type returned from {@link #doInBackground(Object[])}
 *
 * @since TotalCross 4.3.9
 */
public abstract class AsyncTask<Params, Progress, Result> {

    /**
     * A single instance responsible for creating an unique thread that will receive
     * Runnable instances to execute their run methods and will sleep in ausence of
     * instance to be executed.
     */
    static class Executor implements Runnable {
	/**
	 * object used to lock synchronized block
	 */
	private final Lock lock = new Lock();
	/**
	 * list of Runnable instances to be executed
	 */
	private final LinkedList<Runnable> requests = new LinkedList<>();
	/**
	 * single instance of Executor
	 */
	private static Executor instance;

	private Executor() {
	}

	/**
	 * Get execute single instance and run is self if not already created
	 * 
	 * @return
	 */
	public static Executor getInstance() {
	    if (instance == null) {
		instance = new Executor();
		new Thread(instance).start();
	    }
	    return instance;
	}

	/**
	 * Execute a Runnable
	 * 
	 * @param request
	 */
	public void execute(Runnable request) {
	    synchronized (lock) {
		requests.addLast(request);
	    }
	}

	@Override
	public void run() {
	    while (true) {
		Runnable r = null;
		synchronized (lock) {
		    r = requests.poll();
		}
		if (r != null) {
		    try {
			r.run();
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		Vm.safeSleep(40);
	    }
	}
    }

    /**
     * Process task on pre execute in the thread in which this method was called
     */
    protected void onPreExecute() {
    }

    /**
     * Method in background thread when execute method is called
     * 
     * @see #execute(java.lang.Object[])
     * @param params
     * @return
     */
    protected abstract Result doInBackground(Params... params);

    /**
     * Override this method if you want to Provide progress to the UI Thread when
     * calling publishProgress inside doInBackground.
     * 
     * @see #doInBackground(java.lang.Object[])
     * @see #publishProgress(java.lang.Object[])
     * @param values
     */
    protected void onProgressUpdate(Progress... values) {
    }

    /**
     * Calls {@link #onProgressUpdate(java.lang.Object[])} in UI Thread
     * 
     * @see #onProgressUpdate(java.lang.Object[])
     * @param values
     */
    @SafeVarargs
    protected final void publishProgress(Progress... values) {
	MainWindow.getMainWindow().runOnMainThread(new Runnable() {
	    @Override
	    public void run() {
		onProgressUpdate(values);
	    }
	});
    }

    /**
     * Method called in UI Thread when the task executed in
     * {@link #doInBackground(java.lang.Object[])} is finished.
     * 
     * @see #doInBackground(java.lang.Object[])
     * @param result
     */
    protected void onPostExecute(Result result) {
    }

    /**
     * execute {@link #doInBackground(java.lang.Object[])} outside UI Thread
     * 
     * @param params
     * @return
     */
    @SafeVarargs
    public final AsyncTask<Params, Progress, Result> execute(final Params... params) {
	onPreExecute();
	Executor.getInstance().execute(new Runnable() {
	    @Override
	    public void run() {
		final Result result = doInBackground(params);
		MainWindow.getMainWindow().runOnMainThread(new Runnable() {
		    @Override
		    public void run() {
			onPostExecute(result);
		    }
		});
	    }
	});
	return this;
    }
}
