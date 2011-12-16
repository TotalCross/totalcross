/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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



package totalcross.ui.media;

/**
 * Sound is used to play sounds such as beeps and tones.
 * <p>
 * Playing beeps is supported under all platforms but tones are only supported
 * where the underlying platform supports generating tones. Tones aren't supported
 * under Java.
 * <p>
 * Here is an example that beeps the speaker and plays a tone:
 *
 * <pre>
 * Sound.beep();
 * Sound.tone(4000, 300);
 * </pre>
 */
public final class Sound
{
   private static boolean toneErrDisplayed;
   private static boolean soundEnabled = true;

   private Sound()
   {
   }

   /** Plays the device's default beep sound. */
   public static void beep()
   {
      if (soundEnabled)
         java.awt.Toolkit.getDefaultToolkit().beep();
   }


   /**
   * Plays a tone of the specified frequency for the specified
   * duration. Tones will only play under Win32 and PalmOS, they won't
   * play under Java due to underlying platform limitations
   * (in these cases, use totalcross.ui.media.MediaClip).
   * @param freq frequency in hertz from 32 to 32767
   * @param duration duration in milliseconds
   */
   public static void tone(int freq, int duration)
   {
      if (!toneErrDisplayed)
      {
         beep();
         totalcross.Launcher.print("NOTICE: tones aren't supported under Java");
         toneErrDisplayed = true;
      }
   }

   /**
   * Sets the sound of the device on/off. Note that in PalmOS this turns off
   * the system sound only and, in this case, the Sound.beep goes off but the
   * Sound.tone will still work. The original volume is restored at the vm exit.
   * <p>Important: If the user had set its device sound to off, this method will
   * not turn it on, ie, it will keep the device in silence. Otherwise, it will
   * set the volume to its original configuration.
   * @param on if true enables the sound, if false disable it.
   */
   public static void setEnabled(boolean on)
   {
      soundEnabled = on;
   }

   /**
   * Plays a tone of the specified Midi Note Number for the specified
   * duration. Tones will only play under Win32 and PalmOS, they won't
   * play under Java due to underlying platform limitations
   * (in these cases, use totalcross.ui.media.MediaClip).
   * <p>Note: The smaller the midi number, the greater the error in the frequency,
   * due to the fact that the frequency calculated must be an integer.
   * @param midiNoteNumber number of the note according to MIDI standard
   * (eg. A (440hz) = 69), from 24 to 143
   * @param duration duration in milliseconds
   */
   public static void midiTone(int midiNoteNumber, int duration) // guich@300_33
   {
      int freq = (int)Math.round(440.0 * Math.pow (2, ((double)midiNoteNumber-69.0) / 12));
      tone(freq,duration);
   }
}