// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android.compat;

import static totalcross.android.compat.Level5.ERROR;
import static totalcross.android.compat.Level5.NO_ERROR;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import totalcross.AndroidUtils;
import totalcross.Launcher4A;

/** Owns device discovery and discoverability, but never client/server transport sockets. */
final class BluetoothDiscovery {
    private static final int DISCOVERABILITY_REQUEST_BASE = 0x7000;
    private static final int DISCOVERABILITY_REQUEST_MASK = 0x0fff;
    private static final AtomicInteger DISCOVERABILITY_REQUEST_IDS = new AtomicInteger();

    private final Level5Impl owner;
    private final Handler mainHandler;
    private final ThreadPoolExecutor connectionExecutor;
    private final ThreadPoolExecutor discoveryControlExecutor;
    private final AtomicReference<DiscoverabilityOperation> activeDiscoverability = new AtomicReference<>();
    private final ConcurrentHashMap<Integer, Boolean> discoverabilityRequestCodes = new ConcurrentHashMap<>();
    private final AtomicReference<DiscoveryOperation> activeDiscovery = new AtomicReference<>();

    BluetoothDiscovery(Level5Impl owner, Handler mainHandler, ThreadPoolExecutor connectionExecutor,
                       ThreadPoolExecutor discoveryControlExecutor) {
        this.owner = owner;
        this.mainHandler = mainHandler;
        this.connectionExecutor = connectionExecutor;
        this.discoveryControlExecutor = discoveryControlExecutor;
    }

    void shutdown() {
        DiscoveryOperation scanning = activeDiscovery.get();
        if (scanning != null) finishDiscovery(scanning, false, true);
        DiscoverabilityOperation visible = activeDiscoverability.getAndSet(null);
        if (visible != null) visible.request.cancel();
    }

    private enum DiscoveryState { NOT_STARTED, STARTING, STARTED, CANCELLING, FINISHED }

    void makeDiscoverable(final BluetoothRequest request) throws Exception {
        if (request.isComplete()) return;
        if (activeDiscoverability.get() != null) {
            request.complete(ERROR, null);
            return;
        }
        BluetoothAdapter adapter = owner.getAdapter();
        if (adapter == null) {
            request.complete(ERROR, null);
            return;
        }
        if (adapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            request.complete(NO_ERROR, null);
            return;
        }
        int activityRequestCode = reserveDiscoverabilityRequestCode();
        if (activityRequestCode < 0) {
            request.complete(ERROR, null);
            return;
        }
        final DiscoverabilityOperation operation = new DiscoverabilityOperation(request, activityRequestCode);
        if (!activeDiscoverability.compareAndSet(null, operation)) {
            discoverabilityRequestCodes.remove(activityRequestCode);
            request.complete(ERROR, null);
            return;
        }
        request.registerCompletionCallback(() -> {
            synchronized (operation) {
                activeDiscoverability.compareAndSet(operation, null);
                if (!operation.activityLaunched || !request.isCancelled()) {
                    discoverabilityRequestCodes.remove(operation.activityRequestCode);
                }
            }
        });
        boolean posted = mainHandler.post(() -> {
            if (request.isDeadlineExpired()) request.cancel();
            if (request.isComplete() || activeDiscoverability.get() != operation || owner.isShuttingDown()) return;
            LoaderHost host = currentLoader();
            if (host == null) {
                request.complete(ERROR, null);
                return;
            }
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            synchronized (operation) {
                if (request.isComplete() || activeDiscoverability.get() != operation || owner.isShuttingDown()) return;
                operation.activityLaunched = true;
                try {
                    host.loader.startActivityForResult(intent, operation.activityRequestCode);
                } catch (RuntimeException failure) {
                    operation.activityLaunched = false;
                    AndroidUtils.handleException(failure, false);
                    request.complete(ERROR, null);
                }
            }
        });
        if (!posted) {
            request.complete(ERROR, null);
        }
    }

    private static final class LoaderHost {
        final totalcross.android.Loader loader;
        LoaderHost(totalcross.android.Loader loader) { this.loader = loader; }
    }

    private static LoaderHost currentLoader() {
        totalcross.android.Loader loader = Launcher4A.loader;
        return loader == null ? null : new LoaderHost(loader);
    }

    boolean handleActivityResult(int requestCode, int resultCode) {
        if (!isDiscoverabilityRequestCode(requestCode)) return false;
        discoverabilityRequestCodes.remove(requestCode);
        DiscoverabilityOperation operation = activeDiscoverability.get();
        if (owner.isShuttingDown()) {
            if (operation != null && operation.activityRequestCode == requestCode)
                activeDiscoverability.compareAndSet(operation, null);
            return true;
        }
        if (operation != null && operation.activityRequestCode == requestCode
                && activeDiscoverability.compareAndSet(operation, null)
                && operation.activityLaunched && !operation.request.isComplete()) {
            operation.request.complete(resultCode == Activity.RESULT_CANCELED ? ERROR : NO_ERROR, null);
        }
        return true;
    }

