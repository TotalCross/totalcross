/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.android;

import totalcross.*;
import totalcross.android.compat.*;

import java.io.*;
import java.lang.reflect.Method;

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

public class CameraViewer extends Activity // guich@tc126_34
{
   class Preview extends SurfaceView implements SurfaceHolder.Callback
   { 
      Preview(Context context)
      {
         super(context);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         holder = getHolder(); 
         holder.addCallback(this); 
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
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
      }

      // Called when holder has changed
      public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
      { 
         if (camera != null)
         {
            Camera.Parameters parameters=camera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            int ww = Math.max(width,height);
            int hh = Math.min(width,height);
            Level5.getInstance().setPictureParameters(parameters, stillQuality, ww,hh);
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
   }

   SurfaceHolder holder; 
   Camera camera; 
   boolean isMovie;
   String fileName;
   int stillQuality, width,height;
   Preview preview; 
   MediaRecorder recorder;

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
                           break;
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
      stopPreview(); // stop camera's preview
      recorder = new MediaRecorder();
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
         try {recorder.stop();} catch (Exception e) {}
         try {recorder.reset();} catch (Exception e) {}   // You can reuse the object by going back to setAudioSource() step
         try {recorder.release();} catch (Exception e) {} // Now the object cannot be reused
         recorder = null;
      }
   }
   
   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      Bundle b = getIntent().getExtras();
      fileName = b.getString("file");
      stillQuality = b.getInt("quality");
      width = b.getInt("width");
      height = b.getInt("height");

      isMovie = fileName.endsWith(".3gp");

      preview = new Preview(this); 
      ((FrameLayout) findViewById(R.id.preview)).addView(preview);

      final Button buttonExit = (Button) findViewById(R.id.buttonExit);
      buttonExit.setOnClickListener(new OnClickListener()
      {
         public void onClick(View v)
         {
            setResult(RESULT_CANCELED);
            finish();
         }
      });
      
      final Button buttonClick = (Button) findViewById(R.id.buttonClick);
      if (isMovie)
         buttonClick.setText("Start");
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
