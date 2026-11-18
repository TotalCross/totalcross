// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android.compat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import totalcross.AndroidUtils;
import totalcross.Launcher4A;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Level5Impl extends Level5 {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Present on BOND_NONE broadcasts; value 1 is the platform authentication-failed reason.
    private static final String EXTRA_UNBOND_REASON = "android.bluetooth.device.extra.REASON";
    private static final int UNBOND_REASON_AUTH_FAILED = 1;
    private static final int DISCOVERABILITY_REQUEST_BASE = 0x7000;
    private static final int DISCOVERABILITY_REQUEST_MASK = 0x0fff;
    private static final int MAX_EMERGENCY_CLEANUP_THREADS = 4;
    private static final int MAX_DEFERRED_CLIENT_CLEANUPS = 256;
    private static final int CLIENT_SOCKET_CLOSE_MAX_ATTEMPTS = 3;
    private static final long CLEANUP_RETRY_INTERVAL_MS = 250;
    private static final long CLEANUP_SHUTDOWN_TIMEOUT_MS = 10000;
    private static final AtomicLong THREAD_IDS = new AtomicLong();
    private static final AtomicLong EMERGENCY_CLEANUP_THREAD_IDS = new AtomicLong();
    private static final AtomicInteger ACTIVE_EMERGENCY_CLEANUP_THREADS = new AtomicInteger();
    private static final AtomicInteger DISCOVERABILITY_REQUEST_IDS = new AtomicInteger();

    private final ThreadPoolExecutor connectionExecutor = boundedExecutor("connection", 2, 16);
    private final ThreadPoolExecutor discoveryControlExecutor = boundedExecutor("discovery-control", 1, 8);
    private final ThreadPoolExecutor ioExecutor = boundedExecutor("io", 4, 32);
    private final ThreadPoolExecutor socketControlExecutor = boundedExecutor("control", 2, 16);
    private final ThreadPoolExecutor serverAcceptExecutor = boundedExecutor("server", 1, 1);
    private final ThreadPoolExecutor cleanupExecutor = boundedExecutor("cleanup", 2, 64);
    private final ScheduledThreadPoolExecutor cleanupRetryDispatcher = createCleanupRetryDispatcher();
    private final ScheduledThreadPoolExecutor timeoutExecutor = createTimeoutExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile BluetoothAdapter btAdapter;
    private final ConcurrentHashMap<SessionKey, BluetoothSession> sessionsByKey = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BluetoothSession> sessionsByHandle = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BluetoothSocket> acceptedSockets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BluetoothSocket, SocketIoState> socketIoStates = new ConcurrentHashMap<>();
    private final AtomicLong sessionGenerations = new AtomicLong();
    private final AtomicLong serverGenerations = new AtomicLong();
    private final AtomicBoolean shuttingDown = new AtomicBoolean();
    private final ConcurrentHashMap<BluetoothRequest, Boolean> outstandingRequests = new ConcurrentHashMap<>();
    private final AtomicReference<DiscoverabilityOperation> activeDiscoverability = new AtomicReference<>();
    private final ConcurrentHashMap<Integer, Boolean> discoverabilityRequestCodes = new ConcurrentHashMap<>();
    private final AtomicReference<DiscoveryOperation> activeDiscovery = new AtomicReference<>();
    private final AtomicReference<ServerAcceptOperation> activeServerAccept = new AtomicReference<>();
    private final Object serverLifecycleLock = new Object();
    private final Object cleanupLock = new Object();
    private final Map<Object, PendingCleanup> pendingCleanups = new IdentityHashMap<>();
    // Counts active sockets and unresolved closes, not just deferred queue entries.
    private final IdentityHashMap<BluetoothSocket, Boolean> ownedClientSockets = new IdentityHashMap<>();
    private int clientSocketsBeingCreated;
    private final ArrayDeque<PendingCleanup> deferredClientCleanups = new ArrayDeque<>();
    private final AtomicBoolean cleanupDrainRunning = new AtomicBoolean();
    private long nextCleanupSequence;
    private boolean cleanupRetryDispatcherStarted;
    private boolean cleanupShutdownStarted;
    private boolean cleanupShutdownFinalizing;
    private boolean cleanupShutdownFinalized;
    private long cleanupShutdownDeadlineElapsed;
    private volatile BluetoothServerSocket serverSocket;
    private ServerLifecycleState serverLifecycleState = ServerLifecycleState.IDLE;
    private ServerCloseContext serverCloseContext;

    private interface RequestAction {
        void run() throws Exception;
    }

    private interface CleanupAction {
        boolean close() throws IOException;
    }

    private interface CleanupCallback {
        void completed(boolean closed);
    }

    private enum PendingCleanupState { QUEUED, RUNNING, DEFERRED, COMPLETED }

    private static final class PendingCleanup {
        final Object resource;
        final CleanupAction action;
        final List<CleanupCallback> callbacks = new ArrayList<>();
        PendingCleanupState state = PendingCleanupState.QUEUED;
        boolean inDeferredQueue;
        boolean deferredOverflow;
        boolean overflowReported;
        int closeAttempts;
        long nextAttemptElapsed;
        long deferredSequence;

        PendingCleanup(Object resource, CleanupAction action) {
            this.resource = resource;
            this.action = action;
        }
    }

    private static final class SocketIoState {
        final AtomicBoolean readActive = new AtomicBoolean();
        final AtomicBoolean writeActive = new AtomicBoolean();
    }

    private enum SessionState { CONNECTING, CONNECTED, CLOSING, CLOSED }

    private enum DiscoveryState { NOT_STARTED, STARTING, STARTED, CANCELLING, FINISHED }

    private enum ServerLifecycleState { IDLE, CLOSING, CLOSE_FAILED }

    private static final class SessionKey {
        final String normalizedAddress;
        final boolean secure;

        SessionKey(String normalizedAddress, boolean secure) {
            this.normalizedAddress = normalizedAddress;
            this.secure = secure;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SessionKey)) return false;
            SessionKey key = (SessionKey) other;
            return secure == key.secure && normalizedAddress.equals(key.normalizedAddress);
        }

        @Override
        public int hashCode() {
            return 31 * normalizedAddress.hashCode() + (secure ? 1 : 0);
        }
    }

    private static final class BluetoothSession {
        final SessionKey key;
        volatile long generation;
        final Set<String> handles = new HashSet<>();
        volatile SessionState state = SessionState.CONNECTING;
        volatile BluetoothSocket socket;

        BluetoothSession(SessionKey key, long generation) {
            this.key = key;
            this.generation = generation;
        }
    }

    private enum BondOutcome { BONDED, AUTHENTICATION_FAILED, FAILED, CANCELLED }

    private final class BondMonitor {
        final BluetoothDevice target;
        final String targetAddress;
        final BluetoothRequest request;
        final totalcross.android.Loader loader;
        final int initialBondState;
        final BroadcastReceiver receiver;
        final Object lock = new Object();
        final CountDownLatch registrationFinished = new CountDownLatch(1);
        final AtomicBoolean closed = new AtomicBoolean();
        volatile boolean registered;
        volatile RuntimeException registrationFailure;
        private int lastState;
        private boolean bondingObserved;
        private boolean bonded;
        private boolean bondFailed;
        private boolean authenticationFailed;

        BondMonitor(BluetoothDevice target, BluetoothRequest request, totalcross.android.Loader loader) {
            this.target = target;
            this.targetAddress = normalizeApiAddress(target.getAddress());
            this.request = request;
            this.loader = loader;
            this.initialBondState = target.getBondState();
            this.lastState = initialBondState;
            this.bondingObserved = initialBondState == BluetoothDevice.BOND_BONDING;
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (closed.get() || request.isComplete() || intent == null
                            || !BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) return;
                    final BluetoothDevice changed;
                    try {
                        changed = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    } catch (RuntimeException invalidDevice) {
                        return;
                    }
                    if (changed == null) return;
                    String changedAddress;
                    try {
                        changedAddress = changed.getAddress();
                    } catch (RuntimeException invalidDevice) {
                        return;
                    }
                    if (changedAddress == null || !normalizeApiAddress(changedAddress).equals(targetAddress)) return;
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, Integer.MIN_VALUE);
                    int previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                            Integer.MIN_VALUE);
                    int reason = intent.getIntExtra(EXTRA_UNBOND_REASON, Integer.MIN_VALUE);
                    recordBondState(state, previousState, reason);
                }
            };
        }

        boolean register() throws InterruptedException {
            request.registerCancellationCallback(this::signal);
            request.registerCancellationCallback(this::unregister);
            if (loader == null) {
                registrationFinished.countDown();
                return false;
            }
            boolean posted = mainHandler.post(() -> {
                try {
                    if (!closed.get() && !request.isComplete()) {
                        loader.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
                        registered = true;
                    }
                } catch (RuntimeException failure) {
                    registrationFailure = failure;
                } finally {
                    registrationFinished.countDown();
                }
            });
            if (!posted) registrationFinished.countDown();
            while (!request.isComplete() && registrationFinished.getCount() > 0) {
                long remaining = remainingDeadlineMillis(request);
                if (remaining <= 0) {
                    request.cancel();
                    break;
                }
                registrationFinished.await(Math.min(remaining, 50), TimeUnit.MILLISECONDS);
            }
            return registered && registrationFailure == null && !request.isComplete();
        }

        void recordBondState(int state, int previousState, int reason) {
            synchronized (lock) {
                if (state == BluetoothDevice.BOND_BONDING) bondingObserved = true;
                if (previousState == BluetoothDevice.BOND_BONDING) bondingObserved = true;
                if (state == BluetoothDevice.BOND_BONDED && bondingObserved) bonded = true;
                if (state == BluetoothDevice.BOND_NONE && bondingObserved
                        && (previousState == BluetoothDevice.BOND_BONDING
                        || lastState == BluetoothDevice.BOND_BONDING)) {
                    bondFailed = true;
                    authenticationFailed = reason == UNBOND_REASON_AUTH_FAILED;
                }
                lastState = state;
                lock.notifyAll();
            }
        }

        boolean hasObservedBonding() {
            synchronized (lock) {
                return bondingObserved;
            }
        }

        BondOutcome awaitOutcome() throws InterruptedException {
            synchronized (lock) {
                while (!bonded && !bondFailed && !request.isComplete()) {
                    long remaining = remainingDeadlineMillis(request);
                    if (remaining <= 0) {
                        request.cancel();
                        break;
                    }
                    lock.wait(remaining);
                }
                if (bonded) return BondOutcome.BONDED;
                if (authenticationFailed) return BondOutcome.AUTHENTICATION_FAILED;
                if (bondFailed) return BondOutcome.FAILED;
                return BondOutcome.CANCELLED;
            }
        }

        BondOutcome currentOutcome() {
            synchronized (lock) {
                if (bonded) return BondOutcome.BONDED;
                if (authenticationFailed) return BondOutcome.AUTHENTICATION_FAILED;
                if (bondFailed) return BondOutcome.FAILED;
                return BondOutcome.CANCELLED;
            }
        }

        void reconcileAfterConnectionFailure() {
            final int currentState;
            try {
                currentState = target.getBondState();
            } catch (RuntimeException failure) {
                return;
            }
            synchronized (lock) {
                if (currentState == BluetoothDevice.BOND_BONDING) {
                    bondingObserved = true;
                    bonded = false;
                    bondFailed = false;
                    authenticationFailed = false;
                } else if (currentState == BluetoothDevice.BOND_BONDED) {
                    if (bondingObserved || initialBondState == BluetoothDevice.BOND_NONE
                            || initialBondState == BluetoothDevice.BOND_BONDING) {
                        bondingObserved = true;
                        bonded = true;
                        bondFailed = false;
                        authenticationFailed = false;
                    } else {
                        bonded = false;
                    }
                } else if (currentState == BluetoothDevice.BOND_NONE) {
                    bonded = false;
                    if (bondingObserved) bondFailed = true;
                }
                lastState = currentState;
                lock.notifyAll();
            }
        }

        void signal() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        void unregister() {
            if (!closed.compareAndSet(false, true) || loader == null) return;
            Runnable cleanup = () -> {
                if (!registered) return;
                registered = false;
                try {
                    loader.unregisterReceiver(receiver);
                } catch (RuntimeException ignored) {
                    // Activity teardown may have already released the receiver.
                }
            };
            if (Looper.myLooper() == Looper.getMainLooper()) cleanup.run();
            else mainHandler.post(cleanup);
        }
    }

    private static ThreadPoolExecutor boundedExecutor(String name, int threads, int queueCapacity) {
        return new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity), namedDaemonFactory(name),
                new ThreadPoolExecutor.AbortPolicy());
    }

    private static String normalizeApiAddress(String address) {
        String normalized = normalizeBluetoothAddressOrNull(address);
        return normalized == null ? "" : normalized;
    }

    private static String normalizeBluetoothAddressOrNull(String address) {
        if (address == null) return null;
        boolean colonSeparated = address.length() == 17;
        if (!colonSeparated && address.length() != 12) return null;
        StringBuilder normalized = new StringBuilder(12);
        for (int i = 0; i < address.length(); i++) {
            char value = address.charAt(i);
            if (colonSeparated && i % 3 == 2) {
                if (value != ':') return null;
                continue;
            }
            if (!((value >= '0' && value <= '9') || (value >= 'a' && value <= 'f')
                    || (value >= 'A' && value <= 'F'))) return null;
            normalized.append(Character.toUpperCase(value));
        }
        return normalized.length() == 12 ? normalized.toString() : null;
    }

    private static long remainingDeadlineMillis(BluetoothRequest request) {
        long deadline = request.getDeadlineElapsedRealtime();
        return Math.max(0, deadline - SystemClock.elapsedRealtime());
    }

    private static ThreadFactory namedDaemonFactory(final String name) {
        return runnable -> {
            Thread thread = new Thread(runnable, "TotalCross-Bluetooth-" + name + "-" + THREAD_IDS.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    private static ScheduledThreadPoolExecutor createTimeoutExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, namedDaemonFactory("timeout"));
        if (Build.VERSION.SDK_INT >= 21) executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    private static ScheduledThreadPoolExecutor createCleanupRetryDispatcher() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                namedDaemonFactory("cleanup-retry"));
        if (Build.VERSION.SDK_INT >= 21) executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return executor;
    }

    private BluetoothAdapter getAdapter() {
        BluetoothAdapter adapter = btAdapter;
        if (adapter != null) return adapter;
        synchronized (this) {
            adapter = btAdapter;
            if (adapter == null) {
                adapter = BluetoothAdapter.getDefaultAdapter();
                btAdapter = adapter;
            }
        }
        return adapter;
    }

    @Override
    public void processMessage(final BluetoothRequest request) {
        if (request == null) return;
        if (shuttingDown.get()) {
            request.cancel();
            return;
        }
        if (request.isComplete()) return;
        outstandingRequests.put(request, Boolean.TRUE);
        request.registerCompletionCallback(() -> outstandingRequests.remove(request));
        if (shuttingDown.get()) {
            request.cancel();
            return;
        }
        if (request.isComplete()) return;
        int operation = request.getOperation();
        switch (operation) {
            case BT_CONNECT:
                if (isClientCleanupAdmissionBlocked()) request.complete(ERROR, null);
                else dispatch(request, connectionExecutor, () -> btConnect(request));
                break;
            case BT_READ:
            case BT_WRITE:
                dispatchIo(request, operation == BT_READ);
                break;
            case BT_CLOSE:
                dispatchClientClose(request);
                break;
            case BT_SERVER_CLOSE:
                dispatchServerClose(request);
                break;
            case BT_SERVER_ACCEPT:
                dispatchServerAccept(request);
                break;
            case BT_MAKE_DISCOVERABLE:
                dispatch(request, socketControlExecutor, () -> btMakeDiscoverable(request));
                break;
            case BT_GET_UNPAIRED_DEVICES:
                dispatch(request, connectionExecutor, () -> btGetUnpairedDevices(request));
                break;
            case BT_IS_SUPPORTED:
            case BT_IS_RADIO_ON:
            case BT_IS_DISCOVERABLE:
            case BT_ACTIVATE:
            case BT_DEACTIVATE:
            case BT_GET_PAIRED_DEVICES:
                dispatch(request, connectionExecutor, () -> processAdapterRequest(request));
                break;
            default:
                request.complete(ERROR, null);
                break;
        }
    }

    private boolean isClientCleanupAdmissionBlocked() {
        synchronized (cleanupLock) {
            return ownedClientSockets.size() + clientSocketsBeingCreated >= MAX_DEFERRED_CLIENT_CLEANUPS;
        }
    }

    private void dispatch(final BluetoothRequest request, ExecutorService executor, final RequestAction action) {
        dispatch(request, executor, action, null);
    }

    private void dispatch(final BluetoothRequest request, ExecutorService executor, final RequestAction action,
                          final Runnable cleanup) {
        if (shuttingDown.get()) {
            request.cancel();
            runCleanup(cleanup);
            return;
        }
        if (request.isComplete() || !scheduleDeadline(request)) {
            runCleanup(cleanup);
            return;
        }
        try {
            executor.execute(() -> {
                try {
                    if (request.isDeadlineExpired()) request.cancel();
                    if (!request.isComplete()) action.run();
                } catch (Exception failure) {
                    AndroidUtils.handleException(failure, false);
                    request.complete(ERROR, null);
                } finally {
                    runCleanup(cleanup);
                }
            });
        } catch (RejectedExecutionException rejected) {
            runCleanup(cleanup);
            request.complete(ERROR, null);
        }
    }

    private static void runCleanup(Runnable cleanup) {
        if (cleanup == null) return;
        try {
            cleanup.run();
        } catch (RuntimeException failure) {
            AndroidUtils.handleException(failure, false);
        }
    }

    private boolean scheduleDeadline(BluetoothRequest request) {
        long deadline = request.getDeadlineElapsedRealtime();
        if (deadline <= 0) return !request.isComplete();
        long remainingMillis = deadline - SystemClock.elapsedRealtime();
        if (remainingMillis <= 0) {
            request.cancel();
            return false;
        }
        try {
            final ScheduledFuture<?> timeout = timeoutExecutor.schedule(request::cancel, remainingMillis,
                    TimeUnit.MILLISECONDS);
            request.registerCompletionCallback(() -> timeout.cancel(false));
            return !request.isComplete();
        } catch (RejectedExecutionException rejected) {
            request.complete(ERROR, null);
            return false;
        }
    }

    private void dispatchIo(final BluetoothRequest request, final boolean isRead) {
        byte[] bytes = request.getBytes();
        int offset = request.getOffset();
        int length = request.getLength();
        if (bytes == null || offset < 0 || length < 0 || offset > bytes.length - length) {
            request.complete(ERROR, null);
            return;
        }
        if (length == 0) {
            request.complete(0, null);
            return;
        }
        String address = request.getAddress();
        BluetoothSocket socket = socketForHandle(address);
        if (socket == null) {
            request.complete(ERROR, null);
            return;
        }
        SocketIoState ioState = socketIoStates.get(socket);
        if (ioState == null) {
            SocketIoState created = new SocketIoState();
            SocketIoState existing = socketIoStates.putIfAbsent(socket, created);
            ioState = existing != null ? existing : created;
        }
        AtomicBoolean active = isRead ? ioState.readActive : ioState.writeActive;
        if (!active.compareAndSet(false, true)) {
            request.complete(ERROR, null);
            return;
        }
        request.registerCancellationCallback(() -> invalidateSocket(address, socket));
        if (request.isComplete()) {
            active.set(false);
            return;
        }
        dispatch(request, ioExecutor, () -> btReadWrite(request, socket, isRead), () -> active.set(false));
    }

    private void dispatchClientClose(final BluetoothRequest request) {
        String address = request.getAddress();
        BluetoothSession session = address == null ? null : sessionsByHandle.get(address);
        if (session != null) {
            BluetoothSocket socket = session.socket;
            if (socket == null) {
                request.complete(ERROR, null);
                return;
            }
            request.registerCancellationCallback(() -> invalidateSession(session, socket));
            dispatch(request, socketControlExecutor, () -> btCloseClient(request, session, socket));
            return;
        }
        BluetoothSocket socket = address == null ? null : acceptedSockets.get(address);
        if (socket == null) {
            request.complete(ERROR, null);
            return;
        }
        request.registerCancellationCallback(() -> invalidateAcceptedSocket(address, socket));
        dispatch(request, socketControlExecutor, () -> btCloseAccepted(request, address, socket));
    }

    private void dispatchServerClose(final BluetoothRequest request) {
        if (request.isComplete() || !scheduleDeadline(request)) return;
        final ServerCloseContext closeContext;
        final ServerAcceptOperation accept;
        synchronized (serverLifecycleLock) {
            if (serverLifecycleState == ServerLifecycleState.IDLE && !shuttingDown.get()) {
                accept = activeServerAccept.getAndSet(null);
                BluetoothServerSocket listener = serverSocket;
                serverSocket = null;
                serverGenerations.incrementAndGet();
                if (accept != null) accept.cancelled = true;
                closeContext = newServerCloseContextLocked(accept, request, null);
                trackServerListenerLocked(closeContext, listener);
                attachServerAcceptLocked(closeContext, accept);
            } else {
                closeContext = null;
                accept = null;
            }
        }
        if (closeContext == null) {
            request.complete(ERROR, null);
            return;
        }
        if (accept != null) accept.request.cancel();
        scheduleServerListenerClosures(closeContext);
        maybeFinishServerCloseContext(closeContext);
    }

    private void dispatchServerAccept(final BluetoothRequest request) {
        String originalKey = request.getAddress();
        String serviceKey = originalKey == null ? "" : originalKey;
        boolean insecure = serviceKey.startsWith("*");
        if (insecure) serviceKey = serviceKey.length() == 1 ? "" : serviceKey.substring(1);

        final ServerAcceptOperation operation;
        final ServerCloseContext recoveryContext;
        synchronized (serverLifecycleLock) {
            if (!shuttingDown.get() && serverLifecycleState == ServerLifecycleState.IDLE
                    && activeServerAccept.get() == null) {
                long generation = serverGenerations.incrementAndGet();
                operation = new ServerAcceptOperation(request, serviceKey, insecure, generation);
                activeServerAccept.set(operation);
                recoveryContext = null;
            } else {
                operation = null;
                recoveryContext = serverLifecycleState == ServerLifecycleState.CLOSE_FAILED
                        ? serverCloseContext : null;
            }
        }
        if (operation == null) {
            request.complete(ERROR, null);
            if (recoveryContext != null) scheduleServerRecovery(recoveryContext);
            return;
        }

        request.registerCancellationCallback(() -> cancelServerAccept(operation));
        request.registerCompletionCallback(() -> {
            if (request.isCancelled()) return;
            if (request.getResultCode() == NO_ERROR) releaseServerAccept(operation);
            else cancelServerAccept(operation);
        });
        dispatch(request, serverAcceptExecutor, () -> btServerAccept(request, operation));
    }

    private void releaseServerAccept(ServerAcceptOperation operation) {
        synchronized (serverLifecycleLock) {
            activeServerAccept.compareAndSet(operation, null);
        }
    }

    private void cancelServerAccept(ServerAcceptOperation operation) {
        ServerCloseContext closeContext;
        BluetoothServerSocket listener;
        synchronized (serverLifecycleLock) {
            operation.cancelled = true;
            if (activeServerAccept.compareAndSet(operation, null))
                serverGenerations.incrementAndGet();
            listener = operation.listener;
            if (listener != null && serverSocket == listener) serverSocket = null;
            closeContext = operation.closeContext;
            if (closeContext != serverCloseContext) closeContext = null;
            if (closeContext == null && serverCloseContext != null) closeContext = serverCloseContext;
            if (closeContext == null && (listener != null || operation.listenerCreationInFlight)) {
                closeContext = newServerCloseContextLocked(operation, null, null);
            }
            if (closeContext != null) {
                attachServerAcceptLocked(closeContext, operation);
                trackServerListenerLocked(closeContext, listener);
            }
        }
        if (closeContext != null) {
            if (listener != null) scheduleServerListenerClose(closeContext, listener, null);
            maybeFinishServerCloseContext(closeContext);
        }
    }

    private ServerCloseContext newServerCloseContextLocked(ServerAcceptOperation accept,
                                                            BluetoothRequest closeRequest,
                                                            ServerAcceptOperation resumeAccept) {
        ServerCloseContext context = new ServerCloseContext(serverGenerations.get(), accept,
                closeRequest, resumeAccept);
        serverCloseContext = context;
        serverLifecycleState = ServerLifecycleState.CLOSING;
        return context;
    }

    private void attachServerAcceptLocked(ServerCloseContext context, ServerAcceptOperation operation) {
        if (operation == null) return;
        operation.closeContext = context;
        if (operation.listenerCreationInFlight) context.pendingCreations.put(operation, Boolean.TRUE);
        trackServerListenerLocked(context, operation.listener);
    }

    private static void trackServerListenerLocked(ServerCloseContext context, BluetoothServerSocket listener) {
        if (listener != null && !context.listeners.containsKey(listener))
            context.listeners.put(listener, new ServerListenerClosure(listener));
    }

    private void scheduleServerListenerClosures(ServerCloseContext context) {
        List<BluetoothServerSocket> listeners = new ArrayList<>();
        synchronized (serverLifecycleLock) {
            if (serverCloseContext != context) return;
            for (ServerListenerClosure closure : context.listeners.values())
                if (!closure.closed && !closure.closeInFlight) listeners.add(closure.listener);
        }
        for (BluetoothServerSocket listener : listeners)
            scheduleServerListenerClose(context, listener, null);
    }

    private void scheduleServerListenerClose(ServerCloseContext context, BluetoothServerSocket listener,
                                             Runnable afterAttempt) {
        boolean claimed;
        synchronized (serverLifecycleLock) {
            ServerListenerClosure closure = context.listeners.get(listener);
            claimed = serverCloseContext == context && closure != null
                    && !closure.closed && !closure.closeInFlight;
            if (claimed) {
                closure.closeAttempted = true;
                closure.closeInFlight = true;
            }
        }
        if (!claimed) return;
        enqueuePendingCleanup(listener, () -> closeServerSocketNow(listener), closed -> {
            finishServerListenerClose(context, listener, closed);
            if (afterAttempt != null) afterAttempt.run();
        });
    }

    private void finishServerListenerClose(ServerCloseContext context, BluetoothServerSocket listener,
                                           boolean closed) {
        synchronized (serverLifecycleLock) {
            if (serverCloseContext != context) return;
            ServerListenerClosure closure = context.listeners.get(listener);
            if (closure == null) return;
            closure.closeInFlight = false;
            closure.closed = closed;
            if (closed) {
                ServerAcceptOperation accept = context.accept;
                if (accept != null && accept.listener == listener) accept.listener = null;
                ServerAcceptOperation resumeAccept = context.resumeAccept;
                if (resumeAccept != null && resumeAccept.listener == listener) resumeAccept.listener = null;
            }
            if (!closed) serverLifecycleState = ServerLifecycleState.CLOSE_FAILED;
        }
        maybeFinishServerCloseContext(context);
    }

    private void maybeFinishServerCloseContext(ServerCloseContext context) {
        BluetoothRequest requestToComplete = null;
        ServerAcceptOperation acceptToResume = null;
        boolean completeSuccess = false;
        boolean completeFailure = false;
        synchronized (serverLifecycleLock) {
            if (serverCloseContext != context) return;
            boolean hasUnresolvedListener = false;
            boolean hasFailedListener = false;
            for (ServerListenerClosure closure : context.listeners.values()) {
                if (closure.closed) continue;
                hasUnresolvedListener = true;
                if (closure.closeAttempted && !closure.closeInFlight) hasFailedListener = true;
            }

            if (hasFailedListener) {
                serverLifecycleState = ServerLifecycleState.CLOSE_FAILED;
                if (context.resultDelivered.compareAndSet(false, true)) completeFailure = true;
            } else if (!hasUnresolvedListener && context.pendingCreations.isEmpty()) {
                serverLifecycleState = ServerLifecycleState.IDLE;
                serverCloseContext = null;
                if (context.accept != null && context.accept.closeContext == context)
                    context.accept.closeContext = null;
                if (context.resumeAccept != null && context.resumeAccept.closeContext == context)
                    context.resumeAccept.closeContext = null;
                if (context.resultDelivered.compareAndSet(false, true)) {
                    completeSuccess = true;
                    acceptToResume = context.resumeAccept;
                }
            } else if (serverLifecycleState != ServerLifecycleState.CLOSE_FAILED) {
                serverLifecycleState = ServerLifecycleState.CLOSING;
            }

            if (completeSuccess || completeFailure) requestToComplete = context.closeRequest;
        }

        if (requestToComplete != null)
            requestToComplete.complete(completeSuccess ? NO_ERROR : ERROR, null);
        if (completeFailure && context.resumeAccept != null)
            context.resumeAccept.request.complete(ERROR, null);
        if (completeSuccess && acceptToResume != null) resumeServerAccept(acceptToResume);
    }

    private void resumeServerAccept(final ServerAcceptOperation operation) {
        boolean current;
        synchronized (serverLifecycleLock) {
            current = serverLifecycleState == ServerLifecycleState.IDLE && !shuttingDown.get()
                    && !operation.cancelled && !operation.request.isComplete()
                    && activeServerAccept.get() == operation
                    && serverGenerations.get() == operation.generation;
        }
        if (!current) {
            operation.request.complete(ERROR, null);
            return;
        }
        dispatch(operation.request, serverAcceptExecutor,
                () -> btServerAccept(operation.request, operation));
    }

    private void scheduleServerRecovery(ServerCloseContext context) {
        if (!context.retryRunning.compareAndSet(false, true)) return;
        List<BluetoothServerSocket> listeners = new ArrayList<>();
        synchronized (serverLifecycleLock) {
            if (serverCloseContext != context || serverLifecycleState != ServerLifecycleState.CLOSE_FAILED) {
                context.retryRunning.set(false);
                return;
            }
            for (ServerListenerClosure closure : context.listeners.values()) {
                if (!closure.closed && !closure.closeInFlight) {
                    closure.closeAttempted = true;
                    closure.closeInFlight = true;
                    listeners.add(closure.listener);
                }
            }
        }
        if (listeners.isEmpty()) {
            context.retryRunning.set(false);
            return;
        }
        AtomicInteger remaining = new AtomicInteger(listeners.size());
        for (BluetoothServerSocket listener : listeners) {
            enqueuePendingCleanup(listener, () -> closeServerSocketNow(listener), closed -> {
                finishServerListenerClose(context, listener, closed);
                if (remaining.decrementAndGet() == 0) context.retryRunning.set(false);
            });
        }
    }

    private BluetoothSocket socketForHandle(String address) {
        if (address == null) return null;
        BluetoothSession session = sessionsByHandle.get(address);
        if (session != null && session.state == SessionState.CONNECTED) return session.socket;
        return acceptedSockets.get(address);
    }

    private boolean isCurrentSession(BluetoothSession session) {
        BluetoothSession current = sessionsByKey.get(session.key);
        return current == session && current.generation == session.generation;
    }

    private void removeSessionMappings(BluetoothSession session) {
        sessionsByKey.remove(session.key, session);
        for (String handle : session.handles) sessionsByHandle.remove(handle, session);
        session.handles.clear();
    }

    private void invalidateSession(BluetoothSession session, BluetoothSocket expectedSocket) {
        BluetoothSocket socket;
        synchronized (session) {
            socket = session.socket;
            if (expectedSocket != null && socket != expectedSocket) return;
            if (session.state != SessionState.CLOSED) {
                session.state = SessionState.CLOSING;
                removeSessionMappings(session);
                session.state = SessionState.CLOSED;
            }
        }
        if (socket != null) {
            socketIoStates.remove(socket);
            closeSocketAsync(socket);
        }
    }

    private boolean detachSession(BluetoothSession session, BluetoothSocket expectedSocket) {
        synchronized (session) {
            if (session.state == SessionState.CLOSING || session.state == SessionState.CLOSED
                    || session.socket != expectedSocket) return false;
            session.state = SessionState.CLOSING;
            removeSessionMappings(session);
            session.state = SessionState.CLOSED;
        }
        socketIoStates.remove(expectedSocket);
        return true;
    }

    private void invalidateSocket(String address, BluetoothSocket socket) {
        BluetoothSession session = address == null ? null : sessionsByHandle.get(address);
        if (session != null && session.socket == socket) {
            invalidateSession(session, socket);
            return;
        }
        invalidateAcceptedSocket(address, socket);
    }

    private void invalidateAcceptedSocket(String address, BluetoothSocket socket) {
        if (address != null) acceptedSockets.remove(address, socket);
        socketIoStates.remove(socket);
        closeSocketAsync(socket);
    }

    private void processAdapterRequest(BluetoothRequest request) throws Exception {
        if (request.isComplete()) return;
        BluetoothAdapter adapter = getAdapter();
        if (request.getOperation() == BT_IS_SUPPORTED) {
            request.complete(adapter == null ? ERROR : NO_ERROR, null);
            return;
        }
        if (adapter == null) {
            request.complete(ERROR, null);
            return;
        }
        switch (request.getOperation()) {
            case BT_ACTIVATE:
                request.complete(adapter.isEnabled() || adapter.enable() ? NO_ERROR : ERROR, null);
                break;
            case BT_DEACTIVATE:
                request.complete(!adapter.isEnabled() || adapter.disable() ? NO_ERROR : ERROR, null);
                break;
            case BT_IS_RADIO_ON:
                request.complete(adapter.getState() == BluetoothAdapter.STATE_ON ? NO_ERROR : ERROR, null);
                break;
            case BT_IS_DISCOVERABLE:
                request.complete(adapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
                        ? NO_ERROR : ERROR, null);
                break;
            case BT_GET_PAIRED_DEVICES:
                request.complete(NO_ERROR, pairedDevices(adapter.getBondedDevices()));
                break;
            default:
                request.complete(ERROR, null);
                break;
        }
    }

    private static String[] pairedDevices(Set<BluetoothDevice> pairedDevices) {
        if (pairedDevices == null || pairedDevices.isEmpty()) return null;
        List<String> result = new ArrayList<>(pairedDevices.size());
        for (BluetoothDevice device : pairedDevices) {
            if (device == null || device.getAddress() == null) continue;
            String name = device.getName();
            result.add(device.getAddress().replace(":", "") + "|" + (name == null ? "" : name));
        }
        return result.isEmpty() ? null : result.toArray(new String[0]);
    }

    private void btMakeDiscoverable(final BluetoothRequest request) throws Exception {
        if (request.isComplete()) return;
        if (activeDiscoverability.get() != null) {
            request.complete(ERROR, null);
            return;
        }
        BluetoothAdapter adapter = getAdapter();
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
            if (request.isComplete() || activeDiscoverability.get() != operation || shuttingDown.get()) return;
            LoaderHost host = currentLoader();
            if (host == null) {
                request.complete(ERROR, null);
                return;
            }
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            synchronized (operation) {
                if (request.isComplete() || activeDiscoverability.get() != operation || shuttingDown.get()) return;
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

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode) {
        if (!isDiscoverabilityRequestCode(requestCode)) return false;
        discoverabilityRequestCodes.remove(requestCode);
        DiscoverabilityOperation operation = activeDiscoverability.get();
        if (shuttingDown.get()) {
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

    private static final class ServerAcceptOperation {
        final BluetoothRequest request;
        final String serviceKey;
        final boolean insecure;
        final long generation;
        volatile BluetoothServerSocket listener;
        volatile boolean cancelled;
        boolean listenerCreationInFlight;
        ServerCloseContext closeContext;

        ServerAcceptOperation(BluetoothRequest request, String serviceKey, boolean insecure, long generation) {
            this.request = request;
            this.serviceKey = serviceKey;
            this.insecure = insecure;
            this.generation = generation;
        }
    }

    private static final class ServerListenerClosure {
        final BluetoothServerSocket listener;
        boolean closed;
        boolean closeAttempted;
        boolean closeInFlight;

        ServerListenerClosure(BluetoothServerSocket listener) {
            this.listener = listener;
        }
    }

    private static final class ServerCloseContext {
        final long originalGeneration;
        final ServerAcceptOperation accept;
        final BluetoothRequest closeRequest;
        final ServerAcceptOperation resumeAccept;
        final IdentityHashMap<BluetoothServerSocket, ServerListenerClosure> listeners = new IdentityHashMap<>();
        final IdentityHashMap<ServerAcceptOperation, Boolean> pendingCreations = new IdentityHashMap<>();
        final AtomicBoolean retryRunning = new AtomicBoolean();
        final AtomicBoolean resultDelivered = new AtomicBoolean();

        ServerCloseContext(long originalGeneration, ServerAcceptOperation accept,
                           BluetoothRequest closeRequest, ServerAcceptOperation resumeAccept) {
            this.originalGeneration = originalGeneration;
            this.accept = accept;
            this.closeRequest = closeRequest;
            this.resumeAccept = resumeAccept;
        }
    }

    private void btGetUnpairedDevices(final BluetoothRequest request) {
        final BluetoothAdapter adapter;
        try {
            adapter = getAdapter();
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
                    reportCleanupFailure(rejected);
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
                    if (operation.finished.get() || operation.request.isComplete() || shuttingDown.get()) return;
                    if (operation.pendingDeviceQueries.get() == 0) finishDiscovery(operation, true, false);
                    return;
                }
                if (operation.finished.get() || operation.request.isComplete() || shuttingDown.get()) return;
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
                                address = normalizeBluetoothAddressOrNull(address);
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
                    || operation.request.isComplete() || shuttingDown.get()
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
                        || shuttingDown.get() || activeDiscovery.get() != operation;
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

        if (startFailure != null) reportCleanupFailure(startFailure);
        if (cancelFailure != null) reportCleanupFailure(cancelFailure);
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
            reportCleanupFailure(rejected);
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
                enqueuePendingCleanup(operation, () -> {
                    cancelDiscoveryOnWorker(operation);
                    return true;
                }, closed -> {
                    if (!closed) reportCleanupFailure(new IllegalStateException(
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
            reportCleanupFailure(failure);
            if (operation.cancellationAttempts.incrementAndGet() <= 1) {
                try {
                    discoveryControlExecutor.execute(() -> cancelDiscoveryOnWorker(operation));
                    return;
                } catch (RejectedExecutionException rejected) {
                    reportCleanupFailure(rejected);
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

    private static final class ParsedAddress {
        final String normalized;
        final boolean secure;

        ParsedAddress(String normalized, boolean secure) {
            this.normalized = normalized;
            this.secure = secure;
        }
    }

    private static ParsedAddress parseAddress(String originalAddress) {
        if (originalAddress == null) return null;
        boolean insecure = originalAddress.startsWith("*");
        String address = insecure ? originalAddress.substring(1) : originalAddress;
        if (address.length() != 12) return null;
        StringBuilder normalized = new StringBuilder(12);
        for (int i = 0; i < address.length(); i++) {
            char value = address.charAt(i);
            if (!((value >= '0' && value <= '9') || (value >= 'a' && value <= 'f')
                    || (value >= 'A' && value <= 'F'))) return null;
            normalized.append(Character.toUpperCase(value));
        }
        return new ParsedAddress(normalized.toString(), !insecure);
    }

    private static String formatAddress(String normalizedAddress) {
        StringBuilder formatted = new StringBuilder(17);
        for (int i = 0; i < normalizedAddress.length(); i += 2) {
            if (i > 0) formatted.append(':');
            formatted.append(normalizedAddress, i, i + 2);
        }
        return formatted.toString();
    }

    private void btConnect(final BluetoothRequest request) throws Exception {
        if (request.isComplete()) return;
        if (isClientCleanupAdmissionBlocked()) {
            request.complete(ERROR, null);
            return;
        }
        final String originalAddress = request.getAddress();
        ParsedAddress parsed = parseAddress(originalAddress);
        if (parsed == null) {
            request.complete(ERROR, null);
            return;
        }
        final SessionKey key = new SessionKey(parsed.normalized, parsed.secure);
        BluetoothSession session;
        while (true) {
            BluetoothSession existing = sessionsByKey.get(key);
            if (existing == null) {
                BluetoothSession created = new BluetoothSession(key, sessionGenerations.incrementAndGet());
                existing = sessionsByKey.putIfAbsent(key, created);
                if (existing == null) {
                    session = created;
                    break;
                }
            }
            if (existing.state == SessionState.CONNECTING || existing.state == SessionState.CLOSING) {
                request.complete(ERROR, null);
                return;
            }
            if (existing.state == SessionState.CONNECTED) {
                BluetoothSocket existingSocket = existing.socket;
                boolean connected;
                try {
                    connected = existingSocket != null && existingSocket.isConnected();
                } catch (RuntimeException failure) {
                    connected = false;
                }
                if (connected) {
                    if (attachSessionHandle(request, existing, originalAddress, existingSocket)) return;
                    if (request.isComplete()) return;
                }
                invalidateSession(existing, existingSocket);
                continue;
            }
            sessionsByKey.remove(key, existing);
        }

        final BluetoothSession connectingSession = session;
        request.registerCancellationCallback(() -> invalidateSession(connectingSession, connectingSession.socket));
        boolean connectedSuccessfully = false;
        BondMonitor bondMonitor = null;
        BluetoothSocket pendingSocket = null;
        try {
            if (request.isComplete()) return;
            BluetoothAdapter adapter = getAdapter();
            if (request.isDeadlineExpired()) request.cancel();
            if (adapter == null || request.isComplete()) {
                request.complete(ERROR, null);
                return;
            }
            if (adapter.isDiscovering()) adapter.cancelDiscovery();
            if (request.isDeadlineExpired()) request.cancel();
            if (request.isComplete()) return;
            BluetoothDevice device = adapter.getRemoteDevice(formatAddress(parsed.normalized));
            if (request.isDeadlineExpired()) request.cancel();
            if (request.isComplete()) return;
            bondMonitor = new BondMonitor(device, request, Launcher4A.loader);
            if (!bondMonitor.register()) {
                request.complete(ERROR, null);
                return;
            }
            pendingSocket = createSocket(device, parsed.secure);
            registerPendingSocketClose(request, pendingSocket);
            if (request.isDeadlineExpired()) request.cancel();
            if (request.isComplete()) return;
            try {
                pendingSocket.connect();
            } catch (IOException firstFailure) {
                closeSocketAsync(pendingSocket);
                pendingSocket = null;
                if (request.isComplete()) return;
                bondMonitor.reconcileAfterConnectionFailure();
                BondOutcome outcome = bondMonitor.currentOutcome();
                if (outcome == BondOutcome.AUTHENTICATION_FAILED) {
                    request.complete(INVALID_PASSWORD, null);
                    return;
                }
                if (!bondMonitor.hasObservedBonding()) {
                    AndroidUtils.handleException(firstFailure, false);
                    request.complete(ERROR, null);
                    return;
                }
                outcome = bondMonitor.awaitOutcome();
                if (outcome == BondOutcome.AUTHENTICATION_FAILED) {
                    request.complete(INVALID_PASSWORD, null);
                    return;
                }
                if (outcome != BondOutcome.BONDED || request.isComplete()) {
                    AndroidUtils.handleException(firstFailure, false);
                    request.complete(ERROR, null);
                    return;
                }
                if (request.isDeadlineExpired()) request.cancel();
                if (request.isComplete()) return;
                pendingSocket = createSocket(device, parsed.secure);
                registerPendingSocketClose(request, pendingSocket);
                if (request.isDeadlineExpired()) request.cancel();
                if (request.isComplete()) return;
                try {
                    pendingSocket.connect();
                } catch (IOException retryFailure) {
                    closeSocketAsync(pendingSocket);
                    pendingSocket = null;
                    AndroidUtils.handleException(retryFailure, false);
                    request.complete(bondMonitor.currentOutcome() == BondOutcome.AUTHENTICATION_FAILED
                            ? INVALID_PASSWORD : ERROR, null);
                    return;
                }
            }

            BluetoothSocket socket = pendingSocket;
            if (request.isDeadlineExpired()) request.cancel();
            if (request.isComplete() || !publishSession(connectingSession, originalAddress, socket, request)) {
                request.complete(ERROR, null);
                return;
            }
            if (!request.complete(NO_ERROR, null) || request.getResultCode() != NO_ERROR) {
                pendingSocket = null;
                invalidateSession(connectingSession, socket);
                return;
            }
            pendingSocket = null;
            connectedSuccessfully = true;
        } catch (InterruptedException interrupted) {
            request.cancel();
            Thread.currentThread().interrupt();
        } finally {
            if (bondMonitor != null) bondMonitor.unregister();
            if (pendingSocket != null) closeSocketAsync(pendingSocket);
            if (!connectedSuccessfully) invalidateSession(connectingSession, connectingSession.socket);
        }
    }

    private BluetoothSocket createSocket(BluetoothDevice device, boolean secure) throws IOException {
        synchronized (cleanupLock) {
            if (ownedClientSockets.size() + clientSocketsBeingCreated >= MAX_DEFERRED_CLIENT_CLEANUPS)
                throw new IOException("Bluetooth client socket capacity exhausted");
            clientSocketsBeingCreated++;
        }
        BluetoothSocket socket = null;
        try {
            socket = secure ? device.createRfcommSocketToServiceRecord(SPP_UUID)
                    : device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            if (socket == null) throw new IOException("Unable to create Bluetooth client socket");
            synchronized (cleanupLock) {
                ownedClientSockets.put(socket, Boolean.TRUE);
            }
            return socket;
        } finally {
            synchronized (cleanupLock) {
                clientSocketsBeingCreated--;
            }
        }
    }

    private boolean ownAcceptedSocket(BluetoothSocket socket) {
        if (socket == null) return false;
        synchronized (cleanupLock) {
            if (ownedClientSockets.size() + clientSocketsBeingCreated >= MAX_DEFERRED_CLIENT_CLEANUPS)
                return false;
            ownedClientSockets.put(socket, Boolean.TRUE);
            return true;
        }
    }

    private void registerPendingSocketClose(BluetoothRequest request, BluetoothSocket socket) {
        request.registerCancellationCallback(() -> closeSocketAsync(socket));
    }

    private boolean attachSessionHandle(BluetoothRequest request, BluetoothSession session, String handle,
                                        BluetoothSocket socket) {
        final boolean added;
        synchronized (session) {
            if (shuttingDown.get() || request.isComplete() || session.state != SessionState.CONNECTED
                    || session.socket != socket || !isCurrentSession(session)) {
                return false;
            }
            added = session.handles.add(handle);
            sessionsByHandle.put(handle, session);
        }
        if (added) request.registerCancellationCallback(() -> removeSessionHandle(session, handle));
        if (!request.complete(NO_ERROR, null) || request.getResultCode() != NO_ERROR) {
            if (added) removeSessionHandle(session, handle);
            return false;
        }
        return true;
    }

    private void removeSessionHandle(BluetoothSession session, String handle) {
        synchronized (session) {
            if (sessionsByHandle.remove(handle, session)) session.handles.remove(handle);
        }
    }

    private boolean publishSession(BluetoothSession session, String handle, BluetoothSocket socket,
                                   BluetoothRequest request) {
        synchronized (session) {
            if (shuttingDown.get() || request.isComplete() || session.state != SessionState.CONNECTING
                    || !isCurrentSession(session)) {
                return false;
            }
            session.socket = socket;
            session.state = SessionState.CONNECTED;
            session.handles.add(handle);
            sessionsByHandle.put(handle, session);
            return true;
        }
    }

    private void btReadWrite(final BluetoothRequest request, BluetoothSocket socket, boolean isRead) throws Exception {
        if (request.isComplete()) return;
        String address = request.getAddress();
        byte[] bytes = request.getBytes();
        int offset = request.getOffset();
        int length = request.getLength();
        if (bytes == null || offset < 0 || length < 0 || offset > bytes.length - length) {
            request.complete(ERROR, null);
            return;
        }
        if (length == 0) {
            request.complete(0, null);
            return;
        }
        try {
            if (!socket.isConnected()) throw new IOException("Bluetooth socket is disconnected");
            if (isRead) {
                InputStream input = socket.getInputStream();
                if (input == null) throw new IOException("Bluetooth input stream is unavailable");
                request.complete(input.read(bytes, offset, length), null);
            } else {
                OutputStream output = socket.getOutputStream();
                if (output == null) throw new IOException("Bluetooth output stream is unavailable");
                output.write(bytes, offset, length);
                output.flush();
                request.complete(length, null);
            }
        } catch (Exception failure) {
            invalidateSocket(address, socket);
            throw failure;
        }
    }

    private void btCloseClient(BluetoothRequest request, BluetoothSession session, BluetoothSocket socket) {
        if (request.isComplete()) return;
        if (!detachSession(session, socket)) {
            request.complete(ERROR, null);
            return;
        }
        enqueuePendingCleanup(socket, () -> closeSocketNow(socket),
                closed -> request.complete(closed ? NO_ERROR : ERROR, null));
    }

    private void btCloseAccepted(BluetoothRequest request, String address, BluetoothSocket socket) {
        if (request.isComplete()) return;
        if (!acceptedSockets.remove(address, socket)) {
            request.complete(ERROR, null);
            return;
        }
        socketIoStates.remove(socket);
        enqueuePendingCleanup(socket, () -> closeSocketNow(socket),
                closed -> request.complete(closed ? NO_ERROR : ERROR, null));
    }

    private void closeSocketAsync(final BluetoothSocket socket) {
        if (socket == null) return;
        enqueuePendingCleanup(socket, () -> closeSocketNow(socket), null);
    }

    private void enqueuePendingCleanup(Object resource, CleanupAction action, CleanupCallback callback) {
        if (resource == null) {
            if (callback != null) callback.completed(true);
            return;
        }
        final PendingCleanup cleanup;
        synchronized (cleanupLock) {
            PendingCleanup existing = pendingCleanups.get(resource);
            if (existing != null) {
                if (callback != null) existing.callbacks.add(callback);
                return;
            }
            cleanup = new PendingCleanup(resource, action);
            if (callback != null) cleanup.callbacks.add(callback);
            pendingCleanups.put(resource, cleanup);
        }
        submitPendingCleanup(cleanup);
    }

    private void submitPendingCleanup(final PendingCleanup cleanup) {
        try {
            cleanupExecutor.execute(() -> performPendingCleanup(cleanup));
        } catch (RejectedExecutionException rejected) {
            startEmergencyCleanup(cleanup, rejected);
        }
    }

    private void startEmergencyCleanup(final PendingCleanup cleanup, RejectedExecutionException rejected) {
        synchronized (cleanupLock) {
            if (pendingCleanups.get(cleanup.resource) != cleanup
                    || cleanup.state != PendingCleanupState.QUEUED) return;
        }
        while (true) {
            int active = ACTIVE_EMERGENCY_CLEANUP_THREADS.get();
            if (active >= MAX_EMERGENCY_CLEANUP_THREADS) {
                RejectedExecutionException saturated = new RejectedExecutionException(
                        "Bluetooth emergency cleanup thread limit reached", rejected);
                reportCleanupFailure(saturated);
                if (cleanup.resource instanceof BluetoothSocket) {
                    deferPendingCleanup(cleanup, CLEANUP_RETRY_INTERVAL_MS, PendingCleanupState.QUEUED, false);
                } else {
                    finishPendingCleanup(cleanup, false);
                }
                return;
            }
            if (ACTIVE_EMERGENCY_CLEANUP_THREADS.compareAndSet(active, active + 1)) break;
        }

        final AtomicBoolean slotReleased = new AtomicBoolean();
        Runnable releaseSlot = () -> {
            if (slotReleased.compareAndSet(false, true)) ACTIVE_EMERGENCY_CLEANUP_THREADS.decrementAndGet();
        };
        try {
            Thread emergency = new Thread(() -> {
                try {
                    performPendingCleanup(cleanup);
                } finally {
                    releaseSlot.run();
                }
            },
                    "TotalCross-Bluetooth-cleanup-emergency-" + EMERGENCY_CLEANUP_THREAD_IDS.incrementAndGet());
            emergency.setDaemon(true);
            emergency.start();
        } catch (Throwable failure) {
            releaseSlot.run();
            reportCleanupFailure(rejected);
            reportCleanupFailure(failure);
            if (cleanup.resource instanceof BluetoothSocket) {
                deferPendingCleanup(cleanup, CLEANUP_RETRY_INTERVAL_MS, PendingCleanupState.QUEUED, false);
            } else {
                finishPendingCleanup(cleanup, false);
            }
        }
    }

    private static void reportCleanupFailure(Throwable failure) {
        try {
            AndroidUtils.handleException(failure, false);
        } catch (Throwable ignored) {
            // Cleanup bookkeeping must complete even if diagnostics fail.
        }
    }

    private void performPendingCleanup(PendingCleanup cleanup) {
        int attempt;
        synchronized (cleanupLock) {
            if (pendingCleanups.get(cleanup.resource) != cleanup
                    || cleanup.state != PendingCleanupState.QUEUED) return;
            cleanup.state = PendingCleanupState.RUNNING;
            if (cleanup.resource instanceof BluetoothSocket) cleanup.closeAttempts++;
            attempt = cleanup.closeAttempts;
        }

        try {
            if (cleanup.action.close()) finishPendingCleanup(cleanup, true);
            else finishPendingCleanup(cleanup, false);
        } catch (IOException failure) {
            reportCleanupFailure(failure);
            if (cleanup.resource instanceof BluetoothSocket && attempt < CLIENT_SOCKET_CLOSE_MAX_ATTEMPTS) {
                long delay = attempt == 1 ? CLEANUP_RETRY_INTERVAL_MS : 1000;
                deferPendingCleanup(cleanup, delay, PendingCleanupState.RUNNING, false);
            } else {
                if (cleanup.resource instanceof BluetoothSocket) {
                    reportCleanupFailure(new IOException("Bluetooth client socket remains unresolved after "
                            + attempt + " close attempts", failure));
                }
                finishPendingCleanup(cleanup, false);
            }
        } catch (Throwable failure) {
            reportCleanupFailure(failure);
            finishPendingCleanup(cleanup, false);
        } finally {
            drainDeferredClientCleanups();
            maybeFinishCleanupShutdown();
        }
    }

    private void finishPendingCleanup(PendingCleanup cleanup, boolean closed) {
        List<CleanupCallback> callbacks;
        synchronized (cleanupLock) {
            if (pendingCleanups.get(cleanup.resource) != cleanup) return;
            cleanup.state = PendingCleanupState.COMPLETED;
            if (cleanup.inDeferredQueue) deferredClientCleanups.remove(cleanup);
            cleanup.inDeferredQueue = false;
            cleanup.deferredOverflow = false;
            pendingCleanups.remove(cleanup.resource);
            promoteOverflowDeferredLocked();
            callbacks = new ArrayList<>(cleanup.callbacks);
            cleanup.callbacks.clear();
        }
        for (CleanupCallback callback : callbacks) {
            try {
                callback.completed(closed);
            } catch (Throwable failure) {
                reportCleanupFailure(failure);
            }
        }
        drainDeferredClientCleanups();
        maybeFinishCleanupShutdown();
    }

    private void deferPendingCleanup(PendingCleanup cleanup, long delayMillis,
                                     PendingCleanupState expectedState, boolean firstInQueue) {
        boolean overflow = false;
        boolean terminal = false;
        synchronized (cleanupLock) {
            if (pendingCleanups.get(cleanup.resource) != cleanup || cleanup.state != expectedState) return;
            if (cleanupShutdownFinalized || (cleanupShutdownStarted
                    && SystemClock.elapsedRealtime() >= cleanupShutdownDeadlineElapsed)) {
                terminal = true;
            } else {
                cleanup.state = PendingCleanupState.DEFERRED;
                cleanup.nextAttemptElapsed = SystemClock.elapsedRealtime() + delayMillis;
                cleanup.deferredSequence = ++nextCleanupSequence;
                cleanup.deferredOverflow = true;
                cleanup.inDeferredQueue = false;
                if (firstInQueue && deferredClientCleanups.size() < MAX_DEFERRED_CLIENT_CLEANUPS) {
                    cleanup.inDeferredQueue = true;
                    cleanup.deferredOverflow = false;
                    deferredClientCleanups.addFirst(cleanup);
                    promoteOverflowDeferredLocked();
                } else {
                    promoteOverflowDeferredLocked();
                    if (!cleanup.inDeferredQueue && deferredClientCleanups.size() < MAX_DEFERRED_CLIENT_CLEANUPS) {
                        cleanup.inDeferredQueue = true;
                        cleanup.deferredOverflow = false;
                        deferredClientCleanups.addLast(cleanup);
                    }
                }
                overflow = !cleanup.inDeferredQueue && !cleanup.overflowReported;
                if (overflow) cleanup.overflowReported = true;
            }
        }

        if (terminal) {
            reportCleanupFailure(new RejectedExecutionException(
                    "Bluetooth client socket cleanup reached its terminal shutdown deadline"));
            finishPendingCleanup(cleanup, false);
            return;
        }
        if (overflow) {
            reportCleanupFailure(new RejectedExecutionException(
                    "Deferred Bluetooth client cleanup capacity exhausted; socket retained for shutdown cleanup"));
        }
        if (!ensureCleanupRetryDispatcherStarted()) {
            failDeferredClientCleanups(new RejectedExecutionException(
                    "Bluetooth cleanup retry dispatcher could not be started"));
        }
    }

    private void promoteOverflowDeferredLocked() {
        while (deferredClientCleanups.size() < MAX_DEFERRED_CLIENT_CLEANUPS) {
            PendingCleanup oldest = null;
            for (PendingCleanup candidate : pendingCleanups.values()) {
                if (candidate.state == PendingCleanupState.DEFERRED && candidate.deferredOverflow
                        && (oldest == null || candidate.deferredSequence < oldest.deferredSequence)) {
                    oldest = candidate;
                }
            }
            if (oldest == null) return;
            oldest.deferredOverflow = false;
            oldest.inDeferredQueue = true;
            deferredClientCleanups.addLast(oldest);
        }
    }

    private boolean ensureCleanupRetryDispatcherStarted() {
        Throwable failure = null;
        synchronized (cleanupLock) {
            if (cleanupRetryDispatcherStarted && !cleanupRetryDispatcher.isShutdown()) return true;
            if (cleanupShutdownFinalized || cleanupRetryDispatcher.isShutdown()) return false;
            try {
                cleanupRetryDispatcher.scheduleWithFixedDelay(this::cleanupRetryDispatcherTick,
                        CLEANUP_RETRY_INTERVAL_MS, CLEANUP_RETRY_INTERVAL_MS, TimeUnit.MILLISECONDS);
                cleanupRetryDispatcherStarted = true;
                return true;
            } catch (Throwable startFailure) {
                failure = startFailure;
                cleanupRetryDispatcherStarted = false;
            }
        }
        reportCleanupFailure(failure);
        return false;
    }

    private void cleanupRetryDispatcherTick() {
        try {
            drainDeferredClientCleanups();
            progressCleanupShutdown();
        } catch (Throwable failure) {
            reportCleanupFailure(failure);
        }
    }

    private void drainDeferredClientCleanups() {
        if (!cleanupDrainRunning.compareAndSet(false, true)) return;
        try {
            while (true) {
                PendingCleanup cleanup;
                synchronized (cleanupLock) {
                    if (cleanupShutdownFinalized) return;
                    promoteOverflowDeferredLocked();
                    cleanup = deferredClientCleanups.peekFirst();
                    if (cleanup == null || cleanup.nextAttemptElapsed > SystemClock.elapsedRealtime()) return;
                    deferredClientCleanups.removeFirst();
                    cleanup.inDeferredQueue = false;
                    cleanup.deferredOverflow = false;
                    if (pendingCleanups.get(cleanup.resource) != cleanup
                            || cleanup.state != PendingCleanupState.DEFERRED) continue;
                    cleanup.state = PendingCleanupState.QUEUED;
                    promoteOverflowDeferredLocked();
                }
                try {
                    cleanupExecutor.execute(() -> performPendingCleanup(cleanup));
                } catch (RejectedExecutionException rejected) {
                    reportCleanupFailure(rejected);
                    deferPendingCleanup(cleanup, CLEANUP_RETRY_INTERVAL_MS,
                            PendingCleanupState.QUEUED, true);
                    return;
                }
            }
        } finally {
            cleanupDrainRunning.set(false);
            maybeFinishCleanupShutdown();
        }
    }

    private void failDeferredClientCleanups(Throwable reason) {
        List<PendingCleanup> deferred = new ArrayList<>();
        synchronized (cleanupLock) {
            for (PendingCleanup cleanup : pendingCleanups.values())
                if (cleanup.resource instanceof BluetoothSocket
                        && cleanup.state == PendingCleanupState.DEFERRED) deferred.add(cleanup);
        }
        for (PendingCleanup cleanup : deferred) {
            reportCleanupFailure(reason);
            finishPendingCleanup(cleanup, false);
        }
    }

    private void maybeFinishCleanupShutdown() {
        boolean deadlineReached;
        boolean sourcesTerminated;
        synchronized (cleanupLock) {
            if (!cleanupShutdownStarted || cleanupShutdownFinalized) return;
            deadlineReached = SystemClock.elapsedRealtime() >= cleanupShutdownDeadlineElapsed;
            sourcesTerminated = connectionExecutor.isTerminated() && discoveryControlExecutor.isTerminated()
                    && ioExecutor.isTerminated()
                    && serverAcceptExecutor.isTerminated() && socketControlExecutor.isTerminated()
                    && timeoutExecutor.isTerminated();
            if (!deadlineReached && (!sourcesTerminated || !pendingCleanups.isEmpty())) return;
        }

        if (!deadlineReached && isServerCloseInProgress()) return;
        finalizeCleanupShutdown(deadlineReached);
    }

    private boolean isServerCloseInProgress() {
        synchronized (serverLifecycleLock) {
            return serverLifecycleState == ServerLifecycleState.CLOSING;
        }
    }

    private void progressCleanupShutdown() {
        maybeFinishCleanupShutdown();
    }

    private void finalizeCleanupShutdown(boolean deadlineReached) {
        synchronized (cleanupLock) {
            if (!cleanupShutdownStarted || cleanupShutdownFinalizing || cleanupShutdownFinalized) return;
            cleanupShutdownFinalizing = true;
        }

        if (deadlineReached) finalDrainDeferredClientCleanups();

        List<PendingCleanup> unresolved = new ArrayList<>();
        synchronized (cleanupLock) {
            if (cleanupShutdownFinalized) return;
            if (deadlineReached) {
                for (PendingCleanup cleanup : pendingCleanups.values()) {
                    if (cleanup.resource instanceof BluetoothSocket
                            && cleanup.state == PendingCleanupState.DEFERRED) unresolved.add(cleanup);
                }
            }
            cleanupShutdownFinalized = true;
            cleanupShutdownFinalizing = false;
        }
        for (PendingCleanup cleanup : unresolved) {
            reportCleanupFailure(new RejectedExecutionException(
                    "Bluetooth client socket cleanup was unresolved at shutdown deadline"));
            finishPendingCleanup(cleanup, false);
        }
        cleanupExecutor.shutdown();
        cleanupRetryDispatcher.shutdown();
    }

    private void finalDrainDeferredClientCleanups() {
        List<PendingCleanup> finalAttempt = new ArrayList<>();
        synchronized (cleanupLock) {
            for (PendingCleanup cleanup : pendingCleanups.values()) {
                if (cleanup.resource instanceof BluetoothSocket
                        && cleanup.state == PendingCleanupState.DEFERRED) {
                    cleanup.state = PendingCleanupState.QUEUED;
                    cleanup.inDeferredQueue = false;
                    cleanup.deferredOverflow = false;
                    deferredClientCleanups.remove(cleanup);
                    finalAttempt.add(cleanup);
                }
            }
        }
        for (PendingCleanup cleanup : finalAttempt) {
            try {
                cleanupExecutor.execute(() -> performPendingCleanup(cleanup));
            } catch (RejectedExecutionException rejected) {
                reportCleanupFailure(rejected);
                synchronized (cleanupLock) {
                    if (pendingCleanups.get(cleanup.resource) == cleanup
                            && cleanup.state == PendingCleanupState.QUEUED) {
                        cleanup.state = PendingCleanupState.DEFERRED;
                    }
                }
            }
        }
    }

    private boolean closeSocketNow(BluetoothSocket socket) throws IOException {
        socket.close();
        synchronized (cleanupLock) {
            ownedClientSockets.remove(socket);
        }
        return true;
    }

    private void btServerAccept(final BluetoothRequest request, final ServerAcceptOperation operation)
            throws Exception {
        BluetoothServerSocket previousListener;
        ServerCloseContext replacementClose = null;
        boolean current;
        synchronized (serverLifecycleLock) {
            current = serverLifecycleState == ServerLifecycleState.IDLE && !shuttingDown.get()
                    && !operation.cancelled && !request.isComplete()
                    && activeServerAccept.get() == operation
                    && serverGenerations.get() == operation.generation;
            previousListener = current ? serverSocket : null;
            if (current && previousListener != null) {
                serverSocket = null;
                operation.listener = previousListener;
                replacementClose = newServerCloseContextLocked(operation, null, operation);
                operation.closeContext = replacementClose;
                trackServerListenerLocked(replacementClose, previousListener);
            } else if (current) {
                operation.listenerCreationInFlight = true;
            }
        }
        if (!current) {
            request.complete(ERROR, null);
            return;
        }
        if (previousListener != null) {
            scheduleServerListenerClose(replacementClose, previousListener, null);
            maybeFinishServerCloseContext(replacementClose);
            return;
        }

        final BluetoothAdapter adapter;
        try {
            adapter = getAdapter();
        } catch (RuntimeException failure) {
            ServerCloseContext closeContext = finishServerListenerCreation(operation, null);
            if (closeContext != null) maybeFinishServerCloseContext(closeContext);
            throw failure;
        }
        if (adapter == null || request.isComplete()) {
            ServerCloseContext closeContext = finishServerListenerCreation(operation, null);
            if (closeContext != null) maybeFinishServerCloseContext(closeContext);
            request.complete(ERROR, null);
            return;
        }

        final BluetoothServerSocket listener;
        try {
            listener = operation.insecure
                    ? adapter.listenUsingInsecureRfcommWithServiceRecord("MyBluetoothApp", SPP_UUID)
                    : adapter.listenUsingRfcommWithServiceRecord("MyBluetoothApp", SPP_UUID);
        } catch (Exception failure) {
            ServerCloseContext closeContext = finishServerListenerCreation(operation, null);
            if (closeContext != null) maybeFinishServerCloseContext(closeContext);
            throw failure;
        }

        ServerCloseContext closeContext = finishServerListenerCreation(operation, listener);
        boolean published = closeContext == null && listener != null;
        if (!published) {
            if (closeContext != null) {
                scheduleServerListenerClose(closeContext, listener, null);
                maybeFinishServerCloseContext(closeContext);
            }
            request.complete(ERROR, null);
            return;
        }

        BluetoothSocket accepted;
        try {
            accepted = listener.accept();
        } catch (Exception failure) {
            throw failure;
        }
        boolean canPublish;
        BluetoothSocket existingAccepted = null;
        boolean ownedAccepted = ownAcceptedSocket(accepted);
        synchronized (serverLifecycleLock) {
            canPublish = ownedAccepted && serverLifecycleState == ServerLifecycleState.IDLE
                    && !operation.cancelled && !request.isCancelled() && !request.isComplete()
                    && !shuttingDown.get() && activeServerAccept.get() == operation
                    && serverSocket == listener && serverGenerations.get() == operation.generation;
            if (canPublish) existingAccepted = acceptedSockets.putIfAbsent(operation.serviceKey, accepted);
        }
        if (!canPublish || existingAccepted != null) {
            closeSocketAsync(accepted);
            request.complete(ERROR, null);
            return;
        }
        if (!request.complete(NO_ERROR, null) || request.getResultCode() != NO_ERROR) {
            acceptedSockets.remove(operation.serviceKey, accepted);
            closeSocketAsync(accepted);
        }
    }

    private ServerCloseContext finishServerListenerCreation(ServerAcceptOperation operation,
                                                            BluetoothServerSocket listener) {
        synchronized (serverLifecycleLock) {
            operation.listenerCreationInFlight = false;
            ServerCloseContext context = operation.closeContext;
            if (context != serverCloseContext) context = serverCloseContext;
            boolean current = context == null && serverLifecycleState == ServerLifecycleState.IDLE
                    && !shuttingDown.get() && !operation.cancelled && !operation.request.isComplete()
                    && activeServerAccept.get() == operation
                    && serverGenerations.get() == operation.generation;
            if (current && listener != null) {
                operation.listener = listener;
                serverSocket = listener;
                return null;
            }
            if (context == null && listener != null) context = newServerCloseContextLocked(operation, null, null);
            if (context != null) {
                operation.closeContext = context;
                context.pendingCreations.remove(operation);
                operation.listener = listener;
                trackServerListenerLocked(context, listener);
            }
            return context;
        }
    }

    private static boolean closeServerSocketNow(BluetoothServerSocket listener) {
        if (listener == null) return true;
        try {
            listener.close();
            return true;
        } catch (IOException failure) {
            AndroidUtils.handleException(failure, false);
            return false;
        }
    }

    @Override
    public void shutdown() {
        if (!shuttingDown.compareAndSet(false, true)) return;

        synchronized (cleanupLock) {
            cleanupShutdownStarted = true;
            cleanupShutdownDeadlineElapsed = SystemClock.elapsedRealtime() + CLEANUP_SHUTDOWN_TIMEOUT_MS;
        }

        super.shutdown();
        for (BluetoothRequest request : outstandingRequests.keySet()) request.cancel();

        DiscoveryOperation discovery = activeDiscovery.get();
        if (discovery != null) finishDiscovery(discovery, false, true);

        DiscoverabilityOperation discoverability = activeDiscoverability.getAndSet(null);
        if (discoverability != null) discoverability.request.cancel();

        ServerAcceptOperation accept = activeServerAccept.get();
        if (accept != null) cancelServerAccept(accept);
        ServerCloseContext serverCleanup;
        boolean retryServerCleanup;
        synchronized (serverLifecycleLock) {
            BluetoothServerSocket listener = serverSocket;
            serverSocket = null;
            serverCleanup = serverCloseContext;
            if (listener != null) {
                if (serverCleanup == null)
                    serverCleanup = newServerCloseContextLocked(null, null, null);
                trackServerListenerLocked(serverCleanup, listener);
            }
            retryServerCleanup = serverCleanup != null
                    && serverLifecycleState == ServerLifecycleState.CLOSE_FAILED;
        }
        if (serverCleanup != null) {
            if (retryServerCleanup)
                scheduleServerRecovery(serverCleanup);
            else {
                scheduleServerListenerClosures(serverCleanup);
                maybeFinishServerCloseContext(serverCleanup);
            }
        }

        for (BluetoothSession session : sessionsByKey.values()) invalidateSession(session, session.socket);
        for (Map.Entry<String, BluetoothSocket> accepted : acceptedSockets.entrySet()) {
            invalidateAcceptedSocket(accepted.getKey(), accepted.getValue());
        }

        connectionExecutor.shutdown();
        discoveryControlExecutor.shutdown();
        ioExecutor.shutdown();
        serverAcceptExecutor.shutdown();
        socketControlExecutor.shutdown();
        timeoutExecutor.shutdownNow();
        if (!ensureCleanupRetryDispatcherStarted()) {
            reportCleanupFailure(new RejectedExecutionException(
                    "Bluetooth cleanup shutdown dispatcher could not be started"));
            finalizeCleanupShutdown(true);
        } else {
            progressCleanupShutdown();
        }
    }
}
