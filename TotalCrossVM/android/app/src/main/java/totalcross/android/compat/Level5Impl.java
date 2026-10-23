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
import android.content.Intent;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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

/**
 * Owns socket transport, server lifecycle and deferred resource cleanup.
 * Socket ownership and cancellation remain together intentionally.
 * Device discovery is delegated to BluetoothDiscovery.
 */
public class Level5Impl extends Level5 {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int MAX_EMERGENCY_CLEANUP_THREADS = 4;
    private static final int MAX_DEFERRED_CLIENT_CLEANUPS = 256;
    private static final int CLIENT_SOCKET_CLOSE_MAX_ATTEMPTS = 3;
    private static final long CLEANUP_RETRY_INTERVAL_MS = 250;
    private static final long CLEANUP_SHUTDOWN_TIMEOUT_MS = 10000;
    private static final AtomicLong THREAD_IDS = new AtomicLong();
    private static final AtomicLong EMERGENCY_CLEANUP_THREAD_IDS = new AtomicLong();
    private static final AtomicInteger ACTIVE_EMERGENCY_CLEANUP_THREADS = new AtomicInteger();

    private final ThreadPoolExecutor connectionExecutor = boundedExecutor("connection", 2, 16);
    private final ThreadPoolExecutor discoveryControlExecutor = boundedExecutor("discovery-control", 1, 8);
    private final ThreadPoolExecutor ioExecutor = boundedExecutor("io", 4, 32);
    private final ThreadPoolExecutor socketControlExecutor = boundedExecutor("control", 2, 16);
    private final ThreadPoolExecutor serverAcceptExecutor = boundedExecutor("server", 1, 1);
    private final ThreadPoolExecutor cleanupExecutor = boundedExecutor("cleanup", 2, 64);
    private final ScheduledThreadPoolExecutor cleanupRetryDispatcher = createCleanupRetryDispatcher();
    private final ScheduledThreadPoolExecutor timeoutExecutor = createTimeoutExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final BluetoothDiscovery discovery = new BluetoothDiscovery(this, mainHandler,
            connectionExecutor, discoveryControlExecutor);

    private volatile BluetoothAdapter btAdapter;
    private final ConcurrentHashMap<SessionKey, BluetoothSession> sessionsByKey = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BluetoothSession> sessionsByHandle = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BluetoothSocket> acceptedSockets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BluetoothSocket, SocketIoState> socketIoStates = new ConcurrentHashMap<>();
    private final AtomicLong sessionGenerations = new AtomicLong();
    private final AtomicLong serverGenerations = new AtomicLong();
    private final AtomicBoolean shuttingDown = new AtomicBoolean();
    private final ConcurrentHashMap<BluetoothRequest, Boolean> outstandingRequests = new ConcurrentHashMap<>();
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

    private static ThreadPoolExecutor boundedExecutor(String name, int threads, int queueCapacity) {
        return new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity), namedDaemonFactory(name),
                new ThreadPoolExecutor.AbortPolicy());
    }

    static String normalizeApiAddress(String address) {
        String normalized = normalizeBluetoothAddressOrNull(address);
        return normalized == null ? "" : normalized;
    }

    static String normalizeBluetoothAddressOrNull(String address) {
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

    static long remainingDeadlineMillis(BluetoothRequest request) {
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
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    private static ScheduledThreadPoolExecutor createCleanupRetryDispatcher() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                namedDaemonFactory("cleanup-retry"));
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return executor;
    }

    BluetoothAdapter getAdapter() {
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
                dispatch(request, socketControlExecutor, () -> discovery.makeDiscoverable(request));
                break;
            case BT_GET_UNPAIRED_DEVICES:
                dispatch(request, connectionExecutor, () -> discovery.getUnpairedDevices(request));
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

    boolean isShuttingDown() {
        return shuttingDown.get();
    }

    void enqueueDiscoveryCleanup(Object resource, Runnable cancel,
                                 java.util.function.Consumer<Boolean> completed) {
        enqueuePendingCleanup(resource, () -> {
            cancel.run();
            return true;
        }, completed::accept);
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode) {
        return discovery.handleActivityResult(requestCode, resultCode);
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
        BluetoothBondMonitor bondMonitor = null;
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
            bondMonitor = new BluetoothBondMonitor(device, request, Launcher4A.loader, mainHandler);
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
                BluetoothBondMonitor.Outcome outcome = bondMonitor.currentOutcome();
                if (outcome == BluetoothBondMonitor.Outcome.AUTHENTICATION_FAILED) {
                    request.complete(INVALID_PASSWORD, null);
                    return;
                }
                if (!bondMonitor.hasObservedBonding()) {
                    AndroidUtils.handleException(firstFailure, false);
                    request.complete(ERROR, null);
                    return;
                }
                outcome = bondMonitor.awaitOutcome();
                if (outcome == BluetoothBondMonitor.Outcome.AUTHENTICATION_FAILED) {
                    request.complete(INVALID_PASSWORD, null);
                    return;
                }
                if (outcome != BluetoothBondMonitor.Outcome.BONDED || request.isComplete()) {
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
                    request.complete(bondMonitor.currentOutcome() == BluetoothBondMonitor.Outcome.AUTHENTICATION_FAILED
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

    static void reportCleanupFailure(Throwable failure) {
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

        discovery.shutdown();

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
