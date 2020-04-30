// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.media;

public final class Sound4D {
  private Sound4D() {
  }

  native public static void play(String filename);

  native public static void beep();

  native public static void tone(int freq, int duration);

  native public static void setEnabled(boolean on);

  public static void midiTone(int midiNoteNumber, int duration) // guich@300_33
  {
    int freq = (int) Math.round(440.0 * Math.pow(2, ((double) midiNoteNumber - 69.0) / 12));
    tone(freq, duration);
  }

  native public static String toText(String params);

  native public static void fromText(String text);
}