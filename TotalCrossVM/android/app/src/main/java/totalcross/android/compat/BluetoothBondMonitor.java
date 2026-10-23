// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android.compat;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Tracks pairing outcomes and receiver registration for one Bluetooth connection request. */
final class BluetoothBondMonitor {
    static final String EXTRA_UNBOND_REASON = "android.bluetooth.device.extra.REASON";
    static final int UNBOND_REASON_AUTH_FAILED = 1;
    enum Outcome { BONDED, AUTHENTICATION_FAILED, FAILED, CANCELLED }

    private final Handler mainHandler;
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

    BluetoothBondMonitor(BluetoothDevice target, BluetoothRequest request, totalcross.android.Loader loader, Handler mainHandler) {
        this.mainHandler = mainHandler;
        this.target = target;
        this.targetAddress = Level5Impl.normalizeApiAddress(target.getAddress());
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
                if (changedAddress == null || !Level5Impl.normalizeApiAddress(changedAddress).equals(targetAddress)) return;
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
            long remaining = Level5Impl.remainingDeadlineMillis(request);
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

    Outcome awaitOutcome() throws InterruptedException {
        synchronized (lock) {
            while (!bonded && !bondFailed && !request.isComplete()) {
                long remaining = Level5Impl.remainingDeadlineMillis(request);
                if (remaining <= 0) {
                    request.cancel();
                    break;
                }
                lock.wait(remaining);
            }
            if (bonded) return Outcome.BONDED;
            if (authenticationFailed) return Outcome.AUTHENTICATION_FAILED;
            if (bondFailed) return Outcome.FAILED;
            return Outcome.CANCELLED;
        }
    }

    Outcome currentOutcome() {
        synchronized (lock) {
            if (bonded) return Outcome.BONDED;
            if (authenticationFailed) return Outcome.AUTHENTICATION_FAILED;
            if (bondFailed) return Outcome.FAILED;
            return Outcome.CANCELLED;
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
