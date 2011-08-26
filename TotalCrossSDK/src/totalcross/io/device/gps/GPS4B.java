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



package totalcross.io.device.gps;

import totalcross.io.*;
import totalcross.io.device.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

import javax.microedition.location.*;

public class GPS4B extends Container implements Runnable
{
   public double[] location = new double[2];
   public Time lastFix = new Time();
   public double direction = -1.0f;
   public double velocity = -1.0f;
   public int satellites;   
   public String messageReceived;
   public static String LON_LOW_SIGNAL = "lon: low signal";
   public static String LAT_LOW_SIGNAL = "lat: low signal";
   public double pdop;

   private PortConnector sp;
   private Label[] text = new Label[5];
   private int readInterval = 2000;
   private byte[] buf = new byte[1];
   private TimerEvent timer;
   private StringBuffer sb = new StringBuffer(512);
   private boolean text4set;

   // blackberry exclusive fields
   private Location bbLocation;
   private LocationProvider provider;
   private Thread updateData; // gps data is updated on a separate thread to not block user interaction
   
   public static String getWinCEGPSCom()
   {
      return null;
   }

   public GPS4B() throws IOException
   {
      this(null, 2000);
   }

   public GPS4B(int readInterval) throws IOException
   {
      this(null, readInterval);
   }

   public GPS4B(PortConnector sp, int readInterval) throws IOException
   {
      if (sp == null) // use blackberry api
      {
         // let's start supporting only autonomous GPS.
         Criteria criteria = new Criteria();
         criteria.setCostAllowed(false);

         try
         {
            provider = LocationProvider.getInstance(criteria);
         }
         catch (Exception e)
         {
            throw new totalcross.io.IOException(e.getMessage());
         }
      }
      else
         this.sp = sp; // use the portconnector

      this.readInterval = readInterval;
      for (int i = text.length - 1; i >= 0; i--)
         text[i] = new Label("");
   }

   public void stop() throws IOException
   {
      removeTimer(timer);
      if (sp != null)
         sp.close();
      sp = null;
   }

   public double[] getLocation()
   {
      return location;
   }

   public double getLatitude()
   {
      return location[0];
   }

   public double getLongitude()
   {
      return location[1];
   }

   public double toCoordinate(String s, char dir)
   {
      double deg = 0;
      int i = s.indexOf('.');
      if (i >= 0)
      {
         int divider = 1;
         int size = s.length();
         for (int d = size - i - 1; d > 0; d--)
            divider *= 10;
         try
         {
            deg = (double) Convert.toInt(s.substring(0, i - 2)) +
                  ((double) Convert.toInt(s.substring(i - 2, i)) +
                  (double) Convert.toInt(s.substring(i + 1, size)) / divider) / 60;
         }
         catch (InvalidNumberException ine)
         {
            return 0;
         }
         if (dir == 'S' || dir == 'W')
            deg = -deg;
      }
      return deg;
   }

   public String toCoordinateGGMMSS(String coord, char dir) throws InvalidNumberException
   {
      try
      {
         String g, m, s;

         if (dir == 'E' || dir == 'W') // LONGITUDE
         {
            int gint = Convert.toInt(coord.substring(0, 3));
            g = gint + " ";
            m = coord.substring(3, 5) + " ";
            double temp = Convert.toDouble("0" + coord.substring(5, 10)) * 60;
            s = Convert.toString(temp, 4);
         }
         else
         {
            g = coord.substring(0, 2) + " "; // LATITUDE
            m = coord.substring(2, 4) + " ";
            double temp = Convert.toDouble("0" + coord.substring(4, 9)) * 60;
            s = Convert.toString(temp, 4);
         }
         return g + m + s + " " + dir;
      }
      catch (IndexOutOfBoundsException ioobe)
      {
         return "Err: " + coord + " - " + dir;
      }
   }

