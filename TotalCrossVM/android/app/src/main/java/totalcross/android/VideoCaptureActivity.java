// Copyright (C) 2025-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
    private Recording activeRecording;

    // Progress UI
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private long recordingStartTime = 0;
    private final int PROGRESS_UPDATE_INTERVAL = 50; // ms
    private boolean progressActive = false;

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
    private boolean recordingGestureActive;
    private boolean microphoneRequestPending;
    private boolean cameraPermissionRequestStarted;

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
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/* video/*");
            startActivity(intent);
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
            if (!hasCameraPermission()) {
                requestCameraPermission();
                return true;
            }
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    btnSwitchCamera.setEnabled(false);

                    if (captureMode != MODE_PICTURE) {
                        recordingGestureActive = true;
                        btnRecord.setImageDrawable(progressDrawable);
                        videoCapture.setTargetRotation(deviceOrientation);

                        requestMicrophoneAndStartRecording();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    recordingGestureActive = false;
                    if (captureMode == MODE_PICTURE) {
                        takePhoto();
                    } else {
                        stopRecording();
                        stopProgress();
                        btnRecord.setImageResource(R.drawable.bg_record_button_small);
                    }

                    flashEnabled = false;
                    btnFlash.setImageResource(R.drawable.ic_flash_off);
                    btnSwitchCamera.setEnabled(true);
                    return true;
            }
            return false;
        });

        btnBackCapture.setOnClickListener(v -> {
            stopRecording();
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
        AndroidPermissionManager.requestRuntime(result -> {
            if (result.granted && hasCameraPermission() && !isFinishing() && !isDestroyed()) {
                startCamera();
            } else if (!isFinishing() && !isDestroyed()) {
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
        }
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
                bindCamera();
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

    private void requestMicrophoneAndStartRecording() {
        if (videoCapture == null || activeRecording != null || microphoneRequestPending) {
            return;
        }
        if (!audioRequired) {
            startRecordingNow(false);
            return;
        }
        microphoneRequestPending = true;
        AndroidPermissionManager.requestRuntime(result -> {
            microphoneRequestPending = false;
            if (result.granted && recordingGestureActive && !isFinishing()) {
                startRecordingNow(true);
            } else if (!result.granted) {
                Toast.makeText(this, "Microphone permission is required for audio recording.", Toast.LENGTH_LONG).show();
                stopProgress();
                btnRecord.setImageResource(R.drawable.bg_record_button_small);
                btnSwitchCamera.setEnabled(true);
                if (captureMode == MODE_VIDEO) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }, Manifest.permission.RECORD_AUDIO);
    }

    private void startRecordingNow(boolean withAudio) {
        if (videoCapture == null) {
            return;
        }

        File file = new File(lastSavedVideoPath);
        FileOutputOptions outputOptions =
                new FileOutputOptions.Builder(file).build();

        try {
            androidx.camera.video.PendingRecording pendingRecording =
                    videoCapture.getOutput().prepareRecording(this, outputOptions);
            if (withAudio) {
                pendingRecording = pendingRecording.withAudioEnabled();
            }
            activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(this),
                                event -> {
                                    if (event instanceof VideoRecordEvent.Status) {
                                        VideoRecordEvent.Status s = (VideoRecordEvent.Status) event;

                                        long seconds =
                                                TimeUnit.NANOSECONDS.toSeconds(
                                                        s.getRecordingStats().getRecordedDurationNanos()
                                                );

                                        updateTimer(seconds);
                                        tvTimer.setVisibility(VISIBLE);
                                        tvTimer.setCompoundDrawablesWithIntrinsicBounds(
                                                R.drawable.ic_rec_dot, 0, 0, 0
                                        );
                                    }
                                    if (event instanceof VideoRecordEvent.Finalize) {
                                        if (isFinishing()) return;
                                        tvTimer.setVisibility(View.GONE);
                                        VideoRecordEvent.Finalize finalize =
                                                (VideoRecordEvent.Finalize) event;

                                        if (!finalize.hasError()) {
                                            if (file.isFile() && file.length() > 0 && !isFinishing()) {
                                                openVideoPlayer(lastSavedVideoPath);
                                            } else {
                                                file.delete();
                                                if (!isFinishing()) {
                                                    setResult(RESULT_CANCELED);
                                                    finish();
                                                }
                                            }
                                        } else {
                                            if (isFileAccessDenied(finalize.getCause())) {
                                                requestFileAccessAfterCameraFailure();
                                            }
                                            if (TimeUnit.NANOSECONDS.toMillis(
                                                    finalize.getRecordingStats().getRecordedDurationNanos()
                                            ) < PHOTO_THRESHOLD_MS) {
                                                // it's fine
                                                if (captureMode == MODE_FULL) {
                                                    file.delete();
                                                    takePhoto();
                                                }
                                            } else {
                                                file.delete();
                                                Toast.makeText(
                                                        this,
                                                        "Record failed: " + finalize.getError(),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                if (captureMode == MODE_VIDEO) {
                                                    setResult(RESULT_CANCELED);
                                                    finish();
                                                }
                                            }
                                        }
                                    }
                                });
            startProgress();
        } catch (SecurityException deniedOutput) {
            if (isFileAccessDenied(deniedOutput)) requestFileAccessAfterCameraFailure();
            failRecordingStart(deniedOutput);
        } catch (RuntimeException failure) {
            failRecordingStart(failure);
        }
    }


    private void failRecordingStart(Throwable failure) {
        AndroidUtils.handleException(failure, false);
        stopProgress();
        btnRecord.setImageResource(R.drawable.bg_record_button_small);
        btnSwitchCamera.setEnabled(true);
        setResult(RESULT_CANCELED);
        if (captureMode == MODE_VIDEO) finish();
    }

    private void requestFileAccessAfterCameraFailure() {
        AndroidPermissionManager.requestFileAccess(result -> {
            String message = result.authorizationChanged
                    ? "File access changed. Start the recording again."
                    : "Unable to access the selected output file.";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private boolean isFileAccessDenied(Throwable failure) {
        while (failure != null) {
            String message = failure.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                if (normalized.contains("eacces") || normalized.contains("eperm")
                        || (normalized.contains("permission denied")
                        && !normalized.contains("camera") && !normalized.contains("microphone"))) return true;
            }
            failure = failure.getCause();
        }
        return false;
    }

    private void stopRecording() {
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
        }
    }

    private void startProgress() {
        progressActive = true;
        recordingStartTime = System.currentTimeMillis();
        progressDrawable.setProgress(0f);
        progressDrawable.setProgressColor(Color.RED);
        progressHandler.post(progressRunnable);
    }

    private void stopProgress() {
        progressActive = false;
        progressDrawable.setProgress(0f);
        btnRecord.setImageResource(R.drawable.bg_record_button_small);
        progressHandler.removeCallbacks(progressRunnable);
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!progressActive) {
                return;
            }
            long elapsed = System.currentTimeMillis() - recordingStartTime;
            float p = Math.min(1f, (float) elapsed / (maxSeconds * 1000));
            progressDrawable.setProgress(p);
            btnRecord.invalidate();

            if (p >= 1f) {
                stopRecording();
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

    private void takePhoto() {
        takePhoto(false);
    }

    private void takePhoto(boolean permissionRetry) {
        if (imageCapture == null) return;
        File photoFile = new File(lastSavedVideoPath);
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
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
                if (!permissionRetry && isFileAccessDenied(exception.getCause())) {
                    AndroidPermissionManager.requestFileAccess(result -> {
                        if (result.authorizationChanged && !isFinishing()) {
                            takePhoto(true);
                        } else {
                            failPhotoCapture(exception, photoFile);
                        }
                    });
                } else {
                    failPhotoCapture(exception, photoFile);
                }
            }
        });
    }

    private void failPhotoCapture(Throwable failure, File photoFile) {
        if (photoFile.exists() && photoFile.length() == 0) photoFile.delete();
        AndroidUtils.handleException(failure, false);
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
    protected void onDestroy() {
        stopRecording();
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
