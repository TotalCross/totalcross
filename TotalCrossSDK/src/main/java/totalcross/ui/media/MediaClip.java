// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.media;

import totalcross.Launcher;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.ui.MainWindow;

/**
 * MediaClip is a sound clip. It will be updated in the future to support movie clips.
 * <p>
 * Support for sound clips varies between platforms. Some Java virtual machines support .wav and .au sound files and
 * some versions don't seem to support either format.
 * <p>
 * Using a TotalCross Virtual Machine, .wav format sound clips are supported under Win32, WinCE and Palm OS. Under Win32
 * and WinCE, the .wav files for sound clips may exist in a file outside of the program's tcz file; the wav file can be
 * stored inside a pdb/tcz file (and it has precedence over the one located in the file system).
 * <p>
 * In Palm OS, the wav must be added to the pdb (The Deployer does this automagically. Just reference a .wav and it will
 * be added).
 * <p>
 * If you're playing a sound clip under a Windows CE device and you don't hear anything, make sure that the device is
 * set to allow programs to play sounds. To check the setting, look at:
 * <p>
 * Start->Settings->Volume & Sounds
 * <p>
 * for the check box:
 * <p>
 * Enable sounds for: Programs
 * <p>
 * If it is not checked on, sound clips won't play.
 * <p>
 * Here is an example that plays a sound:
 * 
 * <pre>
 * File soundFile = new File("sound.wav", File.READ_WRITE);
 * MediaClip s = new MediaClip(soundFile);
 * s.start();
 * </pre>
 * 
 * Under Palm OS, the currently supported formats are: uncompressed (PCM) or IMA 4-bit adaptive differential (IMA
 * ADPCM). The ADPCM type is also known as DVI ADPCM; in a WAVE file, it's known as format 0x11. One or two-channels;
 * All normal sampling rates (8k, 11k, 22.05k, 44.1k, 48, 96k).
 * <p>
 * Note that some Palm OS devices does not support 16bit waves, so better store them in 8bits.
 * <p>
 * MediaClip also support sound recording. When recording sound, you can only call the record and stop methods. Sound
 * recording does not work under JavaSE.
 * <p>
 * The MediaClip events are broadcasted to the MainWindow controls.
 */
public class MediaClip {
  Object mediaClipRef;
  Launcher.S2IS mediaStream;
  int currentState;

  static MainWindow mainWindow = MainWindow.getMainWindow();

  /** The state of the MediaClip indicating that it has not acquired the required information and resources to function. */
  public final static int UNREALIZED = 0;
  /** The state of the MediaClip indicating that it has acquired the required information but not the resources to function. */
  public final static int REALIZED = 1;
  /** The state of the MediaClip indicating that it has acquired all the resources to begin playing. */
  public final static int PREFETCHED = 2;
  /** The state of the MediaClip indicating that the MediaClip has already started. */
  public final static int STARTED = 3;
  /** The state of the MediaClip indicating that the MediaClip is closed. */
  public final static int CLOSED = 4;

  /** Used as a samplesPerSecond parameter in record method. */
  public static final int VOICE = 8000;
  /** Used as a samplesPerSecond parameter in record method. */
  public static final int LOW = 11025;
  /** Used as a samplesPerSecond parameter in record method. */
  public static final int MEDIUM = 22050;
  /** Used as a samplesPerSecond parameter in record method. */
  public static final int HIGH = 44100;

  /**
   * Create a MediaClip to play back or record a media from/to a RandomAccessStream. Currently the only media type
   * supported is audio wave.<br>
   * It's important to notice that you may not use the same object for play back and record.<br>
   * 
   * @param stream
   *           The random access stream used to read (when playing) or writting (when recording) the data.
   * @throws IOException
   * @see totalcross.io.RandomAccessStream
   */
  public MediaClip(RandomAccessStream stream) throws IOException {
    try {
      mediaStream = new Launcher.S2IS(stream);
      mediaClipRef = new sun.audio.AudioStream(mediaStream);
    } catch (java.io.IOException e) {
      throw new totalcross.io.IOException(e.getMessage());
    }

    if (mediaClipRef == null) {
      throw new totalcross.io.IOException("Could not load the given file.");
    }
    currentState = PREFETCHED;
  }

  /**
   * Starts the MediaClip as soon as possible. If the MediaClip was previously stopped by calling stop, it will resume
   * playback from where it was previously stopped. If the MediaClip has reached the end of media, calling start will
   * automatically start the playback from the start of the media.<br>
   * <br>
   * When start returns successfully, the MediaClip must have been started and a STARTED event will be delivered.
   * However, the MediaClip is not guaranteed to be in the STARTED state. The MediaClip may have already stopped (in
   * the PREFETCHED state) because the media has 0 or a very short duration.<br>
   * <br>
   * If start is called when the MediaClip is in the STARTED state, the request will be ignored.
   * 
   * @throws IOException
   */
  final public void start() throws IOException {
    if (currentState != PREFETCHED) {
      return;
    }

    if (!Launcher.isApplication) {
      ((java.applet.AudioClip) mediaClipRef).play();
    } else {
      sun.audio.AudioPlayer.player.start((sun.audio.AudioStream) mediaClipRef);
    }
    currentState = STARTED;
    mainWindow.broadcastEvent(new MediaClipEvent(MediaClipEvent.STARTED, mainWindow));
  }

