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

import javax.microedition.media.*;

public final class Sound4B
{
   private static boolean soundEnabled = true;

   private Sound4B()
   {
   }

   public static void beep()
   {
      tone(600, 100);
   }

   public static void tone(int freq, int duration)
   {
      if (soundEnabled)
      {
         int note = (int) Math.round((Math.log(freq / 8.176) * 17.31234049066755));
         if (note < 0)
            note = 0;
         else if (note > 127)
            note = 127;

         try
         {
            Manager.playTone(note, duration, 100);
         }
         catch (Exception e) {}
      }
   }

   public static void setEnabled(boolean on)
   {
      soundEnabled = on;
   }

   public static void midiTone(int midiNoteNumber, int duration) // guich@300_33
   {
      int freq = (int)Math.round(440.0 * Math.pow (2, ((double)midiNoteNumber-69.0) / 12));
      tone(freq,duration);
   }
}