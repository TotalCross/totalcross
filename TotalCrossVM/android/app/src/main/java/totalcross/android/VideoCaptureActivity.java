// Copyright (C) 2025-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Size;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import totalcross.AndroidUtils;

public class VideoCaptureActivity extends AdjustedInsetsActivity {

    public enum VideoQuality {
        FHD(1920, 1080), HD(1280, 720), SD(720, 480);

        final int width;
        final int height;

        VideoQuality(int width, int height) {
            this.width = width;
            this.height = height;
        }

        private Size getQualitySelectorFor(boolean isLandscape) {
            return isLandscape ? new Size(width, height) : new Size(height, width);
        }

        public static Size getQualitySelectorFor(int width, int height, boolean isLandscape, VideoQuality quality) {
            if (width <= 0 || height <= 0) {
                return quality.getQualitySelectorFor(isLandscape);
            }
            return isLandscape ? new Size(width, height) : new Size(height, width);
        }
    }

    public static final String EXTRA_MAX_SECONDS = "max_seconds";
    public static final String EXTRA_TARGET_FPS = "target_fps";
    public static final String EXTRA_QUALITY = "quality";

    private Camera camera;
    private ImageButton btnSwitchCamera;
    private TextView tvTimer;
    private ImageButton btnFlash;

    private String lastSavedVideoPath = null;

    private static final int PLAYBACK_REQ = 201; // new request code for the player
    private static final int IMAGE_REQ = 301;

    private PreviewView previewView;
    private ImageButton btnRecord;
    private ImageButton btnBackCapture;

    // CameraX
    private ProcessCameraProvider cameraProvider;
    private VideoCapture<Recorder> videoCapture;

    private enum RecordingPhase {
        STARTING,
        RECORDING,
        FINALIZING
    }

    private enum RecordingStopReason {
        NONE,
        COMPLETED,
        CANCELLED
    }

    private static final class RecordingSession {
        final long id;
        final long gestureGeneration;
        final File destinationFile;
        final File stagingFile;
        Recording recording;
        RecordingPhase phase = RecordingPhase.STARTING;
        RecordingStopReason stopReason = RecordingStopReason.NONE;
        boolean stopCalled;
        boolean closeCalled;
        boolean stopFailed;
        boolean stopFailureWatchdogScheduled;
        boolean stopFailureTimeoutReported;
        boolean finalizeProcessed;
        Runnable stopFailureWatchdog;

        RecordingSession(long id, long gestureGeneration, File destinationFile, File stagingFile) {
            this.id = id;
            this.gestureGeneration = gestureGeneration;
            this.destinationFile = destinationFile;
            this.stagingFile = stagingFile;
        }
    }

    private RecordingSession currentRecordingSession;
    private long nextRecordingSessionId;

    // Progress UI
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private long recordingStartTime = 0;
    private long progressSessionId;
    private final int PROGRESS_UPDATE_INTERVAL = 50; // ms
    private boolean progressActive = false;
    private static final long STOP_FAILURE_WATCHDOG_MS = 10_000L;

    // Params
    private int maxSeconds = 30;
    private int targetFps = 30;
    private VideoQuality qualidade = VideoQuality.HD;

    private RecordProgressDrawable progressDrawable;

    private int deviceOrientation = Surface.ROTATION_0;

    private int width;

    private int height;

    private int stillQuality;

    private int bitrate;

    private ImageCapture imageCapture;
    private static final long PHOTO_THRESHOLD_MS = 200;

    public static final int MODE_VIDEO = 0;
    public static final int MODE_PICTURE = 1;
    public static final int MODE_FULL = 2;

    private int captureMode;
    private boolean audioRequired;
    private boolean cameraPermissionRequestStarted;
    private boolean capturePressActive;
    private boolean videoGestureDecisionPending;
    private boolean fullVideoGestureClassified;
    private boolean photoCapturePending;
    private boolean photoFileAccessRecoveryPending;
    private boolean photoCaptureRetryAttempted;

    private long captureGestureGeneration;
    private long capturePressStartTime;
    private Runnable pendingVideoGestureDecision;

    private enum PermissionState {
        UNKNOWN,
        REQUEST_PENDING,
        AUTHORIZED,
        DENIED
    }

    private PermissionState cameraPermissionState = PermissionState.UNKNOWN;
    private PermissionState microphonePermissionState = PermissionState.UNKNOWN;

    boolean flashEnabled;

    boolean isFrontCamera;

    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidPermissionManager.attach(this);
        setContentView(R.layout.activity_video_capture);

