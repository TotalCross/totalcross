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

/**
 * Sound is used to play sounds such as beeps and tones and mp3.
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
public final class Sound {
  private static boolean toneErrDisplayed;
  private static boolean soundEnabled = true;

  private Sound() {
  }

  /** Plays the device's default beep sound. */
  public static void beep() {
    if (soundEnabled) {
      java.awt.Toolkit.getDefaultToolkit().beep();
    }
  }

  /**
   * Plays a tone of the specified frequency for the specified
   * duration. Tones will only play under Win32, they won't
   * play under Java due to underlying platform limitations.
   * 
   * Works on Windows CE Hand Held products to play tones, however the tone
   * is played asynchronously, so you must do a Vm.sleep(ms) for each tone played if you play more than one.
   * 
   * @param freq frequency in hertz from 32 to 32767
   * @param duration duration in milliseconds
   */
  public static void tone(int freq, int duration) {
    if (!toneErrDisplayed) {
      beep();
      totalcross.Launcher.print("NOTICE: tones aren't supported under Java");
      toneErrDisplayed = true;
    }
  }

  /**
   * Sets the sound of the device on/off. 
   * The original volume is restored at the vm exit.
   * <p>Important: If the user had set its device sound to off, this method will
   * not turn it on, ie, it will keep the device in silence. Otherwise, it will
   * set the volume to its original configuration.
   * @param on if true enables the sound, if false disable it.
   */
  public static void setEnabled(boolean on) {
    soundEnabled = on;
  }

  /**
   * Plays a tone of the specified Midi Note Number for the specified
   * duration. Tones will only play under Win32, they won't
   * play under Java due to underlying platform limitations
   * (in these cases, use totalcross.ui.media.MediaClip).
   * <p>Note: The smaller the midi number, the greater the error in the frequency,
   * due to the fact that the frequency calculated must be an integer.
   * @param midiNoteNumber number of the note according to MIDI standard
   * (eg. A (440hz) = 69), from 24 to 143
   * @param duration duration in milliseconds
   * @deprecated Since it only works on Windows 32.
   */
  @Deprecated
  public static void midiTone(int midiNoteNumber, int duration) // guich@300_33
  {
    int freq = (int) Math.round(440.0 * Math.pow(2, ((double) midiNoteNumber - 69.0) / 12));
    tone(freq, duration);
  }

  /** Plays the given short wav or mp3 file. Make sure that the sounds are enabled, or you will not hear it!
   * Works on Android, iOS and Win32, but does not work on JavaSE. On WinCE, supports playing only WAV files.
   * 
   * The file must be located in the file system.
   * If you store a mp3 file in the TCZ, you can get it out using this code:
   * <pre>
   * new File("device/mysound.mp3", File.CREATE_EMPTY).writeAndClose(Vm.getFile("mysound.mp3"));
   * </pre>
   * Then you play it using:
   * <pre>
   * Sound.play("device/mysound.mp3");
   * </pre>
   * The last sound is cached, so playing it again is fast. If you want to unload it, just call <code>Sound.play("");</code>,
   * but this is not needed since small mp3/wav files consumes just a few memory. Cache is not done in Win32.
   */
  public static void play(String filename) {
  }

  /** Activates speech and let the user dictate something that will be returned. The params can be:
   * <pre>
   * title:title to show to user
   * timeout: timeout in millis that will be waiten to return after dictation finishes 
   * </pre>
   * If you pass more than one parameter, separate them with | (pipe).
   * 
   * So, for example:
   * <pre>
   * String ret = Sound.toText("title:Answer|timeout:500");
   * </pre>
   */
  public static String toText(String params) {
    return "";
  }

  /** Activates speech and let the user hear the given text.
   * The very first time it is used in the device, Android will ask you to install a high definition 
   * language package (and you should install it because the difference is impressive).
   * 
   * The first time it is called, you can pass some parameters:
   * <pre>
   * languages: shows in adb's logcat the available languages. For example, in the Samsung S4 mini, it shows: [spa_MEX_f00, tur_TUR_l02, jpn_JPN_f00, kor_KOR_f00, por_PRT_f00, eng_USA_f00, rus_RUS_f00, eng_GBR_f00, por_BRA_f00, fra_FRA_f00, por_BRA_l01, ita_ITA_f00, deu_DEU_f00, zho_CHN_f00, spa_ESP_f00]
   * locale: the locale to be used. You can pass, for example, spa_ESP or spa_ESP_f00
   * speech: the speech speed. The standard is 1.0. A lower value will decrease the speed, a higher value will increase it.
   * quit: quits the speech engine. 
   * </pre>
   * Note that the method call blocks until the text dictation finishes.
   * 
   * This is a sample to use Brazilian portuguese:
   * <pre>
   * Sound.fromText("locale:por_BRA,speech:0.75");
   * Sound.fromText("você é homem ou mulher?");
   * String ret = Sound.toText("title:Responda|timeout:500");
   * lab.setText("Answer: "+ret);
   * </pre>
   */
  public static void fromText(String text) {
  }
}