// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.Manifest;
import android.content.Context;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import totalcross.AndroidUtils;

public class CameraViewer extends AppCompatActivity {
   private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
   PreviewView previewView;
   private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recorder;
    private Camera camera;

    boolean isMovie;
    String fileName;
    int stillQuality, width, height;

    boolean allowRotation;

   /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      setTitle("");

      Bundle b = getIntent().getExtras();
      fileName = b.getString("file");
      stillQuality = b.getInt("quality");
      width = b.getInt("width");
      height = b.getInt("height");
      allowRotation = b.getBoolean("allowRotation");

      isMovie = fileName.endsWith(".3gp");

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
      if (isMovie) {
         buttonClick.setText("Start");
            buttonClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               try {
                  if (buttonClick.getText() == "Start") {
                     buttonClick.setText("Stop");
                     File f = new File(fileName);
                     recordVideo(f);
                  } else {
                            stopRecording();
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
                  OutputStream outStream = getContentResolver().openOutputStream(Uri.fromFile(new File(fileName)));
                  capturePhoto(outStream);
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

        adjustToSafeArea(this);

      cameraProviderFuture = ProcessCameraProvider.getInstance(this);
      cameraProviderFuture.addListener(() -> {
         try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            startCameraX(cameraProvider);
         } catch (ExecutionException e) {
            e.printStackTrace();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

      }, getExecutor());
   }

   private Executor getExecutor() {
      return ContextCompat.getMainExecutor(this);
   }

   private void startCameraX(ProcessCameraProvider cameraProvider) {

      cameraProvider.unbindAll();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

      Preview preview = new Preview.Builder().build();

      preview.setSurfaceProvider(previewView.getSurfaceProvider());

      imageCapture = new ImageCapture.Builder()
              .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
              .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
              .setTargetResolution(new Size(width, height))
              .setJpegQuality(stillQuality == 1 ? 75 : stillQuality == 2 ? 85 : 100)
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

   private void recordVideo(File f) {
      if (videoCapture != null) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED) {
            return;
         }

            recorder = videoCapture.getOutput()
                    .prepareRecording(this,
                            new FileOutputOptions.Builder(f).build())
                    .withAudioEnabled()
                    .start(getExecutor(), event -> {
                        if (event instanceof VideoRecordEvent.Finalize) {
                            setResult(RESULT_OK);
                       finish();
                    }
                    });
      }
   }

   private Bitmap imageProxyToBitmap(ImageProxy image) {
      ByteBuffer buffer = image.getPlanes()[0].getBuffer();
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
   }

   private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
      Matrix matrix = new Matrix();
      matrix.postRotate(rotationDegrees);
      return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
   }

   private void saveBitmap(Bitmap bitmap, OutputStream outStream) throws IOException {
      bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
      outStream.flush();
      outStream.close();
   }

   private void capturePhoto(OutputStream outStream) {
//      long timeStamp = System.currentTimeMillis();
//      ContentValues contentValues = new ContentValues();
//      contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
//      contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

      imageCapture.takePicture(
              getExecutor(),
              new ImageCapture.OnImageCapturedCallback() {
                 @Override
                 public void onCaptureSuccess(@NonNull ImageProxy image) {
//                    Toast.makeText(CameraViewer.this,"Saving...",Toast.LENGTH_SHORT).show();
                    int rotation = image.getImageInfo().getRotationDegrees();

                    // 1. Convert ImageProxy to Bitmap
                    Bitmap bmp = imageProxyToBitmap(image);

                    // 2. Adjust rotation
                    bmp = rotateBitmap(bmp, rotation);

                    try {
                       saveBitmap(bmp, outStream);
                       setResult(RESULT_OK);
                       finish();
                    } catch (IOException e) {
                       AndroidUtils.handleException(e, false);
                    }

                    image.close();
                 }

                 @Override
                 public void onError(@NonNull ImageCaptureException exception) {
//                    Toast.makeText(CameraViewer.this,"Error: "+exception.getMessage(),Toast.LENGTH_SHORT).show();
                    AndroidUtils.handleException(exception, false);
                    setResult(RESULT_CANCELED);
                    finish();
                 }
              });

   }

   private void stopRecording()
   {
      if (recorder != null)
      {
            recorder.stop();
		   recorder = null;
         }
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

    private void adjustToSafeArea(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );
        View rootView = activity.getWindow().getDecorView();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, insets) -> {

            WindowInsetsCompat rootInsets =
                    ViewCompat.getRootWindowInsets(view);

            if (rootInsets == null) {
                return insets;
            }

            Insets safeInsets = rootInsets.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );

            Insets imeInsets = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int bottomInset = Math.max(
                    safeInsets.bottom,
                    imeInsets.bottom
            );

            view.setPadding(
                    safeInsets.left,
                    safeInsets.top,
                    safeInsets.right,
                    bottomInset
            );

            return insets;
        });
    }
}
