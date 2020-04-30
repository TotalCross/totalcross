// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
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
import totalcross.ui.MainWindow;

/**
 * AsyncTask provides use of asynchronous operations without ocupying UI Thread
 * with non user interface tasks. In addition, you can easily publish results
 * without the need of manupulating Thread, see the example bellow (Copy and
 * paste this code inside a Container instance):
 * 
 * <pre>
 * Button dldButton = new Button("download zip");
 * add(dldButton, CENTER, CENTER);
 *
 * final ProgressBar progressBar = new ProgressBar();
 * add(progressBar, CENTER, AFTER + UnitsConverter.toPixels(DP + 16),
 *         PARENTSIZE + 80, PREFERRED);
 *
 * dldButton.addPressListener((c) -> {
 *     new AsyncTask()<Void, Void, Void> {
 *         int progress = 0;
 *         UpdateListener updateListener = null;
 *
 *         &#64;Override
 *         protected Object doInBackground(Object... objects) {
 *             HttpStream.Options o = new HttpStream.Options();
 *             o.httpType = HttpStream.GET;
 *             final String url = "<INSERT AN URL TO DOWNLOAD A ZIP FILE>";
 *
 *             if(url.startsWith("https:"))
 *                 o.socketFactory = new SSLSocketFactory();
 *
 *             try {
 *                 HttpStream p = new HttpStream(new URI(url));
 *                 File f = new File("file.zip", File.CREATE_EMPTY);
 *                 int totalSize = p.contentLength;
 *                 byte [] buff = new  byte[4096];
 *                 BufferedStream bs = new BufferedStream(f, BufferedStream.WRITE, 4096);
 *                 int counter = 0;
 *                 while(true) {
 *                     int size = p.readBytes(buff, 0, buff.length);
 *                     counter += size;
 *                     progress = (int)((counter/(double)totalSize)*100);
 *                     if(size <= 0) break;
 *                     bs.writeBytes(buff, 0, size);
 *                 }
 *                 progress = 100;
 *                 bs.close();
 *                 p.close();
 *                 f.close();
 *             } catch (IOException e) {
 *                 e.printStackTrace();
 *             }
 *             return null;
 *         }
 *
 *         &#64;Override
 *         protected void onPreExecute() {
 *             dldButton.setEnabled(false);
 *             MainWindow.getMainWindow().addUpdateListener(updateListener = (elapsed) -> {
 *                 progressBar.setValue(progress);
 *             });
 *         }
 *
 *         &#64;Override
 *         protected void onPostExecute(Object result) {
 *             dldButton.setEnabled(true);
 *             MainWindow.getMainWindow().removeUpdateListener(updateListener);
 *         }
 *     }.execute();
 * });
 *
 * </pre>
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
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