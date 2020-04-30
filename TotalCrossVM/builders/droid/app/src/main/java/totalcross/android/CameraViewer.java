// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



package totalcross.android;

import java.io.*;
import java.lang.reflect.Method;
import totalcross.AndroidUtils;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera;
import android.media.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.content.res.*;

public class CameraViewer extends Activity // guich@tc126_34
{
   class Preview extends SurfaceView implements SurfaceHolder.Callback
   { 
      Preview(Context context)
      {
         super(context);
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
   Preview preview; 
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

      preview = new Preview(this); 
      ((FrameLayout) findViewById(R.id.preview)).addView(preview);

      final Button buttonExit = (Button) findViewById(R.id.buttonExit);
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
      buttonClick.setText(isMovie ? "Start" : "Click");
      buttonExit.setText("Exit");
      buttonClick.setOnClickListener(new OnClickListener()
      {
         public void onClick(View v)
         {
            try
            {
               if (!isMovie)
               {
                  if (camera == null) // guich@tc130: prevent NPE
                     startPreview();
                  if (camera != null)
                  {
                     buttonClick.setClickable(false);
                     Camera.Parameters parameters = camera.getParameters();
                     parameters.setRotation(rotation);
                     try {
                    	 camera.setParameters(parameters);
                    	 camera.reconnect();
                     } catch (Exception re) {
                    	 AndroidUtils.handleException(re, false);
                     }
                     camera.takePicture(null, null, jpegCallback);
                  }
                  else
                  {
                     setResult(RESULT_CANCELED);
                     finish();
                  }
               }
               else
               {
                  if (recorder == null)
                  {
                     buttonClick.setText("Stop");
                     startRecording();
                  }
                  else
                  {
                     stopRecording();
                     setResult(RESULT_OK);
                     finish();
                  }
               }
            }
            catch (Exception e) 
            {
               AndroidUtils.handleException(e,false);
               stopPreview();
               setResult(RESULT_CANCELED);
               finish();
            }
         }
      });
   }
   
   int rotation = 0;

   // Handles data for jpeg picture
   PictureCallback jpegCallback = new PictureCallback()
   {
      public void onPictureTaken(byte[] data, Camera camera)
      {
         try
         {
            FileOutputStream outStream = new FileOutputStream(fileName); 
            outStream.write(data);
            outStream.close();
            Loader.autoRotatePhoto(fileName);
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