    private int reserveDiscoverabilityRequestCode() {
        for (int i = 0; i <= DISCOVERABILITY_REQUEST_MASK; i++) {
            int requestCode = DISCOVERABILITY_REQUEST_BASE
                    + (DISCOVERABILITY_REQUEST_IDS.getAndIncrement() & DISCOVERABILITY_REQUEST_MASK);
            if (discoverabilityRequestCodes.putIfAbsent(requestCode, Boolean.TRUE) == null) return requestCode;
        }
        return -1;
    }

    private static boolean isDiscoverabilityRequestCode(int requestCode) {
        return requestCode >= DISCOVERABILITY_REQUEST_BASE
                && requestCode <= DISCOVERABILITY_REQUEST_BASE + DISCOVERABILITY_REQUEST_MASK;
    }

    private static final class DiscoveryOperation {
        final BluetoothRequest request;
        final BluetoothAdapter adapter;
        final ReentrantLock transitionLock = new ReentrantLock();
        final AtomicBoolean finished = new AtomicBoolean();
        final AtomicBoolean cleanupStarted = new AtomicBoolean();
        final AtomicBoolean cancelRequested = new AtomicBoolean();
        final AtomicBoolean cancelEnqueued = new AtomicBoolean();
        final AtomicBoolean cancelFallbackEnqueued = new AtomicBoolean();
        final AtomicInteger cancellationAttempts = new AtomicInteger();
        final AtomicBoolean finishStateRequested = new AtomicBoolean();
        final AtomicInteger pendingDeviceQueries = new AtomicInteger();
        final Map<String, String> found = new LinkedHashMap<>();
        volatile BroadcastReceiver receiver;
        volatile totalcross.android.Loader owner;
        final AtomicBoolean receiverRegistered = new AtomicBoolean();
        volatile boolean startRequested;
        volatile boolean discoveryFinished;
        volatile boolean finishSuccess;
        DiscoveryState state = DiscoveryState.NOT_STARTED;

        DiscoveryOperation(BluetoothRequest request, BluetoothAdapter adapter) {
            this.request = request;
            this.adapter = adapter;
        }
    }

    private static final class DiscoverabilityOperation {
        final BluetoothRequest request;
        final int activityRequestCode;
        volatile boolean activityLaunched;

        DiscoverabilityOperation(BluetoothRequest request, int activityRequestCode) {
            this.request = request;
            this.activityRequestCode = activityRequestCode;
        }
    }

    void getUnpairedDevices(final BluetoothRequest request) {
        final BluetoothAdapter adapter;
        try {
            adapter = owner.getAdapter();
        } catch (RuntimeException failure) {
            request.complete(ERROR, null);
            return;
        }
        if (adapter == null) {
            request.complete(ERROR, null);
            return;
        }
        final DiscoveryOperation operation = new DiscoveryOperation(request, adapter);
        if (!activeDiscovery.compareAndSet(null, operation)) {
            DiscoveryOperation previous = activeDiscovery.get();
            if (previous != null && previous.cancelRequested.get()) {
                try {
                    discoveryControlExecutor.execute(() -> cancelDiscoveryOnWorker(previous));
                } catch (RejectedExecutionException rejected) {
                    Level5Impl.reportCleanupFailure(rejected);
                }
            }
            request.complete(ERROR, null);
            return;
        }
        request.registerCancellationCallback(() -> finishDiscovery(operation, false, true));
        if (!mainHandler.post(() -> setupDiscoveryOnMain(operation))) {
            finishDiscovery(operation, false, false);
        }
    }

