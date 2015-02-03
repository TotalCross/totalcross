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



package totalcross.ui.media;

public final class Sound4D
{
   private Sound4D()
   {
   }

   native public static void play(String filename);
   native public static void beep();
   native public static void tone(int freq, int duration);
   native public static void setEnabled(boolean on);
   public static void midiTone(int midiNoteNumber, int duration) // guich@300_33
   {
      int freq = (int)Math.round(440.0 * Math.pow (2, ((double)midiNoteNumber-69.0) / 12));
      tone(freq,duration);
   }
}