   // user interface
   public void initUI()
   {
      for (int i = 0; i < text.length; i++)
         add(text[i], LEFT, AFTER);

      text[0].setText("GPS Initialising");
      timer = addTimer(readInterval);
   }

   public void onEvent(Event e)
   {
      if (e.type == TimerEvent.TRIGGERED)
      {
         if (sp != null)
         {
            try
            {
               processInput();
            }
            catch (totalcross.io.IOException e1)
            {
               text[4].setText(e1.getMessage());
               try
               {
                  stop();
               }
               catch (IOException e2)
               {
               }
               e1.printStackTrace();
            }
         }
         else if (provider != null)
         {
            if (updateData == null || !updateData.isAlive())
            {
               updateData = new Thread(this);
               updateData.start();
            }
         }
         else
            lowSignal(null);
      }
   }
   
   private void lowSignal(String ex)
   {
      text[0].setText(LAT_LOW_SIGNAL);
      text[1].setText(LON_LOW_SIGNAL);
      for (int i = 2; i < text.length; i++)
         text[i].setText("");
      if (ex != null)
         text[text.length-1].setText(ex);
   }

   public int getPreferredWidth()
   {
      return FILL;
   }

   public int getPreferredHeight()
   {
      return fmH * text.length + insets.top + insets.bottom;
   }

   protected void onColorsChanged(boolean colorsChanged)
   {
      for (int i = 0; i < text.length; i++)
         text[i].setBackForeColors(getBackColor(), getForeColor());
   }

   protected void onFontChanged()
   {
      for (int i = 0; i < text.length; i++)
         text[i].setFont(font);
   }

   // private methods
   private void processInput() throws IOException
   {
      try
      {
         String message;
         while ((message = nextMessage()) != null)
         {
            messageReceived = message;
            String[] sp = Convert.tokenizeString(message, ',');
            if (sp[0].equals("$GPGGA"))
            {
               if ("".equals(sp[2]) || "".equals(sp[4]))
               {
                  text[0].setText("lat: low signal");
                  text[1].setText("lon: low signal");
               }
               else
               {
                  text[0].setText("lat: " + toCoordinateGGMMSS(sp[2], sp[3].charAt(0)));
                  text[1].setText("lon: " + toCoordinateGGMMSS(sp[4], sp[5].charAt(0)));
               }
               if (sp.length > 7 && sp[7].length() > 0)
                  text[2].setText("sat: " + sp[7]);
            }
            else if (sp[0].equals("$GPGLL"))
            {
               location[0] = toCoordinate(sp[1], sp[2].charAt(0));
               location[1] = toCoordinate(sp[3], sp[4].charAt(0));
               if (sp[5].length() > 0)
               {
                  lastFix.hour = Convert.toInt(sp[5].substring(0, 2));
                  lastFix.minute = Convert.toInt(sp[5].substring(2, 4));
                  lastFix.second = Convert.toInt(sp[5].substring(4, 6));
                  lastFix.millis = 0;
               }

               text[0].setText("lat: " + toCoordinateGGMMSS(sp[1], sp[2].charAt(0)));
               text[1].setText("lon: " + toCoordinateGGMMSS(sp[3], sp[4].charAt(0)));
               showLastFix(2);
               text[3].setText("valid: " + "A".equals(sp[6]));
            }
            else if (sp[0].equals("$GPRMC") && sp.length >= 8 && "A".equals(sp[2]))
            {
               location[0] = toCoordinate(sp[3], sp[4].charAt(0));
               location[1] = toCoordinate(sp[5], sp[6].charAt(0));
               if (sp[1].length() >= 6)
               {
                  lastFix.hour = Convert.toInt(sp[1].substring(0, 2));
                  lastFix.minute = Convert.toInt(sp[1].substring(2, 4));
                  lastFix.second = Convert.toInt(sp[1].substring(4, 6));
                  lastFix.millis = 0;
               }
               text[0].setText("lat: " + toCoordinateGGMMSS(sp[3], sp[4].charAt(0)));
               text[1].setText("lon: " + toCoordinateGGMMSS(sp[5], sp[6].charAt(0)));
               showLastFix(2);

               if (sp[7].length() > 0)
               {
                  velocity = Convert.toDouble(sp[7]); //knots
                  text[3].setText("speed: " + velocity);
               }
               if (sp[8].length() > 0)
               {
                  direction = Convert.toDouble(sp[8]); //degrees
                  text[4].setText("direction: " + direction);
                  text4set = true;
               }
            }
            else if (sp[0].equals("$GPGSA")) // guich@tc126_66
               try 
               {
                  pdop = sp.length > 15 ? Convert.toDouble(sp[15]) : 0;
               } catch (Exception e) {}

            if (!text4set)
            {
               text[4].setText(message);
               text[4].repaintNow();
            }
         }
      }
      catch (InvalidNumberException ine)
      {
         lowSignal("Error1: " + ine.getMessage());
      }
      catch (ArrayIndexOutOfBoundsException aioobe)
      {
         lowSignal("Error2: " + aioobe.getMessage());
      }
      Window.needsPaint = true;
   }