        tvTimer = findViewById(R.id.tvTimer);
        previewView = findViewById(R.id.previewView);
        btnRecord = findViewById(R.id.btnCapture);
        btnBackCapture = findViewById(R.id.btnBackCapture);
        ImageButton btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(v -> {
            if (isFinishing() || isDestroyed() || currentRecordingSession != null
                    || isPhotoCaptureBusy() || capturePressActive) return;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException | SecurityException failure) {
                AndroidUtils.handleException(failure, false);
                Toast.makeText(this, "No compatible media picker is available.", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException failure) {
                AndroidUtils.handleException(failure, false);
                return;
            }
        });

        btnFlash = findViewById(R.id.btnFlash);
        btnFlash.setOnClickListener(v -> {
            if (camera == null) return;

            boolean enabled = flashEnabled;
            camera.getCameraControl().enableTorch(!enabled);
            flashEnabled = !enabled;

            btnFlash.setImageResource(
                    flashEnabled ? R.drawable.ic_flash_on : R.drawable.ic_flash_off
            );
        });

        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnSwitchCamera.setOnClickListener(v -> {
            if (isPhotoCaptureBusy() || currentRecordingSession != null
                    || capturePressActive || isFinishing() || isDestroyed()) return;
            isFrontCamera = !isFrontCamera;
            cameraSelector =
                    isFrontCamera
                            ? CameraSelector.DEFAULT_FRONT_CAMERA
                            : CameraSelector.DEFAULT_BACK_CAMERA;

            bindCamera(deviceOrientation);
        });

        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

        Bundle b = getIntent().getExtras();
        captureMode = b.getInt("mode");
        audioRequired = captureMode != MODE_PICTURE && b.getInt("capture_mode", 0) != 1;
        maxSeconds = Math.max(1, b.getInt(EXTRA_MAX_SECONDS, 30));
        targetFps = Math.max(5, b.getInt(EXTRA_TARGET_FPS, 30));
        width = b.getInt("width");
        height = b.getInt("height");
        bitrate = b.getInt("bitrate", 200_000);
        stillQuality = b.getInt(EXTRA_QUALITY, 2);
        qualidade = (stillQuality == 3 ? VideoQuality.FHD : stillQuality == 1 ? VideoQuality.SD : VideoQuality.HD);

        lastSavedVideoPath = b.getString("file");
//        lastSavedVideoPath = "/sdcard/DCIM/" + lastSavedVideoPath.substring(lastSavedVideoPath.lastIndexOf('/'));
        if (lastSavedVideoPath == null)
            lastSavedVideoPath = buildOutputFilePath();

        progressDrawable = new RecordProgressDrawable();

