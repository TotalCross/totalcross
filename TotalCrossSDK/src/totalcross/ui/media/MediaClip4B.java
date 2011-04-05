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



package totalcross.ui.media;

import java.io.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import totalcross.*;
import totalcross.io.*;
import totalcross.io.IOException;
import totalcross.ui.*;

public class MediaClip4B implements PlayerListener
{
   Object mediaClipRef;
   Launcher4B.S2IS mediaStream;
   int currentState;
   RandomAccessStream stream;
   Player player;
   RecordControl recordControl;
   boolean recording;

   static MainWindow mainWindow = MainWindow.getMainWindow();

   public final static int UNREALIZED = 0;
   public final static int REALIZED = 1;
   public final static int PREFETCHED = 2;
   public final static int STARTED = 3;
   public final static int CLOSED = 4;

   public static final int VOICE = 8000;
   public static final int LOW = 11025;
   public static final int MEDIUM = 22050;
   public static final int HIGH = 44100;

   public MediaClip4B(RandomAccessStream stream) throws IOException
   {
      mediaStream = new Launcher4B.S2IS(stream);
      this.stream = stream;
      currentState = UNREALIZED;

      /*
       * possible types: if (path.endsWith(".wav")) content = "audio/x-wav"; else if (path.endsWith(".mp3")) content =
       * "audio/mpeg"; else if (path.endsWith(".midi")) content = "audio/midi"; else content = "audio/basic"
       */
   }

   final public void start() throws IOException
   {
      try
      {
         if (currentState == UNREALIZED)
         {
            player = Manager.createPlayer(mediaStream, "audio/x-wav"); // right now, only wav is supported.
            player.addPlayerListener(this);
            player.realize();
            player.prefetch();
            currentState = PREFETCHED;
         }
         if (currentState == PREFETCHED)
            player.start();
      }
      catch (java.io.IOException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
      catch (MediaException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
   }

   final public void stop() throws IOException
   {
      try
      {
         if (currentState == STARTED)
            player.stop();
      }
      catch (MediaException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
   }

   final public void reset() throws IOException
   {
      try
      {
         if (currentState == STARTED)
            player.stop();
         player.setMediaTime(0);
      }
      catch (java.lang.IllegalStateException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
      catch (javax.microedition.media.MediaException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
   }

   public int getCurrentState()
   {
      return currentState;
   }

   final public void close() throws IOException
   {
      if (!recording)
         reset();
      else
      {
         try
         {
            recordControl.commit();
            player.close();
         }
         catch (java.io.IOException e)
         {
         }
      }
   }

   final public void record(int samplesPerSecond, int bitsPerSample, boolean stereo) throws totalcross.io.IOException
   {
      try
      {
         player = Manager.createPlayer("capture://audio");
         player.addPlayerListener(this);
         player.realize();
         recordControl = (RecordControl) player.getControl("RecordControl");
         OutputStream os = new Launcher4B.S2OS(stream);
         recordControl.setRecordStream(os);
         recordControl.startRecord();
         player.start();
         recording = true;
      }
      catch (java.io.IOException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
      catch (MediaException e)
      {
         throw new totalcross.io.IOException(e.getMessage());
      }
   }

   final public void playerUpdate(Player player, String event, Object eventData)
   {
      int eventType = -1;
      if (event == PlayerListener.STARTED)
      {
         eventType = MediaClipEvent.STARTED;
         currentState = STARTED;
      }
      else if (event == PlayerListener.STOPPED)
      {
         eventType = MediaClipEvent.STOPPED;
         currentState = PREFETCHED;
      }
      else if (event == PlayerListener.END_OF_MEDIA)
      {
         eventType = MediaClipEvent.END_OF_MEDIA;
         currentState = PREFETCHED;
      }
      else if (event == PlayerListener.CLOSED)
      {
         eventType = MediaClipEvent.CLOSED;
         currentState = CLOSED;
      }
      else if (event == PlayerListener.ERROR)
         eventType = MediaClipEvent.ERROR;

      if (eventType > -1)
         mainWindow.broadcastEvent(new MediaClipEvent(eventType, mainWindow));
   }
}
