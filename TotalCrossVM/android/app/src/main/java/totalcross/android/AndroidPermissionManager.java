// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Coordinates Android runtime permissions and app-specific special access flows. */
public final class AndroidPermissionManager {
    public static final int DENIED = -1;
    public static final int PENDING = 0;
    public static final int GRANTED = 1;

    private static final int FIRST_OPERATION_REQUEST = 0x6000;
    private static final int LAST_OPERATION_REQUEST = 0x6FFF;
    private static final long REQUEST_TIMEOUT_MS = 120_000L;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final AtomicLong NEXT_ID = new AtomicLong();
    private static final Map<String, Operation> pending = new HashMap<>();
    private static final ArrayDeque<Operation> queue = new ArrayDeque<>();
    private static WeakReference<Activity> host = new WeakReference<>(null);
    private static Operation active;
    private static Map<String, Boolean> deniedFileAccessSnapshot;
    private static long interactionGeneration;
    private static long deniedFileAccessInteractionGeneration;
    private static int nextRequestCode = FIRST_OPERATION_REQUEST;

    public enum MediaAccess {
        FULL,
        PARTIAL,
        DENIED
    }

    public interface Callback {
        void onComplete(Result result);
    }

    public static final class Result {
        public final boolean granted;
        public final boolean authorizationChanged;
        public final MediaAccess mediaAccess;

        private Result(boolean granted, boolean authorizationChanged, MediaAccess mediaAccess) {
            this.granted = granted;
            this.authorizationChanged = authorizationChanged;
            this.mediaAccess = mediaAccess;
        }
    }

    private enum PlanType {
        RUNTIME,
        FILE_ACCESS,
        ALL_FILES,
        INSTALL
    }

    private enum StageType {
        RUNTIME,
        ALL_FILES,
        INSTALL
    }

    private static final class Plan {
        final PlanType type;
        final boolean explicit;
        final List<String> permissions;
        final String key;

        Plan(PlanType type, String... permissions) {
            this(type, false, permissions);
        }

        Plan(PlanType type, boolean explicit, String... permissions) {
            this.type = type;
            this.explicit = explicit;
            TreeSet<String> ordered = new TreeSet<>();
            if (permissions != null) {
                for (String permission : permissions) {
                    if (permission != null && !permission.isEmpty()) {
                        ordered.add(permission);
                    }
                }
            }
            this.permissions = new ArrayList<>(ordered);
            this.key = type.name() + ":" + (explicit ? "explicit:" : "") + String.join(",", this.permissions);
        }
    }

    private static final class Stage {
        final StageType type;
        final List<String> permissions;

        Stage(StageType type, List<String> permissions) {
            this.type = type;
            this.permissions = permissions;
        }
    }

    private static final class Operation {
        final Plan plan;
        final List<Callback> callbacks = new ArrayList<>();
        final Map<String, Boolean> before = new HashMap<>();
        List<Stage> stages;
        int stageIndex;
        long id;
        boolean authorizationChangedBeforeStart;
        int pendingRequestCode;
        StageType waitingFor;

        Operation(Plan plan) {
            this.plan = plan;
        }
    }

    private AndroidPermissionManager() {
    }

    static void attach(Activity activity) {
        synchronized (pending) {
            host = new WeakReference<>(activity);
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            startNext();
        } else {
            MAIN.post(AndroidPermissionManager::startNext);
        }
    }

    public static int requestBlocking(String... permissions) {
        return requestBlocking(new Plan(PlanType.RUNTIME, permissions));
    }

    public static int requestFileAccessBlocking() {
        return requestBlocking(new Plan(PlanType.FILE_ACCESS));
    }

    public static void requestRuntime(Callback callback, String... permissions) {
        submit(new Plan(PlanType.RUNTIME, permissions), callback);
    }

    public static void requestInstallAuthorization(Callback callback) {
        submit(new Plan(PlanType.INSTALL), callback);
    }

    public static void requestAllFilesAccess(Callback callback) {
        submit(new Plan(PlanType.ALL_FILES), callback);
    }

    public static void requestFileAccess(Callback callback) {
        submit(new Plan(PlanType.FILE_ACCESS, true), callback);
    }