   private String nextMessage() throws IOException
   {
      StringBuffer sb = this.sb;
      byte[] buf = this.buf;
      String ret = null;
      int len = 0;

      while (sp.readBytes(buf, 0, 1) == 1)
      {
         int c = buf[0] & 0xFF;
         if (c == '\n') // stop only after \n
            break;
         else
         {
            sb.append((char) c);
            if (++len > 512 || c > 128) // just trash?
               throw new IOException("Invalid port: only trash was retrieved.");
         }
      }
      if (len > 0)
      {
         // cut everything after the *
         if (len > 3 && sb.charAt(len - 3) == '*')
            sb.setLength(len - 3);
         ret = sb.toString();
      }
      sb.setLength(0);
      return ret;
   }

   // blackberry only
   public void run()
   {
      try
      {
         bbLocation = provider.getLocation(-1);
         if (bbLocation.isValid())
         {
            QualifiedCoordinates coordinates = bbLocation.getQualifiedCoordinates();
            location[0] = coordinates.getLatitude();
            location[1] = coordinates.getLongitude();
            lastFix.update();
            velocity = (double) bbLocation.getSpeed() / 0.514; // converting speed from m/s to knots.
            direction = (double) bbLocation.getCourse();
            try 
            {
               messageReceived = bbLocation.getExtraInfo("application/X-jsr179-location-nmea");
               if (messageReceived != null && messageReceived.startsWith("$GPGSA")) // guich@tc126_66
               {
                  String[] toks = Convert.tokenizeString(messageReceived,',');
                  pdop = toks.length > 15 ? Convert.toDouble(toks[15]) : 0;
               }
            } catch (Exception e) {}

            text[0].setText("lat: " + location[0]);
            text[1].setText("lon: " + location[1]);
            showLastFix(2);
            text[3].setText("direction: " + direction);
            text[4].setText("speed: " + velocity);
            postPressedEvent(); // guich@tc126_67
         }
      }
      catch (LocationException e)
      {
         lowSignal(e.getMessage());
         if (provider.getState() == LocationProvider.OUT_OF_SERVICE)
         {
            try
            {
               stop();
            }
            catch (IOException e1)
            {
               e1.printStackTrace();
            }
         }
      }
      catch (Exception e)
      {
         lowSignal(e.getMessage());
         try
         {
            stop();
         }
         catch (IOException e1)
         {
         }
         e.printStackTrace();
      }
   }
   
   protected void finalize()
   {
      try
      {
         this.stop();
      }
      catch (IOException e)
      {
      }
   }

   private void showLastFix(int pos)
   {
      lastFix.hour += Settings.timeZone;
      text[pos].setText("fix: "+lastFix);
      lastFix.hour -= Settings.timeZone;
   }
}
