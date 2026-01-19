package totalcross.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCapture.OutputFileOptions;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class VideoCaptureActivity extends AppCompatActivity {

    public enum VideoQuality {
        FHD(1920, 1080), HD(1280,720), SD(720,480);

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

    private String lastSavedVideoPath = null;

    private static final int REQ_PERMS = 101;
    private static final int PLAYBACK_REQ = 201; // new request code for the player

    private PreviewView previewView;
    private ImageButton btnRecord;
    private ImageButton btnBackCapture;

    // CameraX
    private ProcessCameraProvider cameraProvider;
    private VideoCapture videoCapture;

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

    private int bitrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture);

        previewView = findViewById(R.id.previewView);
        btnRecord = findViewById(R.id.btnRecord);
        btnBackCapture = findViewById(R.id.btnBackCapture);

        Bundle b = getIntent().getExtras();
        maxSeconds = Math.max(1, b.getInt(EXTRA_MAX_SECONDS, 30));
        targetFps = Math.max(5, b.getInt(EXTRA_TARGET_FPS, 30));
        width = b.getInt("width");
        height = b.getInt("height");
        bitrate = b.getInt("bitrate", 200_000);
        int q = b.getInt(EXTRA_QUALITY, 2);
        qualidade = (q == 3 ? VideoQuality.FHD : q == 1 ? VideoQuality.SD : VideoQuality.HD);

        lastSavedVideoPath = b.getString("file");
//        lastSavedVideoPath = "/sdcard/DCIM/" + lastSavedVideoPath.substring(lastSavedVideoPath.lastIndexOf('/'));
        if (lastSavedVideoPath == null)
            lastSavedVideoPath = buildOutputFilePath();

        progressDrawable = new RecordProgressDrawable();

        btnRecord.setOnTouchListener((v, ev) -> {
            if (!permissionsGranted()) {
                requestPermissions();
                return true;
            }
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    btnRecord.setImageDrawable(progressDrawable);
                    videoCapture.setTargetRotation(deviceOrientation);

                    startRecording();
                    startProgress();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopRecording();
                    stopProgress();
                    return true;
            }
            return false;
        });

        btnBackCapture.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

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


        if (permissionsGranted()) startCamera();
        else requestPermissions();
    }

    // ================================
    // PERMISSIONS
    // ================================
    private boolean permissionsGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQ_PERMS);
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] p, @NonNull int[] r) {
        super.onRequestPermissionsResult(code, p, r);
        if (code == REQ_PERMS && permissionsGranted()) {
            startCamera();
        } else {
            Toast.makeText(this, "Permissões necessárias", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCamera();
            } catch (ExecutionException | InterruptedException ignored) {}
        }, ContextCompat.getMainExecutor(this));
    }

    private int lastRotation = -1;

    private void bindCamera() {
        this.bindCamera(Surface.ROTATION_0);
    }

    private void bindCamera(int rotation) {
        if (cameraProvider == null) {
            return;
        }

        // Nothing changed
        if (rotation == lastRotation) {
            return;
        }
        lastRotation = rotation;

        boolean isLandscape =
                rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;

        Size targetSize = VideoQuality.getQualitySelectorFor(width, height, isLandscape, qualidade);

        // Remove all before recreating
        cameraProvider.unbindAll();

        // PREVIEW
        Preview preview = new Preview.Builder()
                .setTargetRotation(rotation)
                .setTargetResolution(targetSize)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // VIDEO CAPTURE (CameraX VideoCapture)
        videoCapture = new VideoCapture.Builder()
                .setTargetRotation(rotation)
                .setTargetResolution(targetSize)
                .setVideoFrameRate(targetFps)
                .setBitRate(bitrate)
                .build();

        // Final bind
        cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                videoCapture
        );
    }

    private void startRecording() {
        if (videoCapture == null) {
            return;
        }

        File file = new File(lastSavedVideoPath);
        OutputFileOptions opts = new OutputFileOptions.Builder(file).build();

        videoCapture.startRecording(
                opts,
                ContextCompat.getMainExecutor(this),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        openVideoPlayer(lastSavedVideoPath);
                    }

                    @Override
                    public void onError(int videoCaptureError,
                                        @NonNull String message,
                                        Throwable cause) {
                        new File(lastSavedVideoPath).delete();
                        Toast.makeText(VideoCaptureActivity.this,
                                "Record failed: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void stopRecording() {
        if (videoCapture != null) {
            videoCapture.stopRecording();
        }
    }

    private void startProgress() {
        progressActive = true;
        recordingStartTime = System.currentTimeMillis();
        progressDrawable.setProgress(0f);
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

    private void openVideoPlayer(String path) {
        Intent videoIntent = new Intent(this, VideoPlayerActivity.class);
        videoIntent.putExtra("video_path", path);
        videoIntent.putExtra("playback_only", false);
        // start playback activity - restore capture upon return
        startActivityForResult(videoIntent, PLAYBACK_REQ);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAYBACK_REQ) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            } else {
                startCamera();
            }
        }
    }
}
