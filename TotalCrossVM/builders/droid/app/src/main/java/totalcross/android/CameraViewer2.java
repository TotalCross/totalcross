package totalcross.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
//import android.hardware.camera2;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import totalcross.AndroidUtils;

public class CameraViewer2 extends Activity {
   private static final String TAG = "AndroidCameraApi";
   private ImageButton takePictureButton;
   private TextureView textureView;
   private static final int PERMISSION_REQUEST_CODE = 1;
   private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
   static {
      ORIENTATIONS.append(Surface.ROTATION_0, 0);
      ORIENTATIONS.append(Surface.ROTATION_90, 90);
      ORIENTATIONS.append(Surface.ROTATION_180, 180);
      ORIENTATIONS.append(Surface.ROTATION_270, 270);
   }
   private String cameraId;
   protected CameraDevice cameraDevice;
   protected CameraCaptureSession cameraCaptureSessions;
   protected CaptureRequest captureRequest;
   protected CaptureRequest.Builder captureRequestBuilder;
   private Size imageDimension;
   private ImageReader imageReader;
   private File file;
   private static final int REQUEST_CAMERA_PERMISSION = 200;
   private boolean mFlashSupported;
   private Handler mBackgroundHandler;
   private HandlerThread mBackgroundThread;
   private String fileName;
   private int stillQuality;
   private boolean allowRotation;
   private int captureMode;
   private int width;
   private int height;
   private int scrWidth, scrHeight;
   private boolean isMovie;
   Intent startIntent = null;
   static int currentCamera = CameraMetadata.LENS_FACING_FRONT;
   private MediaRecorder mMediaRecorder;
   private boolean isRecording = false;
   private File mOutputFile;
   private android.hardware.Camera mCamera;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      startIntent = getIntent();
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      /*
         Get Params
       */
      if(Build.VERSION.SDK_INT >= 21) {
         Bundle b = getIntent().getExtras();
         fileName = b.getString("file");
         AndroidUtils.debug("file: " + fileName);
         stillQuality = b.getInt("quality");
         AndroidUtils.debug("quality: " + stillQuality);

         width = b.getInt("width");
         height = b.getInt("height");
         AndroidUtils.debug("width x height: " + width + " " + height);
         allowRotation = b.getBoolean("allowRotation");
         captureMode = b.getInt("captureMode");

         isMovie = fileName.endsWith(".3gp");

         AndroidUtils.debug("-2");
         FrameLayout fl = (FrameLayout) findViewById(R.id.preview);
         textureView = new TextureView(this);
         imageDimension = getBestFit();
         Size textureViewSize = getBestTextureViewSize(imageDimension);
         imageDimension = new Size(imageDimension.getHeight(), imageDimension.getWidth());
         AndroidUtils.debug("-1");
         assert textureView != null;

         FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(textureViewSize.getWidth(), textureViewSize.getHeight());
         lp.gravity = Gravity.CENTER;
         fl.addView(textureView, lp);
         textureView.setRotation(-getOrientationDegress());
         textureView.setSurfaceTextureListener(textureListener);
         takePictureButton = (ImageButton) findViewById(R.id.buttonClick);
         assert takePictureButton != null;
         AndroidUtils.debug("0");
         takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(isMovie) {
                  recordVideo();
               } else {
                  takePicture();
               }
            }
         });

         (findViewById(R.id.face)).setOnClickListener((v) -> changeCamera());
      }
   }
   TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
      @Override
      public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
         //open your camera here
         if(!isMovie) {
            AndroidUtils.debug("1");
            openCamera();
            AndroidUtils.debug("2");
         }
      }
      @Override
      public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
         DisplayMetrics dm = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(dm);
         scrWidth = dm.widthPixels;
         scrHeight = dm.heightPixels;
         if(!isMovie) {
            openCamera();
         }
      }
      @Override
      public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
         return false;
      }
      @Override
      public void onSurfaceTextureUpdated(SurfaceTexture surface) {
      }
   };
   private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
      @Override
      public void onOpened(CameraDevice camera) {
         //This is called when the camera is open
         AndroidUtils.debug( "onOpened");
         cameraDevice = camera;
         createCameraPreview();
      }
      @Override
      public void onDisconnected(CameraDevice camera) {
         if(Build.VERSION.SDK_INT >= 21) {
            cameraDevice.close();
         }
      }
      @Override
      public void onError(CameraDevice camera, int error) {
         if(Build.VERSION.SDK_INT >= 21) {
            cameraDevice.close();
            cameraDevice = null;
         }
      }
   };

   final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
      @Override
      public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
         super.onCaptureCompleted(session, request, result);
         AndroidUtils.debug("capture completed");
         createCameraPreview();
      }
   };

   private void releaseMediaRecorder() {
      if (mMediaRecorder != null) {
         // clear recorder configuration
         mMediaRecorder.reset();
         // release the recorder object
         mMediaRecorder.release();
         mMediaRecorder = null;
         // Lock camera for later use i.e taking it back from MediaRecorder.
         // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
         mCamera.lock();
      }
   }

   private void releaseCamera() {
      if (mCamera != null) {
         // release the camera for other applications
         mCamera.release();
         mCamera = null;
      }
   }

   protected void startBackgroundThread() {
      mBackgroundThread = new HandlerThread("Camera Background");
      mBackgroundThread.start();
      mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
   }
   protected void stopBackgroundThread() {
      if(Build.VERSION.SDK_INT >= 21) {
         if(mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
               mBackgroundThread.join();
               mBackgroundHandler = null;
               mBackgroundThread = null;
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }
   }

   private boolean checkPermissionFromDevice()
   {
      int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
      int camera_result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
      int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
      return write_external_storage_result == PackageManager.PERMISSION_GRANTED
              && camera_result == PackageManager.PERMISSION_GRANTED
              && record_audio_result == PackageManager.PERMISSION_GRANTED;
   }

   private void requestPermission()
   {
      ActivityCompat.requestPermissions(
              this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
              PERMISSION_REQUEST_CODE);
   }

   protected void recordVideo() {
      AndroidUtils.debug("before asking for permission to use camera from device.");
      if (!checkPermissionFromDevice()) {
         requestPermission();
      }

      if (isRecording) {
         AndroidUtils.debug("is recording!");
         // BEGIN_INCLUDE(stop_release_media_recorder)

         // stop recording and release camera
         try {
            mMediaRecorder.stop();  // stop the recording
         } catch (RuntimeException e) {
            // RuntimeException is thrown when stop() is called immediately after start().
            // In this case the output file is not properly constructed ans should be deleted.
            Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
            //noinspection ResultOfMethodCallIgnored
            mOutputFile.delete();
         }
         releaseMediaRecorder(); // release the MediaRecorder object
         mCamera.lock();         // take camera access back from MediaRecorder

         // inform the user that recording has stopped
         isRecording = false;
         releaseCamera();
         // END_INCLUDE(stop_release_media_recorder)
         finish();
      } else {

         // BEGIN_INCLUDE(prepare_start_media_recorder)
         AndroidUtils.debug("isn't recording!");
         new MediaPrepareTask().execute(null, null, null);

         // END_INCLUDE(prepare_start_media_recorder)

      }
   }

   protected void takePicture() {
      if(Build.VERSION.SDK_INT >= 21) {

         if (null == cameraDevice) {
            AndroidUtils.debug( "cameraDevice is null");
            return;
         }
         CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
         try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = ORIENTATIONS.get(rotation);
            int result;
            int orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing == CameraMetadata.LENS_FACING_FRONT)
            {
               result = (orientation + degrees) % 360;
               result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
               result = (orientation - degrees + 360) % 360;
            }

// int result = ORIENTATIONS.get(rotation);

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, result);
            final File file = new File(fileName);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
               @Override
               public void onImageAvailable(ImageReader reader) {
                  Image image = null;
                  try {
                     image = reader.acquireLatestImage();
                     ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                     byte[] bytes = new byte[buffer.capacity()];
                     buffer.get(bytes);
                     save(bytes);
                     AndroidUtils.debug("Photo taken");
                  } catch (FileNotFoundException e) {
                     e.printStackTrace();
                  } catch (IOException e) {
                     e.printStackTrace();
                  } finally {
                     if (image != null) {
                        image.close();
                     }
                  };

               }

               private void save(byte[] bytes) throws IOException {
                  OutputStream output = null;
                  try {
                     AndroidUtils.debug("saving...");
                     output = new FileOutputStream(file);
                     output.write(bytes);
                     setResult(RESULT_OK);
                     finish();
                  }
                  catch (Exception e) {
                     AndroidUtils.debug("Exception on saving");
                     AndroidUtils.handleException(e,false);
                     e.printStackTrace();
                     setResult(RESULT_CANCELED);
                     finish();
                  }
                  finally {
                     if (null != output) {
                        output.close();
                     }
                  }
               }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
               @Override
               public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                  super.onCaptureCompleted(session, request, result);
               }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
               @Override
               public void onConfigured(CameraCaptureSession session) {
                  try {
                     session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                  } catch (CameraAccessException e) {
                     e.printStackTrace();
                  }
               }

               @Override
               public void onConfigureFailed(CameraCaptureSession session) {
               }
            }, mBackgroundHandler);
         } catch (CameraAccessException e) {
            e.printStackTrace();
         }
      }
   }
   protected void createCameraPreview() {
      if(Build.VERSION.SDK_INT >= 21) {

         try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
               @Override
               public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                  //The camera is already closed
                  if (null == cameraDevice) {
                     return;
                  }
                  // When the session is ready, we start displaying the preview.
                  cameraCaptureSessions = cameraCaptureSession;
                  updatePreview();
               }

               @Override
               public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                  Toast.makeText(CameraViewer2.this, "Configuration change", Toast.LENGTH_SHORT).show();
               }
            }, null);
         } catch (CameraAccessException e) {
            e.printStackTrace();
         }
      }
   }

   public int getOrientationDegress () {
      int rotation = getWindowManager().getDefaultDisplay().getRotation();
      switch (rotation)
      {
         case Surface.ROTATION_0:
            return 0;
         case Surface.ROTATION_90:
            return 90;
         case Surface.ROTATION_180:
            return  180;
         case Surface.ROTATION_270:
            return  270;
      }

      return 0;
   }
   private void openCamera() {
      if(Build.VERSION.SDK_INT >= 21) {
         AndroidUtils.debug("3");
         CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
         AndroidUtils.debug( "is camera open");
         try {
            cameraId = manager.getCameraIdList()[currentCamera];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            AndroidUtils.debug("dimension: " + imageDimension.toString());
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
               ActivityCompat.requestPermissions(CameraViewer2.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
               return;
            }
            manager.openCamera(cameraId, stateCallback, null);
         } catch (CameraAccessException e) {
            e.printStackTrace();
         }
         AndroidUtils.debug("openCamera X");
      }
   }
   protected void updatePreview() {
      if(Build.VERSION.SDK_INT >= 21) {
         if (null == cameraDevice) {
            AndroidUtils.debug("updatePreview error, return");
         }
         captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
         try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
         } catch (CameraAccessException e) {
            e.printStackTrace();
         }
      }
   }
   private void closeCamera() {
      if(Build.VERSION.SDK_INT >= 21) {
         AndroidUtils.debug("closing camera");
         if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
         }
         if (null != imageReader) {
            imageReader.close();
            imageReader = null;
         }
      }
   }
   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      if (requestCode == REQUEST_CAMERA_PERMISSION) {
         if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // close the app
            Toast.makeText(CameraViewer2.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
            finish();
         }
      }
   }


   @Override
   protected void onResume() {
      super.onResume();
      AndroidUtils.debug("onResume");
      startBackgroundThread();
      if (textureView.isAvailable()) {
         if(!isMovie) {
            openCamera();
         }
      } else {
         textureView.setSurfaceTextureListener(textureListener);
      }
   }
   @Override
   protected void onPause() {
      super.onPause();

   }


   public void changeCamera() {
      currentCamera =
              currentCamera == CameraMetadata.LENS_FACING_FRONT ?
                      CameraMetadata.LENS_FACING_BACK :
                      CameraMetadata.LENS_FACING_FRONT;
//      closeCamera();
//      stopBackgroundThread();
      recreate();
   }


   @Override
   public void onBackPressed() {
//      closeCamera();
//      stopBackgroundThread();
      setResult(RESULT_CANCELED);
      finish();
   }

   private Size getBestTextureViewSize(Size size) {
      if(Build.VERSION.SDK_INT >= 21) {
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         scrWidth = metrics.widthPixels;
         scrHeight = metrics.heightPixels;
         if(scrHeight < scrWidth) {
            int aux = scrHeight;
            scrHeight = scrWidth;
            scrWidth = aux;
         }
         AndroidUtils.debug("screen: " + scrWidth + "x" + scrHeight);

         double widthRatio =  scrWidth / (double) size.getWidth();
         double heightRatio = scrHeight / (double) size.getHeight();
         double ratio = widthRatio <= heightRatio ? widthRatio : heightRatio;
         Size result = new Size((int)(ratio * size.getWidth()), (int)(ratio * size.getHeight()));
         AndroidUtils.debug("view: " + result.toString());
         return  result;
      }
      return null;
   }

   private Size getBestFit() {
      if (Build.VERSION.SDK_INT >= 21) {
         CameraManager manager =
                 (CameraManager) getSystemService(CAMERA_SERVICE);
         CameraCharacteristics cameraCharacteristics = null;
         try {
            cameraCharacteristics = manager
                    .getCameraCharacteristics(manager.getCameraIdList()[currentCamera]);
         } catch (CameraAccessException e) {
            e.printStackTrace();
         }
         StreamConfigurationMap map = cameraCharacteristics.get(
                 CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
         if (map == null) {
            throw new IllegalStateException("Failed to get configuration map: " + currentCamera);
         }
         Size[] sizes = map.getOutputSizes(SurfaceTexture.class);

         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         scrWidth = metrics.widthPixels;
         scrHeight = metrics.heightPixels;
         if(scrWidth < scrHeight) {
            int aux = scrHeight;
            scrHeight = scrWidth;
            scrWidth = aux;
         }

         double scrAspectRatio = scrWidth / (double) scrHeight;
         double bestAspectDiff = Double.MAX_VALUE;
         Size bestSize = null;
         // Get best aspect
         for (int i = 0; i < sizes.length; i++) {
            double asp = sizes[i].getWidth() / (double) sizes[i].getHeight();
            double aspDiff = Math.abs(asp - scrAspectRatio);
            if(aspDiff < bestAspectDiff) bestAspectDiff = aspDiff;
         }

         // Get best aspect sizes
         ArrayList<Size> bestAspects = new ArrayList<Size>();
         for (int i = 0; i < sizes.length; i++) {
            double asp = sizes[i].getWidth() / (double) sizes[i].getHeight();
            double aspDiff = Math.abs(asp - scrAspectRatio);
            if(aspDiff == bestAspectDiff) {
               bestAspects.add(sizes[i]);
            }
         }

         // double bestRelativeDiffSumOfSizes = Double.MIN_VALUE;

            Collections.sort(bestAspects, new Comparator<Size>() {
            public int compare (Size a, Size b) {
               int aa = a.getWidth() * a.getHeight();
               int ab = b.getWidth() * b.getHeight();
               return aa - ab;
            }
         });
         bestSize = bestAspects.get((bestAspects.size() / 2) + 1);

         // Order by least scale factor
         // for (Size size : bestAspects) {
         //    int width = size.getWidth();
         //    int height = size.getHeight();
         //    double relativeDiff = Math.abs(width - scrWidth)/scrWidth + Math.abs(height - scrHeight)/scrHeight;
         //    if(relativeDiff > bestRelativeDiffSumOfSizes) {
         //       bestRelativeDiffSumOfSizes = relativeDiff;
         //       bestSize = size;
         //    }
         // }

//         return metrics.widthPixels > metrics.heightPixels ?
//                 bestSize :
                 return new Size(
                         bestSize.getHeight(),
                         bestSize.getWidth()
                 );
      }
      return null;
   }

   private boolean prepareVideoRecorder() {
      AndroidUtils.debug("just got to prepare the video Recorder");
      // BEGIN_INCLUDE (configure_preview)
      mCamera = CameraHelper.getDefaultCameraInstance();

      AndroidUtils.debug("settings parameters");
      // We need to make sure that our preview and recording video size are supported by the
      // camera. Query camera to find all the sizes and choose the optimal size given the
      // dimensions of our preview surface.
      android.hardware.Camera.Parameters parameters = mCamera.getParameters();
      AndroidUtils.debug("getting supported preview sizes");
      List<android.hardware.Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
      AndroidUtils.debug("getting supported video sizes");
      List<android.hardware.Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
      AndroidUtils.debug("getting optimal sizes");
      android.hardware.Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
              mSupportedPreviewSizes, textureView.getWidth(), textureView.getHeight());

      AndroidUtils.debug("getting camcoder profile");
      // Use the same size for recording profile.
      CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
      AndroidUtils.debug("setting video frame width");
      profile.videoFrameWidth = width;
      AndroidUtils.debug("setting video frame height");
      profile.videoFrameHeight = height;
      AndroidUtils.debug("setting file format");
      profile.fileFormat = MediaRecorder.OutputFormat.THREE_GPP;

      AndroidUtils.debug("settings preview sizes");
      // likewise for the camera object itself.
      parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
      int result = 0;
      try {
         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
               CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
               CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
               // Orientation
               int rotation = getWindowManager().getDefaultDisplay().getRotation();
               int degrees = ORIENTATIONS.get(rotation);
               int orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
               int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
               if (facing == CameraMetadata.LENS_FACING_FRONT)
               {
                  result = (orientation + degrees) % 360;
                  result = (360 - result) % 360;  // compensate the mirror
               } else {  // back-facing
                  result = (orientation - degrees + 360) % 360;
               }

            parameters.setRotation(result);
         }
      } catch (CameraAccessException e) {
         e.printStackTrace();
         AndroidUtils.debug("camera access problem.");
      }
      AndroidUtils.debug("setting parameters");
      mCamera.setParameters(parameters);



      try {
         AndroidUtils.debug("setting the preview texture");
         // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
         // with {@link SurfaceView}
         mCamera.setPreviewTexture(textureView.getSurfaceTexture());
      } catch (IOException e) {
         Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
         return false;
      }
      // END_INCLUDE (configure_preview)

      AndroidUtils.debug("instantiating media recorder");
      // BEGIN_INCLUDE (configure_media_recorder)
      mMediaRecorder = new MediaRecorder();
      AndroidUtils.debug("setting display orientation");
      mCamera.setDisplayOrientation(result);
      AndroidUtils.debug("setting orientation hint");
      mMediaRecorder.setOrientationHint(result);

      AndroidUtils.debug("unlocking camera");
      // Step 1: Unlock and set camera to MediaRecorder
      mCamera.unlock();
      AndroidUtils.debug("setting camera");
      mMediaRecorder.setCamera(mCamera);

      // Step 2: Set sources
      if(captureMode == 2) {
         mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
      }
      mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

      // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
      mMediaRecorder.setProfile(profile);
      // Step 4: Set output file
      mMediaRecorder.setOutputFile(fileName);
      // END_INCLUDE (configure_media_recorder)

      // Step 5: Prepare configured MediaRecorder
      try {
         mMediaRecorder.prepare();
      } catch (IllegalStateException e) {
         Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
         releaseMediaRecorder();
         return false;
      } catch (IOException e) {
         Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
         releaseMediaRecorder();
         return false;
      }
      return true;
   }

   /**
    * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
    * operation.
    */
   class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

      @Override
      protected Boolean doInBackground(Void... voids) {
         // initialize video camera
         if (prepareVideoRecorder()) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            mMediaRecorder.start();

            isRecording = true;
         } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            return false;
         }
         return true;
      }

      @Override
      protected void onPostExecute(Boolean result) {
         if (!result) {
            CameraViewer2.this.finish();
         }
      }
   }

}