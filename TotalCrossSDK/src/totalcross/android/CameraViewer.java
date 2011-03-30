/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: CameraViewer.java,v 1.1 2011-01-21 23:29:31 guich Exp $

package totalcross.android;

import java.io.*;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.hardware.Camera.PictureCallback;
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
            camera.startPreview();
      }
   }

   SurfaceHolder holder; 
   Camera camera; 
   boolean isMovie;
   String fileName;
   Preview preview; 
   MediaRecorder recorder;

   private void startPreview()
   {
      if (camera == null)
         try
         {
            // The Surface has been created, acquire the camera and tell it where to draw.
            camera = Camera.open(); 
            camera.setPreviewDisplay(holder);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
   }

   private void stopPreview()
   {
      if (camera != null)
      {
         camera.stopPreview();
         camera.release();
         camera = null;
      }
   }
   
   private void startRecording() throws IllegalStateException, IOException
   {
      recorder = new MediaRecorder();
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
      recorder.setOutputFile(fileName);
      recorder.prepare();
      recorder.start();   // Recording is now started
   }
   
   private void stopRecording()
   {
      if (recorder != null)
      {
         recorder.stop();
         recorder.reset();   // You can reuse the object by going back to setAudioSource() step
         recorder.release(); // Now the object cannot be reused
         recorder = null;
      }
   }
   
   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      fileName = getIntent().getExtras().getString("file");

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
                  camera.takePicture(null, null, jpegCallback);
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
            catch (Exception e) {e.printStackTrace();}
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
            // Write to SD Card
            FileOutputStream outStream = new FileOutputStream(fileName); 
            outStream.write(data);
            outStream.close();
            setResult(RESULT_OK);
            finish();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   };

}
