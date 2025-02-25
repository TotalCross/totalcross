// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



package totalcross.android;

import java.io.*;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import totalcross.AndroidUtils;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera;
import android.media.*;
import android.os.*;
import android.provider.MediaStore;
import android.util.Size;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.net.Uri;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.video.VideoCapture;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

public class CameraViewer extends AppCompatActivity // guich@tc126_34
{
   private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
   PreviewView previewView;
   private ImageCapture imageCapture;
   @SuppressLint("RestrictedApi")
   private VideoCapture videoCapture;

   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState)
   {
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

//      myPreview = new MyPreview(this);
//      ((FrameLayout) findViewById(R.id.preview)).addView(myPreview);
      previewView = findViewById(R.id.previewView);

      final Button buttonExit = (Button) findViewById(R.id.buttonExit);
      buttonExit.setText("Exit");
      buttonExit.setOnClickListener(new OnClickListener()
      {
         public void onClick(View v)
         {
            stopRecording();
            setResult(RESULT_CANCELED);
            finish();
         }
      });

      final Button buttonClick = (Button) findViewById(R.id.buttonClick);
      if (isMovie) {
         buttonClick.setText("Start");
         buttonClick.setOnClickListener(new OnClickListener() {
            @SuppressLint("RestrictedApi")
            public void onClick(View v) {
               try {
                  if (buttonClick.getText() == "Start") {
                     buttonClick.setText("Stop");
                     File f = new File(fileName);
                     recordVideo(f);
                  } else {
                     videoCapture.stopRecording();
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
                  stopPreview();
                  setResult(RESULT_CANCELED);
                  finish();
               }
            }
         });
      } else {
         buttonClick.setText("Click");
         buttonClick.setOnClickListener(new OnClickListener() {
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

   @SuppressLint("RestrictedApi")
   private void startCameraX(ProcessCameraProvider cameraProvider) {

      cameraProvider.unbindAll();

      CameraSelector cameraSelector = new CameraSelector.Builder()
              .requireLensFacing(CameraSelector.LENS_FACING_BACK)
              .build();

      Preview preview = new Preview.Builder().build();

      preview.setSurfaceProvider(previewView.getSurfaceProvider());

      imageCapture = new ImageCapture.Builder()
              .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
              .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
              .setTargetResolution(new Size(width, height))
              .setJpegQuality(stillQuality == 1 ? 75 : stillQuality == 2 ? 85 : 100)
              .build();

      videoCapture = new VideoCapture.Builder()
              .setVideoFrameRate(30)
              .build();

      cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
   }

   @SuppressLint("RestrictedApi")
   private void recordVideo(File f) {
      if (videoCapture != null) {
//         long timeStamp = System.currentTimeMillis();
//         ContentValues contentValues = new ContentValues();
//         contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
//         contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/3gp");


         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
         }
         videoCapture.startRecording(
                 new VideoCapture.OutputFileOptions.Builder(
                         f
//                         getContentResolver(),
//                         MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                         contentValues
                 ).build(),
                 getExecutor(),
                 new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
//                       Toast.makeText(CameraViewer.this,"Saving...",Toast.LENGTH_SHORT).show();
                       Uri uri = outputFileResults.getSavedUri();
                       setResult(RESULT_OK);
                       finish();
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
//                       Toast.makeText(CameraViewer.this,"Error: "+ message ,Toast.LENGTH_SHORT).show();
                       AndroidUtils.handleException(cause, false);
                       setResult(RESULT_CANCELED);
                       finish();
                    }
                 }

         );


      }
   }

   private void capturePhoto(OutputStream outStream) {
//      long timeStamp = System.currentTimeMillis();
//      ContentValues contentValues = new ContentValues();
//      contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
//      contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

      imageCapture.takePicture(
              new ImageCapture.OutputFileOptions.Builder(
                      outStream
//                      getContentResolver(),
//                      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                      contentValues
              ).build(),
              getExecutor(),
              new ImageCapture.OnImageSavedCallback() {
                 @Override
                 public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                    Toast.makeText(CameraViewer.this,"Saving...",Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
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



   int exifRotation = 0;

   class MyPreview extends SurfaceView implements SurfaceHolder.Callback
   { 
      MyPreview(Context context)
      {
         super(context);
/* Disabled to fix rotation support
		 if (allowRotation) 
		 { 
			switch (getResources().getConfiguration().orientation)
			{
			   case Configuration.ORIENTATION_PORTRAIT:
			      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				  break;
			   case Configuration.ORIENTATION_LANDSCAPE:
			      AndroidUtils.debug("" + getWindowManager().getDefaultDisplay().getRotation());
			      if (getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_270)
				     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); 
				  else 
				     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		    }
		 }
		 else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
*/
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         holder = getHolder(); 
         holder.addCallback(this); 
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // DON'T REMOVE THIS! 2.3 (LEVEL 10) STILL REQUIRES IT 
      }

      // Called once the holder is ready
      public void surfaceCreated(SurfaceHolder holder)
      {
         startPreview();
      }

      // Called when the holder is destroyed
      public void surfaceDestroyed(SurfaceHolder holder)
      {
         stopPreview();
         stopRecording();
         holder.removeCallback(this);
      }

      // Called when holder has changed
      public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
      { 
         if (camera != null)
         {
            Camera.Parameters parameters = camera.getParameters();
			
   			Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) 
            {
               case Surface.ROTATION_0: 
                  degrees = 0; 
                  break;
               case Surface.ROTATION_90: 
                  degrees = 90; 
                  break;
               case Surface.ROTATION_180: 
                  degrees = 180; 
                  break;
               case Surface.ROTATION_270: 
                  degrees = 270;
            }

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
            {
               result = (info.orientation + degrees) % 360;
               result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
               result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);
            exifRotation = result;
		
            parameters.setPictureFormat(PixelFormat.JPEG);
            if (Build.VERSION.SDK_INT >= 14 && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) && !inFocusExclusionList())
               parameters.setFocusMode("continuous-picture"); // FOCUS_MODE_CONTINUOUS_PICTURE 
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
               parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            int ww = Math.max(width,height);
            int hh = Math.min(width,height);
//            parameters.setPreviewSize(ww,hh);
            parameters.setJpegQuality(stillQuality == 1 ? 75 : stillQuality == 2 ? 85 : 100);
            if (width != 0 && height != 0)
               parameters.setPictureSize(ww,hh);
            try
            {
               camera.setParameters(parameters);
            }
            catch (RuntimeException re)
            {
               AndroidUtils.handleException(re,false);
            }
            camera.startPreview();
         }
      }

      private boolean inFocusExclusionList()
      {
         String id = Settings4A.deviceId;
         return id.indexOf("GT-S7580") != -1;
      }
   }

   SurfaceHolder holder; 
   Camera camera; 
   boolean isMovie;
   boolean allowRotation;
   String fileName;
   int stillQuality, width,height;
   MyPreview myPreview;
   MediaRecorder recorder;
   int cameraId;
   int result;



   private void startPreview()
   {
      if (camera == null)
         try
         {
            // The Surface has been created, acquire the camera and tell it where to draw.
            if ((camera = Camera.open()) == null)
            {
               Method getNumberOfCameras = android.hardware.Camera.class.getMethod("getNumberOfCameras");
               if (getNumberOfCameras != null)
               {
                  int i = (Integer) getNumberOfCameras.invoke(null, (Object[]) null);
                  Method open = android.hardware.Camera.class.getMethod("open", int.class);
                  if (open != null)
                     while (--i >= 0)
                        if ((camera = (Camera) open.invoke(null, i)) != null)
                        {
						   cameraId = i;
						   break;
						}
               }
            }
            camera.setPreviewDisplay(holder);
         }
         catch (Exception e)
         {
            AndroidUtils.handleException(e,false);
            setResult(RESULT_CANCELED);
            finish();
         }
   }

   private void stopPreview()
   {
      if (camera != null)
      {
         try {camera.stopPreview();} catch (Exception e) {e.printStackTrace();}
         try {camera.release();} catch (Exception e) {e.printStackTrace();}
         camera = null;
      }
   }
   
   private void startRecording() throws IllegalStateException, IOException
   {     
      try {camera.stopPreview();} catch (Exception e) {e.printStackTrace();} // stop camera's preview
	   recorder = new MediaRecorder();
	   camera.unlock();
	   recorder.setCamera(camera);
      recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
      recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
      recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      recorder.setVideoEncoder (MediaRecorder.VideoEncoder.DEFAULT);
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);     
	   recorder.setVideoSize(320,240);
     
      recorder.setOutputFile(fileName);
      recorder.setPreviewDisplay(holder.getSurface());

      recorder.prepare();
      recorder.start();   // Recording is now started	  
   }
   
   private void stopRecording()
   {
      if (recorder != null)
      {
         try {recorder.stop();} catch (Exception e) {e.printStackTrace();}         
		   try {recorder.reset();} catch (Exception e) {e.printStackTrace();}   // You can reuse the object by going back to setAudioSource() step
		   try {recorder.release();} catch (Exception e) {e.printStackTrace();} // Now the object cannot be reused
		   recorder = null;
		   camera.lock();	
		   stopPreview();
      }  
   }
   




   // Handles data for jpeg picture
   PictureCallback jpegCallback = new PictureCallback()
   {
      public void onPictureTaken(byte[] data, Camera camera)
      {
         try
         {
            OutputStream outStream = getContentResolver().openOutputStream(Uri.fromFile(new File(fileName)));
            outStream.write(data);
            outStream.close();
            Loader.autoRotatePhoto(getContentResolver(), fileName);
            setResult(RESULT_OK);
            finish();
         }
         catch (Exception e)
         {
            AndroidUtils.handleException(e,false);
            setResult(RESULT_CANCELED);
            finish();
         }
      }
   };
}