  /**
   * Stops the MediaClip. It will pause the playback/record at the current media time.<br>
   * <br>
   * When stop returns, the MediaClip is in the PREFETCHED state. A STOPPED event will be delivered.<br>
   * <br>
   * If stop is called on a stopped MediaClip, the request is ignored.
   * 
   * @throws IOException
   */
  final public void stop() throws IOException {
    if (currentState != STARTED) {
      return;
    }

    if (!Launcher.isApplication) {
      ((java.applet.AudioClip) mediaClipRef).stop();
    } else {
      sun.audio.AudioPlayer.player.stop((sun.audio.AudioStream) mediaClipRef);
    }
    currentState = PREFETCHED;
    mainWindow.broadcastEvent(new MediaClipEvent(MediaClipEvent.STOPPED, mainWindow));
  }

  /**
   * Stops the playback and reset the media time to 0.<br>
   * 
   * @throws IOException
   */
  final public void reset() throws IOException {
    try {
      this.stop();
    } catch (IOException e) {
    }
  }

  /**
   * Gets the current state of this MediaClip. The possible states are: UNREALIZED, REALIZED, PREFETCHED, STARTED,
   * CLOSED.
   * 
   * @see #UNREALIZED
   * @see #REALIZED
   * @see #PREFETCHED
   * @see #STARTED
   * @see #CLOSED
   */
  public int getCurrentState() {
    return currentState;
  }

  /**
   * Close the MediaClip and release its resources, WITHOUT closing the underlying stream.<br>
   * <br>
   * When the method returns, the MediaClip is in the CLOSED state and can no longer be used. A CLOSED event will be
   * delivered. <br>
   * <br>
   * If close is called on a closed MediaClip the request is ignored.
   * 
   * @throws IOException
   */
  final public void close() throws IOException {
    if (currentState == CLOSED) {
      return;
    }
    if (!Launcher.isApplication) {
      ((java.applet.AudioClip) mediaClipRef).stop();
    } else {
      sun.audio.AudioPlayer.player.stop((sun.audio.AudioStream) mediaClipRef); // guich@582_8
    }
    mediaClipRef = null;
    currentState = CLOSED;
    mainWindow.broadcastEvent(new MediaClipEvent(MediaClipEvent.CLOSED, mainWindow));
  }

  /**
   * Starts recording the media until the <code>stop</code> method is called.<br>
   * Please notice you must explicitly close the MediaClip and close the underlying stream to make sure the data was
   * fully written to the underlying stream.<br>
   * <br>
   * 
   * <pre>
   * Platform specific limitations:
   *   Palm OS
   *       you may only record if the given stream is an instance of totalcross.io.File.
   *       the byte rate of the audio recorded must be under 60kbps. The byte rate is calculated as following:
   *           byte rate = samples per second * bits per sample * (stereo ? 2 : 1) / 8&lt;br&gt;
   *           If the byte rate exceeds this value, the record method will record in mono instead of stereo, and also 
   *           reduce also bits per sample to 8 if necessary.&lt;br&gt;
   *   BlackBerry
   *       the parameters received by record are not supported by BlackBerry, and therefore ignored.
   *       &quot;The BlackBerry smartphone supports audio recording using two different formats, Adaptive Multi-Rate (AMR) 
   *       and 8kHz mono-16-bit Pulse Code Modulation (PCM). The default encoding used by the BlackBerry smartphone is AMR.&quot;
   * </pre>
   * 
   * @param samplesPerSecond
   *           may be VOICE (8000), LOW (11025), MEDIUM (22050) or HIGH (44100), otherwise an IllegalArgumentException
   *           is thrown.
   * @param bitsPerSample
   *           must be 8 or 16, otherwise an IllegalArgumentException is thrown.
   * @param stereo
   *           true for stereo recording or false for mono recording.
   * @throws IOException
   * @see #VOICE
   * @see #LOW
   * @see #MEDIUM
   * @see #HIGH
   */
  final public void record(int samplesPerSecond, int bitsPerSample, boolean stereo) throws IOException {
    if (samplesPerSecond != VOICE && samplesPerSecond != LOW && samplesPerSecond != MEDIUM
        && samplesPerSecond != HIGH) {
      throw new IllegalArgumentException("Invalid value for samplesPerSecond: " + samplesPerSecond);
    }
    if (bitsPerSample != 8 && bitsPerSample != 16) {
      throw new IllegalArgumentException("Invalid value for bitsPerSample: " + bitsPerSample);
    }
  }
}
