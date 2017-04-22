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



package tc.samples.app.watch;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

/**
 * This class draws a watch at the screen.
 */

public class Watch extends Container
{
   /** array of used Cities */
   public static String[] cities = new String[]
   {
      "Local Time", "Acapulco", "Amsterdan", "Athens", "Atlanta", "Baghdad", "Bangkok", "Barcelona",
      "Beijing", "Beirut", "Berlin", "Bogota", "Brasilia", "Bucharest", "Budapest", "Buenos Aires",
      "Cairo", "Caracas", "Chicago", "Copenhagen", "Detroit", "Dublin", "Frankfurt", "Geneva",
      "Guadalajara", "Hamburg", "Honolulu", "Havana", "Hong Kong", "Indianapolis", "Istambul", "Jerusalem",
      "La Paz", "Lima", "Lisbon", "London", "Los Angeles", "Madrid", "Mexico City", "Miami",
      "Milan", "Montevideo", "Montreal", "Moscow", "New Orleans", "New York", "Oslo", "Paris",
      "Philadelphia", "Rio de Janeiro", "Rome", "San Francisco", "Santiago", "Sao Paulo", "Seoul", "Shanghai",
      "Stockholm", "Sydney", "Tokyo", "Toronto", "Washington", "Zurich"
   };

   /** array of city timezones */
   public static int[] cities_tzdelta = new int[]
   {
      0, -6, 1, 2, -5, 3, 7, 1,
      8, 2, 1, -5, -3, 2, 1, -3,
      2, -4, -6, 1, -5, 0, 1, 1,
      -6, 1, -10, -5, 8, -5, 2, 2,
      -4, -5, 0, 0, -8, 1, -6, -5,
      1, -3, -5, 3, -6, -5, 1, 1,
      -5, -3, 1, -8, -4, -3, 9, 8,
      1, 10, 9, -5, -5, 1
   };

   private ComboBox            cbxCities;
   private Label               labTime;
   private int                 city_delta_localtime;
   private Graphics            myg;
   public int                  fillColor    = 0xCCFFFF;
   public int                  borderColor  = Color.BLACK;
   public int                  labColor     = 0x330099;
   public int                  pointerColor = Color.BLACK;
   public int                  markColor    = Color.BRIGHT;
   private static double       acos[];
   private static double       asin[];
   private static StringBuffer sb           = new StringBuffer(12);
   private int                 size, centerX, centerY;
   private Time time = new Time();

   public Watch()
   {
      cbxCities = new ComboBox(cities);
      labTime = new Label("",CENTER);
      for (int i = cities_tzdelta.length; --i >= 0;) cities_tzdelta[i] *= 60; // convert to minutes
   }

   public void initUI()
   {
      add(cbxCities, CENTER, TOP);
      add(labTime, LEFT, BOTTOM+1);
      if (acos == null) // create a lookup table for sin and cos - these tend to be slooow when in loop.
      {
         acos = new double[60];
         asin = new double[60];
         double tick = 2.0 * Math.PI / 60;
         double a = 0;
         for (int i = 0; i < 60; a += tick, i++)
         {
            acos[i] = -Math.cos(a);
            asin[i] = Math.sin(a);
         }
      }
      // avoid recompute everything all the time
      size = Math.min(width, height);
      centerX = width >> 1;
      centerY = height >> 1;
      // only add after the pre-computation
      addTimer(1000);
   }

   /** Changes the city to the given index of the cities array */
   public void setCity(int i)
   {
      cbxCities.setSelectedIndex(i);
      if (i != 0 && Settings.timeZoneMinutes != cities_tzdelta[i]) // if the selected city is in our timezone, keep it. Moreover the daylight saving will be correct
      {
         int delta_localtime_utc = Settings.timeZoneMinutes; // delta to apply to localtime to get the UTC time
         city_delta_localtime = cities_tzdelta[i] - delta_localtime_utc; // apply the city's TZ but we don't know the daylight saving information
      }
      else
         city_delta_localtime = 0;
   }

   public void onColorsChanged(boolean colorsChanged)
   {
      if (colorsChanged)
      {
         cbxCities.setBackColor(this.getBackColor());
         labTime.setBackForeColors(this.getBackColor(), labColor);
      }
   }

   private void drawTicks(Graphics g)
   {
      double b, c;
      int i;
      int centerX = this.centerX; // get a local reference to an instance variable
      int centerY = this.centerY;
      int s4 = size / 4;
      int s32 = (int) (size / 3.2);

      for (i = 0; i < 60; i++)
      {
         c = acos[i];
         b = asin[i];

         if ((i % 5) == 0)
            g.drawLine(centerX + (int) (b * s4), centerY + (int) (c * s4), centerX + (int) (b * s32), centerY + (int) (c * s32));
         else
            g.setPixel(centerX + (int) (b * s32), centerY + (int) (c * s32));
      }
   }

   private void drawMarks(Graphics g)
   {
      int hourHand = size / 6, minuteHand = size / 4, secondHand = minuteHand;
      time.update();
      int h = time.hour + city_delta_localtime;
      int m = time.minute;
      int s = time.second;
      g.foreColor = pointerColor;
      drawPointer(g, (h * 5) + (m / 12), hourHand);
      drawPointer(g, m, minuteHand);

      if (h < 0)
         h += 24;
      h = h % 24;

      drawPointer(g, s, secondHand);
      // draw the hour, saving memory by using a stringbuffer
      sb.setLength(0);
      if (h < 10)
         sb.append('0');
      sb.append(h).append(':');
      if (m < 10)
         sb.append('0');
      sb.append(m).append(':');
      if (s < 10)
         sb.append('0');
      sb.append(s);
      labTime.setText(sb.toString());
   }

   private void drawPointer(Graphics g, int angle, int len)
   {
      double b, c;
      while (angle < 0)
         angle += 60;
      if (angle >= 60)
         angle %= 60;
      c = acos[angle];
      b = asin[angle];

      int x2 = centerX + (int) (b * len);
      int y2 = centerY + (int) (c * len);

      g.drawLine(centerX, centerY, x2, y2);
   }

   private void drawClock(Graphics g)
   {
      int s1 = (int) (size / 2.95);
      int s2 = (int) (size / 3.05);
      g.foreColor = borderColor;
      g.drawCircle(centerX, centerY, size / 3);
      g.drawCircle(centerX, centerY, s1);
      g.backColor = fillColor;
      g.fillCircle(centerX, centerY, s2);
      drawTicks(g);
      drawMarks(g);
   }

   public void onPaint(Graphics g)
   {
      super.onPaint(g);
      drawClock(g);
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == cbxCities)
               setCity(cbxCities.getSelectedIndex());
            break;
         case TimerEvent.TRIGGERED:
            if (myg == null)
               myg = getGraphics();
            // repaint just the clock
            drawClock(myg);
            break;
      }
   }

   public void reposition()
   {
      setW = Settings.screenWidth/2; setH = Settings.screenHeight/2;
      super.reposition();
      centerX = width >> 1;
      centerY = height >> 1;
   }
}
