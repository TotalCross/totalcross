// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.content.Context;
import android.Manifest;
import android.os.Bundle;
import android.util.Size;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import totalcross.AndroidUtils;

public class CameraViewer extends AdjustedInsetsActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recorder;
    private Camera camera;

    boolean isMovie;
    String fileName;
    int stillQuality, width, height;
    int captureMode;
    boolean audioRequired;
    boolean recordingStartPending;
    Button captureButton;

    boolean allowRotation;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidPermissionManager.attach(this);
        setContentView(R.layout.main);
        setTitle("");

        Bundle b = getIntent().getExtras();
        fileName = b.getString("file");
        stillQuality = b.getInt("quality");
        width = b.getInt("width");
        height = b.getInt("height");
        allowRotation = b.getBoolean("allowRotation");
        captureMode = b.getInt("capture_mode", 0);
        audioRequired = captureMode != 1;

        isMovie = captureMode != 0;

        previewView = findViewById(R.id.previewView);

        final Button buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonExit.setText("Exit");
        buttonExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecording();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        final Button buttonClick = (Button) findViewById(R.id.buttonClick);
        captureButton = buttonClick;
        if (isMovie) {
            buttonClick.setText("Start");
            buttonClick.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (buttonClick.getText().toString().equals("Start")) {
                            buttonClick.setText("Stop");
                            File f = new File(fileName);
                            requestMicrophoneAndRecord(f);
                        } else {
                            stopRecording();
                            buttonClick.setText("Start");
                        }
//                  if (recorder == null) {
//                     buttonClick.setText("Stop");
//                     startRecording();
//                  } else {
//                     stopRecording();
//                     setResult(RESULT_OK);
//                     finish();
//                  }
                    } catch (Exception e) {
                        AndroidUtils.handleException(e, false);
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
        } else {
            buttonClick.setText("Click");
            buttonClick.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        capturePhoto(new File(fileName), false);
//                  if (camera == null) // guich@tc130: prevent NPE
//                     startPreview();
//                  if (camera != null) {
//                     buttonClick.setClickable(false);
//                     Camera.Parameters parameters = camera.getParameters();
//                     parameters.setRotation(exifRotation);
//                     try {
//                        camera.setParameters(parameters);
//                        camera.reconnect();
//                     } catch (Exception re) {
//                        AndroidUtils.handleException(re, false);
//                     }
//                     camera.takePicture(null, null, jpegCallback);
//                  } else {
//                     setResult(RESULT_CANCELED);
//                     finish();
//                  }
                    } catch (Exception e) {
                        AndroidUtils.handleException(e, false);
//                  stopPreview();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if (AndroidPermissionManager.isAuthorized(Manifest.permission.CAMERA)) {
                    startCameraX(cameraProvider);
                } else {
                    AndroidPermissionManager.requestRuntime(result -> {
                        if (result.granted && !isFinishing() && !isDestroyed()) {
                            startCameraX(cameraProvider);
                        } else if (!isFinishing() && !isDestroyed()) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }, Manifest.permission.CAMERA);
                }
            } catch (ExecutionException e) {
                AndroidUtils.handleException(e, false);
                setResult(RESULT_CANCELED);
                finish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                AndroidUtils.handleException(e, false);
                setResult(RESULT_CANCELED);
                finish();
            } catch (RuntimeException failure) {
                AndroidUtils.handleException(failure, false);
                setResult(RESULT_CANCELED);
                finish();
            }

        }, getExecutor());
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @Override
    protected void onResume() {
        AndroidPermissionManager.attach(this);
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndroidPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        if (AndroidPermissionManager.onActivityResult(requestCode, resultCode)) return;
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        final boolean isLandscape =
                getResources().getConfiguration().screenWidthDp >
                        getResources().getConfiguration().screenHeightDp;

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .setJpegQuality(stillQuality == 1 ? 75 : stillQuality == 2 ? 85 : 100)
                .setTargetRotation(allowRotation && previewView.getDisplay() != null
                        ? previewView.getDisplay().getRotation() : Surface.ROTATION_0)
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

        videoCapture = VideoCapture.withOutput(new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build());

        camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
        );

        enablePinchToZoom(this, camera, previewView);
    }

    private void requestMicrophoneAndRecord(File file) {
        if (recordingStartPending) return;
        if (!audioRequired) {
            recordVideo(file, false);
            return;
        }
        recordingStartPending = true;
        captureButton.setEnabled(false);
        AndroidPermissionManager.requestRuntime(result -> {
            recordingStartPending = false;
            captureButton.setEnabled(true);
            if (result.granted && !isFinishing()) {
                recordVideo(file, true);
            } else {
                Toast.makeText(this, "Microphone permission is required for audio recording.", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }, Manifest.permission.RECORD_AUDIO);
    }

    private void recordVideo(File file, boolean withAudio) {
        if (videoCapture == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        try {
            androidx.camera.video.PendingRecording pending = videoCapture.getOutput()
                    .prepareRecording(this, new FileOutputOptions.Builder(file).build());
            if (withAudio) pending = pending.withAudioEnabled();
            recorder = pending.start(getExecutor(), event -> {
                if (event instanceof VideoRecordEvent.Finalize) {
                    if (isFinishing()) return;
                    VideoRecordEvent.Finalize finalize = (VideoRecordEvent.Finalize) event;
                    if (!finalize.hasError() && file.isFile() && file.length() > 0) {
                        setResult(RESULT_OK);
                    } else {
                        if (isFileAccessDenied(finalize.getCause())) requestFileAccessAfterCameraFailure();
                        if (file.exists() && file.length() == 0) file.delete();
                        AndroidUtils.handleException(finalize.getCause() == null
                                ? new IOException("Camera recording failed: " + finalize.getError())
                                : finalize.getCause(), false);
                        setResult(RESULT_CANCELED);
                    }
                    finish();
                }
            });
        } catch (SecurityException deniedOutput) {
            if (isFileAccessDenied(deniedOutput)) requestFileAccessAfterCameraFailure();
            AndroidUtils.handleException(deniedOutput, false);
            setResult(RESULT_CANCELED);
            finish();
        } catch (RuntimeException failure) {
            AndroidUtils.handleException(failure, false);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void capturePhoto(File file, boolean permissionRetry) {
        if (imageCapture == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        ImageCapture.OutputFileOptions output = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(output, getExecutor(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                if (file.isFile() && file.length() > 0) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    failPhoto(file, new IOException("Camera produced an empty image file."));
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                if (!permissionRetry && isFileAccessDenied(exception.getCause())) {
                    AndroidPermissionManager.requestFileAccess(result -> {
                        if (result.authorizationChanged && !isFinishing()) capturePhoto(file, true);
                        else failPhoto(file, exception);
                    });
                } else {
                    failPhoto(file, exception);
                }
            }
        });
    }

    private void failPhoto(File file, Throwable failure) {
        if (file.exists() && file.length() == 0) file.delete();
        AndroidUtils.handleException(failure, false);
        setResult(RESULT_CANCELED);
        finish();
    }

    private boolean isFileAccessDenied(Throwable failure) {
        while (failure != null) {
            String message = failure.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(java.util.Locale.ROOT);
                if (normalized.contains("eacces") || normalized.contains("eperm")
                        || (normalized.contains("permission denied")
                        && !normalized.contains("camera") && !normalized.contains("microphone"))) return true;
            }
            failure = failure.getCause();
        }
        return false;
    }

    private void requestFileAccessAfterCameraFailure() {
        AndroidPermissionManager.requestFileAccess(result -> {
            String message = result.authorizationChanged
                    ? "File access changed. Start the recording again."
                    : "Unable to access the selected output file.";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopRecording();
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
