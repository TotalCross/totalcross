// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android.compat;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** A single correlated operation crossing the VM and Android threads. */
public final class BluetoothRequest {
    private static final AtomicLong NEXT_ID = new AtomicLong();

    private final long id = NEXT_ID.incrementAndGet();
    private final int operation;
    private final String address;
    private final byte[] bytes;
    private final int offset;
    private final int length;
    private final CountDownLatch completionLatch = new CountDownLatch(1);
    private final AtomicBoolean completed = new AtomicBoolean();
    private final Object cancellationLock = new Object();
    private final List<Runnable> cancellationCallbacks = new ArrayList<>();
    private final Set<Runnable> registeredCallbacks = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Runnable> completionCallbacks = new ArrayList<>();
    private final Set<Runnable> registeredCompletionCallbacks = Collections.newSetFromMap(new IdentityHashMap<>());
    private volatile int resultCode = Level5.ERROR;
    private volatile Object payload;
    private volatile boolean cancelled;
    private final long deadlineElapsedRealtime;

    public BluetoothRequest(int operation, String address, byte[] bytes, int offset, int length) {
        this.operation = operation;
        this.address = address;
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        long timeoutMillis = timeoutForOperation(operation);
        this.deadlineElapsedRealtime = timeoutMillis <= 0 ? 0 : SystemClock.elapsedRealtime() + timeoutMillis;
    }

    private static long timeoutForOperation(int operation) {
        switch (operation) {
            case Level5.BT_CONNECT:
                return 60000;
            case Level5.BT_READ:
            case Level5.BT_WRITE:
                return 12000;
            case Level5.BT_CLOSE:
            case Level5.BT_SERVER_CLOSE:
                return 5000;
            case Level5.BT_GET_UNPAIRED_DEVICES:
                return 30000;
            case Level5.BT_MAKE_DISCOVERABLE:
                return 120000;
            case Level5.BT_SERVER_ACCEPT:
                return 0;
            default:
                return 5000;
        }
    }

    public long getId() {
        return id;
    }

    public int getOperation() {
        return operation;
    }

    public String getAddress() {
        return address;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Object getPayload() {
        return payload;
    }

    public boolean isComplete() {
        return completed.get();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean complete(int code, Object result) {
        List<Runnable> cancelCallbacks;
        List<Runnable> completedCallbacks;
        synchronized (cancellationLock) {
            if (completed.get()) return false;
            boolean expired = deadlineElapsedRealtime > 0
                    && SystemClock.elapsedRealtime() >= deadlineElapsedRealtime;
            if (expired) cancelled = true;
            boolean failed = cancelled;
            resultCode = failed ? Level5.ERROR : code;
            payload = failed ? null : result;
            cancelCallbacks = failed ? new ArrayList<>(cancellationCallbacks) : Collections.emptyList();
            cancellationCallbacks.clear();
            completedCallbacks = new ArrayList<>(completionCallbacks);
            completionCallbacks.clear();
            completed.set(true);
        }
        runCallbacks(cancelCallbacks);
        runCallbacks(completedCallbacks);
        completionLatch.countDown();
        return true;
    }

    public boolean cancel() {
        synchronized (cancellationLock) {
            if (completed.get() || cancelled) {
                return false;
            }
            cancelled = true;
        }
        complete(Level5.ERROR, null);
        return true;
    }

    /**
     * Registers one cancellation action. A late registration runs immediately after
     * cancellation, but never after normal completion.
     */
    public boolean registerCancellationCallback(Runnable callback) {
        if (callback == null) {
            return false;
        }
        boolean runNow;
        synchronized (cancellationLock) {
            if (!registeredCallbacks.add(callback)) {
                return true;
            }
            if (cancelled) {
                runNow = true;
            } else if (completed.get()) {
                return false;
            } else {
                cancellationCallbacks.add(callback);
                runNow = false;
            }
        }
        if (runNow) {
            runCallback(callback);
        }
        return true;
    }

    public boolean registerCompletionCallback(Runnable callback) {
        if (callback == null) return false;
        boolean runNow;
        synchronized (cancellationLock) {
            if (!registeredCompletionCallbacks.add(callback)) return true;
            if (completed.get()) {
                runNow = true;
            } else {
                completionCallbacks.add(callback);
                runNow = false;
            }
        }
        if (runNow) runCallback(callback);
        return true;
    }

    public long getDeadlineElapsedRealtime() {
        return deadlineElapsedRealtime;
    }

    public boolean isDeadlineExpired() {
        long deadline = deadlineElapsedRealtime;
        return deadline > 0 && SystemClock.elapsedRealtime() >= deadline;
    }

    public boolean await(long timeoutMillis) throws InterruptedException {
        return completionLatch.await(Math.max(0, timeoutMillis), TimeUnit.MILLISECONDS);
    }

    public void await() throws InterruptedException {
        completionLatch.await();
    }

    private static void runCallbacks(List<Runnable> callbacks) {
        for (Runnable callback : callbacks) {
            runCallback(callback);
        }
    }

    private static void runCallback(Runnable callback) {
        try {
            callback.run();
        } catch (RuntimeException ignored) {
            // Cancellation must still release the caller even if cleanup fails.
        }
    }
}