    static void onUserInteraction() {
        Runnable advanceGeneration = () -> interactionGeneration++;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            advanceGeneration.run();
        } else {
            MAIN.post(advanceGeneration);
        }
    }

    private static int requestBlocking(Plan plan) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            submit(plan, result -> { });
            return PENDING;
        }
        CountDownLatch latch = new CountDownLatch(1);
        final Result[] result = new Result[1];
        Callback waiter = completed -> {
            result[0] = completed;
            latch.countDown();
        };
        submit(plan, waiter);
        try {
            if (!latch.await(REQUEST_TIMEOUT_MS + 5_000L, TimeUnit.MILLISECONDS)) {
                cancelWaiter(plan.key, waiter);
                return DENIED;
            }
        } catch (InterruptedException interrupted) {
            cancelWaiter(plan.key, waiter);
            Thread.currentThread().interrupt();
            return DENIED;
        }
        if (result[0] == null) {
            return DENIED;
        }
        return plan.type == PlanType.FILE_ACCESS
                ? (result[0].authorizationChanged ? GRANTED : DENIED)
                : (result[0].granted ? GRANTED : DENIED);
    }

    private static void submit(Plan plan, Callback callback) {
        Callback effectiveCallback = callback == null ? result -> { } : callback;
        Runnable enqueue = () -> {
            Activity activity = currentHost();
            if (activity == null) {
                effectiveCallback.onComplete(new Result(false, false, MediaAccess.DENIED));
                return;
            }
            Operation operation = pending.get(plan.key);
            if (operation != null) {
                operation.callbacks.add(effectiveCallback);
                return;
            }
            operation = new Operation(plan);
            operation.id = NEXT_ID.incrementAndGet();
            operation.callbacks.add(effectiveCallback);
            pending.put(plan.key, operation);
            queue.add(operation);
            startNext();
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            enqueue.run();
        } else {
            MAIN.post(enqueue);
        }
    }

    private static void cancelWaiter(String key, Callback waiter) {
        MAIN.post(() -> {
            Operation operation = pending.get(key);
            if (operation == null) return;
            operation.callbacks.remove(waiter);
            if (operation.callbacks.isEmpty() && operation != active) {
                queue.remove(operation);
                pending.remove(key);
            }
        });
    }

    private static void startNext() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(AndroidPermissionManager::startNext);
            return;
        }
        if (active != null) {
            return;
        }
        Activity activity = currentHost();
        if (activity == null) {
            return;
        }
        active = queue.poll();
        if (active == null) {
            return;
        }
        active.stages = buildStages(activity, active.plan);
        active.before.clear();
        for (String permission : relevantPermissions(activity, active.plan)) {
            active.before.put(permission, isAuthorized(activity, permission));
        }
        if (active.plan.type == PlanType.FILE_ACCESS && !active.plan.explicit && deniedFileAccessSnapshot != null) {
            Map<String, Boolean> current = authorizationSnapshot(activity, active.plan);
            if (deniedFileAccessSnapshot.equals(current)
                    && deniedFileAccessInteractionGeneration == interactionGeneration) {
                finishActive(false);
                return;
            }
            for (Map.Entry<String, Boolean> entry : deniedFileAccessSnapshot.entrySet()) {
                if (!entry.getValue() && Boolean.TRUE.equals(current.get(entry.getKey()))) {
                    active.authorizationChangedBeforeStart = true;
                }
            }
            deniedFileAccessSnapshot = null;
            if (active.authorizationChangedBeforeStart) {
                finishActive(true);
                return;
            }
        } else if (active.plan.type == PlanType.FILE_ACCESS && active.plan.explicit) {
            deniedFileAccessSnapshot = null;
        }
        final long operationId = active.id;
        MAIN.postDelayed(() -> {
            if (active != null && active.id == operationId) {
                finishActive(false);
            }
        }, REQUEST_TIMEOUT_MS);
        advance();
    }

    private static List<Stage> buildStages(Activity activity, Plan plan) {
        List<Stage> stages = new ArrayList<>();
        switch (plan.type) {
            case RUNTIME:
                stages.add(new Stage(StageType.RUNTIME, plan.permissions));
                break;
            case FILE_ACCESS:
                stages.add(new Stage(StageType.RUNTIME, mediaPermissionCandidates(activity)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    stages.add(new Stage(StageType.ALL_FILES, Collections.singletonList(Manifest.permission.MANAGE_EXTERNAL_STORAGE)));
                }
                break;
            case ALL_FILES:
                stages.add(new Stage(StageType.ALL_FILES, Collections.singletonList(Manifest.permission.MANAGE_EXTERNAL_STORAGE)));
                break;
            case INSTALL:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stages.add(new Stage(StageType.INSTALL, Collections.singletonList(Manifest.permission.REQUEST_INSTALL_PACKAGES)));
                }
                break;
        }
        return stages;
    }

    private static List<String> relevantPermissions(Activity activity, Plan plan) {
        Set<String> names = new LinkedHashSet<>();
        if (plan.type == PlanType.RUNTIME) {
            names.addAll(plan.permissions);
        } else if (plan.type == PlanType.FILE_ACCESS) {
            names.addAll(mediaPermissionCandidates(activity));
            names.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } else if (plan.type == PlanType.ALL_FILES) {
            names.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } else if (plan.type == PlanType.INSTALL) {
            names.add(Manifest.permission.REQUEST_INSTALL_PACKAGES);
        }
        return new ArrayList<>(names);
    }

    private static Map<String, Boolean> authorizationSnapshot(Activity activity, Plan plan) {
        Map<String, Boolean> snapshot = new HashMap<>();
        for (String permission : relevantPermissions(activity, plan)) {
            snapshot.put(permission, isAuthorized(activity, permission));
        }
        return snapshot;
    }

    private static boolean hasMissingFileAccessPermission(Activity activity) {
        for (String permission : mediaPermissionCandidates(activity)) {
            if (isDeclared(activity, permission) && !isAuthorized(activity, permission)) {
                return true;
            }
        }
        String allFiles = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && isDeclared(activity, allFiles) && !isAuthorized(activity, allFiles);
    }

    private static List<String> mediaPermissionCandidates(Activity activity) {
        Set<String> declared = declaredPermissions(activity);
        List<String> candidates = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            addIfDeclared(candidates, declared, Manifest.permission.READ_MEDIA_IMAGES);
            addIfDeclared(candidates, declared, Manifest.permission.READ_MEDIA_VIDEO);
            if (Build.VERSION.SDK_INT >= 34) {
                addIfDeclared(candidates, declared, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
            }
        } else {
            addIfDeclared(candidates, declared, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                addIfDeclared(candidates, declared, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        return candidates;
    }

    private static void addIfDeclared(List<String> permissions, Set<String> declared, String permission) {
        if (declared.contains(permission)) {
            permissions.add(permission);
        }
    }

    private static void advance() {
        Activity activity = currentHost();
        if (active == null || activity == null) {
            finishActive(false);
            return;
        }
        while (active.stageIndex < active.stages.size()) {
            Stage stage = active.stages.get(active.stageIndex);
            if (stage.type == StageType.RUNTIME) {
                List<String> missing = new ArrayList<>();
                for (String permission : stage.permissions) {
                    if (!isSpecial(permission) && isDeclared(activity, permission)
                            && isRuntimePermission(activity, permission) && !isAuthorized(activity, permission)) {
                        missing.add(permission);
                    }
                }
                if (missing.isEmpty()) {
                    active.stageIndex++;
                    continue;
                }
                active.pendingRequestCode = allocateRequestCode();
                active.waitingFor = StageType.RUNTIME;
                try {
                    ActivityCompat.requestPermissions(activity, missing.toArray(new String[0]), active.pendingRequestCode);
                } catch (RuntimeException exception) {
                    active.pendingRequestCode = 0;
                    active.waitingFor = null;
                    finishActive(false);
                }
                return;
            }
            if (stage.type == StageType.ALL_FILES) {
                String permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || !isDeclared(activity, permission) || isAuthorized(activity, permission)) {
                    active.stageIndex++;
                    continue;
                }
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                if (!startSettings(activity, intent, StageType.ALL_FILES)) {
                    active.stageIndex++;
                    continue;
                }
                return;
            }
            String permission = Manifest.permission.REQUEST_INSTALL_PACKAGES;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !isDeclared(activity, permission) || isAuthorized(activity, permission)) {
                active.stageIndex++;
                continue;
            }
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + activity.getPackageName()));
            if (!startSettings(activity, intent, StageType.INSTALL)) {
                active.stageIndex++;
                continue;
            }
            return;
        }
        finishActive(true);
    }

    private static boolean startSettings(Activity activity, Intent intent, StageType stageType) {
        int requestCode = allocateRequestCode();
        try {
            active.pendingRequestCode = requestCode;
            active.waitingFor = stageType;
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (RuntimeException exception) {
            active.pendingRequestCode = 0;
            active.waitingFor = null;
            return false;
        }
    }

    private static int allocateRequestCode() {
        int code = nextRequestCode++;
        if (nextRequestCode > LAST_OPERATION_REQUEST) nextRequestCode = FIRST_OPERATION_REQUEST;
        return code;
    }

    static boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (active == null || requestCode != active.pendingRequestCode || active.waitingFor != StageType.RUNTIME) {
            return false;
        }
        long operationId = active.id;
        MAIN.post(() -> {
            if (active == null || active.id != operationId || active.pendingRequestCode != requestCode) {
                return;
            }
            active.pendingRequestCode = 0;
            active.waitingFor = null;
            if (active.stageIndex >= active.stages.size()
                    || active.stages.get(active.stageIndex).type != StageType.RUNTIME) {
                finishActive(false);
                return;
            }
            // Recheck each permission independently; result-array ordering is not authoritative.
            active.stageIndex++;
            advance();
        });
        return true;
    }

    static boolean onActivityResult(int requestCode, int resultCode) {
        if (active == null || requestCode != active.pendingRequestCode
                || (active.waitingFor != StageType.ALL_FILES && active.waitingFor != StageType.INSTALL)) {
            return false;
        }
        long operationId = active.id;
        StageType expected = active.waitingFor;
        MAIN.post(() -> {
            if (active == null || active.id != operationId || active.pendingRequestCode != requestCode) {
                return;
            }
            if (active.stageIndex >= active.stages.size() || active.stages.get(active.stageIndex).type != expected) {
                finishActive(false);
                return;
            }
            active.pendingRequestCode = 0;
            active.waitingFor = null;
            // Settings Activities do not promise a useful result code. Authorization is re-read.
            active.stageIndex++;
            advance();
        });
        return true;
    }

    private static void finishActive(boolean completedStages) {
        Operation operation = active;
        if (operation == null) {
            startNext();
            return;
        }
        Activity activity = currentHost();
        boolean changed = false;
        boolean granted = completedStages;
        if (activity != null) {
            for (Map.Entry<String, Boolean> entry : operation.before.entrySet()) {
                boolean now = isAuthorized(activity, entry.getKey());
                changed |= !entry.getValue() && now;
            }
            changed |= operation.authorizationChangedBeforeStart;
            if (operation.plan.type == PlanType.RUNTIME) {
                granted = !operation.plan.permissions.isEmpty();
                for (String permission : operation.plan.permissions) {
                    granted &= isDeclared(activity, permission) && isAuthorized(activity, permission);
                }
            } else if (operation.plan.type == PlanType.ALL_FILES) {
                granted = isDeclared(activity, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                        && isAuthorized(activity, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            } else if (operation.plan.type == PlanType.INSTALL) {
                granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                        || (isDeclared(activity, Manifest.permission.REQUEST_INSTALL_PACKAGES)
                        && isAuthorized(activity, Manifest.permission.REQUEST_INSTALL_PACKAGES));
            } else if (operation.plan.type == PlanType.FILE_ACCESS) {
                granted = changed;
                if (hasMissingFileAccessPermission(activity)) {
                    deniedFileAccessSnapshot = authorizationSnapshot(activity, operation.plan);
                    deniedFileAccessInteractionGeneration = interactionGeneration;
                } else {
                    deniedFileAccessSnapshot = null;
                }
            }
        } else {
            granted = false;
        }
        MediaAccess mediaAccess = activity == null ? MediaAccess.DENIED : getMediaAccess(activity);
        Result result = new Result(granted, changed, mediaAccess);
        operation.pendingRequestCode = 0;
        operation.waitingFor = null;
        active = null;
        pending.remove(operation.plan.key);
        for (Callback callback : operation.callbacks) {
            try {
                callback.onComplete(result);
            } catch (RuntimeException ignored) {
            }
        }
        startNext();
    }

    public static boolean isDeclared(String permission) {
        Activity activity = currentHost();
        return activity != null && isDeclared(activity, permission);
    }

    public static boolean isAuthorized(String permission) {
        Activity activity = currentHost();
        return activity != null && isDeclared(activity, permission) && isAuthorized(activity, permission);
    }

    private static boolean isDeclared(Activity activity, String permission) {
        return declaredPermissions(activity).contains(permission);
    }

    private static Set<String> declaredPermissions(Activity activity) {
        try {
            PackageInfo info;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                info = activity.getPackageManager().getPackageInfo(activity.getPackageName(),
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
            } else {
                info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            }
            Set<String> names = new LinkedHashSet<>();
            if (info.requestedPermissions != null) {
                names.addAll(Arrays.asList(info.requestedPermissions));
            }
            return names;
        } catch (PackageManager.NameNotFoundException | RuntimeException exception) {
            return Collections.emptySet();
        }
    }

    private static boolean isRuntimePermission(Activity activity, String permission) {
        if (isSpecial(permission)) {
            return false;
        }
        try {
            PermissionInfo info = activity.getPackageManager().getPermissionInfo(permission, 0);
            return (info.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
                    == PermissionInfo.PROTECTION_DANGEROUS;
        } catch (PackageManager.NameNotFoundException exception) {
            return false;
        }
    }

    private static boolean isSpecial(String permission) {
        return Manifest.permission.MANAGE_EXTERNAL_STORAGE.equals(permission)
                || Manifest.permission.REQUEST_INSTALL_PACKAGES.equals(permission);
    }

    private static boolean isAuthorized(Activity activity, String permission) {
        if (Manifest.permission.MANAGE_EXTERNAL_STORAGE.equals(permission)) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager();
        }
        if (Manifest.permission.REQUEST_INSTALL_PACKAGES.equals(permission)) {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                    || activity.getPackageManager().canRequestPackageInstalls();
        }
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static Activity currentHost() {
        synchronized (pending) {
            Activity activity = host.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return null;
            }
            return activity;
        }
    }

    public static MediaAccess getMediaAccess(Activity activity) {
        Set<String> declared = declaredPermissions(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return declared.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && isAuthorized(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    ? MediaAccess.FULL : MediaAccess.DENIED;
        }
        boolean imagesDeclared = declared.contains(Manifest.permission.READ_MEDIA_IMAGES);
        boolean videoDeclared = declared.contains(Manifest.permission.READ_MEDIA_VIDEO);
        boolean images = imagesDeclared && isAuthorized(activity, Manifest.permission.READ_MEDIA_IMAGES);
        boolean video = videoDeclared && isAuthorized(activity, Manifest.permission.READ_MEDIA_VIDEO);
        boolean selected = Build.VERSION.SDK_INT >= 34
                && declared.contains("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
                && isAuthorized(activity, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
        boolean mediaTypeDeclared = imagesDeclared || videoDeclared;
        boolean allDeclaredGranted = mediaTypeDeclared && (!imagesDeclared || images) && (!videoDeclared || video);
        if (allDeclaredGranted && !(Build.VERSION.SDK_INT >= 34 && targetSdk(activity) < 34 && (images || video))) {
            return MediaAccess.FULL;
        }
        if (images || video || selected) {
            return MediaAccess.PARTIAL;
        }
        return MediaAccess.DENIED;
    }

    private static int targetSdk(Activity activity) {
        try {
            return activity.getApplicationInfo().targetSdkVersion;
        } catch (RuntimeException exception) {
            return 0;
        }
    }
}