    private void setupDiscoveryOnMain(final DiscoveryOperation operation) {
        if (operation.request.isDeadlineExpired()) operation.request.cancel();
        if (operation.request.isComplete() || activeDiscovery.get() != operation) {
            finishDiscovery(operation, false, false);
            return;
        }
        final totalcross.android.Loader loader = Launcher4A.loader;
        if (loader == null) {
            finishDiscovery(operation, false, false);
            return;
        }
        operation.owner = loader;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    operation.discoveryFinished = true;
                    if (operation.finished.get() || operation.request.isComplete() || owner.isShuttingDown()) return;
                    if (operation.pendingDeviceQueries.get() == 0) finishDiscovery(operation, true, false);
                    return;
                }
                if (operation.finished.get() || operation.request.isComplete() || owner.isShuttingDown()) return;
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    final BluetoothDevice device;
                    try {
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    } catch (RuntimeException invalidDevice) {
                        return;
                    }
                    if (device == null || operation.finished.get() || operation.discoveryFinished) return;
                    operation.pendingDeviceQueries.incrementAndGet();
                    try {
                        connectionExecutor.execute(() -> {
                            try {
                                if (operation.finished.get()) return;
                                String address = device.getAddress();
                                address = Level5Impl.normalizeBluetoothAddressOrNull(address);
                                if (address == null) return;
                                String name = device.getName();
                                synchronized (operation.found) {
                                    operation.found.put(address, name == null ? "" : name);
                                }
                            } catch (RuntimeException invalidDevice) {
                                // Discovery can deliver a device that disappears before it is queried.
                            } finally {
                                if (operation.pendingDeviceQueries.decrementAndGet() == 0
                                        && operation.discoveryFinished) {
                                    finishDiscovery(operation, true, false);
                                }
                            }
                        });
                    } catch (RejectedExecutionException ignored) {
                        if (operation.pendingDeviceQueries.decrementAndGet() == 0
                                && operation.discoveryFinished) {
                            finishDiscovery(operation, true, false);
                        }
                    }
                }
            }
        };
        operation.receiver = receiver;
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        try {
            loader.registerReceiver(receiver, filter);
            operation.receiverRegistered.set(true);
        } catch (RuntimeException failure) {
            finishDiscovery(operation, false, false);
            return;
        }
        operation.startRequested = true;
        try {
            discoveryControlExecutor.execute(() -> startDiscoveryOnWorker(operation));
        } catch (RejectedExecutionException rejected) {
            finishDiscovery(operation, false, false);
        }
    }

    private void startDiscoveryOnWorker(DiscoveryOperation operation) {
        boolean completeWithError = false;
        RuntimeException startFailure = null;
        RuntimeException cancelFailure = null;
        if (operation.request.isDeadlineExpired()) operation.request.cancel();
        operation.transitionLock.lock();
        try {
            boolean cancelledBeforeStart = operation.cancelRequested.get() || operation.finished.get()
                    || operation.request.isComplete() || owner.isShuttingDown()
                    || activeDiscovery.get() != operation || operation.state != DiscoveryState.NOT_STARTED;
            if (cancelledBeforeStart) {
                operation.state = DiscoveryState.FINISHED;
                completeWithError = true;
            } else {
                operation.state = DiscoveryState.STARTING;
                boolean started = false;
                try {
                    started = operation.adapter.startDiscovery();
                } catch (RuntimeException failure) {
                    startFailure = failure;
                }

                boolean cancelledDuringStart = operation.cancelRequested.get() || operation.request.isCancelled()
                        || owner.isShuttingDown() || activeDiscovery.get() != operation;
                if (cancelledDuringStart || startFailure != null || !started) {
                    operation.state = DiscoveryState.CANCELLING;
                    if (activeDiscovery.get() == operation)
                        cancelFailure = cancelAdapterDiscoveryLocked(operation);
                    if (cancelFailure != null) operation.cancelRequested.set(true);
                    operation.state = cancelFailure == null ? DiscoveryState.FINISHED : DiscoveryState.CANCELLING;
                    completeWithError = cancelledDuringStart || startFailure != null || !started;
                } else if (operation.finishStateRequested.get() || operation.finished.get()) {
                    operation.state = DiscoveryState.FINISHED;
                } else {
                    operation.state = DiscoveryState.STARTED;
                }
            }
        } finally {
            operation.transitionLock.unlock();
        }

        if (startFailure != null) Level5Impl.reportCleanupFailure(startFailure);
        if (cancelFailure != null) Level5Impl.reportCleanupFailure(cancelFailure);
        if (operation.cancelRequested.get()) cancelDiscoveryOnWorker(operation);
        else if (operation.finished.get() && operation.finishStateRequested.get())
            completeDiscoveryCleanup(operation, operation.finishSuccess);
        if (completeWithError) finishDiscovery(operation, false, false);
    }

    private RuntimeException cancelAdapterDiscoveryLocked(DiscoveryOperation operation) {
        try {
            if (!operation.adapter.isDiscovering()) return null;
            boolean accepted = operation.adapter.cancelDiscovery();
            if (!accepted && operation.adapter.isDiscovering())
                return new IllegalStateException("Android Bluetooth discovery cancellation was rejected");
            return null;
        } catch (RuntimeException failure) {
            return failure;
        }
    }

    private void finishDiscovery(final DiscoveryOperation operation, final boolean success,
                                 final boolean cancelDiscovery) {
        if (cancelDiscovery) {
            operation.cancelRequested.set(true);
            if (operation.startRequested) cancelDiscoveryAsync(operation);
        }
        operation.finishSuccess = success && !cancelDiscovery;
        if (!operation.finished.compareAndSet(false, true)) return;
        if (cancelDiscovery && operation.startRequested) return;
        if (markDiscoveryStateFinishedWithoutWaiting(operation))
            completeDiscoveryCleanup(operation, operation.finishSuccess);
    }

    private void completeDiscoveryCleanup(final DiscoveryOperation operation, final boolean success) {
        if (!operation.cleanupStarted.compareAndSet(false, true)) return;
        Runnable cleanup = () -> {
            BroadcastReceiver receiver = operation.receiver;
            totalcross.android.Loader loader = operation.owner;
            if (receiver != null && loader != null && operation.receiverRegistered.compareAndSet(true, false)) {
                try {
                    loader.unregisterReceiver(receiver);
                } catch (RuntimeException ignored) {
                    // The receiver may already have been unregistered by Activity teardown.
                }
            }
            activeDiscovery.compareAndSet(operation, null);
            if (!operation.request.isComplete()) {
                operation.request.complete(success ? NO_ERROR : ERROR,
                        success ? discoveryResults(operation) : null);
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) cleanup.run();
        else if (!mainHandler.post(cleanup)) cleanup.run();
    }

    private void cancelDiscoveryAsync(final DiscoveryOperation operation) {
        if (!operation.cancelEnqueued.compareAndSet(false, true)) return;
        try {
            discoveryControlExecutor.execute(() -> cancelDiscoveryOnWorker(operation));
        } catch (RejectedExecutionException rejected) {
            Level5Impl.reportCleanupFailure(rejected);
            boolean canFinish = false;
            if (operation.transitionLock.tryLock()) {
                try {
                    if (operation.state == DiscoveryState.NOT_STARTED) {
                        operation.state = DiscoveryState.FINISHED;
                        canFinish = true;
                    } else if (operation.state == DiscoveryState.FINISHED) {
                        canFinish = true;
                    } else {
                        operation.state = DiscoveryState.CANCELLING;
                    }
                } finally {
                    operation.transitionLock.unlock();
                }
            }
            if (canFinish) {
                completeDiscoveryCleanup(operation, false);
            } else if (operation.cancelFallbackEnqueued.compareAndSet(false, true)) {
                owner.enqueueDiscoveryCleanup(operation, () -> cancelDiscoveryOnWorker(operation), closed -> {
                    if (!closed) Level5Impl.reportCleanupFailure(new IllegalStateException(
                            "Bluetooth discovery cancellation remains unresolved"));
                });
            }
        }
    }

    private void cancelDiscoveryOnWorker(DiscoveryOperation operation) {
        RuntimeException failure = null;
        boolean resolved = false;
        operation.transitionLock.lock();
        try {
            if (operation.state == DiscoveryState.NOT_STARTED) {
                operation.state = DiscoveryState.FINISHED;
                resolved = true;
            } else if (operation.state == DiscoveryState.FINISHED) {
                resolved = true;
            } else if (activeDiscovery.get() == operation) {
                operation.state = DiscoveryState.CANCELLING;
                failure = cancelAdapterDiscoveryLocked(operation);
                if (failure == null) {
                    operation.state = DiscoveryState.FINISHED;
                    resolved = true;
                }
            } else {
                operation.state = DiscoveryState.FINISHED;
                resolved = true;
            }
        } finally {
            operation.transitionLock.unlock();
        }
        if (failure != null) {
            Level5Impl.reportCleanupFailure(failure);
            if (operation.cancellationAttempts.incrementAndGet() <= 1) {
                try {
                    discoveryControlExecutor.execute(() -> cancelDiscoveryOnWorker(operation));
                    return;
                } catch (RejectedExecutionException rejected) {
                    Level5Impl.reportCleanupFailure(rejected);
                }
            }
            // Keep activeDiscovery reserved when Android may still be discovering.
            // A subsequent explicit request may retry the unresolved cancellation.
            return;
        }
        if (resolved) completeDiscoveryCleanup(operation, false);
    }

    private boolean markDiscoveryStateFinishedWithoutWaiting(DiscoveryOperation operation) {
        if (!operation.transitionLock.tryLock()) {
            operation.finishStateRequested.set(true);
            return false;
        }
        try {
            if (operation.cancelRequested.get() && operation.startRequested
                    && operation.state != DiscoveryState.NOT_STARTED
                    && operation.state != DiscoveryState.FINISHED) {
                operation.state = DiscoveryState.CANCELLING;
                return false;
            }
            operation.state = DiscoveryState.FINISHED;
            return true;
        } finally {
            operation.transitionLock.unlock();
        }
    }

    private static String[] discoveryResults(DiscoveryOperation operation) {
        List<String> results = new ArrayList<>();
        synchronized (operation.found) {
            for (Map.Entry<String, String> device : operation.found.entrySet()) {
                results.add(device.getKey() + "|" + device.getValue());
            }
        }
        return results.isEmpty() ? null : results.toArray(new String[0]);
    }


}