        btnRecord.setOnTouchListener((v, ev) -> {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    beginCapturePress(ev.getEventTime());
                    return true;
                case MotionEvent.ACTION_UP:
                    finishCapturePress(false, ev.getEventTime());
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    finishCapturePress(true, ev.getEventTime());
                    return true;
            }
            return false;
        });

        btnBackCapture.setOnClickListener(v -> {
            stopRecording(RecordingStopReason.CANCELLED);
            stopProgress();
            setResult(RESULT_CANCELED);
            finish();
        });

        TextView tvHint = findViewById(R.id.tvHint);
        tvHint.setVisibility(captureMode == MODE_FULL ? VISIBLE : INVISIBLE);

        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) return;

                int newOrientation;
                if (orientation >= 315 || orientation < 45) {
                    newOrientation = Surface.ROTATION_0;      // Portrait
                } else if (orientation >= 45 && orientation < 135) {
                    newOrientation = Surface.ROTATION_270;     // Landscape LEFT
                } else if (orientation >= 135 && orientation < 225) {
                    newOrientation = Surface.ROTATION_180;    // Reverse Portrait
                } else {
                    newOrientation = Surface.ROTATION_90;    // Landscape RIGHT
                }

                if (newOrientation != deviceOrientation) {
                    deviceOrientation = newOrientation;
//                    updateCameraConfig should only be called on actual rotation, not when the orientation was just changed
//                    updateCameraConfig(deviceOrientation);
//                    Log.d("ORIENT", "Orientation changed: " + deviceOrientation);
                }
            }
        };
        orientationEventListener.enable();

    }

    // ================================
    // PERMISSIONS
    // ================================
    private boolean hasCameraPermission() {
        return AndroidPermissionManager.isAuthorized(Manifest.permission.CAMERA);
    }

    private void requestCameraPermission() {
        if (cameraPermissionState == PermissionState.REQUEST_PENDING || isFinishing() || isDestroyed()) return;
        cameraPermissionState = PermissionState.REQUEST_PENDING;
        AndroidPermissionManager.requestRuntime(result -> {
            if (isFinishing() || isDestroyed()) return;
            if (result.granted && hasCameraPermission()) {
                cameraPermissionState = PermissionState.AUTHORIZED;
                startCamera();
            } else {
                cameraPermissionState = PermissionState.DENIED;
                Toast.makeText(this, "Camera permission is required to use the camera.", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }, Manifest.permission.CAMERA);
    }

    @Override
    protected void onResume() {
        AndroidPermissionManager.attach(this);
        super.onResume();
        if (!cameraPermissionRequestStarted) {
            cameraPermissionRequestStarted = true;
            requestCameraPermission();
        } else if (cameraPermissionState == PermissionState.AUTHORIZED
                && captureMode != MODE_PICTURE && audioRequired) {
            refreshMicrophoneAuthorization();
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        AndroidPermissionManager.onUserInteraction();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndroidPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                if (isFinishing() || isDestroyed()) return;
                bindCamera();
                restoreCaptureControls();
            } catch (ExecutionException | InterruptedException ignored) {
                setResult(RESULT_CANCELED);
                finish();
            } catch (RuntimeException failure) {
                AndroidUtils.handleException(failure, false);
                setResult(RESULT_CANCELED);
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private int lastRotation = -1;

    private void bindCamera() {
        bindCamera(deviceOrientation);
    }

    private void bindCamera(int rotation) {
        lastRotation = rotation;

        // Remove all before recreating
        cameraProvider.unbindAll();

        /*
            Preview, VideoCapture and ImageCapture must all be initialized using a compatible
            aspect ratio, otherwise the binding may fail. Here, everything is initialized with
            a 16:9 aspect ratio using the ResolutionSelector (Preview and ImageCapture) and
            the QualitySelector with Quality.HD (VideoCapture).
         */
        ResolutionSelector resolutionSelector =
                new ResolutionSelector.Builder()
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                        .build();

        // PREVIEW
        Preview preview = new Preview.Builder()
                .setTargetRotation(rotation)
                .setResolutionSelector(resolutionSelector)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // VIDEO CAPTURE (CameraX VideoCapture)
        Recorder recorder =
                new Recorder.Builder()
                        .setExecutor(ContextCompat.getMainExecutor(this))
                        .setQualitySelector(QualitySelector.from(Quality.HD,
                                FallbackStrategy.higherQualityOrLowerThan(Quality.HD)))
                        .setTargetVideoEncodingBitRate(bitrate)
                        .build();

        videoCapture = VideoCapture.withOutput(recorder);

        final boolean isLandscape =
                getResources().getConfiguration().screenWidthDp >
                        getResources().getConfiguration().screenHeightDp;

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(rotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setJpegQuality(stillQuality == 1 ? 75 : stillQuality == 2 ? 85 : 100)
                /*
                    We'll keep using setTargetResolution for the time being, because the preferred
                    method using ResolutionSelector, as of now, only allows selecting 4:3 or 16:9
                    resolutions.
                 */
                .setTargetResolution(
                        /*
                            setTargetResolution picks resolution based on the current orientation
                            of the application, so in landscape we have to invert the dimensions to
                            pick the correct resolution.
                         */
                        isLandscape ?
                                new Size(width, height) :
                                new Size(height, width))
                .build();

        // Final bind
        camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                videoCapture,
                imageCapture
        );

        enablePinchToZoom(this, camera, previewView);
    }

    private boolean hasMicrophonePermission() {
        return audioRequired && AndroidPermissionManager.isAuthorized(Manifest.permission.RECORD_AUDIO);
    }

    private void refreshMicrophoneAuthorization() {
        if (captureMode == MODE_PICTURE || !audioRequired
                || cameraPermissionState != PermissionState.AUTHORIZED
                || microphonePermissionState == PermissionState.REQUEST_PENDING) return;
        microphonePermissionState = hasMicrophonePermission()
                ? PermissionState.AUTHORIZED : PermissionState.DENIED;
    }

    private void beginCapturePress(long eventTime) {
        if (isPhotoCaptureBusy() || currentRecordingSession != null
                || capturePressActive || isFinishing() || isDestroyed()) return;
        if (!hasCameraPermission()) {
            requestCameraPermission();
            return;
        }
        capturePressActive = true;
        capturePressStartTime = eventTime > 0 ? eventTime : SystemClock.uptimeMillis();
        fullVideoGestureClassified = false;
        long gesture = ++captureGestureGeneration;
        btnSwitchCamera.setEnabled(false);
        if (captureMode == MODE_PICTURE) return;
        if (captureMode == MODE_FULL) {
            videoGestureDecisionPending = true;
            pendingVideoGestureDecision = () -> {
                pendingVideoGestureDecision = null;
                if (!capturePressActive || captureGestureGeneration != gesture || isFinishing() || isDestroyed()) return;
                videoGestureDecisionPending = false;
                fullVideoGestureClassified = true;
                startVideoForGesture(gesture);
            };
            progressHandler.postDelayed(pendingVideoGestureDecision, PHOTO_THRESHOLD_MS);
            return;
        }
        startVideoForGesture(gesture);
    }

    private void finishCapturePress(boolean cancelled, long eventTime) {
        if (!capturePressActive) return;
        long completedAt = eventTime > 0 ? eventTime : SystemClock.uptimeMillis();
        boolean shortFullGesture = captureMode == MODE_FULL && videoGestureDecisionPending
                && completedAt - capturePressStartTime < PHOTO_THRESHOLD_MS;
        boolean takePhoto = !cancelled && (captureMode == MODE_PICTURE || shortFullGesture);
        boolean completedVideoGesture = !cancelled && (captureMode == MODE_VIDEO
                || (captureMode == MODE_FULL && fullVideoGestureClassified));
        cancelPendingVideoGestureDecision();
        capturePressActive = false;
        ++captureGestureGeneration;
        if (takePhoto) {
            takePhoto();
        } else {
            RecordingSession session = currentRecordingSession;
            RecordingStopReason stopReason = !cancelled && completedVideoGesture
                    && session != null && session.phase == RecordingPhase.RECORDING
                    ? RecordingStopReason.COMPLETED : RecordingStopReason.CANCELLED;
            stopRecording(stopReason);
            stopProgress();
        }
        flashEnabled = false;
        btnFlash.setImageResource(R.drawable.ic_flash_off);
        if (isPhotoCaptureBusy()) {
            disableCaptureControlsForFinalization();
        } else if (currentRecordingSession == null) {
            restoreCaptureControls();
        } else {
            disableCaptureControlsForFinalization();
        }
    }

    private void startVideoForGesture(long gesture) {
        if (!capturePressActive || captureGestureGeneration != gesture
                || isPhotoCaptureBusy()
                || videoCapture == null || currentRecordingSession != null
                || isFinishing() || isDestroyed()) return;
        if (!audioRequired) {
            startRecordingNow(false, gesture);
            return;
        }
        if (hasMicrophonePermission()) {
            microphonePermissionState = PermissionState.AUTHORIZED;
            startRecordingNow(true, gesture);
            return;
        }
        if (microphonePermissionState == PermissionState.REQUEST_PENDING) {
            return;
        }
        requestMicrophonePermission(true, gesture);
    }

    private void requestMicrophonePermission(boolean startRecordingWhenGranted, long gesture) {
        if (!audioRequired || cameraPermissionState != PermissionState.AUTHORIZED
                || microphonePermissionState == PermissionState.REQUEST_PENDING
                || isFinishing() || isDestroyed()) return;
        microphonePermissionState = PermissionState.REQUEST_PENDING;
        AndroidPermissionManager.requestRuntime(result -> {
            if (isFinishing() || isDestroyed()) return;
            boolean mayStartRecording = startRecordingWhenGranted && capturePressActive
                    && captureGestureGeneration == gesture;
            boolean fullGestureCanStart = captureMode == MODE_FULL && fullVideoGestureClassified
                    && capturePressActive;
            if (hasMicrophonePermission()) {
                microphonePermissionState = PermissionState.AUTHORIZED;
                if ((mayStartRecording || fullGestureCanStart) && currentRecordingSession == null) {
                    long activeGesture = captureGestureGeneration;
                    startRecordingNow(true, activeGesture);
                }
            } else {
                microphonePermissionState = PermissionState.DENIED;
                showMicrophoneDenied();
                if (captureMode == MODE_VIDEO) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }, Manifest.permission.RECORD_AUDIO);
    }

    private void cancelPendingVideoGestureDecision() {
        videoGestureDecisionPending = false;
        if (pendingVideoGestureDecision != null) {
            progressHandler.removeCallbacks(pendingVideoGestureDecision);
            pendingVideoGestureDecision = null;
        }
    }

    private void showMicrophoneDenied() {
        Toast.makeText(this,
                "Microphone access is needed for video; photos are still available.",
                Toast.LENGTH_LONG).show();
    }

    private void startRecordingNow(boolean withAudio, long gesture) {
        if (videoCapture == null || !capturePressActive || captureGestureGeneration != gesture
                || isPhotoCaptureBusy()
                || isFinishing() || isDestroyed() || currentRecordingSession != null
                || (withAudio && (microphonePermissionState != PermissionState.AUTHORIZED
                || microphonePermissionState == PermissionState.REQUEST_PENDING
                || !hasMicrophonePermission()))) return;
        RecordingSession session = null;
        try {
            File destinationFile = new File(lastSavedVideoPath).getAbsoluteFile();
            File parent = destinationFile.getParentFile();
            ensureOutputDirectory(parent);
            File stagingFile = File.createTempFile(".totalcross-video-", ".mp4", parent);
            session = new RecordingSession(++nextRecordingSessionId, gesture,
                    destinationFile, stagingFile);
            currentRecordingSession = session;

            videoCapture.setTargetRotation(deviceOrientation);
            androidx.camera.video.PendingRecording pendingRecording = videoCapture.getOutput()
                    .prepareRecording(this, new FileOutputOptions.Builder(stagingFile).build());
            if (withAudio) pendingRecording = pendingRecording.withAudioEnabled();

            RecordingSession callbackSession = session;
            session.recording = pendingRecording.start(ContextCompat.getMainExecutor(this),
                    event -> handleRecordingEvent(callbackSession, event));
            if (session.phase == RecordingPhase.FINALIZING) {
                stopSessionRecording(session);
            }
        } catch (IOException | RuntimeException failure) {
            if (session != null) discardFailedRecordingStart(session);
            failRecordingStart(failure);
        }
    }

    private static void ensureOutputDirectory(File directory) throws IOException {
        if (directory == null) {
            throw new IOException("Unable to create the video output directory.");
        }
        android.system.StructStat stat;
        try {
            stat = Os.stat(directory.getAbsolutePath());
        } catch (ErrnoException failure) {
            if (failure.errno != OsConstants.ENOENT) {
                throw new IOException("Unable to access the video output directory.", failure);
            }
            if (directory.mkdirs()) return;
            try {
                stat = Os.stat(directory.getAbsolutePath());
            } catch (ErrnoException retryFailure) {
                throw new IOException("Unable to create the video output directory.", retryFailure);
            }
        }
        if ((stat.st_mode & OsConstants.S_IFMT) != OsConstants.S_IFDIR) {
            throw new IOException("The video output path is not a directory.");
        }
    }

    private void failRecordingStart(Throwable failure) {
        if (AndroidFileAccessErrors.isPermissionDenied(failure)) {
            requestFileAccessAfterCameraFailure();
        }
        AndroidUtils.handleException(failure, false);
        stopProgress();
        restoreCaptureControls();
        setResult(RESULT_CANCELED);
        if (captureMode == MODE_VIDEO) finish();
    }

    private void discardFailedRecordingStart(RecordingSession session) {
        session.finalizeProcessed = true;
        deleteStagingFile(session);
        if (currentRecordingSession == session) currentRecordingSession = null;
    }

    private void handleRecordingEvent(RecordingSession session, VideoRecordEvent event) {
        if (event instanceof VideoRecordEvent.Start) {
            if (currentRecordingSession != session || session.finalizeProcessed) return;
            if (session.phase == RecordingPhase.FINALIZING) {
                stopSessionRecording(session);
                return;
            }
            if (session.phase != RecordingPhase.STARTING) return;
            if (!capturePressActive || captureGestureGeneration != session.gestureGeneration
                    || isFinishing() || isDestroyed()) {
                stopRecording(RecordingStopReason.CANCELLED);
                return;
            }
            session.phase = RecordingPhase.RECORDING;
            btnRecord.setImageDrawable(progressDrawable);
            startProgress(session);
            return;
        }

        if (event instanceof VideoRecordEvent.Status) {
            if (currentRecordingSession != session || session.phase != RecordingPhase.RECORDING
                    || session.finalizeProcessed || isFinishing() || isDestroyed()) return;
            VideoRecordEvent.Status status = (VideoRecordEvent.Status) event;
            long seconds = TimeUnit.NANOSECONDS.toSeconds(
                    status.getRecordingStats().getRecordedDurationNanos());
            updateTimer(seconds);
            tvTimer.setVisibility(VISIBLE);
            tvTimer.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_rec_dot, 0, 0, 0);
            return;
        }

        if (event instanceof VideoRecordEvent.Finalize) {
            processRecordingFinalize(session, (VideoRecordEvent.Finalize) event);
        }
    }

    private void processRecordingFinalize(RecordingSession session, VideoRecordEvent.Finalize finalize) {
        if (session.finalizeProcessed) return;
        session.finalizeProcessed = true;
        cancelStopFailureWatchdog(session);
        if (currentRecordingSession != session) {
            deleteStagingFile(session);
            return;
        }

        session.phase = RecordingPhase.FINALIZING;
        boolean activityUsable = !isFinishing() && !isDestroyed()
                && !session.stopFailureTimeoutReported;
        if (activityUsable) {
            stopProgress();
            tvTimer.setVisibility(View.GONE);
        }

        Throwable failure = null;
        boolean published = false;
        if (session.stopReason == RecordingStopReason.CANCELLED) {
            // User and exceptional-stop cancellations are expected terminal outcomes.
        } else if (finalize.hasError()) {
            failure = new IOException("Camera recording failed: " + finalize.getError(), finalize.getCause());
        } else if (session.stopReason == RecordingStopReason.COMPLETED) {
            try {
                if (!session.stagingFile.isFile() || session.stagingFile.length() == 0) {
                    failure = new IOException("Camera produced an empty video file.");
                } else {
                    Os.rename(session.stagingFile.getAbsolutePath(), session.destinationFile.getAbsolutePath());
                    published = true;
                }
            } catch (ErrnoException | RuntimeException publicationFailure) {
                failure = publicationFailure;
            }
        } else if (session.stopReason == RecordingStopReason.NONE) {
            failure = new IOException("Camera recording finalized without a completion request.");
        }

        if (!published) deleteStagingFile(session);
        currentRecordingSession = null;

        if (!activityUsable) return;
        restoreCaptureControls();

        if (failure != null) {
            if (AndroidFileAccessErrors.isPermissionDenied(failure)) {
                requestFileAccessAfterCameraFailure();
            }
            AndroidUtils.handleException(failure, false);
            Toast.makeText(this, "Record failed: " + failure.getMessage(), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            if (captureMode == MODE_VIDEO) finish();
            return;
        }

        if (published && session.stopReason == RecordingStopReason.COMPLETED) {
            openVideoPlayer(lastSavedVideoPath);
        }
    }

    private void deleteStagingFile(RecordingSession session) {
        try {
            if (session.stagingFile.exists() && !session.stagingFile.delete()
                    && session.stagingFile.exists()) {
                AndroidUtils.handleException(new IOException("Unable to remove the temporary video file."), false);
            }
        } catch (RuntimeException cleanupFailure) {
            AndroidUtils.handleException(cleanupFailure, false);
        }
    }

    private void disableCaptureControlsForFinalization() {
        if (isFinishing() || isDestroyed()) return;
        btnRecord.setEnabled(false);
        btnSwitchCamera.setEnabled(false);
    }

    private void restoreCaptureControls() {
        if (isPhotoCaptureBusy() || currentRecordingSession != null
                || capturePressActive || isFinishing() || isDestroyed()) return;
        btnRecord.setEnabled(true);
        btnRecord.setImageResource(R.drawable.bg_record_button_small);
        btnSwitchCamera.setEnabled(true);
    }

    private void requestFileAccessAfterCameraFailure() {
        AndroidPermissionManager.requestFileAccess(result -> {
            String message = result.authorizationChanged
                    ? "File access changed. Start the recording again."
                    : "Unable to access the selected output file.";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void stopRecording(RecordingStopReason reason) {
        RecordingSession session = currentRecordingSession;
        if (session == null || session.finalizeProcessed) return;
        if (reason == RecordingStopReason.CANCELLED) {
            session.stopReason = RecordingStopReason.CANCELLED;
        } else if (session.stopReason == RecordingStopReason.NONE) {
            session.stopReason = session.phase == RecordingPhase.STARTING
                    ? RecordingStopReason.CANCELLED : RecordingStopReason.COMPLETED;
        }
        session.phase = RecordingPhase.FINALIZING;
        stopProgress();
        disableCaptureControlsForFinalization();
        stopSessionRecording(session);
    }

    private void stopSessionRecording(RecordingSession session) {
        if (session.finalizeProcessed || session.stopCalled || session.recording == null) return;
        session.stopCalled = true;
        try {
            session.recording.stop();
        } catch (RuntimeException stopFailure) {
            session.stopReason = RecordingStopReason.CANCELLED;
            session.phase = RecordingPhase.FINALIZING;
            session.stopFailed = true;
            AndroidUtils.handleException(stopFailure, false);
            scheduleStopFailureWatchdog(session);
            if (!session.closeCalled) {
                session.closeCalled = true;
                try {
                    session.recording.close();
                } catch (Exception closeFailure) {
                    AndroidUtils.handleException(closeFailure, false);
                }
            }
        }
    }

    private void scheduleStopFailureWatchdog(RecordingSession session) {
        if (session.stopFailureWatchdogScheduled) return;
        session.stopFailureWatchdogScheduled = true;
        long sessionId = session.id;
        Runnable watchdog = new Runnable() {
            @Override
            public void run() {
                if (session.stopFailureWatchdog == this) session.stopFailureWatchdog = null;
                if (currentRecordingSession != session || session.id != sessionId
                        || session.phase != RecordingPhase.FINALIZING || !session.stopFailed
                        || session.finalizeProcessed || session.stopFailureTimeoutReported) return;
                session.stopFailureTimeoutReported = true;
                AndroidUtils.handleException(new IllegalStateException(
                        "Camera recording shutdown failed: no Finalize event arrived after stop() and close()."), false);
                // Keep the session and staging file owned; CameraX may still be writing to it.
                if (isFinishing() || isDestroyed()) return;
                setResult(RESULT_CANCELED);
                finish();
            }
        };
        session.stopFailureWatchdog = watchdog;
        progressHandler.postDelayed(watchdog, STOP_FAILURE_WATCHDOG_MS);
    }

    private void cancelStopFailureWatchdog(RecordingSession session) {
        if (session.stopFailureWatchdog == null) return;
        progressHandler.removeCallbacks(session.stopFailureWatchdog);
        session.stopFailureWatchdog = null;
    }

    private void startProgress(RecordingSession session) {
        if (currentRecordingSession != session || session.phase != RecordingPhase.RECORDING
                || isFinishing() || isDestroyed()) return;
        progressActive = true;
        progressSessionId = session.id;
        recordingStartTime = System.currentTimeMillis();
        progressDrawable.setProgress(0f);
        progressDrawable.setProgressColor(Color.RED);
        progressHandler.post(progressRunnable);
    }

    private void stopProgress() {
        progressActive = false;
        progressDrawable.setProgress(0f);
        if (!isFinishing() && !isDestroyed()) btnRecord.setImageResource(R.drawable.bg_record_button_small);
        progressHandler.removeCallbacks(progressRunnable);
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!progressActive) {
                return;
            }
            RecordingSession session = currentRecordingSession;
            if (session == null || session.id != progressSessionId
                    || session.phase != RecordingPhase.RECORDING || isFinishing() || isDestroyed()) {
                progressActive = false;
                progressHandler.removeCallbacks(this);
                return;
            }
            long elapsed = System.currentTimeMillis() - recordingStartTime;
            float p = Math.min(1f, (float) elapsed / (maxSeconds * 1000));
            progressDrawable.setProgress(p);
            btnRecord.invalidate();

            if (p >= 1f) {
                if (currentRecordingSession != session || session.id != progressSessionId
                        || session.phase != RecordingPhase.RECORDING) return;
                cancelPendingVideoGestureDecision();
                capturePressActive = false;
                ++captureGestureGeneration;
                session.stopReason = RecordingStopReason.COMPLETED;
                stopRecording(RecordingStopReason.COMPLETED);
                stopProgress();
            } else {
                progressHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        }
    };

    private void updateTimer(long seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;

        String text = String.format(Locale.US, "%02d:%02d", min, sec);
        tvTimer.setText(text);
    }

    /** A photo remains busy until the permission-recovery callback has completed. */
    private boolean isPhotoCaptureBusy() {
        return photoCapturePending || photoFileAccessRecoveryPending;
    }

    private void takePhoto() {
        takePhoto(false);
    }

    private void takePhoto(boolean permissionRetry) {
        if (isFinishing() || isDestroyed()) return;
        if (permissionRetry) {
            if (!photoCapturePending || !photoCaptureRetryAttempted || photoFileAccessRecoveryPending) return;
        } else {
            if (imageCapture == null || currentRecordingSession != null || photoCapturePending
                    || photoFileAccessRecoveryPending) return;
            photoCapturePending = true;
            photoCaptureRetryAttempted = false;
            disableCaptureControlsForPhoto();
        }
        if (imageCapture == null || currentRecordingSession != null) {
            failPhotoCapture(new IllegalStateException("Camera is not available for photo capture."),
                    new File(lastSavedVideoPath));
            return;
        }

        File photoFile = new File(lastSavedVideoPath);
        try {
            ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
            imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                    if (isFinishing() || isDestroyed()) return;
                    clearPhotoCapturePending();
                    if (!photoFile.isFile() || photoFile.length() == 0) {
                        failPhotoCapture(new IOException("Camera produced an empty image file."), photoFile);
                        return;
                    }
                    try {
                        openImagePreview(photoFile.getAbsolutePath());
                    } catch (RuntimeException failure) {
                        failPhotoCapture(failure, photoFile);
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    if (isFinishing() || isDestroyed()) return;
                    if (!permissionRetry && AndroidFileAccessErrors.isPermissionDenied(exception)) {
                        requestPhotoFileAccessRecovery(exception, photoFile);
                    } else {
                        failPhotoCapture(exception, photoFile);
                    }
                }
            });
        } catch (RuntimeException failure) {
            if (!permissionRetry && AndroidFileAccessErrors.isPermissionDenied(failure)) {
                requestPhotoFileAccessRecovery(failure, photoFile);
            } else {
                failPhotoCapture(failure, photoFile);
            }
        }
    }

    private void requestPhotoFileAccessRecovery(Throwable failure, File photoFile) {
        if (!isPhotoCaptureBusy() || photoCaptureRetryAttempted
                || isFinishing() || isDestroyed()) return;
        photoFileAccessRecoveryPending = true;
        try {
            AndroidPermissionManager.requestFileAccess(result -> {
                if (isFinishing() || isDestroyed()) return;
                if (!photoCapturePending || !photoFileAccessRecoveryPending) return;
                photoFileAccessRecoveryPending = false;
                if (result.authorizationChanged && !photoCaptureRetryAttempted) {
                    photoCaptureRetryAttempted = true;
                    takePhoto(true);
                } else {
                    failPhotoCapture(failure, photoFile);
                }
            });
        } catch (RuntimeException recoveryFailure) {
            photoFileAccessRecoveryPending = false;
            failPhotoCapture(recoveryFailure, photoFile);
        }
    }

    private void disableCaptureControlsForPhoto() {
        if (isFinishing() || isDestroyed()) return;
        btnRecord.setEnabled(false);
        btnSwitchCamera.setEnabled(false);
    }

    private void clearPhotoCapturePending() {
        photoCapturePending = false;
        photoFileAccessRecoveryPending = false;
        photoCaptureRetryAttempted = false;
    }

    private void cancelPendingPhotoCapture() {
        if (isPhotoCaptureBusy()) clearPhotoCapturePending();
    }

    private void failPhotoCapture(Throwable failure, File photoFile) {
        clearPhotoCapturePending();
        if (isFinishing() || isDestroyed()) return;
        if (photoFile.exists() && photoFile.length() == 0) photoFile.delete();
        AndroidUtils.handleException(failure, false);
        restoreCaptureControls();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void openVideoPlayer(String path) {
        Intent videoIntent = new Intent(this, VideoPlayerActivity.class);
        videoIntent.putExtra("video_path", path);
        videoIntent.putExtra("playback_only", false);
        // start playback activity - restore capture upon return
        startActivityForResult(videoIntent, PLAYBACK_REQ);
    }

    private void openImagePreview(String path) {
        Intent i = new Intent(this, ImagePreviewActivity.class);
        i.putExtra("image_path", path);
        startActivityForResult(i, IMAGE_REQ);
    }

    private String buildOutputFilePath() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(dir, "VID_" + ts + ".mp4").getAbsolutePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (AndroidPermissionManager.onActivityResult(requestCode, resultCode)) return;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAYBACK_REQ || requestCode == IMAGE_REQ) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            } else {
                startCamera();
            }
        }
    }

    @Override
    public void finish() {
        cancelPendingVideoGestureDecision();
        cancelPendingPhotoCapture();
        capturePressActive = false;
        ++captureGestureGeneration;
        stopRecording(RecordingStopReason.CANCELLED);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        cancelPendingVideoGestureDecision();
        cancelPendingPhotoCapture();
        capturePressActive = false;
        ++captureGestureGeneration;
        stopRecording(RecordingStopReason.CANCELLED);
        stopProgress();
        super.onDestroy();
    }

    /* =========================
   🔍 PINCH TO ZOOM
   ========================= */
    private void enablePinchToZoom(Context context, Camera camera, PreviewView previewView) {
        ScaleGestureDetector detector =
                new ScaleGestureDetector(context,
                        new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            @Override
                            public boolean onScale(@NonNull ScaleGestureDetector d) {
                                float scale =
                                        Objects.requireNonNull(camera.getCameraInfo().getZoomState()
                                                .getValue()).getZoomRatio() * d.getScaleFactor();
                                camera.getCameraControl().setZoomRatio(scale);
                                return true;
                            }
                        });

        previewView.setOnTouchListener((v, e) -> {
            detector.onTouchEvent(e);
            return true;
        });
    }
}
